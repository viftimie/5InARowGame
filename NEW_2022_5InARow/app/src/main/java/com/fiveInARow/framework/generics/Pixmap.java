package com.fiveInARow.framework.generics;

import android.graphics.Bitmap;

import com.fiveInARow.framework.generics.Graphics.PixmapFormat;


public abstract class Pixmap {
	
	public abstract int getWidth();
	public abstract int getHeight();
	public abstract PixmapFormat getFormat();
	public abstract void dispose();
	public abstract Bitmap getBitmap();
	
}	
