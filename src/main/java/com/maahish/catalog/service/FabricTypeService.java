package com.maahish.catalog.service;

import com.maahish.catalog.dto.request.FabricTypeRequest;
import com.maahish.catalog.dto.response.FabricTypeResponse;

import java.util.List;

public interface FabricTypeService {

    List<FabricTypeResponse> getActiveFabricTypes();

    List<FabricTypeResponse> getAllFabricTypes();

    FabricTypeResponse createFabricType(FabricTypeRequest request);

    FabricTypeResponse updateFabricType(Long id, FabricTypeRequest request);

    void deleteFabricType(Long id);
}
