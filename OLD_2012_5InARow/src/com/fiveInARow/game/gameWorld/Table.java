package com.fiveInARow.game.gameWorld;

import java.util.Random;

import android.content.ContentValues;

import com.fiveInARow.game.gameWorld.support.BTGameEnums.PlayerColor;

public class Table {
	private Random rnd = new Random();
	private int[][] pieces = new int [10][10]; //[-1,0,1]: 0 - EMPTY |-1 - BLACK|1 - WHITE
	private int piecesLeft = 100;
	
	public Table(PlayerColor players1Color, boolean player1IsComputer) {
		if(player1IsComputer==true) {
			compSimbol = players1Color.getCode();
			userSimbol = compSimbol *(-1);
		} else {
			userSimbol = players1Color.getCode();
			compSimbol = userSimbol *(-1);
		}
	}
	
	public int[][] getPieces(){
		return this.pieces;
	}
	
	//[0-9] I dont check here
	public void addPiece(int i, int j, PlayerColor playerColor){ 
		this.pieces[i][j] = playerColor.getCode();
		piecesLeft--;
	}
	
	public boolean checkAvailability (int i, int j){
		//daca indicii nu sunt corecti sau zona nu e libera
		if (i>=this.pieces[0].length || j>=this.pieces[0].length || this.pieces[i][j]!=0)
			return false;
		else 
			return true;
	}
	
	public synchronized ContentValues getRandomMove(){
		ContentValues cv = new ContentValues();
		
		if(piecesLeft>0){
			//daca adversarul sau utilizatorul curent nu muta in timpul limita -> sa se genereze o mutare Random
			int x, y;
			x = rnd.nextInt(10);
			y = rnd.nextInt(10);
			
			while(this.checkAvailability(x, y)==false){
				x = rnd.nextInt(10);
				y = rnd.nextInt(10);
			}
			
			cv.put("x", x);
			cv.put("y", y);
		} else {
			cv.put("x", -1);
			cv.put("y", -1);
		}
		
		return cv;
	}
	
	//------------------------------------------------------------------
	//-------------------------------------------------------------------------
	// Necesare pt ImplPlayerCompLevel3
	private static final int boardSize = 10;
	private int userScores[][] = new int[10][10];
	private int compScores[][] = new int[10][10];
	public static final int winningMove = 999999;// secvente critice!
	public static final int openFour = 888888;
	public static final int twoThrees = 777777;
	private boolean drawPos = false;
	
	public synchronized int externalWinningPos(int i, int j, PlayerColor playerColor) {
		return this.winningPos(i, j, playerColor.getCode());
	}
	//returneaza: -1, winningMove, openFour, twoThrees 
	//e folosita: dupa click user, mutare comp, dar si in calcul previzional logica mutare comp
	private int winningPos(int i, int j, int playerSimbol) {
		int table[][] = this.pieces;
		            
		int test3=0, test4=0;
		int L=1; //lungimea
		int m, auxM1, auxM2;
		boolean side1, side2; //pt calcul twoThrees
		
		m=1; while (j+m<boardSize  && table[i][j+m]==playerSimbol) {L++; m++;} auxM1=m;
		m=1; while (j-m>=0 && table[i][j-m]==playerSimbol) {L++; m++;} auxM2=m;   
		
		if (L>4) { 
			return winningMove; 
		}
		
		side1=(j+auxM1<boardSize && table[i][j+auxM1]==0);
		side2=(j-auxM2>=0 && table[i][j-auxM2]==0);

		if (L==4 && (side1 || side2)) test3++;
		if (side1 && side2) {
			if (L==4) test4=1;
			if (L==3) test3++;
		}

		L=1;
		m=1; while (i+m<boardSize  && table[i+m][j]==playerSimbol) {L++; m++;} auxM1=m;
		m=1; while (i-m>=0 && table[i-m][j]==playerSimbol) {L++; m++;} auxM2=m;   
		
		if (L>4) { 
			return winningMove; 
		}
		side1=(i+auxM1<boardSize && table[i+auxM1][j]==0);
		side2=(i-auxM2>=0 && table[i-auxM2][j]==0);
		if (L==4 && (side1 || side2)) test3++;
		if (side1 && side2) {
			if (L==4) test4=1;
			if (L==3) test3++;
		}

		L=1;
		m=1; while (i+m<boardSize && j+m<boardSize && table[i+m][j+m]==playerSimbol) {L++; m++;} auxM1=m;
		m=1; while (i-m>=0 && j-m>=0 && table[i-m][j-m]==playerSimbol) {L++; m++;} auxM2=m;   
		
		if (L>4) { 
			return winningMove; 
		}
		side1=(i+auxM1<boardSize && j+auxM1<boardSize && table[i+auxM1][j+auxM1]==0);
		side2=(i-auxM2>=0 && j-auxM2>=0 && table[i-auxM2][j-auxM2]==0);
		if (L==4 && (side1 || side2)) test3++;
		if (side1 && side2) {
			if (L==4) test4=1;
			if (L==3) test3++;
		}

		L=1;
		m=1; while (i+m<boardSize  && j-m>=0 && table[i+m][j-m]==playerSimbol) {L++; m++;} auxM1=m;
		m=1; while (i-m>=0 && j+m<boardSize && table[i-m][j+m]==playerSimbol) {L++; m++;} auxM2=m; 
		
		if (L>4) { 
			return winningMove; 
		}
		
		side1=(i+auxM1<boardSize && j-auxM1>=0 && table[i+auxM1][j-auxM1]==0);
		side2=(i-auxM2>=0 && j+auxM2<boardSize && table[i-auxM2][j+auxM2]==0);
		if (L==4 && (side1 || side2)) test3++;
		if (side1 && side2) {
			if (L==4) test4=1;
			if (L==3) test3++;
		}

		if (test4 != 0) return openFour;
		if (test3 >= 2) return twoThrees;
		return -1;
	}
		
	//-------------------------------------------------------------------------------
	private int userSimbol; //user  |-1 - BLACK|1 - WHITE
	private int compSimbol; //comp
	private int iMaxBuffer[] = new int[100]; //sunt tot rescrise, mai bine globale decat recreate
	private int jMaxBuffer[] = new int[100];
	private double w[] = new double[] {0,20,17,15.4,14,10};
		
    //calculeaza miscarea optima pt comp
    public synchronized ContentValues getOptimumMove() {
    	
		int nMaxAux =0;
		int maxScoreUser, maxScoreComp;
		
		maxScoreUser = evaluatePos(userScores, userSimbol);
		maxScoreComp = evaluatePos(compScores, compSimbol);

		if (maxScoreComp >= maxScoreUser) {
			maxScoreUser = -1;
			for (int i = 0; i < boardSize; i++) {
				for (int j = 0; j < boardSize; j++) {
					if (compScores[i][j] == maxScoreComp) {
						if (userScores[i][j] > maxScoreUser) {
							maxScoreUser = userScores[i][j];
							nMaxAux = 0;
						}
						if (userScores[i][j] == maxScoreUser) {
							iMaxBuffer[nMaxAux] = i;
							jMaxBuffer[nMaxAux] = j;
							nMaxAux++;
						}
					}
				}
			}
		} else {
			maxScoreComp = -1;
			for (int i = 0; i < boardSize; i++) {
				for (int j = 0; j < boardSize; j++) {
					if (userScores[i][j] == maxScoreUser) {
						if (compScores[i][j] > maxScoreComp) {
							maxScoreComp = compScores[i][j];
							nMaxAux = 0;
						}
						if (compScores[i][j] == maxScoreComp) {
							iMaxBuffer[nMaxAux] = i;
							jMaxBuffer[nMaxAux] = j;
							nMaxAux++;
						}
					}
				}
			}
		}
		
		ContentValues cv = new ContentValues();
		int randomIndex = rnd.nextInt(nMaxAux);
		cv.put("x", iMaxBuffer[randomIndex]);
		cv.put("y", jMaxBuffer[randomIndex]);
		return cv;
	}
	    
	//calculeaza un scor referitor la positia unui jucator
	//comp o foloseste in calcul mutare
	private synchronized int evaluatePos(int[][] playerScores, int playerSimbol) {
		int score=-1; //asta va fi returnat
		boolean newDrawPos=true;
		int mAux,aux1,aux2,aux3,aux4;
		int nPosAux[]=new int[5];//0-4
		int auxArray[]=new int[5];
		int minMAux,minNAux,maxMAux,maxNAux;
		int table[][] = this.pieces;

		for (int i=0;i<boardSize;i++) {
		for (int j=0;j<boardSize;j++) {
			if (table[i][j] != 0) {
				playerScores[i][j] = -1;
				continue;
			}
			if (hasNeighbors(i, j) == 0) {
				playerScores[i][j] = -1;
				continue;
			}

			int wp =winningPos(i, j, playerSimbol);
			if (wp>0) {
				playerScores[i][j]=wp;
			} else {
			    minMAux=i-4; if (minMAux<0) minMAux=0;
			    minNAux=j-4; if (minNAux<0) minNAux=0;
			    maxMAux=i+5; if (maxMAux>boardSize) maxMAux=boardSize;
			    maxNAux=j+5; if (maxNAux>boardSize) maxNAux=boardSize;

			    nPosAux[1]=1; aux1=0;
			    mAux=1; while (j+mAux<maxNAux  && table[i][j+mAux]!=-playerSimbol) {nPosAux[1]++; aux1+=w[mAux]*table[i][j+mAux]; mAux++;}
			    if (j+mAux>=boardSize || table[i][j+mAux]==-playerSimbol) aux1-=(table[i][j+mAux-1]==playerSimbol)?(w[5]*playerSimbol):0;
			    mAux=1; while (j-mAux>=minNAux && table[i][j-mAux]!=-playerSimbol) {nPosAux[1]++; aux1+=w[mAux]*table[i][j-mAux]; mAux++;}   
			    if (j-mAux<0 || table[i][j-mAux]==-playerSimbol) aux1-=(table[i][j-mAux+1]==playerSimbol)?(w[5]*playerSimbol):0;
			    if (nPosAux[1]>4) {
			    	newDrawPos=false;
			    }

			    nPosAux[2]=1; aux2=0;
			    mAux=1; while (i+mAux<maxMAux  && table[i+mAux][j]!=-playerSimbol) {nPosAux[2]++; aux2+=w[mAux]*table[i+mAux][j]; mAux++;}
			    if (i+mAux>=boardSize || table[i+mAux][j]==-playerSimbol) aux2-=(table[i+mAux-1][j]==playerSimbol)?(w[5]*playerSimbol):0;
			    mAux=1; while (i-mAux>=minMAux && table[i-mAux][j]!=-playerSimbol) {nPosAux[2]++; aux2+=w[mAux]*table[i-mAux][j]; mAux++;}   
			    if (i-mAux<0 || table[i-mAux][j]==-playerSimbol) aux2-=(table[i-mAux+1][j]==playerSimbol)?(w[5]*playerSimbol):0; 
			    if (nPosAux[2]>4) {
			    	newDrawPos=false;
			    }

			    nPosAux[3]=1; aux3=0;
			    mAux=1; while (i+mAux<maxMAux  && j+mAux<maxNAux  && table[i+mAux][j+mAux]!=-playerSimbol) {nPosAux[3]++; aux3+=w[mAux]*table[i+mAux][j+mAux]; mAux++;}
			    if (i+mAux>=boardSize || j+mAux>=boardSize || table[i+mAux][j+mAux]==-playerSimbol) aux3-=(table[i+mAux-1][j+mAux-1]==playerSimbol)?(w[5]*playerSimbol):0;
			    mAux=1; while (i-mAux>=minMAux && j-mAux>=minNAux && table[i-mAux][j-mAux]!=-playerSimbol) {nPosAux[3]++; aux3+=w[mAux]*table[i-mAux][j-mAux]; mAux++;}   
			    if (i-mAux<0 || j-mAux<0 || table[i-mAux][j-mAux]==-playerSimbol) aux3-=(table[i-mAux+1][j-mAux+1]==playerSimbol)?(w[5]*playerSimbol):0; 
			    if (nPosAux[3]>4) {
			    	newDrawPos=false;
			    }

			    nPosAux[4]=1; aux4=0;
			    mAux=1; while (i+mAux<maxMAux  && j-mAux>=minNAux && table[i+mAux][j-mAux]!=-playerSimbol) {nPosAux[4]++; aux4+=w[mAux]*table[i+mAux][j-mAux]; mAux++;}
			    if (i+mAux>=boardSize || j-mAux<0 || table[i+mAux][j-mAux]==-playerSimbol) aux4-=(table[i+mAux-1][j-mAux+1]==playerSimbol)?(w[5]*playerSimbol):0;
			    mAux=1; while (i-mAux>=minMAux && j+mAux<maxNAux  && table[i-mAux][j+mAux]!=-playerSimbol) {nPosAux[4]++; aux4+=w[mAux]*table[i-mAux][j+mAux]; mAux++;} 
			    if (i-mAux<0 || j+mAux>=boardSize || table[i-mAux][j+mAux]==-playerSimbol) aux4-=(table[i-mAux+1][j+mAux-1]==playerSimbol)?(w[5]*playerSimbol):0;
			    if (nPosAux[4]>4) {
			    	newDrawPos=false;
			    }

			    auxArray[1] = (nPosAux[1]>4) ? aux1*aux1 : 0;
			    auxArray[2] = (nPosAux[2]>4) ? aux2*aux2 : 0;
			    auxArray[3] = (nPosAux[3]>4) ? aux3*aux3 : 0;
			    auxArray[4] = (nPosAux[4]>4) ? aux4*aux4 : 0;

				aux1 = 0;
				aux2 = 0;
				for (int k = 1; k < 5; k++) {
					if (auxArray[k] >= aux1) {
						aux2 = aux1;
						aux1 = auxArray[k];
					}
				}
				playerScores[i][j] = aux1 + aux2;
				}
				if (playerScores[i][j] > score) {
					score = playerScores[i][j];
				}
			}
		}
		
		if(piecesLeft==100)//or pc is first
			newDrawPos = false;
		
		this.drawPos = newDrawPos;
		return score;
	}
		
	//e folosit de evaluatePos
	private synchronized int hasNeighbors(int i, int j) {
		int table[][] = this.pieces;
		
		if (j > 0 && table[i][j - 1] != 0)
			return 1;
		if (j + 1 < boardSize && table[i][j + 1] != 0)
			return 1;
		if (i > 0) {
			if (table[i - 1][j] != 0)
				return 1;
			if (j > 0 && table[i - 1][j - 1] != 0)
				return 1;
			if (j + 1 < boardSize && table[i - 1][j + 1] != 0)
				return 1;
		}
		if (i + 1 < boardSize) {
			if (table[i + 1][j] != 0)
				return 1;
			if (j > 0 && table[i + 1][j - 1] != 0)
				return 1;
			if (j + 1 < boardSize && table[i + 1][j + 1] != 0)
				return 1;
		}
		return 0;
	}

	public synchronized boolean isDraw(){
		return this.drawPos;
	}
}
