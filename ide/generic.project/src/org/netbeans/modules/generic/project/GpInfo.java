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

package org.netbeans.modules.generic.project;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.Icon;
import javax.swing.UIManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.openide.util.ImageUtilities;

/**
 * The {@link ProjectInformation} of a generic project. The display
 * name is the project folder name unless a display name override is
 * stored in the project's private preferences (see
 * {@link GenericProject#getDisplayNameOverride}).
 */
final class GpInfo implements ProjectInformation {

    private final GenericProject prj;
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    GpInfo(GenericProject prj) {
        this.prj = prj;
    }

    @Override
    public String getName() {
        return prj.getProjectDirectory().getNameExt();
    }

    @Override
    public String getDisplayName() {
        String override = GenericProject.getDisplayNameOverride(prj);
        return override != null ? override : prj.getProjectDirectory().getNameExt();
    }

    @Override
    public Icon getIcon() {
        Icon icon = ImageUtilities.loadIcon(GenericProject.ICON);
        return icon != null ? icon : UIManager.getIcon("Nb.Explorer.Folder.icon"); // NOI18N
    }

    @Override
    public Project getProject() {
        return prj;
    }

    /**
     * Notifies listeners that the display name changed.
     */
    void fireDisplayNameChanged() {
        changeSupport.firePropertyChange(PROP_DISPLAY_NAME, null, null);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }
}
