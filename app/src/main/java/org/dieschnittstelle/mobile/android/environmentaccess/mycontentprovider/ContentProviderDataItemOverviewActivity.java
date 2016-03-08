package org.dieschnittstelle.mobile.android.environmentaccess.mycontentprovider;

import org.dieschnittstelle.mobile.android.environmentaccess.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

@SuppressLint("NewApi") public class ContentProviderDataItemOverviewActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

	protected static String logger = ContentProviderDataItemOverviewActivity.class
			.getSimpleName();
	
	// the adapter
	private SimpleCursorAdapter adapter;

	public void onCreate(Bundle savedInstanceState) {
		Log.i(logger, "onCreate()...");
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.itemlistview);
		
		// access the listview
		final ListView listview = (ListView) findViewById(R.id.list);
		
		// create the adapter with a null cursor -- this will be taken care of by the LoaderManager
		this.adapter = new SimpleCursorAdapter(ContentProviderDataItemOverviewActivity.this, R.layout.item_in_listview,
				null,
				new String[] { DataItemContract.COLUMN_ITEM_NAME },
				new int[] { R.id.item_name });
		
		// set the adapter on the view
		listview.setAdapter(this.adapter);
		
		// start loading the cursor
        getLoaderManager().initLoader(0, null, this);		
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
		Log.i(logger,"onCreateLoader()");
		return new CursorLoader(this, DataItemContract.CONTENT_URI,
                null, null, null,
                null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.i(logger,"onLoadFinished()");
		this.adapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		Log.i(logger,"onLoaderReset()");
		this.adapter.swapCursor(null);
	}

}
