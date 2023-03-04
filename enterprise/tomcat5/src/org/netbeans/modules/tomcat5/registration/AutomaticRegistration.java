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

package org.netbeans.modules.tomcat5.registration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.tomcat5.TomcatFactory;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import org.netbeans.modules.tomcat5.util.TomcatProperties;
import org.netbeans.modules.tomcat5.util.Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Registers a Tomcat instance by creating instance file in cluster config
 * directory. Designed to be called from installer.
 * <p>
 * Sample command line<br>
 * java -cp platform/core/core.jar:platform/core/core-base.jar:platform/lib/boot.jar:platform/lib/org-openide-modules.jar:platform/core/org-openide-filesystems.jar:platform/lib/org-openide-util.jar:platform/lib/org-openide-util-lookup.jar:platform/lib/org-openide-util-ui.jar:enterprise/modules/org-netbeans-modules-j2eeapis.jar:enterprise/modules/org-netbeans-modules-j2eeserver.jar:enterprise/modules/org-netbeans-modules-tomcat5.jar org.netbeans.modules.tomcat5.registration.AutomaticRegistration --add %lt;clusterDir&gt; &lt;catalinaHome&gt;
 *
 * @author Petr Hejl
 * @see #main(args)
 */
public class AutomaticRegistration {

    private static final Logger LOGGER = Logger.getLogger(AutomaticRegistration.class.getName());

    /**
     * Performs registration/uregistration of server instance. May also list
     * existing tomcat instances.
     *
     * Exit codes:<p>
     * <ul>
     *   <li> 1: could not hadle cluster folder
     *   <li> 2: could not find/create config/J2EE/InstalledServers folder
     *   <li> 3: could not find catalina home
     *   <li> 4: could not recognize Tomcat version
     *   <li> 5: unsupported version of Tomcat
     *   <li> 6: could not write registration FileObject
     * </ul>
     * @param args command line arguments
     * <ul>
     *  <li>--add cluster_path catalina_home
     *  <li>--remove cluster_path catalina_home
     *  <li>--list cluster_path
     * </ul>
     */
    public static void main(String[] args) {
        if (args.length <= 0) {
            printHelpAndExit();
        }

        if ("--add".equals(args[0])) {
            if (args.length < 3) {
                printHelpAndExit();
            }
            int status = registerTomcatInstance(args[1], args[2]);
            System.exit(status);
        } else if ("--remove".equals(args[0])) {
            if (args.length < 3) {
                printHelpAndExit();
            }
            int status = unregisterTomcatInstance(args[1], args[2]);
            System.exit(status);
        } else if ("--list".equals(args[0])) {
            if (args.length < 2) {
                printHelpAndExit();
            }
            list(args[1]);
        } else {
            printHelpAndExit();
        }
    }

    private static void printHelpAndExit() {
        System.out.println("Available actions:");
        System.out.println("\t--add <clusterDir> <catalinaHome>");
        System.out.println("\t--remove <clusterDir> <catalinaHome>");
        System.out.println("\t--list <clusterDir>");
        System.exit(-1);
    }

    private static int registerTomcatInstance(String clusterDirValue, String catalinaHomeValue) {
        // tell the infrastructure that the userdir is cluster dir
        System.setProperty("netbeans.user", clusterDirValue); // NOI18N

        FileObject serverInstanceDir = FileUtil.getConfigFile("J2EE/InstalledServers"); // NOI18N

        if (serverInstanceDir == null) {
            LOGGER.log(Level.INFO, "Cannot register the default Tomcat server. The config/J2EE/InstalledServers folder cannot be created."); // NOI18N
            return 2;
        }

        File catalinaHome = new File(catalinaHomeValue);
        if (!catalinaHome.exists()) {
            LOGGER.log(Level.INFO, "Cannot register the default Tomcat server. The Catalina Home directory {0} does not exist.", catalinaHomeValue); // NOI18N
            return 3;
        }

        String version;
        try {
            version = TomcatFactory.getTomcatVersionString(catalinaHome);
        } catch (IllegalStateException e) {
            LOGGER.log(Level.INFO, "Cannot register the default Tomcat server.  Cannot recognize the Tomcat version."); // NOI18N
            LOGGER.log(Level.INFO, null, e);
            return 4;
        }

        // build URL
        StringBuilder urlTmp;
        if (version.startsWith("5.0.")) { // NOI18N
            urlTmp = new StringBuilder(TomcatFactory.TOMCAT_URI_PREFIX_50);
        } else if (version.startsWith("5.5.")) { // NOI18N
            urlTmp = new StringBuilder(TomcatFactory.TOMCAT_URI_PREFIX_55);
        } else if (version.startsWith("6.")) { // NOI18N
            urlTmp = new StringBuilder(TomcatFactory.TOMCAT_URI_PREFIX_60);
        } else if (version.startsWith("7.")) { // NOI18N
            urlTmp = new StringBuilder(TomcatFactory.TOMCAT_URI_PREFIX_70);
        } else if (version.startsWith("8.")) { // NOI18N
            urlTmp = new StringBuilder(TomcatFactory.TOMCAT_URI_PREFIX_80);
        } else if (version.startsWith("9.")) { // NOI18N
            urlTmp = new StringBuilder(TomcatFactory.TOMCAT_URI_PREFIX_90);
        } else if (version.startsWith("10.")) { // NOI18N
            urlTmp = new StringBuilder(TomcatFactory.TOMCAT_URI_PREFIX_100);
        } else if (version.startsWith("10.1")) { // NOI18N
            urlTmp = new StringBuilder(TomcatFactory.TOMCAT_URI_PREFIX_101);
        } else {
            LOGGER.log(Level.INFO, "Cannot register the default Tomcat server.  The version {0} is not supported.", version); // NOI18N
            return 5;
        }
        urlTmp.append(TomcatFactory.TOMCAT_URI_HOME_PREFIX);
        urlTmp.append(catalinaHomeValue);
        urlTmp.append(TomcatFactory.TOMCAT_URI_BASE_PREFIX);
        urlTmp.append("apache-tomcat-"); // NOI18N
        urlTmp.append(version);
        urlTmp.append("_base"); // NOI18N

        final String url = urlTmp.toString();

        // make sure the server is not registered yet
        for (FileObject fo : serverInstanceDir.getChildren()) {
            if (url.equals(fo.getAttribute(InstanceProperties.URL_ATTR))) {
                // the server is already registered, do nothing
                return 0;
            }
        }

        String displayName = generateUniqueDisplayName(serverInstanceDir, version);
        boolean ok = registerServerInstanceFO(serverInstanceDir, url, displayName);
        if (ok) {
            return 0;
        } else {
            return 6;
        }
    }

    private static int unregisterTomcatInstance(String clusterDirValue, String catalinaHomeValue) {
        // tell the infrastructure that the userdir is cluster dir
        System.setProperty("netbeans.user", clusterDirValue); // NOI18N

        // we could do this via registry, but the classspath would explode
        FileObject serverInstanceDir = FileUtil.getConfigFile("J2EE/InstalledServers"); // NOI18N

        if (serverInstanceDir == null) {
            LOGGER.log(Level.INFO, "The config/J2EE/InstalledServers folder does not exist."); // NOI18N
            return 2;
        }

        Pattern pattern = Pattern.compile(
                "^(" + Pattern.quote(TomcatFactory.TOMCAT_URI_PREFIX_50) // NOI18N
                + "|" + Pattern.quote(TomcatFactory.TOMCAT_URI_PREFIX_55) // NOI18N
                + "|" + Pattern.quote(TomcatFactory.TOMCAT_URI_PREFIX_60) // NOI18N
                + "|" + Pattern.quote(TomcatFactory.TOMCAT_URI_PREFIX_70) // NOI18N
                + "|" + Pattern.quote(TomcatFactory.TOMCAT_URI_PREFIX_80) // NOI18N
                + "|" + Pattern.quote(TomcatFactory.TOMCAT_URI_PREFIX_90) // NOI18N
                + "|" + Pattern.quote(TomcatFactory.TOMCAT_URI_PREFIX_100) // NOI18N
                + "|" + Pattern.quote(TomcatFactory.TOMCAT_URI_PREFIX_101) // NOI18N
                + ")" + Pattern.quote(TomcatFactory.TOMCAT_URI_HOME_PREFIX) // NOI18N
                + Pattern.quote(catalinaHomeValue)
                + "(" + Pattern.quote(TomcatFactory.TOMCAT_URI_BASE_PREFIX) + ".+)?$"); // NOI18N

        try {
            for (FileObject f : serverInstanceDir.getChildren()) {
                String url = f.getAttribute(InstanceProperties.URL_ATTR).toString();
                if (url != null) {
                    if (pattern.matcher(url).matches()) {
                        f.delete();
                        return 0;
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Cannot unregister the default Tomcat server."); // NOI18N
            LOGGER.log(Level.INFO, null, ex);
            return 6;
        }
        return 0;
    }

    private static int list(String clusterDirValue) {
        // tell the infrastructure that the userdir is cluster dir
        System.setProperty("netbeans.user", clusterDirValue); // NOI18N

        // we could do this via registry, but the classspath would explode
        FileObject serverInstanceDir = FileUtil.getConfigFile("J2EE/InstalledServers"); // NOI18N

        if (serverInstanceDir == null) {
            LOGGER.log(Level.INFO, "The config/J2EE/InstalledServers folder does not exist."); // NOI18N
            return 2;
        }

        Pattern pattern = Pattern.compile(
                "^(" + Pattern.quote(TomcatFactory.TOMCAT_URI_PREFIX_50) // NOI18N
                + "|" + Pattern.quote(TomcatFactory.TOMCAT_URI_PREFIX_55)  // NOI18N
                + "|" + Pattern.quote(TomcatFactory.TOMCAT_URI_PREFIX_60)  // NOI18N
                + "|" + Pattern.quote(TomcatFactory.TOMCAT_URI_PREFIX_70)  // NOI18N
                + "|" + Pattern.quote(TomcatFactory.TOMCAT_URI_PREFIX_80)  // NOI18N
                + "|" + Pattern.quote(TomcatFactory.TOMCAT_URI_PREFIX_90)  // NOI18N
                + "|" + Pattern.quote(TomcatFactory.TOMCAT_URI_PREFIX_100)  // NOI18N
                + "|" + Pattern.quote(TomcatFactory.TOMCAT_URI_PREFIX_101)  // NOI18N
                + ")" + Pattern.quote(TomcatFactory.TOMCAT_URI_HOME_PREFIX)  // NOI18N
                + "(.+)$"); // NOI18N

        for (FileObject f : serverInstanceDir.getChildren()) {
            String url = f.getAttribute(InstanceProperties.URL_ATTR).toString();
            if (url != null) {
                Matcher matcher = pattern.matcher(url);
                if (matcher.matches()) {
                    String loc = matcher.group(2);
                    int base = loc.indexOf(TomcatFactory.TOMCAT_URI_BASE_PREFIX);
                    System.out.println(loc.substring(0, base));
                }
            }
        }
        return 0;
    }

    /**
     * Generates a unique display name for the specified version of Tomcat
     *
     * @param serverInstanceDir /J2EE/InstalledServers folder
     * @param version Tomcat version
     *
     * @return a unique display name for the specified version of Tomcat
     */
    private static String generateUniqueDisplayName(FileObject serverInstanceDir, String version) {
        // find a unique display name
        String displayName = NbBundle.getMessage(AutomaticRegistration.class, "LBL_ApacheTomcat", version);
        boolean unique = true;
        int i = 1;
        while (true) {
            for (FileObject fo : serverInstanceDir.getChildren()) {
                if (displayName.equals(fo.getAttribute(InstanceProperties.DISPLAY_NAME_ATTR))) {
                    // there is already some server of the same name
                    unique = false;
                    break;
                }
            }
            if (unique) {
                break;
            }
            displayName = NbBundle.getMessage(AutomaticRegistration.class, "LBL_ApacheTomcatAlt", version, i++);
            unique = true;
        }
        return displayName;
    }

    /**
     * Registers the server instance file object and set the default properties.
     *
     * @param serverInstanceDir /J2EE/InstalledServers folder
     * @param url server instance url/ID
     * @param displayName display name
     */
    private static boolean registerServerInstanceFO(FileObject serverInstanceDir, String url, String displayName) {
        String name = FileUtil.findFreeFileName(serverInstanceDir, "tomcat_autoregistered_instance", null); // NOI18N
        FileObject instanceFO;
        try {
            instanceFO = serverInstanceDir.createData(name);
            instanceFO.setAttribute(InstanceProperties.URL_ATTR, url);
            instanceFO.setAttribute(InstanceProperties.USERNAME_ATTR, "ide"); // NOI18N
            String password = Utils.generatePassword(8);
            instanceFO.setAttribute(InstanceProperties.PASSWORD_ATTR, password);
            instanceFO.setAttribute(InstanceProperties.DISPLAY_NAME_ATTR, displayName);
            instanceFO.setAttribute(InstanceProperties.HTTP_PORT_NUMBER, "8084"); // NOI18N
            instanceFO.setAttribute(TomcatProperties.PROP_SHUTDOWN, "8025"); // NOI18N
            instanceFO.setAttribute(TomcatProperties.PROP_MONITOR, "true"); // NOI18N
            instanceFO.setAttribute(TomcatManager.PROP_BUNDLED_TOMCAT, "true"); // NOI18N
            instanceFO.setAttribute(TomcatProperties.PROP_AUTOREGISTERED, "true"); // NOI18N
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Cannot register the default Tomcat server."); // NOI18N
            LOGGER.log(Level.INFO, null, e);
        }
        return false;
    }
}
