package com.ecrans.vente.etape2;

import com.ecrans.vente.domaine.commande.Billet;
import com.ecrans.vente.domaine.commande.Categorie;
import com.ecrans.vente.domaine.commande.Commande;
import com.ecrans.vente.domaine.commande.CommandeVide;
import com.ecrans.vente.domaine.commande.PlaceEnDouble;
import com.ecrans.vente.domaine.commande.StatutCommande;
import com.ecrans.vente.domaine.commande.TransitionInterdite;
import com.ecrans.vente.domaine.commande.TropDePlaces;
import com.ecrans.vente.domaine.commun.Montant;
import com.ecrans.vente.domaine.evenement.CommandeExpiree;
import com.ecrans.vente.domaine.evenement.CommandePayee;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.ecrans.vente.support.Donnees.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Étape 2 · l'agrégat Commande protège B3, B6 et son cycle de vie. */
class CommandeTest {

    private Commande nouvelleCommande() {
        return Commande.creer(C1, S42, g7PleinG8Reduit(), le12Novembre("20:12"));
    }

    @Test
    void une_nouvelle_commande_attend_son_paiement() {
        Commande commande = nouvelleCommande();

        assertThat(commande.statut()).isEqualTo(StatutCommande.EN_ATTENTE_PAIEMENT);
        assertThat(commande.places()).isEqualTo(places("G7", "G8"));
        assertThat(commande.expireA()).isEqualTo(le12Novembre("20:12"));
    }

    @Test
    void le_total_est_la_somme_des_prix_figes_dans_les_billets_B6() {
        assertThat(nouvelleCommande().total()).isEqualTo(Montant.centimes(2050));
    }

    @Test
    void accepte_10_places() {
        Commande commande = Commande.creer(C1, S42, billets(10), le12Novembre("20:12"));

        assertThat(commande.billets()).hasSize(10);
    }

    @Test
    void refuse_plus_de_10_places_B3() {
        assertThatThrownBy(() -> Commande.creer(C1, S42, billets(11), le12Novembre("20:12")))
                .isInstanceOf(TropDePlaces.class);
    }

    @Test
    void refuse_une_commande_vide() {
        assertThatThrownBy(() -> Commande.creer(C1, S42, List.of(), le12Novembre("20:12")))
                .isInstanceOf(CommandeVide.class);
    }

    @Test
    void refuse_deux_fois_la_meme_place() {
        List<Billet> doublon = List.of(billet("G7", Categorie.PLEIN, 1150), billet("G7", Categorie.REDUIT, 900));

        assertThatThrownBy(() -> Commande.creer(C1, S42, doublon, le12Novembre("20:12")))
                .isInstanceOf(PlaceEnDouble.class);
    }

    @Test
    void payer_rend_la_commande_payee_et_enregistre_l_evenement() {
        Commande commande = nouvelleCommande();

        commande.payer(le12Novembre("20:09"));

        assertThat(commande.statut()).isEqualTo(StatutCommande.PAYEE);
        assertThat(commande.retirerEvenements()).containsExactly(
                new CommandePayee(C1, S42, places("G7", "G8"), Montant.centimes(2050), le12Novembre("20:09")));
    }

    @Test
    void on_ne_paie_pas_deux_fois() {
        Commande commande = nouvelleCommande();
        commande.payer(le12Novembre("20:09"));

        assertThatThrownBy(() -> commande.payer(le12Novembre("20:10")))
                .isInstanceOf(TransitionInterdite.class);
    }

    @Test
    void expirer_rend_la_commande_expiree_et_enregistre_l_evenement() {
        Commande commande = nouvelleCommande();

        commande.expirer(le12Novembre("20:13"));

        assertThat(commande.statut()).isEqualTo(StatutCommande.EXPIREE);
        assertThat(commande.retirerEvenements()).containsExactly(new CommandeExpiree(C1, S42, le12Novembre("20:13")));
    }

    @Test
    void une_commande_payee_n_expire_pas() {
        Commande commande = nouvelleCommande();
        commande.payer(le12Novembre("20:09"));

        assertThatThrownBy(() -> commande.expirer(le12Novembre("20:13")))
                .isInstanceOf(TransitionInterdite.class);
    }

    @Test
    void les_evenements_ne_sont_retires_qu_une_fois() {
        Commande commande = nouvelleCommande();
        commande.payer(le12Novembre("20:09"));
        commande.retirerEvenements();

        assertThat(commande.retirerEvenements()).isEmpty();
    }

    private static List<Billet> billets(int nombre) {
        List<Billet> billets = new ArrayList<>();
        for (int n = 1; n <= nombre; n++) {
            char rang = n <= 10 ? 'G' : 'F';
            billets.add(billet("" + rang + (n <= 10 ? n : n - 10), Categorie.PLEIN, 1150));
        }
        return billets;
    }
}
