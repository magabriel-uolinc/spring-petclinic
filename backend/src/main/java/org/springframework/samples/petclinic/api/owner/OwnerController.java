package org.springframework.samples.petclinic.api.owner;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Owners", description = "Gerenciamento de tutores")
@RestController
@RequestMapping("/api/owners")
public class OwnerController {

    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @Operation(summary = "Lista tutores por sobrenome com paginação")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista paginada de tutores")
    })
    @GetMapping
    public PageResponse<OwnerResponse> findAll(
        @Parameter(description = "Sobrenome para filtrar (prefixo)") @RequestParam(defaultValue = "") String lastName,
        @Parameter(description = "Número da página (zero-based)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "5") int size
    ) {
        Page<Owner> owners = ownerService.findByLastName(lastName, page, size);
        Page<OwnerResponse> responses = owners.map(OwnerResponse::from);
        return PageResponse.from(responses);
    }

    @Operation(summary = "Retorna o detalhe de um tutor com seus pets e consultas")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tutor encontrado"),
        @ApiResponse(responseCode = "404", description = "Tutor não encontrado")
    })
    @GetMapping("/{ownerId}")
    public OwnerResponse findById(
        @Parameter(description = "ID do tutor") @PathVariable Integer ownerId
    ) {
        return OwnerResponse.from(ownerService.findById(ownerId));
    }

    @Operation(summary = "Cria um novo tutor")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tutor criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OwnerResponse create(@Valid @RequestBody OwnerRequest request) {
        return OwnerResponse.from(ownerService.create(request));
    }

    @Operation(summary = "Atualiza um tutor existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tutor atualizado"),
        @ApiResponse(responseCode = "404", description = "Tutor não encontrado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping("/{ownerId}")
    public OwnerResponse update(
        @Parameter(description = "ID do tutor") @PathVariable Integer ownerId,
        @Valid @RequestBody OwnerRequest request
    ) {
        return OwnerResponse.from(ownerService.update(ownerId, request));
    }
}
