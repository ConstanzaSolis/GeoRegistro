package com.conysolis.georegistro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class IncidenciaAdapter extends RecyclerView.Adapter<IncidenciaAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void OnEliminarClick(Incidencia incidencia, int position);
    }

    private final List<Incidencia> lista;
    private final OnItemClickListener listener;

    public IncidenciaAdapter(List<Incidencia> lista, OnItemClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_incidencia, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Incidencia item = lista.get(position);
        holder.txtTitulo.setText(item.getTitulo());
        holder.txtCategoriaFecha.setText(item.getCategoria() + " | " + item.getFechaHora());
        holder.txtCoordenadas.setText("Lat: " + item.getLatitud() + " | Lng: " + item.getLongitud());

        holder.btnEliminar.setOnClickListener(v -> listener.OnEliminarClick(item, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtCategoriaFecha, txtCoordenadas;
        Button btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTituloItem);
            txtCategoriaFecha = itemView.findViewById(R.id.txtCategoriaFechaItem);
            txtCoordenadas = itemView.findViewById(R.id.txtCoordenadasItem);
            btnEliminar = itemView.findViewById(R.id.btnEliminarItem);
        }
    }
}