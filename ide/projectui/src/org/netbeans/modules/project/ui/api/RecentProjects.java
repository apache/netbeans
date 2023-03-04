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

package org.netbeans.modules.project.ui.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.util.List;

import org.netbeans.modules.project.ui.OpenProjectList;

/**
 * Provides simple information about recent projects and fires PropertyChangeEvent
 * in case of change in the list of recent projects
 * @author Milan Kubec
 * @since 1.9.0
 */
public final class RecentProjects {

    /**
     * Property representing recent project information
     */
    public static final String PROP_RECENT_PROJECT_INFO = "RecentProjectInformation"; // NOI18N
    
    private static final RecentProjects INSTANCE = new RecentProjects();
    
    private PropertyChangeSupport pch;
    
    public static RecentProjects getDefault() {
        return INSTANCE;
    }
    
    /**
     * Creates a new instance of RecentProjects
     */
    private RecentProjects() {
        pch = new PropertyChangeSupport(this);
        OpenProjectList.getDefault().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(OpenProjectList.PROPERTY_RECENT_PROJECTS)) {
                    pch.firePropertyChange(new PropertyChangeEvent(RecentProjects.class,
                            PROP_RECENT_PROJECT_INFO, null, null));
                }
            }
        });
    }
    
    /**
     * Gets simple info {@link org.netbeans.modules.project.ui.api.UnloadedProjectInformation} about recent projects in IDE.
     * Project in the list might not exist or might not be valid e.g. in case when
     * project was deleted or changed. It's responsibility of the user of the API
     * to make sure the project exists and is valid.
     * @return list of project information about recently opened projects
     */
    public List<UnloadedProjectInformation> getRecentProjectInformation() {
        return OpenProjectList.getDefault().getRecentProjectsInformation();
    }
    
    /**
     * Adds a listener, use WeakListener or properly remove listeners
     * @param listener listener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pch.addPropertyChangeListener(listener);
    }
    
    /**
     * Removes a listener
     * @param listener listener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pch.removePropertyChangeListener(listener);
    }
    
}
