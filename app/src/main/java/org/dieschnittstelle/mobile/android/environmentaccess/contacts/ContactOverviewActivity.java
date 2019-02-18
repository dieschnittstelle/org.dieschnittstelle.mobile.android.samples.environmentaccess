package org.dieschnittstelle.mobile.android.environmentaccess.contacts;

import java.util.ArrayList;
import java.util.List;

import org.dieschnittstelle.mobile.android.environmentaccess.R;
import org.dieschnittstelle.mobile.android.environmentaccess.contacts.model.Contact;
import org.dieschnittstelle.mobile.android.environmentaccess.contacts.model.ContactsAccessorImpl;
import org.dieschnittstelle.mobile.android.environmentaccess.contacts.model.IContactsAccessor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class ContactOverviewActivity extends Activity {

	protected static String logger = ContactOverviewActivity.class.getSimpleName();

	/**
	 * the list of contacts
	 */
	private List<Contact> contactsList = new ArrayList<Contact>();

	/**
	 * the view of the list
	 */
	private ListView contactsListView;

	/**
	 * the adapter for the list
	 */
	private ArrayAdapter<Contact> contactsListAdapter;

	/**
	 * an action for creating new contacts
	 */
	private MenuItem createContact;

	/**
	 * the accessor for carrying out the CRUD actions on the contacts
	 */
	private IContactsAccessor accessor;

	/**
	 * track the contextmenu mode
	 */
	private boolean useContextMenuAsDialog = true;

	/**
	 * the two calls for editing and creating an entry
	 */
	private static final int REQUEST_EDIT_ENTRY = 1;
	private static final int REQUEST_CREATE_ENTRY = 2;

	private static final int REQUEST_PERMISSIONS = 3;

	/**
	 * track the action mode (if some is active)
	 */
	private ActionMode contactsListActionMode;

	/**
	 * the action mode callback
	 */
	private ContactsListActionModeCallback contactsListActionModeCallback;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(logger, "onCreate()...");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addressbookview);

		// we need to check whether we have permissions
		int hasReadContactsPermissions = checkSelfPermission(Manifest.permission.READ_CONTACTS);
		if (hasReadContactsPermissions == PackageManager.PERMISSION_GRANTED) {
			Log.i("Overview","access to contacts permitted!");
		}
		else {
			Log.i("Overview","access to contacts not yet permitted!");
			requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},REQUEST_PERMISSIONS);
			return;
		}


		// instantiate the accessor
		this.accessor = new ContactsAccessorImpl(this.getContentResolver());

		// initialise the ui elments
		contactsListView = (ListView) findViewById(R.id.contactsList);

		/*
		 * initialise an adapter for the list view
		 * 
		 * this exemplifies overriding the getView() Method of the adapter and
		 * setting particular listeners on the single views that display the
		 * contact items
		 */
		contactsListAdapter = new ArrayAdapter<Contact>(this,
				R.layout.addressbookitem_in_listview, contactsList) {
			@Override
			public View getView(final int position, View view, ViewGroup parent) {

				final ViewGroup listItemView = (ViewGroup) getLayoutInflater()
						.inflate(R.layout.addressbookitem_in_listview, null);

				TextView nameView = (TextView) listItemView
						.findViewById(R.id.contactName);

				final Contact contactItem = contactsList.get(position);

				if (contactItem.getName() != null) {
					nameView.setText(contactItem.getName());
				} else if (contactItem.getEmails().size() > 0) {
					nameView.setText(String.valueOf(contactItem.getEmails()
							.get(0)));
					;
				} else {
					nameView.setText("N.N.");
				}

				/*
				 * check which contextmenu mode is active and set the listeners
				 * required for the ActionMode case
				 */
				if (!useContextMenuAsDialog) {
					Log.i(logger,
							"setting actionmode trigger on listview item...");
					listItemView
							.setOnLongClickListener(new View.OnLongClickListener() {
								// Called when the user long-clicks on someView
								public boolean onLongClick(View view) {

									// this is called the first time we enter the action mode
									if (contactsListActionModeCallback == null) {
										contactsListActionModeCallback = new ContactsListActionModeCallback(
												contactItem);
										contactsListActionMode = ContactOverviewActivity.this
												.startActionMode(contactsListActionModeCallback);
									} else {
										// if the action mode callback has already been created, we reset the item
										contactsListActionModeCallback
												.setContact(contactItem);
										// if the action mode is still available, we update its view
										if (contactsListActionMode != null) {
											contactsListActionMode.invalidate();
										} else {
											// otherwise we re-start the action mode
											contactsListActionMode = ContactOverviewActivity.this
													.startActionMode(contactsListActionModeCallback);
										}
									}

									return true;
								}
							});

					// the registration for the long click shadows the
					// onItemClickListener of the contactsView, therefore we
					// need to handle the onClick on some item via an
					// OnClickHandler on
					// the item itself.
					listItemView.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							processSelectContact(contactItem);
						}

					});
				}

				return listItemView;
			}
		};

		// set the adapter on the list view
		contactsListView.setAdapter(contactsListAdapter);

		// register the view for the context menu
		registerForContextMenu(contactsListView);

		// set a listener that clicks on an item from the list
		contactsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View itemView,
					int itemPosition, long itemId) {

				Log.i(logger, "onItemClick: position is: " + itemPosition
						+ ", id is: " + itemId);

				Contact item = contactsList.get(itemPosition);

				processSelectContact(item);
			}

		});

		// load the contacts list and update the view
		new AsyncTask<Void, Void, List<Contact>>() {

			@Override
			protected List<Contact> doInBackground(Void... params) {
				return ContactOverviewActivity.this.accessor.readAllContacts();
			}

			@Override
			protected void onPostExecute(List<Contact> contacts) {
				// add the contacts to the list and notify the adapter (since
				// API 11, addAll is also available on ArrayAdapter)
				ContactOverviewActivity.this.contactsList.addAll(contacts);
				ContactOverviewActivity.this.contactsListAdapter
						.notifyDataSetChanged();
			}

		}.execute();

	}

	/* ********************************************************
	 * handle the main actions with regard to the contact list
	 * ********************************************************
	 */

	/**
	 * handle selection of a contact in the contact list by direct click or via
	 * the contect menu
	 */
	protected void processSelectContact(Contact contact) {
		// create the intent
		Intent editItemIntent = new Intent(this, ContactDetailsViewActivity.class);
		editItemIntent.putExtra(ContactDetailsViewActivity.ARG_ENTRY, contact);

		startActivityForResult(editItemIntent, REQUEST_EDIT_ENTRY);
	}

	/**
	 * handle creation of a new contact
	 */
	protected void processCreateContact() {
		// create the intent
		Intent editItemIntent = new Intent(this, ContactDetailsViewActivity.class);

		startActivityForResult(editItemIntent, REQUEST_CREATE_ENTRY);
	}

	/**
	 * handle deletion of a contact
	 * 
	 * @param contact
	 */
	protected void processDeleteContact(final Contact contact) {

		new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				return ContactOverviewActivity.this.accessor.deleteContact(contact);
			}

			@Override
			protected void onPostExecute(Boolean deleted) {
				if (deleted) {
					Toast.makeText(
							ContactOverviewActivity.this,
							"Der Eintrag " + contact.getName()
									+ " wurde geloescht.", Toast.LENGTH_SHORT)
							.show();
					// delete the entry from the list and notify the adapter
					contactsListAdapter.remove(contact);
					if (!useContextMenuAsDialog && contactsListActionMode != null) {
						contactsListActionMode.finish();
					}
				} else {
					Toast.makeText(
							ContactOverviewActivity.this,
							"Der Eintrag " + contact.getName()
									+ " konnte nicht geloescht werden!",
							Toast.LENGTH_SHORT).show();
				}
			}

		}.execute();
	}

	/**
	 * process a return to this activity from the AddressbookEntryActivity
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if ((requestCode == REQUEST_CREATE_ENTRY || resultCode == ContactDetailsViewActivity.RESULT_CODE_EDITED)
				&& data != null
				&& data.hasExtra(ContactDetailsViewActivity.RESPONSE_ENTRY)) {
			final Contact newContact = (Contact) data
					.getSerializableExtra(ContactDetailsViewActivity.RESPONSE_ENTRY);

			Log.i(logger,
					"creating new addressbook entry, given return value: "
							+ newContact);

			new AsyncTask<Void, Void, Boolean>() {

				@Override
				protected Boolean doInBackground(Void... params) {
					return ContactOverviewActivity.this.accessor
							.createContact(newContact);
				}

				@Override
				protected void onPostExecute(Boolean created) {
					if (created) {
						Toast.makeText(
								ContactOverviewActivity.this,
								"Der Eintrag " + newContact.getName()
										+ " wurde gespeichert.",
								Toast.LENGTH_SHORT).show();
						contactsList.add(newContact);
						contactsListAdapter.notifyDataSetChanged();

						Log.i(logger, "entry " + newContact
								+ " was added to contactsList: " + contactsList);
					} else {
						Toast.makeText(
								ContactOverviewActivity.this,
								"Der Eintrag " + newContact.getName()
										+ " konnte nicht gespeicher werden!",
								Toast.LENGTH_LONG).show();
					}
				}

			}.execute();

		}
	}

	/* ****************************************************
	 * control the options menu
	 * ****************************************************
	 */

	/**
	 * create the menu, inflating the layout resource (this is called once)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(logger, "onCreateOptionsMenu()");

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_addressbookview, menu);

		// we extract the createItemAction
		createContact = menu.findItem(R.id.createEntryAction);
		
		// return true for the menu to be shown
		return true;
	}

	/**
	 * update the menu, considering the current mode of the activity, as
	 * indicated by the instance attribute useContextMenuAsDialog (called
	 * whenever the user opens the menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Log.d(logger, "onPrepareOptionsMenu()");

		MenuItem item = menu.findItem(R.id.toggleContextMenuModeAction);
		Log.d(logger, "found toggleContextMenuMode item: " + item);

		item.setTitle(this.useContextMenuAsDialog ? R.string.optionsmenu_contextmenuAsActionmode
				: R.string.optionsmenu_contextmenuAsDialog);

		// return true for the menu to be shown
		return true;
	}

	/**
	 * handle selection of the context menu entry
	 */
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item == createContact) {
			processCreateContact();
		}
		else if (item.getItemId() == R.id.toggleContextMenuModeAction) {
			Log.i(logger, "change contextMenuMode...");
			if (this.useContextMenuAsDialog) {
				this.useContextMenuAsDialog = false;
				unregisterForContextMenu(contactsListView);

				// we provoke the action mode activation through letting the
				// list redraw, which will result in OnLongClickListeners being
				// added to the elements on the list...
				this.contactsListAdapter.notifyDataSetChanged();
				Toast.makeText(this, "Kontextmenue im Aktionsmodus",
						Toast.LENGTH_SHORT).show();
			} else {
				registerForContextMenu(contactsListView);
				this.useContextMenuAsDialog = true;
				registerForContextMenu(contactsListView);
				// this results in re-creating the list view without setting the
				// actionmode triggers on the
				// list items!
				this.contactsListAdapter.notifyDataSetChanged();
				Toast.makeText(this, "Kontextmenue als Dialog",
						Toast.LENGTH_SHORT).show();
			}
		}

		return false;
	}

	/* **************************************************************************
	 * provide the context menu via a floating menu on the item for which it was
	 * selected
	 * *****************************************************************
	 */

	/**
	 * callback on context menu creation
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		Log.i(logger,"onCreateContextMenu(): "+  menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_addressbookitem_in_listview, menu);
	}

	/**
	 * handle selection of an item
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);

		Log.i(logger, "got selection on context menu item: " + item
				+ " with menuInfo: " + item.getMenuInfo());

		if (item.getItemId() == R.id.deleteEntry) {
			processDeleteContact(contactsListAdapter
					.getItem(((AdapterContextMenuInfo) item.getMenuInfo()).position));
		} else if (item.getItemId() == R.id.editEntry) {
			processSelectContact(contactsListAdapter
					.getItem(((AdapterContextMenuInfo) item.getMenuInfo()).position));
		}
		return true;
	}

	/* ****************************************************
	 * provide the context menu via an action mode
	 * ****************************************************
	 */

	/**
	 * this is an action mode callback used as an alternative for context menu
	 * selection, follows http://www.vogella.com/articles/Android/article.html#
	 * tutorial_contextualmenu
	 */
	private class ContactsListActionModeCallback implements ActionMode.Callback {

		// we hold a reference to the item on which are supposed to run
		private Contact contact;

		// construct the callback passing a contact item
		public ContactsListActionModeCallback(Contact contact) {
			this.contact = contact;
		}

		// (re)set the contact
		public void setContact(Contact contact) {
			this.contact = contact;
		}

		// Called when the action mode is created; startActionMode() was called
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			Log.i(logger, "onCreateActionMode()");
			// Inflate a menu resource providing context menu items
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.context_addressbookitem_in_listview,
					menu);
			return true;
		}

		// Called each time the action mode is shown. Always called after
		// onCreateActionMode, but may be called multiple times if the mode is
		// invalidated.
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			Log.i(logger, "onPrepareActionMode()");

			// we update the prompts of the menu items using the name of the
			// item
			MenuItem deleteItem = menu.findItem(R.id.deleteEntry);
			MenuItem editItem = menu.findItem(R.id.editEntry);

			if (deleteItem != null) {
				deleteItem.setTitle(String.format(
						getResources().getString(
								R.string.contextmenu_deleteEntry),
						this.contact.getName()));
			}
			if (editItem != null) {
				editItem.setTitle(String.format(
						getResources()
								.getString(R.string.contextmenu_editEntry),
						this.contact.getName()));
			}

			return false;
		}

		// Called when the user selects a contextual menu item
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			Log.i(logger, "onActionItemClicked(): " + contact);

			if (item.getItemId() == R.id.deleteEntry) {
				processDeleteContact(contact);
				return true;
			} else if (item.getItemId() == R.id.editEntry) {
				processSelectContact(contact);
				return true;
			}

			return false;
		}

		// Called when the user exits the action mode
		public void onDestroyActionMode(ActionMode mode) {
			Log.i(logger, "onDestroyActionMode()");
			// we set the activitiy's actionMode attribute to null
			contactsListActionMode = null;
		}
	};

	// handle permission result
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

}
