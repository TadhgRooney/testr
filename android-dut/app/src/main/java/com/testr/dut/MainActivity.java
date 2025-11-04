package com.testr.dut;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.testr.dut.dto.DiagnosticReport;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private TextView txtOutput;

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

    public void getDiagnostics(View v){
        txtOutput = findViewById(R.id.txtOutput);
        String sessionId = UUID.randomUUID().toString();

        DiagnosticManager mgr = new DiagnosticManager(this);
        DiagnosticReport report = mgr.collectAll(sessionId);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        txtOutput.setText(gson.toJson(report));
    }

}