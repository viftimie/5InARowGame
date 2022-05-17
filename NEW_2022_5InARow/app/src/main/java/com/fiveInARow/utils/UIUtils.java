package com.fiveInARow.utils;

import android.graphics.Paint;
import android.graphics.Paint.Align;

import com.fiveInARow.game.Assets;
import com.fiveInARow.platform.GameApplication;

public class UIUtils {

	public static final Paint getPaintForTextDrawing(int fontSize, Align textAlignement, int colorId){
		Paint paint = new Paint();
		paint.setTextSize(fontSize);
		paint.setTextAlign(textAlignement);
		paint.setColor(GameApplication.INSTANCE.getResources().getColor(colorId));
		paint.setTypeface(Assets.theme_font);
		return paint;
	}
}
