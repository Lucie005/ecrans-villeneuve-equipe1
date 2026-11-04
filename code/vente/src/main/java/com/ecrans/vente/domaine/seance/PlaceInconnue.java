package com.ecrans.vente.domaine.seance;

import com.ecrans.vente.domaine.commun.ErreurMetier;
import com.ecrans.vente.domaine.commun.PlaceId;
import com.ecrans.vente.domaine.commun.SeanceId;

public class PlaceInconnue extends ErreurMetier {
    public PlaceInconnue(SeanceId seance, PlaceId place) {
        super("La place " + place + " n'existe pas dans la salle de la séance " + seance);
    }
}
