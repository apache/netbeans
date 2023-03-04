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
package org.netbeans.modules.nativeexecution.support;

import java.io.OutputStreamWriter;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.Exceptions;

/**
 *
 * @author ak119685
 */
public final class EnvWriter {

    public static final Collection<String> wellKnownVars = Collections.unmodifiableList(Arrays.asList(
        "LANG", "LC_COLLATE", "LC_CTYPE", "LC_MESSAGES", "LC_MONETARY", // NOI18N
        "LC_NUMERIC", "LC_TIME", "TMPDIR", "PATH", "LD_LIBRARY_PATH", // NOI18N
        "LD_PRELOAD")); // NOI18N

    private final OutputStreamWriter writer;

    public EnvWriter(final OutputStream os, final boolean remote) {
        OutputStreamWriter w = null;

        if (remote) {
            try {
                String charSet = ProcessUtils.getRemoteCharSet();
                if (java.nio.charset.Charset.isSupported(charSet)) {
                    w = new OutputStreamWriter(os, charSet);
                }
            } catch (UnsupportedEncodingException ex) {
            }
        }

        if (w == null) {
            w = new OutputStreamWriter(os);
        }

        this.writer = w;
    }

    public static byte[] getBytes(String str, boolean remote) {
        if (remote) {
            final String charSet = ProcessUtils.getRemoteCharSet();
            if (java.nio.charset.Charset.isSupported(charSet)) {
                try {
                    return str.getBytes(charSet);
                } catch (UnsupportedEncodingException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return str.getBytes();
    }

    public EnvWriter(OutputStreamWriter writer) {
        this.writer = writer;
    }

    public void write(final MacroMap env) throws IOException {
        if (!env.isEmpty()) {
            String value = null;
            // Very simple sanity check of vars...
            Pattern pattern = Pattern.compile("[A-Z0-9_]+"); // NOI18N

            for (String name : env.getExportVariablesSet()) {
                // check capitalized key by pattern
                if (!pattern.matcher(name.toUpperCase(java.util.Locale.ENGLISH)).matches()) {
                    Logger.getInstance().log(Level.WARNING,
                            "Will not pass environment variable named {0} as it contains non alpha-numeric characters", name); // NOI18N
                    continue;
                }

                value = env.get(name);

                // As value will be enclosed in quotes, will escape all
                // existent quotes.

                if (value != null) {
                    if (value.indexOf('"') >= 0) { // NOI18N
                        value = value.replace("\"", "\\\""); // NOI18N
                    }

                    writer.write(name + "=\"" + value + "\" && export " + name + "\n"); // NOI18N
                }
            }

            writer.flush();
        }
    }
}
