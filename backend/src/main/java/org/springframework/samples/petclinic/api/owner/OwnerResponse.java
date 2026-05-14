package org.springframework.samples.petclinic.api.owner;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Dados do tutor")
public record OwnerResponse(
    @Schema(description = "Identificador do tutor", example = "1")
    Integer id,

    @Schema(description = "Primeiro nome", example = "George")
    String firstName,

    @Schema(description = "Sobrenome", example = "Franklin")
    String lastName,

    @Schema(description = "Endereço", example = "110 W. Liberty St.")
    String address,

    @Schema(description = "Cidade", example = "Madison")
    String city,

    @Schema(description = "Telefone", example = "6085551023")
    String telephone,

    @Schema(description = "Lista de pets do tutor")
    List<PetResponse> pets
) {

    public static OwnerResponse from(Owner owner) {
        List<PetResponse> petResponses = owner.getPets().stream()
            .map(PetResponse::from)
            .toList();
        return new OwnerResponse(
            owner.getId(),
            owner.getFirstName(),
            owner.getLastName(),
            owner.getAddress(),
            owner.getCity(),
            owner.getTelephone(),
            petResponses
        );
    }
}
