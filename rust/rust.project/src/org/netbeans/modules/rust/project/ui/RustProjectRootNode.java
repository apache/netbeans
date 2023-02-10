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

import org.netbeans.modules.rust.project.ui.src.RustProjectSrcNode;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.rust.project.RustProject;
import org.netbeans.modules.rust.project.api.RustProjectAPI;
import org.netbeans.modules.rust.project.cargotoml.CargoTOML;
import org.netbeans.modules.rust.project.ui.important.RustProjectImportantFilesNode;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * The root project of the project.
 */
public class RustProjectRootNode extends AbstractNode implements PropertyChangeListener {

    private enum ROOT_CHILDREN {
        SRC,
        DEPENDENCIES,
        IMPORTANT_FILES,
    };

    private static final class RustProjectRootNodeChildren extends Children.Keys<ROOT_CHILDREN> implements PropertyChangeListener {

        private final RustProject project;

        RustProjectRootNodeChildren(RustProject project) {
            this.project = project;
        }

        @Override
        protected void addNotify() {
            project.getCargoTOML().addPropertyChangeListener(this);
            setKeys(ROOT_CHILDREN.values());
        }

        @Override
        protected void removeNotify() {
            project.getCargoTOML().removePropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String name = evt.getPropertyName();
            // TODO: Listen for "dependencies" and update the dependencies node.
        }

        @Override
        protected Node[] createNodes(ROOT_CHILDREN key) {
            switch (key) {
                case SRC: 
                    try {
                    return new Node[]{new RustProjectSrcNode(project)};
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                    return new Node[0];
                }
                case DEPENDENCIES:
                    // TODO: Add dependencies.
                    return new Node[0];
                case IMPORTANT_FILES:
                    try {
                    return new Node[]{new RustProjectImportantFilesNode(project)};
                } catch (Throwable e) {
                    Exceptions.printStackTrace(e);
                }
            }
            return new Node[0];
        }

    }

    private final RustProject project;

    public RustProjectRootNode(RustProject project) {
        this(project, new InstanceContent());
    }

    private RustProjectRootNode(RustProject project, InstanceContent content) {
        super(new RustProjectRootNodeChildren(project), new AbstractLookup(content));
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
