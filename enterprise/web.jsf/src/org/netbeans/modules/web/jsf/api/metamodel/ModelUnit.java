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
package org.netbeans.modules.web.jsf.api.metamodel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;


/**
 * For given classpath the list of configuration files is maintained and
 * event is fired when list of configuration files or individual
 * file has changed.
 */
public class ModelUnit implements PropertyChangeListener, FileChangeListener {

    private static final RequestProcessor RP = new RequestProcessor(ModelUnit.class);
    private static final Logger LOGGER = Logger.getLogger(ModelUnit.class.getName());

    private final ClassPath bootPath;
    private final ClassPath compilePath;
    private final ClassPath sourcePath;
    private final WeakReference<Project> projectRef;

    private final PropertyChangeSupport changeSupport;

    /**
     * Cached list of JSF configuration files.
     */
    private List<FileObject> configFiles;

    /**
     * Cached list of folders under which some configuration files may be created,
     * eg. Java source root under which MEAT-INF/*faces-config.xml can be created.
     */
    private List<FileObject> configRoots = Collections.synchronizedList(new LinkedList<FileObject>());

    private static final String META_INF = "META-INF";      // NOI18N
    private static final String FACES_CONFIG = "faces-config.xml";// NOI18N
    private static final String FACES_CONFIG_SUFFIX = ".faces-config.xml"; // NOI18N
    private static final String DEFAULT_FACES_CONFIG_PATH = "WEB-INF/faces-config.xml"; //NOI18N

    /**
     * Property name which is fired when there was
     * a configuration files relevant change.
     */
    public final String PROP_CONFIG_FILES = "configFiles";

    public static ModelUnit create(ClassPath bootPath, ClassPath compilePath, ClassPath sourcePath, Project project) {
        return new ModelUnit(bootPath, compilePath, sourcePath, project);
    }

    private ModelUnit(ClassPath bootPath, ClassPath compilePath, ClassPath sourcePath, Project project) {
        Parameters.notNull("sourcePath", sourcePath);
        this.bootPath= bootPath;
        this.compilePath = compilePath;
        this.sourcePath = sourcePath;
        this.projectRef = new WeakReference<Project>(project);
        changeSupport = new PropertyChangeSupport(this);
        initListeners();
    }


    public ClassPath getBootPath() {
        return bootPath;
    }

    public ClassPath getCompilePath() {
        return compilePath;
    }

    public ClassPath getSourcePath() {
        return sourcePath;
    }

    /**
     * Returns main faces-config.xml file if presented or null.
     */
    public FileObject getApplicationFacesConfig() {
        List<FileObject> l = getConfigFilesImpl();
        if (l.isEmpty()) {
            return null;
        }
        FileObject first = l.iterator().next();

        Project project = projectRef.get();
        if (project == null) {
            return null;
        }
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if (webModule != null) {
            FileObject documentBase = webModule.getDocumentBase();
            if (documentBase == null) {
                return null;
            }

            FileObject mainConfigFile = documentBase.getFileObject(DEFAULT_FACES_CONFIG_PATH);
            if (mainConfigFile != null && mainConfigFile.equals(first)) {
                return first;
            }
        } else {
            List<FileObject> projectConfigs = new LinkedList<FileObject>();
            collectConfigurationFilesFromClassPath(sourcePath, projectConfigs, new LinkedList<FileObject>());
            for (FileObject config : projectConfigs) {
                if (config.getName().equals(FACES_CONFIG)) {
                    return config;
                }
            }
        }

        return null;
    }

    /**
     * Returns list of other configuration files excluding
     * the main faces-config.xml file. Returns always non-null potentially
     * empty list.
     */
    public List<FileObject> getApplicationConfigurationResources() {
        List<FileObject> l = getConfigFilesImpl();
        FileObject applicationFacesConfig = getApplicationFacesConfig();
        if (applicationFacesConfig != null) {
            return l.subList(1, l.size());
        } else {
            return l;
        }
    }

    private static void collectConfigurationFilesFromClassPath(ClassPath cp, List<FileObject> configs, List<FileObject> configRoots) {
        for (ClassPath.Entry entry : cp.entries()) {
            FileObject roots[];
            if (entry.isValid()) {
                roots = new FileObject[]{entry.getRoot()};
            } else {
                // if classpath root does not exist then perhaps it is
                // a project which has not been built - use SourceForBinaryQuery
                // to use project sources instead:
                SourceForBinaryQuery.Result res = SourceForBinaryQuery.findSourceRoots(entry.getURL());
                roots = res.getRoots();
            }
            for (FileObject root : roots) {
                configRoots.add(root);
                FileObject metaInf = root.getFileObject(META_INF);
                if (metaInf != null) {
                    FileObject[] children = metaInf.getChildren();
                    for (FileObject fileObject : children) {
                        String name = fileObject.getNameExt();
                        if (name.equals(FACES_CONFIG) || name.endsWith(FACES_CONFIG_SUFFIX)) {
                             if(!configs.contains(fileObject)) {
                                //do not duplicate
                                configs.add( fileObject );
                             }
                        }
                    }
                }
            }
        }
    }

    private synchronized List<FileObject> getConfigFiles() {
        return configFiles;
    }

    private synchronized void setConfigFiles(List<FileObject> configFiles, List<FileObject> configRoots) {
        this.configFiles = configFiles;
        this.configRoots = configRoots;
    }

    private synchronized List<FileObject> getConfigRoots() {
        return configRoots;
    }

    private List<FileObject> getConfigFilesImpl() {
        List<FileObject> configs = getConfigFiles();
        Project project = projectRef.get();
        if (configs != null || project == null) {
            return configs;
        }

        List<FileObject> webFacesConfigs = new LinkedList<FileObject>();
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if (webModule != null) {
            webFacesConfigs.addAll(Arrays.asList(ConfigurationUtils.getFacesConfigFiles(webModule)));
        }
        //add all the configs from WEB-INF/faces-config.xml and all configs declared in faces config DD entry
        //we need to ensure the original ordering
        configs = Collections.synchronizedList(new LinkedList<FileObject>(webFacesConfigs));
        List<FileObject> localconfigRoots = Collections.synchronizedList(new LinkedList<FileObject>());
        if (webModule != null && webModule.getDocumentBase() != null) {
            localconfigRoots.add(webModule.getDocumentBase());
        }
        //find for configs in meta-inf
        collectConfigurationFilesFromClassPath(sourcePath, configs, localconfigRoots);
        collectConfigurationFilesFromClassPath(compilePath, configs, localconfigRoots);
        setConfigFiles(configs, localconfigRoots);
        return configs;
    }

    public void addPropertyChangeListener( PropertyChangeListener listener ) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener( PropertyChangeListener listener ) {
        changeSupport.removePropertyChangeListener(listener);
    }

    private void fireChange() {
        // reset list of config files to be re-read:
        setConfigFiles(null, new LinkedList<FileObject>());

        changeSupport.firePropertyChange(PROP_CONFIG_FILES, null, null);
    }

    private void initListeners() {
        // listen to any change on source path classpath:
        sourcePath.addPropertyChangeListener(WeakListeners.propertyChange(ModelUnit.this, sourcePath));
        // listen to any change on compilation classpath:
        compilePath.addPropertyChangeListener(WeakListeners.propertyChange(ModelUnit.this, compilePath));

        // The time-consuming listener registration is moved to a background thread since it can block project's
        // actions also in case of non-JSF project which is pain. It can miss initial JSF configuration changes.
        RP.submit(new Runnable() {
            @Override
            public void run() {
                // listen to any relevant configuration file change:
                long start = System.currentTimeMillis();
                Project project = projectRef.get();
                if (project != null) {
                    project.getProjectDirectory().addRecursiveListener(FileUtil.weakFileChangeListener(ModelUnit.this, project.getProjectDirectory()));
                }
                LOGGER.log(Level.FINE, "JSF''s ModelUnit ResursiveListener registration took {0}ms.", new Object[]{(System.currentTimeMillis() - start)});
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(ClassPath.PROP_ENTRIES)) {
            fireChange();
        }
    }

    private boolean isRelevantFileEvent(FileEvent fe) {
        // relevant files changes are (JSF spec "11.4.2 Application Startup Behavior"):
        //   - resources that match either "META-INF/faces-config.xml" or
        //     end with ".facesconfig.xml" directly in the "META-INF" directory
        //   - existence of a context initialization parameter named javax.faces.CONFIG_FILES iun web.xml
        //   - /WEB-INF/faces-config.xml
        String path = fe.getFile().getPath();
            boolean res = path.endsWith("/web.xml") ||
                   path.endsWith("/WEB-INF/faces-config.xml") ||
                   path.endsWith("/META-INF/faces-config.xml") ||
                  (path.endsWith(FACES_CONFIG_SUFFIX) && fe.getFile().getParent() != null && fe.getFile().getParent().getNameExt().equals("META-INF"));
        if (!res && fe instanceof FileRenameEvent) {
                FileRenameEvent fre = (FileRenameEvent)fe;
                res = (fre.getName().equals("faces-config") || fre.getName().endsWith(".faces-config") || fre.getName().endsWith("web.xml")) &&
                        fre.getExt().equals("xml");
        }
        if (res) {
            // file passed filename criteria but it must be also under one
            // of the folder we are keeping eye on; that way we will ignore
            // events coming for JSF configuration files from different projects
            res = false;
            List<FileObject> cfRoots = ModelUnit.this.getConfigRoots();
            synchronized (cfRoots) {
                for (FileObject fo : cfRoots) {
                    if (FileUtil.isParentOf(fo, fe.getFile())) {
                        res = true;
                        break;
                    }
                }
            }
        }
        return res;
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        if (isRelevantFileEvent(fe)) {
            fireChange();
        }
    }

    @Override
    public void fileChanged(FileEvent fe) {
        if (isRelevantFileEvent(fe)) {
            fireChange();
        }
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        if (isRelevantFileEvent(fe)) {
            fireChange();
        }
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        if (isRelevantFileEvent(fe)) {
            fireChange();
        }
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

}
