package com.bt.messages.factory;

import com.bt.BTEnums.MessageType;
import com.bt.messages.C_Message;
import com.bt.messages.HS_Message;
import com.bt.messages.generic.GenericMessage;
import com.bt.settings.AppSettings;
import com.bt.utils.BtUtils;
import com.fiveInARow.game.gameSave.GameSaveBundleManager;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.GameStateChangingAction;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerColor;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerFace;

//used only by local device to send messages
public class MessageFactory {
	
	public static final C_Message new_KeepAlive_Msg(){
		C_Message result = new C_Message(MessageType.KEEP_ALIVE);
		return result;
	}
	
	//sent only by server after receiving a wrong HS (so this is sent instead of a HS & then close socket)
	public static final C_Message new_WrongConnectKey_Msg(){
		C_Message result = new C_Message(MessageType.WRONG_CONNECT_KEY);
		return result;
	}
	
	//HS from server DOES NOT contain ConnectKey
	public static final HS_Message new_ServerHandshake_Msg(){
		HS_Message result = new HS_Message(AppSettings.MY_APP_DEVICE_ID);
		return result;
	}
	
	//HS from Client DOES contain ConnectKey (input from user, 1234 form)
	public static final HS_Message new_ClientHandshake_Msg(byte[] inputConnectyKey){
		HS_Message result = new HS_Message(AppSettings.MY_APP_DEVICE_ID, inputConnectyKey);
		return result;
	}
	
	public static final GenericMessage new_ChangeGameState_Msg(GameStateChangingAction newAction){
		C_Message result = new C_Message(MessageType.CHANGE_GAME_STATE);
		result.setCargo(BtUtils.intToByteArray(newAction.getCode()));
		return result;
	}
	
	public static final GenericMessage new_Move_Msg(int x, int y){
		C_Message result = new C_Message(MessageType.NEW_MOVE);
		byte[] cargo = BtUtils.concatByteArrays(BtUtils.intToByteArray(x), 
				                                BtUtils.intToByteArray(y));
		result.setCargo(cargo);
		return result;
	}
	
	//sent only by server after RECORD, before BEGIN
	public static final GenericMessage new_GameInit_Msg(PlayerFace opponentFace, PlayerColor opponentColor, PlayerColor firstPlayer){
		C_Message result = new C_Message(MessageType.GAME_INIT);
		byte[] cargo = BtUtils.concatByteArrays(BtUtils.intToByteArray(opponentFace.getCode()), 
				                                BtUtils.intToByteArray(opponentColor.getCode()),
				                                BtUtils.intToByteArray(firstPlayer.getCode()));		
		result.setCargo(cargo);
		return result;
	}
	
	public static final GenericMessage new_Record_Message(){
		C_Message result = new C_Message(MessageType.RECORD);
		
		int gamesWon = GameSaveBundleManager.getGamesWon();
		int gamesDraw = GameSaveBundleManager.getGamesDraw();
		int gamesLost = GameSaveBundleManager.getGamesLost();
		
		byte[] cargo = BtUtils.concatByteArrays(BtUtils.intToByteArray(gamesWon), 
				                                BtUtils.intToByteArray(gamesDraw),
				                                BtUtils.intToByteArray(gamesLost));
		result.setCargo(cargo);
		
		return result;
	}
	
	//sent by first player to PRESS - PlayAgain
	public static final GenericMessage new_PlayAgain_Msg(PlayerColor firstPlayer) {
		C_Message result = new C_Message(MessageType.PLAY_AGAIN);
		byte[] cargo = BtUtils.concatByteArrays(BtUtils.intToByteArray(firstPlayer.getCode()));
		result.setCargo(cargo);
		return result;
	}
	
}
