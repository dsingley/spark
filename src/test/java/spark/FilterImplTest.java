package spark;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class FilterImplTest {

    public String PATH_TEST;
    public String ACCEPT_TYPE_TEST;

    public FilterImpl filter;

    @BeforeEach
    public void setUp(){
        PATH_TEST = "/etc/test";
        ACCEPT_TYPE_TEST  = "test/*";
    }

    @Test
    public void testConstructor(){
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
    public void testGets_thenReturnGetPathAndGetAcceptTypeSuccessfully() throws Exception {
        filter = FilterImpl.create(PATH_TEST, ACCEPT_TYPE_TEST, null);
        assertAll(
                () -> assertThat(filter.getPath()).isEqualTo(PATH_TEST),
                () -> assertThat(filter.getAcceptType()).isEqualTo(ACCEPT_TYPE_TEST)
        );
    }

    @Test
    public void testCreate_whenOutAssignAcceptTypeInTheParameters_thenReturnPathAndAcceptTypeSuccessfully(){
        filter = FilterImpl.create(PATH_TEST, null);
        assertAll(
                () -> assertThat(filter.getPath()).isEqualTo(PATH_TEST),
                () -> assertThat(filter.getAcceptType()).isEqualTo(RouteImpl.DEFAULT_ACCEPT_TYPE)
        );
    }

    @Test
    public void testCreate_whenAcceptTypeNullValueInTheParameters_thenReturnPathAndAcceptTypeSuccessfully(){
        filter = FilterImpl.create(PATH_TEST, null, null);
        assertAll(
                () -> assertThat(filter.getPath()).isEqualTo(PATH_TEST),
                () -> assertThat(filter.getAcceptType()).isEqualTo(RouteImpl.DEFAULT_ACCEPT_TYPE)
        );
    }
}
