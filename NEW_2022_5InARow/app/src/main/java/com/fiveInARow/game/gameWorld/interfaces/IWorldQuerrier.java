package com.fiveInARow.game.gameWorld.interfaces;

import android.content.ContentValues;

import com.fiveInARow.game.gameWorld.players.generic.GenericPlayer;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.GameEnding;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerColor;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerFace;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerPopup;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.TimeStatus;

public interface IWorldQuerrier {
	
	public PlayerColor getCurrentPlayerColor();
	public PlayerColor getColorOfPlayer(boolean forPlayer1);
	public PlayerFace getFaceOfPlayer(boolean forPlayer1);
	public GenericPlayer getCurrentPlayer();
	
	//used by screen
	public int[][] getPiecesToPresent();
	public PlayerPopup getPlayerPopupToPresent();
	public GameEnding getGameEndingToPresent();
	public TimeStatus getTimeStatusToPresent();
	public ContentValues getLastMoveToPresent();
	
	//used by computerPlayer
	public ContentValues getOptimumMove();
	public ContentValues getRandomMove();
}
