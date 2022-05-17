package com.bt.objects;

public class BtDevice {
	private String MAC; //the devideId = MAC
	private String name;

	private int bytesSent;
	private int bytesReceived;

	public void addToSent(int bytes) {
		bytesSent += bytes;
	}

	public void addToReceived(int bytes) {
		bytesReceived += bytes;
	}

	public BtDevice(String MAC, String name) {
		this.MAC = MAC;
		this.name = name;
	}

	public final String toString() {
		return "BtDevice [MAC "+MAC +", name "+name+"] sent: "
				+ bytesSent + " bytes, received: " + bytesReceived + " bytes";
	}

	public int getBytesSent() {
		return bytesSent;
	}

	public int getBytesReceived() {
		return bytesReceived;
	}
	
	public String getName(){
		return this.name;
	}

}
