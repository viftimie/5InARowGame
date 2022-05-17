package com.fiveInARow.game.gameWorld.support.sessionSavedData;

import com.fiveInARow.game.gameWorld.support.BTGameEnums.GameType;

public class SessionSaveData {
	public PlayerScores ssd_PlayerScores; 
	public PlayerDetails ssd_PlayerDetails;
	public GameType ssd_GameType;
	
	public SessionSaveData(){
		this.ssd_PlayerScores = new PlayerScores();
		this.ssd_PlayerDetails = new PlayerDetails();
	}
}
