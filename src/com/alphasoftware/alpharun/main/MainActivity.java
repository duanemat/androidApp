package com.alphasoftware.alpharun.main;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewParent;
import android.webkit.CookieSyncManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alphasoftware.alpharun.R;
import com.alphasoftware.alpharun.pref.HelpPreferenceActivity;
import com.alphasoftware.alpharun.pref.PreferenceStorageUnit;
import com.alphasoftware.alpharun.utils.BookmarkAdapter;
import com.alphasoftware.alpharun.utils.BookmarkData;
import com.alphasoftware.alpharun.utils.BookmarkDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends ListActivity {

	public final static String UID = "uid";
	public final static String TITLE = "title";
	public final static String URL = "url";
	public final static String SCAN = "scan";
	public final static String INDEX = "index";
	public final static String ORDER = "order";	
	public final static int BOOKMARK_INTENT_ADD = 1;
	public final static int BOOKMARK_INTENT_REMOVE = 2;
	public final static int BOOKMARK_INTENT_EDIT = 3;
	public final static int BOOKMARK_INTENT_CANCEL = 4;
	public final static int CALLER_WEBVIEW = 0x00001234;

	private static ArrayList<BookmarkData> bookmarkList;
	private static BookmarkAdapter adapter;
	private static ListView lv;
	private static BookmarkDatabase bookmarkDB;
	private static BookmarkData scanButtonBookmark;
	private static ActionMode actionMode;
	private static PreferenceStorageUnit psu;

	// Callback functionality for context menu
	private ActionMode.Callback longHoldListItemCallback = new ActionMode.Callback() {

		// Called each time the action mode is shown. Always called after onCreateActionMode, but
		// may be called multiple times if the mode is invalidated.
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false; // Do nothing
		}

		public void onDestroyActionMode(ActionMode mode) {
			actionMode = null;

			// Make everything reset color
			lv = (ListView)findViewById(android.R.id.list);
			int childCount= lv.getChildCount();
			for(int i=0; i<childCount; i++){
				lv.getChildAt(i).setBackgroundResource(0); // Reset blank
			}

		}

		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Inflate a menu resource providing context menu items
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.bookmark_menu, menu);
			return true;

		}

		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			int position = (Integer) mode.getTag();
			BookmarkData dat;			
			dat = adapter.getBookmark(position);
			ListView listview;
			int childCount;

			switch (item.getItemId()){

			// Remove from the list
			case R.id.menu_del:

				bookmarkDB.deleteBookmark(dat);
				adapter.removeBookmark(dat); // Remove this element	
				adapter.refresh();
				actionMode.finish();
				checkEmptyList();
				return true;

				// Edit entry
			case R.id.menu_edit:
				editBookmark(dat);				
				return true;

				// Move up the list
			case R.id.menu_up:

				if (position == 0)
					return true;

				// Move the entry up one and move the next entry down
				BookmarkData aboveMark = adapter.getBookmark(position-1);

				if(aboveMark == null)
					return true;

				flipBoomarks(dat, aboveMark);

				mode.setTag(dat.iOrder);

				// Reset and re-color
				listview = (ListView)findViewById(android.R.id.list);
				childCount = lv.getChildCount();
				for(int i=0; i<childCount; i++){
					if(i == dat.iOrder){
						listview.getChildAt(i).setBackgroundResource(android.R.color.holo_blue_dark);
					}
					else
						listview.getChildAt(i).setBackgroundResource(0); // Reset blank
				}

				adapter.refresh();

				return true;

				// Move down the list
			case R.id.menu_down:

				if (position == adapter.count()-1)
					return true;

				// Move the entry up one and move the next entry down
				BookmarkData belowMark = adapter.getBookmark(position+1);

				if(belowMark == null)
					return true;

				flipBoomarks(dat, belowMark);

				mode.setTag(dat.iOrder);
				
				// Reset and re-color
				listview = (ListView)findViewById(android.R.id.list);
				childCount = lv.getChildCount();
				for(int i=0; i<childCount; i++){
					if(i == dat.iOrder){
						listview.getChildAt(i).setBackgroundResource(android.R.color.holo_blue_dark);
					}
					else
						listview.getChildAt(i).setBackgroundResource(0); // Reset blank
				}
				
				adapter.refresh();

				return true;
			default:

				return false;
			}

		}
	};

	// Flips two bookmarks in the list
	private void flipBoomarks(BookmarkData firstMark, BookmarkData secondMark){
		// Switch.  Could also just do +1 and -1, but just to be safe.
		int secondOrder = secondMark.iOrder;
		secondMark.iOrder = firstMark.iOrder;
		firstMark.iOrder = secondOrder;

		adapter.updateBookmark(firstMark);
		adapter.updateBookmark(secondMark);

		// Update the bookmark
		bookmarkDB.updateBookmark(firstMark);
		bookmarkDB.updateBookmark(secondMark);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Remove title bar
		//requestWindowFeature(Window.FEATURE_NO_TITLE);

		Intent callerIntent = getIntent();
		int activityCaller = callerIntent.getIntExtra("CALLER", -1);
		if(activityCaller != -1)
			getActionBar().setDisplayHomeAsUpEnabled(true);
		else
			getActionBar().setDisplayHomeAsUpEnabled(false);

		setContentView(R.layout.activity_main);

		// Holder for the data from the database		
		bookmarkDB = new BookmarkDatabase(this);
		bookmarkList = bookmarkDB.getBookmarks();
		psu = new PreferenceStorageUnit(this);

		// Cookies syncing
		CookieSyncManager.createInstance(this);

		// Set the preferences
		PreferenceManager.setDefaultValues(this, R.xml.preference_main, false);

		// Create the adapter and initialize with the data
		adapter = new BookmarkAdapter(this, bookmarkList);
		setListAdapter(adapter);

		lv = (ListView)findViewById(android.R.id.list);
		lv.setClickable(true);

		// The on-click listener
		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				BookmarkData dat = (BookmarkData) lv.getItemAtPosition(position);
				Intent intent;
				// If we aren't running phonegap, then load normal browser; else, Phonegap version
				if ((Boolean)psu.getPreference(PreferenceStorageUnit.PHONEGAP) == false)
					intent = new Intent(MainActivity.this, WebViewActivity.class);
				else
					intent = new Intent(MainActivity.this, CordovaWebViewActivity.class);

				String urlVar = (String) psu.getPreference(PreferenceStorageUnit.URL_VARIABLE);
				String intentUrl;
				if(!urlVar.isEmpty()){
					intentUrl = dat.sUrl.replaceAll("\\[\\[v1\\]\\]", urlVar);
				}
				else{
					intentUrl = dat.sUrl;
				}

				intent.putExtra(MainActivity.URL, intentUrl);
				intent.putExtra(PreferenceStorageUnit.TOUCH_HIGHLIGHTING, (Boolean) psu.getPreference(PreferenceStorageUnit.TOUCH_HIGHLIGHTING));
				// Set intent flags that basically render any past actions forgotten
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP); // This only eliminates new instances.  
				
				// Close up the actionMode if it is being used.
				
				if (actionMode != null){
					actionMode.finish();
					actionMode = null;
				}
				startActivity(intent);

			}
		});

		// Long-hold click
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {

				Log.d("Found", adapter.getBookmark(position).toString());

				if(actionMode != null)
					return false;

				TextView tv = (TextView) view.findViewById(R.id.url);
				Log.d("Found", tv.getText().toString());

				actionMode = startActionMode(longHoldListItemCallback);
				actionMode.setTag(position);

				view.setBackgroundResource(android.R.color.holo_blue_dark);

				return true;
			}
		});


		// Update the preferences for the system
		updatePrefs();
		
		// Set the listener
		LinearLayout helpLayout = (LinearLayout)findViewById(R.id.startupHelpLayout);
		helpLayout.setOnClickListener(new OnClickListener() {
			
			// Load the help page
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, HelpPreferenceActivity.class);
				startActivity(intent);
				
			}
		});
		
		// Check to see if help sections necessary.
		checkEmptyList();

	}



	@Override
	protected void onResume() {

		super.onResume();
		CookieSyncManager.getInstance().startSync();
		bookmarkList = bookmarkDB.getBookmarks();

		// Re-create the list adapter.  A bit inefficient, but won't kill anyone.
		adapter = new BookmarkAdapter(this, bookmarkList);
		setListAdapter(adapter);
		
		// Check to see if help sections necessary.
		checkEmptyList();

	}

	@Override
	protected void onPause() {
		super.onPause();
		CookieSyncManager.getInstance().stopSync();
	}



	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		
		// We were open, so close the "old" one and just restart a new one
		if(actionMode != null){
			int position = (Integer) actionMode.getTag(); 
			actionMode.finish();
			actionMode = null;
			actionMode = startActionMode(longHoldListItemCallback);
			actionMode.setTag(position);
			
			ListView lv = (ListView)findViewById(android.R.id.list);
			lv.getChildAt(position).setBackgroundResource(android.R.color.holo_blue_dark);
		}
		super.onConfigurationChanged(newConfig);
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);		
		return true;
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()){

		// Add a bookmark
		case R.id.mainAdd:
			addBookmark();
			break;

		case R.id.mainPref:
			loadPrefs();
			break;

		case android.R.id.home:
			finish(); // If you want to go back, then just close out this app.
			break;

		}
		return super.onOptionsItemSelected(item);
	}

	// Called when prefs are changed
	private void updatePrefs(){
		psu.updateAllPreferences();

		// Update the variable URL field, if provided
		// Edit:  Just update view.  Don't actually change the URL
		/*String urlVar = (String) psu.getPreference(PreferenceStorageUnit.URL_VARIABLE);
		if(!urlVar.isEmpty()){
			// Cycle through all of the known bookmarks and update
			int bookmarkCount = adapter.count();
			for(int i=0; i<bookmarkCount; i++){
				BookmarkData bk = adapter.getBookmark(i);

				// If it is included, update the adapter
				if(bk.sUrl.contains("[[v1]]")){
					//bk.sUrl = bk.sUrl.replaceAll("\\[\\[v1\\]\\]", urlVar);
					//adapter.updateBookmark(bk);
					//adapter.refresh();
				}
			}
		}*/
		adapter.refresh();
	}
	
	private void checkEmptyList(){
		ListAdapter la = getListAdapter();
		
		// If empty, then show the two help texts
		if(la.getCount() == 0){
			findViewById(R.id.emptyListLayout).setVisibility(View.VISIBLE);
			findViewById(R.id.startupHelpLayout).setVisibility(View.VISIBLE);
		}
		else{
			findViewById(R.id.emptyListLayout).setVisibility(View.GONE);
			findViewById(R.id.startupHelpLayout).setVisibility(View.GONE);
		}
	}
	
	// Calls the BookmarkActivity for adding a new mark.
	public void addBookmark(){
		// Creates the intent and sends along a message saying this is a new entry
		Intent intent = new Intent(this, BookmarkActivity.class);		

		// The more efficient solution is to pass in a Parceable data element, but for this project probably not necessary at this point 
		intent.putExtra(MainActivity.TITLE, "");
		intent.putExtra(MainActivity.URL, "");
		intent.putExtra(MainActivity.UID, "");
		intent.putExtra(MainActivity.SCAN, false);
		intent.putExtra(MainActivity.INDEX, -1); // -1 means it is new
		intent.putExtra(MainActivity.ORDER, adapter.count()); // Gives me the size of the current adapter, which should be the new "lowest" order
		intent.putExtra("requestCode", this.BOOKMARK_INTENT_ADD);

		startActivityForResult(intent, this.BOOKMARK_INTENT_ADD);
	}

	// Calls the BookmarkActivity for adding a new mark.
	public void editBookmark(BookmarkData bookmark){
		// Creates the intent and sends along a message saying this is a known item
		Intent intent = new Intent(this, BookmarkActivity.class);		

		// The more efficient solution is to pass in a Parceable data element, but for this project probably not necessary at this point 
		intent.putExtra(MainActivity.TITLE, bookmark.sTitle);
		intent.putExtra(MainActivity.URL, bookmark.sUrl);
		intent.putExtra(MainActivity.UID, bookmark.sUid);
		intent.putExtra(MainActivity.SCAN, bookmark.bScan);
		intent.putExtra(MainActivity.INDEX, bookmark.index);
		intent.putExtra(MainActivity.ORDER, bookmark.iOrder); 
		intent.putExtra("requestCode", MainActivity.BOOKMARK_INTENT_EDIT);

		startActivityForResult(intent, MainActivity.BOOKMARK_INTENT_EDIT);
	}

	// Load the preference screen
	public void loadPrefs(){
		Intent intent = new Intent(MainActivity.this, PreferencesActivity.class);
		intent.putExtra(PreferencesActivity.PREFERENCE_CALLER, "MainActivity");
		startActivityForResult(intent, PreferencesActivity.PREFERENCE_REQUEST);
	}

	public void scanBtnClick(View v){
		ViewParent parent = v.getParent();

		Log.d("Button", v.getTag()+ " ");
		IntentIntegrator scanIntent = new IntentIntegrator(this);
		scanIntent.setPosition((Integer) v.getTag());
		scanIntent.initiateScan();

	}
		

	// When you return from any activity with a result - in this case, most likely BookmarkActivity
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		// If nothing returned, just exit
		if(data == null)
			return;

		if (requestCode == IntentIntegrator.REQUEST_CODE){
			IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

			// Get the position related to this scan
			Integer position = scanResult.getPosition();
			BookmarkData dat = adapter.getBookmark(position);
			String url = BookmarkActivity.checkURL(scanResult.getContents());
			if (url != null){
				dat.sUrl = url;
				bookmarkDB.updateBookmark(dat);
				adapter.updateBookmark(dat);
				adapter.refresh();
			}else{
				Toast.makeText(this, "Could not update bookmark because URL is invalid: " + scanResult.getContents(), Toast.LENGTH_LONG).show();
			}

		}
		else if (resultCode == PreferencesActivity.PREFERENCE_REQUEST){
			updatePrefs();
		}
		else{
			BookmarkData dat = new BookmarkData();

			dat.sTitle = data.getStringExtra(MainActivity.TITLE);
			dat.sUrl = data.getStringExtra(MainActivity.URL);
			dat.index = data.getLongExtra(MainActivity.INDEX, -1);
			dat.sUid = data.getStringExtra(MainActivity.UID);
			dat.bScan = data.getBooleanExtra(MainActivity.SCAN, false);
			dat.iOrder = data.getIntExtra(MainActivity.ORDER, adapter.count());

			if(resultCode == MainActivity.BOOKMARK_INTENT_ADD){

				// If a valid entry
				if(dat.index != -1){
					adapter.addBookmark(dat);
					adapter.refresh();
				}
			}
			// Just refresh the screen
			else if (resultCode == this.BOOKMARK_INTENT_EDIT){

				adapter.updateBookmark(dat);
				adapter.refresh();
				if(actionMode != null)
					actionMode.finish();
			}
			else if (resultCode == this.BOOKMARK_INTENT_CANCEL){
				// Do nothing
				if(actionMode != null)
					actionMode.finish();
			}
		}

		// Update hte listview
		checkEmptyList();
	}
	


}
