package org.springframework.samples.petclinic.api.pet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.samples.petclinic.api.owner.Pet;
import org.springframework.samples.petclinic.api.owner.PetResponse;
import org.springframework.samples.petclinic.api.owner.PetType;
import org.springframework.samples.petclinic.api.owner.PetTypeRepository;
import org.springframework.samples.petclinic.api.owner.PetTypeResponse;

import java.util.List;

@Tag(name = "Pets", description = "Gerenciamento de pets e tipos de pet")
@RestController
public class PetController {

    private final PetService petService;
    private final PetTypeRepository petTypeRepository;

    public PetController(PetService petService, PetTypeRepository petTypeRepository) {
        this.petService = petService;
        this.petTypeRepository = petTypeRepository;
    }

    @Operation(summary = "Lista todos os tipos de pet ordenados por nome")
    @ApiResponse(responseCode = "200", description = "Lista de tipos de pet")
    @GetMapping("/api/pet-types")
    public List<PetTypeResponse> findAllPetTypes() {
        return petTypeRepository.findAll(org.springframework.data.domain.Sort.by("name")).stream()
            .map(PetTypeResponse::from)
            .toList();
    }

    @Operation(summary = "Cria um novo pet para um tutor")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pet criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou nome duplicado"),
        @ApiResponse(responseCode = "404", description = "Tutor não encontrado")
    })
    @PostMapping("/api/owners/{ownerId}/pets")
    @ResponseStatus(HttpStatus.CREATED)
    public PetResponse create(
        @Parameter(description = "ID do tutor") @PathVariable Integer ownerId,
        @Valid @RequestBody PetRequest request
    ) {
        Pet pet = petService.create(ownerId, request);
        return PetResponse.from(pet);
    }

    @Operation(summary = "Atualiza um pet existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pet atualizado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou nome duplicado"),
        @ApiResponse(responseCode = "404", description = "Tutor ou pet não encontrado")
    })
    @PutMapping("/api/owners/{ownerId}/pets/{petId}")
    public PetResponse update(
        @Parameter(description = "ID do tutor") @PathVariable Integer ownerId,
        @Parameter(description = "ID do pet") @PathVariable Integer petId,
        @Valid @RequestBody PetRequest request
    ) {
        Pet pet = petService.update(ownerId, petId, request);
        return PetResponse.from(pet);
    }
}
