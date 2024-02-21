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

package org.netbeans.modules.debugger.jpda.truffle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.extexecution.base.ProcessBuilder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Get the MIME types of languages installed in the Truffle/GraalVM platform.
 */
public final class MIMETypes {

    private static final Logger LOG = Logger.getLogger(MIMETypes.class.getName());
    
    public static final String PROP_MIME_TYPES = "MIME types";                  // NOI18N
    
    private static final String MIME_TYPES_MAIN = "org.netbeans.modules.debugger.jpda.backend.truffle.GetMIMETypes";    // NOI18N
    private static final MIMETypes INSTANCE = new MIMETypes();
    private static String TEMP_TRUFFLE_JAR;

    private final Map<JavaPlatform, Set<String>> platformMIMETypes = new WeakHashMap<>();
    private Set<String> allPlatformsMIMETypes;
    private PropertyChangeListener allPlatformsListener;
    
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private MIMETypes() {
    }
    
    public static MIMETypes getDefault() {
        return INSTANCE;
    }
    
    public Set<String> get(Project prj) {
        JavaPlatform jp = getProjectPlatform(prj);
        if (jp == null) {
            return Collections.emptySet();
        }
        return get(jp);
    }
    
    private synchronized Set<String> get(JavaPlatform jp) {
        Set<String> mTypes = platformMIMETypes.get(jp);
        if (mTypes == null) {
            FileObject graalvm = jp.findTool("polyglot");                       // NOI18N
            FileObject java = jp.findTool("java");                              // NOI18N
            if (graalvm != null && java != null) {
                File javaFile = FileUtil.toFile(java);
                if (javaFile != null) {
                    ProcessBuilder pb = ProcessBuilder.getLocal();
                    pb.setExecutable(javaFile.getAbsolutePath());
                    try {
                        pb.setArguments(Arrays.asList("-cp", getTruffleJarPath(), MIME_TYPES_MAIN));   // NOI18N
                        Process proc = pb.call();
                        try (BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                            mTypes = new HashSet<>();
                            String line;
                            while ((line = r.readLine()) != null) {
                                mTypes.add(line);
                            }
                        }
                        try (BufferedReader r = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {
                            String line;
                            while ((line = r.readLine()) != null) {
                                LOG.info("Error from "+javaFile+" : "+line);
                            }
                        }
                        LOG.log(Level.FINE, "MIME types of {0} are: {1}", new Object[]{jp, mTypes});
                    } catch (IOException ioex) {
                        LOG.log(Level.CONFIG, "", ioex);
                    }
                }
            }
            if (mTypes == null) {
                mTypes = Collections.emptySet();
            }
            platformMIMETypes.put(jp, mTypes);
        }
        return mTypes;
    }
    
    private static synchronized String getTruffleJarPath() throws IOException {
        if (TEMP_TRUFFLE_JAR == null) {
            File truffleJarFile = Files.createTempFile("TmpTruffleBcknd", ".jar").toFile();   // NOI18N
            truffleJarFile.deleteOnExit();
            FileUtil.copy(RemoteServices.openRemoteClasses(), new FileOutputStream(truffleJarFile));
            TEMP_TRUFFLE_JAR = truffleJarFile.getAbsolutePath();
        }
        return TEMP_TRUFFLE_JAR;
    }
    
    /**
     * Get MIME types based on registered Java platforms.
     * The call returns either a cached set, or queries the platforms.
     * 
     * @return a set of MIME types.
     */
    public synchronized Set<String> get() {
        if (allPlatformsMIMETypes != null) {
            return allPlatformsMIMETypes;
        }
        JavaPlatformManager pm = JavaPlatformManager.getDefault();
        if (allPlatformsListener == null) {
            allPlatformsListener = new PropertyChangeListener() {
                @Override public void propertyChange(PropertyChangeEvent evt) {
                    synchronized (MIMETypes.this) {
                        allPlatformsMIMETypes = null;
                    }
                    pcs.firePropertyChange(PROP_MIME_TYPES, null, null);
                }
            };
            pm.addPropertyChangeListener(allPlatformsListener);
        }
        JavaPlatform[] installedPlatforms = pm.getPlatforms(null, new Specification ("j2se", null));   //NOI18N
        Set<String> mTypes = new HashSet<>();
        for (int i = 0; i < installedPlatforms.length; i++) {
            mTypes.addAll(get(installedPlatforms[i]));
        }
        allPlatformsMIMETypes = mTypes;
        return mTypes;
    }
    
    /**
     * Get cached MIME types based on registered Java platforms.
     * @return a cached set, or <code>null</code>.
     */
    public synchronized Set<String> getCached() {
        return allPlatformsMIMETypes;
    }
    
    private static JavaPlatform getProjectPlatform(Project prj) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(prj).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath bootClassPath = ClassPath.getClassPath(sourceGroups[0].getRootFolder(), ClassPath.BOOT);
        FileObject[] prjBootRoots = bootClassPath.getRoots();
        JavaPlatformManager pm = JavaPlatformManager.getDefault();
        JavaPlatform[] installedPlatforms = pm.getPlatforms(null, new Specification ("j2se", null));   //NOI18N
        for (int i = 0; i < installedPlatforms.length; i++) {
            ClassPath bootstrapLibraries = installedPlatforms[i].getBootstrapLibraries();
            if (Arrays.equals(prjBootRoots, bootstrapLibraries.getRoots())) {
                return installedPlatforms[i];
            }
        }
        return null;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
}
