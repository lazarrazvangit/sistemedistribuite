package org.example.Metode;

import com.google.gson.Gson;

public class MetodeSerializare {
    public static String serializeaza(Object obiect){
        Gson gson = new Gson();
        String obiectSerializat = gson.toJson(obiect);
        return obiectSerializat;
    }
}
