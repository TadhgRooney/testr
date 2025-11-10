package com.testr.dut;

import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void getDiagnostics(View v) {
        String sessionId = java.util.UUID.randomUUID().toString();
        DiagnosticManager mgr = new DiagnosticManager(this);
        com.testr.dut.dto.DiagnosticReport report = mgr.collectAll(sessionId);

        DiagnosticsUploader uploader =
                new DiagnosticsUploader("http://172.20.10.2:8080");
                // Hot spot = 172.20.10.2

        DiagnosticRunPayload payload = uploader.buildPayloadFromReport(report);


        android.widget.Toast.makeText(this, "Uploading diagnostics…", android.widget.Toast.LENGTH_SHORT).show();

        uploader.upload(this, payload, new DiagnosticsUploader.ResultCallback() {
            @Override public void onSuccess(String resp) {
                runOnUiThread(() ->
                        android.widget.Toast.makeText(MainActivity.this, "Uploaded ✓", android.widget.Toast.LENGTH_LONG).show()
                );
            }
            @Override public void onError(Exception e) {
                runOnUiThread(() ->
                        android.widget.Toast.makeText(MainActivity.this, "Upload failed: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show()
                );
            }
        });
    }
    }
