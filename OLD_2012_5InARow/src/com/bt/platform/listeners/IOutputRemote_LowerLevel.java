package com.bt.platform.listeners;

import java.util.EventListener;

import com.bt.messages.generic.GenericMessage;

/*
 * so i dont have to split the main path of code at the unnecessary level 
 * code will eventually reach a IOutputRemote 
 */

public interface IOutputRemote_LowerLevel extends EventListener{
	
	//TODO: test if this is used??
	public void onRemoteDeviceEndedConnection(String btDeviceId);
	public void onRemoteDeviceSent_X_Msg(String btDeviceId, GenericMessage genericMessage);
	public void onRemoteDeviceSent_KeepAlive_Msg(String btDeviceId);
	
}
