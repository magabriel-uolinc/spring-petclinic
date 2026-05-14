package org.springframework.samples.petclinic.api.visit;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

@Schema(description = "Dados para registro de consulta")
public record VisitRequest(
    @Schema(description = "Data da consulta", example = "2026-05-14")
    @NotNull(message = "A data da consulta é obrigatória")
    @PastOrPresent(message = "A data da consulta não pode ser futura")
    LocalDate date,

    @Schema(description = "Descrição da consulta", example = "rabies shot")
    @NotBlank(message = "A descrição da consulta é obrigatória")
    String description
) {
}
