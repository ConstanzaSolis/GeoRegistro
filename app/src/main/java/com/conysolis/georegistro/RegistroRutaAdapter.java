package com.conysolis.georegistro;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RegistroRutaAdapter extends RecyclerView.Adapter<RegistroRutaAdapter.ViewHolder> {

    private final List<RegistroRuta> listaRutas;

    public RegistroRutaAdapter(List<RegistroRuta> listaRutas) {
        this.listaRutas = listaRutas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_registro_ruta, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RegistroRuta item = listaRutas.get(position);

        String nombre = (item.getNombreDestino() != null && !item.getNombreDestino().isEmpty())
                ? item.getNombreDestino()
                : "Ubicación marcada";

        holder.tvNombreDestino.setText(nombre);
        holder.tvOrigen.setText(String.format(Locale.getDefault(),
                "Origen: %.5f, %.5f", item.getOrigenLat(), item.getOrigenLng()));
        holder.tvDestino.setText(String.format(Locale.getDefault(),
                "Destino: %.5f, %.5f", item.getDestinoLat(), item.getDestinoLng()));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.getDefault());
        holder.tvFecha.setText("Fecha y hora: " + sdf.format(new Date(item.getFecha())));

        // Botón Ver en Mapa
        holder.btnVerRutaMapa.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), VerRutaActivity.class);
            intent.putExtra("nombreDestino", nombre);
            intent.putExtra("origenLat", item.getOrigenLat());
            intent.putExtra("origenLng", item.getOrigenLng());
            intent.putExtra("destinoLat", item.getDestinoLat());
            intent.putExtra("destinoLng", item.getDestinoLng());
            v.getContext().startActivity(intent);
        });

        // Botón Eliminar
        holder.btnEliminar.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Eliminar registro")
                    .setMessage("¿Deseas eliminar \"" + nombre + "\"?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        String docId = item.getDocumentId();
                        if (docId != null && !docId.isEmpty()) {
                            FirebaseFirestore.getInstance()
                                    .collection("registros_rutas")
                                    .document(docId)
                                    .delete()
                                    .addOnSuccessListener(unused ->
                                            Toast.makeText(v.getContext(), "Registro eliminado", Toast.LENGTH_SHORT).show()
                                    )
                                    .addOnFailureListener(e ->
                                            Toast.makeText(v.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                    );
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return listaRutas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreDestino, tvOrigen, tvDestino, tvFecha;
        Button btnVerRutaMapa, btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreDestino = itemView.findViewById(R.id.tvNombreDestino);
            tvOrigen = itemView.findViewById(R.id.tvCoordenadasOrigen);
            tvDestino = itemView.findViewById(R.id.tvCoordenadasDestino);
            tvFecha = itemView.findViewById(R.id.tvFechaRuta);
            btnVerRutaMapa = itemView.findViewById(R.id.btnVerRutaMapa);
            btnEliminar = itemView.findViewById(R.id.btnEliminarRuta);
        }
    }
}