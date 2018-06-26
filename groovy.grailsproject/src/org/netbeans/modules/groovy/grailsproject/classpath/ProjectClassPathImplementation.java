/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.groovy.grailsproject.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.grails.api.GrailsPlatform;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.SourceCategoriesFactory;
import org.netbeans.modules.groovy.grailsproject.SourceCategoryType;
import org.netbeans.modules.groovy.grailsproject.config.BuildConfig;
import org.netbeans.modules.groovy.grailsproject.plugins.GrailsPlugin;
import org.netbeans.modules.groovy.grailsproject.plugins.GrailsPluginSupport;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

final class ProjectClassPathImplementation implements ClassPathImplementation {

    private static final Logger LOGGER = Logger.getLogger(ProjectClassPathImplementation.class.getName());

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final ProjectConfigListener projectConfigListener = new ProjectConfigListener();
    private final BuildConfigListener buildConfigListener = new BuildConfigListener();
    private final GrailsProjectConfig projectConfig;
    private final File projectRoot;
    private final SourceCategoriesFactory sourceCategoriesFactory;

    private List<PathResourceImplementation> resources;
    private GrailsPlatform.Version version;
    private File pluginsDir;
    private File globalPluginsDir;
    private PluginsLibListener listenerPluginsLib;


    private ProjectClassPathImplementation(GrailsProjectConfig projectConfig) {
        this.projectConfig = projectConfig;
        this.projectRoot = FileUtil.toFile(projectConfig.getProject().getProjectDirectory());
        this.version = projectConfig.getGrailsPlatform().getVersion();

        //TODO: would be nice to reuse the project source categories factory here:
        this.sourceCategoriesFactory = new SourceCategoriesFactory();
    }

    public static ProjectClassPathImplementation forProject(Project project) {
        GrailsProjectConfig config = GrailsProjectConfig.forProject(project);
        ProjectClassPathImplementation impl = new ProjectClassPathImplementation(config);

        BuildConfig build = ((GrailsProject) config.getProject()).getBuildConfig();
        build.addPropertyChangeListener(WeakListeners.propertyChange(impl.buildConfigListener, build));

        config.addPropertyChangeListener(WeakListeners.propertyChange(impl.projectConfigListener, config));

        return impl;
    }

    @Override
    public synchronized List<PathResourceImplementation> getResources() {
        if (this.resources == null) {
            this.resources = this.getPath();
        }
        return this.resources;
    }

    private List<PathResourceImplementation> getPath() {
        assert Thread.holdsLock(this);

        BuildConfig buildConfig = ((GrailsProject) projectConfig.getProject()).getBuildConfig();

        List<PathResourceImplementation> result = new ArrayList<>();
        // lib directory from project root
        addLibs(projectRoot, result);

        // compile dependencies
        List<File> compileDeps = buildConfig.getCompileDependencies();
        addJars(compileDeps.toArray(new File[compileDeps.size()]), result, false);

        // FIXME move this to plugin specific support
        // http://grails.org/GWT+Plugin
        GrailsPluginSupport pluginSupport = GrailsPluginSupport.forProject(projectConfig.getProject());
        if (pluginSupport != null && pluginSupport.usesPlugin("gwt")) { // NOI18N
            File gwtDir = new File(new File(projectRoot, sourceCategoriesFactory.getSourceCategory(SourceCategoryType.LIB).getRelativePath()), "gwt"); // NOI18N
            if (gwtDir.exists() && gwtDir.isDirectory()) {
                addJars(gwtDir, result, false);
            }
        }

        // FIXME move this to plugin specific support
        // http://grails.org/plugin/app-engine
        if (pluginSupport != null && pluginSupport.usesPlugin("app-engine")) { // NOI18N
            // FIXME BuilConfig defined value
            String value = System.getenv("APPENGINE_HOME"); // NOI18N
            if (value != null) {
                File appEngineLib = new File(new File(value), "lib"); // NOI18N
                // http://code.google.com/intl/cs/appengine/docs/java/tools/ant.html - classpath
                File lib = new File(appEngineLib, "shared"); // NOI18N
                if (lib.exists() && lib.isDirectory()) {
                    addJars(lib, result, true);
                }
                // not sure about this
                lib = new File(appEngineLib, "user"); // NOI18N
                if (lib.exists() && lib.isDirectory()) {
                    addJars(lib, result, true);
                }
            }
        }

        // in-place plugins
        List<GrailsPlugin> localPlugins = buildConfig.getLocalPlugins();
        for (GrailsPlugin plugin : localPlugins) {
            if (plugin.getPath() != null) {
                addLibs(plugin.getPath(), result);
            }
        }
        // TODO listeners ?

        // project plugins
        File oldPluginsDir = pluginsDir;
        File currentPluginsDir = buildConfig.getProjectPluginsDir();

        if (pluginsDir == null || !pluginsDir.equals(currentPluginsDir)) {
            LOGGER.log(Level.FINE, "Project plugins dir changed from {0} to {1}",
                    new Object[] {pluginsDir, currentPluginsDir});
            this.pluginsDir = currentPluginsDir;
        }

        if (pluginSupport != null && pluginsDir.isDirectory()) {
            addPlugins(pluginsDir, result, pluginSupport.getProjectPluginFilter());
        }

        // global plugins
        // TODO philosophical question: Is the global plugin boot or compile classpath?
        File oldGlobalPluginsDir = globalPluginsDir;
        File currentGlobalPluginsDir = buildConfig.getGlobalPluginsDir();
        if (globalPluginsDir == null || !globalPluginsDir.equals(currentGlobalPluginsDir)) {
            LOGGER.log(Level.FINE, "Project plugins dir changed from {0} to {1}",
                    new Object[] {pluginsDir, currentPluginsDir});
            this.globalPluginsDir = currentGlobalPluginsDir;
        }

        if (globalPluginsDir != null && globalPluginsDir.isDirectory()) {
            addPlugins(globalPluginsDir, result, null);
        }

        // Adding jars from Ivy cache - hopefully it won't hurt start-up performance
        addJars(buildConfig.getIvyCacheDir(), result, true);

        if (listenerPluginsLib == null) {
            File libDir = FileUtil.normalizeFile(new File(projectRoot, "lib")); // NOI18N

            listenerPluginsLib = new PluginsLibListener(this);
            FileUtil.addFileChangeListener(listenerPluginsLib, libDir);
        }

        // project plugins listener
        updateListener(listenerPluginsLib, oldPluginsDir, currentPluginsDir);

        // global plugins listener
        updateListener(listenerPluginsLib, oldGlobalPluginsDir, currentGlobalPluginsDir);

        return Collections.unmodifiableList(result);
    }

    private void updateListener(FileChangeListener listener, File oldDir, File newDir) {
        if (oldDir == null || !oldDir.equals(newDir)) {
            if (oldDir != null) {
                FileUtil.removeFileChangeListener(listener, oldDir);
            }
            if (newDir != null) {
                FileUtil.addFileChangeListener(listener, newDir);
            }
        }
    }

    private void addPlugins(File dir, List<PathResourceImplementation> result, GrailsPluginSupport.FolderFilter filter) {
        for (String name : dir.list()) {
            File file = new File(dir, name);
            if (file.isDirectory() && (filter == null || filter.accept(name))) {
                // lib directories of installed plugins
                addLibs(file, result);
            }
        }
    }

    private void addLibs(File root, List<PathResourceImplementation> result) {
        if (!root.exists() || !root.isDirectory()) {
            return;
        }

        File libDir = new File(root, sourceCategoriesFactory.getSourceCategory(SourceCategoryType.LIB).getRelativePath());
        if (!libDir.exists() || !libDir.isDirectory()) {
            return;
        }

        addJars(libDir, result, false);
    }

    private static void addJars(File dir, List<PathResourceImplementation> result, boolean recurse) {
        addJars(dir.listFiles(), result, recurse);
    }

    private static void addJars(File[] jars, List<PathResourceImplementation> result, boolean recurse) {
        if (jars != null) {
            for (File f : jars) {
                try {
                    if (f.isFile()) {
                        URL entry = Utilities.toURI(f).normalize().toURL();
                        if (FileUtil.isArchiveFile(entry)) {
                            entry = FileUtil.getArchiveRoot(entry);
                            result.add(ClassPathSupport.createResource(entry));
                        }
                    } else if (recurse && f.isDirectory()) {
                        addJars(f, result, recurse);
                    }
                } catch (MalformedURLException mue) {
                    assert false : mue;
                }
            }
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    private class ProjectConfigListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (GrailsProjectConfig.GRAILS_PLATFORM_PROPERTY.equals(evt.getPropertyName())) {
                GrailsPlatform platform = ((GrailsProjectConfig) evt.getSource()).getGrailsPlatform();
                GrailsPlatform.Version currentVersion = platform.getVersion();

                if ((GrailsPlatform.Version.VERSION_1_1.compareTo(currentVersion) <= 0 // 1.1 or above
                        && GrailsPlatform.Version.VERSION_1_1.compareTo(version) > 0) // lower than 1.1
                            || (GrailsPlatform.Version.VERSION_1_1.compareTo(currentVersion) > 0 // lower than 1.1
                                && GrailsPlatform.Version.VERSION_1_1.compareTo(version) <= 0)) { // 1.1 or above

                    LOGGER.log(Level.INFO, "Project classpath changed due to change in {0}", evt.getPropertyName());

                    synchronized (ProjectClassPathImplementation.this) {
                        resources = null;
                    }
                    support.firePropertyChange(ClassPathImplementation.PROP_RESOURCES, null, null);
                }
                version = currentVersion;
            }
        }
    }

    private class BuildConfigListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (BuildConfig.BUILD_CONFIG_PLUGINS.equals(evt.getPropertyName())) {

                LOGGER.log(Level.INFO, "Project classpath changed due to change in {0}", evt.getPropertyName());

                synchronized (ProjectClassPathImplementation.this) {
                    resources = null;
                }
                support.firePropertyChange(ClassPathImplementation.PROP_RESOURCES, null, null);
            }
        }
    }

    private static class PluginsLibListener implements FileChangeListener {

        private final ProjectClassPathImplementation impl;

        public PluginsLibListener(ProjectClassPathImplementation impl) {
            this.impl = impl;
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        @Override
        public void fileChanged(FileEvent fe) {
            fireChange();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            fireChange();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            fireChange();
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            fireChange();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            fireChange();
        }

        private void fireChange() {
            synchronized (impl) {
                impl.resources = null;
            }
            impl.support.firePropertyChange(ClassPathImplementation.PROP_RESOURCES, null, null);
        }
    }
}
