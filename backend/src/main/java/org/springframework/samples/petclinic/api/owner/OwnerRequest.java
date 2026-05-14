package org.springframework.samples.petclinic.api.owner;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Dados para criação ou atualização de tutor")
public record OwnerRequest(
    @Schema(description = "Primeiro nome", example = "George")
    @NotBlank(message = "O primeiro nome é obrigatório")
    String firstName,

    @Schema(description = "Sobrenome", example = "Franklin")
    @NotBlank(message = "O sobrenome é obrigatório")
    String lastName,

    @Schema(description = "Endereço completo", example = "110 W. Liberty St.")
    @NotBlank(message = "O endereço é obrigatório")
    String address,

    @Schema(description = "Cidade", example = "Madison")
    @NotBlank(message = "A cidade é obrigatória")
    String city,

    @Schema(description = "Telefone com 10 dígitos numéricos", example = "6085551023")
    @NotBlank(message = "O telefone é obrigatório")
    @Pattern(regexp = "\\d{10}", message = "O telefone deve conter exatamente 10 dígitos numéricos")
    String telephone
) {
}
