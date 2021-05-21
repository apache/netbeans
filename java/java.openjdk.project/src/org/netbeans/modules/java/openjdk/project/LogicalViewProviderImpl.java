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
package org.netbeans.modules.java.openjdk.project;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.ImageUtilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author lahvac
 */
public class LogicalViewProviderImpl implements LogicalViewProvider  {

    private final Project p;

    public LogicalViewProviderImpl(Project p) {
        this.p = p;
    }
    
    @Override
    public Node createLogicalView() {
        return new RootNode(p);
    }

    //from java.api.common's LogicalViewProviders:
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
            @NonNull final FileObject fo,
            @NonNull final Project me) {
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
            setIconBaseWithExtension("org/netbeans/modules/java/openjdk/project/resources/jdk-project.png");
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
            return CommonProjectActions.forType(JDKProject.PROJECT_KEY);
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
            Set<SourceGroup> javaSourceGroups = Collections.newSetFromMap(new IdentityHashMap<SourceGroup, Boolean>());

            javaSourceGroups.addAll(Arrays.asList(sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)));

            Set<SourceGroup> testGroups = Collections.newSetFromMap(new IdentityHashMap<SourceGroup, Boolean>());

            testGroups.addAll(Arrays.asList(sources.getSourceGroups(SourcesImpl.SOURCES_TYPE_JDK_PROJECT_TESTS)));

            for (SourceGroup sg : sources.getSourceGroups(SourcesImpl.SOURCES_TYPE_JDK_PROJECT)) {
                if (testGroups.contains(sg)) {
                    //for tests, don't create PackageView:
                    toPopulate.add(new Key(sg) {
                        @Override public Node createNode() {
                            try {
                                return new PathFindingNode(group) {
                                    @Override public String getDisplayName() {
                                        return group.getDisplayName();
                                    }
                                };
                            } catch (DataObjectNotFoundException ex) {
                                return Node.EMPTY;
                            }
                        }
                    });
                } else if (javaSourceGroups.contains(sg)) {
                    toPopulate.add(new Key(sg) {
                        @Override public Node createNode() {
                            return PackageView.createPackageView(group);
                        }
                    });
                } else {
                    toPopulate.add(new Key(sg) {
                        @Override public Node createNode() {
                            try {
                                return new PathFindingNode(group) {
                                    @Override public Image getIcon(int type) {
                                        return ImageUtilities.loadImage("org/netbeans/modules/java/openjdk/project/resources/nativeFilesFolder.gif");
                                    }
                                    @Override public Image getOpenedIcon(int type) {
                                        return ImageUtilities.loadImage("org/netbeans/modules/java/openjdk/project/resources/nativeFilesFolderOpened.gif");
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
                if (!Objects.equals(this.group, other.group)) {
                    return false;
                }
                return true;
            }
            
        }
    }

    /** Based on java/java.project.ui/src/org/netbeans/spi/java/project/support/ui/TreeRootNode.java. */
    public static class PathFindingNode extends FilterNode {

        public PathFindingNode(SourceGroup sg) throws DataObjectNotFoundException {
            this(sg, DataObject.find(sg.getRootFolder()).getNodeDelegate());
        }

        private PathFindingNode(SourceGroup sg, Node delegate) {
            super(delegate,
                  new Children(delegate),
                  new ProxyLookup(delegate.getLookup(),
                                  Lookups.singleton(new TreeViewPathFinder(sg, false))));
        }
    }

    /** Copied from java/java.project.ui/src/org/netbeans/spi/java/project/support/ui/TreeRootNode.java. */
    public static final class TreeViewPathFinder implements org.netbeans.spi.project.ui.PathFinder {

        private final SourceGroup g;
        private final boolean reduced;

        TreeViewPathFinder(SourceGroup g, boolean reduced) {
            this.g = g;
            this.reduced = reduced;
        }

        @Override
        public Node findPath(Node rootNode, Object o) {
            FileObject fo;
            if (o instanceof FileObject) {
                fo = (FileObject) o;
            } else if (o instanceof DataObject) {
                fo = ((DataObject) o).getPrimaryFile();
            } else {
                return null;
            }
            FileObject groupRoot = g.getRootFolder();
            if (FileUtil.isParentOf(groupRoot, fo) /* && group.contains(fo) */) {
                return reduced ? findPathReduced(fo, rootNode) : findPathPlain(fo, groupRoot, rootNode);
            } else if (groupRoot.equals(fo)) {
                return rootNode;
            } else {
                return null;
            }
        }

        private Node findPathPlain(FileObject fo, FileObject groupRoot, Node rootNode) {
            FileObject folder = fo.isFolder() ? fo : fo.getParent();
            String relPath = FileUtil.getRelativePath(groupRoot, folder);
            List<String> path = new ArrayList<String>();
            StringTokenizer strtok = new StringTokenizer(relPath, "/"); // NOI18N
            while (strtok.hasMoreTokens()) {
                String token = strtok.nextToken();
               path.add(token);
            }
            try {
                Node folderNode =  folder.equals(groupRoot) ? rootNode : NodeOp.findPath(rootNode, Collections.enumeration(path));
                if (fo.isFolder()) {
                    return folderNode;
                } else {
                    Node[] childs = folderNode.getChildren().getNodes(true);
                    for (int i = 0; i < childs.length; i++) {
                       DataObject dobj = childs[i].getLookup().lookup(DataObject.class);
                       if (dobj != null && dobj.getPrimaryFile().getNameExt().equals(fo.getNameExt())) {
                           return childs[i];
                       }
                    }
                }
            } catch (NodeNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        private Node findPathReduced(FileObject fo, Node n) {
            FileObject f = n.getLookup().lookup(FileObject.class);
            if (f == fo) {
                return n;
            } else if (f != null && FileUtil.isParentOf(f, fo)) {
                for (Node child : n.getChildren().getNodes(true)) {
                    Node found = findPathReduced(fo, child);
                    if (found != null) {
                        return found;
                    }
                }
            }
            return null;
        }

    }
}
