package com.testr_backend.web;

import com.testr_backend.model.DiagnosticRun;
import com.testr_backend.repo.DiagnosticRunRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/diagnostics")
@CrossOrigin(origins = "*")
public class DiagnosticRunController {

    private final DiagnosticRunRepository repo;

    public DiagnosticRunController(DiagnosticRunRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    public DiagnosticRun create(@RequestBody DiagnosticRunRequest body){
        DiagnosticRun run = new DiagnosticRun(
                body.deviceModel,
                body.batteryHealth,
                body.storageSpeedPct,
                body.cpuPerformancePct,
                body.ramHealthPct,
                body.displayTouchPct,
                body.cameraCheckPct
        );
        return repo.save(run);
    }
}
