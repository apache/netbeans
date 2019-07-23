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

package org.netbeans.modules.java.j2seplatform.wizard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.java.j2seplatform.platformdefinition.J2SEPlatformImpl;
import org.netbeans.modules.java.j2seplatform.platformdefinition.PlatformConvertor;
import org.netbeans.modules.java.j2seplatform.platformdefinition.Util;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;

/**
 * Rather dummy implementation of the Java Platform, but sufficient for communication
 * inside the Wizard.
 */
public final class NewJ2SEPlatform extends J2SEPlatformImpl implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(NewJ2SEPlatform.class.getName());

    private boolean valid;

    public static NewJ2SEPlatform create (FileObject installFolder) throws IOException {
        assert installFolder != null;
        Map<String,String> platformProperties = new HashMap<> ();
        return new NewJ2SEPlatform (null,Collections.singletonList(installFolder.toURL()),platformProperties,Collections.<String,String>emptyMap());
    }

    private NewJ2SEPlatform (String name, List<URL> installFolders, Map<String,String> platformProperties, Map<String,String> systemProperties) {
        super(name, name, installFolders, platformProperties, systemProperties,null,null);
    }

    public boolean isValid () {
        return this.valid;
    }

    /**
     * Actually performs the detection and stores relevant information
     * in this Iterator
     */
    @Override
    public void run() {
        try {
            //Verify all needed tools
            for (String toolName : PlatformConvertor.IMPORTANT_TOOLS) {
                if (findTool(toolName) == null) {
                    return;
                }
            }
            FileObject java = findTool("java"); //NOI18N
            if (java == null) {
                return;
            }
            File javaFile = FileUtil.toFile (java);
            if (javaFile == null) {
                return;
            }
            String javapath = javaFile.getAbsolutePath();
            String filePath = File.createTempFile("nb-platformdetect", "properties").getAbsolutePath(); //NOI18N
            final String probePath = getSDKProperties(javapath, filePath);
            File f = new File(filePath);
            Properties p = new Properties();
            try (InputStream is = new FileInputStream(f)) {
                p.load(is);
            }
            final Collection<? extends FileObject> instFolders = getInstallFolders();
            final Map<String,String> m = new HashMap<>(p.size());
            for (Enumeration en = p.keys(); en.hasMoreElements(); ) {
                String k = (String)en.nextElement();
                String v = p.getProperty(k);
                if (J2SEPlatformImpl.SYSPROP_USER_DIR.equals(k)) {
                    v = ""; //NOI18N
                }
                v = Util.fixSymLinks (k,v, instFolders);
                m.put(k, v);
            }
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.log(
                        Level.FINEST,
                        "Platform properties: {0}", //NOI18N
                        m);
            }
            this.setSystemProperties(Util.filterProbe(m, probePath));
            this.valid = true;
            f.delete();
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Cannot execute probe process", ex);
            this.valid = false;
        }
    }

    private String getSDKProperties(String javaPath, String path) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        try {
            String[] command = new String[5];
            command[0] = javaPath;
            command[1] = "-classpath";    //NOI18N
            command[2] = InstalledFileLocator.getDefault().locate("modules/ext/org-netbeans-modules-java-j2seplatform-probe.jar", "org.netbeans.modules.java.j2seplatform", false).getAbsolutePath(); // NOI18N
            command[3] = "org.netbeans.modules.java.j2seplatform.wizard.SDKProbe";
            command[4] = path;
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("Executing: %s %s %s %s %s", command[0],command[1],command[2],command[3],command[4]));
            }
            final Process process = runtime.exec(command);
            // PENDING -- this may be better done by using ExecEngine, since
            // it produces a cancellable task.
            process.waitFor();
            int exitValue = process.exitValue();
            if (exitValue != 0) {
                throw new IOException(String.format("Java process exit code: %d", exitValue));  //NOI18N
            }
            return command[2];
        } catch (InterruptedException ex) {
            IOException e = new IOException(ex);
            throw e;
        }
    }
}
