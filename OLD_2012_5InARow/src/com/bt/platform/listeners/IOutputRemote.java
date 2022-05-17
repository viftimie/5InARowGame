package com.bt.platform.listeners;

import java.util.EventListener;

import com.fiveInARow.game.gameWorld.support.BTGameEnums.GameStateChangingAction;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerColor;

//or service-to-UI link (service mean remote peer) for in-game actions
public interface IOutputRemote extends EventListener{
	
	public void onRemoteDeviceSent_ChangeGameState_Msg(String btDeviceId, GameStateChangingAction newAction);
	public void onRemoteDeviceSent_Move_Msg(String btDeviceId, int x, int y);
	public void onRemoteDeviceSent_PlayAgain_Msg(PlayerColor firstPlayer);

}
