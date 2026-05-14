package org.springframework.samples.petclinic.infrastructure.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        log.warn("Erro de validação: {}", ex.getMessage());
        List<FieldErrorDetail> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> new FieldErrorDetail(fe.getField(), resolveFieldMessage(fe)))
            .toList();
        return ErrorResponse.of("VALIDATION_ERROR", "Erro de validação nos campos informados", fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Violação de constraint: {}", ex.getMessage());
        List<FieldErrorDetail> fieldErrors = ex.getConstraintViolations().stream()
            .map(cv -> {
                String field = cv.getPropertyPath().toString();
                if (field.contains(".")) {
                    field = field.substring(field.lastIndexOf('.') + 1);
                }
                return new FieldErrorDetail(field, cv.getMessage());
            })
            .toList();
        return ErrorResponse.of("VALIDATION_ERROR", "Erro de validação nos campos informados", fieldErrors);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ResourceNotFoundException ex) {
        log.warn("Recurso não encontrado: {}", ex.getMessage());
        return ErrorResponse.of("NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(BusinessRuleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBusinessRule(BusinessRuleException ex) {
        log.warn("Regra de negócio violada: {}", ex.getMessage());
        return ErrorResponse.of("BUSINESS_RULE_ERROR", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception ex) {
        log.error("Erro inesperado: {}", ex.getMessage(), ex);
        return ErrorResponse.of("INTERNAL_ERROR", "Algum erro ocorreu, tente novamente em instantes");
    }

    private String resolveFieldMessage(FieldError fe) {
        String msg = fe.getDefaultMessage();
        return (msg != null && !msg.isBlank()) ? msg : "Campo inválido";
    }
}
