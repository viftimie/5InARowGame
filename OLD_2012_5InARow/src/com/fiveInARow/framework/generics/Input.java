package com.fiveInARow.framework.generics;

import java.util.List;

public abstract class Input {
	
	//KEYBOARD-----------------------------------------------
	public static class KeyEvent {
		public static final int KEY_DOWN = 0;
		public static final int KEY_UP = 1;
		public int type;
		public int keyCode;
		public char keyChar;
	}
	
	public abstract List<KeyEvent> getKeyEvents();
	public abstract boolean isKeyPressed(int keyCode);
	
	//TOUCHSCREEN--------------------------------------------
	public static class TouchEvent {
		public static final int TOUCH_DOWN = 0;
		public static final int TOUCH_UP = 1;
		public static final int TOUCH_DRAGGED = 2;
		public int type;
		public int x, y;
		public int pointer;
	}
	
	public abstract List<TouchEvent> getTouchEvents();
	
	public abstract boolean isTouchDown(int pointer);
	public abstract int getTouchX(int pointer);
	public abstract int getTouchY(int pointer);

}
