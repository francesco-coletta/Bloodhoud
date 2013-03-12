package it.cf.bloodhoud.client.android.receiver;

import it.cf.bloodhoud.client.android.model.ContactManager;
import it.cf.bloodhoud.client.android.model.Sms;
import it.cf.bloodhoud.client.android.model.SmsFactory;
import it.cf.bloodhoud.client.android.serviceApp.RepositoryLocalSQLLite;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class IncomingSmsListener extends BroadcastReceiver
    {
        static private final Logger LOG                 = LoggerFactory.getLogger(IncomingSmsListener.class);
        private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

        @Override
        public void onReceive(Context context, Intent intent)
            {

                try
                    {
                        LOG.debug(">>>>>>>>>> CHECK INCOMING SMS");
                        onReceiveSmsIncoming(context, intent);
                        LOG.debug("CHECK INCOMING SMS <<<<<<<<<<");
                    }
                catch (Exception e)
                    {
                        LOG.error(e.getMessage());
                    }
            }

        private void onReceiveSmsIncoming(Context context, Intent intent) throws Exception
            {
                LOG.debug("Intent received: " + intent.getAction());

                // verifico il tipo di intent, ossia azione
                if (isSmsReceived(intent))
                    {
                        LOG.debug("SMS Message Received.");
                        List<Sms> messages = getIncomingSms(intent);
                        LOG.debug("Num SMS Message Received = {}", String.valueOf(messages.size()));

                        // valorizzo il nome del contatto associato al numero di telefono da cui giunge l'SMS
                        setCantactName(ContactManager.getInstance(context), messages);
                        LOG.debug("Impostati contact name negli sms");
                        
                        writeSmsIntoDababase(RepositoryLocalSQLLite.getRepository(context), messages);
                        LOG.debug("Sms salvati nel DB locale");
                    }
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
        
        
        private boolean isSmsReceived(Intent intent)
            {
                return (intent != null && intent.getAction() != null && ACTION_SMS_RECEIVED.compareToIgnoreCase(intent.getAction()) == 0);
            }

        private List<Sms> getIncomingSms(Intent intent)
            {
                List<Sms> incomingSms = new ArrayList<Sms>();
                if (intent != null)
                    {
                        Bundle bundle = intent.getExtras();
                        if (bundle != null)
                            {
                                // ---retrieve the SMS message received---
                                Sms sms;
                                Object[] pduArray = (Object[]) bundle.get("pdus");
                                for (int i = 0; i < pduArray.length; i++)
                                    {
                                        sms = SmsFactory.newIncomingSmsFromPdu((byte[]) pduArray[i]);
                                        incomingSms.add(sms);
                                    }
                            }
                    }
                return incomingSms;
            }

    }
