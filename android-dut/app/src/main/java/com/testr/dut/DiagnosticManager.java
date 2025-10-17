package com.testr.dut;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;

import com.testr.dut.dto.BatteryInfo;
import com.testr.dut.dto.DiagnosticReport;

public class DiagnosticManager {
    private final Context appContext;

    public DiagnosticManager(Context context){
        this.appContext = context.getApplicationContext();
    }

    //Collect all diagnostics supported
    public DiagnosticReport collectAll(String sessionId){
        DiagnosticReport diagnosticReport = new DiagnosticReport();
        diagnosticReport.sessionId = sessionId;
        diagnosticReport.collectedAtEpochMs = System.currentTimeMillis();

        diagnosticReport.manufacturer = Build.MANUFACTURER;
        diagnosticReport.model = Build.MODEL;
        diagnosticReport.product = Build.PRODUCT;
        diagnosticReport.androidVersion = Build.VERSION.RELEASE;
        diagnosticReport.securityPatch = Build.VERSION.SECURITY_PATCH;

        diagnosticReport.battery = collectBattery();

        return diagnosticReport;
    }

    private BatteryInfo collectBattery(){
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent i = appContext.registerReceiver(null,filter);

        BatteryInfo b = new BatteryInfo();

        BatteryManager bm = (BatteryManager) appContext.getSystemService(Context.BATTERY_SERVICE);
        if (bm!=null){
            int pct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            b.levelPct = pct;
        } else{
            b.levelPct = -1;
        }
        if(i !=null){
            // Health
            b.status = i.getIntExtra(BatteryManager.EXTRA_STATUS,-1);
            // Voltage (mV)
            b.voltageMv = i.getIntExtra(BatteryManager.EXTRA_VOLTAGE,-1);
            b.health = i.getIntExtra(BatteryManager.EXTRA_HEALTH,-1);
            // Temperature in tenths of a degree C (e.g., 320 => 32.0°C)
            b.temperatureTenthsC = i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,-1);
        } else{
            //if null, keep sentinel values
            b.status = -1;
            b.health = -1;
            b.voltageMv = -1;
            b.temperatureTenthsC = -1;
        }

        return b;
    }
}
