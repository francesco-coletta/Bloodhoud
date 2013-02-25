package it.cf.bloodhoud.client.android.receiver;

import it.cf.bloodhoud.client.android.App;
import it.cf.bloodhoud.client.android.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class OutgoingSmsScheduleReceiver extends BroadcastReceiver {
	static private final Logger LOG = LoggerFactory
			.getLogger(OutgoingSmsScheduleReceiver.class);


	private static final String TAG = "OutgoingSmsScheduleReceiver";

	@Override
	public void onReceive(final Context context, final Intent intent) {
		LOG.info("Intent received: {}", intent.getAction());
		Log.i(TAG, "Intent received: " + intent.getAction());

		try {
			Log.i(TAG, "Boot Completed");
			LOG.info("Boot Completed");
			storeTimestampLastCheck(context);
			configureAlarmManagerForCheckOutgoingSms(context, intent);
			LOG.info("AlarmManager For Check Outgoing Sms config Completed");
			Log.i(TAG, "AlarmManager For Check Outgoing Sms config Completed");
		} catch (final Exception e) {
			LOG.error(e.getMessage());
			Log.e(TAG, e.getMessage());
		}
	}



	private void configureAlarmManagerForCheckOutgoingSms(final Context context, final Intent intent) throws Exception {
		if (context == null || intent == null) {
			throw new Exception("(context == null || intent == null)");
		}

		Intent customIntent = new Intent(context, OutgoingSmsReceiver.class);
		final PendingIntent outgoingSmsLogger = PendingIntent.getBroadcast(context, 0, customIntent, 0);
		final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+ App.REPEAT_INTERVAL_CHECK_OUTGOING_SMS, App.REPEAT_INTERVAL_CHECK_OUTGOING_SMS, outgoingSmsLogger);
	}

	private void storeTimestampLastCheck(final Context context) {
		final long currentTime = System.currentTimeMillis();
		final Editor editor = context.getSharedPreferences(App.APP_FILE_PREFERENCES, Context.MODE_PRIVATE).edit();
		editor.putLong(App.APP_PROP_NAME_TIMESTAMP_LASTCHECK_OUTGOING_SMS, currentTime);
		editor.commit();

		// Log.d(TAG, "Update timestamp last check: " + currentTime + " = " +
		// Utils.formatDatetime(currentTime));
		LOG.debug("Update timestamp last check: {}  = {}", currentTime, Utils.formatDatetime(currentTime));
		Log.d(TAG, "Update timestamp last check: " +  currentTime + " = " +  Utils.formatDatetime(currentTime));
	}

}
