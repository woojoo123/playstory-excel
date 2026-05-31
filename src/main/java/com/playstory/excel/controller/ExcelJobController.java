package com.playstory.excel.controller;

import com.playstory.excel.dto.ExcelJobResponse;
import com.playstory.excel.entity.ExcelJob;
import com.playstory.excel.service.ExcelJobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}