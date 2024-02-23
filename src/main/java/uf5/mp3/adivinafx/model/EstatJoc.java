package uf5.mp3.adivinafx.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class EstatJoc implements Serializable {
    public static final long serialVersionUID = 1L;
    private String response;
    public Map<String,Integer> jugadors = new LinkedHashMap<>();
    private String turn;
    private int currentPlayerIndex;


    public EstatJoc() {
        currentPlayerIndex = 1;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getTurn() {
        return turn;
    }

    public String nextTurn() {
        String[] playerNames = jugadors.keySet().toArray(new String[0]);
        if (playerNames.length == 0) {
            return null;
        }
        turn = playerNames[currentPlayerIndex];
        currentPlayerIndex = (currentPlayerIndex + 1) % playerNames.length;

        System.out.println("Turn: " + currentPlayerIndex);

        return turn;
    }

    public void setTurn(String nom) {
        this.turn = nom;
    }
}
