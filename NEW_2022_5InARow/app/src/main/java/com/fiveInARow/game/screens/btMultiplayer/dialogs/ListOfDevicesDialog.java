package com.fiveInARow.game.screens.btMultiplayer.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fiveInARow.R;
import com.fiveInARow.game.screens.btMultiplayer.dialogs.listeners.ICustomCancelListener;
import com.fiveInARow.game.screens.interfaces.IActivityEventsListener;
import com.fiveInARow.platform.GameActivity;
import com.fiveInARow.utils.Logger;

public class ListOfDevicesDialog extends AlertDialog implements IActivityEventsListener{
	private static final String TAG = "ListOfDevicesDialog";
	
	//requst codes
	private static final int REQUEST_CODE_ENABLE_BT_DISCOVERABILITY = 1821;
	private static final int REQUEST_CODE_ENABLE_BT = 1845;//TODO: not used
	private static final int STOP_SCANNING_AFTER = 30000; //30 sec
	
	private Activity m_ParentActivity;
	private ListView m_ListOfDevicesListView;
	private ProgressBar m_LoadingProgBar;
	private BluetoothAdapter m_BtAdapter;
	private ListOfDevicesWorker m_ListOfDevicesWorker;
	private ListOfDevicesBCR m_ListOfDevicesBCR;
	private Timer m_StopSearchingTimer;
	private ICustomCancelListener m_CustomCancelListener;
	
	public static ListOfDevicesDialog getListOfDevicesDialog(Activity activity, ICustomCancelListener listener){
		ListOfDevicesDialog result = new ListOfDevicesDialog(activity, listener);
		return result;
	}

	protected ListOfDevicesDialog(Activity activity, ICustomCancelListener listener) {
		super(activity);
		this.m_CustomCancelListener = listener;
		this.m_ParentActivity = activity;
		
		this.setupView();
	}

	private void setupView() {
		this.setCancelable(true);
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.bt_client_list_of_devices_list, null);

		// setup views
		this.setupListOfDevices(view);
		
		// close button
		OnClickListener onCancel = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				m_CustomCancelListener.onCustomCancel();
			}
		};
		this.setButton(BUTTON_NEGATIVE, "Cancel", onCancel);
		this.setView(view, 0, 0, 0, 0);
	}

	private void setupListOfDevices(View view) {
		
		// get ids
		this.m_ListOfDevicesListView = (ListView) view.findViewById(R.id.bt_client_list_of_devices_list_listview_id);
		this.m_LoadingProgBar = (ProgressBar) view.findViewById(R.id.bt_client_list_of_devices_list_progressbar_id);

		// LONG CLICK!
		this.m_ListOfDevicesListView.setOnItemLongClickListener(new OnItemLongClickListener() {
					public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int index, long indexToo) {
						BluetoothDevice deviceToConnectTo = ((BTClientWaitingCustomListAdapter) m_ListOfDevicesListView.getAdapter()).getItem(index).bdw_Device;
						InsertConnectKeyDialog dialog = InsertConnectKeyDialog.getInsertConnectKeyDialog((GameActivity) m_ParentActivity, deviceToConnectTo, m_CustomCancelListener);
						ListOfDevicesDialog.this.dismiss();
						dialog.show();
						return false;
					}
				});

		this.m_BtAdapter = BluetoothAdapter.getDefaultAdapter();
		if (this.m_BtAdapter == null) {
			Toast.makeText(getContext(),"Device doesn't seem to have a Bluetooth adapter! ", Toast.LENGTH_LONG);
			return;
		}

		if (!this.m_BtAdapter.isEnabled()) {
			enableBluetoothDiscoverability();
			return;
		}

		//this fills list of devices
		if(this.m_ListOfDevicesWorker!=null && m_ListOfDevicesWorker.isCancelled()==false){
			this.m_ListOfDevicesWorker.cancel(true);
			this.m_ListOfDevicesWorker = null;
		}
		
		this.m_ListOfDevicesWorker = new ListOfDevicesWorker();
		this.m_ListOfDevicesWorker.execute();
	}
	
	private class ListOfDevicesWorker extends AsyncTask<Void, Void, List<BluetoothDeviceWrapper>> {
		private static final String TAG = "ListOfDevicesWorker";
		
		@Override
		protected List<BluetoothDeviceWrapper> doInBackground(Void... param) {
			Logger.d(TAG, "doInBackground()");
			resetStopSearchingTimer();
			
			List<BluetoothDeviceWrapper> devices = new ArrayList<BluetoothDeviceWrapper>();
			this.addPairedDevices(devices);
			this.startSearchForOnlineDevices();//asyncronus
			
			return devices;
		}
		
		private void addPairedDevices(List<BluetoothDeviceWrapper> devices) {
			Logger.d(TAG, "addPairedDevices()");
			
			Set<BluetoothDevice> pairedDevices = m_BtAdapter.getBondedDevices();
			for(BluetoothDevice b:pairedDevices){
				devices.add(new BluetoothDeviceWrapper(b, false));
			}
		}

		private void startSearchForOnlineDevices () {
			Logger.d(TAG, "startSearchForOnlineDevices()");
			
			m_ListOfDevicesBCR = new ListOfDevicesBCR();
			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			m_ParentActivity.registerReceiver(m_ListOfDevicesBCR, filter); 
			
			// Don't forget to unregister during onDestroy
			m_BtAdapter.startDiscovery();
		}

		@Override
		protected void onPostExecute(List<BluetoothDeviceWrapper> devices) {
			m_ListOfDevicesListView.setAdapter(new BTClientWaitingCustomListAdapter(devices));
		}
	}

	private class BluetoothDeviceWrapper{
		public BluetoothDevice bdw_Device;
		public boolean bdw_IsOnline;
		
		public BluetoothDeviceWrapper (BluetoothDevice device, boolean isOnline){
			this.bdw_Device=device;
			this.bdw_IsOnline=isOnline;
		}
	}
	
	private class ListOfDevicesBCR extends BroadcastReceiver {
		private static final String TAG = "ListOfDevicesBCR";
		
		@Override
		public void onReceive(Context paramContext, Intent intent) {
			Logger.d(TAG, "onReceive()");
			
			String action = intent.getAction();
			
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				((BTClientWaitingCustomListAdapter)m_ListOfDevicesListView.getAdapter()).add(new BluetoothDeviceWrapper(device, true));
				((BTClientWaitingCustomListAdapter)m_ListOfDevicesListView.getAdapter()).notifyDataSetChanged();
			}
		}
	}
	
	private class ViewHolder {
		public TextView vh_name_textview;
		public TextView vh_mac_textview;
	}
	
	private class BTClientWaitingCustomListAdapter extends ArrayAdapter<BluetoothDeviceWrapper> {
		private LayoutInflater btc_LayoutInflater;
		
		BTClientWaitingCustomListAdapter(List <BluetoothDeviceWrapper> foundDevices) {
			super(m_ParentActivity, R.layout.bt_client_list_of_devices_list, foundDevices);
			this.btc_LayoutInflater = m_ParentActivity.getLayoutInflater();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;// e clasa externa
			
			if (convertView == null) {
				convertView = this.btc_LayoutInflater.inflate(R.layout.bt_client_list_of_devices_list_item, parent, false);
				holder = new ViewHolder();
				
				holder.vh_name_textview = (TextView) convertView.findViewById(R.id.bt_client_list_of_devices_list_item_textview_for_name_id);
				holder.vh_mac_textview = (TextView) convertView.findViewById(R.id.bt_client_list_of_devices_list_item_textview_for_mac_id);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			BluetoothDeviceWrapper currentItem = this.getItem(position);
			String name = currentItem.bdw_Device.getName();
			if(currentItem.bdw_IsOnline==true)
				name = name + " (online)";
			String MAC = currentItem.bdw_Device.getAddress();
			
			holder.vh_name_textview.setText(name);
			holder.vh_mac_textview.setText(MAC);
			return convertView;
		}

		@Override
		public void add(BluetoothDeviceWrapper object) {
			//Update if exist
			for(int i=0;i<this.getCount();i++){
				if(getItem(i).bdw_Device.getAddress().equals(object.bdw_Device.getAddress())
						&& getItem(i).bdw_Device.getName().equals(object.bdw_Device.getName())) {

					getItem(i).bdw_IsOnline = object.bdw_IsOnline;
					return;
				}		
			}
			
			super.add(object);
		}
	}

	private void enableBluetoothDiscoverability() {
		Logger.d(TAG, "enableBluetoothDiscoverability()");
		
		// Note: If Bluetooth has not been enabled on the device, then enabling device discoverability will automatically enable Bluetooth.
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		this.m_ParentActivity.startActivityForResult(discoverableIntent, REQUEST_CODE_ENABLE_BT_DISCOVERABILITY);
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		
		this.closeQuietly();
	}
	
	private void closeQuietly(){
		//cancel discovery
		if(this.m_BtAdapter!=null){
			if(this.m_BtAdapter.isDiscovering())
				this.m_BtAdapter.cancelDiscovery();
		}
		
		//unregister receiver
		if(this.m_ListOfDevicesBCR!=null){
			try {
				this.m_ParentActivity.unregisterReceiver(this.m_ListOfDevicesBCR);
			} catch (IllegalArgumentException iae) {
			}
		}
		
		//cancel timer
		if(this.m_StopSearchingTimer!=null) {
			this.m_StopSearchingTimer.cancel();
			this.m_StopSearchingTimer = null;
		}
	}
	
	private void resetStopSearchingTimer(){
		// cancel current timer
		if (this.m_StopSearchingTimer != null) {
			this.m_StopSearchingTimer.cancel();
			this.m_StopSearchingTimer = null;
    	}
		
		// setup again
		this.m_ParentActivity.runOnUiThread(new Runnable() {
			public void run() {
				m_LoadingProgBar.setVisibility(View.VISIBLE);
			}
		});
		
		this.m_StopSearchingTimer = new Timer();
		this.m_StopSearchingTimer.schedule(new TimerTask(){
			private static final String TAG = "StopSearchingTimer";
			
			@Override
			public void run() {
				Logger.d(TAG, "run()");
				
				m_ParentActivity.runOnUiThread(new Runnable() {
					public void run() {
						m_LoadingProgBar.setVisibility(View.INVISIBLE);
					}
				});
				
				closeQuietly();
			}}, STOP_SCANNING_AFTER);
		//scheduleAtFixedRate(new TimerTask(){ public void run() {counter++;}}, 0, 1000L);
	}
	
	// FROM: IActivityEventsListener
	public void onActivityResult_II(int requestCode, int resultCode, Intent data) {
		Logger.d(TAG, "onActivityResult()");

		if (resultCode != Activity.RESULT_CANCELED) {
			switch (requestCode) {
			case REQUEST_CODE_ENABLE_BT:
				//TODO: nothing yet
				break;
			case REQUEST_CODE_ENABLE_BT_DISCOVERABILITY:
				new ListOfDevicesWorker().execute();
				break;

			default:
				break;
			}
		}
	}

	public boolean onBackPress_II() {
		//intro: onBackPressed()
		this.m_CustomCancelListener.onCustomCancel();
		return false;
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) 
	    {
			this.onLongBackPress_II();
	        return true;
	    }
		return super.onKeyLongPress(keyCode, event);
	}

	@Override
	public void onLongBackPress_II() {
		//intro: onKeyLongPress()
		this.dismiss();
        this.m_ParentActivity.finish();
	}
}
