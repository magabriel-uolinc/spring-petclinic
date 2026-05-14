package org.springframework.samples.petclinic.api.owner;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Dados de uma consulta")
public record VisitResponse(
    @Schema(description = "Identificador da consulta", example = "1")
    Integer id,

    @Schema(description = "Data da consulta", example = "2026-05-14")
    LocalDate date,

    @Schema(description = "Descrição da consulta", example = "rabies shot")
    String description
) {

    public static VisitResponse from(Visit visit) {
        return new VisitResponse(visit.getId(), visit.getDate(), visit.getDescription());
    }
}
