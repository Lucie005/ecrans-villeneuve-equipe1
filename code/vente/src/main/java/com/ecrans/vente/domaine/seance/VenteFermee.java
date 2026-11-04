package com.ecrans.vente.domaine.seance;

import com.ecrans.vente.domaine.commun.ErreurMetier;
import com.ecrans.vente.domaine.commun.SeanceId;

/** B4 : la vente ferme 15 minutes après l'heure de début de la séance. */
public class VenteFermee extends ErreurMetier {
    public VenteFermee(SeanceId seance) {
        super("La vente de la séance " + seance + " est fermée");
    }
}
