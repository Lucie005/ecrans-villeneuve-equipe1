package com.ecrans.vente.adapter.in.web;

import com.ecrans.vente.application.port.in.ReserverDesPlaces;
import com.ecrans.vente.application.port.in.DemandeBillet;
import com.ecrans.vente.application.port.in.DemandeReservation;
import com.ecrans.vente.application.port.in.Reservation;
import com.ecrans.vente.domaine.commande.Categorie;
import com.ecrans.vente.domaine.commun.PlaceId;
import com.ecrans.vente.domaine.commun.SeanceId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ReservationController {

    private final ReserverDesPlaces reserverDesPlaces;

    public ReservationController(ReserverDesPlaces reserverDesPlaces) {
        this.reserverDesPlaces = reserverDesPlaces;
    }

    @PostMapping("/seances/{seance}/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationReponse reserver(
            @PathVariable String seance, 
            @RequestBody ReservationRequete requete) {

        // 1. Traduction du langage HTTP vers le langage du Domaine
        SeanceId seanceId = new SeanceId(seance);
        
        List<DemandeBillet> demandeBillets = requete.billets().stream()
                .map(b -> new DemandeBillet(PlaceId.de(b.place()), Categorie.valueOf(b.categorie())))
                .toList();

        DemandeReservation demande = new DemandeReservation(seanceId, demandeBillets);

        // 2. Appel du port d'entrée (Cas d'usage)
        Reservation reservation = reserverDesPlaces.executer(demande);

        // 3. Traduction de la réponse du Domaine vers le langage HTTP
        return new ReservationReponse(
                reservation.commande().valeur(),
                reservation.total().centimes(),
                reservation.total().toString(),
                reservation.urlPaiement(),
                reservation.payerAvant()
        );
    }
}