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
package org.netbeans.modules.j2ee.weblogic9.deploy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import org.netbeans.modules.j2ee.core.api.support.progress.ProgressSupport;
import org.netbeans.modules.j2ee.core.api.support.progress.ProgressSupport.Context;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentFactory;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.WLProductProperties;
import org.netbeans.modules.j2ee.weblogic9.j2ee.WLJ2eePlatformFactory;
import org.netbeans.modules.weblogic.common.api.WebLogicLayout;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public final class WLJpa2SwitchSupport {

    private static final Version SWITCH_SUPPORTED_VERSION_MIN =
            Version.fromJsr277NotationWithFallback("10.3.4"); // NOI18N

    private static final String OEPE_CONTRIBUTIONS_JAR = "oepe-contributions.jar"; // NO18N

    private static final Pattern JPA_JAR_1_PATTERN = Pattern.compile("^.*javax\\.persistence.*(_2-\\d+(-\\d+)?)\\.jar$"); // NO18N

    private static final String JPA_JAR1_FALLBACK = "javax.persistence_1.0.0.0_2-0-0.jar"; // NOI18N

    private static final Pattern JPA_JAR_2_PATTERN = Pattern.compile("^.*com\\.oracle\\.jpa2support.*\\.jar$"); // NO18N

    private static final String JPA_JAR2_FALLBACK = "com.oracle.jpa2support_1.0.0.0_2-0.jar"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(WLJpa2SwitchSupport.class.getName());

    private final File serverRoot;

    private final WLDeploymentManager dm;

    /** GuardedBy("this")*/
    private Version serverVersion;

    public WLJpa2SwitchSupport(File serverRoot) {
        this.dm = null;
        this.serverRoot = serverRoot;
    }

    public WLJpa2SwitchSupport(WLDeploymentManager dm) {
        this.dm = dm;
        this.serverRoot = WLPluginProperties.getServerRoot(dm, true);
    }

    public boolean isSwitchSupported() {
        Version version = null;
        synchronized (this) {
            if (serverVersion != null) {
                version = serverVersion;
            } else {
                if (dm != null) {
                    version = dm.getServerVersion();
                } else {
                    version = WLPluginProperties.getServerVersion(serverRoot);
                }
                serverVersion = version;
            }
        }
        return version != null && version.isAboveOrEqual(SWITCH_SUPPORTED_VERSION_MIN)
                && SWITCH_SUPPORTED_VERSION_MIN.getMajor().equals(version.getMajor())
                && SWITCH_SUPPORTED_VERSION_MIN.getMinor().equals(version.getMinor());
    }

    public void enable() {
        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();
        actions.add(new ProgressSupport.BackgroundAction() {

            @Override
            protected void run(Context actionContext) {
                actionContext.progress(NbBundle.getMessage(WLJpa2SwitchSupport.class, "MSG_Enabling_JPA2"));
                doEnable();
            }
        });
        ProgressSupport.invoke(actions);
    }
    
    public void disable() {
        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();
        actions.add(new ProgressSupport.BackgroundAction() {

            @Override
            protected void run(Context actionContext) {
                actionContext.progress(NbBundle.getMessage(WLJpa2SwitchSupport.class, "MSG_Disabling_JPA2"));
                doDisable();
            }
        });
        ProgressSupport.invoke(actions);
    }
    
    private void doEnable() {
        if (!isSwitchSupported()) {
            throw new IllegalStateException("JPA2 switching is not supported for WebLogic " + serverRoot);
        }

        try {
            File libDir = WLPluginProperties.getServerLibDirectory(serverRoot);
            if (libDir != null) {
                libDir = FileUtil.normalizeFile(libDir);
            }

            String path = getPathToModules(libDir);
            if (path.length() > 0) {
                path = path + "/"; // NOI18N
            }
            final File oepeFile = new File(libDir, OEPE_CONTRIBUTIONS_JAR);
            final String relPath = path;

            Jpa2Jars jars = getJpa2Jars(libDir, path);

            final String contribPath = path + jars.getJpa1Jar() + " " // NOI18N
                    + path + jars.getJpa2Jar();

            
            // oepe does not exist
            if (!oepeFile.exists()) {
                createContributionsJar(oepeFile, contribPath);
                // exists so update cp
            } else {
                JarFile oepeJarFile = new JarFile(oepeFile);
                try {
                    Manifest mf = oepeJarFile.getManifest();
                    String cp = mf.getMainAttributes().getValue(Name.CLASS_PATH);
                    if (cp == null) {
                        cp = ""; // NOI18N
                    }
                    if ((!JPA_JAR_1_PATTERN.matcher(cp).matches() || !cp.contains(jars.getJpa1Jar()))
                            || (!JPA_JAR_2_PATTERN.matcher(cp).matches() || !cp.contains(jars.getJpa2Jar()))) {

                        StringBuilder updated = new StringBuilder();
                        for (String element : cp.split("\\s+")) { // NOI18N
                            if (!JPA_JAR_1_PATTERN.matcher(element).matches() && !JPA_JAR_2_PATTERN.matcher(element).matches()) {
                                updated.append(element).append(" "); // NOI18N
                            }
                        }

                        updated.insert(0, " ").insert(0, jars.getJpa2Jar()).insert(0, relPath); // NOI18N
                        updated.insert(0, " ").insert(0, jars.getJpa1Jar()).insert(0, relPath); // NOI18N

                        if (cp.length() == 0) {
                            updated.deleteCharAt(updated.length() - 1);
                        }
                        mf.getMainAttributes().put(Name.CLASS_PATH, updated.toString());
                        replaceManifest(oepeFile, mf);
                    }
                } finally {
                    oepeJarFile.close();
                }
            }

            // update weblogic.jar
            File weblogicFile = WebLogicLayout.getWeblogicJar(serverRoot);
            JarFile weblogicJarFile = new JarFile(weblogicFile);
            try {
                Manifest wlManifest = weblogicJarFile.getManifest();
                String cp = wlManifest.getMainAttributes().getValue(Name.CLASS_PATH);
                if (cp == null) {
                    cp = ""; // NOI18N
                }
                if (!cp.contains(OEPE_CONTRIBUTIONS_JAR)) {
                    if (cp.length() == 0) {
                        cp = OEPE_CONTRIBUTIONS_JAR;
                    } else {
                        cp = OEPE_CONTRIBUTIONS_JAR + " " + cp; // NOI18N
                    }
                    wlManifest.getMainAttributes().put(Name.CLASS_PATH, cp);
                    replaceManifest(weblogicFile, wlManifest);
                }
            } finally {
                weblogicJarFile.close();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            notifyLibrariesChanged();
        }
    }

    private void doDisable() {
        if (!isSwitchSupported()) {
            throw new IllegalStateException("JPA2 switching is not supported for WebLogic " + serverRoot);
        }

        try {
            File libDir = WLPluginProperties.getServerLibDirectory(serverRoot);
            if (libDir != null) {
                libDir = FileUtil.normalizeFile(libDir);
            }
            File oepeJarFile = new File(libDir, OEPE_CONTRIBUTIONS_JAR);
            if (!oepeJarFile.exists() || !oepeJarFile.isFile()) {
                return;
            }
            JarFile file = new JarFile(oepeJarFile);
            try {
                Manifest mf = file.getManifest();
                String cp = mf.getMainAttributes().getValue(Name.CLASS_PATH);
                if (cp == null) {
                    return;
                }

                StringBuilder builder = new StringBuilder();
                for (String element : cp.split("\\s+")) { // NOI18N
                    if (!JPA_JAR_1_PATTERN.matcher(element).matches() && !JPA_JAR_2_PATTERN.matcher(element).matches()) {
                        builder.append(element).append(" "); // NOI18N
                    }
                }
                if (builder.length() > 0) {
                    mf.getMainAttributes().put(Name.CLASS_PATH,
                            builder.substring(0, builder.length() - 1));
                } else {
                    mf.getMainAttributes().remove(Name.CLASS_PATH);
                }
                replaceManifest(oepeJarFile, mf);
            } finally {
                file.close();
            }
        } catch (IOException ex) {
            // TODO some exception/message to the user
            Exceptions.printStackTrace(ex);
        } finally {
            notifyLibrariesChanged();
        }
    }

    public boolean isEnabled() {
        if (dm != null) {
            return dm.getJ2eePlatformImpl().isJpa2Available();
        } else {
            List<URL> classpath = WLJ2eePlatformFactory.getWLSClassPath(serverRoot,
                    WLPluginProperties.getMiddlewareHome(serverRoot), null);
            for (URL url : classpath) {
                URL file = FileUtil.getArchiveFile(url);
                if (file != null && JPA_JAR_1_PATTERN.matcher(file.getFile()).matches()) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean isEnabledViaSmartUpdate() {
        if (dm != null) {
            dm.getJ2eePlatformImpl().getLibraries();
            for (LibraryImplementation lib : dm.getJ2eePlatformImpl().getLibraries()) {
                List<URL> urls = lib.getContent("classpath"); // NOI18N
                if (isEnabledViaSmartUpdate(urls)) {
                    return true;
                }
            }
            return false;
        } else {
            List<URL> urls = WLJ2eePlatformFactory.getWLSClassPath(serverRoot,
                    WLPluginProperties.getMiddlewareHome(serverRoot), null);
            return isEnabledViaSmartUpdate(urls);
        }
    }

    private boolean isEnabledViaSmartUpdate(List<URL> urls) {
        if (urls != null) {
            for (URL url : urls) {
                URL file = FileUtil.getArchiveFile(url);
                if (file != null && file.getFile().endsWith("BUG9923849_WLS103MP4.jar")) { // NOI18N
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Method sends classpath change event to any instance registered
     * in the IDE and sharing the same server root.
     */
    private void notifyLibrariesChanged() {
        if (dm != null) {
            dm.getJ2eePlatformImpl().notifyLibrariesChange();
        }
        String[] urls = Deployment.getDefault().getInstancesOfServer(WLDeploymentFactory.SERVER_ID);
        for (String url : urls) {
            if (dm != null && url.equals(dm.getUri())) {
                continue;
            }
            InstanceProperties props = InstanceProperties.getInstanceProperties(url);
            if (props != null) {
                String serverRootValue = props.getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
                File root = FileUtil.normalizeFile(new File(serverRootValue));
                if (root.equals(serverRoot)) {
                    try {
                        WLDeploymentManager manager = (WLDeploymentManager)
                                WLDeploymentFactory.getInstance().getDisconnectedDeploymentManager(url);
                        manager.getJ2eePlatformImpl().notifyLibrariesChange();
                    } catch (DeploymentManagerCreationException ex) {
                        LOGGER.log(Level.INFO, null, ex);
                    }
                }
            }
        }
    }
    
    private void replaceManifest(File jarFile, Manifest manifest) throws IOException {
        FileObject fo = FileUtil.toFileObject(jarFile);
        String tmpName = FileUtil.findFreeFileName(fo.getParent(),
                jarFile.getName(), "tmp"); // NOI18N
        File tmpJar = new File(jarFile.getParentFile(), tmpName + ".tmp"); // NOI18N
        try {
            InputStream is = new BufferedInputStream(
                    new FileInputStream(jarFile));
            try {
                OutputStream os = new BufferedOutputStream(
                        new FileOutputStream(tmpJar));
                try {
                    replaceManifest(is, os, manifest);
                } finally {
                    os.close();
                }
            } finally {
                is.close();
            }

            if (tmpJar.renameTo(jarFile)) {
                LOGGER.log(Level.FINE, "Successfully moved {0}", tmpJar);
                return;
            }
            LOGGER.log(Level.FINE, "Byte to byte copy {0}", tmpJar);
            copy(tmpJar, jarFile);
        } finally {
            tmpJar.delete();
        }
    }

    private void replaceManifest(InputStream is, OutputStream os, Manifest manifest) throws IOException {
        JarInputStream in = new JarInputStream(is);
        try {
            JarOutputStream out = new JarOutputStream(os, manifest);
            try {
                JarEntry entry = null;
                byte[] temp = new byte[32768];
                while ((entry = in.getNextJarEntry()) != null) {
                    String name = entry.getName();
                    if (name.equalsIgnoreCase("META-INF/MANIFEST.MF")) { // NOI18N
                        continue;
                    }
                    out.putNextEntry(entry);
                    while (in.available() != 0) {
                        int read = in.read(temp);
                        if (read != -1) {
                            out.write(temp, 0, read);
                        }
                    }
                    out.closeEntry();
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    private void createContributionsJar(File jarFile, String classpath) throws IOException {
        //need to create zip file
        OutputStream os = new BufferedOutputStream(new FileOutputStream(jarFile));
        try {
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Name.MANIFEST_VERSION, "1.0"); // NOI18N
            manifest.getMainAttributes().put(Name.CLASS_PATH, classpath);
            JarOutputStream dest = new JarOutputStream(new BufferedOutputStream(os), manifest);
            try {
                dest.closeEntry();
                dest.finish();
            } finally {
                dest.close();
            }
        } finally {
            os.close();
        }
    }

    private void copy(File source, File dest) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(source));
        try {
            OutputStream os = new BufferedOutputStream(new FileOutputStream(dest));
            try {
                FileUtil.copy(is, os);
            } finally {
                os.close();
            }
        } finally {
            is.close();
        }
    }

    private String getPathToModules(File from) {
        File mwHomeFile = null;
        String mwHome = (dm != null)
                ? dm.getProductProperties().getMiddlewareHome()
                : WLProductProperties.getMiddlewareHome(serverRoot);
        if (mwHome == null) {
            if (serverRoot != null && serverRoot.getParentFile() != null) {
                mwHomeFile = serverRoot.getParentFile();
            }
        } else {
            mwHomeFile = new File(mwHome);
        }
        if (mwHomeFile != null) {
            File modules = FileUtil.normalizeFile(new File(mwHomeFile, "modules")); // NOI18N
            String relativePath = getRelativePath(from, modules);
            if (relativePath == null) {
                // FIXME forward slashes
                return modules.getAbsolutePath();
            }
            return relativePath;
        }
        // just improbable fallback :(
        return "../../../modules"; // NOI18N
    }

    private Jpa2Jars getJpa2Jars(File libDir, String path) {
        String jar1 = null;
        String jar2 = null;

        if (libDir != null) {
            File dir = new File(libDir, path);

            for (File candidate : dir.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return JPA_JAR_1_PATTERN.matcher(name).matches() || JPA_JAR_2_PATTERN.matcher(name).matches();
                }
            })) {
                if (jar1 == null && JPA_JAR_1_PATTERN.matcher(candidate.getName()).matches()) {
                    jar1 = candidate.getName();
                } else if (jar2 == null) {
                    jar2 = candidate.getName();
                }
            }
        }
        // just fallback
        if (jar1 == null) {
            jar1 = JPA_JAR1_FALLBACK;
        }
        if (jar2 == null) {
            jar2 = JPA_JAR2_FALLBACK;
        }
        return new Jpa2Jars(jar1, jar2);
    }

    // package for testing only
    static String getRelativePath(File from, File to) {
        String toPath = to.getAbsolutePath();
        String fromPath = from.getAbsolutePath();
        if (toPath.startsWith(fromPath)) {
            if (toPath.length() == fromPath.length()) {
                return "";
            }
            StringBuilder builder = new StringBuilder();
            File currentPath = to;
            while (!currentPath.equals(from)) {
                builder.insert(0, currentPath.getName());
                builder.insert(0, "/"); // NOI18N
                currentPath = currentPath.getParentFile();
            }
            return builder.substring(1);
        } else {
            File parent = from.getParentFile();
            if (parent == null) {
                return null;
            } else {
                return "../" + getRelativePath(parent, to); // NOI18N
            }
        }
    }

    private static class Jpa2Jars {

        private final String Jpa1Jar;

        private final String Jpa2Jar;

        public Jpa2Jars(String Jpa1Jar, String Jpa2Jar) {
            this.Jpa1Jar = Jpa1Jar;
            this.Jpa2Jar = Jpa2Jar;
        }

        public String getJpa1Jar() {
            return Jpa1Jar;
        }

        public String getJpa2Jar() {
            return Jpa2Jar;
        }
    }
}
