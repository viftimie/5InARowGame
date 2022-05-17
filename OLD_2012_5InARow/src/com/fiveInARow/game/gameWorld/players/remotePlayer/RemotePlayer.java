package com.fiveInARow.game.gameWorld.players.remotePlayer;

import com.bt.platform.BTService;
import com.bt.platform.listeners.IOutputRemote;
import com.fiveInARow.game.gameWorld.players.generic.GenericPlayer;
import com.fiveInARow.game.gameWorld.players.remotePlayer.listeners.IRemoteGameStateChanger;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.GameStateChangingAction;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerColor;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerFace;
import com.fiveInARow.utils.Logger;

public class RemotePlayer extends GenericPlayer{
	private static final String TAG = "RemotePlayer";
	
	private IRemoteGameStateChanger m_GamePauser;

	public RemotePlayer(PlayerColor playerColor, PlayerFace playerFace) {
		super(playerColor, playerFace);
		
		Logger.d(TAG, "RemotePlayer()");
		BTService.getINSTANCE().setOutputRemote(getOutputRemote());
	}
	
	public void setRemoteGamePauser(IRemoteGameStateChanger gamePauser){
		this.m_GamePauser = gamePauser;
	}
	
	private IOutputRemote getOutputRemote(){
		return new IOutputRemote() {
			private static final String TAG = "RemotePlayer - IOutputRemote";

			@Override
			public void onRemoteDeviceSent_Move_Msg(String btDeviceId, int x, int y) {
				Logger.d(TAG, "onRemoteDeviceSentNewMoveMessage()");
				firePieceMove(x, y);
			}

			@Override
			public void onRemoteDeviceSent_ChangeGameState_Msg(String btDeviceId, GameStateChangingAction newAction) {
				Logger.d(TAG, "onRemoteDeviceSentGameStateChangeAction()");
				switch (newAction) {
				case BEGIN:
					if(m_GamePauser!=null)
						m_GamePauser.onRemoteGameBegin();
					break;

				case PAUSE:
					if(m_GamePauser!=null)
						m_GamePauser.onRemoteGamePause();
					break;
					
				case RESUME:
					if(m_GamePauser!=null)
						m_GamePauser.onRemoteGameResume();
					break;
					
				case QUIT:
					if(m_GamePauser!=null)
						m_GamePauser.onRemoteGameQuit();
					break;
				}
			}

			@Override
			public void onRemoteDeviceSent_PlayAgain_Msg(PlayerColor firstPlayer) {
				if(m_GamePauser!=null)
					m_GamePauser.onRemoteGamePlayAgain(firstPlayer);
			}
		};
	}

}
