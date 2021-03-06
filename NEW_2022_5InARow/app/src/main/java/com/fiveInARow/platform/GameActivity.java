package com.fiveInARow.platform;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.fiveInARow.framework.AndroidAudio;
import com.fiveInARow.framework.AndroidFileSystemUtils;
import com.fiveInARow.framework.AndroidGraphics;
import com.fiveInARow.framework.AndroidInput;
import com.fiveInARow.framework.generics.Audio;
import com.fiveInARow.framework.generics.Graphics;
import com.fiveInARow.framework.generics.IGame;
import com.fiveInARow.framework.generics.Input;
import com.fiveInARow.framework.generics.Screen;
import com.fiveInARow.game.screens.Screen_Loading;

public class GameActivity extends Activity implements IGame {
	private GameFastRenderView renderView;
	private Graphics graphics;
	private Audio audio;
	private Input input;
	private Screen screen;
	private WakeLock wakeLock;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		boolean isLandscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
		
		int frameBufferWidth = isLandscape ? 480 : 320;
		int frameBufferHeight = isLandscape ? 320 : 480;
		
		Bitmap frameBuffer = Bitmap.createBitmap(frameBufferWidth,frameBufferHeight, Config.RGB_565);
		
		float scaleX = (float) frameBufferWidth/ getWindowManager().getDefaultDisplay().getWidth();
		float scaleY = (float) frameBufferHeight/ getWindowManager().getDefaultDisplay().getHeight();
		
		renderView = new GameFastRenderView(this, frameBuffer);
		graphics = new AndroidGraphics(getAssets(), frameBuffer);
		audio = new AndroidAudio(this);
		input = new AndroidInput(this, renderView, scaleX, scaleY);
		screen = getStartScreen();
		
		setContentView(renderView);
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,"GLGame");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		wakeLock.acquire();
		screen.resume();
		renderView.resume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		wakeLock.release();
		renderView.pause();
		screen.pause();
		if (isFinishing())
			screen.dispose();
	}

	public Input getInput() {
		return input;
	}
	
	public AndroidFileSystemUtils getFileIO() {
		return AndroidFileSystemUtils.getINSTANCE();
	}
	
	public Graphics getGraphics() {
		return graphics;
	}

	public Audio getAudio() {
		return audio;
	}

	public void setScreen(Screen screen) {
		if (screen == null)
			throw new IllegalArgumentException("Screen must not be null");
		
		this.screen.pause();
		this.screen.dispose();
		screen.resume();
		screen.update(0);
		this.screen = screen;
	}
	
	public Screen getCurrentScreen() {
		return screen;
	}
	
	//----------------ME
	public Screen getStartScreen() {
		return new Screen_Loading(this, this);
	}

	@Override
	public void onBackPressed() {
		if(screen.onBackPress_II()==true)
			super.onBackPressed();
	}
	
	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) 
	    {
	    	this.screen.onLongBackPress_II();
	        this.finish();
	        return true;
	    }
	    return super.onKeyLongPress(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		screen.onActivityResult_II(requestCode, resultCode, data);
	}
}
