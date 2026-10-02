package spark.staticfiles;

import static spark.utils.StringUtils.removeLeadingAndTrailingSlashesFrom;

import java.nio.file.Paths;

/**
 * Protecting against Directory traversal
 */
public class DirectoryTraversal {

    private DirectoryTraversal() {
    }

    /**
     * Throws if the given classpath resource path escapes the given classpath folder.
     *
     * @param path        the requested path
     * @param localFolder the classpath folder the path must stay within
     * @throws DirectoryTraversalDetection if the path escapes localFolder
     */
    public static void protectAgainstInClassPath(String path, String localFolder) {
        if (!isPathWithinFolder(path, localFolder)) {
            throw new DirectoryTraversalDetection("classpath");
        }
    }

    /**
     * Throws if the given external file path escapes the given external folder.
     *
     * @param path           the requested path
     * @param externalFolder the external folder the path must stay within
     * @throws DirectoryTraversalDetection if the path escapes externalFolder
     */
    public static void protectAgainstForExternal(String path, String externalFolder) {
    	var unixLikeFolder = unixifyPath(externalFolder);
        var nixLikePath = unixifyPath(path);
        if (!isPathWithinFolder(nixLikePath, unixLikeFolder)) {
            throw new DirectoryTraversalDetection("external");
        }
    }
    
    private static String unixifyPath(String path) {
    	return Paths.get(path).toAbsolutePath().toString().replace("\\", "/");
    }
    
    private static boolean isPathWithinFolder(String path, String folder) {
    	var rlatsPath = removeLeadingAndTrailingSlashesFrom(path);
    	var rlatsFolder = removeLeadingAndTrailingSlashesFrom(folder);
    	return rlatsPath.startsWith(rlatsFolder);
    }

    /**
     * Thrown when a requested path escapes the folder it's meant to be confined to.
     */
    public static final class DirectoryTraversalDetection extends RuntimeException {
        private static final long serialVersionUID = 1L;

        /**
         * @param msg which protection check detected the traversal ("classpath" or "external")
         */
        public DirectoryTraversalDetection(String msg) {
            super(msg);
        }

    }

}
