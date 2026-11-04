package com.ecrans.vente.application.port.out;

import com.ecrans.vente.domaine.commande.Categorie;
import com.ecrans.vente.domaine.commun.Montant;
import com.ecrans.vente.domaine.commun.SeanceId;

/** Port secondaire vers le contexte Tarification (relation Customer/Supplier). */
public interface GrilleTarifaire {
    Montant prix(SeanceId seance, Categorie categorie);
}
