package com.playstory.excel.repository;

import com.playstory.excel.entity.ExcelJob;
import com.playstory.excel.entity.ExcelJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExcelJobRepository extends JpaRepository<ExcelJob, Long> {

    Optional<ExcelJob> findFirstByStatusOrderByRequestedAtAsc(
            ExcelJobStatus status
    );
}
