/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.common.registration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.tooling.data.GlassFishVersion;
import org.netbeans.modules.glassfish.tooling.utils.ServerUtils;
import org.netbeans.modules.glassfish.common.GlassfishInstanceProvider;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Registers a GlassFish instance by creating instance file in cluster
 * configuration directory.
 * <p/>
 * Designed to be called from installer.
 * <p/>
 * Sample command line<br/>
 * java -cp ./platform/core/core.jar:./platform/core/core-base.jar:./platform/lib/boot.jar:./platform/lib/org-openide-modules.jar:./platform/core/org-openide-filesystems.jar:./platform/lib/org-openide-util.jar:./platform/lib/org-openide-util-lookup.jar:./platform/lib/org-openide-util-ui.jar:./enterprise/modules/org-netbeans-modules-j2eeapis.jar:./enterprise/modules/org-netbeans-modules-j2eeserver.jar:./enterprise/modules/org-netbeans-modules-glassfish-common.jar:./enterprise/modules/org-netbeans-modules-glassfish-tooling.jar org.netbeans.modules.glassfish.common.registration.AutomaticRegistration %lt;clusterDir&gt; &lt;glassfishDir&gt;
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
     *   <li> 3: could not find GlassFish home
     *   <li> 4: could not recognize GlassFish version
     *   <li> 5: unsupported version of GlassFish
     *   <li> 6: could not write registration FileObject
     * </ul>
     * @param args command line arguments - cluster path and GlassFish home expected
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Parameters: <ide clusterDir> <GlassFishHome> <Java 7 or later home>");
            System.exit(-1);
        }
        
        String javaExe = args.length == 3 ? args[2] : "";
        int status = autoregisterGlassFishInstance(args[0], args[1], javaExe);
        System.exit(status);
    }

    private static int autoregisterGlassFishInstance(String clusterDirValue, String glassfishRoot, String jdk7orLaterPath) throws IOException {
        // tell the infrastructure that the userdir is cluster dir
        System.setProperty("netbeans.user", clusterDirValue); // NOI18N

        File glassfishHome = new File(glassfishRoot);
        if (!glassfishHome.exists()) {
            LOGGER.log(Level.INFO, "Cannot register the default GlassFish server. " // NOI18N
                    + "The GlassFish Root directory {0} does not exist.", glassfishRoot); // NOI18N
            return 3;
        }
        String config = "GlassFishEE6/Instances";
        String deployer = "deployer:gfv3ee6";
        String defaultDisplayNamePrefix = "GlassFish Server ";
        GlassFishVersion version = ServerUtils.getServerVersion(glassfishRoot);
        if (GlassFishVersion.ge(version, GlassFishVersion.GF_3_1)) {
            deployer = "deployer:gfv3ee6wc";
        }
        StringBuilder sb = new StringBuilder(
                defaultDisplayNamePrefix.length() + 12);
        if (version != null) {
            sb.append(defaultDisplayNamePrefix);
            sb.append(version.toString());
        } else {
            LOGGER.log(Level.INFO, "Cannot register the default GlassFish server. " // NOI18N
                    + "The GlassFish Root directory {0} is of unknown version.", glassfishRoot); // NOI18N
            return 4;
        }
        String defaultDisplayName = sb.toString();
        FileObject serverInstanceDir = FileUtil.getConfigFile(config); // NOI18N

        if (serverInstanceDir == null) {
            serverInstanceDir = FileUtil.createFolder(FileUtil.getConfigRoot(), config);
            if (serverInstanceDir == null) {
                LOGGER.log(Level.INFO, "Cannot register the default GlassFish"
                        + " server. The config/{0} folder cannot be created.", config); // NOI18N
                return 2;
            }
        }
        
        // beware of trailling File.separator
        //
        glassfishRoot = new File(glassfishRoot).getAbsolutePath();

        final String url = "[" + glassfishRoot + File.pathSeparator + 
                glassfishRoot + File.separator + "domains" + File.separator
                + "domain1]" + deployer + ":localhost:4848"; // NOI18N

        // make sure the server is not registered yet
        for (FileObject fo : serverInstanceDir.getChildren()) {
            if (url.equals(fo.getAttribute(GlassfishModule.URL_ATTR))) {
                // the server is already registered, do nothing
                return 0;
            }
        }

        File jdk7orLaterExecutable = new File(jdk7orLaterPath);
        if (!jdk7orLaterExecutable.exists()) {
            jdk7orLaterExecutable = null;
        }
        String displayName = generateUniqueDisplayName(serverInstanceDir, defaultDisplayName);
        boolean ok = registerServerInstanceFO(serverInstanceDir, url, displayName, glassfishHome, jdk7orLaterExecutable);
        if (ok) {
            return 0;
        } else {
            return 6;
        }
    }

    /**
     * Generates a unique display name for the specified version of GlassFish
     *
     * @param serverInstanceDir /J2EE/InstalledServers folder
     * @param version GlassFish version
     *
     * @return a unique display name for the specified version of GlassFish
     */
    private static String generateUniqueDisplayName(FileObject serverInstanceDir, String defaultDisplayName) {
        // find a unique display name
        String displayName = defaultDisplayName; // NOI18N
        boolean unique = true;
        int i = 1;
        while (true) {
            for (FileObject fo : serverInstanceDir.getChildren()) {
                if (displayName.equals(fo.getAttribute(GlassfishModule.DISPLAY_NAME_ATTR))) {
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
    private static boolean registerServerInstanceFO(FileObject serverInstanceDir, String url, String displayName, File glassfishRoot, File java7orLaterExecutable) {
        String name = FileUtil.findFreeFileName(serverInstanceDir, 
                GlassfishInstanceProvider.GLASSFISH_AUTOREGISTERED_INSTANCE, null);
        FileObject instanceFO;
        try {
            instanceFO = serverInstanceDir.createData(name);
            instanceFO.setAttribute(GlassfishModule.URL_ATTR, url);
            instanceFO.setAttribute(GlassfishModule.USERNAME_ATTR, "admin"); // NOI18N
            //String password = Utils.generatePassword(8);
            instanceFO.setAttribute(GlassfishModule.PASSWORD_ATTR, "");
            instanceFO.setAttribute(GlassfishModule.DISPLAY_NAME_ATTR, displayName);
            instanceFO.setAttribute(GlassfishModule.ADMINPORT_ATTR, "4848"); // NOI18N
            instanceFO.setAttribute(GlassfishModule.INSTALL_FOLDER_ATTR, glassfishRoot.getParent());
            instanceFO.setAttribute(GlassfishModule.DEBUG_PORT, ""); // NOI18N
            instanceFO.setAttribute(GlassfishModule.DOMAIN_NAME_ATTR, "domain1"); // NOI18N
            instanceFO.setAttribute(GlassfishModule.DOMAINS_FOLDER_ATTR, (new File(glassfishRoot, "domains")).getAbsolutePath());
            instanceFO.setAttribute(GlassfishModule.DRIVER_DEPLOY_FLAG, "true");
            instanceFO.setAttribute(GlassfishModule.INSTALL_FOLDER_ATTR, glassfishRoot.getParent());
            instanceFO.setAttribute(GlassfishModule.HOSTNAME_ATTR, "localhost"); // NOI18N
            instanceFO.setAttribute(GlassfishModule.GLASSFISH_FOLDER_ATTR, glassfishRoot.getAbsolutePath());
            instanceFO.setAttribute(GlassfishModule.JAVA_PLATFORM_ATTR, java7orLaterExecutable == null ? "" : java7orLaterExecutable.getAbsolutePath()); // NOI18N
            instanceFO.setAttribute(GlassfishModule.HTTPPORT_ATTR, "8080"); // NOI18N
            instanceFO.setAttribute(GlassfishModule.HTTPHOST_ATTR, "localhost"); // NOI18N
            instanceFO.setAttribute(GlassfishModule.JVM_MODE, GlassfishModule.NORMAL_MODE);
            instanceFO.setAttribute(GlassfishModule.SESSION_PRESERVATION_FLAG, true);
            instanceFO.setAttribute(GlassfishModule.START_DERBY_FLAG, true);
            instanceFO.setAttribute(GlassfishModule.USE_IDE_PROXY_FLAG, true);
            instanceFO.setAttribute(GlassfishModule.USE_SHARED_MEM_ATTR, false);
            
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Cannot register the default GlassFish server."); // NOI18N
            LOGGER.log(Level.INFO, null, e);
        }
        return false;
    }
}
