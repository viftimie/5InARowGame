package com.bt;


public class BTEnums {
	
	public enum MessageType {	
		//Must have unique codes.. just increment by 1
		HAND_SHAKE(101),
		WRONG_CONNECT_KEY(102),
		RECORD(103),
		KILL_CONNECTION(104),
		KEEP_ALIVE(105),
		CHANGE_GAME_STATE(106),
		NEW_MOVE(107),
		GAME_INIT(108),
		PLAY_AGAIN (109),
		OBJECT_CARGO_HOLDER(110);
		
		private final int m_Code;

		MessageType(int code) {
			this.m_Code = code;
		}

		public int getCode() {
			return this.m_Code;
		}

		public static MessageType getByCode(int code) {
			if (code <HAND_SHAKE.getCode() && code>HAND_SHAKE.getCode()+MessageType.values().length) {
				return null;
			}

			for (MessageType type : MessageType.values()) {
				if (type.m_Code == code) {
					return type;
				}
			}

			return null;
		}
	}

	public enum BTConnectionState {
		INITIALISING,   //doar in initConnection()
		WAIT_HS,        //dupa init -> recieve HS
		WAIT_REPLAY,    //doar pt Msg cu Replay
		COMUNICATING,   //dupa HS in general
		ENDING          //about to close
	}

	public enum FailedHandshakeReason {
		WRONG_CONNECT_KEY_ON_CLIENT ("Connect key was not accepted.."),
		WRONG_CONNECT_KEY_ON_SERVER ("Wrong connect key.."), 
		SOCKET_REASONS ("Failed to connect..");
		
		private final String m_Reason;

		private FailedHandshakeReason(String reason) {
			this.m_Reason = reason;
		}

		public String getReason() {
			return this.m_Reason;
		}
	}
}
