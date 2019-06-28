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

package org.netbeans.modules.payara.common.registration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.payara.tooling.data.PayaraVersion;
import org.netbeans.modules.payara.tooling.utils.ServerUtils;
import org.netbeans.modules.payara.common.PayaraInstanceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.payara.spi.PayaraModule;

/**
 * Registers a Payara instance by creating instance file in cluster
 * configuration directory.
 * <p/>
 * Designed to be called from installer.
 * <p/>
 * Sample command line<br/>
 * java -cp ./platform/core/core.jar:./platform/core/core-base.jar:./platform/lib/boot.jar:./platform/lib/org-openide-modules.jar:./platform/core/org-openide-filesystems.jar:./platform/lib/org-openide-util.jar:./platform/lib/org-openide-util-lookup.jar:./platform/lib/org-openide-util-ui.jar:./enterprise/modules/org-netbeans-modules-j2eeapis.jar:./enterprise/modules/org-netbeans-modules-j2eeserver.jar:./enterprise/modules/org-netbeans-modules-payara-common.jar:./enterprise/modules/org-netbeans-modules-payara-tooling.jar org.netbeans.modules.payara.common.registration.AutomaticRegistration %lt;clusterDir&gt; &lt;glassfishDir&gt;
 * <p/>
 * @author Vince Kraemer, Petr Hejl Tomas Kraus
 * @see #main(args)
 */
public class AutomaticRegistration {

    private static final Logger LOGGER = Logger.getLogger(AutomaticRegistration.class.getName());

    /**
     * Performs registration.
     * <p/>
     * Exit codes:<p>
     * <ul>
     *   <li> 2: could not find/create config/J2EE/InstalledServers folder
     *   <li> 3: could not find Payara home
     *   <li> 4: could not recognize Payara version
     *   <li> 5: unsupported version of Payara
     *   <li> 6: could not write registration FileObject
     * </ul>
     * @param args command line arguments - cluster path and Payara home expected
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Parameters: <ide clusterDir> <PayaraHome> <Java 7 or later home>");
            System.exit(-1);
        }
        
        String javaExe = args.length == 3 ? args[2] : "";
        int status = autoregisterPayaraInstance(args[0], args[1], javaExe);
        System.exit(status);
    }

    private static int autoregisterPayaraInstance(String clusterDirValue, String payaraRoot, String jdk7orLaterPath) throws IOException {
        // tell the infrastructure that the userdir is cluster dir
        System.setProperty("netbeans.user", clusterDirValue); // NOI18N

        File payaraHome = new File(payaraRoot);
        if (!payaraHome.exists()) {
            LOGGER.log(Level.INFO, "Cannot register the default Payara server. " // NOI18N
                    + "The Payara Root directory {0} does not exist.", payaraRoot); // NOI18N
            return 3;
        }
        String config = "PayaraEE6/Instances";
        String deployer = "deployer:pfv3ee6";
        String defaultDisplayNamePrefix = "Payara Server ";
        PayaraVersion version = ServerUtils.getServerVersion(payaraRoot);
        if (PayaraVersion.ge(version, PayaraVersion.PF_4_1_144)) {
            deployer = "deployer:pfv4ee7";
        } 
        if (PayaraVersion.ge(version, PayaraVersion.PF_5_181)) {
            deployer = "deployer:pfv5ee8";
        }

        StringBuilder sb = new StringBuilder(
                defaultDisplayNamePrefix.length() + 12);
        if (version != null) {
            sb.append(defaultDisplayNamePrefix);
            sb.append(version.toString());
        } else {
            LOGGER.log(Level.INFO, "Cannot register the default Payara server. " // NOI18N
                    + "The Payara Root directory {0} is of unknown version.", payaraRoot); // NOI18N
            return 4;
        }
        String defaultDisplayName = sb.toString();
        FileObject serverInstanceDir = FileUtil.getConfigFile(config); // NOI18N

        if (serverInstanceDir == null) {
            serverInstanceDir = FileUtil.createFolder(FileUtil.getConfigRoot(), config);
            if (serverInstanceDir == null) {
                LOGGER.log(Level.INFO, "Cannot register the default Payara"
                        + " server. The config/{0} folder cannot be created.", config); // NOI18N
                return 2;
            }
        }
        
        // beware of trailling File.separator
        //
        payaraRoot = new File(payaraRoot).getAbsolutePath();

        final String url = "[" + payaraRoot + File.pathSeparator + 
                payaraRoot + File.separator + "domains" + File.separator
                + "domain1]" + deployer + ":localhost:4848"; // NOI18N

        // make sure the server is not registered yet
        for (FileObject fo : serverInstanceDir.getChildren()) {
            if (url.equals(fo.getAttribute(PayaraModule.URL_ATTR))) {
                // the server is already registered, do nothing
                return 0;
            }
        }

        File jdk7orLaterExecutable = new File(jdk7orLaterPath);
        if (!jdk7orLaterExecutable.exists()) {
            jdk7orLaterExecutable = null;
        }
        String displayName = generateUniqueDisplayName(serverInstanceDir, defaultDisplayName);
        boolean ok = registerServerInstanceFO(serverInstanceDir, url, displayName, payaraHome, jdk7orLaterExecutable);
        if (ok) {
            return 0;
        } else {
            return 6;
        }
    }

    /**
     * Generates a unique display name for the specified version of Payara
     *
     * @param serverInstanceDir /J2EE/InstalledServers folder
     * @param version Payara version
     *
     * @return a unique display name for the specified version of Payara
     */
    private static String generateUniqueDisplayName(FileObject serverInstanceDir, String defaultDisplayName) {
        // find a unique display name
        String displayName = defaultDisplayName; // NOI18N
        boolean unique = true;
        int i = 1;
        while (true) {
            for (FileObject fo : serverInstanceDir.getChildren()) {
                if (displayName.equals(fo.getAttribute(PayaraModule.DISPLAY_NAME_ATTR))) {
                    // there is already some server of the same name
                    unique = false;
                    break;
                }
            }
            if (unique) {
                break;
            }
            displayName = defaultDisplayName + " "+i++;
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
    private static boolean registerServerInstanceFO(FileObject serverInstanceDir, String url, String displayName, File payaraRoot, File java7orLaterExecutable) {
        String name = FileUtil.findFreeFileName(serverInstanceDir, 
                PayaraInstanceProvider.PAYARA_AUTOREGISTERED_INSTANCE, null);
        FileObject instanceFO;
        try {
            instanceFO = serverInstanceDir.createData(name);
            instanceFO.setAttribute(PayaraModule.URL_ATTR, url);
            instanceFO.setAttribute(PayaraModule.USERNAME_ATTR, "admin"); // NOI18N
            //String password = Utils.generatePassword(8);
            instanceFO.setAttribute(PayaraModule.PASSWORD_ATTR, "");
            instanceFO.setAttribute(PayaraModule.DISPLAY_NAME_ATTR, displayName);
            instanceFO.setAttribute(PayaraModule.ADMINPORT_ATTR, "4848"); // NOI18N
            instanceFO.setAttribute(PayaraModule.INSTALL_FOLDER_ATTR, payaraRoot.getParent());
            instanceFO.setAttribute(PayaraModule.DEBUG_PORT, ""); // NOI18N
            instanceFO.setAttribute(PayaraModule.DOMAIN_NAME_ATTR, "domain1"); // NOI18N
            instanceFO.setAttribute(PayaraModule.DOMAINS_FOLDER_ATTR, (new File(payaraRoot, "domains")).getAbsolutePath());
            instanceFO.setAttribute(PayaraModule.DRIVER_DEPLOY_FLAG, "true");
            instanceFO.setAttribute(PayaraModule.INSTALL_FOLDER_ATTR, payaraRoot.getParent());
            instanceFO.setAttribute(PayaraModule.HOSTNAME_ATTR, "localhost"); // NOI18N
            instanceFO.setAttribute(PayaraModule.PAYARA_FOLDER_ATTR, payaraRoot.getAbsolutePath());
            instanceFO.setAttribute(PayaraModule.JAVA_PLATFORM_ATTR, java7orLaterExecutable == null ? "" : java7orLaterExecutable.getAbsolutePath()); // NOI18N
            instanceFO.setAttribute(PayaraModule.HTTPPORT_ATTR, "8080"); // NOI18N
            instanceFO.setAttribute(PayaraModule.HTTPHOST_ATTR, "localhost"); // NOI18N
            instanceFO.setAttribute(PayaraModule.JVM_MODE, PayaraModule.NORMAL_MODE);
            instanceFO.setAttribute(PayaraModule.SESSION_PRESERVATION_FLAG, true);
            instanceFO.setAttribute(PayaraModule.START_DERBY_FLAG, false);
            instanceFO.setAttribute(PayaraModule.USE_IDE_PROXY_FLAG, true);
            instanceFO.setAttribute(PayaraModule.USE_SHARED_MEM_ATTR, false);
            
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Cannot register the default Payara server."); // NOI18N
            LOGGER.log(Level.INFO, null, e);
        }
        return false;
    }
}
