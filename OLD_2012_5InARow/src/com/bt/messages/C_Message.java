package com.bt.messages;

import com.bt.BTEnums.MessageType;
import com.bt.messages.generic.GenericMessage;
import com.bt.settings.AppSettings;
import com.bt.utils.BtUtils;

//message with cargo
public class C_Message extends GenericMessage{
	private byte[] m_Cargo = new byte[] {};// can be empty (EX: for Keep Alive)

	public C_Message(MessageType type) {
		super(type);
	}
	
	public C_Message() {
		super();
	}

	public void setCargo(byte[] cargo){
		this.m_Cargo = cargo;
	}
	
	public byte[] getCargo(){
		return this.m_Cargo;
	}
	
	@Override
	protected final byte[] generate() {
		byte[] length = BtUtils.intToByteArray(this.m_Cargo.length);
		return BtUtils.concatByteArrays(this.m_Type, 
				                        length, 
				                        this.m_Cargo);
	}

	@Override
	protected final boolean isValid() {
		return C_Message.isValid(this.m_Cargo.length);
	}
	
	@SuppressWarnings("unused")
	public static final boolean isValid (int cargoLength){
		if(AppSettings.C_MESSAGE_MAX_CARGO==-1)
			return true;
		
		if(cargoLength > AppSettings.C_MESSAGE_MAX_CARGO)
			return false;
		
		return true;
	}
	
}

