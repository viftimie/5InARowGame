package com.fiveInARow.game.screens;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;

import com.fiveInARow.framework.generics.Graphics;
import com.fiveInARow.framework.generics.IGame;
import com.fiveInARow.framework.generics.Input.TouchEvent;
import com.fiveInARow.framework.generics.Screen;
import com.fiveInARow.game.Assets;
import com.fiveInARow.game.gameSave.GameSaveBundleManager;
import com.fiveInARow.utils.Logger;
import com.fiveInARow.utils.TouchAndCoordinatesUtils;

public class Screen_Help4  extends Screen {
	private static final int WAIT_TIME_ON_A_PAGE = 11000; //11 seconds
	private boolean m_ShowSlide1 = true;
	private Timer m_PageFlippingTimer;
	
	public Screen_Help4(IGame game, Activity activity) {
		super(game, activity);
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
				if (TouchAndCoordinatesUtils.inBoundOfSquareArea(event, TouchAndCoordinatesUtils.squareArea_nextOnHelpScreens)) {
					this.m_Game.setScreen(new Screen_MainMenu(this.m_Game, this.m_Activity));
					
					if (GameSaveBundleManager.isSoundEnabled())
						Assets.sound_click.play(1);
					return;
				}
			}
		}
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = this.m_Game.getGraphics();
		g.drawPixmap(Assets.img_main_game_bg, 0, 0);
		
		if(this.m_ShowSlide1==true)
			g.drawPixmap(Assets.img_help4_part1, 0, 0);
		else
			g.drawPixmap(Assets.img_help4_part2, 0, 0);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
	}
	
	@Override
	public boolean onBackPress_II() {
		this.cancelPageFlippingTimer();
		this.m_Game.setScreen(new Screen_Help3(this.m_Game, this.m_Activity));
		return false;
	}
	
	private void cancelPageFlippingTimer(){		
		if (this.m_PageFlippingTimer != null) {
			this.m_PageFlippingTimer.cancel();
			this.m_PageFlippingTimer = null;
    	}
	}
	
	// FOR: Timer
	private void setupPageFlippingTimer(){
		// cancel current timer
		this.cancelPageFlippingTimer();
		
		// setup again
		this.m_PageFlippingTimer = new Timer();
		this.m_PageFlippingTimer.scheduleAtFixedRate(new TimerTask(){
			private static final String TAG = "PageFlippingTimer";
			
			@Override
			public void run() {
				Logger.d(TAG, "run()");
				m_ShowSlide1 = !m_ShowSlide1;
			}}, WAIT_TIME_ON_A_PAGE, WAIT_TIME_ON_A_PAGE);
	}
}