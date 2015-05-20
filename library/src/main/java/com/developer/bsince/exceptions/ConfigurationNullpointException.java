package com.developer.bsince.exceptions;

public class ConfigurationNullpointException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ConfigurationNullpointException() {
	}

	public ConfigurationNullpointException(String detailMessage) {
		super(detailMessage);
	}

	public ConfigurationNullpointException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ConfigurationNullpointException(Throwable throwable) {
		super(throwable);
	}
}
