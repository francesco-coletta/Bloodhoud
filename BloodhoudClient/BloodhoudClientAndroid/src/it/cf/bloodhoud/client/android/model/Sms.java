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
        private int                localId               = 0;
        @Element
        private final String       phoneNumber;
        @Element
        private String             nameContact           = "UNKNOWN";
        @Element
        private final long         timestamp;
        @Element
        private final String       timestampFormatted;
        @Element
        private final String       text;
        @Element
        private final SmsDirection direction;
        @Element
        private int                serverSyncro          = 0;
        @Element
        private String               serverId              = "";
        @Element
        private long               serverSyncroTimestamp = 0;

        public enum SmsDirection
            {
                Incoming, Outgoing
            }

        public Sms(int localId, SmsDirection directionType, String phoneNumber, long timestamp, String text)
            {
                this(directionType, phoneNumber, timestamp, text);
                this.localId = localId;
            }

        
        public Sms(SmsDirection directionType, String phoneNumber, long timestamp, String text)
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

        public String getTimestampFormatted()
            {
                return this.timestampFormatted;
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

        public int getServerSyncro()
            {
                return serverSyncro;
            }

        public void setServerSyncro(int serverSyncro)
            {
                this.serverSyncro = serverSyncro;
            }

        public String getServerId()
            {
                return serverId;
            }

        public void setServerId(String serverId)
            {
                this.serverId = serverId;
            }

        public long getServerSyncroTimestamp()
            {
                return serverSyncroTimestamp;
            }

        public void setServerSyncroTimestamp(long serverSyncroTimestamp)
            {
                this.serverSyncroTimestamp = serverSyncroTimestamp;
            }

        public int getLocalId()
            {
                return localId;
            }

        public void setLocalId(int localId)
            {
                this.localId = localId;
            }

        @Override
        public String toString()
            {
                ToStringBuilder toString = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
                toString.append("LocalId", localId);
                toString.append(" Direction", direction.name());
                toString.append(" Time", Utils.formatDatetime(timestamp));
                toString.append(" Phone", phoneNumber);
                toString.append(" Contact", nameContact);
                toString.append(" Text", text);
                return toString.build().concat("\n");
            }

    }
