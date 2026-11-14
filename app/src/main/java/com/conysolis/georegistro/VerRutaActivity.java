package com.conysolis.georegistro;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VerRutaActivity extends AppCompatActivity {

    private MapView mapView;
    private static final int COLOR_MORADO_RUTA = Color.parseColor("#8A2BFF");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_ver_ruta);

        String nombreDestino = getIntent().getStringExtra("nombreDestino");
        double origenLat = getIntent().getDoubleExtra("origenLat", 0);
        double origenLng = getIntent().getDoubleExtra("origenLng", 0);
        double destinoLat = getIntent().getDoubleExtra("destinoLat", 0);
        double destinoLng = getIntent().getDoubleExtra("destinoLng", 0);

        TextView tvTitulo = findViewById(R.id.tvTituloRutaGuardada);
        TextView tvDetalle = findViewById(R.id.tvDetalleCoordenadas);
        Button btnVolver = findViewById(R.id.btnVolverHistorial);

        tvTitulo.setText(nombreDestino != null ? nombreDestino : "Ruta guardada");
        tvDetalle.setText(String.format(Locale.getDefault(),
                "Origen: %.5f, %.5f  |  Destino: %.5f, %.5f",
                origenLat, origenLng, destinoLat, destinoLng));

        btnVolver.setOnClickListener(v -> finish());

        mapView = findViewById(R.id.mapViewGuardado);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        GeoPoint origen = new GeoPoint(origenLat, origenLng);
        GeoPoint destino = new GeoPoint(destinoLat, destinoLng);

        // Marcador de Origen
        Marker mOrigen = new Marker(mapView);
        mOrigen.setPosition(origen);
        mOrigen.setTitle("Origen guardado");
        mOrigen.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(mOrigen);

        // Marcador de Destino
        Marker mDestino = new Marker(mapView);
        mDestino.setPosition(destino);
        mDestino.setTitle(nombreDestino != null ? nombreDestino : "Destino");
        mDestino.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(mDestino);

        // Encadrar ambos puntos al cargar el mapa
        mapView.post(() -> {
            List<GeoPoint> puntos = new ArrayList<>();
            puntos.add(origen);
            puntos.add(destino);
            BoundingBox box = BoundingBox.fromGeoPoints(puntos);
            mapView.zoomToBoundingBox(box, true, 160);
        });

        trazarRutaGuardada(origen, destino);
    }

    private void trazarRutaGuardada(GeoPoint origen, GeoPoint destino) {
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
                        puntosRuta.add(new GeoPoint(coord.getDouble(1), coord.getDouble(0)));
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
                if (isFinishing() || mapView == null) return;

                Polyline linea = new Polyline(mapView);
                linea.setPoints(puntosRuta);

                Paint paint = linea.getOutlinePaint();
                paint.setColor(COLOR_MORADO_RUTA);
                paint.setStrokeWidth(16f);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStrokeJoin(Paint.Join.ROUND);
                paint.setAntiAlias(true);

                mapView.getOverlays().add(linea);
                mapView.invalidate();
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }
}