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
package org.netbeans.modules.java.j2seembedded.project;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.java.j2seembedded.platform.RemotePlatform;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.cookies.CloseCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
final class Utilities {

    private static final String TARGET_RUN = "$target.run";             //NOI18N
    private static final String TARGET_DEBUG = "$target.debug";         //NOI18N
    private static final String TARGET_PROFILE = "$target.profile";         //NOI18N
    private static final String COS_DISABLE = "compile.on.save.unsupported.remote.platform"; //NOI18N
    private static final String DEBUG_TRANSPORT = "debug-transport";        //NOI18N
    private static final String EXTENSION_NAME = "remote-platform-1";       //NOI18N
    private static final String[] OLD_EXTENSION_NAMES = {};
    private static final String BUILD_SCRIPT_PATH = "nbproject/remote-platform-impl.xml";   //NOI18N
    private static final String BUILD_SCRIPT_BACK_UP = "remote-platform-impl_backup";   //NOI18N
    private static final String BUILD_SCRIPT_PROTOTYPE = "/org/netbeans/modules/java/j2seembedded/resources/remote-platform-impl.xml";  //NOI18N
    private static final Map<String,String> CONFIG_PROPERTIES;
    private static final Set<String> REMOVE_CONFIG_PROPERTIES = Collections.unmodifiableSet(
        new HashSet<String>(Arrays.asList(new String[] {TARGET_RUN, TARGET_DEBUG, TARGET_PROFILE})));

    private static final String PLATFORM_RUNTIME = "platform.runtime"; //NOI18N

    static {
        Map<String,String> m = new HashMap<>();
        m.put(TARGET_RUN,"run-remote");     //NOI18N
        m.put(TARGET_DEBUG,"debug-remote");     //NOI18N
        m.put(TARGET_PROFILE,"profile-remote"); //NOI18N
        m.put(COS_DISABLE, Boolean.TRUE.toString());
        m.put(DEBUG_TRANSPORT,"dt_socket"); //NOI18N
        CONFIG_PROPERTIES = Collections.unmodifiableMap(m);
    }

    private static final Logger LOG = Logger.getLogger(Utilities.class.getName());

    private static volatile Long templateCRCCache;

    private Utilities() {
        throw new IllegalStateException();
    }

    static final class UpdateConfigResult {

        private final Collection<String> updated;
        private final Collection<String> upToDate;


        private UpdateConfigResult(
            @NonNull final Collection<String> updated,
            @NonNull final Collection<String> upToDate) {
            Parameters.notNull("updated", updated); //NOI18N
            Parameters.notNull("upToDate", upToDate); //NOI18N
            this.updated = Collections.unmodifiableCollection(updated);
            this.upToDate = Collections.unmodifiableCollection(upToDate);
        }

        @NonNull
        Collection<String> getUpdatedConfigs() {
            return updated;
        }

        @NonNull
        Collection<String> getUpToDateConfigs() {
            return upToDate;
        }

        boolean hasRemotePlatform() {
            return !updated.isEmpty() || !upToDate.isEmpty();
        }
    };

    @CheckForNull
    static RemotePlatform getRemotePlatform(@NonNull final Project prj) {
        final PropertyEvaluator eval = prj.getLookup().lookup(J2SEPropertyEvaluator.class).evaluator();
        final String rpid = eval.getProperty(PLATFORM_RUNTIME);
        if (rpid == null || rpid.isEmpty()) {
            return null;
        }
        return findRemotePlatform(rpid);
    }


    static boolean hasRemotePlatform(@NonNull final Project prj) {
        final PropertyEvaluator eval = prj.getLookup().lookup(J2SEPropertyEvaluator.class).evaluator();
        final String rpid = eval.getProperty(PLATFORM_RUNTIME);
        if (rpid != null && !rpid.isEmpty()) {
            if (findRemotePlatform(rpid) != null) {
                return true;
            }
        }
        return false;
    }

    static UpdateConfigResult updateRemotePlatformConfigurations(@NonNull final Project prj) throws IOException {
        try {
            return ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<UpdateConfigResult>() {
                @Override
                public UpdateConfigResult run() throws Exception {
                    final Set<String> updated = new HashSet<>();
                    final Set<String> upToDate = new HashSet<>();
                    final FileObject prjDir = prj.getProjectDirectory();
                    if (prjDir != null) {
                        final FileObject cfgFolder = prjDir.getFileObject("nbproject/configs"); //NOI18N
                        if (cfgFolder != null) {
                            for (FileObject cfgFile : cfgFolder.getChildren()) {
                                if (!cfgFile.hasExt("properties")) {    //NOI18N
                                    continue;
                                }
                                final String relPath = FileUtil.getRelativePath(prjDir, cfgFile);
                                if (relPath != null) {
                                    final EditableProperties ep = new EditableProperties(true);
                                    try (final InputStream in = cfgFile.getInputStream()){
                                        ep.load(in);
                                    }
                                    final String runtimePlatform = ep.getProperty(PLATFORM_RUNTIME);
                                    if (runtimePlatform != null && !runtimePlatform.isEmpty()) {
                                        if (configAlreadyUpdated(ep, true)) {
                                            upToDate.add(relPath);
                                        } else {
                                            extendConfig(ep);
                                            final FileLock lock = cfgFile.lock();
                                            try (final OutputStream out = cfgFile.getOutputStream(lock)) {
                                                ep.store(out);
                                            } finally {
                                                lock.releaseLock();
                                            }
                                            updated.add(relPath);
                                        }
                                    } else if (configAlreadyUpdated(ep, false)) {
                                        clearConfig(ep);
                                        final FileLock lock = cfgFile.lock();
                                        try (final OutputStream out = cfgFile.getOutputStream(lock)) {
                                            ep.store(out);
                                        } finally {
                                            lock.releaseLock();
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return new UpdateConfigResult(updated, upToDate);
                }
            });
        } catch (MutexException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw new IOException(e);
            }
        }
    }

    static boolean  hasRemoteExtension(@NonNull final Project project) {
        final AntBuildExtender extender = project.getLookup().lookup(AntBuildExtender.class);
        if (extender == null) {
            return false;
        }
        return extender.getExtension(EXTENSION_NAME) != null;
    }

    static boolean removeOldRemoteExtensions(@NonNull final Project project) {
        Parameters.notNull("project", project); //NOI18N
        boolean result = false;
        final AntBuildExtender extender = project.getLookup().lookup(AntBuildExtender.class);
        if (extender != null) {
            for (String oldExtensionName : OLD_EXTENSION_NAMES) {
                if (extender.getExtension(oldExtensionName) != null) {
                    extender.removeExtension(oldExtensionName);
                    result = true;
                }
            }
        }
        return result;
    }

    static void addRemoteExtension(@NonNull final Project project) throws IOException {
        final AntBuildExtender extender = project.getLookup().lookup(AntBuildExtender.class);
        if (extender == null) {
            LOG.log(
                Level.WARNING,
                "The project {0} ({1}) does not support AntBuildExtender.",     //NOI18N
                new Object[] {
                    ProjectUtils.getInformation(project).getDisplayName(),
                    FileUtil.getFileDisplayName(project.getProjectDirectory())
                });
            return;
        }
        final FileObject rpBuildScript = copyBuildScript(project);
        extender.addExtension(EXTENSION_NAME, rpBuildScript);
    }

    @NonNull
    static FileObject copyBuildScript (@NonNull final Project project) throws IOException {
        final FileObject projDir = project.getProjectDirectory();
        FileObject rpBuildScript = projDir.getFileObject(BUILD_SCRIPT_PATH);
        if (rpBuildScript != null && !isBuildScriptUpToDate(project)) {
            // try to close the file just in case the file is already opened in editor
            DataObject dobj = DataObject.find(rpBuildScript);
            CloseCookie closeCookie = dobj.getLookup().lookup(CloseCookie.class);
            if (closeCookie != null) {
                closeCookie.close();
            }
            final FileObject nbproject = projDir.getFileObject("nbproject");                    //NOI18N
            final FileObject backupFile = nbproject.getFileObject(BUILD_SCRIPT_BACK_UP, "xml"); //NOI18N
            if (backupFile != null) {
                backupFile.delete();
            }
            FileUtil.moveFile(rpBuildScript, nbproject, BUILD_SCRIPT_BACK_UP);
            rpBuildScript = null;
        }
        if (rpBuildScript == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(
                    Level.FINE,
                    "Updating remote build script in project {0} ({1})", //NOI18N
                    new Object[]{
                        ProjectUtils.getInformation(project).getDisplayName(),
                        FileUtil.getFileDisplayName(projDir)
                    });
            }
            rpBuildScript = FileUtil.createData(project.getProjectDirectory(), BUILD_SCRIPT_PATH);
            try(
                final InputStream in = new BufferedInputStream(RemotePlatformProjectSaver.class.getResourceAsStream(BUILD_SCRIPT_PROTOTYPE));
                final OutputStream out = new BufferedOutputStream(rpBuildScript.getOutputStream())) {
                FileUtil.copy(in, out);
            }
        }
        return rpBuildScript;
    }
    
    protected static String getTargetOSForRP(
            @NonNull final String os,
            @NonNull final String arch,
            @NullAllowed final String abi,
            @NonNull final String vmName) {
        String targetOS;
        if (os.toLowerCase().contains("win")) { //NOI18N
            targetOS="win"; //NOI18N
            if (arch.toLowerCase().contains("64")) { //NOI18N
                targetOS += "amd64-15"; //NOI18N
            } else if (arch.toLowerCase().contains("86")) { //NOI18N
                targetOS +="-15"; //NOI18N
            } else if ("CVM".equals(vmName)) { //NOI18N
                targetOS+="cvm"; //NOI18N
            }
        } else if (os.toLowerCase().contains("linux")) { //NOI18N
            targetOS="linux"; //NOI18N
            if (arch.toLowerCase().contains("arm")) { //NOI18N
                targetOS+="arm"; //NOI18N
                if (abi != null && abi.toLowerCase().contains("abihf")) { //NOI18N
                    targetOS+="vfphflt"; //NOI18N
                }
                targetOS += "-15"; //NOI18N
            } else if (arch.toLowerCase().contains("64")) { //NOI18N
                targetOS += "amd64-15"; //NOI18N
            } else if (arch.toLowerCase().contains("86")) { //NOI18N
                targetOS +="-15"; //NOI18N
            } else if ("CVM".equals(vmName)) { //NOI18N
                targetOS+="cvm"; //NOI18N
            }
        } else if (os.toLowerCase().contains("sol")) { //NOI18N
            targetOS="sol"; //NOI18N
            if (arch.toLowerCase().contains("sparc")) { //NOI18N
                targetOS+="sparc"; //NOI18N
                if (arch.toLowerCase().contains("v9")) { //NOI18N
                 targetOS+="v9"; //NOI18N
                }
            } else if (arch.toLowerCase().contains("64")) { //NOI18N
                targetOS+="amd64"; //NOI18N
            } else if (arch.toLowerCase().contains("86")) { //NOI18N
                targetOS+="x86"; //NOI18N
            }
            targetOS+="-15"; //NOI18N
        } else if (os.toLowerCase().contains("mac")) { //NOI18N
            targetOS="mac-15"; //NOI18N
        } else {
            targetOS=""; //NOI18N
        }
        return targetOS;
    }


    private static RemotePlatform findRemotePlatform(@NonNull final String platformId) {
        Parameters.notNull("platformId", platformId);   //NOI18N
        final JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms(
                null,
                new Specification(RemotePlatform.SPEC_NAME, null));
        for (JavaPlatform platform : platforms) {
            final String antPlatformName = platform.getProperties().get(RemotePlatform.PLAT_PROP_ANT_NAME);
            if (platformId.equals(antPlatformName) && (platform instanceof RemotePlatform)) {
                return (RemotePlatform) platform;
            }
        }
        return null;
    }

    private static boolean isBuildScriptUpToDate(@NonNull final Project project) {
        final FileObject prjDir = project.getProjectDirectory();
        if (prjDir == null) {
            return false;
        }
        final FileObject remoteBuildScript = prjDir.getFileObject(BUILD_SCRIPT_PATH);
        if (remoteBuildScript == null) {
            return false;
        }
        try {
            final long scriptCRC;
            try (final InputStream in = new BufferedInputStream(remoteBuildScript.getInputStream())) {
                scriptCRC = calculateCRC(in);
            }
            Long templateCRC = templateCRCCache;
            if (templateCRC == null) {
                try (final InputStream in = new BufferedInputStream(
                        RemotePlatformProjectSaver.class.getResourceAsStream(BUILD_SCRIPT_PROTOTYPE))) {
                    templateCRCCache = templateCRC = calculateCRC(in);
                }
            }
            return scriptCRC == templateCRC;
        } catch (IOException ioe) {
            return false;
        }
    }

    private static long calculateCRC(@NonNull final InputStream in) throws IOException {
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

    private static boolean configAlreadyUpdated(
        @NonNull final EditableProperties props,
        final boolean fullCheck) {
        for (Map.Entry<String,String> e : CONFIG_PROPERTIES.entrySet()) {
            if (fullCheck || REMOVE_CONFIG_PROPERTIES.contains(e.getKey())) {
                if (!e.getValue().equals(props.get(e.getKey()))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void extendConfig(@NonNull final EditableProperties props) {
        for (Map.Entry<String,String> e : CONFIG_PROPERTIES.entrySet()) {
            props.setProperty(e.getKey(), e.getValue());
        }
    }

    private static void clearConfig(@NonNull final EditableProperties props) {
        for (String key : REMOVE_CONFIG_PROPERTIES) {
            props.remove(key);
        }
    }

}
