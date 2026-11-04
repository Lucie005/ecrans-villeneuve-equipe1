package com.ecrans.vente.domaine.commande;

import com.ecrans.vente.domaine.commun.ErreurMetier;
import com.ecrans.vente.domaine.commun.PlaceId;

public class PlaceEnDouble extends ErreurMetier {
    public PlaceEnDouble(PlaceId place) {
        super("La place " + place + " apparaît deux fois dans la commande");
    }
}
