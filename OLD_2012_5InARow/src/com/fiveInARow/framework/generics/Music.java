package com.fiveInARow.framework.generics;

public abstract class Music {
	
	public abstract void play();
	public abstract void stop();
	public abstract void pause();
	
	public abstract void setLooping(boolean looping);
	public abstract void setVolume(float volume);
	
	public abstract boolean isPlaying();
	public abstract boolean isStopped();
	public abstract boolean isLooping();
	
	public abstract void dispose();
	
}
