package org.dieschnittstelle.mobile.android.environmentaccess.mycontentprovider;

import android.net.Uri;

/**
 * we copy this here...
 */
public interface DataItemContract {

	/*
	 * the Todo attributes as column names
	 */

	public static final String COLUMN_ITEM_ID = "_ID";

	public static final String COLUMN_ITEM_NAME = "name";

	public static final String COLUMN_ITEM_DESCRIPTION = "description";

	/*
	 * the mime types for single and multiple todos
	 */

	public static final String MIMETYPE_DATAITEM_ITEM = "vnd.android.cursor.item/org.dieschnittstelle.mobile.android.dataitem";

	public static final String MIMETYPE_DATAITEM_LIST = "vnd.android.cursor.dir/org.dieschnittstelle.mobile.android.dataitem";

	/*
	 * the uri prefix for todo
	 */
	public static final String URI_DATAITEM_LIST_AUTHORITY = "org.dieschnittstelle.mobile.android.dataaccess";

	public static final String URI_DATAITEM_LIST_PATH = "dataitem";

	public static final Uri CONTENT_URI = new Uri.Builder().scheme("content")
			.authority(URI_DATAITEM_LIST_AUTHORITY)
			.appendPath(URI_DATAITEM_LIST_PATH).build();

}
