package org.example;

import java.util.HashMap;

public class VariabileGlobale {
    static int id;
    static int portServerLocal;
    static String ipServerBootstrap;
    static int portServerBootstrap;

    // instantiere dictionar gol
    static HashMap<Integer, String> perechiIdIp = new HashMap<>();
    static HashMap<Integer, Integer> perechiIdPort = new HashMap<>();
    static int idLider = -1;

    //cheia pentru cautarea unui document e de tip String
    //valoarea e un dictionar care are chei de tip String
    //iar valorile sunt orice obiecte
    static HashMap<String, HashMap<String, Object>> colectieDocumente = new HashMap<>();
}
