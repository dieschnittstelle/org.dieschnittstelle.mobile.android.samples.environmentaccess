package org.dieschnittstelle.mobile.android.environmentaccess.communication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/**
 * this component receives noifications about incoming sms
 *
 * @author kreutel
 */
public class SMSInboundBroadcastReceiver extends BroadcastReceiver {

	protected static String logger = SMSInboundBroadcastReceiver.class.getSimpleName();
	
	/**
	 * this is taken from the Darcey/Conder book
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle data = intent.getExtras();
		
		Log.i(logger,"received SMS: " + data);
		
		// read out the message and the sender
		String message = "";
		String sender = null;
		
		// read out the data
		Object[] pdus = (Object[]) data.get("pdus");
		for (Object pdu : pdus) {
			SmsMessage part = SmsMessage.createFromPdu((byte[])pdu);			
			message += part.getDisplayMessageBody();
			
			if (sender == null) {
				sender = part.getDisplayOriginatingAddress();
			}
		}
		
		Toast.makeText(context, "Received sms from sender: " + message, Toast.LENGTH_SHORT).show();
		
	}

}
