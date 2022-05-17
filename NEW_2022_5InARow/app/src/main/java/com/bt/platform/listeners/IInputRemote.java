package com.bt.platform.listeners;

import java.util.EventListener;

import com.fiveInARow.game.gameWorld.support.BTGameEnums.GameStateChangingAction;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerColor;
import com.fiveInARow.game.gameWorld.support.sessionSavedData.PlayerDetails;

/*
 * Since you dont react DONT USE "onXXX()"
 */
public interface IInputRemote extends EventListener{
	
	public void send_ChangeGameState_Msg(GameStateChangingAction newAction);
	public void send_PlayAgain_Msg (PlayerColor firstPlayer);
	public void send_Move_Msg(int x, int y);
	public void send_GameInit_Msg(PlayerDetails playerDetails);
	
}
