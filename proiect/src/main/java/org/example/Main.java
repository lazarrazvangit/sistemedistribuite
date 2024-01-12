package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
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
                out.println("Hello");
                String res = in.readLine();
                System.out.println(res);

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