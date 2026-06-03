package com.dhairya.Placement_management_system.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class FunnelDTO {
    private Map<String, Long> statusCounts;
    private long total;
}
