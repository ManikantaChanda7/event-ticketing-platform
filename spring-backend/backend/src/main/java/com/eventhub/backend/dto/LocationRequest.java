package com.eventhub.backend.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationRequest {
    private String type;
    private List<Double> coordinates;
    private String label;
}
