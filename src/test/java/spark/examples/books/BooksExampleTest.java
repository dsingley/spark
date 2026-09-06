package spark.examples.books;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static spark.Spark.after;
import static spark.Spark.before;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.Spark;
import spark.utils.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

class BooksExampleTest {

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

    @AfterAll
    static void afterAll() {
        Spark.stop();
        Spark.awaitStop();
    }

    @Test
    void canCreateBook() {
        UrlResponse response = createBookViaPOST();

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

        UrlResponse response = doMethod("GET", "/books", null);

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

        UrlResponse response = doMethod("GET", "/books/" + bookId, null);

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

        UrlResponse response = updateBook();

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

        UrlResponse response = doMethod("GET", "/books/" + bookId, null);

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

        UrlResponse response = doMethod("DELETE", "/books/" + bookId, null);

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

    private static UrlResponse doMethod(String requestMethod, String path, String body) {
        UrlResponse response = new UrlResponse();

        try {
            getResponse(requestMethod, path, response);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    private static void getResponse(String requestMethod, String path, UrlResponse response)
            throws IOException {
        URL url = new URL("http://localhost:" + PORT + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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
        return doMethod("POST", "/books?author=" + AUTHOR + "&title=" + TITLE, null);
    }

    private UrlResponse updateBook() {
        return doMethod("PUT", "/books/" + bookId + "?title=" + NEW_TITLE, null);
    }

    private boolean afterFilterIsSet(UrlResponse response) {
        return response.headers.get("FOO").get(0).equals("BAR");
    }

    private boolean beforeFilterIsSet(UrlResponse response) {
        return response.headers.get("FOZ").get(0).equals("BAZ");
    }
}
