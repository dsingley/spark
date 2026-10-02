package spark.utils;

/**
 * Created by Per Wendel on 2016-04-25.
 */
public interface Wrapper {

    /**
     * @return the underlying object this instance wraps
     */
    Object delegate();

}
