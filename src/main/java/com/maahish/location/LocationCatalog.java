package com.maahish.location;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationCatalog {

    private List<CountryNode> countries = new ArrayList<>();

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CountryNode {
        private String code;
        private String name;
        private List<StateNode> states = new ArrayList<>();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StateNode {
        private String code;
        private String name;
        private List<DistrictNode> districts = new ArrayList<>();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DistrictNode {
        private String code;
        private String name;
        private List<String> cities = new ArrayList<>();
    }
}
