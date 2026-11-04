package com.ecrans.vente.domaine.evenement;

import com.ecrans.vente.domaine.commun.CommandeId;
import com.ecrans.vente.domaine.commun.SeanceId;

import java.time.LocalDateTime;

public record CommandeExpiree(CommandeId commande, SeanceId seance, LocalDateTime survenuLe) implements EvenementVente {
}
