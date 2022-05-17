package com.fiveInARow.utils;

import com.fiveInARow.framework.generics.Input.TouchEvent;

public class TouchAndCoordinatesUtils {
	public static final SquareArea squareArea_nextOnHelpScreens = new SquareArea(420, 260, 50, 50);
	public static final SquareArea squareArea_mainMenuHelp = new SquareArea(17, 272, 160, 40);
	public static final SquareArea squareArea_mainMenuSoundIcon = new SquareArea(10, 10, 40, 40);
	public static final SquareArea squareArea_mainMenuSinglePlayer = new SquareArea(17, 143, 174, 37);
	public static final SquareArea squareArea_mainMenuMultiPlayer = new SquareArea(17, 185, 174, 37);
	public static final SquareArea squareArea_highScoreBack = new SquareArea(10, 260, 50, 50);
	public static final SquareArea squareArea_mainMenuHighScores = new SquareArea(17, 228, 174, 37);
	public static final SquareArea squareArea_table = new SquareArea(5, 5, 304, 314);
	public static final SquareArea squareArea_gamePaused = new SquareArea(103, 147, 160, 68);
	public static final SquareArea squareArea_gameOver = new SquareArea(89, 147, 195, 68);
	
	public static final SquareArea squareArea_multiplayer_options_local_device = new SquareArea(105,113, 268, 34);
	public static final SquareArea squareArea_multiplayer_options_host_bt_game = new SquareArea(146,183, 153, 34);
	public static final SquareArea squareArea_multiplayer_options_join_bt_game = new SquareArea(146,217, 153, 34);
	
	public static final SquareArea squareArea_highscores_record = new SquareArea(441, 57, 33, 33);
	public static final SquareArea squareArea_highscores_page1 = new SquareArea(169, 247, 51, 33);
	public static final SquareArea squareArea_highscores_page2 = new SquareArea(221, 247, 33, 33);
	
	
	public static boolean inBoundOfSquareArea(TouchEvent event, SquareArea area) {
		return inBoundOfSquareArea(event, area.x, area.y, area.width, area.height);
	}
	
	private static boolean inBoundOfSquareArea(TouchEvent event, int x, int y, int width, int height) {
		if(event.x > x & event.x < x + width - 1 & event.y > y & event.y < y + height - 1)
			return true;
		else
			return false;
	}
}

class SquareArea{
	int x, y, width, height;
	public SquareArea(int x, int y, int width, int height) {
		this.x=x;
		this.y=y;
		this.width=width;
		this.height=height;
	}
}
