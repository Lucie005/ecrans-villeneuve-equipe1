package com.ecrans.vente;

import com.ecrans.vente.adapter.in.payvia.NotificationPayVia;
import com.ecrans.vente.application.port.in.Reservation;
import com.ecrans.vente.config.ApplicationVente;
import com.ecrans.vente.domaine.commun.PlaceId;
import com.ecrans.vente.domaine.evenement.CommandePayee;
import com.ecrans.vente.domaine.seance.PlacesIndisponibles;
import com.ecrans.vente.domaine.seance.StatutPlace;
import com.ecrans.vente.support.HorlogeReglable;
import org.junit.jupiter.api.Test;

import static com.ecrans.vente.support.Donnees.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Le test final : toute l'application branchée par la composition root, de la réservation au webhook PayVia.
 * Il passe quand les étapes 1 à 5 sont terminées.
 */
class ScenarioSoireeDuJeudiTest {

    @Test
    void une_spectatrice_reserve_paie_et_ses_places_ne_peuvent_plus_etre_vendues() {
        HorlogeReglable horloge = HorlogeReglable.a("20:02");
        ApplicationVente app = new ApplicationVente(horloge);
        app.seances.enregistrer(seanceS42());

        // 20 h 02 : elle choisit G7 (plein) et G8 (réduit) sur le site
        Reservation reservation = app.reserverDesPlaces.executer(demande("G7", "PLEIN", "G8", "REDUIT"));

        // 20 h 09 : PayVia nous notifie, dans SON format
        horloge.reglerA("20:09");
        app.webhookPayVia.recevoir(new NotificationPayVia("payment.succeeded", "pv_8f2kQ71",
                reservation.commande().valeur(), 2050, "EUR", "2026-11-12T19:09:41Z"));

        // 20 h 13 : le planificateur passe ; rien ne doit être libéré
        horloge.reglerA("20:13");
        app.expirerBlocages.executer();

        assertThat(app.seances.parId(S42).orElseThrow().etat(PlaceId.de("G7")).statut()).isEqualTo(StatutPlace.VENDUE);
        assertThat(app.publicateur.publies()).singleElement().isInstanceOf(CommandePayee.class);

        // 20 h 20 : un client à la borne veut G7
        horloge.reglerA("20:20");
        assertThatThrownBy(() -> app.reserverDesPlaces.executer(demande("G7", "PLEIN")))
                .isInstanceOf(PlacesIndisponibles.class);
    }

    @Test
    void sans_paiement_les_places_reviennent_a_la_vente() {
        HorlogeReglable horloge = HorlogeReglable.a("20:02");
        ApplicationVente app = new ApplicationVente(horloge);
        app.seances.enregistrer(seanceS42());

        app.reserverDesPlaces.executer(demande("G7", "PLEIN", "G8", "REDUIT"));
        horloge.reglerA("20:13");
        app.expirerBlocages.executer();

        app.reserverDesPlaces.executer(demande("G7", "PLEIN"));
        assertThat(app.seances.parId(S42).orElseThrow().etat(PlaceId.de("G8")).statut()).isEqualTo(StatutPlace.LIBRE);
    }
}
