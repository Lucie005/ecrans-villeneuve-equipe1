package com.ecrans.vente.domaine.commun;

import java.util.Objects;
import java.util.UUID;

/** Identifiant d'une commande. C'est aussi la « reference » transmise à PayVia. */
public record CommandeId(String valeur) {
    public CommandeId {
        Objects.requireNonNull(valeur);
        if (valeur.isBlank()) throw new IllegalArgumentException("Identifiant de commande vide");
    }

    public static CommandeId nouvelle() {
        return new CommandeId("C-" + UUID.randomUUID().toString().substring(0, 8));
    }

    @Override
    public String toString() {
        return valeur;
    }
}
