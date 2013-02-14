package it.cf.bloodhoud.client.android.dao;

public interface TableCall {
	
	public String TABLE_NAME = "call";
	
	public String COLUMN_ID = "id";
	public String COLUMN_DIRECTION = "direction"; //outgoing/incoming
	public String COLUMN_TIMESTAMP_START = "timestampStart";
	public String COLUMN_TIMESTAMP_END = "timestampEnd";
	public String COLUMN_PHONENUMBER = "phoneNum";
	public String COLUMN_CONTACT = "contact";
	public String COLUMN_STATE = "state";
	public String COLUMN_DURATION = "duration";
	public String COLUMN_SENDED_SERVER = "sendedServer";
	public String COLUMN_TIMESTAMP_SENDED = "timestampSended";
	}
