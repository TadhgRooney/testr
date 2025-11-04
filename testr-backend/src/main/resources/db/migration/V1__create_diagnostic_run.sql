CREATE TABLE IF NOT EXISTS diagnostic_run (
                                              id UUID PRIMARY KEY,
                                              session_id TEXT NOT NULL,
                                              collected_at TIMESTAMPTZ NOT NULL,
                                              manufacturer TEXT,
                                              model TEXT,
                                              score INT,
                                              grade TEXT,
                                              payload_json JSONB NOT NULL,
                                              created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_diagnostic_run_session ON diagnostic_run (session_id);
CREATE INDEX IF NOT EXISTS idx_diagnostic_run_model ON diagnostic_run (manufacturer, model);
CREATE INDEX IF NOT EXISTS idx_diagnostic_run_created ON diagnostic_run (created_at DESC);
