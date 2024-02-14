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

package org.openide.filesystems;

import java.io.Externalizable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Enumerations;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/** Implementation of the file object for multi file system.
*
* @author Jaroslav Tulach,
*/
final class MultiFileObject extends AbstractFolder implements FileObject.PriorityFileChangeListener {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -2343651324897646809L;

    /** default extension separator */
    private static final char EXT_SEP = '.';

    /** default path separator */
    private static final char PATH_SEP = '/';
    static final String WEIGHT_ATTRIBUTE = "weight"; // NOI18N
    private static final FileSystem.AtomicAction markAtomicAction = new FileSystem.AtomicAction() {
            public void run() {
            }
        };

    static final ThreadLocal<FileObject> attrAskedFileObject = new ThreadLocal<FileObject>();

    /** list of objects that we delegate to and that already
    * has been created.
    */
    private Set<FileObject> delegates;

    /** current delegate (the first object to delegate to), never null */
    private FileObject leader;

    /** Reference to lock or null */
    private Reference<MfLock> lock;
    
    /** Stack trace of locking thread. */
    protected Throwable lockedBy;

    /** listener */
    private FileChangeListener weakL;

    /**performance trick:  holds delegetad FileObject used last time to getAttributes.
     * It may looks simply, but all more sophisticated solutions was less efficient.
     */
    private static Map<MultiFileObject, AttributeCache> fo2AttribCache = 
            new WeakHashMap<MultiFileObject, AttributeCache>();

    /** Constructor. Takes reference to file system this file belongs to.
    *
    * @param fs the file system
    * @param parent the parent object (folder)
    * @param name name of the object (e.g. <code>filename.ext</code>)
    */
    public MultiFileObject(MultiFileSystem fs, MultiFileObject parent, String name) {
        super(fs, parent, name);

        weakL = org.openide.util.WeakListeners.create(
                FileObject.PriorityFileChangeListener.class, FileChangeListener.class, this, null
            );

        update();

        if (leader == null) {
            leader = new AbstractFileObject.Invalid(name);
            validFlag = false;
        }
    }

    /** Constructor for root.
    *
    * @param fs the file system
    */
    public MultiFileObject(MultiFileSystem fs) {
        this(fs, null, ""); // NOI18N
    }

    /** File system.
    */
    public FileSystem getLeaderFileSystem() throws FileStateInvalidException {
        return leader.getFileSystem();
    }

    static synchronized void freeAllAttribCaches() {
        fo2AttribCache.clear();
    }
    
    /** Frees cached objects - added with perf.changes  */
    private void freeAttribCache() {
        getAttributeCache().free();
    }
    
    private AttributeCache getAttributeCache() {
        synchronized (MultiFileObject.class) {
            AttributeCache retval = fo2AttribCache.get(this);
            if (retval == null) {
                retval = new AttributeCache();
                fo2AttribCache.put(this, retval);
            }
            return retval;
        }
    }

    private boolean setAttributeOnFO(FileObject fo, String attrToSet, Object value, boolean fire) throws IOException {
        if (fo instanceof AbstractFolder) {
            ((AbstractFolder) fo).setAttribute(attrToSet, value, false);
        } else {
            /** cannot disable firing from underlaying delegate if not AbstractFolder
             * without API change. So I don`t fire events from this method with one
             * exception - root.
             */
            fire = fire && fo.isRoot();
            fo.setAttribute(attrToSet, value);
        }
        return fire;
    }

    /** Updates list of all references.
    */
    private void update() {
        MultiFileSystem mfs = getMultiFileSystem();
        FileSystem[] arr = mfs.getDelegates();

        Set<FileObject> now = (delegates == null) ? Collections.emptySet() : delegates;
        Set<FileObject> del = new HashSet<FileObject>(arr.length * 2);
        Number maxWeight = 0;
        FileObject led = null;

        String name = getPath();
        FileSystem writable = mfs.writableLayer(name);

        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != null) {
                FileObject fo = mfs.findResourceOn(arr[i], name);

                if (fo != null) {
                    del.add(fo);

                    if (!now.remove(fo)) {
                        // now there yet
                        fo.addFileChangeListener(weakL);
                    }

                    if (fo.isValid()) {
                        Number weight = 
                            fo.isRoot() &&
                            !mfs.canHaveRootAttributeOnReadOnlyFS(WEIGHT_ATTRIBUTE)
                            ? 0 : weightOf(fo, writable);
                        if (led == null || weight.doubleValue() > maxWeight.doubleValue()) {
                            led = fo;
                            maxWeight = weight;
                        }
                    }
                }
            }
        }

        Iterator<FileObject> it = now.iterator();
        while (it.hasNext()) {
            FileObject fo = it.next();
            fo.removeFileChangeListener(weakL);
        }

        if (led != null) {
            // otherwise leave the leader to be last file that represented
            // this one
            if (!led.equals(this.leader) && (this.leader != null)) {
                // isValid prevents here from firing events after MFO.delete
                if (isData() && isValid()) {
                    fileChanged0(new FileEvent(this));
                }

                getMultiFileSystem().notifyMigration(this);
            }

            this.leader = led;
        }

        this.delegates = del;
    }

    /** Update all existing subobjects.
    */
    void updateAll() {
        FileSystem mfs = getMultiFileSystem();

        try {
            mfs.beginAtomicAction();

            // enumeration of all existing objects
            Enumeration<AbstractFolder> en = existingSubFiles(true);

            while (en.hasMoreElements()) {
                MultiFileObject mfo = (MultiFileObject) en.nextElement();

                if (mfo.isFolder() && !mfo.isInitialized()) {
                    continue;
                }

                mfo.freeAttribCache();
                mfo.superRefresh(true);
            }
        } finally {
            mfs.finishAtomicAction();
        }
    }

    /** This method was added to achieve firing events that attributes
     * were changed after setDelegates. Events are not fired reliable but this solution was
     * choosed because of performance reasons. Attributes name is set to null - what means
     * that one of attributes was probably changed.
     */
    void updateAllAfterSetDelegates(FileSystem[] oldFileSystems) {
        try {
            getMultiFileSystem().beginAtomicAction();

            FileSystem[] fileSystems = getMultiFileSystem().getDelegates();
            Enumeration<AbstractFolder> en = existingSubFiles(true);

            while (en.hasMoreElements()) {
                MultiFileObject mfo = (MultiFileObject) en.nextElement();

                if (mfo.isFolder() && !mfo.isInitialized()) {
                    continue;
                }

                if (mfo.hasListeners()) {
                    String path = mfo.getPath();
                    FileObject oldLeader = findLeader(oldFileSystems, path);
                    FileObject newLeader = findLeader(fileSystems, path);

                    if ((oldLeader != null) && (newLeader != null) && !oldLeader.equals(newLeader)) {
                        mfo.fileAttributeChanged0(new FileAttributeEvent(mfo, null, null, null));
                    }
                }

                mfo.freeAttribCache();
                mfo.refresh(true);
            }
        } finally {
            getMultiFileSystem().finishAtomicAction();
        }
    }

    private void refreshAfterEvent(FileEvent fe) {
        FileObject fFile = fe.getFile();
        superRefresh(false);

        MultiFileObject mFile = (MultiFileObject) getFileObject(fFile.getName(), fFile.getExt());

        if (mFile != null) {
            mFile.superRefresh(false);
        }
    }

    private void superRefresh(boolean expected) {
        super.refresh(expected);
    }

    private FileObject findLeader(FileSystem[] fs, String path) {
        MultiFileSystem mfs = getMultiFileSystem();

        Number maxWeight = 0;
        FileObject _leader = null;
        FileSystem writable = mfs.writableLayer(path);
        
        for (FileSystem f : fs) {
            if (f == null) {
                continue;
            }
            FileObject fo = mfs.findResourceOn(f, path);

            if (fo != null) {
                Number weight = weightOf(fo, writable);
                if (_leader == null || weight.doubleValue() > maxWeight.doubleValue()) {
                    _leader = fo;
                    maxWeight = weight;
                }
            }
        }

        return _leader;
    }

    private static Number weightOf(FileObject f, FileSystem writable) {
        try {
            if (f.getFileSystem() == writable) {
                return Double.MAX_VALUE;
            }
        } catch (FileStateInvalidException x) {/* ignore */}
        Object weight = f.getAttribute(WEIGHT_ATTRIBUTE);
        if (weight instanceof Number) {
            return (Number) weight;
        } else if (weight == null) {
            return 0;
        } else {
            try {
                Logger.getLogger(MultiFileObject.class.getName()).log(
                        Level.WARNING, "File {0} in {1} has nonnumeric weight {2} of type {3}",
                        new Object[] {f.getPath(), f.getFileSystem(), weight, weight.getClass().getName()});
            } catch (FileStateInvalidException x) {/* ignore */}
            return 0;
        }
    }

    /** Getter for the right file system */
    private MultiFileSystem getMultiFileSystem() {
        return (MultiFileSystem) getFileSystem();
    }

    /** Getter for one of children.
    */
    private MultiFileObject getMultiChild(String name) {
        return (MultiFileObject) getChild(name);
    }

    /** Converts the file to be writable.
    * The file has to be locked!
    *
    * @return file object (new leader) that is writable
    * @exception IOException if the object cannot be writable
    */
    private FileObject writable(boolean copyContents) throws IOException {
        MultiFileSystem fs = getMultiFileSystem();
        FileSystem single = fs.createWritableOn(getPath());

        if (single != leader.getFileSystem()) {
            // if writing to a file that is not on writable fs =>
            // copy it
            if (leader.isFolder()) {
                leader = FileUtil.createFolder(root(single), getPath());
            } else {
                FileObject folder = FileUtil.createFolder(root(single), getParent().getPath());
                if (copyContents) {
                    leader = leader.copy(folder, leader.getName(), leader.getExt());
                } else {
                    leader = folder.createData(leader.getNameExt());
                }
            }

            MfLock l = ((lock == null) ? null : lock.get());

            if (l != null) {
                // update the lock
                l.addLock(leader);
            }
        }

        return leader;
    }

    /** All objects that are beyond this one.
    * @return enumeration of FileObject
    */
    private Enumeration<FileObject> delegates() {
        return getMultiFileSystem().delegates(getPath());
    }

    /** Method that goes upon list of folders and updates its locks. This is used when
    * an object is masked which may lead to creation of folders on a disk.
    *
    * @param fo folder to check
    * @exception IOException if something locks cannot be updated
    */
    private static void updateFoldersLock(FileObject fo)
    throws IOException {
        while (fo != null) {
            MultiFileObject mfo = (MultiFileObject) fo;

            MfLock l = (mfo.lock == null) ? null : mfo.lock.get();

            if (l != null) {
                // the file has been locked => update the lock
                mfo.writable(true);
            }

            fo = fo.getParent();
        }
    }

    //
    // List
    //

    /** Method that allows subclasses to return its children.
    *
    * @return names (name . ext) of subfiles
    */
    protected final String[] list() {
        Properties exclude = new Properties();
        List<String> addList = new LinkedList<String>();
        Set<String> addSet = new HashSet<String>(101);

        Enumeration<FileObject> it = delegates();

        while (it.hasMoreElements()) { // cycle 1

            FileObject folder = it.nextElement();

            if ((folder == null) || !folder.isFolder()) {
                continue;
            }

            FileObject[] arr = folder.getChildren();
            Properties local = null;

            for (int i = 0; i < arr.length; i++) {
                String name = arr[i].getNameExt();

                if (name.endsWith(MultiFileSystem.MASK)) {
                    String basename = name.substring(0, name.length() - MultiFileSystem.MASK.length());

                    // this name should be excluded from next rounds of cycle 1
                    if (local == null) {
                        local = new Properties();
                        local.putAll(exclude);
                    }

                    local.setProperty(basename, basename);

                    if (!getMultiFileSystem().getPropagateMasks()) {
                        // By default, unused mask files are not displayed as file children.
                        // When propagate masks is turned on, though, they should be.
                        // This is useful e.g. when using MFSs as delegates for other MFSs.
                        continue;
                    }
                }

                // lets add this name, if not added yet and is not excluded by
                // previous level
                if (!addSet.contains(name) && (exclude.getProperty(name) == null)) {
                    addSet.add(name);
                    addList.add(name);
                }
            }

            // change the excludes to the new ones produced at this round
            if (local != null) {
                exclude = local;
            }
        }

        if (getMultiFileSystem().getPropagateMasks()) {
            // remove all masked files from the array, even if they were
            // masked on the same level as they were added
            addList.removeAll(exclude.keySet());
        }

        String[] res = addList.toArray(new String[0]);

        return res;
    }

    /** When refreshing, also update the state of delegates.
    */
    /** [PENDING] expected rename of some refresh method */

    /*protected void internalRefresh (
        String add, String remove, boolean fire, boolean expected, String[] list
     **/
    protected void refresh(String add, String remove, boolean fire, boolean expected) {
        try {
            getFileSystem().beginAtomicAction();

            synchronized (this) {
                update();

                /** [PENDING] expected rename of some refresh method */

                //super.internalRefresh (add, remove, fire, expected, list);
                super.refresh(add, remove, fire, expected);
            }

            validFlag &= leader.isValid();
        } finally {
            getFileSystem().finishAtomicAction();
        }
    }

    /** Method to create a file object for given subfile.
    * @param name of the subfile
    * @return the file object
    */
    protected final AbstractFolder createFile(String name) {
        return new MultiFileObject(getMultiFileSystem(), this, name);
    }

    //
    // Info
    //

    /* Test whether this object is a folder.
    * @return true if the file object is a folder (i.e., can have children)
    */
    public boolean isFolder() {
        return (parent == null) || leader.isFolder();
    }

    /*
    * Get last modification time.
    * @return the date
    */
    public java.util.Date lastModified() {
        return leader.lastModified();
    }

    /* Test whether this object is a data object.
    * This is exclusive with {@link #isFolder}.
    * @return true if the file object represents data (i.e., can be read and written)
    */
    public boolean isData() {
        return leader.isData();
    }

    /* Test whether this FileObject is Virtual.
    * @return true if the file object represents a Virtual File
    */
    public boolean isVirtual() {
        return leader.isVirtual();
    }

    @Deprecated // have to override for compat
    public boolean isReadOnly() {
        MultiFileSystem fs = getMultiFileSystem();

        if (fs.isReadOnly()) {
            return true;
        }

        if (leader.isReadOnly()) {
            // if we can make it writable then nothing
            try {
                FileSystem simple = fs.createWritableOn(getPath());

                return simple == leader.getFileSystem();
            } catch (IOException e) {
                return true;
            }
        }

        return false;
    }

    public boolean canWrite() {
        MultiFileSystem fs = getMultiFileSystem();

        if (fs.isReadOnly()) {
            return false;
        }

        if (!leader.canWrite()) {
            // if we can make it writable then nothing
            try {
                FileSystem simple = fs.createWritableOn(getPath());

                return simple != leader.getFileSystem();
            } catch (IOException e) {
                return false;
            }
        }

        return true;
    }

    /* Get the MIME type of this file.
    * The MIME type identifies the type of the file's contents and should be used in the same way as in the <B>Java
    * Activation Framework</B> or in the {@link java.awt.datatransfer} package.
    * <P>
    * The default implementation calls {@link FileUtil#getMIMEType}.
    *
    * @return the MIME type textual representation, e.g. <code>"text/plain"</code>
    */
    public String getMIMEType() {
        return leader.getMIMEType();
    }

    /* Get the size of the file.
    * @return the size of the file in bytes or zero if the file does not contain data (does not
    *  exist or is a folder).
    */
    public long getSize() {
        return leader.getSize();
    }

    /** Get input stream.
    * @return an input stream to read the contents of this file
    * @exception FileNotFoundException if the file does not exists, is a folder
    * rather than a regular file  or is invalid
    */
    public InputStream getInputStream() throws java.io.FileNotFoundException {
        return leader.getInputStream();
    }

    /* Get output stream.
    * @param lock the lock that belongs to this file (obtained by a call to
    *   {@link #lock})
    * @return output stream to overwrite the contents of this file
    * @exception IOException if an error occures (the file is invalid, etc.)
    */
    public OutputStream getOutputStream(FileLock lock)
    throws java.io.IOException {
        MfLock l;
        FileLock lWritable;
        FileObject fo;

        try {
            getFileSystem().beginAtomicAction(markAtomicAction);

            synchronized (this) {
                l = testLock(lock);

                // this can also change lock in l.lock
                fo = writable(false);
                lWritable = l.findLock(fo);
            }

            return fo.getOutputStream(lWritable);
        } finally {
            getFileSystem().finishAtomicAction();
        }
    }

    @Override
    public synchronized boolean isLocked() {
        return lock != null && lock.get() != null;
    }
    
    /* Lock this file.
    * @return lock that can be used to perform various modifications on the file
    * @throws FileAlreadyLockedException if the file is already locked
    */
    public synchronized FileLock lock() throws IOException {
        if (lock != null) {
            FileLock f = (FileLock) lock.get();

            if (f != null) {
                FileAlreadyLockedException alreadyLockedException = new FileAlreadyLockedException(getPath());
                alreadyLockedException.initCause(lockedBy);
                throw alreadyLockedException;
            }
        }

        Set<? extends FileSystem> set = getMultiFileSystem().createLocksOn(getPath());
        MfLock l = new MfLock(leader, delegates(), set);

        lock = new WeakReference<MfLock>(l);

        assert (lockedBy = new Throwable("Locked by:")) != null;  //NOI18N
        return l;
    }

    /** Tests the lock if it is valid, if not throws exception.
    * @param l lock to test
    * @return the mf lock for this file object
    */
    private MfLock testLock(FileLock l) throws java.io.IOException {
        if (lock == null) {
            throw new FSException(NbBundle.getMessage(MultiFileObject.class, "EXC_InvalidLock",
                    l, getPath(), getMultiFileSystem().getDisplayName(), lock));
        }

        if (lock.get() != l) {
            throw new FSException(NbBundle.getMessage(MultiFileObject.class, "EXC_InvalidLock",
                    l, getPath(), getMultiFileSystem().getDisplayName(), lock.get()));
        }

        return (MfLock) l;
    }

    @Deprecated // have to override for compat
    public void setImportant(boolean b) {
        Enumeration<FileObject> en = delegates();

        while (en.hasMoreElements()) {
            FileObject fo = en.nextElement();
            fo.setImportant(b);
        }

        if (!b) {
            getMultiFileSystem().markUnimportant(this);
        }
    }

    /** Add one void-wrapping to an object. */
    private static final Object voidify(Object o) {
        if (o == null) {
            return new VoidValue(0);
        } else if (o instanceof VoidValue) {
            VoidValue vv = (VoidValue) o;

            return new VoidValue(vv.level + 1);
        } else {
            return o;
        }
    }

    /** Strip off one void-wrapping from an object. */
    private static final Object devoidify(Object o) {
        if (o instanceof VoidValue) {
            VoidValue vv = (VoidValue) o;

            if (vv.level == 0) {
                return null;
            } else {
                return new VoidValue(vv.level - 1);
            }
        } else {
            return o;
        }
    }

    /* Get the file attribute with the specified name.
    * @param attrName name of the attribute
    * @return appropriate (serializable) value or <CODE>null</CODE> if the attribute is unset (or could not be properly restored for some reason)
    */
    @Override
    public Object getAttribute(String attrName) {
        // Performance optimization (avoid calling getPath() too many times):
        final String path = getPath();
        if (path.isEmpty() && attrName.indexOf('\\') >= 0) {
            return null;
        }
        return getAttribute(attrName, path);
    }

    /** Special attributes which should not be checked for weight. See RemoveWritablesTest. */
    private static final Set<String> SPECIAL_ATTR_NAMES = new HashSet<String>(Arrays.asList(FileObject.REMOVE_WRITABLES_ATTR, WEIGHT_ATTRIBUTE, "java.io.File")); // NOI18N
    private final Object getAttribute(String attrName, String path) {
        // Look for attribute in any file system starting at the front.
        // Additionally, look for attribute in root folder, where
        // the relative path from the folder to the target file is
        // prepended to the attribute name, all separated with slashes.
        // This search scheme permits writable front systems to set file
        // attributes on deeply buried files in back systems without
        // actually creating copies of the files or even their parent folders.
        // [PENDING] consider the effects of mask files
        String prefixattr = ((path.length() == 0) ? null : (path.replace('/', '\\') + '\\' + attrName));

        { /*There was proved that this block enhances performance*/

            Object oPerf;
            FileObject localFo = getAttributeCache().getDelegate();
            String cachedAttrName = getAttributeCache().getAttributeName();

            if ((localFo != null) && !localFo.equals(this) && cachedAttrName.equals(attrName)) {
                if (localFo.isRoot() && (prefixattr != null)) {
                    try {
                        FileSystem foFs = localFo.getFileSystem();
                        if (!foFs.isReadOnly() || getMultiFileSystem().canHaveRootAttributeOnReadOnlyFS(prefixattr)) {
                            localFo = foFs.getRoot();
                            oPerf = getAttribute(localFo, prefixattr, ""); // NOI18N

                            if (oPerf != null) {
                                return devoidify(oPerf);
                            }
                        }
                    } catch (FileStateInvalidException fiex) {
                        //then continue
                    }
                }

                /** There is no chance to cache localFo.getPath(), because every
                 *  rename up to it in hierarchy makes this cache invalid.
                 */
                oPerf = getAttribute(localFo, attrName, localFo.getPath());

                if (oPerf != null) {
                    return devoidify(oPerf);
                }
            }
        }

        FileSystem[] systems = getMultiFileSystem().getDelegates();

        Number maxWeight = 0;
        Object attr = null;
        FileSystem writable = getMultiFileSystem().writableLayer(path);
        
        boolean revealEntries = isFolder() && "revealEntries".equals(attrName) &&
                writable != null && !writable.isReadOnly(); //NOI18N
        
        if (revealEntries) {
            return collectRevealedFiles();
        }

        //        boolean isLoaderAttr = /* DataObject.EA_ASSIGNED_LOADER */ "NetBeansAttrAssignedLoader".equals (attrName); // NOI18N                
        for (int i = 0; i < systems.length; i++) {
            if (systems[i] == null) {
                continue;
            }

            // Don't check for any assigned loader overrides except for leader & writable systems.
            // Note that this prevents front layers from overriding default loader!
            // Could be revisited but in the meantime this is a performance optimization.
            //            if (isLoaderAttr && leaderfs != null && systems[i] != leaderfs && systems[i].isReadOnly ()) {
            //                continue;
            //            }
            // The normal check:
            FileObject fo = getMultiFileSystem().findResourceOn(systems[i], path);

            if (fo != null) {
                Object o = getAttribute(fo, attrName, fo.getPath()); // Performance tricks:                

                if (o != null) {
                    if (SPECIAL_ATTR_NAMES.contains(attrName)) {
                        return devoidify(o);
                    }
                    Number weight = weightOf(fo, writable);
                    if (attr == null || weight.doubleValue() > maxWeight.doubleValue()) {
                        getAttributeCache().setDelegate(fo);
                        getAttributeCache().setAttributeName(attrName);
                        attr = o;
                        maxWeight = weight;
                    }
                }
            } 

            if (prefixattr != null) {
                if (
                    !systems[i].isReadOnly() ||
                    getMultiFileSystem().canHaveRootAttributeOnReadOnlyFS(prefixattr)
                ) {
                    fo = systems[i].getRoot();

                    Object o = getAttribute(fo, prefixattr, ""); // NOI18N

                    if (o != null) {
                        Number weight = weightOf(fo, writable);
                        if (attr == null || weight.doubleValue() > maxWeight.doubleValue()) {
                            getAttributeCache().setDelegate(fo);
                            getAttributeCache().setAttributeName(attrName);
                            attr = o;
                            maxWeight = weight;
                        }
                    }
                }
            }
        }

        return devoidify(attr);
    }

    private static boolean sameFullName(FileObject f1, FileObject f2) {
        while (f1 != null && f2 != null) {
            if (!f1.getNameExt().equals(f2.getNameExt())) {
                return false;
            }
            f1 = f1.getParent();
            f2 = f2.getParent();
        }
        return f1 == null && f2 == null;
    }

    private Object getAttribute(FileObject fo, String attrName, String path) {
        Object o;

        FileObject previousFO = attrAskedFileObject.get();

        if (previousFO == null || !sameFullName(previousFO, this))  {
            attrAskedFileObject.set(this);
        }

        try {
            if (fo instanceof MultiFileObject) {
                o = ((MultiFileObject) fo).getAttribute(attrName, path);
            } else if (fo instanceof AbstractFileObject) {
                o = ((AbstractFileObject) fo).getAttribute(attrName, path);
            } else {
                o = fo.getAttribute(attrName);
            }
        } finally {
            attrAskedFileObject.set(previousFO);
        }
        return o;
    }

    /* Set the file attribute with the specified name.
    * @param attrName name of the attribute
    * @param value new value or <code>null</code> to clear the attribute. Must be serializable, although particular file systems may or may not use serialization to store attribute values.
    * @exception IOException if the attribute cannot be set. If serialization is used to store it, this may in fact be a subclass such as {@link NotSerializableException}.
    */
    public void setAttribute(String attrName, Object value)
    throws IOException {
        setAttribute(attrName, value, true); //NOI18N        
    }

    /* helper method for MFO.setAttribute. MFO can disable firing from underlaying
     * layers. Should be reviewed in 3.4 or later
      *@see MultiFileObject#setAttribute*/
    void setAttribute(String attrName, Object value, boolean fire)
    throws IOException {
        // Similar to getAttribute. Here we use createWritableOn to decide which fs
        // the attribute should be stored on; it is stored on the actual file if
        // possible, if not then the lowest containing folder.
        String path = getPath();
        FileSystem fs = getMultiFileSystem().createWritableOn(path);
        FileObject fo = getMultiFileSystem().findResourceOn(fs, path);
        Object oldValue = null;
        String attrToSet = attrName;

        if (fire) {
            oldValue = getAttribute(attrName);
            if (oldValue == value) {
                return;
            }
        }

        if (fo == null) {
            fo = fs.getRoot();
            attrToSet = path.replace('/', '\\') + '\\' + attrName;
        }

        getAttributeCache().setDelegate(fo);
        getAttributeCache().setAttributeName(attrToSet);

        if (value == null) {
            fire = setAttributeOnFO(fo, attrToSet, null, fire);
            if (fo.getAttribute(attrToSet) != null) {
                fire = setAttributeOnFO(fo, attrToSet, voidify(null), fire);
            }
        } else {
            fire = setAttributeOnFO(fo, attrToSet, voidify(value), fire);
        }

        // fire changes for original attribute name even if the attr is actually
        // stored in the root FO if FO instanceof AbstractFolder (that has supressed firing).

        /* [PENDING] only this MultiFileObject should fire event and it`s delegate
         * should not fire event (nevertheless this delegate is physically used
         * to write given attribute - only implementation detail -
         * nobody should rely on current implementation).
         **/
        if (fire && (oldValue != value) && hasAtLeastOneListeners()) {
            fileAttributeChanged0(new FileAttributeEvent(this, attrName, oldValue, value));
        }
    }

    /* Get all file attribute names for this file.
    * @return enumeration of keys (as strings)
    */
    public Enumeration<String> getAttributes() {
        return getAttributes(getPath());
    }

    private final Enumeration<String> getAttributes(String path) {
        Set<String> s = new HashSet<String>();
        FileSystem[] systems = getMultiFileSystem().getDelegates();
        final boolean empty = path.length() == 0;

        // [PENDING] will not remove from the enumeration voided-out attributes
        // (though this is probably not actually necessary)
        String prefix = empty ? null : (path.replace('/', '\\') + '\\');

        for (int i = 0; i < systems.length; i++) {
            if (systems[i] == null) {
                continue;
            }

            FileObject fo = getMultiFileSystem().findResourceOn(systems[i], path);

            if (fo != null) {
                Enumeration<String> e = fo.getAttributes();

                while (e.hasMoreElements()) {
                    String attr = e.nextElement();
                    if (empty && attr.indexOf('\\') >= 0) {
                        continue;
                    }
                    s.add(attr);
                }
            }

            if (prefix != null) {
                fo = systems[i].getRoot();

                Enumeration<String> e;

                if (fo instanceof MultiFileObject) {
                    e = ((MultiFileObject) fo).getAttributes(""); // NOI18N
                } else if (fo instanceof AbstractFileObject) {
                    e = ((AbstractFileObject) fo).getAttributes(""); // NOI18N
                } else {
                    e = fo.getAttributes();
                }

                while (e.hasMoreElements()) {
                    String attr = e.nextElement();

                    if (attr.startsWith(prefix) && (attr.substring(prefix.length()).indexOf('\\') == -1)) {
                        s.add(attr.substring(prefix.length()));
                    }
                }
            }
        }

        return Collections.enumeration(s);
    }

    /* Create a new folder below this one with the specified name. Fires
    * <code>fileCreated</code> event.
    *
    * @param name the name of folder to create (without extension)
    * @return the new folder
    * @exception IOException if the folder cannot be created (e.g. already exists)
    */
    @Override
    public FileObject createFolder(String name) throws IOException {
        MultiFileObject fo;

        try {
            getFileSystem().beginAtomicAction();

            synchronized (this) {
                MultiFileSystem fs = getMultiFileSystem();

                if (fs.isReadOnly()) {
                    throw new FSException(NbBundle.getMessage(MultiFileObject.class, "EXC_FSisRO", fs.getDisplayName()));
                }

                if (isReadOnly()) {
                    IOException ex = new IOException("Read only: " + leader + " delegates: " + delegates); // NOI18N
                    Exceptions.attachLocalizedMessage(ex, NbBundle.getMessage(MultiFileObject.class, "EXC_FisRO", name, fs.getDisplayName()));
                    throw ex;
                }

                String fullName = getPath() + PATH_SEP + name;

                if (!isFolder()) {
                    throw new FSException(NbBundle.getMessage(MultiFileObject.class, "EXC_FoNotFolder", name, getPath(), fs.getDisplayName()));
                }

                if (this.getFileObject(name) != null) {
                    throw new FSException(NbBundle.getMessage(MultiFileObject.class, "EXC_FolderAlreadyExist", name, fs.getDisplayName(), getPath()));
                }

                FileSystem simple = fs.createWritableOn(fullName);

                // create
                FileUtil.createFolder(root(simple), fullName);

                // try to unmask if necessary
                getMultiFileSystem().unmaskFileOnAll(simple, fullName);

                /** [PENDING] expected rename of some refresh method */
                //internalRefresh (name, null, true, false,null);
                refresh(name, null, true, false);

                fo = getMultiChild(name);

                if (fo == null) {
                    // system error
                    throw new FileStateInvalidException(NbBundle.getMessage(MultiFileObject.class, "EXC_ApplicationCreateError", getPath(), name));
                }

                FileObject[] chlds = fo.getChildren();

                for (int i = 0; i < chlds.length; i++) {
                    getMultiFileSystem().maskFile(simple, chlds[i].getPath());
                }

                if (hasListeners()) {
                    fileCreated0(new FileEvent(this, fo), false);
                }
            }
        } finally {
            getFileSystem().finishAtomicAction();
        }

        return fo;
    }

    /* Create new data file in this folder with the specified name. Fires
    * <code>fileCreated</code> event.
    *
    * @param name the name of data object to create (should not contain a period)
    * @param ext the extension of the file (or <code>null</code> or <code>""</code>)
    *
    * @return the new data file object
    * @exception IOException if the file cannot be created (e.g. already exists)
    */
    public FileObject createData(String name, String ext)
    throws IOException {
        MultiFileObject fo;

        try {
            getFileSystem().beginAtomicAction();

            synchronized (this) {
                MultiFileSystem fs = getMultiFileSystem();

                if (fs.isReadOnly()) {
                    throw new FSException(NbBundle.getMessage(MultiFileObject.class, "EXC_FSisRO", fs.getDisplayName()));
                }

                if (isReadOnly()) {
                    throw new FSException(NbBundle.getMessage(MultiFileObject.class, "EXC_FisRO", name, fs.getDisplayName()));
                }

                String n = "".equals(ext) ? name : (name + EXT_SEP + ext); // NOI18N

                if (!isFolder()) {
                    throw new FSException(NbBundle.getMessage(MultiFileObject.class, "EXC_FoNotFolder", n, getPath(), fs.getDisplayName()));
                }

                if (this.getFileObject(name, ext) != null) {
                    throw new FSException(NbBundle.getMessage(MultiFileObject.class, "EXC_DataAlreadyExist", n, fs.getDisplayName(), getPath()));
                }

                String fullName = getPath() + PATH_SEP + n;

                FileSystem simple = fs.createWritableOn(fullName);

                // create
                FileUtil.createData(root(simple), fullName);

                // try to unmask if necessary
                getMultiFileSystem().unmaskFileOnAll(simple, fullName);

                /** [PENDING] expected rename of some refresh method */
                //internalRefresh (n, null, true, false,null);
                refresh(n, null, true, false);

                fo = getMultiChild(n);

                if (fo == null) {
                    // system error
                    throw new FileStateInvalidException(NbBundle.getMessage(
                            MultiFileObject.class,
                            "EXC_ApplicationCreateErrorMulti", //NOI18N
                            getPath(), n, simple,
                            simple.getRoot().getFileObject(fullName)));
                }

                if (hasListeners()) {
                    fileCreated0(new FileEvent(this, fo), true);
                }
            }
        } finally {
            getFileSystem().finishAtomicAction();
        }

        return fo;
    }

    /* Renames this file (or folder).
    * Both the new basename and new extension should be specified.
    * <p>
    * Note that using this call, it is currently only possible to rename <em>within</em>
    * a parent folder, and not to do moves <em>across</em> folders.
    * Conversely, implementing file systems need only implement "simple" renames.
    * If you wish to move a file across folders, you should call {@link FileUtil#moveFile}.
    * @param lock File must be locked before renaming.
    * @param name new basename of file
    * @param ext new extension of file (ignored for folders)
    */
    public void rename(FileLock lock, String name, String ext)
    throws IOException {
        MultiFileSystem fs = getMultiFileSystem();

        if (parent == null) {
            throw new FSException(NbBundle.getMessage(MultiFileObject.class, "EXC_CannotRenameRoot", fs.getDisplayName()));
        }

        try {
            getFileSystem().beginAtomicAction();

            synchronized (parent) {
                // synchronize on your folder
                MfLock l = testLock(lock);

                String newFullName = parent.getPath() + PATH_SEP + name;

                if (isData() && ext != null && ext.trim().length() > 0) {
                    newFullName += (EXT_SEP + ext);
                }

                String oldFullName = getPath();

                if (isReadOnly()) {
                    throw new FSException(NbBundle.getMessage(MultiFileObject.class, "EXC_CannotRenameFromTo",
                            getPath(), getMultiFileSystem().getDisplayName(), newFullName));
                }

                if (getFileSystem().isReadOnly()) {
                    throw new FSException(NbBundle.getMessage(MultiFileObject.class, "EXC_FSisRO", getMultiFileSystem().getDisplayName()));
                }

                String on = getName();
                String oe = getExt();

                //!!!      getMultiFileSystem ().change.rename (oldFullName, newFullName);
                FileSystem single = fs.createWritableOnForRename(oldFullName, newFullName);

                if (single == leader.getFileSystem()) {
                    // delete the file if we can on the selected
                    // system
                    leader.rename(l.findLock(leader), name, ext);
                    getMultiFileSystem().unmaskFileOnAll(single, newFullName);
                    copyContent(this, leader);
                } else {
                    // rename file that is on different file system
                    // means to copy it
                    FileObject previousLeader = leader;

                    if (isData()) {
                        // data
                        FileObject folder = FileUtil.createFolder(root(single), getParent().getPath());
                        leader = leader.copy(folder, name, ext);
                        copyAttrs(this, leader);
                    } else {
                        // folder
                        FileObject fo = FileUtil.createFolder(root(single), newFullName);
                        copyContent(this, fo);

                        leader = fo;
                        this.name = name; // must be done before update                        
                        update();
                    }

                    // releases lock for previousLeader and aquiares 
                    // new for leader
                    l.changeLocks(previousLeader, leader);
                }

                if (getMultiFileSystem().delegates(oldFullName).hasMoreElements()) {
                    // if there is older version of the file
                    // then we have to mask it
                    getMultiFileSystem().maskFile(single, oldFullName);
                    updateFoldersLock(getParent());
                }

                if (isData()) {
                    name = name + EXT_SEP + ext;
                }

                String oldName = this.name;
                this.name = name;

                /*
                      System.out.println ("Resulting file is: " + getPath ());
                      System.out.println ("Bedw      file is: " + newFullName);
                      System.out.println ("Name: " + name);
                      System.out.println ("Old : " + oldName);
                */

                if (parent instanceof AbstractFolder) {
                    /** [PENDING] expected to delete*/
                    ((AbstractFolder) parent).refresh(name, oldName);
                }

                //!!!      getMultiFileSystem ().attr.renameAttributes (oldFullName, newFullName);
                if (hasAtLeastOneListeners()) {
                    fileRenamed0(new FileRenameEvent(this, on, oe));
                }
            }
        } finally {
            getFileSystem().finishAtomicAction();
        }
    }

    /* Delete this file. If the file is a folder and it is not empty then
    * all of its contents are also recursively deleted.
    *
    * @param lock the lock obtained by a call to {@link #lock}
    * @exception IOException if the file could not be deleted
    */
    void handleDelete(FileLock lock) throws IOException {
        if (parent == null) {
            throw new FSException(NbBundle.getMessage(MultiFileObject.class, "EXC_CannotDeleteRoot", getMultiFileSystem().getDisplayName()));
        }

        MultiFileSystem fs = getMultiFileSystem();

        try {
            getFileSystem().beginAtomicAction();

            synchronized (parent) {
                String fullName = getPath();
                FileSystem single = fs.createWritableOn(fullName);

                if (needsMask(lock, true)) {
                    getMultiFileSystem().maskFile(single, fullName);
                    updateFoldersLock(getParent());
                }

                String n = name;
                validFlag = false;

                if (parent instanceof AbstractFolder) {
                    /** [PENDING] expected rename of some refresh method */
                    //parent.internalRefresh (null, n, true, false, null);
                    ((AbstractFolder) parent).refresh(null, n, true, false);
                }

                if (hasAtLeastOneListeners()) {
                    fileDeleted0(new FileEvent(this));
                }
            }
        } finally {
            getFileSystem().finishAtomicAction();
        }
    }

    //
    // Transfer
    //

    /** Copies this file. This allows the filesystem to perform any additional
    * operation associated with the copy. But the default implementation is simple
    * copy of the file and its attributes
    *
    * @param target target folder to move this file to
    * @param name new basename of file
    * @param ext new extension of file (ignored for folders)
    * @return the newly created file object representing the moved file
    */
    public FileObject copy(FileObject target, String name, String ext)
    throws IOException {
        if (FileUtil.isParentOf(this, target)) {
            throw new FSException(NbBundle.getMessage(FileObject.class, "EXC_CopyChild", this, target)); // NOI18N
        }
        return leader.copy(target, name, ext);
    }

    /** Moves this file. This allows the filesystem to perform any additional
    * operation associated with the move. But the default implementation is encapsulated
    * as copy and delete.
    *
    * @param lock File must be locked before renaming.
    * @param target target folder to move this file to
    * @param name new basename of file
    * @param ext new extension of file (ignored for folders)
    * @return the newly created file object representing the moved file
    */
    public FileObject move(FileLock lock, FileObject target, String name, String ext)
    throws IOException {
        if (FileUtil.isParentOf(this, target)) {
            throw new FSException(NbBundle.getMessage(FileObject.class, "EXC_MoveChild", this, target)); // NOI18N
        }
        MultiFileSystem fs = getMultiFileSystem();

        try {
            fs.beginAtomicAction();

            if (parent == null) {
                throw new FSException(NbBundle.getMessage(MultiFileObject.class, "EXC_CannotDeleteRoot", fs.getDisplayName()));
            }

            MfLock lck = testLock(lock);
            FileLock l = lck.findLock(leader);

            FileSystem simple = fs.createWritableOn(getPath());

            if (fs.isReadOnly()) {
                throw new FSException(NbBundle.getMessage(MultiFileObject.class, "EXC_FSisRO", fs.getDisplayName()));
            }

            if ((l == null) && (leader.getFileSystem() != simple)) {
                leader = writable(true);
                l = lck.findLock(leader);
            }

            if (needsMask(lock, false)) {
                getMultiFileSystem().maskFile(simple, getPath());
                updateFoldersLock(getParent());
            }
            FileObject ret = leader.move(l, target, name, ext);
            FileObjectLkp.reassign(this, ret);
            return ret;
        } finally {
            fs.finishAtomicAction();
        }
    }

    /* Refresh the contents of a folder. Rescans the list of children names.
    */
    public final void refresh(boolean expected) {
        if (!isInitialized() && isFolder()) {
            return;
        }

        Enumeration<FileObject> en = delegates();

        while (en.hasMoreElements()) {
            FileObject fo = en.nextElement();
            fo.refresh(expected);
        }

        super.refresh(expected);
    }

    //
    // Listeners
    //

    /** Fired when a new folder is created. This action can only be
     * listened to in folders containing the created folder up to the root of
     * file system.
     *
     * @param fe the event describing context where action has taken place
     */
    public void fileFolderCreated(FileEvent fe) {
        /*One of underlaing layers notifies that new folder was created.
         And this folder may have any other childern anywhere deep in hierarchy
         and then must be updated and refreshed deep down*/
        updateAll();
    }

    /** Fired when a new file is created. This action can only be
     * listened in folders containing the created file up to the root of
     * file system.
     *
     * @param fe the event describing context where action has taken place
     */
    public void fileDataCreated(FileEvent fe) {
        refreshAfterEvent(fe);
    }

    /** Fired when a file is changed.
     * @param fe the event describing context where action has taken place
     */
    public void fileChanged(FileEvent fe) {
        FileObject changedFile = this;

        if (fe.getSource().equals(leader) && hasAtLeastOneListeners() && !fe.firedFrom(markAtomicAction)) {
            /**There should not dissapear information about source and changed file*/
            if (!fe.getFile().equals(fe.getSource())) {
                changedFile = getFileObject(fe.getFile().getName(), fe.getFile().getExt());
            }

            /**fileChanged1 - should not fire event for this.getParent ().
             * I think that already bottom layer forked event.*/
            /** [PENDING] fix of #16926, #16895. But there should be investigated
             *  why this MFO doesn`t know about child ?*/
            if (changedFile != null) {
                fileChanged1(new FileEvent(this, changedFile, fe.getTime()));
            }
        }
    }

    /** Fired when a file is deleted.
     * @param fe the event describing context where action has taken place
     */
    public void fileDeleted(FileEvent fe) {
        if (fe.getFile().isFolder()) {
            updateAll();
        } else {
            refreshAfterEvent(fe);
        }
    }

    /** Fired when a file is renamed.
     * @param fe the event describing context where action has taken place
     *           and the original name and extension.
     */
    public void fileRenamed(FileRenameEvent fe) {
        updateAll();
    }

    /** Fired when a file attribute is changed.
     * @param fe the event describing context where action has taken place,
     *           the name of attribute and the old and new values.
     */
    public void fileAttributeChanged(FileAttributeEvent fe) {
        // [PENDING] this is not at all sufficient to notify every change in attributes.
        // One, parent dirs of front filesystems can now hold attributes for missing
        // files. Two, non-leader files can have attributes too which are merged in.
        // In principle all files/folders whose path is a prefix of this path on all
        // contained filesystems should be listened to for attribute change events.
        if (!hasAtLeastOneListeners() || (leader == null)) {
            return;
        }

        /** If change is not fired from leader then leader may mask this attribute
         *  and then event should not be fired */
        if (!fe.getFile().equals(leader) && (fe.getName() != null) && (leader.getAttribute(fe.getName()) != null)) {
            return;
        }

        /** If change is not fired from leader then another delegate may mask this attribute
         *  and then event should not be fired. */
        if (
            !fe.getFile().equals(leader) && (fe.getNewValue() != null) && (fe.getName() != null) &&
                !fe.getNewValue().equals(getAttribute(fe.getName()))
        ) {
            return;
        }
        final FileAttributeEvent ev = new FileAttributeEvent(this, fe.getName(), fe.getOldValue(), fe.getNewValue());
        try {
            ev.inheritPostNotify(fe);
            fileAttributeChanged0(ev);
        } finally {
            ev.setPostNotify(null);
        }
    }

    /** Copies content of one folder into another.
    * @param source source folder
    * @param target target folder
    * @exception IOException if it fails
    */
    private static void copyContent(FileObject source, FileObject target)
    throws IOException {
        FileObject[] srcArr = source.getChildren();

        copyAttrs(source, target); //added

        for (int i = 0; i < srcArr.length; i++) {
            FileObject child = srcArr[i];

            if (MultiFileSystem.isMaskFile(child)) {
                continue;
            }

            if (target.getFileObject(child.getName(), child.getExt()) == null) {
                if (child.isData()) {
                    FileObject fo = FileUtil.copyFile(child, target, child.getName(), child.getExt());

                    if (fo != null) {
                        copyAttrs(child, fo);
                    }
                } else {
                    FileObject targetChild = target.createFolder(child.getName());
                    copyContent(child, targetChild);
                }
            }
        }
    }

    /** Copies attributes of one FileObject into another.
    * @param source source folder or file
    * @param target target folder or file
    * @exception IOException if it fails
    */
    private static void copyAttrs(FileObject source, FileObject target) {
        Enumeration<String> en = source.getAttributes();

        while (en.hasMoreElements()) {
            String key = en.nextElement();
            Object value = source.getAttribute(key);

            try {
                target.setAttribute(key, value);
            } catch (IOException ie) {
            }
        }
    }

    /**
     * auxiliary method that returns true if mask is needed and deletes all delegates
     * on writable layers if deleteDelegates is true.
     * @param lock
     * @param deleteDelegates if true all delegates on writable layers will be deleted
     * @throws IOException is thrown  if lock is not valid.
     * @return  true if mask is necessary*/
    private boolean needsMask(FileLock lock, boolean deleteDelegates)
    throws IOException {
        MfLock lck = testLock(lock);
        Enumeration<FileObject> e = getMultiFileSystem().delegates(getPath());
        boolean needsMask = false;

        while (e.hasMoreElements()) {
            FileObject fo = e.nextElement();
            FileLock lockForFo = lck.findLock(fo);

            if (lockForFo == null) {
                // we will need to create mask
                needsMask = true;
            } else {
                if (deleteDelegates) {
                    fo.delete(lockForFo);
                }
            }
        }

        return needsMask;
    }

    /** Finds a root for given file system. It also counts with
    * redefined method findResourceOn.
    *
    * @param fs the filesystem to seach on
    * @return the root on the fs
    */
    private FileObject root(FileSystem fs) {
        return getMultiFileSystem().findResourceOn(fs, ""); // NOI18N
    }

    final FileObject getLeader() {
        return leader;
    }

    /** Special value used to indicate null masking of an attribute.
     * The level is zero in simple cases; incremented when one MFS asks
     * another to store a VoidValue.
     */
    static final class VoidValue implements Externalizable {
        // Externalizable:
        private static final long serialVersionUID = -2743645909916238684L;
        int level;

        VoidValue(int level) {
            this.level = level;
        }

        public VoidValue() {
        }

        public String toString() {
            return "org.openide.filesystems.MultiFileObject.VoidValue#" + level; // NOI18N
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(level);
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            level = in.readInt();
        }
    }

    /** Implementation of lock for abstract files.
    */
    private class MfLock extends FileLock {
        /** lock for all files (map from FileObject to FileLock) */
        private Map<FileObject, FileLock> map = new HashMap<FileObject, FileLock>(11);

        /**
        * @param leader leader file object
        * @param delegates all delegates for this file object
        * @param systems a set of filesystems we should create lock on
        * @exception IOException if the lock cannot be obtained
        */
        public MfLock(FileObject leader, Enumeration<FileObject> delegates, Set<? extends FileSystem> systems)
        throws IOException {
            while (delegates.hasMoreElements()) {
                FileObject fo = delegates.nextElement();

                if (systems.contains(fo.getFileSystem())) {
                    FileLock l = fo.lock();
                    map.put(fo, l);
                }
            }

            /* JST: Commented out because cause problems when locking a file that
                    is only of filesystems that are readonly (for example when
                    file is only on shared installation and locks are allowed only
                    on local => then there is nothing to lock.

                      if (map.isEmpty()) {
                          // trouble, the filesystem returned wrong set of systems to lock
                          // on => warn about possibly wrong implementation
                          // to correct the problem override appropriatelly createLocksOn
                          // method of MultiFileSystem
                          throw new IOException ("Writable file is not on filesystem from createLocksOn"); // NOI18N
                          }
            */
        }

        /** Finds lock for given file object.
        * @param fo one of delegates
        * @return the lock or null
        */
        public FileLock findLock(FileObject fo) {
            return map.get(fo);
        }

        /** Adds another lock into this lock.
        * @param fo file object to keep the lock for
        * @exception IOException if the lock cannot be obtained
        */
        public void addLock(FileObject fo) throws IOException {
            map.put(fo, fo.lock());
        }

        /** Releases lock for old file object and
        * takes new one from newFo
        */
        public void changeLocks(FileObject old, FileObject n)
        throws IOException {
            FileLock l = map.remove(old);

            if (l != null) {
                l.releaseLock();
            }

            addLock(n);
        }

        public void releaseLock() {
            if (this.isValid()) {
                super.releaseLock();
                releaseLockForDelegates();

                if (getCurrentMfLock() == this) {
                    // clears the reference to this lock from the file object
                    MultiFileObject.this.lock = null;
                }
            }
        }

        private FileLock getCurrentMfLock() {
            FileLock currentLock = null;
            ;

            if (lock != null) {
                currentLock = (FileLock) lock.get();
            }

            return currentLock;
        }

        private void releaseLockForDelegates() {
            for (FileLock l : map.values()) {
                l.releaseLock();
            }

            map.clear();
        }

        // for better debugging
        public String toString() {
            return super.toString() + " for " + MultiFileObject.this + " valid=" + isValid(); // NOI18N
        }
    }    
    // MfLock
    
    private static class AttributeCache {
        private FileObject delegate;
        private String attribName = ""; // NOI18N        
        
        private void free() {
            delegate = null;
            attribName = ""; // NOI18N            
        }
        
        private void setDelegate(FileObject delegate) {
            this.delegate = delegate;            
        }
        
        private void setAttributeName(String attribName) {
            this.attribName = attribName;
        }
                        
        private FileObject getDelegate() {
            return delegate;
        }
        
        private String getAttributeName() {
            return attribName;
        }
    }
    
    /**
     * Revealed FileObject represents a FileObject which has been either overriden by
     * the writable layer, or masked out by the layer. It delegates to the original object,
     * and its Callable mixing interface allows to revert the change by removing the 
     * override file.
     */
    private final class RevealedFileObject extends FileObject implements Callable<FileObject> {
        /**
         * The masking file. It may be either _hidden file, or a replacing
         * file on the writable layer
         */
        private final FileObject  maskFile;
        
        /**
         * Original file on lower layer
         */
        private final FileObject  delegate;

        public RevealedFileObject(FileObject parent, FileObject delegate) {
            this.maskFile = parent;
            this.delegate = delegate;
        }

        @Override
        public Object getAttribute(String attrName) {
            return delegate.getAttribute(attrName);
        }

        /**
         * Return the revived file from the MFS
         * @return the revived file, if the operation succeeds.
         * @throws IOException from the deletion of the masking file.
         */
        @Override
        public FileObject call() throws IOException {
            if (maskFile.isValid()) {
                maskFile.delete();
            }
            return MultiFileObject.this.getFileObject(maskFile.getNameExt());
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public String getExt() {
            return delegate.getExt();
        }

        @Override
        public void rename(FileLock lock, String name, String ext) throws IOException {
            throw new IOException("Unsupported oepration"); // NOI18N
        }

        @Override
        public FileObject copy(FileObject target, String name, String ext) throws IOException {
            return delegate.copy(target, name, ext);
        }

        @Override
        public FileObject move(FileLock lock, FileObject target, String name, String ext) throws IOException {
            throw new IOException("Unsupported oepration"); // NOI18N
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        @Override
        public String getPath() {
            return delegate.getPath();
        }

        @Override
        public String getPackageNameExt(char separatorChar, char extSepChar) {
            return delegate.getPackageNameExt(separatorChar, extSepChar);
        }

        @Override
        public String getPackageName(char separatorChar) {
            return delegate.getPackageName(separatorChar);
        }

        @Override
        public String getNameExt() {
            return delegate.getNameExt();
        }

        @Override
        public FileSystem getFileSystem() throws FileStateInvalidException {
            return MultiFileObject.this.getFileSystem();
        }

        @Override
        public FileObject getParent() {
            return MultiFileObject.this;
        }

        @Override
        public boolean isFolder() {
            return delegate.isFolder();
        }

        @Override
        public Date lastModified() {
            return delegate.lastModified();
        }

        @Override
        public boolean isRoot() {
            return delegate.isRoot();
        }

        @Override
        public boolean isData() {
            return delegate.isData();
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public boolean existsExt(String ext) {
            return delegate.existsExt(ext);
        }

        @Override
        public void delete(FileLock lock) throws IOException {
            throw new IOException("Unsupported oepration"); //NOI18N
        }

        @Override
        public Lookup getLookup() {
            return delegate.getLookup();
        }

        @Override
        public void setAttribute(String attrName, Object value) throws IOException {
            throw new IOException("Unsupported oepration"); //NOI18N
        }

        @Override
        public Enumeration<String> getAttributes() {
            return delegate.getAttributes();
        }

        @Override
        boolean isHasExtOverride() {
            return delegate.isHasExtOverride();
        }

        @Override
        boolean hasExtOverride(String ext) {
            return delegate.hasExtOverride(ext);
        }

        @Override
        public void addFileChangeListener(FileChangeListener fcl) {
            delegate.addFileChangeListener(fcl);
        }

        @Override
        public void removeFileChangeListener(FileChangeListener fcl) {
            delegate.removeFileChangeListener(fcl);
        }

        @Override
        public void addRecursiveListener(FileChangeListener fcl) {
            delegate.addRecursiveListener(fcl);
        }

        @Override
        public void removeRecursiveListener(FileChangeListener fcl) {
            delegate.removeRecursiveListener(fcl);
        }

        @Override
        protected void fireFileDataCreatedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        }

        @Override
        protected void fireFileFolderCreatedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        }

        @Override
        protected void fireFileChangedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        }

        @Override
        protected void fireFileDeletedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        }

        @Override
        protected void fireFileAttributeChangedEvent(Enumeration<FileChangeListener> en, FileAttributeEvent fe) {
        }

        @Override
        protected void fireFileRenamedEvent(Enumeration<FileChangeListener> en, FileRenameEvent fe) {
        }

        @Override
        public String getMIMEType() {
            return delegate.getMIMEType();
        }

        @Override
        public String getMIMEType(String... withinMIMETypes) {
            return delegate.getMIMEType(withinMIMETypes);
        }

        @Override
        public long getSize() {
            return delegate.getSize();
        }

        @Override
        public InputStream getInputStream() throws FileNotFoundException {
            return delegate.getInputStream();
        }

        @Override
        public byte[] asBytes() throws IOException {
            return delegate.asBytes();
        }

        @Override
        public String asText(String encoding) throws IOException {
            return delegate.asText(encoding);
        }

        @Override
        public String asText() throws IOException {
            return delegate.asText();
        }

        @Override
        public List<String> asLines() throws IOException {
            return delegate.asLines();
        }

        @Override
        public List<String> asLines(String encoding) throws IOException {
            return delegate.asLines(encoding);
        }

        @Override
        public OutputStream getOutputStream(FileLock lock) throws IOException {
            throw new IOException("Unsupported oepration"); //NOI18N
        }

        @Override
        public FileLock lock() throws IOException {
            throw new IOException("Unsupported oepration"); //NOI18N
        }

        @Override
        public boolean isLocked() {
            return delegate.isLocked();
        }

        @Override
        public void setImportant(boolean b) {
        }

        @Override
        public FileObject[] getChildren() {
            return new FileObject[0];
        }

        @Override
        public Enumeration<? extends FileObject> getChildren(boolean rec) {
            return Enumerations.empty();
        }

        @Override
        public Enumeration<? extends FileObject> getFolders(boolean rec) {
            return Enumerations.empty();
        }

        @Override
        public Enumeration<? extends FileObject> getData(boolean rec) {
            return Enumerations.empty();
        }

        @Override
        public FileObject getFileObject(String name, String ext) {
            if (maskFile.isFolder()) {
                return maskFile.getFileObject(name, ext);
            } else {
                return null;
            }
        }

        @Override
        public FileObject getFileObject(String relativePath) {
            if (maskFile.isFolder()) {
                return maskFile.getFileObject(relativePath);
            } else {
                return null;
            }
        }

        @Override
        public FileObject createFolder(String name) throws IOException {
            throw new IOException("Unsupported oepration"); //NOI18N
        }

        @Override
        public FileObject createData(String name, String ext) throws IOException {
            throw new IOException("Unsupported oepration"); //NOI18N
        }

        @Override
        public FileObject createData(String name) throws IOException {
            throw new IOException("Unsupported oepration"); //NOI18N
        }

        @Override
        public OutputStream createAndOpen(String name) throws IOException {
            throw new IOException("Unsupported oepration"); //NOI18N
        }

        @Override
        public boolean isReadOnly() {
            return delegate.isReadOnly();
        }

        @Override
        public boolean canWrite() {
            return false;
        }

        @Override
        public boolean canRead() {
            return delegate.canRead();
        }

        @Override
        public void refresh(boolean expected) {
            delegate.refresh(expected);
        }

        @Override
        public void refresh() {
            delegate.refresh();
        }

        @Override
        public boolean isVirtual() {
            return delegate.isVirtual();
        }
        
        public int hashCode() {
            return 0xaa ^ delegate.hashCode() << 3;
        }
        
        public boolean equals(Object o) {
            if (!(o instanceof RevealedFileObject)) {
                return false;
            }
            RevealedFileObject other = (RevealedFileObject)o;
            return other.delegate.equals(delegate) && other.maskFile.equals(maskFile);
        }
    }
    
    /**
     * The returned collection contains hidden or removed FileObjects, from
     * the lower layers. The returned FileObjects are 'special' in that DataObjects
     * cannot be created from them, and they cannot be changed. All mutation methods
     * will throw IOException - unsupported operation.
     */
    private Collection<FileObject> collectRevealedFiles() {
        String path = getPath();
        FileSystem[] systems = getMultiFileSystem().getDelegates();
        FileSystem writable = getMultiFileSystem().writableLayer(path);
        
        if (writable == null) {
            return Collections.emptyList();
        }
        FileObject writableFolder = writable.findResource(path);
        // no overrides as the containing folder does not exist on the writable fs.
        if (writableFolder == null) {
            return Collections.emptyList();
        }
        FileObject[] ch = writableFolder.getChildren();
        int sl = systems.length;
        
        Collection<FileObject> result = null;
        String parentPath = writableFolder.getPath();
        for (FileObject mask : ch) {
            String fn = mask.getNameExt();
            if (fn.endsWith(MultiFileSystem.MASK)) {
                fn = fn.substring(0, fn.length() - MultiFileSystem.MASK.length());
            }
            String p = parentPath + "/" + fn; // NOI18N
            for (int i = 0; i < sl; i++) {
                if (writable == systems[i]) {
                    continue;
                }
                FileObject hiddenFo = systems[i].findResource(p);
                if (hiddenFo != null) {
                    if (result == null) {
                        result = new ArrayList<FileObject>(ch.length);
                    }

                    result.add(new RevealedFileObject(mask, hiddenFo));
                    break;
                }
            }
        }
        if (result == null) {
            return Collections.emptyList();
        } else {
            return result;
        }
    }
}
