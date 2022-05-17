package com.fiveInARow.game.gameWorld.players.remotePlayer.listeners;

import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerColor;

public interface IRemoteGameStateChanger {
	
	void onRemoteGameBegin();
	void onRemoteGamePause();
	void onRemoteGameResume();
	void onRemoteGameQuit();
	void onRemoteGamePlayAgain(PlayerColor firstPlayer);
	
}
