package com.fiveInARow.platform;

import android.app.Application;

import com.fiveInARow.framework.AndroidFileSystemUtils;
import com.fiveInARow.utils.Logger;

public class GameApplication extends Application{
	private static final String TAG = "GameApplication";
	
	public static GameApplication INSTANCE = null;
	
	@Override
	public void onCreate() {
		Logger.d(TAG, "onCreate()");
		super.onCreate();
		
		INSTANCE = this;
		
		//init
		AndroidFileSystemUtils.preuseInit(this);
	}
}
