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

    static ThreadHeartbeat threadHeartbeat;
}
