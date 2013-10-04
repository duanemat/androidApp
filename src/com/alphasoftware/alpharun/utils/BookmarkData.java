package com.alphasoftware.alpharun.utils;

import android.util.Log;

// The data object for storing a bookmark element
public class BookmarkData {
	public String sUrl;
	public String sTitle;
	public String sUid;
	public Integer iOrder;
	public boolean bScan;
	public long index;
	
	
	/**
	 * Creates an empty Bookmark entity for storing a link
	 */
	public BookmarkData(){
		sUrl = "";
		sTitle = "";
		sUid = "";
		bScan = false;
		index = -1;
		iOrder = -1;
	}
	
	/**
	 * Creates a Bookmark entity initialized with the provided values
	 * @param title - The title for the bookmark
	 * @param url - The url for the bookmark 
	 * @param uid - The bookmark's UID
	 * @param button_scan - Whether or not the url can be updated from the bookmark list
	 */
	public BookmarkData(String title, String url, String uid, boolean button_scan){
		this.sTitle = title;
		this.sUrl = url;
		this.sUid = uid;
		this.bScan = button_scan;
		index = -1;
		iOrder = -1;
	}
	
	
	// Override for returning as a string
	/**
	 * Returns a string representation of a bookmark object
	 * @return A string version of this object
	 */
	@Override
	public String toString(){
		String str;
		
		str = index + ";" + sTitle + ";" + iOrder + ";" + sUrl + ";" + sUid + ";";
		str += (bScan == true) ? "1;" : "0;"; 
		return str;
	}
	
	public boolean copyFrom(BookmarkData fromBookmark){
		boolean valid = true;
		try{
			this.sTitle = fromBookmark.sTitle;
			this.sUid = fromBookmark.sUid;
			this.sUrl = fromBookmark.sUrl;
			this.bScan = fromBookmark.bScan;
			this.index = fromBookmark.index;
			this.iOrder = fromBookmark.iOrder;
			
		}catch (Exception e){
			Log.e("ErrorCopying", e.getMessage());
			valid = false;
		}
		
		return valid;
	}
}
