package com.bt.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import com.fiveInARow.utils.Logger;

public class StreamUtils {
	public static void closeQuietly(String TAG, InputStream is){
		if(is!=null)
			try {			
				is.close();
				is = null;
			} catch (Exception e) {
				Logger.e(TAG, "while closing stream..", e);
			}
	}
	
	public static void closeQuietly(String TAG, OutputStream out){
		if(out!=null)
			try {			
				out.close();
				out = null;
			} catch (Exception e) {
				Logger.e(TAG, "while closing stream..", e);
			}
	}
	
	public static void closeQuietly(String TAG, BluetoothSocket socket){
		if(socket!=null)
			try {
				socket.close();
				socket = null;
			} catch (IOException ioe) { 
				Logger.e(TAG, "while closing socket", ioe);
			}
	}
	
	public static void closeQuietly(String TAG, BluetoothServerSocket socket){
		if(socket!=null)
			try {
				socket.close();
				socket = null;
			} catch (IOException ioe) { 
				Logger.e(TAG, "while closing socket", ioe);
			}
	}
	
}
