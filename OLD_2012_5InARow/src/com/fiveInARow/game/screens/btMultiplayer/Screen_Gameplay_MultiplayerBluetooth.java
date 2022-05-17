package com.fiveInARow.game.screens.btMultiplayer;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;

import com.bt.platform.BTService;
import com.fiveInARow.R;
import com.fiveInARow.framework.AndroidFileSystemUtils;
import com.fiveInARow.framework.generics.Graphics;
import com.fiveInARow.framework.generics.IGame;
import com.fiveInARow.framework.generics.Input.TouchEvent;
import com.fiveInARow.framework.generics.Screen;
import com.fiveInARow.framework.generics.Sound;
import com.fiveInARow.game.Assets;
import com.fiveInARow.game.gameSave.GameSaveBundleManager;
import com.fiveInARow.game.gameWorld.World;
import com.fiveInARow.game.gameWorld.players.remotePlayer.RemotePlayer;
import com.fiveInARow.game.gameWorld.players.remotePlayer.listeners.IRemoteGameStateChanger;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.GameEnding;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.GameState;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.GameStateChangingAction;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.GameType;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerColor;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerFace;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerPopup;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.TimeStatus;
import com.fiveInARow.game.gameWorld.support.sessionSavedData.PlayerDetails;
import com.fiveInARow.game.gameWorld.support.sessionSavedData.SessionSaveData;
import com.fiveInARow.game.screens.Screen_MainMenu;
import com.fiveInARow.utils.GraphicsUtils;
import com.fiveInARow.utils.Logger;
import com.fiveInARow.utils.TouchAndCoordinatesUtils;
import com.fiveInARow.utils.UIUtils;

public class Screen_Gameplay_MultiplayerBluetooth extends Screen{
	private static final String TAG = "Screen_Gameplay_MultiplayerBluetooth";

	private SessionSaveData m_SessionSaveDataObj;
	private GameState m_GameState;
	private boolean m_showKillerMove = false;
	private World m_World;
	
	public Screen_Gameplay_MultiplayerBluetooth(IGame game, Activity activity, PlayerDetails playerDetails) {
		super(game, activity);
		
		Assets.stopAllAudio();
		this.m_World = new World(GameType.MULTI_PLAYER_BLUETOOTH, playerDetails);
		RemotePlayer remotePlayer = this.m_World.getRemotePlayer();
		remotePlayer.setRemoteGamePauser(getRemoteGamePause());
		
		this.m_SessionSaveDataObj = new SessionSaveData();
		this.m_SessionSaveDataObj.ssd_PlayerDetails = playerDetails;
		
		this.m_SessionSaveDataObj.ssd_GameType = GameType.MULTI_PLAYER_BLUETOOTH;		
		useDelayForFirstTouch();
		m_GameState = GameState.RUNNING; //we allready seen a "READY" screen in WAITING
		startGameWorld();
	}
	
	public Screen_Gameplay_MultiplayerBluetooth(IGame game, Activity activity, SessionSaveData sessionSaveDataObj) {
		super(game, activity);
		System.err.println("$$$ Screen_Gameplay_MultiplayerBluetooth() #2 ");
		
		Assets.stopAllAudio();
		this.m_World = new World(sessionSaveDataObj);
		RemotePlayer remotePlayer = this.m_World.getRemotePlayer();
		remotePlayer.setRemoteGamePauser(getRemoteGamePause());
		this.m_SessionSaveDataObj = sessionSaveDataObj;
		this.m_GameState = GameState.READY;
		this.useDelayForFirstTouch();
	}
	
	private IRemoteGameStateChanger getRemoteGamePause(){
		return new IRemoteGameStateChanger() {
			@Override
			public void onRemoteGameResume() {
				if(m_GameState==GameState.PAUSED) {
					m_GameState = GameState.RUNNING;
					resumeGameWorld();
				}
			}
			
			@Override
			public void onRemoteGamePause() {
				if(m_GameState==GameState.RUNNING) {
					m_GameState = GameState.PAUSED;
					pauseGameWorld();
				}
			}

			@Override
			public void onRemoteGameBegin() {
				if(m_GameState==GameState.READY) {
					useDelayForFirstTouch();
					m_GameState = GameState.RUNNING;
					startGameWorld();
				}
			}

			@Override
			public void onRemoteGameQuit() {
				m_GameState = GameState.PAUSED;
				killGameWorld();
				m_Game.setScreen(new Screen_MainMenu(m_Game, m_Activity));
				
				//stop service
				Intent intent = new Intent(BTService.ACTION_STRING_STOP);
				m_Activity.stopService(intent);
			}

			@Override
			public void onRemoteGamePlayAgain(PlayerColor firstPlayer) {
				// TODO:
				useDelayForFirstTouch();//dont let local user click now
				m_SessionSaveDataObj.ssd_PlayerDetails.firstPlayer = firstPlayer;
				m_Game.setScreen(new Screen_Gameplay_MultiplayerBluetooth(m_Game, m_Activity, m_SessionSaveDataObj));
			}
		};
	}

	private boolean hasGameJustEnded() {//"just"
		return (this.m_GameState!=GameState.GAMEOVER && m_World.getIWorldQuerrier().getGameEndingToPresent()!=null);
	}
	
	private void reactToGameHasJustEnded() {
		Logger.d(TAG, "reactToGameHasJustEnded()");
		
		this.m_GameState=GameState.GAMEOVER;
		
		//update scores
		GameEnding ending = m_World.getIWorldQuerrier().getGameEndingToPresent();
		if(ending==GameEnding.DRAW){
			this.m_SessionSaveDataObj.ssd_PlayerScores.ps_draws++;
		} else {
			PlayerColor currentColor = m_World.getIWorldQuerrier().getCurrentPlayerColor();//is player with "killer-move"
			if(currentColor==PlayerColor.BLACK)
				this.m_SessionSaveDataObj.ssd_PlayerScores.ps_BLACK_Victories++;
			else
				this.m_SessionSaveDataObj.ssd_PlayerScores.ps_WHITE_Victories++;
			
			if(ending == GameEnding.KILLER_MOVE) {
				resetTimerForKillerMove();
			}
		}
				
		if(GameSaveBundleManager.isSoundEnabled()==true){
			Sound victorySound;
			
			if(ending==GameEnding.DRAW)
				victorySound = Assets.getSoundForGameEnding(this.m_World.getIWorldQuerrier().getCurrentPlayer().getPlayerFace(), null);
			else {
				PlayerColor currentColor = m_World.getIWorldQuerrier().getCurrentPlayerColor();
				if(currentColor==m_World.getIWorldQuerrier().getColorOfPlayer(true))
					victorySound = Assets.getSoundForGameEnding(m_World.getIWorldQuerrier().getFaceOfPlayer(true), 
							                                    m_World.getIWorldQuerrier().getFaceOfPlayer(false));
				else
					victorySound = Assets.getSoundForGameEnding(m_World.getIWorldQuerrier().getFaceOfPlayer(false), 
							                                    m_World.getIWorldQuerrier().getFaceOfPlayer(true));
			}
			victorySound.play(1); 
		}
		
		System.err.println("$$$ reactToGameHasJustEnded(): "+m_SessionSaveDataObj.ssd_PlayerScores.toString());
	}

	@Override
	public void update(float deltaTime) {
		if(this.hasGameJustEnded()==true){//will run just 1 time!
			reactToGameHasJustEnded();
			return;
		}
		
		List<TouchEvent> touchEvents = m_Game.getInput().getTouchEvents();
		m_Game.getInput().getKeyEvents();
		
		if(touchEvents==null ||touchEvents.isEmpty())
			return;
		
		//in fct de starea jocului trimiti input/touch events la metoda corespunzatoare
		if (m_GameState == GameState.READY)
			updateREADY_TO_PLAY_AGAIN(touchEvents);
		else if (m_GameState == GameState.RUNNING)
			updateRUNNING(touchEvents);
		else if (m_GameState == GameState.PAUSED)
			updatePAUSED(touchEvents);
		else if (m_GameState == GameState.GAMEOVER)
			updateGAMEOVER(touchEvents);
	}

	@Override
	public void present(float deltaTime) {		
		Graphics g = m_Game.getGraphics();

		presentCOMMON(g);
		if(m_GameState == GameState.READY)
			presentREADY_TO_PLAY_AGAIN(g);
		else if(m_GameState == GameState.RUNNING)
			presentRUNNING(g);
		else if(m_GameState == GameState.PAUSED)
			presentPAUSED(g);
		else if(m_GameState == GameState.GAMEOVER)
			presentGAMEOVER(g);
	}
	
	private void presentREADY_TO_PLAY_AGAIN(Graphics g) {
		final int offsetHomer = +5;
		
		g.drawPixmap(Assets.img_hue, 0, 0);
		g.drawPixmap(Assets.img_dialog_game_ready_waiting, 40, 75);
		g.drawPixmap(Assets.img_dialog_game_ready_waiting_msg, 141, 125, 0, 0, 197, 87);
		
		//I. msg central
		g.drawPixmap(Assets.img_dialog_game_ready_waiting_msg, 141, 125, 0, 0, 197, 87);
		
		PlayerDetails playerDetails = m_SessionSaveDataObj.ssd_PlayerDetails;
		boolean amFirstPlayer = (playerDetails.firstPlayer == playerDetails.player1Color ?  true: false);
		
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
		Paint paint = UIUtils.getPaintForTextDrawing(20, Paint.Align.CENTER, R.color.white);
		int offSet = 0;
		if(amFirstPlayer==true){
			offSet = (playerDetails.player1Face==PlayerFace.HOMER? offsetHomer : 0);
			g.drawText(GameSaveBundleManager.getFormattedScore(true), 100 - offSet, 242, paint);
			
			offSet = (playerDetails.player2Face==PlayerFace.HOMER? offsetHomer : 0);
			g.drawText(playerDetails.getOpponentRecord().getFormattedRecord(), 378 + offSet, 242, paint);
		} else {
			offSet = (playerDetails.player1Face==PlayerFace.HOMER? offsetHomer : 0);
			g.drawText(GameSaveBundleManager.getFormattedScore(true), 378 + offSet, 242, paint);
			
			offSet = (playerDetails.player2Face==PlayerFace.HOMER? offsetHomer : 0);
			g.drawText(playerDetails.getOpponentRecord().getFormattedRecord(), 100 - offSet, 242, paint);
		}
	}
	
	private int[] m_Frames_for_showing = 
		{120, //not showing
		   8, //showing
		  12, //not showing
		   8, //showing
		  12, //not showing
		   8};//showing
	
	private int m_Frames_for_showing_index = 0, m_Frames_for_showing_index_index = 0;
	
	private void highlightPlayer(Graphics g, boolean userServerFrameArray, boolean leftPlayer){
		int[] frames = this.m_Frames_for_showing;

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
	
	private void updateREADY_TO_PLAY_AGAIN(List<TouchEvent> touchEvents) {
		Logger.d(TAG, "updateREADY_TO_PLAY_AGAIN()");
		
		if(touchEvents==null) return;
		
		int len = touchEvents.size();
		if (len > 0) {
			BTService.getINSTANCE().getIInputRemote().send_ChangeGameState_Msg(GameStateChangingAction.BEGIN);
			useDelayForFirstTouch();
			m_GameState = GameState.RUNNING;
			startGameWorld();
		}
		
		System.err.println("$$$ updateREADY_TO_PLAY_AGAIN(): "+m_SessionSaveDataObj.ssd_PlayerScores.toString());
	}

	@Override
	public void pause() {
		Assets.stopAllAudio();
		if(m_GameState==GameState.RUNNING){
			m_GameState = GameState.PAUSED;
			pauseGameWorld();
		}
	}

	@Override
	public void resume() {
		//TODO: ??
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean onBackPress_II() {
		if(m_GameState==GameState.RUNNING){
			this.pause();
			BTService.getINSTANCE().getIInputRemote().send_ChangeGameState_Msg(GameStateChangingAction.PAUSE);
			return false;
		} else if (m_GameState==GameState.GAMEOVER) {
			if(m_showKillerMove==false){
				persistHighScoresData();
				BTService.getINSTANCE().getIInputRemote().send_ChangeGameState_Msg(GameStateChangingAction.QUIT);
				return true;//world is already killed
			} else 
				return false;
		} else if(m_GameState == GameState.PAUSED){
			persistHighScoresData();
			killGameWorld();
			BTService.getINSTANCE().getIInputRemote().send_ChangeGameState_Msg(GameStateChangingAction.QUIT);
			return true;
		} else 
			return false;
	}
	
	private void updateGAMEOVER(List<TouchEvent> touchEvents) {
		if(m_showKillerMove==true)
			return;
		
		int len = touchEvents.size();
		for (int l = 0; l < len; l++) {
			TouchEvent event = touchEvents.get(l);
			if (event.type == TouchEvent.TOUCH_UP) {
				if (TouchAndCoordinatesUtils.inBoundOfSquareArea(event, TouchAndCoordinatesUtils.squareArea_gameOver)){
					if (event.y < 177) {
						//PLAY AGAIN
						PlayerDetails playerDetails = m_SessionSaveDataObj.ssd_PlayerDetails;
						playerDetails.newOrder();
						
						//TODO:
						System.err.println("$$$ updateGAMEOVER() ");
						BTService.getINSTANCE().getIInputRemote().send_PlayAgain_Msg(playerDetails.firstPlayer);
						m_Game.setScreen(new Screen_Gameplay_MultiplayerBluetooth(m_Game, m_Activity, m_SessionSaveDataObj));
						
						if (GameSaveBundleManager.isSoundEnabled())
							Assets.sound_click.play(1);
						return;
			        } else if (event.y > 183){
			        	//QUIT
			        	BTService.getINSTANCE().getIInputRemote().send_ChangeGameState_Msg(GameStateChangingAction.QUIT);
			        	
			        	updateHighScoresTable();
			        	persistHighScoresData();
			        	m_Game.setScreen(new Screen_MainMenu(m_Game, m_Activity));
			        	
						if (GameSaveBundleManager.isSoundEnabled())
							Assets.sound_click.play(1);
			        	return;
			        }
				}
			}
		}
	}
	
	private void updateHighScoresTable(){
		int localPlayerScore = this.m_SessionSaveDataObj.ssd_PlayerScores.getPlayerScore(m_World.getIWorldQuerrier().getColorOfPlayer(true));
		int computerScore = this.m_SessionSaveDataObj.ssd_PlayerScores.getPlayerScore(m_World.getIWorldQuerrier().getColorOfPlayer(false));
		
		if(this.m_SessionSaveDataObj.ssd_GameType==GameType.MULTI_PLAYER_BLUETOOTH){
			GameSaveBundleManager.addScore(localPlayerScore-computerScore, "$DEVICE$");
		}
	}
	
	private void persistHighScoresData(){
		GameSaveBundleManager.save(AndroidFileSystemUtils.getINSTANCE());
	}

	private void updatePAUSED(List<TouchEvent> touchEvents) {
		int len = touchEvents.size();
		for (int l = 0; l < len; l++) {
			TouchEvent event = touchEvents.get(l);
			if (event.type == TouchEvent.TOUCH_UP) {
				if (TouchAndCoordinatesUtils.inBoundOfSquareArea(event, TouchAndCoordinatesUtils.squareArea_gamePaused)){
					if (event.y < 176){
						//RESUME
						BTService.getINSTANCE().getIInputRemote().send_ChangeGameState_Msg(GameStateChangingAction.RESUME);
						useDelayForFirstTouch();
						
						m_GameState = GameState.RUNNING;
						m_World.resumeGameWorld();
						
						if (GameSaveBundleManager.isSoundEnabled())
							Assets.sound_click.play(1);
						return;
			        } if (event.y > 184){
			        	//QUIT
			        	BTService.getINSTANCE().getIInputRemote().send_ChangeGameState_Msg(GameStateChangingAction.QUIT);
			        	updateHighScoresTable();
			        	persistHighScoresData();
			        	m_World.killGameWorld();
			        	m_Game.setScreen(new Screen_MainMenu(m_Game, m_Activity));
			        	
						if (GameSaveBundleManager.isSoundEnabled())
							Assets.sound_click.play(1);
			        	return;
			        }
				}
			}
		}
	}

	private void updateRUNNING(List<TouchEvent> touchEvents) {	
		if(skipSinceStillInDelayForFirstTouch()==true)
			return;
		
		int len = touchEvents.size();
		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);
			if (event.type == TouchEvent.TOUCH_UP) {
				m_World.interpretTouchEvent(event);
				return;
			}
		}
	}
	
	//required for KILLER_MOVE highlight (KM)
	private int frame_count_for_showing_KM = 0, frame_count_for_not_showing_KM = 0, frames_per_flash_KM = 7;
	private Timer m_Timer;
	private static final int SHOW_KILLER_MOVE_FOR = 3000; //+ 2 sec
	
	private void resetTimerForKillerMove(){
		// cancel current timer
		if (this.m_Timer != null) {
			this.m_Timer.cancel();
			this.m_Timer = null;
    	}
		
		this.m_showKillerMove = true;
		
		// setup again
		this.m_Timer = new Timer();
		this.m_Timer.schedule(new TimerTask(){
			private static final String TAG = "TimerTask";
			
			@Override
			public void run() {
				Logger.d(TAG, "run()");
				
				m_showKillerMove = false;
			}}, SHOW_KILLER_MOVE_FOR);
		//scheduleAtFixedRate(new TimerTask(){ public void run() {counter++;}}, 0, 1000L);
	}
	
	private void highlightKillerMove(Graphics g){ //true == is displaying
		if(frame_count_for_showing_KM<frames_per_flash_KM){
			ContentValues cv = m_World.getIWorldQuerrier().getLastMoveToPresent();
			g.drawPixmap(GraphicsUtils.getKillerMoveOverlay(cv.getAsInteger("x"), cv.getAsInteger("y")), 0, 0);
			frame_count_for_showing_KM++;
		} else if(frame_count_for_not_showing_KM<frames_per_flash_KM){
			frame_count_for_not_showing_KM++;
		}
		
		if (frame_count_for_showing_KM == frames_per_flash_KM
				&& frame_count_for_not_showing_KM == frames_per_flash_KM) {
			frame_count_for_showing_KM = 0;
			frame_count_for_not_showing_KM = 0;
		}
	}
	
	private void presentGAMEOVER(Graphics g) {
		if(m_World.getIWorldQuerrier().getGameEndingToPresent()==GameEnding.KILLER_MOVE 
				&& m_showKillerMove == true){
			highlightKillerMove(g);
		}
		
		if(m_showKillerMove==false){
			g.drawPixmap(Assets.img_hue, 0, 0);
			g.drawPixmap(Assets.img_dialog_game_over, 55, 95);
			
			//the faces
			if(m_World.getIWorldQuerrier().getGameEndingToPresent()==GameEnding.KILLER_MOVE){
				PlayerFace currentPlayerFace = m_World.getIWorldQuerrier().getCurrentPlayer().getPlayerFace();
				if(currentPlayerFace==PlayerFace.BART){
					g.drawPixmap(Assets.img_characters_faces_victory, 305, 124, 0, 0, 106, 100);
				}else if(currentPlayerFace==PlayerFace.HOMER){
					g.drawPixmap(Assets.img_characters_faces_victory, 305, 124, 0, 100, 106, 100);
				}else {
					g.drawPixmap(Assets.img_characters_faces_victory, 305, 124, 0, 200, 106, 100);
				}
			} else {
				//TODO:
			}
			
			//the text
			Paint paint = new Paint();
			paint.setTextSize(18);
			paint.setColor(Color.rgb(44, 127, 193));
			paint.setTypeface(Assets.theme_font);
			if(m_World.getIWorldQuerrier().getGameEndingToPresent()==GameEnding.KILLER_MOVE){
				g.drawText("Victory &", 325, 112, paint);
				g.drawText("+5 points for", 315, 125, paint);
			} else {
				g.drawText("Draw game &", 310, 112, paint);
				g.drawText("+1 point for each", 300, 125, paint);
			}
		}
	}

	private void presentPAUSED(Graphics g) {
		g.drawPixmap(Assets.img_hue, 0, 0);
		g.drawPixmap(Assets.img_dialog_game_paused, 70, 95);
	}

	private void presentRUNNING(Graphics g) {
		highlightLastMove(g);
	}

	//required for LAST MOVE highlight (LM)
	private int frame_count_for_showing_LM = 0, frame_count_for_not_showing_LM = 0, frames_per_flash_showing_LM = 12, frames_per_flash_not_showing_LM = 20;
	
	private void highlightLastMove(Graphics g) {
		ContentValues cv = m_World.getIWorldQuerrier().getLastMoveToPresent();
		int x = cv.getAsInteger("x");
		int y = cv.getAsInteger("y");
		
		//who moved?
		if(frame_count_for_showing_LM<frames_per_flash_showing_LM){
			PlayerColor colorToHighlight = m_World.getIWorldQuerrier().getCurrentPlayerColor().flip();
			if(colorToHighlight==PlayerColor.BLACK)
				g.drawPixmap(Assets.img_the_pieces_for_last_move, 5+x*30, 5+y*31, 0, 0, 29, 30);
			else
				g.drawPixmap(Assets.img_the_pieces_for_last_move, 5+x*30, 5+y*31, 0, 30, 29, 30);
				
			frame_count_for_showing_LM++;
		} else if(frame_count_for_not_showing_LM<frames_per_flash_not_showing_LM){
			frame_count_for_not_showing_LM++;
		}
		
		if(frame_count_for_showing_LM== frames_per_flash_showing_LM && 
				frame_count_for_not_showing_LM == frames_per_flash_not_showing_LM){
			frame_count_for_showing_LM = 0;
			frame_count_for_not_showing_LM = 0;
		}
	}

	private void presentCOMMON(Graphics g) {
		g.drawPixmap(Assets.img_main_game_bg, 0, 0);
		g.drawPixmap(Assets.img_game_overlay_level1, 0, 0);
		
		//pieces---------------------
		int[][]pieces = m_World.getIWorldQuerrier().getPiecesToPresent();

		int len = pieces[0].length;
		for (int i = 0; i < len; i++)
			for (int j = 0; j < len; j++) {
				int code = pieces[i][j];
				PlayerColor playerColor = PlayerColor.getByCode(code);
				if (playerColor == PlayerColor.BLACK)
					g.drawPixmap(Assets.img_the_pieces, 5+i*30, 5+j*31, 0, 0, 29, 30);
				else if (playerColor == PlayerColor.WHITE)
					g.drawPixmap(Assets.img_the_pieces, 5+i*30, 5+j*31, 0, 30, 29, 30);
			}
		
		//popups
		PlayerPopup popup = m_World.getIWorldQuerrier().getPlayerPopupToPresent();
		if(popup==PlayerPopup.NOT_YOUR_TURN){
			g.drawPixmap(Assets.img_warning_popups, 375, 160, 0, 0, 105, 60);
		}else if(popup == PlayerPopup.YOU_CANT_MOVE_THERE){
			g.drawPixmap(Assets.img_warning_popups, 375, 160, 0, 60, 105, 60);
		}
		
		//player faces
		PlayerFace currentPlayerFace = m_World.getIWorldQuerrier().getCurrentPlayer().getPlayerFace();
		if(currentPlayerFace==PlayerFace.BART){
			g.drawPixmap(Assets.img_characters_faces, 380, 200, 0, 0, 90, 120);
		}else if(currentPlayerFace==PlayerFace.HOMER){
			g.drawPixmap(Assets.img_characters_faces, 380, 200, 90, 0, 90, 120);
		}else {
			g.drawPixmap(Assets.img_characters_faces, 380, 200, 180, 0, 90, 120);
		}
		
		//clepsidra
		TimeStatus status = m_World.getIWorldQuerrier().getTimeStatusToPresent();
		if(status==null || status==TimeStatus._1_DIN_6){
			g.drawPixmap(Assets.img_hourglasses, 320, 200, 0, 0, 32, 60);
		}else if(status==TimeStatus._2_DIN_6){
			g.drawPixmap(Assets.img_hourglasses, 320, 200, 32, 0, 32, 60);
		}else if(status==TimeStatus._3_DIN_6){
			g.drawPixmap(Assets.img_hourglasses, 320, 200, 64, 0, 32, 60);
		}else if(status==TimeStatus._4_DIN_6){
			g.drawPixmap(Assets.img_hourglasses, 320, 200, 96, 0, 32, 60);
		}else {
			g.drawPixmap(Assets.img_hourglasses, 320, 200, 128, 0, 32, 60);
		}
		
		//score
		Paint paint = new Paint();
		paint.setTextSize(22);
		paint.setColor(Color.rgb(44, 127, 193));
		paint.setTypeface(Assets.theme_font);
		
		String BLACK_PlayerScoreAsString = String.valueOf(this.m_SessionSaveDataObj.ssd_PlayerScores.getPlayerScore(PlayerColor.BLACK));
		String WHITE_PlayerScoreAsString = String.valueOf(this.m_SessionSaveDataObj.ssd_PlayerScores.getPlayerScore(PlayerColor.WHITE));
		g.drawText(BLACK_PlayerScoreAsString, 420, 25, paint);
		g.drawText(WHITE_PlayerScoreAsString, 420, 57, paint);
		
		//player color
		PlayerColor currentColor = m_World.getIWorldQuerrier().getCurrentPlayerColor();
		if(currentColor==PlayerColor.BLACK)
			g.drawPixmap(Assets.img_the_pieces, 353, 230, 0, 0, 29, 30);
		else
			g.drawPixmap(Assets.img_the_pieces, 353, 230, 0, 30, 29, 30);
		
		paint.setTextSize(16);
		if(currentPlayerFace==PlayerFace.BART)
			g.drawText("Bart", 385, 205, paint);
		else if (currentPlayerFace==PlayerFace.HOMER)
			g.drawText("Homer", 385, 205, paint);
		else
			g.drawText("Prof. Frink", 385, 205, paint);
	}

	private void killGameWorld(){
		m_World.killGameWorld();
	}
	
	private void pauseGameWorld(){
		m_World.pauseGameWorld();
	}
	
	private void resumeGameWorld(){
		m_World.resumeGameWorld();
	}
	
	private void startGameWorld(){
		m_World.startGameWorld();
	}

}
