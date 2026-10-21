package com.conysolis.georegistro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapaFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final FirestoreManager firestoreManager = new FirestoreManager();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mapa, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        cargarMarcadores();
    }

    private void cargarMarcadores() {
        firestoreManager.obtenerIncidencias(new FirestoreManager.ListadoCallback() {
            @Override
            public void onSuccess(List<Incidencia> lista) {
                if (mMap == null) return;
                mMap.clear();

                for (Incidencia inc : lista) {
                    LatLng posicion = new LatLng(inc.getLatitud(), inc.getLongitud());
                    mMap.addMarker(new MarkerOptions()
                            .position(posicion)
                            .title(inc.getTitulo())
                            .snippet(inc.getCategoria() + " - " + inc.getFechaHora()));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicion, 12f));
                }
            }

            @Override
            public void onError(String mensajeError) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), mensajeError, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}