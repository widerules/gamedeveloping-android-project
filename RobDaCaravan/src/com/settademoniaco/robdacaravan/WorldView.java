package com.settademoniaco.robdacaravan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class WorldView extends SurfaceView {
	private Bitmap texture;
	private Bitmap bmp;
	private SurfaceHolder holder;
	private WorldSprite worldSprite;
	protected static WorldLoopThread worldLoopThread;

	public WorldView(Context context) {
		super(context);
		worldLoopThread = new WorldLoopThread(this);
		holder = getHolder();
		holder.addCallback(new Callback() {
			
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				boolean retry = true;
				worldLoopThread.setRunning(false);
				while(retry) {
					try {
						worldLoopThread.join();
						retry = false;
					}
					catch (InterruptedException e) {
					
					}
				}
			}
			
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				worldLoopThread.setRunning(true);
				worldLoopThread.start();
			}
			
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width,
					int height) {
				
			}
		});
		texture = BitmapFactory.decodeResource(getResources(), R.drawable.texture);
		bmp = BitmapFactory.decodeResource(getResources(), R.drawable.hero);
		worldSprite = new WorldSprite(this, bmp);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		canvas.drawBitmap(texture, 0, 0, null);
		worldSprite.onDraw(canvas);
	}
	

}
