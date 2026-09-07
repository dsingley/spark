package spark.staticfiles;

import static spark.utils.StringUtils.removeLeadingAndTrailingSlashesFrom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

/**
 * Created by Per Wendel on 2016-11-05.
 */
public class StaticFilesFolder {

    private static final Logger LOG = LoggerFactory.getLogger(StaticFilesFolder.class);
    
    private static volatile String local;
    private static volatile String external;

    private StaticFilesFolder() {
    }

    /**
     * @deprecated static file locations are no longer global; use {@link spark.Service#staticFileLocation(String)}
     */
    @Deprecated(since = "2.9.0")
    public static void localConfiguredTo(String folder) {

        local = removeLeadingAndTrailingSlashesFrom(folder);
    }

    /**
     * @deprecated static file locations are no longer global; use {@link spark.Service#externalStaticFileLocation(String)}
     */
    @Deprecated(since = "2.9.0")
    public static void externalConfiguredTo(String folder) {

        String unixLikeFolder = Paths.get(folder).toAbsolutePath().toString().replace("\\", "/");
        LOG.warn("Registering external static files folder [{}] as [{}].", folder, unixLikeFolder);
        external = removeLeadingAndTrailingSlashesFrom(unixLikeFolder);
    }

    /**
     * @deprecated static file locations are no longer global; each {@link spark.Service} now
     * has its own, which is not exposed via a getter
     */
    @Deprecated(since = "2.9.0")
    public static String local() {
        return local;
    }

    /**
     * @deprecated static file locations are no longer global; each {@link spark.Service} now
     * has its own, which is not exposed via a getter
     */
    @Deprecated(since = "2.9.0")
    public static String external() {
        return external;
    }

}
