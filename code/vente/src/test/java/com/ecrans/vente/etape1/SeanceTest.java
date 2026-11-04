package com.ecrans.vente.etape1;

import com.ecrans.vente.domaine.commun.PlaceId;
import com.ecrans.vente.domaine.seance.EtatPlace;
import com.ecrans.vente.domaine.seance.PlaceInconnue;
import com.ecrans.vente.domaine.seance.PlacesIndisponibles;
import com.ecrans.vente.domaine.seance.Seance;
import com.ecrans.vente.domaine.seance.StatutPlace;
import com.ecrans.vente.domaine.seance.VenteFermee;
import org.junit.jupiter.api.Test;

import static com.ecrans.vente.support.Donnees.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Étape 1 · l'agrégat Seance protège B1, B2 et B4. Aucun framework, aucune base : du Java. */
class SeanceTest {

    @Test
    void bloque_des_places_libres_pendant_10_minutes_B2() {
        Seance seance = seanceS42();

        var jusqua = seance.bloquer(places("G7", "G8"), C1, le12Novembre("20:02"));

        assertThat(jusqua).isEqualTo(le12Novembre("20:12"));
        assertThat(seance.etat(PlaceId.de("G7"))).isEqualTo(EtatPlace.bloquee(C1, le12Novembre("20:12")));
        assertThat(seance.etat(PlaceId.de("G8")).statut()).isEqualTo(StatutPlace.BLOQUEE);
    }

    @Test
    void refuse_une_place_deja_bloquee_par_un_autre_client_B1() {
        Seance seance = seanceS42();
        seance.bloquer(places("G7"), C1, le12Novembre("20:02"));

        assertThatThrownBy(() -> seance.bloquer(places("G7", "G8"), C2, le12Novembre("20:05")))
                .isInstanceOf(PlacesIndisponibles.class);
    }

    @Test
    void tout_ou_rien_si_une_place_est_prise_aucune_n_est_bloquee() {
        Seance seance = seanceS42();
        seance.bloquer(places("G7"), C1, le12Novembre("20:02"));

        try {
            seance.bloquer(places("G7", "G8"), C2, le12Novembre("20:05"));
        } catch (PlacesIndisponibles attendue) {
            // attendu
        }

        assertThat(seance.etat(PlaceId.de("G8")).statut()).isEqualTo(StatutPlace.LIBRE);
    }

    @Test
    void indique_toutes_les_places_deja_prises() {
        Seance seance = seanceS42();
        seance.bloquer(places("G7", "G8"), C1, le12Novembre("20:02"));

        assertThatThrownBy(() -> seance.bloquer(places("G6", "G7", "G8"), C2, le12Novembre("20:05")))
                .isInstanceOfSatisfying(PlacesIndisponibles.class,
                        e -> assertThat(e.places()).isEqualTo(places("G7", "G8")));
    }

    @Test
    void refuse_une_place_vendue_B1() {
        Seance seance = seanceS42();
        seance.bloquer(places("G7"), C1, le12Novembre("20:02"));
        seance.vendre(C1, places("G7"), le12Novembre("20:05"));

        assertThatThrownBy(() -> seance.bloquer(places("G7"), C2, le12Novembre("20:20")))
                .isInstanceOf(PlacesIndisponibles.class);
    }

    @Test
    void une_place_dont_le_blocage_a_expire_peut_etre_bloquee_par_un_autre_client_B2() {
        Seance seance = seanceS42();
        seance.bloquer(places("G7"), C1, le12Novembre("20:02"));

        seance.bloquer(places("G7"), C2, le12Novembre("20:12"));

        assertThat(seance.etat(PlaceId.de("G7")).commande()).isEqualTo(C2);
    }

    @Test
    void la_vente_est_ouverte_jusqu_a_15_minutes_apres_le_debut_B4() {
        Seance seance = seanceS42();

        seance.bloquer(places("G7"), C1, le12Novembre("20:45"));

        assertThat(seance.etat(PlaceId.de("G7")).statut()).isEqualTo(StatutPlace.BLOQUEE);
    }

    @Test
    void la_vente_est_fermee_au_dela_B4() {
        Seance seance = seanceS42();

        assertThatThrownBy(() -> seance.bloquer(places("G7"), C1, le12Novembre("20:46")))
                .isInstanceOf(VenteFermee.class);
    }

    @Test
    void refuse_une_place_qui_n_existe_pas() {
        Seance seance = seanceS42();

        assertThatThrownBy(() -> seance.bloquer(places("Z99"), C1, le12Novembre("20:02")))
                .isInstanceOf(PlaceInconnue.class);
    }

    @Test
    void vend_les_places_bloquees_par_la_commande() {
        Seance seance = seanceS42();
        seance.bloquer(places("G7", "G8"), C1, le12Novembre("20:02"));

        seance.vendre(C1, places("G7", "G8"), le12Novembre("20:09"));

        assertThat(seance.etat(PlaceId.de("G7"))).isEqualTo(EtatPlace.vendue(C1));
        assertThat(seance.etat(PlaceId.de("G8"))).isEqualTo(EtatPlace.vendue(C1));
    }

    @Test
    void ne_vend_pas_une_place_bloquee_par_une_autre_commande() {
        Seance seance = seanceS42();
        seance.bloquer(places("G7"), C2, le12Novembre("20:02"));

        assertThatThrownBy(() -> seance.vendre(C1, places("G7"), le12Novembre("20:05")))
                .isInstanceOf(PlacesIndisponibles.class);
    }

    @Test
    void libere_les_blocages_expires_et_renvoie_les_commandes_concernees_B2() {
        Seance seance = seanceS42();
        seance.bloquer(places("G7", "G8"), C1, le12Novembre("20:02"));
        seance.bloquer(places("F1"), C2, le12Novembre("20:05"));

        var commandes = seance.libererBlocagesExpires(le12Novembre("20:13"));

        assertThat(commandes).containsExactly(C1);
        assertThat(seance.etat(PlaceId.de("G7")).statut()).isEqualTo(StatutPlace.LIBRE);
        assertThat(seance.etat(PlaceId.de("G8")).statut()).isEqualTo(StatutPlace.LIBRE);
        assertThat(seance.etat(PlaceId.de("F1")).statut()).isEqualTo(StatutPlace.BLOQUEE);
    }

    @Test
    void ne_libere_pas_les_places_vendues() {
        Seance seance = seanceS42();
        seance.bloquer(places("G7"), C1, le12Novembre("20:02"));
        seance.vendre(C1, places("G7"), le12Novembre("20:05"));

        var commandes = seance.libererBlocagesExpires(le12Novembre("20:30"));

        assertThat(commandes).isEmpty();
        assertThat(seance.etat(PlaceId.de("G7")).statut()).isEqualTo(StatutPlace.VENDUE);
    }

}
