package com.alphasoftware.alpharun.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.text.Html;

public class CookieEntry {
	String cookieURL;
	String cookieValue;
	long cookieDatetime;

	public CookieEntry(){
		cookieURL = "";
		cookieValue = "";
		cookieDatetime = -1;
	}

	public CookieEntry(String url, String value, long datetime){
		this.cookieURL = url;
		this.cookieValue = value;
		this.cookieDatetime = datetime;
	}

	private String convertTime(long time){
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		
		return df.format(new Date(time));

	}

	public String getCookieURL() {
		return cookieURL;
	}

	public void setCookieURL(String cookieURL) {
		this.cookieURL = cookieURL;
	}

	public String getCookieValue() {
		return cookieValue;
	}

	public void setCookieValue(String cookieValue) {
		this.cookieValue = cookieValue;
	}

	public String getCookieDatetime() {
		return convertTime(cookieDatetime);
	}

	public void setCookieDatetime(long cookieDatetime) {
		this.cookieDatetime = cookieDatetime;
	}

	

}
