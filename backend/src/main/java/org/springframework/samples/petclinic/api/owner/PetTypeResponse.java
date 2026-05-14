package org.springframework.samples.petclinic.api.owner;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipo de pet")
public record PetTypeResponse(
    @Schema(description = "Identificador do tipo", example = "1")
    Integer id,

    @Schema(description = "Nome do tipo", example = "cat")
    String name
) {

    public static PetTypeResponse from(PetType petType) {
        return new PetTypeResponse(petType.getId(), petType.getName());
    }
}
