package com.alphasoftware.alpharun.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alphasoftware.alpharun.R;
import com.alphasoftware.alpharun.R.layout;
import com.alphasoftware.alpharun.pref.PreferenceStorageUnit;

public class BookmarkAdapter extends ArrayAdapter<BookmarkData> {

	private final Context c;
	private ArrayList<BookmarkData> bookmarks;
	private static BookmarkDatabase bookmarkDB;
	private static PreferenceStorageUnit psu;

	private static final int BGCOLOR_EVEN = 0xffdddddd;
	private static final int BGCOLOR_ODD = 0xffc6c6c6;

	// Optimized class found at http://www.vogella.com/articles/AndroidListView/article.html
	static class ViewHolder {
		public TextView title;
		public TextView url;
	}

	public BookmarkAdapter(Context c, List<BookmarkData> b){
		super(c, layout.rowlayout, b);

		this.c = c;
		this.bookmarks = new ArrayList<BookmarkData>();		

		// Add in all of the old bookmarks
		this.bookmarks.addAll(b);

		psu = new PreferenceStorageUnit(c);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View rowView = convertView;

		// Get the updated preferences
		//psu.updateAllPreferences();

		if(rowView == null){
			LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			rowView = inflater.inflate(R.layout.rowlayout, parent, false);

			/*if(position % 2 == 0)
				rowView.setBackgroundColor(BookmarkAdapter.BGCOLOR_EVEN);
			else
				rowView.setBackgroundColor(BookmarkAdapter.BGCOLOR_ODD);*/			

			ViewHolder holder = new ViewHolder();
			holder.title = (TextView) rowView.findViewById(R.id.title);
			holder.url = (TextView) rowView.findViewById(R.id.url);

			rowView.setTag(holder);
		}

		// Set the visiblity of the scanning icon
		boolean visibleScan = bookmarks.get(position).bScan;

		if (!visibleScan){
			ImageView imgBtn = (ImageView) rowView.findViewById(R.id.imgBtnScanner);
			imgBtn.setTag(position);
			imgBtn.setVisibility(View.GONE);
		}
		else{
			ImageView imgBtn = (ImageView) rowView.findViewById(R.id.imgBtnScanner);
			imgBtn.setTag(position);
			imgBtn.setVisibility(View.VISIBLE);
		}

		// Load the current data to the object
		ViewHolder holder = (ViewHolder)rowView.getTag();

		String url = bookmarks.get(position).sUrl;
		String varReplace = (String) psu.getPreference(PreferenceStorageUnit.URL_VARIABLE);

		url = url.replaceAll("\\[\\[v1\\]\\]", varReplace);

		holder.title.setText(bookmarks.get(position).sTitle);
		holder.url.setText(url);

		return rowView;
	}

	// Get the number of bookmarks currently stored
	public int count(){
		return bookmarks.size();
	}

	// Returns a reference to the requested bookmark
	public BookmarkData getBookmark(int position){
		return bookmarks.get(position);
	}

	// Adds the bookmark to the bottom of the list
	public void addBookmark(BookmarkData bookmark){
		bookmarks.add(count(), bookmark);
	}

	public void removeBookmark(BookmarkData bookmark){
		bookmarks.remove(bookmark);
		// Update each later bookmarks with correct indexes
		for(BookmarkData dat : bookmarks){
			if(dat.iOrder > bookmark.iOrder){				
				dat.iOrder -= 1; // Minus 1

				if(dat.iOrder < 0){
					Log.e("Error", "Ordering error with bookmark list - order less than 0");
					dat.iOrder = count(); // Put at the end
				}
			}
		}
	}

	// Add a bookmark to the view
	@Override
	public void add(BookmarkData object) {
		bookmarks.add(object);
		super.add(object);
	}

	@Override
	public void remove(BookmarkData object) {
		bookmarks.remove(object);
		super.remove(object);
	}

	// Clears the list but doesn't remove entries
	@Override
	public void clear(){
		super.clear();
		bookmarks.clear();
	}

	public void empty(){
		clear();
		bookmarks.clear();
		notifyDataSetChanged();
		notifyDataSetInvalidated();
	}

	public void refresh(){
		ArrayList<BookmarkData> tempList = new ArrayList<BookmarkData>();
		for(BookmarkData dat : bookmarks){
			tempList.add(dat);
		}

		clear();
		notifyDataSetChanged();

		Collections.sort(tempList, new Comparator<BookmarkData>() {

			public int compare(BookmarkData lhs, BookmarkData rhs) {

				if(lhs.iOrder < rhs.iOrder)
					return -1;
				else if (lhs.iOrder > rhs.iOrder)
					return 1;
				else
					return 0;
			}

		});

		for(int i=0; i<tempList.size(); i++){
			add(tempList.get(i));
		}


		//notifyDataSetInvalidated();
	}

	public void updateBookmark(BookmarkData bookmark) {

		Iterator<BookmarkData> itr = bookmarks.iterator();
		boolean found = false;

		while(itr.hasNext()){
			BookmarkData dat = itr.next();
			if(dat.index == bookmark.index){
				dat.copyFrom(bookmark);
				found = true;
				break;
			}
		}

		if(found)
			Log.d("Debug", "Found old");
		else
			Log.d("Debug", "Not found old.");
	}

}
