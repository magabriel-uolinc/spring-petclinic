package org.springframework.samples.petclinic.api.pet;

import org.springframework.samples.petclinic.api.owner.Owner;
import org.springframework.samples.petclinic.api.owner.OwnerRepository;
import org.springframework.samples.petclinic.api.owner.Pet;
import org.springframework.samples.petclinic.api.owner.PetType;
import org.springframework.samples.petclinic.api.owner.PetTypeRepository;
import org.springframework.samples.petclinic.infrastructure.exception.BusinessRuleException;
import org.springframework.samples.petclinic.infrastructure.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PetService {

    private final OwnerRepository ownerRepository;
    private final PetTypeRepository petTypeRepository;

    public PetService(OwnerRepository ownerRepository, PetTypeRepository petTypeRepository) {
        this.ownerRepository = ownerRepository;
        this.petTypeRepository = petTypeRepository;
    }

    @Transactional
    public Pet create(Integer ownerId, PetRequest request) {
        Owner owner = ownerRepository.findById(ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado"));

        boolean duplicateName = owner.getPets().stream()
            .anyMatch(p -> p.getName().equalsIgnoreCase(request.name()));
        if (duplicateName) {
            throw new BusinessRuleException("Já existe um pet com o nome '" + request.name() + "' para este tutor");
        }

        PetType petType = petTypeRepository.findById(request.typeId())
            .orElseThrow(() -> new ResourceNotFoundException("Tipo de pet não encontrado"));

        Pet pet = new Pet();
        pet.setName(request.name());
        pet.setBirthDate(request.birthDate());
        pet.setType(petType);
        owner.getPets().add(pet);

        ownerRepository.save(owner);
        return pet;
    }

    @Transactional
    public Pet update(Integer ownerId, Integer petId, PetRequest request) {
        Owner owner = ownerRepository.findById(ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado"));

        Pet pet = owner.getPets().stream()
            .filter(p -> p.getId().equals(petId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado"));

        boolean duplicateName = owner.getPets().stream()
            .filter(p -> !p.getId().equals(petId))
            .anyMatch(p -> p.getName().equalsIgnoreCase(request.name()));
        if (duplicateName) {
            throw new BusinessRuleException("Já existe um pet com o nome '" + request.name() + "' para este tutor");
        }

        PetType petType = petTypeRepository.findById(request.typeId())
            .orElseThrow(() -> new ResourceNotFoundException("Tipo de pet não encontrado"));

        pet.setName(request.name());
        pet.setBirthDate(request.birthDate());
        pet.setType(petType);
        ownerRepository.save(owner);
        return pet;
    }
}
