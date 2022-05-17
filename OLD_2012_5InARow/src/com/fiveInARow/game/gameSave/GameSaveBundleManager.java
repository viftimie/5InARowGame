package com.fiveInARow.game.gameSave;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.bt.utils.StreamUtils;
import com.fiveInARow.framework.AndroidFileSystemUtils;
import com.fiveInARow.game.gameSave.scoring.Score;
import com.fiveInARow.game.gameSave.support.ScoringBundle;
import com.fiveInARow.game.gameWorld.support.BTGameEnums.ScoreType;
import com.fiveInARow.utils.Logger;

//doar scrierea si citirea fisierului
public class GameSaveBundleManager {
	private static final String TAG = "GameSaveBundleManager";
	
	private static final String SAVE_BUNDLE_FILE_NAME = ".5inARow";
	private static SaveBundle m_SaveBundle;

	public static void load(AndroidFileSystemUtils files) {
		Logger.d(TAG, "load()");
		
		ObjectInputStream input = null;
		try {
			input = new ObjectInputStream(files.readFileFromInternalFolder(SAVE_BUNDLE_FILE_NAME));
			m_SaveBundle = (SaveBundle) input.readObject();
		} catch (IOException ioe) {
			Logger.e(TAG, "load()", ioe);
			m_SaveBundle = new SaveBundle();
		}  catch (Exception e) {
			Logger.e(TAG, "load()", e);
			m_SaveBundle = new SaveBundle();
		}finally {
			StreamUtils.closeQuietly(TAG, input);
		}
	}

	public static void save(AndroidFileSystemUtils files) {
		Logger.d(TAG, "save()");
		
		ObjectOutputStream output = null;
		try {
			output = new ObjectOutputStream(files.writeFileInInternalFolder(SAVE_BUNDLE_FILE_NAME));
			output.writeObject(m_SaveBundle);
		} catch (IOException ioe) {
			Logger.e(TAG, "save()", ioe);
		} finally {
			StreamUtils.closeQuietly(TAG, output);
		}
	}
	
	public static boolean isSoundEnabled(){
		return m_SaveBundle.sb_SettingsBundle.sb_SoundEnabled;
	}
	
	public static void flippSoundEnablement(){
		m_SaveBundle.sb_SettingsBundle.sb_SoundEnabled = !m_SaveBundle.sb_SettingsBundle.sb_SoundEnabled;
	}
	
	public static void addScore(int score) {	
		addScore(score, null);
	}
	
	public static void addScore(int score, String playerName) {	
		m_SaveBundle.sb_ScoringBundle.addScore(score, playerName);
	}
	
	public static void addToGamesWon(){
		m_SaveBundle.sb_ScoringBundle.sb_Record.pr_gamesWon++;
	}
	
	public static void addToGamesLost(){
		m_SaveBundle.sb_ScoringBundle.sb_Record.pr_gamesLost++;
	}
	
	public static void addToGamesDraw(){
		m_SaveBundle.sb_ScoringBundle.sb_Record.pr_gamesDraw++;
	}
	
	public static int getGamesDraw(){
		return m_SaveBundle.sb_ScoringBundle.sb_Record.pr_gamesDraw;
	}
	
	public static int getGamesLost(){
		return m_SaveBundle.sb_ScoringBundle.sb_Record.pr_gamesLost;
	}
	
	public static int getGamesWon(){
		return m_SaveBundle.sb_ScoringBundle.sb_Record.pr_gamesWon;
	}
	
	public static String[] getFormattedScores(ScoreType scoreType){
		return m_SaveBundle.sb_ScoringBundle.getFormattedScores(scoreType);
	}
	
	public static String getFormattedRecord(){
		return m_SaveBundle.sb_ScoringBundle.sb_Record.getFormattedRecord();
	}
	
	public static String getFormattedRecordForCPU(){
		return m_SaveBundle.sb_ScoringBundle.sb_Record.getFormattedRecordForCPU();
	}	
	
	//the actuall object wr
	private static class SaveBundle implements Serializable{
		private static final long serialVersionUID = 8424574508373135570L;
		private SettingsBundle sb_SettingsBundle;
		private ScoringBundle sb_ScoringBundle;
		
		public SaveBundle(){
			this.sb_SettingsBundle= new SettingsBundle();
			this.sb_ScoringBundle = new ScoringBundle();
		}
	}

	private static class SettingsBundle implements Serializable{
		private static final long serialVersionUID = 7757938331185522466L;
		
		private boolean sb_SoundEnabled;
	}

}


