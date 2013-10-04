package com.alphasoftware.alpharun.main;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.Window;

import com.alphasoftware.alpharun.pref.SettingsFragment;
import com.loopj.android.http.PersistentCookieStore;

public class PreferencesActivity extends PreferenceActivity{

	public final static int PREFERENCE_REQUEST = 5;
	public final static String PREFERENCE_CALLER = "CALLER";
	private Intent incomingIntent;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		incomingIntent = getIntent();

		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
		.replace(android.R.id.content, new SettingsFragment())
		.commit();
				

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			
			// Figure out how to respond
			String caller = incomingIntent.getStringExtra(PreferencesActivity.PREFERENCE_CALLER);
			if(caller == "MainActivity")
				NavUtils.navigateUpFromSameTask(this);
			else
				onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// When you leave with the preference
	@Override
	public void onBackPressed() {

		Intent returnIntent = new Intent();
		returnIntent.putExtra("data", "Completed");

		this.setResult(returnIntent.getIntExtra("requestCode", PreferencesActivity.PREFERENCE_REQUEST), returnIntent);	
		finish();
	}


}
