package org.dieschnittstelle.mobile.android.environmentaccess;

import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class EnvironmentAccessActivity extends Activity {

	/**
	 * the logger
	 */
	protected static final String logger = EnvironmentAccessActivity.class.getName();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			// set the list view as content view
			setContentView(R.layout.listview);

			/*
			 * access the list view for the options to be displayed
			 */
			ListView listview = (ListView) findViewById(R.id.list);

			// read out the options
			final String[] menuItems = getResources().getStringArray(
					R.array.main_menu);

			/*
			 * create an adapter that allows for the view to access the list's
			 * content and that holds information about the visual
			 * representation of the list items
			 */
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, menuItems);

			// set the adapter on the list view
			listview.setAdapter(adapter);

			// set a listener that reacts to the selection of an element
			listview.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> adapterView,
						View selectedView, int itemPosition, long arg3) {

					String selectedItem = (String) adapterView.getAdapter()
							.getItem(itemPosition);

					Log.i(this.getClass().getName(), "got item selected: "
							+ selectedItem);

					handleSelectedItem(selectedItem);
				}

			});
		} catch (Exception e) {
			String err = "got an exception: " + e;
			Log.e(logger, err, e);
			Toast.makeText(this, err,Toast.LENGTH_LONG).show();
		}
	}

	protected void handleSelectedItem(String selectedItem) {
		/*
		 * depending on the selected item, we create an activity class and call it
		 */
		try {
			Intent intent = new Intent(EnvironmentAccessActivity.this,
					Class.forName(getResources().getStringArray(
									R.array.main_menu_activities)[Arrays
									.asList(getResources().getStringArray(
											R.array.main_menu)).indexOf(
											selectedItem)]));

			/*
			 * start the activity
			 */
			startActivity(intent);
		} catch (Exception e) {
			String err = "got exception trying to handle selected item "
					+ selectedItem + ": " + e;
			Log.e(logger, err, e);
			Toast.makeText(this,err,Toast.LENGTH_LONG).show();
		}
	}
}
