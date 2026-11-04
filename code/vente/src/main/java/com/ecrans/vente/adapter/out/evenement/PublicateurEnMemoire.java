package com.ecrans.vente.adapter.out.evenement;

import com.ecrans.vente.application.port.out.PublicateurEvenements;
import com.ecrans.vente.domaine.evenement.EvenementVente;

import java.util.ArrayList;
import java.util.List;

/** Adapter secondaire provisoire. En production : une table outbox dans la même transaction. */
public class PublicateurEnMemoire implements PublicateurEvenements {

    private final List<EvenementVente> publies = new ArrayList<>();

    @Override
    public void publier(EvenementVente evenement) {
        publies.add(evenement);
    }

    public List<EvenementVente> publies() {
        return List.copyOf(publies);
    }
}
