import React, { useState } from 'react';

function DiagnosticsDashboard() {
  // no data yet, will replace with data from DB later
  const runs = [];
  // State for the search box text.
  const [searchModel, setSearchModel] = useState('');

  // Summary stats (all zero until it's hooked up to backend/DB).
  const totalDevices = 0;
  const avgBattery = 0;
  const avgCpu = 0;
  const avgStorageSpeed = 0;

  // nothing to show yet
  const filteredRuns = []; 

  // Placeholder for an overall score per device.
  const overallScore = () => 0;

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
                placeholder="e.g.  Samsung, Pixel"
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
                {/* No data yet */}
                <tr>
                  <td colSpan="10" className="text-center text-muted">
                    No diagnostics loaded yet.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <p className="text-muted small mb-0 mt-2">
          </p>
        </div>
      </div>
    </div>
  );
}

export default DiagnosticsDashboard;

