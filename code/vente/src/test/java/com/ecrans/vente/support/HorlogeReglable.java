package com.ecrans.vente.support;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Une horloge qu'on règle à la main : « il est 20 h 02 le 12 novembre 2026 ». */
public class HorlogeReglable extends Clock {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private Instant maintenant;

    private HorlogeReglable(LocalDateTime maintenant) {
        regler(maintenant);
    }

    /** {@code HorlogeReglable.a("20:02")} : le jeudi 12 novembre 2026, jour de la séance S42. */
    public static HorlogeReglable a(String heure) {
        return new HorlogeReglable(Donnees.le12Novembre(heure));
    }

    public void reglerA(String heure) {
        regler(Donnees.le12Novembre(heure));
    }

    public void avancerDeMinutes(int minutes) {
        maintenant = maintenant.plusSeconds(minutes * 60L);
    }

    private void regler(LocalDateTime t) {
        maintenant = t.atZone(PARIS).toInstant();
    }

    @Override
    public ZoneId getZone() {
        return PARIS;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return Clock.fixed(maintenant, zone);
    }

    @Override
    public Instant instant() {
        return maintenant;
    }
}
