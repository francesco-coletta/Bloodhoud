package it.cf.bloodhoud.client.android;

public interface App
    {

        public static  final String APP_FILE_PREFERENCES           = "smsListener";
        
        public static final String DATABASE_NAME    = "BloodhoudDB";
        public static final int    DATABASE_VERSION = 1;
        
        
        public static final String APP_PROP_NAME_TIMESTAMP_LASTCHECK_OUTGOING_SMS = "time_last_checked_outgoing_sms";
        public static final long REPEAT_INTERVAL_CHECK_OUTGOING_SMS = 10 * 1000;

        public static final long REPEAT_INTERVAL_SEND_DATA_TO_SERVER = 60 * 1000;
        
        
        public static final String APP_PROP_NAME_PASSWORD = "password";
        public static final String DEFAULT_PASSWORD = "0123456789";
        
        
        public static final String APP_PROP_NAME_SERVER = "server";
        
        //public static final String DEFAULT_SERVER = "192.168.137.129:1337"; 
        public static final String DEFAULT_SERVER = "bloodhoud.cloudfoundry.com"; 
        
        //The minimum distance to change Updates in meters
        public static final int LOCATION_TRACKING_MIN_DISTANCE_CHANGE_FOR_UPDATES = 100; //meter
        // The minimum time between updates in milliseconds
        public static final int LOCATION_TRACKING_MIN_TIME_BW_UPDATES = 1000 * 60 * 1; //1 minute

    }
