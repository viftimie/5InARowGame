package com.fiveInARow.game.screens.interfaces;

import android.content.Intent;

public interface IActivityEventsListener {

	public void onActivityResult_II(int requestCode, int resultCode, Intent data);

	public boolean onBackPress_II();
	
	public void onLongBackPress_II();

}
