package com.ecrans.vente.etape4;

import com.ecrans.vente.adapter.out.evenement.PublicateurEnMemoire;
import com.ecrans.vente.adapter.out.memoire.CommandesEnMemoire;
import com.ecrans.vente.adapter.out.memoire.SeancesEnMemoire;
import com.ecrans.vente.application.exception.CommandeInconnue;
import com.ecrans.vente.application.service.ConfirmerPaiementService;
import com.ecrans.vente.domaine.commande.Commande;
import com.ecrans.vente.domaine.commande.StatutCommande;
import com.ecrans.vente.domaine.commun.CommandeId;
import com.ecrans.vente.domaine.commun.Montant;
import com.ecrans.vente.domaine.commun.PlaceId;
import com.ecrans.vente.domaine.evenement.CommandePayee;
import com.ecrans.vente.domaine.seance.Seance;
import com.ecrans.vente.domaine.seance.StatutPlace;
import com.ecrans.vente.support.HorlogeReglable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.ecrans.vente.support.Donnees.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Étape 4 · le cas d'usage « Confirmer le paiement » : la commande est payée ET les places vendues. */
class ConfirmerPaiementTest {

    private SeancesEnMemoire seances;
    private CommandesEnMemoire commandes;
    private PublicateurEnMemoire publicateur;
    private HorlogeReglable horloge;
    private ConfirmerPaiementService service;

    @BeforeEach
    void preparer() {
        seances = new SeancesEnMemoire();
        commandes = new CommandesEnMemoire();
        publicateur = new PublicateurEnMemoire();
        horloge = HorlogeReglable.a("20:09");

        // État de départ : à 20 h 02, la commande C-1 a bloqué G7 et G8 jusqu'à 20 h 12.
        Seance seance = seanceS42();
        seance.bloquer(places("G7", "G8"), C1, le12Novembre("20:02"));
        seances.enregistrer(seance);
        commandes.enregistrer(Commande.creer(C1, S42, g7PleinG8Reduit(), le12Novembre("20:12")));

        service = new ConfirmerPaiementService(seances, commandes, publicateur, horloge);
    }

    @Test
    void la_commande_devient_payee() {
        service.executer(C1);

        assertThat(commandes.parId(C1).orElseThrow().statut()).isEqualTo(StatutCommande.PAYEE);
    }

    @Test
    void les_places_sont_vendues() {
        service.executer(C1);

        Seance seance = seances.parId(S42).orElseThrow();
        assertThat(seance.etat(PlaceId.de("G7")).statut()).isEqualTo(StatutPlace.VENDUE);
        assertThat(seance.etat(PlaceId.de("G8")).statut()).isEqualTo(StatutPlace.VENDUE);
    }

    @Test
    void l_evenement_CommandePayee_est_publie() {
        service.executer(C1);

        assertThat(publicateur.publies()).containsExactly(
                new CommandePayee(C1, S42, places("G7", "G8"), Montant.centimes(2050), le12Novembre("20:09")));
    }

    @Test
    void une_commande_inconnue_est_signalee() {
        assertThatThrownBy(() -> service.executer(new CommandeId("C-404")))
                .isInstanceOf(CommandeInconnue.class);
        assertThat(publicateur.publies()).isEmpty();
    }
}
