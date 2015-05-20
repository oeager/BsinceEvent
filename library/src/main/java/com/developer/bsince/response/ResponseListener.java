package com.developer.bsince.response;


public interface ResponseListener<T> {

	public void onSuccessResponse(T response);
	public void onErrorResponse(Exception error);
	
	
}
