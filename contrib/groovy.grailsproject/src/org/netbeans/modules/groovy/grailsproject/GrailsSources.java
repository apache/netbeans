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

package org.netbeans.modules.groovy.grailsproject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.groovy.support.api.GroovySources;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek
 */
public class GrailsSources extends FileChangeAdapter implements Sources {

    //  those are dirs in project root we already know and create specific source groups
    public static final List KNOWN_FOLDERS = Arrays.asList(
            "docs", // NOI18N
            "grails-app", // NOI18N
            "lib", // NOI18N
            "scripts", // NOI18N
            "src", // NOI18N
            "test", // NOI18N
            "web-app" // NOI18N
            );

    //  those are dirs in grails-app root we already know and create specific source groups
    public static final List KNOWN_FOLDERS_IN_GRAILS_APP = Arrays.asList(
            "conf", // NOI18N
            "controllers", // NOI18N
            "domain", // NOI18N
            "i18n", // NOI18N
            "services", // NOI18N
            "taglib", // NOI18N
            "utils", // NOI18N
            "views" // NOI18N
            );

    public static final List KNOWN_OR_IGNORED_FOLDERS_IN_TEST = Arrays.asList(
            "unit", // NOI18N
            "integration", // NOI18N
            "reports" // NOI18N
            );
    
    // these are working folders that we should hide from the project tree:
    public static final List IGNORED_FOLDERS_IN_GRAILS_APP = Arrays.asList(
            "target", // NOI18N
            "gradle" // NOI18N
            );

    private final FileObject projectDir;

    private final GrailsProject project;

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private GrailsSources(GrailsProject project) {
        this.project = project;
        this.projectDir = project.getProjectDirectory();
    }

    static GrailsSources create(GrailsProject project) {
        GrailsSources sources = new GrailsSources(project);
        sources.startFSListener();
        return sources;
    }

    private void startFSListener () {
        try {
            FileSystem fs = projectDir.getFileSystem();
            fs.addFileChangeListener(FileUtil.weakFileChangeListener(this, fs));
        } catch (FileStateInvalidException x) {
            Exceptions.printStackTrace(x);
        }
    }

    public SourceGroup[] getSourceGroups(String type) {
        List<Group> result = new ArrayList<Group>();
        if (Sources.TYPE_GENERIC.equals(type)) {
            addGroup(projectDir, projectDir.getName(), result);
        } else if (JavaProjectConstants.SOURCES_TYPE_JAVA.equals(type)) {
            addGroup(SourceCategoryType.SRC_JAVA, "LBL_SrcJava", result);
        } else if (GroovySources.SOURCES_TYPE_GROOVY.equals(type)) {
            addGroup(SourceCategoryType.GRAILSAPP_CONF, "LBL_grails-app_conf", result);
            addGroup(SourceCategoryType.GRAILSAPP_CONTROLLERS, "LBL_grails-app_controllers", result);
            addGroup(SourceCategoryType.GRAILSAPP_DOMAIN, "LBL_grails-app_domain", result);
            addGroup(SourceCategoryType.GRAILSAPP_SERVICES, "LBL_grails-app_services", result);
            addGroup(SourceCategoryType.GRAILSAPP_TAGLIB, "LBL_grails-app_taglib", result);
            addGroup(SourceCategoryType.GRAILSAPP_UTILS, "LBL_grails-app_utils", result);
            addGroup(SourceCategoryType.SCRIPTS, "LBL_scripts", result);
            addGroup(SourceCategoryType.SRC_GROOVY, "LBL_SrcGroovy", result);
            addGroup(SourceCategoryType.TEST_INTEGRATION, "LBL_IntegrationTests", result);
            addGroup(SourceCategoryType.TEST_UNIT, "LBL_UnitTests", result);
        } else if (GroovySources.SOURCES_TYPE_GRAILS.equals(type)) {
            addGroup(SourceCategoryType.LIB, "LBL_lib", result);
            addGroup(SourceCategoryType.GRAILSAPP_I18N, "LBL_grails-app_i18n", result);
            addGroup(SourceCategoryType.WEBAPP, "LBL_web-app", result);
            addGroup(SourceCategoryType.GRAILSAPP_VIEWS, "LBL_grails-app_views", result);
            addGroup(SourceCategoryType.TEMPLATES, "LBL_grails-templates", result);
        } else if (GroovySources.SOURCES_TYPE_GRAILS_UNKNOWN.equals(type)) {
            // plugins may reside in project dir
            File pluginsDirFile = project.getBuildConfig().getProjectPluginsDir();
            FileObject pluginsDir = pluginsDirFile == null ? null : FileUtil.toFileObject(
                    FileUtil.normalizeFile(pluginsDirFile));
            File globalPluginsDirFile = project.getBuildConfig().getGlobalPluginsDir();
            FileObject globalPluginsDir = globalPluginsDirFile == null ? null : FileUtil.toFileObject(
                    FileUtil.normalizeFile(globalPluginsDirFile));

            for (FileObject child : projectDir.getChildren()) {
                if (child.isFolder()
                        && VisibilityQuery.getDefault().isVisible(child)
                        && !KNOWN_FOLDERS.contains(child.getName())
                        && !IGNORED_FOLDERS_IN_GRAILS_APP.contains(child.getName())
                        && child != pluginsDir
                        && child != globalPluginsDir) {
                    String name = child.getName();
                    addGroup(child, Character.toUpperCase(name.charAt(0)) + name.substring(1), result);
                }
            }

            addGroup(SourceCategoryType.SRC_GWT, "LBL_SrcGwt", result);
            addUnknownGroups(KNOWN_FOLDERS_IN_GRAILS_APP, result, "grails-app", null);
            addUnknownGroups(KNOWN_OR_IGNORED_FOLDERS_IN_TEST, result, "test", "LBL_SomeTests");
        }
        return result.toArray(new SourceGroup[0]);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        changeSupport.fireChange();
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        changeSupport.fireChange();
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        changeSupport.fireChange();
    }

    private void addUnknownGroups(Collection<String> alreadyKnown, List<Group> result,
            String relativePath, String bundleKey) {
        FileObject folder = projectDir.getFileObject(relativePath);
        if (folder != null) {
            for (FileObject child : folder.getChildren()) {
                if (child.isFolder()
                        && VisibilityQuery.getDefault().isVisible(child)
                        && !alreadyKnown.contains(child.getName())) {

                    String name = child.getName();
                    String localizedName = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                    if (bundleKey != null) {
                        localizedName = NbBundle.getMessage(GrailsSources.class, bundleKey, localizedName);
                    }
                    addGroup(child, localizedName, result);
                }
            }
        }
    }

    private void addGroup(FileObject fileObject, String displayName, List<Group> list) {
        if (fileObject != null) {
            list.add(new Group(fileObject, displayName));
        }
    }

    private void addGroup(SourceCategoryType sourceCategory, String bundleLabel, List<Group> list) {
        FileObject fileObject = projectDir.getFileObject(
                project.getSourceCategoriesFactory().getSourceCategory(sourceCategory).getRelativePath()
        );
        if (fileObject != null) {
            list.add(new Group(fileObject, NbBundle.getMessage(GrailsSources.class, bundleLabel)));
        }
    }

    private final class Group implements SourceGroup {

        private final FileObject loc;
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private final String displayName;

        public Group(FileObject loc, String displayName) {
            this.loc = loc;
            this.displayName = displayName;
        }

        public FileObject getRootFolder() {
            return loc;
        }

        public String getName() {
            String location = loc.getPath();
            return location.length() > 0 ? location : "generic"; // NOI18N
        }

        public String getDisplayName() {
            return displayName;
        }

        public Icon getIcon(boolean opened) {
            return null;
        }

        public boolean contains(FileObject file) throws IllegalArgumentException {
            if (file == loc) {
                return true;
            }
            String path = FileUtil.getRelativePath(loc, file);
            if (path == null) {
                throw new IllegalArgumentException();
            }
            if (file.isFolder()) {
                path += File.separator; // NOI18N
            }
            if (file.isFolder() && file != projectDir && ProjectManager.getDefault().isProject(file)) {
                // #67450: avoid actually loading the nested project.
                return false;
            }
            File f = FileUtil.toFile(file);
            if (f != null && SharabilityQuery.getSharability(f) == SharabilityQuery.NOT_SHARABLE) {
                return false;
            } // else MIXED, UNKNOWN, or SHARABLE; or not a disk file
            return true;
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }

        @Override
        public String toString() {
            return "GrailsSources.Group[name=" + getName() + ",rootFolder=" + getRootFolder() + "]"; // NOI18N
        }

    }

}
