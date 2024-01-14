package org.example.Metode;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

public class MetodeDeserializare {
    public static HashMap<String, Object> deserializeazaDocument(String continutDocumentJson){
        Gson gson = new Gson();
        Type mapType = new TypeToken<HashMap<String, Object>>() {
        }.getType(); //chat gpt
        HashMap<String, Object> continutDocument = gson.fromJson(continutDocumentJson, mapType);
        return continutDocument;
    }
    public static HashMap<String, HashMap<String, Object>> deserializeazaColectieDocumente(String raspunsJsonColectieDocumente){
        Gson gson = new Gson();
        Type mapType = new TypeToken<HashMap<String, HashMap<String, Object>>>() {
        }.getType();
        HashMap<String, HashMap<String, Object>> raspunsColectieDocumente = gson.fromJson(raspunsJsonColectieDocumente, mapType);
        return raspunsColectieDocumente;
    }
    public static HashMap<Integer, String> deserializeazaDictionarPerechiIdIp(String raspunsJsonPerechiIdIp){
        Gson gson = new Gson();
        Type typeOfIpMap = new TypeToken<HashMap<Integer, String>>() {
        }.getType();
        HashMap<Integer, String> raspunsPerechiIdIp = gson.fromJson(raspunsJsonPerechiIdIp, typeOfIpMap);
        return raspunsPerechiIdIp;
    }
    public static HashMap<Integer, Integer> deserializeazaDictionarPerechiIdPort(String raspunsJsonPerechiIdPort){
        Gson gson = new Gson();
        Type typeOfPortMap = new TypeToken<HashMap<Integer, Integer>>() {
        }.getType();
        HashMap<Integer, Integer> raspunsPerechiIdPort = gson.fromJson(raspunsJsonPerechiIdPort, typeOfPortMap);
        return raspunsPerechiIdPort;
    }
}
