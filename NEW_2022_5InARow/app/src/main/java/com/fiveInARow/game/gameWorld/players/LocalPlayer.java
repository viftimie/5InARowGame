package com.fiveInARow.game.gameWorld.players;

import com.fiveInARow.framework.generics.Input.TouchEvent;
import com.fiveInARow.game.gameWorld.players.generic.GenericPlayer;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerColor;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerFace;
import com.fiveInARow.utils.Logger;
import com.fiveInARow.utils.TouchAndCoordinatesUtils;

public class LocalPlayer extends GenericPlayer{
	private static final String TAG = "LocalPlayer";

	public LocalPlayer(PlayerColor playerColor, PlayerFace playerFace) {
		super(playerColor, playerFace);
	}
	
	public void interpretTouchEvent(TouchEvent event){
		if (event.type == TouchEvent.TOUCH_UP) {
			if (TouchAndCoordinatesUtils.inBoundOfSquareArea(event, TouchAndCoordinatesUtils.squareArea_table)) {
				//tabla incepe la 5,5 + W=29+1;H=30+1 (1=dunga)
				int i,j;
				i=(event.x-5)/30;
				j=(event.y-5)/31;
				
				this.firePieceMove(i, j);
				Logger.d(TAG, " "+ m_PlayerColor +" interpretTouchEvent() - just made a move..");
				return;
			}
		}
	}

}
