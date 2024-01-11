package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadServer extends Thread{
    public boolean running = true;
    @Override
    public void run(){
        while (running){
            try  {
                ServerSocket socketServer = new ServerSocket(VariabileGlobale.port);
                // asteapta conexiune noua initializata de un client
                Socket socketClient = socketServer.accept();

                BufferedReader in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
                PrintWriter out = new PrintWriter(socketClient.getOutputStream(), true);

                // citire mesaj primit
                String mesaj = in.readLine();
                System.out.println(mesaj);

                //eliberare resurse
                in.close();
                out.close();
                socketClient.close();
            }catch (IOException e){
                e.printStackTrace();
                System.out.println("eroare server");
            }
        }
    }
}
