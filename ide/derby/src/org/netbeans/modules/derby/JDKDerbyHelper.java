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

package org.netbeans.modules.derby;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Utilities;

/**
 *
 * @author Andrei Badea, Jiri Rechtacek
 */
public class JDKDerbyHelper {

    private static final Logger LOGGER = Logger.getLogger(JDKDerbyHelper.class.getName());
    private static final JDKDerbyHelper INSTANCE = new JDKDerbyHelper();
    private final String javaHome;
    private final String javaVersion;

    public static JDKDerbyHelper forDefault() {
        return INSTANCE;
    }

    private JDKDerbyHelper() {
        this.javaHome = System.getProperty("java.home");
        this.javaVersion = System.getProperty("java.version");
    }

    public boolean canBundleDerby() {
        // IMHO, most JDK1.6 contains Java DB
        return true;
    }

    public String findDerbyLocation() {
        if (!canBundleDerby()) {
            return null;
        }
        // find in registration by NBI
        String derbyHome = DerbyRegistration.getRegisteredDerbyHome();
        if (derbyHome != null) {
            LOGGER.log(Level.FINE, "Registered JavaDB:  " + derbyHome);
            File derbyHomeFile = new File(derbyHome);
            String result = testDerbyInstallLocation(derbyHomeFile);
            if (result != null) {
                return result;
            }
        }

        // find in JDK
        File locInJDK = getLocationInJDK(javaHome);
        if (locInJDK != null) {
            LOGGER.log(Level.FINE, "JavaDB in JDK(" + javaVersion + "):  " + locInJDK);
            String result = testDerbyInstallLocation(locInJDK);
            if (result != null) {
                return result;
            }
        }
        // see issue 83144
        if (Utilities.isWindows()) {
            LOGGER.log(Level.FINE, "Operating system: Windows");
            String programFilesPath = System.getenv("ProgramFiles"); // NOI18N
            LOGGER.log(Level.FINE, "Program Files path: {0}", programFilesPath);
            if (programFilesPath != null) {
                File derbyDirFile = new File(programFilesPath, "Sun/JavaDB"); // NOI18N
                String result = testDerbyInstallLocation(derbyDirFile);
                if (result != null) {
                    return result;
                }
            }
        }
        if (Utilities.isUnix()) {
            LOGGER.log(Level.FINE, "Operating system: Unix");
            String result = testDerbyInstallLocation(new File("/opt/SUNWjavadb")); // NOI18N
            if (result != null) {
                return result;
            }
            result = testDerbyInstallLocation(new File("/opt/sun/javadb")); // NOI18N
            if (result != null) {
                return result;
            }
            result = testDerbyInstallLocation(new File("/usr/share/javadb")); // NOI18N
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private static File getLocationInJDK(String javaHome) {
        File dir = new File(javaHome);
        assert dir != null && dir.exists() && dir.isDirectory() : "java.home is directory";
        // path to JavaDB in JDK6
        File loc = new File(dir.getParentFile(), "db"); // NOI18N
        return loc != null && loc.exists() && loc.isDirectory() ? loc : null;
    }

    private static String testDerbyInstallLocation(File directory) {
        LOGGER.log(Level.FINE, "Testing directory: {0}", directory);
        if (Util.isDerbyInstallLocation(directory)) {
            return directory.getAbsolutePath();
        }
        return null;
    }
}
