package com.ecrans.vente.domaine.commande;

import com.ecrans.vente.domaine.commun.Montant;

import java.util.Objects;

/** Value object : une catégorie et le prix calculé au moment du blocage (B6 : prix figé). */
public record Tarif(Categorie categorie, Montant prix) {
    public Tarif {
        Objects.requireNonNull(categorie);
        Objects.requireNonNull(prix);
    }
}
