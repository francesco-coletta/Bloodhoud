package it.cf.bloodhoud.client.android.model;

import it.cf.bloodhoud.client.android.model.Sms.SmsDirection;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.telephony.SmsMessage;

public class SmsFactory
	{
		static private final Logger LOG = LoggerFactory.getLogger(SmsFactory.class);

		static public Sms sms(int localId, SmsDirection direction, String phoneNumber, long timestamp, String text)
		{
			return new Sms(localId, direction, StringUtils.trimToEmpty(phoneNumber), timestamp, StringUtils.trimToEmpty(text));
		}
		
		static public Sms sms(int localId, String direction, String phoneNumber, long timestamp, String text)
		{
			SmsDirection dir = Sms.SmsDirection.Incoming;
			if (StringUtils.equalsIgnoreCase(direction, Sms.SmsDirection.Outgoing.name())){
				dir = Sms.SmsDirection.Outgoing;
			}
			return new Sms(localId, dir, StringUtils.trimToEmpty(phoneNumber), timestamp, StringUtils.trimToEmpty(text));
		}
		
		
		static public Sms newIncomingSmsFromSmsMessage(SmsMessage smsMessage)
			{
				Sms sms;
				if (smsMessage == null)
					{
						LOG.error("smsMessage == null");
						sms = new Sms(SmsDirection.Incoming, "", 0, "");
					}
				else
					{
						sms = new Sms(SmsDirection.Incoming, smsMessage.getOriginatingAddress(), smsMessage.getTimestampMillis(), smsMessage.getMessageBody());
					}
				LOG.info("New SMS incoming created: " + sms.toString());

				return sms;
			}

		public static Sms newIncomingSmsFromPdu(byte[] pdu)
			{
				Sms sms;
				if (pdu.length > 0)
					{
						sms = newIncomingSmsFromSmsMessage(SmsMessage.createFromPdu(pdu));
					}
				else
					{
						sms = newIncomingSmsFromSmsMessage(null);
					}
				return sms;
			}

		public static Sms newOutgoingSms(String fromPhoneNumber, long timestamp, String text)
			{
				Sms sms = new Sms(SmsDirection.Outgoing, fromPhoneNumber, timestamp, text);
				LOG.info("NEW SMS outgoing created: " + sms.toString());
				return sms;
			}

	}
