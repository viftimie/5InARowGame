package com.fiveInARow.framework;

import java.io.IOException;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

import com.fiveInARow.framework.generics.Music;

public class AndroidMusic extends Music implements OnCompletionListener {
	private MediaPlayer mediaPlayer;
	private boolean isPrepared = false;
	
	public AndroidMusic(AssetFileDescriptor assetDescriptor) {
		mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setDataSource(assetDescriptor.getFileDescriptor(),
			assetDescriptor.getStartOffset(),
			assetDescriptor.getLength());
			
			mediaPlayer.prepare();
			isPrepared = true;
			
			mediaPlayer.setOnCompletionListener(this);
		} catch (Exception e) {
			isPrepared = false;
		}
	}

	public void dispose() {
		if (mediaPlayer.isPlaying())
			mediaPlayer.stop();
		mediaPlayer.release();
	}

	public boolean isLooping() {
		return mediaPlayer.isLooping();
	}

	public boolean isPlaying() {
		return mediaPlayer.isPlaying();
	}

	public boolean isStopped() {
		return !isPrepared;
	}

	public void pause() {
		if (!mediaPlayer.isPlaying())
			return;
		
		try {
			synchronized (this) {
				isPrepared=false;
				mediaPlayer.pause();
			}
		} catch (IllegalStateException ise) {
		}
	}

	public void play() {
		if (mediaPlayer.isPlaying())
			return;
		
		try {
			synchronized (this) {
				if (!isPrepared){
					mediaPlayer.prepare();
					isPrepared=true;
				}
				mediaPlayer.start();
			}
		} catch (IllegalStateException ise) {
		} catch (IOException ioe) {
		}
	}

	public void setLooping(boolean isLooping) {
		mediaPlayer.setLooping(isLooping);
	}

	public void setVolume(float volume) {
		mediaPlayer.setVolume(volume, volume);
	}

	public void stop() {
		mediaPlayer.stop();
		
		synchronized (this) {
			isPrepared = false;
		}
	}

	public void onCompletion(MediaPlayer paramMediaPlayer) {
		synchronized (this) {
			isPrepared = false;
		}
	}

}
