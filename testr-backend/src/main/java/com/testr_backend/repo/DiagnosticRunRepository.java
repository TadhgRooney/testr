package com.testr_backend.repo;

import com.testr_backend.model.DiagnosticRun;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosticRunRepository extends JpaRepository<DiagnosticRun, Long> {
}
