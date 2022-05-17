package com.fiveInARow.game.screens.btMultiplayer.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bt.BTEnums.FailedHandshakeReason;
import com.bt.platform.BTService;
import com.bt.platform.listeners.IHandshakeAndGameInitListener;
import com.bt.utils.BtUtils;
import com.fiveInARow.R;
import com.fiveInARow.framework.generics.IGame;
import com.fiveInARow.game.gameSave.scoring.Record;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerColor;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerFace;
import com.fiveInARow.game.gameWorld.support.sessionSavedData.PlayerDetails;
import com.fiveInARow.game.screens.btMultiplayer.Screen_BT_Waiting;
import com.fiveInARow.game.screens.btMultiplayer.dialogs.listeners.ICustomCancelListener;
import com.fiveInARow.utils.ConvertUtils;
import com.fiveInARow.utils.Logger;

public class InsertConnectKeyDialog extends AlertDialog {
	private static final String TAG = "InsertConnectKeyDialog";
	public static final String BT_DEVICE_KEY = "BT_DEVICE_KEY";

	private Activity m_ParentActivity;
	private EditText m_ConnectKeyEtxt;
	private BluetoothDevice m_DeviceToConnectTo;
	private ICustomCancelListener m_CustomCancelListener;

	public static InsertConnectKeyDialog getInsertConnectKeyDialog(Activity activity, BluetoothDevice deviceToConnectTo, ICustomCancelListener listener) {
		InsertConnectKeyDialog result = new InsertConnectKeyDialog(activity, deviceToConnectTo, listener);
		return result;
	}

	protected InsertConnectKeyDialog(Activity activity, BluetoothDevice deviceToConnectTo, ICustomCancelListener listener) {
		super(activity);
		this.m_ParentActivity = activity;
		this.m_CustomCancelListener = listener;
		this.m_DeviceToConnectTo = deviceToConnectTo;

		this.setupView();
	}

	private void setupView() {
		this.setCancelable(true);
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.bt_client_insert_connect_key, null);

		this.m_ConnectKeyEtxt = (EditText) view.findViewById(R.id.bt_client_insert_connect_key_etxt);
		
		// close button
		OnClickListener onCancel = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				m_CustomCancelListener.onCustomCancel();
			}
		};
		this.setButton(BUTTON_NEGATIVE, "Cancel", onCancel);
		
		// ok button
		OnClickListener onOk = new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				startService();
			}
		};
		
		this.setButton(BUTTON_POSITIVE, "Ok", onOk);
		this.setView(view, 0, 0, 0, 0);
	}
	
	private void startService(){
		BTService.setHandshakeListener(getHandshakeListener());
		
		String connectKey = m_ConnectKeyEtxt.getText().toString();
		Intent intent = new Intent(BTService.ACTION_STRING_START_AS_CLIENT);
		intent.putExtra(BTService.EXTRA_SERVER_TO_CONNECT_TO, m_DeviceToConnectTo);
		intent.putExtra(BTService.EXTRA_CONNECT_KEY, BtUtils.intToByteArray(ConvertUtils.getInt(connectKey, 0)));
		this.m_ParentActivity.startService(intent);
	}
	
	private void stopService(){
		Intent intent = new Intent(BTService.ACTION_STRING_STOP);
		this.m_ParentActivity.stopService(intent);
	}
	
	//used only by client
	private IHandshakeAndGameInitListener getHandshakeListener() {
		return new IHandshakeAndGameInitListener() {
			private int ih_gamesWon, ih_gamesDraw, ih_gamesLost;

			@Override
			public void onCorrectHandshake() {
//				m_ParentActivity.runOnUiThread(new Runnable() {
//					public void run() {
//						Toast.makeText(m_ParentActivity, "Succesfully connected to remote device!", Toast.LENGTH_LONG).show();
//					}
//				});
			}

			@Override
			public void onFailedHandshake(final FailedHandshakeReason reason) {
				stopService();
				m_CustomCancelListener.onCustomCancel();
				m_ParentActivity.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(m_ParentActivity, reason.getReason(), Toast.LENGTH_LONG).show();
					}
				});
			}
			
			@Override
			public void onReceivedRecordMessage(int gamesWon, int gamesDraw, int gamesLost) {
				this.ih_gamesWon = gamesWon;
				this.ih_gamesDraw = gamesDraw;
				this.ih_gamesLost = gamesLost;
			}
			
			//received only by client-> server is player1
			@Override
			public void onReceivedGameInitMessage(PlayerFace myFace, PlayerColor myColor, PlayerColor firstPlayer) {
				Record playerRecord = new Record(this.ih_gamesWon, this.ih_gamesDraw, this.ih_gamesLost);
				PlayerDetails playerDetails = new PlayerDetails();
				
				//rule #1) local player is allways player 1
				playerDetails.player1Face = myFace;
				playerDetails.player1Color = myColor;
				playerDetails.player2Face = (myFace==PlayerFace.BART)? PlayerFace.HOMER : PlayerFace.BART;;
				playerDetails.player2Color = myColor.flip();
				playerDetails.firstPlayer = firstPlayer;
				
				Logger.d(TAG, "CLIENT onReceivedGameInitMessage(): "+playerDetails.toString()); //TODO: remove
				
				((IGame) m_ParentActivity).setScreen(new Screen_BT_Waiting((IGame) m_ParentActivity, m_ParentActivity, playerDetails, playerRecord));
			}

			@Override
			public void onClickToBeginGame() {
				//nothing here to do: check Waiting screen
			}
		};
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.m_CustomCancelListener.onCustomCancel();
	}
	
	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) 
	    {
			this.dismiss();
	        this.m_ParentActivity.finish();
	        return true;
	    }
		return super.onKeyLongPress(keyCode, event);
	}
}
