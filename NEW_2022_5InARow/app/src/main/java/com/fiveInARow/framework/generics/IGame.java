package com.fiveInARow.framework.generics;

import com.fiveInARow.framework.AndroidFileSystemUtils;

public interface IGame {
	
	public Input getInput();
	public AndroidFileSystemUtils getFileIO();
	public Graphics getGraphics();
	public Audio getAudio();
	public void setScreen(Screen screen);
	public Screen getCurrentScreen();
	public Screen getStartScreen();
	
}