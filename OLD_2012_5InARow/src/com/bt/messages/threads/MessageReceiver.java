package com.bt.messages.threads;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Timer;
import java.util.TimerTask;

import com.bt.BTEnums.MessageType;
import com.bt.exceptions.BtException;
import com.bt.messages.C_Message;
import com.bt.messages.HS_Message;
import com.bt.messages.O_Message;
import com.bt.messages.generic.GenericMessage;
import com.bt.messages.threads.listeners.IIncomingListener;
import com.bt.settings.AppSettings;
import com.bt.utils.BtUtils;
import com.bt.utils.StreamUtils;
import com.fiveInARow.utils.EventListenerList;
import com.fiveInARow.utils.Logger;

/*
 * #1. Reads data(bytes or Object) from inputStream & creates & sends Messages
 * #2. Does validation on message else sends NULL Message -> end up in closing connection
 */
public class MessageReceiver extends Thread {
	private static final String TAG = "MessageReceiver";
	
	private final EventListenerList m_Listeners = new EventListenerList(); 
    private boolean m_RunFlag = true;
    private ObjectInputStream m_ObjectInputStream = null;
    
    //received inputStream is in unknown state (OR socket is in unknownState)
    private InputStream m_UnknownStateInputStream = null;
    private Timer m_StreamTimeOutTimer;
    private boolean m_AmServer;

    public MessageReceiver(InputStream unknownStateInputStream, boolean amServer){
    	Logger.d(TAG, "MessageReceiver()");
    	
    	this.m_UnknownStateInputStream = unknownStateInputStream;
    	this.m_AmServer = amServer;
    }
    
    //listeners 
	public void addIncomingListener(IIncomingListener listener) {
		this.m_Listeners.add(IIncomingListener.class, listener);
	}

	public void removeIncomingListener(IIncomingListener listener) {
		this.m_Listeners.remove(IIncomingListener.class, listener);
	}

	public IIncomingListener[] getIncomingListeners() {
		return this.m_Listeners.getListeners(IIncomingListener.class);
	}
	
	//reads byte data
    private int read(byte[] data){
        try{
            this.m_ObjectInputStream.readFully(data);
        }catch(IOException ioe){
        	Logger.e(TAG, "read()", ioe);
            return -1;
        }
        return data.length;
    }

    //reads an object
    private Object readObject(){
    	Object result = null;
    	try{
    		result = this.m_ObjectInputStream.readObject();
        }catch(IOException ioe){
        	Logger.e(TAG, "readObject()", ioe);
        } catch (ClassNotFoundException cnfe) {
        	Logger.e(TAG, "readObject()", cnfe);
		}
    	
    	return result;
    }
    
    public void run() {
    	Logger.d(TAG, "run()");
    
    	//getting a functional stream (unknownStateInputStream might not be functional)
        try {
        	if(this.m_AmServer==false)
        		this.resetStreamTimeoutTimer();
        	ObjectInputStream aux = new ObjectInputStream(this.m_UnknownStateInputStream);
        	this.setWorkingObjectInputStream(aux);
		} catch (Exception ex) {
			Logger.e(TAG, "run()", ex);
			this.fireOnMessageReceived(null);
			return;
		} finally {
			this.cancelStreamTimeoutTimer();
		}
        
    	byte[] type = new byte[4];
		byte[] length = new byte[4];
		byte[] peerId_part = new byte[20];
		byte[] connectKey_part = new byte[4];
		byte[] cargo_part;
		Object cargoObj;
		
		HS_Message hs_msg = new HS_Message();
		C_Message c_msg = new C_Message();
		O_Message o_msg = new O_Message();
		
		MessageType msg_type;
		
		while(this.m_RunFlag){
			try {
				
				if (this.read(type) < 0){
					throw new BtException("Could not read the 4 bytes representing the type of the message..");
				}
				
				msg_type = MessageType.getByCode(BtUtils.byteArrayToInt(type));
				
				if(msg_type==null){
					throw new BtException("Unrecognized message type, code: "+type);
				}
				
				switch(msg_type){
					case HAND_SHAKE:
						if (this.read(peerId_part) < 0){
							throw new BtException("Could not read the 20 bytes representing the peerId of the HS_Message..");
						}
						
						if (this.read(connectKey_part) < 0){
							throw new BtException("Could not read the 4 bytes representing the connectKey of the HS_Message..");
						}
						
						hs_msg.setPeerId(peerId_part);
						hs_msg.setConnectKey(connectKey_part);
						this.fireOnMessageReceived(hs_msg);
						break;
				
					case OBJECT_CARGO_HOLDER://object cargo
						cargoObj = this.readObject();
						o_msg.setCargo(cargoObj);
						this.fireOnMessageReceived(o_msg);
						break;
					
					default: //all others contain bytes in cargo & have same blocks
						if (this.read(length) < 0){
							throw new BtException("Could not read the 4 bytes representing the length of the C_Message..");
						}
						
						c_msg.setType(msg_type);
						int l = BtUtils.byteArrayToInt(length);
						
						if(C_Message.isValid(l)==false){
							throw new BtException(l +"is a invalid C_Message cargo length..");
						}
						
						cargo_part = new byte[l];
						if (this.read(cargo_part) < 0){
							throw new BtException("Could not read the "+l+" bytes representing the cargo of the C_Message..");
						}
						
						c_msg.setCargo(cargo_part);
						this.fireOnMessageReceived(c_msg);
						break;
				}
			//any exception caught means "Rules were not respected"!
			} catch (Exception e) { 
				Logger.e(TAG, "run()", e);
				this.fireOnMessageReceived(null);
			}		
		}//end while
		
		StreamUtils.closeQuietly(TAG, m_ObjectInputStream);
		
		Logger.d(TAG, "run() - Ended..");
	}

	// Sets the runFlag to false, causing the thread to stop on its next loop.
	public void stopThread() {
		this.m_RunFlag = false;
	}
	
	//FIRE methods
	private void fireOnMessageReceived(GenericMessage message) {
		for (IIncomingListener listener : this.getIncomingListeners()) {
			listener.onMessageReceived(message);
		}
	}
	
	//Stream TimeOut Timer
	private void cancelStreamTimeoutTimer(){
    	Logger.d(TAG, "cancelStreamTimeoutTimer()");
    	
    	if (this.m_StreamTimeOutTimer != null) {
			this.m_StreamTimeOutTimer.cancel();
			this.m_StreamTimeOutTimer = null;
    	}
    }
    
	private void resetStreamTimeoutTimer(){
		Logger.d(TAG, "resetStreamTimeoutTimer()");
		
		// cancel current timer
		this.cancelStreamTimeoutTimer();
		
		// setup again
		this.m_StreamTimeOutTimer = new Timer();
		this.m_StreamTimeOutTimer.schedule(new TimerTask(){
				private static final String TAG = "MessageReceiver-StreamTimeoutTimer";
				
				@Override
				public void run() {
					Logger.d(TAG, "run()");
					if(getWorkingObjectInputStream()==null){
						fireOnMessageReceived(null); //this will cancel thread
					}
				}
			}, AppSettings.WAIT_TIME_FOR_SOCKET_TO_BECOME_CONNECTED);
	}
	
	private synchronized ObjectInputStream getWorkingObjectInputStream(){
		return this.m_ObjectInputStream;
	}
	
	private synchronized void setWorkingObjectInputStream(ObjectInputStream stream){
		this.m_ObjectInputStream = stream;
	}
}
