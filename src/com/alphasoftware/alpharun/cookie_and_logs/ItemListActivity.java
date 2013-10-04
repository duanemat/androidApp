package com.alphasoftware.alpharun.cookie_and_logs;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alphasoftware.alpharun.R;
import com.alphasoftware.alpharun.pref.PreferenceStorageUnit;
import com.alphasoftware.alpharun.utils.BookmarkDatabase;
import com.loopj.android.http.PersistentCookieStore;

/**
 * An activity representing a list of Items. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link ItemDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ItemListFragment} and the item details (if present) is a
 * {@link ItemDetailFragment}.
 * <p>
 * This activity also implements the required {@link ItemListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class ItemListActivity extends FragmentActivity implements
ItemListFragment.Callbacks {

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;
	private String itemType;
	private boolean isDisplayFragmented; // If true, then display in fragments.  If false, then flat view 
	private String listViewedFragmentID = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		
		Log.d("tracking", this.getClass().toString());
		itemType = getIntent().getDataString();
		// Get the stored value for the type of display
		if(itemType.equals(ItemListFragment.COOKIE))
			isDisplayFragmented = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getBoolean(PreferenceStorageUnit.DISPLAY_LAYOUT_COOKIE, true);
		else
			isDisplayFragmented = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getBoolean(PreferenceStorageUnit.DISPLAY_LAYOUT_LOG, true);

		setContentView(R.layout.log_activity_item_list);

		ProgressBar pb = (ProgressBar)findViewById(R.id.log_progress_bar);

		if (findViewById(R.id.item_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((ItemListFragment) getSupportFragmentManager().findFragmentById(
					R.id.item_list)).setActivateOnItemClick(true);
		}
		
		// Handle the "initial" data appearance.
		if(mTwoPane){
			switchModeFragment();
		}
		else
			switchModeSingle();
		
		// TODO: If exposing deep links into your app, handle intents here.
	}

	/**
	 * Callback method from {@link ItemListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */

	public void onItemSelected(String id) {

		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(ItemDetailFragment.ARG_ITEM_ID, id);

			if(itemType.equals(ItemListFragment.LOG)){
				arguments.putString(ItemDetailFragment.ITEM_TYPE, ItemListFragment.LOG);
			}
			if(itemType.equals(ItemListFragment.COOKIE)){
				arguments.putString(ItemDetailFragment.ITEM_TYPE, ItemListFragment.COOKIE);
			}
			ItemDetailFragment fragment = new ItemDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
			.replace(R.id.item_detail_container, fragment).commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, ItemDetailActivity.class);
			detailIntent.putExtra(ItemDetailFragment.ARG_ITEM_ID, id);

			if(itemType.equals(ItemListFragment.LOG)){
				detailIntent.putExtra(ItemDetailFragment.ITEM_TYPE, ItemListFragment.LOG);
			}
			if(itemType.equals(ItemListFragment.COOKIE)){
				detailIntent.putExtra(ItemDetailFragment.ITEM_TYPE, ItemListFragment.COOKIE);
			}
			startActivity(detailIntent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.logmenu_frag, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()){

		case R.id.clearFiles:
			clearLogFiles();
			//updateMenuItem(item, false);
			break;

			// Switch between tiered and flat screen
		case R.id.switchModes:
			isDisplayFragmented = !isDisplayFragmented; // Flip
			if(mTwoPane)
				switchModeFragment();
			else
				switchModeSingle();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	// Handles the single mode switching
	private void switchModeSingle(){

		// Flat file
		if(!isDisplayFragmented){
			Fragment listFrag = getSupportFragmentManager().findFragmentById(R.id.item_list);
			listFrag.getView().setVisibility(View.GONE);
			//Log.d("debugging", listFrag.getTag());

			// Remove the old list fragment
			LinearLayout ll = (LinearLayout) findViewById(R.id.log_activity_layout);
			

			// Add the "flat" to tbe bottom
			Bundle arguments = new Bundle();
			arguments.putString(ItemDetailFragment.ITEM_TYPE, itemType);
			ItemListFlatFragment fragment = new ItemListFlatFragment();
			fragment.setArguments(arguments);			

			// Add the tag to find later, if necessary
			getSupportFragmentManager().beginTransaction().add(R.id.log_activity_layout, fragment, "flatView").commit();
			Log.d("fragging", "Frag ID = " + fragment.getId());
			//getSupportFragmentManager().beginTransaction().replace(listFrag.getId(), fragment).commit();

		}
		else{
			// Hack, but basically adds/removes the new layout while making the list appear
			Fragment listFrag = getSupportFragmentManager().findFragmentById(R.id.item_list);
			listFrag.getView().setVisibility(View.VISIBLE);

			// Remove this flat fragment
			ItemListFlatFragment fragment = (ItemListFlatFragment) getSupportFragmentManager().findFragmentByTag("flatView");
			if(fragment != null){
				getSupportFragmentManager().beginTransaction().remove(fragment).commit();
			}			

		}


	}

	// Handles the fragment switching
	private void switchModeFragment(){
		// Flat files
		if(!isDisplayFragmented){

			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
					LayoutParams.MATCH_PARENT);
			LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0,
					LayoutParams.MATCH_PARENT); 
			lp.weight = 0;
			lp2.weight = 1;

			if(getFragmentManager().findFragmentById(R.id.item_detail_container) != null)
				listViewedFragmentID = getFragmentManager().findFragmentById(R.id.item_detail_container).getArguments().getString(ItemDetailFragment.ARG_ITEM_ID); 

			Bundle arguments = new Bundle();
			arguments.putString(ItemDetailFragment.ITEM_TYPE, itemType);
			ItemListFlatFragment fragment = new ItemListFlatFragment();
			fragment.setArguments(arguments);

			getSupportFragmentManager().beginTransaction()
			.replace(R.id.item_detail_container, fragment).commit();

			Fragment listfrag = getSupportFragmentManager().findFragmentById(R.id.item_list);			
			FrameLayout fl_list = (FrameLayout) listfrag.getView();

			fl_list.setVisibility(View.GONE);



		}

		else{ // Fragmented

			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT); 
			lp.weight = 1.0f;
			lp2.weight = 3.0f;

			Bundle arguments = new Bundle();

			arguments.putString(ItemDetailFragment.ARG_ITEM_ID, "1");

			if(itemType.equals(ItemListFragment.LOG)){
				arguments.putString(ItemDetailFragment.ITEM_TYPE, ItemListFragment.LOG);
			}
			if(itemType.equals(ItemListFragment.COOKIE)){
				arguments.putString(ItemDetailFragment.ITEM_TYPE, ItemListFragment.COOKIE);
			}

			ItemDetailFragment detailFragment = new ItemDetailFragment();
			detailFragment.setArguments(arguments);			

			getSupportFragmentManager().beginTransaction()
			.replace(R.id.item_detail_container, detailFragment).commit();


			Fragment listFrag = getSupportFragmentManager().findFragmentById(R.id.item_list);
			((ItemListFragment)listFrag).clearItemClicked();
			listFrag.getView().setVisibility(View.VISIBLE);	

		}
	}

	private void clearLogFiles(){
		try{
			// Cookies and logs
			if(itemType.equals(ItemListFragment.LOG)){
				//if(itemType.equals(LOG))
				BookmarkDatabase db = new BookmarkDatabase(this.getApplicationContext());
				db.clearLogEntries();

			}else if(itemType.equals(ItemListFragment.COOKIE)){
				//if(itemType.equals(COOKIE))
				PersistentCookieStore cookieStore = new PersistentCookieStore(this.getApplicationContext());
				cookieStore.clear();
			}
			ItemListFragment frag = (ItemListFragment) getSupportFragmentManager().findFragmentById(R.id.item_list);
			frag.clearView();

			LinearLayout ll = (LinearLayout) findViewById(R.id.log_activity_layout);
			ll.removeAllViews();

			TextView tv = new TextView(this);
			tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			tv.setTextSize(17.0f);
			tv.setText("No entries to display!");
			ll.addView(tv);

		}
		catch (Exception e){
			Log.e("Error", "Could not delete log files because " +e.getMessage());
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		ItemListFragment.itemMap.clear();		

		Editor edit = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit();		
		try{
			if(itemType.equals(ItemListFragment.COOKIE)){
				edit.putBoolean(PreferenceStorageUnit.DISPLAY_LAYOUT_COOKIE, isDisplayFragmented);
			}
			else{
				edit.putBoolean(PreferenceStorageUnit.DISPLAY_LAYOUT_LOG, isDisplayFragmented);
			}
			
			edit.apply();
		}catch (Exception e){
			Log.e("Error", "Could not save log layout preference because " + e.getMessage());
		}
		edit = null;
	}


}
