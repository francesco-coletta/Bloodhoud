package it.cf.bloodhoud.client.android.receiver;


import it.cf.bloodhoud.client.android.App;
import it.cf.bloodhoud.client.android.Utils;
import it.cf.bloodhoud.client.android.model.Phone;
import it.cf.bloodhoud.client.android.serviceApp.RepositoryLocalSQLLite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class InitApp
        extends BroadcastReceiver
	{
		static private final Logger LOG = LoggerFactory.getLogger(InitApp.class);

		private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
		private static final String ACTION_QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON";
		private static final String TAG = "InitApp";
		

		//@formatter:off
		//file di configurazione di Logback
	    static final String LOGBACK_XML =
	    	    "<configuration>" +
	    	    "	<appender name='LOGCAT' class='ch.qos.logback.classic.android.LogcatAppender'>" +
	    	    "		<tagEncoder>" +
	    	    "			<pattern>%logger{0}</pattern>" +
	    	    "		</tagEncoder>" +
	    	    "		<encoder>" +
	    	    "			<pattern>%method \\(%line\\): %msg</pattern>" +
	    	    "		</encoder>" +
	    	    "	</appender>" +
	    	    " " +
	    	    "	<appender name='FILE' class='ch.qos.logback.core.rolling.RollingFileAppender'>" +
	    	    "		<file>${log_path:-}CallSmsListener.log</file>" +
	    	    "		<rollingPolicy class='ch.qos.logback.core.rolling.TimeBasedRollingPolicy'>" +
	    	    "			<!-- rollover monthly -->" +
	    	    "			<fileNamePattern>${log_path:-}CallSmsListener-%d{yyyy-MM}.%i.log</fileNamePattern>" +
	    	    "			<!-- or whenever the file size reaches 1MB -->" +
	    	    "			<timeBasedFileNamingAndTriggeringPolicy class='ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP'>" +
	    	    "				<maxFileSize>1MB</maxFileSize>" +
	    	    "			</timeBasedFileNamingAndTriggeringPolicy>" +
	    	    "			<!-- keep 12 months' worth of history -->" +
	    	    "			<maxHistory>12</maxHistory>" +
	    	    "		</rollingPolicy>" +
	    	    "		<encoder>" +
	    	    "			<pattern>%d{dd-MM-yyyy HH:mm:ss.SSS} [%-30.-30file] %-5level %-20.-20logger{0}.%-25.-25method \\(%4line\\): %msg%n</pattern>" +
	    	    "		</encoder>" +
	    	    "	</appender>" +	    	    
	    	    " " +
	    	    "	<root level='TRACE'>" +
	    	    "		<appender-ref ref='LOGCAT' />" +
	    	    "		<appender-ref ref='FILE' />" +
	    	    "	</root>" +
	    	    "</configuration>"
	    	    ;		
	    //@formatter:on

		@Override
		public void onReceive(final Context context, final Intent intent)
			{
				// Log.i(TAG, "Intent received: " + intent.getAction());
				LOG.info("Intent received: {}", intent.getAction());

				try
					{
						// verifico il tipo di intent, ossia azione
						if (isBootCompleted(intent))
							{
								Log.i(TAG, "Boot Completed");
								LOG.info("Boot Completed");

								configureDefaultPassword(context);
								LOG.info("Default password configuration Completed");
								Log.i(TAG, "Default password configuration Completed");

                                                                configureDefaultServer(context);
                                                                LOG.info("Default server configuration Completed");
                                                                Log.i(TAG, "Default server configuration Completed");
								
								saveDeviceInfo(context);
							}
					}
				catch (final Exception e)
					{
						// Log.e(TAG, e.getMessage());
						LOG.error(e.getMessage());
						Log.e(TAG, e.getMessage());
					}
			}

		private void configureDefaultPassword(Context context)
			{
				SharedPreferences pref = context.getSharedPreferences(App.APP_FILE_PREFERENCES, Context.MODE_PRIVATE);
				if (pref.contains(App.APP_PROP_NAME_PASSWORD))
					{
						LOG.debug("La password esiste già nelle preferenze");
						Log.d(TAG, "La password esiste già nelle preferenze");
					}
				else
					{
						LOG.debug("La password non esiste nelle preferenze. Creo la password di default");
						Log.d(TAG, "La password non esiste nelle preferenze. Creo la password di default");
						final Editor editor = pref.edit();
						editor.putString(App.APP_PROP_NAME_PASSWORD, App.DEFAULT_PASSWORD);
						editor.commit();
					}
			}

                private void configureDefaultServer(Context context)
                    {
                            SharedPreferences pref = context.getSharedPreferences(App.APP_FILE_PREFERENCES, Context.MODE_PRIVATE);
                            if (pref.contains(App.APP_PROP_NAME_SERVER))
                                    {
                                            LOG.debug("Il server esiste già nelle preferenze");
                                            Log.d(TAG, "Il server esiste già nelle preferenze");
                                    }
                            else
                                    {
                                            LOG.debug("Il server non esiste nelle preferenze. Creo il server di default");
                                            Log.d(TAG, "Il server non esiste nelle preferenze. Creo il server di default");
                                            final Editor editor = pref.edit();
                                            editor.putString(App.APP_PROP_NAME_SERVER, App.DEFAULT_SERVER);
                                            editor.commit();
                                    }
                    }
		
                private void saveDeviceInfo(Context context)
                    {
                        RepositoryLocalSQLLite repoDb =  RepositoryLocalSQLLite.getRepository(context);
                        String deviceModel = Utils.getDeviceName();
                        String deviceId = Utils.getDeviceId(context);
                        
                        if (repoDb.getPhone(deviceId) == null){
                            LOG.info("Phone non registrato");
                            Phone phone = new Phone(deviceId, deviceModel);
                            repoDb.writePhone(phone);
                            LOG.info("Phone registrato {}", phone.toString());
                            Log.i(TAG, "Phone registrato: " + phone.toString());
                        }
                        else{
                            LOG.info("Phone già registrato");
                            Log.i(TAG, "Phone già registrato");
                        }
                    }
		
		
		private boolean isBootCompleted(final Intent intent)
			{
				return ((intent != null) && (intent.getAction() != null) && ((ACTION_BOOT_COMPLETED.compareToIgnoreCase(intent.getAction()) == 0) || (ACTION_QUICKBOOT_POWERON.compareToIgnoreCase(intent.getAction()) == 0)));
			}

		
		// private void configureLogging(final Context context, final Intent intent) throws Exception
		// {
		// if (context == null || intent == null)
		// {
		// throw new Exception("(context == null || intent == null)");
		// }
		//
		// String logPathIntoInternalStorage = context.getFilesDir().getCanonicalPath() + File.separator;
		// Log.d(TAG, "Path of internal storage fro log: " + logPathIntoInternalStorage);
		//
		// String logbackXmlWithAbsolutePath = StringUtils.replace(LOGBACK_XML, "${log_path:-}", logPathIntoInternalStorage);
		// Log.d(TAG, "LOGBACK_XML: " + logbackXmlWithAbsolutePath);
		//
		// LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		// try
		// {
		// // load a specific logback.xml
		// JoranConfigurator configurator = new JoranConfigurator();
		// configurator.setContext(lc);
		// lc.reset(); // override default configuration
		// // configurator.doConfigure("/path/to/logback.xml");
		//
		// // hard-coded config
		// configurator.doConfigure(new ByteArrayInputStream(logbackXmlWithAbsolutePath.getBytes()));
		//
		// }
		// catch (JoranException je)
		// {
		// // StatusPrinter will handle this
		// }
		// StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
		// }

	}
