package com.bt.messages.threads.listeners;

import java.util.EventListener;

import com.bt.messages.generic.GenericMessage;

public interface IIncomingListener extends EventListener{
    
    public void onMessageReceived(GenericMessage message);
    
}
