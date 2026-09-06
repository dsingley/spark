package spark.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

class MimeParseTest {

    @Test
    void testBestMatch() {

        final String header = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

        Collection<String> supported = List.of("application/xml", "text/html");

        assertThat(MimeParse.bestMatch(supported, header))
                .describedAs("""
				    bestMatch should return the supported mime type with the highest quality factor\
				     because it is preferred mime type\
				     as indicated in the HTTP header""")
                .isEqualTo("text/html");

    }

    @Test
    void testBestMatch_whenSupportedIsLowQualityFactor() {

        final String header = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

        Collection<String> supported = List.of("application/json");

        assertThat(MimeParse.bestMatch(supported, header))
                .describedAs("""
				    bestMatch should return the mime type even if it is not included in the supported\
				     mime types because it is considered by the */* all media type specified in the Accept\
				     Header""")
                .isEqualTo("application/json");
    }

    @Nested
    class FitnessAndQualityTest {

        @Test
        void compareToComparesFitnessFirst() {
            var lower = new MimeParse.FitnessAndQuality(1, 1.0f);
            var higher = new MimeParse.FitnessAndQuality(2, 0.0f);

            assertThat(lower).isLessThan(higher);
            assertThat(higher).isGreaterThan(lower);
        }

        @Test
        void compareToComparesQualityWhenFitnessIsEqual() {
            var lower = new MimeParse.FitnessAndQuality(1, 0.5f);
            var higher = new MimeParse.FitnessAndQuality(1, 0.8f);

            assertThat(lower).isLessThan(higher);
            assertThat(higher).isGreaterThan(lower);
        }

        @Test
        void compareToReturnsZeroForEqualValues() {
            var first = new MimeParse.FitnessAndQuality(1, 0.5f);
            var second = new MimeParse.FitnessAndQuality(1, 0.5f);

            assertThat(first).isEqualByComparingTo(second);
        }

        @Test
        void equalsReturnsTrueForEqualValues() {
            var first = new MimeParse.FitnessAndQuality(1, 0.5f);
            var second = new MimeParse.FitnessAndQuality(1, 0.5f);

            assertThat(first)
                .isEqualTo(second)
                .hasSameHashCodeAs(second);
        }

        @Test
        void equalsReturnsFalseForDifferentFitness() {
            var first = new MimeParse.FitnessAndQuality(1, 0.5f);
            var second = new MimeParse.FitnessAndQuality(2, 0.5f);

            assertThat(first).isNotEqualTo(second);
        }

        @Test
        void equalsReturnsFalseForDifferentQuality() {
            var first = new MimeParse.FitnessAndQuality(1, 0.5f);
            var second = new MimeParse.FitnessAndQuality(1, 0.8f);

            assertThat(first).isNotEqualTo(second);
        }

        @SuppressWarnings({ "AssertBetweenInconvertibleTypes", "EqualsWithItself" })
        @Test
        void equalsHandlesSameInstanceNullAndDifferentType() {
            var value = new MimeParse.FitnessAndQuality(1, 0.5f);

            assertThat(value)
                .isEqualTo(value)
                .isNotEqualTo(null)
                .isNotEqualTo("not a FitnessAndQuality");
        }

        @Test
        void compareToIsConsistentWithEquals() {
            var first = new MimeParse.FitnessAndQuality(1, 0.5f);
            var second = new MimeParse.FitnessAndQuality(1, 0.5f);

            assertThat(first)
                .isEqualByComparingTo(second)
                .isEqualTo(second)
                .hasSameHashCodeAs(second);
        }

        @Test
        void signedZerosAreNotEqual() {
            var positiveZero = new MimeParse.FitnessAndQuality(1, 0.0f);
            var negativeZero = new MimeParse.FitnessAndQuality(1, -0.0f);

            assertThat(positiveZero).isNotEqualTo(negativeZero)
                .isNotEqualByComparingTo(negativeZero);
        }

        @Test
        void nanValuesAreEqualAndCompareAsEqual() {
            var first = new MimeParse.FitnessAndQuality(1, Float.NaN);
            var second = new MimeParse.FitnessAndQuality(1, Float.NaN);

            assertThat(first)
                .isEqualByComparingTo(second)
                .isEqualTo(second)
                .hasSameHashCodeAs(second);
        }
    }
}
