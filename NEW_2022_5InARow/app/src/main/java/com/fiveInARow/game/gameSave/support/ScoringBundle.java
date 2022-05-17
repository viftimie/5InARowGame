package com.fiveInARow.game.gameSave.support;

import java.io.Serializable;

import com.fiveInARow.game.gameSave.scoring.Record;
import com.fiveInARow.game.gameSave.scoring.Score;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.ScoreType;

public class ScoringBundle implements Serializable {
	private static final long serialVersionUID = 2226464603343578014L;
	private static final int MAX_HISTORY = 5;
	
	public Record sb_Record;
	public Score[] sb_scoresWithCPU;
	public Score[] sb_scoresInMultiplayer;
	
	public ScoringBundle(){
		this.sb_scoresWithCPU = new Score[] {
				Score.getVoidScore(ScoreType.WITH_CPU),
				Score.getVoidScore(ScoreType.WITH_CPU),
				Score.getVoidScore(ScoreType.WITH_CPU),
				Score.getVoidScore(ScoreType.WITH_CPU),
				Score.getVoidScore(ScoreType.WITH_CPU)
		};
		
		this.sb_scoresInMultiplayer = new Score[] {
				Score.getVoidScore(ScoreType.IN_MULTIPLAYER),
				Score.getVoidScore(ScoreType.IN_MULTIPLAYER),
				Score.getVoidScore(ScoreType.IN_MULTIPLAYER),
				Score.getVoidScore(ScoreType.IN_MULTIPLAYER),
				Score.getVoidScore(ScoreType.IN_MULTIPLAYER)
		};
		
		this.sb_Record = new Record(0, 0, 0);
	}
	
	public void addScore(int score, String label){
		if(score>0){
			Score[] scores;
			if(label==null)
				scores = this.sb_scoresWithCPU;
			else
				scores = this.sb_scoresInMultiplayer;
			
			for (int i = 0; i < MAX_HISTORY; i++) {
				if (scores[i].getScore() < score) {
					for (int j = MAX_HISTORY-1; j > i; j--){
						scores[j].updateScore(scores[j-1].getScore(), scores[j-1].getLabel());
					}
				
					scores[i].updateScore(score, label);
					break;
				}
			}
		}	
	}

	public String[] getFormattedScores(ScoreType type){
		String[] result = new String[MAX_HISTORY];
		Score[] scores;
		if(type==ScoreType.WITH_CPU)
			scores = this.sb_scoresWithCPU;
		else
			scores = this.sb_scoresInMultiplayer;
		
		for(int i=0; i<MAX_HISTORY;i++){
			result[i] = scores[i].getFormattedScore();
		}
		
		return result;
	}
	
	public void updateRecord (int addToWinnings, int addToDraws, int addToLosses){
		this.sb_Record.updateRecord(addToWinnings, addToDraws, addToLosses);
	}
}
