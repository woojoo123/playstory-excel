package com.playstory.excel.controller;

import com.playstory.excel.dto.ExcelJobResponse;
import com.playstory.excel.entity.ExcelJob;
import com.playstory.excel.service.ExcelJobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}