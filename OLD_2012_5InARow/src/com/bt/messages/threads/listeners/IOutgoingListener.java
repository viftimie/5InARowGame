package com.bt.messages.threads.listeners;

import java.util.EventListener;

public interface IOutgoingListener extends EventListener{
	
    public void onConnectionClosed();
    public void onKeepAliveSent();
    
}
