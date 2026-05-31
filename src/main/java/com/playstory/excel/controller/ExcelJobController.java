package com.playstory.excel.controller;

import com.playstory.excel.dto.ExcelJobResponse;
import com.playstory.excel.entity.ExcelJob;
import com.playstory.excel.entity.ExcelJobStatus;
import com.playstory.excel.service.ExcelJobService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/excel-jobs")
public class ExcelJobController {

    private final ExcelJobService excelJobService;

    public ExcelJobController(ExcelJobService excelJobService) {
        this.excelJobService = excelJobService;
    }

    @PostMapping
    public ResponseEntity<ExcelJobResponse> createJob() {
        ExcelJob job = excelJobService.createJob();
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ExcelJobResponse.from(job));
    }

    @GetMapping
    public List<ExcelJobResponse> getJobs() {
        List<ExcelJobResponse> responses = new ArrayList<>();

        for (ExcelJob job : excelJobService.getJobs()) {
            responses.add(ExcelJobResponse.from(job));
        }

        return responses;
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ExcelJobResponse> getJob(@PathVariable Long jobId) {
        Optional<ExcelJob> job = excelJobService.getJob(jobId);

        if (job.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(ExcelJobResponse.from(job.get()));
    }

    @GetMapping("/{jobId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long jobId) {
        Optional<ExcelJob> job = excelJobService.getJob(jobId);

        if (job.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ExcelJob excelJob = job.get();

        if (excelJob.getStatus() != ExcelJobStatus.DONE) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        if (excelJob.getFilePath() == null || excelJob.getFilePath().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = Path.of(excelJob.getFilePath());

        if (!Files.isRegularFile(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(filePath);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filePath.getFileName() + "\""
                )
                .body(resource);
    }
}
