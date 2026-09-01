package spark;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import static spark.Service.ignite;

public class InitExceptionHandlerTest {

    private static int NON_VALID_PORT = Integer.MAX_VALUE;
    private static Service service;
    private static String errorMessage = "";

    @BeforeAll
    public static void beforeAll() throws Exception {
        service = ignite();
        service.port(NON_VALID_PORT);
        service.initExceptionHandler((e) -> errorMessage = "Custom init error");
        service.init();
        service.awaitInitialization();
    }

    @AfterAll
    public static void afterAll() throws Exception {
        service.stop();
    }

    @Test
    public void testInitExceptionHandler() throws Exception {
        assertThat(errorMessage).isEqualTo("Custom init error");
    }

}
