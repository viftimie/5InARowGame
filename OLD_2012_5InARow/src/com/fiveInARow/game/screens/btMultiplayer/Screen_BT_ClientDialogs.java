package com.fiveInARow.game.screens.btMultiplayer;

import android.app.Activity;
import android.content.Intent;

import com.fiveInARow.framework.generics.Graphics;
import com.fiveInARow.framework.generics.IGame;
import com.fiveInARow.framework.generics.Screen;
import com.fiveInARow.game.Assets;
import com.fiveInARow.game.screens.Screen_MainMenu;
import com.fiveInARow.game.screens.btMultiplayer.dialogs.ListOfDevicesDialog;
import com.fiveInARow.game.screens.btMultiplayer.dialogs.listeners.ICustomCancelListener;

public class Screen_BT_ClientDialogs extends Screen{	
	private ListOfDevicesDialog m_dialog;

	public Screen_BT_ClientDialogs(IGame game, Activity activity) {
		super(game, activity);
		this.init() ;
	}

	private void init() {
		//shows first dialog (aka ListOfDevices)
		this.m_Activity.runOnUiThread(new Runnable() {
			public void run() {				
				m_dialog = ListOfDevicesDialog.getListOfDevicesDialog(m_Activity, getCustomCancelListener());
				m_dialog.show();
			}
		});
	}

	@Override
	public void update(float deltaTime) {
		this.m_Game.getInput().getTouchEvents();
		this.m_Game.getInput().getKeyEvents();
		
		return;
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = this.m_Game.getGraphics();
		g.drawPixmap(Assets.img_main_game_bg, 0, 0);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
	}

	private ICustomCancelListener getCustomCancelListener(){
		return new ICustomCancelListener() {
			public void onCustomCancel() {
				m_Game.setScreen(new Screen_MainMenu(m_Game, m_Activity));
			}
		};
	}

	@Override
	public void onActivityResult_II(int requestCode, int resultCode, Intent data) {
		super.onActivityResult_II(requestCode, resultCode, data);
		
		if(this.m_dialog!=null && this.m_dialog.isShowing())
			this.m_dialog.onActivityResult_II(requestCode, resultCode, data);
	}

	@Override
	public void onLongBackPress_II() {
		// TODO: ??
	}
	
}
