package com.playstory.excel.worker;

import com.playstory.excel.service.ExcelExportService;
import com.playstory.excel.service.ExcelJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ExcelJobWorker {

    private static final Logger log =
            LoggerFactory.getLogger(ExcelJobWorker.class);

    private final ExcelJobService excelJobService;
    private final ExcelExportService excelExportService;

    public ExcelJobWorker(
            ExcelJobService excelJobService,
            ExcelExportService excelExportService
    ) {
        this.excelJobService = excelJobService;
        this.excelExportService = excelExportService;
    }

    @Scheduled(fixedDelay = 1000)
    public void processNextJob() {
        Optional<Long> jobId = excelJobService.startNextPendingJob();

        if (jobId.isEmpty()) {
            return;
        }

        try {
            String filePath = excelExportService.exportOrders(jobId.get());
            excelJobService.completeJob(jobId.get(), filePath);
        } catch (Exception exception) {
            log.error("Excel job failed. jobId={}", jobId.get(), exception);
            excelJobService.failJob(
                    jobId.get(),
                    getErrorMessage(exception)
            );
        }
    }

    private String getErrorMessage(Exception exception) {
        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            message = exception.getClass().getSimpleName();
        }

        return message.substring(0, Math.min(message.length(), 1000));
    }
}
