package com.conysolis.georegistro;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirestoreManager {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Interfaces para callbacks
    public interface FirestoreCallback {
        void onSuccess();
        void onError(String mensajeError);
    }

    public interface ListadoCallback {
        void onSuccess(List<Incidencia> lista);
        void onError(String mensajeError);
    }

    public interface OnEliminarCallback {
        void onSuccess();
        void onError(String error);
    }

    // Guardar una nueva incidencia
    public void guardarIncidencia(Incidencia incidencia, FirestoreCallback callback) {
        db.collection("incidencias")
                .add(incidencia)
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Error al guardar: " + e.getMessage()));
    }

    // Obtener la lista de todas las incidencias
    public void obtenerIncidencias(ListadoCallback callback) {
        db.collection("incidencias")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Incidencia> lista = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Incidencia inc = document.toObject(Incidencia.class);
                            // Asignar el ID único generado por Firestore al objeto Incidencia
                            inc.setId(document.getId());
                            lista.add(inc);
                        }
                        callback.onSuccess(lista);
                    } else {
                        callback.onError("Error al obtener datos");
                    }
                });
    }

    // Eliminar una incidencia por su ID
    public void eliminarIncidencia(String documentId, OnEliminarCallback callback) {
        db.collection("incidencias")
                .document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}