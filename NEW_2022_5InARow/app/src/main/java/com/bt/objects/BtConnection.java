package com.bt.objects;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;

import missingInAndroid.EventListenerList;
import android.bluetooth.BluetoothSocket;

import com.bt.BTEnums.BTConnectionState;
import com.bt.BTEnums.FailedHandshakeReason;
import com.bt.BTEnums.MessageType;
import com.bt.exceptions.BtException;
import com.bt.messages.C_Message;
import com.bt.messages.HS_Message;
import com.bt.messages.factory.MessageFactory;
import com.bt.messages.generic.GenericMessage;
import com.bt.messages.threads.MessageReceiver;
import com.bt.messages.threads.MessageSender;
import com.bt.messages.threads.listeners.IIncomingListener;
import com.bt.messages.threads.listeners.IOutgoingListener;
import com.bt.platform.listeners.IHandshakeAndGameInitListener;
import com.bt.platform.listeners.IOutputRemote_LowerLevel;
import com.bt.settings.AppSettings;
import com.bt.utils.BtUtils;
import com.bt.utils.StreamUtils;
import com.fiveInARow.utils.Logger;

public class BtConnection extends Thread implements IIncomingListener, IOutgoingListener{
	private static final String TAG = "BtConnection";
	
	private final EventListenerList m_Listeners = new EventListenerList();
	private BluetoothSocket m_WorkingSocket;
	
	private OutputStream m_OutputStream = null;
	private InputStream m_InputStream = null;
	
	private MessageSender m_MessageSender = null;
	private MessageReceiver m_MessageReceiver = null;
	
	private boolean m_AmServer = false;
	private byte[] m_ConnectKey = new byte[4];
	private BTConnectionState m_CurrentState = BTConnectionState.INITIALISING;
	
	private boolean m_RunFlag = false;
	private long m_TimeOfLastMessage;
	
	//Constructor used by client
	public BtConnection(BluetoothSocket socket, byte[] connectKey) {
		Logger.d(TAG, "BtConnection() MAC ["+socket.getRemoteDevice().getAddress()+"]");
		
		this.m_AmServer = false;//client sends HS first
		this.m_ConnectKey = connectKey;
		this.m_RunFlag = true;
		this.m_WorkingSocket = socket;
	}
	
	//Constructor used by server
	public BtConnection(BluetoothSocket socket) {
		Logger.d(TAG, "BtConnection() MAC ["+socket.getRemoteDevice().getAddress()+"]");
		
		this.m_AmServer = true;//client sends HS first
		this.m_RunFlag = true;
		this.m_WorkingSocket = socket;
	}
	
	public void run() {
		Logger.d(TAG, "run()");
		
		try {
			this.initConnection();

			/**
			 * Wait for the task to end
			 */
			while (this.m_RunFlag)
				synchronized (this) {
					this.wait();
				}
		} catch (UnknownHostException uhe) {
			Logger.e(TAG, "run()", uhe);
		} catch (IOException ioe) {
			Logger.e(TAG, "run()", ioe);
		} catch (InterruptedException ie) {
			Logger.e(TAG, "run()", ie);
		} finally {
			this.fireOnRemoteDeviceEndedConnection();
			this.stopThreadAndClean();
		}
		
		Logger.d(TAG, "run() - Ended..");
	}

	
	//listeners
    public synchronized void addOutputRemote_LowerLevel(IOutputRemote_LowerLevel listener) {
    	this.m_Listeners.add(IOutputRemote_LowerLevel.class, listener);
    }

    public synchronized void removeOutputRemote_LowerLevel(IOutputRemote_LowerLevel listener) {
    	this.m_Listeners.remove(IOutputRemote_LowerLevel.class, listener);
    }

    public synchronized IOutputRemote_LowerLevel[] getOutputRemote_LowerLevelListeners() {
        return this.m_Listeners.getListeners(IOutputRemote_LowerLevel.class);
    }
    
    public void addHandshakeListener(IHandshakeAndGameInitListener listener) {
		this.m_Listeners.add(IHandshakeAndGameInitListener.class, listener);
	}

	public void removeHandshakeListener(IHandshakeAndGameInitListener listener) {
		this.m_Listeners.remove(IHandshakeAndGameInitListener.class, listener);
	}

	public IHandshakeAndGameInitListener[] getHandshakeListeners() {
		return m_Listeners.getListeners(IHandshakeAndGameInitListener.class);
	}
    
    //Object logic
	private void initConnection() throws IOException {
		this.m_InputStream=m_WorkingSocket.getInputStream();
        this.m_OutputStream=m_WorkingSocket.getOutputStream();
           		
        this.m_MessageSender = new MessageSender(this.m_OutputStream);
        this.m_MessageSender.addOutgoingListener(this);
        this.m_MessageSender.start();
        
        this.m_MessageReceiver = new MessageReceiver(this.m_InputStream, this.m_AmServer);
        this.m_MessageReceiver.addIncomingListener(this);
        this.m_MessageReceiver.start();

        //upon init client sends HS
        if(this.m_AmServer==false)
        	this.m_MessageSender.addMessageToQueue(MessageFactory.new_ClientHandshake_Msg(this.m_ConnectKey));
        this.m_CurrentState=BTConnectionState.WAIT_HS;
    }
	
	public void stopThreadAndClean(){
		this.m_CurrentState=BTConnectionState.ENDING;
		
		this.m_RunFlag=false;
		synchronized (this) {
			if (this.m_MessageSender != null) {
				this.m_MessageSender.stopThread();
				this.m_MessageSender = null;// so it's collected by CG
			}

			if (this.m_MessageReceiver != null) {
				this.m_MessageReceiver.stopThread();
				this.m_MessageReceiver = null;// so it's collected by CG
			}

			//might be allready closed..but who cares
			StreamUtils.closeQuietly(TAG, this.m_WorkingSocket);
		}
	} 
		
	public void sendMessage(GenericMessage message){
		this.m_MessageSender.addMessageToQueue(message);
	}

	private void fireOnCorrectHandshake() {
		for (IHandshakeAndGameInitListener listener : this.getHandshakeListeners()) {
			listener.onCorrectHandshake();
		}
	}
	
	private void fireOnFailedHandshake(FailedHandshakeReason reason) {
		for (IHandshakeAndGameInitListener listener : this.getHandshakeListeners()) {
			listener.onFailedHandshake(reason);
		}
	}
	
	private void fireOnRecordReceived(int gamesWon, int gamesDraw, int gamesLost) {
		for(IHandshakeAndGameInitListener listener: this.getHandshakeListeners()){
			listener.onReceivedRecordMessage(gamesWon, gamesDraw, gamesLost);
		}
	}
	
	//FROM IIncomingListener
	public void onMessageReceived(GenericMessage m) {
		Logger.d(TAG, "onMessageReceived()");
		
		//if state doesnt deal with incomming messages OR working socket is NULL.. exit
		if(this.m_CurrentState==BTConnectionState.INITIALISING || this.m_CurrentState==BTConnectionState.ENDING)
			return;
		
		if (this.m_WorkingSocket==null){
			Logger.e(TAG, "messageReceived()", new BtException("workingSocket is null!!"));
			return;
		}
			
		Logger.d(TAG, "messageReceived() MAC ["+this.m_WorkingSocket.getRemoteDevice().getAddress()+"] : "+((m==null)? "NULL MSG!!" : m.toString()));
		if (m == null) {
			Logger.d(TAG, "Null message recived!");

			if(m_CurrentState==BTConnectionState.WAIT_HS)
				this.fireOnFailedHandshake(FailedHandshakeReason.SOCKET_REASONS);
			this.fireOnRemoteDeviceEndedConnection();
            return;
        }
		
		this.m_TimeOfLastMessage = System.currentTimeMillis();
		
		MessageType messageType = m.getType();
		switch (messageType) {
		case HAND_SHAKE:
			if(this.m_CurrentState!=BTConnectionState.WAIT_HS){
				Logger.d(TAG, "Received HS_Message again..");
				this.fireOnRemoteDeviceEndedConnection();
				return;
			} 
			
			if(this.m_AmServer==true && BtUtils.compareByteArrays(((HS_Message)m).getConnectKey(), AppSettings.MY_APP_CONNECT_KEY)==false) {
				Logger.d(TAG, "Received a wrong connectKey in the HS_Message..");
				this.fireOnFailedHandshake(FailedHandshakeReason.WRONG_CONNECT_KEY_ON_SERVER);
				this.sendMessage(MessageFactory.new_WrongConnectKey_Msg());
				this.m_CurrentState = BTConnectionState.ENDING;
				this.fireOnRemoteDeviceEndedConnection();
				return;
			}
			
			if(this.m_AmServer==true) {
				this.sendMessage(MessageFactory.new_ServerHandshake_Msg());
				//Server send RECORD after Handshake
				this.sendMessage(MessageFactory.new_Record_Message()); 
			}
			
			this.fireOnCorrectHandshake();
			this.m_CurrentState = BTConnectionState.COMUNICATING;
			break;
			
		case RECORD:
			byte[] cargo = ((C_Message)m).getCargo();
			int gamesWon = BtUtils.byteArrayToInt(BtUtils.subArray(cargo, 0, 4));
			int gamesDraws = BtUtils.byteArrayToInt(BtUtils.subArray(cargo, 4, 4));
			int gamesLost = BtUtils.byteArrayToInt(BtUtils.subArray(cargo, 8, 4));
			
			this.fireOnRecordReceived(gamesWon, gamesDraws, gamesLost);
			if(this.m_AmServer==false) {
				//Client send RECORD after receiving RECORD
				this.sendMessage(MessageFactory.new_Record_Message());
			}
			
			break;
			
		case KILL_CONNECTION:
			//TODO: is this used?? OR usable??
			Logger.d(TAG, "Received a Kill message..");
			this.fireOnRemoteDeviceEndedConnection();
			return;
			
		case WRONG_CONNECT_KEY:
			this.fireOnFailedHandshake(FailedHandshakeReason.WRONG_CONNECT_KEY_ON_CLIENT);
			this.m_CurrentState = BTConnectionState.ENDING;
			this.fireOnRemoteDeviceEndedConnection();
			break;
			
		default:
			//NOTE: interpretation will be done at the corect level (the remote)
			Logger.d(TAG, "Received message of Type: "+ m.getType());
			this.fireOnRemoteDeviceSentMessage(m);
			break;
		}
	}

	//FROM IOutGoingListener
	public void onConnectionClosed() {
		this.fireOnRemoteDeviceEndedConnection();
	}

	public void onKeepAliveSent() {
		Logger.d(TAG, "onKeepAliveSent()");
		if (System.currentTimeMillis() - this.m_TimeOfLastMessage > AppSettings.WAIT_TIME_BEFORE_KILLING_CONNECTION) {
			Logger.d(TAG, "TIME OUT!!!");
			this.fireOnRemoteDeviceEndedConnection();
            return;
        } else 
        	this.fireOnRemoteDeviceIsStillAlive();
	}
	
	//FIRE methods
	private void fireOnRemoteDeviceSentMessage(GenericMessage m) {
		for (IOutputRemote_LowerLevel listener : this.getOutputRemote_LowerLevelListeners()) {
            listener.onRemoteDeviceSent_X_Msg(this.m_WorkingSocket.getRemoteDevice().getAddress(), m);
        }
	}

	private void fireOnRemoteDeviceIsStillAlive() {
		for (IOutputRemote_LowerLevel listener : this.getOutputRemote_LowerLevelListeners()) {
            listener.onRemoteDeviceSent_KeepAlive_Msg(this.m_WorkingSocket.getRemoteDevice().getAddress());
        }
	}
	
	//TODO: test if this is used
	private void fireOnRemoteDeviceEndedConnection() {
		for (IOutputRemote_LowerLevel listener : this.getOutputRemote_LowerLevelListeners()) {
			if(this.m_WorkingSocket!=null)
				listener.onRemoteDeviceEndedConnection(this.m_WorkingSocket.getRemoteDevice().getAddress());
        }
		this.stopThreadAndClean();
	}
	
}
