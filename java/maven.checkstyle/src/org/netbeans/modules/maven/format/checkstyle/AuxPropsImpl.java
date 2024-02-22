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

package org.netbeans.modules.maven.format.checkstyle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(projectType="org-netbeans-modules-maven", service=AuxiliaryProperties.class)
public class AuxPropsImpl implements AuxiliaryProperties, PropertyChangeListener {
    private final Project project;

    private Properties cache;
    private boolean recheck = true;
    private final List<String> defaults = new ArrayList<String>();
    private final AtomicBoolean enabledSet = new AtomicBoolean(false);
    private boolean enabled = false;


    private final RequestProcessor RP = new RequestProcessor("Download checkstyle plugin classpath", 1);

    public AuxPropsImpl(Project prj) {
        this.project = prj;
        defaults.add("config/sun_checks.xml");
        defaults.add("config/maven_checks.xml");
        defaults.add("config/avalon_checks.xml");
        defaults.add("config/turbine_checks.xml");
        NbMavenProject.addPropertyChangeListener(prj, this);
    }

    private FileObject cacheDir() throws IOException {
        return ProjectUtils.getCacheDirectory(project, AuxPropsImpl.class);
    }
    
    private FileObject copyToCacheDir(InputStream in) throws IOException {
        FileObject cacheDir = cacheDir();
        FileObject file = cacheDir.getFileObject("checkstyle-checker.xml");
        if (file == null) {
            file = cacheDir.createData("checkstyle-checker", "xml");
        }
        try (in; OutputStream outst = file.getOutputStream()) {
            FileUtil.copy(in, outst);
        }
        return file;
    }

    private Properties convert() {
        try {
            FileObject cachedFile = cacheDir().getFileObject("checkstyle-checker.xml");
            boolean hasCached = cachedFile != null && cache != null;
            ModuleConvertor mc = new ModuleConvertor();
            FileObject fo = project.getProjectDirectory().getFileObject("target/checkstyle-checker.xml");
            if (fo != null) {
                //somehow check that the cached file is same as the output dir one..
                if (hasCached && cachedFile.getSize() == fo.getSize()) {
                    return cache;
                } else {
                    // no cached file or the current one is different..
                    fo = copyToCacheDir(fo.getInputStream());
                }
            } else {
                FileObject pom = project.getProjectDirectory().getFileObject("pom.xml");
                if (hasCached && pom != null && cachedFile.lastModified().after(pom.lastModified())) {
                    //sort of simplistic
                    return cache;
                } else {
                    String loc = PluginPropertyUtils.getReportPluginProperty(project, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_CHECKSTYLE, "configLocation", null);
                    if (loc == null && definesCheckStyle(project)) {
                        loc = "config/sun_checks.xml"; //this is the default NOI18N
                    }
                    if (loc != null && defaults.contains(loc)) {
                        InputStream in = getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/maven/format/checkstyle/" + loc);
                        fo = copyToCacheDir(in);
                    } else if (loc != null) {
                        //find in local fs
                        File file = FileUtilities.resolveFilePath(FileUtil.toFile(project.getProjectDirectory()), loc);
                        if (file != null && file.exists()) {
                            fo = copyToCacheDir(FileUtil.toFileObject(file).getInputStream());
                        } else {
                            List<File> deps = findDependencyArtifacts();
                            if (deps.size() > 0) {
                                for (File d : deps) {
                                    FileObject fileFO = FileUtil.toFileObject(d);
                                    if (FileUtil.isArchiveFile(fileFO)) {
                                        FileObject root = FileUtil.getArchiveRoot(fileFO);
                                        if (root != null) {
                                            fo = root.getFileObject(loc);
                                            if (fo != null) {
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            if (fo == null) {
                                try {
                                    final URL url = new URL(loc);
                                    RP.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try (InputStream urlis = url.openStream()) {
                                                byte[] arr = urlis.readAllBytes();
                                                synchronized (AuxPropsImpl.this) {
                                                    //#174401
                                                    ByteArrayInputStream bais = new ByteArrayInputStream(arr);
                                                    copyToCacheDir(bais);
                                                    recheck = true;
                                                }
                                            } catch (IOException ex) {
                                                ex.printStackTrace();
                                            }
                                        }
                                    });
                                } catch (MalformedURLException ex) {
                                    //#172067 ignore badly formed urls..
                                    //probably a local relative file path, but not existing..
                                }
                            }
                        }
                    }
                }
            }
            if (fo != null) {
                return mc.convert(fo.getInputStream());
            }
        } catch (IOException io) {
            Exceptions.printStackTrace(io);
        }
        return new Properties();
    }

    static boolean definesCheckStyle(Project prj) {
        NbMavenProject project = prj.getLookup().lookup(NbMavenProject.class);
        assert project != null : "Requires a maven project instance"; //NOI18N
        return definesCheckStyle(project.getMavenProject());
    }

    static boolean definesCheckStyle(MavenProject prj) {
        for (ReportPlugin plug : prj.getReportPlugins()) {
            if (Constants.GROUP_APACHE_PLUGINS.equals(plug.getGroupId()) &&
                    Constants.PLUGIN_CHECKSTYLE.equals(plug.getArtifactId())) { //NOI18N
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @return list of files in local repository
     */
    private List<File> findDependencyArtifacts() {
        List<File> cpFiles = new ArrayList<File>();
        final NbMavenProject p = project.getLookup().lookup(NbMavenProject.class);
        List<Plugin> plugins = p.getMavenProject().getBuildPlugins();
        for (Plugin plug : plugins) {
            if (Constants.PLUGIN_CHECKSTYLE.equals(plug.getArtifactId()) &&
                    Constants.GROUP_APACHE_PLUGINS.equals(plug.getGroupId())) {

                List<Dependency> deps = plug.getDependencies();
                final MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
                online.setUpLegacySupport();

                //TODO: check alternative for deprecated maven components
                final MavenProjectBuilder builder = online.lookupComponent(MavenProjectBuilder.class);
                assert builder !=null : "MavenProjectBuilder component not found in maven";

                for (Dependency d : deps) {
                    final Artifact projectArtifact = online.createArtifactWithClassifier(d.getGroupId(), d.getArtifactId(), d.getVersion(), d.getType(), d.getClassifier());
                    String localPath = online.getLocalRepository().pathOf(projectArtifact);
                    File f = FileUtil.normalizeFile(new File(online.getLocalRepository().getBasedir(), localPath));
                    if (f.exists()) {
                        cpFiles.add(f);
                    } else {
                        RP.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    //TODO add progress bar.
                                    // XXX does online.resolve(...) not suffice?
                                    online.setUpLegacySupport();
                                    builder.buildFromRepository(projectArtifact, p.getMavenProject().getRemoteArtifactRepositories(), online.getLocalRepository());
                                    synchronized (AuxPropsImpl.this) {
                                        recheck = true;
                                    }
                                } catch (ProjectBuildingException ex) {
                                    ex.printStackTrace();
//                                        Exceptions.printStackTrace(ex);
                                }
                            }
                        });
                    }
                }
            }
        }
        return cpFiles;
    }

    Properties getCache() {
        if (enabledSet.compareAndSet(false, true)) { //#238910
            String en = project.getLookup().lookup(AuxiliaryProperties.class).get(Constants.HINT_CHECKSTYLE_FORMATTING, true);
            enabled = en != null && Boolean.parseBoolean(en);
        }
        
        synchronized (this) {
        if (cache == null || recheck) {
            if (enabled) {
                    try {
                        RequestProcessor rp = new RequestProcessor("Checkstyle cache" , 1);
                        cache = rp.submit(new Callable<Properties>() {
                                @Override
                                public Properties call() throws Exception {
                                    return convert();
                                }
                            }).get();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (ExecutionException ex) {
                        Exceptions.printStackTrace(ex);
                    }
            } else {
                cache = new Properties();
            }
            recheck = false;
        }
        }
        return cache;
    }

    @Override
    public String get(String key, boolean shared) {
        if (Constants.HINT_CHECKSTYLE_FORMATTING.equals(key)) {
            return null;
        }
        if (shared) {
            return getCache().getProperty(key);
        }
        return null;
    }

    @Override
    public void put(String key, String value, boolean shared) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<String> listKeys(boolean shared) {
        if (shared) {
            List<String> str = new ArrayList<String>();
            for (Object k : getCache().keySet()) {
                str.add((String)k);
            }
            return str;
        }
        return new ArrayList<String>();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
            enabledSet.compareAndSet(true, false);
            synchronized (this) {
                recheck = true;
            }
        }
    }

}
