package com.goodNews.genesis.exceptions;

public class BadRequestException extends RuntimeException {
	public BadRequestException(String mensaje) {
		super(mensaje);
	}

}
