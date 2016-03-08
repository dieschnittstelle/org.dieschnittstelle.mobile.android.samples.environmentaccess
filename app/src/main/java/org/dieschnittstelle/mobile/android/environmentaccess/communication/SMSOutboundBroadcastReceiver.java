package org.dieschnittstelle.mobile.android.environmentaccess.communication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

/**
 * component that receives status updates regarding sms delivery
 * 
 * see http://mobiforge.com/developing/story/sms-messaging-android
 */
public class SMSOutboundBroadcastReceiver extends BroadcastReceiver {

	protected static String logger = SMSOutboundBroadcastReceiver.class
			.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {

		Log.i(logger, "onReceive(): context is: " + context + ", intent is: "
				+ intent);

		if (CommunicationActivity.SMS_DELIVERED.equals(intent.getAction())) {

			switch (getResultCode()) {
			case Activity.RESULT_OK:
				Toast.makeText(context, "SMS has been delivered",
						Toast.LENGTH_SHORT).show();
				break;
			case Activity.RESULT_CANCELED:
				Toast.makeText(context, "SMS not delivered", Toast.LENGTH_SHORT)
						.show();
				break;
			}
		} else if (CommunicationActivity.SMS_SENT.equals(intent.getAction())) {
			switch (getResultCode()) {
			case Activity.RESULT_OK:
				Toast.makeText(context, "SMS has been sent", Toast.LENGTH_SHORT)
						.show();
				break;
			case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
				Toast.makeText(context, "Generic failure", Toast.LENGTH_SHORT)
						.show();
				break;
			case SmsManager.RESULT_ERROR_NO_SERVICE:
				Toast.makeText(context, "No service", Toast.LENGTH_SHORT)
						.show();
				break;
			case SmsManager.RESULT_ERROR_NULL_PDU:
				Toast.makeText(context, "Null PDU", Toast.LENGTH_SHORT).show();
				break;
			case SmsManager.RESULT_ERROR_RADIO_OFF:
				Toast.makeText(context, "Radio off", Toast.LENGTH_SHORT).show();
				break;
			}
		} else {
			Log.e(logger,
					"Cannot handle intent with action: " + intent.getAction());
		}

	}

}
