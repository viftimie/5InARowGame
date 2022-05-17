package com.fiveInARow.game.gameSave.scoring;

import java.io.Serializable;

import com.fiveInARow.game.gameWorld.support.BTGameEnums.ScoreType;

public abstract class Score implements Serializable{
	private static final long serialVersionUID = 5689010988712553264L;
	protected static final String INVALID_BUT_FORMATTED = "-";
	
	protected int m_Score;
	protected String m_Label;
	
	protected Score(int score, String label){
		this.m_Score = score;
		this.m_Label = label;
	}
	
	public abstract String getFormattedScore();
	
	protected boolean isValid(){
		return (this.m_Score>0);
	}
	
	public static Score getVoidScore(ScoreType scoreType){
		if(scoreType==ScoreType.WITH_CPU)
			return new Score_Type_LocalGames(0, null);
		else
			return new Score_Type_MultiplayerBTGames(0, null);
	}

	public void updateScore(int score, String label){
		this.m_Score = score;
		this.m_Label = label;
	}
	
	public int getScore(){
		return this.m_Score;
	}
	
	public String getLabel(){
		return this.m_Label;
	}
}

class Score_Type_MultiplayerBTGames extends Score{	
	private static final long serialVersionUID = 7427314688144511773L;
	
	private static final String FORMAT =  "%-6s %-10s";
	
	protected Score_Type_MultiplayerBTGames(int score, String label) {
		super(score, label);
	}

	@Override
	public String getFormattedScore() {
		if(this.isValid()==false)
			return INVALID_BUT_FORMATTED;
		
		if(this.m_Label!=null && this.m_Label.length()>10)
			this.m_Label = this.m_Label.substring(0, 8)+"..";
		return String.format(FORMAT, this.m_Score, this.m_Label);
	}
}

class Score_Type_LocalGames extends Score{
	private static final long serialVersionUID = -7418507067305523994L;

	protected Score_Type_LocalGames(int score, String label) {
		super(score, label);
	}

	@Override
	public String getFormattedScore() {
		if(this.isValid()==false)
			return INVALID_BUT_FORMATTED;
		
		return String.valueOf(this.m_Score);
	}
}
