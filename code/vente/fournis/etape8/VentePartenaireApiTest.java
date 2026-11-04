package com.ecrans.vente.etape8;

import com.ecrans.vente.VenteApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Étape 8 : SortiesFlash nous envoie une vente déjà payée, dans SON format (annexe B).
 * La séance S42 (demain 20 h 30, salle 3) s'appelle chez eux « VIL-aaaammjj-2030-S3 ».
 * Chaque test utilise ses propres places : le contexte Spring est partagé entre les tests.
 */
@SpringBootTest(classes = VenteApplication.class)
@AutoConfigureMockMvc
class VentePartenaireApiTest {

    private static final String SHOW_REF =
            "VIL-" + LocalDate.now().plusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE) + "-2030-S3";

    @Autowired
    MockMvc mvc;

    private ResultActions vendre(String showRef, String seats, String types) throws Exception {
        return mvc.perform(post("/partner/v2/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"show_ref": "%s", "seats": [%s], "ticket_types": [%s],
                         "partner_order_id": "SF-77812", "paid_amount_cents": 1840}
                        """.formatted(showRef, seats, types)));
    }

    @Test
    @DisplayName("201 : la vente est acceptée et renvoyée dans le format de SortiesFlash")
    void venteAcceptee() throws Exception {
        vendre(SHOW_REF, "\"D1\", \"D2\"", "\"ADULT\", \"STUDENT\"")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.partner_order_id").value("SF-77812"))
                .andExpect(jsonPath("$.cinema_order_id").value(startsWith("C-")))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("B1 : une place vendue par SortiesFlash ne peut plus être réservée sur notre site (409)")
    void placeVenduePartenaireIndisponibleSurLeSite() throws Exception {
        vendre(SHOW_REF, "\"D5\"", "\"ADULT\"").andExpect(status().isCreated());
        mvc.perform(post("/seances/S42/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"billets": [{"place": "D5", "categorie": "PLEIN"}]}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("B11 : si une seule place est prise, toute la vente est refusée (409)")
    void unePlacePriseToutEstRefuse() throws Exception {
        vendre(SHOW_REF, "\"E1\"", "\"ADULT\"").andExpect(status().isCreated());
        vendre(SHOW_REF, "\"E1\", \"E2\"", "\"ADULT\", \"CHILD\"").andExpect(status().isConflict());
        vendre(SHOW_REF, "\"E2\"", "\"CHILD\"").andExpect(status().isCreated());   // E2 n'avait pas été prise
    }

    @Test
    @DisplayName("404 : séance inconnue chez nous")
    void seanceInconnue() throws Exception {
        vendre("VIL-19990101-2030-S3", "\"E8\"", "\"ADULT\"").andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("400 : type de billet inconnu, ou pas autant de types que de places")
    void formatInvalide() throws Exception {
        vendre(SHOW_REF, "\"E9\"", "\"VIP\"").andExpect(status().isBadRequest());
        vendre(SHOW_REF, "\"E10\", \"E11\"", "\"ADULT\"").andExpect(status().isBadRequest());
    }
}
