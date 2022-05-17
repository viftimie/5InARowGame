package com.fiveInARow.framework;

import android.graphics.Bitmap;

import com.fiveInARow.framework.generics.Graphics.PixmapFormat;
import com.fiveInARow.framework.generics.Pixmap;

public class AndroidPixmap extends Pixmap {
	private Bitmap bitmap;
	private PixmapFormat format;

	public AndroidPixmap(Bitmap bitmap, PixmapFormat format) {
		this.bitmap = bitmap;
		this.format = format;
	}

	public int getWidth() {
		return bitmap.getWidth();
	}

	public int getHeight() {
		return bitmap.getHeight();
	}

	public PixmapFormat getFormat() {
		return format;
	}

	public void dispose() {
		bitmap.recycle();
	}

	@Override
	public Bitmap getBitmap() {
		return bitmap;
	}
	
}
