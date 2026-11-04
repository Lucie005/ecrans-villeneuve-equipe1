package com.ecrans.vente.application.port.in;

import com.ecrans.vente.domaine.commun.SeanceId;

import java.util.List;

/** Ce qu'envoient le site, l'appli, les bornes et les guichets quand le client valide ses places. */
public record DemandeReservation(SeanceId seance, List<DemandeBillet> billets) {
    public DemandeReservation {
        billets = List.copyOf(billets);
    }
}
