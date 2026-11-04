package com.ecrans.vente.domaine.seance;

import com.ecrans.vente.domaine.commun.ErreurMetier;
import com.ecrans.vente.domaine.commun.PlaceId;

import java.util.Set;
import java.util.TreeSet;

/** B1 : au moins une des places demandées est déjà bloquée ou vendue. */
public class PlacesIndisponibles extends ErreurMetier {
    private final Set<PlaceId> places;

    public PlacesIndisponibles(Set<PlaceId> places) {
        super("Places déjà prises : " + new TreeSet<>(places));
        this.places = Set.copyOf(places);
    }

    public Set<PlaceId> places() {
        return places;
    }
}
