package com.fiveInARow.game.gameWorld.players;

import android.content.ContentValues;

import com.fiveInARow.game.gameWorld.interfaces.ITimeUpdateListener;
import com.fiveInARow.game.gameWorld.interfaces.IWorldQuerrier;
import com.fiveInARow.game.gameWorld.players.generic.GenericPieceMover;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerColor;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.TimeStatus;
import com.fiveInARow.utils.Logger;

public class Clepsidra extends GenericPieceMover{
	private static final String TAG = "Clepsidra";

	private static final long WAIT_TIME_FOR_PLAYER_TO_MOVE = 20000; //1 move = 20 sec
	private static final long WAIT_TIME_IN_PAUSED_MODE = 300;
	
	private boolean m_IsPaused = false;
	private boolean m_IsRunning = true;
	private ITimeUpdateListener m_TimeMonitor;
	private long m_StartTime, m_PausedTime;
	private ClepsidraThread m_ClepsidraThread;
	private IWorldQuerrier m_IWorldQuerrier;

	public Clepsidra(ITimeUpdateListener timeMonitor, IWorldQuerrier iWorldQuerrier) {
		super(PlayerColor.CURRENT_COLOR);
		
		this.m_TimeMonitor = timeMonitor;
		this.m_ClepsidraThread = new ClepsidraThread();
		this.m_IWorldQuerrier = iWorldQuerrier;
	}

	public void start() {
		Thread t = new Thread(m_ClepsidraThread);
		t.setDaemon(true);
		t.setName(TAG);
		t.start();
	}

	private class ClepsidraThread implements Runnable {

		public void run() {
			Logger.d(TAG, "run()");

			m_StartTime = System.currentTimeMillis();
			TimeStatus lastTimeStatus = null;
			while (true) {
				if (m_IsRunning == false)
					break;

				if (m_IsPaused == true) {
					Logger.d(TAG, "run() - is paused");
					try {
						Thread.sleep(WAIT_TIME_IN_PAUSED_MODE);
					} catch (InterruptedException e) {
					}
					m_PausedTime+=WAIT_TIME_IN_PAUSED_MODE;
					continue;
				}

				long delta = (System.currentTimeMillis() - m_PausedTime)- m_StartTime;
				
				TimeStatus newTimeStatus = getTimeStatus(delta);
				if (newTimeStatus != lastTimeStatus) {
					m_TimeMonitor.onTimeUpdate(newTimeStatus);
					lastTimeStatus = newTimeStatus;
					
					if(newTimeStatus==TimeStatus._6_DIN_6){
						ContentValues rndMove = m_IWorldQuerrier.getRandomMove();
						int x = rndMove.getAsInteger("x");
						int y = rndMove.getAsInteger("y");
						firePieceMove(x, y);
						Logger.d(TAG, "run() - just made a move..");
						//reset(); world does reset
					}
				}

				try {
					Thread.sleep(WAIT_TIME_FOR_PLAYER_TO_MOVE/12);
				} catch (InterruptedException e) {
				}
			}

			Logger.d(TAG, "run() - ending..");
		}
	}

	private TimeStatus getTimeStatus(long delta) {
		if (delta >= WAIT_TIME_FOR_PLAYER_TO_MOVE * 6 / 6) {
			return TimeStatus._6_DIN_6;
		} else if (delta >= WAIT_TIME_FOR_PLAYER_TO_MOVE * 5 / 6) {
			return TimeStatus._5_DIN_6_SHOW_WARNING;
		} else if (delta >= WAIT_TIME_FOR_PLAYER_TO_MOVE * 4 / 6) {
			return TimeStatus._4_DIN_6;
		} else if (delta >= WAIT_TIME_FOR_PLAYER_TO_MOVE * 3 / 6) {
			return TimeStatus._3_DIN_6;
		} else if (delta >= WAIT_TIME_FOR_PLAYER_TO_MOVE * 2 / 6) {
			return TimeStatus._2_DIN_6;
		} else if (delta >= WAIT_TIME_FOR_PLAYER_TO_MOVE * 1 / 5) {
			return TimeStatus._1_DIN_6;
		} else
			return null;
	}

	public void reset() {
		Logger.d(TAG, "reset()");
		this.m_StartTime = System.currentTimeMillis();
		this.m_TimeMonitor.onTimeUpdate(null);
		this.m_PausedTime = 0;
	}

	public void kill() {
		Logger.d(TAG, "kill()");
		this.m_IsRunning = false;
	}

	public void resume() {
		Logger.d(TAG, "resume()");
		this.m_IsPaused = false;
	}

	public void pause() {
		Logger.d(TAG, "pause()");
		this.m_IsPaused = true;
	}
	
}
