package com.ecrans.vente.application.port.out;

import com.ecrans.vente.domaine.commande.Commande;
import com.ecrans.vente.domaine.commun.CommandeId;

import java.util.Optional;

/** Port secondaire : la collection des commandes. */
public interface Commandes {
    Optional<Commande> parId(CommandeId id);

    void enregistrer(Commande commande);
}
