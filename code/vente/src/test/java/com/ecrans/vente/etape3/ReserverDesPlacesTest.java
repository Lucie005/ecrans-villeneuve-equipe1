package com.ecrans.vente.etape3;

import com.ecrans.vente.adapter.out.memoire.CommandesEnMemoire;
import com.ecrans.vente.adapter.out.memoire.SeancesEnMemoire;
import com.ecrans.vente.adapter.out.paiement.PaiementsSimules;
import com.ecrans.vente.adapter.out.paiement.PaiementsSimules.DemandePaiement;
import com.ecrans.vente.adapter.out.tarification.GrilleTarifaireFixe;
import com.ecrans.vente.application.exception.SeanceInconnue;
import com.ecrans.vente.application.port.in.DemandeReservation;
import com.ecrans.vente.application.port.in.Reservation;
import com.ecrans.vente.application.service.ReserverDesPlacesService;
import com.ecrans.vente.domaine.commande.Commande;
import com.ecrans.vente.domaine.commande.StatutCommande;
import com.ecrans.vente.domaine.commande.TropDePlaces;
import com.ecrans.vente.domaine.commun.Montant;
import com.ecrans.vente.domaine.commun.PlaceId;
import com.ecrans.vente.domaine.commun.SeanceId;
import com.ecrans.vente.domaine.seance.PlacesIndisponibles;
import com.ecrans.vente.domaine.seance.StatutPlace;
import com.ecrans.vente.support.HorlogeReglable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.ecrans.vente.support.Donnees.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Étape 3 · le cas d'usage « Réserver des places », avec les adapters en mémoire. */
class ReserverDesPlacesTest {

    private SeancesEnMemoire seances;
    private CommandesEnMemoire commandes;
    private PaiementsSimules paiements;
    private HorlogeReglable horloge;
    private ReserverDesPlacesService service;

    @BeforeEach
    void preparer() {
        seances = new SeancesEnMemoire();
        commandes = new CommandesEnMemoire();
        paiements = new PaiementsSimules();
        horloge = HorlogeReglable.a("20:02");
        seances.enregistrer(seanceS42());
        service = new ReserverDesPlacesService(seances, commandes, new GrilleTarifaireFixe(), paiements, horloge);
    }

    @Test
    void renvoie_le_total_l_adresse_de_paiement_et_l_heure_limite() {
        Reservation reservation = service.executer(demande("G7", "PLEIN", "G8", "REDUIT"));

        assertThat(reservation.total()).isEqualTo(Montant.centimes(2050));
        assertThat(reservation.payerAvant()).isEqualTo(le12Novembre("20:12"));
        assertThat(reservation.urlPaiement()).isEqualTo("https://paiement.payvia.example/p/" + reservation.commande());
    }

    @Test
    void les_places_sont_bloquees_en_base() {
        Reservation reservation = service.executer(demande("G7", "PLEIN", "G8", "REDUIT"));

        var seance = seances.parId(S42).orElseThrow();
        assertThat(seance.etat(PlaceId.de("G7")).statut()).isEqualTo(StatutPlace.BLOQUEE);
        assertThat(seance.etat(PlaceId.de("G7")).commande()).isEqualTo(reservation.commande());
    }

    @Test
    void la_commande_est_enregistree_en_attente_de_paiement_avec_les_prix_de_la_grille() {
        Reservation reservation = service.executer(demande("G7", "PLEIN", "G8", "REDUIT"));

        Commande commande = commandes.parId(reservation.commande()).orElseThrow();
        assertThat(commande.statut()).isEqualTo(StatutCommande.EN_ATTENTE_PAIEMENT);
        assertThat(commande.total()).isEqualTo(Montant.centimes(2050));
    }

    @Test
    void le_paiement_est_demande_pour_le_bon_montant() {
        Reservation reservation = service.executer(demande("G7", "PLEIN", "G8", "REDUIT"));

        assertThat(paiements.demandes()).containsExactly(new DemandePaiement(reservation.commande(), Montant.centimes(2050)));
    }

    @Test
    void une_place_deja_prise_fait_echouer_toute_la_reservation_B1() {
        service.executer(demande("G7", "PLEIN"));

        assertThatThrownBy(() -> service.executer(demande("G7", "PLEIN", "G8", "REDUIT")))
                .isInstanceOf(PlacesIndisponibles.class);
        assertThat(seances.parId(S42).orElseThrow().etat(PlaceId.de("G8")).statut()).isEqualTo(StatutPlace.LIBRE);
        assertThat(commandes.nombre()).isEqualTo(1);
        assertThat(paiements.demandes()).hasSize(1);
    }

    @Test
    void plus_de_10_places_sont_refusees_et_rien_n_est_bloque_B3() {
        DemandeReservation onze = demande("F1", "PLEIN", "F2", "PLEIN", "F3", "PLEIN", "F4", "PLEIN", "F5", "PLEIN",
                "F6", "PLEIN", "F7", "PLEIN", "F8", "PLEIN", "F9", "PLEIN", "F10", "PLEIN", "G1", "PLEIN");

        assertThatThrownBy(() -> service.executer(onze)).isInstanceOf(TropDePlaces.class);
        assertThat(seances.parId(S42).orElseThrow().placesLibresA(le12Novembre("20:02"))).hasSize(20);
    }

    @Test
    void une_seance_inconnue_est_signalee() {
        DemandeReservation ailleurs = new DemandeReservation(new SeanceId("S99"), demande("G7", "PLEIN").billets());

        assertThatThrownBy(() -> service.executer(ailleurs)).isInstanceOf(SeanceInconnue.class);
    }

    @Test
    void deux_reservations_successives_sur_des_places_differentes_reussissent() {
        service.executer(demande("G7", "PLEIN"));
        horloge.avancerDeMinutes(1);
        service.executer(demande("G8", "PLEIN"));

        assertThat(seances.parId(S42).orElseThrow().placesLibresA(le12Novembre("20:03"))).hasSize(18);
        assertThat(commandes.nombre()).isEqualTo(2);
    }
}
