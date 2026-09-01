package spark;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class Base64Test {

    //CS304 manually Issue link:https://github.com/perwendel/spark/issues/1061

    @Test
    public final void test_encode() {
        String in = "hello";
        String encode = Base64.encode(in);
        assertThat(in.equals(encode)).isFalse();
    }

    //CS304 manually Issue link:https://github.com/perwendel/spark/issues/1061

    @Test
    public final void test_decode() {
        String in = "hello";
        String encode = Base64.encode(in);
        String decode = Base64.decode(encode);

        assertThat(in.equals(decode)).isTrue();
    }

}
