package com.ecrans.vente.etape8;

import com.ecrans.vente.adapter.out.evenement.PublicateurEnMemoire;
import com.ecrans.vente.adapter.out.memoire.CommandesEnMemoire;
import com.ecrans.vente.adapter.out.memoire.SeancesEnMemoire;
import com.ecrans.vente.adapter.out.tarification.GrilleTarifaireFixe;
import com.ecrans.vente.application.exception.SeanceInconnue;
import com.ecrans.vente.application.port.in.VentePartenaire;
import com.ecrans.vente.application.service.EnregistrerVentePartenaireService;
import com.ecrans.vente.domaine.commande.StatutCommande;
import com.ecrans.vente.domaine.commande.TropDePlaces;
import com.ecrans.vente.domaine.commun.CommandeId;
import com.ecrans.vente.domaine.commun.Montant;
import com.ecrans.vente.domaine.commun.PlaceId;
import com.ecrans.vente.domaine.commun.SeanceId;
import com.ecrans.vente.domaine.evenement.CommandePayee;
import com.ecrans.vente.domaine.evenement.VentePartenaireEnregistree;
import com.ecrans.vente.domaine.seance.PlacesIndisponibles;
import com.ecrans.vente.domaine.seance.Seance;
import com.ecrans.vente.domaine.seance.StatutPlace;
import com.ecrans.vente.domaine.seance.VenteFermee;
import com.ecrans.vente.support.HorlogeReglable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.ecrans.vente.support.Donnees.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Étape 8 · le cas d'usage « Enregistrer une vente partenaire », sans Spring : une vente déjà payée (B11). */
class EnregistrerVentePartenaireTest {

    private SeancesEnMemoire seances;
    private CommandesEnMemoire commandes;
    private PublicateurEnMemoire publicateur;
    private HorlogeReglable horloge;
    private EnregistrerVentePartenaireService service;

    @BeforeEach
    void preparer() {
        seances = new SeancesEnMemoire();
        commandes = new CommandesEnMemoire();
        publicateur = new PublicateurEnMemoire();
        horloge = HorlogeReglable.a("20:02");
        seances.enregistrer(seanceS42());
        service = new EnregistrerVentePartenaireService(seances, commandes, new GrilleTarifaireFixe(), publicateur,
                horloge);
    }

    /** G7 adulte + G8 étudiant, payés 18,40 € chez SortiesFlash (remise comprise). */
    private static VentePartenaire venteG7G8() {
        return vente(S42, 1840, "G7", "PLEIN", "G8", "REDUIT");
    }

    private static VentePartenaire vente(SeanceId seance, long centimesPayes, String... placesEtCategories) {
        return new VentePartenaire("SortiesFlash", "SF-77812", seance, demande(placesEtCategories).billets(),
                Montant.centimes(centimesPayes));
    }

    @Test
    void les_places_sont_vendues_tout_de_suite_sans_blocage() {
        CommandeId commande = service.executer(venteG7G8());

        Seance seance = seances.parId(S42).orElseThrow();
        assertThat(seance.etat(PlaceId.de("G7")).statut()).isEqualTo(StatutPlace.VENDUE);
        assertThat(seance.etat(PlaceId.de("G8")).statut()).isEqualTo(StatutPlace.VENDUE);
        assertThat(seance.etat(PlaceId.de("G8")).commande()).isEqualTo(commande);
    }

    @Test
    void la_commande_est_enregistree_payee_au_prix_de_la_grille() {
        CommandeId commande = service.executer(venteG7G8());

        assertThat(commandes.parId(commande).orElseThrow().statut()).isEqualTo(StatutCommande.PAYEE);
        assertThat(commandes.parId(commande).orElseThrow().total()).isEqualTo(Montant.centimes(2050));
    }

    @Test
    void CommandePayee_puis_VentePartenaireEnregistree_avec_le_montant_paye_chez_le_partenaire() {
        CommandeId commande = service.executer(venteG7G8());

        assertThat(publicateur.publies()).containsExactly(
                new CommandePayee(commande, S42, places("G7", "G8"), Montant.centimes(2050), le12Novembre("20:02")),
                new VentePartenaireEnregistree(commande, "SortiesFlash", "SF-77812", Montant.centimes(1840),
                        le12Novembre("20:02")));
    }

    @Test
    void une_place_vendue_par_un_partenaire_ne_peut_plus_etre_vendue_B1() {
        service.executer(vente(S42, 1150, "G7", "PLEIN"));

        assertThatThrownBy(() -> service.executer(vente(S42, 1150, "G7", "PLEIN")))
                .isInstanceOf(PlacesIndisponibles.class);
    }

    @Test
    void si_une_seule_place_est_prise_toute_la_vente_est_refusee_B11() {
        service.executer(vente(S42, 1150, "G7", "PLEIN"));

        assertThatThrownBy(() -> service.executer(venteG7G8())).isInstanceOf(PlacesIndisponibles.class);
        assertThat(seances.parId(S42).orElseThrow().etat(PlaceId.de("G8")).statut()).isEqualTo(StatutPlace.LIBRE);
        assertThat(commandes.nombre()).isEqualTo(1);
        assertThat(publicateur.publies()).hasSize(2); // seulement ceux de la première vente
    }

    @Test
    void plus_de_10_places_sont_refusees_et_rien_n_est_vendu_B3() {
        VentePartenaire onze = vente(S42, 0, "F1", "PLEIN", "F2", "PLEIN", "F3", "PLEIN", "F4", "PLEIN",
                "F5", "PLEIN", "F6", "PLEIN", "F7", "PLEIN", "F8", "PLEIN", "F9", "PLEIN", "F10", "PLEIN", "G1", "PLEIN");

        assertThatThrownBy(() -> service.executer(onze)).isInstanceOf(TropDePlaces.class);
        assertThat(seances.parId(S42).orElseThrow().placesLibresA(le12Novembre("20:02"))).hasSize(20);
    }

    @Test
    void la_vente_partenaire_respecte_aussi_la_fermeture_B4() {
        horloge.reglerA("20:46");

        assertThatThrownBy(() -> service.executer(venteG7G8())).isInstanceOf(VenteFermee.class);
        assertThat(commandes.nombre()).isZero();
        assertThat(publicateur.publies()).isEmpty();
    }

    @Test
    void une_seance_inconnue_est_signalee() {
        assertThatThrownBy(() -> service.executer(vente(new SeanceId("S99"), 1150, "G7", "PLEIN")))
                .isInstanceOf(SeanceInconnue.class);
    }
}
