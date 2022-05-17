package com.bt.messages.generic;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import com.bt.BTEnums.MessageType;
import com.bt.exceptions.BtException;
import com.bt.utils.BtUtils;

public abstract class GenericMessage{
	
	protected byte[] m_Type = new byte[4];//OR code of MessageType
	
	protected GenericMessage(){
    }
	
	protected GenericMessage(MessageType type){
		this.setType(type);
    }
	
	public final void setType(MessageType type){
		this.m_Type = BtUtils.intToByteArray(type.getCode());
	}
	
	public final MessageType getType() {
		return MessageType.getByCode(BtUtils.byteArrayToInt(this.m_Type));
	}
	
	@Override
	public String toString (){
		byte[] content = this.generate();
		int lenght = content.length;
		return "Message of type ["+ getType().toString() +"] content (size "+lenght+"): " +Arrays.toString(content);
	}
	
	protected abstract byte[] generate();
	
	public void writeYoureSelfTo (ObjectOutputStream out) throws BtException{
		boolean isValid = isValid();
		if(isValid==false)
			throw new BtException("writeYoureSelfTo() - Invalid message..");
		else {
			try {
				out.write(this.generate());
				out.flush();
			} catch (IOException ioe) {
				throw new BtException("writeYoureSelfTo() - Could not write msg to outputStream..");
			}
		}
	}
	
	protected abstract boolean isValid();

}
