package com.developer.bsince.request;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import android.support.annotation.IntDef;

public class CacheModel {

	private CacheModel() {
	}
	
	public static final int CACHE_FOREVER = 0x1;
	
	public static final int CACHE_NEVER = 0x2;
	
	public static final int CACHE_BY_SERVER = 0x3;


	public static boolean invalCache(@CacheEnum int mode){
		return mode==CACHE_NEVER;
	}
	
	public static boolean mustCache(@CacheEnum int mode){
		return mode ==CACHE_FOREVER;
	}

	
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({CACHE_FOREVER,CACHE_NEVER,CACHE_BY_SERVER})
	public @interface CacheEnum{
		
	}
}
