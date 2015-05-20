package com.developer.bsince.core.assist;

import java.io.File;
import java.io.UnsupportedEncodingException;

import com.developer.bsince.extras.Base64;

public class AuthorizationHelper {

	private final static String BASIC = "Basic ";

	public static String BASIC(String userName, String password) {
		
		String usernameAndPassword = userName + File.pathSeparatorChar + password;
		
		try {
			byte[] bytes = usernameAndPassword.getBytes("ISO-8859-1");
			return BASIC+Base64.encode(bytes);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new AssertionError();
		}
		
		
	}
	
}
