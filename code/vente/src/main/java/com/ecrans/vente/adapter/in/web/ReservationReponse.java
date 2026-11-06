package com.ecrans.vente.adapter.in.web;

import java.time.LocalDateTime;

public record ReservationReponse(
        String commande, 
        long totalCentimes, 
        String total, 
        String urlPaiement, 
        LocalDateTime payerAvant
) {}