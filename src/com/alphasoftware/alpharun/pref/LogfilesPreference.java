package com.alphasoftware.alpharun.pref;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.alphasoftware.alpharun.utils.LogEntry;

public class LogfilesPreference extends Activity {


	private static BookmarkDatabase bkdb;
	private ProgressBar progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		bkdb = new BookmarkDatabase(this.getApplicationContext());

		setContentView(R.layout.logfileview);

		/*Runnable r = new Runnable() {
			
			public void run() {
				populateLogEntries();
				
			}
		};
		
		new Thread(r).start();*/
		LogfileTask logTask = new LogfileTask(this);
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

	private void clearLogFiles(){
		try{
			if (bkdb != null){
				bkdb.clearLogEntries();
				//populateLogEntries();
				LogfileTask logTask = new LogfileTask(this);
				logTask.execute();
				
			}
		}
		catch (Exception e){
			Log.e("Error", "Could not delete log files because " +e.getMessage());
		}
	}

	private class LogfileTask extends AsyncTask<Void, TextView, Void>{

		private Context c; 		
		private ProgressBar progressBar;
		private ArrayList<LogEntry> entries;
		private LinearLayout.LayoutParams llp;
		private int progressValue; 
		private LinearLayout ll;
		private TextView progressText;
		
		public LogfileTask(Context c){
			this.c = c;
		}
		
		@Override
		protected void onPreExecute() {
			progressBar = (ProgressBar) findViewById(R.id.progressBar_log);
			
			ll = (LinearLayout)findViewById(R.id.logLayoutID);
			
			progressText = (TextView)findViewById(R.id.progressBar_log_text);
			progressText.setText("Fetching log entries...");

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
			
			Log.d("progress", "Progress = " + progressValue);
			progressBar.setProgress(progressValue);
			
		}

		@Override
		protected Void doInBackground(Void... params) {
			

			// Get the entries
			entries = bkdb.getLogEntries();
			
			if(entries.size() > 0){

				//for(LogEntry e: entries){
				for(int i=0; i<entries.size(); i++){
					LogEntry e = entries.get(i);
					TextView tv = new TextView(c);
					tv.setLayoutParams(llp);
					StringBuilder str = new StringBuilder();
					str.append("JAVASCRIPT: " + e.getLogDatetime() + "\n");
					str.append(e.getLogCommand()+"\n");
					str.append("Result\n----\n" + e.getLogResult() + "\n----\n");
					tv.setText(str);
					
					
					Float progVal = (float)(i+1)/(float)entries.size();
					progressValue = (int)(progVal * 100);
					Log.d("progress", "Background progress = " + progressValue);
					publishProgress(tv);
				}

				
			} else{
				TextView tv = new TextView(c);
				tv.setLayoutParams(llp);				
				StringBuilder str = new StringBuilder();
				str.append("No log file entries to display!\n");
				tv.setText(str);
				
				progressValue = 100;
				publishProgress(tv);
			}
			
			
			return null;
		}
	}

}
