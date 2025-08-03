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

package org.netbeans.modules.projectapi.nb;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ProjectIconAnnotator;
import org.netbeans.spi.project.ProjectInformationProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.*;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service = ProjectInformationProvider.class, position = 100)
public final class NbProjectInformationProvider implements ProjectInformationProvider {

    private static final Logger LOGGER = Logger.getLogger(NbProjectInformationProvider.class.getName());

    @Override
    @NonNull
    public ProjectInformation getProjectInformation(Project project) {
        final ProjectInformation pi = project.getLookup().lookup(ProjectInformation.class);
        return new AnnotateIconProxyProjectInformation(
            pi != null ? pi : new BasicInformation(project));
    }

    private static final class BasicInformation implements ProjectInformation {

        private final Project p;

        public BasicInformation(Project p) {
            this.p = p;
        }

        @Override
        public String getName() {
            return getProjectDirectory().toURL().toExternalForm();
        }

        @Override
        public String getDisplayName() {
            return getProjectDirectory().getNameExt();
        }

        @Override
        public Icon getIcon() {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/projectapi/resources/empty.gif", false); // NOI18N
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            // never changes
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            // never changes
        }

        @Override
        public Project getProject() {
            return p;
        }

        @NonNull
        private FileObject getProjectDirectory() {
            final FileObject pd = p.getProjectDirectory();
            if (pd == null) {
                throw new IllegalStateException(String.format("Project: %s returned null project directory.", p));  //NOI18N
            }
            return pd;
        }

    }

    private static final class AnnotateIconProxyProjectInformation implements ProjectInformation, PropertyChangeListener, ChangeListener, LookupListener {

        private final ProjectInformation pinfo;
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private final Set<ProjectIconAnnotator> annotators = Collections.newSetFromMap(new WeakHashMap<>());
        private boolean annotatorsInitialized = false;
        private boolean addedPropertyListener = false;
        private final Object LOCK = new Object(); //protects access to annotatorsInitialized, addedPropertyListener and icon
        private Lookup.Result<ProjectIconAnnotator> annotatorResult;
        private Icon icon;

        @SuppressWarnings("LeakingThisInConstructor")
        public AnnotateIconProxyProjectInformation(ProjectInformation pi) {
            pinfo = pi;
        }

        private void annotatorsChanged() {
            synchronized (LOCK) {
                if (!annotatorsInitialized) {
                    annotatorResult = Lookup.getDefault().lookupResult(ProjectIconAnnotator.class);
                    annotatorResult.addLookupListener(WeakListeners.create(LookupListener.class, this, annotatorResult));
                    annotatorsInitialized = true;
                }
                for (ProjectIconAnnotator pa : annotatorResult.allInstances()) {
                    if (annotators.add(pa)) {
                        pa.addChangeListener(WeakListeners.change(this, pa));
                    }
                }
            }
        }

        public @Override void resultChanged(LookupEvent ev) {
            annotatorsChanged();
            updateIcon(true);
        }

        public @Override void propertyChange(PropertyChangeEvent evt) {
            if (ProjectInformation.PROP_ICON.equals(evt.getPropertyName())) {
                synchronized (LOCK) {
                    if (!annotatorsInitialized) {
                        annotatorsChanged();
                    }
                }
                updateIcon(true);
            } else {
                pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        }

        public @Override void stateChanged(ChangeEvent e) {
            updateIcon(true);
        }

        private void updateIcon(boolean fireChange) {
            Icon original = pinfo.getIcon();
            if (original == null) {
                // Forbidden generally but common in tests.
                LOGGER.log(Level.WARNING, "no icon for {0}", pinfo);
                return;
            }
            Image _icon = ImageUtilities.icon2Image(original);
            final Project prj = getProject();
            assert prj != null : "ProjectIformation.getProject() == null for " + pinfo;    //NOI18N
            if (prj != null) {
                for (ProjectIconAnnotator pa : annotatorResult.allInstances()) {
                    _icon = pa.annotateIcon(prj, _icon, false);
                }
            }
            Icon old = icon;
            Icon newOne;
            synchronized (LOCK) {
                icon = ImageUtilities.image2Icon(_icon);
                newOne = icon;
            }
            if (fireChange) {
                pcs.firePropertyChange(ProjectInformation.PROP_ICON, old, newOne);
            }
        }

        public @Override Icon getIcon() {
            synchronized (LOCK) {
                if (icon == null) {
                    if (!annotatorsInitialized) {
                        annotatorsChanged();
                    }
                    updateIcon(false);
                }
                return icon;
            }
        }

        public @Override void addPropertyChangeListener(PropertyChangeListener listener) {
            synchronized (LOCK) {
                if (!addedPropertyListener) {
                    pinfo.addPropertyChangeListener(WeakListeners.propertyChange(this, pinfo));
                    addedPropertyListener = true;
                }
            }
            pcs.addPropertyChangeListener(listener);
        }

        public @Override void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

        public @Override Project getProject() {
            return pinfo.getProject();
        }
        public @Override String getName() {
            return pinfo.getName();
        }
        public @Override String getDisplayName() {
            return pinfo.getDisplayName();
        }

    }

}
