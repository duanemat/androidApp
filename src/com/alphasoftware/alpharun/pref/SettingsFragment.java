package com.alphasoftware.alpharun.pref;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.webkit.WebStorage;
import android.webkit.WebView;

import com.alphasoftware.alpharun.R;
import com.alphasoftware.alpharun.main.CordovaWebViewActivity;
import com.alphasoftware.alpharun.main.WebViewActivity;
import com.alphasoftware.alpharun.utils.BookmarkDatabase;
import com.alphasoftware.alpharun.utils.LogEntry;
import com.loopj.android.http.PersistentCookieStore;

public class SettingsFragment extends PreferenceFragment {

	private static final String PAST_JS_COMMANDS = "past_js";
	private static final Integer NUM_OLD_JS_COMMANDS = 5;

	private static PersistentCookieStore myCookieStore;
	private static BookmarkDatabase bkdb;
	private SharedPreferences sharedPrefs;
	private Set<String> jsCommands;
	private ArrayList<String> jsCommandList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preference_main);

		Preference myPref = findPreference("clear_cache_preference");

		getCacheSize();

		// Get the cookies for updating the Preferences Cookie screen
		myCookieStore = new PersistentCookieStore(this.getActivity());

		getNumCookies();

		// Get the stored log files
		bkdb = new BookmarkDatabase(this.getActivity().getApplicationContext());

		getLastLogEntry();

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
		//sharedPrefs.edit().remove(PAST_JS_COMMANDS).apply();

		myPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				WebStorage.getInstance().deleteAllData();
				WebView webview = WebViewActivity.webview;
				if(webview != null){
					webview.clearCache(true);
					webview.clearHistory();
				}

				webview = CordovaWebViewActivity.webview;
				if(webview != null){
					webview.clearCache(true);
					webview.clearHistory();
				}
				getCacheSize();
				return false;
			}

		});

		// Sets the input type to URI format
		EditTextPreference edt = (EditTextPreference) findPreference("url_variable");
		edt.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_URI);

		// Update the javascript section with most recent commands
		loadJSPastEntries();


	}

	private void loadJSPastEntries(){
		
		// Get the known preferences from the list
		ListPreference JSPreference = (ListPreference) findPreference("pastJS_commands");
		jsCommands = sharedPrefs.getStringSet(SettingsFragment.PAST_JS_COMMANDS, new LinkedHashSet<String>());
		
		// Clear out the value so that none are selected on start-up.
		JSPreference.setValue("");
		
		// Get them in a list so that I can sort them
		jsCommandList = new ArrayList<String>(SettingsFragment.NUM_OLD_JS_COMMANDS);

		if(jsCommands.size() > 0){
			JSPreference.setEnabled(true);
			
			// Order the commands
			// Each old command has an order attached to it.  Ex. 0alert("hi") would be first position.  Basically, I read each entry in the list and order them appropriately.
			
			String[] strArr = new String[jsCommands.size()];
			
			for(String command : jsCommands){
				int pos = (Integer.valueOf(String.valueOf(command.charAt(0))));
				strArr[pos] = command.substring(1);
			}
			
			jsCommandList.addAll(Arrays.asList(strArr));
			
			JSPreference.setEntries(strArr);
			JSPreference.setEntryValues(strArr);
		}else{
			// Just disable JSPreference
			JSPreference.setEnabled(false);
		}
		
		JSPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference, Object newValue) {
				EditTextPreference edtp = (EditTextPreference)findPreference("javascript_text");
				
				
				if(newValue != null){
					edtp.setText((String)newValue); // Change the text and it will save when you leave
					return true;
				}
				else
					return false;
			}
		});
		

	}

	private void getCacheSize(){
		Preference myPref = findPreference("clear_cache_preference");

		// Get the number of files in storage
		File[] files = this.getActivity().getCacheDir().listFiles();
		long size = 0;
		for (File f:files) {
			size = size+f.length();
			f.delete();
		}

		float sizeKB = size / 1024.0f;
		myPref.setSummary("Total cache: " + String.format("%1.2f", sizeKB) + " KB");

		return;
	}

	// Get the number of cookies and put in summary
	private void getNumCookies(){
		int numCookies = myCookieStore.getCookies().size();

		Preference myCookies = findPreference("cookies_preference");
		myCookies.setSummary(numCookies + " cookies");

		return;
	}

	private void getLastLogEntry(){
		ArrayList<LogEntry> entries = bkdb.getLogEntries();

		LogEntry firstEntry = null;
		if(entries.size() > 0)
			firstEntry = entries.get(0);

		if(firstEntry != null){
			Preference logPref = findPreference("logfiles_preference");
			logPref.setSummary("Last log item: " + firstEntry.getLogDatetime());
		}
		else{
			Preference logPref = findPreference("logfiles_preference");
			logPref.setSummary("No entries");
		}

		return;
	}

	@Override
	public void onResume() {

		// Update the cookies and log entries
		myCookieStore = new PersistentCookieStore(this.getActivity()); // Get new cookies reference
		getNumCookies();

		getLastLogEntry();

		super.onResume();

	}
	
	@Override
	public void onDestroy(){
		
		super.onDestroy();
		
		// Look at the text entered
		EditTextPreference edtp = (EditTextPreference)findPreference("javascript_text");
		if(edtp.getText() == null)
			return;
		
		String currentCommand = edtp.getText().toString().trim();
		
		if(currentCommand.length() == 0)
			return; // Do nothing
				
		if(jsCommandList.contains(currentCommand)){			
			jsCommandList.remove(currentCommand);
			jsCommandList.add(0, currentCommand);
		}else{ // Add to the bottom
			jsCommandList.add(0, currentCommand);
			
			if(jsCommandList.size() > SettingsFragment.NUM_OLD_JS_COMMANDS){
				while(jsCommandList.size() > SettingsFragment.NUM_OLD_JS_COMMANDS){
					jsCommandList.remove((int)SettingsFragment.NUM_OLD_JS_COMMANDS);
				}
			}
		}
		
		for(int i=0; i<jsCommandList.size(); i++){
			String str = i+jsCommandList.get(i);
			jsCommandList.remove(i);
			jsCommandList.add(i, str);
		}
		
		// Update the saved commands
		sharedPrefs.edit().putStringSet(SettingsFragment.PAST_JS_COMMANDS, new LinkedHashSet<String>(jsCommandList)).apply();
	}


}
