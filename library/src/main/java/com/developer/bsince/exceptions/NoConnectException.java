package com.developer.bsince.exceptions;

import java.io.IOException;

public class NoConnectException extends IOException {
	private static final long serialVersionUID = 1L;

	public NoConnectException() {
	}

	public NoConnectException(String detailMessage) {
		super(detailMessage);
	}

	public NoConnectException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public NoConnectException(Throwable throwable) {
		super(throwable);
	}
}
