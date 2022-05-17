package com.bt.utils;

import java.util.Arrays;
import java.util.Random;

import com.bt.settings.AppSettings;

public class BtUtils {

	// Concatenate the 2 or more byte arrays
	public static byte[] concatByteArrays(byte[] head, byte[]... body) {
		if (body.length == 0) {
			return head;
		}

		byte[] result = head;
		for (int i = 0; i < body.length; i++) {
			byte[] aux = new byte[result.length + body[i].length];
			System.arraycopy(result, 0, aux, 0, result.length);
			System.arraycopy(body[i], 0, aux, result.length, body[i].length);
			result = aux;
		}

		return result;
	}
	
    public static byte[] intToByteArray(int value) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }

    public static int byteArrayToInt(byte[] b) {
        if(b.length == 4)
            return b[0] << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8 | (b[3] & 0xff);
        else if(b.length == 2)
            return 0x00 << 24 | 0x00 << 16 | (b[0] & 0xff) << 8 | (b[1] & 0xff);
        return 0;
    }
    
    public static byte[] generateID(){
    	final String PREFIX = AppSettings.MY_APP_NAME;
        return BtUtils.concatByteArrays(PREFIX.getBytes(), 
        		                        generate(20-PREFIX.length()));
    }
    
    public static byte[] generateConnectKey() {//4chars
    	int from = (int) Math.pow(10, 3);
    	int to = (int) Math.pow(10, 4)-1;
    	int randomInt = new Random().nextInt(to-from)+from;
        return BtUtils.intToByteArray(randomInt);
    }
    
    public static byte[] generate(int lenght) {
        byte[] RANDOM_BYTES = new byte[lenght];
        Random r = new Random(System.currentTimeMillis());
        r.nextBytes(RANDOM_BYTES);
        return RANDOM_BYTES;
    }
    
    public static byte[] subArray(byte[] full, int from, int length){
        byte[] sub = new byte[length];
        for(int i = from; i < from + length; i++)
            sub[i-from] = full[i];
        return sub;
    }
    
    public static boolean compareByteArrays(byte[] a, byte[] b){
        if(a.length != b.length)
            return false;
        for(int i = 0; i < a.length; i++)
            if(a[i] != b[i])
                return false;
        return true;
    }
    
	public static void TODO_PRINBYTES(String tag, byte[] printMe){
		System.out.println("## TODO_PRINBYTES  "+tag+" | "+Arrays.toString(printMe));
	}
}
