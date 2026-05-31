package com.playstory.excel.service;

import com.playstory.excel.entity.ExcelJob;
import com.playstory.excel.repository.ExcelJobRepository;
import org.springframework.stereotype.Service;

@Service
public class ExcelJobService {

    private final ExcelJobRepository excelJobRepository;

    public ExcelJobService(ExcelJobRepository excelJobRepository) {
        this.excelJobRepository = excelJobRepository;
    }

    public ExcelJob createJob() {
        ExcelJob job = ExcelJob.pending();
        return excelJobRepository.save(job);
    }
}