package com.fiveInARow.game.gameWorld.support.sessionSavedData;

import java.util.Random;

import com.fiveInARow.game.gameSave.scoring.Record;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerColor;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerFace;

public class PlayerDetails {
	
	/* NOTE:
	 * #1) SINGLE PLAYER: Player1 = local user | Player2 = PC
	 * #2) MULTIPLAYE BT: Player1 = local user | Player2 = Remote
	 */
	public PlayerFace player1Face;
	public PlayerColor player1Color;
	
	public PlayerFace player2Face;
	public PlayerColor player2Color;

	//used when gameIniting
	public PlayerColor firstPlayer;
	
	//used only to save opponent record in multiplayerBluetooth
	public Record opponentRecord;
	
	public static PlayerDetails getPlayerDetailsForMultiplayer(){
		PlayerDetails result = new PlayerDetails();
		Random rnd = new Random();
		
		boolean isPlayer1Black = rnd.nextBoolean();
		if(isPlayer1Black==true){
			result.player1Color = PlayerColor.BLACK;
			result.player2Color = PlayerColor.WHITE;
		} else {
			result.player1Color = PlayerColor.WHITE;
			result.player2Color = PlayerColor.BLACK;
		}
		
		boolean isPlayer1Bart = rnd.nextBoolean();
		
		if(isPlayer1Bart==true){
			result.player1Face = PlayerFace.BART;
			result.player2Face = PlayerFace.HOMER;
		} else {
			result.player2Face = PlayerFace.BART;
			result.player1Face = PlayerFace.HOMER;
		}
		
		boolean isPlayer1First = rnd.nextBoolean();
		if(isPlayer1First==true){
			result.firstPlayer = result.player1Color;
		} else {
			result.firstPlayer = result.player2Color;
		}
		
		return result;
	}

	public static PlayerDetails getPlayerDetailsForMultiPlayerLocal(){
		return getPlayerDetailsForMultiplayer();
	}
	
	public static PlayerDetails getPlayerDetailsForSinglePlayer(){
		PlayerDetails result = new PlayerDetails();
		Random rnd = new Random();
		
		boolean isPlayer1Black = rnd.nextBoolean();
		if(isPlayer1Black==true){
			result.player1Color = PlayerColor.BLACK;
			result.player2Color = PlayerColor.WHITE;
		} else {
			result.player1Color = PlayerColor.WHITE;
			result.player2Color = PlayerColor.BLACK;
		}
		
		boolean isPlayer1Bart = rnd.nextBoolean();
		
		if(isPlayer1Bart==true){
			result.player1Face = PlayerFace.BART;
		} else {
			result.player1Face = PlayerFace.HOMER;
		}
		result.player2Face = PlayerFace.FRINK_THE_SCIENTIST;
		
		boolean isPlayer1First = rnd.nextBoolean();
		if(isPlayer1First==true){
			result.firstPlayer = result.player1Color;
		} else {
			result.firstPlayer = result.player2Color;
		}
		
		return result;
	}
	
	public void newOrder(){
		Random rnd = new Random();
		
		boolean isBlackFirst = rnd.nextBoolean();
		if(isBlackFirst==true){
			this.firstPlayer = PlayerColor.BLACK;
		} else {
			this.firstPlayer = PlayerColor.WHITE;
		}
	}
	
	@Override
	public String toString() {
		return "PlayerDetails [player1Face=" + player1Face + ", player1Color="
				+ player1Color + ", player2Face=" + player2Face
				+ ", player2Color=" + player2Color + ", firstPlayer="
				+ firstPlayer + "]";
	}

	public Record getOpponentRecord() {
		return opponentRecord;
	}

	public void setOpponentRecord(Record opponentRecord) {
		this.opponentRecord = opponentRecord;
	}
	
}
