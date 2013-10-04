package com.alphasoftware.alpharun.cookie_and_logs;

import java.util.ArrayList;
import java.util.Date;

import org.apache.http.cookie.Cookie;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alphasoftware.alpharun.R;
import com.alphasoftware.alpharun.utils.BookmarkDatabase;
import com.alphasoftware.alpharun.utils.LogEntry;
import com.loopj.android.http.PersistentCookieStore;

public class ItemListFlatFragment extends Fragment{

	private String itemType;
	// Mandatory empty fragment 
	public ItemListFlatFragment(){		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		
		Log.d("tracking", this.getClass().toString());
		itemType = getArguments().getString(ItemDetailFragment.ITEM_TYPE); // Type of data to display
	}



	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		LogTask lt = new LogTask(this.getActivity().getApplicationContext(), itemType);
		lt.execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.logfileview_flat, container, false);

		return view;

	}

	private class LogTask extends AsyncTask<Void, Void, Void>{

		private Context c; 	
		private Activity a;
		private ProgressBar progressBar;
		private ArrayList entries;		
		private BookmarkDatabase bkdb;
		private PersistentCookieStore cookieStore;
		private TextView tv;
		private StringBuilder logStr = new StringBuilder();
		private String itemType;

		public LogTask(Context c, String itemType){
			this.c = c;	
			if(itemType.equals(ItemListFragment.LOG))
				bkdb = new BookmarkDatabase(c);
			else if (itemType.equals(ItemListFragment.COOKIE))
				cookieStore = new PersistentCookieStore(c);

			this.itemType = itemType;
		}

		@Override
		protected void onPreExecute() {
			progressBar = (ProgressBar)getActivity().findViewById(R.id.progressBar_log);

			tv = (TextView) getActivity().findViewById(R.id.log_text);
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			progressBar.setVisibility(View.GONE);
			tv.setTextIsSelectable(true);
			tv.setTextSize(17.0f);
			tv.setText(logStr);
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			Log.d("progress", "Progressing");
			tv.setText(logStr);
		}

		@Override
		protected Void doInBackground(Void... params) {


			// Get the entries
			if(itemType.equals(ItemListFragment.LOG))
				entries = bkdb.getLogEntries();
			else if (itemType.equals(ItemListFragment.COOKIE))
				entries = (ArrayList) cookieStore.getCookies();


			if(entries.size() > 0){


				for(int i=0; i<entries.size(); i++){
					// Log entries
					if(itemType.equals(ItemListFragment.LOG)){
						LogEntry e = (LogEntry) entries.get(i);					
						StringBuilder str = new StringBuilder();
						str.append("JAVASCRIPT: " + e.getLogDatetime() + "\n");
						str.append(e.getLogCommand()+"\n");
						str.append("Result\n----\n" + e.getLogResult() + "\n**********\n");					
						logStr.append(str);						
					}
					// Cookies
					else if(itemType.equals(ItemListFragment.COOKIE)){
						Cookie cookie = (Cookie) entries.get(i);
						Date expiration = cookie.getExpiryDate();
						String expirationDate;
						if (expiration == null)
							expirationDate = "null";
						else
							expirationDate = expiration.toGMTString();

						String str = "DOMAIN:  " + cookie.getDomain() + "\n" +
								"NAME:      " + cookie.getName() + "\n" + 
								"VALUE:     " + cookie.getValue() + "\n" +
								"EXPIRES: " + expirationDate + "\n**********\n";
						logStr.append(str);
					}
				}
				
				


			} else{

				StringBuilder str = new StringBuilder();
				str.append("No entries to display!\n");
				logStr.append(str);
			}


			return null;
		}
	}




}
