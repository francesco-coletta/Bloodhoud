package it.cf.bloodhoud.client.android.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Phone
    {

        private int                localId               = 0;
        private String deviceId     = null;
        private String modelPhone   = null;
        private String numberSim1   = "";
        private String numberSim2   = "";
        private int    serverSyncro = 0;
        private String serverId     = "";

        public Phone(String deviceId, String modelPhone)
            {
                super();
                this.deviceId = StringUtils.trimToEmpty(deviceId);
                this.modelPhone = StringUtils.trimToEmpty(modelPhone);
            }


        public int getLocalId()
            {
                return localId;
            }


        public void setLocalId(int localId)
            {
                this.localId = localId;
            }


        public String getNumberSim1()
            {
                return numberSim1;
            }

        public void setNumberSim1(String numberSim1)
            {
                this.numberSim1 = numberSim1;
            }

        public String getNumberSim2()
            {
                return numberSim2;
            }

        public void setNumberSim2(String numberSim2)
            {
                this.numberSim2 = numberSim2;
            }

        public String getDeviceId()
            {
                return deviceId;
            }

        public String getModelPhone()
            {
                return modelPhone;
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

        @Override
        public String toString()
            {
                ToStringBuilder toString = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
                toString.append("localId", localId);
                toString.append(" Imei", deviceId);
                toString.append(" Model", modelPhone);
                toString.append(" SIM1", this.numberSim1);
                toString.append(" SIM2", this.numberSim2);
                return toString.build();
            }
    }
