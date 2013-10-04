package com.alphasoftware.alpharun.pref;

import java.util.Date;
import java.util.List;

import org.apache.http.cookie.Cookie;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alphasoftware.alpharun.R;
import com.alphasoftware.alpharun.utils.BookmarkDatabase;
import com.loopj.android.http.PersistentCookieStore;

public class CookiesPreference extends Activity {
	private static BookmarkDatabase bkdb;
	private PersistentCookieStore myCookieStore;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		bkdb = new BookmarkDatabase(this.getApplicationContext());

		setContentView(R.layout.logfileview);
		myCookieStore = new PersistentCookieStore(this);

		CookieTask logTask = new CookieTask(this);
		logTask.execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.logmenu, menu);		
		return true;
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()){

		case R.id.clearFiles:
			clearLogFiles();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void clearLogFiles(){
		try{
			if (bkdb != null){
				//bkdb.clearCookies();
				myCookieStore.clear();
				CookieTask logTask = new CookieTask(this);
				logTask.execute();
			}
		}
		catch (Exception e){
			Log.e("Error", "Could not delete cookies because " +e.getMessage());
		}
	}

	private class CookieTask extends AsyncTask<Void, TextView, Void>{

		private Context c; 		
		private ProgressBar progressBar;
		private List<Cookie> cookies;
		private LinearLayout.LayoutParams llp;
		private int progressValue; 
		private LinearLayout ll;
		private TextView progressText;

		public CookieTask(Context c){
			this.c = c;
		}

		@Override
		protected void onPreExecute() {
			progressBar = (ProgressBar) findViewById(R.id.progressBar_log);

			ll = (LinearLayout)findViewById(R.id.logLayoutID);

			progressText = (TextView)findViewById(R.id.progressBar_log_text);
			progressText.setText("Fetching cookies...");

			int childCount = ll.getChildCount();

			if(childCount > 0){
				ll.removeAllViewsInLayout(); // Remove all of the child layouts
			}


			llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			llp.setMargins(10, 0, 0, 0); // llp.setMargins(left, top, right, bottom);

			progressValue = 0;

			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			progressBar.setVisibility(View.GONE);
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(TextView... values) {

			TextView tv = values[0];

			// Means you should remove the text b/c we are starting to display
			progressText.setVisibility(View.GONE);
			tv.setTextIsSelectable(true);

			ll.addView(tv);

			TextView blank = new TextView(c);
			blank.setText("");
			ll.addView(blank);

			Log.d("progress", "Progress = " + progressValue);
			progressBar.setProgress(progressValue);

		}

		@Override
		protected Void doInBackground(Void... params) {


			// Get the cookies
			cookies = myCookieStore.getCookies();		

			if(cookies.size() > 0){
				for(int i=0; i<cookies.size(); i++){

					Cookie cookie = cookies.get(i);

					// Create Domain
					TextView cookieView = new TextView(c);

					Date expiration = cookie.getExpiryDate();
					String expirationDate;
					if (expiration == null)
						expirationDate = "null";
					else
						expirationDate = expiration.toGMTString();

					cookieView.setText(Html.fromHtml("<b>DOMAIN:</b>&nbsp;&nbsp;" + cookie.getDomain() + "<br/>" +
							"<b>NAME:</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + cookie.getName() + "<br/>" + 
							"<b>VALUE:</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + cookie.getValue() + "<br/> " +
							"<b>EXPIRES:</b> " + expirationDate));

					Float progVal = (float)(i+1)/(float)cookies.size();
					progressValue = (int)(progVal * 100);
					Log.d("progress", "Background progress = " + progressValue);
					publishProgress(cookieView);

				}
			} else{
				TextView tv = new TextView(c);
				StringBuilder str = new StringBuilder();
				str.append("No cookies to display!\n");
				tv.setText(str);

				progressValue = 100;
				publishProgress(tv);
			}


			return null;
		}
	}


}
