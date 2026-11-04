package com.ecrans.vente.domaine.commande;

import com.ecrans.vente.domaine.commun.CommandeId;
import com.ecrans.vente.domaine.commun.ErreurMetier;

public class TransitionInterdite extends ErreurMetier {
    public TransitionInterdite(CommandeId commande, StatutCommande actuel, String action) {
        super("Impossible de " + action + " la commande " + commande + " : elle est " + actuel);
    }
}
