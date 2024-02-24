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
package org.netbeans.modules.javaee.wildfly.ide.ui;

import java.io.File;

import static java.io.File.separatorChar;

import java.beans.PropertyVetoException;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.JarFileSystem;

/**
 *
 * @author Ivan Sidorkin
 */
public class WildflyPluginUtils {

    public static final Version JBOSS_7_0_0 = new Version("7.0.0", true); // NOI18N

    public static final Version EAP_6_1_0 = new Version("7.2.0", false); // NOI18N

    public static final Version EAP_6_2_0 = new Version("7.3.0", false); // NOI18N

    public static final Version EAP_6_3_0 = new Version("7.4.0", false); // NOI18N

    public static final Version EAP_6_4_0 = new Version("7.5.0", false); // NOI18N

    public static final Version EAP_7_0 = new Version("7.0.0", false); // NOI18N

    public static final Version WILDFLY_8_0_0 = new Version("8.0.0", true); // NOI18N

    public static final Version WILDFLY_8_1_0 = new Version("8.1.0", true); // NOI18N

    public static final Version WILDFLY_8_2_0 = new Version("8.2.0", true); // NOI18N

    public static final Version WILDFLY_9_0_0 = new Version("9.0.0", true); // NOI18N

    public static final Version WILDFLY_10_0_0 = new Version("10.0.0", true); // NOI18N

    public static final Version WILDFLY_11_0_0 = new Version("11.0.0", true); // NOI18N

    public static final Version WILDFLY_12_0_0 = new Version("12.0.0", true); // NOI18N

    public static final Version WILDFLY_13_0_0 = new Version("13.0.0", true); // NOI18N

    public static final Version WILDFLY_14_0_0 = new Version("14.0.0", true); // NOI18N

    public static final Version WILDFLY_15_0_0 = new Version("15.0.0", true); // NOI18N

    public static final Version WILDFLY_16_0_0 = new Version("16.0.0", true); // NOI18N

    public static final Version WILDFLY_17_0_0 = new Version("17.0.0", true); // NOI18N

    public static final Version WILDFLY_18_0_0 = new Version("18.0.0", true); // NOI18N

    public static final Version WILDFLY_19_0_0 = new Version("19.0.0", true); // NOI18N

    public static final Version WILDFLY_20_0_0 = new Version("20.0.0", true); // NOI18N

    public static final Version WILDFLY_21_0_0 = new Version("21.0.0", true); // NOI18N

    public static final Version WILDFLY_22_0_0 = new Version("22.0.0", true); // NOI18N

    public static final Version WILDFLY_23_0_0 = new Version("23.0.0", true); // NOI18N

    public static final Version WILDFLY_24_0_0 = new Version("24.0.0", true); // NOI18N

    public static final Version WILDFLY_25_0_0 = new Version("25.0.0", true); // NOI18N

    public static final Version WILDFLY_26_0_0 = new Version("26.0.0", true); // NOI18N

    public static final Version WILDFLY_27_0_0 = new Version("27.0.0", true); // NOI18N
    
    public static final Version WILDFLY_28_0_0 = new Version("28.0.0", true); // NOI18N
    
    public static final Version WILDFLY_29_0_0 = new Version("29.0.0", true); // NOI18N
    
    public static final Version WILDFLY_30_0_0 = new Version("30.0.0", true); // NOI18N

    public static final Version WILDFLY_31_0_0 = new Version("31.0.0", true); // NOI18N

    private static final Logger LOGGER = Logger.getLogger(WildflyPluginUtils.class.getName());

    public static final String LIB = "lib" + separatorChar;

    public static final String MODULES_BASE = "modules" + separatorChar + "system"
            + separatorChar + "layers" + separatorChar + "base" + separatorChar;

    public static final String CLIENT = "client" + separatorChar;

    public static final String COMMON = "common" + separatorChar;

    //--------------- checking for possible domain directory -------------
    private static List<String> domainRequirements;

    private static synchronized List<String> getDomainRequirements8x() {
        if (domainRequirements == null) {
            domainRequirements = new ArrayList<String>(11);
            Collections.addAll(domainRequirements,
                    "configuration"// NOI18N
            );
        }
        return domainRequirements;
    }

    //--------------- checking for possible server directory -------------
    private static List<String> serverRequirements7x;

    private static synchronized List<String> getServerRequirements8x() {
        if (serverRequirements7x == null) {
            serverRequirements7x = new ArrayList<String>(6);
            Collections.addAll(serverRequirements7x,
                    "bin", // NOI18N
                    "modules", // NOI18N
                    "jboss-modules.jar"); // NOI18N
        }
        return serverRequirements7x;
    }

    @NonNull
    public static String getModulesBase(String serverRoot) {
        return MODULES_BASE;
    }

    public static final String getDefaultConfigurationFile(String installDir) {
        return installDir + File.separatorChar + "standalone"
                + File.separatorChar + "configuration"
                + File.separatorChar + "standalone-full.xml";
    }

    //------------  getting exists servers---------------------------
    /**
     * returns Hashmap key = server name value = server folder full path
     */
    public static Map getRegisteredDomains(String serverLocation) {
        Map result = new HashMap();
        File serverDirectory = new File(serverLocation);

        if (isGoodJBServerLocation(serverDirectory)) {
            String[] files = new String[]{"standalone"};
            File file = serverDirectory;
            if (files != null) {
                for (String file1 : files) {
                    String path = file.getAbsolutePath() + separatorChar + file1;
                    if (isGoodJBInstanceLocation(serverDirectory, new File(path))) {
                        result.put(file1, path);
                    }
                }
            }
        }
        return result;
    }

    private static boolean isGoodJBInstanceLocation(File candidate, List<String> requirements) {
        return null != candidate && candidate.exists() && candidate.canRead()
                && candidate.isDirectory()
                && hasRequiredChildren(candidate, requirements);
    }

    private static boolean isGoodJBInstanceLocation8x(File serverDir, File candidate) {
        return isGoodJBInstanceLocation(candidate, getDomainRequirements8x());
    }

    public static boolean isGoodJBInstanceLocation(File serverDir, File candidate) {
       return WildflyPluginUtils.isGoodJBInstanceLocation8x(serverDir, candidate);
    }

    private static boolean isGoodJBServerLocation(File candidate, List<String> requirements) {
        if (null == candidate
                || !candidate.exists()
                || !candidate.canRead()
                || !candidate.isDirectory()
                || !hasRequiredChildren(candidate, requirements)) {
            return false;
        }
        return true;
    }

    private static boolean isGoodJBServerLocation8x(File candidate) {
        return isGoodJBServerLocation(candidate, getServerRequirements8x());
    }

    public static boolean isGoodJBServerLocation(File candidate) {
        return WildflyPluginUtils.isGoodJBServerLocation8x(candidate);
    }

    /**
     * Checks whether the given candidate has all required childrens. Children
     * can be both files and directories. Method does not distinguish between
     * them.
     *
     * @return true if the candidate has all files/directories named in
     * requiredChildren, false otherwise
     */
    private static boolean hasRequiredChildren(File candidate, List<String> requiredChildren) {
        if (null == candidate || null == candidate.list()) {
            return false;
        }
        if (null == requiredChildren) {
            return true;
        }

        for (String next : requiredChildren) {
            File test = new File(candidate.getPath() + separatorChar + next);
            if (!test.exists()) {
                return false;
            }
        }
        return true;
    }

    public static String getDeployDir(String domainDir) {
        return domainDir + separatorChar + "deployments"; //NOI18N
    }

    public static String getHTTPConnectorPort(String configFile) {
        String defaultPort = "8080"; // NOI18N
        return defaultPort;
    }

    public static String getManagementConnectorPort(String configFile) {
        String defaultPort = "9990"; // NOI18N
        return defaultPort;
    }

    /**
     * Return true if the specified port is free, false otherwise.
     */
    public static boolean isPortFree(int port) {
        ServerSocket soc = null;
        try {
            soc = new ServerSocket(port);
        } catch (IOException ioe) {
            return false;
        } finally {
            if (soc != null) {
                try {
                    soc.close();
                } catch (IOException ex) {
                } // noop
            }
        }

        return true;
    }

    /**
     * Return the version of the server located at the given path. If the server
     * version can't be determined returns <code>null</code>.
     *
     * @param serverPath path to the server directory
     * @return specification version of the server
     */
    @CheckForNull
    public static Version getServerVersion(File serverPath) {
        assert serverPath != null : "Can't determine version with null server path"; // NOI18N

        Version version = getProductVersion(serverPath);
        if (version == null) {
            File serverDir = new File(serverPath, getModulesBase(serverPath.getAbsolutePath()) + "org/jboss/as/version/main");
            File[] files = serverDir.listFiles(new VersionJarFileFilter());
            if (files != null) {
                for (File jarFile : files) {
                    if (jarFile.getName().startsWith("jboss-as-version")) {
                        version = getVersion(jarFile);
                        if (version != null) {
                            break;
                        }
                    }
                }
            }
        }
        if(version == null) {
            return WILDFLY_8_0_0;
        }
        return version;
    }

    /**
     * Return the version of the server located at the given path. If the server
     * version can't be determined returns <code>null</code>.
     *
     * @param serverPath path to the server directory
     * @return specification version of the server using product.conf.
     */
    @CheckForNull
    public static Version getProductVersion(File serverPath) {
        assert serverPath != null : "Can't determine version with null server path"; // NOI18N
        String productConf = getProductConf(serverPath);
        if(productConf != null) {
            try (FileReader reader = new FileReader(productConf)) {
                Properties props = new Properties();
                props.load(reader);
                String slot = props.getProperty("slot");
                if (slot != null) {
                    Attributes manifestAttributes = findManifestAttributes(serverPath, slot);
                    String productName = manifestAttributes.getValue("JBoss-Product-Release-Name");
                    if(productName == null || productName.isEmpty()) {
                        productName = manifestAttributes.getValue("JBoss-Project-Release-Name");
                    }
                    boolean wildfly =  productName == null || !productName.toLowerCase().contains("eap");
                    return new Version(manifestAttributes.getValue("JBoss-Product-Release-Version"), wildfly);
                }
            } catch (Exception e) {
                // Don't care
            }
        }
        return null;
    }

    private static String getProductConf(File serverPath) {
        final String defaultVal = serverPath.getAbsolutePath() + File.separatorChar + "bin" + File.separatorChar + "product.conf";
        String env = System.getenv("JBOSS_PRODUCT_CONF");
        if (env == null) {
            env = defaultVal;
        }
        return env;
    }

    /**
     * Check the product name of the server located at the given path. If the server
     * version can't be determined returns <code>null</code>.
     *
     * @param serverPath path to the server directory
     * @return specification version of the server using product.conf.
     */
    @CheckForNull
    public static boolean isWildFly(File serverPath) {
        assert serverPath != null : "Can't determine version with null server path"; // NOI18N
        String productConf = getProductConf(serverPath);
        if(productConf != null) {
            try (FileReader reader = new FileReader(productConf)) {
                Properties props = new Properties();
                props.load(reader);
                String slot = props.getProperty("slot");
                if (slot != null) {
                    Attributes manifestAttributes = findManifestAttributes(serverPath, slot);
                    String productName = manifestAttributes.getValue("JBoss-Product-Release-Name");
                    if(productName == null || productName.isEmpty()) {
                        productName = manifestAttributes.getValue("JBoss-Project-Release-Name");
                    }
                    return productName == null || !productName.toLowerCase().contains("eap");
                }
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Can't determine version", e); // NOI18N
            }
        }
        return true;
    }

    /**
     * Check the product name of the server located at the given path. If the server
     * version can't be determined returns <code>null</code>.
     *
     * @param serverPath path to the server directory
     * @return specification version of the server using product.conf.
     */
    @CheckForNull
    public static boolean isWildFlyServlet(File serverPath) {
        assert serverPath != null : "Can't determine version with null server path"; // NOI18N
        String productConf = getProductConf(serverPath);
        if(productConf != null) {
            File productConfFile = new File(productConf);
            if(productConfFile.exists() && productConfFile.canRead()) {
                try (FileReader reader = new FileReader(productConf)) {
                    Properties props = new Properties();
                    props.load(reader);
                    String slot = props.getProperty("slot");
                    if (slot != null) {
                        Attributes manifestAttributes = findManifestAttributes(serverPath, slot);
                        String productName = manifestAttributes.getValue("JBoss-Product-Release-Name");
                        if(productName == null || productName.isEmpty()) {
                            productName = manifestAttributes.getValue("JBoss-Project-Release-Name");
                        }
                        return productName != null && (productName.contains("WildFly Web Lite") || productName.contains("WildFly Servlet"));
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.INFO, "Can't determine version", e); // NOI18N
                }
            }
        }
        return false;
    }
    
    /**
     * Check if the jakarta directory exists in the module base. If the
     * directory exists then WildFly support Jakarta EE.
     *
     * @param serverPath path to the server directory
     * @return true if the jakarta directory exists, {@code false} otherwise
     */
    @CheckForNull
    public static boolean isWildFlyJakartaEE(File serverPath) {
        assert serverPath != null : "Can't determine jakarta directory with null server path"; // NOI18N

        File jakartaDir = new File(serverPath, getModulesBase(serverPath.getAbsolutePath()) + "jakarta");
        return jakartaDir.exists();
    }

    private static Attributes findManifestAttributes(File serverPath, String slot) {
        File productDir = new File(serverPath, getModulesBase(serverPath.getAbsolutePath()) + "org.jboss.as.product".replace('.', separatorChar) + separatorChar + slot);
        File[] files = productDir.listFiles();
        for (File file : files) {
            try {
                if (file.getName().startsWith("wildfly-feature-pack-product-conf") && file.getName().endsWith(".jar")) {
                    JarFileSystem featurePackProductConfJar = new JarFileSystem(file);
                    return featurePackProductConfJar.getManifest().getMainAttributes();
                }
            } catch (Exception ignore) {
            }
        }
        File manifestFile = new File(productDir, "dir" + separatorChar + "META-INF" + separatorChar + "MANIFEST.MF");
        if (manifestFile.exists()) {
            try (InputStream stream = new FileInputStream(manifestFile)) {
                Manifest manifest = new Manifest(stream);
                return manifest.getMainAttributes();
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    private static class VersionJarFileFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".jar") && (name.startsWith("jboss-as-version") || name.startsWith("wildfly-version"));
        }
    }

    private static Version getVersion(File systemJarFile) {
        if (!systemJarFile.exists()) {
            return null;
        }

        try {
            JarFileSystem systemJar = new JarFileSystem();
            systemJar.setJarFile(systemJarFile);
            Attributes attributes = systemJar.getManifest().getMainAttributes();
            String version = attributes.getValue("Specification-Version"); // NOI18N
            if (version != null) {
                return new Version(version, true);
            }
            return null;
        } catch (IOException | PropertyVetoException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return null;
        }
    }

    /**
     * Class representing the JBoss version.
     * <p>
     * <i>Immutable</i>
     *
     * @author Petr Hejl
     */
    public static final class Version implements Comparable<Version> {

        private String majorNumber = "0";

        private String minorNumber = "0";

        private String microNumber = "0";

        private String update = "";

        private boolean wildfly = true;

        /**
         * Constructs the version from the spec version string. Expected format
         * is <code>MAJOR_NUMBER[.MINOR_NUMBER[.MICRO_NUMBER[.UPDATE]]]</code>.
         *
         * @param version spec version string with the following format:
         * <code>MAJOR_NUMBER[.MINOR_NUMBER[.MICRO_NUMBER[.UPDATE]]]</code>
         */
        public Version(String version, boolean wildfly) {
            assert version != null : "Version can't be null"; // NOI18N
            this.wildfly = wildfly;
            String[] tokens = version.split("\\.");

            if (tokens.length >= 4) {
                update = tokens[3];
            }
            if (tokens.length >= 3) {
                microNumber = tokens[2];
            }
            if (tokens.length >= 2) {
                minorNumber = tokens[1];
            }
            majorNumber = tokens[0];
        }

        /**
         * Returns the major number.
         *
         * @return the major number. Never returns <code>null</code>.
         */
        public String getMajorNumber() {
            return majorNumber;
        }

        /**
         * Returns the minor number.
         *
         * @return the minor number. Never returns <code>null</code>.
         */
        public String getMinorNumber() {
            return minorNumber;
        }

        /**
         * Returns the micro number.
         *
         * @return the micro number. Never returns <code>null</code>.
         */
        public String getMicroNumber() {
            return microNumber;
        }

        /**
         * Returns the update.
         *
         * @return the update. Never returns <code>null</code>.
         */
        public String getUpdate() {
            return update;
        }

        /**
         * {@inheritDoc}
         * <p>
         * Two versions are equal if and only if they have same major, minor,
         * micro number and update.
         */
        @Override
        public boolean equals(Object obj) {
            // XXX thiw want match compareTo contract in case there will be numbers like "1" and "01".
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Version other = (Version) obj;
            if (this.majorNumber != other.majorNumber
                    && (this.majorNumber == null || !this.majorNumber.equals(other.majorNumber))) {
                return false;
            }
            if (this.minorNumber != other.minorNumber
                    && (this.minorNumber == null || !this.minorNumber.equals(other.minorNumber))) {
                return false;
            }
            if (this.microNumber != other.microNumber
                    && (this.microNumber == null || !this.microNumber.equals(other.microNumber))) {
                return false;
            }
            if (this.update != other.update
                    && (this.update == null || !this.update.equals(other.update))) {
                return false;
            }
            if(this.wildfly != other.wildfly) {
                return false;
            }
            return true;
        }

        public boolean isWidlfy() {
            return wildfly;
        }

        /**
         * {@inheritDoc}
         * <p>
         * The implementation consistent with {@link #equals(Object)}.
         */
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + (this.majorNumber != null ? this.majorNumber.hashCode() : 0);
            hash = 17 * hash + (this.minorNumber != null ? this.minorNumber.hashCode() : 0);
            hash = 17 * hash + (this.microNumber != null ? this.microNumber.hashCode() : 0);
            hash = 17 * hash + (this.update != null ? this.update.hashCode() : 0);
            return hash;
        }

        /**
         * {@inheritDoc}
         * <p>
         * Compares the versions based on its major, minor, micro and update.
         * Major number is the most significant. Implementation is consistent
         * with {@link #equals(Object)}.
         */
        @Override
        public int compareTo(Version o) {
            int comparison = compareToIgnoreUpdate(o);
            if (comparison != 0) {
                return comparison;
            }
            return update.compareTo(o.update);
        }

        /**
         * Compares the versions based on its major, minor, micro. Update field
         * is ignored. Major number is the most significant.
         *
         * @param o version to compare with
         */
        public int compareToIgnoreUpdate(Version o) {
            if (o == null) {
                return 1;
            }
            if(this.wildfly != o.wildfly) {
               return convertEAP(this).compareToIgnoreUpdate(convertEAP(o));

            }
            int comparison = compare(majorNumber, o.majorNumber);
            if (comparison != 0) {
                return comparison;
            }
            comparison = compare(minorNumber, o.minorNumber);
            if (comparison != 0) {
                return comparison;
            }
            return compare(microNumber, o.microNumber);
        }

        private Version convertEAP(Version version) {
            if (!version.isWidlfy()) {
                if (version.compareToIgnoreUpdate(EAP_7_0) >= 0) {
                    return WILDFLY_10_0_0;
                }
                return JBOSS_7_0_0;
            }
            return version;
        }

        private int compare(String number1, String number2) {
            if (number1.length() != number2.length()) {
                try {
                    Integer i1 = Integer.parseInt(number1);
                    Integer i2 = Integer.parseInt(number2);
                    return i1.compareTo(i2);
                } catch (NumberFormatException ex) {
                    // compare as strings below
                }
            }
            return number1.compareTo(number2);
        }

        @Override
        public String toString() {
            return "Version{" + (wildfly ? "WILDFLY " : "EAP ") + "majorNumber=" + majorNumber + ", minorNumber=" + minorNumber + ", microNumber=" + microNumber + ", update=" + update + '}';
        }

    }

}
