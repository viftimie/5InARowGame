package com.fiveInARow.game.gameWorld.players.generic;

import com.fiveInARow.game.gameWorld.interfaces.IPlayerMoveListner;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerColor;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerFace;

public abstract class GenericPlayer extends GenericPieceMover{
	protected final PlayerFace m_PlayerFace;

	public GenericPlayer(PlayerColor playerColor, PlayerFace playerFace) {
		super(playerColor);
		this.m_PlayerFace = playerFace;
	}
	
	protected final void firePlayerExited(){
		for (IPlayerMoveListner listener : getIPlayerMoveListners()) {
			listener.onPlayerExited(m_PlayerColor);
		}
	}

	public final PlayerFace getPlayerFace(){
		return this.m_PlayerFace;
	}
}
