package org.example.FireExecutie;

import org.example.Metode.MetodeGlobale;
import org.example.VariabileGlobale;

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

                // se trimite heartbeat
                String raspuns = MetodeGlobale.trimiteMesaj(VariabileGlobale.idLider, "Heartbeat");

                // se verifica raspunsul primit in urma heartbeat-ului
                if (raspuns.equals("Alive")){
                    // dupa un Heartbeat efectuat cu succes se reseteaza contorul
                    contorEroare = 0;
                }else {
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
