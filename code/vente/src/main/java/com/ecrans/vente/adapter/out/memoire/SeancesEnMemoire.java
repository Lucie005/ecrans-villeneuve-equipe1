package com.ecrans.vente.adapter.out.memoire;

import com.ecrans.vente.application.port.out.ConflitDeVersion;
import com.ecrans.vente.application.port.out.Seances;
import com.ecrans.vente.domaine.commun.PlaceId;
import com.ecrans.vente.domaine.commun.SeanceId;
import com.ecrans.vente.domaine.seance.EtatPlace;
import com.ecrans.vente.domaine.seance.Seance;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter secondaire en mémoire, qui se comporte comme une vraie base :
 * - il stocke des « lignes » (des copies), jamais l'objet du domaine lui-même ;
 * - il applique un verrou optimiste : on ne peut enregistrer qu'une séance chargée à la dernière version.
 */
public class SeancesEnMemoire implements Seances {

    /** L'équivalent d'une ligne de table : un instantané de la séance. */
    private record LigneSeance(SeanceId id, LocalDateTime debut, Map<PlaceId, EtatPlace> places, long version) {
    }

    private final Map<SeanceId, LigneSeance> lignes = new ConcurrentHashMap<>();

    @Override
    public Optional<Seance> parId(SeanceId id) {
        return Optional.ofNullable(lignes.get(id)).map(SeancesEnMemoire::versDomaine);
    }

    @Override
    public List<Seance> enVente() {
        return lignes.values().stream().map(SeancesEnMemoire::versDomaine).toList();
    }

    @Override
    public synchronized void enregistrer(Seance seance) {
        LigneSeance actuelle = lignes.get(seance.id());
        long versionActuelle = actuelle == null ? 0 : actuelle.version();
        if (actuelle != null && versionActuelle != seance.version()) {
            throw new ConflitDeVersion(seance.id(), seance.version(), versionActuelle);
        }
        lignes.put(seance.id(), new LigneSeance(seance.id(), seance.debut(), seance.places(), versionActuelle + 1));
    }

    private static Seance versDomaine(LigneSeance l) {
        return Seance.reconstituer(l.id(), l.debut(), l.places(), l.version());
    }
}
