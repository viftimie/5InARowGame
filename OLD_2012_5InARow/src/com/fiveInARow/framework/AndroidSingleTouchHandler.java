package com.fiveInARow.framework;

import java.util.ArrayList;
import java.util.List;

import android.view.MotionEvent;
import android.view.View;

import com.fiveInARow.framework.generics.ITouchHandler;
import com.fiveInARow.framework.generics.Input.TouchEvent;
import com.fiveInARow.utils.Pool;
import com.fiveInARow.utils.Pool.PoolObjectFactory;

public class AndroidSingleTouchHandler implements ITouchHandler {
	private boolean isTouched;
	private int touchX;
	private int touchY;

	private Pool<TouchEvent> touchEventPool;
	private List<TouchEvent> touchEvents = new ArrayList<TouchEvent>();
	private List<TouchEvent> touchEventsBuffer = new ArrayList<TouchEvent>();
	private float scaleX;
	private float scaleY;

	public AndroidSingleTouchHandler(View view, float scaleX, float scaleY) {
		PoolObjectFactory<TouchEvent> factory = new PoolObjectFactory<TouchEvent>() {
			public TouchEvent createObject() {
				return new TouchEvent();
			}
		};
		touchEventPool = new Pool<TouchEvent>(factory, 100);

		view.setOnTouchListener(this);
		this.scaleX = scaleX;
		this.scaleY = scaleY;
	}

	public boolean onTouch(View v, MotionEvent event) {
		synchronized (this) {
			TouchEvent touchEvent = touchEventPool.newObject();

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				touchEvent.type = TouchEvent.TOUCH_DOWN;
				isTouched = true;
				break;
			case MotionEvent.ACTION_MOVE:
				touchEvent.type = TouchEvent.TOUCH_DRAGGED;
				isTouched = true;
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				touchEvent.type = TouchEvent.TOUCH_UP;
				isTouched = false;
				break;
			}

			touchEvent.x = touchX = (int) (event.getX() * scaleX);
			touchEvent.y = touchY = (int) (event.getY() * scaleY);
			touchEventsBuffer.add(touchEvent);
			return true;
		}
	}

	public boolean isTouchDown(int pointer) {
		synchronized (this) {
			if (pointer == 0)
				return isTouched;
			else
				return false;
		}
	}

	public int getTouchX(int pointer) {
		synchronized (this) {
			return touchX;
		}
	}

	public int getTouchY(int pointer) {
		synchronized (this) {
			return touchY;
		}
	}

	public List<TouchEvent> getTouchEvents() {
		if (touchEventsBuffer.isEmpty())
			return null;
		
		synchronized (this) {
			touchEventPool.freeAll(touchEvents);
			touchEvents.clear();
			touchEvents.addAll(touchEventsBuffer);
			touchEventsBuffer.clear();
			return touchEvents;
		}
	}

}

