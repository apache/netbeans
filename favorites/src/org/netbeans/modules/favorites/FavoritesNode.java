/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
        List<File> list = new ArrayList<File>(1);
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
            } catch( java.net.URISyntaxException e ) {
                // malformed URI
            } catch( IllegalArgumentException e ) {
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
                List<Action> newArr = new ArrayList<Action>(arr.length + 2);
                newArr.addAll(Arrays.asList(arr));
                if (!newArr.isEmpty()) {
                    newArr.add(null);
                }
                newArr.add(Actions.remove());
                return newArr.toArray(new Action[newArr.size()]);
            } else {
                return arr;
            }
        }
        
        /** Add action 'Remove from Favorites'. */
        private Action [] createActionsForFavoriteFolder (Action [] arr) {
            boolean added = false;
            List<Action> newArr = new ArrayList<Action>();
            for (int i = 0; i < arr.length; i++) {
                //Add before CopyAction or CutAction
                if (!added && ((arr[i] instanceof CopyAction) || (arr[i] instanceof CutAction))) {
                    added = true;
                    newArr.add(Actions.remove());
                    newArr.add(null);
                }
                //Do not add Delete action
                if (!(arr[i] instanceof DeleteAction)) {
                    newArr.add(arr[i]);
                }
            }
            if (!added) {
                added = true;
                newArr.add(null);
                newArr.add(Actions.remove());
            }
            
            return newArr.toArray (new Action[newArr.size()]);
        }
        
        /** Add action 'Remove from Favorites'. */
        private Action [] createActionsForFavoriteFile (Action [] arr) {
            boolean added = false;
            List<Action> newArr = new ArrayList<Action>();
            for (int i = 0; i < arr.length; i++) {
                //Add before CopyAction or CutAction
                if (!added && ((arr[i] instanceof CopyAction) || (arr[i] instanceof CutAction))) {
                    added = true;
                    newArr.add(Actions.remove());
                    newArr.add(null);
                }
                //Do not add Delete action
                if (!(arr[i] instanceof DeleteAction)) {
                    newArr.add(arr[i]);
                }
            }
            if (!added) {
                added = true;
                newArr.add(null);
                newArr.add(Actions.remove());
            }
            return newArr.toArray (new Action[newArr.size()]);
        }
        
        /** Add action 'Add to Favorites'. */
        private Action [] createActionsForFolder (Action [] arr) {
            boolean added = false;
            List<Action> newArr = new ArrayList<Action>();
            for (int i = 0; i < arr.length; i++) {
                //Add before CopyAction or CutAction
                if (!added && ((arr[i] instanceof CopyAction) || (arr[i] instanceof CutAction))) {
                    added = true;
                    newArr.add(Actions.add());
                    newArr.add(null);
                }
                newArr.add(arr[i]);
            }
            if (!added) {
                added = true;
                newArr.add(null);
                newArr.add(Actions.add());
            }
            return newArr.toArray (new Action[newArr.size()]);
        }
        
        /** Add action 'Add to Favorites'. */
        private Action [] createActionsForFile (Action [] arr) {
            boolean added = false;
            List<Action> newArr = new ArrayList<Action>();
            for (int i = 0; i < arr.length; i++) {
                //Add before CopyAction or CutAction
                if (!added && ((arr[i] instanceof CopyAction) || (arr[i] instanceof CutAction))) {
                    added = true;
                    newArr.add(Actions.add());
                    newArr.add(null);
                }
                newArr.add(arr[i]);
            }
            if (!added) {
                added = true;
                newArr.add(null);
                newArr.add(Actions.add());
            }
            return newArr.toArray (new Action[newArr.size()]);
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
            Tab.RP.post(new Runnable () {
                @Override
                public void run() {
                    Actions.Add.addToFavorites(Arrays.asList(dos));
                }
            });
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
            Tab.RP.post(new Runnable () {
                @Override
                public void run() {
                    Set<FileObject> fos = new HashSet<FileObject>(files.size());
                    for (File f : files) {
                        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(f));
                        if (fo != null) {
                            fos.add(fo);
                        }
                    }
                    if (!fos.isEmpty()) {
                        try {
                            Favorites.getDefault().add(fos.toArray(new FileObject[fos.size()]));
                        } catch (DataObjectNotFoundException ex) {
                            Logger.getLogger(FavoritesNode.class.getName()).log(Level.INFO, null, ex);
                        }
                    }
                }
            });
            return null;
        }

    }
}
