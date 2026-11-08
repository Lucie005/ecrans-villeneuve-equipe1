package com.ecrans.vente.etape8;

import com.ecrans.vente.adapter.in.sortiesflash.AclSortiesFlash;
import com.ecrans.vente.adapter.in.sortiesflash.CommandeSortiesFlash;
import com.ecrans.vente.adapter.in.sortiesflash.CorrespondanceSeances;
import com.ecrans.vente.application.exception.SeanceInconnue;
import com.ecrans.vente.application.port.in.DemandeBillet;
import com.ecrans.vente.application.port.in.VentePartenaire;
import com.ecrans.vente.domaine.commande.Categorie;
import com.ecrans.vente.domaine.commun.Montant;
import com.ecrans.vente.domaine.commun.PlaceId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.ecrans.vente.support.Donnees.S42;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Étape 8 · l'ACL SortiesFlash, sans Spring : leur format (annexe B) traduit dans notre langage. */
class AclSortiesFlashTest {

    private static final String SHOW_REF = "VIL-20261112-2030-S3";

    private AclSortiesFlash acl;

    @BeforeEach
    void preparer() {
        CorrespondanceSeances correspondance = new CorrespondanceSeances();
        correspondance.associer(SHOW_REF, S42);
        acl = new AclSortiesFlash(correspondance);
    }

    private static CommandeSortiesFlash commande(String showRef, List<String> seats, List<String> types) {
        return new CommandeSortiesFlash(showRef, seats, types, "SF-77812", 1840);
    }

    @Test
    void traduit_la_vente_de_l_annexe_B() {
        VentePartenaire vente = acl.traduire(commande(SHOW_REF, List.of("G7", "G8"), List.of("ADULT", "STUDENT")));

        assertThat(vente).isEqualTo(new VentePartenaire("SortiesFlash", "SF-77812", S42,
                List.of(new DemandeBillet(PlaceId.de("G7"), Categorie.PLEIN),
                        new DemandeBillet(PlaceId.de("G8"), Categorie.REDUIT)),
                Montant.centimes(1840)));
    }

    @Test
    void CHILD_devient_ENFANT() {
        VentePartenaire vente = acl.traduire(commande(SHOW_REF, List.of("G7"), List.of("CHILD")));

        assertThat(vente.billets().get(0).categorie()).isEqualTo(Categorie.ENFANT);
    }

    @Test
    void le_montant_paye_chez_le_partenaire_est_conserve() {
        VentePartenaire vente = acl.traduire(commande(SHOW_REF, List.of("G7"), List.of("ADULT")));

        assertThat(vente.montantPaye()).isEqualTo(Montant.centimes(1840));
    }

    @Test
    void un_show_ref_inconnu_est_une_seance_inconnue() {
        assertThatThrownBy(() -> acl.traduire(commande("VIL-19990101-2030-S3", List.of("G7"), List.of("ADULT"))))
                .isInstanceOf(SeanceInconnue.class);
    }

    @Test
    void un_type_de_billet_inconnu_est_refuse() {
        assertThatThrownBy(() -> acl.traduire(commande(SHOW_REF, List.of("G7"), List.of("VIP"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void il_faut_autant_de_types_que_de_places() {
        assertThatThrownBy(() -> acl.traduire(commande(SHOW_REF, List.of("G7", "G8"), List.of("ADULT"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void une_place_mal_ecrite_est_refusee() {
        assertThatThrownBy(() -> acl.traduire(commande(SHOW_REF, List.of("7G"), List.of("ADULT"))))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
