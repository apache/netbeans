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
package org.netbeans.modules.cnd.lsp.pkgconfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;

/**
 * Responsible for expanding substrings of the form "`pkgconfig --cflags XXX`"
 * and similar, and keeping expansions in a map to avoid excessive Process
 * calls. Can be run in test mode (substrings are checked for correctness, but
 * are not expanded).
 *
 * @author antonio
 */
public final class PkgConfigExpander {

    private static final Logger LOG = Logger.getLogger(PkgConfigExpander.class.getName());

    public static enum ExpansionStrategy {
        TESTING,
        PRODUCTION,
    }

    private final HashMap<String, String> expansions;
    private final ExpansionStrategy strategy;

    public PkgConfigExpander() {
        this(ExpansionStrategy.PRODUCTION);
    }

    public PkgConfigExpander(ExpansionStrategy strategy) {
        this.strategy = strategy;
        this.expansions = new HashMap<>();
    }

    /**
     * Expands `pkg-config ` invocations in a command line.
     *
     * @param commandLine The command line, with one or more `pkg-config` calls
     * @return The expanded command line, substituting `pkg-config --cflags ...`
     * with the result of pkg-config invocations.
     * @throws IOException If an I/O error happens.
     */
    public String expandPkgConfig(String commandLine) throws IOException {
        StringBuilder expanded = new StringBuilder();
        String string = commandLine;

        do {
            int i = string.indexOf('`');
            if (i == -1) {
                expanded.append(string);
                break;
            }
            int j = string.indexOf('`', i + 1);
            if (j == -1) {
                throw new IOException(String.format("Unmatched ` quote in '%s'", string));
            }
            expanded.append(string.substring(0, i));
            String expansion = expandSubstring(string.substring(i + 1, j));
            expanded.append(expansion);
            string = string.substring(j + 1);
        } while (string.length() > 0);

        return expanded.toString();
    }

    protected String expandSubstring(String toExpand) throws IOException {
        // We don't want to block the EDT...
        assert !SwingUtilities.isEventDispatchThread();
        if (ExpansionStrategy.TESTING == strategy) {
            return "`" + toExpand + "`"; // NOI18N
        }
        // Check if we're invoking `pkgconfig` or not
        if (!toExpand.startsWith("pkg-config")) // NOI18N
        {
            throw new IOException(String.format("Invalid expansion `%s` not starting with pkg-config", toExpand));
        }
        String result = expansions.get(toExpand);
        if (result != null) {
            // We've invoked `pkg-config` previously, let's reuse the result..
            LOG.log(Level.FINE, "Cache hit for expansion `{0}`", toExpand);
            return result;
        }
        ProcessBuilder builder = new ProcessBuilder();
        String[] arguments = splitCommandIntoArguments(toExpand);
        builder.command(arguments);
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process pkgConfigProcess = builder.start();
        int exitCode;
        try {
            exitCode = pkgConfigProcess.waitFor();
        } catch (InterruptedException ex) {
            throw new IOException(String.format("Expansion of command `%s` failed with %s(%s)", toExpand, ex.getMessage(), ex.getClass().getName()), ex);
        }
        if (exitCode != 0) {
            throw new IOException(String.format("Expansion of command `%s` failed with code %d", toExpand, exitCode));
        }
        byte[] buffer = new byte[4 * 1024];
        String outputString = null;
        try ( InputStream input = pkgConfigProcess.getInputStream();  ByteArrayOutputStream output = new ByteArrayOutputStream(10 * 1024)) {
            do {
                int n = input.read(buffer);
                if (n == -1) {
                    break;
                }
                output.write(buffer, 0, n);
            } while (true);
            outputString = new String(output.toByteArray(), StandardCharsets.US_ASCII);
        }
        expansions.put(toExpand, outputString);
        return outputString;
    }

    /**
     * Matches "...", or '...' or not.
     */
    private static final Pattern PARTS = Pattern.compile("\"([^\"]*)\"|'([^']*)'|(\\S+)"); // NOI18N

    /**
     * Splits a string into whitespace separated parts, but keeping together
     * strings quoted by " and '.
     *
     * @param command A string with substrings quoted with " or '.
     * @return An array of substrings.
     */
    public static String[] splitCommandIntoArguments(String command) {
        ArrayList<String> parts = new ArrayList<>(16);
        Matcher matcher = PARTS.matcher(command);
        while (matcher.find()) {
            String doubleQuoted = matcher.group(1);
            if (doubleQuoted != null) {
                parts.add(doubleQuoted);
            } else {
                String singleQuoted = matcher.group(2);
                if (singleQuoted != null) {
                    parts.add(singleQuoted);
                } else {
                    parts.add(matcher.group(3));
                }
            }
        }
        return parts.toArray(new String[parts.size()]);
    }

}
