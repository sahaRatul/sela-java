package org.sela.exception;

public class FileException extends Exception {
	private static final long serialVersionUID = 1L;

	public FileException() {
		super();
	}

	public FileException(String message) {
		super(message);
	}

	public FileException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileException(Throwable cause) {
		super(cause);
	}
}