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

package org.netbeans.modules.groovy.grails;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Hejl
 */
public final class RuntimeHelper {

    public static final String GRAILS_HOME_PROPERTY = "GRAILS_HOME"; // NOI18N

    public static final String WIN_EXECUTABLE_FILE = "grails.bat"; // NOI18N

    public static final String WIN_EXECUTABLE = "\\bin\\" + WIN_EXECUTABLE_FILE; // NOI18N

    public static final String WIN_DEBUG_EXECUTABLE = "\\bin\\grails-debug.bat"; // NOI18N

    public static final String NIX_EXECUTABLE_FILE = "grails"; // NOI18N

    public static final String NIX_EXECUTABLE = "/bin/" + NIX_EXECUTABLE_FILE; // NOI18N

    public static final String NIX_DEBUG_EXECUTABLE = "/bin/grails-debug"; // NOI18N

    public static final String DEB_EXECUTABLE = "/usr/bin/" + NIX_EXECUTABLE_FILE; // NOI18N

    public static final String DEB_DEBUG_EXECUTABLE = "/usr/bin/grails-debug"; // NOI18N

    public static final String DEB_START_FILE = "/bin/startGrails"; // NOI18N

    public static final String DEB_LOCATION = "/usr/share/grails"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(RuntimeHelper.class.getName());

    private RuntimeHelper() {
        super();
    }

    public static boolean isValidRuntime(File grailsBase) {
        String pathToBinary = Utilities.isWindows() ? WIN_EXECUTABLE : NIX_EXECUTABLE;
        return new File(grailsBase, pathToBinary).isFile() || isDebian(grailsBase);
// leave following for future
//                && new File(grailsBase, "dist").isDirectory() // NOI18N
//                && new File(grailsBase, "lib").isDirectory() // NOI18N
//                && new File(grailsBase, "build.properties").isFile(); // NOI18N
    }

    public static boolean isDebian(File grailsBase) {
        return new File(grailsBase, DEB_START_FILE).isFile()
                && new File(DEB_EXECUTABLE).isFile()
                && FileUtil.normalizeFile(grailsBase).equals(new File(DEB_LOCATION));
    }

    public static File getGrailsExecutable(File grailsBase, boolean debug) {
        assert grailsBase != null;
        if (grailsBase == null) {
            return null;
        }

        File grailsExecutable = null;
        if (debug) {
            if (Utilities.isWindows()) {
              grailsExecutable = new File(grailsBase, RuntimeHelper.WIN_DEBUG_EXECUTABLE);
            } else {
                if (RuntimeHelper.isDebian(grailsBase)) {
                    grailsExecutable = new File(RuntimeHelper.DEB_DEBUG_EXECUTABLE);
                } else {
                    grailsExecutable = new File(grailsBase, RuntimeHelper.NIX_DEBUG_EXECUTABLE);
                }
            }
        } else {
            if (Utilities.isWindows()) {
              grailsExecutable = new File(grailsBase, RuntimeHelper.WIN_EXECUTABLE);
            } else {
                if (RuntimeHelper.isDebian(grailsBase)) {
                    grailsExecutable = new File(RuntimeHelper.DEB_EXECUTABLE);
                } else {
                    grailsExecutable = new File(grailsBase, RuntimeHelper.NIX_EXECUTABLE);
                }
            }
        }
        return grailsExecutable;
    }

    public static String getRuntimeVersion(File grailsBase) {
        if (!isValidRuntime(grailsBase)) {
            return null;
        }
        
        String version = getRuntimeVersionFromProperties(grailsBase);
        
        if (version == null) {
            version = getRuntimeVersionFromInvocation(grailsBase);
        }

        return version;
    }
    
    /**
     * This method gets the grails runtime version reading the build.properties
     * file.
     * This approach for getting the runtime version works with grails versions
     * 1 and 2 but not with version 3.
     * 
     * @param grailsBase The base grails installation directory
     * @return the version
     */
    private static String getRuntimeVersionFromProperties(File grailsBase) {
        Properties props = new Properties();
        try {
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(
                    new File(grailsBase, "build.properties"))); // NOI18N
            try {
                props.load(is);
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return null;
        }
        
        return props.getProperty("grails.version"); // NOI18N
    }
    
    /**
     * This method gets the grails runtime version invoking the grails command.
     * This approach for getting the runtime version works with grails versions
     * 2 and 3 but not with version 1.
     * 
     * @param grailsBase The base grails installation directory
     * @return the version
     */
    private static String getRuntimeVersionFromInvocation(File grailsBase) {
        
        File grailsExecutable = RuntimeHelper.getGrailsExecutable(
                grailsBase, false);
        
        NbProcessDescriptor grailsProcessDesc = new NbProcessDescriptor(
                    grailsExecutable.getAbsolutePath(), "--version"); // NOI18N
        
        String[] envp = new String[] {
                "GRAILS_HOME=" + grailsBase.getAbsolutePath(), // NOI18N
                "JAVA_HOME=" + System.getProperty("java.home") // NOI18N                
            };
        
        Process process = null;
        String preProcessUUID = UUID.randomUUID().toString();
        try {
            process = new WrapperProcess(
                    grailsProcessDesc.exec(null, envp, true, grailsBase),
                    preProcessUUID);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return null;
        }
        
        try {
            process.waitFor();
            InputStream stdout = process.getInputStream();
            BufferedReader reader = new BufferedReader (new InputStreamReader(stdout));

            String line;
            while ( (line = reader.readLine() ) != null) {
                if (line.toLowerCase().contains("grails version:")) { // NOI18N
                    String version = line.split(":")[1]; // NOI18N
                    return version.trim();
                }
            }
        } catch (InterruptedException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }       
        
        return null;
    }

}
