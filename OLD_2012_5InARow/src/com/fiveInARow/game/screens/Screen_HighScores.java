package com.fiveInARow.game.screens;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.graphics.Paint;
import android.graphics.Paint.Align;

import com.fiveInARow.R;
import com.fiveInARow.framework.generics.Graphics;
import com.fiveInARow.framework.generics.IGame;
import com.fiveInARow.framework.generics.Input.TouchEvent;
import com.fiveInARow.framework.generics.Screen;
import com.fiveInARow.game.Assets;
import com.fiveInARow.game.gameSave.GameSaveBundleManager;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.ScoreType;
import com.fiveInARow.game.gameWorld.support.sessionSavedData.PlayerScores;
import com.fiveInARow.utils.Logger;
import com.fiveInARow.utils.TouchAndCoordinatesUtils;
import com.fiveInARow.utils.UIUtils;

public class Screen_HighScores extends Screen{
	private static final int WAIT_TIME_THEN_CLOSE_RECORD_POPUP = 6000; //6 seconds
	private static final int WAIT_TIME_ON_A_PAGE = 11000; //11 seconds
	
	private boolean m_ShowRecordPopup;
	private boolean m_ShowScoresWithCPU = true;
	private Timer m_RecordPopupTimer;
	private Timer m_PageFlippingTimer;
	
	public Screen_HighScores(IGame game, Activity activity) {
		super(game, activity);
		
		if (GameSaveBundleManager.isSoundEnabled())
			Assets.resumeAudio(Assets.music_high_scores);
		
		this.setupPageFlippingTimer();
	}
	
	@Override
	public void update(float deltaTime) {
		List<TouchEvent> touchEvents = this.m_Game.getInput().getTouchEvents();
		this.m_Game.getInput().getKeyEvents();
		
		if(touchEvents==null) return;
		
		int len = touchEvents.size();
		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);
			if (event.type == TouchEvent.TOUCH_UP) {
				if (TouchAndCoordinatesUtils.inBoundOfSquareArea(event, TouchAndCoordinatesUtils.squareArea_highScoreBack)) {
					if(GameSaveBundleManager.isSoundEnabled())
						Assets.sound_click.play(1);
					
					this.m_Game.setScreen(new Screen_MainMenu(this.m_Game, this.m_Activity));
					return;
				} else if (TouchAndCoordinatesUtils.inBoundOfSquareArea(event, TouchAndCoordinatesUtils.squareArea_highscores_record)){
					if(GameSaveBundleManager.isSoundEnabled())
						Assets.sound_click.play(1);
					
					this.toggleRecordPopup();
				} else if (TouchAndCoordinatesUtils.inBoundOfSquareArea(event, TouchAndCoordinatesUtils.squareArea_highscores_page1)){
					if(GameSaveBundleManager.isSoundEnabled())
						Assets.sound_click.play(1);
					
					this.m_ShowScoresWithCPU = true;
					this.setupPageFlippingTimer();
				} else if (TouchAndCoordinatesUtils.inBoundOfSquareArea(event, TouchAndCoordinatesUtils.squareArea_highscores_page2)){
					if(GameSaveBundleManager.isSoundEnabled())
						Assets.sound_click.play(1);
					
					this.m_ShowScoresWithCPU = false;
					this.setupPageFlippingTimer();
				}
			}
		}
	}
	
	@Override
	public void present(float deltaTime) {
		Graphics g = m_Game.getGraphics();
		g.drawPixmap(Assets.img_main_game_bg, 0, 0);
		g.drawPixmap(Assets.img_highscores, 0, 0);
		
		if(this.m_ShowScoresWithCPU==true){
			g.drawPixmap(Assets.img_highscores_titles, 22, 57, 0, 0, 228, 26);
			this.drawHighScores_GamesWithCPU(g);
		} else {
			g.drawPixmap(Assets.img_highscores_titles, 22, 57, 0, 26, 228, 26);
			this.drawHighScores_GamesInMultiplayerBT(g);
		}
		
		this.drawRecord(g);
		this.drawFace(g);
		
		if(this.m_ShowRecordPopup==true){
			g.drawPixmap(Assets.img_highscores_record_popup, 297, 88);
		}
	}

	private void drawHighScores_GamesInMultiplayerBT(Graphics g) {
		String[] highScores = GameSaveBundleManager.getFormattedScores(ScoreType.IN_MULTIPLAYER);
		Paint paint = UIUtils.getPaintForTextDrawing(22, Align.LEFT, R.color.blue_universal);		
		int y = 110, x = 35;
		for (int i = 0; i < highScores.length; i++) {
			g.drawText((i + 1) + ". "+ highScores[i], ((i!=0)? x : x + 3), y, paint);
			y += 30;
		}
	}

	private void drawHighScores_GamesWithCPU(Graphics g) {
		String[] highScores = GameSaveBundleManager.getFormattedScores(ScoreType.WITH_CPU);
		Paint paint = UIUtils.getPaintForTextDrawing(22, Align.LEFT, R.color.blue_universal);		
		int y = 110, x = 35;
		for (int i = 0; i < highScores.length; i++) {
			g.drawText((i + 1) + ". "+ highScores[i], ((i!=0)? x : x + 3), y, paint);
			y += 30;
		}
	}

	private void drawFace(Graphics g) {
		boolean looser = (GameSaveBundleManager.getGamesWon()==0 && GameSaveBundleManager.getGamesLost()!=0);
		if(looser==true){
			g.drawPixmap(Assets.img_characters_for_highscores, 280, 150, 400, 0, 200, 170);
		    return;
		}
		
		boolean stillALooser = (GameSaveBundleManager.getGamesWon() * PlayerScores.VICTORY_MULTIPLYER+
				                GameSaveBundleManager.getGamesDraw() * PlayerScores.DRAW_MULTIPLYER)
				                < 
		                       (GameSaveBundleManager.getGamesLost() * PlayerScores.VICTORY_MULTIPLYER);
		if(stillALooser==true)
			g.drawPixmap(Assets.img_characters_for_highscores, 280, 150, 0, 0, 200, 170);
		else
			g.drawPixmap(Assets.img_characters_for_highscores, 280, 150, 200, 0, 200, 170);
	}

	private void drawRecord(Graphics g) {
		Paint paint = UIUtils.getPaintForTextDrawing(22, Align.CENTER, R.color.blue_universal);	
		String record = GameSaveBundleManager.getFormattedScore(true);
		g.drawText(record, 376, 80, paint);
	}

	@Override
	public void pause() {
		Assets.stopAllAudio();
	}
	
	@Override
	public void resume() {
		if (GameSaveBundleManager.isSoundEnabled())
			Assets.resumeAudio(Assets.music_high_scores);
	}
	
	@Override
	public void dispose() {
		//nothing
	}
	
	@Override
	public boolean onBackPress_II() {
		this.cancelRecordPopupTimer();
		this.cancelPageFlippingTimer();
		this.m_Game.setScreen(new Screen_MainMenu(m_Game, m_Activity));
		return false;
	}
	
	// FOR: Timer
	private void resetRecordPopupTimer(){
		// cancel current timer
		this.cancelRecordPopupTimer();
		
		// setup again
		this.m_ShowRecordPopup = true;
		this.m_RecordPopupTimer = new Timer();
		this.m_RecordPopupTimer.schedule(new TimerTask(){
			private static final String TAG = "Screen_HighScores-RecordPopupTimer";
			
			@Override
			public void run() {
				Logger.d(TAG, "run()");
				m_ShowRecordPopup = false;
			}}, WAIT_TIME_THEN_CLOSE_RECORD_POPUP);
	}
	
	private void cancelRecordPopupTimer(){
		this.m_ShowRecordPopup = false;
		
		if (this.m_RecordPopupTimer != null) {
			this.m_RecordPopupTimer.cancel();
			this.m_RecordPopupTimer = null;
    	}
	}
	
	private void toggleRecordPopup(){
		if(this.m_ShowRecordPopup==true)
			this.cancelRecordPopupTimer();
		else
			this.resetRecordPopupTimer();
	}
	
	private void cancelPageFlippingTimer(){		
		if (this.m_PageFlippingTimer != null) {
			this.m_PageFlippingTimer.cancel();
			this.m_PageFlippingTimer = null;
    	}
	}
	
	private void setupPageFlippingTimer(){
		// cancel current timer
		this.cancelPageFlippingTimer();
		
		// setup again
		this.m_PageFlippingTimer = new Timer();
		this.m_PageFlippingTimer.scheduleAtFixedRate(new TimerTask(){
			private static final String TAG = "Screen_HighScores-PageFlippingTimer";
			
			@Override
			public void run() {
				Logger.d(TAG, "run()");
				m_ShowScoresWithCPU = !m_ShowScoresWithCPU;
			}}, WAIT_TIME_ON_A_PAGE, WAIT_TIME_ON_A_PAGE);
	}
	
}
