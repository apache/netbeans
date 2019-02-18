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

package org.netbeans.modules.gradle.api;

import org.netbeans.modules.gradle.spi.WatchedResourceProvider;
import org.netbeans.modules.gradle.NbGradleProjectImpl;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/**
 * Facade object for NetBeans Gradle project internals, with some convenience
 * methods.
 *
 *
 * @since 1.0
 * @author Laszlo Kishalmi
 */
public final class NbGradleProject {

    /**
     * As loading a Gradle project information into the memory could be a time
     * consuming task each the Gradle Plugin uses heuristics and offline
     * evaluation of a project in order to provide optimal responsiveness.
     * E.g. If we just need to know if the project is a Gradle project, there
     * is no need to go and fetch all the dependencies.
     * <p>
     * <img src="doc-files/gradle-quality.png" alt="Quality States"/>
     * </p>
     * <p>
     * Gradle project is associated with the quality of the
     * information available at the time. The quality of data can be improved,
     * by reloading the project.
     * </p>
     * @since 1.0
     */
    public static enum Quality {

        /**
         * The data of this project is unreliable, based on heuristics. This is
         * the quickest way to retrieve some information as it the code do not
         * even turns to Gradle for it. Tries to apply some common usage
         * patterns.
         */
        FALLBACK,

        /** The data of this project is unreliable. This usually means that the
         * project was once in a better quality, but some recent change made the
         * the project un-loadable. E.g. syntax error in the recently edited
         * {@code build.gradle} file. The IDE cannot reload it but tries to work with
         * the previously retrieved information. */
        EVALUATED,

        /** The data of this project is reliable, dependency information can be partial though. */
        SIMPLE,

        /** The data of this project is reliable, full dependency information is available offline. */
        FULL,

        /** The data of this project is reliable. with full dependency information. */
        FULL_ONLINE;

        public boolean betterThan(Quality q) {
            return this.ordinal() > q.ordinal();
        }

        public boolean atLeast(Quality q) {
            return this.ordinal() >= q.ordinal();
        }

        public boolean worseThan(Quality q) {
            return this.ordinal() < q.ordinal();
        }

        public boolean notBetterThan(Quality q) {
            return this.ordinal() <= q.ordinal();
        }

    }

    public static final String GRADLE_PROJECT_TYPE = "org-netbeans-modules-gradle";
    public static final String GRADLE_PLUGIN_TYPE = GRADLE_PROJECT_TYPE + "/Plugins";
    /** This property is fired to change on every project reload. */
    public static final String PROP_PROJECT_INFO = "ProjectInfo";
    /** This property is fired when a project watched resource is changed.
     * E.g. a previously non existent source root appears. */
    public static final String PROP_RESOURCES = "resources";

    @StaticResource
    private static final String GRADLE_ICON = "org/netbeans/modules/gradle/resources/gradle.png"; //NOI18
    @StaticResource
    private static final String WARNING_BADGE = "org/netbeans/modules/gradle/resources/warning-badge.png"; //NOI18

    private static Icon warningIcon;
    public static final String CODENAME_BASE = "org.netbeans.modules.gradle";

    private final NbGradleProjectImpl project;
    private final PropertyChangeSupport support;
    private final Set<File> resources = new HashSet<>();

    private Preferences privatePrefs;
    private Preferences sharedPrefs;

    static {
        AccessorImpl impl = new AccessorImpl();
        impl.assign();
    }

    public boolean isGradleProjectLoaded() {
        return project.isGradleProjectLoaded();
    }

    static class AccessorImpl extends NbGradleProjectImpl.WatcherAccessor {

        public void assign() {
            if (NbGradleProjectImpl.ACCESSOR == null) {
                NbGradleProjectImpl.ACCESSOR = this;
            }
        }

        @Override
        public NbGradleProject createWatcher(NbGradleProjectImpl proj) {
            return new NbGradleProject(proj);
        }

        @Override
        public void doFireReload(NbGradleProject watcher) {
            watcher.doFireReload();
        }

        @Override
        public void activate(NbGradleProject watcher) {
            watcher.attachResourceWatchers();
        }

        @Override
        public void passivate(NbGradleProject watcher) {
            watcher.detachResourceWatchers();
        }
    }

    private NbGradleProject(NbGradleProjectImpl project) {
        this.project = project;
        support = new PropertyChangeSupport(project);
    }

    public <T> T projectLookup(Class<T> clazz) {
        return project.getGradleProject().getLookup().lookup(clazz);
    }

    /**
     * Return the actual Quality information on the currently loaded Project.
     *
     * @return the information Quality of the project data;
     */
    public Quality getQuality() {
        return project.getGradleProject().getQuality();
    }

    /**
     * The requested information on this project. Mostly FALLBACK or FULL.
     * @return the information Quality requested.
     */
    public Quality getAimedQuality() {
        return project.getAimedQuality();
    }

    /**
     * The project is unloadable if it's actual quality is worse than {@link Quality#SIMPLE}.
     * @return true if the project is unloadable.
     */
    public boolean isUnloadable() {
        return getQuality().worseThan(Quality.SIMPLE);
    }

    public Preferences getPreferences(boolean shared) {
        Preferences ret = shared ? sharedPrefs : privatePrefs;
        if (ret == null) {
            if (shared) {
                ret = sharedPrefs = ProjectUtils.getPreferences(project, NbGradleProject.class, true);
            } else {
                ret = privatePrefs = ProjectUtils.getPreferences(project, NbGradleProject.class, false);
            }
        }
        return ret;
    }

    private void fireProjectReload() {
        project.fireProjectReload(false);
    }

    private void doFireReload() {
        detachResourceWatchers();
        support.firePropertyChange(PROP_PROJECT_INFO, null, null);
        attachResourceWatchers();
    }

    private void detachResourceWatchers() {
        for (File resource : resources) {
            try {
                FileUtil.removeFileChangeListener(FCHSL, resource);
            } catch (IllegalArgumentException ex) {
                assert false : "Something is wrong with the resource handling";
            }
        }
        resources.clear();
    }

    private void attachResourceWatchers() {
        //Never listen on resource changes when only FALLBACK quality is needed
        if (project.getAimedQuality() == Quality.FALLBACK) return;

        Collection<? extends WatchedResourceProvider> all
                = project.getLookup().lookupAll(WatchedResourceProvider.class);
        for (WatchedResourceProvider pvd : all) {
            resources.addAll(pvd.getWatchedResources());
        }
        for (File resource : resources) {
            try {
                FileUtil.addFileChangeListener(FCHSL, resource);
            } catch (IllegalArgumentException ex) {
                assert false : "Something is wrong with the resource handling";
            }
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener(propertyChangeListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener(propertyChangeListener);
    }

    /**
     * Retrieves the watcher for the given project. Usually the project watcher
     * can be retrieved from the project Lookup. This implementation does not
     * use the project Lookup, so it can be used inside of the constructors
     * of ProjectServiceProvider implementations.
     *
     * @param project the project to query
     * @return the watcher of the project or {@code null} if the given project
     *         is not a Gradle project.
     */
    public static NbGradleProject get(Project project) {
        return project instanceof NbGradleProjectImpl ? ((NbGradleProjectImpl) project).getProjectWatcher() : null;
    }

    @Override
    public String toString() {
        return "Watcher for " + project.toString(); //NOI18N
    }

    public static ImageIcon getIcon() {
        return ImageUtilities.loadImageIcon(GRADLE_ICON, false);
    }

    public static final Icon getWarningIcon() {
        if (warningIcon == null) {
            Image icon = ImageUtilities.icon2Image(NbGradleProject.getIcon());
            Image badge = ImageUtilities.loadImage(WARNING_BADGE);
            icon = ImageUtilities.mergeImages(icon, badge, 8, 0);
            warningIcon = ImageUtilities.image2Icon(icon);
        }
        return warningIcon;
    }

    /**
     * Convenient method to add a Property Listener to a Gradle project.
     *
     * @param project
     * @param l
     */
    public static void addPropertyChangeListener(Project project, PropertyChangeListener l) {
        if (project != null && project instanceof NbGradleProjectImpl) {
            ((NbGradleProjectImpl) project).getProjectWatcher().addPropertyChangeListener(l);
        } else {
            assert false : "Attempted to add PropertyChangeListener to project " + project; //NOI18N
        }
    }

    /**
     * Convenient method to remove a Property Listener from a Gradle project.
     *
     * @param project
     * @param l
     */
    public static void removePropertyChangeListener(Project project, PropertyChangeListener l) {
        if (project != null && project instanceof NbGradleProjectImpl) {
            ((NbGradleProjectImpl) project).getProjectWatcher().removePropertyChangeListener(l);
        } else {
            assert false : "Attempted to remove PropertyChangeListener to project " + project; //NOI18N
        }
    }

    public static void fireGradleProjectReload(Project prj) {
        if (prj != null) {
            NbGradleProject watcher = NbGradleProject.get(prj);
            if (watcher != null) {
                watcher.fireProjectReload();
            }
        }
    }

    public static Preferences getPreferences(Project project, boolean shared) {
        NbGradleProject watcher = NbGradleProject.get(project);
        return watcher.getPreferences(shared);
    }

    private void fireChange(File f) {
        support.firePropertyChange(PROP_RESOURCES, null, Utilities.toURI(f));
    }

    private final FileChangeListener FCHSL = new FileChangeListener() {

        @Override
        public void fileFolderCreated(FileEvent fe) {
            fireChange(FileUtil.toFile(fe.getFile()));
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            fireChange(FileUtil.toFile(fe.getFile()));
        }

        @Override
        public void fileChanged(FileEvent fe) {
            fireChange(FileUtil.toFile(fe.getFile()));
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            fireChange(FileUtil.toFile(fe.getFile()));
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            fireChange(FileUtil.toFile(fe.getFile()));
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

    };
}
