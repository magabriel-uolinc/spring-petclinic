package org.springframework.samples.petclinic.api.pet;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

@Schema(description = "Dados para criação ou atualização de pet")
public record PetRequest(
    @Schema(description = "Nome do pet", example = "Leo")
    @NotBlank(message = "O nome do pet é obrigatório")
    String name,

    @Schema(description = "Data de nascimento (não pode ser futura)", example = "2010-09-07")
    @NotNull(message = "A data de nascimento é obrigatória")
    @Past(message = "A data de nascimento não pode ser futura")
    LocalDate birthDate,

    @Schema(description = "ID do tipo de pet", example = "1")
    @NotNull(message = "O tipo do pet é obrigatório")
    Integer typeId
) {
}
