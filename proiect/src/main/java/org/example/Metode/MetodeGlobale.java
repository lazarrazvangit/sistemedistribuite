package org.example.Metode;

import org.example.Tranzactie;
import org.example.VariabileGlobale;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MetodeGlobale {

    public static ArrayList<String> broadcast(String mesaj) {
        System.out.println("Am trimis broadcast mesajul: " + mesaj);
        System.out.println("-----------------------------------------------");

        ArrayList<String> raspunsuriPrimite = new ArrayList<>();

        Set<Map.Entry<Integer, String>> entrySet = VariabileGlobale.perechiIdIp.entrySet();
        for (Map.Entry<Integer, String> entry : entrySet) {
            int id = entry.getKey();
            String raspuns = trimiteMesaj(id, mesaj);
            raspunsuriPrimite.add(raspuns);
        }

        return raspunsuriPrimite;
    }

    public static ArrayList<Integer> cautaPeersIdMaiMare() {
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

    public static boolean sendElectionMessage() {
        // se cauta in colectie acei peers id mai mare decat peer curent
        ArrayList<Integer> colectiePeersIdMaiMare = cautaPeersIdMaiMare();

        // se trimite un mesaj de electie pe rand catre fiecare peer cu id mai mare decat acest peer
        for (int id : colectiePeersIdMaiMare) {
            String raspuns = MetodeGlobale.trimiteMesaj(id, "Election");
            // daca cineva raspunde cu Ok
            if (raspuns.equals("Ok")) {
                // returneaza true
                // asteapta un alt peer sa anunte cine e coordonator
                return true;
            }
        }
        // altfel returneaza false
        // si va deveni el insusi coordinator
        return false;
    }


    public static void ringElection() {
        // se trimite un mesaj "Election" catre acei peers cu id mai mare
        // daca unul raspunde cu Ok se returneaza true
        // si nu se mai trimite Election catre ceilalti deoarece el insusi devinde coordonator
        boolean okFlag = sendElectionMessage();

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

    public static String trimiteMesaj(int idDestinatar, String mesaj) {
        //gasire ip si port destinatar in dictionare pe baza id-ului(cheii)
        String ip = VariabileGlobale.perechiIdIp.get(idDestinatar);
        int port = VariabileGlobale.perechiIdPort.get(idDestinatar);

        try {
            Socket socketClient = new Socket(ip, port);

            BufferedReader in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
            PrintWriter out = new PrintWriter(socketClient.getOutputStream(), true);

            out.println(mesaj);
            String raspuns = in.readLine();

            System.out.println("Raspuns pentru mesajul " + mesaj + " catre " + idDestinatar);
            System.out.println(raspuns);
            System.out.println("-----------------------------------------------");
            return raspuns;
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("eroare trimitere mesaj " + mesaj + " catre " + idDestinatar);
            System.out.println("************************************************");
            return "";
        }
    }

    public static void twoPhaseCommit(String numeDocument, String continutDocumentJson) {
        // converteste din sir de caractere in dictionar
        HashMap<String, Object> continutDocument = MetodeDeserializare.deserializeazaDocument(continutDocumentJson);

        //salveaza documentul temporar in tranzactie
        Tranzactie.numeDocumentInTranzactie = numeDocument;
        Tranzactie.continutDocumentInTranzactie = continutDocument;

        //trimite mesaj PREPARE catre toti din retea
        //format mesaj: PREPARE numeDocument continutDocumentJson
        ArrayList<String> raspunsuriPrimite = broadcast("PREPARE " + numeDocument + " " + continutDocumentJson);

        // initializare contoare
        // contoarele sunt necesare deoarece nu s-a implementat inca o modalitate de a mentine dictionarele
        // cu ip-uri si porturi actualizate dupa iesirea unui peer din retea
        // intr-un caz ideal acel peer care paraseste reteaua trebuie eliminat din dictionarele celorlalti peers
        int contorPeersCareAuRaspuns = 0;
        int contorPeersCareNuAuRaspuns = 0;
        int contorRaspunsuriReady = 0;

        // parurgem raspunsurile si incrementam contoarele
        for (String raspuns : raspunsuriPrimite) {
            if (raspuns.equals("READY")) {
                contorRaspunsuriReady++;
                contorPeersCareAuRaspuns++;
            } else if (raspuns.equals("")) {
                contorPeersCareNuAuRaspuns++;
            } else {
                // exista situatia in care un peer raspunde la prepare cu alt mesaj decat ready
                contorPeersCareAuRaspuns++;
            }
        }

        System.out.println("Numar total peers inregistrati in dictionar: " + VariabileGlobale.perechiIdIp.size());
        System.out.println("Numar peers care au raspuns: " + contorPeersCareAuRaspuns);
        System.out.println("Numar peers care NU au raspuns: " + contorPeersCareNuAuRaspuns);
        System.out.println("Numar raspunsuri READY: " + contorRaspunsuriReady);
        System.out.println("-----------------------------------------------");

        //daca toti peers inca existenti in retea au raspuns cu READY
        //se trimite un mesaj COMMIT
        //si se salveaza local documentul nou
        if (contorPeersCareAuRaspuns == contorRaspunsuriReady) {
            MetodeGlobale.broadcast("COMMIT");

            VariabileGlobale.colectieDocumente.put(Tranzactie.numeDocumentInTranzactie,
                    Tranzactie.continutDocumentInTranzactie);

            System.out.println("Documentul nou a fost salvat");
            System.out.println("-----------------------------------------------");
        } else {
            MetodeGlobale.broadcast("ROLLBACK");

            Tranzactie.numeDocumentInTranzactie = null;
            Tranzactie.continutDocumentInTranzactie = null;

            System.out.println("Tranzactie anulata");
            System.out.println("-----------------------------------------------");
        }
    }
}
