package com.ecrans.vente.adapter.out.paiement;

import com.ecrans.vente.application.port.out.Paiements;
import com.ecrans.vente.domaine.commun.CommandeId;
import com.ecrans.vente.domaine.commun.Montant;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter secondaire provisoire, en attendant l'ACL vers l'API PayVia.
 * Il retient les demandes pour que les tests puissent les vérifier.
 */
public class PaiementsSimules implements Paiements {

    public record DemandePaiement(CommandeId commande, Montant montant) {
    }

    private final List<DemandePaiement> demandes = new ArrayList<>();

    @Override
    public String demanderPaiement(CommandeId commande, Montant montant) {
        demandes.add(new DemandePaiement(commande, montant));
        return "https://paiement.payvia.example/p/" + commande;
    }

    public List<DemandePaiement> demandes() {
        return List.copyOf(demandes);
    }
}
