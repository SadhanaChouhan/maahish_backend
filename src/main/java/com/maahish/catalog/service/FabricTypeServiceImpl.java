package com.maahish.catalog.service;

import com.maahish.catalog.dto.request.FabricTypeRequest;
import com.maahish.catalog.dto.response.FabricTypeResponse;
import com.maahish.catalog.entity.FabricType;
import com.maahish.catalog.repository.FabricTypeRepository;
import com.maahish.common.exception.BadRequestException;
import com.maahish.common.exception.ResourceNotFoundException;
import com.maahish.common.util.SlugUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FabricTypeServiceImpl implements FabricTypeService {

    private final FabricTypeRepository fabricTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FabricTypeResponse> getActiveFabricTypes() {
        return fabricTypeRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FabricTypeResponse> getAllFabricTypes() {
        return fabricTypeRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public FabricTypeResponse createFabricType(FabricTypeRequest request) {
        if (fabricTypeRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new BadRequestException("Fabric type name already exists");
        }
        FabricType fabricType = FabricType.builder()
                .name(request.getName().trim())
                .slug(SlugUtil.toSlug(request.getName()))
                .description(request.getDescription())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
        return toResponse(fabricTypeRepository.save(fabricType));
    }

    @Override
    @Transactional
    public FabricTypeResponse updateFabricType(Long id, FabricTypeRequest request) {
        FabricType fabricType = fabricTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fabric type not found"));
        fabricType.setName(request.getName().trim());
        fabricType.setSlug(SlugUtil.toSlug(request.getName()));
        fabricType.setDescription(request.getDescription());
        if (request.getActive() != null) {
            fabricType.setActive(request.getActive());
        }
        return toResponse(fabricTypeRepository.save(fabricType));
    }

    @Override
    @Transactional
    public void deleteFabricType(Long id) {
        FabricType fabricType = fabricTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fabric type not found"));
        if (!fabricType.getProducts().isEmpty()) {
            throw new BadRequestException("Cannot delete fabric type with linked products");
        }
        fabricTypeRepository.delete(fabricType);
    }

    private FabricTypeResponse toResponse(FabricType fabricType) {
        return FabricTypeResponse.builder()
                .id(fabricType.getId())
                .name(fabricType.getName())
                .slug(fabricType.getSlug())
                .description(fabricType.getDescription())
                .active(fabricType.getActive())
                .build();
    }
}
