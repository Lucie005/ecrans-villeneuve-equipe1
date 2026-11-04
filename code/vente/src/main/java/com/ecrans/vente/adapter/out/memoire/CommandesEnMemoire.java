package com.ecrans.vente.adapter.out.memoire;

import com.ecrans.vente.application.port.out.Commandes;
import com.ecrans.vente.domaine.commande.Billet;
import com.ecrans.vente.domaine.commande.Commande;
import com.ecrans.vente.domaine.commande.StatutCommande;
import com.ecrans.vente.domaine.commun.CommandeId;
import com.ecrans.vente.domaine.commun.SeanceId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Adapter secondaire en mémoire. Stocke des copies : oublier d'enregistrer se voit dans les tests. */
public class CommandesEnMemoire implements Commandes {

    private record LigneCommande(CommandeId id, SeanceId seance, List<Billet> billets, LocalDateTime expireA,
                                 StatutCommande statut) {
    }

    private final Map<CommandeId, LigneCommande> lignes = new ConcurrentHashMap<>();

    @Override
    public Optional<Commande> parId(CommandeId id) {
        return Optional.ofNullable(lignes.get(id))
                .map(l -> Commande.reconstituer(l.id(), l.seance(), l.billets(), l.expireA(), l.statut()));
    }

    @Override
    public void enregistrer(Commande c) {
        lignes.put(c.id(), new LigneCommande(c.id(), c.seance(), c.billets(), c.expireA(), c.statut()));
    }

    public int nombre() {
        return lignes.size();
    }
}
