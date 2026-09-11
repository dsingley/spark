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

    private static volatile boolean guessingOn = true;

    private static final Map<String, String> MAPPINGS = new HashMap<>();

    static {
        MAPPINGS.put("au", "audio/basic");
        MAPPINGS.put("avi", "video/msvideo,video/avi,video/x-msvideo");
        MAPPINGS.put("bmp", "image/bmp");
        MAPPINGS.put("bz2", "application/x-bzip2");
        MAPPINGS.put("css", "text/css");
        MAPPINGS.put("dtd", "application/xml-dtd");
        MAPPINGS.put("doc", "application/msword");
        MAPPINGS.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        MAPPINGS.put("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
        MAPPINGS.put("eot", "application/vnd.ms-fontobject");
        MAPPINGS.put("es", "application/ecmascript");
        MAPPINGS.put("exe", "application/octet-stream");
        MAPPINGS.put("gif", "image/gif");
        MAPPINGS.put("gz", "application/x-gzip");
        MAPPINGS.put("ico", "image/x-icon");
        MAPPINGS.put("hqx", "application/mac-binhex40");
        MAPPINGS.put("htm", "text/html");
        MAPPINGS.put("html", "text/html");
        MAPPINGS.put("jar", "application/java-archive");
        MAPPINGS.put("jpg", "image/jpeg");
        MAPPINGS.put("js", "application/javascript");
        MAPPINGS.put("mjs", "application/javascript");
        MAPPINGS.put("json", "application/json");
        MAPPINGS.put("midi", "audio/x-midi");
        MAPPINGS.put("mp3", "audio/mpeg");
        MAPPINGS.put("mpeg", "video/mpeg");
        MAPPINGS.put("ogg", "audio/vorbis,application/ogg");
        MAPPINGS.put("otf", "application/font-otf");
        MAPPINGS.put("pdf", "application/pdf");
        MAPPINGS.put("pl", "application/x-perl");
        MAPPINGS.put("png", "image/png");
        MAPPINGS.put("potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
        MAPPINGS.put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
        MAPPINGS.put("ppt", "application/vnd.ms-powerpointtd");
        MAPPINGS.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        MAPPINGS.put("ps", "application/postscript");
        MAPPINGS.put("qt", "video/quicktime");
        MAPPINGS.put("ra", "audio/x-pn-realaudio,audio/vnd.rn-realaudio");
        MAPPINGS.put("rar", "application/x-rar-compressed");
        MAPPINGS.put("ram", "audio/x-pn-realaudio,audio/vnd.rn-realaudio");
        MAPPINGS.put("rdf", "application/rdf,application/rdf+xml");
        MAPPINGS.put("rtf", "application/rtf");
        MAPPINGS.put("sgml", "text/sgml");
        MAPPINGS.put("sit", "application/x-stuffit");
        MAPPINGS.put("sldx", "application/vnd.openxmlformats-officedocument.presentationml.slide");
        MAPPINGS.put("svg", "image/svg+xml");
        MAPPINGS.put("swf", "application/x-shockwave-flash");
        MAPPINGS.put("tgz", "application/x-tar");
        MAPPINGS.put("tiff", "image/tiff");
        MAPPINGS.put("tsv", "text/tab-separated-values");
        MAPPINGS.put("ttf", "application/font-ttf");
        MAPPINGS.put("txt", "text/plain");
        MAPPINGS.put("wav", "audio/wav,audio/x-wav");
        MAPPINGS.put("woff", "application/font-woff");
        MAPPINGS.put("woff2", "application/font-woff2");
        MAPPINGS.put("xlam", "application/vnd.ms-excel.addin.macroEnabled.12");
        MAPPINGS.put("xls", "application/vnd.ms-excel");
        MAPPINGS.put("xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12");
        MAPPINGS.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        MAPPINGS.put("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
        MAPPINGS.put("xml", "application/xml");
        MAPPINGS.put("zip", "application/zip,application/x-compressed-zip");
    }

    public static void register(String extension, String mimeType) {
        MAPPINGS.put(extension, mimeType);
    }

    public static void disableGuessing() {
        guessingOn = false;
    }

    public static String fromResource(AbstractFileResolvingResource resource) {
        var filename = Optional.ofNullable(resource.getFilename()).orElse("");
        return getMimeType(filename);
    }

    protected static String getMimeType(String filename) {
        var fileExtension = filename.replaceAll("^.*\\.(.*)$", "$1");
        return MAPPINGS.getOrDefault(fileExtension, "application/octet-stream");
    }

    protected static String fromPathInfo(String pathInfo) {
        return getMimeType(pathInfo);
    }

    protected static boolean shouldGuess() {
        return guessingOn;
    }
}
