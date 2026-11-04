package com.ecrans.vente.domaine.commande;

import com.ecrans.vente.domaine.commun.ErreurMetier;

public class CommandeVide extends ErreurMetier {
    public CommandeVide() {
        super("Une commande contient au moins une place");
    }
}
