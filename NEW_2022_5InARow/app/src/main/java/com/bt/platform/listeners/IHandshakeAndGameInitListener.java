package com.bt.platform.listeners;

import java.util.EventListener;

import com.bt.BTEnums.FailedHandshakeReason;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerColor;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerFace;

//a way to notify the success of the connection (correct key & all)
public interface IHandshakeAndGameInitListener extends EventListener{
	
	public void onCorrectHandshake();
	public void onFailedHandshake(FailedHandshakeReason reason);
	public void onReceivedRecordMessage(int gamesWon, int gamesDraw, int gamesLost);
	public void onReceivedGameInitMessage(PlayerFace myFace, PlayerColor myColor, PlayerColor firstPlayer);
	public void onClickToBeginGame();
	
}


