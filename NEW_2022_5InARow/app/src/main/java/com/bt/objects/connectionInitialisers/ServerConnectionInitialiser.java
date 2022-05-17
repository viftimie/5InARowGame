package com.bt.objects.connectionInitialisers;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import com.bt.objects.connectionInitialisers.generic.GenericConnectionInitialiser;
import com.bt.settings.AppSettings;
import com.bt.utils.StreamUtils;
import com.fiveInARow.utils.Logger;

//thread that listens on a serverSocket
public class ServerConnectionInitialiser extends GenericConnectionInitialiser {
	private static final String TAG = "ServerConnectionInitialiser";
	
	private BluetoothServerSocket m_Socket = null;
	private boolean m_AllowMultipleConnections = false; 
	private boolean m_RunFlag = true;
	
	public ServerConnectionInitialiser(BluetoothAdapter adapter, boolean allowMultipleConnections) {
		super(adapter);
		this.m_AllowMultipleConnections = allowMultipleConnections;
	}
	
	public ServerConnectionInitialiser(BluetoothAdapter adapter) {
		super(adapter);
		this.m_AllowMultipleConnections = false;
	}

	@Override
	protected boolean tryCreateSocket() {
		Logger.d(TAG, "tryCreateSocket()");
		
        try {
            //NOTE: MY_UUID is the app's UUID string, also used by the client code
        	this.m_Socket = this.m_BtAdapter.listenUsingRfcommWithServiceRecord(AppSettings.MY_APP_UUID, UUID.fromString(AppSettings.MY_APP_UUID));
        	return true;
        } catch (IOException ioe) { 
        	Logger.e(TAG, "tryCreateSocket()", ioe);
        	return false;
        }
	}
	
	@Override
	protected void makeConnection() throws Exception {
		Logger.d(TAG, "makeConnection()");
		
        BluetoothSocket aux = null;
        
        // Keep listening until exception occurs or a socket is returned
        while (this.m_RunFlag) {
        	
        	aux = this.m_Socket.accept();
    		// Note that when accept() returns the BluetoothSocket, the socket is already connected, so you should not call connect() (as you do from the client-side).
            this.fireOnConnectTo(aux);
            
            if(this.m_AllowMultipleConnections==true)
    			sleep(AppSettings.WAIT_TIME_FOR_SERVER_TO_RETRY_FOR_A_NEW_CLIENT_CONNECTION);
    		else {
    			while(this.m_RunFlag)
    				sleep(AppSettings.WAIT_TIME_FOR_SERVER_TO_RETRY_FOR_A_NEW_CLIENT_CONNECTION);
    		}
        }
        
        //the clean from GenericConnInit is run on Exception Thrown (this might leave socket open)
        this.clean();
	}
	
	@Override
	protected void clean() {
		Logger.d(TAG, "clean()");
		
		StreamUtils.closeQuietly(TAG, this.m_Socket);
	}

	@Override
	public void stopThread() {
		this.m_RunFlag = false;
	}
	
}
