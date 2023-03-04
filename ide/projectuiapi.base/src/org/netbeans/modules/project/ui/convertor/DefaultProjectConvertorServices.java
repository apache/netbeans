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
package org.netbeans.modules.project.ui.convertor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Collections;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import static org.netbeans.api.project.ProjectInformation.PROP_DISPLAY_NAME;
import static org.netbeans.api.project.ProjectInformation.PROP_ICON;
import static org.netbeans.api.project.ProjectInformation.PROP_NAME;
import org.netbeans.modules.project.uiapi.ProjectConvertorServiceFactory;
import org.netbeans.spi.project.ui.ProjectConvertor;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service = ProjectConvertorServiceFactory.class)
public class DefaultProjectConvertorServices implements ProjectConvertorServiceFactory {

    @Override
    public Collection<?> createServices(
            @NonNull final Project project,
            @NonNull final ProjectConvertor.Result result) {
        return Collections.singleton(new ProjectInfo(project, result));
    }

    //<editor-fold defaultstate="collapsed" desc="ProjectInformation implementation">
    private static final class ProjectInfo implements ProjectInformation, LookupListener {

        private final Project project;
        private final ProjectConvertor.Result result;
        private final PropertyChangeSupport pcs;
        private final Lookup.Result<ProjectInformation> eventSource;
        private volatile ProjectInformation delegate;

        ProjectInfo(
                @NonNull final Project project,
                @NonNull final ProjectConvertor.Result result) {
            Parameters.notNull("project", project); //NOI18N
            Parameters.notNull("result", result);   //NOI18N
            this.project = project;
            this.result = result;
            this.pcs = new PropertyChangeSupport(this);
            this.eventSource = project.getLookup().lookupResult(ProjectInformation.class);
            this.eventSource.addLookupListener(WeakListeners.create(LookupListener.class, this, eventSource));
        }

        @Override
        @NonNull
        public String getName() {
            final ProjectInformation d = delegate;
            return d != null ?
                d.getName() :
                project.getProjectDirectory().getName();
        }

        @Override
        @NonNull
        public String getDisplayName() {
            final ProjectInformation d = delegate;
            if (d != null) {
                return d.getDisplayName();
            } else {
                String res = result.getDisplayName();
                if (res == null) {
                    res = getName();
                }
                return res;
            }
        }

        @Override
        @NonNull
        public Icon getIcon() {
            final ProjectInformation d = delegate;
            if (d != null) {
                return d.getIcon();
            } else {
                Icon res = result.getIcon();
                //Todo: Handle null res
                return res;
            }
        }

        @Override
        @NonNull
        public Project getProject() {
            final ProjectInformation d = delegate;
            if (d != null) {
                return d.getProject();
            } else {
                return project;
            }
        }

        @Override
        public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            pcs.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            pcs.removePropertyChangeListener(listener);
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            //In case someone holds this transient ProjectInfo
            //keep it alive and delegate to real one.
            final Collection<? extends ProjectInformation> instances = eventSource.allInstances();
            if (!instances.isEmpty()) {
                final ProjectInformation instance = instances.iterator().next();
                if (instance != this) {
                    delegate = instance;
                    pcs.firePropertyChange(PROP_NAME, null, null);
                    pcs.firePropertyChange(PROP_DISPLAY_NAME, null, null);
                    pcs.firePropertyChange(PROP_ICON, null, null);
                }
            }
        }
    }
    //</editor-fold>
}
