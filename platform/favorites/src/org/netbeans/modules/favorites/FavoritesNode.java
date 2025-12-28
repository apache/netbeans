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

package org.netbeans.modules.favorites;

import java.awt.EventQueue;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.favorites.api.Favorites;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataShadow;
import org.openide.loaders.LoaderTransfer;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author  Jaroslav Tulach
 */
public final class FavoritesNode extends FilterNode implements Index {
    /** default node */
    private static Node node;
    static RequestProcessor RP = new RequestProcessor("Favorites Nodes"); //NOI18N

    /** Creates new ProjectRootFilterNode. */
    private FavoritesNode(Node node) {
        super(node, new Chldrn (node, false));
    }

    @Override
    public PasteType getDropType(Transferable t, int action, int index) {
        // #139713: drop into empty area creates new link, otherwise disabled
        if (index != -1)
            return null;
        // any kind of drop just creates link in Favorites
        DataObject[] dos = LoaderTransfer.getDataObjects(t, LoaderTransfer.DND_COPY_OR_MOVE | LoaderTransfer.CLIPBOARD_CUT);
        if (dos == null) {
            List<File> files = getDraggedFilesList(t);
            if (!files.isEmpty()) {
                return new FavoritesExternalPasteType(files);
            }
            return null;
        }
        for (DataObject dataObject : dos) {
            if (! Actions.Add.isAllowed(dataObject))
                return null;
        }
        return new FavoritesPasteType(dos);
    }
    
    @Override
    public <T extends Node.Cookie> T getCookie(Class<T> cl) {
        if (cl == Index.class) {
            return cl.cast(this);
        } else {
            return super.getCookie(cl);
        }
    }
    
    @Override
    public int getNodesCount() {
        Index ind = getOriginal().getCookie(Index.class);
        if (ind != null) {
            return ind.getNodesCount();
        } else {
            return 0;
        }
    }
    
    @Override
    public Node[] getNodes() {
        Index ind = getOriginal().getCookie(Index.class);
        if (ind != null) {
            return ind.getNodes();
        } else {
            return new Node [] {};
        }        
    }

    @Override
    public int indexOf(final Node node) {
        Index ind = getOriginal().getCookie(Index.class);
        if (ind != null) {
            if (node instanceof FavoritesNode.ProjectFilterNode) {
                FavoritesNode.ProjectFilterNode fn = (FavoritesNode.ProjectFilterNode) node;
                int i = ind.indexOf(fn.getOriginal());
                return i;
            } else {
                int i = ind.indexOf(node);
                return i;
            }
        } else {
            return -1;
        }                
    }

    @Override
    public void reorder() {
        Index ind = getOriginal().getCookie(Index.class);
        if (ind != null) {
            ind.reorder();
        }
    }

    @Override
    public void reorder(int[] perm) {
        Index ind = getOriginal().getCookie(Index.class);
        if (ind != null) {
            ind.reorder(perm);
        }
    }

    @Override
    public void move(int x, int y) {
        Index ind = getOriginal().getCookie(Index.class);
        if (ind != null) {
            ind.move(x,y);
        }
    }

    @Override
    public void exchange(int x, int y) {
        Index ind = getOriginal().getCookie(Index.class);
        if (ind != null) {
            ind.exchange(x,y);
        }
    }

    @Override
    public void moveUp(int x) {
        Index ind = getOriginal().getCookie(Index.class);
        if (ind != null) {
            ind.moveUp(x);
        }
    }

    @Override
    public void moveDown(int x) {
        Index ind = getOriginal().getCookie(Index.class);
        if (ind != null) {
            ind.moveDown(x);
        }
    }

    @Override
    public void addChangeListener(final ChangeListener chl) {
        Index ind = getOriginal().getCookie(Index.class);
        if (ind != null) {
            ind.addChangeListener(chl);
        }
    }

    @Override
    public void removeChangeListener(final ChangeListener chl) {
        Index ind = getOriginal().getCookie(Index.class);
        if (ind != null) {
            ind.removeChangeListener(chl);
        }
    }
    
    @Override
    public boolean canCopy () {
        return false;
    }
    
    @Override
    public boolean canCut () {
        return false;
    }
    
    @Override
    public boolean canRename () {
        return false;
    }
    
    public static DataFolder getFolder () {
        try {
            FileObject fo = FileUtil.createFolder (
                FileUtil.getConfigRoot(),
                "Favorites" // NOI18N
            );
            DataFolder folder = DataFolder.findFolder(fo);
            return folder;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return DataFolder.findFolder (FileUtil.getConfigRoot());
        }
        
    }
    
    /** Getter for default filter node.
     */
    public static synchronized Node getNode () {
        if (node == null) {
            node = new FavoritesNode (getFolder().getNodeDelegate ());
        }
        return node;
    }
    
    /** Get name of home directory. Used from layer.
     */
    public static URL getHome () 
    throws FileStateInvalidException, MalformedURLException {
        String s = System.getProperty("user.home"); // NOI18N
        
        File home = new File (s);
        home = FileUtil.normalizeFile (home);
        
        return Utilities.toURI (home).toURL ();
    }

    /** Finds file for a given node 
     */
    static File fileForNode (Node n) {
        DataObject obj = n.getCookie (DataObject.class);
        if (obj == null) return null;
        
        return FileUtil.toFile (
            obj.getPrimaryFile()
        );
    }

    @Override
    public Handle getHandle () {
        return new RootHandle ();
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {Actions.addOnFavoritesNode()};
    }
    
    /** Drag'n'drop DataFlavor used on Linux for file dragging */
    private static DataFlavor uriListDataFlavor;
    /**
     * Copy&paste from DataFolder
     */
    private List<File> getDraggedFilesList( Transferable t ) {
        try {
            if( t.isDataFlavorSupported( DataFlavor.javaFileListFlavor ) ) {
                //windows & mac
                List<?> fileList = (List<?>) t.getTransferData(DataFlavor.javaFileListFlavor);
                //#92812 - make sure mac os does not return null value
                if( null != fileList ) {
                    return NbCollections.checkedListByCopy(fileList, File.class, true);
                }
            } else if( t.isDataFlavorSupported( getUriListDataFlavor() ) ) {
                //linux
                String uriList = (String)t.getTransferData( getUriListDataFlavor() );
                return textURIListToFileList( uriList );
            }
        } catch( UnsupportedFlavorException ex ) {
            Logger.getLogger(FavoritesNode.class.getName()).log(Level.WARNING, null, ex);
        } catch( IOException ex ) {
            // Ignore. Can be just "Owner timed out" from sun.awt.X11.XSelection.getData.
            Logger.getLogger(FavoritesNode.class.getName()).log(Level.FINE, null, ex);
        }
        return Collections.<File>emptyList();
    }

    private DataFlavor getUriListDataFlavor() {
        if( null == uriListDataFlavor ) {
            try {
                uriListDataFlavor = new DataFlavor("text/uri-list;class=java.lang.String");
            } catch( ClassNotFoundException cnfE ) {
                //cannot happen
                throw new AssertionError(cnfE);
            }
        }
        return uriListDataFlavor;
    }

    private List<File> textURIListToFileList( String data ) {
        List<File> list = new ArrayList<>(1);
        // XXX consider using BufferedReader(StringReader) instead
        for( StringTokenizer st = new StringTokenizer(data, "\r\n");
            st.hasMoreTokens();) {
            String s = st.nextToken();
            if( s.startsWith("#") ) {
                // the line is a comment (as per the RFC 2483)
                continue;
            }
            try {
                URI uri = new URI(s);
                File file = Utilities.toFile(uri);
                list.add( file );
            } catch(java.net.URISyntaxException |
                    IllegalArgumentException e ) {
                // malformed URI
                // the URI is not a valid 'file:' URI
            }
        }
        return list;
    }
    
    private static class RootHandle implements Node.Handle {
        static final long serialVersionUID = 1907300072945111595L;

        /** Return a node for the current project.
        */
        @Override
        public Node getNode () {
            return FavoritesNode.getNode ();
        }
    }



    static class VisQ 
    implements DataFilter.FileBased, ChangeableDataFilter, ChangeListener {
        public static final VisQ DEFAULT = new VisQ();

        private ChangeListener weak;
        private ChangeSupport support = new ChangeSupport(this);

        VisQ() {
            weak = org.openide.util.WeakListeners.change(this, VisibilityQuery.getDefault());
            VisibilityQuery.getDefault().addChangeListener(weak);
        }
        
        @Override
        public boolean acceptFileObject(FileObject fo) {
            return VisibilityQuery.getDefault().isVisible(fo);
        }

        @Override
        public boolean acceptDataObject(DataObject obj) {
            return acceptFileObject(obj.getPrimaryFile());
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            support.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            support.removeChangeListener(listener);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            support.fireChange();
        }
    } // end of VisQ
    
    private static class Chldrn extends FilterNode.Children {
        private boolean hideHidden;
        /** Creates new Chldrn. */
        public Chldrn(Node node, boolean hideHidden) {
            super (node);
            this.hideHidden = hideHidden;
        }
        
        @Override
        protected Node[] createNodes(Node node) {
            return new Node[] { createFilterNode(node) };
        }

    } // end of Chldrn
    
    static Node createFilterNode(Node node) {
        return new ProjectFilterNode(node, EventQueue.isDispatchThread()
                ? Children.LEAF
                : findChildren(node));
    }
    private static org.openide.nodes.Children findChildren(Node node) {
        assert !EventQueue.isDispatchThread();
        org.openide.nodes.Children ch;
        DataFolder folder = node.getLookup().lookup(DataFolder.class);
        if (folder != null) {
            ch = new Chldrn(new FilterNode(node, folder.createNodeChildren(new VisQ())), true);
        } else {
            if (node.isLeaf()) {
                ch = org.openide.nodes.Children.LEAF;
            } else {
                ch = new Chldrn(node, true);
            }
        }
        return ch;
    }

    /** This FilterNode is sensitive to 'Delete Original Files' property of {@link ProjectOption}.
     * When this property is true then original DataObjects pointed to by links under the project's node
     * are deleted as the Delete is performed on the link's node.
     */
    private static class ProjectFilterNode extends FilterNode implements NodeListener, Runnable {
        private DataShadow ds;

        /** Creates new ProjectFilterNode. */
        public ProjectFilterNode (Node node, org.openide.nodes.Children children) {
            super (node, children);
            addNodeListener(this);
            RP.post(this);
        }

        @Override
        public void run () {
            setChildren(findChildren(getOriginal()));
            if (FavoritesNode.getNode().equals(this.getParentNode())) {
                ds = getCookie(DataShadow.class);
                fireDisplayNameChange(null, null);
            }
        }

        @Override
        protected NodeListener createNodeListener() {
            return new NodeAdapter(this) {
                @Override
                protected void propertyChange(FilterNode fn, PropertyChangeEvent ev) {
                    super.propertyChange(fn, ev);
                    if (Node.PROP_LEAF.equals(ev.getPropertyName())) {
                        RP.post(ProjectFilterNode.this);
                    }
                }
            };
        }
        
        
        
        @Override
        public void setName(String name) {
            // #113859 - keep order of children in favorites folder after rename
            final DataFolder favoritesFolder = FavoritesNode.getFolder();
            final DataObject[] children = favoritesFolder.getChildren();
            super.setName(name);
            try {
                favoritesFolder.setOrder(children);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public String getDisplayName () {
            //Change display name only for favorite nodes (links) under Favorites node.
            if (FavoritesNode.getNode().equals(this.getParentNode())) {
                DataShadow ds = getShadow();
                if (ds != null) {
                    String name = ds.getName();
                    String path = FileUtil.getFileDisplayName(ds.getOriginal().getPrimaryFile());
                    return NbBundle.getMessage(FavoritesNode.class, "CTL_DisplayNameTemplate", name, path);
                } else {
                    return super.getDisplayName();
                }
            } else {
                return super.getDisplayName();
            }
        }
        
        @Override
        public String getHtmlDisplayName() {
            if (FavoritesNode.getNode().equals(this.getParentNode())) {
                DataShadow ds = getShadow();
                if (ds != null) {
                    String name = ds.getName();
                    String path = FileUtil.getFileDisplayName(ds.getOriginal().getPrimaryFile());
                    return NbBundle.getMessage(FavoritesNode.class, "CTL_DisplayNameTemplateHtml", name, path); //NOI18N
                } else {
                    return super.getDisplayName();
                }
            } else {
                return getOriginal().getHtmlDisplayName();
            }
        }
        
        @Override
        protected Node getOriginal() {
            return super.getOriginal();
        }

        @Override
        public Action[] getActions(boolean context) {
            Action[] arr;
            arr = super.getActions(context);
            
            //Find if given node is root
            boolean isRoot = false;
            FileObject fo = getOriginal().getLookup().lookup(FileObject.class);
            if (fo == null) {
                Logger.getLogger(FavoritesNode.class.getName()).log(Level.INFO, "No FO in node: {0}:{1}", //NOI18N
                        new Object[] { getOriginal().getName(), getOriginal()});
            } else {
                //Check if it is root.
                isRoot = fo.isRoot();
            }
            
            if (isRoot) {
                return createActionsForRoot(arr, FavoritesNode.getNode().equals(this.getParentNode()));
            } else {
                if (FavoritesNode.getNode().equals(this.getParentNode())) {
                    DataShadow ds = getShadow();
                    if (ds != null) {
                        if (ds.getOriginal().getPrimaryFile().isFolder()) {
                            return createActionsForFavoriteFolder(arr);
                        } else {
                            return createActionsForFavoriteFile(arr);
                        }
                    }
                } else {
                    if (fo != null) {
                        if (fo.isFolder()) {
                            return createActionsForFolder(arr);
                        } else {
                            return createActionsForFile(arr);
                        }
                    }
                }
            }
            //Unknown node - return unmodified actions.
            return arr;
        }
        
        /** Do not change original actions. */
        private Action [] createActionsForRoot (Action [] arr, boolean removeAvailable) {
            // Only Remove from favorites is added
            if (removeAvailable) {
                List<Action> newArr = new ArrayList<>(arr.length + 2);
                newArr.addAll(Arrays.asList(arr));
                if (!newArr.isEmpty()) {
                    newArr.add(null);
                }
                newArr.add(Actions.remove());
                return newArr.toArray(new Action[0]);
            } else {
                return arr;
            }
        }
        
        /** Add action 'Remove from Favorites'. */
        private Action [] createActionsForFavoriteFolder (Action [] arr) {
            boolean added = false;
            List<Action> newArr = new ArrayList<>();
            for (Action arr1 : arr) {
                //Add before CopyAction or CutAction
                if (!added && ((arr1 instanceof CopyAction) || (arr1 instanceof CutAction))) {
                    added = true;
                    newArr.add(Actions.remove());
                    newArr.add(null);
                }
                //Do not add Delete action
                if (arr1 instanceof DeleteAction ||
                        (arr1 != null && "delete".equals(arr1.getValue("key")))) {
                    continue;
                }
                newArr.add(arr1);
            }
            if (!added) {
                added = true;
                newArr.add(null);
                newArr.add(Actions.remove());
            }
            
            return newArr.toArray (new Action[0]);
        }
        
        /** Add action 'Remove from Favorites'. */
        private Action [] createActionsForFavoriteFile (Action [] arr) {
            boolean added = false;
            List<Action> newArr = new ArrayList<>();
            for (Action arr1 : arr) {
                //Add before CopyAction or CutAction
                if (!added && ((arr1 instanceof CopyAction) || (arr1 instanceof CutAction))) {
                    added = true;
                    newArr.add(Actions.remove());
                    newArr.add(null);
                }
                //Do not add Delete action
                if (arr1 instanceof DeleteAction ||
                        (arr1 != null && "delete".equals(arr1.getValue("key")))) {
                    continue;
                }
                newArr.add(arr1);
            }
            if (!added) {
                added = true;
                newArr.add(null);
                newArr.add(Actions.remove());
            }
            return newArr.toArray (new Action[0]);
        }
        
        /** Add action 'Add to Favorites'. */
        private Action [] createActionsForFolder (Action [] arr) {
            boolean added = false;
            List<Action> newArr = new ArrayList<>();
            for (Action arr1 : arr) {
                //Add before CopyAction or CutAction
                if (!added && ((arr1 instanceof CopyAction) || (arr1 instanceof CutAction))) {
                    added = true;
                    newArr.add(Actions.add());
                    newArr.add(null);
                }
                newArr.add(arr1);
            }
            if (!added) {
                added = true;
                newArr.add(null);
                newArr.add(Actions.add());
            }
            return newArr.toArray (new Action[0]);
        }
        
        /** Add action 'Add to Favorites'. */
        private Action [] createActionsForFile (Action [] arr) {
            boolean added = false;
            List<Action> newArr = new ArrayList<>();
            for (Action arr1 : arr) {
                //Add before CopyAction or CutAction
                if (!added && ((arr1 instanceof CopyAction) || (arr1 instanceof CutAction))) {
                    added = true;
                    newArr.add(Actions.add());
                    newArr.add(null);
                }
                newArr.add(arr1);
            }
            if (!added) {
                added = true;
                newArr.add(null);
                newArr.add(Actions.add());
            }
            return newArr.toArray (new Action[0]);
        }

        @Override
        public void childrenAdded(NodeMemberEvent ev) {
        }

        @Override
        public void childrenRemoved(NodeMemberEvent ev) {
        }

        @Override
        public void childrenReordered(NodeReorderEvent ev) {
        }

        @Override
        public void nodeDestroyed(NodeEvent ev) {
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
        }

        private DataShadow getShadow () {
            return ds;
        }
    }

    private static class FavoritesPasteType extends PasteType {
        private final DataObject[] dos;

        private FavoritesPasteType(DataObject[] dos) {
            this.dos = dos;
        }

        @Override
        public Transferable paste() throws IOException {
            Tab.RP.post(() -> Actions.Add.addToFavorites(Arrays.asList(dos)));
            return null;
        }

    }
    
    private static class FavoritesExternalPasteType extends PasteType {
        private final List<File> files;

        private FavoritesExternalPasteType (List<File> files) {
            this.files = files;
        }

        @Override
        public Transferable paste() throws IOException {
            Tab.RP.post(() -> {
                Set<FileObject> fos = new HashSet<>(files.size());
                for (File f : files) {
                    FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(f));
                    if (fo != null) {
                        fos.add(fo);
                    }
                }
                if (!fos.isEmpty()) {
                    try {
                        Favorites.getDefault().add(fos.toArray(new FileObject[0]));
                    } catch (DataObjectNotFoundException ex) {
                        Logger.getLogger(FavoritesNode.class.getName()).log(Level.INFO, null, ex);
                    }
                }
            });
            return null;
        }

    }
}
