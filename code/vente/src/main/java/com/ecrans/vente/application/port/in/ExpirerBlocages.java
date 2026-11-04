package com.ecrans.vente.application.port.in;

/** Port primaire : appelé chaque minute par un planificateur pour appliquer B2. */
public interface ExpirerBlocages {
    void executer();
}
