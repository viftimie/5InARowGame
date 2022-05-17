package com.fiveInARow.framework.generics;

import java.util.Random;

import android.app.Activity;
import android.content.Intent;

import com.fiveInARow.game.screens.interfaces.IActivityEventsListener;


public abstract class Screen implements IActivityEventsListener{

	protected final IGame m_Game;
	protected final Activity m_Activity;
	protected final static Random m_Rnd = new Random();
	
	private static long DEALAY_FOR_FIRST_TOUCH = 500; //half second
	private long m_TimeOfFirstTouch = -1;
	
	protected void useDelayForFirstTouch(){
		this.m_TimeOfFirstTouch = System.currentTimeMillis();
	}
	
	//delay on first touch (else first touch will be first move)
	protected boolean skipSinceStillInDelayForFirstTouch(){
		if(m_TimeOfFirstTouch!=-1){
			if(System.currentTimeMillis()-m_TimeOfFirstTouch>DEALAY_FOR_FIRST_TOUCH){
				m_TimeOfFirstTouch = -1;
				return false;
			} else
				return true;
		}
		return false;
	}

	public Screen(IGame game, Activity activity) {
		this.m_Game = game;
		this.m_Activity = activity;
	}

	public abstract void update(float deltaTime);

	public abstract void present(float deltaTime);

	public abstract void pause();

	public abstract void resume();

	public abstract void dispose();
	
	
	//from: IActivityEventsListener
	
	public boolean onBackPress_II(){
		return true;
	}

	public void onActivityResult_II(int requestCode, int resultCode, Intent data) {
	}
	
	@Override
	public void onLongBackPress_II() {
		// TODO: ??
	}
}
