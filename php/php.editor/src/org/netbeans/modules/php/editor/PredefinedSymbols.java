/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.php.editor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;

/**
 *
 * @author tomslot
 */
public final class PredefinedSymbols {
    private static final Logger LOGGER = Logger.getLogger(PredefinedSymbols.class.getName());
    // see http://www.php.net/manual/en/reserved.variables.php
    public static final Collection<String> SUPERGLOBALS = Collections.unmodifiableSet(new TreeSet<>(Arrays.asList(
            "GLOBALS", "_SERVER", "_GET", "_POST", "_FILES", //NOI18N
            "_COOKIE", "_SESSION", "_REQUEST", "_ENV", "php_errormsg", //NOI18N
            "HTTP_RAW_POST_DATA", "http_response_header", "argc", "argv"))); //NOI18N


    public static final List<String> SERVER_ENTRY_CONSTANTS =
            Collections.unmodifiableList(Arrays.asList(new String[]{
                "PHP_SELF",
                "GATEWAY_INTERFACE",
                "SERVER_ADDR",
                "SERVER_NAME",
                "SERVER_SOFTWARE",
                "SERVER_PROTOCOL",
                "REQUEST_METHOD",
                "QUERY_STRING",
                "DOCUMENT_ROOT",
                "HTTP_ACCEPT",
                "HTTP_ACCEPT_CHARSET",
                "HTTP_ACCEPT_ENCODING",
                "HTTP_ACCEPT_LANGUAGE",
                "HTTP_CONNECTION",
                "HTTP_HOST",
                "HTTP_REFERER",
                "HTTP_USER_AGENT",
                "HTTPS",
                "REMOTE_ADDR",
                "REMOTE_HOST",
                "REMOTE_PORT",
                "SCRIPT_FILENAME",
                "SERVER_ADMIN",
                "SERVER_PORT",
                "SERVER_SIGNATURE",
                "PATH_TRANSLATED",
                "SCRIPT_NAME",
                "REQUEST_URI",
                "PHP_AUTH_DIGEST",
                "PHP_AUTH_USER",
                "PHP_AUTH_PW",
                "AUTH_TYPE"
            }));


    public static final Set<String> MAGIC_METHODS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(new String[]{
                "__callStatic",
                "__set_state",
                "__call",
                "__clone",
                "__construct",
                "__destruct",
                "__get",
                "__set",
                "__isset",
                "__unset",
                "__sleep",
                "__wakeup",
                "__toString"
            })));

    public static enum VariableKind {
        STANDARD,
        THIS,
        SELF,
        PARENT
    };

    private static String docURLBase;

    private PredefinedSymbols() {
    }

    private static void initDoc() {
        File file = InstalledFileLocator.getDefault().locate("docs/predefined_vars.zip", null, true); //NoI18N
        if (file != null) {
            try {
                URL urll = Utilities.toURI(file).toURL();
                urll = FileUtil.getArchiveRoot(urll);
                docURLBase = urll.toString();
            } catch (java.net.MalformedURLException e) {
                LOGGER.log(Level.FINE, null, e);
            }
        }
    }

    public static boolean isSuperGlobalName(String name) {
        return SUPERGLOBALS.contains(name);
    }

    public static String getDocumentation(String name) {
        if (docURLBase == null) {
            initDoc();
        }

        String resPath = String.format("%s%s.desc", docURLBase, name); //NOI18N

        try {
            URL url = new URL(resPath);
            ByteArrayOutputStream baos;
            try (InputStream is = url.openStream()) {
                byte[] buffer = new byte[1000];
                baos = new ByteArrayOutputStream();
                int count;
                do {
                    count = is.read(buffer);
                    if (count > 0) {
                        baos.write(buffer, 0, count);
                    }
                } while (count > 0);
            }
            String text = baos.toString(Charset.defaultCharset().name());
            baos.close();
            return text;
        } catch (java.io.IOException e) {
            return null;
        }
    }
}
