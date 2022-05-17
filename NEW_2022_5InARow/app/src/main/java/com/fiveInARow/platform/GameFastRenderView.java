package com.fiveInARow.platform;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameFastRenderView extends SurfaceView implements Runnable {
	private GameActivity game;
	private Bitmap framebuffer;
	private Thread renderThread = null;
	private SurfaceHolder holder;
	private volatile boolean running = false;
	
	public GameFastRenderView(GameActivity game, Bitmap framebuffer) {
		super(game);
		this.game = game;
		this.framebuffer = framebuffer;
		this.holder = getHolder();
	}
	
	public void resume() {
		running = true;
		renderThread = new Thread(this);
		renderThread.setName("GameFastRenderView");
		renderThread.start();
	}
	
	public void run() {
		Rect dstRect = new Rect();
		long startTime = System.nanoTime();
		
		Canvas canvas;
		float deltaTime;
		while(running) {
			if(!holder.getSurface().isValid())
				continue;
			
			deltaTime = (System.nanoTime()-startTime) / 1000000000.0f;
			startTime = System.nanoTime();
			
			game.getCurrentScreen().update(deltaTime);
			game.getCurrentScreen().present(deltaTime);
			
			canvas = holder.lockCanvas();
			canvas.getClipBounds(dstRect);
			canvas.drawBitmap(framebuffer, null, dstRect, null);
			
			holder.unlockCanvasAndPost(canvas);
		}
	}
	
	public void pause() {
		running = false;
		while(true) {
			try {
				renderThread.join();
				break;
			} catch (InterruptedException e) {
				// retry
			}
		}
	}

}
