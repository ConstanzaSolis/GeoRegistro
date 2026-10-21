package com.conysolis.georegistro;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistorialActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private IncidenciaAdapter adapter;
    private final List<Incidencia> listaIncidencias = new ArrayList<>();
    private final FirestoreManager firestoreManager = new FirestoreManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        recyclerView = findViewById(R.id.recyclerViewIncidencias);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cargarDatos();
    }

    private void cargarDatos() {
        firestoreManager.obtenerIncidencias(new FirestoreManager.ListadoCallback() {
            @Override
            public void onSuccess(List<Incidencia> lista) {
                listaIncidencias.clear();
                listaIncidencias.addAll(lista);

                adapter = new IncidenciaAdapter(listaIncidencias, (incidencia, position) -> {
                    confirmarEliminacion(incidencia, position);
                });

                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onError(String mensajeError) {
                Toast.makeText(HistorialActivity.this, "Error: " + mensajeError, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmarEliminacion(Incidencia incidencia, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar registro")
                .setMessage("¿Deseas eliminar el registro \"" + incidencia.getTitulo() + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    if (incidencia.getId() != null) {
                        firestoreManager.eliminarIncidencia(incidencia.getId(), new FirestoreManager.OnEliminarCallback() {
                            @Override
                            public void onSuccess() {
                                listaIncidencias.remove(position);
                                adapter.notifyItemRemoved(position);
                                Toast.makeText(HistorialActivity.this, "Registro eliminado", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(HistorialActivity.this, "Error al eliminar: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}