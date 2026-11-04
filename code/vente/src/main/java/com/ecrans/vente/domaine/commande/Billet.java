package com.ecrans.vente.domaine.commande;

import com.ecrans.vente.domaine.commun.PlaceId;

import java.util.Objects;

/** Un billet : le droit d'entrer, pour une place et un tarif. Vit dans l'agrégat Commande. */
public record Billet(PlaceId place, Tarif tarif) {
    public Billet {
        Objects.requireNonNull(place);
        Objects.requireNonNull(tarif);
    }
}
