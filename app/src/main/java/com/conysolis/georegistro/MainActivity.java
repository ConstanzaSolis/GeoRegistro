package com.conysolis.georegistro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Cargar el formulario por defecto al abrir la aplicación
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RegistroFragment())
                    .commit();
        }

        // Navegación por pestañas inferiores
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_registro) {
                selectedFragment = new RegistroFragment();
            } else if (itemId == R.id.nav_listado) {
                selectedFragment = new SensorFragment(); // Pantalla del Acelerómetro
            } else if (itemId == R.id.nav_mapa) {
                selectedFragment = new MapaFragment(); // Pantalla del Mapa
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        // Botón opcional para abrir el historial de registros
        Button btnAbrirHistorial = findViewById(R.id.btnAbrirHistorial);
        if (btnAbrirHistorial != null) {
            btnAbrirHistorial.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, HistorialActivity.class);
                startActivity(intent);
            });
        }
    }
}