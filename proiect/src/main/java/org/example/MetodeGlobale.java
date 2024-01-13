package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MetodeGlobale {
    static void broadcast(String mesaj) {
        System.out.println("Am trimis broadcast mesajul: " + mesaj);
        System.out.println("-----------------------------------------------");

        Set<Map.Entry<Integer, String>> entrySet = VariabileGlobale.perechiIdIp.entrySet();
        for (Map.Entry<Integer, String> entry : entrySet) {
            int id = entry.getKey();
            String ip = entry.getValue();
            // luam din cealalta colectie portul pe care asculta procesul
            int port = VariabileGlobale.perechiIdPort.get(id);

            // trimitem mesaj catre peer
            try {
                Socket socketClient = new Socket(ip, port);

                BufferedReader in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
                PrintWriter out = new PrintWriter(socketClient.getOutputStream(), true);

                out.println(mesaj);
                String raspunsIdServerRemote = in.readLine();

                System.out.println("Raspuns pentru mesajul " + mesaj + " catre " + id);
                System.out.println(raspunsIdServerRemote);
                System.out.println("-----------------------------------------------");
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("eroare trimitere mesaj " + mesaj + " catre " + id);
                System.out.println("************************************************");
            }
        }
    }

    static ArrayList<Integer> cautaPeersIdMaiMare() {
        ArrayList<Integer> colectiePeersIdMaiMare = new ArrayList<>();
        Set<Map.Entry<Integer, String>> entrySet = VariabileGlobale.perechiIdIp.entrySet();
        for (Map.Entry<Integer, String> entry : entrySet) {
            int id = entry.getKey();
            if (id > VariabileGlobale.id) {
                colectiePeersIdMaiMare.add(id);
            }
        }
        return colectiePeersIdMaiMare;
    }

    static boolean sendElectionMessage(ArrayList<Integer> colectiePeersIdMaiMare) {
        boolean okFlag = false;
        for (int id : colectiePeersIdMaiMare) {
            // obtinem ip-ul si portul din dictionare
            String ip = VariabileGlobale.perechiIdIp.get(id);
            int port = VariabileGlobale.perechiIdPort.get(id);

            try {
                Socket socketClient = new Socket(ip, port);

                BufferedReader in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
                PrintWriter out = new PrintWriter(socketClient.getOutputStream(), true);

                out.println("Election");
                System.out.println("Am trimis mesajul election catre peer cu id: " + id);

                String raspuns = in.readLine();

                System.out.println("Raspuns primit pentru mesajul Election de la peer cu id: " + id);
                System.out.println(raspuns);
                System.out.println("-----------------------------------------------");

                // daca cineva raspunde cu Ok
                if (raspuns.equals("Ok")) {
                    // asteapta un alt peer sa anunte cine e coordonator
                    okFlag = true;
                    break;
                }
            } catch (IOException e) {
                // daca nu se poate stabili conexiunea cu un peer
                //e.printStackTrace();
                System.out.println("Eroare trimitere mesaj Election catre un peer cu id mai mare");
                System.out.println("************************************************");
            }
        }
        return okFlag;
    }

    static void ringElection() {
        // se cauta in colectie acei peers id mai mare decat peer curent
        ArrayList<Integer> colectiePeersIdMaiMare = cautaPeersIdMaiMare();

        // se trimite un mesaj "Election" catre acei peers cu id mai mare
        // daca unul raspunde cu Ok se returneaza true si nu se mai trimite Election catre ceilalti
        boolean okFlag = sendElectionMessage(colectiePeersIdMaiMare);

        // situatia exceptionala in care nimeni nu raspunde la mesajul Election
        // adica peer precedent detecteaza caderea liderului
        // de exemplu idLider = 6, idPeerCurent = 5
        if (okFlag == false) {
            // acest peer devine lider
            VariabileGlobale.idLider = VariabileGlobale.id;
            // transmite un mesaj Coordinator catre toti ceilalti
            broadcast("Coordinator " + VariabileGlobale.id);

            System.out.println("Am devenit lider!");
            System.out.println("-----------------------------------------------");
        }
    }

    static String trimiteMesaj(int idDestinatar, String mesaj){
        //gasire ip si port destinatar in dictionare
        String ip = VariabileGlobale.perechiIdIp.get(idDestinatar);
        int port =  VariabileGlobale.perechiIdPort.get(idDestinatar);

        String raspuns = "";

        try {
            Socket socketClient = new Socket(ip, port);

            BufferedReader in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
            PrintWriter out = new PrintWriter(socketClient.getOutputStream(), true);

            out.println(mesaj);
            raspuns = in.readLine();

            System.out.println("Raspuns pentru mesajul " + mesaj + " catre " + idDestinatar);
            System.out.println(raspuns);
            System.out.println("-----------------------------------------------");
            return raspuns;
        }catch (IOException e){
            //e.printStackTrace();
            System.out.println("eroare trimitere mesaj " + mesaj + " catre " + idDestinatar);
            System.out.println("************************************************");
            return raspuns;
        }
    }

    static void twoPhaseCommit(String numeDocument, String continutDocumentJson){
        //salveaza documentul temporar in tranzactie
        HashMap<String, Object> continutDocument = deserializeazaDocument(continutDocumentJson);

        Tranzactie.numeDocumentInTranzactie = numeDocument;
        Tranzactie.continutDocumentInTranzactie = continutDocument;

        //trimite mesaj PREPARE catre toti din retea
        //format mesaj: PREPARE numeDocument continutDocumentJson
        System.out.println("Broadcast mesaj PREPARE");
        System.out.println("-----------------------------------------------");

        int contorPeersCareAuRaspuns = 0;
        int contorRaspunsuriReady = 0;

        Set<Map.Entry<Integer, String>> entrySet = VariabileGlobale.perechiIdIp.entrySet();
        for (Map.Entry<Integer, String> entry : entrySet) {
            int id = entry.getKey();
            String ip = entry.getValue();
            // luam din cealalta colectie portul pe care asculta procesul
            int port = VariabileGlobale.perechiIdPort.get(id);

            try {
                Socket socketClient = new Socket(ip, port);

                BufferedReader in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
                PrintWriter out = new PrintWriter(socketClient.getOutputStream(), true);

                out.println("PREPARE " + numeDocument + " " + continutDocumentJson);
                String raspuns = in.readLine();

                contorPeersCareAuRaspuns++;

                if(raspuns.equals("READY")){
                    contorRaspunsuriReady++;
                }
            }catch (IOException e) {
                //e.printStackTrace();
                System.out.println("eroare trimitere mesaj PREAPARE catre " + id);
                System.out.println("************************************************");
            }
        }

        System.out.println("Numar total peers inregistrati in dictionar: " + VariabileGlobale.perechiIdIp.size());
        System.out.println("Numar peers care au raspuns: " + contorPeersCareAuRaspuns);
        System.out.println("Numar raspunsuri READY: " + contorRaspunsuriReady);
        System.out.println("-----------------------------------------------");

        //daca toti peers inca existenti in retea au raspuns cu READY
        //se trimite un mesaj COMMIT
        //si se salveaza local documentul nou
        if (contorPeersCareAuRaspuns == contorRaspunsuriReady){
            MetodeGlobale.broadcast("COMMIT");

            VariabileGlobale.colectieDocumente.put(Tranzactie.numeDocumentInTranzactie,
                    Tranzactie.continutDocumentInTranzactie);

            System.out.println("Documentul nou a fost salvat");
            System.out.println("-----------------------------------------------");
        }
    }

    static HashMap<String, Object> deserializeazaDocument(String continutDocumentJson){
        Gson gson = new Gson();
        Type mapType = new TypeToken<HashMap<String, Object>>() {
        }.getType(); //chat gpt
        HashMap<String, Object> continutDocument = gson.fromJson(continutDocumentJson, mapType);
        return continutDocument;
    }
}
