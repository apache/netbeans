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

package org.openide.loaders;


import java.awt.datatransfer.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import javax.swing.Action;
import org.netbeans.modules.openide.loaders.DataNodeUtils;
import org.netbeans.modules.openide.loaders.UIException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.PasteType;

/** Standard node representing a data object.
*
* @author Jaroslav Tulach
*/
public class DataNode extends AbstractNode {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -7882925922830244768L;

    /** DataObject of this node. */
    private DataObject obj;

    /** property change listener */
    private PropL propL;

    /** should file extensions be displayed? */
    private static boolean showFileExtensions = true;

    /** Name of extension property. Allows to change extension. */
    private static final String PROP_EXTENSION = "extension"; // NOI18N

    /** Create a data node with the given children set for the given data object.
    * @param obj object to work with
    * @param ch children container for the node
    * @see #getShowFileExtensions
    */
    public DataNode (DataObject obj, Children ch) {
        this(obj, ch, null);
    }

    /** Create a data node for a given data object.
    * The provided children object will be used to hold all child nodes.
    * The name is always set to the base name of the primary file;
    * the display name may instead be set to the base name with extension.
    * @param obj object to work with
    * @param ch children container for the node
    * @param lookup the lookup to provide content of {@link #getLookup}
    *   and also {@link #getCookie}
    * @see #getShowFileExtensions
    *
    * @since 5.6
    * @author Libor Kotouc
    */
    public DataNode (DataObject obj, Children ch, Lookup lookup) {
        super (ch, lookup);
        this.obj = obj;

        propL = new PropL ();
        if (lookup == null) {
            setCookieSet(CookieSet.createGeneric(propL));
        }

        obj.addPropertyChangeListener (org.openide.util.WeakListeners.propertyChange (propL, obj));

        super.setName (obj.getName ());
        updateDisplayName ();
    }

    private void updateDisplayName () {
        FileObject prim = obj.getPrimaryFile ();
        String newDisplayName;
        
        if (prim.isRoot()) {
            newDisplayName = FileUtil.getFileDisplayName(prim);
        } else if (showFileExtensions || obj instanceof DataFolder || obj instanceof DefaultDataObject) {
            newDisplayName = prim.getNameExt();
        } else {
            newDisplayName = prim.getName ();
        }

        if (displayFormat != null)
            setDisplayName (displayFormat.format (new Object[] { newDisplayName }));
        else
            setDisplayName (newDisplayName);
    }

    /** Get the represented data object.
     * @return the data object
    */
    public DataObject getDataObject() {
        return obj;
    }

    /** Changes the name of the node and may also rename the data object.
    * If the object is renamed and file extensions are to be shown,
    * the display name is also updated accordingly.
    *
    * @param name new name for the object
    * @param rename rename the data object?
    * @exception IllegalArgumentException if the rename failed
    */
    public void setName (String name, boolean rename) {
        try {
            if (rename) {
                obj.rename (name);
            }
            super.setName (name);
            updateDisplayName ();
        } catch (IOException ex) {
            String msg = Exceptions.findLocalizedMessage(ex);
            if (msg == null) {
                msg = NbBundle.getMessage(DataNode.class, "MSG_renameError", getName(), name); // NOI18N
            }
            RuntimeException e = new IllegalArgumentException(ex);
            Exceptions.attachLocalizedMessage(e, msg);
            throw e;
        }
    }

    /* Rename the data object.
    * @param name new name for the object
    * @exception IllegalArgumentException if the rename failed
    */
    @Override
    public void setName (String name) {
        setName (name, true);
    }


    /** Get the display name for the node.
     * A filesystem may {@link org.openide.filesystems.FileSystem#getDecorator specially alter} this.
     * Subclassers overriding this method should consider the recommendations
     * in {@link DataObject#createNodeDelegate}.
     * @return the desired name
    */
    @Override
    public String getDisplayName () {
        String s = super.getDisplayName ();

        try {
            s = obj.getPrimaryFile ().getFileSystem ().getDecorator().annotateName (s, new LazyFilesSet());
        } catch (FileStateInvalidException e) {
            // no fs, do nothing
        }

        return s;
    }

     
    /**
     * Get a display name formatted using the limited HTML subset supported by
     * <code>HtmlRenderer</code>. If the underlying
     * <code>StatusDecorator</code> supports HTML annotations, this method
     * will return non-null if status information is added.
     *
     * @return a string containing compliant HTML markup or null
     * @see org.openide.awt.HtmlRenderer
     * @see org.openide.nodes.Node#getHtmlDisplayName
     * @since 4.13
     */
    @Override
    public String getHtmlDisplayName() {
        try {
            StatusDecorator stat = obj.getPrimaryFile().getFileSystem().getDecorator();
            String result = stat.annotateNameHtml(
                    super.getDisplayName(), new LazyFilesSet());

            //Make sure the super string was really modified
            if (result != null && !super.getDisplayName().equals(result)) {
                return result;
            }
        } catch (FileStateInvalidException e) {
            //do nothing and fall through
        }
        return super.getHtmlDisplayName();
    }
     
     private java.awt.Image getImageFromFactory(int type) {
         MimeFactory<?> fact = getLookup().lookup(MimeFactory.class);
         return fact != null ? fact.getImage(type) : null;
     }

    /** Get the displayed icon for this node.
     * Subclassers overriding this method should consider the recommendations
     * in {@link DataObject#createNodeDelegate}.
     * @param type the icon type from {@link java.beans.BeanInfo}
     * @return the desired icon
    */
    @Override
    public java.awt.Image getIcon (int type) {
        java.awt.Image img = getImageFromFactory(type);
        if (img == null) {
            img = super.getIcon (type);
        }

        try {
            img = FileUIUtils.getImageDecorator(obj.getPrimaryFile ().getFileSystem ()).annotateIcon (img, type, new LazyFilesSet());
        } catch (FileStateInvalidException e) {
            // no fs, do nothing
        }

        return img;
    }

    /** Get the displayed icon for this node.
     * Subclassers overriding this method should consider the recommendations
     * in {@link DataObject#createNodeDelegate}.
    * @param type the icon type from {@link java.beans.BeanInfo}
    * @return the desired icon
    */
    @Override
    public java.awt.Image getOpenedIcon (int type) {
        java.awt.Image img = getImageFromFactory(type);
        if (img == null) {
            img = super.getOpenedIcon(type);
        }

        try {
            img = FileUIUtils.getImageDecorator(obj.getPrimaryFile ().getFileSystem ()).annotateIcon (img, type, new LazyFilesSet());
        } catch (FileStateInvalidException e) {
            // no fs, do nothing
        }

        return img;
    }
    
    @Override
    public HelpCtx getHelpCtx () {
        return obj.getHelpCtx ();
    }

    /** Indicate whether the node may be renamed.
    * @return tests {@link DataObject#isRenameAllowed}
    */
    @Override
    public boolean canRename () {
        return obj.isRenameAllowed ();
    }

    /** Indicate whether the node may be destroyed.
     * @return tests {@link DataObject#isDeleteAllowed}
     */
    @Override
    public boolean canDestroy () {
        return obj.isDeleteAllowed ();
    }

    /**
     * Removes the node from its parent, deletes it and fires a property change.
     * Since a DataNode is always bound to a DataObject, which itself is rooted
     * in a filesystem, destroying a DataNode also implies deleting the associated
     * FileObject.
     */
    @Override
    public void destroy () throws IOException {
        if (obj.isDeleteAllowed ()) {
            obj.delete ();
        }
        super.destroy ();
    }

    /* Returns true if this object allows copying.
    * @returns true if this object allows copying.
    */
    @Override
    public boolean canCopy () {
        return obj.isCopyAllowed ();
    }

    /* Returns true if this object allows cutting.
    * @returns true if this object allows cutting.
    */
    @Override
    public boolean canCut () {
        return obj.isMoveAllowed ();
    }

    @Override
    protected void createPasteTypes(Transferable t, List<PasteType> s) {
        super.createPasteTypes(t, s);
        if (!(this instanceof DataFolder.FolderNode)) {
            s.addAll(getPasteTypesFromParent(t)); // #250134
        }
    }

    /**
     * Get paste types from parent folder. To be able to achieve this, we need
     * to know on which node the paste operation was originally invoked (it is
     * usually some FilterNode, not this DataNode), otherwise information about
     * parent node is not available. Thus, Transferable passed from
     * o.n.m.openide.explorer.ExplorerActionsImpl.updatePasteTypes implements
     * Lookup.Provider, and getLookup(Node.class) invoked on it returns the
     * original node. See bug 250134.
     *
     * @param t The transferable.
     * @return List of parent node's paste types (can be empty).
     */
    private List<PasteType> getPasteTypesFromParent(Transferable t) {
        if (t instanceof Lookup.Provider) {
            Lookup l = ((Lookup.Provider) t).getLookup();
            Node n = l.lookup(Node.class);
            if (n != null) {
                Node parentNode = n.getParentNode();
                if (parentNode != null && DataNode.isNodeForFolder(parentNode)) {
                    PasteType[] pts = parentNode.getPasteTypes(t);
                    PasteType[] updated = updateParentPasteTypes(pts);
                    return Arrays.asList(updated);
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Check whether a node represents a file-system folder.
     * <p>
     * Note: Simply checking type for FolderNode is not sufficient, as we need
     * to support also FolderNodes wrapped in FilterNodes.
     * </p>
     *
     * @param node Node to check.
     * @return True if the node represents a folder, false otherwise.
     */
    private static boolean isNodeForFolder(Node node) {
        Collection<? extends FileObject> fos = node.getLookup().lookupAll(FileObject.class);
        if (fos.size() == 1) {
            FileObject fo = fos.iterator().next();
            if (fo.isFolder()) {
                return true;
            }
        }
        Collection<? extends DataObject> dos = node.getLookup().lookupAll(DataObject.class);
        if (dos.size() == 1) {
            DataObject dob = dos.iterator().next();
            if (dob.getPrimaryFile().isFolder()) {
                return true;
            }
        }
        return false;
    }

    @NbBundle.Messages({
        "# Text appended to action name so that it is clear that the action",
        "# will be invoked on parent node. For example:",
        "# Paste -> Copy (to parent); Paste -> Refactory Copy... (to parent)",
        "# Please note the leading space.",
        "LBL_PasteToParent= (to parent)"
    })
    private PasteType[] updateParentPasteTypes(PasteType[] parentTypes) {
        PasteType[] ret = new PasteType[parentTypes.length];
        for (int i = 0; i < parentTypes.length; i++) {
            final PasteType parentType = parentTypes[i];
            ret[i] = new PasteType() {
                @Override
                public Transferable paste() throws IOException {
                    return parentType.paste();
                }

                @Override
                public String getName() {
                    return parentType.getName() + Bundle.LBL_PasteToParent();
                }

                @Override
                public HelpCtx getHelpCtx() {
                    return parentType.getHelpCtx();
                }
            };
        }
        return ret;
    }

    /** This method returns null to signal that actions
    * provide by DataLoader.getActions should be returned from 
    * method getActions. If overriden to provide some actions,
    * then these actions will be preferred to the loader's ones.
    *
    * @return null
     * @deprecated Use {@link #getActions(boolean)} or do nothing and let the
     *             data loader specify actions.
    */
    @Deprecated
    @Override
    protected SystemAction[] createActions () {
        return null;
    }

    /** Get actions for this data object.
    * @see DataLoader#getActions
    * @return array of actions or <code>null</code>
    */
    @Override
    public Action[] getActions (boolean context) {
        if (systemActions == null) {
            systemActions = createActions ();
        }

        if (systemActions != null) {
            return systemActions;
        }

        MimeFactory<?> mime = getLookup().lookup(MimeFactory.class);
        if (mime != null) {
            return mime.getActions();
        }

        return obj.getLoader ().getSwingActions ();
    }

    /** Get actions for this data object.
    * @deprecated Use getActions(boolean)
    * @return array of actions or <code>null</code>
    */
    @Deprecated
    @Override
    public SystemAction[] getActions () {
        if (systemActions == null) {
            systemActions = createActions ();
        }

        if (systemActions != null) {
            return systemActions;
        }

        return obj.getLoader ().getActions ();
    }

    
    /** Get default action. In the current implementation the 
    *<code>null</code> is returned in case the underlying data 
    * object is a template. The templates should not have any default 
    * action.
    * @return no action if the underlying data object is a template. 
    *    Otherwise the abstract node's default action is returned, if <code>null</code> then
    *    the first action returned from getActions (false) method is used.
    */
    @Override
    public Action getPreferredAction () {
        if (obj.isTemplate ()) {
            return null;
        } else {
            Action action = super.getPreferredAction ();
            if (action != null) {
                return action;
            }
            Action[] arr = getActions(false);
            if (arr != null && arr.length > 0) {
                return arr[0];
            }
            return null;
        }
    }

    /** Get a cookie.
     * First of all {@link DataObject#getCookie} is
    * called. If it produces non-<code>null</code> result, that is returned.
    * Otherwise the superclass is tried.
    * Subclassers overriding this method should consider the recommendations
    * in {@link DataObject#createNodeDelegate}. Since version 5.6, if 
    * non-null {@link Lookup} is passed to the constructor, then this 
    * method directly delegates to <a href="@org-openide-nodes@/org/openide/nodes/Node.html">super.getCookie</a> and does
    * not query data object at all. This is supposed to provide consistency
    * between results in <code>getLookup().lookup</code> and <code>getCookie</code>.
    *
    * @return the cookie or <code>null</code>
    */
    @Override
    public <T extends Node.Cookie> T getCookie(Class<T> cl) {
        if (ownLookup()) {
            return super.getCookie(cl);
        }
        T c = obj.getCookie(cl);
        if (c != null) {
            return c;
        } else {
            return super.getCookie (cl);
        }
    }

    /* Initializes sheet of properties. Allow subclasses to
    * overwrite it.
    * @return the default sheet to use
    */
    @Override
    protected Sheet createSheet () {
        Sheet s = Sheet.createDefault ();
        Sheet.Set ss = s.get (Sheet.PROPERTIES);

        Node.Property p;

        p = createNameProperty (obj);
        ss.put (p);

        FileObject fo = getDataObject().getPrimaryFile();
        if (couldBeTemplate(fo) && fo.canWrite()) {
            try {            
                p = new PropertySupport.Reflection<Boolean>(obj, Boolean.TYPE, "isTemplate", "setTemplate"); // NOI18N
                p.setName(DataObject.PROP_TEMPLATE);
                p.setDisplayName(DataObject.getString("PROP_template"));
                p.setShortDescription(DataObject.getString("HINT_template"));
                ss.put(p);
            } catch (Exception ex) {
                throw new InternalError();
            }
        }

        if (fo.isData()) {
            if (!obj.getPrimaryFile().getNameExt().equals(obj.getName())) {
                // show extension property only if name differs from getNameExt
                ss.put(new ExtensionProperty());
            }
            ss.put(new SizeProperty());
            ss.put(new LastModifiedProperty());
        }
        ss.put(new AllFilesProperty()); // #120560, #188315
        return s;
    }
    
    @Override
    public Object getValue(String attributeName) {

        if ("slowRename".equals(attributeName)) { // NOI18N
            return Boolean.TRUE; // #232045
        }
        return super.getValue(attributeName);
    }

    private static boolean couldBeTemplate(FileObject fo) {
        FileSystem fs;
        try {
            fs = fo.getFileSystem();
        } catch (FileStateInvalidException e) {
            return false;
        }
        return fs.isDefault() && fo.getPath().startsWith("Templates/"); // NOI18N
    }
    
    /**
     * A property with a list of all contained files.
     * Sorted to first show primary file, then all secondary files alphabetically.
     * Shows absolute file path or the closest equivalent.
     */
    private final class AllFilesProperty extends PropertySupport.ReadOnly<String[]> {
        
        public AllFilesProperty() {
            super(DataObject.PROP_FILES, String[].class,
                  DataObject.getString("PROP_files"), DataObject.getString("HINT_files"));
        }
       
        public String[] getValue() {
            Set<FileObject> files = obj.files();
            FileObject primary = obj.getPrimaryFile();
            String[] res = new String[files.size()];
            assert files.contains(primary);

            int i=1;
            for (Iterator<FileObject> it = files.iterator(); it.hasNext(); ) {
                FileObject next = it.next();
                res[next == primary ? 0 : i++] = name(next);
            }
            
            Arrays.sort(res, 1, res.length);
            return res;
        }
        
        private String name(FileObject fo) {
            return FileUtil.getFileDisplayName(fo);
        }
        
    }
    
    private final class SizeProperty extends PropertySupport.ReadOnly<Long> {
        
        public SizeProperty() {
            super("size", Long.TYPE, DataObject.getString("PROP_size"), DataObject.getString("HINT_size"));
        }
        
        public Long getValue() {
            return new Long(getDataObject().getPrimaryFile().getSize());
        }
        
    }
    
    private final class LastModifiedProperty extends PropertySupport.ReadOnly<Date> {
        
        public LastModifiedProperty() {
            super("lastModified", Date.class, DataObject.getString("PROP_lastModified"), DataObject.getString("HINT_lastModified"));
        }
        
        public Date getValue() {
            return getDataObject().getPrimaryFile().lastModified();
        }
        
    }

    /** 
     * A property with an extension of this object. It allows to change the extension (#27444).
     */
    private final class ExtensionProperty extends PropertySupport.ReadWrite<String> {

        public ExtensionProperty() {
            super(PROP_EXTENSION, String.class,
                    DataObject.getString("PROP_extension"), DataObject.getString("HINT_extension"));  //NOI18N
        }

        @Override
        public boolean canWrite() {
            return obj.isRenameAllowed();
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return obj.getPrimaryFile().getExt();
        }

        @Override
        public void setValue(final String newExt) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            if (getValue().equals(newExt)) {
                // #164819 - no change when string editor canceled
            } else {
                if (obj.isModified()) {
                    String message = DataObject.getString("ERROR_extension");  //NOI18N
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message));
                    return;
                }
                DataNodeUtils.reqProcessor(obj.getPrimaryFile()).post(new Runnable() { // #232671
                    @Override
                    public void run() {
                        setNewExt(newExt);
                    }
                });
            }
        }

        private void setNewExt(String newExt) {
            try {
                FileObject prim = obj.getPrimaryFile();
                FileLock lock = prim.lock();
                try {
                    prim.rename(lock, prim.getName(), newExt);
                } finally {
                    lock.releaseLock();
                }
                // Dispose current DataObject which enforces refresh
                // and new DataObject will be created.
                obj.dispose();
                if (obj instanceof MultiDataObject) {
                    // Refresh folder to show possible new single DataObjects
                    // (e.g. when renaming form DataObject).
                    FolderList folderList = FolderList.find(prim.getParent(), true);
                    folderList.getChildren(); // need to be here - refresh is not enough
                    folderList.refresh();
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }

    /** Copy this node to the clipboard.
    *
    * @return {@link org.openide.util.datatransfer.ExTransferable.Single} with one copy flavor
    * @throws IOException if it could not copy
    * @see org.openide.nodes.NodeTransfer
    */
    @Override
    public Transferable clipboardCopy () throws IOException {
        ExTransferable t = ExTransferable.create (super.clipboardCopy ());
        t.put (LoaderTransfer.transferable (
            getDataObject (), 
            LoaderTransfer.CLIPBOARD_COPY)
        );
        //add extra data flavors to allow dragging the file outside the IDE window
        addExternalFileTransferable( t, getDataObject() );
        return t;
    }

    /** Cut this node to the clipboard.
    *
    * @return {@link org.openide.util.datatransfer.ExTransferable.Single} with one cut flavor
    * @throws IOException if it could not cut
    * @see org.openide.nodes.NodeTransfer
    */
    @Override
    public Transferable clipboardCut () throws IOException {
        ExTransferable t = ExTransferable.create (super.clipboardCut ());
        t.put (LoaderTransfer.transferable (
            getDataObject (), 
            LoaderTransfer.CLIPBOARD_CUT)
        );
        //add extra data flavors to allow dragging the file outside the IDE window
        addExternalFileTransferable( t, getDataObject() );
        return t;
    }
    
    private void addExternalFileTransferable( ExTransferable t, DataObject d ) {
        FileObject fo = d.getPrimaryFile();
        File file = FileUtil.toFile( fo );
        if( null != file ) {
            //windows & mac
            final ArrayList<File> list = new ArrayList<File>(1);
            list.add( file );
            t.put( new ExTransferable.Single( DataFlavor.javaFileListFlavor ) {
                public Object getData() {
                    return list;
                }
            });
            //linux
            final String uriList = Utilities.toURI(file).toString() + "\r\n";
            t.put( new ExTransferable.Single( createUriListFlavor() ) {
                public Object getData() {
                    return uriList;
                }
            });
        }
    }

    private DataFlavor createUriListFlavor () {
        try {
            return new DataFlavor("text/uri-list;class=java.lang.String");
        } catch (ClassNotFoundException ex) {
            //cannot happen
            throw new AssertionError(ex);
        }
    }

    /** Creates a name property for given data object.
    */
    static Node.Property createNameProperty (final DataObject obj) {
        Node.Property p = new org.openide.nodes.PropertySupport.ReadWrite<String>(org.openide.loaders.DataObject.PROP_NAME,
                                                        String.class,
                                                        org.openide.loaders.DataObject.getString("PROP_name"),
                                                        org.openide.loaders.DataObject.getString("HINT_name")) {

            public String getValue() {
                return obj.getName();
            }

            public void setValue(String val) throws IllegalAccessException,
                                                              IllegalArgumentException,
                                                              java.lang.reflect.InvocationTargetException {
                if (!canWrite())
                    throw new java.lang.IllegalAccessException();
                if (val == null)
                    throw new java.lang.IllegalArgumentException();
                try {
                    obj.rename(val);
                }
                catch (java.io.IOException ex) {
                    java.lang.String msg = null;

                    if ((ex.getLocalizedMessage() == null) ||
                        (ex.getLocalizedMessage().equals(ex.getMessage()))) {
                        msg = org.openide.util.NbBundle.getMessage(org.openide.loaders.DataNode.class,
                                                                   "MSG_renameError",
                                                                   obj.getName(),
                                                                   val);
                    } else {
                        msg = ex.getLocalizedMessage();
                    }
                    UIException.annotateUser(ex, null, msg, null, null);
                    throw new java.lang.reflect.InvocationTargetException(ex);
                }
            }

            @Override
            public boolean canWrite() {
                return obj.isRenameAllowed();
            }
            // #33296 - suppress custom editor

            @Override
            public Object getValue(String key) {
                if ("suppressCustomEditor".equals(key)) {
                    return Boolean.TRUE;
                } else {
                    return super.getValue(key);
                }
            }
        };

        return p;
    }
    
    /** Update files, if we are using CookieSet
     */
    private void updateFilesInCookieSet(Set<FileObject> obj) {
        if (ownLookup()) {
            return;
        }
        getCookieSet().assign(FileObject.class, obj.toArray(new FileObject[0]));
    }

    /** Support for firing property change.
    * @param ev event describing the change
    */
    void fireChange(final PropertyChangeEvent ev) {
        Mutex.EVENT.writeAccess(new Runnable() {
            public void run() {

                if (DataFolder.PROP_CHILDREN.equals(ev.getPropertyName())) {
                    // the node is not interested in children changes
                    return;
                }

                if (DataObject.PROP_PRIMARY_FILE.equals(ev.getPropertyName())) {
                    propL.updateStatusListener();
                    setName(obj.getName(), false);
                    updateFilesInCookieSet(obj.files());
                    return;
                }

                if (DataObject.PROP_FILES.equals(ev.getPropertyName())) {
                    updateFilesInCookieSet(obj.files());
                }

                if (DataObject.PROP_NAME.equals(ev.getPropertyName())) {
                    DataNode.super.setName(obj.getName());
                    updateDisplayName();
                }
                if (DataObject.PROP_COOKIE.equals(ev.getPropertyName())) {
                    fireCookieChange();
                    //return;
                } 
        
                // if the DataOjbect is not valid the node should be
                // removed
                if (DataObject.PROP_VALID.equals(ev.getPropertyName())) {
                    Object newVal = ev.getNewValue();
                    if ((newVal instanceof Boolean) && (!((Boolean) newVal).booleanValue())) {
                        fireNodeDestroyed();
                    }
                    return;
                } 
                
                 /*See #31413*/
                List<String> transmitProperties = Arrays.asList(new String[] {
                    DataObject.PROP_NAME, DataObject.PROP_FILES, DataObject.PROP_TEMPLATE});
                if (transmitProperties.contains(ev.getPropertyName())) {
                    firePropertyChange(ev.getPropertyName(), ev.getOldValue(), ev.getNewValue());
                }                
            }
        });
    }

    /** Handle for location of given data object.
    * @return handle that remembers the data object.
    */
    @Override
    public Node.Handle getHandle () {
        return new ObjectHandle(obj, obj.isValid() ? (this != obj.getNodeDelegate()) : /* to be safe */ true);
    }

    /** Access method to fire icon change.
    */
    final void fireChangeAccess (boolean icon, boolean name) {
        if (name) {
            fireDisplayNameChange (null, null);
        }
        if (icon) {
            fireIconChange ();
        }
    }

    /** Determine whether file extensions should be shown by default.
    * By default, no.
    * @return <code>true</code> if so
    */
    public static boolean getShowFileExtensions () {
        return showFileExtensions;
    }

    /** Set whether file extensions should be shown by default.
     * 
     * <p>Note that this method affects all <code>DataNode</code>s.</p>
     * 
    * @param s <code>true</code> if so
    */
    public static void setShowFileExtensions (boolean s) {
        boolean refresh = ( showFileExtensions != s );
        showFileExtensions = s;
        
        if ( refresh ) {
            // refresh current nodes display name
            Map<RequestProcessor, List<DataObject>> mapping
                    = new HashMap<RequestProcessor, List<DataObject>>();
            Iterator<DataObjectPool.Item> it = DataObjectPool.getPOOL().getActiveDataObjects();

            // Assign DataNodes to RequestProcessors. See bug 252073 comment 17.
            while (it.hasNext()) {
                DataObject obj = it.next().getDataObjectOrNull();
                if (obj != null && obj.getNodeDelegate() instanceof DataNode) {
                    RequestProcessor rp = DataNodeUtils.reqProcessor(obj.getPrimaryFile());
                    List<DataObject> list = mapping.get(rp);
                    if (list == null) {
                        list = new ArrayList<DataObject>();
                        mapping.put(rp, list);
                    }
                    list.add(obj);
                }
            }

            for (Map.Entry<RequestProcessor, List<DataObject>> e : mapping.entrySet()) {
                final List<DataObject> list = e.getValue();
                e.getKey().post(new Runnable() {
                    @Override
                    public void run() {
                        for (DataObject obj: list) {
                            ((DataNode) obj.getNodeDelegate()).updateDisplayName();
                        }
                    }
                }, 300, Thread.MIN_PRIORITY);
            }
        }
    }

    private static Class<?> defaultLookup;
    /** Returns true if this node is using own lookup and not the standard one.
     */
    private boolean ownLookup() {
        if (defaultLookup == null) {
            try {
                defaultLookup = Class.forName("org.openide.nodes.NodeLookup", false, Node.class.getClassLoader());
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
                return false;
            }
        }
        return !defaultLookup.isInstance(getLookup());
    }
    
    /** Request processor task to update a bunch of names/icons.
     * Potentially faster to do many nodes at once; see #16478.
     */
    private static RequestProcessor.Task refreshNamesIconsTask = null;
    /** nodes which should be refreshed */
    private static Set<DataNode> refreshNameNodes = null;
    private static Set<DataNode> refreshIconNodes = null;
    /** whether the task is current scheduled and will still look in above sets */
    private static boolean refreshNamesIconsRunning = false;
    private static final Object refreshNameIconLock = "DataNode.refreshNameIconLock"; // NOI18N
    
    /** Property listener on data object that delegates all changes of
    * properties to this node.
    */
    private class PropL extends Object
    implements PropertyChangeListener, FileStatusListener, CookieSet.Before {
        /** weak version of this listener */
        private FileStatusListener weakL;
        /** previous filesystem we were attached to */
        private FileSystem previous;

        public PropL () {
            updateStatusListener ();
        }

        public void propertyChange (PropertyChangeEvent ev) {
            fireChange (ev);
        }

        /** Updates listening on a status of filesystem.
        */
        private void updateStatusListener () {
            if (previous != null) {
                previous.removeFileStatusListener (weakL);
            }
            try {
                previous = obj.getPrimaryFile ().getFileSystem ();

                if (weakL == null) {
                    weakL = org.openide.filesystems.FileUtil.weakFileStatusListener (this, null);
                }

                previous.addFileStatusListener (weakL);
            } catch (FileStateInvalidException ex) {
                previous = null;
            }
        }

        /** Notifies listener about change in annotataion of a few files.
        * @param ev event describing the change
        */
        public void annotationChanged (FileStatusEvent ev) {
            // #16541: listen for changes in both primary and secondary files
            boolean thisChanged;
            if (getDataObject() instanceof MultiDataObject) {
                MultiDataObject multi = (MultiDataObject)getDataObject();
                thisChanged = ev.hasChanged(multi.getPrimaryFile());
                if (!thisChanged) {
                    for (FileObject fo : multi.getSecondary().keySet()) {
                        if (ev.hasChanged(fo)) {
                            thisChanged = true;
                            break;
                        }
                    }
                }
            } else {
                thisChanged = false;
                
                for (FileObject fo : obj.files()) {
                    if (ev.hasChanged(fo)) {
                        thisChanged = true;
                        break;
                    }
                }
            }
            if (thisChanged) {
                // #12368: fire display name & icon changes asynch
                synchronized (refreshNameIconLock) {
                    boolean post = false;
                    if (ev.isNameChange()) {
                        if (refreshNameNodes == null) {
                            refreshNameNodes = new HashSet<DataNode>();
                        }
                        post |= refreshNameNodes.add(DataNode.this);
                    }
                    if (ev.isIconChange()) {
                        if (refreshIconNodes == null) {
                            refreshIconNodes = new HashSet<DataNode>();
                        }
                        post |= refreshIconNodes.add(DataNode.this);
                    }
                    if (post && !refreshNamesIconsRunning) {
                        refreshNamesIconsRunning = true;
                        if (refreshNamesIconsTask == null) {
                            refreshNamesIconsTask = DataNodeUtils.reqProcessor().post(new NamesUpdater());
                        } else {
                            // Should be OK even if it is running right now.
                            // (Cf. RequestProcessorTest.testScheduleWhileRunning.)
                            refreshNamesIconsTask.schedule(0);
                        }
                    }
                }
            }
        }

        public void beforeLookup(Class<?> clazz) {
            if (clazz.isAssignableFrom(FileObject.class)) {
                updateFilesInCookieSet(obj.files());
            }
        }
    }
            
    private static class NamesUpdater implements Runnable {
        /** Refreshes names and icons for a whole batch of data nodes at once.
         */
        public void run() {
            DataNode[] _refreshNameNodes, _refreshIconNodes;
            synchronized (refreshNameIconLock) {
                if (refreshNameNodes != null) {
                    _refreshNameNodes = refreshNameNodes.toArray(new DataNode[0]);
                    refreshNameNodes.clear();
                } else {
                    _refreshNameNodes = new DataNode[0];
                }
                if (refreshIconNodes != null) {
                    _refreshIconNodes = refreshIconNodes.toArray(new DataNode[0]);
                    refreshIconNodes.clear();
                } else {
                    _refreshIconNodes = new DataNode[0];
                }
                refreshNamesIconsRunning = false;
            }
            // refresh name nodes
            for (final Map.Entry<RequestProcessor, List<DataNode>> e
                    : groupByRP(_refreshNameNodes).entrySet()) {
                e.getKey().post(new Runnable() { // post list to assigned RP
                    @Override
                    public void run() {
                        for (DataNode n: e.getValue()) {
                            n.fireChangeAccess(false, true);
                        }
                    }
                });
            }
            // refresh icon nodes
            for (final Map.Entry<RequestProcessor, List<DataNode>> e
                    : groupByRP(_refreshIconNodes).entrySet()) {
                e.getKey().post(new Runnable() { // post list to assigned RP
                    @Override
                    public void run() {
                        for (DataNode n: e.getValue()) {
                            n.fireChangeAccess(true, false);
                        }
                    }
                });
            }
        }

        /**
         * Group array of nodes by assigned RequestProcessors.
         *
         * @param nodes
         * @return Mapping from RequestProcessor to list of nodes assigned to
         * it.
         */
        private Map<RequestProcessor, List<DataNode>> groupByRP(DataNode nodes[]) {
            Map<RequestProcessor, List<DataNode>> mapping
                    = new HashMap<RequestProcessor, List<DataNode>>();
            for (DataNode node : nodes) {
                DataObject dob = node.getDataObject();
                FileObject fo = dob == null ? null : dob.getPrimaryFile();
                RequestProcessor rp = DataNodeUtils.reqProcessor(fo);
                List<DataNode> set = mapping.get(rp);
                if (set == null) {
                    set = new ArrayList<DataNode>();
                    mapping.put(rp, set);
                }
                set.add(node);
            }
            return mapping;
        }
    }

    /** Handle for data object nodes */
    private static class ObjectHandle implements Node.Handle {
        private FileObject obj;
        private boolean clone;

        static final long serialVersionUID =6616060729084681518L;


        public ObjectHandle (DataObject obj, boolean clone) {
            this.obj = obj.getPrimaryFile ();
            this.clone = clone;
        }

        public Node getNode () throws IOException {
            if (obj == null) {
                // Serialization problem? Seems to occur frequently with connection support:
                // java.lang.IllegalArgumentException: Called DataObject.find on null
                //         at org.openide.loaders.DataObject.find(DataObject.java:435)
                //         at org.openide.loaders.DataNode$ObjectHandle.getNode(DataNode.java:757)
                //         at org.netbeans.modules.java.JavaDataObject$PersistentConnectionHandle.getNode(JavaDataObject.java:977)
                //         at org.openide.loaders.ConnectionSupport$Pair.getNode(ConnectionSupport.java:357)
                //         at org.openide.loaders.ConnectionSupport.register(ConnectionSupport.java:94)
                //         at org.netbeans.modules.java.codesync.SourceConnectionSupport.registerDependency(SourceConnectionSupport.java:475)
                //         at org.netbeans.modules.java.codesync.SourceConnectionSupport.addDependency(SourceConnectionSupport.java:554)
                //         at org.netbeans.modules.java.codesync.ClassDependencyImpl.supertypesAdded(ClassDependencyImpl.java:241)
                //         at org.netbeans.modules.java.codesync.ClassDependencyImpl.refreshClass(ClassDependencyImpl.java:121)
                //         at org.netbeans.modules.java.codesync.SourceConnectionSupport.refreshLinks(SourceConnectionSupport.java:357)
                //         at org.netbeans.modules.java.codesync.SourceConnectionSupport.access$000(SourceConnectionSupport.java:44)
                //         at org.netbeans.modules.java.codesync.SourceConnectionSupport$2.run(SourceConnectionSupport.java:223)
                throw new IOException("File could not be restored"); // NOI18N
            }
            Node n = DataObject.find (obj).getNodeDelegate ();
            return clone ? n.cloneNode () : n;
        }
    }
    
    /** Wrapping class for obj.files(). Used in getIcon() and getDisplayName()
        to have something lazy to pass to annotateIcon() and annotateName()
        instead of calling obj.files() immediately. */
    private class LazyFilesSet implements Set<FileObject> {
        
        private Set<FileObject> obj_files;
        
        private synchronized void lazyInitialization () {
           obj_files = obj.files();
        }
        
        public boolean add(FileObject o) {
            lazyInitialization();
            return obj_files.add(o);
        }
        
        public boolean addAll(Collection<? extends FileObject> c) {
            lazyInitialization();
            return obj_files.addAll(c);
        }
        
        public void clear() {
            lazyInitialization();
            obj_files.clear();
        }
        
        public boolean contains(Object o) {
            lazyInitialization();
            return obj_files.contains(o);
        }
        
        public boolean containsAll(Collection c) {
            lazyInitialization();
            return obj_files.containsAll(c);
        }
        
        public boolean isEmpty() {
            return false;
        }
        
        public Iterator<FileObject> iterator() {
            return new FilesIterator ();
        }
        
        public boolean remove(Object o) {
            lazyInitialization();
            return obj_files.remove(o);
        }
        
        public boolean removeAll(Collection c) {
            lazyInitialization();
            return obj_files.removeAll(c);
        }
        
        public boolean retainAll(Collection c) {
            lazyInitialization();
            return obj_files.retainAll(c);
        }
        
        public int size() {
            lazyInitialization();
            return obj_files.size();
        }
        
        public Object[] toArray() {
            lazyInitialization();
            return obj_files.toArray();
        }
        
        public <FileObject> FileObject[] toArray(FileObject[] a) {
            lazyInitialization();
            return obj_files.toArray(a);
        }

        @Override
        public boolean equals(Object obj) {
            lazyInitialization();
            return (obj instanceof Set) && obj_files.equals(obj);
        }

        @Override
        public String toString() {
            lazyInitialization();
            return obj_files.toString();
        }

        @Override
        public int hashCode() {
            lazyInitialization();
            return obj_files.hashCode();
        }
        
        /** Iterator for FilesSet. It returns the primaryFile first and 
         * then initialize the delegate iterator for secondary files.
         */
        private final class FilesIterator implements Iterator<FileObject> {
            /** Was the first element (primary file) already returned?
             */
            private boolean first = true;

            /** Delegation iterator for secondary files. It is lazy initialized after
             * the first element is returned.
             */
            private Iterator<FileObject> itDelegate = null;

            FilesIterator() {}

            public boolean hasNext() {
                return first ? true : getIteratorDelegate().hasNext();
            }

            public FileObject next() {
                if (first) {
                    first = false;
                    return obj.getPrimaryFile ();
                }
                else {
                    return getIteratorDelegate().next();
                }
            }

            @Override
            public void remove() {
                getIteratorDelegate().remove();
            }

            /** Initialize the delegation iterator.
             */
            private Iterator<FileObject> getIteratorDelegate() {
                if (itDelegate == null) {
                    lazyInitialization ();
                    // this should return iterator of all files of the MultiDataObject...
                    itDelegate = obj_files.iterator ();
                    // ..., so it is necessary to skip the primary file
                    itDelegate.next();
                }
                return itDelegate;
            }
        }
    }    
    
    
    
}
