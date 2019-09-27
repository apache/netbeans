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


import java.awt.Component;
import java.awt.Graphics;
import java.beans.*;
import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.event.*;
import org.netbeans.api.actions.Savable;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.openide.loaders.DataObjectAccessor;
import org.netbeans.modules.openide.loaders.DataObjectEncodingQueryImplementation;
import org.netbeans.modules.openide.loaders.Unmodify;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.util.*;

/** Object that represents one or more file objects, with added behavior 
* accessible though {@link #getLookup} lookup pattern. Since version 6.0
* this class implements {@link org.openide.util.Lookup.Provider}.
*
* @author Jaroslav Tulach, Petr Hamernik, Jan Jancura, Ian Formanek
*/
public abstract class DataObject extends Object
implements Node.Cookie, Serializable, HelpCtx.Provider, Lookup.Provider {
    /** generated Serialized Version UID */
    private static final long serialVersionUID = 3328227388376142699L;

    /** Name of the template property. */
    public static final String PROP_TEMPLATE = "template"; // NOI18N

    /** Name of the name property. */
    public static final String PROP_NAME = "name"; // NOI18N

    /** Name of the help context property. */
    public static final String PROP_HELP = "helpCtx"; // NOI18N

    /** Name of the modified property. */
    public static final String PROP_MODIFIED = "modified"; // NOI18N

    /** Name of the property used during notification of changes in the set of cookies attached to this object. */
    public static final String PROP_COOKIE = Node.PROP_COOKIE;

    /** Name of valid property. Allows listening to deletion or disposal of the data object. */
    public static final String PROP_VALID = "valid"; // NOI18N

    /** Name of primary file property. Primary file is changed when the object is moved */
    public static final String PROP_PRIMARY_FILE = "primaryFile"; // NOI18N
    /** Name of files property. Allows listening to set of files handled by this object. */
    public static final String PROP_FILES = "files"; // NOI18N

    private static ThreadLocal<ProgressInfo> PROGRESS_INFO_TL
            = new ThreadLocal<ProgressInfo>();

    /** Extended attribute for holding the class of the loader that should
    * be used to recognize a file object before the normal processing takes
    * place.
    */
    static final String EA_ASSIGNED_LOADER = "NetBeansAttrAssignedLoader"; // NOI18N
    /** Extended attribute which may be used in addition to EA_ASSIGNED_LOADER
     * which indicates the code name base of the module that installed that preferred
     * loader. If the indicated module is not installed, ignore the loader request.
     * See #13816.
     */
    static final String EA_ASSIGNED_LOADER_MODULE = "NetBeansAttrAssignedLoaderModule"; // NOI18N

    private static final Logger OBJ_LOG = Logger.getLogger(DataObject.class.getName());

    /**
     * CAS for {@link DataObject#changeSupport}.
     */
    private static final AtomicReferenceFieldUpdater<DataObject, PropertyChangeSupport> changeSupportUpdater =
        AtomicReferenceFieldUpdater.newUpdater(
            DataObject.class,
            PropertyChangeSupport.class,
            "changeSupport");   //NOI18N

    /** all modified data objects contains DataObjects.
    * ! Use syncModified for modifications instead !*/
    private static final ModifiedRegistry modified = new ModifiedRegistry();
    /** sync modified data (for modification operations) */
    private static final Set<DataObject> syncModified = Collections.synchronizedSet(modified);

    /** Modified flag 
     * @GuardedBy(LOCK)
     */
    private boolean modif = false;

    /** the node delegate for this data object 
     * @GuardedBy(LOCK)
       */
    private transient Node nodeDelegate;
    private static final Node BEING_CREATED = Node.EMPTY.cloneNode();

    /** item with info about this data object 
     * @GuardedBy(DataObjectPool.getPOOL())
     */
    private transient DataObjectPool.Item item;

    /** the loader for this data object */
    private final DataLoader loader;

    /** property change listener support.
     * Threading: lock free, changes HAS to go through {@link DataObject#changeSupportUpdater}.
     */
    private volatile PropertyChangeSupport changeSupport;

    /** vetoable property change listener support 
        *  @GuardedBy(LOCK)
        */
    private VetoableChangeSupport vetoableChangeSupport;

    /** Lock used for ensuring there will be just one node delegate 
       * and also to synchronize on other changed aspects inside of a single DataObject
       */
    private static final Object LOCK = new Object();
    
    /** Lock for copy/move/rename/etc. operations */
    private static Object synchObject = new Object ();


    /** default logger for whole package */
    static final Logger LOG = Logger.getLogger("org.openide.loaders"); // NOI18N

    static {
        DataObjectAccessor.DEFAULT = new DataObjectAccessorImpl();
    }
    
    /** Create a new data object.
     *
     * @param pf primary file object for this data object
     * @param loader loader that created the data object
     * @exception DataObjectExistsException if there is already a data object
     *    for this primary file
     */
    public DataObject (FileObject pf, DataLoader loader) throws DataObjectExistsException {
        // By registering we'll also get notifications about file changes.
        this (pf, DataObjectPool.getPOOL().register (pf, loader), loader);
    }

    /** Private constructor. At this time the constructor receives
    * the primary file and pool item where it should register itself.
    *
    * @param pf primary file
    * @param item the item to register into
    * @param loader loader that created the data object
    */
    private DataObject (FileObject pf, DataObjectPool.Item item, DataLoader loader) {
        OBJ_LOG.log(Level.FINE, "created {0}", pf); // NOI18N
        this.item = item;
        this.loader = loader;
        item.setDataObject (this);
    }

    // This method first unregisters the object, then calls method unreferenced.
    // After that it asks the parent folder to regenerate its list of children,
    // so different object is usually created for primary file of this object.
    /** Allows subclasses to discard the object. When an object is discarded,
    * it is released from the list of objects registered in the system.
    * Then the contents of the parent folder (if it still exists) are rescanned, which
    * may result in the creation of a new data object for the primary file.
    * <P>
    * The normal use of this method is to change the type of a data object.
    * Because this would usually only be invoked from
    * the original data object, it is protected.
    */
    protected void dispose () {
        DataObjectPool.Item i = item();
        
        if (i != null) {
            DataObjectPool.getPOOL().countRegistration(i.primaryFile);
            i.deregister (true);
            i.setDataObject(null);
            firePropertyChange (PROP_VALID, Boolean.TRUE, Boolean.FALSE);
        }
    }

    //
    // Item accessors
    //
    final DataObjectPool.Item item() {
        synchronized (DataObjectPool.getPOOL()) {
            return item;
        }
    }
    private void changeItem(DataObjectPool.Item item) {
        synchronized (DataObjectPool.getPOOL()) {
            this.item = item;
        }
    }
    final void changeItemByFolder(DataObjectPool.Item item) {
        assert this instanceof DataFolder;
        this.changeItem(item);
    }

    /** Setter that allows to destroy this data object. Because such
    * operation can be dangerous and not always possible (if the data object
    * is opened in editor) it can be vetoed. Either by this data object
    * or by any vetoable listener attached to this object (like editor support)
    *
    * @param valid should be false
    * @exception PropertyVetoException if the invalidation has been vetoed
    */
    public void setValid (boolean valid) throws PropertyVetoException {
        if (!valid && isValid ()) {
            markInvalid0 ();
        }
    }
        
    /** Tries to mark the object invalid. Called from setValid or from 
     * MultiDataObject.notifyDeleted
     */
    final void markInvalid0 () throws PropertyVetoException {    
        fireVetoableChange (PROP_VALID, Boolean.TRUE, Boolean.FALSE);
        dispose ();
        setModified(false);
    }

    /** Test whether the data object is still valid and usable.
    * <P>
    * The object can become invalid when it is deleted, its files are deleted, or
    * {@link #dispose} is called.
    * <P>
    * When the validity of the object changes a property change event is fired, so
    * anyone can listen and be notified when the object is deleted/disposed.
    */
    public final boolean isValid () {
        return item().isValid ();
    }



    /** Get the loader that created this data object.
    * @return the data loader
    */
    public final DataLoader getLoader () {
        return loader;
    }

    /** Mark all contained files as belonging to this loader.
     * If the files are rescanned (e.g. after a disposal), the current data loader will be given preference.
    */
    protected final void markFiles () throws IOException {
        Iterator en = files ().iterator ();
        while (en.hasNext ()) {
            FileObject fo = (FileObject)en.next ();
            loader.markFile (fo);
        }
    }

    /** Get all contained files.
     * These file objects should ideally have had the {@linkplain FileObject#setImportant important flag} set appropriately.
    * <P>
    * The default implementation returns a set consisting only of the primary file.
    *
    * @return set of files
    */
    public Set<FileObject> files() {
        return java.util.Collections.singleton (getPrimaryFile ());
    }


    /** Get the node delegate. Either {@link #createNodeDelegate creates it} (if it does not
    * already exist) or
    * returns a previously created instance of it.
    * @return the node delegate (without parent) for this data object
    * @see <a href="doc-files/api.html#delegate">Datasystems API - Node Delegates</a>
    */
    public final Node getNodeDelegate () {
        if (! isValid()) {
            String debugMessage = "this=" + this + " id=" + System.identityHashCode(this) + " primaryFileId=" + System.identityHashCode(this.getPrimaryFile()) + " valid=" + this.getPrimaryFile().isValid() + "\n";  //NOI18N
            DataObject dob = DataObjectPool.getPOOL().find(getPrimaryFile());
            debugMessage += "pool=" + dob;  //NOI18N
            if (dob != null) {
                debugMessage += " id=" + System.identityHashCode(dob);  //NOI18N
                if (dob.getPrimaryFile() != null) {
                    debugMessage += " primaryFileId=" + System.identityHashCode(dob.getPrimaryFile()) + " valid=" + dob.getPrimaryFile().isValid();  //NOI18N
                }
            }

            Exception e = new IllegalStateException("The data object " + getPrimaryFile() + " is invalid; you may not call getNodeDelegate on it any more; see #17020 and please fix your code.\n" + debugMessage); // NOI18N
            Logger.getLogger(DataObject.class.getName()).log(Level.INFO, null, e);
        }
        return getNodeDelegateImpl();
    }
    
    private final Node getNodeDelegateImpl() {
        for (;;) {
            synchronized (LOCK) {
                if (nodeDelegate != null && nodeDelegate != BEING_CREATED) {
                    return nodeDelegate;
                }
            }
            // synchronize on something private, so only one delegate can be created
            // do not synchronize on this, because we could deadlock with
            // subclasses could synchronize too.
            Children.MUTEX.readAccess (new Runnable() {
                @Override
                public void run() {
                    synchronized(LOCK) {
                        if (nodeDelegate == null) {
                            nodeDelegate = BEING_CREATED;
                        } else {
                            if (nodeDelegate == BEING_CREATED) {
                                try {
                                    LOCK.wait();
                                } catch (InterruptedException ex) {
                                    LOG.log(Level.FINE, null, ex);
                                }
                            }
                            return;
                        }
                    }
                    Node newNode = createNodeDelegate();
                    synchronized (LOCK) {
                        if (nodeDelegate == BEING_CREATED) {
                            nodeDelegate = newNode;
                        }
                        LOCK.notifyAll();
                    }
                }
            });

            synchronized (LOCK) {
                if (nodeDelegate == null) {
                    throw new IllegalStateException("DataObject " + this + " has null node delegate"); // NOI18N
                }
            }
        }
    }
    
    /** This method allows DataFolder to filter its nodes.
    *
    * @param filter filter for subdata objects
    * @return the node delegate (without parent) the node is new instance
    *   of node and can be inserted to any place in the hierarchy
    */
    Node getClonedNodeDelegate (DataFilter filter) {
        return getNodeDelegate ().cloneNode ();
    }

    /** Access method for node delagate.
     * @return node delegate or null
     */
    final Node getNodeDelegateOrNull () {
        synchronized (LOCK) {
            return nodeDelegate;
        }
    }

    final void setNodeDelegate(Node n) {
        synchronized (LOCK) {
            nodeDelegate = n;
        }
    }

    /** Provides node that should represent this data object.
    * <p>The default implementation creates an instance of {@link DataNode}.
    * Most subclasses will override this method to provide a <code>DataNode</code>
    * (usually subclassed).
    * <P>
    * This method is called only once per data object.
    * <p>It is strongly recommended that the resulting node will, when asked for
    * the cookie <samp>DataObject.class</samp>, return this same data object.
    * <p>It is also recommended that the node:
    * <ol>
    * <li>Base its name on {@link #getName}.
    * <li>Base its display name additionally on {@link DataNode#getShowFileExtensions}.
    * <li>Tune its display name and icon according to {@link org.openide.filesystems.FileSystem.Status}.
    * </ol>
    * @return the node delegate (without parent) for this data object
    * @see <a href="doc-files/api.html#create-delegate">Datasystems API - Creating a node delegate</a>
    */
    protected Node createNodeDelegate () {
        return new DataNode (this, Children.LEAF);
    }

    /** Obtains lock for primary file.
    *
    * @return the lock
    * @exception IOException if taking the lock fails
    */
    protected FileLock takePrimaryFileLock () throws IOException {
        return getPrimaryFile ().lock ();
    }

    /** Package private method to assign template attribute to a file.
    * Used also from FileEntry.
    *
    * @param fo the file
    * @param newTempl is template or not
    * @return true if the value change/false otherwise
    */
    static boolean setTemplate (FileObject fo, boolean newTempl) throws IOException {
        boolean oldTempl = false;

        Object o = fo.getAttribute(DataObject.PROP_TEMPLATE);
        if ((o instanceof Boolean) && ((Boolean)o).booleanValue())
            oldTempl = true;
        if (oldTempl == newTempl)
            return false;

        fo.setAttribute(DataObject.PROP_TEMPLATE, (newTempl ? Boolean.TRUE : null));

        return true;
    }

    /** Set the template status of this data object.
    * @param newTempl <code>true</code> if the object should be a template
    * @exception IOException if setting the template state fails
    */
    public final void setTemplate (boolean newTempl) throws IOException {
        if (!setTemplate (getPrimaryFile(), newTempl)) {
            // no change in state
            return;
        }

        firePropertyChange(DataObject.PROP_TEMPLATE,
                           !newTempl ? Boolean.TRUE : Boolean.FALSE,
                           newTempl ? Boolean.TRUE : Boolean.FALSE);
    }

    /** Get the template status of this data object.
    * @return <code>true</code> if it is a template
    */
    public final boolean isTemplate () {
        Object o = getPrimaryFile().getAttribute(PROP_TEMPLATE);
        boolean ret = false;
        if (o instanceof Boolean)
            ret = ((Boolean) o).booleanValue();
        return ret;
    }


    /** Test whether the object may be deleted.
    * @return <code>true</code> if it may
    */
    public abstract boolean isDeleteAllowed ();

    /** Test whether the object may be copied.
    * @return <code>true</code> if it may
    */
    public abstract boolean isCopyAllowed ();

    /** Test whether the object may be moved.
    * @return <code>true</code> if it may
    */
    public abstract boolean isMoveAllowed ();

    /** Test whether the object may create shadows.
     * <p>The default implementation returns <code>true</code>.
    * @return <code>true</code> if it may
    */
    public boolean isShadowAllowed () {
        return true;
    }

    /** Test whether the object may be renamed.
    * @return <code>true</code> if it may
    */
    public abstract boolean isRenameAllowed ();


    /** Test whether the object is modified.
    * @return <code>true</code> if it is modified
    */
    public boolean isModified() {
        synchronized (LOCK) {
            return modif;
        }
    }

    /** Set whether the object is considered modified.
     * Also fires a change event.
    * If the new value is <code>true</code>, the data object is added into a {@link #getRegistry registry} of opened data objects.
    * If the new value is <code>false</code>,
    * the data object is removed from the registry.
    */
    public void setModified(boolean modif) {
        boolean log = OBJ_LOG.isLoggable(Level.FINE);
        synchronized (LOCK) {
            if (log) {
                String msg = "setModified(): modif=" + modif + ", original-modif=" + this.modif; // NOI18N
                if (OBJ_LOG.isLoggable(Level.FINEST)) {
                    OBJ_LOG.log(Level.FINEST, msg, new Exception());
                } else {
                    OBJ_LOG.log(Level.FINE, msg);
                }
            }
            if (this.modif == modif) {
                return;
            }
            this.modif = modif;
        }
        Savable present = getLookup().lookup(AbstractSavable.class);
        if (log) {
            OBJ_LOG.log(Level.FINE, "setModified(): present={0}", new Object[]{present}); // NOI18N
        }
        if (modif) {
            syncModified.add (this);
            if (present == null) {
                new DOSavable(this).add();
            }
        } else {
            syncModified.remove (this);
            if (present == null) {
                new DOSavable(this).remove();
            } 
            Unmodify un = getLookup().lookup(Unmodify.class);
            if (un != null) {
                un.unmodify();
            }
        }
        firePropertyChange(DataObject.PROP_MODIFIED,
                           !modif ? Boolean.TRUE : Boolean.FALSE,
                           modif ? Boolean.TRUE : Boolean.FALSE);
    }

    /** Get help context for this object.
    * @return the help context
    */
    public abstract HelpCtx getHelpCtx ();

    /** Get the primary file for this data object.
     * For example,
    * Java source uses <code>*.java</code> and <code>*.class</code> files but the primary one is
    * always <code>*.java</code>. Please note that two data objects are {@link #equals equivalent} if
    * they use the same primary file.
    * <p><em>Warning:</em> do not call {@link Node#getHandle} or {@link DefaultHandle#createHandle} in this method.
    *
    * @return the primary file
    */
    public final FileObject getPrimaryFile () {
        return item().primaryFile;
    }

    /** Finds the data object for a specified file object.
    * @param fo file object
    * @return the data object for that file
    * @exception DataObjectNotFoundException if the file does not have a
    *   data object
    */
    @NbBundle.Messages({
        "# {0} - the path",
        "EXC_FIND_4_INVALID=The file {0} seems no longer valid!"
    })
    public static DataObject find (FileObject fo)
    throws DataObjectNotFoundException {
        if (fo == null)
            throw new IllegalArgumentException("Called DataObject.find on null"); // NOI18N
        
        try {
            if (!fo.isValid()) {
                FileStateInvalidException ex = new FileStateInvalidException(fo.toString());
                Exceptions.attachLocalizedMessage(ex, Bundle.EXC_FIND_4_INVALID(fo.getPath()));
                throw ex;
            }
            
            // try to scan directly the pool (holds only primary files)
            DataObject obj = DataObjectPool.getPOOL().find (fo);
            if (obj != null) {
                return obj;
            }

            // try to use the loaders machinery
            DataLoaderPool p = DataLoaderPool.getDefault();
            assert p != null : "No DataLoaderPool found in " + Lookup.getDefault();
            obj = p.findDataObject (fo);
            if (obj != null) {
                return obj;
            }
                
            throw new DataObjectNotFoundException (fo);
        } catch (DataObjectExistsException ex) {
            return ex.getDataObject ();
        } catch (IOException ex) {
            throw (DataObjectNotFoundException) new DataObjectNotFoundException(fo).initCause(ex);
        }
    }

    /** the only instance */
    private static Registry REGISTRY_INSTANCE = new Registry();
    
    /** Get the registry containing all modified objects.
    *
    * @return the registry
    */
    public static Registry getRegistry () {
        return REGISTRY_INSTANCE;
    }

    /** Get the name of the data object.
    * <p>The default implementation uses the name of the primary file.
    * @return the name
    */
    public String getName () {
        return getPrimaryFile ().getName ();
    }

    @Override
    public String toString () {
        return super.toString () + '[' + getPrimaryFile () + ']';
    }

    /** Get the folder this data object is stored in.
    * @return the folder; <CODE>null</CODE> if the primary file
    *   is the {@link FileObject#isRoot root} of its filesystem
    */
    public final DataFolder getFolder () {
        FileObject fo = getPrimaryFile ().getParent ();
        // could throw IllegalArgumentException but only if fo is not folder
        // => then there is a bug in filesystem implementation
        return fo == null ? null : DataFolder.findFolder (fo);
    }

    /** Copy this object to a folder. The copy of the object is required to
    * be deletable and movable.
    * <p>An event is fired, and atomicity is implemented.
    * @param f the folder to copy the object to
    * @exception IOException if something went wrong
    * @return the new object
    */
    @NbBundle.Messages({
        "# {0} - File name",
        "LBL_Copying=Copying {0}"
    })
    public final DataObject copy (final DataFolder f) throws IOException {
        ProgressInfo pi = getProgressInfo();
        if (pi == null) {
            pi = initProgressInfo(Bundle.LBL_Copying(this.getName()), this);
        } else if (pi.isTerminated()) {
            return null;
        }
        try {
            pi.updateProgress(this);
            final DataObject[] result = new DataObject[1];
            invokeAtomicAction(f.getPrimaryFile(), new FileSystem.AtomicAction() {
                                public void run () throws IOException {
                                    result[0] = handleCopy (f);
                                }
                            }, null);
            fireOperationEvent(
                    new OperationEvent.Copy(result[0], this), OperationEvent.COPY);
            return result[0];
        } finally {
            finishProgressInfoIfDone(pi, this);
        }
    }

    /** Copy this object to a folder (implemented by subclasses).
    * @param f target folder
    * @return the new data object
    * @exception IOException if an error occures
    */
    protected abstract DataObject handleCopy (DataFolder f) throws IOException;

    /** Copy this object to a folder under a different name and file extension.
     * The copy of the object is required to be deletable and movable.
     * <p>An event is fired, and atomicity is implemented.
     * @param f the folder to copy the object to
     * @exception IOException if something went wrong
     * @return the new object
     * @since 6.3
     */
    final DataObject copyRename (final DataFolder f, final String name, final String ext) throws IOException {
        final DataObject[] result = new DataObject[1];
        invokeAtomicAction (f.getPrimaryFile (), new FileSystem.AtomicAction () {
                                public void run () throws IOException {
                                    result[0] = handleCopyRename (f, name, ext);
                                }
                            }, null);
        fireOperationEvent (
            new OperationEvent(result[0]), OperationEvent.CREATE
        );
        return result[0];
    }
    /** 
     * Copy and rename this object to a folder (implemented by subclasses).
     * @param f target folder
     * @param name new file name
     * @param ext new file extension
     * @return the new data object
     * @exception IOException if an error occures or the file cannot be copied/renamed
     * @since 6.3
     */
    protected DataObject handleCopyRename (DataFolder f, String name, String ext) throws IOException {
        throw new IOException( "Unsupported operation" ); //NOI18N
    }

    /** Delete this object.
     * <p>Events are fired and atomicity is implemented.
    * @exception IOException if an error occures
    */
    @NbBundle.Messages({
        "# {0} - Deleted file or folder",
        "LBL_Deleting=Deleting {0}"})
    public final void delete () throws IOException {
        ProgressInfo pi = getProgressInfo();
        if (pi == null) {
            pi = initProgressInfo(Bundle.LBL_Deleting(this.getName()), this);
        } else if (pi.isTerminated()) {
            return;
        }
        try {
            pi.updateProgress(this);
            // the object is ready to be closed
            invokeAtomicAction(getPrimaryFile(), new FileSystem.AtomicAction() {
                public void run () throws IOException {
                    handleDelete ();
                    if (isCurrentActionTerminated() && isValid()) {
                        return;
                    }
                    DataObjectPool.getPOOL().countRegistration(item().primaryFile);
                    item().deregister(false);
                    item().setDataObject(null);
                }
            }, synchObject());
            if (pi.isTerminated() && isValid()) {
                return;
            }
            firePropertyChange(PROP_VALID, Boolean.TRUE, Boolean.FALSE);
            fireOperationEvent(new OperationEvent(this), OperationEvent.DELETE);
        } finally {
            finishProgressInfoIfDone(pi, this);
        }
    }

    /** Delete this object (implemented by subclasses).
    * @exception IOException if an error occures
    */
    protected abstract void handleDelete () throws IOException;


    /** Rename this object.
     * <p>Events are fired and atomicity is implemented.
    *
    * @param name the new name
    *
    * @exception IOException if an error occurs
    */
    public final void rename (String name) throws IOException {
        if (name == null || name.trim ().length ()==0) {
            IllegalArgumentException iae = new IllegalArgumentException (this.getName ());
            String msg = NbBundle.getMessage (DataObject.class,
                                  "MSG_NotValidName", getName ()); // NOI18N
            Exceptions.attachLocalizedMessage(iae, msg);
            throw iae;
        }
        
        
        class Op implements FileSystem.AtomicAction {
            FileObject oldPf;
            FileObject newPf;
            
            String oldName;
            String newName;
            public void run() throws IOException {
                oldName = getName ();

                if (oldName.equals (newName)) return; // the new name is the same as the old one

                oldPf = getPrimaryFile ();
                newPf = handleRename (newName);
                if (oldPf != newPf) {
                    changeItem(item().changePrimaryFile(newPf));
                }
                newName = getName ();
            }
        }
        
        // executes atomic action with renaming
        Op op = new Op();
        op.newName = name;
        FileObject target = getPrimaryFile().getParent();
        if (target == null) {
            target = getPrimaryFile();
        }
        invokeAtomicAction (target, op, synchObject());

        if (op.oldName.equals (op.newName)) {
            return; // the new name is the same as the old one
        }
        
        if (op.oldPf != op.newPf) {
            firePropertyChange (PROP_PRIMARY_FILE, op.oldPf, op.newPf);
        }
        firePropertyChange (PROP_NAME, op.oldName, op.newName);
        firePropertyChange (PROP_FILES, null, null);
        
        fireOperationEvent (new OperationEvent.Rename (this, op.oldName), OperationEvent.RENAME);
    }

    /** Rename this object (implemented in subclasses).
    *
    * @param name name to rename the object to
    * @return new primary file of the object
    * @exception IOException if an error occures
    */
    protected abstract FileObject handleRename (String name) throws IOException;

    /** Move this object to another folder.
     * <p>An event is fired and atomicity is implemented.
    * @param df folder to move object to
    * @exception IOException if an error occurs
    */
    @NbBundle.Messages({
        "# {0} - File name",
        "LBL_Moving=Moving {0}"})
    public final void move (final DataFolder df) throws IOException {
        class Op implements FileSystem.AtomicAction {
            FileObject old;
            public void run () throws IOException {
                if ((getFolder () == null)) return; // cannot move filesystem root
                if (df.equals (getFolder ())) return; // if the destination folder is the same as the current one ==>> do nothing

                // executes atomic action for moving
                old = getPrimaryFile ();
                FileObject mf = handleMove (df);
                changeItem(item().changePrimaryFile (mf));
            }
        }
        Op op = new Op();
        ProgressInfo pi = getProgressInfo();
        if (pi == null) {
            pi = initProgressInfo(Bundle.LBL_Moving(this.getName()), this);
        } else if (pi.isTerminated()) {
            return;
        }
        try {
            pi.updateProgress(this);
            invokeAtomicAction(df.getPrimaryFile(), op, synchObject());

            firePropertyChange(PROP_PRIMARY_FILE, op.old, getPrimaryFile());
            fireOperationEvent(
                    new OperationEvent.Move(this, op.old), OperationEvent.MOVE);
        } finally {
            finishProgressInfoIfDone(pi, this);
        }
    }

    /** Move this object to another folder (implemented in subclasses).
    *
    * @param df target data folder
    * @return new primary file of the object
    * @exception IOException if an error occures
    */
    protected abstract FileObject handleMove (DataFolder df) throws IOException;

    /** Creates shadow for this object in specified folder (overridable in subclasses).
     * <p>The default
    * implementation creates a reference data shadow and pastes it into
    * the specified folder.
    *
    * @param f the folder to create a shortcut in
    * @return the shadow
    */
    protected DataShadow handleCreateShadow (DataFolder f) throws IOException {
        return DataShadow.create (f, this);
    }

    /** Creates shadow for this object in specified folder.
     * <p>An event is fired and atomicity is implemented.
    *
    * @param f the folder to create shortcut in
    * @return the shadow
    */
    public final DataShadow createShadow (final DataFolder f) throws IOException {
        final DataShadow[] result = new DataShadow[1];

        invokeAtomicAction (f.getPrimaryFile (), new FileSystem.AtomicAction () {
                                public void run () throws IOException {
                                    result[0] =  handleCreateShadow (f);
                                }
                            }, null);
        fireOperationEvent (
            new OperationEvent.Copy (result[0], this), OperationEvent.SHADOW
        );
        return result[0];
    }

    /** Create a new object from template (with a name depending on the template).
    *
    * @param f folder to create object in
    * @return new data object based on this one
    * @exception IOException if an error occured
    * @see #createFromTemplate(DataFolder,String)
    */
    public final DataObject createFromTemplate (DataFolder f)
    throws IOException {
        return createFromTemplate (f, null);
    }

    /** Create a new object from template.
    * Asks {@link #handleCreateFromTemplate}.
    *
    * @param f folder to create object in
    * @param name name of object that should be created, or <CODE>null</CODE> if the
    *    name should be same as that of the template (or otherwise mechanically generated)
    * @return the new data object
    * @exception IOException if an error occured
    */
    public final DataObject createFromTemplate (
        final DataFolder f, final String name
    ) throws IOException {
        return createFromTemplate(f, name, Collections.<String,Object>emptyMap());
    }
    
    /** More generic way how to instantiate a {@link DataObject}. One can
    * not only specify its name, but also pass a map of parameters that
    * can influence the copying of the stream.
    *
    * @param f folder to create object in
    * @param name name of object that should be created, or <CODE>null</CODE> if the
    *    name should be same as that of the template (or otherwise mechanically generated)
    * @param parameters map of named objects that are going to be used when
    *    creating the new object
    * @return the new data object
    * @exception IOException if an error occured
    * @since 6.1
    */
    public final DataObject createFromTemplate(
        final DataFolder f, final String name, final Map<String,? extends Object> parameters
    ) throws IOException {
        CreateAction create = new CreateAction(this, f, name, parameters);
        invokeAtomicAction (f.getPrimaryFile (), create, null);
        fireOperationEvent (
            new OperationEvent.Copy (create.result, this), OperationEvent.TEMPL
        );
        return create.result;
    }

    /** Create a new data object from template (implemented in subclasses).
     * This method should
    * copy the content of the template to the destination folder and assign a new name
    * to the new object.
    *
    * @param df data folder to create object in
    * @param name name to give to the new object (or <CODE>null</CODE>
    *    if the name should be chosen according to the template)
    * @return the new data object
    * @exception IOException if an error occured
    */
    protected abstract DataObject handleCreateFromTemplate (
        DataFolder df, String name
    ) throws IOException;


    /** Fires operation event to data loader pool.
    * @param ev the event
    * @param type OperationEvent.XXXX constant
    */
    private static void fireOperationEvent (OperationEvent ev, int type) {
        DataLoaderPool.getDefault().fireOperationEvent (ev, type);
    }

    /** Provide object used for synchronization. 
     * @return <CODE>this</CODE> in DataObject implementation. Other DataObjects
     *     (MultiDataObject) can rewrite this method and return own synch object.
     */
    Object synchObject() {
        return synchObject;
    }
    
    /** Invokes atomic action. 
     */
    private void invokeAtomicAction (FileObject target, final FileSystem.AtomicAction action, final Object lockTheSession) throws IOException {
        FileSystem.AtomicAction toRun;
        
        if (lockTheSession != null) {
            class WrapRun implements FileSystem.AtomicAction {
                public void run() throws IOException {
                    synchronized (lockTheSession) {
                        action.run();
                    }
                }
            }
            toRun = new WrapRun();
        } else {
            toRun = action;
        }
        
        if (Boolean.getBoolean ("netbeans.dataobject.insecure.operation")) {
            DataObjectPool.getPOOL ().runAtomicActionSimple (target, toRun);
            return;
        }
            
        
        if (this instanceof DataFolder) {
            // action is slow
            DataObjectPool.getPOOL ().runAtomicActionSimple (target, toRun);
        } else {
            // it is quick, make it block DataObject recognition
            DataObjectPool.getPOOL ().runAtomicAction (target, toRun);
        }
    }
     
    
    //
    // Property change support
    //

    /** Add a property change listener.
     * @param l the listener to add
    */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        PropertyChangeSupport sup = changeSupport;
        if (sup == null) {
            sup = new PropertyChangeSupport(this);
            if (!changeSupportUpdater.compareAndSet(this, null, sup)) {
                sup = changeSupport;
            }
        }
        assert sup != null;
        sup.addPropertyChangeListener(l);
    }

    /** Remove a property change listener.
     * @param l the listener to remove
    */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        final PropertyChangeSupport sup = changeSupport;
        if (sup != null) {
            sup.removePropertyChangeListener(l);
        }
    }

    /** Fires property change notification to all listeners registered via
    * {@link #addPropertyChangeListener}.
    *
    * @param name of property
    * @param oldValue old value
    * @param newValue new value
    */
    protected final void firePropertyChange (String name, Object oldValue, Object newValue) {
        PropertyChangeSupport ch = changeSupport;
        if (ch != null) {
            ch.firePropertyChange(name, oldValue, newValue);
        }
    }

    //
    // Property change support
    //

    /** Add a listener to vetoable changes.
     * @param l the listener to add
     * @see #PROP_VALID
    */
    public void addVetoableChangeListener (VetoableChangeListener l) {
        synchronized (LOCK) {
            if (vetoableChangeSupport == null) {
                vetoableChangeSupport = new VetoableChangeSupport(this);
            }
            vetoableChangeSupport.addVetoableChangeListener(l);
        }
    }

    /** Add a listener to vetoable changes.
     * @param l the listener to remove
     * @see #PROP_VALID
    */
    public void removeVetoableChangeListener (VetoableChangeListener l) {
        synchronized (LOCK) {
            if (vetoableChangeSupport != null) {
                vetoableChangeSupport.removeVetoableChangeListener(l);
            }
        }
    }

    /** Fires vetoable change notification.
    *
    * @param name of property
    * @param oldValue old value
    * @param newValue new value
    * @exception PropertyVetoException if the change has been vetoed
    */
    protected final void fireVetoableChange (String name, Object oldValue, Object newValue)
        throws PropertyVetoException
    {
        VetoableChangeSupport ch;
        synchronized (LOCK) {
            ch = vetoableChangeSupport;
            if (ch == null) {
                return;
            }
        }
        ch.fireVetoableChange(name, oldValue, newValue);
    }

    //
    // Cookie
    //

    /** Obtain a cookie from the data object.
    * May be overridden by subclasses to extend the behaviour of
    * data objects.
    * <P>
    * The default implementation tests if this object is of the requested class and
    * if so, returns it.
    * <p>
    * <b>Warning:</b> the {@link #getCookie} method and {@link #getLookup}
    * method are ment to be interchangable - e.g. if you override one of them
    * be sure to override also the other and try as much as possible to 
    * keep the same content in each of them. The default implementation tries
    * to do that as much as possible.
    *
    * @param c class of requested cookie
    * @return a cookie or <code>null</code> if such cookies are not supported
    */
    public <T extends Node.Cookie> T getCookie(Class<T> c) {
        if (c.isInstance (this)) {
            return c.cast(this);
        }
        return null;
    }
    
    /** Represents a context of the data object. This method is a more 
     * general replacement for {@link #getCookie} and should preferably
     * be used instead of the old method. The default implementation 
     * inside a data object 
     * returns the <code>getNodeDelegate().getLookup()</code> - which is
     * the most compatible behaviour with previous versions. However
     * this code has significant potential to deadlocks. That is why the
     * preferred advice is to override the method to:
     * <pre>
     * class MyDataObject extends {@link MultiDataObject} {
     *   public &#64;Override Lookup getLookup() {
     *     return getCookieSet().getLookup();
     *   }
     * }
     * </pre>
     * <p>
     * <b>Warning:</b> the {@link #getCookie} method and {@link #getLookup}
     * method are ment to be interchangable - e.g. if you override one of them
     * be sure to override also the other and try as much as possible to 
     * keep the same content in each of them. The default implementation tries
     * to do that as much as possible.
     * 
     * @return lookup representing this data object and its content
     * @since 6.0
     */
    public Lookup getLookup() {
        Class<?> c = getClass();
        if (warnedClasses.add(c)) {
            LOG.warning("Should override getLookup() in " + c + ", e.g.: [MultiDataObject.this.]getCookieSet().getLookup()");
        }
        if (isValid()) {
            return getNodeDelegateImpl().getLookup();
        } else {
            // Fallback for invalid DO; at least provide something reasonable.
            return createNodeDelegate().getLookup();
        }
    }
    private static final Set<Class<?>> warnedClasses = Collections.synchronizedSet(new WeakSet<Class<?>>());
    
    /** When a request for a cookie is done on a DataShadow of this DataObject
     * this methods gets called (by default) so the DataObject knows which
     * DataShadow is asking and extract some information from the shadow itself.
     * <P>
     * Subclasses can override this method with better logic, but the default
     * implementation just delegates to <code>getCookie (Class)</code>.
     *
     * @param clazz class to search for
     * @param shadow the shadow for which is asking
     * @return the cookie or <code>null</code>
     *
     * @since 1.16
     */
    protected <T extends Node.Cookie> T getCookie(DataShadow shadow, Class<T> clazz) {
        return getCookie (clazz);
    }

    // =======================
    //  Serialization methods
    //

    /** The Serialization replacement for this object stores the primary file instead.
     * @return a replacement
    */
    public Object writeReplace () {
        return new Replace (this);
    }

    /** The default replace for the data object
    */
    private static final class Replace extends Object implements Serializable {
        /** the primary file */
        private FileObject fo;
        /** the object to return */
        private transient DataObject obj;

        private static final long serialVersionUID =-627843044348243058L;
        /** Constructor.
        * @param obj the object to use
        */
        public Replace (DataObject obj) {
            this.obj = obj;
            this.fo = obj.getPrimaryFile ();
        }

        public Object readResolve () {
            return obj;
        }

        /** Read method */
        private void readObject (ObjectInputStream ois)
        throws IOException, ClassNotFoundException {
            ois.defaultReadObject ();
            if (fo == null) {
                throw new java.io.FileNotFoundException ();
            }
            // DataObjectNotFoundException extends IOException:
            obj = DataObject.find(fo);
        }
    }

    /** Getter for a text from resource bundle.
    */
    static String getString (String name) {
        return NbBundle.getMessage (DataObject.class, name);
    }
    
    /** Factory interface for converting file object to data objects. Read
     * more about the layer based registrations in 
     * <a href="@TOP@/org/openide/loaders/doc-files/api.html#register"/>separate document</a>.
     * @since 7.0
     */
    public static interface Factory {
        /** Find a data object appropriate to the given file object--the meat of this class.
        * The loader can add all files it has recognized into the <CODE>recognized</CODE>
        * buffer. Then all these files will be excluded from further processing.
        *
        * @param fo file object to recognize
        * @param recognized recognized file buffer
        * @exception DataObjectExistsException if the data object for the
        *    primary file already exists
        * @exception IOException if the object is recognized but cannot be created
        * @exception InvalidClassException if the class is not instance of
        *    {@link #getRepresentationClass}
        *
        * @return suitable data object or <CODE>null</CODE> if the handler cannot
        *   recognize this object (or its group)
        * @see DataLoader
        */
        public DataObject findDataObject(FileObject fo, Set<? super FileObject> recognized)
        throws IOException;
    }
    
    /** Interface for objects that can contain other data objects.
     * For example DataFolder and DataShadow implement this interface
     * to allow others to access the contained objects in uniform maner
     */
    public static interface Container extends Node.Cookie {
        /** Name of property that holds children of this container. */
        public static final String PROP_CHILDREN = "children"; // NOI18N
        
        /** @return the array of contained objects
         */
        public DataObject[] getChildren ();
        
        /** Adds a listener.
         * @param l the listener
         */
        public void addPropertyChangeListener (PropertyChangeListener l);
        
        /** Removes property change listener.
         * @param l the listener
         */
        public void removePropertyChangeListener (PropertyChangeListener l);
    }

    /** Registers new file type into the system.
     * Apply this annotation to a class that extends either 
     * (@link org.openide.loaders.DataObject.Factory) or 
     *(@link org.openide.loaders.DataObject). This methods generates
     * a layer registration as described by {@link DataLoaderPool#factory(java.lang.Class, java.lang.String, java.awt.Image)}.
     * 
     * @see Registrations
     * @since 7.36
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE})
    public static @interface Registration {
        /**
         * Mime type to recognize. Use
         * {@link MIMEResolver.ExtensionRegistration} and co. to assign
         * a mime types to {@link FileObject files} in the system.
         */
        String mimeType();

        /**
         * Display name for the file type created by this registration.
         */
        String displayName() default "";

        /**
         * Path to icon to be used by default for nodes created by
         * this registration. 
         */
        String iconBase() default "";

        /**
         * Position of the registration among other {@link DataObject.Factory 
         * factories} registered for the given
         * {@link #mimeType() mime type}.
         */
        int position() default Integer.MAX_VALUE;
    }
    
     /**
     * May be used to allow multiple {@link DataObject.Registration DataObject.Registration} at one place.
     * @since 7.36
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE})
    public static @interface Registrations {
        
        Registration[] value();
    }
    
    
    /** Registry of modified data objects.
     * The registry permits attaching of a change listener
    * to be informed when the count of modified objects changes.
    */
    public static final class Registry extends Object {

        /** Private constructor */
        private Registry () {
        }

        /** Add new listener to changes in the set of modified objects.
        * @param chl listener to add
        */
        public void addChangeListener (final ChangeListener chl) {
            modified.addChangeListener(chl);
        }

        /** Remove a listener to changes in the set of modified objects.
        * @param chl listener to remove
        */
        public void removeChangeListener (final ChangeListener chl) {
            modified.removeChangeListener(chl);
        }

        /** Get a set of modified data objects.
        * @return an unmodifiable set of data objects
        */
        public Set<DataObject> getModifiedSet() {
            synchronized (syncModified) {
                HashSet<DataObject> set = new HashSet<DataObject>(syncModified);
                return set;
            }
        }

        /** Get modified objects.
        * @return array of objects
        */
        public DataObject[] getModified () {
            return getModifiedSet().toArray(new DataObject[0]);
        }
    }

    private static final class ModifiedRegistry extends HashSet<DataObject> {
        static final long serialVersionUID =-2861723614638919680L;
        private static final Logger REGLOG = Logger.getLogger("org.openide.loaders.DataObject.Registry"); // NOI18N
        
        private final ChangeSupport cs = new ChangeSupport(this);

        ModifiedRegistry() {}

        /** Adds new listener.
        * @param chl new listener
        */
        public final void addChangeListener(final ChangeListener chl) {
            cs.addChangeListener(chl);
        }

        /** Removes listener from the listener list.
        * @param chl listener to remove
        */
        public final void removeChangeListener(final ChangeListener chl) {
            cs.removeChangeListener(chl);
        }

        /***** overriding of methods which change content in order to notify
        * listeners about the content change */
        @Override
        public boolean add (DataObject o) {
            boolean result = super.add(o);
            REGLOG.log(Level.FINER, "Data Object {0} modified, change {1}", new Object[] { o, result }); // NOI18N
            if (result) {
                cs.fireChange();
            }
            return result;
        }

        @Override
        public boolean remove (Object o) {
            boolean result = super.remove(o);
            REGLOG.log(Level.FINER, "Data Object {0} unmodified, change {1}", new Object[] { o, result }); // NOI18N
            if (result) {
                cs.fireChange();
            }
            return result;
        }

    }  // end of ModifiedRegistry inner class
    
    private static final class DOSavable extends AbstractSavable 
    implements Icon {
        final DataObject obj;

        public DOSavable(DataObject obj) {
            this.obj = obj;
        }

        @Override
        public String findDisplayName() {
            return obj.getNodeDelegate().getDisplayName();
        }

        @Override
        protected void handleSave() throws IOException {
            SaveCookie sc = obj.getCookie(SaveCookie.class);
            if (sc != null) {
                sc.save();
            }
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof DOSavable) {
                DOSavable dos = (DOSavable)other;
                return obj.equals(dos.obj);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return obj.hashCode();
        }

        final void remove() {
            unregister();
        }

        final void add() {
            register();
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            icon().paintIcon(c, g, x, y);
        }

        @Override
        public int getIconWidth() {
            return icon().getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return icon().getIconHeight();
        }
        
        private Icon icon() {
            return ImageUtilities.image2Icon(obj.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));
        }
    }

    /** A.N. - profiling shows that MultiLoader.checkFiles() is called too often
    * This method is part of the fix - empty for DataObject.
    */
    void recognizedByFolder() {
    }
    
    // This methods are called by DataObjectPool whenever the primary file
    // gets changed. The Pool listens on the whole FS thus reducing
    // the number of individual listeners created/registered.
    void notifyFileRenamed(FileRenameEvent fe) {
        if (fe.getFile ().equals (getPrimaryFile ())) {
            firePropertyChange(PROP_NAME, fe.getName(), getName());
        }
    }

    void notifyFileDeleted(FileEvent fe) {
    }

    void notifyFileChanged(FileEvent fe) {
    }
    
    void notifyFileDataCreated(FileEvent fe) {
    }
    
    void notifyAttributeChanged(FileAttributeEvent fae) {
       if (! EA_ASSIGNED_LOADER.equals(fae.getName())) {
            // We are interested only in assigned loader
            return;
        }
        FileObject f = fae.getFile();
        if (f != null) {
            String attrFromFO = (String)f.getAttribute(EA_ASSIGNED_LOADER);
            if (attrFromFO == null || (! attrFromFO.equals(getLoader().getClass().getName()))) {
                Set<FileObject> single = new HashSet<FileObject>(); // Collections.singleton is r/o, this must be writable
                single.add(f);
                if (!DataObjectPool.getPOOL().revalidate(single).isEmpty()) {
                    LOG.info("It was not possible to invalidate data object: " + this); // NOI18N
                } else {
                    // we need to refresh parent folder if it is there 
                    // this should be covered by DataLoaderPoolTest.testChangeIsAlsoReflectedInNodes
                    FolderList.changedDataSystem (f.getParent());
                }
            }
        }
    }
    static final class CreateAction implements FileSystem.AtomicAction {
        public DataObject result;
        private String name;
        private DataFolder f;
        private DataObject orig;
        private Map<String, ? extends Object> param;
        
        private static ThreadLocal<CreateAction> CURRENT = new ThreadLocal<CreateAction>();
        
        public CreateAction(DataObject orig, DataFolder f, String name, Map<String, ? extends Object> param) {
            this.orig = orig;
            this.f = f;
            this.name = name;
            this.param = param;
        }
        
        public void run () throws IOException {
            DataFolder prevFold = DataObjectEncodingQueryImplementation.enterIgnoreTargetFolder(f);
            CreateAction prev = CURRENT.get();
            try {
                CURRENT.set(this);
                result = orig.handleCreateFromTemplate(f, name);
            } finally {
                DataObjectEncodingQueryImplementation.exitIgnoreTargetFolder(prevFold);
                CURRENT.set(prev);
            }
        }
        
        public static Map<String, Object> getCallParameters(String name) {
            CreateAction c  = CURRENT.get();
            if (c == null || c.param == null) {
                return Collections.emptyMap();
            }
            return Collections.unmodifiableMap(c.param);
        }
        
        static String getOrigName() {
            CreateAction c  = CURRENT.get();
            return c == null ? null : c.name;
        }
        
        public static Map<String,Object> findParameters(String name) {
            CreateAction c  = CURRENT.get();
            if (c == null) {
                return Collections.emptyMap();
            }
            HashMap<String,Object> all = new HashMap<String,Object>();
            for (CreateFromTemplateAttributesProvider provider : Lookup.getDefault().lookupAll(CreateFromTemplateAttributesProvider.class)) {
                Map<String,? extends Object> map = provider.attributesFor(c.orig, c.f, c.name);
                if (map != null) {
                    for (Map.Entry<String,? extends Object> e : map.entrySet()) {
                        all.put(e.getKey(), e.getValue());
                    }
                }
            }
            if (c.param != null) {
                for (Map.Entry<String,? extends Object> e : c.param.entrySet()) {
                    all.put(e.getKey(), e.getValue());
                }
            }

            if (!all.containsKey("name") && name != null) { // NOI18N
                if (Boolean.TRUE.equals(all.get(CreateFromTemplateHandler.FREE_FILE_EXTENSION))) {
                    name = name.replaceFirst("[.].*", "");
                }
                all.put("name", name); // NOI18N
            }
            if (!all.containsKey("user")) { // NOI18N
                all.put("user", System.getProperty("user.name")); // NOI18N
            }
            Date d = new Date();
            if (!all.containsKey("date")) { // NOI18N
                all.put("date", DateFormat.getDateInstance().format(d)); // NOI18N
            }
            if (!all.containsKey("time")) { // NOI18N
                all.put("time", DateFormat.getTimeInstance().format(d)); // NOI18N
            }
            if (!all.containsKey("dateTime")) { // NOI18N
                all.put("dateTime", d); // NOI18N
            }
            
            return Collections.unmodifiableMap(all);
        }
        
        public static Map<String,Object> enhanceParameters(Map<String,Object> old, String name, String ext) {
            HashMap<String,Object> all = new HashMap<String,Object>(old);
            if (!all.containsKey("nameAndExt") && name != null) { // NOI18N
                if (ext != null && ext.length() > 0 &&
                        (!Boolean.TRUE.equals(old.get(CreateFromTemplateHandler.FREE_FILE_EXTENSION)) || name.indexOf('.') == -1)) {
                    all.put("nameAndExt", name + '.' + ext); // NOI18N
                } else {
                    all.put("nameAndExt", name); // NOI18N
                }
            }
            return Collections.unmodifiableMap(all);
        }
        
    } // end of CreateAction

    /**
     * Get existing thread-local ProgressInfo instance.
     *
     * @return The thread-local instance, or null if not yet initialized.
     */
    static ProgressInfo getProgressInfo() {
        return PROGRESS_INFO_TL.get();
    }

    /**
     * Initialize a thread-local ProgressInfo instance. The instance mustn't be
     * already initialized.
     *
     * @return The new ProgressInfo instance.
     */
    static ProgressInfo initProgressInfo(String name, DataObject root) {
        assert PROGRESS_INFO_TL.get() == null;
        ProgressInfo pi = new ProgressInfo(name, root);
        PROGRESS_INFO_TL.set(pi);
        OBJ_LOG.log(Level.FINEST, "ProgressInfo init: {0}", name);      //NOI18N
        return pi;
    }

    /**
     * Finish the progress bar and remove the thread-local ProgressInfo
     * instance, but only if the root object of the operation has just been
     * processed.
     */
    static void finishProgressInfoIfDone(ProgressInfo pi,
            DataObject dob) {
        assert PROGRESS_INFO_TL.get() == null || PROGRESS_INFO_TL.get() == pi;
        if (pi.finishIfDone(dob)) {
            PROGRESS_INFO_TL.remove();
        }
    }

    /**
     * Check whether the current delete, move or copy action has been terminated
     * by the user.
     *
     * @return True if the action has been terminatad, false otherwise.
     */
    static boolean isCurrentActionTerminated() {
        ProgressInfo pi = getProgressInfo();
        return pi != null && pi.isTerminated();
    }

    /**
     * Object holding information about a move, delete or copy operation. It
     * should be stored in a thread-local variable, see methods
     * {@link #getProgressInfo()}, {@link #initProgressInfo(DataObject)
     * and {@link #finishProgressInfoIfDone(ProgressInfo, DataObject)}}.
     */
    static class ProgressInfo {

        private final int NAME_LEN_LIMIT = 128;

        private final ProgressHandle progressHandle;
        private final AtomicBoolean terminated = new AtomicBoolean();
        private final DataObject root;

        public ProgressInfo(String name, DataObject root) {
            final Cancellable can;
            if (root instanceof DataFolder) {
                can = new Cancellable() {

                    @Override
                    public boolean cancel() {
                        terminated.set(true);
                        return true;
                    }
                };
            } else {
                can = null;
            }
            ProgressHandle ph = ProgressHandleFactory.createHandle(name, can);
            ph.setInitialDelay(500);
            ph.start();
            this.progressHandle = ph;
            this.root = root;
        }

        public void updateProgress(DataObject dob) {
            OBJ_LOG.log(Level.FINEST, "Update ProgressInfo: {0}", dob); //NOI18N
            String displayName;
            if (dob.getPrimaryFile() == null) {
                displayName = dob.getName();
            } else {
                displayName = dob.getPrimaryFile().getPath();
            }
            if (displayName != null && displayName.length() > NAME_LEN_LIMIT) {
                displayName = "..." + displayName.substring( //NOI18N
                        displayName.length() - NAME_LEN_LIMIT + 3,
                        displayName.length());
            }
            progressHandle.progress(displayName);
        }

        /**
         * Terminate the current action.
         */
        public void terminate() {
            terminated.set(true);
        }

        public boolean isTerminated() {
            return terminated.get();
        }

        /**
         * If the passed data object is the root object for the operation,
         * finish the progress bar and return true, otherwise do nothing and
         * return false.
         */
        public boolean finishIfDone(DataObject currentFile) {
            if (currentFile == root) {
                progressHandle.finish();
                return true;
            } else {
                return false;
            }
        }
    }
}
