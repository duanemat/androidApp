package com.alphasoftware.alpharun.pref;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alphasoftware.alpharun.R;

public class AboutPreferenceActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.aboutview);
		
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.setMargins(0, 10, 0, 20); // llp.setMargins(left, top, right, bottom);
		
		LinearLayout ll = (LinearLayout) findViewById(R.id.aboutLayout);
		
		// Cordova version
		TextView tv = new TextView(this);
		tv.setTextIsSelectable(true);
		tv.setLayoutParams(llp);
		tv.setText("CORDOVA VERSION:\n\t2.9.0");
		
		ll.addView(tv);
		
		tv = new TextView(this);
		tv.setLayoutParams(llp);
		StringBuilder str = new StringBuilder();
		str.append("\tApp\n");
		str.append("\tGeolocation\n");
		str.append("\tDevice\n");
		str.append("\tAccelerometer\n");
		str.append("\tCompass\n");
		str.append("\tMedia\n");
		str.append("\tCamera\n");
		str.append("\tContacts\n");
		str.append("\tFile\n");
		str.append("\tNetworkStatus\n");
		str.append("\tNotification\n");
		str.append("\tStorage\n");
		str.append("\tFileTransfer\n");
		str.append("\tCapture\n");
		str.append("\tBattery\n");
		str.append("\tSplashScreen\n");
		str.append("\tEcho\n");
		str.append("\tGlobalization\n");
		str.append("\tInAppBrowser\n");
		tv.setText("CORDOVA PLUGINS:\n" + str);
		
		ll.addView(tv);
		
		tv = new TextView(this);
		tv.setLayoutParams(llp);
		tv.setText("Barcode scanning uses the ZXing Open Source library.  That library is licensed under the Apache License 2.0 (http://www.apache.org/licenses/LICENSE-2.0).  It is Copyright 2008-2013 ZXing authors.  All rights reserved.");
		
		ll.addView(tv);
		
		tv = new TextView(this);
		tv.setLayoutParams(llp);
		tv.setText("This app uses the Android Asynchronous Http Client library (http://loopj.com/android-async-http/) developed by James Smith and is licensed under the Apache License 2.0 (http://www.apache.org/licenses/LICENSE-2.0).");
		
		ll.addView(tv);
		
		
		tv = new TextView(this);
		tv.setLayoutParams(llp);
		tv.setText("This app includes Apache Cordova. Apache Cordova:\nCopyright 2012 The Apache Software Foundation.");
		
		ll.addView(tv);
		
		
		tv = new TextView(this);
		tv.setLayoutParams(llp);
		tv.setText("Apache Cordova is licensed under the Apache License 2.0 (http://www.apache.org/licenses/LICENSE-2.0.html).  Some of the Cordova plugins are licensed under the MIT license.\n\nThe Apache Software Foundation: http://www.apache.org");
		
		ll.addView(tv);
	}

}
