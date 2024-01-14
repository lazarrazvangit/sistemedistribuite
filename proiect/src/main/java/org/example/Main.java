package org.example;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.example.FireExecutie.ThreadHeartbeat;
import org.example.FireExecutie.ThreadServer;
import org.example.Metode.MetodeDeserializare;
import org.example.Metode.MetodeGlobale;

import java.util.HashMap;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // daca nu sunt date argumentele necesare, programul se opreste imediat
        if (args.length != 4) {
            System.exit(-1);
        }

        // citire argumente
        VariabileGlobale.id = Integer.parseInt(args[0]);
        VariabileGlobale.portServerLocal = Integer.parseInt(args[1]);
        VariabileGlobale.ipServerBootstrap = args[2];
        VariabileGlobale.portServerBootstrap = Integer.parseInt(args[3]);

        System.out.println("Id here: " + VariabileGlobale.id);
        System.out.println("Portul local pe care ruleaza serverul: " + VariabileGlobale.portServerLocal);
        System.out.println("Ip-ul serverului remote la care ne conectam: " + VariabileGlobale.ipServerBootstrap);
        System.out.println("Portul serverului remote la care ne conectam: " + VariabileGlobale.portServerBootstrap);
        System.out.println("-----------------------------------------------");

        // pornire server pe alt fir de executie
        ThreadServer threadServer = new ThreadServer();
        threadServer.start();

        // pornire thread heartbeat
        // trimiterea unui heartbeat incepe doar dupa ce se afla cine e lider
        ThreadHeartbeat threadHeartbeat = new ThreadHeartbeat();
        threadHeartbeat.start();

        // exista cazul in care un peer este primul intrat in retea
        if (VariabileGlobale.ipServerBootstrap.equals("0")) {
            // primul peer care intra in retea se autoproclama lider
            VariabileGlobale.idLider = VariabileGlobale.id;

            System.out.println("Eu sunt lider");
            System.out.println("-----------------------------------------------");

            //date pentru test
            HashMap<String, Object> documentTest = new HashMap<>();
            documentTest.put("nume", "calculator");
            documentTest.put("pret", 2000);

            HashMap<String, Object> specificatii = new HashMap<>();
            specificatii.put("RAM", 6);
            specificatii.put("HDD", 500);

            documentTest.put("specificatii", specificatii);
            VariabileGlobale.colectieDocumente.put("produs1", documentTest);
            //aici se incheie datele pentru test

            System.out.println("Colectia de documente:");
            System.out.println(VariabileGlobale.colectieDocumente.toString());
            System.out.println("-----------------------------------------------");
        } else {
            try {
                // trimitere mesaj GetPerechiIdIp
                // primeste inapoi dictionarul cu id-ul si ip-ul fiecarui peer
                String raspunsJsonPerechiIdIp = MetodeGlobale.trimiteMesaj(
                        VariabileGlobale.ipServerBootstrap,
                        VariabileGlobale.portServerBootstrap,
                        "GetPerechiIdIp"
                );
                // trimitere mesaj GetPerechiIdPort
                // primeste inapoi dictionarul cu id-ul si portul fiecarui peer
                String raspunsJsonPerechiIdPort = MetodeGlobale.trimiteMesaj(
                        VariabileGlobale.ipServerBootstrap,
                        VariabileGlobale.portServerBootstrap,
                        "GetPerechiIdPort"
                );
                // trimitere mesaj GetColectieDocumente
                // primeste inapoi ca raspuns colectia de documente
                String raspunsJsonColectieDocumente = MetodeGlobale.trimiteMesaj(
                        VariabileGlobale.ipServerBootstrap,
                        VariabileGlobale.portServerBootstrap,
                        "GetColectieDocumente"
                );

                // convertire dictionare convertite in sir de caractere in dictionare
                HashMap<Integer, String> raspunsPerechiIdIp =
                        MetodeDeserializare.deserializeazaDictionarPerechiIdIp(raspunsJsonPerechiIdIp);
                HashMap<Integer, Integer> raspunsPerechiIdPort =
                        MetodeDeserializare.deserializeazaDictionarPerechiIdPort(raspunsJsonPerechiIdPort);
                HashMap<String, HashMap<String, Object>> raspunsColectieDocumente =
                        MetodeDeserializare.deserializeazaColectieDocumente(raspunsJsonColectieDocumente);

                // actualizare dictionare locale
                VariabileGlobale.perechiIdIp.putAll(raspunsPerechiIdIp);
                VariabileGlobale.perechiIdPort.putAll(raspunsPerechiIdPort);
                VariabileGlobale.colectieDocumente.putAll(raspunsColectieDocumente);

                // trimite mesaj Hello catre ceilalti peers (serverul de legatura nu se gaseste in dictionare)
                // pentru ca peer nou sa fie inregistrat in dictionarele locale ale fiecaruia
                MetodeGlobale.broadcast("Hello " + VariabileGlobale.id + " " + VariabileGlobale.portServerLocal);

                // trimitere mesaj Hello catre serverul de legatura
                //se primeste raspuns id server de legatura
                String raspunsIdServerBootstrap = MetodeGlobale.trimiteMesaj(
                        VariabileGlobale.ipServerBootstrap,
                        VariabileGlobale.portServerBootstrap,
                        "Hello " + VariabileGlobale.id + " " + VariabileGlobale.portServerLocal
                );

                // punem in dictionarele locale id-ul, ip-ul si portul serverlui de legatura
                int idServerBootstrap = Integer.parseInt(raspunsIdServerBootstrap);
                VariabileGlobale.perechiIdIp.put(idServerBootstrap, VariabileGlobale.ipServerBootstrap);
                VariabileGlobale.perechiIdPort.put(idServerBootstrap, VariabileGlobale.portServerBootstrap);

                System.out.println("Dictionare actualizate:");
                System.out.println(VariabileGlobale.perechiIdIp.toString());
                System.out.println(VariabileGlobale.perechiIdPort.toString());
                System.out.println(VariabileGlobale.colectieDocumente.toString());
                System.out.println("-----------------------------------------------");

                // dupa ce i-am cunoscut pe ceilalti din retea aflam cine e liderul
                do {
                    //daca un peer nou intra in retea in timpul alegerii unui nou lider atunci va reincerca
                    //dupa un anumit numar de secunde
                    Thread.sleep(3000);

                    String raspunsIdLider = MetodeGlobale.trimiteMesaj(idServerBootstrap, "Lider?");
                    VariabileGlobale.idLider = Integer.parseInt(raspunsIdLider);
                } while (VariabileGlobale.idLider == -1);

                // dupa ce am aflat cine e liderul monitorizam starea acestuia
                // vezi cod bucla while din clasa ThreadHeartbeat
                System.out.println("Se trimite Hearbeat catre lider in background");
                System.out.println("-----------------------------------------------");

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("eroare la intrare in retea");
                System.out.println("***********************************************");
            }
        }

        Scanner scanner = new Scanner(System.in);

        boolean running = true;
        while (running) {
            String line = scanner.nextLine();

            //aici se interpreteaza linia introdusa de la tastatura
            //" " este separatorul dupa care se face split
            //3 este numarul maxim de subsiruri rezultate (o comanda, 2 argumente)
            //al doilea argument poate fi un json
            String[] stringArray = line.split(" ", 3);
            //comanda este primul cuvant din linie
            String comanda = stringArray[0];

            switch (comanda) {
                case "print":
                    String argument = stringArray[1];
                    if (argument.equals("all")) {
                        System.out.println("Se afiseaza intreaga colectie de documente");
                        System.out.println(VariabileGlobale.colectieDocumente.toString());
                        System.out.println("###############################################");
                    } else {
                        //argument = numele unui document
                        //numele unui document este identificator in colectie
                        //nu pot exista doua documente cu acelasi nume pentru simplificarea demonstratiei
                        HashMap<String, Object> document = VariabileGlobale.colectieDocumente.get(argument);
                        if (document == null) {
                            //exista situatia in care se tasteaza numele unui document inexistent
                            System.out.println("Documentul cautat nu exista!");
                            System.out.println("###############################################");
                        } else {
                            System.out.println(document.toString());
                            System.out.println("###############################################");
                        }
                    }
                    break;
                case "save":
                    String numeDocument = stringArray[1]; //cheie
                    String continutDocumentJson = stringArray[2]; //valoare

                    //verifica daca datele sunt in format JSON corect
                    boolean formatCorect = true;
                    try {
                        JsonParser jsonParser = new JsonParser();
                        jsonParser.parse(continutDocumentJson);
                    } catch (JsonSyntaxException e) {
                        formatCorect = false;
                    }

                    //daca formatul e corect se trimite tranzactia catre lider
                    //sau daca acest peer e lider se va ocupa de tranzactie
                    if (formatCorect == true) {
                        if (VariabileGlobale.id == VariabileGlobale.idLider) {
                            MetodeGlobale.twoPhaseCommit(numeDocument, continutDocumentJson);
                        } else {
                            //trimite tranzactia catre lider
                            //format mesaj: "TRANZACTIE numeDocument continutDocumentJSON"
                            String mesaj = "TRANZACTIE " + numeDocument + " " + continutDocumentJson;
                            MetodeGlobale.trimiteMesaj(VariabileGlobale.idLider, mesaj);

                            System.out.println("Tranzactia a fost trimisa catre lider: " + VariabileGlobale.idLider);
                            System.out.println("-----------------------------------------------");
                        }
                    } else {
                        System.out.println("formamtul JSON e incorect");
                        System.out.println("###############################################");
                    }
                    break;
                default:
                    System.out.println("nu am inteles comanda");
                    System.out.println("###############################################");
            }
        }
        scanner.close();
        //TODO: nu am reusit sa inchid corect threadurile
    }
}