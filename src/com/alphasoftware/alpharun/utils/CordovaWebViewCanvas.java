package com.alphasoftware.alpharun.utils;

import org.apache.cordova.CordovaWebView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class CordovaWebViewCanvas extends CordovaWebView {

	private Float spotX, spotY;
	public boolean inSpotMode;

	public CordovaWebViewCanvas(Context context, AttributeSet attrs) {
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

		//canvas.drawLine (20, 10, 300, 400, p);
		if(inSpotMode){
			try{
				Paint p = new Paint();
				p.setColor (Color.RED);
				p.setAlpha(50);
				if(spotX != null)
					canvas.drawCircle(spotX, spotY, 10, p);
			}catch (Exception e){
				Log.e("Error", e.getMessage());
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

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

		return super.onTouchEvent(event);

	}

}
