package it.cf.bloodhoud.client.android.model;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cf.bloodhoud.client.android.model.Call.CallDirection;
import it.cf.bloodhoud.client.android.model.Call.CallState;
import it.cf.bloodhoud.client.android.model.Sms.SmsDirection;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;

public class CallFactory
	{
		static private final Logger LOG = LoggerFactory.getLogger(CallFactory.class);
		private final static CallFactory singleton = new CallFactory();

		private static Call call = null;

		private CallFactory()
			{

			}

		static public CallFactory getInstance()
			{
				return singleton;
			}
		
		
		public Call call(int localId, String phoneNumber, String nameContact,
				long timestampStartCall, long timestampEndCall,
				CallDirection direction, CallState state) throws Exception
		{
			Call call;
			switch (direction)
			{
			case OUTGOING:
				call = new OutgoingCall(localId, phoneNumber, nameContact, timestampStartCall, timestampEndCall, direction, state);
				break;
			case INCOMING:
				call = new IncomingCall(localId, phoneNumber, nameContact, timestampStartCall, timestampEndCall, direction, state);
				break;
			default:
				throw new Exception("La direzione deve essere incoming o outgoing");
			}
			return call;
		}
		
		public Call call(int localId, String phoneNumber, String nameContact,
				long timestampStartCall, long timestampEndCall,
				String direction, String state) throws Exception
		{
			CallDirection callDir = getCallDirection(direction);
			CallState callState = getCallState(state);
			
			return call(localId, phoneNumber, nameContact, timestampStartCall, timestampEndCall, callDir, callState);
		}		

		public Call createNewCall(CallDirection direction, Intent intent) throws Exception
			{
				String callingPhoneNumber = getCallingPhoneNumber(intent);
				switch (direction)
					{
					case OUTGOING:
						call = new OutgoingCall(callingPhoneNumber);
						LOG.info("Outgoing call created: {}", call.toString());
						break;
					case INCOMING:
						call = new IncomingCall(callingPhoneNumber);
						LOG.info("Incoming call created: {}", call.toString());
						break;
					default:
						throw new Exception("La direzione deve essere incoming o outgoing");
					}
				return call;
			}

		public Call getCall(int telephonyCallState, Intent intent) throws Exception
			{
				if (call == null)
					{
						throw new Exception("Chiamare prima il metodo createNewCall()");
					}

				switch (telephonyCallState)
					{
					case TelephonyManager.CALL_STATE_RINGING:
						{
							// il telefono squilla, dovrei averlo gi� gestito nella creazione della chiamata
							LOG.debug("Call already created: {}", call.toString());
							break;
						}
					case TelephonyManager.CALL_STATE_OFFHOOK:
						{
							// la chiamata viene accettata
							if (call.getDirection() == CallDirection.OUTGOING)
								{
									((OutgoingCall) call).accepted();
								}
							else
								{
									((IncomingCall) call).accepted();
								}
							LOG.info("Call accepted: {}", call.toString());
							break;
						}
					case TelephonyManager.CALL_STATE_IDLE:
						{
							/*
							 * la chiamata viene:
							 * - conclusa, se il precedente stato � accettata
							 * - rifiutata, se il precedente stato � ringing, ossia appena creata
							 */
							if (call.getDirection() == CallDirection.OUTGOING)
								{
									OutgoingCall outgoinhCall = ((OutgoingCall) call);
									if (outgoinhCall.getState() == CallState.ACCEPTED)
										{
											outgoinhCall.finished();
										}
									if (outgoinhCall.getState() == CallState.RINGING)
										{
											outgoinhCall.refused();
										}
								}
							else
								{
									IncomingCall incomingCall = ((IncomingCall) call);
									if (incomingCall.getState() == CallState.ACCEPTED)
										{
											incomingCall.finished();
										}
									if (incomingCall.getState() == CallState.RINGING)
										{
											incomingCall.refused();
										}
								}
							LOG.info("Call finished or refused: {}", call.toString());
							break;
						}
					default:
						{
							LOG.error("Inconsistent situation");
							break;
						}
					}
				return call;
			}

		private static String getCallingPhoneNumber(Intent intent)
			{
				String phoneNumber = "UNKNOW";
				if (intent != null && intent.getExtras() != null)
					{
						Bundle bundle = intent.getExtras();
						String state = bundle.getString(TelephonyManager.EXTRA_STATE);
						if ((state != null) && (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)))
							{
								phoneNumber = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
							}
						else if (state == null)
							{
								phoneNumber = bundle.getString(Intent.EXTRA_PHONE_NUMBER);
								// Here: do something with the number
							}
					}
				else
					{
						LOG.error("Intent is null");
					}
				return phoneNumber;
			}
		
		private CallDirection getCallDirection(String callDirection){
			CallDirection dir = CallDirection.INCOMING;
			if (StringUtils.equalsIgnoreCase(callDirection, CallDirection.OUTGOING.name())){
				dir= CallDirection.OUTGOING;
			}
			return dir;
		}		
		
		private CallState getCallState(String callState){
			CallState state = CallState.ACCEPTED;
			if (StringUtils.equalsIgnoreCase(callState, CallState.FINISHED.name())){
				state = CallState.FINISHED;
			}
			else if (StringUtils.equalsIgnoreCase(callState, CallState.REFUSED.name())){
				state = CallState.REFUSED;
			}
			else if (StringUtils.equalsIgnoreCase(callState, CallState.RINGING.name())){
				state = CallState.RINGING;
			}
			else if (StringUtils.equalsIgnoreCase(callState, CallState.UNKNOW.name())){
				state = CallState.UNKNOW;
			}
			return state;
		}

	}
