package it.cf.bloodhoud.client.android.dao;

public interface TableSms {
	
	public String TABLE_NAME = "sms";
	
	public String COLUMN_ID = "id";
	public String COLUMN_DIRECTION = "direction"; //outgoing/incoming
	public String COLUMN_TIMESTAMP = "timestamp";
	public String COLUMN_PHONENUMBER = "phoneNum";
	public String COLUMN_CONTACT = "contact";
	public String COLUMN_TEXT = "text";
	public String COLUMN_SENDED_SERVER = "sendedServer";
	public String COLUMN_TIMESTAMP_SENDED = "timestampSended";
}
