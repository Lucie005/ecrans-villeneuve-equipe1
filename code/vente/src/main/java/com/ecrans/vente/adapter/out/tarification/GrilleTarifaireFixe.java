package com.ecrans.vente.adapter.out.tarification;

import com.ecrans.vente.application.port.out.GrilleTarifaire;
import com.ecrans.vente.domaine.commande.Categorie;
import com.ecrans.vente.domaine.commun.Montant;
import com.ecrans.vente.domaine.commun.SeanceId;

import java.util.EnumMap;
import java.util.Map;

/**
 * Adapter secondaire provisoire : la grille de la section 3.4 du sujet, sans suppléments ni opérations.
 * Il sera remplacé par un client du contexte Tarification ; le port, lui, ne bougera pas.
 */
public class GrilleTarifaireFixe implements GrilleTarifaire {

    private final Map<Categorie, Montant> prix = new EnumMap<>(Map.of(
            Categorie.PLEIN, Montant.centimes(1150),
            Categorie.REDUIT, Montant.centimes(900),
            Categorie.ENFANT, Montant.centimes(650),
            Categorie.MATIN, Montant.centimes(700),
            Categorie.ABONNE, Montant.ZERO));

    @Override
    public Montant prix(SeanceId seance, Categorie categorie) {
        return prix.get(categorie);
    }
}
