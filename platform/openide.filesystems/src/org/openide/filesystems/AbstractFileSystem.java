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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.StringTokenizer;
import org.openide.util.NbCollections;

/**
 * This convenience implementation does much of the hard work of
 * <code>FileSystem</code> and is generally more pleasant to create
 * subclasses of.
 *
 * <p>It caches information about the filesystem in memory and periodically refreshes its content. Many other
 * operations are performed in a safer manner so as to reuse experience of NetBeans developers; should be
 * substantially simpler to subclass than {@link FileSystem} itself.
 *
 * <p>Besides what is mentioned here, the <code>AbstractFileSystem</code> subclass be configurable as a JavaBean
 * (e.g. server address, prefix, whatever it needs).
 *
 * <p>First of all, you need not separately implement a {@link FileObject} - this is taken care of for you. You
 * need only provide some information about how essential actions should be carried out.
 *
 * <p>You should implement {@link #getDisplayName()} to set the display name.
 *
 * <p>You may cause the filesystem to automatically refresh itself at periodic intervals, in case it is not
 * possible to be notified of changes directly. To do so, call {@link #setRefreshTime(int)}, e.g. from the
 * constructor.
 *
 * <p>{@link #refreshRoot()} must be called if the filesystem supports changing of its root directory (as the
 * local filesystem does, for example).
 *
 * <p>Most of the meat of the implementation is provided by setting appropriate values for four protected
 * variables, which should be initialized in the constructor; each represents an implementation of a separate
 * interface handling one aspect of the filesystem.
 * <p>
 * <strong>List member</strong>
 * </p>
 * The variable {@link #list} should contain an implementation of {@link AbstractFileSystem.List}. This only
 * specifies a way of accessing the list of children in a folder.
 * <p>
 * <strong>Info member</strong>
 * <p>
 * {@link #info} contains an {@link AbstractFileSystem.Info} which provides basic file statistics.
 *
 * <p>It also specifies the raw implementation of two basic types of actions: getting input and output streams
 * for the file; and locking and unlocking it physically (optional and not to be confused with locking within
 * NetBeans).
 * <p>
 * <strong>Change member</strong>
 * <p>
 * {@link #change} is an {@link AbstractFileSystem.Change} which provides the interface to create new folders
 * and files; delete them; and rename them (within the same directory).
 * <p>
 * <strong>Attribute member</strong>
 * <p>
 * {@link #attr} is an {@link AbstractFileSystem.Attr} allowing NetBeans to read and write serializable
 * attributes (meta-information) to be associated with the file. Such attributes are not much used any more, but
 * they are occasionally.
 *
 * <p>There is a default implementation in {@link DefaultAttributes} which stores attributes in a file called
 * <code>.nbattrs</code> in each folder for which a file has some attributes, using XML augmented by Java
 * serialization, though the regular filesystems in NetBeans now instead store all attributes globally in
 * <code>$userdir/var/attributes.xml</code>. If you do use <code>DefaultAttributes</code>, use it not only for
 * {@link #attr} but also for {@link #list} (passing in a separate private {@link AbstractFileSystem.List}
 * implementation to its constructor) in order to filter the <code>.nbattrs</code> files from view.
 */
public abstract class AbstractFileSystem extends FileSystem {
    /** generated Serialized Version UID */
    private static final long serialVersionUID = -3345098214331282438L;

    /** cached last value of Enumeration which holds resource name (enumeration like StringTokenizer)*/
    private static transient PathElements lastEnum;

    /** root object for the filesystem */
    private transient AbstractFileObject root;

    /** refresher */
    private transient RefreshRequest refresher;

    /** Provider of hierarchy of files. */
    protected List list;

    /** Methods for modification of files. */
    protected Change change;

    /** Methods for moving of files. This field can be left null if the filesystem
    * does not require special handling handling of FileObject.move and is satified
    * with the default implementation.
    */
    protected Transfer transfer;

    /** Methods for obtaining information about files. */
    protected Info info;

    /** Handling of attributes for files. */
    protected Attr attr;

    /**
     * Actually implements contract of FileSystem.refresh().
     */
    public void refresh(boolean expected) {
        for (FileObject fo : NbCollections.iterable(getAbstractRoot().existingSubFiles(true))) {
            fo.refresh(expected);
        }
    }

    /* Provides a name for the system that can be presented to the user.
    * @return user presentable name of the filesystem
    */
    public abstract String getDisplayName();

    /* Getter for root folder in the filesystem.
    *
    * @return root folder of whole filesystem
    */
    public FileObject getRoot() {
        return getAbstractRoot();
    }

    /* Finds file when its resource name is given.
    * The name has the usual format for the {@link ClassLoader#getResource(String)}
    * method. So it may consist of "package1/package2/filename.ext".
    * If there is no package, it may consist only of "filename.ext".
    *
    * @param name resource name
    *
    * @return FileObject that represents file with given name or
    *   <CODE>null</CODE> if the file does not exist
    */
    public FileObject findResource(String name) {
        if (name.length() == 0) {
            return getAbstractRoot();
        }

        /**Next piece of code is preformance enhancement; lastEnum = last value cache;
         PathElements is StringTokenizer wrapper that caches individual elements*/
        PathElements local = lastEnum;

        if ((local == null) || !local.getOriginalName().equals(name)) {
            local = new PathElements(name);
            lastEnum = local;
        }

        return getAbstractRoot().find(local.getEnumeration());
    }

    /** Creates Reference. In FileSystem, which subclasses AbstractFileSystem, you can overload method
    * createReference(FileObject fo) to achieve another type of Reference (weak, strong etc.)
    * @param fo is FileObject. It`s reference yourequire to get.
    * @return Reference to FileObject
    */
    protected <T extends FileObject> Reference<T> createReference(T fo) {
        return (new WeakReference<T>(fo));
    }

    /** This method allows to find Reference to resourceName
    * @param resourceName is name of resource
    * @return Reference to resourceName
    */
    protected final Reference<? extends FileObject> findReference(String resourceName) {
        if (resourceName.length() == 0) {
            return null;
        } else {
            Enumeration<String> tok = NbCollections.checkedEnumerationByFilter(new StringTokenizer(resourceName, "/"), String.class, true); // NOI18N

            return getAbstractRoot().findRefIfExists(tok);
        }
    }

    /*
    * @return true if RefreshAction should be enabled
    */
    boolean isEnabledRefreshFolder() {
        return (refresher != null);
    }

    /** Set the number of milliseconds between automatic
    * refreshes of the directory structure.
    *
    * @param ms number of milliseconds between two refreshes; if <code>&lt;= 0</code> then refreshing is disabled
    */
    protected final synchronized void setRefreshTime(int ms) {
        if (refresher != null) {
            refresher.stop();
        }

        if ((ms <= 0) || (System.getProperty("netbeans.debug.heap") != null)) {
            refresher = null;
        } else {
            refresher = new RefreshRequest(this, ms);
        }
    }

    /** Get the number of milliseconds between automatic
    * refreshes of the directory structure.
    * By default, automatic refreshing is disabled.
    * @return the number of milliseconds, or <code>0</code> if refreshing is disabled
    */
    protected final int getRefreshTime() {
        RefreshRequest r = refresher;

        return (r == null) ? 0 : r.getRefreshTime();
    }

    /** Instruct the filesystem
    * that the root should change.
    * A fresh root is created. Subclasses that support root changes should use this.
    *
    * @return the new root
    */
    final synchronized AbstractFileObject refreshRootImpl() {
        if (root != null) {
            root.validFlag = false;
        }

        root = createFileObject(null, ""); // NOI18N

        return root;
    }

    /** Instruct the filesystem
    * that the root should change.
    * A fresh root is created. Subclasses that support root changes should use this.
    *
    * @return the new root
    */
    protected final FileObject refreshRoot() {
        return refreshRootImpl();
    }

    /** Allows subclasses to fire that a change occured in a
    * file or folder. The change can be "expected" when it is
    * a result of an user action and the user knows that such
    * change should occur.
    *
    * @param name resource name of the file where the change occured
    * @param expected true if the user initiated change and expects it
    */
    protected final void refreshResource(String name, boolean expected) {
        AbstractFileObject fo = (AbstractFileObject) findResourceIfExists(name);

        if (fo != null) {
            // refresh and behave like the changes is expected
            fo.refresh(null, null, true, expected);
        }
    }

    /**
     * For the FileObject specified as parameter, returns the recursive enumeration
     * of existing children fileobjects (both folders and data). It doesn't create
     * any new FileObject instances. Direct children are at the begining of the enumeration.
     * @param fo the starting point for the recursive fileobject search
     * @return enumeration of currently existing fileobjects.
     */
    protected final Enumeration<? extends FileObject> existingFileObjects(FileObject fo) {
        return existingFileObjects((AbstractFolder) fo);
    }

    final Enumeration<? extends FileObject> existingFileObjects(AbstractFolder fo) {
        class OnlyValidAndDeep implements org.openide.util.Enumerations.Processor<Reference<AbstractFolder>,FileObject> {
            public FileObject process(Reference<AbstractFolder> obj, Collection<Reference<AbstractFolder>> toAdd) {
                AbstractFolder file = obj.get();

                if (file != null) {
                    AbstractFolder[] arr = file.subfiles();

                    // make the array weak
                    for (int i = 0; i < arr.length; i++) {
                        toAdd.add(new WeakReference<AbstractFolder>(arr[i]));
                    }

                    return file.isValid() ? file : null;
                }

                return null;
            }
        }

        Reference<AbstractFolder> ref =  new WeakReference<AbstractFolder>(fo);
        Enumeration<Reference<AbstractFolder>> singleEn =  org.openide.util.Enumerations.<Reference<AbstractFolder>>singleton(ref);
        return org.openide.util.Enumerations.removeNulls(
            org.openide.util.Enumerations.queue(singleEn, new OnlyValidAndDeep())
        );
    }


    /**
    * @return if value of lastModified should be cached
    */
    boolean isLastModifiedCacheEnabled() {
        return true;
    }

    /* Finds file when its resource name is given.
    * The name has the usual format for the {@link ClassLoader#getResource(String)}
    * method. So it may consist of "package1/package2/filename.ext".
    * If there is no package, it may consist only of "filename.ext".
    *
    * @param name resource name
    *
    * @return FileObject that represents file with given name or
    *   <CODE>null</CODE> if the file does not exist
    */
    private FileObject findResourceIfExists(String name) {
        if (name.length() == 0) {
            return getAbstractRoot();
        } else {
            Enumeration<String> tok = NbCollections.checkedEnumerationByFilter(new StringTokenizer(name, "/"), String.class, true); // NOI18N

            return getAbstractRoot().findIfExists(tok);
        }
    }

    /** Hooking method to allow MultiFileSystem to be informed when a new
    * file object is created. This is the only method that creates AbstractFileObjects.
    *
    * @param parent parent object
    * @param name of the object
    */
    AbstractFileObject createFileObject(AbstractFileObject parent, String name) {
        return new AbstractFileObject(this, parent, name);
    }

    /**
     * Creates root object for the fs.
     */
    final AbstractFileObject getAbstractRoot() {
        synchronized (this) {
            if (root == null) {
                return refreshRootImpl();
            }
        }

        return root;
    }

    /** Writes the common fields and the state of refresher.
    */
    private void writeObject(ObjectOutputStream oos) throws IOException {
        ObjectOutputStream.PutField fields = oos.putFields();

        fields.put("change", change); // NOI18N
        fields.put("info", info); // NOI18N
        fields.put("attr", attr); // NOI18N
        fields.put("list", list); // NOI18N
        fields.put("transfer", transfer); // NOI18N
        oos.writeFields();

        oos.writeInt(getRefreshTime());
    }

    /** Reads common fields and state of refresher.
    */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = ois.readFields();

        Object o1 = readImpl("change", fields); // change // NOI18N
        Object o2 = readImpl("info", fields); // info // NOI18N
        Object o3 = readImpl("attr", fields); // attr // NOI18N
        Object o4 = readImpl("list", fields); // list // NOI18N
        Object o5 = readImpl("transfer", fields); // transfer // NOI18N

        change = (Change) o1;
        info = (Info) o2;
        attr = (Attr) o3;
        list = (List) o4;
        transfer = (Transfer) o5;

        setRefreshTime(ois.readInt());
    }

    //
    // Backward compatibility methods
    //

    /** Reads object from input stream, if it is
    * LocalFileSystem or JarFileSystem then replaces the object
    * by its Impl.
    */
    static Object readImpl(String name, ObjectInputStream.GetField fields)
    throws ClassNotFoundException, IOException {
        Object o = fields.get(name, null);

        if (o instanceof LocalFileSystem) {
            return new LocalFileSystem.Impl((LocalFileSystem) o);
        } else if (o instanceof JarFileSystem) {
            return new JarFileSystem.Impl((JarFileSystem) o);
        }

        return o;
    }

    /**
     * This method is called from AbstractFileObject.isVirtual. Tests if file
     * really exists or is missing. Some operation on it may be restricted if returns true.
     * @param name of the file
     * @return  true indicates that the file is missing.
     * @since 1.9
     */
    protected boolean checkVirtual(String name) {
        return false;
    }

    /** Tests if this file can be written to.
     * @param name resource name
     * @return true if this file can be written, false if not.
     * @since 3.31
     */
    protected boolean canWrite(String name) {
        AbstractFileObject afo = (AbstractFileObject) this.findResource(name);

        return (afo != null) ? afo.superCanWrite() : false;
    }

    /** Tests if this file can be read.
     * @param name resource name
     * @return true if this file can be read, false if not.
     * @since 3.31
     */
    protected boolean canRead(String name) {
        AbstractFileObject afo = (AbstractFileObject) this.findResource(name);

        return (afo != null) ? afo.superCanRead() : false;
    }

    /** Mark the file as being important or unimportant.
    * @param name the file to mark
    * @param important true indicates that file is important, false conversely
    * file is unimportant.
    * @since 1.9
    */
    protected void markImportant(String name, boolean important) {
        if (!important && (info != null)) {
            info.markUnimportant(name);
        }
    }

    /** Provides access to the hierarchy of resources.
    */
    public interface List extends Serializable {
        /** @deprecated Only public by accident. */
        @Deprecated
        /* public static final */ long serialVersionUID = -6242105832891012528L;

        /** Get a list of children files for a given folder.
        *
        * @param f the folder, by name; e.g. <code>top/next/afterthat</code>
        * @return a list of children of the folder, as <code>file.ext</code> (no path)
        *   the array can contain <code>null</code> values that will be ignored
        */
        public String[] children(String f);
    }

    /** Controls modification of files.
    */
    public interface Change extends Serializable {
        /** @deprecated Only public by accident. */
        @Deprecated
        /* public static final */ long serialVersionUID = -5841597109944924596L;

        /** Create new folder.
        * @param name full name of new folder, e.g. <code>topfolder/newfolder</code>
        * @throws IOException if the operation fails
        */
        public void createFolder(String name) throws IOException;

        /** Create new data file.
        *
        * @param name full name of the file, e.g. <code>path/from/root/filename.ext</code>
        *
        * @exception IOException if the file cannot be created (e.g. already exists)
        */
        public void createData(String name) throws IOException;

        /** Rename a file.
        *
        * @param oldName old name of the file; fully qualified
        * @param newName new name of the file; fully qualified
        * @throws IOException if it could not be renamed
        */
        public void rename(String oldName, String newName)
        throws IOException;

        /** Delete a file.
        *
        * @param name name of file; fully qualified
        * @exception IOException if the file could not be deleted
        */
        public void delete(String name) throws IOException;
    }

    /** Controls on moving of files. This is additional interface to
    * allow filesystem that require special handling of move to implement
    * it in different way then is the default one.
    */
    public interface Transfer extends Serializable {
        /** @deprecated Only public by accident. */
        @Deprecated
        /* public static final */ long serialVersionUID = -8945397853892302838L;

        /** Move a file.
        *
        * @param name of the file on current filesystem
        * @param target move implementation
        * @param targetName of target file
        * @exception IOException if the move fails
        * @return false if the method is not able to handle the request and
        *    default implementation should be used instead
        */
        public boolean move(String name, Transfer target, String targetName)
        throws IOException;

        /** Copy a file.
        *
        * @param name of the file on current filesystem
        * @param target target transfer implementation
        * @param targetName name of target file
        * @exception IOException if the copy fails
        * @return false if the method is not able to handle the request and
        *    default implementation should be used instead
        */
        public boolean copy(String name, Transfer target, String targetName)
        throws IOException;
    }

    /** Information about files.
    */
    public interface Info extends Serializable {
        /** @deprecated Only public by accident. */
        @Deprecated
        /* public static final */ long serialVersionUID = -2438286177948307985L;

        /**
        * Get last modification time.
        * @param name the file to test
        * @return the date of last modification
        */
        public Date lastModified(String name);

        /** Test if the file is a folder or contains data.
        * @param name name of the file
        * @return <code>true</code> if the file is folder, <code>false</code> if it is data
        */
        public boolean folder(String name);

        /** Test whether this file can be written to or not.
        * @param name the file to test
        * @return <CODE>true</CODE> if the file is read-only
        */
        public boolean readOnly(String name);

        /** Get the MIME type of the file. If filesystem has no special support
        * for MIME types then can simply return null. FileSystem can register
        * MIME types for a well-known extensions: FileUtil.setMIMEType(String ext, String mimeType)
        * or together with filesystem supply some resolvers subclassed from MIMEResolver.
        *
        * @param name the file to test
        * @return the MIME type textual representation (e.g. <code>"text/plain"</code>)
        * or null if no special support for recognizing MIME is implemented.
         */
        public String mimeType(String name);

        /** Get the size of the file.
        *
        * @param name the file to test
        * @return the size of the file in bytes, or zero if the file does not contain data (does not
        *  exist or is a folder).
        */
        public long size(String name);

        /** Get input stream.
        *
        * @param name the file to test
        * @return an input stream to read the contents of this file
        * @exception FileNotFoundException if the file does not exist or is invalid
        */
        public InputStream inputStream(String name) throws FileNotFoundException;

        /** Get output stream.
        *
        * @param name the file to test
        * @return output stream to overwrite the contents of this file
        * @exception IOException if an error occurs (the file is invalid, etc.)
        */
        public OutputStream outputStream(String name) throws IOException;

        /** Lock the file.
        * May do nothing if the underlying storage does not support locking.
        * This does not affect locking using {@link FileLock} within NetBeans, however.
        * @param name name of the file
        * @throws IOException (e.g. {@link FileAlreadyLockedException}) if the file is already locked or otherwise cannot be locked
        */
        public void lock(String name) throws IOException;

        /** Unlock the file.
        * @param name name of the file
        */
        public void unlock(String name);

        /** Mark the file as being unimportant.
         * If not called, the file is assumed to be important.
         * <em>Deprecated</em> and new implementations need not do anything.
         * @param name the file to mark
         */
        public void markUnimportant(String name);
    }

    /**
     * Information about files related to symbolic links.
     *
     * @since openide.filesystem/9.4
     */
    @SuppressWarnings("PublicInnerClass")
    public interface SymlinkInfo extends Serializable {

        /**
         * Check whether a file represents a symbolic link.
         *
         * @param name Name of the file.
         *
         * @return True if this file represents a symbolic link, false
         * otherwise.
         * @throws java.io.IOException If some I/O problem occurs.
         * @since openide.filesystem/9.4
         *
         * @see #readSymbolicLink(java.lang.String)
         * @see #getCanonicalName(java.lang.String)
         */
        boolean isSymbolicLink(String name) throws IOException;

        /**
         * Read symbolic link.
         *
         * If the file represents a symbolic link, return its target path.
         * Otherwise the return value is undefined, or
         * {@link IllegalArgumentException} can be thrown.
         *
         * This method does not perform any normalization. If the target of
         * symbolic link is defined using relative path, this relative path will
         * be returned.
         *
         * @param name Name of the file.
         *
         * @throws java.io.IOException If some I/O problem occurs.
         * @since openide.filesystem/9.4
         *
         * @return Target of the symbolic link (as absolute or relative path),
         * or some undefined value if {@code name} does not represent symbolic
         * link.
         *
         * @see #isSymbolicLink(java.lang.String)
         */
        String readSymbolicLink(String name) throws IOException;

        /**
         * Get canonical file name (resolve all symbolic links).
         *
         * @param name Name of the file.
         *
         * @return Path that represents the same file as {@code name} and
         * where all symbolic links are resolved.
         * @throws java.io.IOException If some I/O problem occurs.
         * @since openide.filesystem/9.4
         */
        String getCanonicalName(String name) throws IOException;
    }

    /** Handle attributes of files.
    */
    public interface Attr extends Serializable {
        /** @deprecated Only public by accident. */
        @Deprecated
        /* public static final */ long serialVersionUID = 5978845941846736946L;

        /** Get the file attribute with the specified name.
        * @param name the file
        * @param attrName name of the attribute
        * @return appropriate (serializable) value or <CODE>null</CODE> if the attribute is unset (or could not be properly restored for some reason)
        */
        public Object readAttribute(String name, String attrName);

        /** Set the file attribute with the specified name.
        * @param name the file
        * @param attrName name of the attribute
        * @param value new value or <code>null</code> to clear the attribute. Must be serializable, although particular filesystems may or may not use serialization to store attribute values.
        * @exception IOException if the attribute cannot be set. If serialization is used to store it, this may in fact be a subclass such as {@link java.io.NotSerializableException}.
        */
        public void writeAttribute(String name, String attrName, Object value)
        throws IOException;

        /** Get all file attribute names for the file.
        * @param name the file
        * @return enumeration of keys (as strings)
        */
        public Enumeration<String> attributes(String name);

        /** Called when a file is renamed, to appropriately update its attributes.
        * @param oldName old name of the file
        * @param newName new name of the file
        */
        public void renameAttributes(String oldName, String newName);

        /** Called when a file is deleted, to also delete its attributes.
        *
        * @param name name of the file
        */
        public void deleteAttributes(String name);
    }
}
