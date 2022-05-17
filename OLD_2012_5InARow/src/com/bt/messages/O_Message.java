package com.bt.messages;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import com.bt.BTEnums.MessageType;
import com.bt.exceptions.BtException;
import com.bt.messages.generic.GenericMessage;

public class O_Message extends GenericMessage{
	private Object m_Cargo;

	public O_Message(){
		super(MessageType.OBJECT_CARGO_HOLDER);
	}
	
	public Object getCargo() {
		return this.m_Cargo;
	}

	public void setCargo(Object cargo) {
		this.m_Cargo = cargo;
	}
	
	@Override
	public final byte[] generate() {
		return this.m_Type;
	}
	
	@Override
	public final void writeYoureSelfTo(ObjectOutputStream out) throws BtException {
		try {
			super.writeYoureSelfTo(out);
			out.writeObject(this.m_Cargo);
			out.flush();
		} catch (IOException ioe) {
			throw new BtException("writeYoureSelfTo() - Could not write msg to outputStream..");
		}	
	}
	
	@Override
	public String toString (){
		byte[] content = this.generate();
		int lenght = content.length;
		return "Message of type ["+ getType().toString() +"] byte content (size "+lenght+"): " +Arrays.toString(content) + " Object content "+this.m_Cargo.getClass()+" value "+this.m_Cargo.toString();
	}

	@Override
	protected boolean isValid() {
		return true;
	}
}
