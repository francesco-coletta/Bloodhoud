package it.cf.bloodhoud.client.android.model;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class Sms
	{

		@Element
		private final String phoneNumber;
		@Element
		private String nameContact = "UNKNOWN";
		@Attribute
		private final long timestamp;
		@Attribute
		private final String timestampFormatted;
		@Element
		private final String text;
		@Element
		private final SmsDirection direction;

		public enum SmsDirection
			{
				Incoming, Outgoing
			}

		public Sms(SmsDirection directionType, String phoneNumber,
		           long timestamp, String text)
			{
				super();
				this.phoneNumber = StringUtils.trimToEmpty(phoneNumber);
				this.timestamp = timestamp;
				this.text = StringUtils.trimToEmpty(text);
				this.direction = directionType;
				this.timestampFormatted = Utils.formatDatetime(timestamp);
			}

		public String getPhoneNumber()
			{
				return phoneNumber;
			}

		public long getTimestamp()
			{
				return timestamp;
			}
		
		public String getTimestampFormatted(){
			return this.timestampFormatted ;
		}

		public String getText()
			{
				return text;
			}

		public SmsDirection getDirection()
			{
				return direction;
			}

		public String getNameContact()
			{
				return nameContact;
			}

		public void setNameContact(String nameContact)
			{
				if (StringUtils.isBlank(nameContact) == false)
					{
						this.nameContact = nameContact;
					}
			}

		@Override
		public String toString()
			{
				ToStringBuilder toString = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
				toString.append("Direction", direction.name());
				toString.append(" Time", Utils.formatDatetime(timestamp));
				toString.append(" Phone", phoneNumber);
				toString.append(" Contact", nameContact);
				toString.append(" Text", text);
				return toString.build().concat("\n");
			}

	}
