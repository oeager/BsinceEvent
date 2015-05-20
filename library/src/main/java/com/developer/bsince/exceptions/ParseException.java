package com.developer.bsince.exceptions;

public class ParseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ParseException() {
	}

	public ParseException(String detailMessage) {
		super(detailMessage);
	}

	public ParseException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ParseException(Throwable throwable) {
		super(throwable);
	}
}
