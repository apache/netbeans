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

package org.netbeans.modules.gradle.queries;

import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.GradleSettings;
import org.netbeans.modules.gradle.spi.ProjectIconProvider;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.ImageUtilities;
import org.openide.util.WeakListeners;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = ProjectInformation.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public final class Info implements ProjectInformation, PropertyChangeListener {

    @StaticResource
    private static final String GRADLE_BADGE = "org/netbeans/modules/gradle/resources/gradle-large-badge.png"; //NOI18

    private final Project project;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final PreferenceChangeListener preferenceChangeListener = (PreferenceChangeEvent evt) -> {
        if (GradleSettings.PROP_DISPLAY_DESCRIPTION.equals(evt.getKey())) {
            pcs.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME, null, null);
        }
    };
    private final AtomicBoolean prefChangeListenerSet = new AtomicBoolean(false);

    public Info(final Project project) {
        this.project = project;
    }

    @Override
    public String getName() {
        GradleBaseProject prj = GradleBaseProject.get(project);

        String ret = prj.isRoot() ? prj.getName() : prj.getRootDir().getName() + prj.getPath();
        return ret;
    }

    @Override
    public String getDisplayName() {
        final NbGradleProject nb = project.getLookup().lookup(NbGradleProject.class);
        if (SwingUtilities.isEventDispatchThread() && !nb.isGradleProjectLoaded()) {
            return project.getProjectDirectory().getNameExt();
        }

        GradleBaseProject prj = GradleBaseProject.get(project);
        String ret;
        if (GradleSettings.getDefault().isDisplayDesctiption()
                && (prj.getDescription() != null)
                && !prj.getDescription().isEmpty()) {
            ret = prj.getDescription();
        } else {
            // The current implementation of Gradle's displayName is kind of ugly
            // and cannot be configured.
            //ret = prj.getDisplayName() != null ? prj.getDisplayName() : getName();
            ret = getName();
        }
        return ret;
    }

    @Override
    public Icon getIcon() {
        Collection<? extends ProjectIconProvider> providers = project.getLookup().lookupAll(ProjectIconProvider.class);
        Image icon = null;
        for (ProjectIconProvider provider : providers) {
            icon = provider.getIcon();
            if (icon != null) {
                if (provider.isGradleBadgeRequested()) {
                    Image badge = ImageUtilities.loadImage(GRADLE_BADGE);
                    icon = ImageUtilities.mergeImages(icon, badge, 0, 0);
                }
                break;
            }
        }
        return icon != null ? ImageUtilities.image2Icon(icon) : NbGradleProject.getIcon();
    }

    @Override
    public Project getProject() {
        return project;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (prefChangeListenerSet.compareAndSet(false, true)) {
            Preferences prefs = GradleSettings.getDefault().getPreferences();
            prefs.addPreferenceChangeListener(WeakListeners.create(
                    PreferenceChangeListener.class, preferenceChangeListener, prefs));
        }
        if (!pcs.hasListeners(null)) {
            project.getLookup().lookup(NbGradleProject.class).addPropertyChangeListener(this);
        }
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        boolean had = pcs.hasListeners(null);
        pcs.removePropertyChangeListener(listener);
        if (had && !pcs.hasListeners(null)) {
            project.getLookup().lookup(NbGradleProject.class).removePropertyChangeListener(this);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
            pcs.firePropertyChange(PROP_NAME, null, null);
            pcs.firePropertyChange(PROP_DISPLAY_NAME, null, null);
            pcs.firePropertyChange(PROP_ICON, null, null);
        }
    }

}
