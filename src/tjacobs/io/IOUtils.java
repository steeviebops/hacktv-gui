package tjacobs.io;

import java.io.*;

/**
 * IOUtils class
 * 
 * Utilities for reducing the complexity of IO operations. Reading text / binary files & streams,
 * piping between streams, creating threadable fetchers.
 * @author tjacobs01
 * 
 * Modified by Stephen McGarry to include only the methods necessary for our needs.
 */
public class IOUtils {


	/**
	 * Will auto detect files in the following formats:
	 * UTF-16
	 * UTF-8
	 * ANSI (windows-1252)
	 * otherwise, assumes ASCII
	 * 
	 * NOTE: Existence of OTHER_SYMBOL characters (classified SO in Unicode) in UTF-8
	 * files will trick this method into thinking that its using ANSI encoding. If
	 * this functionality is not desired, use the longer version of this method
	 * 
	 * @param source
	 * @return
	 * @throws IOException
	 */

	public static void copyBufs(byte src[], byte target[]) {
		int length = Math.min(src.length, target.length);
		for (int i = 0; i < length; i++) {
			target[i] = src[i];
		}		
	}        
		
	public static byte[] expandBuf(byte array[]) {
		return expandBuf(array, array.length * 2);
	}

	public static byte[] expandBuf(byte array[], int newlength) {
		byte newbuf[] = new byte[newlength];
		copyBufs(array, newbuf);
		return newbuf;
	}
	
	public static byte[] trimBuf(byte[] array, int size) {
		byte[] newbuf = new byte[size];
		for (int i = 0; i < size; i++) {
			newbuf[i] = array[i];
		}
		return newbuf;
	}
        
}
