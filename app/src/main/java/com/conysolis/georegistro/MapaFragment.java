package com.conysolis.georegistro;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;

public class MapaFragment extends Fragment {

    private MapView map;
    private final FirestoreManager firestoreManager = new FirestoreManager();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Context ctx = requireContext().getApplicationContext();
        SharedPreferences sharedPref = ctx.getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE);
        Configuration.getInstance().load(ctx, sharedPref);

        View view = inflater.inflate(R.layout.fragment_mapa, container, false);
        map = view.findViewById(R.id.map);

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        GeoPoint startPoint = new GeoPoint(-33.44888, -70.66926);
        map.getController().setZoom(12.0);
        map.getController().setCenter(startPoint);

        cargarMarcadores();

        return view;
    }

    private void cargarMarcadores() {
        firestoreManager.obtenerIncidencias(new FirestoreManager.ListadoCallback() {
            @Override
            public void onSuccess(List<Incidencia> lista) {
                if (map == null || getContext() == null) return;

                map.getOverlays().clear();

                if (lista.isEmpty()) {
                    Toast.makeText(getContext(), "No hay registros para mostrar", Toast.LENGTH_SHORT).show();
                    return;
                }

                GeoPoint ultimaPos = null;
                for (Incidencia inc : lista) {
                    GeoPoint punto = new GeoPoint(inc.getLatitud(), inc.getLongitud());

                    Marker marker = new Marker(map);
                    marker.setPosition(punto);
                    marker.setTitle(inc.getTitulo());
                    marker.setSnippet(inc.getCategoria() + " - " + inc.getFechaHora());
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                    map.getOverlays().add(marker);
                    ultimaPos = punto;
                }

                if (ultimaPos != null) {
                    map.getController().setCenter(ultimaPos);
                }
                map.invalidate();
            }

            @Override
            public void onError(String mensajeError) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error al cargar datos: " + mensajeError, Toast.LENGTH_SHORT).show();
                }
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