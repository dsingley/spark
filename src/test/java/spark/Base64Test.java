package spark;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class Base64Test {

    //CS304 manually Issue link:https://github.com/perwendel/spark/issues/1061

    @Test
    final void test_encode() {
        String in = "hello";
        String encode = Base64.encode(in);
        assertThat(in).isNotEqualTo(encode);
    }

    //CS304 manually Issue link:https://github.com/perwendel/spark/issues/1061

    @Test
    final void test_decode() {
        String in = "hello";
        String encode = Base64.encode(in);
        String decode = Base64.decode(encode);

        assertThat(in).isEqualTo(decode);
    }

}
