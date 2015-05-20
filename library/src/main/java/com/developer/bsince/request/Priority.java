package com.developer.bsince.request;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import android.support.annotation.IntDef;

public interface Priority {
	public static final int LOW = 0x0;
	public static final int NORMAL = 0x1;
	public static final int HIGH = 0x2;
	public static final int IMMEDIATE = 0x3;
	
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({LOW,NORMAL,HIGH,IMMEDIATE})
	public @interface Sequence {
		
	}
}
