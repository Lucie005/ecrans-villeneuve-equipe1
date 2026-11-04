package com.ecrans.vente.domaine.commun;

/** Un fauteuil du plan de salle : un rang (lettre) et un numéro. « G7 » = rang G, fauteuil 7. */
public record PlaceId(char rang, int numero) implements Comparable<PlaceId> {

    public PlaceId {
        if (rang < 'A' || rang > 'Z') throw new IllegalArgumentException("Rang invalide : " + rang);
        if (numero < 1) throw new IllegalArgumentException("Numéro de fauteuil invalide : " + numero);
    }

    /** {@code PlaceId.de("G7")} */
    public static PlaceId de(String code) {
        return new PlaceId(Character.toUpperCase(code.charAt(0)), Integer.parseInt(code.substring(1)));
    }

    @Override
    public int compareTo(PlaceId autre) {
        return rang != autre.rang ? Character.compare(rang, autre.rang) : Integer.compare(numero, autre.numero);
    }

    @Override
    public String toString() {
        return "" + rang + numero;
    }
}
