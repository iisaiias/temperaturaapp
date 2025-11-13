package com.example.temperaturaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText editTemperature, editHumidity;
    private Button btnSend;
    private TextView textCurrent, textHumidity, lastUpdate;

    private DatabaseReference deviceRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTemperature = findViewById(R.id.editTemperature);
        editHumidity = findViewById(R.id.editHumidity);
        btnSend = findViewById(R.id.btnSend);
        textCurrent = findViewById(R.id.textCurrent);
        textHumidity = findViewById(R.id.textHumidity);
        lastUpdate = findViewById(R.id.lastUpdate);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        deviceRef = database.getReference("devices").child("deviceA");

        // Botón para enviar datos
        btnSend.setOnClickListener(v -> {
            String tempStr = editTemperature.getText().toString().trim();
            String humStr = editHumidity.getText().toString().trim();

            if (tempStr.isEmpty() || humStr.isEmpty()) {
                Toast.makeText(this, "Ingresa temperatura y humedad", Toast.LENGTH_SHORT).show();
                return;
            }

            double temp = Double.parseDouble(tempStr);
            double hum = Double.parseDouble(humStr);

            Map<String, Object> data = new HashMap<>();
            data.put("temperature", temp);
            data.put("humidity", hum);
            data.put("lastUpdated", System.currentTimeMillis());

            deviceRef.setValue(data)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Datos enviados", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // Escuchar cambios en tiempo real
        deviceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("temperature")) {
                        double temp = snapshot.child("temperature").getValue(Double.class);
                        textCurrent.setText("Temperatura actual: " + temp + " °C");
                    }
                    if (snapshot.hasChild("humidity")) {
                        double hum = snapshot.child("humidity").getValue(Double.class);
                        textHumidity.setText("Humedad actual: " + hum + " %");
                    }
                    if (snapshot.hasChild("lastUpdated")) {
                        long lastUpdatedValue = snapshot.child("lastUpdated").getValue(Long.class);
                        String time = new SimpleDateFormat("HH:mm:ss").format(new Date(lastUpdatedValue));
                        lastUpdate.setText("Última actualización: " + time);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
