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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.modules.rust.cargo.api.CargoTOML;
import org.netbeans.modules.rust.project.RustProject;
import org.netbeans.modules.rust.project.api.RustProjectAPI;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * The root project of the project.
 */
public class RustProjectRootNode extends AbstractNode implements PropertyChangeListener {

    private static final Logger LOG = Logger.getLogger(RustProjectRootNode.class.getName());

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
        content.add(project.getProjectDirectory());
        content.add(project, new InstanceContent.Convertor<RustProject, DataObject>() {
                    @Override
                    public DataObject convert(RustProject obj) {
                        try {
                            final FileObject fo = obj.getProjectDirectory();
                            return fo != null && fo.isValid() ? DataObject.
                                    find(fo) : null;
                        } catch (DataObjectNotFoundException ex) {
                            LOG.log(Level.WARNING, null, ex);
                            return null;
                        }
                    }

                    @Override
                    public Class<? extends DataObject> type(RustProject obj) {
                        return DataObject.class;
                    }

                    @Override
                    public String id(RustProject obj) {
                        final FileObject fo = obj.getProjectDirectory();
                        return fo == null ? "" : fo.getPath();  // NOI18N
                    }

                    @Override
                    public String displayName(RustProject obj) {
                        return obj.toString();
                    }

                });

        this.setDisplayName(project.getCargoTOML().getName());
        this.setShortDescription(project.getCargoTOML().getDescription());
        this.project.getCargoTOML().addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        if (CargoTOML.PROP_NAME.equals(property)) {
            setDisplayName(project.getCargoTOML().getName());
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
        return project.getCargoTOML().getDescription();
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
