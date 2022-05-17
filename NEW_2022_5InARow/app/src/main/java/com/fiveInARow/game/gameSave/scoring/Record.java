package com.fiveInARow.game.gameSave.scoring;

import java.io.Serializable;

public class Record implements Serializable{
	private static final long serialVersionUID = 2831843842469247689L;
	private static final String FORMAT = "%d-%d-%d";
	private static final String UNKNOWN_BUT_FORMATTED = "?-?-?";
	
	public int pr_gamesWon, pr_gamesDraw, pr_gamesLost;

	public Record(int gamesWon, int gamesDraw, int gamesLost) {
		this.pr_gamesWon = gamesWon;
		this.pr_gamesDraw = gamesDraw;
		this.pr_gamesLost = gamesLost;
	}
	
	public String getFormattedRecord(){
		return String.format(FORMAT, this.pr_gamesWon, this.pr_gamesDraw, this.pr_gamesLost);
	}
	
	public String getFormattedRecordForCPU(){
		return String.format(FORMAT, this.pr_gamesLost, this.pr_gamesDraw, this.pr_gamesWon);
	}
	
	public static String getFormattedUnknownScore(){
		return UNKNOWN_BUT_FORMATTED;
	}
	
	public void updateRecord (int addToWinnings, int addToDraws, int addToLosses){
		this.pr_gamesWon+=addToWinnings;
		this.pr_gamesDraw+=addToDraws;
		this.pr_gamesLost+=addToLosses;
	}
}
