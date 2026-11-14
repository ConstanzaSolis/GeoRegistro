package com.conysolis.georegistro;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class FirestoreManager {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

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

    // Guardar una nueva incidencia y autoasignar su ID
    public void guardarIncidencia(Incidencia incidencia, FirestoreCallback callback) {
        db.collection("incidencias")
                .add(incidencia)
                .addOnSuccessListener(documentReference -> {
                    // Asignar el ID generado por Firestore
                    incidencia.setId(documentReference.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
    // Obtener la lista asociando el ID único del documento
    public void obtenerIncidencias(ListadoCallback callback) {
        db.collection("incidencias")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Incidencia> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Incidencia inc = doc.toObject(Incidencia.class);
                        if (inc != null) {
                            inc.setId(doc.getId()); // Extrae el ID del documento en Firestore
                            lista.add(inc);
                        }
                    }
                    callback.onSuccess(lista);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Eliminar un registro utilizando su ID de Firestore
    public void eliminarIncidencia(String id, OnEliminarCallback callback) {
        if (id == null || id.isEmpty()) {
            callback.onError("ID no válido");
            return;
        }

        db.collection("incidencias").document(id)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}