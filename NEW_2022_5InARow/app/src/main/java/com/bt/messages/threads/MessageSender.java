package com.bt.messages.threads;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.bt.exceptions.BtException;
import com.bt.messages.C_Message;
import com.bt.messages.factory.MessageFactory;
import com.bt.messages.generic.GenericMessage;
import com.bt.messages.threads.listeners.IOutgoingListener;
import com.bt.settings.AppSettings;
import com.bt.utils.StreamUtils;
import com.fiveInARow.utils.EventListenerList;
import com.fiveInARow.utils.Logger;

/*
 * #1) sends messages to connected socket
 * #2) received stream is in unknown state, just like in MessageReceiver
 * #3) but since getting INPUT & OUTPUT stream on same socket happens at same time is SAFE as it is
 */
public class MessageSender extends Thread {
	private static final String TAG = "MessageSender";
	
    private ObjectOutputStream m_ObjectOutputStream;
    private LinkedBlockingQueue<GenericMessage> m_MessageQueue;
    private boolean m_RunFlag = true;
    private final EventListenerList m_Listeners = new EventListenerList();

    public MessageSender(OutputStream os) throws IOException {
    	Logger.d(TAG, "MessageSender()");
    	
        this.m_ObjectOutputStream = new ObjectOutputStream(os);
        this.m_MessageQueue = new LinkedBlockingQueue<GenericMessage>();
    }

    //listeners
    public void addOutgoingListener(IOutgoingListener listener) {
    	this.m_Listeners.add(IOutgoingListener.class, listener);
    }

    public void removeOutgoingListener(IOutgoingListener listener) {
    	this.m_Listeners.remove(IOutgoingListener.class, listener);
   }

    public IOutgoingListener[] getOutgoingListeners() {
        return this.m_Listeners.getListeners(IOutgoingListener.class);
    }
    
    //FIRE methods
    private void fireOnConnectionClosed() {
        for (IOutgoingListener listener : this.getOutgoingListeners()) {
            listener.onConnectionClosed();
        }
    }

    private void fireOnKeepAliveSent() {
        for (IOutgoingListener listener : this.getOutgoingListeners()) {
            listener.onKeepAliveSent();
        }
    }

    //message in the queue, waiting to be sent
    public synchronized void addMessageToQueue(GenericMessage message){
    	//TODO: REMOVE
    	Logger.d(TAG, "addMessageToQueue() MAC ["+((message==null)? "NULL MSG!!" : message.toString()));
        this.m_MessageQueue.add(message);
    }

    public void run() {
    	Logger.d(TAG, "run()");
    	
        GenericMessage out = null;
        C_Message ka = MessageFactory.new_KeepAlive_Msg();
        
        try {
        	//flushing NECESARY esle IT WONT WORK!
        	this.m_ObjectOutputStream.flush();
            
            while (this.m_RunFlag) {
                if(this.m_MessageQueue != null && this.m_ObjectOutputStream != null)
                	out = this.m_MessageQueue.poll(AppSettings.WAIT_TIME_BEFORE_SENDING_KA, TimeUnit.MILLISECONDS);
                
                if(out != null){
                	out.writeYoureSelfTo(this.m_ObjectOutputStream);	
                    out = null;
                }else if(this.m_RunFlag){
                    ka.writeYoureSelfTo(this.m_ObjectOutputStream);
                    this.fireOnKeepAliveSent();
                }
            }
        } catch (InterruptedException ie) {
        	Logger.e(TAG, "run()", ie);//TODO: ??
        }catch(BtException bte){
        	Logger.e(TAG, "run()", bte);
        	this.fireOnConnectionClosed();
        } catch(Exception e){
        	Logger.e(TAG, "run()", e);
            this.fireOnConnectionClosed();
        }

        if(this.m_MessageQueue != null)
            this.m_MessageQueue.clear();
        this.m_MessageQueue = null;
        
        StreamUtils.closeQuietly(TAG, this.m_ObjectOutputStream);
        
        Logger.d(TAG, "run() - Ended..");
    }

	//Sets the 'run' variable to false, causing the thread to stop on its next loop.
    public void stopThread(){
        this.m_RunFlag = false;
    }
}
