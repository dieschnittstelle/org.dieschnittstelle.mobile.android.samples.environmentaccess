package org.dieschnittstelle.mobile.android.environmentaccess.camera;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.dieschnittstelle.mobile.android.environmentaccess.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import android.media.MediaScannerConnection;

import android.media.MediaScannerConnection.OnScanCompletedListener;

/**
 * note the difference to transfer images and videos to the album...
 *
 * @author kreutel
 *
 */
@SuppressLint("NewApi")
public class CameraActivity extends Activity {

	protected static String logger = CameraActivity.class.getSimpleName();

	/*
	 * constants for the two itents to capture image and video
	 */
	private static final int CALL_ACTION_CAPTURE_IMAGE = 1;
	private static final int CALL_ACTION_CAPTURE_VIDEO = 2;

	/**
	 * the image view
	 */
	private ImageView mediaView;

	/**
	 * the last media file
	 */
	private Uri mediaFileUri;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(logger, "onCreate");
		super.onCreate(savedInstanceState);

		// set the base layout
		setContentView(R.layout.camera);

		// obtain references to the ui elements in the layout
		Button captureImage = (Button) findViewById(R.id.captureImage);
		Button captureVideo = (Button) findViewById(R.id.captureVideo);
		mediaView = (ImageView) findViewById(R.id.mediaView);

		// add listeners to the buttons
		captureImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i(logger, "will capture image...");
				try {
					// create an intent
					Intent imageCaptureIntent = new Intent(
							MediaStore.ACTION_IMAGE_CAPTURE);
					// we use an image path on the external storage
					mediaFileUri = Uri.fromFile(new File(
							Environment
									.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
							System.currentTimeMillis() + ".jpg"));

					// here, a Uri Object must be passed!!!
					imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
							mediaFileUri);
					// start the intent, passing the id of this call
					startActivityForResult(imageCaptureIntent,
							CALL_ACTION_CAPTURE_IMAGE);
				} catch (Exception e) {
					Log.e(logger,
							"got exception trying to initiate image capture: "
									+ e, e);
				}
			}

		});

		captureVideo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i(logger, "will capture video...");
				try {
					// create an intent
					Intent imageCaptureIntent = new Intent(
							MediaStore.ACTION_VIDEO_CAPTURE);
					// we use an image path on the external storage
					mediaFileUri = Uri.fromFile(new File(
							Environment
									.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
							System.currentTimeMillis() + ".mp4"));

					// here, a Uri Object must be passed!!!
					imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
							mediaFileUri);
					// start the intent, passing the id of this call
					startActivityForResult(imageCaptureIntent,
							CALL_ACTION_CAPTURE_VIDEO);
				} catch (Exception e) {
					Log.e(logger,
							"got exception trying to initiate image capture: "
									+ e, e);
				}
			}

		});

	}

	/**
	 * handle the return from the image and video capture activities
	 */
	@Override
	public void onActivityResult(int callActionId, int resultCode, Intent data) {
		Log.i(logger, "onActivityResult(): " + callActionId + ", " + resultCode
				+ ", " + data);

		switch (callActionId) {
			case CALL_ACTION_CAPTURE_IMAGE:
				switch (resultCode) {
					case RESULT_OK:
						Toast.makeText(this,
								"Image capture successul. Adding to album...",
								Toast.LENGTH_LONG).show();
						// make the image available in the album...
						Uri imageAlbumUri = saveImageInAlbum(mediaFileUri);
						mediaView.setImageURI(imageAlbumUri);
						break;
					case RESULT_CANCELED:
						Toast.makeText(this, "Image capture cancelled!",
								Toast.LENGTH_LONG).show();
						break;
					default:
						Toast.makeText(this, "Image capture failed!", Toast.LENGTH_LONG)
								.show();
				}
				break;
			case CALL_ACTION_CAPTURE_VIDEO:
				switch (resultCode) {
					case RESULT_OK:
						String path = "";
				/*
				 * create a thumbnail for the uri and set it on the image view
				 */
						if (false) {
							Toast.makeText(this,
									"Video capture successul. Adding to album...",
									Toast.LENGTH_LONG).show();
							Uri videoAlbumUri = saveVideoInAlbum(mediaFileUri);


							// for thumbnail creation, we need the file path, rather than
							// the content uri, which can be accessed as follows:

							// adding videos to storage does not work anymore...
							String[] proj = {MediaStore.Images.Media.DATA};
							CursorLoader loader = new CursorLoader(this, videoAlbumUri,
									proj, null, null, null);
							Cursor cursor = loader.loadInBackground();
							cursor.moveToFirst();
							path = cursor.getString(cursor
									.getColumnIndex(MediaStore.Images.Media.DATA));
							Log.d(logger, "determined file path for uri: " + path);
						}
						else {
							path = mediaFileUri.getPath();
						}

						// now create the thumbnail using the utility class provided by
						// android
						Bitmap bmp = ThumbnailUtils.createVideoThumbnail(path,
								Thumbnails.MINI_KIND);
						Log.d(logger, "created thumbnail: " + bmp);

						// rotate the bitmap (see
						// http://stackoverflow.com/questions/8608734/android-rotate-bitmap-90-degrees-results-in-squashed-image-need-a-true-rotate-b)
						Matrix matrix = new Matrix();
						//matrix.postRotate(90);
						Bitmap rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
								bmp.getHeight(), matrix, true);

						// set the bitmap on the image view
						mediaView.setImageBitmap(rotated);
						break;
					case RESULT_CANCELED:
						Toast.makeText(this, "Image capture cancelled!",
								Toast.LENGTH_LONG).show();
						break;
					default:
						Toast.makeText(this, "Image capture failed!", Toast.LENGTH_LONG)
								.show();
				}
				break;
		}

	}

	/**
	 * save the picture in the album
	 */
	protected Uri saveImageInAlbum(Uri imgFileUri) {

		Log.i(logger, "adding image file to album: " + imgFileUri);

		try {
			ContentResolver cResolver = getContentResolver();
			InputStream is = cResolver.openInputStream(imgFileUri);
			// create a bitmap
			BitmapFactory.Options options = new BitmapFactory.Options();
			Bitmap imgbitmap = BitmapFactory.decodeStream(is, null, options);
			is.close();

			// pass the bitmap to the album
			String fileUrl = MediaStore.Images.Media.insertImage(
					getContentResolver(), imgbitmap, "Custom Camera Image",
					"Custom Camera Image");

			if (fileUrl == null) {
				Toast.makeText(this,
						"failed to insert picture into media store!",
						Toast.LENGTH_LONG).show();
			} else {
				Uri picUri = Uri.parse(fileUrl);
				Log.d(logger, "got album uri " + picUri + " for image file: "
						+ imgFileUri);
				// notify availability of the new image - this does not seem to work anymore as it has been blocked at some moment...
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
						picUri));
				Toast.makeText(this, "Successfully saved image: " + fileUrl,
						Toast.LENGTH_SHORT).show();
				return picUri;
			}

		} catch (Exception e) {
			Log.e(logger, e.getMessage(), e);
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}

		return null;
	}

	/*
	 * 160308: this does not work anymore, but there does not seem to exist a clear solution for how to let applications add own video content to the media store
	 */
	protected Uri saveVideoInAlbum(Uri videoFileUri) {
		Log.i(logger, "adding video file to album: " + videoFileUri);

		// Save the name and description of a video in a ContentValues map.
		ContentValues values = new ContentValues(2);
		values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
		// values.put(MediaStore.Video.Media.DATA, f.getAbsolutePath());

		// Add a new record (identified by uri) without the video, but with the
		// values just set.
		Uri videoUri = getContentResolver().insert(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

		Log.i(logger, "using videoUri: " + videoUri);

		// Now get a handle to the file for that record, and save the data into
		// it.
		try {

			// get an input stream for the video file
			ContentResolver cResolver = getContentResolver();
			InputStream is = cResolver.openInputStream(videoFileUri);

			// and write the data to an output stream for the videoUri
			OutputStream os = cResolver.openOutputStream(videoUri);

			Log.i(logger, "got output stream for video: " + os);

			// now transfer the data...
			byte[] buffer = new byte[4096];
			int len;
			while ((len = is.read(buffer)) != -1) {
				os.write(buffer, 0, len);
			}

			// and close the streams
			os.flush();
			is.close();
			os.close();

			Toast.makeText(this, "Successfully saved video: " + videoUri,
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Log.e(logger, e.getMessage(), e);
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}

		// scan the new media data
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
				videoUri));

		Log.i(logger, "done adding video file to album.");

		// return the uri
		return videoUri;
	}

}
