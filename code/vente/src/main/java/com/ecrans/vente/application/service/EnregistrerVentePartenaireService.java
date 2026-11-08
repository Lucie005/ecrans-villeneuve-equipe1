package com.ecrans.vente.application.service;

import com.ecrans.vente.application.exception.SeanceInconnue;
import com.ecrans.vente.application.port.in.DemandeBillet;
import com.ecrans.vente.application.port.in.EnregistrerVentePartenaire;
import com.ecrans.vente.application.port.in.VentePartenaire;
import com.ecrans.vente.application.port.out.Commandes;
import com.ecrans.vente.application.port.out.GrilleTarifaire;
import com.ecrans.vente.application.port.out.PublicateurEvenements;
import com.ecrans.vente.application.port.out.Seances;
import com.ecrans.vente.domaine.commande.Billet;
import com.ecrans.vente.domaine.commande.Commande;
import com.ecrans.vente.domaine.commande.Tarif;
import com.ecrans.vente.domaine.commun.CommandeId;
import com.ecrans.vente.domaine.evenement.VentePartenaireEnregistree;
import com.ecrans.vente.domaine.seance.Seance;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * B11 : un partenaire envoie une vente déjà payée, en un seul appel. Elle suit les mêmes règles que les nôtres
 * (B1 à B4) mais sans PayVia et sans blocage de 10 minutes : les places sont vendues et la commande payée
 * dans la même transaction (même choix d'agrégats que ConfirmerPaiement, ADR-0002).
 */
public class EnregistrerVentePartenaireService implements EnregistrerVentePartenaire {

    private final Seances seances;
    private final Commandes commandes;
    private final GrilleTarifaire grille;
    private final PublicateurEvenements publicateur;
    private final Clock horloge;

    public EnregistrerVentePartenaireService(Seances seances, Commandes commandes, GrilleTarifaire grille,
                                             PublicateurEvenements publicateur, Clock horloge) {
        this.seances = seances;
        this.commandes = commandes;
        this.grille = grille;
        this.publicateur = publicateur;
        this.horloge = horloge;
    }

    @Override
    public CommandeId executer(VentePartenaire vente) {
        // 1. Prendre l'heure et un nouvel identifiant de commande
        LocalDateTime maintenant = LocalDateTime.now(horloge);
        CommandeId commandeId = CommandeId.nouvelle();

        // 2. Construire les billets au prix de la grille, comme à l'étape 3 (B6)
        List<Billet> billets = new ArrayList<>();
        for (DemandeBillet db : vente.billets()) {
            Tarif tarif = new Tarif(db.categorie(), grille.prix(vente.seance(), db.categorie()));
            billets.add(new Billet(db.place(), tarif));
        }

        // 3. Faire décider le domaine. La commande d'abord (B3, commande vide, place en double).
        //    Elle n'attend aucun paiement : son heure limite est « maintenant ».
        Commande commande = Commande.creer(commandeId, vente.seance(), billets, maintenant);

        Seance seance = seances.parId(vente.seance())
                .orElseThrow(() -> new SeanceInconnue(vente.seance()));

        // La séance bloque (B4, B1, tout ou rien), puis vend aussitôt : le client a déjà payé chez le partenaire
        seance.bloquer(commande.places(), commandeId, maintenant);
        seance.vendre(commandeId, commande.places(), maintenant);
        commande.payer(maintenant);

        // 4. Enregistrer la séance, puis la commande
        seances.enregistrer(seance);
        commandes.enregistrer(commande);

        // 5. Publier les événements de la commande (CommandePayee), puis celui de la vente partenaire,
        //    qui garde le montant payé chez le partenaire (déclaration CNC, reversement de B11)
        commande.retirerEvenements().forEach(publicateur::publier);
        publicateur.publier(new VentePartenaireEnregistree(commandeId, vente.partenaire(),
                vente.referencePartenaire(), vente.montantPaye(), maintenant));

        return commandeId;
    }
}
