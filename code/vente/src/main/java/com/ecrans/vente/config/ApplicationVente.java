package com.ecrans.vente.config;

import com.ecrans.vente.adapter.in.payvia.WebhookPayVia;
import com.ecrans.vente.adapter.in.sortiesflash.AclSortiesFlash;
import com.ecrans.vente.adapter.in.sortiesflash.CorrespondanceSeances;
import com.ecrans.vente.adapter.out.evenement.PublicateurEnMemoire;
import com.ecrans.vente.adapter.out.memoire.CommandesEnMemoire;
import com.ecrans.vente.adapter.out.memoire.SeancesEnMemoire;
import com.ecrans.vente.adapter.out.paiement.PaiementsSimules;
import com.ecrans.vente.adapter.out.tarification.GrilleTarifaireFixe;
import com.ecrans.vente.application.port.in.ConfirmerPaiement;
import com.ecrans.vente.application.port.in.EnregistrerVentePartenaire;
import com.ecrans.vente.application.port.in.ExpirerBlocages;
import com.ecrans.vente.application.port.in.ReserverDesPlaces;
import com.ecrans.vente.application.service.ConfirmerPaiementService;
import com.ecrans.vente.application.service.EnregistrerVentePartenaireService;
import com.ecrans.vente.application.service.ExpirerBlocagesService;
import com.ecrans.vente.application.service.ReserverDesPlacesService;

import java.time.Clock;

/**
 * Composition root : le SEUL endroit qui connaît à la fois les adapters et les services.
 * Avec Spring, ce serait une classe @Configuration ; le principe est identique.
 */
public class ApplicationVente {

    // adapters secondaires
    public final SeancesEnMemoire seances = new SeancesEnMemoire();
    public final CommandesEnMemoire commandes = new CommandesEnMemoire();
    public final GrilleTarifaireFixe grille = new GrilleTarifaireFixe();
    public final PaiementsSimules paiements = new PaiementsSimules();
    public final PublicateurEnMemoire publicateur = new PublicateurEnMemoire();

    // ports primaires
    public final ReserverDesPlaces reserverDesPlaces;
    public final ConfirmerPaiement confirmerPaiement;
    public final ExpirerBlocages expirerBlocages;
    public final EnregistrerVentePartenaire enregistrerVentePartenaire;

    // adapters primaires
    public final WebhookPayVia webhookPayVia;
    public final CorrespondanceSeances correspondanceSeances = new CorrespondanceSeances();
    public final AclSortiesFlash aclSortiesFlash = new AclSortiesFlash(correspondanceSeances);

    public ApplicationVente(Clock horloge) {
        reserverDesPlaces = new ReserverDesPlacesService(seances, commandes, grille, paiements, horloge);
        confirmerPaiement = new ConfirmerPaiementService(seances, commandes, publicateur, horloge);
        expirerBlocages = new ExpirerBlocagesService(seances, commandes, publicateur, horloge);
        enregistrerVentePartenaire = new EnregistrerVentePartenaireService(seances, commandes, grille, publicateur,
                horloge);
        webhookPayVia = new WebhookPayVia(confirmerPaiement);
    }
}
