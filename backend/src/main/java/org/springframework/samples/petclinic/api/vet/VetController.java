package org.springframework.samples.petclinic.api.vet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.api.owner.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Vets", description = "Listagem de veterinários")
@RestController
@RequestMapping("/api/vets")
public class VetController {

    private final VetService vetService;

    public VetController(VetService vetService) {
        this.vetService = vetService;
    }

    @Operation(summary = "Lista veterinários com paginação")
    @ApiResponse(responseCode = "200", description = "Lista paginada de veterinários")
    @GetMapping
    public PageResponse<VetResponse> findAll(
        @Parameter(description = "Número da página (zero-based)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "5") int size
    ) {
        Page<Vet> vets = vetService.findAll(page, size);
        Page<VetResponse> responses = vets.map(VetResponse::from);
        return PageResponse.from(responses);
    }
}
