package org.springframework.samples.petclinic.api.vet;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Dados do veterinário")
public record VetResponse(
    @Schema(description = "Identificador do veterinário", example = "1")
    Integer id,

    @Schema(description = "Primeiro nome", example = "James")
    String firstName,

    @Schema(description = "Sobrenome", example = "Carter")
    String lastName,

    @Schema(description = "Especialidades do veterinário")
    List<SpecialtyResponse> specialties
) {

    public static VetResponse from(Vet vet) {
        List<SpecialtyResponse> specialties = vet.getSpecialties().stream()
            .map(SpecialtyResponse::from)
            .toList();
        return new VetResponse(vet.getId(), vet.getFirstName(), vet.getLastName(), specialties);
    }
}
