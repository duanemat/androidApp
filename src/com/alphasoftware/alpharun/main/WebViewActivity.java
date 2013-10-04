package com.alphasoftware.alpharun.main;


import java.util.Iterator;
import java.util.List;

import org.apache.http.cookie.Cookie;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.CookieSyncManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alphasoftware.alpharun.R;
import com.alphasoftware.alpharun.pref.PreferenceStorageUnit;
import com.alphasoftware.alpharun.utils.BookmarkDatabase;
import com.alphasoftware.alpharun.utils.WebViewCanvas;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;

public class WebViewActivity extends Activity {

	
	private static final int LOADING_START = 1;
	private static final int LOADING_END = 2;	
	private static final long CONTROLLER_CLICKED_TIME = 200;

	private static BookmarkDatabase bkdb;	
	private static PreferenceStorageUnit psu;
	private static int pushed=0;
	private boolean wasMove = false; // Boolean used to distinguish between move and click.  If true, then don't handle on click event.  If false, then treat as click.
	private static int windowHeight, windowWidth;
	private static int controlBarHeight;
	private static float controlBtnOffset = 25.0f;
	private static boolean controlBarVisible = false;
	private float controlBtnPortraitLoc = 25.0f, controlBtnLandscapeLoc = 25.0f; // Default
	private float distanceMovedThreshold = 0.0f;
	
	private AsyncHttpClient myClient = new AsyncHttpClient(); // Used for cookies
	private PersistentCookieStore myCookieStore;
	
	private Long startControlTouch, endControlTouch; // Used for clicking vs. moving

	public static WebViewCanvas webview; 


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		bkdb = new BookmarkDatabase(this.getApplicationContext());

		// Create the PSU
		psu = new PreferenceStorageUnit(this.getApplicationContext());

		myCookieStore = new PersistentCookieStore(this);
		myClient.setCookieStore(myCookieStore);


		// Remove title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		boolean showStatusBar = (Boolean) psu.getPreference(PreferenceStorageUnit.STATUS_BAR);

		Float storedBtnPortraitOffset, storedBtnLandscapeOffset;
		storedBtnPortraitOffset = (Float)psu.getPreference(PreferenceStorageUnit.TOOLBAR_POSITION_PORTRAIT);
		storedBtnLandscapeOffset = (Float)psu.getPreference(PreferenceStorageUnit.TOOLBAR_POSITION_LANDSCAPE);

		if(storedBtnPortraitOffset == null)
			controlBtnPortraitLoc = 25.0f;
		else
			controlBtnPortraitLoc = storedBtnPortraitOffset;

		if(storedBtnLandscapeOffset == null)
			controlBtnLandscapeLoc = 25.0f;
		else
			controlBtnLandscapeLoc = storedBtnLandscapeOffset;

		// Set up the control button offset properly and recover the earlier version as well 
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
			controlBtnOffset = controlBtnLandscapeLoc;
		}
		else{
			controlBtnOffset = controlBtnPortraitLoc;
		}


		if(!showStatusBar){
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		setContentView(R.layout.webview);

		View ve = findViewById(R.id.mainWebview);
		webview = (com.alphasoftware.alpharun.utils.WebViewCanvas) findViewById(R.id.mainWebview);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setDomStorageEnabled(true);
		webview.getSettings().setDatabasePath("/data/data/" + webview.getContext().getPackageName() + "/databases/");

		CookieSyncManager.createInstance(webview.getContext());

		// Set the webview client watcher
		webview.setWebViewClient(new WebViewClient(){

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);

				Log.d("Error", "Error loading page");
				boolean logErrors = (Boolean) psu.getPreference(PreferenceStorageUnit.LOG_ERRORS);

				// If logging errors, then save
				if(logErrors)
					bkdb.insertLog("Error:\n" + failingUrl, description);
			}


			// Load URLs in the same webview
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				final String URL = url;
				myClient.get(url, new AsyncHttpResponseHandler(){
					
					@Override
					public void onSuccess(String response) {	
						List<Cookie> cookies = myCookieStore.getCookies();
						Iterator<Cookie> itr = cookies.iterator();
						while(itr.hasNext()){
							Cookie c = itr.next();
							Log.d("cookies", c.toString());
						}
						
					}
					
					@Override
					public void onFailure(Throwable arg0, String arg1) {
						super.onFailure(arg0, arg1);
						
						boolean logErrors = (Boolean) psu.getPreference(PreferenceStorageUnit.LOG_ERRORS);
						
						// If logging errors, then save
						if(logErrors)
							bkdb.insertLog("Error:\n" + URL, arg0.getMessage());
					}
				});			
				view.loadUrl(url);
				return true;
			}


			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				super.onPageStarted(view, url, favicon);

				// Update the buttons
				updateButtons(WebViewActivity.LOADING_START);
			}



			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				//getCookieUsingCookieHandler(url);
				

				// Update the buttons
				updateButtons(WebViewActivity.LOADING_END);
			}

		});

		// Set the Chrome client as well
		webview.setWebChromeClient(new WebChromeClient(){

			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					JsResult result) {

				String command = (String) psu.getPreference(PreferenceStorageUnit.JAVASCRIPT);
				bkdb.insertLog(command, message);
				return super.onJsAlert(view, url, message, result);

			}

			@Override
			public boolean onJsBeforeUnload(WebView view, String url,
					String message, JsResult result) {
				// TODO Auto-generated method stub
				String command = (String) psu.getPreference(PreferenceStorageUnit.JAVASCRIPT);
				bkdb.insertLog(command, message);
				return super.onJsBeforeUnload(view, url, message, result);
			}

			@Override
			public boolean onJsConfirm(WebView view, String url,
					String message, JsResult result) {
				// TODO Auto-generated method stub
				String command = (String) psu.getPreference(PreferenceStorageUnit.JAVASCRIPT);
				bkdb.insertLog(command, message);
				return super.onJsConfirm(view, url, message, result);
			}

			@Override
			public boolean onJsPrompt(WebView view, String url, String message,
					String defaultValue, JsPromptResult result) {
				// TODO Auto-generated method stub
				String command = (String) psu.getPreference(PreferenceStorageUnit.JAVASCRIPT);
				bkdb.insertLog(command, message);
				return super.onJsPrompt(view, url, message, defaultValue, result);
			}

			@Override
			public boolean onJsTimeout() {
				// TODO Auto-generated method stub
				String command = (String) psu.getPreference(PreferenceStorageUnit.JAVASCRIPT);
				bkdb.insertLog(command, "Javascript Timeout");
				return super.onJsTimeout();
			}
			

		});
		
		// Handles outputting from JS commands
		class MyJavaScriptInterface {
		    public void someCallback(String jsResult) {
		    	// Handle the results when you don't post an alert or something else.
		    	if(jsResult.compareTo("undefined") != 0){
		    		Log.d("Command", "Result = " + jsResult);
		    		String command = (String) psu.getPreference(PreferenceStorageUnit.JAVASCRIPT);
		    		bkdb.insertLog(command, jsResult);
		    		
		    	}
		    }
		}
		
		webview.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
		
		// Configure the starting position of the controller
		setNavControls();

		// Create moving for the controller
		setControlsListeners();

		// Set visiblity
		LinearLayout ll = (LinearLayout)findViewById(R.id.controlBand);	
		for(int i=0; i<ll.getChildCount(); i++){
			View child = ll.getChildAt(i);
			if (child.getId() != R.id.controlBtn){
				child.setVisibility(View.INVISIBLE);
			}else{
				child.setVisibility(View.VISIBLE);
			}
		}
		ll.setBackgroundColor(Color.TRANSPARENT);
		
		controlBarVisible = false;

		// Set the Control Bar Visiblity from Preference
		setControlBarVisiblity();

		// Get the intent with the URL
		Intent intent = getIntent();
		try{
			final String url = intent.getStringExtra(MainActivity.URL);
			//webview.inSpotMode = intent.getBooleanExtra(PreferenceStorageUnit.TOUCH_HIGHLIGHTING, false);
			webview.inSpotMode = (Boolean) psu.getPreference(PreferenceStorageUnit.TOUCH_HIGHLIGHTING);
			myClient.get(url, new AsyncHttpResponseHandler(){

				@Override
				public void onFailure(Throwable arg0, String arg1) {
					super.onFailure(arg0, arg1);
					
					boolean logErrors = (Boolean) psu.getPreference(PreferenceStorageUnit.LOG_ERRORS);
					
					// If logging errors, then save
					if(logErrors)
						bkdb.insertLog("Error:\n" + url, arg0.getMessage());
				}

				@Override
				public void onSuccess(String response) {					
					webview.loadUrl(url);
					List<Cookie> cookies = myCookieStore.getCookies();
					Iterator<Cookie> itr = cookies.iterator();
					while(itr.hasNext()){
						Cookie c = itr.next();
						Log.d("cookies", c.toString());
					}
					
				}
			});			

		}catch (Exception e){
			Toast.makeText(this, "Could not open URL", Toast.LENGTH_SHORT).show();
			Log.e("WebviewError", e.getMessage());
		}

	}

	// Updates the display buttons based on the functionality available for the circumstance
	// Not very efficient, but also probably not a major resource loss
	protected void updateButtons(int state) {

		// Get the buttons
		ImageButton backBtn = (ImageButton)findViewById(R.id.controlBackBtn);
		ImageButton fwdBtn = (ImageButton)findViewById(R.id.controlFwdBtn);
		ImageButton refreshBtn = (ImageButton)findViewById(R.id.controlRefreshBtn);
		ImageButton stopBtn = (ImageButton)findViewById(R.id.controlStopBtn);

		if(state == WebViewActivity.LOADING_START){
			Log.d("Buttons", "Starting");
			refreshBtn.setEnabled(false);
			stopBtn.setEnabled(true);

			Drawable originalIcon = this.getApplicationContext().getResources().getDrawable(R.drawable.ic_menu_refresh);		    
			Drawable icon = convertDrawableToGrayScale(originalIcon, true);
			refreshBtn.setImageDrawable(originalIcon);

			originalIcon = this.getApplicationContext().getResources().getDrawable(R.drawable.ic_menu_close_clear_cancel);		    
			icon = convertDrawableToGrayScale(originalIcon, false);
			stopBtn.setImageDrawable(originalIcon);

		}else if (state == WebViewActivity.LOADING_END){			
			refreshBtn.setEnabled(true);
			stopBtn.setEnabled(false);

			Drawable originalIcon = this.getApplicationContext().getResources().getDrawable(R.drawable.ic_menu_refresh);		    
			Drawable icon = convertDrawableToGrayScale(originalIcon, false);
			refreshBtn.setImageDrawable(originalIcon);

			originalIcon = this.getApplicationContext().getResources().getDrawable(R.drawable.ic_menu_close_clear_cancel);		    
			icon = convertDrawableToGrayScale(originalIcon, true);
			stopBtn.setImageDrawable(originalIcon);

		}

		if(webview.canGoBack()){
			Drawable originalIcon = this.getApplicationContext().getResources().getDrawable(R.drawable.ic_menu_back);		    
			Drawable icon = convertDrawableToGrayScale(originalIcon, false);
			backBtn.setImageDrawable(originalIcon);

			backBtn.setEnabled(true);
		}
		else{
			Drawable originalIcon = this.getApplicationContext().getResources().getDrawable(R.drawable.ic_menu_back);		    
			Drawable icon = convertDrawableToGrayScale(originalIcon, true);
			backBtn.setImageDrawable(originalIcon);

			backBtn.setEnabled(false);
		}

		if(webview.canGoForward()){
			Drawable originalIcon = this.getApplicationContext().getResources().getDrawable(R.drawable.ic_menu_forward);		    
			Drawable icon = convertDrawableToGrayScale(originalIcon, false);
			fwdBtn.setImageDrawable(originalIcon);

			fwdBtn.setEnabled(true);
		}
		else{
			Drawable originalIcon = this.getApplicationContext().getResources().getDrawable(R.drawable.ic_menu_forward);		    
			Drawable icon = convertDrawableToGrayScale(originalIcon, true);
			fwdBtn.setImageDrawable(originalIcon);

			fwdBtn.setEnabled(false);
		}		
	}

	private static Drawable convertDrawableToGrayScale(Drawable drawable, boolean makeGray) {
		if (drawable == null) {
			return null;
		}
		Drawable res = drawable.mutate();
		if(makeGray)
			res.setColorFilter(Color.GRAY, Mode.SRC_IN);
		else
			res.clearColorFilter();

		return res;
	}

	// Listener setup for the controller
	private void setControlsListeners() {
		ImageButton controlBtn = (ImageButton) WebViewActivity.this.findViewById(R.id.controlBtn);

		controlBtn.setOnTouchListener(new OnTouchListener() {

			private boolean isTouched = false;
			private float distanceMoved = 0.0f;
			public boolean onTouch(View v, MotionEvent event) {

				// Handle the touch events
				if(event.getAction() == MotionEvent.ACTION_DOWN){
					Log.d("Compass", "Down");
					distanceMoved = 0.0f;
					isTouched = true; // Down, start listening
					startControlTouch = System.currentTimeMillis();
				}
				else if (event.getAction() == MotionEvent.ACTION_UP){
					isTouched = false; // Up, stop listening					
					endControlTouch = System.currentTimeMillis();
				}
				else if (event.getAction() == MotionEvent.ACTION_MOVE){ // If we are allowing movement, track the y
					
					Long currentTime = System.currentTimeMillis();
					if(currentTime - startControlTouch < WebViewActivity.CONTROLLER_CLICKED_TIME){
						wasMove = false;
					}
					else{
						if (isTouched){				
							//Log.d("Offset", controlBtnOffset + " " + event.getRawY() + " - " + v.getHeight());
							Log.d("clicking", "moving");
							controlBtnOffset = event.getRawY() - v.getHeight();
							// Get the linear layout location as well

							ImageButton ib = (ImageButton) WebViewActivity.this.findViewById(R.id.controlBtn);
							//controlBarHeight = ib.getLayoutParams().height;
							controlBarHeight = ib.getMeasuredHeight(); // Get the measured height

							// Determine if the offset makes sense to move
							// Gives a bit of clearance at the top and at the bottom
							if(controlBtnOffset < 2){
								controlBtnOffset = 2;
							}
							else if ( controlBtnOffset > (windowHeight - (2*controlBarHeight))){
								controlBtnOffset = windowHeight - (2*controlBarHeight) - 1;
							}
							else{
								LinearLayout ll = (LinearLayout) findViewById(R.id.controlBand);

								Log.d("Moving", "Offset = " + controlBtnOffset);
								ll.setY(controlBtnOffset); // This moves the whole thing
							}
							wasMove = true; // This is a move, so ignore the click event that follows						
						}
					}
				}
				return false;
			}
		});		

	}

	@Override
	protected void onResume() {
		super.onResume();

		// Check JS visiblity
		//checkJSButtonVisiblity();
		ImageButton prefBtn = (ImageButton)findViewById(R.id.controlPrefBtn);
		if(prefBtn.getVisibility() == View.VISIBLE)
			setControlVisiblity(true);
		

		// Set the status bar control
		boolean showStatusBar = (Boolean) psu.getPreference(PreferenceStorageUnit.STATUS_BAR);
		if(!showStatusBar){
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}else{
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Save the appropriate values to the preferences
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
			psu.setPreference(PreferenceStorageUnit.TOOLBAR_POSITION_LANDSCAPE, controlBtnOffset);
			psu.setPreference(PreferenceStorageUnit.TOOLBAR_POSITION_PORTRAIT, controlBtnPortraitLoc);
		}
		else{
			psu.setPreference(PreferenceStorageUnit.TOOLBAR_POSITION_LANDSCAPE, controlBtnLandscapeLoc);
			psu.setPreference(PreferenceStorageUnit.TOOLBAR_POSITION_PORTRAIT, controlBtnOffset);
		}
		
		psu.setLocations();
	}

	// Sets the Nav bar location and button
	private void setNavControls(){

		LinearLayout ll = (LinearLayout) WebViewActivity.this.findViewById(R.id.controlBand);

		// Get the window height and width
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);		

		windowWidth = size.x;
		windowHeight = size.y;

		distanceMovedThreshold = 0.03f * windowHeight;

		Log.d("positionX", ""+ll.getX());
		ll.setTranslationY(controlBtnOffset);	

	}

	private void setControlBarVisiblity(){
		boolean visibleControlBand = (Boolean) psu.getPreference(PreferenceStorageUnit.TOOLBAR);


		LinearLayout ll = (LinearLayout)findViewById(R.id.controlBand);

		if(!visibleControlBand){
			ll.setVisibility(View.GONE);
		}else{
			ll.setVisibility(View.VISIBLE);
		}


	}

	private boolean checkJSButtonVisiblity(){

		// If nothing is visible, who cares?
		if(controlBarVisible == false)
			return false;

		String visibleJSButton = (String) psu.getPreference(PreferenceStorageUnit.JAVASCRIPT);
		// Handle JS button visibility		
		if(visibleJSButton.trim().length() == 0){
			return false;
		}
		else{
			return true;
		}	
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if(event.getAction() == KeyEvent.ACTION_DOWN){
			switch (keyCode){

			case KeyEvent.KEYCODE_BACK:

				if (webview.canGoBack() == true){
					webview.goBack();
				}
				else{
					finish();
				}

				return true;	

			default:
				super.onKeyDown(keyCode, event);
				break;

			}
		}

		return super.onKeyDown(keyCode, event);
	}



	// Click function for when the little controller is clicked
	public void controllerClicked(View v){

		Log.d("Compass", "ControllerClicked");

		switch (v.getId()){

		case R.id.controlBackBtn:
			if(webview.canGoBack())
				webview.goBack();
			break;

		case R.id.controlFwdBtn:
			if (webview.canGoForward())
				webview.goForward();
			break;

		case R.id.controlRefreshBtn:
			webview.reload();
			break;

		case R.id.controlStopBtn:
			webview.stopLoading();
			break;

			// Press the Javascript button
		case R.id.controlJSBtn:
			runJSOnWebview();
			break;

		case R.id.controlPrefBtn:			
			Intent intent = new Intent(WebViewActivity.this, PreferencesActivity.class);
			intent.putExtra(PreferencesActivity.PREFERENCE_CALLER, "WebViewActivity");
			startActivityForResult(intent, PreferencesActivity.PREFERENCE_REQUEST);

			break;

			// Reload the base activity within this chain
		case R.id.controlBookmarkBtn:
			Intent bookmarkIntent = new Intent(WebViewActivity.this, MainActivity.class);
			bookmarkIntent.putExtra("CALLER", MainActivity.CALLER_WEBVIEW);
			bookmarkIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(bookmarkIntent);			
			break;

			// When someone clicks the control button
		case R.id.controlBtn:

			Log.d("clicking", "controlBtn clicked");
			// If this was a move, then ignore.
			if (wasMove){
				wasMove = false;	
				Log.d("controller", "moving");
			}
			else{
				Log.d("controller", "clicked");
				ImageButton backBtn = (ImageButton)findViewById(R.id.controlBackBtn);
				if(backBtn.getVisibility() == View.INVISIBLE){
					controlBarVisible = true;
					setControlVisiblity(true);
				}else{
					controlBarVisible = false;
					setControlVisiblity(false);
				}
			}
			break;

		default:
			break;
		}

	}

	// Run the stored javascript command on the current webview
	private void runJSOnWebview(){

		String tempCommand = (String) psu.getPreference(PreferenceStorageUnit.JAVASCRIPT);
		if(!tempCommand.endsWith(";"))
			tempCommand += ";";
		final String jsCommand = tempCommand;

		
		class jsRunnable implements Runnable{

			String jscommand;
			Context c;
			
			jsRunnable(String str, Context c){
				jscommand = str;
				this.c = c;
			}
			public void run() {				
				//webview.loadUrl("javascript:" + jscommand);
				webview.loadUrl("javascript:( function () { var resultSrc = " + jscommand +" window.HTMLOUT.someCallback(resultSrc); } ) ()");
				Toast.makeText(c, "Javascript completed.", Toast.LENGTH_SHORT).show();				
			}
			
		}
		
		Thread t = new Thread(new jsRunnable(jsCommand, this));
		// Run in the background so as to not slow down current view
		t.run();
		

	}

	// Helper function for setting visiblity of the control bar
	private void setControlVisiblity(boolean isVisible){

		LinearLayout ll = (LinearLayout)findViewById(R.id.controlBand);			


		if(isVisible){

			Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
			// Cycle through all children of ll and make them visible except control button
			for(int i=0; i<ll.getChildCount(); i++){
				View child = ll.getChildAt(i);

				if (child.getId() != R.id.controlBtn){
					
					if (child.getId() == R.id.controlJSBtn){
						boolean showJSButton = checkJSButtonVisiblity();
						if(!showJSButton){							
							child.setVisibility(View.GONE);
							continue;
						}
					}
					child.startAnimation(fadeIn);					
					child.setVisibility(View.VISIBLE);
				}
				

			}

			ll.setBackgroundColor(Color.rgb(0x1e, 0x90, 0xff));
			ll.setAlpha(0.85f);
			

		}else{

			Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
			// Cycle through all children of ll and make them invisible except control button
			for(int i=0; i<ll.getChildCount(); i++){
				View child = ll.getChildAt(i);
				if (child.getId() != R.id.controlBtn){
					
					// If not there, no need to fade out.
					if(child.getVisibility() == View.GONE)
						continue;
					
					child.startAnimation(fadeOut);
					child.setVisibility(View.INVISIBLE);
				}
			}
			ll.setBackgroundColor(Color.TRANSPARENT);
		}

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		// Convert 
		if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
			controlBtnPortraitLoc = controlBtnOffset;
			controlBtnOffset = controlBtnLandscapeLoc;
			Log.i("CRV", "Landscape");			
			setNavControls();
		}
		else{
			controlBtnLandscapeLoc = controlBtnOffset;
			controlBtnOffset = controlBtnPortraitLoc;
			Log.i("CRV", "Portrait");
			setNavControls();
		}
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		// If nothing returned, just exit
		if(data == null)
			return;

		if(resultCode == PreferencesActivity.PREFERENCE_REQUEST){

			psu.updateAllPreferences();

			// Update the view based on the preferences
			webview.inSpotMode = (Boolean) psu.getPreference(PreferenceStorageUnit.TOUCH_HIGHLIGHTING);
			setControlBarVisiblity();
		}
	}




}
