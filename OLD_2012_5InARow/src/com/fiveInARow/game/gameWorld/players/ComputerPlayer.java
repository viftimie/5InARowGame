package com.fiveInARow.game.gameWorld.players;

import java.util.Random;

import android.content.ContentValues;

import com.fiveInARow.game.gameWorld.interfaces.IWorldQuerrier;
import com.fiveInARow.game.gameWorld.players.generic.GenericPlayer;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerColor;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerFace;
import com.fiveInARow.utils.Logger;

public class ComputerPlayer extends GenericPlayer {
	private static final String TAG = "ComputerPlayer";

	private static final int MIN_WAIT_TIME_THEN_MAKE_DECISION = 700;// 700
	private static final int MAX_WAIT_TIME_THEN_MAKE_DECISION = 1400;// 1400
	private static final int WAIT_TIME_OTHER_PLAYER_IS_MOVING = 200;
	private static final int WAIT_TIME_IN_PAUSED_MODE = 1000;

	private Random m_Rnd = new Random();
	private ComputerThread m_ComputerThread;
	private volatile boolean m_IsPaused = false;
	private volatile boolean m_IsRunning = true;
	private IWorldQuerrier m_IWorldQuerrier;// pentru a intreba al cui e randul

	public ComputerPlayer(PlayerColor playerColor, IWorldQuerrier iWorldQuerrier) {
		super(playerColor, PlayerFace.FRINK_THE_SCIENTIST);
		this.m_IWorldQuerrier = iWorldQuerrier;
		this.m_ComputerThread = new ComputerThread();
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

	public void start() {
		Logger.d(TAG, "start()");
		Thread t = new Thread(m_ComputerThread);
		t.setName(TAG);
		t.start();
	}

	private class ComputerThread implements Runnable {

		public void run() {
			Logger.d(TAG, "run()");

			long startDecision;
			long deltaDecision;
			while (true) {

				if (m_IsRunning == false)
					break;

				if (m_IsPaused == true) {
					Logger.d(TAG, "run() - is paused");
					try {
						Thread.sleep(WAIT_TIME_IN_PAUSED_MODE);
					} catch (InterruptedException e) {
					}
					continue;
				}

				if (m_IWorldQuerrier.getCurrentPlayerColor() == m_PlayerColor) {
					startDecision = System.currentTimeMillis();

					ContentValues cv = m_IWorldQuerrier.getOptimumMove();

					int i, j;
					i = cv.getAsInteger("x");
					j = cv.getAsInteger("y");

					deltaDecision = System.currentTimeMillis() - startDecision;

					long currentWaitTime = m_Rnd
							.nextInt(MAX_WAIT_TIME_THEN_MAKE_DECISION
									- MIN_WAIT_TIME_THEN_MAKE_DECISION)
							+ (MIN_WAIT_TIME_THEN_MAKE_DECISION);
					if (deltaDecision < currentWaitTime) {
						try {
							Thread.sleep(currentWaitTime - deltaDecision);
						} catch (InterruptedException e) {
						}
					}
					if(m_IsPaused==true)
						continue;
					
					firePieceMove(i, j);// aici se schimba turile
					Logger.d(TAG, "run() - just made a move..");
				} else {
					try {
						Thread.sleep(WAIT_TIME_OTHER_PLAYER_IS_MOVING);
					} catch (InterruptedException e) {
					}
				}
			}

			Logger.d(TAG, "run() - ending..");
		}
	}

}
