package com.testr.dut;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Looper;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.testr.dut.dto.BatteryInfo;
import com.testr.dut.dto.CameraInfo;
import com.testr.dut.dto.ConnectivityInfo;
import com.testr.dut.dto.DiagnosticReport;
import com.testr.dut.dto.MemoryCpuInfo;
import com.testr.dut.dto.SensorInfo;
import com.testr.dut.dto.StorageInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.ThreadLocalRandom;

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
        diagnosticReport.memoryCpu = collectMemoryInfo();
        diagnosticReport.storage = collectStorageInfo();
        diagnosticReport.sensors = collectSensorInfo();


        return diagnosticReport;
    }

    private BatteryInfo collectBattery() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent i = appContext.registerReceiver(null, filter);

        BatteryInfo b = new BatteryInfo();

        BatteryManager bm = (BatteryManager) appContext.getSystemService(Context.BATTERY_SERVICE);
        if (bm != null) {
            int pct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            b.levelPct = pct;
        } else {
            b.levelPct = -1;
        }
        if (i != null) {
            b.status = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            // Voltage (mV)
            b.voltageMv = i.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
            b.health = i.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
            // Temperature in tenths of a degree C (e.g., 320 => 32.0°C)
            b.temperatureTenthsC = i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        } else {
            //if null, keep sentinel values
            b.status = -1;
            b.health = -1;
            b.voltageMv = -1;
            b.temperatureTenthsC = -1;
        }

        return b;
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

    public MemoryCpuInfo collectMemoryInfo(){
        MemoryCpuInfo out = new MemoryCpuInfo();

        ActivityManager am = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        if(am == null){
            out.totalRamBytes = -1;
            out.availRamBytes = -1;
            out.thresholdBytes = -1;
            out.lowMemory = false;
            out.appMemoryClassMb = -1;
            out.appLargeMemoryClassMb = -1;
            return out;
        }

        ActivityManager.MemoryInfo sys = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(sys);

        out.totalRamBytes = sys.totalMem;
        out.availRamBytes = sys.availMem;
        out.thresholdBytes = sys.threshold;
        out.lowMemory = sys.lowMemory;

        out.appMemoryClassMb = am.getMemoryClass();
        out.appLargeMemoryClassMb = am.getLargeMemoryClass();

        return out;
    }

    private StorageInfo collectStorageInfo(){
        StorageInfo out = new StorageInfo();
        // Internal stats
        try {
            File internalDir = appContext.getFilesDir();
            StatFs s = new StatFs(internalDir.getAbsolutePath());
            long blk = s.getBlockSizeLong();
            out.internalTotalBytes = s.getBlockCountLong() * blk;
            out.internalFreeBytes  = s.getAvailableBlocksLong() * blk;
        } catch (Throwable t) {
            out.internalTotalBytes = -1;
            out.internalFreeBytes  = -1;
        }

        // External (app-specific) stats
        try {
            File ext = appContext.getExternalFilesDir(null);
            if (ext != null) {
                StatFs s = new StatFs(ext.getAbsolutePath());
                long blk = s.getBlockSizeLong();
                out.externalTotalBytes = s.getBlockCountLong() * blk;
                out.externalFreeBytes  = s.getAvailableBlocksLong() * blk;
            } else {
                out.externalTotalBytes = 0;
                out.externalFreeBytes  = 0;
            }
        } catch (Throwable t) {
            out.externalTotalBytes = -1;
            out.externalFreeBytes  = -1;
        }

        // If on the main thread, skip I/O benchmark to avoid ANR
        boolean isMainThread = (Looper.myLooper() == Looper.getMainLooper());
        if (isMainThread) {
            out.writeSpeedMBps = -1;
            out.readSpeedMBps  = -1;
            return out;
        }

        // I/O micro-benchmark (cache dir), sized to available space and short duration
        File cache = appContext.getCacheDir();
        File testFile = new File(cache, "testr_io_probe.bin");

        try {
            StatFs cs = new StatFs(cache.getAbsolutePath());
            long freeBytes = cs.getAvailableBlocksLong() * cs.getBlockSizeLong();

            final int ONE_MB = 1024 * 1024;
            // Aim for ~8 MB, but cap at 1/16 of free space and min 2 MB
            int targetMB = 8;
            long capByFree = Math.max(2, Math.min(targetMB, (int)(freeBytes / ONE_MB / 16)));
            int TEST_MB = (int) capByFree;

            byte[] buf = new byte[ONE_MB];
            new java.util.Random().nextBytes(buf);

            // Write
            long start = System.nanoTime();
            long written = 0;
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(testFile))) {
                for (int i = 0; i < TEST_MB; i++) {
                    bos.write(buf);
                    written += buf.length;
                }
                bos.flush();
            }
            double wSecs = (System.nanoTime() - start) / 1_000_000_000.0;
            out.writeSpeedMBps = (written / (1024.0 * 1024.0)) / Math.max(wSecs, 1e-6);

            // Read
            long read = 0;
            start = System.nanoTime();
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(testFile))) {
                int n;
                while ((n = bis.read(buf)) != -1) read += n;
            }
            double rSecs = (System.nanoTime() - start) / 1_000_000_000.0;
            out.readSpeedMBps = (read / (1024.0 * 1024.0)) / Math.max(rSecs, 1e-6);
        } catch (Throwable t) {
            out.writeSpeedMBps = -1;
            out.readSpeedMBps  = -1;
        } finally {

            testFile.delete();
        }
        return out;
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
