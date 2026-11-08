package com.ecrans.vente.adapter.in.sortiesflash;

import com.ecrans.vente.application.port.in.EnregistrerVentePartenaire;
import com.ecrans.vente.application.port.in.VentePartenaire;
import com.ecrans.vente.domaine.commun.CommandeId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * L'URL que SortiesFlash appelle. Les refus passent par GestionErreurs :
 * 409 si une place est prise, 404 si la séance est inconnue, 400 si le format est invalide.
 */
@RestController
public class SortiesFlashController {

    private final AclSortiesFlash acl;
    private final EnregistrerVentePartenaire enregistrerVentePartenaire;

    public SortiesFlashController(AclSortiesFlash acl, EnregistrerVentePartenaire enregistrerVentePartenaire) {
        this.acl = acl;
        this.enregistrerVentePartenaire = enregistrerVentePartenaire;
    }

    @PostMapping("/partner/v2/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public ReponseSortiesFlash vendre(@RequestBody CommandeSortiesFlash commande) {
        // 1. L'ACL traduit leur JSON dans notre langage
        VentePartenaire vente = acl.traduire(commande);

        // 2. Appel du port d'entrée (cas d'usage)
        CommandeId commandeId = enregistrerVentePartenaire.executer(vente);

        // 3. Réponse dans leur format
        return ReponseSortiesFlash.confirmee(commande.partner_order_id(), commandeId.valeur());
    }
}
