package com.developer.bsince.core;

import java.io.IOException;

import com.developer.bsince.core.assist.AuthorResponse;


/**
 * it is not complete
 * @author oeager
 *
 */
public interface Authenticator {

	String authenticate(AuthorResponse response) throws IOException;
	
	String authenticateProxy(AuthorResponse response) throws IOException;
	
}
