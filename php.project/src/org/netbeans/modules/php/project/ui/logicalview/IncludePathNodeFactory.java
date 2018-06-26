/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
