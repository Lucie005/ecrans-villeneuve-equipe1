package com.ecrans.vente.domaine.commun;

/** Un montant en euros, stocké en centimes pour éviter les erreurs d'arrondi. */
public record Montant(long centimes) {

    public static final Montant ZERO = new Montant(0);

    public Montant {
        if (centimes < 0) throw new IllegalArgumentException("Un montant ne peut pas être négatif : " + centimes);
    }

    public static Montant centimes(long centimes) {
        return new Montant(centimes);
    }

    public Montant plus(Montant autre) {
        return new Montant(centimes + autre.centimes);
    }

    @Override
    public String toString() {
        return String.format("%d,%02d €", centimes / 100, centimes % 100);
    }
}
