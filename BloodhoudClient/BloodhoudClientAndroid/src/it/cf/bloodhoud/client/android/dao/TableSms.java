package it.cf.bloodhoud.client.android.dao;

public interface TableSms {
	
	public String TABLE_NAME = "sms";
	
	public String COLUMN_ID = "id";
	public String COLUMN_DIRECTION = "direction"; //outgoing/incoming
	public String COLUMN_TIMESTAMP = "timestamp";
	public String COLUMN_PHONENUMBER = "phoneNum";
	public String COLUMN_CONTACT = "contact";
	public String COLUMN_TEXT = "text";
	public String COLUMN_SERVER_SYNCRO = "serverSyncro"; // 0 = sms non inviato al server, 1 = sms inviato al server
	public String COLUMN_SERVER_SYNCRO_TIMESTAMP = "serverSyncroTimestamp";
	public String COLUMN_SERVER_ID = "serverId";
}
