package com.settademoniaco.robdacaravan;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import java.util.Random;

public class WorldSprite {
	private static final int BMP_COLUMNS = 11;
	private static final int BMP_ROWS = 8;
	int[] DIRECTION_TO_ANIMATION_MAP = {5, 6, 7, 4, 3, 2, 1, 0};
	private int x = 0;
	private int y = 0;
	private int currentFrame = 0;
	private int xSpeed = 5;
	private int ySpeed = 5;
	private WorldView worldView;
	private Bitmap bmp;
	private int width = 0;
	private int height = 0;

	public WorldSprite(WorldView worldView, Bitmap bmp) {
		this.worldView=worldView;
		this.bmp=bmp;
		this.width = bmp.getWidth() / BMP_COLUMNS;
		this.height = bmp.getHeight() / BMP_ROWS;
	}
	
	private void update () {
		if (x > worldView.getWidth() - width - xSpeed || x + xSpeed < 0) {
			xSpeed = -xSpeed;
			if (ySpeed != 0) ySpeed = 0;
			else ySpeed = -5;
		}
		if (y > worldView.getHeight() - height - ySpeed || y+ ySpeed < 0) {
			ySpeed = -ySpeed;
			if(xSpeed != 0) xSpeed = 0;
			else xSpeed = -5;
		}
		x+=xSpeed;
		y+=ySpeed;
		currentFrame = ++currentFrame % BMP_COLUMNS;
	}
	
	public void onDraw(Canvas canvas) {
		update();
		int srcX = currentFrame * width;
		int srcY = getAnimationRow() * height;
		Rect src = new Rect(srcX, srcY, srcX + width, srcY + height);
		Rect dst = new Rect(x, y, x + width, y + height);
		canvas.drawBitmap(bmp, src, dst, null);
	}
	
	private int getAnimationRow() {
        double dirDouble = (Math.atan2(xSpeed, ySpeed) / (Math.PI / 4) + 3);
        int direction = (int) Math.round(dirDouble);
        return DIRECTION_TO_ANIMATION_MAP[direction];
  }
	
}
