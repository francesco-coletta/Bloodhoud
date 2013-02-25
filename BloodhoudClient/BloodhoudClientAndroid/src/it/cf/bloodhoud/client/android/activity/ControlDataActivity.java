package it.cf.bloodhoud.client.android.activity;

import it.cf.bloodhoud.client.android.App;
import it.cf.bloodhoud.client.android.R;
import it.cf.bloodhoud.client.android.Utils;
import it.cf.bloodhoud.client.android.model.Call;
import it.cf.bloodhoud.client.android.model.ContactManager;
import it.cf.bloodhoud.client.android.model.Sms;
import it.cf.bloodhoud.client.android.model.Sms.SmsDirection;
import it.cf.bloodhoud.client.android.model.SmsFactory;
import it.cf.bloodhoud.client.android.serviceApp.RepositoryLocalRead;
import it.cf.bloodhoud.client.android.serviceApp.RepositoryLocalSQLLite;
import it.cf.bloodhoud.client.android.serviceApp.RepositoryLocalWrite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ControlDataActivity extends Activity implements OnClickListener
    {
        static private final Logger LOG                 = LoggerFactory.getLogger(ControlDataActivity.class);

        static private final String FILE_NAME_SMS_XML   = "sms.xml";
        static private final String FILE_NAME_CALL_XML  = "call.xml";
        static private final String FILE_NAME_SMS_JSON  = "sms.json";
        static private final String FILE_NAME_CALL_JSON = "call.json";

        private ObjectWriter        writerObjectInJson  = new ObjectMapper().writerWithDefaultPrettyPrinter();

        @Override
        protected void onCreate(Bundle savedInstanceState)
            {
                super.onCreate(savedInstanceState);
                this.setContentView(R.layout.activity_control_data);

                Button buttonExport = (Button) findViewById(R.controlActivity.buttonExport);
                buttonExport.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                            {
                                new ExportDataTask(v.getContext()).execute();
                            }
                    });

                Button buttonClearLog = (Button) findViewById(R.controlActivity.buttonClear);
                buttonClearLog.setOnClickListener(this);

                Button buttonCancel = (Button) findViewById(R.controlActivity.buttonCancel);
                buttonCancel.setOnClickListener(this);

                Button buttonExportDb = (Button) findViewById(R.controlActivity.buttonExportDB);
                buttonExportDb.setOnClickListener(this);

                Button buttonSendOldData = (Button) findViewById(R.controlActivity.buttonSendOldData);
                buttonSendOldData.setOnClickListener(this);

                Button buttonSendAgaiDataToServer = (Button) findViewById(R.controlActivity.buttonSendAgaiDataToServer);
                buttonSendAgaiDataToServer.setOnClickListener(this);
            }

        @Override
        public void onClick(View v)
            {
                TextView messaggioEsitoTask = (TextView) findViewById(R.controlActivity.labelMessage);
                messaggioEsitoTask.setText("");

                if (v.getId() == R.controlActivity.buttonClear)
                    {
                        LOG.debug("TODO: clear log");
                        Toast toast = Toast.makeText(this, "TODO: clear log", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                else if (v.getId() == R.controlActivity.buttonCancel)
                    {
                        this.finish();
                    }
                else if (v.getId() == R.controlActivity.buttonExportDB)
                    {
                        new CopyDbTask(v.getContext()).execute();
                    }
                else if (v.getId() == R.controlActivity.buttonSendAgaiDataToServer)
                    {
                        new ConfigureDataForSendItAgainDataToServerTask(v.getContext()).execute();
                    }
                else if (v.getId() == R.controlActivity.buttonSendOldData)
                    {
                        try
                            {
                                new CopyOldDataIntoDatabaseForSendToServerTask(v.getContext()).execute();
                            }
                        catch (Exception e)
                            {
                                String messaggio = "Errore nella copia dei vecchi sms: " + e.getMessage();
                                LOG.error(messaggio);
                                Toast toast = Toast.makeText(this, messaggio, Toast.LENGTH_SHORT);
                                toast.show();
                                messaggioEsitoTask.setText(messaggio);
                            }
                    }
            }

        // Private class for task in background

        // export sms, call in xml and json format
        private class ExportDataTask extends AsyncTask<String, Integer, String>
            {
                private final Context context;
                private Button        buttonExportData;

                public ExportDataTask(Context context)
                    {
                        super();
                        this.context = context;
                        this.buttonExportData = (Button) findViewById(R.controlActivity.buttonExport);
                    }

                @Override
                protected void onPreExecute()
                    {
                        this.buttonExportData.setEnabled(false);
                        super.onPreExecute();
                    }

                @Override
                protected String doInBackground(String... arg0)
                    {
                        String messaggio = "";
                        LOG.debug("Export data Started");
                        if (Utils.isExternalStorageAvailableAndWriteable())
                            {
                                Utils.cleanChacheDirIntoExternalStorage(context);
                                exportData();
                                copyExportedDataIntoExternalStorage(context);
                                messaggio = "Export eseguito";
                            }
                        else
                            {
                                messaggio = "External storage isn't writable. Skip export";
                                LOG.warn("External storage isn't writable. Skip export");
                            }
                        return messaggio;
                    }

                @Override
                protected void onPostExecute(String result)
                    {
                        this.buttonExportData.setEnabled(true);
                        Toast toast = Toast.makeText(this.context, result, Toast.LENGTH_SHORT);
                        toast.show();

                        TextView messaggioEsitoTask = (TextView) findViewById(R.controlActivity.labelMessage);
                        messaggioEsitoTask.setText(result);
                    }

                private void copyExportedDataIntoExternalStorage(Context context)
                    {
                        try
                            {
                                LOG.debug("Copy file to external storage");
                                String fromPath = context.getFilesDir().getCanonicalPath() + File.separator;
                                String toPath = context.getExternalCacheDir() + File.separator;
                                Utils.copyFiles(fromPath, toPath);
                            }
                        catch (IOException e)
                            {
                                LOG.error(e.getMessage());
                            }
                    }

                private void exportData()
                    {
                        exportSms();
                        exportCall();
                    }

                private void exportSms()
                    {
                        RepositoryLocalRead repo = RepositoryLocalSQLLite.getRepository(context);
                        List<Sms> smss = repo.getSms();
                        LOG.debug("Nel db sono presenti {} sms", smss.size());

                        exportSmsToFileXml(smss);
                        exportSmsToFileJson(smss);

                        LOG.debug("Sono stati esportai gli sms");
                    }

                private void exportCall()
                    {
                        RepositoryLocalRead repo = RepositoryLocalSQLLite.getRepository(context);
                        List<Call> calls = repo.getCall();
                        LOG.debug("Nel db sono presenti {} call", calls.size());

                        exportCallToFileXml(calls);
                        exportCallToFileJson(calls);
                    }

                private void exportCallToFileXml(List<Call> calls)
                    {
                        if (calls != null && calls.size() > 0)
                            {

                                FileOutputStream callOutputStream = null;
                                try
                                    {
                                        callOutputStream = context.openFileOutput(FILE_NAME_CALL_XML, Context.MODE_PRIVATE);
                                        Serializer serializer = new Persister();
                                        for (Call call : calls)
                                            {
                                                // LOG.debug("Export call {}", call.toString());
                                                serializer.write(call, callOutputStream);
                                                callOutputStream.write("\n".getBytes());
                                            }
                                        callOutputStream.close();
                                    }
                                catch (Exception e)
                                    {
                                        LOG.error(e.getMessage());
                                    }
                            }
                    }

                private void exportCallToFileJson(List<Call> calls)
                    {
                        if (calls != null && calls.size() > 0)
                            {
                                FileOutputStream callOutputStream = null;
                                try
                                    {
                                        callOutputStream = context.openFileOutput(FILE_NAME_CALL_JSON, Context.MODE_PRIVATE);
                                        for (Call call : calls)
                                            {
                                                callOutputStream.write(writerObjectInJson.writeValueAsString(call).getBytes());
                                                callOutputStream.write("\n".getBytes());
                                            }
                                        callOutputStream.close();
                                    }
                                catch (Exception e)
                                    {
                                        LOG.error(e.getMessage());
                                    }
                            }

                    }

                private void exportSmsToFileXml(List<Sms> smss)
                    {
                        if (smss != null && smss.size() > 0)
                            {
                                FileOutputStream smsOutputStream = null;
                                try
                                    {
                                        smsOutputStream = context.openFileOutput(FILE_NAME_SMS_XML, Context.MODE_PRIVATE);
                                        Serializer serializer = new Persister();
                                        for (Sms sms : smss)
                                            {
                                                serializer.write(sms, smsOutputStream);
                                                smsOutputStream.write("\n".getBytes());
                                            }
                                        smsOutputStream.close();
                                    }
                                catch (Exception e)
                                    {
                                        LOG.error(e.getMessage());
                                    }
                            }
                    }

                private void exportSmsToFileJson(List<Sms> smss)
                    {
                        if (smss != null && smss.size() > 0)
                            {
                                FileOutputStream smsOutputStream = null;
                                try
                                    {
                                        smsOutputStream = context.openFileOutput(FILE_NAME_SMS_JSON, Context.MODE_PRIVATE);
                                        for (Sms sms : smss)
                                            {
                                                smsOutputStream.write(writerObjectInJson.writeValueAsString(sms).getBytes());
                                                smsOutputStream.write("\n".getBytes());
                                            }
                                        smsOutputStream.close();
                                    }
                                catch (Exception e)
                                    {
                                        LOG.error(e.getMessage());
                                    }
                            }
                    }

            }// ExportTask Class

        // export DB file
        private class CopyDbTask extends AsyncTask<String, Integer, String>
            {
                private final Context context;

                public CopyDbTask(Context context)
                    {
                        super();
                        this.context = context;
                    }

                @Override
                protected String doInBackground(String... arg0)
                    {
                        String messaggio = "";
                        LOG.debug("Copy DB started");
                        if (Utils.isExternalStorageAvailableAndWriteable())
                            {
                                Utils.cleanChacheDirIntoExternalStorage(context);
                                LOG.debug("Copy DB to external storage");
                                try
                                    {
                                        String from = context.getDatabasePath(App.DATABASE_NAME).getCanonicalPath();
                                        String to = context.getExternalCacheDir() + File.separator + App.DATABASE_NAME;
                                        Utils.copy(from, to);
                                        messaggio = "Copy Db ok";
                                        LOG.debug(messaggio);
                                    }
                                catch (IOException e)
                                    {
                                        messaggio = "Problem during DB copy: " + e.getMessage();
                                        LOG.error(messaggio);
                                    }
                            }
                        else
                            {
                                messaggio = "External storage isn't writable. Skip export";
                                LOG.warn(messaggio);
                            }
                        return messaggio;
                    }

                @Override
                protected void onPostExecute(String result)
                    {
                        Toast toast = Toast.makeText(this.context, result, Toast.LENGTH_SHORT);
                        toast.show();

                        TextView messaggioEsitoTask = (TextView) findViewById(R.controlActivity.labelMessage);
                        messaggioEsitoTask.setText(result);
                    }

            }// CopyDbTask Class

        // Send again sms, call to server
        private class ConfigureDataForSendItAgainDataToServerTask extends AsyncTask<String, Integer, String>
            {
                private final Context context;
                private Button        buttonSendAgain;

                public ConfigureDataForSendItAgainDataToServerTask(Context context)
                    {
                        super();
                        this.context = context;
                        buttonSendAgain = (Button) findViewById(R.controlActivity.buttonSendAgaiDataToServer);
                    }

                @Override
                protected void onPreExecute()
                    {
                        buttonSendAgain.setEnabled(false);
                        super.onPreExecute();
                    }

                @Override
                protected String doInBackground(String... arg0)
                    {
                        String messagge = "Flag send to server has been reseted";

                        RepositoryLocalWrite repo = RepositoryLocalSQLLite.getRepository(context);
                        repo.markDataForSendedAgainToServer();
                        LOG.debug(messagge);

                        return messagge;
                    }

                @Override
                protected void onPostExecute(String result)
                    {
                        buttonSendAgain.setEnabled(true);
                        Toast toast = Toast.makeText(this.context, result, Toast.LENGTH_SHORT);
                        toast.show();

                        TextView messaggioEsitoTask = (TextView) findViewById(R.controlActivity.labelMessage);
                        messaggioEsitoTask.setText(result);

                        super.onPostExecute(result);
                    }
            }// ConfigureDataForSendItAgainDataToServerTask Class

        // Copy into local database old incoming/outgoing sms and call
        private class CopyOldDataIntoDatabaseForSendToServerTask extends AsyncTask<String, String, Integer>
            {
                private final Context          context;
                private TextView               messaggioEsitoTask;
                private Button                 buttonCopyOldDataIntoDb;

                private ContactManager         contactManager = null;
                private RepositoryLocalSQLLite repo           = null;

                public CopyOldDataIntoDatabaseForSendToServerTask(Context context) throws Exception
                    {
                        super();
                        this.context = context;
                        messaggioEsitoTask = (TextView) findViewById(R.controlActivity.labelMessage);
                        buttonCopyOldDataIntoDb = (Button) findViewById(R.controlActivity.buttonSendOldData);

                        contactManager = ContactManager.getInstance(context);
                        repo = RepositoryLocalSQLLite.getRepository(context);
                    }

                @Override
                protected void onPreExecute()
                    {
                        String messagge = "Inizio copia nel db locale dei vecchi SMS.";
                        LOG.info(messagge);

                        messaggioEsitoTask.setText(messagge);
                        buttonCopyOldDataIntoDb.setEnabled(false);
                    }

                @Override
                protected void onProgressUpdate(String... values)
                    {
                        StringBuilder messagge = new StringBuilder();
                        if (values.length == 1)
                            {
                                messagge.append(values[0]);
                            }
                        else if (values.length == 2)
                            {
                                messagge.append("Creato SMS ").append(values[0]).append("/").append(values[1]);
                            }
                        else if (values.length == 3)
                            {
                                messagge.append("Creato SMS ").append(values[0]).append("/").append(values[1]).append(": ").append(values[2]);
                            }
                        LOG.debug(messagge.toString());
                        messaggioEsitoTask.setText(messagge.toString());
                    }

                @Override
                protected Integer doInBackground(String... arg0)
                    {
                        Integer numSmsWriteIntoLocalDb = 0;

                        Cursor cursor = Utils.getSmsAllCursor(context);
                        if ((cursor != null) && (cursor.getCount() > 0))
                            {
                                Integer numSmsTot = new Integer(cursor.getCount());

                                publishProgress("Num sms recuperati = " + numSmsTot.toString());
                                LOG.debug("Sono presenti {} SMS", numSmsTot);

                                while (cursor.moveToNext())
                                    {
                                        numSmsWriteIntoLocalDb++;
                                        // Gets the SMS information
                                        String address = cursor.getString(cursor.getColumnIndex("address"));
                                        // String person = cursor.getString(cursor.getColumnIndex("person"));
                                        long date = cursor.getLong(cursor.getColumnIndex("date"));
                                        // String protocol = cursor.getString(cursor.getColumnIndex("protocol"));
                                        // String read = cursor.getString(cursor.getColumnIndex("read"));
                                        // String status = cursor.getString(cursor.getColumnIndex("status"));
                                        int type = cursor.getInt(cursor.getColumnIndex("type"));
                                        // String subject = cursor.getString(cursor.getColumnIndex("subject"));
                                        String body = cursor.getString(cursor.getColumnIndex("body"));

                                        // MESSAGE_TYPE_INBOX = 1;
                                        // MESSAGE_TYPE_SENT = 2;
                                        Sms sms;
                                        SmsDirection smsDirection = null;
                                        if (type == 1)
                                            {
                                                LOG.debug("Incoming SMS");
                                                smsDirection = SmsDirection.Incoming;
                                            }
                                        else if (type == 2)
                                            {
                                                LOG.debug("Outgoing SMS (sent)");
                                                smsDirection = SmsDirection.Outgoing;
                                            }
                                        else
                                            {
                                                LOG.debug("Other type SMS {}", String.valueOf(type));
                                            }
                                        if (smsDirection != null)
                                            {
                                                sms = SmsFactory.sms(0, smsDirection, address, date, body);
                                                try
                                                    {
                                                        if (contactManager != null)
                                                            {
                                                                publishProgress(numSmsWriteIntoLocalDb.toString(), numSmsTot.toString(), "search contact name ...");
                                                                sms.setNameContact(contactManager.getContactNameFromNumber(sms.getPhoneNumber()));
                                                                publishProgress(numSmsWriteIntoLocalDb.toString(), numSmsTot.toString(), "contact name ok");
                                                            }
                                                    }
                                                catch (Exception e)
                                                    {
                                                        LOG.warn("Errore nel recupero del nome del contatto {}", e.getMessage());
                                                    }
                                                publishProgress(numSmsWriteIntoLocalDb.toString(), numSmsTot.toString(), "save sms ...");

                                                repo.writeSms(sms);

                                                publishProgress(numSmsWriteIntoLocalDb.toString(), numSmsTot.toString(), "sms saved");
                                                LOG.debug("{} created sms: {} ", String.valueOf(numSmsWriteIntoLocalDb), sms.toString());

                                                publishProgress(numSmsWriteIntoLocalDb.toString(), numSmsTot.toString());

                                                SystemClock.sleep(100);
                                            }
                                    }
                            }
                        else
                            {
                                LOG.debug("Non ci sono SMS");
                            }

                        return numSmsWriteIntoLocalDb;
                    }

                @Override
                protected void onPostExecute(Integer result)
                    {
                        buttonCopyOldDataIntoDb.setEnabled(true);
                        String messagge = "Fine copia nel db locale dei vecchi SMS. Totale sms copiati " + result;
                        LOG.info(messagge);

                        Toast toast = Toast.makeText(this.context, messagge, Toast.LENGTH_SHORT);
                        toast.show();

                        TextView messaggioEsitoTask = (TextView) findViewById(R.controlActivity.labelMessage);
                        messaggioEsitoTask.setText(messagge);
                    }

            }// ExportDbTask Class

    }
