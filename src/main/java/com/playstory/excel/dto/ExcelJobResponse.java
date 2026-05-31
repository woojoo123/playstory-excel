package com.playstory.excel.dto;


import com.playstory.excel.entity.ExcelJob;
import com.playstory.excel.entity.ExcelJobStatus;

import java.time.LocalDateTime;

public record ExcelJobResponse(
        Long jobId,
        ExcelJobStatus status,
        LocalDateTime requestedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        String filePath,
        String errorMessage
) {

    public static ExcelJobResponse from(ExcelJob job) {
        return new ExcelJobResponse(
                job.getId(),
                job.getStatus(),
                job.getRequestedAt(),
                job.getStartedAt(),
                job.getCompletedAt(),
                job.getFilePath(),
                job.getErrorMessage()
        );
    }
}
