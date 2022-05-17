package com.fiveInARow.framework;

import android.media.SoundPool;

import com.fiveInARow.framework.generics.Sound;

public class AndroidSound extends Sound {

	private int soundId;
	private SoundPool soundPool;
	
	public AndroidSound(SoundPool soundPool, int soundId) {
		this.soundId = soundId;
		this.soundPool = soundPool;
	}
	
	public void play(float volume) {
		soundPool.play(soundId, volume, volume, 0, 0, 1);
	}
	
	public void dispose() {
		soundPool.unload(soundId);
	}
}
