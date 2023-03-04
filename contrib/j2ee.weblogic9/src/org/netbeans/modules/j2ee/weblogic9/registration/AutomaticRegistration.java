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

package org.netbeans.modules.j2ee.weblogic9.registration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentFactory;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.ui.wizard.WLInstantiatingIterator;
import org.netbeans.modules.weblogic.common.api.DomainConfiguration;
import org.netbeans.modules.weblogic.common.api.Version;
import org.netbeans.modules.weblogic.common.api.WebLogicLayout;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Registers a WebLogic instance by creating instance file in cluster config
 * directory. Designed to be called from installer.
 * <p>
 * Sample command line<br>
 * java -cp platform/core/core.jar:platform/core/core-base.jar:platform/lib/boot.jar:platform/lib/org-openide-modules.jar:platform/core/org-openide-filesystems.jar:platform/lib/org-openide-util.jar:platform/lib/org-openide-util-lookup.jar:platform/lib/org-openide-util-ui.jar:enterprise/modules/org-netbeans-modules-j2eeapis.jar:enterprise/modules/org-netbeans-modules-j2eeserver.jar:enterprise/modules/org-netbeans-modules-j2ee-weblogic9.jar:enterprise/modules/org-netbeans-modules-weblogic-common.jar org.netbeans.modules.j2ee.weblogic9.registration.AutomaticRegistration --add %lt;clusterDir&gt; &lt;serverDir&gt; &lt;domainDir&gt; &lt;username&gt; &lt;password&gt;
 *
 * @author Petr Hejl
 * @see #main(java.lang.String[]) 
 */
public class AutomaticRegistration {

    private static final Logger LOGGER = Logger.getLogger(AutomaticRegistration.class.getName());

    private static Version JDK8_ONLY_SERVER_VERSION = Version.fromJsr277NotationWithFallback("12.2.1"); // NOI18N

    /**
     * Performs registration/uregistration of server instance. May also list
     * existing weblogic instances.
     *
     * Exit codes:<p>
     * <ul>
     *   <li> 1: could not hadle cluster folder
     *   <li> 2: could not find/create config/J2EE/InstalledServers folder
     *   <li> 3: could not find server dir
     *   <li> 4: could not find domain dir
     *   <li> 5: unsupported version of Weblogic
     *   <li> 6: could not recognize the domain
     *   <li> 7: empty domain name
     *   <li> 8: the domain is in production mode
     *   <li> 9: the domain version does not match the server version
     *   <li> 10: could not write registration FileObject
     * </ul>
     * @param args command line arguments
     * <ul>
     *  <li>--add cluster_path server_dir domain_dir username password
     *  <li>--remove cluster_path server_dir domain_dir
     *  <li>--list cluster_path
     * </ul>
     */
    public static void main(String[] args) {
        if (args.length <= 0) {
            printHelpAndExit();
        }

        if ("--add".equals(args[0])) {
            if (args.length < 6) {
                printHelpAndExit();
            }
            int status = registerWebLogicInstance(args[1], args[2], args[3],
                    args[4], args[5], args.length == 7 ? args[6] : null);
            System.exit(status);
        } else if ("--remove".equals(args[0])) {
            if (args.length < 4) {
                printHelpAndExit();
            }
            int status = unregisterWebLogicInstance(args[1], args[2], args[3]);
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
        System.out.println("\t--add <clusterDir> <serverDir> <domainDir> <username> <password> [javaopts]");
        System.out.println("\t--remove <clusterDir> <serverDir> <domainDir>");
        System.out.println("\t--list <clusterDir>");
        System.exit(-1);
    }

    private static int registerWebLogicInstance(String clusterDirValue, String serverDirValue,
            String domainDirValue, String username, String password, String javaOpts) {
        // tell the infrastructure that the userdir is cluster dir
        System.setProperty("netbeans.user", clusterDirValue); // NOI18N

        FileObject serverInstanceDir = FileUtil.getConfigFile("J2EE/InstalledServers"); // NOI18N

        if (serverInstanceDir == null) {
            LOGGER.log(Level.INFO, "Cannot register the default WebLogic server. The config/J2EE/InstalledServers folder cannot be created."); // NOI18N
            return 2;
        }

        File serverDir = FileUtil.normalizeFile(new File(serverDirValue));
        if (!serverDir.exists()) {
            LOGGER.log(Level.INFO, "Cannot register the default WebLogic server. "
                    + "The server directory " + serverDirValue // NOI18N
                    + " does not exist."); // NOI18N
            return 3;
        }

        File domainDir = FileUtil.normalizeFile(new File(domainDirValue));
        if (!domainDir.exists()) {
            LOGGER.log(Level.INFO, "Cannot register the default WebLogic server. "
                    + "The domain directory " + domainDirValue // NOI18N
                    + " does not exist."); // NOI18N
            return 4;
        }

        Version version = WebLogicLayout.getServerVersion(serverDir);
        if (!WebLogicLayout.isSupportedVersion(version)) {
            LOGGER.log(Level.INFO, "Cannot register the default WebLogic server. "
                    + " The version " + version + " is not supported."); // NOI18N
            return 5;
        }

        DomainConfiguration config = WebLogicLayout.getDomainConfiguration(domainDir.getAbsolutePath());
        if (config == null) {
            LOGGER.log(Level.INFO, "Cannot register the default WebLogic server. "
                    + " The domain " + domainDirValue + " is not valid domain."); // NOI18N
            return 6;
        }

        String name = config.getAdminServer();
        int port = config.getPort();
        String host = config.getHost();
        String domainName = config.getName();
        Version domainVersion = config.getVersion();
        boolean isProductionMode = config.isProduction();
        if (name == null) {
            LOGGER.log(Level.INFO, "Cannot register the default WebLogic server. "
                    + " The domain name is empty."); // NOI18N
            return 7;
        }

        if (isProductionMode) {
            LOGGER.log(Level.INFO, "Cannot register the default WebLogic server. "
                    + " The domain is in production mode."); // NOI18N
            return 8;
        }

        // we do expand version string here because one may be 12.1.4.0 while the other may be 12.1.4.0.0
        if (domainVersion != null
                && version != null
                && !version.expand("0").equals(domainVersion.expand("0"))) { // NOI18N
            LOGGER.log(Level.INFO, "Cannot register the default WebLogic server. "
                    + " The domain version does not match the server version."); // NOI18N
            return 9;
        }

        // build URL
        StringBuilder urlTmp = new StringBuilder();
        urlTmp.append(WLDeploymentFactory.URI_PREFIX);
        urlTmp.append(host).append(":"); // NOI18N
        urlTmp.append(port).append(":"); // NOI18N
        urlTmp.append(serverDir.getAbsolutePath()).append(":"); // NOI18N
        urlTmp.append(domainDir.getAbsolutePath());

        final String url = urlTmp.toString();

        // make sure the server is not registered yet
        for (FileObject fo : serverInstanceDir.getChildren()) {
            if (url.equals(fo.getAttribute(InstanceProperties.URL_ATTR))) {
                // the server is already registered, do nothing
                return 0;
            }
        }

        String displayName = generateUniqueDisplayName(serverInstanceDir, version);
        boolean ok = registerServerInstanceFO(serverInstanceDir, url, displayName,
                serverDir.getAbsolutePath(), domainDir.getAbsolutePath(), domainName,
                Integer.toString(port), username, password, javaOpts, version);
        if (ok) {
            return 0;
        } else {
            return 10;
        }
    }

    private static int unregisterWebLogicInstance(String clusterDirValue,
            String serverDirValue, String domainDirValue) {
        // tell the infrastructure that the userdir is cluster dir
        System.setProperty("netbeans.user", clusterDirValue); // NOI18N

        // we could do this via registry, but the classspath would explode
        FileObject serverInstanceDir = FileUtil.getConfigFile("J2EE/InstalledServers"); // NOI18N

        if (serverInstanceDir == null) {
            LOGGER.log(Level.INFO, "The config/J2EE/InstalledServers folder does not exist."); // NOI18N
            return 2;
        }

        Pattern pattern = Pattern.compile(
                "^" + Pattern.quote(WLDeploymentFactory.URI_PREFIX) // NOI18N
                + "(.+):(.+):" // NOI18N
                + Pattern.quote(serverDirValue)
                + ":" + Pattern.quote(domainDirValue) + "$"); // NOI18N

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
            LOGGER.log(Level.INFO, "Cannot unregister the default WebLogic server."); // NOI18N
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
                "^" + Pattern.quote(WLDeploymentFactory.URI_PREFIX) // NOI18N
                + "(.+):(.+):(.+):(.+)$"); // NOI18N

        for (FileObject f : serverInstanceDir.getChildren()) {
            String url = f.getAttribute(InstanceProperties.URL_ATTR).toString();
            if (url != null) {
                Matcher matcher = pattern.matcher(url);
                if (matcher.matches()) {
                    System.out.println(matcher.group(3) + " " + matcher.group(4));
                }
            }
        }
        return 0;
    }

    /**
     * Generates a unique display name for the specified version of WebLogic
     *
     * @param serverInstanceDir /J2EE/InstalledServers folder
     * @param version WebLogic version
     *
     * @return a unique display name for the specified version of WebLogic
     */
    private static String generateUniqueDisplayName(FileObject serverInstanceDir, Version version) {
        // find a unique display name
        String displayName = NbBundle.getMessage(WLDeploymentFactory.class, "LBL_OracleWebLogic", version);
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
            displayName = NbBundle.getMessage(WLDeploymentFactory.class, "LBL_OracleWebLogicAlt", version, i++);
            unique = true;
        };
        return displayName;
    }

    /**
     * Registers the server instance file object and set the default properties.
     *
     * @param serverInstanceDir /J2EE/InstalledServers folder
     * @param url server instance url/ID
     * @param displayName display name
     */
    private static boolean registerServerInstanceFO(FileObject serverInstanceDir, String url,
            String displayName, String serverRoot, String domainRoot, String domainName,
            String port, String username, String password, String javaOpts, Version version) {

        String name = FileUtil.findFreeFileName(serverInstanceDir, "weblogic_autoregistered_instance", null); // NOI18N
        FileObject instanceFO;
        try {
            instanceFO = serverInstanceDir.createData(name);
            instanceFO.setAttribute(InstanceProperties.URL_ATTR, url);
            instanceFO.setAttribute(InstanceProperties.USERNAME_ATTR, username);
            instanceFO.setAttribute(InstanceProperties.PASSWORD_ATTR, password);
            instanceFO.setAttribute(InstanceProperties.DISPLAY_NAME_ATTR, displayName);
            instanceFO.setAttribute(InstanceProperties.HTTP_PORT_NUMBER, port);
            instanceFO.setAttribute(WLPluginProperties.SERVER_ROOT_ATTR, serverRoot);
            instanceFO.setAttribute(WLPluginProperties.DOMAIN_ROOT_ATTR, domainRoot);
            instanceFO.setAttribute(WLPluginProperties.DEBUGGER_PORT_ATTR,
                    WLInstantiatingIterator.DEFAULT_DEBUGGER_PORT);
            instanceFO.setAttribute(WLPluginProperties.PROXY_ENABLED,
                    WLInstantiatingIterator.DEFAULT_PROXY_ENABLED);
            instanceFO.setAttribute(WLPluginProperties.DOMAIN_NAME, domainName);
            instanceFO.setAttribute(WLPluginProperties.PORT_ATTR, port);
            if (javaOpts != null) {
                instanceFO.setAttribute(WLPluginProperties.JAVA_OPTS, javaOpts);
            }
            if (Utilities.isMac()) {
                StringBuilder memOpts = new StringBuilder(WLInstantiatingIterator.DEFAULT_MAC_MEM_OPTS_HEAP);
                if (version != null && !JDK8_ONLY_SERVER_VERSION.isBelowOrEqual(version)) {
                    memOpts.append(' '); // NOI18N
                    memOpts.append(WLInstantiatingIterator.DEFAULT_MAC_MEM_OPTS_PERM);
                }
                instanceFO.setAttribute(WLPluginProperties.MEM_OPTS, memOpts.toString());
            }
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Cannot register the default WebLogic server."); // NOI18N
            LOGGER.log(Level.INFO, null, e);
        }
        return false;
    }
}
