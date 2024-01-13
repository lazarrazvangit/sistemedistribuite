package org.example;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadServer extends Thread {
    public boolean running = true;

    @Override
    public void run() {
        // declarare variabile
        ServerSocket socketServer = null;

        // initializare
        try {
            socketServer = new ServerSocket(VariabileGlobale.portServerLocal);
        }catch (Exception e){
            // portul pe care se porneste serverul local poate fi folosit de alt proces
            e.printStackTrace();
            System.out.println("eroare pornire server");
        }

        while (running) {
            try {
                // asteapta conexiune noua initializata de un client
                Socket socketClient = socketServer.accept();

                BufferedReader in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
                PrintWriter out = new PrintWriter(socketClient.getOutputStream(), true);

                // citire sir de caractere primit
                String sirCaractere = in.readLine();

                System.out.println("sir de caractere primit:");
                System.out.println(sirCaractere);
                System.out.println("-----------------------------------------------");

                //se imparte sirul de caractere primit in subsiruri
                //primul subsir pana la spatiu reprezinta un mesaj
                //celelalte subsiruri sunt argumente, de exemplu cine a trimis mesajul
                String[] subsiruri = sirCaractere.split(" ");
                String mesaj = subsiruri[0];

                if (mesaj.equals("Hello")){
                    // converteste obiect din program in sir de caractere
                    // pentru a fi trimis prin retea
                    Gson gson = new Gson();
                    String dictionarIdIpConvertit = gson.toJson(VariabileGlobale.perechiIdIp);
                    String dictionarIdPortConvertit = gson.toJson(VariabileGlobale.perechiIdPort);
                    String colectieDocumenteConvertita = gson.toJson(VariabileGlobale.colectieDocumente);

                    // trimitem catre peer nou in retea id-urile, ip-urile si porturile altor peer pe care ii cunoaste
                    // acest peer si documentele stocate local
                    out.println(VariabileGlobale.id);
                    out.println(dictionarIdIpConvertit);
                    out.println(dictionarIdPortConvertit);
                    out.println(colectieDocumenteConvertita);

                    // colectam datele despre clientul nou in retea
                    int idClient = Integer.parseInt(subsiruri[1]);
                    String ipClient = socketClient.getInetAddress().getHostAddress();
                    int portClient = Integer.parseInt(subsiruri[2]);

                    System.out.println("Peer nou conectat: "
                            + "id=" + idClient
                            + " ip=" + ipClient
                            + " port=" + portClient);
                    System.out.println("-----------------------------------------------");

                    // salvam datele despre peer nou conectat in retea in dictionare
                    VariabileGlobale.perechiIdIp.put(idClient, ipClient);
                    VariabileGlobale.perechiIdPort.put(idClient, portClient);

                    System.out.println("Dictionare actualizate:");
                    System.out.println(VariabileGlobale.perechiIdIp.toString());
                    System.out.println(VariabileGlobale.perechiIdPort.toString());
                    System.out.println("-----------------------------------------------");
                } else if (mesaj.equals("Lider?")){
                    // trimite id lider inapoi catre peer care intreaba cine e liderul
                    out.println(VariabileGlobale.idLider);

                    System.out.println("Am trimis id-ul liderului catre peer nou");
                    System.out.println("-----------------------------------------------");
                }
                else if(mesaj.equals("Heartbeat")){
                    out.println("Alive");
                }
                else if(mesaj.equals("Coordinator")){
                    // obtinem id-ul noului lider
                    int idLiderNou = Integer.parseInt(subsiruri[1]);
                    // actualizam variabila locala
                    VariabileGlobale.idLider = idLiderNou;
                    //trimitem acknoledge
                    out.println("Ack");

                    System.out.println("Lider nou " + idLiderNou);
                    System.out.println("-----------------------------------------------");

                }else if(mesaj.equals("Election")){
                    // opreste heartbeat prin marcare cu -1 a liderului
                    VariabileGlobale.idLider = -1;
                    // trimite ok inapoi la peer cu id mai mic
                    out.println("Ok");
                    // trimite mesaj Election mai departe la urmatorul peer cu id mai mare
                    MetodeGlobale.ringElection();
                }

                //eliberare resurse
                in.close();
                out.close();
                socketClient.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("eroare server");
            }
        }
    }
}
