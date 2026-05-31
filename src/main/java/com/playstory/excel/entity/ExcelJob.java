package com.playstory.excel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "excel_jobs")
public class ExcelJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExcelJobStatus status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "error_message")
    private String errorMessage;

    protected ExcelJob() {
    }

    public static ExcelJob pending() {
        ExcelJob job = new ExcelJob();
        job.status = ExcelJobStatus.PENDING;
        job.requestedAt = LocalDateTime.now();
        return job;
    }

    public void start() {
        status = ExcelJobStatus.PROCESSING;
        startedAt = LocalDateTime.now();
    }

    public void complete(String filePath) {
        status = ExcelJobStatus.DONE;
        completedAt = LocalDateTime.now();
        this.filePath = filePath;
    }

    public void fail(String errorMessage) {
        status = ExcelJobStatus.FAILED;
        completedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    public Long getId() {
        return id;
    }

    public ExcelJobStatus getStatus() {
        return status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}