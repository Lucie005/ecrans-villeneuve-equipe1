package com.ecrans.vente.bonus;

import com.ecrans.vente.adapter.in.payvia.NotificationPayVia;
import com.ecrans.vente.application.port.in.Reservation;
import com.ecrans.vente.config.ApplicationVente;
import com.ecrans.vente.domaine.commande.StatutCommande;
import com.ecrans.vente.support.HorlogeReglable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.ecrans.vente.support.Donnees.*;
import static org.assertj.core.api.Assertions.assertThat;

/** Bonus B14 · PayVia notifie « au moins une fois » : la même notification peut arriver deux fois. */
@Disabled("Bonus : supprimez cette ligne pour activer ces tests")
class PaiementEnDoubleTest {

    @Test
    void la_deuxieme_notification_ne_change_rien() {
        HorlogeReglable horloge = HorlogeReglable.a("20:02");
        ApplicationVente app = new ApplicationVente(horloge);
        app.seances.enregistrer(seanceS42());
        Reservation reservation = app.reserverDesPlaces.executer(demande("G7", "PLEIN"));
        NotificationPayVia notification = new NotificationPayVia("payment.succeeded", "pv_1",
                reservation.commande().valeur(), 1150, "EUR", "2026-11-12T19:05:00Z");

        horloge.reglerA("20:05");
        app.webhookPayVia.recevoir(notification);
        horloge.reglerA("20:06");
        app.webhookPayVia.recevoir(notification);

        assertThat(app.commandes.parId(reservation.commande()).orElseThrow().statut()).isEqualTo(StatutCommande.PAYEE);
        assertThat(app.publicateur.publies()).hasSize(1);
    }

    @Test
    void un_paiement_refuse_ne_confirme_rien() {
        HorlogeReglable horloge = HorlogeReglable.a("20:02");
        ApplicationVente app = new ApplicationVente(horloge);
        app.seances.enregistrer(seanceS42());
        Reservation reservation = app.reserverDesPlaces.executer(demande("G7", "PLEIN"));

        app.webhookPayVia.recevoir(new NotificationPayVia("payment.failed", "pv_1",
                reservation.commande().valeur(), 1150, "EUR", "2026-11-12T19:05:00Z"));

        assertThat(app.commandes.parId(reservation.commande()).orElseThrow().statut())
                .isEqualTo(StatutCommande.EN_ATTENTE_PAIEMENT);
    }
}
