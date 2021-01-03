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
package org.netbeans.modules.python.project2.ui;

import java.awt.EventQueue;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.queries.VisibilityQuery;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Copied from java.project API module, the same copy is in ruby project, rails project, etc.
 * Unlike PackageViewChildren this class definitelly requires more generic API module, probably projectui.
 * Displays a package root in a tree.
 * @see "#42151"
 */
public final class TreeRootNode extends FilterNode implements PropertyChangeListener {

    private final static Image SOURCE_ROOT_BADGE = ImageUtilities.loadImage("org/netbeans/modules/python/project2/resources/sourceBadge.gif"); // NOI18N
    private final SourceGroup g;

    public TreeRootNode(SourceGroup g) {
        this(DataFolder.findFolder(g.getRootFolder()), g);
    }

    private TreeRootNode(DataFolder folder, SourceGroup g) {
        this(new FilterNode(folder.getNodeDelegate(), folder.createNodeChildren(new VisibilityQueryDataFilter(g))), g);
    }

    @SuppressWarnings("LeakingThisInConstructor")
    private TreeRootNode(Node originalNode, SourceGroup g) {
        super(originalNode, new PackageFilterChildren(originalNode),
                new ProxyLookup(
                originalNode.getLookup(),
                Lookups.singleton(new PathFinder(g)) // no need for explicit search info
                ));
        this.g = g;
        g.addPropertyChangeListener(WeakListeners.propertyChange(this, g));
    }

    /** Copied from PackageRootNode with modifications. */
    private Image computeIcon(boolean opened, int type) {
        Icon icon = g.getIcon(opened);
        if (icon == null) {
            Image image = opened ? super.getOpenedIcon(type) : super.getIcon(type);
            return ImageUtilities.mergeImages(image, SOURCE_ROOT_BADGE, 7, 7);
        } else {
            return ImageUtilities.icon2Image(icon);
        }
    }

    @Override
    public Image getIcon(int type) {
        return computeIcon(false, type);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return computeIcon(true, type);
    }

    @Override
    public String getName() {
        return g.getName();
    }

    @Override
    public String getDisplayName() {
        return g.getDisplayName();
    }

    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public boolean canDestroy() {
        return false;
    }

    @Override
    public boolean canCut() {
        return false;
    }

    @Override
    public void propertyChange(PropertyChangeEvent ev) {
        // XXX handle SourceGroup.rootFolder change too
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                fireNameChange(null, null);
                fireDisplayNameChange(null, null);
                fireIconChange();
                fireOpenedIconChange();
            }
        });
    }

    /** Copied from PhysicalView and PackageRootNode. */
    public static final class PathFinder {

        private final SourceGroup g;

        PathFinder(SourceGroup g) {
            this.g = g;
        }

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
                FileObject folder = fo.isFolder() ? fo : fo.getParent();
                String relPath = FileUtil.getRelativePath(groupRoot, folder);
                List<String> path = new ArrayList<>();
                StringTokenizer strtok = new StringTokenizer(relPath, "/"); // NOI18N
                while (strtok.hasMoreTokens()) {
                    String token = strtok.nextToken();
                    path.add(token);
                }
                try {
                    Node folderNode = folder.equals(groupRoot) ? rootNode : NodeOp.findPath(rootNode, Collections.enumeration(path));
                    if (fo.isFolder()) {
                        return folderNode;
                    } else {
                        Node[] childs = folderNode.getChildren().getNodes(true);
                        for (Node child : childs) {
                            DataObject dobj = child.getLookup().lookup(DataObject.class);
                            if (dobj != null && dobj.getPrimaryFile().getNameExt().equals(fo.getNameExt())) {
                                return child;
                            }
                        }
                    }
                } catch (NodeNotFoundException e) {
                    LOG.log(Level.WARNING, "TreeRootNode.findPath", e);
                }
            } else if (groupRoot.equals(fo)) {
                return rootNode;
            }
            return null;
        }
        private static final Logger LOG = Logger.getLogger(PathFinder.class.getName());
    }

    private static final class VisibilityQueryDataFilter implements ChangeListener, PropertyChangeListener, ChangeableDataFilter {

        private static final long serialVersionUID = 1L; // in case a DataFolder.ClonedFilterHandle saves me
        private final EventListenerList ell = new EventListenerList();
        private final SourceGroup g;

        @SuppressWarnings("LeakingThisInConstructor")
        public VisibilityQueryDataFilter(SourceGroup g) {
            this.g = g;
            VisibilityQuery.getDefault().addChangeListener(WeakListeners.change(this, VisibilityQuery.getDefault()));
            g.addPropertyChangeListener(WeakListeners.propertyChange(this, g));
        }

        @Override
        public boolean acceptDataObject(DataObject obj) {
            FileObject fo = obj.getPrimaryFile();
            if (fo.getExt().equalsIgnoreCase("pyc") || fo.getExt().equalsIgnoreCase("pyo") | fo.getExt().equalsIgnoreCase("egg-info") || fo.getName().equalsIgnoreCase("build") || fo.getName().equalsIgnoreCase("dist")) {
                return false;
            }
            return g.contains(fo) &&
                    VisibilityQuery.getDefault().isVisible(fo);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            fireChange();
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (SourceGroup.PROP_CONTAINERSHIP.equals(e.getPropertyName())) {
                fireChange();
            }
        }

        private void fireChange() {
            Object[] listeners = ell.getListenerList();
            ChangeEvent event = null;
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == ChangeListener.class) {
                    if (event == null) {
                        event = new ChangeEvent(this);
                    }
                    ((ChangeListener) listeners[i + 1]).stateChanged(event);
                }
            }
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            ell.add(ChangeListener.class, listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            ell.remove(ChangeListener.class, listener);
        }
    }

    private static final class PackageFilterChildren extends FilterNode.Children {

        public PackageFilterChildren(final Node originalNode) {
            super(originalNode);
        }

        @Override
        protected Node copyNode(final Node originalNode) {
            DataObject dobj = originalNode.getLookup().lookup(DataObject.class);
            return (dobj instanceof DataFolder) ? new PackageFilterNode(originalNode) : super.copyNode(originalNode);
        }
    }

    private static final class PackageFilterNode extends FilterNode {

        public PackageFilterNode(final Node origNode) {
            super(origNode, new PackageFilterChildren(origNode));
        }

        @Override
        public void setName(final String name) {
            if (Utilities.isJavaIdentifier(name)) {
                super.setName(name);
            } else {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(TreeRootNode.class, "MSG_InvalidPackageName"), NotifyDescriptor.INFORMATION_MESSAGE));
            }
        }
    }
}
