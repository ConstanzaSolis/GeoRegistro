package com.conysolis.georegistro;
import com.conysolis.georegistro.FirestoreManager;
import com.conysolis.georegistro.Incidencia;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegistroFragment extends Fragment {

    private EditText etTitulo, etDescripcion;
    private Spinner spinnerCategoria;
    private TextView tvCoordenadas;
    private double latitud = 0.0, longitud = 0.0;
    private FusedLocationProviderClient fusedLocationClient;
    private final FirestoreManager firestoreManager = new FirestoreManager();

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    obtenerUbicacion();
                } else {
                    Toast.makeText(getContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registro, container, false);

        etTitulo = view.findViewById(R.id.etTitulo);
        etDescripcion = view.findViewById(R.id.etDescripcion);
        spinnerCategoria = view.findViewById(R.id.spinnerCategoria);
        tvCoordenadas = view.findViewById(R.id.tvCoordenadas);
        Button btnGps = view.findViewById(R.id.btnGps);
        Button btnGuardar = view.findViewById(R.id.btnGuardar);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        String[] categorias = {"Infraestructura", "Seguridad", "Aseo", "Otros"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categorias);
        spinnerCategoria.setAdapter(adapter);

        btnGps.setOnClickListener(v -> solicitarPermisosGPS());
        btnGuardar.setOnClickListener(v -> guardarIncidencia());

        return view;
    }

    private void solicitarPermisosGPS() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacion();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void obtenerUbicacion() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    latitud = location.getLatitude();
                    longitud = location.getLongitude();
                    tvCoordenadas.setText(String.format(Locale.getDefault(), "Lat: %.5f, Lon: %.5f", latitud, longitud));
                } else {
                    Toast.makeText(getContext(), "Asegúrate de tener el GPS activado", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void guardarIncidencia() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String categoria = spinnerCategoria.getSelectedItem().toString();

        if (titulo.isEmpty()) {
            etTitulo.setError("El título es obligatorio");
            return;
        }
        if (descripcion.isEmpty()) {
            etDescripcion.setError("La descripción es obligatoria");
            return;
        }
        if (latitud == 0.0 && longitud == 0.0) {
            Toast.makeText(getContext(), "Debe obtener la ubicación GPS primero", Toast.LENGTH_SHORT).show();
            return;
        }

        String fechaHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        Incidencia incidencia = new Incidencia(titulo, descripcion, categoria, fechaHora, latitud, longitud);

        firestoreManager.guardarIncidencia(incidencia, new FirestoreManager.FirestoreCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Incidencia guardada exitosamente", Toast.LENGTH_SHORT).show();
                etTitulo.setText("");
                etDescripcion.setText("");
            }

            @Override
            public void onError(String mensajeError) {
                Toast.makeText(getContext(), mensajeError, Toast.LENGTH_LONG).show();
            }
        });
    }
}