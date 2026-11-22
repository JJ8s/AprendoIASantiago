package cl.triskeledu.usuarios.controller;

import cl.triskeledu.usuarios.dto.Dtos.ErrorResponse;
import cl.triskeledu.usuarios.service.Excepciones.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Convierte las excepciones del servicio en respuestas HTTP con un mensaje claro. */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(NoEncontrado.class)
    public ResponseEntity<ErrorResponse> noEncontrado(NoEncontrado e) {
        return respuesta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(Conflicto.class)
    public ResponseEntity<ErrorResponse> conflicto(Conflicto e) {
        return respuesta(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(NoAutorizado.class)
    public ResponseEntity<ErrorResponse> noAutorizado(NoAutorizado e) {
        return respuesta(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> invalido(MethodArgumentNotValidException e) {
        String detalle = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .findFirst().orElse("Datos inválidos");
        return respuesta(HttpStatus.BAD_REQUEST, detalle);
    }

    private ResponseEntity<ErrorResponse> respuesta(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(new ErrorResponse(estado.value(), mensaje));
    }
}
