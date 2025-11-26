import React, { useState, useEffect } from 'react';

function DiagnosticsDashboard() {
  const [runs, setRuns] = useState([]);
  const [searchModel, setSearchModel] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Fetch diagnostics from Spring Boot when the component mounts
  useEffect(() => {
    fetch('http://localhost:8080/v1/diagnostics')
      .then((res) => {
        if (!res.ok) {
          throw new Error('Failed to fetch diagnostics');
        }
        return res.json();
      })
      .then((data) => {
        setRuns(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error(err);
        setError(err.message);
        setLoading(false);
      });
  }, []); 

  const filteredRuns = runs.filter((run) =>
    run.deviceModel?.toLowerCase().includes(searchModel.toLowerCase())
  );

  const totalDevices = runs.length;

  const avgBattery =
    runs.length > 0
      ? Math.round(
          runs.reduce((sum, r) => sum + (r.batteryHealth || 0), 0) / runs.length
        )
      : 0;

  const avgCpu =
    runs.length > 0
      ? Math.round(
          runs.reduce((sum, r) => sum + (r.cpuPerformancePct || 0), 0) / runs.length
        )
      : 0;

  const avgStorageSpeed =
    runs.length > 0
      ? Math.round(
          runs.reduce((sum, r) => sum + (r.storageSpeedPct || 0), 0) / runs.length
        )
      : 0;

  const overallScore = (run) => {
    const parts = [
      run.batteryHealth,
      run.storageSpeedPct,
      run.cpuPerformancePct,
      run.ramHealthPct,
      run.displayTouchPct,
      run.cameraCheckPct,
    ].filter((v) => typeof v === 'number');

    if (parts.length === 0) return 0;
    return Math.round(parts.reduce((a, b) => a + b, 0) / parts.length);
  };

  if (loading) {
    return <p className="text-muted">Loading diagnostics…</p>;
  }

  if (error) {
    return <p className="text-danger">Error: {error}</p>;
  }

  return (
    <div>
      {/* Heading */}
      <div className="d-flex justify-content-between align-items-center mb-3">
        <div>
          <h1 className="h3 mb-1">Testr diagnostics overview</h1>
        </div>
      </div>

      {/* Summary cards */}
      <div className="row mb-4">
        <div className="col-md-3 mb-3">
          <div className="card shadow-sm h-100">
            <div className="card-body">
              <h6 className="card-subtitle text-muted mb-1">
                Devices tested
              </h6>
              <p className="display-6 mb-0">{totalDevices}</p>
            </div>
          </div>
        </div>

        <div className="col-md-3 mb-3">
          <div className="card shadow-sm h-100">
            <div className="card-body">
              <h6 className="card-subtitle text-muted mb-1">
                Avg battery health
              </h6>
              <p className="display-6 mb-0">{avgBattery}%</p>
            </div>
          </div>
        </div>

        <div className="col-md-3 mb-3">
          <div className="card shadow-sm h-100">
            <div className="card-body">
              <h6 className="card-subtitle text-muted mb-1">
                Avg CPU performance
              </h6>
              <p className="display-6 mb-0">{avgCpu}%</p>
            </div>
          </div>
        </div>

        <div className="col-md-3 mb-3">
          <div className="card shadow-sm h-100">
            <div className="card-body">
              <h6 className="card-subtitle text-muted mb-1">
                Avg storage speed
              </h6>
              <p className="display-6 mb-0">{avgStorageSpeed}%</p>
            </div>
          </div>
        </div>
      </div>

      {/* Filters -- search by device model */}
      <div className="card shadow-sm mb-3">
        <div className="card-body">
          <div className="row g-3 align-items-center">
            <div className="col-md-6">
              <label className="form-label mb-1">Search by model</label>
              <input
                type="text"
                className="form-control"
                placeholder="e.g. Samsung, Pixel"
                value={searchModel}
                onChange={(e) => setSearchModel(e.target.value)}
              />
            </div>

            <div className="col-md-6 text-md-end">
              <small className="text-muted">
                Showing {filteredRuns.length} of {runs.length} runs
              </small>
            </div>
          </div>
        </div>
      </div>

      {/* Table */}
      <div className="card shadow-sm">
        <div className="card-body">
          <h2 className="h5 mb-3">Recent diagnostic runs</h2>
          <div className="table-responsive">
            <table className="table table-striped table-hover align-middle">
              <thead className="table-dark">
                <tr>
                  <th>ID</th>
                  <th>Model</th>
                  <th>Battery %</th>
                  <th>Storage speed %</th>
                  <th>CPU performance %</th>
                  <th>RAM health %</th>
                  <th>Camera check %</th>
                  <th>Display &amp; touch %</th>
                  <th>Overall score %</th>
                  <th>Tested at</th>
                </tr>
              </thead>
              <tbody>
                {filteredRuns.length === 0 ? (
                  <tr>
                    <td colSpan="10" className="text-center text-muted">
                      No diagnostics match your filters.
                    </td>
                  </tr>
                ) : (
                  filteredRuns.map((run) => (
                    <tr key={run.id}>
                      <td>{run.id}</td>
                      <td>{run.deviceModel}</td>
                      <td>{run.batteryHealth}</td>
                      <td>{run.storageSpeedPct}</td>
                      <td>{run.cpuPerformancePct}</td>
                      <td>{run.ramHealthPct}</td>
                      <td>{run.cameraCheckPct}</td>
                      <td>{run.displayTouchPct}</td>
                      <td>{overallScore(run)}</td>
                      <td>{run.createdAt || run.timestamp}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
          <p className="text-muted small mb-0 mt-2"></p>
        </div>
      </div>
    </div>
  );
}

export default DiagnosticsDashboard;
