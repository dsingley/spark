package spark.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

class CollectionUtilsTest {

    @Test
    void testIsEmpty_whenCollectionIsEmpty_thenReturnTrue() {

        Collection<Object> testCollection = new ArrayList<>();

        assertThat(CollectionUtils.isEmpty(testCollection)).isTrue();

    }

    @Test
    void testIsEmpty_whenCollectionIsNotEmpty_thenReturnFalse() {

        Collection<Integer> testCollection = new ArrayList<>();
        testCollection.add(1);
        testCollection.add(2);

        assertThat(CollectionUtils.isEmpty(testCollection)).isFalse();

    }

    @Test
    void testIsEmpty_whenCollectionIsNull_thenReturnTrue() {

        Collection<Integer> testCollection = null;

        assertThat(CollectionUtils.isEmpty(testCollection)).isTrue();

    }

    @Test
    void testIsNotEmpty_whenCollectionIsEmpty_thenReturnFalse() {

        Collection<Object> testCollection = new ArrayList<>();

        assertThat(CollectionUtils.isNotEmpty(testCollection)).isFalse();

    }

    @Test
    void testIsNotEmpty_whenCollectionIsNotEmpty_thenReturnTrue() {

        Collection<Integer> testCollection = new ArrayList<>();
        testCollection.add(1);
        testCollection.add(2);

        assertThat(CollectionUtils.isNotEmpty(testCollection)).isTrue();

    }

    @Test
    void testIsNotEmpty_whenCollectionIsNull_thenReturnFalse() {

        Collection<Object> testCollection = null;

        assertThat(CollectionUtils.isNotEmpty(testCollection)).isFalse();

    }
}
