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

package org.netbeans.spi.java.project.support.ui;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.queries.AccessibilityQuery;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.java.project.ui.PackageDisplayUtils;
import static org.netbeans.spi.java.project.support.ui.Bundle.*;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.FileSensitiveActions;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.FileSystemAction;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUIUtils;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.FolderRenameHandler;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Displays a package root in a tree.
 * @see "#42151"
 * @author Jesse Glick
 */
final class TreeRootNode extends FilterNode implements PropertyChangeListener {

    private static final Logger LOG = Logger.getLogger(FilterNode.class.getName());
    private static final AtomicReference<Action[]> actions = new AtomicReference<Action[]>();
    private final SourceGroup g;
    
    TreeRootNode(SourceGroup g, boolean reduced) {
        this(DataFolder.findFolder(g.getRootFolder()), g, reduced);
    }
    
    private TreeRootNode(DataFolder folder, SourceGroup g, boolean reduced) {
        this(new FilterNode(folder.getNodeDelegate(), reduced ? Children.LEAF : folder.createNodeChildren(new GroupDataFilter(g))), folder, g, reduced);
    }
    
    private TreeRootNode (Node originalNode, DataFolder folder, SourceGroup g, boolean reduced) {
        super(originalNode, reduced ? Children.create(new ReducedChildren(folder, new GroupDataFilter(g), g), true) : new PackageFilterChildren(originalNode),
            new ProxyLookup(
                originalNode.getLookup(),
                Lookups.singleton(new PathFinder(g, reduced))
                // no need for explicit search info
            ));
        this.g = g;
        g.addPropertyChangeListener(WeakListeners.propertyChange(this, g));
    }

    /** Copied from PackageRootNode with modifications. */
    private Image computeIcon(boolean opened, int type) {
        Icon icon = g.getIcon(opened);
        if (icon == null) {
            Image image = opened ? super.getOpenedIcon(type) : super.getIcon(type);
            return ImageUtilities.mergeImages(image, ImageUtilities.loadImage(PackageRootNode.PACKAGE_BADGE), 7, 7);
        } else {
            return ImageUtilities.icon2Image(icon);
        }
    }
    
    public Image getIcon(int type) {
        return computeIcon(false, type);
    }

    public Image getOpenedIcon(int type) {
        return computeIcon(true, type);
    }

    public String getName() {
        return g.getName();
    }

    public String getDisplayName() {
        return g.getDisplayName();
    }

    public boolean canRename() {
        return false;
    }

    public boolean canDestroy() {
        return false;
    }

    public boolean canCut() {
        return false;
    }

    @Override
    @NonNull
    public Action[] getActions(boolean context) {
        Action[] res = actions.get();
        if (res == null) {
            res = PackageView.createRootNodeActions();
            if (!actions.compareAndSet(null, res)) {
                res = actions.get();
            }
        }
        assert  res != null;
        return res;
    }



    public void propertyChange(PropertyChangeEvent ev) {
        // XXX handle SourceGroup.rootFolder change too
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                fireNameChange(null, null);
                fireDisplayNameChange(null, null);
                fireIconChange();
                fireOpenedIconChange();
            }
        });
    }

    /** Copied from PhysicalView and PackageRootNode. */
    public static final class PathFinder implements org.netbeans.spi.project.ui.PathFinder {
        
        private final SourceGroup g;
        private final boolean reduced;
        
        PathFinder(SourceGroup g, boolean reduced) {
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
    
    private static final class GroupDataFilter implements ChangeListener, PropertyChangeListener,
            ChangeableDataFilter, DataFilter.FileBased {
        
        private static final long serialVersionUID = 1L; // in case a DataFolder.ClonedFilterHandle saves me
        
        private final ChangeSupport cs = new ChangeSupport(this);
        private final SourceGroup g;
        
        public GroupDataFilter(SourceGroup g) {
            this.g = g;
            VisibilityQuery.getDefault().addChangeListener(WeakListeners.change(this, VisibilityQuery.getDefault()));
            g.addPropertyChangeListener(WeakListeners.propertyChange(this, g));
        }
        
        public boolean acceptDataObject(DataObject obj) {
            return acceptFileObject(obj.getPrimaryFile());
        }
        
        public void stateChanged(ChangeEvent e) {
            cs.fireChange();
        }
        
        public void propertyChange(PropertyChangeEvent e) {
            if (SourceGroup.PROP_CONTAINERSHIP.equals(e.getPropertyName())) {
                cs.fireChange();
            }
        }

        public void addChangeListener(ChangeListener listener) {
            cs.addChangeListener(listener);
        }
        
        public void removeChangeListener(ChangeListener listener) {
            cs.removeChangeListener(listener);
        }

        public boolean acceptFileObject(FileObject fo) {
            return fo.isValid() && g.contains(fo) && VisibilityQuery.getDefault().isVisible(fo);
        }
        
    }
    
    private static class ReducedChildren extends ChildFactory<DataObject> implements ChangeListener, PropertyChangeListener {

        private final DataFolder folder;
        private final ChangeableDataFilter filter;
        private final SourceGroup g;

        ReducedChildren(DataFolder folder, ChangeableDataFilter filter, SourceGroup g) {
            this.folder = folder;
            this.filter = filter;
            this.g = g;
            filter.addChangeListener(WeakListeners.change(this, filter));
            folder.addPropertyChangeListener(WeakListeners.propertyChange(this, folder));
        }

        @Override protected boolean createKeys(List<DataObject> keys) {
            List<DataObject> files = new ArrayList<DataObject>();
            for (DataObject f : folder.getChildren()) {
                if (!filter.acceptDataObject(f)) {
                    continue;
                }
                if (f instanceof DataFolder) {
                    while (true) {
                        DataObject candidate = null;
                        f.addPropertyChangeListener(WeakListeners.propertyChange(this, f));
                        for (DataObject kid : ((DataFolder) f).getChildren()) {
                            if (!filter.acceptDataObject(kid)) {
                                continue;
                            }
                            if (kid instanceof DataFolder) {
                                if (candidate == null) {
                                    candidate = kid;
                                } else {
                                    candidate = null;
                                    break;
                                }
                            } else {
                                candidate = null;
                                break;
                            }
                        }
                        if (candidate != null) {
                            f = candidate;
                        } else {
                            break;
                        }
                    }
                    keys.add(f);
                } else {
                    files.add(f);
                }
            }
            keys.addAll(files);
            return true;
        }

        @Override protected Node createNodeForKey(DataObject key) {
            if (!key.isValid()) {
                return null;
            }
            return key instanceof DataFolder ? new PackageFilterNode((DataFolder) key, folder, filter, g) : key.getNodeDelegate().cloneNode();
        }

        @Override public void stateChanged(ChangeEvent e) {
            refresh(false);
        }

        @Override public void propertyChange(PropertyChangeEvent evt) {
            // probably DataFolder.PROP_CHILDREN
            refresh(false);
        }

    }
    
    private static final class PackageFilterChildren extends FilterNode.Children {
        
        public PackageFilterChildren (final Node originalNode) {
            super (originalNode);
        }       
                
        @Override
        protected Node copyNode(final Node originalNode) {
            FileObject fobj = originalNode.getLookup().lookup(FileObject.class);
            if (fobj == null) {
                LOG.log(
                    Level.WARNING,
                    "The node {0} has no FileObject in its Lookup.",    //NOI18N
                    originalNode);
                return super.copyNode(originalNode);
            } else {
                return fobj.isFolder() ?
                    new PackageFilterNode (originalNode) :
                    super.copyNode(originalNode);
            }
        }
    }
    
    private static final class PackageFilterNode extends FilterNode {
        
        private static final @StaticResource String PUBLIC_PACKAGE_BADGE = "org/netbeans/spi/java/project/support/ui/publicBadge.gif";    //NOI18N
        private static final @StaticResource String PRIVATE_PACKAGE_BADGE = "org/netbeans/spi/java/project/support/ui/privateBadge.gif";  //NOI18N
        private static Image unlockBadge;
        private static Image lockBadge;

        /** Non-null only in reduced mode. */
        private final DataFolder parent;
        /** Non-null only in reduced mode. */
        private final SourceGroup g;
        
        private Action[] actions;
        private final Action testPackageAction;
        
        public PackageFilterNode(final Node origNode) {
            super (origNode, new PackageFilterChildren (origNode));
            testPackageAction = FileSensitiveActions.fileCommandAction(ActionProvider.COMMAND_TEST_SINGLE, NbBundle.getMessage(TreeRootNode.class, "LBL_TestPackageAction_Name"), null);
            parent = null;
            g = null;
        }

        PackageFilterNode(DataFolder folder, DataFolder parent, ChangeableDataFilter filter, SourceGroup g) {
            super(folder.getNodeDelegate(), Children.create(new ReducedChildren(folder, filter, g), true));
            testPackageAction = FileSensitiveActions.fileCommandAction(ActionProvider.COMMAND_TEST_SINGLE, NbBundle.getMessage(TreeRootNode.class, "LBL_TestPackageAction_Name"), null);
            this.parent = parent;
            this.g = g;
        }

        @Override public String getName() {
            if (parent != null) {
                DataObject d = getLookup().lookup(DataObject.class);
                if (d != null) {
                    final String relName = FileUtil.getRelativePath(parent.getPrimaryFile(), d.getPrimaryFile());
                    //Null after DO move.
                    if (relName != null) {
                        return relName.replace('/', '.');   //NOI18N
                    }
                }
            }
            return super.getName();
        }
        
        
        @Override
        public Action[] getActions( boolean context ) {
            
            if ( !context ) {
                if ( actions == null ) {                
                    // Copy actions and leave out the PropertiesAction and FileSystemAction.                
                    Action superActions[] = super.getActions( context );            
                    List<Action> actionList = new ArrayList<Action>(superActions.length);
                    
                    for( int i = 0; i < superActions.length; i++ ) {
                        if ( superActions[i] instanceof FileSystemAction ) {
                            actionList.add (null); // insert separator and new action
                            actionList.add (testPackageAction);
                            actionList.addAll((List<Action>) org.openide.util.Utilities.actionsForPath("Projects/package/Actions"));
                        }
                        
                        actionList.add( superActions[i] );                                                  
                    }

                    actions = new Action[ actionList.size() ];
                    actionList.toArray( actions );
                }
                return actions;
            }
            else {
                return super.getActions( context );
            }
        }

        @Override public String getDisplayName() {
            if (parent != null) {
                // XXX annotate with FileSystem.Status? also getHtmlDisplayName + FileSystem.HtmlStatus
                return getName();
            }
            return super.getDisplayName();
        }

        @Override public void destroy() throws IOException {
            if (parent != null) {
                for (DataObject d = getLookup().lookup(DataObject.class); d != null && FileUtil.isParentOf(parent.getPrimaryFile(), d.getPrimaryFile()); d = d.getFolder()) {
                    d.delete();
                }
            } else {
                super.destroy();
            }
        }

        @Messages("MSG_unsupported_rename=Renaming nonterminal package components is not supported in reduced tree mode when subpackages are present.")
        @Override
        public void setName (final String name) {
            if (parent != null) {
                if (PackageViewChildren.isValidPackageName(name)) {
                    PackageRenameHandler prh = Lookup.getDefault().lookup(PackageRenameHandler.class);
                    FolderRenameHandler frh = Lookup.getDefault().lookup(FolderRenameHandler.class);
                    if (prh != null && frh != null) { // refactoring support present
                        DataFolder folder = getLookup().lookup(DataFolder.class);
                        String old = getName();
                        int dot = old.lastIndexOf('.');
                        if (name.lastIndexOf('.') == dot) { // case 1
                            if (dot == -1) {
                                frh.handleRename(folder, name);
                                return;
                            } else if (dot != -1 && name.substring(0, dot).equals(old.substring(0, dot))) {
                                frh.handleRename(folder, name.substring(dot + 1));
                                return;
                            }
                        }
                        for (DataObject d : folder.getChildren()) {
                            if (d instanceof DataFolder) { // case 3
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(MSG_unsupported_rename(), NotifyDescriptor.INFORMATION_MESSAGE));
                                return;
                            }
                        }
                        String parentPackageSlashes = FileUtil.getRelativePath(g.getRootFolder(), parent.getPrimaryFile());
                        if (parentPackageSlashes != null) { // case 2
                            prh.handleRename(new PackageViewChildren(g).new PackageNode(g.getRootFolder(), folder), (parentPackageSlashes.isEmpty() ? "" : parentPackageSlashes.replace('/', '.') + '.') + name);
                            return;
                        }
                    }
                    FileObject d = getLookup().lookup(DataObject.class).getPrimaryFile();
                    FileObject origParent = d.getParent();
                    try {
                        FileLock lock = d.lock();
                        try {
                            FileObject p;
                            String child;
                            int dot = name.lastIndexOf('.');
                            if (dot == -1) {
                                p = parent.getPrimaryFile();
                                child = name;
                            } else {
                                p = FileUtil.createFolder(parent.getPrimaryFile(), name.substring(0, dot).replace('.', '/'));
                                child = name.substring(dot + 1);
                            }
                            d.move(lock, p, child, null);
                        } finally {
                            lock.releaseLock();
                        }
                        while (origParent != null && origParent.isValid() && origParent.getChildren().length == 0) {
                            FileObject higher = origParent.getParent();
                            origParent.delete();
                            origParent = higher;
                        }
                    } catch (IOException x) {
                        Exceptions.printStackTrace(x);
                    }
                } else {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(MSG_InvalidPackageName(), NotifyDescriptor.INFORMATION_MESSAGE));
                }
                return;
            }
            if (Utilities.isJavaIdentifier (name)) {
                super.setName (name);
            }
            else {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(MSG_InvalidPackageName(), NotifyDescriptor.INFORMATION_MESSAGE));
            }
        }

        private @CheckForNull DataFolder topPackage() {
            if (parent == null) {
                return null;
            }
            DataFolder here = getLookup().lookup(DataFolder.class);
            while (here != null) {
                DataFolder there = here.getFolder();
                if (there != null && there != parent) {
                    here = there;
                } else {
                    break;
                }
            }
            return here;
        }

        @Override public Transferable clipboardCut() throws IOException {
            DataFolder top = topPackage();
            if (top != null) {
                return top.getNodeDelegate().clipboardCut();
            } else {
                return super.clipboardCut();
            }
        }

        @Override public Transferable clipboardCopy() throws IOException {
            DataFolder top = topPackage();
            if (top != null) {
                return top.getNodeDelegate().clipboardCopy();
            } else {
                return super.clipboardCopy();
            }
        }

        @Override
        public Image getIcon (int type) {
            return getIcon(type, false);
        }

        @Override
        public Image getOpenedIcon (int type) {
            return getIcon(type, true);
        }

        private Image getIcon(int type, boolean opened) {
            if (parent != null) {
                DataObject dobj = getLookup().lookup(DataObject.class);
                if (dobj != null) {
                    FileObject f = dobj.getPrimaryFile();
                    Image icon = PackageDisplayUtils.getIcon(f, false);
                    try {
                        icon = FileUIUtils.getImageDecorator(f.getFileSystem()).annotateIcon(icon, type, Collections.singleton(f));
                    } catch (FileStateInvalidException x) {
                        Exceptions.printStackTrace(x);
                    }
                    return icon;
                }
            }
            Image icon = opened ? super.getOpenedIcon(type) : super.getIcon(type);
            if (icon == null) {
                return icon;
            }
            final DataObject dobj = getLookup().lookup(DataObject.class);
            if (dobj == null) {
                return icon;
            }
            final FileObject fo = dobj.getPrimaryFile();
            if (fo == null) {
                return icon;
            }
            final Boolean pub = AccessibilityQuery.isPubliclyAccessible(fo);
            if (pub == Boolean.TRUE) {
                synchronized (PackageFilterNode.class) {
                    if (unlockBadge == null) {
                        unlockBadge = ImageUtilities.loadImage(PUBLIC_PACKAGE_BADGE); 
                    }
                }
                return ImageUtilities.mergeImages(icon, unlockBadge, 0, 0);
            } else if (pub == Boolean.FALSE) {
                synchronized (PackageFilterNode.class) {
                    if (lockBadge == null) {
                        lockBadge = ImageUtilities.loadImage(PRIVATE_PACKAGE_BADGE);
                    }
                }
                return ImageUtilities.mergeImages(icon, lockBadge, 0, 0);
            } else {
                return icon;
            }
        }

        @Override
        public String getShortDescription() {
            if (g != null) {
                final DataObject doj = getLookup().lookup(DataObject.class);
                if (doj != null) {
                    final FileObject f = doj.getPrimaryFile();
                    String rel = FileUtil.getRelativePath(g.getRootFolder(), f);
                    if (rel != null) {
                        return PackageDisplayUtils.getToolTip(f, rel.replace('/', '.'));    //NOI18N
                    }
                }
            }
            return super.getShortDescription();
        }

    }
    
}
