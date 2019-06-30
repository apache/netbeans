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
package org.netbeans.modules.payara.tooling.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.payara.tooling.logging.Logger;
import org.netbeans.modules.payara.tooling.server.config.JavaSEPlatform;

/**
 * Java related utilities
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class JavaUtils {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Java executables directory underJava home. */
    private static final String  JAVA_BIN_DIR = "bin";

    /** Java VM executable file name (without path). */
    private static final String  JAVA_VM_EXE = "java";

    /** Java VM command line option to retrieve version. */
    private static final String VM_VERSION_OPT = "-version";

    /** Java SE JDK class path option. */
    public static final String VM_CLASSPATH_OPTION = "-cp";

    /** Java VM system property option. */
    private static final String VM_SYS_PROP_OPT = "-D";

    /** Java VM system property quoting character. */
    private static final char VM_SYS_PROP_QUOTE = '"';

    /** Java VM system property assignment. */
    private static final String VM_SYS_PROP_ASSIGN = "=";

    /** Java VM system environment <code>JAVA_HOME</code> variable name. */
    public static final String JAVA_HOME_ENV = "JAVA_HOME";

    /** UTF-8 {@link Charset}. */
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    /**
     * Java VM version output regular expression pattern.
     * <p/>
     * Regular expression contains tokens to read individual version number
     * components. Expected input is string like
     * <code>java version "1.6.0_30"</code>.
     */
//    private static final String VM_VERSION_PATTERN =
//            " *[jJ][aA][vV][aA] +[vV][eE][rR][sS][iI][oO][nN] +" +
//            "\"{0,1}([0-9]+).([0-9]+).([0-9]+)_([0-9]+)\"{0,1} *";
    private static final String VM_VERSION_PATTERN =
            "[^0-9]*([0-9]+)\\.([0-9]+)(?:\\.([0-9]+)(?:[-_\\.]([0-9]+)){0,1}){0,1}[^0-9]*";

    /** Number of <code>Matcher</code> groups (REGEX tokens) expected in Java VM
     *  version output. */
    private static final int VM_MIN_VERSION_TOKENS = 2;

    ////////////////////////////////////////////////////////////////////////////
    // Static classes                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Java VM version storage class.
     * <p/>
     * Stored version is in
     * <code>&lt;major&gt;.&lt;minor&lt;.&lt;revision&lt;_&lt;update&lt;></code>
     */
    public static class JavaVersion {

        ////////////////////////////////////////////////////////////////////////
        // Instance attributes                                                //
        ////////////////////////////////////////////////////////////////////////

        /** Major version number. */
        final int major;

        /** Minor version number. */
        final int minor;

        /** Revision number. */
        final int revision;

        /** Patch update number. */
        final int patch;

        ////////////////////////////////////////////////////////////////////////
        // Constructors                                                       //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Constructs an instance of Java VM version number.
         * <p/>
         * @param major 
         */
        public JavaVersion(int major, int minor, int revision, int patch) {
            this.major = major;
            this.minor = minor;
            this.revision = revision;
            this.patch = patch;
        }

        ////////////////////////////////////////////////////////////////////////
        // Methods                                                            //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Compares this <code>JavaVersion</code> object against another one.
         * <p/>
         * @param version <code>JavaVersion</code> object to compare with
         *                <code>this</code> object.
         * @return Compare result:<ul>
         *         <li/>Value <code>1</code> if <code>this</code> value
         *         is greater than supplied <code>version</code> value.
         *         <li/>Value <code>-1</code> if <code>this</code> value
         *         is lesser than supplied <code>version</code> value.
         *         <li/>Value <code>0</code> if both <code>this</code> value
         *         and supplied <code>version</code> values are equal.
         *         </ul>
         */
        public int comapreTo(JavaVersion version) {
            return this.major > version.major ? 1 :
                    this.major < version.major ? -1 :
                    this.minor > version.minor ? 1 :
                    this.minor < version.minor ? -1 : 
                    this.revision > version.revision ? 1 :
                    this.revision < version.revision ? -1 :
                    this.patch > version.patch ? 1 :
                    this.patch < version.patch ? -1 : 0;
        }

        /**
         * Return <code>String</code> representation of Java VM version object.
         * <p/>
         * @return Java VM version string.
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(16);
            sb.append(major);
            sb.append('.');
            sb.append(minor);
            sb.append('.');
            sb.append(revision);
            sb.append('_');
            sb.append(patch);
            return sb.toString();
        }

        /**
         * Return {@link JavaSEPlatform} matching this Java SE version.
         * <p/>
         * @return {@link JavaSEPlatform} matching this Java SE version.
         */
        public JavaSEPlatform toPlatform() {
            StringBuilder sb = new StringBuilder(6);
            sb.append(major);
            sb.append('.');
            sb.append(minor);
            return JavaSEPlatform.toValue(sb.toString());
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    // TODO: This test should be rewritten to use probe class to retrieve
    //       system properties from JRE.
    /**
     * Java VM version detector.
     * <p/>
     * Executes java -version and tries to find output line containing<ul>
     * <li/><code>java version "MA.MI.RE_PA"</code>
     * </ul>
     * Where<ul>
     * <li/>MA is major version number,
     * <li/>MI is minor version number,
     * <li/>RE is revision number and
     * <li/>PA is patch update number,
     * </ul>
     * Label <code>java version</code> is parsed as non case sensitive.
     */
    public static JavaVersion javaVmVersion(File javaVm) {
        // Run Java VM: java -version.
        ProcessBuilder pb = new ProcessBuilder(
                javaVm.getAbsolutePath(), VM_VERSION_OPT);
        Process process;
        pb.redirectErrorStream(true);
        try {
            process = pb.start();
        // Handle I/O errors.
        } catch (IOException ioe) {
            Logger.log(Level.WARNING,
                    "Caught IOException while executing Java VM.", ioe);
            return null;
        // Handle security issues.
        } catch (SecurityException se) {
            Logger.log(Level.WARNING,
                    "Caught SecurityException while executing Java VM.", se);
            return null;
        }
        // Read and parse Java VM output to search for version string.
        BufferedReader in = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        int major = 0, minor = 0, revision = 0, patch = 0;
        String line;
        Pattern pattern = Pattern.compile(VM_VERSION_PATTERN);
        try {
            while ((line = in.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    int groupCount = matcher.groupCount();
                    if (groupCount >= VM_MIN_VERSION_TOKENS) {
                        // [0-9]+ REGEX pattern is validating numbers in tokens.
                        // NumberFormatException can't be thrown.
                        major = Integer.parseInt(matcher.group(1));
                        minor = Integer.parseInt(matcher.group(2));
                        revision = groupCount > 2 && matcher.group(3) != null
                                ? Integer.parseInt(matcher.group(3)) : 0;
                        patch = groupCount > 3 && matcher.group(4) != null
                                ? Integer.parseInt(matcher.group(4)) : 0;
                        break;
                    }
                }
            }
        // Handle I/O errors.
        } catch (IOException ioe) {
            Logger.log(Level.WARNING,
                    "Caught IOException while reading Java VM output.", ioe);
            return null;
        }
        return new JavaVersion(major, minor, revision, patch);
    }
    
    /**
     * Build Java VM executable full path from Java Home directory.
     * <p/>
     * @param javaHome Full path to Java Home directory.
     * @return Java VM executable full path.
     */
    public static String javaVmExecutableFullPath(String javaHome) {
        int javaHomeLen = javaHome.length();
        int execSuffixLen = OsUtils.EXEC_SUFFIX.length();
        boolean javaHomeEndsWithPathSep =
                javaHome.charAt(javaHomeLen - 1) ==
                File.separatorChar;
        boolean isExecSuffix = execSuffixLen > 0;
        // Count full size to avoid resizing.
        StringBuilder javaExecStr = new StringBuilder(
                javaHomeLen +
                (javaHomeEndsWithPathSep ? 0 : 1) +
                JAVA_BIN_DIR.length() + 1 + JAVA_VM_EXE.length() +
                (isExecSuffix ? execSuffixLen + 1 : 0));
        // Build string.
        javaExecStr.append(javaHome);
        if (!javaHomeEndsWithPathSep) {
            javaExecStr.append(File.separatorChar);
        }
        javaExecStr.append(JAVA_BIN_DIR);
        javaExecStr.append(File.separatorChar);
        javaExecStr.append(JAVA_VM_EXE);
        if (isExecSuffix) {
            javaExecStr.append(OsUtils.EXEC_SUFFIX);
        }
        return javaExecStr.toString();
    }

    /**
     * Build quoted Java VM system property name by prefixing property name
     * with <code>-D</code> as <code>-D"&lt;name&gt;"</code>.
     * <p/>
     * @param name Java VM system property name to be prefixed.
     */
    public static String systemPropertyName(String name) {
        StringBuilder sb = new StringBuilder(
                2 + VM_SYS_PROP_OPT.length() + name.length());
        return systemPropertyName(sb, name);
    }

    /**
     * Build quoted Java VM system property name by prefixing property name
     * with <code>-D</code> as <code>-D"&lt;name&gt;"</code> into
     * {@link StringBuilder} instance.
     * <p/>
     * @param sb   {@link StringBuilder} instance where to append Java VM
     *             system property.
     * @param name Java VM system property name to be prefixed.
     */
    public static String systemPropertyName(StringBuilder sb, String name) {
        sb.append(VM_SYS_PROP_OPT);
        sb.append(VM_SYS_PROP_QUOTE);
        sb.append(name);
        sb.append(VM_SYS_PROP_QUOTE);
        return sb.toString();
    }

    /**
     * Build quoted Java VM system property
     * <code>-D"&lt;name&gt;=&lt;value&gt;"</code>.
     * <p/>
     * @param name  Java VM system property name.
     * @param value Java VM system property value.
     */
    public static String systemProperty(String name, String value) {
        StringBuilder sb = new StringBuilder(2 + VM_SYS_PROP_OPT.length()
                + name.length() + VM_SYS_PROP_ASSIGN.length() + value.length());
        return systemProperty(sb, name, value);
    }

    /**
     * Append quoted Java VM system property
     * <code>-D"&lt;name&gt;=&lt;value&gt;"</code> into {@link StringBuilder}
     * instance.
     * <p/>
     * @param sb   {@link StringBuilder} instance where to append Java VM
     *             system property.
     * @param name  Java VM system property name.
     * @param value Java VM system property value.
     */
    public static String systemProperty(
            StringBuilder sb, String name, String value) {
        sb.append(VM_SYS_PROP_OPT);
        sb.append(VM_SYS_PROP_QUOTE);
        sb.append(name);
        sb.append(VM_SYS_PROP_ASSIGN);
        sb.append(value);
        sb.append(VM_SYS_PROP_QUOTE);
        return sb.toString();
    }

    /**
     * Get URL to access properties file in the same package as given class.
     * <p/>
     * @param c    Class to determine package.
     * @param file Properties file name (e.g. <code>Messages.properties</code>).
     * @return URL to access properties file.
     */
    public static URL getPropertiesURL(final Class c, final String file) {
        return c.getResource(file);
    }

}
