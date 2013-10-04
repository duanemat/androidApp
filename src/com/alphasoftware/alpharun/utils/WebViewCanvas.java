package com.alphasoftware.alpharun.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;

public class WebViewCanvas extends WebView {

	private Float spotX, spotY;
	public boolean inSpotMode;

	Runnable run = new Runnable() {
		public void run() {
			// do something than send an integer - x in our case
			int x = 0;         
			Log.d("dots", "Thread");
			final Message msg = Message.obtain(handler, x, null);
			//handler.postDelayed(this, 100);
			handler.dispatchMessage(msg);
		}
	};
	
	final Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			Log.d("dots", "Handler");
			super.handleMessage(msg);
		}

	};

	public WebViewCanvas(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		spotX = null;
		spotY = null;
		inSpotMode = false;	
	}

	public void setSpot(Float x, Float y){
		spotX = x;
		spotY = y;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);

		if(inSpotMode){
			try{
				Paint p = new Paint();
				p.setColor (Color.RED);
				p.setAlpha(50);
				if(spotX != null){
					canvas.drawCircle(spotX, spotY, 10, p);
					//new Thread(run).start();
					//handler.postDelayed(run, 1000);
				}


			}catch (Exception e){
				Log.e("Error", e.getMessage());
			}
		}
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if(inSpotMode){
			Log.d("Touch", event.getX() + ", " + event.getY());
			if((event.getAction() == MotionEvent.ACTION_DOWN) || (event.getAction() == MotionEvent.ACTION_MOVE)){
				spotX = event.getX() + this.computeHorizontalScrollOffset();
				spotY = event.getY() + this.computeVerticalScrollOffset();
				//Log.d("Points", spotX + ", " + spotY + " = " + offsetY);
				this.invalidate();
			}
			else if (event.getAction() == MotionEvent.ACTION_UP){
				spotX = null;
				spotY = null;
			}

		}



		return super.onTouchEvent(event);	

	}

}
