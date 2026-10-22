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

import java.awt.Image;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * The logical view of a generic project simply shows all files contained
 * in the project directory.
 *
 * <p>The root node exposes the project, the project directory
 * {@link FileObject} and the directory's {@link DataObject} in its
 * lookup, which is what the stock project actions rely on (e.g.
 * "New File" uses the {@link DataObject} to determine the target
 * folder). Its action set is the standard one, registered in the layer
 * under {@code Projects/<PROJECT_KEY>/Actions} and loaded via
 * {@link CommonProjectActions#forType}.
 */
public record GpLogicalViewProvider(GenericProject project) implements LogicalViewProvider {

    private static final Logger LOG = Logger.getLogger(GpLogicalViewProvider.class.getName()); // NOI18N

    @Override
    public Node createLogicalView() {
        FileObject dir = project.getProjectDirectory();
        Children children;
        try {
            children = DataFolder.findFolder(dir).createNodeChildren(DataFilter.ALL);
        } catch (Exception ex) {
            children = Children.LEAF;
        }
        ProjectInformation info = ProjectUtils.getInformation(project);
        InstanceContent content = new InstanceContent();
        AbstractNode base = new AbstractNode(children, new AbstractLookup(content)) {
            {
                // keep the tree label in sync with the display name
                // override (rename without renaming the folder)
                info.addPropertyChangeListener(evt -> {
                    if (ProjectInformation.PROP_DISPLAY_NAME.equals(evt.getPropertyName())) {
                        fireDisplayNameChange(null, null);
                    }
                });
            }

            @Override
            public String getDisplayName() {
                // honors the display name override (rename without
                // renaming the folder)
                return info.getDisplayName();
            }

            @Override
            public Action[] getActions(boolean context) {
                // the stock actions registered in the layer under
                // "Projects/<PROJECT_KEY>/Actions" (shadows of the
                // stock project actions plus the shared
                // "Projects/Actions" folder, e.g. versioning)
                return CommonProjectActions.forType(GenericProject.PROJECT_KEY);
            }

            @Override
            public Image getIcon(int type) {
                return ImageUtilities.loadImage(GenericProject.ICON);
            }

            @Override
            public Image getOpenedIcon(int type) {
                return getIcon(type);
            }
        };
        content.add(project);
        content.add(dir);
        // also expose the DataObject of the project directory: stock
        // actions (e.g. "New File") use it to determine the target
        // folder
        content.add(project, new InstanceContent.Convertor<GenericProject, DataObject>() {
            @Override
            public DataObject convert(GenericProject obj) {
                try {
                    FileObject fo = obj.getProjectDirectory();
                    return fo != null && fo.isValid() ? DataObject.find(fo) : null;
                } catch (DataObjectNotFoundException ex) {
                    LOG.log(Level.WARNING, null, ex);
                    return null;
                }
            }

            @Override
            public Class<? extends DataObject> type(GenericProject obj) {
                return DataObject.class;
            }

            @Override
            public String id(GenericProject obj) {
                return dir.getPath();
            }

            @Override
            public String displayName(GenericProject obj) {
                return dir.getNameExt();
            }
        });
        base.setName(dir.getNameExt());
        return base;
    }

    @Override
    public Node findPath(Node root, Object target) {
        return target instanceof FileObject fo && fo.equals(project.getProjectDirectory()) ? root : null;
    }

}
