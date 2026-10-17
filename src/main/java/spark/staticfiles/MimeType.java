/*
 * Copyright 2016 - Per Wendel
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
package spark.staticfiles;

import spark.resource.AbstractFileResolvingResource;
import spark.utils.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Configures and holds mappings from file extensions to MIME types.
 */
public class MimeType {

    private MimeType() {
    }

    static final String CONTENT_TYPE = "Content-Type";

    // The built-in mappings. They never change; mappings registered for a Service are kept by its
    // StaticFilesConfiguration.
    private static final Map<String, String> MAPPINGS;

    static {
        var mappings = new HashMap<String, String>();
        mappings.put("au", "audio/basic");
        mappings.put("avi", "video/msvideo,video/avi,video/x-msvideo");
        mappings.put("bmp", "image/bmp");
        mappings.put("bz2", "application/x-bzip2");
        mappings.put("css", "text/css");
        mappings.put("dtd", "application/xml-dtd");
        mappings.put("doc", "application/msword");
        mappings.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        mappings.put("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
        mappings.put("eot", "application/vnd.ms-fontobject");
        mappings.put("es", "application/ecmascript");
        mappings.put("exe", "application/octet-stream");
        mappings.put("gif", "image/gif");
        mappings.put("gz", "application/x-gzip");
        mappings.put("ico", "image/x-icon");
        mappings.put("hqx", "application/mac-binhex40");
        mappings.put("htm", "text/html");
        mappings.put("html", "text/html");
        mappings.put("jar", "application/java-archive");
        mappings.put("jpg", "image/jpeg");
        mappings.put("js", "application/javascript");
        mappings.put("mjs", "application/javascript");
        mappings.put("json", "application/json");
        mappings.put("midi", "audio/x-midi");
        mappings.put("mp3", "audio/mpeg");
        mappings.put("mpeg", "video/mpeg");
        mappings.put("ogg", "audio/vorbis,application/ogg");
        mappings.put("otf", "application/font-otf");
        mappings.put("pdf", "application/pdf");
        mappings.put("pl", "application/x-perl");
        mappings.put("png", "image/png");
        mappings.put("potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
        mappings.put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
        mappings.put("ppt", "application/vnd.ms-powerpointtd");
        mappings.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        mappings.put("ps", "application/postscript");
        mappings.put("qt", "video/quicktime");
        mappings.put("ra", "audio/x-pn-realaudio,audio/vnd.rn-realaudio");
        mappings.put("rar", "application/x-rar-compressed");
        mappings.put("ram", "audio/x-pn-realaudio,audio/vnd.rn-realaudio");
        mappings.put("rdf", "application/rdf,application/rdf+xml");
        mappings.put("rtf", "application/rtf");
        mappings.put("sgml", "text/sgml");
        mappings.put("sit", "application/x-stuffit");
        mappings.put("sldx", "application/vnd.openxmlformats-officedocument.presentationml.slide");
        mappings.put("svg", "image/svg+xml");
        mappings.put("swf", "application/x-shockwave-flash");
        mappings.put("tgz", "application/x-tar");
        mappings.put("tiff", "image/tiff");
        mappings.put("tsv", "text/tab-separated-values");
        mappings.put("ttf", "application/font-ttf");
        mappings.put("txt", "text/plain");
        mappings.put("wav", "audio/wav,audio/x-wav");
        mappings.put("woff", "application/font-woff");
        mappings.put("woff2", "application/font-woff2");
        mappings.put("xlam", "application/vnd.ms-excel.addin.macroEnabled.12");
        mappings.put("xls", "application/vnd.ms-excel");
        mappings.put("xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12");
        mappings.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        mappings.put("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
        mappings.put("xml", "application/xml");
        mappings.put("zip", "application/zip,application/x-compressed-zip");
        MAPPINGS = Map.copyOf(mappings);
    }

    /**
     * Resolves the MIME type for the given resource, based on its filename's extension, using only the
     * built-in mappings.
     *
     * @param resource the resource to resolve a MIME type for
     * @return the resolved MIME type, or {@code application/octet-stream} if unknown
     */
    public static String fromResource(AbstractFileResolvingResource resource) {
        return fromResource(resource, Map.of());
    }

    /**
     * Resolves the MIME type for the given resource, based on its filename's extension. A mapping in
     * {@code registered} takes precedence over a built-in one.
     *
     * @param resource   the resource to resolve a MIME type for
     * @param registered the mappings registered by the application, keyed by extension
     * @return the resolved MIME type, or {@code application/octet-stream} if unknown
     */
    static String fromResource(AbstractFileResolvingResource resource, Map<String, String> registered) {
        var filename = Optional.ofNullable(resource.getFilename()).orElse("");
        return getMimeType(filename, registered);
    }

    /**
     * Resolves the MIME type mapped to the given filename's extension.
     *
     * @param filename   the filename to resolve a MIME type for
     * @param registered the mappings registered by the application, keyed by extension
     * @return the resolved MIME type, or {@code application/octet-stream} if unknown
     */
    static String getMimeType(String filename, Map<String, String> registered) {
        var fileExtension = filename.replaceAll("^.*\\.(.*)$", "$1");
        return registered.getOrDefault(fileExtension, MAPPINGS.getOrDefault(fileExtension, "application/octet-stream"));
    }

    /**
     * Resolves the MIME type mapped to the given path's extension, using only the built-in mappings.
     *
     * @param pathInfo the path to resolve a MIME type for
     * @return the resolved MIME type, or {@code application/octet-stream} if unknown
     */
    protected static String fromPathInfo(String pathInfo) {
        return getMimeType(pathInfo, Map.of());
    }
}
