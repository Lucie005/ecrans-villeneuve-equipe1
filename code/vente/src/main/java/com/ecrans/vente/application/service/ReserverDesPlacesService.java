package com.ecrans.vente.application.service;

import com.ecrans.vente.adapter.out.tarification.GrilleTarifaireFixe;
import com.ecrans.vente.application.exception.SeanceInconnue;
import com.ecrans.vente.application.port.in.DemandeBillet;
import com.ecrans.vente.application.port.in.DemandeReservation;
import com.ecrans.vente.application.port.in.Reservation;
import com.ecrans.vente.application.port.in.ReserverDesPlaces;
import com.ecrans.vente.application.port.out.Commandes;
import com.ecrans.vente.application.port.out.ConflitDeVersion;
import com.ecrans.vente.application.port.out.Paiements;
import com.ecrans.vente.application.port.out.Seances;
import com.ecrans.vente.domaine.commande.Billet;
import com.ecrans.vente.domaine.commande.Commande;
import com.ecrans.vente.domaine.commande.Tarif;
import com.ecrans.vente.domaine.commun.CommandeId;
import com.ecrans.vente.domaine.commun.PlaceId;
import com.ecrans.vente.domaine.seance.Seance;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * Orchestration seulement : charger, faire décider le domaine, enregistrer, puis appeler le PSP.
 *
 * Choix d'agrégats (ADR-0002, option B) : Seance et Commande sont écrits dans la même transaction.
 * L'appel au PSP est fait APRÈS : on ne garde pas une transaction ouverte pendant un appel réseau.
 */
public class ReserverDesPlacesService implements ReserverDesPlaces {

    /** Bonus : nombre d'essais quand une autre vente a modifié la séance en même temps. */
    static final int ESSAIS_MAX = 3;

    private final Seances seances;
    private final Commandes commandes;
    private final GrilleTarifaireFixe grille;
    private final Paiements paiements;
    private final Clock horloge;

    public ReserverDesPlacesService(Seances seances, Commandes commandes, GrilleTarifaireFixe grille,
                                    Paiements paiements, Clock horloge) {
        this.seances = seances;
        this.commandes = commandes;
        this.grille = grille;
        this.paiements = paiements;
        this.horloge = horloge;
    }

    @Override
    public Reservation executer(DemandeReservation demande) {
        // 1. Prendre l'heure actuelle et générer un nouvel ID de commande
        LocalDateTime maintenant = LocalDateTime.now(horloge);
        CommandeId commandeId = CommandeId.nouvelle();

        // 2. Transformer chaque DemandeBillet en Billet avec son tarif figé (B6)
        List<Billet> billets = new ArrayList<>();
        Set<PlaceId> placesDemandees = new HashSet<>(); // Pour l'envoyer à la séance ensuite

        for (DemandeBillet db : demande.billets()) {
            Tarif tarif = new Tarif(db.categorie(), grille.prix(demande.seance(), db.categorie()));
            billets.add(new Billet(db.place(), tarif));
            placesDemandees.add(db.place());
        }

        // 3. Charger la séance depuis la base (ou mémoire)
        Seance seance = seances.parId(demande.seance())
                .orElseThrow(() -> new SeanceInconnue(demande.seance()));

        // 4. Faire décider le domaine !
        // La séance bloque les places (tout ou rien), puis la commande se crée
        LocalDateTime finBlocage = seance.bloquer(placesDemandees, commandeId, maintenant);
        Commande commande = Commande.creer(commandeId, seance.id(), billets, finBlocage);

        // 5. Enregistrer les modifications
        seances.enregistrer(seance);
        commandes.enregistrer(commande);

        // 6. Demander l'URL de paiement à PayVia
        String urlPaiement = paiements.demanderPaiement(commandeId, commande.total());

        // 7. Renvoyer le reçu de réservation au client
        return new Reservation(commandeId, commande.total(), urlPaiement, finBlocage);
    }
}
