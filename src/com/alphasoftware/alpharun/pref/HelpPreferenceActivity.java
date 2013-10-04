package com.alphasoftware.alpharun.pref;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.alphasoftware.alpharun.R;

public class HelpPreferenceActivity extends Activity {

	//private final static String url = "http://www.gardenutils.com/gardenweb/apphelp/";
	//private final static String url = "http://www.matthewduane.com/alpharun/";
	private final static String url = "http://alpharun.builtinaweekend.com/alpharun/apphelp/";
	private ProgressBar progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.helpview);



		Runnable r = new Runnable() {

			public void run() {
				WebView webview = (WebView) findViewById(R.id.webHelpView);

				webview.setWebViewClient(new WebViewClient(){

					@Override
					public void onPageFinished(WebView view, String url) {
						
						super.onPageFinished(view, url);
						progress = (ProgressBar) findViewById(R.id.progressBar1);
						progress.setVisibility(View.GONE);
						
					}

					@Override
					public boolean shouldOverrideUrlLoading(WebView view,
							String url) {

						view.loadUrl(url);
						return true;
					}

				});
				webview.loadUrl(url);

			}
		};
		
		new Thread(r).start();

	}

}
