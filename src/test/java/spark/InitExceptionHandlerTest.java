package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static spark.Service.ignite;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class InitExceptionHandlerTest {

    private static final int NON_VALID_PORT = Integer.MAX_VALUE;
    private static Service service;
    private static String errorMessage = "";

    @BeforeAll
    static void beforeAll() {
        service = ignite();
        service.port(NON_VALID_PORT);
        service.initExceptionHandler(e -> errorMessage = "Custom init error");
        service.init();
        service.awaitInitialization();
    }

    @AfterAll
    static void afterAll() {
        service.stop();
        service.awaitStop();
    }

    @Test
    void testInitExceptionHandler() {
        assertThat(errorMessage).isEqualTo("Custom init error");
    }

}
