package com.conysolis.georegistro;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapaFragment extends Fragment {

    private MapView mapView;
    private EditText etBuscarLugar;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private GeoPoint puntoOrigen;
    private GeoPoint puntoDestino;
    private String nombreDestino = "Destino seleccionado";

    private Marker marcadorOrigen;
    private Marker marcadorDestino;
    private Polyline rutaPolyline;

    private boolean camaraCentradaInicialmente = false;
    private static final int COLOR_MORADO_CLARO = Color.parseColor("#CE93D8");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Configuration.getInstance().load(requireContext(), requireContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        View view = inflater.inflate(R.layout.fragment_mapa, container, false);

        mapView = view.findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(16.0);

        etBuscarLugar = view.findViewById(R.id.etBuscarLugar);
        Button btnBuscarLugar = view.findViewById(R.id.btnBuscarLugar);
        Button btnIniciarGuia = view.findViewById(R.id.btnIniciarGuia);
        Button btnGuardarRegistroMapa = view.findViewById(R.id.btnGuardarRegistroMapa);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Buscar al presionar botón o tecla Search/Enter del teclado
        btnBuscarLugar.setOnClickListener(v -> buscarLugarPorTexto());
        etBuscarLugar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                buscarLugarPorTexto();
                return true;
            }
            return false;
        });

        btnIniciarGuia.setOnClickListener(v -> iniciarNavegacionGPS());
        btnGuardarRegistroMapa.setOnClickListener(v -> guardarRegistroEnFirestore());

        configurarToqueEnMapa();
        configurarActualizacionUbicacion();
        iniciarRastreoGPS();

        return view;
    }

    // 1. Buscar lugar escrito por el usuario (OpenStreetMap Nominatim, sin API Key)
    private void buscarLugarPorTexto() {
        String consulta = etBuscarLugar.getText().toString().trim();
        if (consulta.isEmpty()) {
            Toast.makeText(getContext(), "Escribe una dirección o lugar", Toast.LENGTH_SHORT).show();
            return;
        }

        ocultarTeclado();
        Toast.makeText(getContext(), "Buscando: " + consulta + "...", Toast.LENGTH_SHORT).show();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                String queryEncoded = URLEncoder.encode(consulta, "UTF-8");
                String urlStr = "https://nominatim.openstreetmap.org/search?q=" + queryEncoded
                        + "&format=json&limit=1&countrycodes=cl";

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "GeoRegistroAndroidApp/1.0");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONArray resultados = new JSONArray(response.toString());

                handler.post(() -> {
                    if (!isAdded() || mapView == null) return;

                    if (resultados.length() > 0) {
                        try {
                            JSONObject lugar = resultados.getJSONObject(0);
                            double lat = lugar.getDouble("lat");
                            double lon = lugar.getDouble("lon");
                            nombreDestino = lugar.optString("display_name", consulta);

                            puntoDestino = new GeoPoint(lat, lon);
                            marcarDestinoYEncuadrar();

                            if (puntoOrigen != null) {
                                trazarRutaMorada(puntoOrigen, puntoDestino);
                            } else {
                                mapView.getController().animateTo(puntoDestino);
                                mapView.getController().setZoom(16.0);
                            }
                            Toast.makeText(getContext(), "Destino fijado", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error al procesar ubicación", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "No se encontró el lugar, intenta ser más específico", Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                handler.post(() -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Error de red al buscar", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void ocultarTeclado() {
        if (getActivity() != null && etBuscarLugar != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etBuscarLugar.getWindowToken(), 0);
            }
        }
    }

    // 2. También permitir tocar el mapa para fijar destino
    private void configurarToqueEnMapa() {
        MapEventsReceiver receiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                if (puntoOrigen == null) {
                    Toast.makeText(getContext(), "Esperando ubicación GPS actual...", Toast.LENGTH_SHORT).show();
                    return true;
                }
                puntoDestino = p;
                nombreDestino = "Punto marcado en mapa";
                marcarDestinoYEncuadrar();
                trazarRutaMorada(puntoOrigen, puntoDestino);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };

        MapEventsOverlay overlayEventos = new MapEventsOverlay(receiver);
        mapView.getOverlays().add(0, overlayEventos);
    }

    // 3. Ubicación GPS en tiempo real
    private void configurarActualizacionUbicacion() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null && mapView != null) {
                    puntoOrigen = new GeoPoint(location.getLatitude(), location.getLongitude());

                    if (marcadorOrigen == null) {
                        marcadorOrigen = new Marker(mapView);
                        marcadorOrigen.setTitle("Tu ubicación actual");
                        marcadorOrigen.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        mapView.getOverlays().add(marcadorOrigen);
                    }
                    marcadorOrigen.setPosition(puntoOrigen);

                    if (!camaraCentradaInicialmente) {
                        mapView.getController().animateTo(puntoOrigen);
                        camaraCentradaInicialmente = true;
                    }

                    if (puntoDestino != null) {
                        trazarRutaMorada(puntoOrigen, puntoDestino);
                    }
                    mapView.invalidate();
                }
            }
        };
    }

    private void iniciarRastreoGPS() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateDistanceMeters(5)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    // 4. Marcar destino y encuadrar mapa
    private void marcarDestinoYEncuadrar() {
        if (marcadorDestino == null) {
            marcadorDestino = new Marker(mapView);
            marcadorDestino.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(marcadorDestino);
        }
        marcadorDestino.setTitle(nombreDestino);
        marcadorDestino.setPosition(puntoDestino);

        if (puntoOrigen != null) {
            List<GeoPoint> puntos = new ArrayList<>();
            puntos.add(puntoOrigen);
            puntos.add(puntoDestino);
            BoundingBox box = BoundingBox.fromGeoPoints(puntos);
            mapView.zoomToBoundingBox(box, true, 160);
        }
        mapView.invalidate();
    }

    // 5. Dibujar ruta morada claro por calles (OSRM)
    private void trazarRutaMorada(GeoPoint origen, GeoPoint destino) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            List<GeoPoint> puntosRuta = new ArrayList<>();
            try {
                String urlString = "https://router.project-osrm.org/route/v1/driving/"
                        + origen.getLongitude() + "," + origen.getLatitude() + ";"
                        + destino.getLongitude() + "," + destino.getLatitude()
                        + "?overview=full&geometries=geojson";

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "GeoRegistroAndroidApp/1.0");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                JSONArray routes = json.getJSONArray("routes");

                if (routes.length() > 0) {
                    JSONArray coordinates = routes.getJSONObject(0)
                            .getJSONObject("geometry")
                            .getJSONArray("coordinates");

                    for (int i = 0; i < coordinates.length(); i++) {
                        JSONArray coord = coordinates.getJSONArray(i);
                        double lon = coord.getDouble(0);
                        double lat = coord.getDouble(1);
                        puntosRuta.add(new GeoPoint(lat, lon));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (puntosRuta.isEmpty()) {
                puntosRuta.add(origen);
                puntosRuta.add(destino);
            }

            handler.post(() -> {
                if (mapView == null || !isAdded()) return;

                if (rutaPolyline != null) {
                    mapView.getOverlays().remove(rutaPolyline);
                }

                rutaPolyline = new Polyline(mapView);
                rutaPolyline.setPoints(puntosRuta);

                Paint paint = rutaPolyline.getOutlinePaint();
                paint.setColor(COLOR_MORADO_CLARO);
                paint.setStrokeWidth(16f);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStrokeJoin(Paint.Join.ROUND);
                paint.setAntiAlias(true);

                mapView.getOverlays().add(rutaPolyline);
                mapView.invalidate();
            });
        });
    }

    // 6. Iniciar ruta GPS paso a paso hacia el lugar buscado o marcado
    private void iniciarNavegacionGPS() {
        if (puntoDestino == null) {
            Toast.makeText(getContext(), "Busca un lugar o toca el mapa primero", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = Uri.parse("google.navigation:q=" + puntoDestino.getLatitude() + "," + puntoDestino.getLongitude() + "&mode=d");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Uri geoUri = Uri.parse("geo:" + puntoDestino.getLatitude() + "," + puntoDestino.getLongitude() + "?q=" + puntoDestino.getLatitude() + "," + puntoDestino.getLongitude());
            startActivity(new Intent(Intent.ACTION_VIEW, geoUri));
        }
    }

    // 7. Guardar registro con nombre, coordenadas y fecha/hora en Firestore
    private void guardarRegistroEnFirestore() {
        if (puntoOrigen == null || puntoDestino == null) {
            Toast.makeText(getContext(), "Busca o selecciona un destino antes de guardar", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "Guardando registro...", Toast.LENGTH_SHORT).show();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> registro = new HashMap<>();
        registro.put("nombreDestino", nombreDestino);
        registro.put("origenLat", puntoOrigen.getLatitude());
        registro.put("origenLng", puntoOrigen.getLongitude());
        registro.put("destinoLat", puntoDestino.getLatitude());
        registro.put("destinoLng", puntoDestino.getLongitude());
        registro.put("fecha", System.currentTimeMillis());

        db.collection("registros_rutas")
                .add(registro)
                .addOnSuccessListener(doc ->
                        Toast.makeText(getContext(), "¡Ruta guardada en el historial!", Toast.LENGTH_LONG).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}