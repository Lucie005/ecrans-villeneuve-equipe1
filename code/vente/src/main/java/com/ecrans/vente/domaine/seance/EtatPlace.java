package com.ecrans.vente.domaine.seance;

import com.ecrans.vente.domaine.commun.CommandeId;

import java.time.LocalDateTime;

/**
 * Value object : l'état d'un fauteuil pour une séance.
 * {@code commande} et {@code bloqueeJusqua} sont null quand la place est libre.
 */
public record EtatPlace(StatutPlace statut, CommandeId commande, LocalDateTime bloqueeJusqua) {

    public static EtatPlace libre() {
        return new EtatPlace(StatutPlace.LIBRE, null, null);
    }

    public static EtatPlace bloquee(CommandeId commande, LocalDateTime jusqua) {
        return new EtatPlace(StatutPlace.BLOQUEE, commande, jusqua);
    }

    public static EtatPlace vendue(CommandeId commande) {
        return new EtatPlace(StatutPlace.VENDUE, commande, null);
    }

    /** B2 : un blocage expiré ne retient plus la place, même si personne ne l'a encore nettoyé. */
    public boolean estLibreA(LocalDateTime maintenant) {
        return statut == StatutPlace.LIBRE || (statut == StatutPlace.BLOQUEE && blocageExpireA(maintenant));
    }

    public boolean blocageExpireA(LocalDateTime maintenant) {
        return statut == StatutPlace.BLOQUEE && !maintenant.isBefore(bloqueeJusqua);
    }

    public boolean estBloqueePar(CommandeId c) {
        return statut == StatutPlace.BLOQUEE && commande.equals(c);
    }
}
