package com.fiveInARow.game.gameWorld.interfaces;

import java.util.EventListener;

import com.fiveInARow.game.gameWorld.support.BTGameEnums.TimeStatus;

public interface ITimeUpdateListener extends EventListener {
	void onTimeUpdate(TimeStatus timeStatus);
}
