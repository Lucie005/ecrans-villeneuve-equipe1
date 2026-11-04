package com.ecrans.vente.application.port.in;

/** Port primaire : bloquer des places et créer la commande en attente de paiement. */
public interface ReserverDesPlaces {
    Reservation executer(DemandeReservation demande);
}
