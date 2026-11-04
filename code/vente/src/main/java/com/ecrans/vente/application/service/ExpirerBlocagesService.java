package com.ecrans.vente.application.service;

import com.ecrans.vente.application.exception.CommandeInconnue;
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
        LocalDateTime maintenant = LocalDateTime.now(horloge);

        // 1. Parcourir toutes les séances actuellement en vente
        for (Seance seance : seances.enVente()) {

            // 2. Libérer les places expirées de cette séance
            Set<CommandeId> commandesConcernees = seance.libererBlocagesExpires(maintenant);

            // 3. S'il y a des commandes impactées, on enregistre la séance
            if (!commandesConcernees.isEmpty()) {
                seances.enregistrer(seance);

                // 4. On expire les commandes associées
                for (CommandeId cmdId : commandesConcernees) {
                    Commande commande = commandes.parId(cmdId)
                            .orElseThrow(() -> new CommandeInconnue(cmdId));

                    // Si elle attendait bien son paiement, on l'expire
                    if (commande.statut() == StatutCommande.EN_ATTENTE_PAIEMENT) {
                        expirer(commande, maintenant); // Méthode privée déjà fournie en bas du fichier
                    }
                }
            }
        }
    }


    private void expirer(Commande commande, LocalDateTime maintenant) {
        commande.expirer(maintenant);
        commandes.enregistrer(commande);
        commande.retirerEvenements().forEach(publicateur::publier);
    }
}
