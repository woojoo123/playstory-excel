package com.playstory.excel.service;

import com.playstory.excel.entity.ExcelJob;
import com.playstory.excel.entity.ExcelJobStatus;
import com.playstory.excel.repository.ExcelJobRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public Optional<Long> startNextPendingJob() {
        Optional<ExcelJob> job =
                excelJobRepository.findFirstByStatusOrderByRequestedAtAsc(
                        ExcelJobStatus.PENDING
                );

        job.ifPresent(ExcelJob::start);

        return job.map(ExcelJob::getId);
    }

    @Transactional
    public void completeJob(Long jobId, String filePath) {
        ExcelJob job = getRequiredJob(jobId);
        job.complete(filePath);
    }

    @Transactional
    public void failJob(Long jobId, String errorMessage) {
        ExcelJob job = getRequiredJob(jobId);
        job.fail(errorMessage);
    }

    private ExcelJob getRequiredJob(Long jobId) {
        return excelJobRepository.findById(jobId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Excel job not found: " + jobId
                        )
                );
    }
}
