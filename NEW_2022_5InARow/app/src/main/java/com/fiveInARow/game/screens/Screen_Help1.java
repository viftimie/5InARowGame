package com.fiveInARow.game.screens;

import java.util.List;

import android.app.Activity;

import com.fiveInARow.framework.generics.Graphics;
import com.fiveInARow.framework.generics.IGame;
import com.fiveInARow.framework.generics.Input.TouchEvent;
import com.fiveInARow.framework.generics.Screen;
import com.fiveInARow.game.Assets;
import com.fiveInARow.game.gameSave.GameSaveBundleManager;
import com.fiveInARow.utils.TouchAndCoordinatesUtils;

public class Screen_Help1 extends Screen {
	
	public Screen_Help1(IGame game, Activity activity) {
		super(game, activity);
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
					this.m_Game.setScreen(new Screen_Help2(this.m_Game, this.m_Activity));
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
		g.drawPixmap(Assets.img_help1, 0, 0);
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
		this.m_Game.setScreen(new Screen_MainMenu(this.m_Game, this.m_Activity));
		return false;
	}
	
}
