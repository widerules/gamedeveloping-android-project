package com.settademoniaco.robdacaravan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class FieldView extends SurfaceView {
	private Bitmap bmp;
	private SurfaceHolder holder;
	private FieldLoopThread gameLoopThread;
	private int x=0, y=0;
	private int xSpeed=3, ySpeed=5;
	
	public FieldView(Context context) {
		super(context);
		gameLoopThread = new FieldLoopThread(this);
		holder = getHolder();
		holder.addCallback(new Callback() {
			
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				
			}
			
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				gameLoopThread.setRunning(true);
				gameLoopThread.start();
			}
			
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width,
					int height) {
				
			}
		});
		
		bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(Color.WHITE);
		if (x >= getWidth() - bmp.getWidth()) {xSpeed =-3; ySpeed = 0;}
		if (y >= getHeight() - bmp.getHeight()) {xSpeed = 0; ySpeed = -5;}
		if (x <= 0) {xSpeed = 3; ySpeed = 0;}
		if (y <= 0) {xSpeed = 0; ySpeed = 5;}
		
		x+=xSpeed;
		y+=ySpeed;
		
		canvas.drawBitmap(bmp, x, y, null);
	}

}
