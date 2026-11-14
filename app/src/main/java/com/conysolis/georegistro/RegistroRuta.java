package com.conysolis.georegistro;

import com.google.firebase.firestore.Exclude;

public class RegistroRuta {
    @Exclude
    private String documentId;

    private String nombreDestino;
    private double origenLat;
    private double origenLng;
    private double destinoLat;
    private double destinoLng;
    private long fecha;

    public RegistroRuta() {}

    @Exclude
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getNombreDestino() { return nombreDestino; }
    public void setNombreDestino(String nombreDestino) { this.nombreDestino = nombreDestino; }

    public double getOrigenLat() { return origenLat; }
    public void setOrigenLat(double origenLat) { this.origenLat = origenLat; }

    public double getOrigenLng() { return origenLng; }
    public void setOrigenLng(double origenLng) { this.origenLng = origenLng; }

    public double getDestinoLat() { return destinoLat; }
    public void setDestinoLat(double destinoLat) { this.destinoLat = destinoLat; }

    public double getDestinoLng() { return destinoLng; }
    public void setDestinoLng(double destinoLng) { this.destinoLng = destinoLng; }

    public long getFecha() { return fecha; }
    public void setFecha(long fecha) { this.fecha = fecha; }
}