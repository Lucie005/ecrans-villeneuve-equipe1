package com.ecrans.vente.application.exception;

import com.ecrans.vente.domaine.commun.CommandeId;

public class CommandeInconnue extends RuntimeException {
    public CommandeInconnue(CommandeId id) {
        super("Commande inconnue : " + id);
    }
}
