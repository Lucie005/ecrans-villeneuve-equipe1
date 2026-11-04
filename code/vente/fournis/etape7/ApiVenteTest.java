package com.ecrans.vente.etape7;

import com.ecrans.vente.VenteApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Étape 7 : l'hexagone, vu de l'extérieur, à travers HTTP.
 * Spring démarre l'application complète (séance de démonstration S42 comprise) ;
 * MockMvc envoie de vraies requêtes HTTP sans ouvrir de port réseau.
 * Chaque test utilise ses propres places : le contexte Spring est partagé entre les tests.
 */
@SpringBootTest(classes = VenteApplication.class)
@AutoConfigureMockMvc
class ApiVenteTest {

    @Autowired
    MockMvc mvc;

    private ResultActions reserver(String seance, String json) throws Exception {
        return mvc.perform(post("/seances/" + seance + "/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @Test
    @DisplayName("201 : une réservation valide renvoie la commande, le total et le lien de paiement")
    void reservationValide() throws Exception {
        reserver("S42", """
                {"billets": [{"place": "A1", "categorie": "PLEIN"}, {"place": "A2", "categorie": "ENFANT"}]}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.commande").value(startsWith("C-")))
                .andExpect(jsonPath("$.totalCentimes").value(1800))
                .andExpect(jsonPath("$.urlPaiement").exists())
                .andExpect(jsonPath("$.payerAvant").exists());
    }

    @Test
    @DisplayName("409 : une place déjà bloquée ne peut pas être réservée une seconde fois (B1)")
    void placeDejaPrise() throws Exception {
        String json = """
                {"billets": [{"place": "B5", "categorie": "PLEIN"}]}
                """;
        reserver("S42", json).andExpect(status().isCreated());
        reserver("S42", json).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("404 : séance inconnue")
    void seanceInconnue() throws Exception {
        reserver("S999", """
                {"billets": [{"place": "A1", "categorie": "PLEIN"}]}
                """)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("422 : une règle métier refuse la demande (aucune place demandée)")
    void regleMetier() throws Exception {
        reserver("S42", """
                {"billets": []}
                """)
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("400 : place mal écrite")
    void placeMalEcrite() throws Exception {
        reserver("S42", """
                {"billets": [{"place": "7G", "categorie": "PLEIN"}]}
                """)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("204 : le webhook PayVia confirme le paiement d'une commande existante")
    void webhookPayVia() throws Exception {
        String reponse = reserver("S42", """
                {"billets": [{"place": "C3", "categorie": "PLEIN"}]}
                """)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String commande = reponse.replaceAll(".*\"commande\":\"([^\"]+)\".*", "$1");

        mvc.perform(post("/webhooks/payvia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"event": "payment.succeeded", "paymentId": "pv_1", "reference": "%s",
                                 "amount": 1150, "currency": "EUR", "occurredAt": "2026-11-12T20:05:00Z"}
                                """.formatted(commande)))
                .andExpect(status().isNoContent());
    }
}
