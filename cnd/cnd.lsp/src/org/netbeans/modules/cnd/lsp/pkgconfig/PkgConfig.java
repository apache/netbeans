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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;

/**
 * Responsible for invoking "pkg-config" and retrieving the results.
 *
 * @author antonio
 */
public class PkgConfig {

    private static final Logger LOG = Logger.getLogger(PkgConfig.class.getName());
    private static final String PKG_CONFIG_WINDOWS = "pkg-config.exe"; // NOI18N
    private static final String PKG_CONFIG_UNIX = "pkg-config"; // NOI18N

    private final ExecutionEnvironment env;
    private final HostInfo.OSFamily osFamily;
    private final String pkgConfigExecutable;
    private final HashMap<String, String> expansions;
    private final boolean windows;

    public PkgConfig(ExecutionEnvironment env) throws IOException, ConnectionManager.CancellationException {
        this.expansions = new HashMap<>();
        this.env = env;
        this.osFamily = HostInfoUtils.getHostInfo(env).getOSFamily();
        if (HostInfo.OSFamily.WINDOWS == this.osFamily) {
            this.windows = true;
            this.pkgConfigExecutable = PKG_CONFIG_WINDOWS;
        } else {
            this.windows = false;
            this.pkgConfigExecutable = PKG_CONFIG_UNIX;
        }
    }

    /**
     * Returns true if the ExecutionEnvironment is a windows operating system.
     * @return true if the ExecutionEnvironment is windows, false otherwise.
     */
    public boolean isWindows() {
        return windows;
    }

    /**
     * Expands `pkg-config ` invocations in a command line.
     *
     * @param commandLine The command line, with one or more `pkg-config` calls
     * @return The expanded command line, substituting `pkg-config --cflags ...`
     * with the result of pkg-config invocations.
     * @throws IOException If an I/O error happens.
     */
    public String expand(String commandLine) throws IOException {
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

    private String expandSubstring(String toExpand) throws IOException {
        // We don't want to block the EDT...
        assert !SwingUtilities.isEventDispatchThread();
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
        String[] splittedCommandLine = splitCommandIntoArguments(toExpand);
        String[] arguments = new String[splittedCommandLine.length - 1];
        System.arraycopy(splittedCommandLine, 1, arguments, 0, arguments.length);

        ExitStatus execution = call(arguments);
        String expansion = execution.getOutputString();
        // Cache the result, so we don't keep on invoking the same command over and over.
        expansions.put(toExpand, expansion);
        return expansion;
    }

    /**
     * Invokes "pkg-config" in the ExecutionEnvironment, and returns the output.
     * @param arguments The arguments to "pkg-config".
     * @return An ExitStatus (always isOK).
     * @throws IOException If ExitStatus representing the execution is not ok.
     */
    public ExitStatus call(String [] arguments) throws IOException {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Invoking pkg-config: {0} arguments {1}",
                    new Object[]{pkgConfigExecutable, Arrays.toString(arguments)});
        }
        ExitStatus execution = ProcessUtils.execute(env, pkgConfigExecutable, arguments);
        if (!execution.isOK()) {
            LOG.log(Level.WARNING, "Error invoking {0} arguments: {1} exitStatus: {2} error {3}",
                    new Object[] {
                        pkgConfigExecutable,
                        Arrays.toString(arguments),
                        execution.exitCode,
                        execution.getErrorString()
                    });
            throw new IOException(String.format("Expansion of command '%s' (%s) failed with exit code %d",
                    pkgConfigExecutable, Arrays.toString(arguments), execution.exitCode));
        }
        return execution;
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
    /* package */ static final String[] splitCommandIntoArguments(String command) {
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
