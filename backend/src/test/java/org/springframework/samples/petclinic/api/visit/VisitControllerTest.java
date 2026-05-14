package org.springframework.samples.petclinic.api.visit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.samples.petclinic.api.owner.Visit;
import org.springframework.samples.petclinic.infrastructure.exception.ResourceNotFoundException;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VisitController.class)
class VisitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VisitService visitService;

    @Test
    void create_returnsVisitWith201() throws Exception {
        VisitRequest request = new VisitRequest(LocalDate.of(2026, 5, 14), "rabies shot");
        Visit visit = new Visit();
        visit.setId(1);
        visit.setDate(LocalDate.of(2026, 5, 14));
        visit.setDescription("rabies shot");

        when(visitService.create(eq(1), eq(7), any())).thenReturn(visit);

        mockMvc.perform(post("/api/owners/1/pets/7/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.description").value("rabies shot"));
    }

    @Test
    void create_returns404WhenPetNotFound() throws Exception {
        VisitRequest request = new VisitRequest(LocalDate.of(2026, 5, 14), "rabies shot");
        when(visitService.create(eq(1), eq(99), any()))
            .thenThrow(new ResourceNotFoundException("Pet não encontrado"));

        mockMvc.perform(post("/api/owners/1/pets/99/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Pet não encontrado"));
    }

    @Test
    void create_returns400WhenInvalid() throws Exception {
        VisitRequest invalid = new VisitRequest(null, "");

        mockMvc.perform(post("/api/owners/1/pets/7/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.fieldErrors[0].field").isString())
            .andExpect(jsonPath("$.fieldErrors[0].message").isString());
    }
}
