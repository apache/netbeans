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
                "PHP_SELF", // NOI18N
                "GATEWAY_INTERFACE", // NOI18N
                "SERVER_ADDR", // NOI18N
                "SERVER_NAME", // NOI18N
                "SERVER_SOFTWARE", // NOI18N
                "SERVER_PROTOCOL", // NOI18N
                "REQUEST_METHOD", // NOI18N
                "QUERY_STRING", // NOI18N
                "DOCUMENT_ROOT", // NOI18N
                "HTTP_ACCEPT", // NOI18N
                "HTTP_ACCEPT_CHARSET", // NOI18N
                "HTTP_ACCEPT_ENCODING", // NOI18N
                "HTTP_ACCEPT_LANGUAGE", // NOI18N
                "HTTP_CONNECTION", // NOI18N
                "HTTP_HOST", // NOI18N
                "HTTP_REFERER", // NOI18N
                "HTTP_USER_AGENT", // NOI18N
                "HTTPS", // NOI18N
                "REMOTE_ADDR", // NOI18N
                "REMOTE_HOST", // NOI18N
                "REMOTE_PORT", // NOI18N
                "SCRIPT_FILENAME", // NOI18N
                "SERVER_ADMIN", // NOI18N
                "SERVER_PORT", // NOI18N
                "SERVER_SIGNATURE", // NOI18N
                "PATH_TRANSLATED", // NOI18N
                "SCRIPT_NAME", // NOI18N
                "REQUEST_URI", // NOI18N
                "PHP_AUTH_DIGEST", // NOI18N
                "PHP_AUTH_USER", // NOI18N
                "PHP_AUTH_PW", // NOI18N
                "AUTH_TYPE" // NOI18N
            }));


    public static final Set<String> MAGIC_METHODS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(new String[]{
                "__callStatic", // NOI18N
                "__set_state", // NOI18N
                "__call", // NOI18N
                "__clone", // NOI18N
                "__construct", // NOI18N
                "__destruct", // NOI18N
                "__get", // NOI18N
                "__set", // NOI18N
                "__isset", // NOI18N
                "__unset", // NOI18N
                "__sleep", // NOI18N
                "__wakeup", // NOI18N
                "__toString", // NOI18N
                "__invoke", // NOI18N
                "__debugInfo", // NOI18N
                "__serialize", // NOI18N PHP 7.4
                "__unserialize", // NOI18N PHP 7.4
            })));

    public static final Set<String> ATTRIBUTE_NAMES = Collections.unmodifiableSet(Attributes.ATTRIBUTE_NAMES);
    public static final Set<String> ATTRIBUTE_FQ_NAMES = Collections.unmodifiableSet(Attributes.ATTRIBUTE_FQ_NAMES);

    public static enum VariableKind {
        STANDARD,
        THIS,
        SELF,
        PARENT
    };

    // https://www.php.net/manual/en/reserved.attributes.php
    public static enum Attributes {
        ATTRIBUTE("Attribute"), // NOI18N
        ALLOW_DYNAMIC_PROPERTIES("AllowDynamicProperties"), // NOI18N
        OVERRIDE("Override"), // NOI18N
        RETURN_TYPE_WILL_CHANGE("ReturnTypeWillChange"), // NOI18N
        SENSITIVE_PARAMETER("SensitiveParameter"), // NOI18N
        ;

        private static final Set<String> ATTRIBUTE_NAMES = new HashSet<>();
        private static final Set<String> ATTRIBUTE_FQ_NAMES = new HashSet<>();

        private final String name;
        private final String fqName;
        private final String asAttributeExpression;

        static {
            for (Attributes attribute : Attributes.values()) {
                ATTRIBUTE_NAMES.add(attribute.getName());
                ATTRIBUTE_FQ_NAMES.add(attribute.getFqName());
            }
        }

        private Attributes(String name) {
            this.name = name;
            this.fqName = "\\" + name; // NOI18N
            this.asAttributeExpression = String.format("#[%s]", fqName); // NOI18N
        }

        public String getName() {
            return name;
        }

        public String getFqName() {
            return fqName;
        }

        public String asAttributeExpression() {
            return asAttributeExpression;
        }
    }

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
