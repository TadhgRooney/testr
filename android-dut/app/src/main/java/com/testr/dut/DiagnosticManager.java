package com.testr.dut;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

import com.testr.dut.dto.BatteryInfo;
import com.testr.dut.dto.CpuInfo;
import com.testr.dut.dto.DiagnosticReport;
import com.testr.dut.dto.RamInfo;
import com.testr.dut.dto.StorageInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class DiagnosticManager {
    private final Context appContext;

    public DiagnosticManager(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public DiagnosticReport collectAll(String sessionId) {
        DiagnosticReport diagnosticReport = new DiagnosticReport();
        diagnosticReport.sessionId = sessionId;
        diagnosticReport.collectedAtEpochMs = System.currentTimeMillis();

        // Basic device info
        diagnosticReport.manufacturer = Build.MANUFACTURER;
        diagnosticReport.model = Build.MODEL;
        diagnosticReport.product = Build.PRODUCT;
        diagnosticReport.androidVersion = Build.VERSION.RELEASE;
        diagnosticReport.securityPatch = Build.VERSION.SECURITY_PATCH;

        diagnosticReport.battery = collectBattery();
        diagnosticReport.cpu = collectCpuInfo();
        diagnosticReport.ram = collectRamInfo();
        diagnosticReport.storage = collectStorageInfo();

        return diagnosticReport;
    }

    // Battery
    public BatteryInfo collectBattery() {
        int health = readBatteryHealthPctFromSysfs();
        android.util.Log.d("TestrBattery", "Battery healthPct = " + health);
        return new BatteryInfo(health);
    }

    private int readBatteryHealthPctFromSysfs() {
        File powerSupplyDir = new File("/sys/class/power_supply");
        if (!powerSupplyDir.exists() || !powerSupplyDir.isDirectory()) {
            return -1;
        }

        File[] entries = powerSupplyDir.listFiles();
        if (entries == null) {
            return -1;
        }

        for (File entry : entries) {
            File fullFile = new File(entry, "charge_full");
            File designFile = new File(entry, "charge_full_design");

            long full = readLong(fullFile);
            long design = readLong(designFile);

            if (full > 0 && design > 0) {
                float pct = (full * 100f) / design;
                if (pct < 0) pct = 0;
                if (pct > 100) pct = 100;
                return Math.round(pct);
            }
        }

        return -1; // cannot get battery health
    }

    private long readLong(File file) {
        if (file == null || !file.exists() || !file.canRead()) {
            return -1;
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            if (line == null) return -1;

            line = line.trim();
            if (line.isEmpty()) return -1;

            return Long.parseLong(line);
        } catch (Exception e) {
            return -1;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    //CPU

    public CpuInfo collectCpuInfo() {
        int pct = runCpuBenchmark();
        android.util.Log.d("TestrCPU", "CPU performancePct = " + pct);
        return new CpuInfo(pct);
    }

    private int runCpuBenchmark() {
        final long testDurationNs = 1_000_000_000L; // 1 second

        long start = System.nanoTime();
        long now;
        long iterations = 0;

        double x = 1.0;

        do {
            x = Math.sin(x) + Math.cos(x);
            iterations++;
            now = System.nanoTime();
        } while (now - start < testDurationNs);

        if (iterations <= 0) {
            return -1;
        }

        return computeCpuPerformancePct(iterations);
    }

    private int computeCpuPerformancePct(long iterations) {
        // Baseline iterations for a reference device in 1 second
        final long baselineIterations = 5_000_000L;

        double ratio = (double) iterations / (double) baselineIterations;

        if (ratio > 1.0) ratio = 1.0;
        if (ratio < 0.0) ratio = 0.0;

        int pct = (int) Math.round(ratio * 100.0);
        return pct;
    }

    //Storage

    private StorageInfo collectStorageInfo() {
        int pct = runStorageSpeedTest();
        android.util.Log.d("TestrStorage", "Storage speedPct = " + pct);
        return new StorageInfo(pct);
    }

    private int runStorageSpeedTest() {
        File dir = appContext.getCacheDir();
        File testFile = new File(dir, "testr_storage_test.bin");

        final int sizeMb = 32;
        byte[] buffer = new byte[1024 * 1024]; // 1 MB

        long writeStart = System.nanoTime();
        try (FileOutputStream fos = new FileOutputStream(testFile)) {
            for (int i = 0; i < sizeMb; i++) {
                fos.write(buffer);
            }
            fos.getFD().sync();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        long writeEnd = System.nanoTime();

        long readStart = System.nanoTime();
        try (FileInputStream fis = new FileInputStream(testFile)) {
            while (fis.read(buffer) != -1) {
                // just read
            }
        } catch (IOException e) {
            e.printStackTrace();
            testFile.delete();
            return -1;
        }
        long readEnd = System.nanoTime();

        testFile.delete();

        double writeSeconds = (writeEnd - writeStart) / 1_000_000_000.0;
        double readSeconds = (readEnd - readStart) / 1_000_000_000.0;

        if (writeSeconds <= 0 || readSeconds <= 0) {
            return -1;
        }

        double writeMbPerSec = sizeMb / writeSeconds;
        double readMbPerSec = sizeMb / readSeconds;

        return computeStorageSpeedPct(writeMbPerSec, readMbPerSec);
    }

    private int computeStorageSpeedPct(double writeMbPerSec, double readMbPerSec) {
        double baselineWrite = 200.0; // MB/s
        double baselineRead = 400.0;  // MB/s

        double writeRatio = writeMbPerSec / baselineWrite;
        double readRatio = readMbPerSec / baselineRead;

        if (writeRatio > 1.0) writeRatio = 1.0;
        if (readRatio > 1.0) readRatio = 1.0;

        double pct = ((writeRatio + readRatio) / 2.0) * 100.0;

        if (pct < 0) pct = 0;
        if (pct > 100) pct = 100;

        return (int) Math.round(pct);
    }

    // RAM
    public RamInfo collectRamInfo() {
        int pct = calculateRamHealthPct();
        android.util.Log.d("TestrRAM", "RAM healthPct = " + pct);
        return new RamInfo(pct);
    }

    private int calculateRamHealthPct() {
        ActivityManager am = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return -1;

        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memInfo);

        long totalBytes = memInfo.totalMem;
        if (totalBytes <= 0) return -1;

        double totalGb = totalBytes / (1024.0 * 1024.0 * 1024.0);

        long expectedGb = Math.round(totalGb);
        if (expectedGb <= 0) return -1;

        double ratio = totalGb / expectedGb;

        if (ratio > 1.0) ratio = 1.0;
        if (ratio < 0.0) ratio = 0.0;

        int pct = (int) Math.round(ratio * 100.0);
        return pct;
    }
}

