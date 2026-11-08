package com.ecrans.vente.domaine.evenement;

import com.ecrans.vente.domaine.commun.CommandeId;
import com.ecrans.vente.domaine.commun.Montant;

import java.time.LocalDateTime;

/**
 * B11 : une vente partenaire a été enregistrée. Il porte le montant payé chez le partenaire,
 * que liront la déclaration CNC (3.4) et le reversement mensuel (grille moins 8 %).
 */
public record VentePartenaireEnregistree(CommandeId commande, String partenaire, String referencePartenaire,
                                         Montant montantPaye, LocalDateTime survenuLe) implements EvenementVente {
}
