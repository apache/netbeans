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
package org.netbeans.modules.subversion.client.cli.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.cli.SvnCommand;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;

/**
 *
 * @author Tomas Stupka
 */
public class VersionCommand extends SvnCommand {

    private List<String> output = new ArrayList<String>();
    private boolean unsupportedVersion = false;
    private boolean supportedMetadataFormat = false;
    public static final Version VERSION_15 = Version.parse("1.5");
    public static final Version VERSION_16 = Version.parse("1.6");

    @Override
    protected int getCommand() {
        return ISVNNotifyListener.Command.UNDEFINED;
    }

    @Override
    public void prepareCommand(Arguments arguments) throws IOException {
        arguments.add("--version");
    }

    @Override
    protected void config(File configDir, String username, String password, Arguments arguments) {
        arguments.addConfigDir(configDir);
    }

    @Override
    public void outputText(String lineString) {
        if (lineString == null || lineString.trim().equals("")) {
            return;
        }
        output.add(lineString);
        super.outputText(lineString);
    }

    public boolean checkForErrors() {
        Integer exitCode = getExitCode();
        if ((exitCode == null) || !exitCode.equals(Integer.valueOf(0))) {
            return false;
        }

        boolean outputProduced = false;

        for (String string : output) {
            if ((string = string.trim()).length() == 0) {     //skip empty lines
                continue;
            }

            outputProduced = true;

            int pos = string.indexOf(" version ");
            if (pos > -1) {
                String vString = string.substring(pos + 9);
                Subversion.LOG.log(Level.INFO, "Commandline client version: {0}", vString);
                Version version = Version.parse(vString);
                if (version.lowerThan(VERSION_15)) {
                    unsupportedVersion = true;
                    return false;
                } else if (version.sameMinor(VERSION_15)
                        || version.sameMinor(VERSION_16)) {
                    supportedMetadataFormat = true;
                }
            }

        }
        return outputProduced;
    }

    public boolean isUnsupportedVersion() {
        return unsupportedVersion;
    }

    public boolean isMetadataFormatSupported() {
        return supportedMetadataFormat;
    }

    public String getOutput() {
        StringBuffer sb = new StringBuffer();
        for (String string : output) {
            sb.append(string);
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Version holder with semantic verioning support. Keep svn --version output
     * is in mind. The container is compatible with MAJOR.MINOR.PATCH formatp
     * lus some remainder as it happens in svn --version. I.e. "svn, version
     * 1.10.0 (r1827917)" is supported Regular 1.10.1 is supported too.
     *
     * Note on exceptions: Exceptions are unchecked and not expected to occur,
     * if that happens its a bug.
     */
    public static class Version implements Comparable<Version> {

        private int[] parts;
        public String remainder;

        static final Version UNKNOWN = new Version();

        private Version() {
        }

        /**
         * Parse version string into container. String must be in
         * "MAJOR.MINOR.PATCH reminder" format. Reminder part is optional.
         *
         * @param version non null string
         * @return parsed version or UKNOWN if parsing error happens
         */
        public static Version parse(String version) {
            return parse(version, " ");
        }

        /**
         * Parse version string into container. String must be in
         * "MAJOR.MINOR.PATCH-reminder" format. Reminder part is optional.
         * @param version string with version, say 1.1.1-SNAPSHOT
         * @param remainderDelimiter a symbol to separate semantic version from reminder, say "-" for "1.1.1-SNAPSHOT"
         * @return
         */
        public static Version parse(String version, String remainderDelimiter) {
            String symanticVersion;
            int symanticEnds = version.indexOf(remainderDelimiter);
            symanticVersion = symanticEnds < 0
                    ? version
                    : version.substring(0, symanticEnds);

            StringTokenizer tokenizer = new StringTokenizer(symanticVersion, ".");
            Version result = new Version();
            if (tokenizer.hasMoreTokens()) {
                fetchParts(tokenizer, result);
                result.remainder = symanticEnds < 0
                        ? ""
                        : version.substring(symanticEnds + 1);
                return result;
            } else {
                return UNKNOWN;
            }
        }

        private static void fetchParts(StringTokenizer t, Version r) {
            int n = Integer.parseInt(t.nextToken());
            int a[];
            if (r.parts == null) {
                a = new int[]{n};
            } else {
                a = new int[r.parts.length + 1];
                System.arraycopy(r.parts, 0, a, 0, r.parts.length);
                a[r.parts.length] = n;
            }
            r.parts = a;
            if (t.hasMoreTokens()) {
                fetchParts(t, r);
            }
        }

        public boolean isUnknown() {
            return this == UNKNOWN;
        }

        @Override
        public int compareTo(Version t) {
            if (t == null) {
                return 1;
            } else if (this.isUnknown() && t.isUnknown()) {
                return 0;
            } else {
                for (int i = 0; i < parts.length; i++) {
                    if (t.parts.length < i) {
                        return 1;
                    }
                    int a = parts[i];
                    int b = t.parts[i];
                    if (a < b) {
                        return -1;
                    } else if (a > b) {
                        return +1;
                    }
                }
                return t.parts.length > parts.length ? -1 : 0;
            }
        }

        public boolean lowerThan(Version that) {
            return compareTo(that) < 0;
        }

        /**
         * Check if MAJOR and MINOR parts are equal
         *
         * @param that can be null
         * @return
         */
        public boolean sameMinor(Version that) {
            return that != null && trim(2).compareTo(that.trim(2)) == 0;
        }

        private Version trim(int level) {
            if (isUnknown()) {
                return this;
            } else {
                Version n = new Version();
                n.remainder = this.remainder;
                n.parts = new int[level];
                System.arraycopy(parts, 0, n.parts, 0, Math.min(level, parts.length));
                return n;
            }
        }

        public boolean greaterThan(Version that) {
            return compareTo(that) > 0;
        }
    }
}
