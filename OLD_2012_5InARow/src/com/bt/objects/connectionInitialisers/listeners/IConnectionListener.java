package com.bt.objects.connectionInitialisers.listeners;

import java.util.EventListener;

import android.bluetooth.BluetoothSocket;

public interface IConnectionListener extends EventListener{
	
	public void onConnectTo(BluetoothSocket socket);
	
}
