package org.springframework.samples.petclinic.api.owner;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Dados do pet")
public record PetResponse(
    @Schema(description = "Identificador do pet", example = "1")
    Integer id,

    @Schema(description = "Nome do pet", example = "Leo")
    String name,

    @Schema(description = "Data de nascimento", example = "2010-09-07")
    LocalDate birthDate,

    @Schema(description = "Tipo do pet")
    PetTypeResponse type,

    @Schema(description = "Consultas do pet")
    List<VisitResponse> visits
) {

    public static PetResponse from(Pet pet) {
        List<VisitResponse> visitResponses = pet.getVisits().stream()
            .map(VisitResponse::from)
            .toList();
        return new PetResponse(
            pet.getId(),
            pet.getName(),
            pet.getBirthDate(),
            PetTypeResponse.from(pet.getType()),
            visitResponses
        );
    }
}
