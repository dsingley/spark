package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FilterImplTest {

    public String PATH_TEST;
    public String ACCEPT_TYPE_TEST;

    public FilterImpl filter;

    @BeforeEach
    void setUp(){
        PATH_TEST = "/etc/test";
        ACCEPT_TYPE_TEST  = "test/*";
    }

    @Test
    void testConstructor(){
        FilterImpl filter = new FilterImpl(PATH_TEST, ACCEPT_TYPE_TEST) {
            @Override
            public void handle(Request request, Response response) throws Exception {
            }
        };
        assertAll(
                () -> assertThat(filter.getPath()).isEqualTo(PATH_TEST),
                () -> assertThat(filter.getAcceptType()).isEqualTo(ACCEPT_TYPE_TEST)
        );
    }

    @Test
    void testGets_thenReturnGetPathAndGetAcceptTypeSuccessfully() throws Exception {
        filter = FilterImpl.create(PATH_TEST, ACCEPT_TYPE_TEST, null);
        assertAll(
                () -> assertThat(filter.getPath()).isEqualTo(PATH_TEST),
                () -> assertThat(filter.getAcceptType()).isEqualTo(ACCEPT_TYPE_TEST)
        );
    }

    @Test
    void testCreate_whenOutAssignAcceptTypeInTheParameters_thenReturnPathAndAcceptTypeSuccessfully(){
        filter = FilterImpl.create(PATH_TEST, null);
        assertAll(
                () -> assertThat(filter.getPath()).isEqualTo(PATH_TEST),
                () -> assertThat(filter.getAcceptType()).isEqualTo(RouteImpl.DEFAULT_ACCEPT_TYPE)
        );
    }

    @Test
    void testCreate_whenAcceptTypeNullValueInTheParameters_thenReturnPathAndAcceptTypeSuccessfully(){
        filter = FilterImpl.create(PATH_TEST, null, null);
        assertAll(
                () -> assertThat(filter.getPath()).isEqualTo(PATH_TEST),
                () -> assertThat(filter.getAcceptType()).isEqualTo(RouteImpl.DEFAULT_ACCEPT_TYPE)
        );
    }
}
