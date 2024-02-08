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
import java.nio.file.Files;
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
import org.openide.modules.Places;
import org.openide.util.Utilities;

/**
 * Rather dummy implementation of the Java Platform, but sufficient for communication
 * inside the Wizard.
 */
public final class NewJ2SEPlatform extends J2SEPlatformImpl implements Runnable {

    public static final String DISPLAY_NAME_FILE_ATTR = "J2SEPlatform.displayName";

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
            FileObject javac = findTool("javac"); //NOI18N
            if (javac == null) {
                return;
            }
            File javacFile = FileUtil.toFile (javac);
            if (javacFile == null) {
                return;
            }
            String javacpath = javacFile.getAbsolutePath();
            String filePath = Files.createTempFile("nb-platformdetect", "properties").toFile().getAbsolutePath(); //NOI18N
            final String probePath = getSDKProperties(javapath, javacpath, filePath);
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

    private String getSDKProperties(String javaPath, String javacPath, String path) throws IOException {
        try {
            String probeDir = Places.getCacheSubdirectory("j2seplatform/probe").getAbsolutePath();
            File probeSrc = InstalledFileLocator.getDefault().locate("scripts/J2SEPlatformProbe.java", "org.netbeans.modules.java.j2seplatform", false);
            Process compile = new ProcessBuilder(javacPath,
                                                "-d",
                                                probeDir,
                                                probeSrc.getAbsolutePath())
                              .inheritIO()
                              .start();
            compile.waitFor();
            int compileExitValue = compile.exitValue();
            if (compileExitValue != 0) {
                throw new IOException(String.format("javac process exit code: %d", compileExitValue));  //NOI18N
            }
            Process probe = new ProcessBuilder(javaPath,
                                               "-cp",
                                               probeDir,
                                               "J2SEPlatformProbe",
                                               path)
                            .inheritIO()
                            .start();
            // PENDING -- this may be better done by using ExecEngine, since
            // it produces a cancellable task.
            probe.waitFor();
            int probeExitValue = probe.exitValue();
            if (probeExitValue != 0) {
                throw new IOException(String.format("Java process exit code: %d", probeExitValue));  //NOI18N
            }
            return probeDir;
        } catch (InterruptedException ex) {
            IOException e = new IOException(ex);
            throw e;
        }
    }
}
