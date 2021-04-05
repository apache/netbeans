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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;
import org.netbeans.modules.payara.tooling.admin.CommandException;
import org.netbeans.modules.payara.tooling.data.PayaraContainer;
import org.netbeans.modules.payara.tooling.data.PayaraVersion;
import org.netbeans.modules.payara.tooling.logging.Logger;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Common utilities.
 * <p/>
 * @author Vince Kraemer, Tomas Kraus, Peter Benedikovic
 */
public class ServerUtils {

    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes                                                          //
    ////////////////////////////////////////////////////////////////////////////   

    /**
     * Filtering the set of directories with Payara server home subdirectory
     * shown to the user in file filter.
     */
    public static class PayaraFilter implements FileFilter {

        /**
         * Test whether or not the specified pathname is Payara server
         * subdirectory.
         * <p/>
         * @param path Pathname to be tested.
         * @return Returns <code>true</code> when given <code>File</code>
         *         should be included or <code>false</code> otherwise.
         */
        @Override
        public boolean accept(final File path) {
            if (path.isDirectory() && path.canRead()) {
                File commonUtilJar
                        = getCommonUtilJarInModules(path.getAbsolutePath());
                if (commonUtilJar.isFile() && commonUtilJar.canRead()) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        
    }

    /**
     * Filtering the set of Payara servers using file name pattern.
     */
    private static class VersionFilter implements FileFilter {

        /** Compiled file name pattern. */
        private final Pattern pattern;

        /**
         * Creates an instance of Payara servers file name pattern filter.
         * <p/>
         * @param namePattern Payara servers file name pattern.
         */
        public VersionFilter(final String namePattern) {
            pattern = Pattern.compile(namePattern);
        }

        @Override
        /**
         * Test whether or not the specified pathname is valid Payara server.
         * <p/>
         * @param path Pathname to be tested.
         * @return Returns <code>true</code> when given <code>File</code>
         *         should be included or <code>false</code> otherwise.
         */
        public boolean accept(final File file) {
            return pattern.matcher(file.getName()).matches();
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(ServerUtils.class);

    /** Payara Java VM system environment <code>AS_JAVA</code> variable name.
     *  This value should be equal to system environment <code>JAVA_HOME</code>
     *  value. */
    public static final String AS_JAVA_ENV = "AS_JAVA";

    /** Payara server Java VM root property name. */
    public static final String PF_JAVA_ROOT_PROPERTY
            = "com.sun.aas.javaRoot";

    /** Payara server home property name.
     *  <p/>
     *  It's value says it is server installation root but in reality it is just
     *  <code>payara</code> subdirectory under server installation root which
     *  we usually call server home. */
    public static final String PF_HOME_PROPERTY
            = "com.sun.aas.installRoot";

    /** Payara server domain root property name.
     *  </p>
     *  It's value says it is server instance root which is the same. */
    public static final String PF_DOMAIN_ROOT_PROPERTY
            = "com.sun.aas.instanceRoot";

    /** Payara server Derby root property name. */
    public static final String PF_DERBY_ROOT_PROPERTY
            = "com.sun.aas.derbyRoot";

    /** Payara server home subdirectory filter instance. */
    public static final PayaraFilter PF_HOME_DIR_FILTER
            = new PayaraFilter();

    /** Payara server domains subdirectory. */
    public static final String PF_DOMAINS_DIR_NAME = "domains";

    /** Payara server domains subdirectory. */
    public static final String PF_DOMAIN_CONFIG_DIR_NAME = "config";

    /** Payara server domains subdirectory. */
    public static final String PF_DOMAIN_CONFIG_FILE_NAME = "domain.xml";

    /** Payara server modules subdirectory. */
    public static final String PF_MODULES_DIR_NAME = "modules";

    /** Payara server Derby subdirectory. */
    public static final String PF_DERBY_DIR_NAME = "javadb";

    /** Payara server libraries subdirectory. */
    public static final String PF_LIB_DIR_NAME = "lib";

    /** Payara server embedded libraries subdirectory under libraries. */
    public static final String PF_EMBEDDED_DIR_NAME = "embedded";

    /** Payara server logs subdirectory. */
    public static String PF_LOG_DIR_NAME = "logs";

    /** Payara server log file. */
    public static String PF_LOG_FILE_NAME = "server.log";

    public static final String VERSION_MATCHER
            = "(?:-[0-9bSNAPHOT]+(?:\\.[0-9]+(?:_[0-9]+|)|).*|).jar";
    public static final String GF_JAR_MATCHER
            = "glassfish" + VERSION_MATCHER;

    /** Manifest attribute Bundle-Version containing Payara
     *  version <code>String</code>. */
    public static final String BUNDLE_VERSION = "Bundle-Version";

    /** Common utilities JAR file name. */
    public static final String PF_COMMON_UTIL_JAR = "common-util.jar";

    /** Jersey 2.x common JAR file name. */
    public static final String PF_JERSEY_2_COMMON_JAR = "jersey-common.jar";

    /** Jersey 1.x core JAR file name. */
    public static final String PF_JERSEY_1_CORE_JAR = "jersey-core.jar";

    /** Embedded static shell JAR file name. */
    public static final String PF_EMBEDDED_STATIC_SHELL_JAR
            = "glassfish-embedded-static-shell.jar";

    /** Verifier JAR file name. */
    public static final String PF_VERIFIER_JAR = "verifier.jar";
    
    /** JavaHelp JAR file name. */
    public static final String PF_JAVAHELP_JAR = "javahelp.jar";

    /** Payara Version class name (including package). */
    private static String VERSION_CLASS = "com.sun.appserv.server.util.Version";

    /** Payara VerifierMain class name (including package). */
    public static String VERIFIER_MAIN_CLASS
            = "com.sun.enterprise.tools.verifier.VerifierMain";

    /** Regex pattern to retrieve version string like 3.1.2.2 from
     *  full version string. */
    private static String FULL_VERSION_PATTERN = "[0-9]+(\\.[0-9]+){1,3}";

    /** Payara full version string getter method name. */
    private static String FULL_VERSION_METHOD = "getFullVersion";

    /** Payara Basic Authorization user and password separator. */
    private static String AUTH_BASIC_FIELD_SEPARATPR = ":";

    /** Payara server domain name command line argument. */
    public static String PF_DOMAIN_ARG = "--domain";

    /** Payara server domain directory command line argument. */
    public static String PF_DOMAIN_DIR_ARG = "--domaindir";

    /** Payara server service response while server is not yet ready.
     *  Copy-pasted from
     *  <code>com.sun.enterprise.v3.admin.AdminAdapter</code>. */
    public static final String PF_SERVICE_NOT_YET_READY_MSG
            = "V3 cannot process this command at this time, please wait";
    /** End of line sequence in Manifest. */
    public static final String MANIFEST_EOL = "%%%EOL%%%";

    /** REGEX expression used to split resources returned in
     *  <code>Manifest</code> object from HTTP response. */
    public static final String MANIFEST_RESOURCES_SEPARATOR = "[,;]";

    /** REGEX expression used to split components (applications) returned in
     *  <code>Manifest</code> object from HTTP response. */
    public static final String MANIFEST_COMPONENTS_SEPARATOR = ";";

    /** REGEX expression used to extract component name and containers from
     *  <code>Manifest</code> attribute. */
    private static final String MANIFEST_COMPONENT_FULL_REGEX
            = " *([^ ]+) +< *([^ ,]+) *((?:, *[^ ,]+ *)*)> *";

    /** REGEX expression used to extract additional containers from containers
     *  list of <code>Manifest</code> attribute. */
    private static final String MANIFEST_COMPONENT_COMP_REGEX = ", *([^ ,]+) *";

    /** REGEX pattern used to extract component name and containers from
     *  <code>Manifest</code> attribute. */
    private static final Pattern MANIFEST_COMPONENT_FULL_PATTERN
            = Pattern.compile(MANIFEST_COMPONENT_FULL_REGEX,
            Pattern.CASE_INSENSITIVE);

    /** REGEX pattern used to extract additional containers from containers
     *  list of <code>Manifest</code> attribute. */
    private static final Pattern MANIFEST_COMPONENT_COMP_PATTERN
            = Pattern.compile(MANIFEST_COMPONENT_COMP_REGEX,
            Pattern.CASE_INSENSITIVE);

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Search for <code>.jar</code> file matching given pattern
     * in <code>&lt;serverHome&gt;/modules</code> directory tree.
     * <p/>
     * @param serverHome Payara server home.
     * @param pattern    File name pattern.
     * @return <code>File</code> object containing full <code>.jar<code> file
     *         path or <code>null</code> if no file was found.
     */
    public static File getJarName(final String serverHome,
            final String pattern) {
        return getJarName(serverHome, pattern, PF_MODULES_DIR_NAME);
    }

    /**
     * Search for <code>.jar</code> file matching given pattern
     * in given directory tree.
     * <p/>
     * @param serverHome Payara server home.
     * @param pattern    File name pattern.
     * @param dir        Directory tree root to be searched for
     *                   <code>.jar<code> file.
     * @return <code>File</code> object containing full <code>.jar<code> file
     *         path or <code>null</code> if no file was found.
     */
    public static File getJarName(final String serverHome, final String pattern,
            final String dir) {
        File dirFile = new File(serverHome + File.separatorChar + dir);
        return getFileFromPattern(pattern, dirFile);
    }

    /**
     * Search for file matching given <code>pattern</code> in given
     * <code>dir</code>ectory tree.
     * <p/>
     * @param pattern Name pattern to search for.
     * @param dir     Directory tree root to be searched for pattern.
     * @return <code>File</code> object that matches given <code>pattern</code>
     *         or <code>null</code> otherwise.
     */
    public static File getFileFromPattern(String pattern, File dir) {
        assert pattern != null : "Search pattern should not be null";
        assert dir != null : "Search directory tree root should not be null";
        // TODO: Check if this is OK of File.pathSeparator should be used.
        int subindex = pattern.lastIndexOf("/");
        if (subindex != -1) {
            String subdir = pattern.substring(0, subindex);
            pattern = pattern.substring(subindex + 1);
            dir = new File(dir, subdir);
        }
        if (dir.canRead() && dir.isDirectory()) {
            // Express check first.
            String expressPattern = pattern.replace(VERSION_MATCHER,
                    ".jar");
            File candidate = new File(dir, expressPattern);
            if (!"".equals(expressPattern) && candidate.exists()) {
                return candidate;
            }
            // Longer check second.
            File[] candidates = dir.listFiles(new VersionFilter(pattern));
            if (candidates != null && candidates.length > 0) {
                // First one is returned.
                return candidates[0];
            }
        }
        return null;
    }

    /**
     * Append next path element to existing path in <code>StringBuilder</code>.
     * Path separator is added only when there is no one at the end of existing
     * path.
     * <p/>
     * @param rootPath Already existing path in <code>StringBuilder</code>.
     * @param next     Path to be appended at the end of already existing path.
     */
    public static void addPathElement(final StringBuilder rootPath,
            final String next) {
        int rootPathLength = rootPath.length();
        if (rootPathLength > 0 && rootPath.lastIndexOf(File.separator)
                + OsUtils.FILE_SEPARATOR_LENGTH != rootPathLength) {
            rootPath.append(File.separator);
        }
        rootPath.append(next);
    }

    /**
     * Build path to the <code>common-util.jar</code> file in Payara modules
     * directory.
     * <p/>
     * @param serverHome Payara server home directory.
     * @return Path to the <code>common-util.jar</code> file in Payara
     *         modules directory.
     */
    public static File getCommonUtilJarInModules(final String serverHome) {
        StringBuilder commonUtilJarPath = new StringBuilder(serverHome.length()
                + PF_MODULES_DIR_NAME.length() + PF_COMMON_UTIL_JAR.length()
                + 2 * OsUtils.FILE_SEPARATOR_LENGTH);
        commonUtilJarPath.append(serverHome);
        addPathElement(commonUtilJarPath, PF_MODULES_DIR_NAME);
        addPathElement(commonUtilJarPath, PF_COMMON_UTIL_JAR);
        return new File(commonUtilJarPath.toString());
    }

    /**
     * Build path to the supplied <code>.jar</code> file in Payara
     * modules directory.
     * <p/>
     * @param serverHome Payara server home directory. This argument
     *                   should not be <code>null</code>.
     * @param jarName    Supplied JAR file name. This argument should not
     *                   be <code>null</code>.
     * @return Path to the <code>.jar</code> file in Payara
     *         modules directory.
     */
    public static File getJarInModules(final String serverHome,
            final String jarName) {
        StringBuilder commonUtilJarPath = new StringBuilder(serverHome.length()
                + PF_MODULES_DIR_NAME.length() + jarName.length()
                + 2 * OsUtils.FILE_SEPARATOR_LENGTH);
        commonUtilJarPath.append(serverHome);
        addPathElement(commonUtilJarPath, PF_MODULES_DIR_NAME);
        addPathElement(commonUtilJarPath, jarName);
        return new File(commonUtilJarPath.toString());
    }

    /**
     * Build path to the <code>jersey-common.jar</code> or
     * <code>jersey-core.jar</code> file in Payara modules directory.
     * <p/>
     * Searches for Jersey 1.x or 2.x common (core) jersey JAR file name
     * in Payara modules directory and returns <code>File</code> that was
     * found or <code>null</code> when no such a file exists and is readable.
     * <p/>
     * @param serverHome Payara server home directory. This argument
     *                   should not be <code>null</code>.
     */
    public static File getJerseyCommonJarInModules(final String serverHome) {
        File jerseyCommon = ServerUtils.getJarInModules(
                serverHome, ServerUtils.PF_JERSEY_2_COMMON_JAR);
        if (jerseyCommon.isFile() && jerseyCommon.canRead()) {
            return jerseyCommon;
        }
        jerseyCommon = ServerUtils.getJarInModules(
                serverHome, ServerUtils.PF_JERSEY_1_CORE_JAR);
        if (jerseyCommon.isFile() && jerseyCommon.canRead()) {
            return jerseyCommon;
        } else {
            return null;
        }
    }

    /**
     * Build path to the supplied <code>verifier.jar</code> library in Payara
     * modules directory.
     * <p/>
     * @param serverHome Payara server home directory. This argument
     *                   should not be <code>null</code>.
     * @return Path to the <code>verifier.ja</code> file in Payara
     *         modules directory.
     */
    public static String getVerifierJar(final String serverHome) {
        assert serverHome != null
                : "Payara server home directory should not be null";
        boolean appendSeparator = serverHome.lastIndexOf(File.separator)
                + OsUtils.FILE_SEPARATOR_LENGTH != serverHome.length();
        StringBuilder sb = new StringBuilder(serverHome.length()
                + (appendSeparator ? 2 * OsUtils.FILE_SEPARATOR_LENGTH
                : 1 * OsUtils.FILE_SEPARATOR_LENGTH)
                + PF_MODULES_DIR_NAME.length() + PF_VERIFIER_JAR.length());
        sb.append(serverHome);
        if (appendSeparator) {
            sb.append(File.separator);
        }
        sb.append(PF_MODULES_DIR_NAME);
        sb.append(File.separator);
        sb.append(PF_VERIFIER_JAR);
        return sb.toString();
    }

    /**
     * Build path to the supplied <code>javahelp.jar</code> library in Payara
     * modules directory.
     * <p/>
     * @param serverHome Payara server home directory. This argument
     *                   should not be <code>null</code>.
     * @return Path to the <code>javahelp.jar</code> file in Payara
     *         libraries directory.
     */
    public static String getJavaHelpJar(final String serverHome) {
        assert serverHome != null
                : "Payara server home directory should not be null";
        boolean appendSeparator = serverHome.lastIndexOf(File.separator)
                + OsUtils.FILE_SEPARATOR_LENGTH != serverHome.length();
        StringBuilder sb = new StringBuilder(serverHome.length()
                + (appendSeparator ? 2 * OsUtils.FILE_SEPARATOR_LENGTH
                : 1 * OsUtils.FILE_SEPARATOR_LENGTH)
                + PF_LIB_DIR_NAME.length() + PF_JAVAHELP_JAR.length());
        sb.append(serverHome);
        if (appendSeparator) {
            sb.append(File.separator);
        }
        sb.append(PF_LIB_DIR_NAME);
        sb.append(File.separator);
        sb.append(PF_JAVAHELP_JAR);
        return sb.toString();
    }

    /**
     * Build path to the <code>glassfish-embedded-static-shell.jar</code>
     * library in embedded libraries directory.
     * <p/>
     * @param serverHome Payara server home directory. This argument
     *                   should not be <code>null</code>.
     * @return Path to the <code>glassfish-embedded-static-shell.jar</code> 
     *         file in Payara embedded libraries directory.
     */
    public static String getEmbeddedStaticShellJar(final String serverHome) {
        assert serverHome != null
                : "Payara server home directory should not be null";
        boolean appendSeparator = serverHome.lastIndexOf(File.separator)
                + OsUtils.FILE_SEPARATOR_LENGTH != serverHome.length();
        StringBuilder sb = new StringBuilder(serverHome.length()
                + (appendSeparator ? 3 * OsUtils.FILE_SEPARATOR_LENGTH
                : 2 * OsUtils.FILE_SEPARATOR_LENGTH)
                + PF_LIB_DIR_NAME.length() + PF_EMBEDDED_DIR_NAME.length()
                + PF_EMBEDDED_STATIC_SHELL_JAR.length());
        sb.append(serverHome);
        if (appendSeparator) {
            sb.append(File.separator);
        }
        sb.append(PF_LIB_DIR_NAME);
        sb.append(File.separator);
        sb.append(PF_EMBEDDED_DIR_NAME);
        sb.append(File.separator);
        sb.append(PF_EMBEDDED_STATIC_SHELL_JAR);
        return sb.toString();
    }

    /**
     * Retrieve Jersey version string from Payara modules.
     * <p/>
     * @param serverHome Payara server home directory. This argument
     *                   should not be <code>null</code>.
     * @return Jersey version string from Payara modules.
     */
    public static String getJerseyVersion(final String serverHome) {
        File jerseyFile = ServerUtils.getJerseyCommonJarInModules(serverHome);
        if (jerseyFile != null) {
            Jar jerseyJar = new Jar(jerseyFile);
            String version = jerseyJar.getBundleVersion();
            jerseyJar.close();
            return version;
        } else {
            return null;
        }
    }

    /**
     * Retrieve version numbers substring from full version string.
     * <p/>
     * @param fullVersionString Payara server full version string, e.g.
     *                          <code>Payara Server Open Source Edition
     *                          3.1.2.2 (build 5)</code>
     * @return Version numbers substring, e.g. <code>3.1.2.2</code>
     */
    public static String getVersionString(final String fullVersionString) {
        if (fullVersionString != null) {
            Pattern p = Pattern.compile(FULL_VERSION_PATTERN);
            Matcher m = p.matcher(fullVersionString);
            if (m.find()) {
                return fullVersionString.substring(m.start(), m.end());
            }
        }
        return null;
    }

    /**
     * Retrieve Payara version from local installation using file access.
     * <p/>
     * Payara version is read from modules <code>common-util.jar</code>
     * archive and <code>com.sun.appserv.server.util.Version</code> class.
     * It's not public Payara API so there is no guaranty for this to work
     * forever. However Payara development team promised to keep this
     * API working the same way in Payara 3 and 4.
     * <p/>
     * @param serverHome Payara server home directory.
     * @return Payara server version.
     */
    public static PayaraVersion getServerVersion(final String serverHome) {
        PayaraVersion version = null;
        File commonUtilJar = getCommonUtilJarInModules(serverHome);
        if (commonUtilJar.canRead()) {
            try {
                ClassLoader cl = new URLClassLoader(new URL[] {commonUtilJar.
                            toURI().toURL()});
                Class c = cl.loadClass(VERSION_CLASS);
                // Try to get version from com.sun.appserv.server.util.Version.
                try {
                    Method mGetFullVersion = c.getMethod(FULL_VERSION_METHOD);
                    System.getProperties().put(PF_HOME_PROPERTY, serverHome);
                    String fullVersionString
                            = (String)mGetFullVersion.invoke(c);
                    System.getProperties().remove(PF_HOME_PROPERTY);
                    String versionString
                            = getVersionString(fullVersionString);
                    if (versionString != null) {
                        version = PayaraVersion.toValue(versionString);
                    }
                } catch (IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException
                        | SecurityException | NoClassDefFoundError ex) {
                    Logger.log(Level.WARNING, "Cannot retrieve Payara version: "
                            + commonUtilJar.getAbsolutePath() + ": ", ex);
                }
                // Use Manifest Bundle-Version as fallback option.
                if (version == null) {
                    try {
                        JarFile jar = new JarFile(commonUtilJar);
                        Manifest manifest = jar.getManifest();
                        String versionString = getVersionString(manifest
                                .getMainAttributes().getValue(BUNDLE_VERSION));
                        if (versionString != null) {
                            version = PayaraVersion.toValue(versionString);
                        }
                    } catch (IOException ioe) {
                        Logger.log(Level.WARNING, "Cannot retrieve Payara version: "
                            + commonUtilJar.getAbsolutePath() + ": ", ioe);
                    }
                }
            } catch (MalformedURLException | ClassNotFoundException ex) {
                Logger.log(Level.WARNING, "Cannot retrieve Payara version: "
                        + commonUtilJar.getAbsolutePath() + ": ", ex);
            }
        } else {
            Logger.log(Level.WARNING, "Cannot retrieve Payara version: "
                        + commonUtilJar.getAbsolutePath() + " is not readable:"
                        + " Exists: " + commonUtilJar.exists()
                        + " Can read: " + commonUtilJar.canRead(), (Throwable) null);
        }
        return version;
    }

    /**
     * Decode <code>Manifest</code> string to remove EOL sequences.
     * <p/>
     * @param str String to be decoded.
     */
    public static String manifestDecode(final String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll(MANIFEST_EOL, "\n");
    }

    /**
     * Build HTTP Basic authorization base64 encoded credentials argument
     * containing user name and password.
     * <p/>
     * @param user Username to be stored into encoded argument.
     * @param password Password to be stored into encoded argument.
     */
    public static String basicAuthCredentials(final String user,
            final String password) {
        StringBuilder sb = new StringBuilder(user.length()
                + AUTH_BASIC_FIELD_SEPARATPR.length() + password.length());
        sb.append(user);
        sb.append(AUTH_BASIC_FIELD_SEPARATPR);
        sb.append(password);
        return DatatypeConverter.printBase64Binary(sb.toString().getBytes());
    }

    /**
     * Tests if the server listener port is occupied.
     * <p/>
     * @param server Payara server entity
     * @return Value of <code>true</code> when server listener port
     *         is occupied or <code>false</code> otherwise.
     */
    public static boolean isHttpPortListening(final PayaraServer server) {
        return server.isRemote()
                ? NetUtils.isPortListeningRemote(
                server.getHost(), server.getPort())
                : NetUtils.isPortListeningLocal(
                server.getHost(), server.getPort());
    }
    
    /**
     * Tests if the server listener port is occupied.
     * <p/>
     * @param server Payara server entity
     * @param timeout Network timeout [ms].
     * @return Value of <code>true</code> when server listener port
     *         is occupied or <code>false</code> otherwise.
     */
    public static boolean isHttpPortListening(final PayaraServer server,
            final int timeout) {
        return server.isRemote()
                ? NetUtils.isPortListeningRemote(
                server.getHost(), server.getPort(), timeout)
                : NetUtils.isPortListeningLocal(
                server.getHost(), server.getPort());
    }

    /**
     * Tests if the server administrator's port is occupied.
     * <p/>
     * @param server Payara server entity.
     * @return Value of <code>true</code> when server administrator port
     *         is occupied or <code>false</code> otherwise.
     */
    public static boolean isAdminPortListening(final PayaraServer server) {
        return server.isRemote()
                ? NetUtils.isPortListeningRemote(
                server.getHost(), server.getAdminPort())
                : NetUtils.isPortListeningLocal(
                server.getHost(), server.getAdminPort());
    }
    
    /**
     * Tests if the server administrator's port is occupied.
     * <p/>
     * @param server  Payara server entity.
     * @param timeout Network timeout [ms].
     * @return Value of <code>true</code> when server administrator port
     *         is occupied or <code>false</code> otherwise.
     */
    public static boolean isAdminPortListening(final PayaraServer server,
            final int timeout) {
        return server.isRemote()
                ? NetUtils.isPortListeningRemote(
                server.getHost(), server.getAdminPort(), timeout)
                : NetUtils.isPortListeningLocal(
                server.getHost(), server.getAdminPort());
    }

    /**
     * Builds command line argument containing argument identifier, space
     * and argument value, e.g. <code>--name value</code>.
     * <p/>
     * @param name      Command line argument name including dashes at
     *                  the beginning.
     * @param value     Value to be appended prefixed with single space.
     * @return Command line argument concatenated together.
     */
    public static String cmdLineArgument(final String name,
            final String value) {
        StringBuilder sb = new StringBuilder(name.length() + " ".length()
                + value.length());
        sb.append(name);
        sb.append(" ");
        sb.append(value);
        return sb.toString();
    }

    /**
     * Parse server component (application) record and add it into
     * <code>Map</code> containing container to components <code>List</code>
     * mapping.
     * <p/>
     * Component records: <code>&lt;name&gt; '&lt;' &lt;container&gt;
     * [',' &lt;container&gt;] '&gt;'</code>
     * </p>
     * @param map Map where new component is stored under it's container key.
     * @param component Component record retrieved from server.
     * @throws <code>NullPointerException</code> when provided map argument
     *         is <code>null</code>.
     */
    public static void addComponentToMap(final Map<String, List<String>> map,
            final String component) {
        Logger.log(Level.FINER, "Processing component \"{0}\"",
                new Object[]{component});
        Matcher fullMatecher = ServerUtils.MANIFEST_COMPONENT_FULL_PATTERN
                .matcher(component);
        if (fullMatecher.matches()) {
            String componentName = fullMatecher.group(1);
            PayaraContainer container = PayaraContainer.toValue(
                    fullMatecher.group(2));
            String moreContainers = fullMatecher.group(3);
            if (moreContainers != null && moreContainers.length() > 0) {
                Matcher compMatcher = ServerUtils
                        .MANIFEST_COMPONENT_COMP_PATTERN.matcher(
                        moreContainers);
                while (compMatcher.find()) {
                    PayaraContainer nextContainer = PayaraContainer
                            .toValue(compMatcher.group(1));
                    if (nextContainer != null && container != null
                            && nextContainer.ordinal() < container.ordinal()) {
                        container = nextContainer;
                    } else if (nextContainer == null) {
                        Logger.log(Level.WARNING,
                                "Error processing component \"{0}\"",
                                new Object[]{component});
                    }
                }
            }
            String containerName = container != null
                    ? container.toString() : "null";
            List<String> componentList = map.get(containerName);
            if (componentList == null) {
                componentList = new ArrayList<>();
                map.put(containerName, componentList);
            }
            componentList.add(componentName);
        } else {
            throw new CommandException(
                    CommandException.MANIFEST_INVALID_COMPONENT_ITEM);
        }
    }

    /**
     * Build Payara server log file sub path under domains root directory.
     * <p/>
     * @return Payara server log file path under domains root directory.
     */
    public static String serverLogFileRelativePath() {
        StringBuilder sb = new StringBuilder(PF_LOG_DIR_NAME.length()
                + File.separator.length() + PF_LOG_FILE_NAME.length());
        sb.append(PF_LOG_DIR_NAME);
        sb.append(File.separator);
        sb.append(PF_LOG_FILE_NAME);
        return sb.toString();
    }

    /**
     * Get Payara server domain root full path.
     * <p/>
     * @param server Payara server entity
     * @return Payara server domain root full path or <code>null</code>
     *         when server domains root folder or domain name is not set.
     */
    public static String getDomainPath(final PayaraServer server) {
        String domainName = server.getDomainName();
        String domainsFolder = server.getDomainsFolder();
        boolean appendSeparator = domainsFolder.lastIndexOf(File.separator)
                + OsUtils.FILE_SEPARATOR_LENGTH != domainsFolder.length();
        StringBuilder sb = new StringBuilder(server.getDomainsFolder().length()
                + (appendSeparator ? OsUtils.FILE_SEPARATOR_LENGTH : 0)
                + domainName.length());
        sb.append(domainsFolder);
        if (appendSeparator) {
            sb.append(File.separator);
        }
        sb.append(domainName);
        return sb.toString();
    }

    /**
     * Get Payara server domain configuration directory full path from
     * domain root.
     * <p/>
     * @param domainDir Payara server domain root full path.
     * @return Payara server domain configuration directory full path
     */
    public static String getDomainConfigPath(final String domainDir) {
        if (domainDir == null) {
            return PF_DOMAIN_CONFIG_DIR_NAME;
        }
        boolean appendSeparator = domainDir.lastIndexOf(File.separator)
                + OsUtils.FILE_SEPARATOR_LENGTH != domainDir.length();
        StringBuilder sb = new StringBuilder(domainDir.length()
                + (appendSeparator ? OsUtils.FILE_SEPARATOR_LENGTH : 0)
                + PF_DOMAIN_CONFIG_DIR_NAME.length());
        sb.append(domainDir);
        if (appendSeparator) {
            sb.append(File.separator);
        }
        sb.append(PF_DOMAIN_CONFIG_DIR_NAME);
        return sb.toString();
    }

    /**
     * Get Payara server domain configuration file (domain.xml) full path
     * from domains root and domain name.
     * <p/>
     * @param domainsRoot Payara server domains root full path.
     * @param domainName  Payara server domain name.
     * @return Payara server domain configuration file full path
     */
    public static String getDomainConfigFile(final String domainsRoot,
            final String domainName) {
        final String METHOD = "getDomainConfigFile";
        if (domainsRoot == null) {
            throw new IllegalArgumentException(
                    LOGGER.excMsg(METHOD, "domainsRootNull"));
        }
        if (domainName == null) {
            throw new IllegalArgumentException(
                    LOGGER.excMsg(METHOD, "domainNameNull"));
        }
        StringBuilder sb = new StringBuilder(domainsRoot.length()
                + domainName.length() + PF_DOMAIN_CONFIG_DIR_NAME.length()
                + PF_DOMAIN_CONFIG_FILE_NAME.length()
                + (3 * OsUtils.FILE_SEPARATOR_LENGTH));
        sb.append(domainsRoot).append(File.separator);
        sb.append(domainName).append(File.separator);
        sb.append(PF_DOMAIN_CONFIG_DIR_NAME).append(File.separator);
        sb.append(PF_DOMAIN_CONFIG_FILE_NAME);
        return sb.toString();
    }

    /**
     * Get Payara server log {@link File} object.
     * <p/>
     * @param server Payara server entity.
     * @return Payara server log {@link File} object.
     */
    public static File getServerLogFile(final PayaraServer server) {
        return new File(getDomainPath(server),
                serverLogFileRelativePath());
    }

    /**
     * Get Payara server derby root full path.
     * <p/>
     * @param server Payara server entity
     * @return Payara server derby root full path or <code>null</code>
     *         when server server installation directory is not set.
     */
    public static String getDerbyRoot(final PayaraServer server) {
        String serverRoot = server.getServerRoot();
        if (serverRoot == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(serverRoot.length()
                + File.separator.length() + PF_DERBY_DIR_NAME.length());
        sb.append(serverRoot);
        sb.append(File.separator);
        sb.append(PF_DERBY_DIR_NAME);
        return sb.toString();
    }

    /**
     * Get Payara server Java VM root property.
     * <p/>
     * @param javaHome Java VM root (home) directory to be set as property
     *                 value.
     * @return Payara server Java VM root property to be passed to server
     *         startup command.
     */
    public static String javaRootProperty(final String javaHome) {
        return JavaUtils.systemProperty(PF_JAVA_ROOT_PROPERTY, javaHome);
    }

    /**
     * Check if given message is the one returned by Payara server service
     * response while server is not yet ready.
     * <p/>
     * @param msg Message to be checked.
     * @return Returns <code>true</code> if given message is server service
     *         response while server is not yet ready or <code>false</code>
     *         otherwise.
     */
    public static boolean notYetReadyMsg(final String msg) {
        if (msg == null) {
            return false;
        }
        return msg.startsWith(PF_SERVICE_NOT_YET_READY_MSG);
    }

}
