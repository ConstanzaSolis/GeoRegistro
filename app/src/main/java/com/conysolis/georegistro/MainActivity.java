package com.conysolis.georegistro;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_registro) {
                // Aquí irá el fragmento de registro más adelante
            } else if (itemId == R.id.nav_listado) {
                // Aquí irá el fragmento de listado más adelante
            } else if (itemId == R.id.nav_mapa) {
                // Aquí irá el fragmento de mapa más adelante
            }

            return true;
        });
    }
}