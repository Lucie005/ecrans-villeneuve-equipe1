package com.ecrans.vente.bonus;

import com.ecrans.vente.adapter.out.memoire.CommandesEnMemoire;
import com.ecrans.vente.adapter.out.memoire.SeancesEnMemoire;
import com.ecrans.vente.adapter.out.paiement.PaiementsSimules;
import com.ecrans.vente.adapter.out.tarification.GrilleTarifaireFixe;
import com.ecrans.vente.application.port.out.ConflitDeVersion;
import com.ecrans.vente.application.service.ReserverDesPlacesService;
import com.ecrans.vente.domaine.commun.CommandeId;
import com.ecrans.vente.domaine.commun.PlaceId;
import com.ecrans.vente.domaine.seance.PlacesIndisponibles;
import com.ecrans.vente.domaine.seance.Seance;
import com.ecrans.vente.domaine.seance.StatutPlace;
import com.ecrans.vente.support.HorlogeReglable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.ecrans.vente.support.Donnees.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Bonus ADR-0002 · le soir de l'avant-première, deux ventes chargent la même séance au même moment.
 * Le verrou optimiste de l'adapter fait échouer la seconde à enregistrer : le service doit recharger et réessayer.
 */
@Disabled("Bonus : supprimez cette ligne pour activer ces tests")
class ConcurrenceTest {

    /** Une base « sous pression » : juste avant notre enregistrement, une autre caisse vend une place. */
    static class SeancesSousPression extends SeancesEnMemoire {
        private String placeVendueParUneAutreCaisse;

        void uneAutreCaisseBloqueJusteAvant(String place) {
            this.placeVendueParUneAutreCaisse = place;
        }

        @Override
        public synchronized void enregistrer(Seance seance) {
            if (placeVendueParUneAutreCaisse != null) {
                String place = placeVendueParUneAutreCaisse;
                placeVendueParUneAutreCaisse = null;
                Seance chezLAutreCaisse = parId(seance.id()).orElseThrow();
                chezLAutreCaisse.bloquer(places(place), new CommandeId("C-guichet"), le12Novembre("20:02"));
                super.enregistrer(chezLAutreCaisse);
            }
            super.enregistrer(seance);
        }
    }

    private SeancesSousPression seances;
    private ReserverDesPlacesService service;

    @BeforeEach
    void preparer() {
        seances = new SeancesSousPression();
        seances.enregistrer(seanceS42());
        service = new ReserverDesPlacesService(seances, new CommandesEnMemoire(), new GrilleTarifaireFixe(),
                new PaiementsSimules(), HorlogeReglable.a("20:02"));
    }

    @Test
    void l_adapter_refuse_d_enregistrer_une_seance_perimee() {
        Seance a = seances.parId(S42).orElseThrow();
        Seance b = seances.parId(S42).orElseThrow();
        a.bloquer(places("G7"), C1, le12Novembre("20:02"));
        b.bloquer(places("G8"), C2, le12Novembre("20:02"));
        seances.enregistrer(a);

        assertThatThrownBy(() -> seances.enregistrer(b)).isInstanceOf(ConflitDeVersion.class);
    }

    @Test
    void places_differentes_le_service_reessaie_et_reussit() {
        seances.uneAutreCaisseBloqueJusteAvant("F1");

        service.executer(demande("G7", "PLEIN"));

        Seance seance = seances.parId(S42).orElseThrow();
        assertThat(seance.etat(PlaceId.de("G7")).statut()).isEqualTo(StatutPlace.BLOQUEE);
        assertThat(seance.etat(PlaceId.de("F1")).statut()).isEqualTo(StatutPlace.BLOQUEE);
    }

    @Test
    void meme_place_le_second_essai_constate_qu_elle_est_prise() {
        seances.uneAutreCaisseBloqueJusteAvant("G7");

        assertThatThrownBy(() -> service.executer(demande("G7", "PLEIN")))
                .isInstanceOf(PlacesIndisponibles.class);
    }
}
