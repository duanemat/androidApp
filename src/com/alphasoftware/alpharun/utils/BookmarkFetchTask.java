package com.alphasoftware.alpharun.utils;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

public class BookmarkFetchTask extends AsyncTask<Void, Integer, ArrayList<BookmarkData>> {

	ArrayList<BookmarkData> bookmarks;
	BookmarkDatabase bookmarksDB;
	Context c;
	
	public BookmarkFetchTask(Context c){
		this.c = c;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		bookmarks = new ArrayList<BookmarkData>();
		bookmarksDB = new BookmarkDatabase(c);
	}

	@Override
	protected ArrayList<BookmarkData> doInBackground(Void... params) {
		SQLiteDatabase db = bookmarksDB.getReadableDatabase();
		
		// Get the bookmarks
		Cursor cur = db.rawQuery("SELECT * FROM " + bookmarksDB.tableName, null);
		
		cur.moveToFirst();
		
		// Cycle through the bookmarks
		while(!cur.isAfterLast()){
			BookmarkData dat = new BookmarkData();
			Log.d("Debug", cur.toString());
			cur.moveToNext();
		}
		return bookmarks;
	}

	@Override
	protected void onPostExecute(ArrayList<BookmarkData> result) {
		super.onPostExecute(result);
		bookmarksDB.close();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
	}

}
