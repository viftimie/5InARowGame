package com.bt.objects.connectionInitialisers.generic;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;

import com.bt.objects.connectionInitialisers.listeners.IConnectionListener;
import com.fiveInARow.utils.EventListenerList;
import com.fiveInARow.utils.Logger;

public abstract class GenericConnectionInitialiser extends Thread{
	private final static String TAG = "GenericConnectionInitialiser";
	
	protected final EventListenerList m_Listeners = new EventListenerList();
	protected BluetoothAdapter m_BtAdapter;
	private boolean m_HaveCreatedTheSocket = false;
	
	public GenericConnectionInitialiser(BluetoothAdapter adapter){
		this.m_BtAdapter = adapter;
		this.setDaemon(true);
	}
	
	//listeners
	public final void addConnectionListener(IConnectionListener listener) {
		this.m_Listeners.add(IConnectionListener.class, listener);
	}

	public final void removeConnectionListener(IConnectionListener listener) {
		this.m_Listeners.remove(IConnectionListener.class, listener);
	}

	public final IConnectionListener[] getConnectionListeners() {
		return this.m_Listeners.getListeners(IConnectionListener.class);
	}
	
	//FIRE methods
	protected final void fireOnConnectTo(BluetoothSocket socket) {
		Logger.d(TAG, "fireOnConnectTo()");
		for (IConnectionListener listener : this.getConnectionListeners()) {
			listener.onConnectTo(socket);
		}
	}
	
	//will be overriten by clientInitialiser
	protected void failedToMakeConnection() {
	}
	
	public final void run() {
		Logger.d(TAG, "run()");
		
		while(this.m_HaveCreatedTheSocket==false)
			this.m_HaveCreatedTheSocket = this.tryCreateSocket();	
		Logger.d(TAG, "run() - just created a socked to work with..");
		
		try {
			this.makeConnection();
		} catch (Exception e) {
			Logger.e(TAG, "run() - makeConnection()", e);
			this.failedToMakeConnection();
			this.clean();
		}
		
		Logger.d(TAG, "run() - Ended..");
	}

	protected abstract void makeConnection() throws Exception;

	protected abstract boolean tryCreateSocket();

	protected abstract void clean();
	
	//used only when forcing a clean due to closing ConnectionsManager (for client especially)
	public final void forcedStopAndClean(){
		this.stopThread();
		this.clean(); //to be sure
	}
	
	public abstract void stopThread();
}
