package com.bt.settings;

import com.bt.utils.BtUtils;

public class AppSettings {
	//Constants
	public static final String MY_APP_NAME = "5INAROW";
	public static final String MY_APP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
	
	public static final long WAIT_TIME_BEFORE_SENDING_KA = 60000; //1 min
	public static final long WAIT_TIME_BEFORE_KILLING_CONNECTION = 120000; //2 min
	public static final long WAIT_TIME_FOR_SERVER_TO_RETRY_FOR_A_NEW_CLIENT_CONNECTION = 2000; //2 sec
	
	//Generated at start-up
	public static final byte[] MY_APP_DEVICE_ID = BtUtils.generateID();//20bytes
	public static final byte[] MY_APP_CONNECT_KEY = BtUtils.generateConnectKey();//1000->9999
	
	//Configurable
	public static final int C_MESSAGE_MAX_CARGO = 56;//= 64 total | -1 = no limit
	public static final int SERVER_MAX_CONNECTIONS = 1;
	public static final long WAIT_TIME_FOR_SOCKET_TO_BECOME_CONNECTED = 3500; //3.5 seconds, visually it wont seem 3.5 seconds
}
