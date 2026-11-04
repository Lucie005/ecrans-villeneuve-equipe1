package com.ecrans.vente.application.exception;

import com.ecrans.vente.domaine.commun.SeanceId;

public class SeanceInconnue extends RuntimeException {
    public SeanceInconnue(SeanceId id) {
        super("Séance inconnue : " + id);
    }
}
