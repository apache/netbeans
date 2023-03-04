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

package org.netbeans.modules.maven.classpath;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.indexer.api.RepositoryUtil;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

/**
 * NO listening on changes here, let the BootClassPath deal with it..
 * @author  Milos Kleint
 */
@org.netbeans.api.annotations.common.SuppressWarnings("DMI_COLLECTION_OF_URLS")
public final class EndorsedClassPathImpl implements ClassPathImplementation, FileChangeListener {

    private static final Logger LOG = Logger.getLogger(EndorsedClassPathImpl.class.getName());
    static final RequestProcessor RP = new RequestProcessor(EndorsedClassPathImpl.class);

    private List<? extends PathResourceImplementation> resourcesCache;
    private boolean includeJDKCache;
    private boolean includeFXCache;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final NbMavenProjectImpl project;
    private BootClassPathImpl bcp;
    private String[] current;
    private final File endorsed;
    private final Map<File,File/*|null*/> endorsed2Repo = new HashMap<File,File>();

    @SuppressWarnings("LeakingThisInConstructor")
    EndorsedClassPathImpl(NbMavenProjectImpl project) {
        this.project = project;
        endorsed = new File(project.getPOMFile().getParentFile(), "target/endorsed"); // NOI18N
        FileUtil.addFileChangeListener(this, endorsed);
    }

    public @Override List<? extends PathResourceImplementation> getResources() {
        boolean[] arr = { false };
        return getResources(arr, arr);
    }
    
    final List<? extends PathResourceImplementation> getResources(boolean[] includeJDK, boolean[] includeFx) {
        assert bcp != null;
        synchronized (bcp.LOCK) {
            if (this.resourcesCache == null) {
                ArrayList<PathResourceImplementation> result = new ArrayList<PathResourceImplementation> ();
                String[] boot = getBootClasspath();
                includeJDKCache = true;
                includeFXCache = false;
                if (boot != null) {
                    for (String b : boot) {
                        if ("netbeans.ignore.jdk.bootclasspath".equals(b)) { // NOI18N
                            includeJDK[0] = false;
                            includeFXCache = false;
                            includeJDKCache = false;
                        }
                    }
                    StripPlatformResult res = stripDefaultJavaPlatform(boot);
                    includeFx[0] = res.hasFx;
                    includeFXCache = res.hasFx;
                    for (URL u :  res.urls) {
                        if (u != null) {
                            result.add (ClassPathSupport.createResource(u));
                        }
                    }
                }
                File[] jars = endorsed.listFiles();
                if (jars != null) {
                    for (final File jar : jars) {
                        if (jar.isFile()) {
                            if (endorsed2Repo.containsKey(jar)) {
                                File toScan = endorsed2Repo.get(jar);
                                if (toScan != null) {
                                    URL url = FileUtil.urlForArchiveOrDir(toScan);
                                    if (url != null) {
                                        result.add(ClassPathSupport.createResource(url));
                                    }
                                }
                            } else {
                                // #197510: blocking, must do this asynch
                                LOG.log(Level.FINE, "looking up {0}", jar);
                                RP.post(new Runnable() {
                                    public @Override void run() {
                                        synchronized (bcp.LOCK) {
                                            if (endorsed2Repo.containsKey(jar)) {
                                                // Another task beat us to it.
                                                return;
                                            }
                                        }
                                        if (!jar.isFile()) {
                                            return;
                                        }
                                        File toScan = null;
                                        REPO: for (RepositoryInfo repo : RepositoryPreferences.getInstance().getRepositoryInfos()) {
                                            LOG.log(Level.FINE, "checking {0}", repo);
                                            for (NBVersionInfo analogue : RepositoryQueries.findBySHA1Result(jar, Collections.singletonList(repo)).getResults()) {
                                                toScan = RepositoryUtil.createArtifact(analogue).getFile();
                                                LOG.log(Level.FINE, "found {0}", toScan);
                                                break REPO;
                                            }
                                        }
                                        if (toScan == null) {
                                            try {
                                                toScan = FileUtil.normalizeFile(new File(System.getProperty("java.io.tmpdir"), RepositoryUtil.calculateSHA1Checksum(jar) + ".jar"));
                                                if (!toScan.isFile()) {
                                                    FileUtils.copyFile(jar, toScan);
                                                }
                                            } catch (IOException x) {
                                                LOG.log(Level.INFO, "copying " + jar + " to " + toScan, x);
                                            }
                                        }
                                        LOG.log(Level.FINE, "mapping {0} -> {1}", new Object[] {jar, toScan});
                                        synchronized (bcp.LOCK) {
                                            endorsed2Repo.put(jar, toScan);
                                            resourcesCache = null;
                                        }
                                        support.firePropertyChange(PROP_RESOURCES, null, null);
                                    }
                                });
                            }
                        }
                    }
                }
                current = boot;
                resourcesCache = Collections.unmodifiableList (result);
            } else {
                includeJDK[0] = includeJDKCache;
                includeFx[0] = includeFXCache;
            }
            return this.resourcesCache;
        }
    }

    public @Override void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener (listener);
    }

    public @Override void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener (listener);
    }

    private String[] getBootClasspath() {
        String carg = PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, "compilerArgument", "compile", null);
        if (carg != null) {
            //TODO
        }
        Properties cargs = PluginPropertyUtils.getPluginPropertyParameter(project, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, "compilerArguments", "compile");
        if (cargs != null) {
            String carg2 = cargs.getProperty("bootclasspath");
            if (carg2 != null) {
                return StringUtils.split(carg2, File.pathSeparator);
            }
        }
        return null;
    }

    /**
     * Resets the cache and firesPropertyChange
     */
    boolean resetCache () {
        String[] newones = getBootClasspath();
        boolean fire = false;
        assert bcp != null;
        synchronized (bcp.LOCK) {
            if (!Arrays.equals(newones, current)) {
                resourcesCache = null;
                fire = true;
            }
        }
        if (fire) {
            support.firePropertyChange(PROP_RESOURCES, null, null);
        }
        return fire;
    }

    void setBCP(BootClassPathImpl aThis) {
        bcp = aThis;
    }
    private class StripPlatformResult {
        List<URL> urls;
        boolean hasFx = false;
    }

    private StripPlatformResult stripDefaultJavaPlatform(String[] boot) {
        StripPlatformResult res = new StripPlatformResult();
        List<URL> toRet = new ArrayList<URL>();
        res.urls = toRet;
        Set<URL> defs = getDefJavaPlatBCP();
        OUTER: for (String s : boot) {
            File f = FileUtilities.convertStringToFile(s);
            URL entry = FileUtil.urlForArchiveOrDir(f);
            if (entry != null && !defs.contains(entry)) {
                if (entry.getPath().endsWith("/jfxrt.jar!/")) {
                    //we need to iterate the defs and check again as jdk8 and jdk7 have these at different places
                    for (URL d : defs) {
                        if (d.getPath().endsWith("/jfxrt.jar!/")) {
                            res.hasFx = true;
                            continue OUTER;
                        }
                    }
                }
                toRet.add(entry);
            }
        }
        return res;
    }

    private final Set<URL> djpbcp = new HashSet<URL>();

    private Set<URL> getDefJavaPlatBCP() {
        synchronized (djpbcp) {
            if (djpbcp.isEmpty()) {
                JavaPlatformManager mngr = JavaPlatformManager.getDefault();
                JavaPlatform jp = mngr.getDefaultPlatform();
                ClassPath cp = jp.getBootstrapLibraries();
                for (ClassPath.Entry ent : cp.entries()) {
                    djpbcp.add(ent.getURL());
                }
            }
            return Collections.unmodifiableSet(djpbcp);
        }
    }

    private void fileChange() {
        assert bcp != null;
        synchronized (bcp.LOCK) {
            resourcesCache = null;
        }
        support.firePropertyChange(PROP_RESOURCES, null, null);
    }
    public @Override void fileFolderCreated(FileEvent fe) {
        fileChange();
    }
    public @Override void fileDataCreated(FileEvent fe) {
        fileChange();
    }
    public @Override void fileChanged(FileEvent fe) {
        fileChange();
    }
    public @Override void fileDeleted(FileEvent fe) {
        fileChange();
    }
    public @Override void fileRenamed(FileRenameEvent fe) {
        fileChange();
    }
    public @Override void fileAttributeChanged(FileAttributeEvent fe) {}

}