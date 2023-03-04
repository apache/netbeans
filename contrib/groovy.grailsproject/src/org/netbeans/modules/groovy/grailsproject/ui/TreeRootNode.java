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
package org.netbeans.modules.groovy.grailsproject.ui;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
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
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
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
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Martin Adamek
 */
public final class TreeRootNode extends FilterNode implements PropertyChangeListener {

    private static Image LIBRARIES_BADGE = ImageUtilities.loadImage("org/netbeans/modules/groovy/grailsproject/resources/librariesBadge.png"); // NOI18N
    private final SourceGroup group;
    private final Type visualType;


    enum Type {
        LIBRARY, FOLDER;
    }

    TreeRootNode(DataFolder folder, SourceGroup g, GrailsProject project, Type type) {
        this(new FilterNode(folder.getNodeDelegate(), folder.createNodeChildren(new VisibilityQueryDataFilter(g))), g, project, type);
    }

    private TreeRootNode(Node originalNode, SourceGroup group, GrailsProject project, Type type) {
        super(originalNode, new PackageFilterChildren(originalNode),
                new ProxyLookup(
                originalNode.getLookup(),
                Lookups.fixed(  new PathFinder(group),  // no need for explicit search info
                                // Adding TemplatesImpl to Node's lookup to narrow-down
                                // number of displayed templates with the NewFile action.
                                // see # 122942
                                new TemplatesImpl(project, group)
                                )
                ));
        String pathName = group.getName();
        setShortDescription(pathName.substring(project.getProjectDirectory().getPath().length() + 1));
        this.group = group;
        this.visualType = type;
        group.addPropertyChangeListener(WeakListeners.propertyChange(this, group));
    }

    @Override
    public PasteType getDropType(Transferable t, int action, int index) {
        return null;
    }

    /** Copied from PackageRootNode with modifications. */
    private Image computeIcon(boolean opened, int type) {
        Icon icon = group.getIcon(opened);
        if (icon == null) {
            Image image = opened ? super.getOpenedIcon(type) : super.getIcon(type);

            if (Type.LIBRARY == visualType) {
                return ImageUtilities.mergeImages(image, LIBRARIES_BADGE, 7, 7);
            } else {
                return image;
            }
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
        return group.getName();
    }

    @Override
    public String getDisplayName() {
        return group.getDisplayName();
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
        fireNameChange(null, null);
        fireDisplayNameChange(null, null);
        fireIconChange();
        fireOpenedIconChange();
    }

    public static Node findPath(Node rootNode, Object object) {
        PathFinder finder = rootNode.getLookup().lookup(PathFinder.class);

        if (finder != null) {
            return finder.findPath(rootNode, object);
        }
        return null;
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
                List<String> path = new ArrayList<String>();
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
                        for (int i = 0; i < childs.length; i++) {
                            DataObject dobj = childs[i].getLookup().lookup(DataObject.class);
                            if (dobj != null && dobj.getPrimaryFile().getNameExt().equals(fo.getNameExt())) {
                                return childs[i];
                            }
                        }
                    }
                } catch (NodeNotFoundException e) {
                    Logger.getLogger(TreeRootNode.class.getName()).log(Level.INFO, null, e);
                }
            } else if (groupRoot.equals(fo)) {
                return rootNode;
            }
            return null;
        }
    }

    private static final class VisibilityQueryDataFilter implements ChangeListener, PropertyChangeListener, ChangeableDataFilter {

        private static final long serialVersionUID = 1L; // in case a DataFolder.ClonedFilterHandle saves me
        private final EventListenerList ell = new EventListenerList();
        private final SourceGroup g;

        public VisibilityQueryDataFilter(SourceGroup g) {
            this.g = g;
            VisibilityQuery.getDefault().addChangeListener(WeakListeners.change(this, VisibilityQuery.getDefault()));
            g.addPropertyChangeListener(WeakListeners.propertyChange(this, g));
        }

        @Override
        public boolean acceptDataObject(DataObject obj) {
            FileObject fo = obj.getPrimaryFile();
            return g.contains(fo) && VisibilityQuery.getDefault().isVisible(fo);
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
            final FileObject fo = originalNode.getLookup().lookup(FileObject.class);
            if (fo.isFolder()) {
                return new PackageFilterNode(originalNode);
            } else {
                return super.copyNode(originalNode);
            }
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
