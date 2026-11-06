package com.ecrans.vente.adapter.in.web;

import java.util.List;

public record ReservationRequete(List<BilletRequete> billets) {
    public record BilletRequete(String place, String categorie) {}
}