package com.alphasoftware.alpharun.utils;

import java.util.ArrayList;

import com.alphasoftware.alpharun.pref.PreferenceStorageUnit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BookmarkDatabase extends SQLiteOpenHelper{

	final static String dbName = "bookmarkDB";
	final static String tableName = "bookmarkTable";
	final static String colID = "bookmarkID";
	final static String colOrder = "bookmarkOrder";
	final static String colTitle = "bookmarkTitle";
	final static String colUrl = "bookmarkUrl";
	final static String colUid = "bookmarkUid";
	final static String colScan = "bookmarkScan";
	final static String cookieTableName = "cookieTable";
	final static String cookieID = "cookieID";
	final static String cookieURL = "cookieURL";
	final static String cookieValue = "cookieValue";
	final static String cookieDate = "cookieDate";
	final static String logTableName = "logTable";
	final static String logEntryID = "logEntryID";
	final static String logEntryCommand = "logEntryCommand";
	final static String logEntryResult = "logEntryResult";
	final static String logEntryDate = "logEntryDate";

	Context c;
	private static PreferenceStorageUnit psu;

	// Creates the database helper object
	public BookmarkDatabase(Context context) {
		super(context, dbName, null, 35);
		c = context;
		psu = new PreferenceStorageUnit(context);
	}

	@Override
	public synchronized void close() {
		// TODO Auto-generated method stub
		super.close();
	}

	@Override
	public synchronized SQLiteDatabase getReadableDatabase() {
		// TODO Auto-generated method stub
		return super.getReadableDatabase();
	}

	@Override
	public synchronized SQLiteDatabase getWritableDatabase() {
		// TODO Auto-generated method stub
		return super.getWritableDatabase();
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		super.onOpen(db);
	}

	public String getTableName(){
		return tableName;
	}

	@Override
	public String getDatabaseName() {
		// TODO Auto-generated method stub
		return super.getDatabaseName();
	}


	@Override
	public void onCreate(SQLiteDatabase db) {
		// Create the database
		String sql = "CREATE TABLE " + tableName + " (" + 
				colID +	" INTEGER PRIMARY KEY AUTOINCREMENT, " +
				colOrder +	" INTEGER, " +
				colTitle + " TEXT, " +
				colUrl + " TEXT, " + 
				colUid + " TEXT, " + 
				colScan + " INTEGER)";

		String sql2 = "CREATE TABLE " + cookieTableName + " (" + cookieID + 
				" INTEGER PRIMARY KEY AUTOINCREMENT, " + 
				cookieURL + " TEXT, " +
				cookieValue + " TEXT, " +
				cookieDate + " NUMERIC)";

		String sql3 = "CREATE TABLE " + logTableName + " (" + logEntryID + 
				" INTEGER PRIMARY KEY AUTOINCREMENT, " + 
				logEntryCommand + " TEXT, " +
				logEntryResult + " TEXT, " +
				logEntryDate + " NUMERIC)";
		try{
			db.execSQL(sql);
			db.execSQL(sql2);
			db.execSQL(sql3);
		}catch (Exception e){
			e.printStackTrace();
		}

	}

	// Add a bookmark to the database
	public long insertBookmark(BookmarkData bookmark){		
		long id;

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues cv = new ContentValues();

		cv.put(colTitle, bookmark.sTitle);
		cv.put(colOrder, bookmark.iOrder);
		cv.put(colUrl, bookmark.sUrl);
		cv.put(colUid, bookmark.sUid);
		cv.put(colScan, bookmark.bScan);

		try{
			id = db.insert(tableName, null, cv);

		}catch (Exception e){			
			id = -1;
			Log.e("DB Error", e.getMessage());
		}

		db.close();
		return id;
	}

	// Update the database
	public int updateBookmark(BookmarkData bookmark){
		int updated = 0;

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues cv = new ContentValues();

		cv.put(colTitle, bookmark.sTitle);
		cv.put(colOrder, bookmark.iOrder);
		cv.put(colUrl, bookmark.sUrl);
		cv.put(colUid, bookmark.sUid);
		cv.put(colScan, bookmark.bScan);

		try{
			updated = db.update(tableName, cv, colID + " = " + bookmark.index, null);				
		}catch (Exception e){
			Log.e("Error", "Error updating database because: " + e.getMessage());
		}
		db.close();

		return updated;
	}

	public int deleteBookmark(BookmarkData bookmark){
		SQLiteDatabase db = this.getWritableDatabase();

		int removed = db.delete(tableName, colID + " = " + bookmark.index, null);

		ArrayList<BookmarkData> bookmarks = getBookmarks();

		try{
			for(BookmarkData dat : bookmarks){
				if(dat.iOrder > bookmark.iOrder){
					String sql = String.format("UPDATE %s SET %s = %s WHERE %s = %s", tableName, colOrder, String.format("%d", dat.iOrder-1), colID, dat.index); 
					db.execSQL(sql);
				}
			}
			//db.rawQuery("Update ? SET ? = (SELECT ? FROM ? WHERE ? = ?)+1 WHERE ? = ?", new String[]{tableName, colOrder, colOrder, tableName, colID, String.valueOf(bookmark.index), colID, String.valueOf(bookmark.index)});			
		}catch (Exception e){
			Log.e("Error", "Error updating the database on delete because: " + e.getMessage());
		}
		db.close();

		return removed;
	}

	public ArrayList<BookmarkData> getBookmarks(){
		SQLiteDatabase db = this.getReadableDatabase();
		ArrayList<BookmarkData> bookmarks = new ArrayList<BookmarkData>();

		// Get the bookmarks
		Cursor cur = db.rawQuery("SELECT * FROM " + tableName + " ORDER BY " + colOrder + " ASC", null);

		cur.moveToFirst();

		// Cycle through the bookmarks
		while(!cur.isAfterLast()){
			BookmarkData dat = new BookmarkData();
			dat.index = cur.getInt(0); // Unique ID
			dat.iOrder = cur.getInt(1); // List Order
			dat.sTitle = cur.getString(2); // Title
			dat.sUrl = cur.getString(3); // URL
			dat.sUid =  cur.getString(4);
			dat.bScan = (cur.getInt(5) == 0) ? false : true; // Scan yes or no			
			bookmarks.add(dat);

			cur.moveToNext();
		}	

		return bookmarks;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	public long insertCookie(String url, String cookie){		

		long id;
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues cv = new ContentValues();

		cv.put(cookieURL, url);
		cv.put(cookieValue, cookie);
		cv.put(cookieDate, System.currentTimeMillis());


		try{
			id = db.insert(cookieTableName, null, cv);

		}catch (Exception e){			
			id = -1;
			Log.e("DB Error", e.getMessage());
		}

		db.close();

		return id;
	}

	public ArrayList<CookieEntry> getCookies(){
		SQLiteDatabase db = this.getReadableDatabase();
		ArrayList<CookieEntry> cookieEntries = new ArrayList<CookieEntry>();

		// Get the bookmarks
		Cursor cur = db.rawQuery("SELECT * FROM " + cookieTableName + " ORDER BY " + cookieID + " DESC", null);

		cur.moveToFirst();

		// Cycle through the bookmarks
		while(!cur.isAfterLast()){
			CookieEntry entry = new CookieEntry();
			entry.cookieURL = cur.getString(1);
			entry.cookieValue = cur.getString(2);
			entry.cookieDatetime = cur.getLong(3);
			cookieEntries.add(entry);

			cur.moveToNext();
		}	

		return cookieEntries;
	}

	// Clear out all log entries
	public void clearCookies(){
		SQLiteDatabase db = this.getWritableDatabase();

		db.delete(cookieTableName, null, null);

		db.close();

		return;
	}

	public long insertLog(String command, String result){

		Log.d("JSLog", "Logging");
		long id;
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues cv = new ContentValues();

		cv.put(logEntryCommand, command);
		cv.put(logEntryResult, result);
		cv.put(logEntryDate, System.currentTimeMillis());


		try{
			id = db.insert(logTableName, null, cv);

		}catch (Exception e){			
			id = -1;
			Log.e("DB Error", e.getMessage());
		}

		db.close();

		return id;
	}

	public ArrayList<LogEntry> getLogEntries(){
		SQLiteDatabase db = this.getReadableDatabase();
		ArrayList<LogEntry> logEntries = new ArrayList<LogEntry>();

		// Get the bookmarks
		Cursor cur = db.rawQuery("SELECT * FROM " + logTableName + " ORDER BY " + logEntryID + " DESC", null);

		cur.moveToFirst();

		// Cycle through the bookmarks
		while(!cur.isAfterLast()){
			LogEntry entry = new LogEntry();
			entry.logCommand = cur.getString(1);
			entry.logResult = cur.getString(2);
			entry.logDatetime = cur.getLong(3);
			logEntries.add(entry);

			cur.moveToNext();
		}	

		return logEntries;
	}

	// Clear out all log entries
	public void clearLogEntries(){
		SQLiteDatabase db = this.getWritableDatabase();

		db.delete(logTableName, null, null);

		db.close();

		return;
	}



}
