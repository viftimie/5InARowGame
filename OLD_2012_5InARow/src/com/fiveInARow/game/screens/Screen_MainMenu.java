package com.fiveInARow.game.screens;

import java.util.List;

import android.app.Activity;

import com.fiveInARow.framework.generics.Graphics;
import com.fiveInARow.framework.generics.IGame;
import com.fiveInARow.framework.generics.Input.TouchEvent;
import com.fiveInARow.framework.generics.Screen;
import com.fiveInARow.game.Assets;
import com.fiveInARow.game.gameSave.GameSaveBundleManager;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.GameType;
import com.fiveInARow.game.screens.btMultiplayer.Screen_BT_ClientDialogs;
import com.fiveInARow.game.screens.btMultiplayer.Screen_BT_Waiting;
import com.fiveInARow.utils.TouchAndCoordinatesUtils;

public class Screen_MainMenu extends Screen {
	private int indexOfRandomCharacters;
	private boolean multiplayer_options_is_ON;
	
	public Screen_MainMenu(IGame game, Activity activity) {
		super(game, activity);

		if (GameSaveBundleManager.isSoundEnabled())
			Assets.resumeAudio(Assets.music_main_menu);
		
		indexOfRandomCharacters = m_Rnd.nextInt(4);
	}
	
	public Screen_MainMenu(IGame game, Activity activity, int indexOfRandomCharacters) {
		super(game, activity);

		if (GameSaveBundleManager.isSoundEnabled())
			Assets.resumeAudio(Assets.music_main_menu);
		
		this.indexOfRandomCharacters = indexOfRandomCharacters;
	}

	@Override
	public void update(float deltaTime) {
		if(skipSinceStillInDelayForFirstTouch()==true)
			return;
		
		List<TouchEvent> touchEvents = m_Game.getInput().getTouchEvents();
		m_Game.getInput().getKeyEvents();
		
		if(touchEvents==null) return;
		
		int len = touchEvents.size();
		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);
			if (event.type == TouchEvent.TOUCH_UP) {
				
				if(multiplayer_options_is_ON==false){
					//HELP
					if (TouchAndCoordinatesUtils.inBoundOfSquareArea(event, TouchAndCoordinatesUtils.squareArea_mainMenuHelp)) {
						m_Game.setScreen(new Screen_Help1(m_Game, m_Activity));
						if (GameSaveBundleManager.isSoundEnabled())
							Assets.sound_click.play(1);
						return;
					} 
					//SOUND ON/OFF
					else if (TouchAndCoordinatesUtils.inBoundOfSquareArea(event, TouchAndCoordinatesUtils.squareArea_mainMenuSoundIcon)) {//sound click
						GameSaveBundleManager.flippSoundEnablement();
						if (GameSaveBundleManager.isSoundEnabled()){
							Assets.sound_click.play(1);
							Assets.resumeAudio(Assets.music_main_menu);
						}else{
							Assets.stopAllAudio();
						}
						return;
					} 
					//HIGHSCORES
					else if (TouchAndCoordinatesUtils.inBoundOfSquareArea(event, TouchAndCoordinatesUtils.squareArea_mainMenuHighScores)) {
						m_Game.setScreen(new Screen_HighScores(m_Game, m_Activity));
						if (GameSaveBundleManager.isSoundEnabled())
							Assets.sound_click.play(1);
						return;
					}
					//SINGLE-PLAYER
					else if (TouchAndCoordinatesUtils.inBoundOfSquareArea(event, TouchAndCoordinatesUtils.squareArea_mainMenuSinglePlayer)) {
						m_Game.setScreen(new Screen_Gameplay(m_Game, m_Activity, GameType.SINGLE_LOCAL));
						if (GameSaveBundleManager.isSoundEnabled())
							Assets.sound_click.play(1);
						return;
					}
					//MULTIPLAYER-OPTIONS
					else if (TouchAndCoordinatesUtils.inBoundOfSquareArea(event, TouchAndCoordinatesUtils.squareArea_mainMenuMultiPlayer)) {
						multiplayer_options_is_ON = true;
						useDelayForFirstTouch();
						if (GameSaveBundleManager.isSoundEnabled())
							Assets.sound_click.play(1);
						return;
					}
				} else {
					
					//MULTIPLAYER-LOCAL
					if (TouchAndCoordinatesUtils.inBoundOfSquareArea(event, TouchAndCoordinatesUtils.squareArea_multiplayer_options_local_device)) {
						m_Game.setScreen(new Screen_Gameplay(m_Game, m_Activity, GameType.MULTI_PLAYER_LOCAL));
						if (GameSaveBundleManager.isSoundEnabled())
							Assets.sound_click.play(1);
						
						multiplayer_options_is_ON = false;
						return;
					}
					//HOST BT GAME
					else if (TouchAndCoordinatesUtils.inBoundOfSquareArea(event, TouchAndCoordinatesUtils.squareArea_multiplayer_options_host_bt_game)) {
						//TODO:
						m_Game.setScreen(new Screen_BT_Waiting(m_Game, m_Activity, null, null)); //null - means for server
						if (GameSaveBundleManager.isSoundEnabled())
							Assets.sound_click.play(1);
						
						multiplayer_options_is_ON = false;
						return;
					}
					//JOIN BT GAME
					else if (TouchAndCoordinatesUtils.inBoundOfSquareArea(event, TouchAndCoordinatesUtils.squareArea_multiplayer_options_join_bt_game)) {
						//TODO:

						m_Game.setScreen(new Screen_BT_ClientDialogs(m_Game, m_Activity));
						multiplayer_options_is_ON = true;
						if (GameSaveBundleManager.isSoundEnabled())
							Assets.sound_click.play(1);
						
						multiplayer_options_is_ON = false;
						return;
					}
				}
			}
		}
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = m_Game.getGraphics();
		
		//clous & menu
		g.drawPixmap(Assets.img_main_game_bg, 0, 0);
		g.drawPixmap(Assets.img_main_menu_overlay_level1, 0, 0);
		
		//sound icon
		if(GameSaveBundleManager.isSoundEnabled()==false)
			g.drawPixmap(Assets.img_sound_icons, 10, 10, 0, 0, 40, 40);
		else
			g.drawPixmap(Assets.img_sound_icons, 10, 10, 0, 40, 40, 40);
		
		//random characters
		g.drawPixmap(Assets.img_characters_for_main_menu, 200, 100, indexOfRandomCharacters*280, 0, 280, 220);
		
		if(multiplayer_options_is_ON==true){
			g.drawPixmap(Assets.img_hue, 0, 0);
			g.drawPixmap(Assets.img_dialog_multiplayer_options, 49, 57);
		}
	}
	
	@Override
	public void pause() {
		Assets.stopAllAudio();
	}
	
	@Override
	public void resume() {
		if (GameSaveBundleManager.isSoundEnabled())
			Assets.resumeAudio(Assets.music_main_menu);
	}
	
	@Override
	public void dispose() {
	}
	
	@Override
	public boolean onBackPress_II() {
		if(multiplayer_options_is_ON==true){
			multiplayer_options_is_ON = false;
			useDelayForFirstTouch();
			return false;
		} else
			return true;
	}	
	
}
