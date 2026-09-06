package spark;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.kiwiproject.reflect.KiwiReflection;

import java.lang.reflect.Field;

class ExceptionMapperTest {


    @Test
    void testGetInstance_whenDefaultInstanceIsNull() {
        //given
        ExceptionMapper exceptionMapper = null;
        Field servletInstanceField = servletInstanceField();
        KiwiReflection.setFieldValue(null, servletInstanceField, exceptionMapper);

        //then
        exceptionMapper = ExceptionMapper.getServletInstance();
        assertThat(exceptionMapper)
                .describedAs("Should be same because ExceptionMapper is a singleton")
                .isSameAs(KiwiReflection.getTypedFieldValue(null, servletInstanceField, ExceptionMapper.class));
    }

    @Test
    void testGetInstance_whenDefaultInstanceIsNotNull() {
        //given
        ExceptionMapper.getServletInstance(); //initialize Singleton

        //then
        ExceptionMapper exceptionMapper = ExceptionMapper.getServletInstance();
        assertThat(exceptionMapper)
                .describedAs("Should be same because ExceptionMapper is a singleton")
                .isSameAs(KiwiReflection.getTypedFieldValue(null, servletInstanceField(), ExceptionMapper.class));
    }

    private static Field servletInstanceField() {
        return KiwiReflection.findField(new ExceptionMapper(), "servletInstance");
    }
}
