package com.ecrans.vente.application.port.out;

import com.ecrans.vente.domaine.commun.SeanceId;
import com.ecrans.vente.domaine.seance.Seance;

import java.util.List;
import java.util.Optional;

/** Port secondaire : la collection des séances en vente. */
public interface Seances {

    Optional<Seance> parId(SeanceId id);

    /** Les séances dont la vente n'est pas terminée. */
    List<Seance> enVente();

    /**
     * Enregistre la séance.
     *
     * @throws ConflitDeVersion si la séance a été modifiée par quelqu'un d'autre depuis son chargement
     */
    void enregistrer(Seance seance);
}
