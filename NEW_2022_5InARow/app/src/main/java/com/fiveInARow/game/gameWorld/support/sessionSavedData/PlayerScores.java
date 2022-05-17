package com.fiveInARow.game.gameWorld.support.sessionSavedData;

import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerColor;

public class PlayerScores {
	public static final int VICTORY_MULTIPLYER = 5, DRAW_MULTIPLYER = 1;
	public int ps_WHITE_Victories = 0;
	public int ps_BLACK_Victories = 0;
	public int ps_draws = 0;
	
	public int getPlayerScore (PlayerColor playerColor) {
		if(playerColor==PlayerColor.WHITE)
			return ps_WHITE_Victories * VICTORY_MULTIPLYER + ps_draws * DRAW_MULTIPLYER;
		else
			return ps_BLACK_Victories * VICTORY_MULTIPLYER + ps_draws * DRAW_MULTIPLYER;
	}

	@Override
	public String toString() {
		return "PlayerScores [ps_WHITE_Victories=" + ps_WHITE_Victories
				+ ", ps_BLACK_Victories=" + ps_BLACK_Victories + ", ps_draws="
				+ ps_draws + "]";
	}
	
}
