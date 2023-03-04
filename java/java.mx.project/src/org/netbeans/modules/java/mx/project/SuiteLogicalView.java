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
package org.netbeans.modules.java.mx.project;

import java.awt.Image;
import java.util.List;
import java.util.Objects;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.java.mx.project.SuiteSources.Group;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.Actions;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

@ActionReferences({
    @ActionReference(position = 1100, id = @ActionID(category = "Project", id = "org-netbeans-modules-project-ui-BuildProject"), path = "Projects/mxprojects/Actions", separatorBefore = 1000),
    @ActionReference(position = 1200, id = @ActionID(category = "Project", id = "org-netbeans-modules-project-ui-CleanProject"), path = "Projects/mxprojects/Actions"),
    @ActionReference(position = 1300, id = @ActionID(category = "Project", id = "org-netbeans-modules-project-ui-RebuildProject"), path = "Projects/mxprojects/Actions", separatorAfter = 2000),
    @ActionReference(position = 2100, id = @ActionID(category = "Project", id = "org-netbeans-modules-project-ui-TestSingle"), path = "Projects/mxprojects/Actions"),
    @ActionReference(position = 3100, id = @ActionID(category = "Project", id = "org-netbeans-modules-project-ui-CloseProject"), path = "Projects/mxprojects/Actions", separatorBefore = 3000),
    @ActionReference(position = 4100, id = @ActionID(category = "System", id = "org-openide-actions-EditAction"), path = "Projects/mxprojects/Actions", separatorBefore = 4000),
})
@NbBundle.Messages({
    "# {0} - compliance text",
    "CTL_Compliance=Requires JDK {0}",
    "# {0} - compliance text",
    "CTL_MissingJDK=Missing JDK for compliance {0}"
})
final class SuiteLogicalView implements LogicalViewProvider  {
    private final SuiteProject p;

    public SuiteLogicalView(SuiteProject p) {
        this.p = p;
    }

    @Override
    public Node createLogicalView() {
        return new SuiteRootNode(p);
    }

    @Override
    public Node findPath(Node root, Object target) {
        Project prj = root.getLookup().lookup(Project.class);
        if (prj == null) {
            return null;
        }

        if (target instanceof FileObject) {
            FileObject fo = (FileObject) target;
            if (isOtherProjectSource(fo, prj)) {
                return null; // Don't waste time if project does not own the fo among sources
            }

            for (Node n : root.getChildren().getNodes(true)) {
                Node result = PackageView.findPath(n, target);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    private static boolean isOtherProjectSource(
        final FileObject fo,
        final Project me
    ) {
        final Project owner = FileOwnerQuery.getOwner(fo);
        if (owner == null) {
            return false;
        }
        if (me.equals(owner)) {
            return false;
        }
        for (SourceGroup sg : ProjectUtils.getSources(owner).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            if (FileUtil.isParentOf(sg.getRootFolder(), fo)) {
                return true;
            }
        }
        return false;
    }

    private static final class SuiteRootNode extends AbstractNode {

        private final SuiteProject project;

        public SuiteRootNode(SuiteProject p) {
            super(Children.create(new RootChildFactory(p), true), Lookups.fixed(p, new SuiteEnvEdit(p)));
            this.project = p;
            setDisplayName();
            setIconBaseWithExtension("org/netbeans/modules/java/mx/project/mx-knife.png");
        }

        private void setDisplayName() {
            setDisplayName(ProjectUtils.getInformation(project).getDisplayName());
        }

        @Override
        public String getHtmlDisplayName() {
            return null;
        }

        @Override
        public Action[] getActions(boolean context) {
            return CommonProjectActions.forType("mxprojects"); // NOI18N
        }
    }

    private static final class RootChildFactory extends ChildFactory<RootChildFactory.Key> implements ChangeListener {
        private final SuiteProject suite;
        private final SuiteSources sources;

        RootChildFactory(SuiteProject project) {
            this.suite = project;
            this.sources = project.getSources();
            this.sources.addChangeListener(WeakListeners.change(this, this.sources));
        }

        @Override
        protected boolean createKeys(List<Key> toPopulate) {
            for (SuiteSources.Group sg : sources.groups()) {
                if (sg.getJavaPlatform() != null) {
                    toPopulate.add(new Key(sg) {
                        @Override public Node createNode() {
                            return new FilterNode(PackageView.createPackageView(group)) {
                                @Override
                                public String getShortDescription() {
                                    return Bundle.CTL_Compliance(group.getCompliance());
                                }
                            };
                        }
                    });
                } else {
                    toPopulate.add(new Key(sg) {
                        @Override public Node createNode() {
                            try {
                                DataObject od = DataObject.find(group.getRootFolder());
                                return new SuiteWithoutJDKNode(suite, od, group);
                            } catch (DataObjectNotFoundException ex) {
                                return Node.EMPTY;
                            }
                        }
                    });
                }
            }

            return true;
        }

        @Override
        protected Node createNodeForKey(Key key) {
            return key.createNode();
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            refresh(false);
        }

        private abstract static class Key {
            public final Group group;

            public Key(Group group) {
                this.group = group;
            }

            public abstract Node createNode();

            @Override
            public int hashCode() {
                int hash = 7;
                hash = 53 * hash + Objects.hashCode(this.group);
                return hash;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final Key other = (Key) obj;
                return Objects.equals(this.group, other.group);
            }

        }
    }
    static class SuiteWithoutJDKNode extends FilterNode {
        private final Group group;
        private final DataObject od;

        public SuiteWithoutJDKNode(SuiteProject suite, DataObject od, Group group) {
            this(suite, od, group, od.getNodeDelegate());
        }
        
        private SuiteWithoutJDKNode(SuiteProject suite, DataObject od, Group group, Node n) {
            super(n, new FilterNode.Children(n), new ProxyLookup(
                Lookups.singleton(new SuiteEnvEdit(suite)),
                od.getLookup()
            ));
            this.od = od;
            this.group = group;
        }

        @Override
        public Image getIcon(int type) {
            final Image icon = ImageUtilities.loadImage(SuiteFactory.ICON);
            try {
                ImageDecorator decorator = FileUIUtils.getImageDecorator(od.getPrimaryFile().getFileSystem());
                return decorator.annotateIcon(icon, type, od.files());
            } catch (FileStateInvalidException ex) {
                return icon;
            }
        }

        @Override
        public String getDisplayName() {
            return group.getDisplayName();
        }

        @Override
        public String getShortDescription() {
            return Bundle.CTL_MissingJDK(group.getCompliance());
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[]{
                Actions.forID("System", "org.openide.actions.EditAction") // NOI18N
            };
        }
    }
}