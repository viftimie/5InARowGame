package com.fiveInARow.framework.support;

import java.io.File;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.fiveInARow.utils.Logger;

public class ExternalStorageHelper {
	private static final String TAG = "ExternalStorageHelper";
	
	public static File getExternalWritableFile(Context context, String filename){		
		Logger.d(TAG, "getWritableFile() - "+filename);
		GenericExternalStorageHelper externalStorage;
		int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
		if (sdkVersion >= 8) {
			externalStorage =  new ConcreteExternalStorageHelper_GET8(context);
        } else {
        	externalStorage = new ConcreteExternalStorageHelper_LET7(context);
        }
		
		return externalStorage.getExternalWritableFile(filename);
	}
}

abstract class GenericExternalStorageHelper {
	protected static final String TAG = "GenericExternalStorageHelper";
	
	public File getExternalWritableFile(String filename) {
		return new File(getWorkingDir(), filename);
	}
	
	public abstract File getWorkingDir();
}

class ConcreteExternalStorageHelper_LET7 extends GenericExternalStorageHelper{ //api <=7
	private Context m_Context;
	
	public ConcreteExternalStorageHelper_LET7(Context context){
		Logger.d(TAG, "ConcreteExternalStorageHelper_LET7 (API<=7)");
	}
	
	@Override
	public File getWorkingDir() {
		String externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath(); // = radacina SD card
    	String subPahtToFolder = "/Android/data/"+this.m_Context.getPackageName()+"/files/";
    	File workingDir = new File(externalStoragePath+subPahtToFolder);
    	if(workingDir.exists()==false)
        	workingDir.mkdirs();
    	return workingDir;
	}
}

class ConcreteExternalStorageHelper_GET8 extends GenericExternalStorageHelper { //api >=8
	private Context m_Context;
	
	public ConcreteExternalStorageHelper_GET8(Context context){
		Logger.d(TAG, "ConcreteExternalStorageHelper_GET8 (API>=8)");
		this.m_Context = context;
	}
	
	@Override
	public File getWorkingDir() {
		File workingDir = this.m_Context.getExternalFilesDir(null);// = direct in fisierul aplicatiei
        if(workingDir.exists()==false)
        	workingDir.mkdirs();
        return workingDir;
	}
}
