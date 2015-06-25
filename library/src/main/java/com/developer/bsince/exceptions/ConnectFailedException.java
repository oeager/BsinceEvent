package com.developer.bsince.exceptions;

import java.io.IOException;

public class ConnectFailedException extends IOException {
	private static final long serialVersionUID = 1L;

	public ConnectFailedException() {
	}

	public ConnectFailedException(String detailMessage) {
		super(detailMessage);
	}

	public ConnectFailedException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ConnectFailedException(Throwable throwable) {
		super(throwable);
	}
}
