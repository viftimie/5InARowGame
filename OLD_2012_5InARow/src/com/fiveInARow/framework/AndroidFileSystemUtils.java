package com.fiveInARow.framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.os.Environment;

import com.fiveInARow.framework.support.ExternalStorageHelper;
import com.fiveInARow.utils.Logger;

public class AndroidFileSystemUtils {
	private static final String TAG = "AndroidFileSystemUtils";
	private static AndroidFileSystemUtils INSTANCE;
	
	public static AndroidFileSystemUtils getINSTANCE(){
		if(INSTANCE==null)
			INSTANCE = new AndroidFileSystemUtils();
		
		return INSTANCE;
	}
	
	public static void preuseInit(Context ct){
		m_Context = ct;
	}
	
	private static Context m_Context;
	private String m_InternalStoragePath; //aka internal
	
	private AndroidFileSystemUtils(){
		this.m_InternalStoragePath=  m_Context.getFilesDir().getAbsolutePath()+ File.separator;
	}

	public InputStream readAsset(String fileName) throws IOException {
		return m_Context.getAssets().open(fileName);
	}
	
	public InputStream readFileFromExternalFolder(String fileName) throws IOException {
		return new FileInputStream(ExternalStorageHelper.getExternalWritableFile(m_Context, fileName));
	}
	
	public OutputStream writeFileInExternalFolder(String fileName) throws IOException {
		return new FileOutputStream(ExternalStorageHelper.getExternalWritableFile(m_Context, fileName));
	}
	
	public InputStream readFileFromInternalFolder(String fileName) throws IOException {
		return new FileInputStream(this.m_InternalStoragePath + fileName);
	}
	
	public OutputStream writeFileInInternalFolder(String fileName) throws IOException {
		return new FileOutputStream(this.m_InternalStoragePath + fileName);
	}
	
	public boolean isExternalStorageReady(){
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    Logger.d(TAG, "External storage is good to go!");
			return true;
		} else {
			if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
				Logger.e(TAG, "Cannot write to external storage!", null);
			} else {
				Logger.e(TAG, "External storage not mounted!", null);
			}
			return false;
		}
	}
	
}
