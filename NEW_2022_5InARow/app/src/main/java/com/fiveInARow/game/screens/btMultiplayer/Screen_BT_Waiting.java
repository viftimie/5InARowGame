package com.fiveInARow.game.screens.btMultiplayer;

import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Paint;
import android.widget.Toast;

import com.bt.BTEnums.FailedHandshakeReason;
import com.bt.platform.BTService;
import com.bt.platform.listeners.IHandshakeAndGameInitListener;
import com.bt.settings.AppSettings;
import com.bt.utils.BtUtils;
import com.fiveInARow.R;
import com.fiveInARow.framework.generics.Graphics;
import com.fiveInARow.framework.generics.IGame;
import com.fiveInARow.framework.generics.Input.TouchEvent;
import com.fiveInARow.framework.generics.Screen;
import com.fiveInARow.game.Assets;
import com.fiveInARow.game.gameSave.GameSaveBundleManager;
import com.fiveInARow.game.gameSave.scoring.Record;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.GameStateChangingAction;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerColor;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerFace;
import com.fiveInARow.game.gameWorld.support.sessionSavedData.PlayerDetails;
import com.fiveInARow.game.screens.Screen_MainMenu;
import com.fiveInARow.utils.Logger;
import com.fiveInARow.utils.UIUtils;

public class Screen_BT_Waiting extends Screen{
	private static final String TAG = "Screen_BT_Waiting";
	
	private static final int REQUEST_CODE_ENABLE_BT_DISCOVERABILITY = 1821;
	
	private boolean m_AmServer;
	private PlayerDetails m_PlayerDetails = null;
	private Record m_OpponentsRecord;
	private boolean m_RemotePlayerEntered;
	
	//last two params are NULL for server
	public Screen_BT_Waiting(IGame game, Activity activity, PlayerDetails playerDetails, Record opponentsRecord) {
		super(game, activity);
		
		this.m_PlayerDetails = playerDetails;
		if(playerDetails!=null) {
			this.useDelayForFirstTouch();
			this.m_AmServer = false;
			//Client stores RECORD HERE
			this.m_PlayerDetails.setOpponentRecord(opponentsRecord);
		} else 
			this.m_AmServer = true;
		this.m_OpponentsRecord = opponentsRecord;
		
		this.init();
	}

	private void init() {
		
		if(this.m_AmServer==true){
			this.m_Activity.runOnUiThread(new Runnable() {
				public void run() {
					BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

					if (btAdapter == null) {
						Toast.makeText(m_Activity, "Device doesn't seem to have a Bluetooth adapter! ", Toast.LENGTH_LONG);
						return;
					}

					if (btAdapter.isEnabled()==false) {
						enableBluetoothDiscoverabilityForServer();
						return;
					}
							
					startBTServiceForServer();
				}
			});
		} else {
			//needed just to receive the ClickToBegin
			BTService.setHandshakeListener(this.getHandShakeListenerForClient());
		}
	}

	protected void startBTServiceForServer() {
		BTService.setHandshakeListener(getHandshakeListenerForServer());
		Intent intent = new Intent(BTService.ACTION_STRING_START_AS_SERVER);
		this.m_Activity.startService(intent);
	}
	
	private void stopBTServiceForServer(){
		Intent intent = new Intent(BTService.ACTION_STRING_STOP);
		this.m_Activity.stopService(intent);
	}
	
	//used only by server
	private IHandshakeAndGameInitListener getHandshakeListenerForServer() { 
		return new IHandshakeAndGameInitListener() {

			@Override
			public void onCorrectHandshake() {
				m_RemotePlayerEntered = true;
				PlayerDetails playerDetails = getPlayerDetails();
				Logger.d(TAG, "SERVER onCorrectHandshake(): "+playerDetails.toString()); //TODO: remove
				BTService.getINSTANCE().getIInputRemote().send_GameInit_Msg(playerDetails);
				useDelayForFirstTouch();
				
//				m_Activity.runOnUiThread(new Runnable() {
//					public void run() {
//						Toast.makeText(m_Activity, "Succesfully connected to remote device!", Toast.LENGTH_LONG).show();
//					}
//				});
			}
			
			@Override
			public void onFailedHandshake(final FailedHandshakeReason reason) {
				if(reason==FailedHandshakeReason.WRONG_CONNECT_KEY_ON_SERVER){
					startBTServiceForServer();
				} else {
					stopBTServiceForServer();
				}
				
				m_Activity.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(m_Activity, reason.getReason(), Toast.LENGTH_LONG).show();
					}
				});
			}

			@Override
			public void onReceivedRecordMessage(int gamesWon, int gamesDraw, int gamesLost) {
				m_OpponentsRecord = new Record(gamesWon, gamesDraw, gamesLost);
				//Server stores RECORD HERE
				m_PlayerDetails.setOpponentRecord(m_OpponentsRecord);
			}

			@Override
			public void onClickToBeginGame() {
				m_Game.setScreen(new Screen_Gameplay_MultiplayerBluetooth(m_Game, m_Activity, m_PlayerDetails));
			}

			@Override
			public void onReceivedGameInitMessage(PlayerFace myFace, PlayerColor myColor, PlayerColor firstPlayer) {
				// nothing here since it is server
			}
		};
	}
	
	//used by client only to recieve the onClick to start
	public IHandshakeAndGameInitListener getHandShakeListenerForClient(){
		return new IHandshakeAndGameInitListener() {
			
			@Override
			public void onReceivedRecordMessage(int gamesWon, int gamesDraw, int gamesLost) {
				// not used here
			}
			
			@Override
			public void onFailedHandshake(FailedHandshakeReason reason) {
				// not used here
			}
			
			@Override
			public void onCorrectHandshake() {
				// not used here
			}
			
			@Override
			public void onClickToBeginGame() {
				m_Game.setScreen(new Screen_Gameplay_MultiplayerBluetooth(m_Game, m_Activity, m_PlayerDetails));
			}

			@Override
			public void onReceivedGameInitMessage(PlayerFace myFace, PlayerColor myColor, PlayerColor firstPlayer) {
				// not used here
			}
		};
	}
	
	private void enableBluetoothDiscoverabilityForServer() {
		Logger.d(TAG, "enableBluetoothDiscoverabilityForServer()");
		
		// Note: If Bluetooth has not been enabled on the device, then enabling device discoverability will automatically enable Bluetooth.
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		this.m_Activity.startActivityForResult(discoverableIntent, REQUEST_CODE_ENABLE_BT_DISCOVERABILITY);
	}

	@Override
	public void update(float deltaTime) {
		if(this.m_RemotePlayerEntered==false && this.m_AmServer == true){
			this.m_Game.getInput().getTouchEvents();
			this.m_Game.getInput().getKeyEvents();
			return;
		}
		
		List<TouchEvent> touchEvents = this.m_Game.getInput().getTouchEvents();
		this.m_Game.getInput().getKeyEvents();
		
		if(touchEvents==null) return;
		
		int len = touchEvents.size();
		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);
			if (event.type == TouchEvent.TOUCH_UP) {
				m_Game.setScreen(new Screen_Gameplay_MultiplayerBluetooth(m_Game, m_Activity, m_PlayerDetails));
				//OR CLICK TO BEGIN
				BTService.getINSTANCE().getIInputRemote().send_ChangeGameState_Msg(GameStateChangingAction.BEGIN);
				return;
			}
		}
	}

	@Override
	public void present(float deltaTime) {
		final int offsetHomer = +5;
		PlayerDetails playerDetails = getPlayerDetails();
		
		Graphics g = this.m_Game.getGraphics();
		g.drawPixmap(Assets.img_main_game_bg, 0, 0);
		g.drawPixmap(Assets.img_game_overlay_level1, 0, 0);
		
		//add first player face in bg, playerDetails should not be NULL at this time!
		PlayerFace firstPlayerFace = (playerDetails.firstPlayer == playerDetails.player1Color ?  playerDetails.player1Face : playerDetails.player2Face);
		boolean amFirstPlayer = (playerDetails.firstPlayer == playerDetails.player1Color ?  true: false);
		
		//the background face		
		if(this.m_AmServer==true && this.m_RemotePlayerEntered==false && amFirstPlayer == false){
			if(firstPlayerFace==PlayerFace.BART){
				g.drawPixmap(Assets.img_characters_faces_game_waiting_unknown, 380, 200, 0, 0, 90, 120);
			}else if(firstPlayerFace==PlayerFace.HOMER){
				g.drawPixmap(Assets.img_characters_faces_game_waiting_unknown, 380, 200, 90, 0, 90, 120);
			}
		} else {
			if(firstPlayerFace==PlayerFace.BART){
				g.drawPixmap(Assets.img_characters_faces, 380, 200, 0, 0, 90, 120);
			}else if(firstPlayerFace==PlayerFace.HOMER){
				g.drawPixmap(Assets.img_characters_faces, 380, 200, 90, 0, 90, 120);
			}
		}
		
		if(playerDetails.firstPlayer==PlayerColor.BLACK)
			g.drawPixmap(Assets.img_the_pieces, 353, 230, 0, 0, 29, 30);
		else
			g.drawPixmap(Assets.img_the_pieces, 353, 230, 0, 30, 29, 30);
			
		g.drawPixmap(Assets.img_hue, 0, 0);
		g.drawPixmap(Assets.img_dialog_game_ready_waiting, 40, 75);
		
		
		
		Paint paint;
		if(this.m_AmServer == true) {
			//SERVER======================================================================
			//I. msg central & connect key
			if(this.m_RemotePlayerEntered==false) {
				g.drawPixmap(Assets.img_dialog_game_ready_waiting_msg, 141, 125, 0, 87, 197, 87);
				paint = UIUtils.getPaintForTextDrawing(24, Paint.Align.LEFT, R.color.blue_universal);
				g.drawText(String.valueOf(BtUtils.byteArrayToInt(AppSettings.MY_APP_CONNECT_KEY)), 225, 205, paint);
			} else
				g.drawPixmap(Assets.img_dialog_game_ready_waiting_msg, 141, 125, 0, 0, 197, 87);
			
			//II. fetele
			
				//my face
			if(amFirstPlayer==true){
				if(playerDetails.player1Face==PlayerFace.BART)
					g.drawPixmap(Assets.img_characters_game_ready, 45, 75 ,0, 0, 114, 171);
				else if (playerDetails.player1Face==PlayerFace.HOMER)
					g.drawPixmap(Assets.img_characters_game_ready, 45, 75 ,114, 0, 114, 171);
			} else {
				if (playerDetails.player1Face == PlayerFace.BART)
					g.drawPixmap(Assets.img_characters_game_ready_flipped, 320, 75, 228, 0, 114, 171);
				else
					g.drawPixmap(Assets.img_characters_game_ready_flipped, 320, 75, 114, 0, 114, 171);
			}
			
				//opponent face
			if(amFirstPlayer==true){
				if(this.m_RemotePlayerEntered==false){
					if (playerDetails.player2Face == PlayerFace.BART)
						g.drawPixmap(Assets.img_characters_game_waiting_unknown_flipped, 320, 75, 228, 0, 114, 171);
					else
						g.drawPixmap(Assets.img_characters_game_waiting_unknown_flipped, 320, 75, 114, 0, 114, 171);
				} else {
					if (playerDetails.player2Face == PlayerFace.BART)
						g.drawPixmap(Assets.img_characters_game_ready_flipped, 320, 75, 228, 0, 114, 171);
					else
						g.drawPixmap(Assets.img_characters_game_ready_flipped, 320, 75, 114, 0, 114, 171);
				}
			} else {
				if(this.m_RemotePlayerEntered==false){
					if (playerDetails.player2Face == PlayerFace.BART)
						g.drawPixmap(Assets.img_characters_game_waiting_unknown, 45, 75, 0, 0, 114, 171);
					else
						g.drawPixmap(Assets.img_characters_game_waiting_unknown, 45, 75, 114, 0, 114, 171);
				} else {
					if (playerDetails.player2Face == PlayerFace.BART)
						g.drawPixmap(Assets.img_characters_game_ready, 45, 75, 0, 0, 114, 171);
					else
						g.drawPixmap(Assets.img_characters_game_ready, 45, 75, 114, 0, 114, 171);
				}
			}
			
			//III. Piesele
			if(amFirstPlayer==true){
				if(playerDetails.player1Color==PlayerColor.BLACK) {
					g.drawPixmap(Assets.img_the_pieces, 150, 210, 0, 0, 29, 30);
					g.drawPixmap(Assets.img_the_pieces, 300, 210, 0, 30, 29, 30);
				} else {
					g.drawPixmap(Assets.img_the_pieces, 150, 210, 0, 30, 29, 30);
					g.drawPixmap(Assets.img_the_pieces, 300, 210, 0, 0, 29, 30);
				}
			} else {
				if(playerDetails.player1Color==PlayerColor.BLACK) {
					g.drawPixmap(Assets.img_the_pieces, 300, 210, 0, 0, 29, 30);
					g.drawPixmap(Assets.img_the_pieces, 150, 210, 0, 30, 29, 30);
				} else {
					g.drawPixmap(Assets.img_the_pieces, 300, 210, 0, 30, 29, 30);
					g.drawPixmap(Assets.img_the_pieces, 150, 210, 0, 0, 29, 30);
				}
			}
			
			this.highlightPlayer(g, true, amFirstPlayer);
			
			//IV. Record
			paint = UIUtils.getPaintForTextDrawing(20, Paint.Align.CENTER, R.color.white);
			int offSet = 0;
			if(amFirstPlayer==true){
				offSet = (this.m_PlayerDetails.player1Face==PlayerFace.HOMER? offsetHomer : 0);
				g.drawText(GameSaveBundleManager.getFormattedScore(true), 100 - offSet, 242, paint);
				
				offSet = (this.m_PlayerDetails.player2Face==PlayerFace.HOMER? offsetHomer : 0);
				if(this.m_OpponentsRecord==null)
					g.drawText(Record.getFormattedUnknownScore(), 378 + offSet, 242, paint);
				else
					g.drawText(this.m_OpponentsRecord.getFormattedRecord(), 378 + offSet, 242, paint);
			} else {
				offSet = (this.m_PlayerDetails.player1Face==PlayerFace.HOMER? offsetHomer : 0);
				g.drawText(GameSaveBundleManager.getFormattedScore(true), 378 + offSet, 242, paint);
				
				offSet = (this.m_PlayerDetails.player2Face==PlayerFace.HOMER? offsetHomer : 0);
				if(this.m_OpponentsRecord==null)
					g.drawText(Record.getFormattedUnknownScore(), 100 - offSet, 242, paint);
				else
					g.drawText(this.m_OpponentsRecord.getFormattedRecord(), 100 - offSet, 242, paint);
			}
			
		} else {
			//CLIENT======================================================================
			
			//I. msg central
			g.drawPixmap(Assets.img_dialog_game_ready_waiting_msg, 141, 125, 0, 0, 197, 87);
			
			//II. fetele
			
				//my face
			if(amFirstPlayer==true){
				if(playerDetails.player1Face==PlayerFace.BART)
					g.drawPixmap(Assets.img_characters_game_ready, 45, 75 ,0, 0, 114, 171);
				else if (playerDetails.player1Face==PlayerFace.HOMER)
					g.drawPixmap(Assets.img_characters_game_ready, 45, 75 ,114, 0, 114, 171);
			} else {
				if (playerDetails.player1Face == PlayerFace.BART)
					g.drawPixmap(Assets.img_characters_game_ready_flipped, 320, 75, 228, 0, 114, 171);
				else
					g.drawPixmap(Assets.img_characters_game_ready_flipped, 320, 75, 114, 0, 114, 171);
			}
			
				//opponent face
			if(amFirstPlayer==true){
				if (playerDetails.player2Face == PlayerFace.BART)
					g.drawPixmap(Assets.img_characters_game_ready_flipped, 320, 75, 228, 0, 114, 171);
				else
					g.drawPixmap(Assets.img_characters_game_ready_flipped, 320, 75, 114, 0, 114, 171);
			} else {
				if (playerDetails.player2Face == PlayerFace.BART)
					g.drawPixmap(Assets.img_characters_game_ready, 45, 75, 0, 0, 114, 171);
				else
					g.drawPixmap(Assets.img_characters_game_ready, 45, 75, 114, 0, 114, 171);
			}	
			
			//III. Piesele
			if(amFirstPlayer==true){
				if(playerDetails.player1Color==PlayerColor.BLACK) {
					g.drawPixmap(Assets.img_the_pieces, 150, 210, 0, 0, 29, 30);
					g.drawPixmap(Assets.img_the_pieces, 300, 210, 0, 30, 29, 30);
				} else {
					g.drawPixmap(Assets.img_the_pieces, 150, 210, 0, 30, 29, 30);
					g.drawPixmap(Assets.img_the_pieces, 300, 210, 0, 0, 29, 30);
				}
			} else {
				if(playerDetails.player1Color==PlayerColor.BLACK) {
					g.drawPixmap(Assets.img_the_pieces, 300, 210, 0, 0, 29, 30);
					g.drawPixmap(Assets.img_the_pieces, 150, 210, 0, 30, 29, 30);
				} else {
					g.drawPixmap(Assets.img_the_pieces, 300, 210, 0, 30, 29, 30);
					g.drawPixmap(Assets.img_the_pieces, 150, 210, 0, 0, 29, 30);
				}
			}
			
			this.highlightPlayer(g, false, amFirstPlayer);
			
			//IV. Record
			paint = UIUtils.getPaintForTextDrawing(20, Paint.Align.CENTER, R.color.white);
			int offSet = 0;
			if(amFirstPlayer==true){
				offSet = (this.m_PlayerDetails.player1Face==PlayerFace.HOMER? offsetHomer : 0);
				g.drawText(GameSaveBundleManager.getFormattedScore(true), 100 - offSet, 242, paint);
				
				offSet = (this.m_PlayerDetails.player2Face==PlayerFace.HOMER? offsetHomer : 0);
				if(this.m_OpponentsRecord==null)
					g.drawText(Record.getFormattedUnknownScore(), 378 + offSet, 242, paint);
				else
					g.drawText(this.m_OpponentsRecord.getFormattedRecord(), 378 + offSet, 242, paint);
			} else {
				offSet = (this.m_PlayerDetails.player1Face==PlayerFace.HOMER? offsetHomer : 0);
				g.drawText(GameSaveBundleManager.getFormattedScore(true), 378 + offSet, 242, paint);
				
				offSet = (this.m_PlayerDetails.player2Face==PlayerFace.HOMER? offsetHomer : 0);
				if(this.m_OpponentsRecord==null)
					g.drawText(Record.getFormattedUnknownScore(), 100 - offSet, 242, paint);
				else
					g.drawText(this.m_OpponentsRecord.getFormattedRecord(), 100 - offSet, 242, paint);
			}
		}
	}
	                                 
	private int[] m_Frames_for_showing_FOR_SERVER = 
		{120, //not showing
		   8, //showing
		  12, //not showing
		   8, //showing
		  12, //not showing
		   8};//showing
	
	private int[] m_Frames_for_showing_FOR_CLIENT = 
		{ 12, //not showing
		   8, //showing
		  12, //not showing
		   8, //showing
		 120, //not showing
		   8};//showing

	private int m_Frames_for_showing_index = 0, m_Frames_for_showing_index_index = 0;
	
	private void highlightPlayer(Graphics g, boolean userServerFrameArray, boolean leftPlayer){
		int[] frames;
		if(userServerFrameArray==true)
			frames = this.m_Frames_for_showing_FOR_SERVER;
		else
			frames = this.m_Frames_for_showing_FOR_CLIENT;
		
		if(this.m_Frames_for_showing_index_index < frames[this.m_Frames_for_showing_index]){
			if(this.m_Frames_for_showing_index%2 ==1){
				if(leftPlayer==true)
					g.drawPixmap(Assets.img_player_highlight, 40, 75);
				else
					g.drawPixmap(Assets.img_player_highlight, 305, 75);
			}
			this.m_Frames_for_showing_index_index ++;
		} else {
			if(this.m_Frames_for_showing_index == frames.length-1)
				this.m_Frames_for_showing_index = 0;
			else
				this.m_Frames_for_showing_index ++;
			this.m_Frames_for_showing_index_index = 0;
		}
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
	}
	
	@Override
	public boolean onBackPress_II() {
		this.m_Game.setScreen(new Screen_MainMenu(this.m_Game, this.m_Activity));
		return false;
	}
	
	@Override
	public void onActivityResult_II(int requestCode, int resultCode, Intent data) { //used only by server
		super.onActivityResult_II(requestCode, resultCode, data);
		
		Logger.d(TAG, "onActivityResult()");

		if (resultCode != Activity.RESULT_CANCELED) {
			switch (requestCode) {
			case REQUEST_CODE_ENABLE_BT_DISCOVERABILITY:
				startBTServiceForServer();
				break;

			default:
				break;
			}
		}
	}
	
	private PlayerDetails getPlayerDetails(){
		if(this.m_PlayerDetails==null){ 
			//for server
			this.m_PlayerDetails = PlayerDetails.getPlayerDetailsForMultiplayer();
		}
		
		return this.m_PlayerDetails;
	}

}
