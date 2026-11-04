package com.ecrans.vente.support;

import com.ecrans.vente.application.port.in.DemandeBillet;
import com.ecrans.vente.application.port.in.DemandeReservation;
import com.ecrans.vente.domaine.commande.Billet;
import com.ecrans.vente.domaine.commande.Categorie;
import com.ecrans.vente.domaine.commande.Tarif;
import com.ecrans.vente.domaine.commun.CommandeId;
import com.ecrans.vente.domaine.commun.Montant;
import com.ecrans.vente.domaine.commun.PlaceId;
import com.ecrans.vente.domaine.commun.SeanceId;
import com.ecrans.vente.domaine.seance.Seance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** Le jeu de données des tests : la séance S42 du jeudi 12 novembre 2026 à 20 h 30, salle 3. */
public final class Donnees {

    public static final SeanceId S42 = new SeanceId("S42");
    public static final CommandeId C1 = new CommandeId("C-1");
    public static final CommandeId C2 = new CommandeId("C-2");

    private Donnees() {
    }

    public static LocalDateTime le12Novembre(String heure) {
        return LocalDate.of(2026, 11, 12).atTime(LocalTime.parse(heure));
    }

    /** Rangs F et G, fauteuils 1 à 10 : 20 places. */
    public static List<PlaceId> planSalle3() {
        List<PlaceId> plan = new ArrayList<>();
        for (char rang : new char[]{'F', 'G'}) {
            for (int n = 1; n <= 10; n++) plan.add(new PlaceId(rang, n));
        }
        return plan;
    }

    public static Seance seanceS42() {
        return Seance.ouverte(S42, le12Novembre("20:30"), planSalle3());
    }

    public static Set<PlaceId> places(String... codes) {
        Set<PlaceId> s = new TreeSet<>();
        for (String c : codes) s.add(PlaceId.de(c));
        return s;
    }

    public static Billet billet(String place, Categorie categorie, long centimes) {
        return new Billet(PlaceId.de(place), new Tarif(categorie, Montant.centimes(centimes)));
    }

    /** G7 plein tarif + G8 tarif réduit = 11,50 € + 9,00 € = 20,50 €. */
    public static List<Billet> g7PleinG8Reduit() {
        return List.of(billet("G7", Categorie.PLEIN, 1150), billet("G8", Categorie.REDUIT, 900));
    }

    public static DemandeReservation demande(String... placesEtCategories) {
        List<DemandeBillet> billets = new ArrayList<>();
        for (int i = 0; i < placesEtCategories.length; i += 2) {
            billets.add(new DemandeBillet(PlaceId.de(placesEtCategories[i]), Categorie.valueOf(placesEtCategories[i + 1])));
        }
        return new DemandeReservation(S42, billets);
    }
}
