package com.ecrans.vente.application.port.out;

import com.ecrans.vente.domaine.evenement.EvenementVente;

/** Port secondaire : en production, une table outbox relayée vers le broker. */
public interface PublicateurEvenements {
    void publier(EvenementVente evenement);
}
