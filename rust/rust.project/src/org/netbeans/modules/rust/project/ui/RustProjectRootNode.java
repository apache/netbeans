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
package org.netbeans.modules.rust.project.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.Action;
import org.netbeans.modules.rust.cargo.api.CargoTOML;
import org.netbeans.modules.rust.project.RustProject;
import org.netbeans.modules.rust.project.api.RustProjectAPI;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.nodes.AbstractNode;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * The root project of the project.
 */
public class RustProjectRootNode extends AbstractNode implements PropertyChangeListener {

    private final RustProject project;

    public RustProjectRootNode(RustProject project) {
        this(project, new InstanceContent());
    }

    private RustProjectRootNode(RustProject project, InstanceContent content) {
        super(NodeFactorySupport.createCompositeChildren(project, 
                "Projects/" + RustProjectAPI.RUST_PROJECT_KEY + "/Nodes"), // NOI18N
                new AbstractLookup(content));
        this.project = project;
        content.add(project);
        this.project.getCargoTOML().addPropertyChangeListener(this);
        setName(project.getCargoTOML().getPackageName());
        setDisplayName(project.getCargoTOML().getPackageName());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();

        if (CargoTOML.PROP_PACKAGENAME.equals(property)) {
            setDisplayName(project.getCargoTOML().getPackageName());
        }
    }

    @Override
    public void destroy() throws IOException {
        project.getCargoTOML().removePropertyChangeListener(this);
    }

    @Override
    public Action[] getActions(boolean context) {
        Action [] actionArray = CommonProjectActions.forType(RustProjectAPI.RUST_PROJECT_KEY);

        return actionArray;
    }

    

    @Override
    public String getShortDescription() {
        String description = project.getCargoTOML().getDescription();
        description = description == null ? String.format("Rust project %s", getDisplayName()) : description;
        return description;
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage(RustProjectAPI.ICON);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

}
