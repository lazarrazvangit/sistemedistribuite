package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        // daca nu sunt date argumentele necesare, programul se opreste imediat
        if (args.length != 4){
            System.exit(-1);
        }

        // citire argumente
        VariabileGlobale.id = Integer.parseInt(args[0]);
        VariabileGlobale.portServerLocal = Integer.parseInt(args[1]);
        VariabileGlobale.ipServerRemote = args[2];
        VariabileGlobale.portServerRemote = Integer.parseInt(args[3]);

        System.out.println("Id here: " + VariabileGlobale.id);
        System.out.println("Portul local pe care ruleaza serverul: " + VariabileGlobale.portServerLocal);
        System.out.println("Ip-ul serverului remote la care ne conectam: " + VariabileGlobale.ipServerRemote);
        System.out.println("Portul serverului remote la care ne conectam: " + VariabileGlobale.portServerRemote);
        System.out.println("-----------------------------------------------");

        // pornire server pe alt fir de executie
        ThreadServer threadServer = new ThreadServer();
        threadServer.start();

        // exista cazul in care un peer este primul intrat in retea
        if (VariabileGlobale.ipServerRemote.equals("0")){
            // nimic
        }else {
            try {
                // deschide socket pentru comunicarea cu serverul
                Socket socketClient = new Socket(VariabileGlobale.ipServerRemote, VariabileGlobale.portServerRemote);

                // fluxuri pentru citire si scriere
                BufferedReader in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
                PrintWriter out = new PrintWriter(socketClient.getOutputStream(), true);

                // trimitere mesaj Hello
                out.println("Hello " + VariabileGlobale.id + " " + VariabileGlobale.portServerLocal);

                // primire raspunsuri
                String raspunsIdServerBootstrap = in.readLine();
                String raspunsJsonPerechiIdIp = in.readLine();
                String raspunsJsonPerechiIdPort = in.readLine();

                System.out.println("Raspuns pentru mesajul Hello catre serverul de legatura:");
                System.out.println(raspunsIdServerBootstrap);
                System.out.println(raspunsJsonPerechiIdIp);
                System.out.println(raspunsJsonPerechiIdPort);
                System.out.println("-----------------------------------------------");

                // convertire dictionare convertite in sir de caractere in dictionare
                Gson gson = new Gson();
                Type typeOfIpMap = new TypeToken<HashMap<Integer, String>>(){}.getType();
                Type typeOfPortMap = new TypeToken<HashMap<Integer, Integer>>(){}.getType();
                HashMap<Integer, String> raspunsPerechiIdIp = gson.fromJson(raspunsJsonPerechiIdIp, typeOfIpMap);
                HashMap<Integer, Integer> raspunsPerechiIdPort = gson.fromJson(raspunsJsonPerechiIdPort,typeOfPortMap);

                // actualizare dictionare locale
                VariabileGlobale.perechiIdIp.putAll(raspunsPerechiIdIp);
                VariabileGlobale.perechiIdPort.putAll(raspunsPerechiIdPort);

                // trimite mesaj Hello catre ceilalti peers
                // pentru ca peer nou sa fie inregistrat in dictionarele locale ale fiecaruia
                // iteram fiecare intrare din colectia care contine id-urile si ip-urile
                Set<Map.Entry<Integer, String>> entrySet = VariabileGlobale.perechiIdIp.entrySet();
                for (Map.Entry<Integer, String> entry : entrySet) {
                    int id = entry.getKey();
                    String ip = entry.getValue();
                    // luam din cealalta colectie portul pe care asculta procesul
                    int port = VariabileGlobale.perechiIdPort.get(id);

                    // trimitem mesaj Hello catre peer
                    socketClient = new Socket(ip, port);

                    in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
                    out = new PrintWriter(socketClient.getOutputStream(), true);

                    out.println("Hello " + VariabileGlobale.id + " " + VariabileGlobale.portServerLocal);
                    String raspunsIdServerRemote = in.readLine();

                    System.out.println("Raspuns pentru mesajul Hello catre ceilalti peers:");
                    System.out.println(raspunsIdServerRemote);
                    System.out.println("-----------------------------------------------");
                }

                // punem in dictionarele locale id-ul, ip-ul si portul serverlui de legatura
                int idServerRemote = Integer.parseInt(raspunsIdServerBootstrap);
                VariabileGlobale.perechiIdIp.put(idServerRemote, VariabileGlobale.ipServerRemote);
                VariabileGlobale.perechiIdPort.put(idServerRemote, VariabileGlobale.portServerRemote);

                System.out.println("Dictionare actualizate:");
                System.out.println(VariabileGlobale.perechiIdIp.toString());
                System.out.println(VariabileGlobale.perechiIdPort.toString());
                System.out.println("-----------------------------------------------");

                //eliberare resurse
                in.close();
                out.close();
                socketClient.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("eroare hello");
            }
        }

        boolean running = true;
        while (running){
            Scanner scanner = new Scanner(System.in);
            String mesaj = scanner.nextLine();
            if (mesaj.equals("x")){
                running = false;
            }
            scanner.close();
        }

        // inchide server
        threadServer.running = false;
    }
}