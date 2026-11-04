package com.ecrans.vente.application.port.in;

import com.ecrans.vente.domaine.commande.Categorie;
import com.ecrans.vente.domaine.commun.PlaceId;

/** Une place choisie par le client et la catégorie de tarif demandée. */
public record DemandeBillet(PlaceId place, Categorie categorie) {
}
