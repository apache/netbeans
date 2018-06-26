/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect.files;

import java.nio.charset.Charset;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
            BufferedReader br = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8"))); // NOI18N
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
