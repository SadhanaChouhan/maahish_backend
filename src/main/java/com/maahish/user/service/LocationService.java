package com.maahish.user.service;

import com.maahish.common.exception.BadRequestException;
import com.maahish.user.location.LocationCatalog;
import com.maahish.common.dto.response.LocationOptionResponse;
import com.maahish.common.exception.ResourceNotFoundException;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final ObjectMapper objectMapper;
    private LocationCatalog catalog;

    @PostConstruct
    void loadCatalog() {
        try (InputStream input = new ClassPathResource("data/locations.json").getInputStream()) {
            catalog = objectMapper.readValue(input, LocationCatalog.class);
            log.info("Loaded location catalog with {} countries", catalog.getCountries().size());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load locations.json", ex);
        }
    }

    public List<LocationOptionResponse> getCountries() {
        return catalog.getCountries().stream()
                .map(country -> LocationOptionResponse.builder()
                        .code(country.getCode())
                        .name(country.getName())
                        .build())
                .toList();
    }

    public List<LocationOptionResponse> getStates(String countryCode) {
        return getCountry(countryCode).getStates().stream()
                .map(state -> LocationOptionResponse.builder()
                        .code(state.getCode())
                        .name(state.getName())
                        .build())
                .toList();
    }

    public List<LocationOptionResponse> getDistricts(String countryCode, String stateCode) {
        return getState(countryCode, stateCode).getDistricts().stream()
                .map(district -> LocationOptionResponse.builder()
                        .code(district.getCode())
                        .name(district.getName())
                        .build())
                .toList();
    }

    public String resolveCountryName(String countryCode) {
        return getCountry(countryCode).getName();
    }

    private LocationCatalog.CountryNode getCountry(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            throw new BadRequestException("Country code is required");
        }
        return catalog.getCountries().stream()
                .filter(country -> country.getCode().equalsIgnoreCase(countryCode))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Country not found"));
    }

    private LocationCatalog.StateNode getState(String countryCode, String stateCode) {
        return getCountry(countryCode).getStates().stream()
                .filter(state -> state.getCode().equalsIgnoreCase(stateCode))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("State not found"));
    }
}
