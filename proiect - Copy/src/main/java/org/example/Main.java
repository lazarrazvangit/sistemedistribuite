package org.example;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

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

        // date pentru testare
        //VariabileGlobale.perechiIdIp.put(9, "192.168..");

        // pornire server pe alt fir de executie
        ThreadServer threadServer = new ThreadServer();
        threadServer.start();

        // exista cazul in care un peer este primul intrat in retea
        if (VariabileGlobale.ipServerRemote.equals("0")){

        }else {
            try {
                // deschide socket pentru comunicarea cu serverul
                Socket socketClient = new Socket(VariabileGlobale.ipServerRemote, VariabileGlobale.portServerRemote);

                // fluxuri pentru citire si scriere
                BufferedReader in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
                PrintWriter out = new PrintWriter(socketClient.getOutputStream(), true);

                // trimitere mesaj
                out.println("Hello " + VariabileGlobale.id + " " + VariabileGlobale.portServerLocal);

                // primire raspunsuri
                String res1 = in.readLine();
                String res2 = in.readLine();
                String res3 = in.readLine();

                System.out.println(res1);
                System.out.println(res2);
                System.out.println(res3);

                // convertire dictionare convertite in sir de caractere in dictionare
                // actualizare dictionare locale
                Gson gson = new Gson();
                VariabileGlobale.perechiIdIp = gson.fromJson(res2, HashMap.class);
                VariabileGlobale.perechiIdPort = gson.fromJson(res3, HashMap.class);

                System.out.println("Dictionare actualizate:");
                System.out.println(VariabileGlobale.perechiIdIp.toString());
                System.out.println(VariabileGlobale.perechiIdPort.toString());

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