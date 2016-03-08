package org.dieschnittstelle.mobile.android.environmentaccess.contacts;

import org.dieschnittstelle.mobile.android.environmentaccess.R;
import org.dieschnittstelle.mobile.android.environmentaccess.contacts.model.Contact;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class ContactDetailsViewActivity extends Activity {

	/**
	 * the logger
	 */
	protected static String logger = ContactDetailsViewActivity.class
			.getSimpleName();

	/**
	 * the argument for an addressbook entry to be edited
	 */
	public static final String ARG_ENTRY = "entry";

	/**
	 * the result param for an edited entry
	 */
	public static final String RESPONSE_ENTRY = ARG_ENTRY;

	/**
	 * the status for a successful edit
	 */
	public static final int RESULT_CODE_EDITED = 2;

	/**
	 * the entry to be edited
	 */
	private Contact editContact;

	/**
	 * the ui elements
	 */
	private EditText entryName;
	private EditText entryEmail;
	private EditText entryPhone;
	private MenuItem saveEntry;

	/**
	 * the mode (edit vs. create)
	 */
	boolean editMode = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(logger, "onCreate()...");
		super.onCreate(savedInstanceState);

		// set the layout
		setContentView(R.layout.addressbookitemview);

		// initialise the ui elements
		entryName = (EditText) findViewById(R.id.entryName);
		entryEmail = (EditText) findViewById(R.id.entryEmail);
		entryPhone = (EditText) findViewById(R.id.entryPhone);
		saveEntry = (MenuItem) findViewById(R.id.saveEntryAction);

		// try to read out the contact
		this.editContact = (Contact) getIntent()
				.getSerializableExtra(ARG_ENTRY);
		if (this.editContact == null) {
			this.editContact = new Contact();
		} else {
			this.editMode = true;
		}

		// populate the ui elements
		if (editContact.getName() != null) {
			entryName.setText(editContact.getName());
		}
		if (editContact.getEmails().size() > 0) {
			entryEmail.setText(editContact.getEmails().get(0));
		}
		if (editContact.getPhoneNumbers().size() > 0) {
			entryPhone.setText(editContact.getPhoneNumbers().get(0));
		}

		// if we have editMode, we deactivate all inputs
		if (this.editMode) {
			entryName.setEnabled(false);
			entryEmail.setEnabled(false);
			entryPhone.setEnabled(false);
		}

	}

	protected void saveEntry() {
		String editedName = entryName.getText().toString();
		if ("".equals(editedName)) {
			Toast.makeText(ContactDetailsViewActivity.this,
					"Ein Name muss eingegeben werden!", Toast.LENGTH_SHORT)
					.show();
		} else {
			editContact.setName(editedName);
			String editedEmail = entryEmail.getText().toString();
			String editedPhone = entryPhone.getText().toString();
			if (!"".equals(editedEmail)) {
				editContact.addEmailAddress(entryEmail.getText().toString());
			}
			if (!"".equals(editedPhone)) {
				editContact.addPhoneNumber(entryPhone.getText().toString());
			}

			// create a result intent
			Intent resultIntent = new Intent();
			resultIntent.putExtra(RESPONSE_ENTRY, editContact);

			setResult(RESULT_CODE_EDITED, resultIntent);
			finish();
		}
	}
	
	/**
	 * handle the options menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(logger, "onCreateOptionsMenu()");

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_addressbookitemview, menu);

		// we extract the createItemAction
		saveEntry = menu.findItem(R.id.saveEntryAction);
		if (this.editMode) {
			saveEntry.setEnabled(false);
		}
		
		// return true for the menu to be shown
		return true;
	}
	
	/**
	 * handle selection of the context menu entry
	 */
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item == saveEntry) {
			saveEntry();
		}
		
		return true;
	}

}
