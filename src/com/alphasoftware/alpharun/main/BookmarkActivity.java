package com.alphasoftware.alpharun.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.alphasoftware.alpharun.R;
import com.alphasoftware.alpharun.pref.PreferenceStorageUnit;
import com.alphasoftware.alpharun.utils.BookmarkData;
import com.alphasoftware.alpharun.utils.BookmarkDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class BookmarkActivity extends Activity {

	private static BookmarkDatabase bookmarkDB;
	private Intent intent;
	private static PreferenceStorageUnit psu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// Get the bookmark database reference
		bookmarkDB = new BookmarkDatabase(getApplicationContext());

		// Remove title bar
		//requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.bookmark_activity_layout);

		intent = getIntent();		

		// Get the bookmark data passed in
		BookmarkData entry = getBookmarkData(intent);

		// Get the elements of the bookmark
		EditText eURL = (EditText) findViewById(R.id.editURL);
		EditText eTitle = (EditText) findViewById(R.id.editTitle);
		EditText eUID = (EditText) findViewById(R.id.editUID);
		Switch sScan = (Switch) findViewById(R.id.swScan);

		// New entry
		if(entry.index == -1){
			eURL.setHint("Enter Link");
			eTitle.setHint("Enter bookmark's name");
			eUID.setHint("ID#");
			sScan.setChecked(false);						
		}else{ // Known entry
			eURL.setText(entry.sUrl);
			eTitle.setText(entry.sTitle);
			eUID.setText(entry.sUid);
			sScan.setChecked(entry.bScan);
		}


	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		bookmarkDB.close();
	}

	// Retrieves the bookmark data from the elements passed in
	private BookmarkData getBookmarkData(Intent intent){
		BookmarkData entry = new BookmarkData();

		try{
			entry.sTitle = intent.getStringExtra(MainActivity.TITLE);
			entry.sUrl = intent.getStringExtra(MainActivity.URL);
			entry.sUid = intent.getStringExtra(MainActivity.UID);
			entry.bScan = intent.getBooleanExtra(MainActivity.SCAN, false);
			entry.index = intent.getLongExtra(MainActivity.INDEX, -1); // Either get the DB index or -1 for a new entry
			entry.iOrder = intent.getIntExtra(MainActivity.ORDER, 999); // Something went wrong here then
		}catch (Exception e){
			// If something bad happens
			Toast.makeText(this, "Could not load bookmark data", Toast.LENGTH_SHORT).show();
			Log.e("BookmarkError", e.getMessage());
		}

		return entry;
	}

	// If you hit cancel, exit without doing anything.
	public void cancelBtnClick(View v){
		Intent returnIntent = new Intent();		

		this.setResult(MainActivity.BOOKMARK_INTENT_CANCEL, returnIntent);	
		finish();
	}

	// When you click on the save button
	public void saveBtnClick(View v){
		String url, title, uid; // Only url and title are necessary
		boolean scan;
		long dbIndex = -1;
		int order = -1;

		// Get the entries from the entry fields		
		EditText eURL = (EditText) findViewById(R.id.editURL);
		EditText eTitle = (EditText) findViewById(R.id.editTitle);
		EditText eUID = (EditText) findViewById(R.id.editUID);
		Switch sScan = (Switch) findViewById(R.id.swScan);

		// Retrieve current values
		url = eURL.getText().toString().toLowerCase();
		title = eTitle.getText().toString();
		uid = eUID.getText().toString();
		scan = sScan.isChecked();

		boolean validEntry = true;
		String errorMessage = "The bookmark has an invalid ";
		// Make sure you have valid value for URL 
		url = checkURL(url);
		if (url == null){
			errorMessage += "URL.";
			validEntry = false;
		}


		// If invalid, then don't save and give warning message
		if(!validEntry){
			Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
			return;
		}else{

			BookmarkData bookmark = new BookmarkData(title, url, uid, scan);
			bookmark.index = intent.getLongExtra(MainActivity.INDEX, -1);
			bookmark.iOrder = intent.getIntExtra(MainActivity.ORDER, 998);
			order = bookmark.iOrder;
			// Valid entry, so save to database						
			dbIndex = saveToDatabase(bookmark, intent.getIntExtra("requestCode", -1));
			if(dbIndex != -1){
				Toast.makeText(this, "Bookmark saved!", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(this, "Bookmark failed to save!", Toast.LENGTH_LONG).show();
			}
		}

		Intent returnIntent = new Intent();
		returnIntent.putExtra(MainActivity.TITLE, title);
		returnIntent.putExtra(MainActivity.URL, url);
		returnIntent.putExtra(MainActivity.UID, uid);
		returnIntent.putExtra(MainActivity.SCAN, scan);
		returnIntent.putExtra(MainActivity.INDEX, dbIndex);
		returnIntent.putExtra(MainActivity.ORDER, order);


		this.setResult(intent.getIntExtra("requestCode", -1), returnIntent);	
		finish();

	}

	// Simple helper function for checking validity of the URL and returns either the valid URL or null if invalid
	// If it doesn't match, see if the issue is just with the lack of http:  Add that
	public static String checkURL(String url){

		// Check to see if it doesn't start with http.  If not, then just add it in
		if(!url.startsWith("http")){
			url = "http://" + url;
		}

		// Check valid URL + TLD
		String tld = url.substring(url.lastIndexOf(".")+1);		
		if(tld == null || tld == "")
			tld = ".foo";
		
		if(!Patterns.WEB_URL.matcher(url).matches()){

			// Check to see if it contains '[[v1]]'.  If it does and it failed, just let it go.
			if(url.contains("[[v1]]")){
				// Do nothing
			}
			else{
				url = null;
			}
		}

		return url;

	}

	// Function called when the scan button is pressed
	public void scanBtnClick(View v){
		IntentIntegrator scanIntent = new IntentIntegrator(this);
		scanIntent.initiateScan();
	}

	// Used to store entry to the database
	private long saveToDatabase(BookmarkData bookmark, int requestCode){
		long bookmarkIndex = -1;		

		// Add it into the database
		if(requestCode == MainActivity.BOOKMARK_INTENT_ADD)
			bookmarkIndex = bookmarkDB.insertBookmark(bookmark);
		else if (requestCode == MainActivity.BOOKMARK_INTENT_EDIT){
			bookmarkIndex = bookmarkDB.updateBookmark(bookmark);
			if(bookmarkIndex > 0)
				bookmarkIndex = bookmark.index;
		}

		return bookmarkIndex;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

	// Called when you click on the scan button 
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		// If no return intent, then exit
		if(intent == null)
			return;

		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

		if (scanResult != null) {
			String scanURL = scanResult.getContents(); // Get the contents and put this as the URL

			// If not an empty string, then treat as valid URL
			scanURL = checkURL(scanURL);
			if(scanURL == null){
				Toast.makeText(this, "Could not scan URL from QR code.", Toast.LENGTH_LONG).show();
			}else{
				EditText eURL = (EditText) findViewById(R.id.editURL);
				eURL.setText(scanURL);
			}

		}


	}

}
