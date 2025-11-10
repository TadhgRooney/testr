package com.testr.dut.dto;

public class CpuInfo {
    // CPU performance as % of baseline (0-100), -1 if failed
    public int performancePct;

    public CpuInfo() { }

    public CpuInfo(int performancePct) {
        this.performancePct = performancePct;
    }
}
