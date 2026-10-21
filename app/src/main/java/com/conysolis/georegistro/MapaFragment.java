package com.conysolis.georegistro;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

public class MapaFragment extends Fragment {

    private MapView map;
    private Marker marcacioSeleccionada;
    private final FirestoreManager firestoreManager = new FirestoreManager();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Cargar configuración de OSMDroid
        Context ctx = requireContext().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        View view = inflater.inflate(R.layout.fragment_mapa, container, false);
        map = view.findViewById(R.id.map);

        map.setMultiTouchControls(true);

        // Punto de inicio predeterminado (Centro del mapa)
        GeoPoint startPoint = new GeoPoint(-33.4489, -70.6693);
        map.getController().setZoom(15.0);
        map.getController().setCenter(startPoint);

        // Escuchar clics en el mapa
        configurarClicEnMapa();

        return view;
    }

    private void configurarClicEnMapa() {
        MapEventsReceiver receiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                // Al hacer toque simple en cualquier parte del mapa
                mostrarMarcadorEnPunto(p);
                mostrarDialogoConfirmacion(p);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };

        MapEventsOverlay overlay = new MapEventsOverlay(receiver);
        map.getOverlays().add(overlay);
    }

    private void mostrarMarcadorEnPunto(GeoPoint p) {
        // Remover marcador anterior si existía
        if (marcacioSeleccionada != null) {
            map.getOverlays().remove(marcacioSeleccionada);
        }

        // Crear un marcador temporal en el punto tocado
        marcacioSeleccionada = new Marker(map);
        marcacioSeleccionada.setPosition(p);
        marcacioSeleccionada.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marcacioSeleccionada.setTitle("Ubicación seleccionada");

        map.getOverlays().add(marcacioSeleccionada);
        map.invalidate(); // Refrescar vista del mapa
    }

    private void mostrarDialogoConfirmacion(GeoPoint p) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Punto seleccionado")
                .setMessage("¿Deseas registrar este lugar?\nLatitud: " + p.getLatitude() + "\nLongitud: " + p.getLongitude())
                .setPositiveButton("Guardar Registro", (dialog, which) -> {
                    guardarEnFirestore(p);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarEnFirestore(GeoPoint p) {
        // Crear objeto de incidencia con la coordenada tocada
        Incidencia nuevaIncidencia = new Incidencia();
        nuevaIncidencia.setTitulo("Registro desde Mapa");
        nuevaIncidencia.setCategoria("Mapa Interactivo");
        nuevaIncidencia.setLatitud(p.getLatitude());
        nuevaIncidencia.setLongitud(p.getLongitude());
        nuevaIncidencia.setFechaHora("Fecha actual");

        firestoreManager.guardarIncidencia(nuevaIncidencia, new FirestoreManager.FirestoreCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Ubicación guardada con éxito", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String mensajeError) {
                Toast.makeText(getContext(), "Error: " + mensajeError, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) map.onPause();
    }
}