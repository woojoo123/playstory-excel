package com.playstory.excel.service;

import com.playstory.excel.entity.ExcelJob;
import com.playstory.excel.repository.ExcelJobRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public List<ExcelJob> getJobs() {
        return excelJobRepository.findAll(
                Sort.by(Sort.Direction.DESC, "requestedAt")
        );
    }

    public Optional<ExcelJob> getJob(Long jobId) {
        return excelJobRepository.findById(jobId);
    }
}