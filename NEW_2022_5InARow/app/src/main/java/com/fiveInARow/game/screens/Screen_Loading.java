package com.fiveInARow.game.screens;

import android.app.Activity;
import android.graphics.Typeface;

import com.fiveInARow.framework.generics.Graphics;
import com.fiveInARow.framework.generics.IGame;
import com.fiveInARow.framework.generics.Screen;
import com.fiveInARow.game.Assets;
import com.fiveInARow.game.gameSave.GameSaveBundleManager;

public class Screen_Loading extends Screen {
	
	public Screen_Loading(IGame game, Activity activity) {
		super(game, activity);
	}

	@Override
	public void update(float deltaTime) {
		Graphics g = m_Game.getGraphics();
		
		//load assets - AUDIOS
		Assets.sound_click  = m_Game.getAudio().newSound("audio/click.ogg");
		Assets.music_main_menu  = m_Game.getAudio().newMusic("audio/main_menu_bg.ogg");
		Assets.music_high_scores  = m_Game.getAudio().newMusic("audio/high_scores_bg.ogg");
		Assets.sound_piece_move_version1  = m_Game.getAudio().newSound("audio/piece-moves/version1.ogg");
		Assets.sound_piece_move_version2  = m_Game.getAudio().newSound("audio/piece-moves/version2.ogg");
		Assets.sound_piece_move_version3  = m_Game.getAudio().newSound("audio/piece-moves/version3.ogg");
		
		Assets.sound_bart_loss  = m_Game.getAudio().newSound("audio/win-loss/bart_loss.ogg");
		Assets.sound_bart_win  = m_Game.getAudio().newSound("audio/win-loss/bart_win.ogg");
		Assets.sound_homer_loss  = m_Game.getAudio().newSound("audio/win-loss/homer_loss.ogg");
		Assets.sound_homer_win  = m_Game.getAudio().newSound("audio/win-loss/homer_win.ogg");
		Assets.sound_frink_loss  = m_Game.getAudio().newSound("audio/win-loss/frink_loss.ogg");
		Assets.sound_frink_win  = m_Game.getAudio().newSound("audio/win-loss/frink_win.ogg");
		
		//IMAGES
		Assets.img_hue = g.newPixmap("img/full-screen/hue.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_main_game_bg = g.newPixmap("img/full-screen/main_game_bg.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_game_overlay_level1 = g.newPixmap("img/full-screen/game_overlay_level1.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_main_menu_overlay_level1 = g.newPixmap("img/full-screen/main_menu_overlay_level1.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_dialog_game_over = g.newPixmap("img/dialogs/game_over.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_dialog_game_paused = g.newPixmap("img/dialogs/game_paused.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_dialog_game_ready = g.newPixmap("img/dialogs/game_ready.png", Graphics.PixmapFormat.ARGB4444);
		
		Assets.img_characters_faces = g.newPixmap("img/small-ones/characters_faces.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_characters_for_main_menu = g.newPixmap("img/small-ones/characters_for_main_menu.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_hourglasses = g.newPixmap("img/small-ones/hourglasses.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_numbers = g.newPixmap("img/small-ones/numbers.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_sound_icons = g.newPixmap("img/small-ones/sound_icons.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_the_pieces = g.newPixmap("img/small-ones/the_pieces.png", Graphics.PixmapFormat.ARGB4444);
		
		Assets.img_the_pieces_for_last_move = g.newPixmap("img/small-ones/the_pieces_for_last_move.png", Graphics.PixmapFormat.ARGB4444);
		
		Assets.img_warning_popups = g.newPixmap("img/small-ones/warning_popups.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_characters_faces_victory = g.newPixmap("img/small-ones/characters_faces_victory.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_characters_for_highscores = g.newPixmap("img/small-ones/characters_for_high_score.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_dialog_multiplayer_options = g.newPixmap("img/dialogs/multiplayer_options.png", Graphics.PixmapFormat.ARGB4444);
		
		//Update: 07.09.2012
		//img/dialogs/ready_waiting
		Assets.img_characters_game_ready = g.newPixmap("img/dialogs/ready_waiting/characters_game_ready.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_characters_game_ready_flipped = g.newPixmap("img/dialogs/ready_waiting/characters_game_ready_flipped.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_characters_game_waiting_unknown = g.newPixmap("img/dialogs/ready_waiting/characters_game_waiting_unknown.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_characters_game_waiting_unknown_flipped = g.newPixmap("img/dialogs/ready_waiting/characters_game_waiting_unknown_flipped.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_dialog_game_ready_waiting_msg = g.newPixmap("img/dialogs/ready_waiting/dialog_game_ready_waiting_msg.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_dialog_game_ready_waiting = g.newPixmap("img/dialogs/ready_waiting/dialog_game_ready_waiting.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_characters_faces_game_waiting_unknown = g.newPixmap("img/dialogs/ready_waiting/characters_faces_game_waiting_unknown.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_player_highlight = g.newPixmap("img/dialogs/ready_waiting/player_highlight.png", Graphics.PixmapFormat.ARGB4444);
		
		//img/screens/highscores
		Assets.img_highscores = g.newPixmap("img/screens/highscores/highscores.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_highscores_titles = g.newPixmap("img/screens/highscores/highscores_titles.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_highscores_record_popup = g.newPixmap("img/screens/highscores/highscores_record_popup.png", Graphics.PixmapFormat.ARGB4444);
		
		//img/screens/help
		Assets.img_help1 = g.newPixmap("img/screens/help/help1.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_help2 = g.newPixmap("img/screens/help/help2.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_help3 = g.newPixmap("img/screens/help/help3.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_help4_part1 = g.newPixmap("img/screens/help/help4_slide1.png", Graphics.PixmapFormat.ARGB4444);
		Assets.img_help4_part2 = g.newPixmap("img/screens/help/help4_slide2.png", Graphics.PixmapFormat.ARGB4444);
		
		//theme font
		Assets.theme_font =  Typeface.createFromAsset(m_Activity.getAssets(), "fonts/berlinSansFB.ttf");
		
		//load saved settings
		GameSaveBundleManager.load(m_Game.getFileIO());
		
		//then: go to main menu
		m_Game.setScreen(new Screen_MainMenu(m_Game, m_Activity));
	}
	
	//----------------------------------
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
	}

	@Override
	public void present(float deltaTime) {
		// TODO Auto-generated method stub
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
	}

}
