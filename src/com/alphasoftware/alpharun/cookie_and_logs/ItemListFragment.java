package com.alphasoftware.alpharun.cookie_and_logs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.cookie.Cookie;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alphasoftware.alpharun.R;
import com.alphasoftware.alpharun.utils.BookmarkDatabase;
import com.alphasoftware.alpharun.utils.LogEntry;
import com.loopj.android.http.PersistentCookieStore;

/**
 * A list fragment representing a list of Items. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link ItemDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ItemListFragment extends ListFragment {

	public static final String COOKIE = "COOKIE";
	public static final String LOG = "LOG";
	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */

	public ArrayList<String> itemListID = new ArrayList<String>();
	public static Map<String, LogItem> itemMap = new HashMap<String, LogItem>();
	private ArrayAdapter aa;

	// References to the menu items
	private MenuItem clearFilesMenu, switchViewMenu;

	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(String id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
			Log.d("tracking", "Callback!");
		}
	};

	private Handler handle = new Handler(){
		@Override
		public void handleMessage(Message msg){ // Update the screen
			ListView lv = getListView();			
			BaseAdapter ab = (BaseAdapter) lv.getAdapter();
			ab.notifyDataSetChanged();
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ItemListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		Log.d("tracking", this.getClass().toString());
		super.onCreate(savedInstanceState);

		aa = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_activated_1,
				android.R.id.text1, itemListID);

		// Use the generic ArrayAdapter
		setListAdapter(aa);		
		setHasOptionsMenu(true);
	}



	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		// Check for the data
		DataTask dt = new DataTask(this.getActivity().getApplicationContext(), this.getActivity());
		dt.execute();


	}

	public void clearView(){
		itemListID.clear();
		itemMap.clear();
		DataTask dt = new DataTask(this.getActivity().getApplicationContext(), this.getActivity());
		dt.execute();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		aa = null;

	}

	private void addItem(LogItem item){
		itemMap.put(item.id, item);
		itemListID.add(item.id);
		
		// Update the menu
		updateMenuItem(clearFilesMenu, true);
		updateMenuItem(switchViewMenu, true);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}		
	}



	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mCallbacks.onItemSelected(itemListID.get(position));
	}



	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);

		clearFilesMenu = menu.findItem(R.id.clearFiles);
		switchViewMenu = menu.findItem(R.id.switchModes);

		Log.d("tracking", itemListID.size()+"");
		if(getListAdapter().getCount() == 0){
			updateMenuItem(clearFilesMenu, false);
			updateMenuItem(switchViewMenu, false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()){

		case R.id.clearFiles:			
			updateMenuItem(item, false);
			break;

			// Switch between tiered and flat screen
		case R.id.switchModes:
			break;
		}

		return super.onOptionsItemSelected(item);


	}

	private void updateMenuItem(MenuItem item, boolean enabled){
		if(item != null){
			Drawable originalIcon = item.getIcon();		    
			Drawable icon = convertDrawableToGrayScale(originalIcon, !enabled);
			item.setIcon(icon);
			item.setEnabled(enabled);
		}
	}

	private static Drawable convertDrawableToGrayScale(Drawable drawable, boolean makeGray) {
		if (drawable == null) {
			return null;
		}
		Drawable res = drawable.mutate();
		if(makeGray)
			res.setColorFilter(Color.GRAY, Mode.SRC_IN);
		else
			res.clearColorFilter();

		return res;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	// Clears all of the clicked entries
	public void clearItemClicked(){
		ListView lv = getListView();

		try{
			for(int i=0; i<lv.getAdapter().getCount(); i++){
				lv.setItemChecked(i, false);
			}
		}catch (Exception e){
			Log.e("error", "Could not clear items clicked because " + e.getMessage());
		}
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}

	public static class LogItem {
		public String id;
		public String content;

		public LogItem(String id, String content) {
			this.id = id;
			this.content = content;
		}

		@Override
		public String toString() {
			return content;
		}
	}

	private class DataTask extends AsyncTask<Void, LogItem, Void>{


		private Context c; 	
		private Activity a;
		private String itemType;
		private BookmarkDatabase db;
		private PersistentCookieStore cookieStore;
		private ProgressBar progBar;

		public DataTask(Context c, Activity a){
			this.c = c;
			this.a = a;
		}

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			itemType = a.getIntent().getDataString(); // Used to figure out which data to request

			//if(itemType.equals(LOG))
			db = new BookmarkDatabase(c);

			//if(itemType.equals(COOKIE))
			cookieStore = new PersistentCookieStore(c);

			Log.d("debug", getActivity().toString());
			progBar = (ProgressBar) getActivity().findViewById(R.id.log_progress_bar);

		}

		@Override
		protected void onProgressUpdate(LogItem... values) {

			super.onProgressUpdate(values);

			addItem(values[0]);
		}

		@Override
		protected Void doInBackground(Void... params) {

			// Run it once and done
			if(itemListID.size() == 0){
				ArrayList<LogEntry> arr = db.getLogEntries();

				// Cycle through the entries and parse them into the LogItems
				for(LogEntry le : arr){
					try{
						StringBuilder str = new StringBuilder();

						str.append(le.getLogDatetime() + "\n");
						str.append(le.getLogCommand()+"\n");
						str.append("Result\n----\n" + le.getLogResult() + "\n----\n");
						if(itemType.equals(ItemListFragment.LOG)){
							//addItem(new LogItem(le.getLogDatetime(), str.toString()));
							publishProgress(new LogItem(le.getLogDatetime(), str.toString()));
						}

					}catch (Exception e){
						Log.e("Error", "Error adding LogEntry to LogItem because " + e.getMessage());
					}
				}

				ArrayList<Cookie> cookies = (ArrayList<Cookie>) cookieStore.getCookies();

				for(Cookie cookie : cookies){
					// Parse each cookie into a viewable object
					try{
						Date expiration = cookie.getExpiryDate();
						String expirationDate;
						if (expiration == null)
							expirationDate = "null";
						else
							expirationDate = expiration.toGMTString();

						String str = "DOMAIN:  " + cookie.getDomain() + "\n" +
								"NAME:      " + cookie.getName() + "\n" + 
								"VALUE:     " + cookie.getValue() + "\n" +
								"EXPIRES: " + expirationDate;

						if(itemType.equals(ItemListFragment.COOKIE)){
							//addItem(new LogItem(cookie.getDomain(), str));
							publishProgress(new LogItem(cookie.getDomain(), str));
						}
					}catch (Exception e){
						Log.e("Error", "Error adding Cookie to LogItem because " + e.getMessage());
					}
				}
				//handle.sendEmptyMessage(1);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			ListView lv = getListView();			
			BaseAdapter ab = (BaseAdapter) lv.getAdapter();
			ab.notifyDataSetChanged();

			// If we are empty, just show the "no entries" page
			if (ab.getCount() == 0){
				LinearLayout ll = (LinearLayout) a.findViewById(R.id.log_activity_layout);
				//ll.removeAllViews();
				for(int i=0; i<ll.getChildCount(); i++){
					ll.getChildAt(i).setVisibility(View.GONE);
				}

				TextView tv = new TextView(a);
				tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				tv.setTextSize(17.0f);
				tv.setText("No entries to display!");		

				ll.addView(tv);

				updateMenuItem(clearFilesMenu, false);
				updateMenuItem(switchViewMenu, false);

			}
			if(progBar != null){
				progBar.setVisibility(View.GONE);
			}

			super.onPostExecute(result);
		}

	}
}
