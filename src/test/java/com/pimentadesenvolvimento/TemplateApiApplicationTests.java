package com.pimentadesenvolvimento;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=01234567890123456789012345678901",
        "jwt.expiration-access=400000",
        "jwt.expiration-refresh=86400000",
        "webhook.secret=testsecret",
        "app.admin.default-password=TestAdmin123!@",
        "app.rate-limit.enabled=false",
        "app.jwt.blacklist.cleanup-interval=PT5M"
})
class TemplateApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
