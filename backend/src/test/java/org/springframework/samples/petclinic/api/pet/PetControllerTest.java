package org.springframework.samples.petclinic.api.pet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.samples.petclinic.api.owner.Pet;
import org.springframework.samples.petclinic.api.owner.PetType;
import org.springframework.samples.petclinic.api.owner.PetTypeRepository;
import org.springframework.samples.petclinic.infrastructure.exception.BusinessRuleException;
import org.springframework.samples.petclinic.infrastructure.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PetController.class)
class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PetService petService;

    @MockitoBean
    private PetTypeRepository petTypeRepository;

    @Test
    void create_returnsPetWith201() throws Exception {
        PetRequest request = new PetRequest("Leo", LocalDate.of(2010, 9, 7), 1);
        Pet pet = buildPet(1, "Leo", LocalDate.of(2010, 9, 7));
        when(petService.create(eq(1), any())).thenReturn(pet);

        mockMvc.perform(post("/api/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Leo"));
    }

    @Test
    void create_returns404WhenOwnerNotFound() throws Exception {
        PetRequest request = new PetRequest("Leo", LocalDate.of(2010, 9, 7), 1);
        when(petService.create(eq(99), any())).thenThrow(new ResourceNotFoundException("Tutor não encontrado"));

        mockMvc.perform(post("/api/owners/99/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Tutor não encontrado"));
    }

    @Test
    void create_returns400WhenNameDuplicated() throws Exception {
        PetRequest request = new PetRequest("Leo", LocalDate.of(2010, 9, 7), 1);
        when(petService.create(eq(1), any()))
            .thenThrow(new BusinessRuleException("Já existe um pet com o nome 'Leo' para este tutor"));

        mockMvc.perform(post("/api/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Já existe um pet com o nome 'Leo' para este tutor"));
    }

    @Test
    void create_returns400WithFieldErrorsWhenInvalid() throws Exception {
        PetRequest invalid = new PetRequest("", LocalDate.now().plusDays(1), null);

        mockMvc.perform(post("/api/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.fieldErrors").isArray())
            .andExpect(jsonPath("$.fieldErrors[0].field").isString())
            .andExpect(jsonPath("$.fieldErrors[0].message").isString());
    }

    private Pet buildPet(int id, String name, LocalDate birthDate) {
        PetType type = new PetType();
        type.setId(1);
        type.setName("cat");

        Pet pet = new Pet();
        pet.setId(id);
        pet.setName(name);
        pet.setBirthDate(birthDate);
        pet.setType(type);
        return pet;
    }
}
