package com.pimentadesenvolvimento.conroledebolsaoback;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "webhook.secret=testsecret",
        "jwt.secret=01234567890123456789012345678901",
        "jwt.expiration-access=400000",
        "jwt.expiration-refresh=86400000",
        "app.admin.default-password=TestAdmin123!@",
        "app.rate-limit.enabled=false",
        "app.jwt.blacklist.cleanup-interval=PT5M"
})
public class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static String computeHmac(String payload, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmac);
    }

    @Test
    void webhookShouldAcceptValidSignature() throws Exception {
        String payload = "{\"test\":\"value\"}";
        String signature = computeHmac(payload, "testsecret");

        mockMvc.perform(post("/webhook")
                        .header("X-Signature", signature)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isOk());
    }
}
