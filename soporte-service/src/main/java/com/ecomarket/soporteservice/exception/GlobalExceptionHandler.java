package com.ecomarket.soporteservice.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.ecomarket.soporteservice.dto.ErrorResponseDTO;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // CAPTURA ERRORES DE CAPA DE SERVICIO (REGLAS DE NEGOCIO)
    @ExceptionHandler(YaExisteEnBdException.class)
    public ResponseEntity<ErrorResponseDTO> handleYaExisteEnDBException(YaExisteEnBdException ex, HttpServletRequest request) {
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.CONFLICT.value()).error(HttpStatus.CONFLICT.getReasonPhrase())
        .message(ex.getMessage())
        .path(request.getRequestURI())
        .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(NoExisteEnBdException.class)
    public ResponseEntity<ErrorResponseDTO> handleNoExisteEnDBException(NoExisteEnBdException ex, HttpServletRequest request) {
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.NOT_FOUND.value()).error(HttpStatus.NOT_FOUND.getReasonPhrase())
        .message(ex.getMessage())
        .path(request.getRequestURI())
        .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(PedidoClienteIncompatibleException.class)
    public ResponseEntity<ErrorResponseDTO> handlePedidoClienteIncompatibleException(PedidoClienteIncompatibleException ex, HttpServletRequest request) {
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.CONFLICT.value()).error(HttpStatus.CONFLICT.getReasonPhrase())
        .message(ex.getMessage())
        .path(request.getRequestURI())
        .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    // CAPTURA ERRORES DE VALIDACION (SIRVE PARA MOSTRARLE LOS ERRORES AL CLIENTE)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {

        // Creamos el HashMap como sugeriste
        Map<String, String> errores = new HashMap<>();
        
        // Iteramos sobre todos los campos que fallaron la validación
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String nombreCampo = ((FieldError) error).getField();
            String mensaje = error.getDefaultMessage();
            errores.put(nombreCampo, mensaje);
        });

        // Armamos la respuesta incluyendo el HashMap
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value()) // 400
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("La validación de los datos ha fallado. Revisa los detalles.") // Mensaje general
                .path(request.getRequestURI())
                .details(errores) // Inyectamos el HashMap aquí
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // CAPTURA ERRORES QUE COMPROMETAN LA INTEGRIDAD DE LA BASE DE DATOS
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDatabaseExceptions(DataIntegrityViolationException ex, HttpServletRequest request) {
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.CONFLICT.value())
        .error("Database Conflict")
        .message("Error de integridad en la base de datos. Es posible que el registro ya exista o un dato sea inválido.")
        .path(request.getRequestURI())
        .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    // CAPTURA ERRORES DE JSON MAL FORMADO O PARAMETROS INVALIDOS
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.BAD_REQUEST.value())
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .message("El cuerpo de la solicitud contiene JSON mal formado o tipos de datos incorrectos.")
        .path(request.getRequestURI())
        .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissingParams(MissingServletRequestParameterException ex, HttpServletRequest request) {
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.BAD_REQUEST.value())
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .message("El parametro requerido '" + ex.getParameterName() + "' no fue proporcionado.")
        .path(request.getRequestURI())
        .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.BAD_REQUEST.value())
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .message("El parametro '" + ex.getParameter().getParameterName() + "' debe ser de tipo " + ex.getRequiredType().getSimpleName() + ".")
        .path(request.getRequestURI())
        .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // ERRORES CON FORMATO DETALLADO PARA LOS DESARROLLADORES
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleNoExistInDbException(Exception ex, HttpServletRequest request) {
        
        log.error("Error crítico no controlado en la ruta: {}", request.getRequestURI(), ex);

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        .message("Ocurrio un error inesperado en el servidor. Si esto persiste contacte con administracion.")
        .path(request.getRequestURI())
        .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }   
}
    
