package com.croi.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatusDto {

    private Integer currentPatientCount;
    private Integer estimatedWaitMinutes;
    private String status;
}
