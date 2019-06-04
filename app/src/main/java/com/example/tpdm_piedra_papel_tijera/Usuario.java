package com.example.tpdm_piedra_papel_tijera;

public class Usuario {
    int puntos;
    String tirada, estado;
    boolean tiro;

    public boolean isTiro() {
        return tiro;
    }

    public void setTiro(boolean tiro) {
        this.tiro = tiro;
    }

    public int getPuntos() {
        return puntos;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public String getTirada() {
        return tirada;
    }

    public void setTirada(String tirada) {
        this.tirada = tirada;
    }
}
