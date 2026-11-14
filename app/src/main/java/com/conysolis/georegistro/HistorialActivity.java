package com.conysolis.georegistro;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistorialActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RegistroRutaAdapter adapter;
    private final List<RegistroRuta> listaRutas = new ArrayList<>();
    private ListenerRegistration firestoreListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        recyclerView = findViewById(R.id.recyclerViewIncidencias);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RegistroRutaAdapter(listaRutas);
        recyclerView.setAdapter(adapter);

        cargarRutasDesdeFirestore();
    }

    private void cargarRutasDesdeFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        firestoreListener = db.collection("registros_rutas")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(HistorialActivity.this, "Error al cargar: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        listaRutas.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            RegistroRuta ruta = doc.toObject(RegistroRuta.class);
                            ruta.setDocumentId(doc.getId());
                            listaRutas.add(ruta);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firestoreListener != null) {
            firestoreListener.remove();
        }
    }
}