/*
 * Copyright 2011- Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spark.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Some utility methods
 *
 * @author Per Wendel
 */
public final class SparkUtils {

    /** The path that filters mapped without a path use, which matches every path. */
    public static final String ALL_PATHS = "+/*paths";

    private SparkUtils() {
    }

    /**
     * Splits a route into its path segments, leaving out empty ones.
     *
     * @param route the route, for example {@code /users/:id}
     * @return the segments, for example {@code [users, :id]}
     */
    public static List<String> convertRouteToList(String route) {
        var pathArray = route.split("/");
        List<String> paths = new ArrayList<>();
        for (var path : pathArray) {
            if (!path.isEmpty()) {
                paths.add(path);
            }
        }
        return paths;
    }

    /**
     * @param routePart one segment of a route
     * @return true if the segment is a path parameter, which starts with a colon, such as {@code :id}
     */
    public static boolean isParam(String routePart) {
        return routePart.startsWith(":");
    }

    /**
     * @param routePart one segment of a route
     * @return true if the segment is a splat, which is a single asterisk
     */
    public static boolean isSplat(String routePart) {
        return routePart.equals("*");
    }

}
