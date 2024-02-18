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
package org.netbeans.modules.rust.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Optional;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.SwingPropertyChangeSupport;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.rust.cargo.api.CargoTOML;
import org.netbeans.modules.rust.cargo.api.RustPackage;
import org.netbeans.modules.rust.project.api.RustProjectAPI;
import org.openide.util.ImageUtilities;

/**
 * Rust Project Information.
 */
final class RustProjectInformation implements ProjectInformation, PropertyChangeListener {

    private final RustProject project;
    private final PropertyChangeSupport pcs;

    RustProjectInformation(RustProject project) {
        this.project = project;
        project.getCargoTOML().addPropertyChangeListener(this);
        this.pcs = new SwingPropertyChangeSupport(this);
    }

    @Override
    public String getName() {
        return project.getCargoTOML().getName();
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(ImageUtilities.loadImage(RustProjectAPI.ICON));
    }

    @Override
    public Project getProject() {
        return project;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        if (CargoTOML.PROP_NAME.equals(property)) {
            pcs.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME, evt.getOldValue(), evt.getNewValue());
        }
    }

}
