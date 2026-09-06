package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FilterImplTest {

    public String pathTest;
    public String acceptTypeTest;

    public FilterImpl filter;

    @BeforeEach
    void setUp(){
        pathTest = "/etc/test";
        acceptTypeTest = "test/*";
    }

    @Test
    void testConstructor(){
        var f = new FilterImpl(pathTest, acceptTypeTest) {
            @Override
            public void handle(Request request, Response response) {
                // no-op
            }
        };
        assertAll(
                () -> assertThat(f.getPath()).isEqualTo(pathTest),
                () -> assertThat(f.getAcceptType()).isEqualTo(acceptTypeTest)
        );
    }

    @Test
    void testGets_thenReturnGetPathAndGetAcceptTypeSuccessfully() {
        filter = FilterImpl.create(pathTest, acceptTypeTest, null);
        assertAll(
                () -> assertThat(filter.getPath()).isEqualTo(pathTest),
                () -> assertThat(filter.getAcceptType()).isEqualTo(acceptTypeTest)
        );
    }

    @Test
    void testCreate_whenOutAssignAcceptTypeInTheParameters_thenReturnPathAndAcceptTypeSuccessfully(){
        filter = FilterImpl.create(pathTest, null);
        assertAll(
                () -> assertThat(filter.getPath()).isEqualTo(pathTest),
                () -> assertThat(filter.getAcceptType()).isEqualTo(RouteImpl.DEFAULT_ACCEPT_TYPE)
        );
    }

    @Test
    void testCreate_whenAcceptTypeNullValueInTheParameters_thenReturnPathAndAcceptTypeSuccessfully(){
        filter = FilterImpl.create(pathTest, null, null);
        assertAll(
                () -> assertThat(filter.getPath()).isEqualTo(pathTest),
                () -> assertThat(filter.getAcceptType()).isEqualTo(RouteImpl.DEFAULT_ACCEPT_TYPE)
        );
    }
}
