package it.cf.bloodhoud.client.android.model;

import it.cf.bloodhoud.client.android.Utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.simpleframework.xml.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Call
    {
        static private final Logger LOG                   = LoggerFactory.getLogger(Call.class);

        @Element
        private int                 localId               = 0;
        @Element
        private final String        phoneNumber;
        @Element
        private String              nameContact           = "";
        @Element
        private final long          timestampStartCall;
        @Element
        private final String        timestampStartCallFormatted;
        @Element
        private long                timestampEndCall;
        @Element
        private final String        timestampEndCallFormatted;
        @Element
        private final CallDirection direction;
        @Element
        private CallState           state;
        @Element
        private int                 serverSyncro          = 0;
        @Element
        private long                serverId              = 0;
        @Element
        private long                serverSyncroTimestamp = 0;

        public enum CallDirection
            {
                INCOMING, OUTGOING;// , UNKNOW
            }

        public enum CallState
            {
                RINGING, REFUSED, ACCEPTED, FINISHED, UNKNOW;
            }

        public Call(int localId, String phoneNumber, String nameContact, long timestampStartCall, long timestampEndCall, CallDirection direction, CallState state)
            {
                super();
                this.localId = localId;
                this.phoneNumber = phoneNumber;
                this.nameContact = nameContact;
                this.timestampStartCall = timestampStartCall;
                this.timestampStartCallFormatted = Utils.formatDatetime(timestampStartCall);
                this.timestampEndCall = timestampEndCall;
                this.timestampEndCallFormatted = Utils.formatDatetime(timestampEndCall);
                this.direction = direction;
                this.state = state;
            }

        public Call(CallDirection direction, String phoneNumber)
            {
                super();
                this.phoneNumber = StringUtils.trimToEmpty(phoneNumber);
                this.timestampStartCall = System.currentTimeMillis();
                this.timestampStartCallFormatted = Utils.formatDatetime(timestampStartCall);
                this.timestampEndCall = this.timestampStartCall;
                this.timestampEndCallFormatted = Utils.formatDatetime(timestampEndCall);
                this.direction = direction;

                if (this.direction == CallDirection.INCOMING)
                    {
                        state = CallState.RINGING;
                    }
                else
                    {
                        state = CallState.RINGING;
                    }
            }

        public String getPhoneNumber()
            {
                return phoneNumber;
            }

        public long getTimestampStartCall()
            {
                return timestampStartCall;
            }

        public String getTimestampStartCallFormatted()
            {
                return timestampStartCallFormatted;
            }

        public long getTimestampEndCall()
            {
                return timestampEndCall;
            }

        public String getTimestampEndCallFormatted()
            {
                return timestampEndCallFormatted;
            }

        public long getCallDuration()
            {
                return (timestampEndCall - timestampStartCall);
            }

        public long getCallDurationSec()
            {
                return ((timestampEndCall - timestampStartCall) / 1000);
            }

        public CallDirection getDirection()
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

        public long getServerId()
            {
                return serverId;
            }

        public void setServerId(long serverId)
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
                toString.append(" State", state.toString());
                toString.append(" Start Time", Utils.formatDatetime(timestampStartCall));
                toString.append(" End Time", Utils.formatDatetime(timestampEndCall));
                toString.append(" Durata (sec)", getCallDurationSec());
                toString.append(" Phone", StringUtils.trimToEmpty(phoneNumber));
                toString.append(" Contact", StringUtils.trimToEmpty(nameContact));
                return toString.build().concat("\n");
            }

        public CallState getState()
            {
                return state;
            }

        protected void setState(CallState state)
            {
                this.state = state;
            }

        protected void setTimestampEndCall(long timestampEndCall)
            {
                if (timestampEndCall >= timestampStartCall)
                    {
                        this.timestampEndCall = timestampEndCall;
                    }
                else
                    {
                        LOG.error("Si è tentato di impostare l'ora di fine chiamata prima dell'ora di inizio");
                    }
            }

    }
