package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ThreadHeartbeat extends Thread {
    public boolean running = true;

    @Override
    public void run() {
        int contorEroare = 0;

        // gasim ip-ul si portul liderului folosind id-ul
        String ipLider = VariabileGlobale.perechiIdIp.get(VariabileGlobale.idLider);
        int portLider = VariabileGlobale.perechiIdPort.get(VariabileGlobale.idLider);

        while (running) {
            try {
                try {
                    Socket socketClient = new Socket(ipLider, portLider);

                    BufferedReader in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
                    PrintWriter out = new PrintWriter(socketClient.getOutputStream(), true);

                    out.println("Heartbeat");
                    String raspuns = in.readLine();

                    // dupa un Heartbeat efectuat cu succes se reseteaza contorul
                    contorEroare = 0;

                    System.out.println("Raspuns pentru mesajul Heartbeat catre lider:");
                    System.out.println(raspuns);
                    System.out.println("-----------------------------------------------");
                } catch (IOException e) {
                    contorEroare++;
                    // dupa 3 erori consecutive se considera ca liderul a iesit din retea
                    if (contorEroare == 3){
                        System.out.println("Liderul a parasit reteaua");
                        System.out.println("-----------------------------------------------");
                        contorEroare = 0;
                        running = false;
                    }
                }
                // heartbeat se trimi din 3 in 3 secunde
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
