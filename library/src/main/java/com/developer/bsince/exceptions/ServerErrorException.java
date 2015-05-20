package com.developer.bsince.exceptions;

import java.io.IOException;

public class ServerErrorException extends IOException {

	private static final long serialVersionUID = 1L;

	public ServerErrorException() {
	}

	public ServerErrorException(String detailMessage) {
		super(detailMessage);
	}

	public ServerErrorException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ServerErrorException(Throwable throwable) {
		super(throwable);
	}
}
