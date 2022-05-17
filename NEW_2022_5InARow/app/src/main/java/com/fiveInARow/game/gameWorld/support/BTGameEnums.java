package com.fiveInARow.game.gameWorld.support;

public class BTGameEnums {
	
	public enum GameType {
		SINGLE_LOCAL, 
		MULTI_PLAYER_LOCAL, 
		MULTI_PLAYER_BLUETOOTH
	}
	
	public enum GameEnding {
		KILLER_MOVE, 
		DRAW, 
		PLAYER_EXITED
	}
	
	public enum PlayerPopup {
		NOT_YOUR_TURN, 
		YOU_CANT_MOVE_THERE,
		THIS_IS_YOU_AND_IM_FIRST
	}
	
	public enum PlayerColor {
		BLACK(-1), 
		WHITE (1), 
		CURRENT_COLOR (9999);
		
		private PlayerColor(int code){
			this.m_Code = code;
		}
		
		private int m_Code;
		
		public PlayerColor flip(){
			if (this == BLACK)
				return WHITE;
			else
				return BLACK;
		}
		
		public int getCode(){
			return m_Code;
		}
		
		public static PlayerColor getByCode(int code){
			for(PlayerColor pc: PlayerColor.values())
				if(pc.getCode()==code)
					return pc;
			
			return null;
		}
	}
	
	//cannot use directly GameState since RUNNING can be after READY or after PAUSE, i need the actions NOT the new state
	public enum GameStateChangingAction {
		BEGIN (20), 
		PAUSE (21), 
		RESUME (22), 
		QUIT (23);
		
		private GameStateChangingAction(int code){
			this.m_Code = code;
		}
		
		private int m_Code;
		
		public int getCode(){
			return m_Code;
		}
		
		public static GameStateChangingAction getByCode(int code){
			for(GameStateChangingAction gs: GameStateChangingAction.values())
				if(gs.getCode()==code)
					return gs;
			
			return null;
		}
	}
	
	public enum GameState {
		READY, 
		RUNNING, 
		PAUSED, 
		GAMEOVER
	}
	
	public enum PlayerFace {
		BART(10), 
		HOMER(11), 
		FRINK_THE_SCIENTIST(12);
		
		private PlayerFace(int code){
			this.m_Code = code;
		}
		
		private int m_Code;
		
		public int getCode(){
			return m_Code;
		}
		
		public static PlayerFace getByCode(int code){
			for(PlayerFace pf: PlayerFace.values())
				if(pf.getCode()==code)
					return pf;
			
			return null;
		}
	}
	
	public enum TimeStatus {
		_1_DIN_6, 
		_2_DIN_6, 
		_3_DIN_6, 
		_4_DIN_6,
		_5_DIN_6_SHOW_WARNING, 
		_6_DIN_6
	}
	
	public enum ScoreType{
		WITH_CPU,
		IN_MULTIPLAYER //BT games
	}
}
