package org.example;

import java.util.HashMap;

public class VariabileGlobale {
    public static int id;
    public static int portServerLocal;
    public static String ipServerBootstrap;
    public static int portServerBootstrap;

    // instantiere dictionar gol
    public static HashMap<Integer, String> perechiIdIp = new HashMap<>();
    public static HashMap<Integer, Integer> perechiIdPort = new HashMap<>();
    public static int idLider = -1;

    //cheia pentru cautarea unui document e de tip String
    //cheia coincide cu numele documentului
    //valoarea e un dictionar care are chei de tip String
    //iar valorile sunt orice obiecte
    public static HashMap<String, HashMap<String, Object>> colectieDocumente = new HashMap<>();

    //obiect unde este stocata temporar o singura tranzactie
    public static HashMap<String, Object> documentInTranzactie = new HashMap<>();
}
