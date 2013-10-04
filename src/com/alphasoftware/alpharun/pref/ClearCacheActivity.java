package com.alphasoftware.alpharun.pref;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.alphasoftware.alpharun.R;

public class ClearCacheActivity extends Activity{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
		WebView webview = (WebView)findViewById(R.id.mainWebview);
		
	    //Log.d("Size", size + "");
		webview.clearCache(true);
	    

		finish();
	}

}
