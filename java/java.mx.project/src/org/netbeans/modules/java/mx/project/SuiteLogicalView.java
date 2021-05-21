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
package org.netbeans.modules.java.mx.project;

import java.awt.Image;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

@ActionReferences({
    @ActionReference(position = 1100, id = @ActionID(category = "Project", id = "org-netbeans-modules-project-ui-BuildProject"), path = "Projects/mxprojects/Actions", separatorBefore = 1000),
    @ActionReference(position = 1200, id = @ActionID(category = "Project", id = "org-netbeans-modules-project-ui-CleanProject"), path = "Projects/mxprojects/Actions"),
    @ActionReference(position = 1300, id = @ActionID(category = "Project", id = "org-netbeans-modules-project-ui-RebuildProject"), path = "Projects/mxprojects/Actions", separatorAfter = 2000),
    @ActionReference(position = 2100, id = @ActionID(category = "Project", id = "org-netbeans-modules-project-ui-TestSingle"), path = "Projects/mxprojects/Actions"),
    @ActionReference(position = 3100, id = @ActionID(category = "Project", id = "org-netbeans-modules-project-ui-CloseProject"), path = "Projects/mxprojects/Actions", separatorBefore = 3000),
})
final class SuiteLogicalView implements LogicalViewProvider  {

    private final Project p;

    public SuiteLogicalView(Project p) {
        this.p = p;
    }

    @Override
    public Node createLogicalView() {
        return new RootNode(p);
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

    private static final class RootNode extends AbstractNode {

        private final Project project;

        public RootNode(Project p) {
            super(Children.create(new RootChildFactory(p), true), Lookups.fixed(p));
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

        private final Sources sources;

        public RootChildFactory(Project project) {
            this.sources = ProjectUtils.getSources(project);
            this.sources.addChangeListener(WeakListeners.change(this, this.sources));
        }

        @Override
        protected boolean createKeys(List<Key> toPopulate) {
            Set<SourceGroup> javaSourceGroups = Collections.newSetFromMap(new IdentityHashMap<>());

            javaSourceGroups.addAll(Arrays.asList(sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)));

            for (SourceGroup sg : sources.getSourceGroups(null)) {
                if (javaSourceGroups.contains(sg)) {
                    toPopulate.add(new Key(sg) {
                        @Override public Node createNode() {
                            return PackageView.createPackageView(group);
                        }
                    });
                } else {
                    toPopulate.add(new Key(sg) {
                        @Override public Node createNode() {
                            try {
                                DataObject od = DataObject.find(group.getRootFolder());
                                return new FilterNode(od.getNodeDelegate()) {
                                    @Override public Image getIcon(int type) {
                                        return ImageUtilities.loadImage("org/netbeans/modules/jdk/project/resources/nativeFilesFolder.gif");
                                    }
                                    @Override public Image getOpenedIcon(int type) {
                                        return ImageUtilities.loadImage("org/netbeans/modules/jdk/project/resources/nativeFilesFolderOpened.gif");
                                    }
                                    @Override public String getDisplayName() {
                                        return group.getDisplayName();
                                    }
                                };
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

        private static abstract class Key {
            public final SourceGroup group;

            public Key(SourceGroup group) {
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
}