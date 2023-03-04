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
package org.netbeans.modules.java.j2sedeploy;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import org.netbeans.api.actions.Closable;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.Parameters;

/**
 *  Java SE Deployment panel Project Properties support
 * 
 * @author Petr Somol
 * @author Tomas Zezula
 * @since java.j2seproject 1.65
 */
public final class J2SEDeployProperties {

    // Deployment - native packaging
    public static final String NATIVE_BUNDLING_ENABLED = "native.bundling.enabled"; //NOI18N
    // copied from JFXProjectProperties
    public static final String JAVAFX_ENABLED = "javafx.enabled"; // NOI18N
    private static final String J2SEDEPLOY_EXTENSION = "j2sedeploy";    //NOI18N
    private static final String[] OLD_J2SEDEPLOY_EXTENSION = new String[0];
    private static final String BUILD_SCRIPT_PROTOTYPE = String.format(
        "%s/resources/build-native-prototype.xml",  //NOI18N
        J2SEDeployProperties.class.getPackage().getName().replace('.','/'));   //NOI18N
    private static final String EXTENSION_BUILD_SCRIPT_PATH = "nbproject/build-native.xml";        //NOI18N

    private static final Logger LOG = Logger.getLogger(J2SEDeployProperties.class.getName());
    
    // Project related references
    private J2SEPropertyEvaluator j2sePropEval;
    private PropertyEvaluator evaluator;
    private Project project;

    public Project getProject() {
        return project;
    }
    public PropertyEvaluator getEvaluator() {
        return evaluator;
    }
    
    boolean nativeBundlingEnabled;
    public boolean getNativeBundlingEnabled() {
        return nativeBundlingEnabled;
    }
    public void setNativeBundlingEnabled(boolean enabled) {
        this.nativeBundlingEnabled = enabled;
    }

    /** Keeps singleton instance of J2SEDeployProperties for SE project with Deployment category open */
    private static Map<String, J2SEDeployProperties> propInstance = new HashMap<String, J2SEDeployProperties>();

    /** Factory method */
    public static J2SEDeployProperties getInstance(Lookup context) {
        Project proj = context.lookup(Project.class);
        String projDir = proj.getProjectDirectory().getPath();
        J2SEDeployProperties prop = propInstance.get(projDir);
        if(prop == null) {
            prop = new J2SEDeployProperties(context);
            propInstance.put(projDir, prop);
        }
        return prop;
    }

    /** Getter method */
    public static J2SEDeployProperties getInstanceIfExists(Project proj) {
        assert proj != null;
        String projDir = proj.getProjectDirectory().getPath();
        J2SEDeployProperties prop = propInstance.get(projDir);
        if(prop != null) {
            return prop;
        }
        return null;
    }

    /** Getter method */
    public static J2SEDeployProperties getInstanceIfExists(Lookup context) {
        Project proj = context.lookup(Project.class);
        return getInstanceIfExists(proj);
    }

    public static void cleanup(Lookup context) {
        Project proj = context.lookup(Project.class);
        String projDir = proj.getProjectDirectory().getPath();
        propInstance.remove(projDir);
    }

    static String getCurrentExtensionName() {
        return J2SEDEPLOY_EXTENSION;
    }

    static String[] getOldExtensionNames() {
        return OLD_J2SEDEPLOY_EXTENSION;
    }

    static void updateJ2SEDeployExtension(@NonNull final Project project) throws IOException {
        Parameters.notNull("project", project); //NOI18N
        final FileObject buildNativeXml = copyBuildNativeTemplate(project);
        assert buildNativeXml != null;
        final AntBuildExtender extender = project.getLookup().lookup(AntBuildExtender.class);
        assert extender != null;
        modifyBuildXml(extender, buildNativeXml);
    }

    static boolean isBuildNativeUpToDate(@NonNull final Project project) {
        Parameters.notNull("project", project); //NOI18N
        final FileObject buildNativeXml = project.getProjectDirectory().getFileObject(EXTENSION_BUILD_SCRIPT_PATH);
        if (buildNativeXml == null) {
            return false;
        }
        try (
            final InputStream buildIn = new BufferedInputStream(buildNativeXml.getInputStream());
            final InputStream protoIn = new BufferedInputStream(J2SEDeployProperties.class.getClassLoader().getResourceAsStream(BUILD_SCRIPT_PROTOTYPE))) {
            final long buildNativeCRC = computeCRC(buildIn);
            final long prototypeCRC = computeCRC(protoIn);
            return buildNativeCRC == prototypeCRC;
        } catch (IOException ioe) {
            LOG.log(
                Level.INFO,
                "Cannot read: {0}", //NOI18N
                FileUtil.getFileDisplayName(buildNativeXml));
            return false;
        }
    }

    static FileObject copyBuildNativeTemplate(@NonNull final Project project) throws IOException {
        Parameters.notNull("project", project); //NOI18N
        final FileObject buildExFoBack = project.getProjectDirectory().getFileObject(String.format(
            "%s~",  //NOI18N
            EXTENSION_BUILD_SCRIPT_PATH));
        if (buildExFoBack != null) {
            closeInEditor(buildExFoBack);
            buildExFoBack.delete();
        }
        FileObject buildExFo = project.getProjectDirectory().getFileObject(EXTENSION_BUILD_SCRIPT_PATH);
        FileLock lock;
        if (buildExFo != null) {
            closeInEditor(buildExFo);
            lock = buildExFo.lock();
            try {
                buildExFo.rename(
                    lock,
                    buildExFo.getName(),
                    String.format(
                        "%s~",  //NOI18N
                        buildExFo.getExt()));
            } finally {
                lock.releaseLock();
            }
        }
        buildExFo = FileUtil.createData(project.getProjectDirectory(), EXTENSION_BUILD_SCRIPT_PATH);
        lock = buildExFo.lock();
        try (final InputStream in = J2SEDeployProperties.class.getClassLoader().getResourceAsStream(BUILD_SCRIPT_PROTOTYPE);
             final OutputStream out = buildExFo.getOutputStream(lock)) {
            FileUtil.copy(in, out);
        } finally {
            lock.releaseLock();
        }
        return buildExFo;
    }

    private static AntBuildExtender.Extension modifyBuildXml(
        @NonNull final AntBuildExtender extender,
        @NonNull final FileObject buildNativeXml) {
        Parameters.notNull("extender", extender);   //NOI18N
        Parameters.notNull("buildNativeXml", buildNativeXml);   //NOI18N
        return extender.addExtension(getCurrentExtensionName(), buildNativeXml);
    }

    private static void closeInEditor(@NonNull final FileObject file) {
        Parameters.notNull("file", file);   //NOI18N
        try {
            final DataObject dobj = DataObject.find(file);
            final Closable closeCookie = dobj.getLookup().lookup(Closable.class);
            if (closeCookie != null) {
                closeCookie.close();
            }
        } catch (DataObjectNotFoundException donfe) {
            LOG.log(
                Level.INFO,
                "Cannot close {0}.", //NOI18N
                FileUtil.getFileDisplayName(file));
        }
    }

    private static long computeCRC(@NonNull final InputStream in) throws IOException {
        final CRC32 crc = new CRC32();
        int last = -1;
        int curr;
        while ((curr = in.read()) != -1) {
            if (curr != '\n' && last == '\r') { //NOI18N
                crc.update('\n');               //NOI18N
            }
            if (curr != '\r') {                 //NOI18N
                crc.update(curr);
            }
            last = curr;
        }
        if (last == '\r') {                     //NOI18N
            crc.update('\n');                   //NOI18N
        }        
        return crc.getValue();
    }

    /** Creates a new instance of J2SEDeployProperties */
    private J2SEDeployProperties(Lookup context) {       
        project = context.lookup(Project.class);
        if (project != null) {
            j2sePropEval = project.getLookup().lookup(J2SEPropertyEvaluator.class);
            evaluator = j2sePropEval.evaluator();
            nativeBundlingEnabled = isTrue(evaluator.getProperty(NATIVE_BUNDLING_ENABLED));
        }
    }
    
    private static void setOrRemove(@NonNull org.netbeans.spi.project.support.ant.EditableProperties props, @NonNull String name, String value) {
        if (value != null) {
            props.setProperty(name, value);
        } else {
            props.remove(name);
        }
    }
  
    public void store() throws IOException {
        final EditableProperties ep = new EditableProperties(true);
        final FileObject projPropsFO = project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws Exception {
                    try (final InputStream is = projPropsFO.getInputStream()) {
                        ep.load(is);
                    }
                    setOrRemove(ep, NATIVE_BUNDLING_ENABLED, nativeBundlingEnabled ? "true" : null); //NOI18N
                    FileLock lock = projPropsFO.lock();
                    try (OutputStream os = projPropsFO.getOutputStream(lock)) {
                        ep.store(os);
                    } finally {
                        lock.releaseLock();
                    }
                    final AntBuildExtender extender = project.getLookup().lookup(AntBuildExtender.class);
                    if (extender != null) {
                        AntBuildExtender.Extension extension = extender.getExtension(getCurrentExtensionName());
                        if (nativeBundlingEnabled) {
                            if (extension == null) {
                                final FileObject buildExFo = copyBuildNativeTemplate(project);
                                extension = modifyBuildXml(extender, buildExFo);
                            }
                        } else {
                            if (extension != null) {
                                extender.removeExtension(getCurrentExtensionName());
                            }
                            final FileObject buildExFo = project.getProjectDirectory().getFileObject(EXTENSION_BUILD_SCRIPT_PATH);
                            if (buildExFo != null) {
                                closeInEditor(buildExFo);
                                buildExFo.delete();
                            }
                        }
                    }
                    return null;
                }
            });
        } catch (MutexException mux) {
            throw (IOException) mux.getException();
        }
    }
    
    public static boolean isTrue(final String value) {
        return value != null &&
                (value.equalsIgnoreCase("true") ||  //NOI18N
                 value.equalsIgnoreCase("yes") ||   //NOI18N
                 value.equalsIgnoreCase("on"));     //NOI18N
    }

    public static boolean isNonEmpty(String s) {
        return s != null && !s.isEmpty();
    }
            
    public static boolean isEqual(final String s1, final String s2) {
        return (s1 == null && s2 == null) ||
                (s1 != null && s2 != null && s1.equals(s2));
    }                                   

    public static boolean isEqualIgnoreCase(final String s1, final String s2) {
        return (s1 == null && s2 == null) ||
                (s1 != null && s2 != null && s1.equalsIgnoreCase(s2));
    }                                   

    public static boolean isEqualText(final String s1, final String s2) {
        return ((s1 == null || s1.isEmpty()) && (s2 == null || s2.isEmpty())) ||
                (s1 != null && s2 != null && s1.equals(s2));
    }                                   

}
