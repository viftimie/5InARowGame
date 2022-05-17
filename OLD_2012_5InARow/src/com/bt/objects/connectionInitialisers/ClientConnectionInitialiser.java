package com.bt.objects.connectionInitialisers;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.bt.BTEnums.FailedHandshakeReason;
import com.bt.objects.connectionInitialisers.generic.GenericConnectionInitialiser;
import com.bt.platform.listeners.IHandshakeAndGameInitListener;
import com.bt.settings.AppSettings;
import com.bt.utils.StreamUtils;
import com.fiveInARow.utils.Logger;

public class ClientConnectionInitialiser extends GenericConnectionInitialiser {
	private static final String TAG = "ClientConnectionInitialiser";
	
	private BluetoothDevice m_ServerDevice;
	private BluetoothSocket m_Socket;
	private Timer m_SocketTimeOutTimer;//server has accept(timeOut)
	
	public ClientConnectionInitialiser(BluetoothAdapter adapter, BluetoothDevice serverDevice) {
		super(adapter);
		this.m_ServerDevice = serverDevice;
	}

	@Override
	protected boolean tryCreateSocket() {
		Logger.d(TAG, "tryCreateSocket()");
		
		// Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
        	this.m_Socket = this.m_ServerDevice.createRfcommSocketToServiceRecord(UUID.fromString(AppSettings.MY_APP_UUID));
            return true;
        } catch (IOException ioe) { 
        	Logger.e(TAG, "tryCreateSocket()", ioe);
        	return false;
        }
	}
	
	@Override
	protected void makeConnection() throws Exception {
		Logger.d(TAG, "makeConnection()");
		
		// Cancel discovery because it will slow down the connection
    	if(this.m_BtAdapter.isDiscovering())
    		this.m_BtAdapter.cancelDiscovery();
 
    	// Connect the device through the socket. This will block until it succeeds or throws an exception
    	this.resetSocketTimeoutTimer();
    	this.m_Socket.connect();
    	this.cancelSocketTimeoutTimer();
    	
    	// Do work to manage the connection (in a separate thread)
    	this.fireOnConnectTo(this.m_Socket); 
	}

	@Override
	protected void clean() {
		Logger.d(TAG, "tryClean()");
		//code should not reach this section
		StreamUtils.closeQuietly(TAG, this.m_Socket);
	}
	
	@Override
	public void stopThread() {
		//not used since it is client, life span is SMALL
	}

	@Override
	protected final void failedToMakeConnection() {
		Logger.d(TAG, "failedToMakeConnection()");
		
		this.cancelSocketTimeoutTimer();
		for (IHandshakeAndGameInitListener listener : this.getHandshakeListeners()) {
			listener.onFailedHandshake(FailedHandshakeReason.SOCKET_REASONS);
		}
	}
	
	public void addHandshakeListener(IHandshakeAndGameInitListener listener) {
		this.m_Listeners.add(IHandshakeAndGameInitListener.class, listener);
	}

	public void removeHandshakeListener(IHandshakeAndGameInitListener listener) {
		this.m_Listeners.remove(IHandshakeAndGameInitListener.class, listener);
	}

	public IHandshakeAndGameInitListener[] getHandshakeListeners() {
		return this.m_Listeners.getListeners(IHandshakeAndGameInitListener.class);
	}
	
	//for Socket TimeOut Timer
	private void cancelSocketTimeoutTimer() {
		Logger.d(TAG, "cancelSocketTimeoutTimer()");
		
		if (this.m_SocketTimeOutTimer != null) {
			this.m_SocketTimeOutTimer.cancel();
			this.m_SocketTimeOutTimer = null;
		}
	}

	private void resetSocketTimeoutTimer() {
		Logger.d(TAG, "resetSocketTimeoutTimer()");

		// cancel current timer
		this.cancelSocketTimeoutTimer();

		// setup again
		this.m_SocketTimeOutTimer = new Timer();
		this.m_SocketTimeOutTimer.schedule(new TimerTask() {
			private static final String TAG = "ClientConnectionInitialiser-SocketTimeoutTimer";

			@Override
			public void run() {
				Logger.d(TAG, "run()");
				// i want to cancel a running method
				ClientConnectionInitialiser.this.interrupt();
			}
		}, AppSettings.WAIT_TIME_FOR_SOCKET_TO_BECOME_CONNECTED);
	}

}
