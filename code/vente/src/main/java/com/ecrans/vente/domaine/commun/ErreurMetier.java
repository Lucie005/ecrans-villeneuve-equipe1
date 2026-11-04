package com.ecrans.vente.domaine.commun;

/** Une règle métier a été violée. Message en français, compréhensible par Nadia. */
public abstract class ErreurMetier extends RuntimeException {
    protected ErreurMetier(String message) {
        super(message);
    }
}
