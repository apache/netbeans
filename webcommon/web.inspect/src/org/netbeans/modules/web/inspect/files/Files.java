/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.inspect.files;

import java.nio.charset.Charset;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for fetching content of files stored in separate files.
 *
 * @author Jan Stola
 */
public final class Files {

    private static final Logger LOGGER = Logger.getLogger(Files.class.getName());

    /** Base folder where the scripts are located.  */
    private static final String RESOURCE_BASE = "/org/netbeans/modules/web/inspect/files/"; // NOI18N
    /** Extension of the script files. */
    private static final String SCRIPT_EXTENSION = ".js"; // NOI18N
    /** Comment start of the script files. */
    private static final String SCRIPT_COMMENT_START = "/*"; // NOI18N
    /** Comment end of the script files. */
    private static final String SCRIPT_COMMENT_END = "*/"; // NOI18N
    /** Extension of the html files. */
    private static final String HTML_EXTENSION = ".html"; // NOI18N
    /** Comment start of the script files. */
    private static final String HTML_COMMENT_START = "<!--"; // NOI18N
    /** Comment end of the script files. */
    private static final String HTML_COMMENT_END = "-->"; // NOI18N
    /** Map with already loaded scripts, it maps name of the script to the script. */
    private static final ConcurrentMap<String, String> FILES = new ConcurrentHashMap<String, String>();


    private Files() {
    }

    /**
     * Returns script with the specified name.
     *
     * @param name name of the script.
     * @return script with the specified name.
     */
    public static String getScript(String name) {
        return getContent(name + SCRIPT_EXTENSION, SCRIPT_COMMENT_START, SCRIPT_COMMENT_END);
    }

    /**
     * Returns HTML file with the specified name.
     *
     * @param name name of the HTML file.
     * @return HTML file with the specified name.
     */
    public static String getHtml(String name) {
        return getContent(name + HTML_EXTENSION, HTML_COMMENT_START, HTML_COMMENT_END);
    }

    private static String getContent(String filename, String commentStart, String commentEnd) {
        String content = FILES.get(filename);
        if (content != null) {
            return content;
        }
        String resourceName = RESOURCE_BASE + filename;
        content = loadContent(resourceName, commentStart, commentEnd);
        FILES.put(filename, content);
        return content;
    }

    /**
     * Loads the content with the specified resource name.
     *
     * @param resourceName resource name of the script.
     * @return content with the specified resource name.
     */
    private static String loadContent(String resourceName, String commentStart, String commentEnd) {
        InputStream stream = Files.class.getResourceAsStream(resourceName);
        String content = null;
        if (stream != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            try {
                StringBuilder sb = new StringBuilder(stream.available());
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                content = removeLicense(sb.toString(), commentStart, commentEnd);
            } catch (IOException ioex) {
                LOGGER.log(Level.INFO, null, ioex);
            } finally {
                try {
                    br.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
        }
        return content;
    }

    /**
     * Removes the license header from the loaded content of a file.
     *
     * @param content content of a file that should be stripped of
     * the license.
     * @return content without the license header.
     */
    private static String removeLicense(String content, String commentStart, String commentEnd) {
        while (content.trim().startsWith(commentStart)) {
            int endOfComment = content.indexOf(commentEnd);
            content = content.substring(endOfComment + commentEnd.length()).trim();
        }
        return content;
    }

}
