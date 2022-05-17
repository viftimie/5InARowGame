package com.bt.messages;

import com.bt.BTEnums.MessageType;
import com.bt.messages.generic.GenericMessage;
import com.bt.utils.BtUtils;

public class HS_Message extends GenericMessage{
	private static final int PEER_ID_LENGTH = 20;
	private static final int CONNECT_KEY_LENGTH = 4;
	
	private byte[] m_PeerId;
	private byte[] m_ConnectKey = new byte[CONNECT_KEY_LENGTH];
	
	public HS_Message() {
		super(MessageType.HAND_SHAKE);
	}
	
	//Construct used by Server (no ConnectKey)
	public HS_Message(byte[] peerId)  {
		super(MessageType.HAND_SHAKE);
		this.setPeerId(peerId);
	}
	
	//Construct used by Client (with ConnectKey = input from user)
	public HS_Message(byte[] peerId, byte[] connectKey)  {
		super(MessageType.HAND_SHAKE);
		this.setPeerId(peerId);
		this.setConnectKey(connectKey);
	}
	
	public void setPeerId (byte[] peerId){
		this.m_PeerId = peerId;
	}
	
	public byte[] getPeerId (){
		return this.m_PeerId;
	}
	
	public void setConnectKey (byte[] connectKey){
		this.m_ConnectKey = connectKey;
	}
	
	public byte[] getConnectKey (){
		return this.m_ConnectKey;
	}

	@Override
	protected final byte[] generate() {	
		return BtUtils.concatByteArrays(this.m_Type, 
				                        this.m_PeerId, 
				                        this.m_ConnectKey);
	}

	@Override
	protected boolean isValid() {
		if(this.m_PeerId.length!=PEER_ID_LENGTH)
			return false;
		
		if(this.m_ConnectKey.length!=CONNECT_KEY_LENGTH)
			return false;
		
		return true;
	}

}
