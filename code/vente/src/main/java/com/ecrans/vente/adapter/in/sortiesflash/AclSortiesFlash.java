package com.ecrans.vente.adapter.in.sortiesflash;

import com.ecrans.vente.application.exception.SeanceInconnue;
import com.ecrans.vente.application.port.in.DemandeBillet;
import com.ecrans.vente.application.port.in.VentePartenaire;
import com.ecrans.vente.domaine.commande.Categorie;
import com.ecrans.vente.domaine.commun.Montant;
import com.ecrans.vente.domaine.commun.PlaceId;
import com.ecrans.vente.domaine.commun.SeanceId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Anti-corruption layer, en Java pur : traduit le format de SortiesFlash (annexe B) dans notre langage.
 * SortiesFlash ne changera pas son format (B11) : si un de leurs codes change, seule cette classe bouge.
 */
public class AclSortiesFlash {

    public static final String PARTENAIRE = "SortiesFlash";

    /** Leurs types de billets, traduits en nos catégories tarifaires. */
    private static final Map<String, Categorie> CATEGORIES = Map.of(
            "ADULT", Categorie.PLEIN,
            "STUDENT", Categorie.REDUIT,
            "CHILD", Categorie.ENFANT);

    private final CorrespondanceSeances correspondance;

    public AclSortiesFlash(CorrespondanceSeances correspondance) {
        this.correspondance = correspondance;
    }

    /**
     * @throws SeanceInconnue           si le show_ref n'a pas de correspondance chez nous (404)
     * @throws IllegalArgumentException si le format est invalide : type de billet inconnu,
     *                                  pas autant de ticket_types que de seats, place mal écrite (400)
     */
    public VentePartenaire traduire(CommandeSortiesFlash commande) {
        // 1. Leur identifiant de séance devient le nôtre
        if (commande.show_ref() == null || commande.show_ref().isBlank()) {
            throw new IllegalArgumentException("show_ref manquant");
        }
        SeanceId seance = correspondance.seance(commande.show_ref())
                .orElseThrow(() -> new SeanceInconnue(new SeanceId(commande.show_ref())));

        // 2. seats[i] va avec ticket_types[i] : il en faut autant de l'un que de l'autre
        List<String> seats = commande.seats();
        List<String> types = commande.ticket_types();
        if (seats == null || types == null || seats.size() != types.size()) {
            throw new IllegalArgumentException("Il faut autant de ticket_types que de seats");
        }
        List<DemandeBillet> billets = new ArrayList<>();
        for (int i = 0; i < seats.size(); i++) {
            billets.add(new DemandeBillet(place(seats.get(i)), categorie(types.get(i))));
        }

        // 3. Le montant payé chez eux, remise comprise, est conservé tel quel (B11, déclaration CNC)
        return new VentePartenaire(PARTENAIRE, commande.partner_order_id(), seance, billets,
                Montant.centimes(commande.paid_amount_cents()));
    }

    private static PlaceId place(String seat) {
        if (seat == null || seat.length() < 2) {
            throw new IllegalArgumentException("Place mal écrite : " + seat);
        }
        return PlaceId.de(seat); // lève IllegalArgumentException si mal écrite (ex. « 7G »)
    }

    private static Categorie categorie(String ticketType) {
        Categorie categorie = ticketType == null ? null : CATEGORIES.get(ticketType);
        if (categorie == null) {
            throw new IllegalArgumentException("Type de billet SortiesFlash inconnu : " + ticketType);
        }
        return categorie;
    }
}
