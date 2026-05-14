package org.springframework.samples.petclinic.api.owner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.samples.petclinic.infrastructure.exception.ResourceNotFoundException;

@WebMvcTest(OwnerController.class)
class OwnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OwnerService ownerService;

    @Test
    void findAll_returnsPageWith200() throws Exception {
        Owner owner = buildOwner(1, "George", "Franklin");
        when(ownerService.findByLastName(anyString(), anyInt(), anyInt()))
            .thenReturn(new PageImpl<>(List.of(owner), PageRequest.of(0, 5), 1));

        mockMvc.perform(get("/api/owners"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].firstName").value("George"))
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void findById_returnsOwnerWith200() throws Exception {
        Owner owner = buildOwner(1, "George", "Franklin");
        when(ownerService.findById(1)).thenReturn(owner);

        mockMvc.perform(get("/api/owners/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("George"));
    }

    @Test
    void findById_returns404WhenNotFound() throws Exception {
        when(ownerService.findById(99)).thenThrow(new ResourceNotFoundException("Tutor não encontrado"));

        mockMvc.perform(get("/api/owners/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Tutor não encontrado"));
    }

    @Test
    void create_returnsCreatedOwner() throws Exception {
        OwnerRequest request = new OwnerRequest("George", "Franklin", "110 W. Liberty St.", "Madison", "6085551023");
        Owner saved = buildOwner(1, "George", "Franklin");
        when(ownerService.create(any())).thenReturn(saved);

        mockMvc.perform(post("/api/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_returns400WithFieldErrorsWhenInvalid() throws Exception {
        OwnerRequest invalid = new OwnerRequest("", "", "", "", "abc");

        mockMvc.perform(post("/api/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.fieldErrors").isArray())
            .andExpect(jsonPath("$.fieldErrors[0].field").isString())
            .andExpect(jsonPath("$.fieldErrors[0].message").isString());
    }

    private Owner buildOwner(int id, String firstName, String lastName) {
        Owner owner = new Owner();
        owner.setId(id);
        owner.setFirstName(firstName);
        owner.setLastName(lastName);
        owner.setAddress("110 W. Liberty St.");
        owner.setCity("Madison");
        owner.setTelephone("6085551023");
        return owner;
    }
}
