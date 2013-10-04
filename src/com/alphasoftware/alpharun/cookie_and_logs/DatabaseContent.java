package com.alphasoftware.alpharun.cookie_and_logs;

import java.util.HashMap;
import java.util.List;

import org.apache.http.cookie.Cookie;

import android.content.Context;
import android.util.Log;

import com.alphasoftware.alpharun.utils.BookmarkDatabase;
import com.alphasoftware.alpharun.utils.LogEntry;
import com.loopj.android.http.PersistentCookieStore;

public class DatabaseContent {
	private static List contentList;
	public static HashMap itemMap;
	private static BookmarkDatabase db;
	private PersistentCookieStore myCookieStore;

	public DatabaseContent(Context c, Object contentType){

		db = new BookmarkDatabase(c);

		try{
			// Create the type of the array
			if (contentType instanceof LogEntry){
				contentList = db.getLogEntries();
				itemMap = new HashMap<String, LogEntry>();
			}
			else if (contentType instanceof Cookie){
				myCookieStore = new PersistentCookieStore(c);
				contentList = myCookieStore.getCookies(); // Get the cookies	
				itemMap = new HashMap<String, Cookie>();
			}
			else{
				Log.e("Error", "No match for " + contentType.getClass().toString());
			}
		}catch (Exception e){
			Log.e("Error", "Could not load content list from database for fragments");
			contentList = null;
		}
	}
	
	public Object getContentType(){
		return contentList.getClass();
	}
}
