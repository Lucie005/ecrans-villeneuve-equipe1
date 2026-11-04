package com.ecrans.vente.etape5;

import com.ecrans.vente.adapter.out.evenement.PublicateurEnMemoire;
import com.ecrans.vente.adapter.out.memoire.CommandesEnMemoire;
import com.ecrans.vente.adapter.out.memoire.SeancesEnMemoire;
import com.ecrans.vente.application.service.ExpirerBlocagesService;
import com.ecrans.vente.domaine.commande.Categorie;
import com.ecrans.vente.domaine.commande.Commande;
import com.ecrans.vente.domaine.commande.StatutCommande;
import com.ecrans.vente.domaine.commun.PlaceId;
import com.ecrans.vente.domaine.evenement.CommandeExpiree;
import com.ecrans.vente.domaine.seance.Seance;
import com.ecrans.vente.domaine.seance.StatutPlace;
import com.ecrans.vente.support.HorlogeReglable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.ecrans.vente.support.Donnees.*;
import static org.assertj.core.api.Assertions.assertThat;

/** Étape 5 · le cas d'usage « Expirer les blocages », déclenché chaque minute par un planificateur (B2). */
class ExpirerBlocagesTest {

    private SeancesEnMemoire seances;
    private CommandesEnMemoire commandes;
    private PublicateurEnMemoire publicateur;
    private HorlogeReglable horloge;
    private ExpirerBlocagesService service;

    @BeforeEach
    void preparer() {
        seances = new SeancesEnMemoire();
        commandes = new CommandesEnMemoire();
        publicateur = new PublicateurEnMemoire();
        horloge = HorlogeReglable.a("20:02");

        // C-1 bloque G7 et G8 à 20 h 02 (jusqu'à 20 h 12) ; C-2 bloque F1 à 20 h 05 (jusqu'à 20 h 15).
        Seance seance = seanceS42();
        seance.bloquer(places("G7", "G8"), C1, le12Novembre("20:02"));
        seance.bloquer(places("F1"), C2, le12Novembre("20:05"));
        seances.enregistrer(seance);
        commandes.enregistrer(Commande.creer(C1, S42, g7PleinG8Reduit(), le12Novembre("20:12")));
        commandes.enregistrer(Commande.creer(C2, S42, List.of(billet("F1", Categorie.PLEIN, 1150)), le12Novembre("20:15")));

        service = new ExpirerBlocagesService(seances, commandes, publicateur, horloge);
    }

    @Test
    void avant_l_heure_limite_rien_ne_se_passe() {
        horloge.reglerA("20:11");

        service.executer();

        assertThat(commandes.parId(C1).orElseThrow().statut()).isEqualTo(StatutCommande.EN_ATTENTE_PAIEMENT);
        assertThat(publicateur.publies()).isEmpty();
    }

    @Test
    void apres_l_heure_limite_les_places_sont_liberees() {
        horloge.reglerA("20:13");

        service.executer();

        Seance seance = seances.parId(S42).orElseThrow();
        assertThat(seance.etat(PlaceId.de("G7")).statut()).isEqualTo(StatutPlace.LIBRE);
        assertThat(seance.etat(PlaceId.de("G8")).statut()).isEqualTo(StatutPlace.LIBRE);
        assertThat(seance.etat(PlaceId.de("F1")).statut()).isEqualTo(StatutPlace.BLOQUEE);
    }

    @Test
    void apres_l_heure_limite_la_commande_expire_et_l_evenement_est_publie() {
        horloge.reglerA("20:13");

        service.executer();

        assertThat(commandes.parId(C1).orElseThrow().statut()).isEqualTo(StatutCommande.EXPIREE);
        assertThat(commandes.parId(C2).orElseThrow().statut()).isEqualTo(StatutCommande.EN_ATTENTE_PAIEMENT);
        assertThat(publicateur.publies()).containsExactly(new CommandeExpiree(C1, S42, le12Novembre("20:13")));
    }

    @Test
    void un_second_passage_ne_republie_rien() {
        horloge.reglerA("20:13");
        service.executer();
        horloge.reglerA("20:14");

        service.executer();

        assertThat(publicateur.publies()).hasSize(1);
    }
}
