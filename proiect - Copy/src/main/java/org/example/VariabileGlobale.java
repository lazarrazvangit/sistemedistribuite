package org.example;

import java.util.HashMap;

public class VariabileGlobale {
    static int id;
    static int portServerLocal;
    static String ipServerRemote;
    static int portServerRemote;

    // instantiere dictionar gol
    static HashMap<Integer, String> perechiIdIp = new HashMap<>();
    static HashMap<Integer, Integer> perechiIdPort = new HashMap<>();
}
