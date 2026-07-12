package com.goodNews.genesis.core.exceptions;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.goodNews.genesis.core.exceptions.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// ========== ERROR 400 ==========
	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				"Bad Request",
				ex.getMessage(),
				LocalDateTime.now());

		return new ResponseEntity<ErrorResponse>(error, HttpStatus.BAD_REQUEST);
	}

	// ========== ERROR DE VALIDACIÓN (400) ==========
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
		String mensajeDetallado = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining(", "));

		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				"Validation Error",
				mensajeDetallado,
				LocalDateTime.now());

		return new ResponseEntity<ErrorResponse>(error, HttpStatus.BAD_REQUEST);
	}

	// ========== ERROR 404 ==========
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.NOT_FOUND.value(),
				"No Encontrado",
				ex.getMessage(),
				LocalDateTime.now());

		return new ResponseEntity<ErrorResponse>(error, HttpStatus.NOT_FOUND);
	}

	// ========== ERRORES INESPERADOS BUGS, 500 ==========
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"Internal Server Error",
				"Ocurrió un error inesperado: " + ex.getMessage(),
				LocalDateTime.now());
		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}




