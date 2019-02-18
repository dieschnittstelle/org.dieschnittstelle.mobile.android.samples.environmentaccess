package org.dieschnittstelle.mobile.android.environmentaccess.communication;

import org.dieschnittstelle.mobile.android.environmentaccess.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class CommunicationActivity extends Activity {

	/**
	 * the logger
	 */
	protected static String logger = CommunicationActivity.class
			.getSimpleName();

	/**
	 * constants for identifying the intents used for calling back this activity
	 * with status notifications
	 */
	public static final String SMS_SENT = CommunicationActivity.class.getName()
			+ ".SMS_SENT";
	public static final String SMS_DELIVERED = CommunicationActivity.class
			.getName() + ".SMS_DELIVERED";

	/**
	 * separator for the recipients of email addresses
	 */
	public static final String RECIPIENTS_SEPARATOR = ",";

	/**
	 * the fields for recipient and content
	 */
	private EditText msgRecipient;
	private EditText msgContent;

	/**
	 * the broadcast receiver for outbound sms
	 */
	private BroadcastReceiver smsOutboundMonitor = new SMSOutboundBroadcastReceiver();

	/**
	 * the phone state listener
	 */
	private PhoneStateListener phoneStateListener;

	/**
	 * the email user for which emails shall be composed
	 */
	private String emailuser = "org.dieschnittstelle.mobile@gmail.com";

	/**
	 * onCreate
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.sender);

		// access the two edit text fields
		this.msgRecipient = (EditText) findViewById(R.id.messageReceiver);
		this.msgContent = (EditText) findViewById(R.id.messageText);

		// register a phonestatelistener
		this.phoneStateListener = new MyPhoneStateListener(this);
		((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
				.listen(this.phoneStateListener,
						PhoneStateListener.LISTEN_CALL_STATE);

		// then set onClick listeners on the action buttons
		findViewById(R.id.sendSMSAction).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						sendSMS(getMessageRecipient(), getMessageContent());
					}
				});

		findViewById(R.id.composeSMSAction).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						composeSMS(getMessageRecipient(), getMessageContent());
					}
				});

		findViewById(R.id.composeEmailAction).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						composeEmail(CommunicationActivity.this, "new email",
								getMessageContent(), emailuser,
								getMessageRecipient());
					}
				});

		findViewById(R.id.placeCallAction).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						placeCall(getMessageRecipient());
					}
				});

		// associate the sms monitor with the two broadcast intents for sent and
		// delivery
		registerReceiver(this.smsOutboundMonitor, new IntentFilter(SMS_SENT));
		registerReceiver(this.smsOutboundMonitor, new IntentFilter(
				SMS_DELIVERED));

	}

	/**
	 * on finish we need to unregister the receivers
	 */
	@Override
	public void finish() {
		if (this.smsOutboundMonitor != null) {
			unregisterReceiver(this.smsOutboundMonitor);
		}

		// we also unregister the call state listener - look how this is done :)
		((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
				.listen(this.phoneStateListener, PhoneStateListener.LISTEN_NONE);

		super.finish();
	}

	/**
	 * get the recipient
	 */
	protected String getMessageRecipient() {
		return msgRecipient.getText().toString();
	}

	/**
	 * get the content
	 */
	protected String getMessageContent() {
		return msgContent.getText().toString();
	}

	/**
	 * send an sms
	 */
	protected void sendSMS(String receiver, String message) {

		final SmsManager manager = SmsManager.getDefault();
		Log.d(logger, "using sms manager: " + manager);

		// create two pending intents that specify callbacks to this activity
		// given a status of sms sending/delivery
		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
				SMS_SENT), 0);

		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
				new Intent(SMS_DELIVERED), 0);

		// pass the intents to the sendTextMessage method
		manager.sendTextMessage(receiver, /** sender */
		null, message, sentPI, deliveredPI);

	}

	/**
	 * compose an sms
	 */
	// http://snipt.net/Martin/android-intent-usage/
	protected void composeSMS(String receiver, String message) {

		// the sms compose is identified by the tel: uri and the specified
		// action ACTION_SENDTO
		Uri smsUri = Uri.parse("smsto:" + receiver);
		Intent smsIntent = new Intent(Intent.ACTION_SENDTO, smsUri);
		smsIntent.putExtra("sms_body", message);

		startActivity(smsIntent);
	}

	/**
	 * pass the content and the recipient to an email sending app
	 *
	 *
	 * @param context
	 * @param subject
	 * @param body
	 * @param sender
	 * @param recipients
	 */
	// see
	// http://www.anddev.org/email_send_intent_intentchooser-t3295.html
	public static void composeEmail(Context context, String subject,
			String body, String sender, String recipients) {

		Log.i(logger, "composing email to " + recipients + " with subject "
				+ subject + ": " + body);

		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

		emailIntent.setType("plain/text");

		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
				recipients.split(RECIPIENTS_SEPARATOR));

		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);

		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);

		// determine the activity to use for sending
		Intent chosenIntent = Intent.createChooser(emailIntent,
				"Sending Email...");

		Log.i(logger, "determined intent for sending email: " + chosenIntent);

		context.startActivity(chosenIntent);

	}

	/**
	 * place a call
	 */
	@SuppressLint("MissingPermission")
	protected void placeCall(String receiver) {
		String url = "tel:" + receiver;
		Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));

		startActivity(intent);
	}

}
