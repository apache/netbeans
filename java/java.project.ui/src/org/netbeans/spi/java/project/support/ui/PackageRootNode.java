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

package org.netbeans.spi.java.project.support.ui;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.SourceGroup;
import static org.netbeans.spi.java.project.support.ui.Bundle.*;
import org.netbeans.spi.search.SearchInfoDefinitionFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.MultiTransferObject;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/** Node displaying a packages in given SourceGroup
 * @author Petr Hrebejk
 */
final class PackageRootNode extends AbstractNode implements Runnable, FileStatusListener {

    static final @StaticResource String PACKAGE_BADGE = "org/netbeans/spi/java/project/support/ui/packageBadge.gif";
    static final RequestProcessor PKG_VIEW_RP = new RequestProcessor(PackageRootNode.class.getName(),1);
    private static final AtomicReference<Action[]> actions = new AtomicReference<Action[]>();
        
    private SourceGroup group;

    private final FileObject file;
    private final Set<FileObject> files;
    private FileStatusListener fileSystemListener;
    private RequestProcessor.Task task;
    private volatile boolean iconChange;
    private volatile boolean nameChange;
    
    PackageRootNode( SourceGroup group ) {
        this(group, new PackageViewChildren(group));
    }
    
    private PackageRootNode( SourceGroup group, Children ch) {
        super(ch, new ProxyLookup(createLookup(group), Lookups.singleton(
                SearchInfoDefinitionFactory.createSearchInfoBySubnodes(ch))));
        this.group = group;
        file = group.getRootFolder();
        files = Collections.singleton(file);
        try {
            FileSystem fs = file.getFileSystem();
            fileSystemListener = FileUtil.weakFileStatusListener(this, fs);
            fs.addFileStatusListener(fileSystemListener);
        } catch (FileStateInvalidException e) {            
            Exceptions.printStackTrace(Exceptions.attachMessage(e,"Can not get " + file + " filesystem, ignoring...")); //NOI18N
        }
        setName( group.getName() );
        setDisplayName( group.getDisplayName() );        
        // setIconBase("org/netbeans/modules/java/j2seproject/ui/resources/packageRoot");
    }

    public @Override Image getIcon(int type) {
        return computeIcon( false, type );
    }
        
    public @Override Image getOpenedIcon(int type) {
        return computeIcon( true, type );
    }
    
    public @Override String getDisplayName() {
        String s = super.getDisplayName ();

        try {            
            s = file.getFileSystem ().getDecorator ().annotateName (s, files);
        } catch (FileStateInvalidException e) {
            Exceptions.printStackTrace(e);
        }

        return s;
    }

    public @Override String getHtmlDisplayName() {
         try {
            String result = file.getFileSystem().getDecorator().annotateNameHtml (
                super.getDisplayName(), files);

            //Make sure the super string was really modified
            if (result != null && !super.getDisplayName().equals(result)) {
                return result;
            }
         } catch (FileStateInvalidException e) {
             Exceptions.printStackTrace(e);
         }
         return super.getHtmlDisplayName();
    }

    public void run() {
        if (iconChange) {
            fireIconChange();
            fireOpenedIconChange();
            iconChange = false;
        }
        if (nameChange) {
            fireDisplayNameChange(null, null);
            nameChange = false;
        }
    }

    public void annotationChanged(FileStatusEvent event) {
        if (task == null) {
            task = PKG_VIEW_RP.create(this);
        }

        if ((iconChange == false && event.isIconChange())  || (nameChange == false && event.isNameChange())) {
            if (event.hasChanged(file)) {
                iconChange |= event.isIconChange();
                nameChange |= event.isNameChange();
            }
        }

        task.schedule(50);  // batch by 50 ms
    }    

    @Override
    @NonNull
    public Action[] getActions(boolean context) {
        Action[] res = actions.get();
        if (res == null) {
            res = PackageView.createRootNodeActions();
            if(!actions.compareAndSet(null, res)) {
                res = actions.get();
            }
        }
        assert res != null;
        return res;
    }

    // Show reasonable properties of the DataFolder,
    //it shows the sorting names as rw property, the name as ro property and the path to root as ro property
    @Messages({
        "PROP_name=Name",
        "HINT_name=Package Name",
        "PROP_rootpath=Source Root",
        "HINT_rootpath=Source Root"
    })
    @Override public PropertySet[] getPropertySets() {
        final PropertySet[] properties =  getDataFolderNodeDelegate().getPropertySets();
        final PropertySet[] newProperties = Arrays.copyOf(properties, properties.length);
        for (int i=0; i< newProperties.length; i++) {
            if (Sheet.PROPERTIES.equals(newProperties[i].getName())) {
                //Replace the Sheet.PROPERTIES by the new one
                //having the ro name property and ro path property
                newProperties[i] = Sheet.createPropertiesSet();
                ((Sheet.Set)newProperties[i]).put(new PropertySupport.ReadOnly<String>(DataObject.PROP_NAME, String.class,
                        PROP_name(), HINT_name()) {
                    @Override
                    public String getValue() {
                        return PackageRootNode.this.getDisplayName();
                    }
                });
                ((Sheet.Set)newProperties[i]).put(new PropertySupport.ReadOnly<String>("ROOT_PATH", String.class,    //NOI18N
                        PROP_rootpath(), HINT_rootpath()) {
                    @Override
                    public String getValue() {
                        return FileUtil.getFileDisplayName(PackageRootNode.this.file);
                    }
                });
            }
        }
        return newProperties;
    }

    // XXX Paste types - probably not very nice 
    public @Override void createPasteTypes(Transferable t, List<PasteType> list) {
        if (t.isDataFlavorSupported(ExTransferable.multiFlavor)) {
            try {
                MultiTransferObject mto = (MultiTransferObject) t.getTransferData(ExTransferable.multiFlavor);
                List<PackageViewChildren.PackageNode> l = new ArrayList<PackageViewChildren.PackageNode>();
                boolean isPackageFlavor = false;
                boolean hasTheSameRoot = false;
                int op = -1;
                for (int i=0; i < mto.getCount(); i++) {
                    Transferable pt = mto.getTransferableAt(i);
                    DataFlavor[] flavors = mto.getTransferDataFlavors(i);
                    for (int j=0; j< flavors.length; j++) {
                        if (PackageViewChildren.SUBTYPE.equals(flavors[j].getSubType ()) &&
                                PackageViewChildren.PRIMARY_TYPE.equals(flavors[j].getPrimaryType ())) {
                            if (op == -1) {
                                op = Integer.valueOf (flavors[j].getParameter (PackageViewChildren.MASK)).intValue ();
                            }
                            PackageViewChildren.PackageNode pkgNode = (PackageViewChildren.PackageNode) pt.getTransferData(flavors[j]);
                            if ( !((PackageViewChildren)getChildren()).getRoot().equals( pkgNode.getRoot() ) ) {
                                l.add(pkgNode);
                            }
                            else {
                                hasTheSameRoot = true;
                            }
                            isPackageFlavor = true;
                        }
                    }
                }
                if (isPackageFlavor && !hasTheSameRoot) {
                    list.add(new PackageViewChildren.PackagePasteType(this.group.getRootFolder(),
                            l.toArray(new PackageViewChildren.PackageNode[0]),
                            op));
                }
                else if (!isPackageFlavor) {
                    list.addAll( Arrays.asList( getDataFolderNodeDelegate().getPasteTypes( t ) ) );
                }
            } catch (UnsupportedFlavorException e) {
                Exceptions.printStackTrace(e);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        else {
            DataFlavor[] flavors = t.getTransferDataFlavors();
            FileObject root = this.group.getRootFolder();
            boolean isPackageFlavor = false;
            if (root!= null  && root.canWrite()) {
                for (DataFlavor flavor : flavors) {
                    if (PackageViewChildren.SUBTYPE.equals(flavor.getSubType ()) &&
                            PackageViewChildren.PRIMARY_TYPE.equals(flavor.getPrimaryType ())) {
                        isPackageFlavor = true;
                        try {
                            int op = Integer.parseInt(flavor.getParameter(PackageViewChildren.MASK));
                            PackageViewChildren.PackageNode pkgNode = (PackageViewChildren.PackageNode) t.getTransferData(flavor);
                            if ( !((PackageViewChildren)getChildren()).getRoot().equals( pkgNode.getRoot() ) ) {
                                list.add(new PackageViewChildren.PackagePasteType (root, new PackageViewChildren.PackageNode[] {pkgNode}, op));
                            }
                        } catch (IOException ioe) {
                            Exceptions.printStackTrace(ioe);
                        }
                        catch (UnsupportedFlavorException ufe) {
                            Exceptions.printStackTrace(ufe);
                        }
                    }
                }
            }
            if (!isPackageFlavor) {
                list.addAll( Arrays.asList( getDataFolderNodeDelegate().getPasteTypes( t ) ) );
            }
        }
    }
    
    @Override
    public PasteType getDropType(Transferable t, int action, int index) {
        PasteType pasteType = super.getDropType(t, action, index);
        //The pasteType can be:
        // 1) PackagePasteType - the t.flavor is package flavor
        // 2) null or DataPasteType - the t.flavor in not package flavor
        if (pasteType instanceof PackageViewChildren.PackagePasteType) {
            ((PackageViewChildren.PackagePasteType)pasteType).setOperation (action);
        }
        return pasteType;
    }

    // Private methods ---------------------------------------------------------
    
    private Node getDataFolderNodeDelegate() {
        DataFolder df = getLookup().lookup(DataFolder.class);
        try {
            if (df.isValid()) {
                return df.getNodeDelegate();
            } 
        } catch (IllegalStateException e) {
            //The data systems API is not thread save,
            //the DataObject may become invalid after isValid call and before
            //getNodeDelegate call, we have to catch the ISE. When the DataObject
            //is valid - other cause rethrow it otherwise return leaf node.
            //todo: The DataObject.getNodedelegate should throw specialized exception type.
            if (df.isValid()) {
                throw e;
            }
        }
        return new AbstractNode(Children.LEAF);
    }
    
    private Image computeIcon( boolean opened, int type ) {
        Image image;
        Icon icon = group.getIcon( opened );
        
        if ( icon == null ) {
            image = opened ? getDataFolderNodeDelegate().getOpenedIcon( type ) : 
                             getDataFolderNodeDelegate().getIcon( type );
            image = ImageUtilities.mergeImages(image, ImageUtilities.loadImage(PACKAGE_BADGE), 7, 7);
        }
        else {
            image = ImageUtilities.icon2Image(icon);
        }
        
        return image;        
    }
    
    private static Lookup createLookup( SourceGroup group ) {
        // XXX Remove DataFolder when paste, find and refresh are reimplemented
        FileObject rootFolder = group.getRootFolder();
        DataFolder dataFolder = DataFolder.findFolder( rootFolder );        
        return Lookups.fixed(dataFolder, new PathFinder(group));
    }
    
    /** If contained in the lookup can perform the search for a node
     */    
    public static class PathFinder implements org.netbeans.spi.project.ui.PathFinder {
        
        private SourceGroup group;
        
        public PathFinder( SourceGroup group ) {
            this.group = group;
        }
        
        @Override
        public Node findPath( Node root, Object object ) {
            FileObject fo;
            if (object instanceof FileObject) {
                fo = (FileObject) object;
            } else if (object instanceof DataObject) {
                fo = ((DataObject) object).getPrimaryFile();
            } else {
                return null;
            }
            
            FileObject groupRoot = group.getRootFolder();
            if ( FileUtil.isParentOf( groupRoot, fo ) /* && group.contains( fo ) */ ) {
                // The group contains the object

                String relPath = FileUtil.getRelativePath( groupRoot, fo.isFolder() ? fo : fo.getParent() );

                String[] path = new String[] { relPath.replace( '/', '.' ) };
                try {
                    Node packageNode = NodeOp.findPath( root, path );
                    if (fo.isFolder()) {
                        return packageNode;
                    } else {
                        for (Node child : packageNode.getChildren().getNodes(true)) {
                           DataObject dobj = child.getLookup().lookup(DataObject.class);
                           if (dobj != null && dobj.getPrimaryFile().getNameExt().equals(fo.getNameExt())) {
                               return child;
                           }
                        }
                    }
                }
                catch ( NodeNotFoundException e ) {
                    // did not manage to find it after all... why?
                    return null;
                }
            }   
            else if ( groupRoot.equals( fo ) ) {
                // First try to find default package
                try {
                    return NodeOp.findPath( root, new String[] { "" } ); // NOI18N
                }
                catch ( NodeNotFoundException e ) {
                    // If it does not exists return this node
                }                        
                return root;
            }

            return null;
        }
        
        public @Override String toString() {
            return "PathFinder[" + group + "]"; // NOI18N
        }
                    
    }
}
