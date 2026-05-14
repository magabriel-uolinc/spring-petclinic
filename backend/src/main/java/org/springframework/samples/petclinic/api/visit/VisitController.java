package org.springframework.samples.petclinic.api.visit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.api.owner.Visit;
import org.springframework.samples.petclinic.api.owner.VisitResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Visits", description = "Registro de consultas de pets")
@RestController
@RequestMapping("/api/owners/{ownerId}/pets/{petId}/visits")
public class VisitController {

    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @Operation(summary = "Registra uma nova consulta para um pet")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Consulta registrada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Tutor ou pet não encontrado")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VisitResponse create(
        @Parameter(description = "ID do tutor") @PathVariable Integer ownerId,
        @Parameter(description = "ID do pet") @PathVariable Integer petId,
        @Valid @RequestBody VisitRequest request
    ) {
        Visit visit = visitService.create(ownerId, petId, request);
        return VisitResponse.from(visit);
    }
}
