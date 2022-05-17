package com.fiveInARow.game.gameWorld;

import java.util.Random;

import android.content.ContentValues;

import com.bt.platform.BTService;
import com.fiveInARow.framework.generics.Input.TouchEvent;
import com.fiveInARow.framework.generics.Sound;
import com.fiveInARow.game.Assets;
import com.fiveInARow.game.gameSave.GameSaveBundleManager;
import com.fiveInARow.game.gameWorld.interfaces.IPlayerMoveListner;
import com.fiveInARow.game.gameWorld.interfaces.ITimeUpdateListener;
import com.fiveInARow.game.gameWorld.interfaces.IWorldQuerrier;
import com.fiveInARow.game.gameWorld.players.Clepsidra;
import com.fiveInARow.game.gameWorld.players.ComputerPlayer;
import com.fiveInARow.game.gameWorld.players.LocalPlayer;
import com.fiveInARow.game.gameWorld.players.generic.GenericPlayer;
import com.fiveInARow.game.gameWorld.players.remotePlayer.RemotePlayer;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.GameEnding;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.GameType;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerColor;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerFace;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerPopup;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.TimeStatus;
import com.fiveInARow.game.gameWorld.support.sessionSavedData.PlayerDetails;
import com.fiveInARow.game.gameWorld.support.sessionSavedData.SessionSaveData;

public class World implements IPlayerMoveListner, ITimeUpdateListener{

	private GameType m_GameType;
	private Random m_Rnd = new Random();
	private GenericPlayer m_Player1, m_Player2;
	private PlayerColor m_CurrentPlayerColor; //OR "in turn to move"
	private Clepsidra m_Clepsidra;
	
	private PlayerDetails m_PlayerDetails;
	private IWorldQuerrier m_IWorldQuerrier;
	
	private GameEnding gameEnding;
	private PlayerPopup playerPopup;
	private Table table;
	private TimeStatus timeStatus;
	
	public Table getTable(){
		return table;
	}
	
	//regular construct: new game
	public World(GameType gameType){
		this.m_GameType = gameType;
		
		this.m_PlayerDetails = null;
		if(gameType == GameType.MULTI_PLAYER_LOCAL){
			this.m_PlayerDetails = PlayerDetails.getPlayerDetailsForMultiPlayerLocal();
			this.m_Player1 = new LocalPlayer(this.m_PlayerDetails.player1Color, this.m_PlayerDetails.player1Face);
			this.m_Player2 = new LocalPlayer(this.m_PlayerDetails.player2Color, this.m_PlayerDetails.player2Face);
			
			this.table = new Table(this.m_PlayerDetails.firstPlayer, false);
		} else if(gameType == GameType.SINGLE_LOCAL){
			this.m_PlayerDetails = PlayerDetails.getPlayerDetailsForSinglePlayer();
			this.m_Player1 = new LocalPlayer(this.m_PlayerDetails.player1Color, this.m_PlayerDetails.player1Face);
			this.m_Player2 = new ComputerPlayer(this.m_PlayerDetails.player2Color, this.getIWorldQuerrier());
			
			this.table = new Table(this.m_PlayerDetails.firstPlayer, (this.m_PlayerDetails.player2Color==this.m_PlayerDetails.firstPlayer));
		}
		this.m_CurrentPlayerColor = this.m_PlayerDetails.firstPlayer;
		
		this.m_Player1.addPlayerMoveListner(this);
		this.m_Player2.addPlayerMoveListner(this);
		
		this.m_Clepsidra = new Clepsidra(this, this.getIWorldQuerrier());
		this.m_Clepsidra.addPlayerMoveListner(this);
	}

	//construct for continue game (SINGLE PLAYER & MULTIPLAYER LOCAL & MULTIPLAYER_BT)
	public World(SessionSaveData sessionSaveDataObj) {
		this.m_PlayerDetails = sessionSaveDataObj.ssd_PlayerDetails;
		this.m_GameType = sessionSaveDataObj.ssd_GameType;
		
		//in multiplayerBT newOrder is allready set
		if(this.m_GameType!=GameType.MULTI_PLAYER_BLUETOOTH)
			this.m_PlayerDetails.newOrder();
		
		
		if(this.m_GameType == GameType.MULTI_PLAYER_LOCAL){
			this.m_Player1 = new LocalPlayer(this.m_PlayerDetails.player1Color, this.m_PlayerDetails.player1Face);
			this.m_Player2 = new LocalPlayer(this.m_PlayerDetails.player2Color, this.m_PlayerDetails.player2Face);
			
			this.table = new Table(this.m_PlayerDetails.firstPlayer, false);
		} else if(this.m_GameType == GameType.SINGLE_LOCAL){
			this.m_Player1 = new LocalPlayer(this.m_PlayerDetails.player1Color, this.m_PlayerDetails.player1Face);
			this.m_Player2 = new ComputerPlayer(this.m_PlayerDetails.player2Color, this.getIWorldQuerrier());
			
			this.table = new Table(this.m_PlayerDetails.firstPlayer, (this.m_PlayerDetails.player2Color==this.m_PlayerDetails.firstPlayer));
		} else if(this.m_GameType == GameType.MULTI_PLAYER_BLUETOOTH){
			this.m_Player1 = new LocalPlayer(this.m_PlayerDetails.player1Color, this.m_PlayerDetails.player1Face);
			this.m_Player2 = new RemotePlayer(this.m_PlayerDetails.player2Color,  this.m_PlayerDetails.player2Face);
			
			this.table = new Table(this.m_PlayerDetails.firstPlayer, false);
		}
		
		this.m_CurrentPlayerColor = this.m_PlayerDetails.firstPlayer;
		
		this.m_Player1.addPlayerMoveListner(this);
		this.m_Player2.addPlayerMoveListner(this);	
		
		this.m_Clepsidra = new Clepsidra(this, this.getIWorldQuerrier());
		this.m_Clepsidra.addPlayerMoveListner(this);
	}

	//construct for: multiplayerBT new game
	public World(GameType gameType, PlayerDetails playerDetails) {
		this.m_GameType = gameType;
		this.m_PlayerDetails = playerDetails;
		
		this.m_Player1 = new LocalPlayer(this.m_PlayerDetails.player1Color, this.m_PlayerDetails.player1Face);
		this.m_Player2 = new RemotePlayer(this.m_PlayerDetails.player2Color, this.m_PlayerDetails.player2Face);
		
		this.m_Player1.addPlayerMoveListner(this);
		this.m_Player2.addPlayerMoveListner(this);	
		
		this.m_CurrentPlayerColor = this.m_PlayerDetails.firstPlayer;
		this.table = new Table(this.m_PlayerDetails.firstPlayer, false);
		
		//local player is always first
		this.m_Clepsidra = new Clepsidra(this, this.getIWorldQuerrier());
		this.m_Clepsidra.addPlayerMoveListner(this);
	}

	private int m_LastMove_X=-1, m_LastMove_Y=-1;
	
	public void interpretTouchEvent(TouchEvent event){
		GenericPlayer currentPlayer = this.getIWorldQuerrier().getCurrentPlayer();

		if(currentPlayer instanceof LocalPlayer){
			((LocalPlayer)currentPlayer).interpretTouchEvent(event);
		} else {
			this.playerPopup = PlayerPopup.NOT_YOUR_TURN;
		}
	}

	public void killGameWorld(){
		m_Clepsidra.kill();
		
		if(m_Player1 instanceof ComputerPlayer)
			((ComputerPlayer)m_Player1).kill();
		else if(m_Player2 instanceof ComputerPlayer)
			((ComputerPlayer)m_Player2).kill();
	}

	public void pauseGameWorld() {
		m_Clepsidra.pause();
		
		if(m_Player1 instanceof ComputerPlayer)
			((ComputerPlayer)m_Player1).pause();
		else if(m_Player2 instanceof ComputerPlayer)
			((ComputerPlayer)m_Player2).pause();
	}

	public void resumeGameWorld() {
		m_Clepsidra.resume();
		
		if(m_Player1 instanceof ComputerPlayer)
			((ComputerPlayer)m_Player1).resume();
		else if(m_Player2 instanceof ComputerPlayer)
			((ComputerPlayer)m_Player2).resume();
	}

	public void startGameWorld() {
		m_Clepsidra.start();
		
		if(m_GameType==GameType.SINGLE_LOCAL){
			if(m_Player1 instanceof ComputerPlayer)
				((ComputerPlayer)m_Player1).start();
			else
				((ComputerPlayer)m_Player2).start();
		}
		
		if(this.m_PlayerDetails.firstPlayer==this.m_PlayerDetails.player1Color)
			this.playerPopup = PlayerPopup.THIS_IS_YOU_AND_IM_FIRST;
	}

	public PlayerDetails getPlayerDetails() {
		return m_PlayerDetails;
	}
	
	public IWorldQuerrier getIWorldQuerrier(){
		if(this.m_IWorldQuerrier==null)
			this.m_IWorldQuerrier = new IWorldQuerrier() {
				
				public TimeStatus getTimeStatusToPresent() {
					return timeStatus;
				}
				
				public PlayerPopup getPlayerPopupToPresent() {
					return playerPopup;
				}
				
				public int[][] getPiecesToPresent() {
					return table.getPieces();
				}
				
				public ContentValues getLastMoveToPresent() {
					ContentValues cv = new ContentValues();
					cv.put("x", m_LastMove_X);
					cv.put("y", m_LastMove_Y);
					return cv;
				}
				
				public GameEnding getGameEndingToPresent() {
					return gameEnding;
				}
				
				public PlayerColor getCurrentPlayerColor() {
					return m_CurrentPlayerColor;
					// or getCurrentPlayer().getColor();
				}
				
				public PlayerColor getColorOfPlayer(boolean forPlayer1) {
					if(forPlayer1==true)
						return m_Player1.getPlayerColor();
					else
						return m_Player2.getPlayerColor();
				}

				public GenericPlayer getCurrentPlayer() {
					if(m_CurrentPlayerColor == m_Player1.getPlayerColor())
						return m_Player1;
					else
						return m_Player2;
				}

				public ContentValues getOptimumMove() {
					return table.getOptimumMove();
				}

				public ContentValues getRandomMove() {
					return table.getRandomMove();
				}

				public PlayerFace getFaceOfPlayer(boolean forPlayer1) {
					if(forPlayer1==true)
						return m_Player1.getPlayerFace();
					else
						return m_Player2.getPlayerFace();
				}
			};
			
			return this.m_IWorldQuerrier;
	}
	
	//FROM: ITimeMonitor-----------------------------
	//-----------------------------------------------
	
	public void onTimeUpdate(TimeStatus timeStatus) {
		this.timeStatus = timeStatus;
	}
	
	//FROM: IPlayerMoveListner-----------------------------
	//-----------------------------------------------
	
	public void onNewPieceMove(PlayerColor playerColor, int x, int y) {		
		//check if move is made by clepsidra
		if(playerColor==PlayerColor.CURRENT_COLOR){
			if(x==-1 || y==-1) {
				gameEnding = GameEnding.DRAW; //no place for new pieces
				return;
			}
			this.internalOnNewPieceMove(this.getIWorldQuerrier().getCurrentPlayerColor(), x, y);
		} else {
			this.internalOnNewPieceMove(playerColor, x, y);
		}
	}

	private void internalOnNewPieceMove(PlayerColor playerColor, int x, int y){
		if(table.checkAvailability(x, y)==false){
			playerPopup=PlayerPopup.YOU_CANT_MOVE_THERE;
			return;
		} else
			playerPopup=null;
		
		if(this.m_CurrentPlayerColor!=playerColor) {
			playerPopup = PlayerPopup.NOT_YOUR_TURN;
			return;
		} else
			playerPopup=null;

		if(this.m_CurrentPlayerColor==playerColor){
			if(m_GameType==GameType.MULTI_PLAYER_BLUETOOTH && this.m_CurrentPlayerColor == m_PlayerDetails.player1Color){
				BTService.getINSTANCE().getIInputRemote().send_Move_Msg(x, y);
			}
			
			table.addPiece(x, y, playerColor);
			this.m_CurrentPlayerColor = this.m_CurrentPlayerColor.flip();//flip players
			this.m_Clepsidra.reset();
			
			this.m_LastMove_X = x;
			this.m_LastMove_Y = y;
			
			//check if move ENDED game
			int winningScore = table.externalWinningPos(x, y, playerColor);
			if(winningScore==Table.winningMove){
				this.gameEnding=GameEnding.KILLER_MOVE;
				this.killGameWorld();
				this.m_CurrentPlayerColor=m_CurrentPlayerColor.flip();//flip back
			} else if (table.isDraw()==true){
				this.gameEnding=GameEnding.DRAW;
				this.killGameWorld();
			}
			
			if (GameSaveBundleManager.isSoundEnabled()) {
				Sound piceMoveSound = Assets.getRandomPieceMoveSound();
				piceMoveSound.play(1);
			}
		}
	}
		
	public void onPlayerExited(PlayerColor playerColor) {
		// TODO Auto-generated method stub
	}
	
	public RemotePlayer getRemotePlayer() {
		if(this.m_GameType==GameType.MULTI_PLAYER_BLUETOOTH)
			return (RemotePlayer) this.m_Player2;
		return null;
	}
}
