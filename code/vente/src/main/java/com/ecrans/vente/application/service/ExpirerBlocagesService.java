package com.ecrans.vente.application.service;

import com.ecrans.vente.application.port.in.ExpirerBlocages;
import com.ecrans.vente.application.port.out.Commandes;
import com.ecrans.vente.application.port.out.PublicateurEvenements;
import com.ecrans.vente.application.port.out.Seances;
import com.ecrans.vente.domaine.commande.Commande;
import com.ecrans.vente.domaine.commande.StatutCommande;
import com.ecrans.vente.domaine.commun.CommandeId;
import com.ecrans.vente.domaine.seance.Seance;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Set;

/** B2 : chaque minute, libérer les blocages expirés et faire expirer les commandes correspondantes. */
public class ExpirerBlocagesService implements ExpirerBlocages {

    private final Seances seances;
    private final Commandes commandes;
    private final PublicateurEvenements publicateur;
    private final Clock horloge;

    public ExpirerBlocagesService(Seances seances, Commandes commandes, PublicateurEvenements publicateur,
                                  Clock horloge) {
        this.seances = seances;
        this.commandes = commandes;
        this.publicateur = publicateur;
        this.horloge = horloge;
    }

    @Override
    public void executer() {
        // TODO étape 5
        // INDICE : les séances d'abord (libererBlocagesExpires), puis les commandes concernées.
        throw new UnsupportedOperationException("À compléter : étape 5");
    }

    private void expirer(Commande commande, LocalDateTime maintenant) {
        commande.expirer(maintenant);
        commandes.enregistrer(commande);
        commande.retirerEvenements().forEach(publicateur::publier);
    }
}
