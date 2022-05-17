package com.bt.platform;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;

import com.bt.messages.C_Message;
import com.bt.messages.factory.MessageFactory;
import com.bt.messages.generic.GenericMessage;
import com.bt.objects.BtConnection;
import com.bt.objects.BtDevice;
import com.bt.objects.connectionInitialisers.ClientConnectionInitialiser;
import com.bt.objects.connectionInitialisers.ServerConnectionInitialiser;
import com.bt.objects.connectionInitialisers.generic.GenericConnectionInitialiser;
import com.bt.objects.connectionInitialisers.listeners.IConnectionListener;
import com.bt.platform.listeners.IHandshakeAndGameInitListener;
import com.bt.platform.listeners.IInputRemote;
import com.bt.platform.listeners.IOutputRemote;
import com.bt.platform.listeners.IOutputRemote_LowerLevel;
import com.bt.settings.AppSettings;
import com.bt.utils.BtUtils;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.GameStateChangingAction;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerColor;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerFace;
import com.fiveInARow.game.gameWorld.support.sessionSavedData.PlayerDetails;
import com.fiveInARow.utils.Logger;

public class BTService extends Service {
	private static final String TAG = "BTService";
	
	public static final String ACTION_STRING_START_AS_SERVER = "com.bt.BTService.START_SERVICE_AS_SERVER";
	public static final String ACTION_STRING_START_AS_CLIENT = "com.bt.BTService.START_SERVICE_AS_CLIENT";
	public static final String ACTION_STRING_STOP = "com.bt.BTService.STOP_SERVICE";
	public static final String EXTRA_SERVER_TO_CONNECT_TO = "EXTRA_SERVER_TO_CONNECT_TO";
	public static final String EXTRA_MAX_CONNECTIONS = "EXTRA_MAX_CONNECTIONS";
	public static final String EXTRA_CONNECT_KEY = "EXTRA_CONNECT_KEY";
	
	private static BTService m_Instance;//singleton ref
	
	public static BTService getINSTANCE(){
		return m_Instance;
	}
	
	private static IHandshakeAndGameInitListener m_IHandshakeListener;
	
	public static void setHandshakeListener(IHandshakeAndGameInitListener listener){
		m_IHandshakeListener = listener;
	}
	
	private BluetoothAdapter m_BTAdapter;
	private IOutputRemote m_OutputRemote;
	private ConnectionsManager m_ConnectionManager;

	public void setOutputRemote(IOutputRemote remote){ //remote user action will be acting on this listener
		this.m_OutputRemote = remote;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Logger.d(TAG, "onBind()");
		
		return null;
	}
	
	@Override
	public void onCreate() {
		Logger.d(TAG, "onCreate()");
		
		super.onCreate();
		m_Instance = this;
		this.m_BTAdapter = BluetoothAdapter.getDefaultAdapter();
	}
	
	@Override
	public void onDestroy() {
		Logger.d(TAG, "onDestroy()");
		
		super.onDestroy();
		m_Instance = null;
		this.stopEverything();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {//restarts thread if allready started
		Logger.d(TAG, "onStartCommand()");
		
		if (this.m_BTAdapter == null) {
			Logger.d(TAG, "onStartCommand() - Can't continue: no BT on device..");
		} else if (m_BTAdapter.isEnabled() == false){
			Logger.d(TAG, "onStartCommand() - Can't continue: BT not enabled..");
		} else {
			if (intent.getAction().equals(ACTION_STRING_STOP)) {
				this.stopEverything();
			}else if (intent.getAction().equals(ACTION_STRING_START_AS_SERVER)) {
				this.stopEverything();
				this.startAsServer(intent);
			} else if (intent.getAction().equals(ACTION_STRING_START_AS_CLIENT)) {
				this.stopEverything();
				this.startAsClient(intent);
			}
		}
		return START_NOT_STICKY;
	}
	
	private void startAsServer(Intent intent) {
		Logger.d(TAG, "startAsServer() MAC ["+m_BTAdapter.getAddress()+"]");
		
		this.m_ConnectionManager = new ConnectionsManager();
		this.m_ConnectionManager.start();
	}

	private void startAsClient(Intent intent) {
		Logger.d(TAG, "startAsClient()");
		
		BluetoothDevice server = intent.getParcelableExtra(EXTRA_SERVER_TO_CONNECT_TO);//dont pass null -> becomes server
		byte[] connectKey = intent.getByteArrayExtra(EXTRA_CONNECT_KEY);
		this.m_ConnectionManager = new ConnectionsManager(server, connectKey);
		this.m_ConnectionManager.start();
	}
	
	private void stopEverything(){
		Logger.d(TAG, "stopEverything()");
		
		if(this.m_ConnectionManager!=null && this.m_ConnectionManager.isRunning()) {
			this.m_ConnectionManager.stopThread();
		}
		this.m_ConnectionManager = null;
	}
	
	private void broadCastMessage (GenericMessage message){
		Logger.d(TAG, "broadCastMessage()");
		
		if(this.m_ConnectionManager!=null && this.m_ConnectionManager.isRunning()) {
			this.m_ConnectionManager.broadCastMessage(message);
		}
	}
	
	//========================================SERVICE THREAD========================================//
	private class ConnectionsManager extends Thread implements IConnectionListener, IOutputRemote_LowerLevel{
		private String TAG = "ConnectionsManager #"+(new Random().nextInt(100));
		
		private static final long SLEEP_TIME = 5000;//5 seconds
		private Map<String, BtDevice> cm_BTDevices = null;
	    private Map<String, BtConnection> cm_BTConnections = null;
	    private boolean cm_AmServer = false;
	    private GenericConnectionInitialiser cm_ConnectionInitialiser;
	    private BluetoothDevice cm_BTDevice;//for client only
		private volatile boolean cm_RunFlag = false;
		private byte[] cm_ConnectKey = null;
	    
		//for server
		private ConnectionsManager(){
			Logger.d(TAG, "ConnectionsManager()");
			
			this.cm_AmServer = true;
			this.cm_BTDevices = new TreeMap<String, BtDevice>();
	    	this.cm_BTConnections = new TreeMap<String, BtConnection>();
		}
		
	    //for client
	    private ConnectionsManager(BluetoothDevice BTServerDevice, byte[] connectKey) {
	    	Logger.d(TAG, "ConnectionsManager()");
	    	
	    	this.cm_BTDevices = new TreeMap<String, BtDevice>();
	    	this.cm_BTConnections = new TreeMap<String, BtConnection>();
	    	this.cm_BTDevice = BTServerDevice;
	    	this.cm_ConnectKey = connectKey;
	    }
	    
	    @Override
		public void run() {
	    	Logger.d(TAG, "run()");
	    	
	    	this.cm_RunFlag=true;
	    	if(this.cm_AmServer==true)
	    		this.startServerConnectionInitialiser();
	    	else
	    		this.startClientConnectionInitialiser();
	    	
	    	while(this.cm_RunFlag==true){
	    		try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException ie) {
					Logger.e(TAG, "run()", ie);
				}
	    	}
	    	
	    	this.clean();
	    	
	    	Logger.d(TAG, "run() - Ended..");
		}
	    
	    private void clean(){
	    	Logger.d(TAG, "clean()");
	    	
	    	if(this.cm_ConnectionInitialiser!=null){
	    		// just to illustrate purpose of method forcedStopAndClean()
	    		if(this.cm_ConnectionInitialiser instanceof ClientConnectionInitialiser)
	    			this.cm_ConnectionInitialiser.forcedStopAndClean();
	    		else
	    			this.cm_ConnectionInitialiser.stopThread();
	    	}
	    	
	    	for(Entry<String, BtConnection> connection: this.cm_BTConnections.entrySet()){
	    		connection.getValue().stopThreadAndClean();
	    	}
	    	this.cm_BTConnections.clear();
	    	this.cm_BTConnections = null;
	    	this.cm_BTDevices.clear();
	    	this.cm_BTDevices = null;
	    }
	    
	    public synchronized void stopThread(){
	    	Logger.d(TAG, "stopThread()");
	    	
	    	this.cm_RunFlag = false;
	    }

	    public synchronized boolean isRunning(){
	    	return this.cm_RunFlag;
	    }
	    
		private void startServerConnectionInitialiser() {
			Logger.d(TAG, "startServerConnectionInitialiser()");

			this.cm_ConnectionInitialiser = new ServerConnectionInitialiser(m_BTAdapter);
	        this.cm_ConnectionInitialiser.addConnectionListener(this);
	        this.cm_ConnectionInitialiser.start();
		}
		
		private void startClientConnectionInitialiser() {
			Logger.d(TAG, "startClientConnectionInitialiser()");
			
			this.cm_ConnectionInitialiser = new ClientConnectionInitialiser(m_BTAdapter, this.cm_BTDevice);
	        this.cm_ConnectionInitialiser.addConnectionListener(this);
	        //so i can transmit to UI the HandShake succes
	        ((ClientConnectionInitialiser) this.cm_ConnectionInitialiser).addHandshakeListener(m_IHandshakeListener);
	        this.cm_ConnectionInitialiser.start();
		}

		//shouldn't be NULL
		public synchronized void onConnectTo(BluetoothSocket socket) {
			String MAC = socket.getRemoteDevice().getAddress();
			Logger.d(TAG, "onConnectTo() MAC ["+MAC+"]");
			
			BtDevice device = new BtDevice(MAC, socket.getRemoteDevice().getName());
			if(!this.cm_BTDevices.containsKey(MAC) && this.cm_BTConnections.size() < AppSettings.SERVER_MAX_CONNECTIONS){
				this.cm_BTDevices.put(MAC, device);
				
				BtConnection newConnection = null;
				if(this.cm_AmServer==true)
					newConnection = new BtConnection(socket); //amServer = true -> iContactedHim = false
				else
					newConnection = new BtConnection(socket, this.cm_ConnectKey);
				newConnection.addOutputRemote_LowerLevel(this);
				newConnection.addHandshakeListener(m_IHandshakeListener);
				this.cm_BTConnections.put(MAC, newConnection);
				newConnection.start();
			}
		}
		
		//sends to all connections
		private synchronized void broadCastMessage (GenericMessage message){
			Logger.d(TAG, "broadCastMessage()");
			
			for(Entry<String, BtConnection> entry: this.cm_BTConnections.entrySet()){
				try {
					entry.getValue().sendMessage(message);
				} catch (Exception e) {
					Logger.e(TAG, "broadCastMessage", e);
				}
			}
		}
		
		//TODO: find out if this is used
		public synchronized void onRemoteDeviceEndedConnection(String btDeviceId) {
			Logger.d(TAG, "deviceEndedConnection() MAC ["+btDeviceId+"]");
			
			this.cm_BTDevices.remove(btDeviceId);
		    this.cm_BTConnections.remove(btDeviceId);
		    //TODO: will show error screen & then go home
		}
		

		public synchronized void onRemoteDeviceSent_X_Msg(String btDeviceId, GenericMessage genericMessage) {
			Logger.d(TAG, "onRemoteDeviceSent_X_Msg() MAC ["+btDeviceId+"]");
			
			byte[] cargo;
			
			switch (genericMessage.getType()) {
			case NEW_MOVE:
				int x, y;
				cargo = ((C_Message)genericMessage).getCargo();
				x = BtUtils.byteArrayToInt(BtUtils.subArray(cargo, 0, 4));
				y = BtUtils.byteArrayToInt(BtUtils.subArray(cargo, 4, 4));
				if(m_OutputRemote!=null)
					m_OutputRemote.onRemoteDeviceSent_Move_Msg(btDeviceId, x, y);
				break;
				
			case CHANGE_GAME_STATE:
				cargo = ((C_Message)genericMessage).getCargo();
				int action = BtUtils.byteArrayToInt(BtUtils.subArray(cargo, 0, 4));
				
				GameStateChangingAction gameChangingAction = GameStateChangingAction.getByCode(action);
				if(gameChangingAction==GameStateChangingAction.BEGIN){
					if(m_IHandshakeListener!=null)
						m_IHandshakeListener.onClickToBeginGame();
				} else {
					if(m_OutputRemote!=null)
						m_OutputRemote.onRemoteDeviceSent_ChangeGameState_Msg(btDeviceId, gameChangingAction);
				}
				
				break;
				
			case GAME_INIT:
				int face, color, firstPlayer;
				cargo = ((C_Message)genericMessage).getCargo();
				face = BtUtils.byteArrayToInt(BtUtils.subArray(cargo, 0, 4));
				color = BtUtils.byteArrayToInt(BtUtils.subArray(cargo, 4, 4));
				firstPlayer = BtUtils.byteArrayToInt(BtUtils.subArray(cargo, 8, 4));
				
				if(m_IHandshakeListener!=null)
					m_IHandshakeListener.onReceivedGameInitMessage(PlayerFace.getByCode(face), 
							                                       PlayerColor.getByCode(color), 
							                                       PlayerColor.getByCode(firstPlayer));
				break;
				
			case OBJECT_CARGO_HOLDER:
				//nothing here..but it works
				break;
				
			case PLAY_AGAIN:
				int newFirstPlayer;
				cargo = ((C_Message)genericMessage).getCargo();
				newFirstPlayer = BtUtils.byteArrayToInt(BtUtils.subArray(cargo, 0, 4));
				if(m_OutputRemote!=null)
					m_OutputRemote.onRemoteDeviceSent_PlayAgain_Msg(PlayerColor.getByCode(newFirstPlayer));
				
				break;
				
			default:
				//nothing here
				break;
			}
		}
		
		public synchronized void onRemoteDeviceSent_KeepAlive_Msg(String btDeviceId) {
			Logger.d(TAG, "onRemoteDeviceSent_KeepAlive_Msg() MAC ["+btDeviceId+"]");
		}
	}
	
	//FROM IInputRemote ----------------------------------------------------
	//----------------------------------------------------------------------
	private IInputRemote m_IInputRemote;
	
	public IInputRemote getIInputRemote(){
		if(this.m_IInputRemote==null){
			m_IInputRemote = new IInputRemote() {

				@Override
				public void send_ChangeGameState_Msg(GameStateChangingAction newAction) {
					GenericMessage message = MessageFactory.new_ChangeGameState_Msg(newAction);
					broadCastMessage(message);
				}

				@Override
				public void send_Move_Msg(int x, int y) {
					GenericMessage message = MessageFactory.new_Move_Msg(x, y);
					broadCastMessage(message);
				}

				@Override
				public void send_GameInit_Msg(PlayerDetails playerDetails) {
					//server sends this message
					GenericMessage message = MessageFactory.new_GameInit_Msg(playerDetails.player2Face, playerDetails.player2Color, playerDetails.firstPlayer);
					broadCastMessage(message);
				}

				@Override
				public void send_PlayAgain_Msg(PlayerColor firstPlayer) {
					GenericMessage message = MessageFactory. new_PlayAgain_Msg(firstPlayer);
					broadCastMessage(message);
				}
			};
		}
		
		return this.m_IInputRemote;
	}
	
}
