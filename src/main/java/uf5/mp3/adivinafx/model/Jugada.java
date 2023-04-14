package uf5.mp3.adivinafx.model;

import java.io.Serializable;

public class Jugada implements Serializable {
    private String nom;
    private int tirada;

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getTirada() {
        return tirada;
    }

    public void setTirada(int tirada) {
        this.tirada = tirada;
    }

    @Override
    public String toString() {
        return "Jugada{" +
                "nom='" + nom + '\'' +
                ", tirada=" + tirada +
                '}';
    }
}
