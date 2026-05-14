package org.springframework.samples.petclinic.api.vet;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Especialidade do veterinário")
public record SpecialtyResponse(
    @Schema(description = "Identificador da especialidade", example = "1")
    Integer id,

    @Schema(description = "Nome da especialidade", example = "radiology")
    String name
) {

    public static SpecialtyResponse from(Specialty specialty) {
        return new SpecialtyResponse(specialty.getId(), specialty.getName());
    }
}
