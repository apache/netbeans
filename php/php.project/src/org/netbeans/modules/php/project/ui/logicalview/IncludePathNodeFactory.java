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
package org.netbeans.modules.php.project.ui.logicalview;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.customizer.CompositePanelProviderImpl;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 * @author Radek Matous
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-php-project", position=300)
public class IncludePathNodeFactory implements NodeFactory {

    public IncludePathNodeFactory() {
    }

    @Override
    public NodeList<?> createNodes(Project p) {
        final PhpProject project = p.getLookup().lookup(PhpProject.class);
        return NodeFactorySupport.fixedNodeList(new Nodes.DummyNode(new IncludePathRootNode(project, new IncludePathChildFactory(project))) {
            @Override
            public Action[] getActions(boolean context) {
                return new Action[]{new PhpLogicalViewProvider.CustomizeProjectAction(project, CompositePanelProviderImpl.PHP_INCLUDE_PATH)};
            }
        });
    }

    private static class IncludePathRootNode extends AbstractNode implements PropertyChangeListener {

        private final PhpProject project;
        private final IncludePathChildFactory childFactory;


        public IncludePathRootNode(PhpProject project, IncludePathChildFactory childFactory) {
            super(Children.create(childFactory, true));
            this.project = project;
            this.childFactory = childFactory;
            ProjectPropertiesSupport.addWeakPropertyEvaluatorListener(project, this);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(IncludePathNodeFactory.class, "LBL_IncludePath"); // NOI18N
        }

        @Override
        public Image getIcon(int type) {
            return getIcon(true);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(false);
        }

        private Image getIcon(boolean opened) {
            return Utils.getIncludePathIcon(opened);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            childFactory.refresh();
        }

    }

    private static class IncludePathChildFactory extends Nodes.FileChildFactory {

        public IncludePathChildFactory(PhpProject project) {
            super(project);
        }

        @Override
        protected List<Node> getNodes() {
            List<Node> list = new ArrayList<>();
            // #172092
            List<FileObject> includePath = ProjectManager.mutex().readAccess(new Mutex.Action<List<FileObject>>() {
                @Override
                public List<FileObject> run() {
                    return PhpSourcePath.getIncludePath(project.getProjectDirectory());
                }
            });
            for (FileObject fileObject : includePath) {
                if (fileObject != null && fileObject.isFolder()) {
                    DataFolder df = DataFolder.findFolder(fileObject);
                    list.add(new IncludePathNode(df, project));
                }
            }
            return list;
        }

        public void refresh() {
            refresh(false);
        }

    }

    private static class IncludePathNode extends Nodes.FileNode {

        @StaticResource
        private static final String ICON_PATH = "org/netbeans/modules/php/project/ui/resources/libraries.gif"; //NOI18N
        private static final ImageIcon ICON = ImageUtilities.loadImageIcon(ICON_PATH, false);

        public IncludePathNode(DataObject dobj, PhpProject project) {
            super(dobj, project);
        }

        @Override
        public Image getIcon(int type) {
            return ICON.getImage();
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
    }
}
