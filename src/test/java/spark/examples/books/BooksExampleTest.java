package spark.examples.books;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static spark.Spark.after;
import static spark.Spark.before;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;
import spark.util.SparkStopExtension;
import spark.utils.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

@ExtendWith(SparkStopExtension.class)
class BooksExampleTest {

    private static final Logger LOG = LoggerFactory.getLogger(BooksExampleTest.class);

    private static final int PORT = 4567;

    private static final String AUTHOR = "FOO";
    private static final String TITLE = "BAR";
    private static final String NEW_TITLE = "SPARK";

    private String bookId;

    @BeforeAll
    static void beforeAll() {
        before((request, response) -> response.header("FOZ", "BAZ"));

        Books.main(null);

        after((request, response) -> response.header("FOO", "BAR"));

        Spark.awaitInitialization();
    }

    @AfterEach
    void tearDown() {
        Books.books.clear();
    }

    @Test
    void canCreateBook() {
        var response = createBookViaPOST();

        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.body).isNotNull(),
                () -> assertThat(Integer.valueOf(response.body)).isPositive(),
                () -> assertThat(response.status).isEqualTo(201)
        );
    }

    @Test
    void canListBooks() {
        bookId = createBookViaPOST().body.trim();

        var response = doMethod("GET", "/books");

        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.body).isNotNull(),
                () -> assertThat(Integer.valueOf(response.body.trim())).isPositive(),
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).contains(bookId)
        );
    }

    @Test
    void canGetBook() {
        bookId = createBookViaPOST().body.trim();

        var response = doMethod("GET", "/books/" + bookId);

        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.body).isNotNull(),
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).contains(AUTHOR),
                () -> assertThat(response.body).contains(TITLE),
                () -> assertThat(beforeFilterIsSet(response)).isTrue(),
                () -> assertThat(afterFilterIsSet(response)).isTrue()
        );
    }

    @Test
    void canUpdateBook() {
        bookId = createBookViaPOST().body.trim();

        var response = updateBook();

        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.body).isNotNull(),
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).contains(bookId),
                () -> assertThat(response.body).contains("updated")
        );
    }

    @Test
    void canGetUpdatedBook() {
        bookId = createBookViaPOST().body.trim();
        updateBook();

        var response = doMethod("GET", "/books/" + bookId);

        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.body).isNotNull(),
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).contains(AUTHOR),
                () -> assertThat(response.body).contains(NEW_TITLE)
        );
    }

    @Test
    void canDeleteBook() {
        bookId = createBookViaPOST().body.trim();

        var response = doMethod("DELETE", "/books/" + bookId);

        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.body).isNotNull(),
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).contains(bookId),
                () -> assertThat(response.body).contains("deleted")
        );
    }

    @Test
    void wontFindBook() {
        assertThatThrownBy(() -> getResponse("GET", "/books/" + bookId, null))
                .isInstanceOf(FileNotFoundException.class);
    }

    private static UrlResponse doMethod(String requestMethod, String path) {
        var response = new UrlResponse();

        try {
            getResponse(requestMethod, path, response);
        } catch (IOException e) {
            // TODO (sleberknight): wrap and throw as UncheckedIOException?
            LOG.error("Error getting response from {} {}", requestMethod, path, e);
        }

        return response;
    }

    private static void getResponse(String requestMethod, String path, UrlResponse response)
            throws IOException {
        var url = new URL("http://localhost:" + PORT + path);
        var connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(requestMethod);
        connection.connect();
        response.body = IOUtils.toString(connection.getInputStream());
        response.status = connection.getResponseCode();
        response.headers = connection.getHeaderFields();
    }

    private static class UrlResponse {
        public Map<String, List<String>> headers;
        private String body;
        private int status;
    }

    private UrlResponse createBookViaPOST() {
        return doMethod("POST", "/books?author=" + AUTHOR + "&title=" + TITLE);
    }

    private UrlResponse updateBook() {
        return doMethod("PUT", "/books/" + bookId + "?title=" + NEW_TITLE);
    }

    private boolean afterFilterIsSet(UrlResponse response) {
        return response.headers.get("FOO").get(0).equals("BAR");
    }

    private boolean beforeFilterIsSet(UrlResponse response) {
        return response.headers.get("FOZ").get(0).equals("BAZ");
    }
}
