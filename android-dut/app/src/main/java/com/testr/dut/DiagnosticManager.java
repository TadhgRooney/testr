package com.testr.dut;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.testr.dut.dto.BatteryInfo;
import com.testr.dut.dto.CameraInfo;
import com.testr.dut.dto.ConnectivityInfo;
import com.testr.dut.dto.DiagnosticReport;
import com.testr.dut.dto.CpuInfo;
import com.testr.dut.dto.RamInfo;
import com.testr.dut.dto.SensorInfo;
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

    //Collect all diagnostics supported
    public DiagnosticReport collectAll(String sessionId) {
        DiagnosticReport diagnosticReport = new DiagnosticReport();
        diagnosticReport.sessionId = sessionId;
        diagnosticReport.collectedAtEpochMs = System.currentTimeMillis();

        diagnosticReport.manufacturer = Build.MANUFACTURER;
        diagnosticReport.model = Build.MODEL;
        diagnosticReport.product = Build.PRODUCT;
        diagnosticReport.androidVersion = Build.VERSION.RELEASE;
        diagnosticReport.securityPatch = Build.VERSION.SECURITY_PATCH;

        diagnosticReport.battery = collectBattery();
        diagnosticReport.cameras = collectCamera();
        diagnosticReport.connectivity = collectConnectivity(appContext);
        diagnosticReport.cpu = collectCpuInfo();
        diagnosticReport.ram = collectRamInfo();
        diagnosticReport.storage = collectStorageInfo();
        diagnosticReport.sensors = collectSensorInfo();


        return diagnosticReport;
    }

    public BatteryInfo collectBattery() {
       int health  = readBatteryHealthPctFromSysfs();
        android.util.Log.d("TestrBattery", "Battery healthPct = " + health);
       return new BatteryInfo(health);
    }

    private int readBatteryHealthPctFromSysfs(){
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


        return -1; //Cannot get battery health
    }

    private long readLong(File file){
        if(file == null || !file.exists() || !file.canRead()){
            return -1;
        }

        BufferedReader br = null;
        try{
            br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            if(line == null){
                return -1;
            }

            line = line.trim();

            if(line.isEmpty()){
                return -1;
            }
            return Long.parseLong(line);
        } catch (Exception e) {
            return -1;
        } finally {
            if(br != null){
                try{
                    br.close();
                } catch (IOException ignored) {}
            }
        }
    }

    private CameraInfo collectCamera() {

        //Create a new camera
        CameraInfo cameraInfo = new CameraInfo();

        PackageManager pm = appContext.getPackageManager();

        boolean any = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
        boolean front = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
        boolean back = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);

        cameraInfo.hasFront = front;
        cameraInfo.hasAnyCamera = any;
        cameraInfo.hasBack = back;

        return cameraInfo;
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public ConnectivityInfo collectConnectivity(Context ctx) {
        ConnectivityInfo info = new ConnectivityInfo();

        //Airplane mode
        try {
            info.airplaneModeOn = android.provider.Settings.Global.getInt(
                    ctx.getContentResolver(),
                    android.provider.Settings.Global.AIRPLANE_MODE_ON, 0
            ) == 1;
        } catch (Exception ignored) {
        }

        try {
            ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network active = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                active = cm.getActiveNetwork();
            }
            NetworkCapabilities caps = (active != null) ? cm.getNetworkCapabilities(active) : null;

            if (caps == null) {
                info.activeTransport = "NONE";
            } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                info.activeTransport = "WIFI";
            } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                info.activeTransport = "CELLULAR";
            } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                info.activeTransport = "ETHERNET";
            } else {
                info.activeTransport = "OTHER";
            }
        } catch (Exception e) {
            info.activeTransport = "UNKNOWN";
        }

        //Wifi
        try {
            WifiManager wm = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            info.wifiEnabled = (wm != null && wm.isWifiEnabled());

            if (wm != null) {
                //RSSI + link speed reqire conncection
                WifiInfo wi = wm.getConnectionInfo();
                if (wi != null && wi.getNetworkId() != -1) {
                    info.wifiConnected = true;
                    info.wifiLinkSpeedMbps = wi.getLinkSpeed();
                    info.wifiRssi = wi.getRssi();

                    String ssid = wi.getSSID();
                    if (ssid != null) ssid = ssid.replace("\"", "");
                    info.wifiSsid = ssid;
                } else {
                    info.wifiConnected = false;
                    info.wifiLinkSpeedMbps = 0;
                    info.wifiRssi = 0;
                    info.wifiSsid = null;
                }
            }
        } catch (SecurityException se) {
            info.wifiConnected = false;
            info.wifiLinkSpeedMbps = 0;
            info.wifiRssi = 0;
            info.wifiSsid = null;
        } catch (Exception ignored) {
        }

        //Cellular
        try {
            TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                int simState = tm.getSimState();
                boolean simPresent = (simState == TelephonyManager.SIM_STATE_READY
                        || simState == TelephonyManager.SIM_STATE_PIN_REQUIRED
                        || simState == TelephonyManager.SIM_STATE_PUK_REQUIRED);
                String operator = tm.getNetworkOperatorName();
                String netType = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    netType = networkTypeToString(tm.getDataNetworkType());
                }

                info.carrierName = operator;
                info.dataNetworkType = netType;
                info.cellularReady = simPresent && operator != null && !operator.isEmpty();
            }
        } catch (SecurityException ignored) {
            info.cellularReady = false;
        }

        //Bluetooth
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            info.bluetoothSupported = (adapter != null);
            if (adapter != null) {
                info.bluetoothEnabled = adapter.isEnabled();

                //Check if any common profiles is connected
                boolean anyConnected = false;
                int[] profiles = new int[]{
                        BluetoothProfile.HEADSET,
                        BluetoothProfile.A2DP,
                        BluetoothProfile.HEALTH
                };
                for (int profile : profiles) {
                    int state = adapter.getProfileConnectionState(profile);
                    if (state == BluetoothProfile.STATE_CONNECTED) {
                        anyConnected = true;
                        break;
                    }
                }
                info.bluetoothAnyProfileConnected = anyConnected;
            }
        } catch (SecurityException se) {
            info.bluetoothAnyProfileConnected = false;
        } catch (Exception ignored) {
        }

        return info;

    }

    private String networkTypeToString(int type) {
        switch (type) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "EVDO_0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "EVDO_A";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "EVDO_B";
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "1xRTT";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPA+";
            case TelephonyManager.NETWORK_TYPE_NR:
                return "5G NR";
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
            default:
                return "UNKNOWN";
        }
    }

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
        // Baseline iterations for a "healthy" reference device in 1 second.
        final long baselineIterations = 5_000_000L;

        double ratio = (double) iterations / (double) baselineIterations;


        if (ratio > 1.0) ratio = 1.0;
        if (ratio < 0.0) ratio = 0.0;

        int pct = (int) Math.round(ratio * 100.0);
        return pct;
    }



    private StorageInfo collectStorageInfo(){
        int pct = runStorageSpeedTest();
        android.util.Log.d("TestrStorage", "Storage speedPct = " + pct);
        return new StorageInfo(pct);
    }

    private int runStorageSpeedTest() {
        File dir = appContext.getCacheDir();
        File testFile = new File(dir, "testr_storage_test.bin");

        final int sizeMb = 32;
        byte[] buffer = new byte[1024 * 1024]; // 1MB

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
        double readSeconds  = (readEnd - readStart)  / 1_000_000_000.0;

        if (writeSeconds <= 0 || readSeconds <= 0) {
            return -1;
        }

        double writeMbPerSec = sizeMb / writeSeconds;
        double readMbPerSec  = sizeMb / readSeconds;

        return computeStorageSpeedPct(writeMbPerSec, readMbPerSec);
    }

    private int computeStorageSpeedPct(double writeMbPerSec, double readMbPerSec) {
        // Baseline values for a "healthy" modern device.
        double baselineWrite = 200.0; // MB/s
        double baselineRead  = 400.0; // MB/s

        double writeRatio = writeMbPerSec / baselineWrite;
        double readRatio  = readMbPerSec / baselineRead;

        // Clamp ratios at 1.0 max so faster devices don't exceed 100%
        if (writeRatio > 1.0) writeRatio = 1.0;
        if (readRatio > 1.0)  readRatio  = 1.0;

        double pct = ((writeRatio + readRatio) / 2.0) * 100.0;

        if (pct < 0) pct = 0;
        if (pct > 100) pct = 100;

        return (int) Math.round(pct);
    }

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

        // Convert to GB
        double totalGb = totalBytes / (1024.0 * 1024.0 * 1024.0);


        long expectedGb = Math.round(totalGb);

        if (expectedGb <= 0) return -1;

        double ratio = totalGb / expectedGb;

        if (ratio > 1.0) ratio = 1.0;
        if (ratio < 0.0) ratio = 0.0;

        int pct = (int) Math.round(ratio * 100.0);
        return pct;
    }

    private SensorInfo collectSensorInfo(){
        SensorInfo out = new SensorInfo();

        SensorManager sm = (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
        if (sm != null){
            out.hasAccelerometer =  (sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null);
            out.hasGyroscope     = (sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null);
            out.hasMagnetometer  = (sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null);
            out.hasProximity     = (sm.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null);
            out.hasLight         = (sm.getDefaultSensor(Sensor.TYPE_LIGHT) != null);
            out.hasBarometer     = (sm.getDefaultSensor(Sensor.TYPE_PRESSURE) != null);
            out.hasStepCounter   = (sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null);
            out.hasHeartRate     = (sm.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null);
        }
        try {
            out.hasFingerprint = appContext.getPackageManager()
                    .hasSystemFeature(PackageManager.FEATURE_FINGERPRINT);
        } catch (Throwable ignored) { }

        return out;
    }
}
