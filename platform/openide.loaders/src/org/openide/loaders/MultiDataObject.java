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

package org.openide.loaders;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.io.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.*;
import javax.swing.event.*;
import org.netbeans.api.actions.Editable;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.templates.FileBuilder;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.text.CloneableEditor;
import org.openide.util.*;

/** Provides support for handling of data objects with multiple files.
* One file is represented by one {@link Entry}. Each handler
* has one {@link #getPrimaryEntry primary} entry and zero or more secondary entries.
*
* @author Ales Novak, Jaroslav Tulach, Ian Formanek
*/
public class MultiDataObject extends DataObject {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -7750146802134210308L;

    /** Synchronization object used in getCookieSet and setCookieSet methods.
     */
    private static final Object cookieSetLock = new Object();
    
    /** Lock used for lazy creation of secondary field (in method getSecondary()) */
    private static final Object secondaryCreationLock = new Object();
    
    /** A RequestProceccor used for firing property changes asynchronously */
    private static final RequestProcessor firingProcessor =
		new RequestProcessor( "MDO PropertyChange processor");
    /** map of changes to be delivered later */
    private Map<String,PropertyChangeEvent> later;
    
    /** A RequestProceccor used for waiting for finishing refresh */
    private static final RequestProcessor delayProcessor =
		new RequestProcessor( "MDO Firing delayer");
    /** a task waiting for the FolderList task to finish scanning of the folder */
    private RequestProcessor.Task delayedPropFilesTask;
    /** lock used in firePropFilesAfterFinishing */
    private static final Object delayedPropFilesLock = new Object();
    /** logging of operations in multidataobject */
    static final Logger ERR = Logger.getLogger(MultiDataObject.class.getName());
    
    /** getPrimaryEntry() is intended to have all inetligence for copy/move/... */
    private Entry primary;

    /** Map of secondary entries and its files. (FileObject, Entry) */
    private HashMap<FileObject,Entry> secondary;

    /** array of cookies for this object */
    private CookieSet cookieSet;

    /** flag when to call checkFiles(this) */
    boolean checked = false;

    /** Create a MultiFileObject.
    * @see DataObject#DataObject(org.openide.filesystems.FileObject,org.openide.loaders.DataLoader)
    * @param fo the primary file object
    * @param loader loader of this data object
    */
    public MultiDataObject(FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
        super(fo, loader);        
        primary = createPrimaryEntry (this, getPrimaryFile ());
    }

    /** This constructor is added for backward compatibility, MultiDataObject should be
    * properly constructed using the MultiFileLoader.
    * @param fo the primary file object
    * @param loader loader of this data object
    * @deprecated do not use this constructor, it is for backward compatibility of 
    * {@link #DataShadow} and {@link #DataFolder} only
    * @since 1.13
    */
    @Deprecated
    MultiDataObject(FileObject fo, DataLoader loader) throws DataObjectExistsException {
        super(fo, loader);
        primary = createPrimaryEntry (this, getPrimaryFile ());
    }
    
    /** Getter for the multi file loader that created this
    * object.
    *
    * @return the multi loader for the object
    */
    public final MultiFileLoader getMultiFileLoader () {
        DataLoader loader = getLoader ();
        
        if (!(loader instanceof MultiFileLoader))
            return null;
        
        return (MultiFileLoader)loader;
    }

    @Override
    public Set<FileObject> files () {
        // move lazy initialization to FilesSet
        return new FilesSet (this);
    }

    /* Getter for delete action.
    * @return true if the object can be deleted
    */
    public boolean isDeleteAllowed() {
        return getPrimaryFile().canWrite() && !existReadOnlySecondary();
    }
    
    private boolean existReadOnlySecondary() {
        synchronized ( synchObjectSecondary() ) {
            for (FileObject f : getSecondary().keySet()) {
                if (!f.canWrite()) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Performs checks by calling checkFiles
     * @return getSecondary() method result
     */
    private Map<FileObject,Entry> checkSecondary () {
        // enumeration of all files
        if (! checked) {
            checkFiles (this);
            checked = true;
        }
        return getSecondary();
    }
        
    /** Lazy getter for secondary property
     * @return secondary object
     */
    /* package-private */ Map<FileObject,Entry> getSecondary() {
        HashMap<FileObject,Entry> ret;
        synchronized (secondaryCreationLock) {
            if (secondary == null) {
                secondary = new HashMap<FileObject,Entry>(4);
            }
            ret = secondary;
        }
        if (ERR.isLoggable(Level.FINE)) {
            ERR.fine("getSecondary for " + this + " is " + secondary); // NOI18N
        }
        return ret;
    }
    
    /* Getter for copy action.
    * @return true if the object can be copied
    */
    public boolean isCopyAllowed() {
        return true;
    }

    /* Getter for move action.
    * @return true if the object can be moved
    */
    public boolean isMoveAllowed() {
        return getPrimaryFile().canWrite() && !existReadOnlySecondary();
    }

    /* Getter for rename action.
    * @return true if the object can be renamed
    */
    public boolean isRenameAllowed () {
        return getPrimaryFile().canWrite() && !existReadOnlySecondary();
    }

    /* Help context for this object.
    * @return help context
    */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /** Provide object used for synchronization of methods working with 
     * Secondaries.
     * @return The private field <CODE>secondary</CODE>.
     */
    Object synchObjectSecondary() {
        Object lock = checkSecondary();
        if (lock == null) throw new IllegalStateException("checkSecondary was null from " + this); // NOI18N
        return checkSecondary();
    }
    
    /** Provides node that should represent this data object.
    *
    * @return the node representation
    * @see DataNode
    */
    @Override
    protected Node createNodeDelegate () {
        if (associateLookup() >= 1) {
            return new DataNode(this, Children.LEAF, getLookup());
        }
        DataNode dataNode = (DataNode) super.createNodeDelegate ();
        return dataNode;
    }

    /** Add a new secondary entry to the list.
    * @param fe the entry to add
    */
    protected final void addSecondaryEntry (Entry fe) {
        synchronized ( getSecondary() ) {
            getSecondary().put (fe.getFile (), fe);  
            if (ERR.isLoggable(Level.FINE)) {
                ERR.fine("addSecondaryEntry: " + fe + " for " + this); // NOI18N
            }
        }

        // Fire PROP_FILES only if we have actually finished making the folder.
        // It is dumb to fire this if we do not yet even know what all of our
        // initial secondary files are going to be.
        FolderList l = getFolderList();
        if (l == null) {
            firePropertyChangeLater (PROP_FILES, null, null);
        } else { // l != null
            if (l.isCreated()) {
                firePropertyChangeLater (PROP_FILES, null, null);
            } else {
                firePropFilesAfterFinishing();
            }
        }
    }

    /** Finds FolderList object for the primary file's parent folder
     * @return FolderList object or <code>null</code>
     */
    private FolderList getFolderList() {
        FileObject parent = primary.file.getParent();
        if (parent != null) {
            return FolderList.find(parent, false);
        }
        return null;
    }
    
    /** Remove a secondary entry from the list.
     * @param fe the entry to remove
    */
    protected final void removeSecondaryEntry (Entry fe) {
        synchronized (getSecondary()) {
            getSecondary().remove (fe.getFile ());
            if (ERR.isLoggable(Level.FINE)) {
                ERR.fine("removeSecondaryEntry: " + fe + " for " + this); // NOI18N
            }
        }
        
        firePropertyChangeLater (PROP_FILES, null, null);
        updateFilesInCookieSet();

        if (fe.isImportant ()) {
            checkConsistency(this);
        }
    }

    /** All secondary entries are recognized. Called from multi file object.
    * @param recognized object to mark recognized file to
    */
    final void markSecondaryEntriesRecognized (DataLoader.RecognizedFiles recognized) {
        if (recognized == DataLoaderPool.emptyDataLoaderRecognized) {
            return;
        }

        synchronized (getSecondary()) {
            for (FileObject fo : getSecondary().keySet()) {
                recognized.markRecognized (fo);
            }
        }
    }


    /** Tests whether this file is between entries and if not,
    * creates a secondary entry for it and adds it into set of
    * secondary entries.
    * <P>
    * This method should be used in constructor of MultiDataObject to
    * register all the important files, that could belong to this data object.
    * As example, our XMLDataObject, tries to locate its <CODE>xmlinfo</CODE>
    * file and then do register it
    *
    * @param fo the file to register (can be null, then the action is ignored)
    * @return the entry associated to this file object (returns primary entry if the fo is null)
    */
    protected final Entry registerEntry (FileObject fo) {
        synchronized (getSecondary()) {
            if (fo == null) {
                // is it ok, to do this or somebody would like to see different behavour?
                return primary;
            }
            if (fo.equals (getPrimaryFile ())) {
                return primary;
            }

            Entry e = getSecondary().get(fo);
            if (e != null) {
                return e;
            }

            // add it into set of entries
            e = createSecondaryEntry (this, fo);
            addSecondaryEntry (e);

            return e;
        }
    }

    /** Removes the entry from the set of secondary entries.
     * Called from the notifyFileDeleted
     */
    final void removeFile (FileObject fo) {
        synchronized (getSecondary()) {
            Entry e = getSecondary().get(fo);
            if (e != null) {
                removeSecondaryEntry (e);
            }
        }
    }

    /** Get the primary entry.
    * @return the entry
    */
    public final Entry getPrimaryEntry () {
        return primary;
    }

    /** Get secondary entries.
    * @return immutable set of entries
    */
    public final Set<Entry> secondaryEntries () {
        return secondaryEntries(true);
    }
    final Set<Entry> secondaryEntries (boolean allocate) {
        synchronized ( synchObjectSecondary() ) {
            removeAllInvalid ();

            return allocate ? new HashSet<Entry>(getSecondary().values()) : null;
        }
    }

    /** For a given file, find the associated secondary entry.
    * @param fo file object
    * @return the entry associated with the file object, or <code>null</code> if there is no
    *    such entry
    */
    public final Entry findSecondaryEntry (FileObject fo) {
        Entry e;
        synchronized ( synchObjectSecondary() ) {
            removeAllInvalid ();
            e = getSecondary().get(fo);
        }
        return e;
    }
    
    /** Removes all FileObjects that are not isValid from the
     * set of objects.
     */
    private void removeAllInvalid () {
        ERR.log(Level.FINE, "removeAllInvalid, started {0}", this); // NOI18N
        Iterator<Map.Entry<FileObject, MultiDataObject.Entry>> it = checkSecondary().entrySet().iterator();
        boolean fire = false;
        while (it.hasNext ()) {
            Map.Entry e = it.next();
            FileObject fo = (FileObject)e.getKey ();
            if (fo == null || !fo.isValid ()) {
                it.remove ();
                if (ERR.isLoggable(Level.FINE)) {
                    ERR.log(Level.FINE, "removeAllInvalid, removed: {0} for {1}", new Object[]{fo, this}); // NOI18N
                }
                fire = true;
            }
        }
        ERR.log(Level.FINE, "removeAllInvalid, finished {0}", this); // NOI18N
        if (fire) {
            firePropertyChangeLater (PROP_FILES, null, null);
        }
    }


    //methods overriding DataObjectHandler's abstract methods

    /* Obtains lock for primary file by asking getPrimaryEntry() entry.
    *
    * @return the lock for primary file
    * @exception IOException if it is not possible to set the template
    *   state.
    */
    @Override
    protected FileLock takePrimaryFileLock () throws IOException {
        return getPrimaryEntry ().takeLock ();
    }

    // XXX does nothing of the sort --jglick
    /** Check if in specific folder exists fileobject with the same name.
    * If it exists user is asked for confirmation to rewrite, rename or cancel operation.
    * @param folder destination folder
    * @return the suffix which should be added to the name or null if operation is cancelled
    */
    private String existInFolder(FileObject fo, FileObject folder) {
        // merge folders when neccessary
        if (fo.isFolder () && isMergingFolders (fo, folder))
            return ""; // NOI18N
        
        String orig = fo.getName ();
        String name = FileUtil.findFreeFileName(
                          folder, orig, fo.getExt ()
                      );
        if (name.length () <= orig.length ()) {
            return ""; // NOI18N
        } else {
            return name.substring (orig.length ());
        }
    }

    /** Override to change default handling of name collisions detected during the
     * copy, move operations. Reasonable for MultiDataObjects having folder their
     * primary file (e.g. DataFolder, CompoundDataObject).
     * @return <code>false</code> means, that new folder name should be synthetized when
     * the same folder already exists in the target location of copy, move operation, otherwise
     * existing falder will be used. Default implementation returns <code>false</code>.
     */
    boolean isMergingFolders(FileObject who, FileObject targetFolder) {
        return false;
    }
    
    /** Copies primary and secondary files to new folder.
     * May ask for user confirmation before overwriting.
     * @param df the new folder
     * @return data object for the new primary
     * @throws IOException if there was a problem copying
     * @throws UserCancelException if the user cancelled the copy
    */
    @NbBundle.Messages(
        "EXC_NO_LONGER_VALID=Copied file {0} is no longer valid!"
    )
    @Override
    protected DataObject handleCopy (DataFolder df) throws IOException {
        FileObject fo;

        String suffix = existInFolder(
                            getPrimaryEntry().getFile(),
                            df.getPrimaryFile ()
                        );
        if (suffix == null)
            throw new org.openide.util.UserCancelException();

        boolean template = isTemplate();
        Iterator<Entry> it = secondaryEntries().iterator();
        while (it.hasNext ()) {
            Entry e = it.next();
            fo = e.copy (df.getPrimaryFile (), suffix);
            if (template) {
                FileUtil.copyAttributes(e.getFile(), fo);
                copyTemplateAttributes(e.getFile(), fo);
            }
        }
        //#33244 - copy primary file after the secondary ones
        fo = getPrimaryEntry ().copy (df.getPrimaryFile (), suffix);
        if (fo == null || !fo.isValid()) {
            IOException ex = new IOException("copied file is not valid " + fo); // NOI18N
            Exceptions.attachLocalizedMessage(ex, Bundle.EXC_NO_LONGER_VALID(fo));
            throw ex;
        }

        if (template) {
            FileObject source = getPrimaryEntry().getFile();
            copyUniqueAttribute(source, fo, "displayName");     // NOI18N
            FileUtil.copyAttributes(source, fo);
            copyTemplateAttributes(source, fo);
        }

        boolean fullRescan = getMultiFileLoader() == null ||
            getMultiFileLoader().findPrimaryFile(fo) != fo ||
            getMultiFileLoader() == DataLoaderPool.getDefaultFileLoader();
        try {
            return fullRescan ? DataObject.find(fo) : createMultiObject (fo);
        } catch (DataObjectExistsException ex) {
            return ex.getDataObject ();
        } catch (DataObjectNotFoundException ex) {
            if (!fo.isValid()) {
                Exceptions.attachLocalizedMessage(ex, Bundle.EXC_NO_LONGER_VALID(fo));
            }
            throw ex;
        }
    }

    private static final String[] TEMPLATE_ATTRIBUTES = {
        "iconBase",                  // NOI18N
        "SystemFileSystem.icon",     // NOI18N
        "SystemFileSystem.icon32",   // NOI18N
        "instantiatingIterator",     // NOI18N
        "instantiatingWizardURL",    // NOI18N
    };

    private static void copyAttributes(FileObject source, FileObject dest,
                                       String[] attrNames) throws IOException {
        for (String attr : attrNames) {
            Object value = source.getAttribute(attr);
            if (value != null) {
                dest.setAttribute(attr, value);
            }
        }
    }

    private static void copyTemplateAttributes(FileObject source, FileObject dest) throws IOException {
        copyAttributes(source, dest, TEMPLATE_ATTRIBUTES);
        Enumeration<String> attrs = source.getAttributes();
        while(attrs.hasMoreElements()) {
            String attr = attrs.nextElement();
            if (attr.startsWith("template")) {      // NOI18N
                Object value = source.getAttribute(attr);
                if (value != null) {
                    dest.setAttribute(attr, value);
                }
            }
        }
    }
    
    private static void copyUniqueAttribute(FileObject source, FileObject dest,
                                            String attrName) throws IOException {
        Object value = source.getAttribute(attrName);
        if (value == null) {
            return ;
        }
        FileObject parent = dest.getParent();
        if (parent == null || !(value instanceof String)) {
            dest.setAttribute(attrName, value);
            return ;
        }
        String valueBase = (String) value;
        FileObject[] ch = parent.getChildren();
        int i = 0;
        while(true) {
            boolean isValueInChildren = false;
            for (int j = 0; j < ch.length; j++) {
                Object v = ch[j].getAttribute(attrName);
                if (value.equals(v)) {
                    isValueInChildren = true;
                    break;
                }
            }
            if (!isValueInChildren) {
                dest.setAttribute(attrName, value);
                break;
            }
            value = valueBase + " " + (++i);
        }
    }

    /* Deletes all secondary entries, removes them from the set of
    * secondary entries and then deletes the getPrimaryEntry() entry.
    */
    protected void handleDelete() throws IOException {
        List<FileObject> toRemove = new ArrayList<FileObject>();
        Iterator<Map.Entry<FileObject,Entry>> it;
        synchronized ( synchObjectSecondary() ) {
            removeAllInvalid ();
            it = new ArrayList<Map.Entry<FileObject,Entry>>(getSecondary().entrySet()).iterator();
        }
        
        while (it.hasNext ()) {
            Map.Entry<FileObject,Entry> e = it.next ();
            e.getValue().delete();
            toRemove.add(e.getKey());
        }
        
        synchronized ( synchObjectSecondary() ) {
            for (FileObject f : toRemove) {
                getSecondary().remove(f);
                if (ERR.isLoggable(Level.FINE)) {
                    ERR.fine("  handleDelete, removed entry: " + f);
                }
            }
        }
        
        getPrimaryEntry().delete();
    }

    /* Renames all entries and changes their files to new ones.
    */
    protected FileObject handleRename (String name) throws IOException {
        Map<String, Object> templateAttrs = getTemplateAttrs();
        getPrimaryEntry ().changeFile (getPrimaryEntry().rename (name));
        setTemplateAttrs(templateAttrs);

        Map<FileObject,Entry> add = null;

        List<FileObject> toRemove = new ArrayList<FileObject>();
        
        Iterator<Map.Entry<FileObject,Entry>> it;
        synchronized ( synchObjectSecondary() ) {
            removeAllInvalid ();
            it = new ArrayList<Map.Entry<FileObject,Entry>>(getSecondary().entrySet ()).iterator();
        }
        
        while (it.hasNext ()) {
            Map.Entry<FileObject,Entry> e = it.next();
            FileObject fo = e.getValue().rename(name);
            if (fo == null) {
                // remove the entry
                toRemove.add (e.getKey());
            } else {
                if (!fo.equals (e.getKey ())) {
                    // put the new one into change table
                    if (add == null) add = new HashMap<FileObject,Entry>();
                    Entry entry = e.getValue();
                    entry.changeFile (fo);
                    // using getFile to let the entry correctly annotate
                    // the file by isImportant flag
                    add.put (entry.getFile (), entry);

                    // changed the file => remove the file
                    toRemove.add(e.getKey());
                }
            }
        }

        // if there has been a change in files, apply it
        if ((add != null) || (!toRemove.isEmpty())) {
            synchronized ( synchObjectSecondary() ) {
                // remove entries
                if (!toRemove.isEmpty()) {
                    for (FileObject f : toRemove) {
                        getSecondary().remove(f);
                        if (ERR.isLoggable(Level.FINE)) {
                            ERR.fine("handleRename, removed: " + f + " for " + this); // NOI18N
                        }
                    }
                }
                // add entries
                if (add != null) {
                    getSecondary().putAll (add);
                    if (ERR.isLoggable(Level.FINE)) {
                        ERR.fine("handleRename, putAll: " + add + " for " + this); // NOI18N
                    }
                }
            }
            firePropertyChangeLater (PROP_FILES, null, null);
        }

        return getPrimaryEntry ().getFile ();
    }

    /**
     * Get template attributes that can be set later again, for example after
     * renaming of a template. If this data object is not a template, this
     * method does nothing and returns null.
     *
     * @see #copyTemplateAttributes(FileObject, FileObject)
     * @return Map of template attributes from this data object, or null if this
     * data object does not represent a template.
     */
    private Map<String, Object> getTemplateAttrs() {
        if (isTemplate()) {
            Map<String, Object> map = new HashMap<String, Object>();
            FileObject fo = getPrimaryFile();
            Enumeration<String> attributes = fo.getAttributes();
            while (attributes.hasMoreElements()) {
                String key = attributes.nextElement();
                if (key.startsWith("template")) { //NOI18N
                    Object val = fo.getAttribute(key);
                    if (val != null) {
                        map.put(key, val);
                    }
                }
            }
            for (String key : TEMPLATE_ATTRIBUTES) {
                Object val = fo.getAttribute(key);
                if (val != null) {
                    map.put(key, val);
                }
            }
            return map;
        } else {
            return null;
        }
    }

    /**
     * Set template attributes on this data object. If this data object is not a
     * template, or the {@code attrs} argument is null, this method does
     * nothing.
     *
     * @param attrs Map of template attributes, or null.
     * @see #copyTemplateAttributes(FileObject, FileObject)
     * @see #getTemplateAttrs()
     * @throws IOException
     */
    private void setTemplateAttrs(Map<String, Object> attrs)
            throws IOException {
        if (attrs != null && isTemplate()) {
            FileObject fo = getPrimaryFile();
            for (Map.Entry<String, Object> entry : attrs.entrySet()) {
                if (entry.getValue() != null
                        && fo.getAttribute(entry.getKey()) == null) {
                    fo.setAttribute(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    /** Moves primary and secondary files to a new folder.
     * May ask for user confirmation before overwriting.
     * @param df the new folder
     * @return the moved primary file object
     * @throws IOException if there was a problem moving
     * @throws UserCancelException if the user cancelled the move
    */
    protected FileObject handleMove (DataFolder df) throws IOException {
        String suffix = existInFolder(getPrimaryEntry().getFile(), df.getPrimaryFile ());
        if (suffix == null)
            throw new org.openide.util.UserCancelException();

        List<Pair> backup = saveEntries();

        try {
            HashMap<FileObject,Entry> add = null;

            ArrayList<FileObject> toRemove = new ArrayList<FileObject>();
            Iterator<Map.Entry<FileObject,Entry>> it;
            int count;
            synchronized ( synchObjectSecondary() ) {
                removeAllInvalid ();
                ArrayList<Map.Entry<FileObject,Entry>> list = 
                        new ArrayList<Map.Entry<FileObject,Entry>>(getSecondary().entrySet ());
                count = list.size();
                it = list.iterator();
            }
            
            if (ERR.isLoggable(Level.FINE)) {
                ERR.fine("move " + this + " to " + df + " number of secondary entries: " + count); // NOI18N
                ERR.fine("moving primary entry: " + getPrimaryEntry()); // NOI18N
            }
            final FileObject newPF = getPrimaryEntry ().move (df.getPrimaryFile (), suffix);
            if (newPF == null) {
                throw new NullPointerException("Invalid move on " + getPrimaryEntry() + " of " + MultiDataObject.this + " returned null"); // NOI18N
            }
            getPrimaryEntry ().changeFile (newPF);
            if (ERR.isLoggable(Level.FINE)) ERR.fine("               moved: " + getPrimaryEntry().getFile()); // NOI18N

            
            while (it.hasNext ()) {
                Map.Entry<FileObject,Entry> e = it.next ();
                if (ERR.isLoggable(Level.FINE)) ERR.fine("moving entry :" + e); // NOI18N
                FileObject fo = (e.getValue ()).move (df.getPrimaryFile (), suffix);
                if (ERR.isLoggable(Level.FINE)) ERR.fine("  moved to   :" + fo); // NOI18N
                if (fo == null) {
                    // remove the entry
                    toRemove.add(e.getKey());
                } else {
                    if (!fo.equals (e.getKey ())) {
                        // put the new one into change table
                        if (add == null) add = new HashMap<FileObject,Entry> ();
                        Entry entry = e.getValue ();
                        entry.changeFile (fo);
                        // using entry.getFile, so the file has correctly
                        // associated its isImportant flag
                        add.put (entry.getFile (), entry);

                        // changed the file => remove the file
                        toRemove.add(e.getKey());
                    }
                }
            }

            // if there has been a change in files, apply it
            if ((add != null) || (!toRemove.isEmpty())) {
                synchronized ( synchObjectSecondary() ) {
                    // remove entries
                    if (!toRemove.isEmpty()) {
                        Object[] objects = toRemove.toArray();
                        for (int i = 0; i < objects.length; i++) {
                            getSecondary().remove(objects[i]);
                            if (ERR.isLoggable(Level.FINE)) {
                                ERR.fine("handleMove, remove: " + objects[i] + " for " + this); // NOI18N
                            }
                        }
                    }
                    // add entries
                    if (add != null) {
                        getSecondary().putAll (add);
                        if (ERR.isLoggable(Level.FINE)) {
                            ERR.fine("handleMove, putAll: " + add + " for " + this); // NOI18N
                        }
                    }
                }
                firePropertyChangeLater (PROP_FILES, null, null);
            }

            if (ERR.isLoggable(Level.FINE)) {
                ERR.fine("successfully moved " + this); // NOI18N
            }
            return getPrimaryEntry ().getFile ();
        } catch (IOException e) {
            if (ERR.isLoggable(Level.FINE)) {
                ERR.fine("exception is here, restoring entries " + this); // NOI18N
                ERR.log(Level.FINE, null, e);
            }
            restoreEntries(backup);
            if (ERR.isLoggable(Level.FINE)) {
                ERR.fine("entries restored " + this); // NOI18N
            }
            throw e;
        }
    }

    /* Creates new object from template.
    * @exception IOException
    */
    protected DataObject handleCreateFromTemplate (
        DataFolder df, String name
    ) throws IOException {
        return this.handleCreateFromTemplate(df, name, new int[1]);
    }

    final DataObject handleCreateFromTemplate (
        DataFolder df, String name, int[] fileBuilderUsed
    ) throws IOException {
        assert fileBuilderUsed != null;
        if (name == null) {
            name = FileUtil.findFreeFileName(
                       df.getPrimaryFile (), getPrimaryFile ().getName (), getPrimaryFile ().getExt ()
                   );
        }

        FileObject pf = null;
        Map<String, Object> params = CreateAction.getCallParameters(name);
        // #248975: backwards-compatible hack: initializes Lookup caches, so Lookup finds registrations against
        // the old deprecated service when trying to locate new CFTH instances. This allows to freely order
        // old and new handlers
        Lookup.getDefault().lookupAll(CreateFromTemplateHandler.class);
        pf = FileBuilder.createFromTemplate(getPrimaryFile(), df.getPrimaryFile(), name, params, FileBuilder.Mode.FAIL);
        if (pf == null) {
            // do the regular creation
            pf = getPrimaryEntry().createFromTemplate (df.getPrimaryFile (), name);
        } else {
            fileBuilderUsed[0]++;
        }
        
        
        Iterator<Entry> it = secondaryEntries().iterator();
        NEXT_ENTRY: while (it.hasNext ()) {
            Entry entry = it.next();
            FileObject current = entry.getFile();
            FileObject fo = FileBuilder.createFromTemplate(current, df.getPrimaryFile(), name, params, FileBuilder.Mode.FAIL);
            if (fo == null) {
                entry.createFromTemplate (df.getPrimaryFile (), name);
            } else {
                fileBuilderUsed[0]++;
            }
        }
        
        try {
            // #61600: not very object oriented, but covered by DefaultVersusXMLDataObjectTest
            if (getMultiFileLoader() == DataLoaderPool.getDefaultFileLoader()) {
                return DataObject.find(pf);
            }
            
            return createMultiObject (pf);
        } catch (DataObjectExistsException ex) {
            return ex.getDataObject ();
        }
    }

    @Override
    protected DataObject handleCopyRename(DataFolder df, String name, String ext) throws IOException {
        if (getLoader() instanceof UniFileLoader || getLoader() == DataLoaderPool.getDefaultFileLoader()) {
            //allow the operation for single file DataObjects
            FileObject fo = getPrimaryEntry().copyRename (df.getPrimaryFile (), name, ext);
            return DataObject.find( fo );
        }
        
        throw new IOException( "SaveAs operation not supported for this file type." );
    }
    
    /** Set the set of cookies.
     * To the provided cookie set a listener is attached,
    * and any change to the set is propagated by
    * firing a change on {@link #PROP_COOKIE}.
    *
    * @param s the cookie set to use
    * @deprecated just use getCookieSet().add(...) instead
    */
    @Deprecated
    protected final void setCookieSet (CookieSet s) {
        setCookieSet(s, true);
    }

    /** Set the set of cookies.
     *
     * @param s the cookie set to use
     * @param fireChange used when called from getter. In this case event shouldn't
     * be fired.
     */
    private void setCookieSet (CookieSet s, boolean fireChange) {
        synchronized (cookieSetLock) {
            ChangeListener ch = getChangeListener();

            if (cookieSet != null) {
                cookieSet.removeChangeListener (ch);
            }

            s.addChangeListener (ch);
            cookieSet = s;
        }
        
        if (fireChange) {
            fireCookieChange ();
        }
    }
    
    /** Get the set of cookies.
     * If the set had been
    * previously set by {@link #setCookieSet}, that set
    * is returned. Otherwise an empty set is
    * returned.
    *
    * @return the cookie set (never <code>null</code>)
    */
    protected final CookieSet getCookieSet () {
        return getCookieSet(true);
    }

    final CookieSet getCookieSet(boolean create) {
        synchronized (cookieSetLock) {
            if (cookieSet != null) return cookieSet;
            if (!create) {
                return null;
            }

            // generic cookie set with reference to data object and 
            // a callback that updates FileObjects in its list.
            CookieSet g = CookieSet.createGeneric(getChangeListener());
            g.assign(DataObject.class, this);
            setCookieSet (g, false);
            return cookieSet;
        }
    }

    /** Look for a cookie in the current cookie set matching the requested class.
    *
    * @param type the class to look for
    * @return an instance of that class, or <code>null</code> if this class of cookie
    *    is not supported
    */
    @Override
    public <T extends Node.Cookie> T getCookie(Class<T> type) {
        CookieSet c = cookieSet;
        if (c != null) {
            T cookie = c.getCookie (type);
            if (cookie != null) return cookie;
        }
        return super.getCookie (type);
    }

    @Override
    public Lookup getLookup() {
        int version = associateLookup();
        assert version <= 1;
        if (version >= 1) {
            return getCookieSet().getLookup();
        }
        return super.getLookup();
    }
    
    /** Influences behavior of {@link #getLookup()} method. Depending on the
     * returned integer, one can get different, better and more modern  content 
     * of the {@link Lookup}:
     * <ul>
     * <li><b>version 0</b> - delegates to <code>getNodeDelegate().getLookup()</code>.</li>
     * <li><b>version 1</b> - delegates to <code>getCookieSet().getLookup()</code>
     *   and makes sure {@link FileObject}, <code>this</code> and 
     *   {@link Node} are in the lookup. The {@link Node} is created lazily
     *   by calling {@link #getNodeDelegate()}.
     * </li>
     * </ul>
     * General suggestion is to always return the highest supported version
     * when creating new objects and to stick with certain version when backward
     * compatibility is requested.
     * 
     * @return version identifying content of the lookup (currently 0 or 1)
     * @since 7.27
     */
    protected int associateLookup() {
        return 0;
    }
    
    /** Utility method to register editor for this {@link DataObject}.
     * Call it from constructor with appropriate mimeType. The system will
     * make sure that appropriate cookies ({@link Openable}, {@link Editable},
     * {@link CloseCookie}, {@link EditorCookie}, {@link SaveAsCapable},
     * {@link LineCookie} are registered into {@link #getCookieSet()}.
     * <p>
     * The selected editor is <a href="@org-netbeans-core-multiview@/org/netbeans/core/api/multiview/MultiViews.html">
     * MultiView component</a>, if requested (this requires presence of
     * the <a href="@org-netbeans-core-multiview@/overview-summary.html">MultiView API</a>
     * in the system. Otherwise it is plain {@link CloneableEditor}.
     * 
     * @param mimeType mime type to associate with
     * @param useMultiview should the used component be multiview?
     * @since 7.27
     */
    protected final void registerEditor(final String mimeType, boolean useMultiview) {
        MultiDOEditor.registerEditor(this, mimeType, useMultiview);
    }

    /** Fires cookie change.
    */
    final void fireCookieChange () {
        firePropertyChange (PROP_COOKIE, null, null);
    }

    /** Fires property change but in event thread.
    */
    private void firePropertyChangeLater (
        final String name, final Object oldV, final Object newV
    ) {
        synchronized (firingProcessor) {
            if (later == null) {
                later = new LinkedHashMap<String, PropertyChangeEvent>();
            }
            later.put(name, new PropertyChangeEvent(this, name, oldV, newV));
        }
        firingProcessor.post(new Runnable () {
    	    public void run () {
                Map<String,PropertyChangeEvent> fire;
                synchronized (firingProcessor) {
                    fire = later;
                    later = null;
                }
                if (fire == null) {
                    return;
                }
                for (PropertyChangeEvent ev : fire.values()) {
                    String name = ev.getPropertyName();
                    firePropertyChange (name, ev.getOldValue(), ev.getNewValue());
                    if (PROP_FILES.equals(name) || PROP_PRIMARY_FILE.equals(name)) {
                        updateFilesInCookieSet();
                    }
                }
            }
        }, 100, Thread.MIN_PRIORITY);
    }

    /**
     * Posts a task to delayProcessor such that task
     *   1. waits for the FolderList to finish
     *   2. calls firePropertyChangeLater with PROP_FILES
     * Second time this method is called (delayedPropFilesTask is not null)
     * the new task is not created - the old one is rescheduled to run again.
     *
     * NOTE: this method should be improved not to fire twice in some cases.
     */
    private void firePropFilesAfterFinishing() {
        synchronized (delayedPropFilesLock) {
            if (delayedPropFilesTask == null) {
                delayedPropFilesTask = delayProcessor.post(new Runnable() {
                    public void run() {
                        FolderList l = getFolderList();
                        if (l != null) {
                            l.waitProcessingFinished();
                        }
                        firePropertyChangeLater(PROP_FILES, null, null);
                    }
                });
            } else {
                delayedPropFilesTask.schedule(0);
            }
        }
    }
    
    /** sets checked to true */
    @Override
    final void recognizedByFolder() {
        checked = true;
    }

    private ChangeAndBefore chLis;

    final ChangeAndBefore getChangeListener() {
        if (chLis == null) {
            chLis = new ChangeAndBefore();
        }
        return chLis;
    }

    // -- Following methods were added in order to wrap calls to MultiFileLoader
    // and check if the loader is really of this type. This hack was added to
    // keep backward compatibility of DataFolder and DataShadow classes, which
    // were originally subclassing DataObject, but was changed to subclass
    // MultiDataObject. Methods can be removed as the deprecated constructor
    // MultiDataObject(FileObject, DataLoader) disappears.
    
    private final MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject fo) {
        MultiFileLoader loader = getMultiFileLoader ();
        
        if (loader != null)
            return loader.createPrimaryEntry (obj, fo);
        
        Entry e;
        if (fo.isFolder ())
            e = new FileEntry.Folder(obj, fo);
        else
            e = new FileEntry (obj, fo);
        
        return e;
    }

    private final MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject fo) {
        MultiFileLoader loader = getMultiFileLoader ();
        
        if (loader != null)
            return loader.createSecondaryEntryImpl (obj, fo);
        
        Entry e;
        if (fo.isFolder ())
            e = new FileEntry.Folder(obj, fo);
        else
            e = new FileEntry (obj, fo);
        
        return e;
    }

    private final MultiDataObject createMultiObject(FileObject fo) throws DataObjectExistsException, IOException {
        MultiFileLoader loader = getMultiFileLoader ();

        MultiDataObject obj;

        if (loader != null) {
            obj = DataObjectPool.createMultiObject(loader, fo);
        } else {
            obj = (MultiDataObject)getLoader ().findDataObject (fo, RECOGNIZER);
        }
        return obj;
    }

    private final void checkConsistency (MultiDataObject obj) {
        MultiFileLoader loader = getMultiFileLoader ();

        if (loader != null)
            loader.checkConsistency (obj);
    }

    private final void checkFiles (MultiDataObject obj) {
        MultiFileLoader loader = getMultiFileLoader ();

        if (loader != null)
            loader.checkFiles (obj);
    }

    private static EmptyRecognizer RECOGNIZER = new EmptyRecognizer();
    
    private static class EmptyRecognizer implements DataLoader.RecognizedFiles {
        EmptyRecognizer() {}
        public void markRecognized (FileObject fo) {
        }
    }
    
    // End of compatibility hack. --^

    /** Save pairs Entry <-> Entry.getFile () in the list
     *  @return list of saved pairs
     */
    final List<Pair> saveEntries() {
        synchronized ( synchObjectSecondary() ) {
            LinkedList<Pair> ll = new LinkedList<Pair>();

            ll.add (new Pair(getPrimaryEntry ()));
            for (MultiDataObject.Entry en: secondaryEntries()) {
                ll.add (new Pair(en));
            }
            return ll;
        }
    }
    
    /** Restore entries from the list. If Entry.getFile () has changed from
     * time when backup list was created, original file is restored and
     * Entry is re-assigned to it.
     * @param backup list obtained from {@link #saveEntries ()} function
     */
    final void restoreEntries(List<Pair> backup) {
        for (Pair p: backup) {
            if (p.entry.getFile ().equals (p.file))
                continue;
            if (p.file.isValid()) {
                p.entry.changeFile (p.file);
            } else {
                // copy back
                try {
                    if (p.entry.getFile ().isData ())
                        p.entry.changeFile (p.entry.getFile ().copy (p.file.getParent (), p.file.getName (), p.file.getExt ()));
                    else {
                        FileObject fo = p.file.getParent ().createFolder (p.file.getName ());
                        FileUtil.copyAttributes (p.entry.getFile (), fo);
                        p.entry.changeFile (fo);
                    }
                } catch (IOException e) {
                    // should not occure
                }
            }
        }
    }
    
    static final class Pair {
        MultiDataObject.Entry entry;
        FileObject file;

        Pair(MultiDataObject.Entry e) {
            entry = e;
            file = e.getFile ();
        }
    }
    
    /** Represents one file in a {@link MultiDataObject group data object}. */
    public abstract class Entry implements java.io.Serializable {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 6024795908818133571L;

        /** modified from MultiDataObject operations, that is why it is package
        * private. Do not assign anything to this object, use changeFile method
        */
        private FileObject file;

        /** This factory is used for creating new clones of the holding lock for internal
        * use of this DataObject. It factory is null it means that the file entry is not
        */
        private transient WeakReference<FileLock> lock;

        @SuppressWarnings("deprecation")
        protected Entry (FileObject file) {
            if (file == null) {
                throw new NullPointerException();
            }
            this.file = file;
            if (!isImportant()) {
                file.setImportant(false);
            }
        }

        /** A method to change the entry file to some else.
        * @param newFile
        */
        @SuppressWarnings("deprecation")
        final void changeFile (FileObject newFile) {
            if (newFile == null) {
                throw new NullPointerException("NPE for " + file); // NOI18N
            }
            if (newFile.equals (file)) {
                return;
            }
            if (ERR.isLoggable(Level.FINE)) {
                ERR.fine("changeFile: " + newFile + " for " + this + " of " + getDataObject());  // NOI18N
            }
            newFile.setImportant (isImportant ());
            this.file = newFile;
            
            // release lock for old file
            FileLock l = lock == null ? null : lock.get();
            if (l != null && l.isValid ()) {
                if (ERR.isLoggable(Level.FINE)) {
                    ERR.fine("releasing old lock: " + this + " was: " + l);
                }
                l.releaseLock ();
            }
            lock = null;
        }

        /** Get the file this entry works with.
        */
        public final FileObject getFile () {
            return file;
        }
        
        /** Get the multi data object this entry is assigned to.
         * @return the data object
        */
        public final MultiDataObject getDataObject () {
            return MultiDataObject.this;
        }
        
        /** Method that allows to check whether an entry is important or is not.
        * Should be overriden by subclasses, the current implementation returns 
        * true.
        *
        * @return true if this entry is important or false if not
        */
        public boolean isImportant () {
            return true;
        }

        /** Called when the entry is to be copied.
        * Depending on the entry type, it should either copy the underlying <code>FileObject</code>,
        * or do nothing (if it cannot be copied).
        * @param f the folder to create this entry in
        * @param suffix the suffix to add to the name of original file
        * @return the copied <code>FileObject</code> or <code>null</code> if it cannot be copied
        * @exception IOException when the operation fails
        */
        public abstract FileObject copy (FileObject f, String suffix) throws IOException;

        /** Called when the entry is to be renamed.
        * Depending on the entry type, it should either rename the underlying <code>FileObject</code>,
        * or delete it (if it cannot be renamed).
        * @param name the new name
        * @return the renamed <code>FileObject</code> or <code>null</code> if it has been deleted
        * @exception IOException when the operation fails
        */
        public abstract FileObject rename (String name) throws IOException;

        /** Called when the entry is to be moved.
        * Depending on the entry type, it should either move the underlying <code>FileObject</code>,
        * or delete it (if it cannot be moved).
        * @param f the folder to move this entry to
        * @param suffix the suffix to use
        * @return the moved <code>FileObject</code> or <code>null</code> if it has been deleted
        * @exception IOException when the operation fails
        */
        public abstract FileObject move (FileObject f, String suffix) throws IOException;

        /** Called when the entry is to be deleted.
        * @exception IOException when the operation fails
        */
        public abstract void delete () throws IOException;

        /** Called when the entry is to be created from a template.
        * Depending on the entry type, it should either copy the underlying <code>FileObject</code>,
        * or do nothing (if it cannot be copied).
        * @param f the folder to create this entry in
        * @param name the new name to use
        * @return the copied <code>FileObject</code> or <code>null</code> if it cannot be copied
        * @exception IOException when the operation fails
        */
        public abstract FileObject createFromTemplate (FileObject f, String name) throws IOException;

        /** 
         * Called when the entry is to be copied and renamed.
         * @param f the folder to create this entry in
         * @param name new file name
         * @param ext new file extension
         * @return the copied and renamed <code>FileObject</code>, never null
         * @exception IOException when the operation fails
         * @since 6.3
         */
        public FileObject copyRename (FileObject f, String name, String ext) throws IOException {
            throw new IOException( "Unsupported operation" );
        }
        
        /** Try to lock this file entry.
        * @return the lock if the operation was successful; otherwise <code>null</code>
        * @throws IOException if the lock could not be taken
        */
        public FileLock takeLock() throws IOException {
            FileLock l = lock == null ? null : lock.get ();
            if (l == null || !l.isValid ()){
                l = getFile ().lock ();
                lock = new WeakReference<FileLock> (l);
            }
            if (ERR.isLoggable(Level.FINE)) {
                ERR.fine("takeLock: " + this + " is: " + l);
            }
            return l;
        }

        /** Tests whether the entry is locked.
         * @return <code>true</code> if so
         */
        public boolean isLocked() {
            FileLock l = lock == null ? null : lock.get ();
            return l != null && l.isValid ();
        }

        @Override
        public boolean equals(Object o) {
            if (! (o instanceof Entry)) return false;
            return getFile ().equals(((Entry) o).getFile ());
        }

        @Override
        public int hashCode() {
            return getFile ().hashCode();
        }

        /** Make a Serialization replacement.
         * The entry is identified by the
        * file object is holds. When serialized, it stores the
        * file object and the data object. On deserialization
        * it finds the data object and creates the right entry
        * for it.
        */
        protected Object writeReplace () {
            return new EntryReplace (getFile ());
        }
    }    

    @Override
    void notifyFileDeleted (FileEvent fe) {
        removeFile (fe.getFile ());
        if (fe.getFile ().equals (getPrimaryFile ())) {
            try {
                MultiDataObject.this.markInvalid0 ();
            } catch (PropertyVetoException ex) {
                // silently ignore - the file has an opened editor with unsaved changes
                // and user chose to keep the editor opened
                Logger.getLogger(MultiDataObject.class.getName()).log(Level.FINE, null, ex);
            }
        }
    }

    /** Fired when a file has been added to the same folder
     * @param fe the event describing context where action has taken place
     */
    @Override
    void notifyFileDataCreated(FileEvent fe) {
        checked = false;
    }

    final void updateFilesInCookieSet() {
        CookieSet set = getCookieSet(false);
        if (set != null) {
            set.assign(FileObject.class, files().toArray(new FileObject[0]));
        }
    }

    final void updateNodeInCookieSet() {
        if (isValid() && associateLookup() >= 1) {
            CookieSet set = getCookieSet(false);
            if (set != null) {
                set.assign(Node.class, getNodeDelegate());
            }
        }
    }

    void checkCookieSet(Class<?> c) {
    }
    
    /** Change listener and implementation of before.
     */
    private final class ChangeAndBefore implements ChangeListener, CookieSet.Before {
        @Override
        public void stateChanged (ChangeEvent ev) {
            fireCookieChange ();
        }

        @Override
        public void beforeLookup(Class<?> clazz) {
            if (clazz.isAssignableFrom(FileObject.class)) {
                updateFilesInCookieSet();
            }
            if (clazz.isAssignableFrom(Node.class)) {
                updateNodeInCookieSet();
            }
            checkCookieSet(clazz);
        }
    }

    /** Entry replace.
    */
    private static final class EntryReplace extends Object implements java.io.Serializable {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -1498798537289529182L;

        /** file object of the entry */
        private FileObject file;
        /** entry to be used during read */
        private transient Entry entry;

        public EntryReplace (FileObject fo) {
            file = fo;
        }

        private void readObject (ObjectInputStream ois) throws IOException, ClassNotFoundException {
            ois.defaultReadObject ();
            try {
                DataObject obj = DataObject.find (file);
                if (obj instanceof MultiDataObject) {
                    MultiDataObject m = (MultiDataObject)obj;

                    if (file.equals (m.getPrimaryFile ())) {
                        // primary entry
                        entry = m.getPrimaryEntry ();
                    } else {
                        // secondary entry
                        Entry e = m.findSecondaryEntry(file);
                        if (e == null) {
                            throw new InvalidObjectException (obj.toString ());
                        }
                        // remember the entry
                        entry = e;
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                throw new InvalidObjectException (ex.getMessage ());
            }
        }

        public Object readResolve () {
            return entry;
        }
    }
}
