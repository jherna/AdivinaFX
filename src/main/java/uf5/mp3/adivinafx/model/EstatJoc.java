package uf5.mp3.adivinafx.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class EstatJoc implements Serializable {
    private String response;
    public Map<String,Integer> jugadors = new HashMap<>();

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
