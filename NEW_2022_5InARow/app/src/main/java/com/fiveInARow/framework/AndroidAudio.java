package com.fiveInARow.framework;

import java.io.IOException;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;

import com.fiveInARow.framework.generics.Audio;
import com.fiveInARow.framework.generics.Music;
import com.fiveInARow.framework.generics.Sound;

public class AndroidAudio extends Audio {
	private AssetManager assets;
	private SoundPool soundPool;
	
	public AndroidAudio(Activity activity) {
		activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		this.assets = activity.getAssets();
		this.soundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
	}
	
	public Music newMusic(String filename){
		try {
			AssetFileDescriptor assetDescriptor = assets.openFd(filename);
			return new AndroidMusic(assetDescriptor);
		} catch (IOException ioe) {
			throw new RuntimeException("Couldn't load Music from asset '"+ filename + "'");
		}
	}
	
	public Sound newSound(String filename){
		try {
			AssetFileDescriptor assetDescriptor = assets.openFd(filename);
			int soundId = soundPool.load(assetDescriptor, 0);
			return new AndroidSound(soundPool, soundId);
		} catch (IOException ioe) {
			throw new RuntimeException("Couldn't load Sound from asset '"+ filename + "'");
		}
	}

}
