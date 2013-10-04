package com.alphasoftware.alpharun.pref;

import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.alphasoftware.alpharun.main.MainActivity;

// Little helper class that stores all of the preference data

public class PreferenceStorageUnit {

	private static HashMap<String, Object> prefMap = null;
	private Context c;
	public static final String TOUCH_HIGHLIGHTING = "touch_highlighting_preference";
	public static final String STATUS_BAR = "status_bar_preference";
	public static final String TOOLBAR = "toolbar_preference";
	public static final String PHONEGAP = "phonegap_preference";
	public static final String LOG_ERRORS = "log_errors_preference";
	public static final String URL_VARIABLE = "url_variable";
	public static final String JAVASCRIPT = "javascript_text";
	public static final String TOOLBAR_POSITION_LANDSCAPE = "toolbar_position_landscape";
	public static final String TOOLBAR_POSITION_PORTRAIT = "toolbar_position_portrait";
	public static final String DISPLAY_LAYOUT_COOKIE = "display_layout_COOKIE";
	public static final String DISPLAY_LAYOUT_LOG = "display_layout_LOG";
	private String [] prefArr = new String[] { 	"touch_highlighting_preference", "status_bar_preference", 
			"toolbar_preference", "phonegap_preference", "log_errors_preference",
			"url_variable", "javascript_text"};


	public PreferenceStorageUnit(Context c){
		if (prefMap == null){
			prefMap = new HashMap<String, Object>();
		}
		this.c = c;
	}

	public void setPreference(String key, Object val){
		prefMap.put(key, val);

	}

	public Object getPreference(String key){

		return prefMap.get(key);
	}

	// Updates the location values to the SharedPreferences controller
	public void setLocations(){
		Editor edit = PreferenceManager.getDefaultSharedPreferences(c).edit();		
		try{
			edit.putString(PreferenceStorageUnit.TOOLBAR_POSITION_LANDSCAPE, String.valueOf(prefMap.get(TOOLBAR_POSITION_LANDSCAPE)));
			edit.putString(PreferenceStorageUnit.TOOLBAR_POSITION_PORTRAIT, String.valueOf(prefMap.get(TOOLBAR_POSITION_PORTRAIT)));
			edit.apply();
		}catch (Exception e){
			Log.e("Error", "Could not save toolbar positions properly " + e.getMessage());
		}
		edit = null;
	}

	// Update all of the preferences using the default application context
	public boolean updateAllPreferences(){
		boolean goodUpdate = true;

		try{
			// Get the known preferences from the list
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);

			for(String p : prefArr){
				if(p.equals(URL_VARIABLE) || p.equals(JAVASCRIPT)){
					this.setPreference(p, pref.getString(p, ""));
				}
				else{
					this.setPreference(p, pref.getBoolean(p, false));
				}
				Log.d("Prefs", p + " = " + prefMap.get(p));
			}
		}
		catch (Exception e){
			Log.e("Error", e.getMessage());
			goodUpdate = false;
		}
		return goodUpdate;
	}


}
