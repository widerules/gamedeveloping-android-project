package com.settademoniaco.robdacaravan;

import android.graphics.Canvas;

public class GameLoopThread extends Thread {
	static final long FPS = 100;
	private GameView view;
	private boolean running = false;
	
	public GameLoopThread(GameView view) {
		this.view = view;
	}
	
	public void setRunning(boolean run) {
		running = run;
	}
	
	@Override
	public void run() {
		long ticksPS = 1000 / FPS;
		long startTime;
		long sleepTime;
		while (running) {
			Canvas canvas=null;
			startTime = System.currentTimeMillis();
			try {
				canvas = view.getHolder().lockCanvas();
				synchronized (view.getHolder()) {
					view.onDraw(canvas);
				}
			}
			finally {
				if (canvas != null) {
					view.getHolder().unlockCanvasAndPost(canvas);
				}
				
			}
			sleepTime = ticksPS - (System.currentTimeMillis() - startTime);
			try {
				if (sleepTime > 0) sleep(sleepTime);
				else sleep(10);
			}
			catch (Exception e) {}

		}
			
		
	}

}
