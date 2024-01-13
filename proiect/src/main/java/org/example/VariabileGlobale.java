package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VariabileGlobale {
    static int id;
    static int portServerLocal;
    static String ipServerBootstrap;
    static int portServerBootstrap;

    // instantiere dictionar gol
    static HashMap<Integer, String> perechiIdIp = new HashMap<>();
    static HashMap<Integer, Integer> perechiIdPort = new HashMap<>();
    static int idLider = -1;
}
