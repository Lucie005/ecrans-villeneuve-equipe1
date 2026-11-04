package com.ecrans.vente.domaine.commun;

import java.util.Objects;

/** Identifiant d'une séance, attribué par la Programmation (ex. « S42 »). */
public record SeanceId(String valeur) {
    public SeanceId {
        Objects.requireNonNull(valeur);
        if (valeur.isBlank()) throw new IllegalArgumentException("Identifiant de séance vide");
    }

    @Override
    public String toString() {
        return valeur;
    }
}
