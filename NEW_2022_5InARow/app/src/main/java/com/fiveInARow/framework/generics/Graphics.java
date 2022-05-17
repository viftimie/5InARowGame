package com.fiveInARow.framework.generics;

import android.graphics.Paint;

public abstract class Graphics {

	public static enum PixmapFormat {
		ARGB8888, ARGB4444, RGB565
	}
	
	public abstract int getWidth();

	public abstract int getHeight();

	public abstract Pixmap newPixmap(String fileName, PixmapFormat format);

	public abstract void drawPixel(int x, int y, int color);

	public abstract void drawLine(int x, int y, int x2, int y2, int color);

	public abstract void drawRect(int x, int y, int width, int height, int color);

	public abstract void drawPixmap(Pixmap pixmap, int x, int y, int srcX, int srcY, int srcWidth, int srcHeight);

	public abstract void drawPixmap(Pixmap pixmap, int x, int y);

	public abstract void drawText(String text, int x, int y, Paint paint);
	
	public abstract void clear(int color);

}
