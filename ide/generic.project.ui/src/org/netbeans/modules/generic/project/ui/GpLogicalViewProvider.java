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

package org.netbeans.modules.generic.project.ui;

import java.awt.Image;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.generic.project.GenericProject;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
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
 * The logical view of a generic project shows the files contained
 * in the project directory, filtered by the IDE's global "ignored
 * files" pattern unless the project has disabled it in its
 * customizer (see {@link GpFileFilter}).
 *
 * <p>The root node exposes the project, the project directory
 * {@link FileObject} and the directory's {@link DataObject} in its
 * lookup, which is what the stock project actions rely on (e.g.
 * "New File" uses the {@link DataObject} to determine the target
 * folder). Its action set is the standard one, registered in the layer
 * under {@code Projects/<PROJECT_KEY>/Actions} and loaded via
 * {@link CommonProjectActions#forType}.
 */
@ProjectServiceProvider(service=LogicalViewProvider.class, projectType=GenericProject.PROJECT_KEY)
public final class GpLogicalViewProvider implements LogicalViewProvider {

    private static final Logger LOG = Logger.getLogger(GpLogicalViewProvider.class.getName()); // NOI18N

    private final Project project;

    public GpLogicalViewProvider(Project project) {
        this.project = project;
    }

    @Override
    public Node createLogicalView() {
        FileObject dir = project.getProjectDirectory();
        Children children;
        try {
            // the filter needs the project's own settings accessors
            children = DataFolder.findFolder(dir).createNodeChildren(new GpFileFilter((GenericProject) project));
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
        content.add(project, new InstanceContent.Convertor<Project, DataObject>() {
            @Override
            public DataObject convert(Project obj) {
                try {
                    FileObject fo = obj.getProjectDirectory();
                    return fo != null && fo.isValid() ? DataObject.find(fo) : null;
                } catch (DataObjectNotFoundException ex) {
                    LOG.log(Level.WARNING, null, ex);
                    return null;
                }
            }

            @Override
            public Class<? extends DataObject> type(Project obj) {
                return DataObject.class;
            }

            @Override
            public String id(Project obj) {
                return dir.getPath();
            }

            @Override
            public String displayName(Project obj) {
                return dir.getNameExt();
            }
        });
        base.setName(dir.getNameExt());
        return base;
    }

    @Override
    public Node findPath(Node root, Object target) {
        if (!(target instanceof FileObject fo)) {
            return null;
        }
        FileObject dir = project.getProjectDirectory();
        return dir.equals(fo) || FileUtil.isParentOf(dir, fo) ? findDataNode(root, fo) : null;
    }

    /**
     * Finds the node of the given file in the logical view: the node
     * with the {@link DataObject} of that file in its lookup
     * ({@link DataNode}s created by
     * {@link DataFolder#createNodeChildren} qualify), searching
     * depth-first.
     */
    private static Node findDataNode(Node node, FileObject target) {
        DataObject dobj = node.getLookup().lookup(DataObject.class);
        if (dobj != null && dobj.getPrimaryFile().equals(target)) {
            return node;
        }
        for (Node child : node.getChildren().getNodes(true)) {
            Node found = findDataNode(child, target);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

}
