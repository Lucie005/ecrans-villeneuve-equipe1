package com.ecrans.vente.domaine.commande;

import com.ecrans.vente.domaine.commun.ErreurMetier;

/** B3 : une commande contient 10 places au maximum. */
public class TropDePlaces extends ErreurMetier {
    public TropDePlaces(int demandees) {
        super(demandees + " places demandées : " + Commande.MAX_PLACES + " au maximum par commande");
    }
}
