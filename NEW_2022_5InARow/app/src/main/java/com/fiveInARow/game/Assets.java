package com.fiveInARow.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.graphics.Typeface;

import com.fiveInARow.framework.generics.Music;
import com.fiveInARow.framework.generics.Pixmap;
import com.fiveInARow.framework.generics.Sound;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerFace;

public class Assets {
	//audio
	public static Sound sound_click;
	public static Music music_main_menu;
	public static Sound sound_piece_move_version1;
	public static Sound sound_piece_move_version2;
	public static Sound sound_piece_move_version3;
	public static Sound sound_bart_loss;
	public static Sound sound_bart_win;
	public static Sound sound_homer_loss;
	public static Sound sound_homer_win;
	public static Sound sound_frink_loss;
	public static Sound sound_frink_win;
	public static Music music_high_scores;
	
	//images
	public static Pixmap img_hue;

	public static Pixmap img_main_game_bg;//the clouds
	public static Pixmap img_game_overlay_level1;//the clouds
	
	public static Pixmap img_main_menu_overlay_level1; //main menu
	public static Pixmap img_dialog_game_over;
	public static Pixmap img_dialog_game_paused;
	public static Pixmap img_dialog_game_ready;
	public static Pixmap img_characters_faces;
	public static Pixmap img_characters_for_main_menu;
	public static Pixmap img_hourglasses;
	public static Pixmap img_numbers;
	public static Pixmap img_sound_icons;
	public static Pixmap img_the_pieces;
	public static Pixmap img_the_pieces_for_last_move;
	public static Pixmap img_warning_popups;
	public static Pixmap img_characters_faces_victory;
	public static Pixmap img_characters_for_highscores;
	public static Pixmap img_dialog_multiplayer_options;
	
	//Update: 07.09.2012
	public static Pixmap img_characters_game_ready;
	public static Pixmap img_characters_game_ready_flipped;
	public static Pixmap img_characters_game_waiting_unknown;
	public static Pixmap img_characters_game_waiting_unknown_flipped;
	public static Pixmap img_dialog_game_ready_waiting_msg;
	public static Pixmap img_dialog_game_ready_waiting;
	
	public static Pixmap img_highscores;
	public static Pixmap img_highscores_titles;
	public static Pixmap img_highscores_record_popup;
	public static Pixmap img_help1;
	public static Pixmap img_help2;
	public static Pixmap img_help3;
	public static Pixmap img_help4_part1;
	public static Pixmap img_help4_part2;
	public static Pixmap img_characters_faces_game_waiting_unknown;
	public static Pixmap img_player_highlight;
	
	//the font
	public static Typeface theme_font;
	
	public static void stopAllAudio() {
		stopAudio(music_main_menu);
		stopAudio(music_high_scores);
	}
	
	public static void stopAudio(Music audioAsset){
		if (audioAsset.isPlaying()==true)
			audioAsset.stop();
	}
	
	public static void resumeAudio(Music audioAsset) {
		if (audioAsset.isPlaying()==false)
			audioAsset.play();
	}
	
	private static Random rnd = new Random();
	
	public static Sound getRandomPieceMoveSound(){
		int version = rnd.nextInt(3);
		
		switch (version) {
		case 0:
			return Assets.sound_piece_move_version1;
		case 1:
			return Assets.sound_piece_move_version2;		
		default://=2
			return Assets.sound_piece_move_version3;
		}
	}
	
	public static Sound getSoundForGameEnding (PlayerFace theWinner, PlayerFace theLooser){
		List<Sound>chooseFrom = new ArrayList<Sound>();
		
		if(theWinner==PlayerFace.HOMER)
			chooseFrom.add(Assets.sound_homer_win);
		if(theLooser==PlayerFace.HOMER)
			chooseFrom.add(Assets.sound_homer_loss);
		
		if(theWinner==PlayerFace.BART)
			chooseFrom.add(Assets.sound_bart_win);
		if(theLooser==PlayerFace.BART)
			chooseFrom.add(Assets.sound_bart_loss);
		
		if(theWinner==PlayerFace.FRINK_THE_SCIENTIST)
			chooseFrom.add(Assets.sound_frink_win);
		if(theLooser==PlayerFace.FRINK_THE_SCIENTIST)
			chooseFrom.add(Assets.sound_frink_loss);
		
		int randomIndex = rnd.nextInt(chooseFrom.size());
		//TODO: null, null -> index = 0 -> a DRAW sound
		return chooseFrom.get(randomIndex);
	}
	
}
