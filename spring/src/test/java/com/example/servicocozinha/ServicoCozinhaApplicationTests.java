package com.example.servicocozinha;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.mensageria.habilitada=false")
class ServicoCozinhaApplicationTests {

    @Test
    void contextLoads() {
    }
}
