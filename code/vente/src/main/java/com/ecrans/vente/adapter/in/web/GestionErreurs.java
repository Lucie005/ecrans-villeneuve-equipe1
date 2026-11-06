package com.ecrans.vente.adapter.in.web;

import com.ecrans.vente.application.exception.CommandeInconnue;
import com.ecrans.vente.application.exception.SeanceInconnue;
import com.ecrans.vente.application.port.out.ConflitDeVersion;
import com.ecrans.vente.domaine.commun.ErreurMetier;
import com.ecrans.vente.domaine.seance.PlacesIndisponibles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GestionErreurs {

    /** 409 : la place vient d'être prise par quelqu'un d'autre (B1). */
    @ExceptionHandler({PlacesIndisponibles.class, ConflitDeVersion.class})
    ProblemDetail conflit(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    /** 404 : Ce qu'on demande n'existe pas (séance ou commande introuvable). */
    @ExceptionHandler({SeanceInconnue.class, CommandeInconnue.class})
    ProblemDetail introuvable(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /** 422 : Demande bien écrite, mais une règle métier la refuse (VenteFermee, TropDePlaces...). */
    @ExceptionHandler(ErreurMetier.class)
    ProblemDetail erreurMetier(ErreurMetier e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    /** 400 : Demande mal écrite (ex: format de place invalide, catégorie inconnue). */
    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail mauvaiseRequete(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
