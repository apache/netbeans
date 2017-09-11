/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.derby;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Registers installation of JavaDB by creating instance file in cluster config
 * directory. Designed to be called from installer.
 * The DerbyRegistration can called multiple times but only the first invocation
 * is stored.
 * <p>
 * Sample command line<br>
 *
 * java -cp platform/core/core.jar:platform/lib/boot.jar:platform/lib/org-openide-modules.jar:
 *          platform/core/org-openide-filesystems.jar:platform/lib/org-openide-util.jar:
 *          platform/lib/org-openide-util-lookup.jar:ide/modules/org-netbeans-modules-derby.jar
 *             org.netbeans.modules.derby.DerbyRegistration nb /usr/local/share/java/jdk1.6.0_18/db
 *
 * @author Jiri Rechtacek
 * @see #main(args)
 */
public class DerbyRegistration {

    private static final String DERBY_REGISTRATION_DIR = "JavaDB"; // NOI18N
    private static final String DERBY_REGISTRATION_FILE = "registration_instance"; // NOI18N
    private static final String ATTR_DERBY_HOME = "javadb_installation_home"; // NOI18N
    private static final Logger LOGGER = Logger.getLogger(DerbyRegistration.class.getName());

    /**
     * Performs registration.
     *
     * Exit codes:<p>
     * <ul>
     *   <li> 2: could not find/create config/JavaDB folder
     *   <li> 3: could not find JavaDB installation home
     *   <li> 4: JavaDB installation doesn't contain expected libraries, i.e. lib/derbyclient.jar
     *   <li> 5: could not write registration FileObject
     * </ul>
     * @param args command line arguments - cluster path and Derby home expected
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Parameters: <nb clusterDir> <Derby home>");
            System.exit(-1);
        }

        int status = registerDerbyInstallation(args[0], args[1]);
        System.exit(status);
    }

    private static int registerDerbyInstallation(String cluster, String derbyHome) throws IOException {
        LOGGER.log(Level.FINE, "Cluster for JavaDB registration is " + cluster);
        System.setProperty("netbeans.user", cluster); // NOI18N
        // check writable cluster
        FileObject javaDBRegistrationDir = FileUtil.getConfigFile(DERBY_REGISTRATION_DIR);

        if (javaDBRegistrationDir == null) {
            javaDBRegistrationDir = FileUtil.createFolder(FileUtil.getConfigRoot(), DERBY_REGISTRATION_DIR);
            if (javaDBRegistrationDir == null) {
                LOGGER.log(Level.INFO, "Cannot register the default JavaDB. The config/" + DERBY_REGISTRATION_DIR + " folder cannot be created."); // NOI18N
                return 2;
            }
        }

        // check if exists
        File derbyHomeFile = new File(derbyHome);
        if (!derbyHomeFile.exists()) {
            LOGGER.log(Level.INFO, "Cannot register the default JavaDB. " // NOI18N
                    + "The JavaDB installation directory " + derbyHome // NOI18N
                    + " does not exist."); // NOI18N
            return 3;
        }

        // check if solid JavaDB home
        if (! Util.isDerbyInstallLocation(derbyHomeFile)) {
            LOGGER.log(Level.INFO, "Cannot register the default JavaDB. " // NOI18N
                    + "The JavaDB installation directory " + derbyHome // NOI18N
                    + " does not contain expected libraries."); // NOI18N
            return 4;
        }

        FileObject registrationFO = FileUtil.getConfigFile(DERBY_REGISTRATION_DIR + "/" + DERBY_REGISTRATION_FILE); // NOI18N
        if (registrationFO == null) {
            try {
                registrationFO = javaDBRegistrationDir.createData(DERBY_REGISTRATION_FILE);
                registrationFO.setAttribute(ATTR_DERBY_HOME, derbyHome);
                LOGGER.log(Level.FINE, "New registration links to " + registrationFO.getAttribute(ATTR_DERBY_HOME)); // NOI18N
            } catch (IOException e) {
                LOGGER.log(Level.INFO, "Cannot register JavaDB, cause " + e.getLocalizedMessage(), e); // NOI18N
                return 5;
            }
        } else {
            LOGGER.log(Level.INFO, "The previous registration found. Links to " + registrationFO.getAttribute(ATTR_DERBY_HOME)); // NOI18N
        }

        return 0;
    }

    public static String getRegisteredDerbyHome () {
        FileObject registrationFO = FileUtil.getConfigFile(DERBY_REGISTRATION_DIR + "/" + DERBY_REGISTRATION_FILE); // NOI18N
        String home = (String) (registrationFO == null ? null : registrationFO.getAttribute(ATTR_DERBY_HOME));
        LOGGER.log(Level.FINE, "Registered home is " + home);
        return home;
    }

}
