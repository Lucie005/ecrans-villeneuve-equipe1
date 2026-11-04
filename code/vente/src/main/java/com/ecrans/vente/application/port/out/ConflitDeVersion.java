package com.ecrans.vente.application.port.out;

import com.ecrans.vente.domaine.commun.SeanceId;

/**
 * Verrou optimiste : deux transactions ont chargé la même séance, la seconde à enregistrer perd.
 * Ce n'est pas une erreur métier : il suffit de recharger et de réessayer (voir ADR-0002).
 */
public class ConflitDeVersion extends RuntimeException {
    public ConflitDeVersion(SeanceId seance, long attendue, long trouvee) {
        super("Séance " + seance + " modifiée entre-temps (version chargée " + attendue + ", version actuelle " + trouvee + ")");
    }
}
