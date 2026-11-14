package com.conysolis.georegistro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistorialFragment extends Fragment {

    private RecyclerView recyclerRutas;
    private RegistroRutaAdapter adapter;
    private List<RegistroRuta> listaRutas;
    private ListenerRegistration firestoreListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historial, container, false);

        recyclerRutas = view.findViewById(R.id.recyclerRutas);
        recyclerRutas.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerRutas.setHasFixedSize(false);

        listaRutas = new ArrayList<>();
        adapter = new RegistroRutaAdapter(listaRutas);
        recyclerRutas.setAdapter(adapter);

        cargarRutasDesdeFirestore();

        return view;
    }

    private void cargarRutasDesdeFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        firestoreListener = db.collection("registros_rutas")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (!isAdded() || getContext() == null) {
                        return;
                    }

                    if (error != null) {
                        Toast.makeText(getContext(), "Error al cargar registros: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
    public void onDestroyView() {
        super.onDestroyView();
        if (firestoreListener != null) {
            firestoreListener.remove();
            firestoreListener = null;
        }
    }
}