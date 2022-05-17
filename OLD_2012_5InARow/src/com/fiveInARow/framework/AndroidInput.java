package com.fiveInARow.framework;

import java.util.List;

import android.content.Context;
import android.view.View;

import com.fiveInARow.framework.generics.ITouchHandler;
import com.fiveInARow.framework.generics.Input;

public class AndroidInput extends Input {
	private AndroidKeyboardHandler keyHandler;
	private ITouchHandler touchHandler;
	
	public AndroidInput(Context context, View view, float scaleX, float scaleY) {
		keyHandler = new AndroidKeyboardHandler(view);
		touchHandler = new AndroidSingleTouchHandler(view, scaleX, scaleY);
	}
	
	public boolean isKeyPressed(int keyCode) {
		return keyHandler.isKeyPressed(keyCode);
	}
	
	public boolean isTouchDown(int pointer) {
		return touchHandler.isTouchDown(pointer);
	}
	
	public int getTouchX(int pointer) {
		return touchHandler.getTouchX(pointer);
	}

	public int getTouchY(int pointer) {
		return touchHandler.getTouchY(pointer);
	}

	public List<TouchEvent> getTouchEvents() {
		return touchHandler.getTouchEvents();
	}

	public List<KeyEvent> getKeyEvents() {
		return keyHandler.getKeyEvents();
	}
}
