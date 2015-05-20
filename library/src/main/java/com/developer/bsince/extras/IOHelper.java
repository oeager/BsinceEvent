package com.developer.bsince.extras;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

public class IOHelper {

	private static final String LOG_TAG = "IOHelper";
	
	public static void silentCloseInputStream(InputStream is) {
		try {
			if (is != null) {
				is.close();
			}
		} catch (IOException e) {
			Log.w(LOG_TAG, "Cannot close input stream", e);
		}
	}

	/**
	 * A utility function to close an output stream without raising an
	 * exception.
	 * 
	 * @param os
	 *            output stream to close safely
	 */
	public static void silentCloseOutputStream(OutputStream os) {
		try {
			if (os != null) {
				os.close();
			}
		} catch (IOException e) {
			Log.w(LOG_TAG, "Cannot close output stream", e);
		}
	}
	
	public static String streamToString(InputStream is){
		if(is==null){
			return null;
		}
		byte [] buffer = new byte[10*1024];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			while(is.read(buffer)!=-1){
				baos.write(buffer);
			}
			baos.close();
			return baos.toString();
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			silentCloseInputStream(is);
		}
		return null;
	}
}
