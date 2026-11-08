package com.ecrans.vente.config;

import com.ecrans.vente.adapter.in.payvia.WebhookPayVia;
import com.ecrans.vente.adapter.in.sortiesflash.AclSortiesFlash;
import com.ecrans.vente.application.port.in.ConfirmerPaiement;
import com.ecrans.vente.application.port.in.EnregistrerVentePartenaire;
import com.ecrans.vente.application.port.in.ExpirerBlocages;
import com.ecrans.vente.application.port.in.ReserverDesPlaces;
import com.ecrans.vente.domaine.commun.PlaceId;
import com.ecrans.vente.domaine.commun.SeanceId;
import com.ecrans.vente.domaine.seance.Seance;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class VenteConfiguration {

    @Bean
    Clock horloge() {
        return Clock.systemDefaultZone();
    }

    @Bean
    ApplicationVente applicationVente(Clock horloge) {
        return new ApplicationVente(horloge);
    }

    @Bean
    ReserverDesPlaces reserverDesPlaces(ApplicationVente app) {
        return app.reserverDesPlaces;
    }

    @Bean
    ConfirmerPaiement confirmerPaiement(ApplicationVente app) {
        return app.confirmerPaiement;
    }

    @Bean
    ExpirerBlocages expirerBlocages(ApplicationVente app) {
        return app.expirerBlocages;
    }

    @Bean
    EnregistrerVentePartenaire enregistrerVentePartenaire(ApplicationVente app) {
        return app.enregistrerVentePartenaire;
    }

    @Bean
    WebhookPayVia webhookPayVia(ApplicationVente app) {
        return app.webhookPayVia;
    }

    @Bean
    AclSortiesFlash aclSortiesFlash(ApplicationVente app) {
        return app.aclSortiesFlash;
    }

    /** La Programmation n'existe pas encore : on publie à la main la séance S42,
     *  demain à 20 h 30, salle 3 (rangs A à H, fauteuils 1 à 12). */
    @Bean
    CommandLineRunner seanceDeDemonstration(ApplicationVente app) {
        return args -> {
            List<PlaceId> plan = new ArrayList<>();
            for (char rang = 'A'; rang <= 'H'; rang++) {
                for (int n = 1; n <= 12; n++) plan.add(new PlaceId(rang, n));
            }
            LocalDate demain = LocalDate.now().plusDays(1);
            SeanceId s42 = new SeanceId("S42");
            app.seances.enregistrer(Seance.ouverte(s42, demain.atTime(20, 30), plan));

            // Chez SortiesFlash, la même séance s'appelle « VIL-aaaammjj-2030-S3 » (annexe B)
            app.correspondanceSeances.associer(
                    "VIL-" + demain.format(DateTimeFormatter.BASIC_ISO_DATE) + "-2030-S3", s42);
        };
    }
}
