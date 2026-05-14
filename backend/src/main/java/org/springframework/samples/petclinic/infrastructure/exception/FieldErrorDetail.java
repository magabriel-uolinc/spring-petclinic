package org.springframework.samples.petclinic.infrastructure.exception;

public record FieldErrorDetail(String field, String message) {
}
