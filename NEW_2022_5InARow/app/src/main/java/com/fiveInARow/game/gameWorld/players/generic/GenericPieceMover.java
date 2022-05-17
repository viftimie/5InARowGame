package com.fiveInARow.game.gameWorld.players.generic;

import com.fiveInARow.game.gameWorld.interfaces.IPlayerMoveListner;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerColor;
import com.fiveInARow.utils.EventListenerList;

public abstract class GenericPieceMover {
	private final EventListenerList m_Listeners;
	protected final PlayerColor m_PlayerColor;

	public GenericPieceMover(PlayerColor playerColor) {
		this.m_Listeners = new EventListenerList();
		this.m_PlayerColor = playerColor;
	}
	
	public final void addPlayerMoveListner(IPlayerMoveListner playerMoveListner){
		this.m_Listeners.add(IPlayerMoveListner.class, playerMoveListner);
	}
	
	public final IPlayerMoveListner[] getIPlayerMoveListners() {
		return this.m_Listeners.getListeners(IPlayerMoveListner.class);
	}
	
	protected final void firePieceMove(int x, int y){
		for (IPlayerMoveListner listener : this.getIPlayerMoveListners()) {
			listener.onNewPieceMove(this.m_PlayerColor, x, y);
		}
	}
	
	public final PlayerColor getPlayerColor(){
		return this.m_PlayerColor;
	}
}
