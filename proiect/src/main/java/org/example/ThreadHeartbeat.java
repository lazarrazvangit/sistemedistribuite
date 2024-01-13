package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ThreadHeartbeat extends Thread {

    @Override
    public void run() {
        int contorEroare = 0;

        while (Thread.interrupted() == false) {
            try {
                // heartbeat se trimi din 3 in 3 secunde
                Thread.sleep(3000);

                // -1 marcheaza lipsa liderului
                // thread-ul nu poate fi oprit deoarece este interzisa repornirea
                if (VariabileGlobale.idLider == -1){
                    continue;
                }
                // un peer care e lider nu isi trimite heartbeat singur
                if (VariabileGlobale.idLider == VariabileGlobale.id){
                    continue;
                }

                // gasim ip-ul si portul liderului folosind id-ul
                String ipLider = VariabileGlobale.perechiIdIp.get(VariabileGlobale.idLider);
                int portLider = VariabileGlobale.perechiIdPort.get(VariabileGlobale.idLider);

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
                    if (contorEroare >= 3){
                        contorEroare = 0;

                        if (VariabileGlobale.idLider == -1){
                            // aceasta ramura if previne inceperea electiilor simultane in retea
                            // daca idLider = -1 atunci nu trebuie sa se trimita heartbeat
                            // idLider poate avea valoarea -1 in timpul unui heartbeat
                            // daca idLider este modificat de pe ThreadServer
                            // atunci cand se primeste mesajul Election
                        }else {
                            // marcam lipsa liderului
                            VariabileGlobale.idLider = -1;

                            System.out.println("Liderul a parasit reteaua, se incepe electia");
                            System.out.println("-----------------------------------------------");

                            MetodeGlobale.ringElection();
                        }
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
