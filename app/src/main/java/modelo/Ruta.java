package modelo;

import java.io.Serializable;

public class Ruta implements Serializable{
    private String nombreRuta, cooperativa ,salida, llegada, tiempoBus;
    public Ruta (String nombreRuta, String cooperativa, String salida, String llegada, String tiempoBus){
        this.nombreRuta = nombreRuta;
        this.cooperativa = cooperativa;
        this.salida = salida;
        this.llegada = llegada;
        this.tiempoBus = tiempoBus;
    }

    public String getNombreRuta() {
        return nombreRuta;
    }

    public String getCooperativa() {
        return cooperativa;
    }

    public String getSalida() {
        return salida;
    }

    public String getLlegada() {
        return llegada;
    }

    public String getTiempoBus() {
        return tiempoBus;
    }

    public void setNombreRuta(String nombreRuta) {
        this.nombreRuta = nombreRuta;
    }

    public void setCooperativa(String cooperativa) {
        this.cooperativa = cooperativa;
    }

    public void setSalida(String salida) {
        this.salida = salida;
    }

    public void setLlegada(String llegada) {
        this.llegada = llegada;
    }

    public void setTiempoBus(String tiempoBus) {
        this.tiempoBus = tiempoBus;
    }

    @Override
    public String toString() {
        return nombreRuta;
    }
}
