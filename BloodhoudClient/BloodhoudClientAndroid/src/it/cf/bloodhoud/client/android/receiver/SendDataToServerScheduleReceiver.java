package it.cf.bloodhoud.client.android.receiver;


import it.cf.bloodhoud.client.android.App;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SendDataToServerScheduleReceiver extends BroadcastReceiver {
	static private final Logger LOG = LoggerFactory
			.getLogger(SendDataToServerScheduleReceiver.class);

	private static final String TAG = "SendDataToServerScheduleReceiver";

	@Override
	public void onReceive(final Context context, final Intent intent) {
		LOG.info("Intent received: {}", intent.getAction());
		Log.i(TAG, "Intent received: " + intent.getAction());

		try {
			Log.i(TAG, "Boot Completed");
			LOG.info("Boot Completed");
			configureAlarmManagerForSendDataToServer(context, intent);
			LOG.info("AlarmManager For Check Send Data To Server config Completed");
			Log.i(TAG, "AlarmManager For Check Send Data To Server config Completed");
		} catch (final Exception e) {
			LOG.error(e.getMessage());
			Log.e(TAG, e.getMessage());
		}
	}


	private void configureAlarmManagerForSendDataToServer(final Context context, final Intent intent) throws Exception {
		if (context == null || intent == null) {
			throw new Exception("(context == null || intent == null)");
		}

		Intent customIntent = new Intent(context, SendDataToServerReceiver.class);
		final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, customIntent, 0);
		final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+ App.REPEAT_INTERVAL_SEND_DATA_TO_SERVER, App.REPEAT_INTERVAL_SEND_DATA_TO_SERVER, pendingIntent);
	}
}
