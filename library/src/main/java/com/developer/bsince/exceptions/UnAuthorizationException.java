package com.developer.bsince.exceptions;

import java.io.IOException;

public class UnAuthorizationException extends IOException {
	private static final long serialVersionUID = 1L;

	public UnAuthorizationException() {
	}

	public UnAuthorizationException(String detailMessage) {
		super(detailMessage);
	}

	public UnAuthorizationException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public UnAuthorizationException(Throwable throwable) {
		super(throwable);
	}
}
