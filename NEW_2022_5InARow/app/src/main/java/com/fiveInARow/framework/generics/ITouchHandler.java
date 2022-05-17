package com.fiveInARow.framework.generics;

import java.util.List;

import android.view.View.OnTouchListener;

import com.fiveInARow.framework.generics.Input.TouchEvent;

public interface ITouchHandler extends OnTouchListener {
	
	public boolean isTouchDown(int pointer);
	public int getTouchX(int pointer);
	public int getTouchY(int pointer);
	public List<TouchEvent> getTouchEvents();
	
}