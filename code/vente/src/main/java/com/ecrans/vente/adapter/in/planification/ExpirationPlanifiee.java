package com.ecrans.vente.adapter.in.planification;

import com.ecrans.vente.application.port.in.ExpirerBlocages;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExpirationPlanifiee {

    private final ExpirerBlocages expirerBlocages;

    public ExpirationPlanifiee(ExpirerBlocages expirerBlocages) {
        this.expirerBlocages = expirerBlocages;
    }

    @Scheduled(fixedRate = 60_000)
    public void chaqueMinute() {
        expirerBlocages.executer();
    }
}