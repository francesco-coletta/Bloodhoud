package it.cf.bloodhoud.client.android.dao;

public interface TablePhone {
	public String TABLE_NAME = "phone";
	public String COLUMN_ID = "id";
	public String COLUMN_IMEI = "imei";
	public String COLUMN_MODEL = "model";
	public String COLUMN_OPERATOR = "operator";
	public String COLUMN_NUMSIM1 = "numSim1";
	public String COLUMN_NUMSIM2 = "numSim2";
        public String COLUMN_SERVER_SYNCRO = "serverSyncro"; // 0 = sms non inviato al server, 1 = sms inviato al server
        public String COLUMN_SERVER_SYNCRO_TIMESTAMP = "serverSyncroTimestamp";
        public String COLUMN_SERVER_ID = "serverId";
}
