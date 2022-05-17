package com.fiveInARow.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Xfermode;

import com.fiveInARow.framework.AndroidPixmap;
import com.fiveInARow.framework.generics.Graphics.PixmapFormat;
import com.fiveInARow.framework.generics.Pixmap;
import com.fiveInARow.game.Assets;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerFace;

public class GraphicsUtils {
	private enum LastMoveType {SimplePieceMove, KillerMove};
	private static LastMoveType m_LastMoveType;
	
	private static Bitmap LAST_MOVE_OVERLAY;
	private static int LAST_MOVE_X = -1, LAST_MOVE_Y = -1;
	
	public static Pixmap getKillerMoveOverlay(int xRow, int yRow){
		if(xRow==LAST_MOVE_X && yRow==LAST_MOVE_Y && m_LastMoveType == LastMoveType.KillerMove)
			return new AndroidPixmap(LAST_MOVE_OVERLAY, PixmapFormat.ARGB4444);
		
		Bitmap original = Assets.img_hue.getBitmap();
		Bitmap bg = original.copy(original.getConfig(), true);
	    Canvas c = new Canvas(bg);
	    Paint paint = new Paint();
		Xfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
		paint.setXfermode(xfermode);
		
		int X, Y, RADIUS = 25;
		X = xRow*30 +6 +14;
		Y = yRow*31 +6 +14;
		
	    c.drawCircle(X, Y, RADIUS, paint);
	    
	    Pixmap result = new AndroidPixmap(bg, PixmapFormat.ARGB4444);
		
		LAST_MOVE_OVERLAY = result.getBitmap();
		LAST_MOVE_X = xRow;
		LAST_MOVE_Y = yRow;
		m_LastMoveType = LastMoveType.KillerMove;
		
		return result;
	}

//	public static void drawText(Graphics g, String line, int x, int y) {
//		int len = line.length();
//		char character;
//		int srcX;
//		int srcWidth;
//		
//		for (int i = 0; i < len; i++) {
//			character = line.charAt(i);
//			
//			if (character == ' ') {
//				x += 10;
//				continue;
//			}
//			
//			srcX = 0;
//			srcWidth = 0;
//			
//			if (character == '.') {
//				srcX = 200;
//				srcWidth = 10;
//			} else {
//				srcX = (character - '0') * 20;
//				srcWidth = 20;
//			}
//			g.drawPixmap(Assets.img_numbers, x, y, srcX, 0, srcWidth, 20);
//			x += srcWidth;
//		}
//	}
}
