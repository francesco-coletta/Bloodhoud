package it.cf.bloodhoud.client.android.receiver;

import it.cf.bloodhoud.client.android.model.Call;
import it.cf.bloodhoud.client.android.model.Call.CallDirection;
import it.cf.bloodhoud.client.android.model.CallFactory;
import it.cf.bloodhoud.client.android.model.ContactManager;
import it.cf.bloodhoud.client.android.serviceApp.RepositoryLocalSQLLite;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class IncomingOutgoingCallListener extends BroadcastReceiver
    {
        static private final Logger LOG                      = LoggerFactory.getLogger(IncomingOutgoingCallListener.class);
        private static final String ACTION_NEW_OUTGOING_CALL = "android.intent.action.NEW_OUTGOING_CALL";

        @Override
        public void onReceive(Context context, Intent intent)
            {
                LOG.debug(">>>>>>>>>> CHECK INCOMING/OUTGOING CALL");
                LOG.debug("Intent received: {}", intent.getAction());

                Call call;
                try
                    {
                        if (isStartedNewOutgoingCall(intent))
                            {
                                call = CallFactory.getInstance().createNewCall(CallDirection.OUTGOING, intent);
                                LOG.debug("Started New Outgoing Call");
                            }
                        else if (isStartedNewIncomingCall(context))
                            {
                                call = CallFactory.getInstance().createNewCall(CallDirection.INCOMING, intent);
                                LOG.debug("Started New Incoming Call");
                            }
                        else
                            {
                                TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                                int callState = telManager.getCallState();
                                call = CallFactory.getInstance().getCall(callState, intent);
                                LOG.debug("Worflow previous started Call with state {}", callState);
                            }

                        if (StringUtils.isBlank(call.getNameContact()))
                            {
                                ContactManager contactManager = ContactManager.getInstance(context);
                                call.setNameContact(contactManager.getContactNameFromNumber(call.getPhoneNumber()));
                            }

                        try
                            {
                                // RepositoryLocal repo = new RepositoryLocalFile(context);
                                RepositoryLocalSQLLite repo = RepositoryLocalSQLLite.getRepository(context);
                                repo.writeCall(call);
                            }
                        catch (Exception e)
                            {
                                LOG.error(e.getMessage());
                            }

                    }
                catch (Exception e)
                    {
                        LOG.error(e.getMessage());
                    }
                LOG.debug("CHECK INCOMING/OUTGOING CALL <<<<<<<<<<");
            }

        private boolean isStartedNewOutgoingCall(Intent intent)
            {
                return (intent != null && intent.getAction() != null && ACTION_NEW_OUTGOING_CALL.compareToIgnoreCase(intent.getAction()) == 0);
            }

        private boolean isStartedNewIncomingCall(Context context)
            {
                TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                int callState = telManager.getCallState();
                return (callState == TelephonyManager.CALL_STATE_RINGING);
            }

    }
