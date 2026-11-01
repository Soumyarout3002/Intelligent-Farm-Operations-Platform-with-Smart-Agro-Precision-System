
package com.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class BackendApplicationTests {

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        Dotenv dotenv = Dotenv.configure()
                .directory(".")
                .ignoreIfMissing()
                .load();

        String url = System.getenv("DB_URL");
        if (url == null || url.isBlank()) {
            url = dotenv.get("DB_URL",
                    "jdbc:mysql://localhost:3306/farmverse_nexus");
        }

        String username = System.getenv("DB_USERNAME");
        if (username == null || username.isBlank()) {
            username = dotenv.get("DB_USERNAME", "root");
        }

        String password = System.getenv("DB_PASSWORD");
        if (password == null) {
            password = dotenv.get("DB_PASSWORD");
        }

        final String dbUrl = url;
final String dbUsername = username;
final String dbPassword = password;

registry.add("spring.datasource.url", () -> dbUrl);
registry.add("spring.datasource.username", () -> dbUsername);

if (dbPassword != null) {
    registry.add("spring.datasource.password", () -> dbPassword);
}
    }

    @Test
    void contextLoads() {
    }
}
