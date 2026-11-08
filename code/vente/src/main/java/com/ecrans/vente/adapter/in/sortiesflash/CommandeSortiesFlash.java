package com.ecrans.vente.adapter.in.sortiesflash;

import java.util.List;

/**
 * Le JSON envoyé par SortiesFlash (annexe B du sujet), tel quel, avec SES noms de champs.
 * C'est leur langage : il ne sort pas de ce package, et n'entre JAMAIS dans le domaine.
 */
public record CommandeSortiesFlash(String show_ref, List<String> seats, List<String> ticket_types,
                                   String partner_order_id, long paid_amount_cents) {
}
