package com.fiveInARow.game.gameWorld.interfaces;

import java.util.EventListener;

import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerColor;

public interface IPlayerMoveListner extends EventListener{
	public abstract void onNewPieceMove(PlayerColor playerColor, int x, int y);
	public abstract void onPlayerExited(PlayerColor playerColor);
}
