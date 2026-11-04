package com.ecrans.vente.bonus;

import com.ecrans.vente.application.port.in.Reservation;
import com.ecrans.vente.config.ApplicationVente;
import com.ecrans.vente.domaine.commande.StatutCommande;
import com.ecrans.vente.domaine.commun.Montant;
import com.ecrans.vente.domaine.commun.PlaceId;
import com.ecrans.vente.domaine.evenement.CommandeExpiree;
import com.ecrans.vente.domaine.evenement.CommandePayee;
import com.ecrans.vente.domaine.evenement.PaiementARembourser;
import com.ecrans.vente.domaine.seance.StatutPlace;
import com.ecrans.vente.support.HorlogeReglable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.ecrans.vente.support.Donnees.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bonus B13 · le lien PayVia vit 15 minutes, le blocage 10. Règle B13 :
 * « Si les places sont encore libres, on les reprend et on vend. Sinon on rembourse tout de suite. »
 */
@Disabled("Bonus : supprimez cette ligne pour activer ces tests")
class PaiementTardifTest {

    private HorlogeReglable horloge;
    private ApplicationVente app;
    private Reservation reservation;

    @BeforeEach
    void preparer() {
        horloge = HorlogeReglable.a("20:02");
        app = new ApplicationVente(horloge);
        app.seances.enregistrer(seanceS42());
        reservation = app.reserverDesPlaces.executer(demande("G7", "PLEIN", "G8", "REDUIT"));
        horloge.reglerA("20:13");
        app.expirerBlocages.executer(); // la commande expire, G7 et G8 sont libérées
    }

    @Test
    void places_encore_libres_on_vend_quand_meme() {
        horloge.reglerA("20:14");
        app.confirmerPaiement.executer(reservation.commande());

        assertThat(app.commandes.parId(reservation.commande()).orElseThrow().statut()).isEqualTo(StatutCommande.PAYEE);
        assertThat(app.seances.parId(S42).orElseThrow().etat(PlaceId.de("G7")).statut()).isEqualTo(StatutPlace.VENDUE);
        assertThat(app.publicateur.publies().get(1)).isInstanceOf(CommandePayee.class);
    }

    @Test
    void places_reprises_entre_temps_on_rembourse() {
        app.reserverDesPlaces.executer(demande("G7", "PLEIN")); // 20 h 13 : un autre client prend G7

        horloge.reglerA("20:14");
        app.confirmerPaiement.executer(reservation.commande());

        assertThat(app.commandes.parId(reservation.commande()).orElseThrow().statut()).isEqualTo(StatutCommande.A_REMBOURSER);
        assertThat(app.seances.parId(S42).orElseThrow().etat(PlaceId.de("G8")).statut()).isEqualTo(StatutPlace.LIBRE);
        assertThat(app.publicateur.publies().get(0)).isInstanceOf(CommandeExpiree.class);
        assertThat(app.publicateur.publies().get(1)).isEqualTo(
                new PaiementARembourser(reservation.commande(), Montant.centimes(2050), le12Novembre("20:14")));
    }
}
