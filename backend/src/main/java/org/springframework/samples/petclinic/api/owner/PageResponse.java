package org.springframework.samples.petclinic.api.owner;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Página de resultados")
public record PageResponse<T>(
    @Schema(description = "Conteúdo da página")
    List<T> content,

    @Schema(description = "Número da página atual (zero-based)", example = "0")
    int page,

    @Schema(description = "Tamanho da página", example = "5")
    int size,

    @Schema(description = "Total de elementos", example = "10")
    long totalElements,

    @Schema(description = "Total de páginas", example = "2")
    int totalPages
) {

    public static <T> PageResponse<T> from(org.springframework.data.domain.Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }
}
