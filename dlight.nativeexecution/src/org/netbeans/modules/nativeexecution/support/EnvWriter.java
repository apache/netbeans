/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
