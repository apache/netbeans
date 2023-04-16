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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import org.openide.util.BaseUtilities;
import org.openide.util.Enumerations;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;

/** This is the base for all implementations of file objects on a filesystem.
* Provides basic information about the object (its name, parent,
* whether it exists, etc.) and operations on it (move, delete, etc.).
*
* @author Jaroslav Tulach, Petr Hamernik, Ian Formanek
*/
public abstract class FileObject extends Object implements Serializable, Lookup.Provider {
    /**
     * Name of default line separator attribute.
     * File object can provide default line separator if it differs from
     * <code>System.getProperty("line.separator")</code>. Call
     * <code>fo.getAttribute(DEFAULT_LINE_SEPARATOR_PROP)</code> returns string with
     * default line separator. Default line separator will be used by the text
     * editor if saving new content to an initially empty file. Any other code
     * which creates file content programmatically must manually read this
     * property if it cares.
     * @since 7.56
     */
    public static final String DEFAULT_LINE_SEPARATOR_ATTR = "default-line-separator"; //NOI18N

    /**
     * Name of default path name separator attribute.
     * File object can provide default separator if it differs from
     * <code>File.separator</code>. Call
     * <code>fo.getAttribute(DEFAULT_PATHNAME_SEPARATOR_ATTR)</code> returns string with
     * default separator.
     * @since 9.6
     */
    public static final String DEFAULT_PATHNAME_SEPARATOR_ATTR = "default-pathname-separator"; //NOI18N

    /** generated Serialized Version UID */
    static final long serialVersionUID = 85305031923497718L;
    
    /** implementation of lookup associated with this file object */
    private FileObjectLkp lkp;

    /** Get the name without extension of this file or folder.
    * Period at first position is not considered as extension-separator
    * For the root folder of a filesystem, this will be the empty
    * string (the extension will also be the empty string, and the
    * fully qualified name with any delimiters will also be the
    * empty string).
    * @return name of the file or folder(in its enclosing folder)
    */
    public abstract String getName();

    /** Get the extension of this file or folder.
    * Period at first position is not considered as extension-separator
    * This is the string after the last dot of the full name, if any.
    *
    * @return extension of the file or folder (if any) or empty string if there is none
    */
    public abstract String getExt();

    /** Renames this file (or folder).
    * Both the new basename and new extension should be specified.
    * <p>
    * Note that using this call, it is currently only possible to rename <em>within</em>
    * a parent folder, and not to do moves <em>across</em> folders.
    * Conversely, implementing filesystems need only implement "simple" renames.
    * If you wish to move a file across folders, you should call {@link FileUtil#moveFile}.
    * @param lock File must be locked before renaming.
    * @param name new basename of file
    * @param ext new extension of file (ignored for folders)
    */
    public abstract void rename(FileLock lock, String name, String ext)
    throws IOException;

    /** Copies this file. This allows the filesystem to perform any additional
    * operation associated with the copy. But the default implementation is simple
    * copy of the file and its attributes  Since version 9.32, the file POSIX
    * permissions are copied as well.
    *
    * @param target target folder to move this file to
    * @param name new basename of file
    * @param ext new extension of file (ignored for folders)
    * @return the newly created file object representing the moved file
    */
    public FileObject copy(FileObject target, String name, String ext)
    throws IOException {
        if (isFolder()) {
            if (FileUtil.isParentOf(this, target)) {
                throw new FSException(NbBundle.getMessage(FileObject.class, "EXC_OperateChild", this, target)); // NOI18N
            }
            FileObject peer = target.createFolder(name);
            FileUtil.copyAttributes(this, peer);
            for (FileObject fo : getChildren()) {
                fo.copy(peer, fo.getName(), fo.getExt());
            }
            return peer;
        }

        FileObject dest = FileUtil.copyFileImpl(this, target, name, ext);

        return dest;
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
        if (getParent().equals(target)) {
            // it is possible to do only rename
            rename(lock, name, ext);

            return this;
        } else {
            // have to do copy
            FileObject dest = copy(target, name, ext);
            delete(lock);
            FileObjectLkp.reassign(this, dest);
            return dest;
        }
    }

    /**
     * Gets a textual represtentation of this <code>FileObject</code>.
     * The precise format is not defined. In particular it is probably
     * <em>not</em> a resource path.
     * For that purpose use {@link #getPath} directly.
     * <p>Typically it is useful for debugging purposes. Example of correct usage:
     * <pre>
     * <font class="type">FileObject</font> <font class="variable-name">fo</font> = getSomeFileObject();
     * ErrorManager.getDefault().log(<font class="string">"Got a change from "</font> + fo);
     * </pre>
     * @return some representation of this file object
     */
    @Override
    public String toString() {
        String cname = getClass().getName();
        String cnameShort = cname.substring(cname.lastIndexOf('.') + 1);
        
        try {
            return cnameShort + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + (isRoot() ? "root of " + getFileSystem() : getPath()) + ']'; // NOI18N
        } catch (FileStateInvalidException x) {
            return cnameShort + '@' + Integer.toHexString(System.identityHashCode(this)) + "[???]"; // NOI18N
        }
    }

    /** Get the full resource path of this file object starting from the filesystem root.
     * Folders are separated with forward slashes. File extensions, if present,
     * are included. The root folder's path is the empty string. The path of a folder
     * never ends with a slash.
     * <p>Subclasses are strongly encouraged to override this method.
     * <p>Never use this to get a display name for the file! Use {@link FileUtil#getFileDisplayName}.
     * <p>Do not use this method to find a file path on disk! Use {@link FileUtil#toFile}.
     * @return the path, for example <code>path/from/root.ext</code>
     * @see FileSystem#findResource
     * @since 3.7
     */
    public String getPath() {
        StringBuilder[] buf = { null };
        constructName(buf, '/', 0);
        return buf[0].toString();
    }

    /** Get fully-qualified filename. Does so by walking through all folders
    * to the root of the filesystem. Separates files with provided <code>separatorChar</code>.
    * The extension, if present, is separated from the basename with <code>extSepChar</code>.
    * <p><strong>Note:</strong> <code>fo.getPath()</code> will have the
    * same effect as using this method with <code>/</code> and <code>.</code> (the standard
    * path and extension delimiters).
    * @param separatorChar char to separate folders and files
    * @param extSepChar char to separate extension
    * @return the fully-qualified filename
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public String getPackageNameExt(char separatorChar, char extSepChar) {
        assert false : "Deprecated.";

        if (isRoot() || getParent().isRoot()) {
            return getNameExt();
        }

        StringBuilder[] arr = new StringBuilder[1];
        getParent().constructName(arr, separatorChar, 50);

        String ext = getExt();

        if ((ext == null) || ext.equals("")) { // NOI18N
            arr[0].append(separatorChar).append(getNameExt());
        } else {
            arr[0].append(separatorChar).append(getName()).append(extSepChar).append(getExt());
        }

        return arr[0].toString();
    }

    /** Get fully-qualified filename, but without extension.
    * Like {@link #getPackageNameExt} but omits the extension.
    * @param separatorChar char to separate folders and files
    * @return the fully-qualified filename
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public String getPackageName(char separatorChar) {
        assert false : "Deprecated.";

        if (isRoot() || getParent().isRoot()) {
            return (isFolder()) ? getNameExt() : getName();
        }

        StringBuilder[] arr = new StringBuilder[1];
        String name = getName();

        getParent().constructName(arr, separatorChar, name.length());
        arr[0].append(separatorChar).append(name);
        return arr[0].toString();
    }

    /** Getter for name and extension of a file object. Dot is used
     * as separator between name and ext.
     * @return string name of the file in the folder (with extension)
     */
    public String getNameExt() {
        String n = getName();
        String e = getExt();

        return ((e == null) || (e.length() == 0)) ? n : (n + '.' + e);
    }

    /** Constructs path of file.
    * @param arr to place the string buffer
    * @param sepChar separator character
    */
    private void constructName(StringBuilder[] arr, char sepChar, int lengthSoFar) {
        String myName = getNameExt();
        int myLen = lengthSoFar + myName.length();

        FileObject parent = getParent();
        
        if (parent == this) {
            Object fs;
            try {
                fs = getFileSystem();
            } catch (IOException ex) {
                fs = "unknown"; // NOI18N
            }
            throw new IllegalStateException("Dangerous self-reproductive parentship: " + this + " type: " + getClass() + " fs: " + fs); // NOI18N
        }

        if ((parent != null) && !parent.isRoot()) {
            parent.constructName(arr, sepChar, myLen + 1);
            arr[0].append(sepChar);
        } else {
            assert arr[0] == null;
            arr[0] = new StringBuilder(myLen);
        }
        arr[0].append(getNameExt());
    }

    /** Get the filesystem containing this file.
    * <p>
    * Note that it may be possible for a stale file object to exist which refers to a now-defunct filesystem.
    * If this is the case, this method will throw an exception.
    * @return the filesystem
    * @exception FileStateInvalidException if the reference to the file
    *   system has been lost (e.g., if the filesystem was deleted)
    */
    public abstract FileSystem getFileSystem() throws FileStateInvalidException;

    /** Get parent folder.
    * The returned object will satisfy {@link #isFolder}.
    *
    * @return the parent folder or <code>null</code> if this object {@link #isRoot}.
    */
    public abstract FileObject getParent();

    /** Test whether this object is a folder.
    * @return true if the file object is a folder (i.e., can have children)
    */
    public abstract boolean isFolder();

    /**
    * Get last modification time.
    * @return the date
    */
    public abstract java.util.Date lastModified();

    /** Test whether this object is the root folder.
    * The root should always be a folder.
    * @return true if the object is the root of a filesystem
    */
    public abstract boolean isRoot();

    /** Test whether this object is a data object.
    * This is exclusive with {@link #isFolder}.
    * @return true if the file object represents data (i.e., can be read and written)
    */
    public abstract boolean isData();

    /** Test whether the file is valid. The file can be invalid if it has been deserialized
    * and the file no longer exists on disk; or if the file has been deleted.
    *
    * @return true if the file object is valid
    */
    public abstract boolean isValid();

    /** Test whether there is a file with the same basename and only a changed extension in the same folder.
    * The default implementation asks this file's parent using {@link #getFileObject(String name, String ext)}.
    *
    * @param ext the alternate extension
    * @return true if there is such a file
    */
    public boolean existsExt(String ext) {
        FileObject parent = getParent();

        return (parent != null) && (parent.getFileObject(getName(), ext) != null);
    }

    /** Delete this file. If the file is a folder and it is not empty then
    * all of its contents are also recursively deleted.
    *
    * @param lock the lock obtained by a call to {@link #lock}
    * @exception IOException if the file could not be deleted
    */
    public abstract void delete(FileLock lock) throws IOException;

    /** Delete this file. If the file is a folder and it is not empty then
    * all of its contents are also recursively deleted. FileObject is locked
    * before delete and finally is this lock released.
    *
    * @exception IOException if the file could not be deleted or
    * FileAlreadyLockedException if the file is already locked {@link #lock}
    * @since 1.15
    */
    public final void delete() throws IOException {
        FileLock lock = lock();

        try {
            delete(lock);
        } finally {
            lock.releaseLock();
        }
    }

    /** A lookup containing various logical views of the underlying represented file.
     * The lookup is supposed to contain <code>this</code> {@link FileObject}
     * (however not necessarily only one, possibly more). The identity of the 
     * lookup should survive 
     * {@link #move(org.openide.filesystems.FileLock, org.openide.filesystems.FileObject, java.lang.String, java.lang.String) move operation}
     * - the resulting {@link FileObject} after successful <em>move</em>
     * will share the same {@link Lookup} as the original {@link FileObject}.
     * That is why one can put <code>fileObject.getLookup()</code> into 
     * {@link java.util.IdentityHashMap}{@code <Lookup,Anything>} and cache 
     * <code>Anything</code> regardless the actual location of (moved) file.
     * Or one can obtain a {@link Result} from the {@link Lookup}, keep
     * its reference, attach a listener to it and be assured that it
     * will fire events even if the file gets renamed.
     * 
     * <p class="nonnormative">
     * Inside of NetBeans Platform application the content of this lookup is usually
     * identical to the one provided by the 
     * <code><a href="@org-openide-loaders@/org/openide/loaders/DataObject.html">DataObject</a>.find(this).getLookup()</code>.
     * This functionality is provided by the <code>org.netbeans.modules.settings</code> 
     * module. 
     * <code><a href="@org-openide-loaders@/org/openide/loaders/DataObject.html">DataObject</a>.move</code>
     * operation preserves the object's identity, and to mimic the same behavior 
     * without reference to 
     * <a href="@org-openide-loaders@/org/openide/loaders/DataObject.html">DataObject</a>
     * the behavior of {@link FileObject#getLookup() FileObject.getLookup()} has 
     * been modelled.
     * </p>
     * 
     * @return lookup providing logical interfaces additionally describing the 
     *   content of the underlying file
     * 
     * @since 8.0
     */
    @Override
    public Lookup getLookup() {
        return FileObjectLkp.create(this, true);
    }
    
    final FileObjectLkp lookup() {
        assert Thread.holdsLock(FileObjectLkp.class);
        return lkp;
    }
    
    final void assignLookup(FileObjectLkp lkp) {
        assert Thread.holdsLock(FileObjectLkp.class);
        if (this.lkp == lkp) {
            return;
        }
        assert this.lkp == null : "Should be null, but was " + this.lkp;
        this.lkp = lkp;
    }

    /** Get the file attribute with the specified name.
    * @param attrName name of the attribute
    * @return appropriate (serializable) value or <CODE>null</CODE> if the attribute is unset (or could not be properly restored for some reason)
    */
    public abstract Object getAttribute(String attrName);

    /** Set the file attribute with the specified name. The actual meaning of 
     * this method is implementation dependent. It is generally expected that
     * the attribute will later be available from {@link #getAttribute(java.lang.String)}
     * method.
     * <div class="nonnormative">
     *   Many <a href="@TOP@/org/openide/filesystems/FileSystem.html">FileSystem</a>
     *   implementations (since version 7.43) 
     *   support special form of arguments for their
     *   <code>setAttribute</code> method. One can use 
     *   prefix <code>methodvalue:</code> or <code>newvalue:</code>
     *   to store references to 
     *   <a href="@JDK@/java/lang/reflect/Method.html">Method</a> or
     *   <a href="@JDK@/java/lang/Class.html">Class</a> respectively. 
     *   The meaning is then similar to {@link XMLFileSystem} attributes
     *  <code>methodvalue</code> and <code>newvalue</code>.
     * </div>
    * @param attrName name of the attribute
    * @param value new value or <code>null</code> to clear the attribute. Must be serializable, although particular filesystems may or may not use serialization to store attribute values.
    * @exception IOException if the attribute cannot be set. If serialization is used to store it, this may in fact be a subclass such as {@link java.io.NotSerializableException}.
    */
    public abstract void setAttribute(String attrName, Object value)
    throws IOException;

    /** Get all file attribute names for this file.
    * @return enumeration of keys (as strings)
    */
    public abstract Enumeration<String> getAttributes();

    /** Test whether this file has the specified extension.
    * @param ext the extension the file should have
    * @return true if the text after the last period (<code>.</code>) is equal to the given extension
    */
    public final boolean hasExt(String ext) {
        if (isHasExtOverride()) {
            return hasExtOverride(ext);
        }

        return getExt().equals(ext);
    }

    /** Overriden in AbstractFolder */
    boolean isHasExtOverride() {
        return false;
    }

    /** Overridden in AbstractFolder */
    boolean hasExtOverride(String ext) {
        return false;
    }

    /** Add new listener to this object.
    * @param fcl the listener
    */
    public abstract void addFileChangeListener(FileChangeListener fcl);

    /** Remove listener from this object.
    * @param fcl the listener
    */
    public abstract void removeFileChangeListener(FileChangeListener fcl);


    /** Adds a listener to this {@link FileObject} and all its children and
     * children or its children.
     * It is guaranteed that whenever a change
     * is made via the FileSystem API itself under this {@link FileObject}
     * that it is notified to the <code>fcl</code> listener. Whether external
     * changes (if they make sense) are detected and
     * notified depends on actual implementation. As some implementations may
     * need to perform non-trivial amount of work during initialization of
     * listeners, this methods can take long time. Usage of this method may
     * consume a lot of system resources and as such it shall be used with care.
     * Traditional {@link #addFileChangeListener(org.openide.filesystems.FileChangeListener)}
     * is definitely preferred variant.
     * <p class="nonnormative">
     * If you are running with the MasterFS module enabled, it guarantees
     * that for files backed with real {@link File}, the system initializes
     * itself to detect external changes on the whole subtree.
     * This requires non-trivial amount of work and especially on slow
     * disks (aka networks ones) may take a long time to add the listener
     * and also refresh the system when {@link FileObject#refresh()}
     * and especially {@link FileUtil#refreshAll()} is requested.
     * </p>
     *
     * @param fcl the listener to register
     * @since 7.28
     */
    public void addRecursiveListener(FileChangeListener fcl) {
        if (!isFolder()) {
            addFileChangeListener(fcl);
            return;
        }
        try {
            boolean allowsExternalChanges = getFileSystem() instanceof LocalFileSystem;
            getFileSystem().addFileChangeListener(new RecursiveListener(this, fcl, allowsExternalChanges));
        } catch (FileStateInvalidException ex) {
            ExternalUtil.LOG.log(Level.FINE, "Cannot remove listener from " + this, ex);
        }
    }

    /** Removes listener previously added by {@link #addRecursiveListener(org.openide.filesystems.FileChangeListener)}
     *
     * @param fcl the listener to remove
     * @since 7.28
     */
    public void removeRecursiveListener(FileChangeListener fcl) {
        if (!isFolder()) {
            removeFileChangeListener(fcl);
            return;
        }
        try {
            getFileSystem().removeFileChangeListener(new RecursiveListener(this, fcl, false));
        } catch (FileStateInvalidException ex) {
            ExternalUtil.LOG.log(Level.FINE, "Cannot remove listener from " + this, ex);
        }
    }

    /** Fire data creation event.
    * @param en listeners that should receive the event
    * @param fe the event to fire in this object
    */
    protected void fireFileDataCreatedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        dispatchEvent(FCLSupport.Op.DATA_CREATED, en, fe);
    }

    /** Fire folder creation event.
    * @param en listeners that should receive the event
    * @param fe the event to fire in this object
    */
    protected void fireFileFolderCreatedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        dispatchEvent(FCLSupport.Op.FOLDER_CREATED, en, fe);
    }

    /** Fire file change event.
    * @param en listeners that should receive the event
    * @param fe the event to fire in this object
    */
    protected void fireFileChangedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        dispatchEvent(FCLSupport.Op.FILE_CHANGED, en, fe);
    }

    /** Fire file deletion event.
    * @param en listeners that should receive the event
    * @param fe the event to fire in this object
    */
    protected void fireFileDeletedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        dispatchEvent(FCLSupport.Op.FILE_DELETED, en, fe);
    }

    /** Fire file attribute change event.
    * @param en listeners that should receive the event
    * @param fe the event to fire in this object
    */
    protected void fireFileAttributeChangedEvent(Enumeration<FileChangeListener> en, FileAttributeEvent fe) {
        dispatchEvent(FCLSupport.Op.ATTR_CHANGED, en, fe);
    }

    /** Fire file rename event.
    * @param en listeners that should receive the event
    * @param fe the event to fire in this object
    */
    protected void fireFileRenamedEvent(Enumeration<FileChangeListener> en, FileRenameEvent fe) {
        dispatchEvent(FCLSupport.Op.FILE_RENAMED, en, fe);
    }

    /** Puts the dispatch event into the filesystem.
    */
    private void dispatchEvent(FCLSupport.Op op, Enumeration<FileChangeListener> en, FileEvent fe) {
        try {
            FileSystem fs = getFileSystem();
            fs.dispatchEvent(new ED(op, en, fe));
        } catch (FileStateInvalidException ex) {
            // no filesystem, no notification
        }
    }

    final void dispatchEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        try {
            getFileSystem().dispatchEvent(new ED(en, fe));
        } catch (FileStateInvalidException ex) {
            // no filesystem, no notification
        }
    }

    /** Get the MIME type of this file.
    * The MIME type identifies the type of the file's contents and should be used in the same way as in the <B>Java
    * Activation Framework</B> or in the {@link java.awt.datatransfer} package.
    * <P>
    * The default implementation calls {@link FileUtil#getMIMEType}.
    * (As a fallback return value, <code>content/unknown</code> is used.)
    * @return the MIME type textual representation, e.g. <code>"text/plain"</code>; never <code>null</code>
    */
    public String getMIMEType() {
        String mimeType = FileUtil.getMIMEType(this);
        return  mimeType == null ? "content/unknown" : mimeType;  //NOI18N
    }
    
    /** Resolves MIME type from the list of acceptable ones. By default
     * calls {@link FileUtil#getMIMEType(org.openide.filesystems.FileObject, java.lang.String[])},
     * but subclasses may override this method to be more effective.
     * 
     * @param withinMIMETypes 
     *   A hint to the underlaying infrastructure to 
     *   limit the search to given array of MIME types. 
     * @return the MIME type for the FileObject, or <code>null</code> if 
     *   the FileObject is unrecognized. It may return {@code content/unknown} instead of {@code null}.
     *   It is possible for the resulting MIME type to not be a member of given <code>withinMIMETypes</code>
     *   list.
     * @since 7.50
     */
    public String getMIMEType(String... withinMIMETypes) {
        return FileUtil.getMIMEType(this, withinMIMETypes);
    }

    /** Get the size of the file.
    * @return the size of the file in bytes or zero if the file does not contain data (does not
    *  exist or is a folder).
    */
    public abstract long getSize();

    /** Get input stream.
    * @return an input stream to read the contents of this file
    * @exception FileNotFoundException if the file does not exists, is a folder
    * rather than a regular file  or is invalid
    */
    public abstract InputStream getInputStream() throws FileNotFoundException;

    /** Reads the full content of the file object and returns it as array of
     * bytes.
     * @return array of bytes
     * @exception IOException in case the content cannot be fully read
     * @since 7.21
     */
    public byte[] asBytes() throws IOException {
        long len = getSize();
        if (len > Integer.MAX_VALUE) {
            throw new IOException("Too big file " + getPath()); // NOI18N
        }
        InputStream is = getInputStream();
        try {
            byte[] arr = new byte[(int)len];
            int pos = 0;
            while (pos < arr.length) {
                int read = is.read(arr, pos, arr.length - pos);
                if (read == -1) {
                    break;
                }
                pos += read;
            }
            if (pos != arr.length) {
                throw new IOException("Just " + pos + " bytes read from " + getPath()); // NOI18N
            }
            return arr;
        } finally {
            is.close();
        }
    }

    /** Reads the full content of the file object and returns it as string.
     * @param encoding the encoding to use
     * @return string representing the content of the file
     * @exception IOException in case the content cannot be fully read
     * @since 7.21
     */
    public String asText(String encoding) throws IOException {
        return new String(asBytes(), encoding);
    }

    /** Reads the full content of the file object and returns it as string.
     * This is similar to calling {@link #asText(java.lang.String)} with
     * default system encoding.
     * 
     * @return string representing the content of the file
     * @exception IOException in case the content cannot be fully read
     * @since 7.21
     */
    public String asText() throws IOException {
        return asText(Charset.defaultCharset().name());
    }

    /** Reads the full content of the file line by line with default
     * system encoding. Typical usage is
     * in <code>for</code> loops:
     * <pre>
     * for (String line : fo.asLines()) {
     *   // do something
     * }
     * </pre>
     * <p>
     * The list is optimized for iterating line by line, other operations,
     * like accessing all the lines or counting the number of its lines may
     * be suboptimal.
     *
     * @return list of strings representing the content of the file
     * @exception IOException in case the content cannot be fully read
     * @since 7.21
     */
    public List<String> asLines() throws IOException {
        return asLines(Charset.defaultCharset().name());
    }
    
    /** Reads the full content of the file line by line. Typical usage is
     * in <code>for</code> loops:
     * <pre>
     * for (String line : fo.asLines("UTF-8")) {
     *   // do something
     * }
     * </pre>
     * <p>
     * The list is optimized for iterating line by line, other operations,
     * like accessing all the lines or counting the number of its lines may
     * be suboptimal.
     *
     * @param encoding the encoding to use
     * @return list of strings representing the content of the file
     * @exception IOException in case the content cannot be fully read
     * @since 7.21
     */
    public List<String> asLines(final String encoding) throws IOException {
        return new FileObjectLines(encoding, this);
    }

    /** Get output stream.
    * @param lock the lock that belongs to this file (obtained by a call to
    *   {@link #lock})
    * @return output stream to overwrite the contents of this file
    * @exception IOException if an error occures (the file is invalid, etc.)
    */
    public abstract OutputStream getOutputStream(FileLock lock)
    throws IOException;

    /** Get output stream. This method does its best even
     * when this file object is {@linkplain #isValid() invalid} - since
     * version 9.23 it tries to recreate the parent hierarchy
     * and really open the stream.
     *
     * @return output stream to overwrite the contents of this file
     * @throws IOException if an error occurs
     * @throws FileAlreadyLockedException if the file is already locked
     * @since 6.6
     */
    public final OutputStream getOutputStream() throws FileAlreadyLockedException, IOException  {
        if (!isValid()) {
            final FileObject recreate = FileUtil.createData(getFileSystem().getRoot(), getPath());
            if (recreate != null) {
                return recreate.getOutputStreamImpl();
            }
        }
        return getOutputStreamImpl();
    }

    private OutputStream getOutputStreamImpl() throws IOException {
        final FileLock lock = lock();
        final OutputStream os;
        try {
            os = getOutputStream(lock);
            return new FilterOutputStream(os) {
                @Override
                public void write(byte b[], int off, int len) throws IOException {
                    // Delegate to real stream because it is more efficient if it is FileOutputStream.
                    // Otherwise it is copied byte by byte.
                    os.write(b, off, len);
                }

                @Override
                public void close() throws IOException {
                    try {
                        super.flush();
                        lock.releaseLock();
                        super.close();
                    } catch(IOException iex) {
                        if (lock.isValid()) {
                            lock.releaseLock();
                        }
                        throw iex;
                    }
                }
            };
        } catch(IOException iex) {
            if (lock.isValid()) {
                lock.releaseLock();
            }
            throw iex;
        }
    }

    
    /** Lock this file.
    * @return lock that can be used to perform various modifications on the file
    * @throws FileAlreadyLockedException if the file is already locked
    * @throws IOException (UserQuestionException) in case when the lock cannot be obtained now,
    *    but the underlaying implementation is able to do it after some
    *    complex/dangerous/long-lasting operation and request confirmation
    *    from the user
    *
    */
    public abstract FileLock lock() throws IOException;

    /**
     * Test if file is locked
     * @return true if file is locked
     * @since 7.3
     */
    public boolean isLocked() {
        FileLock fLock = null;
        try {
            fLock = lock();
        } catch (FileAlreadyLockedException fax) {
            return true;
        } catch (IOException ex) {
            return false;
        } finally {
            if (fLock != null) {
                fLock.releaseLock();
            }
        }
        return fLock == null;
    }

    /** Indicate whether this file is important from a user perspective.
    * This method allows a filesystem to distingush between important and
    * unimportant files when this distinction is possible.
    * <P>
    * <em>For example:</em> Java sources have important <code>.java</code> files and
    * unimportant <code>.class</code> files. If the filesystem provides
    * an "archive" feature it should archive only <code>.java</code> files.
    * @param b true if the file should be considered important
    * @deprecated No longer used. Instead use
    * <a href="@org-netbeans-modules-queries@/org/netbeans/api/queries/SharabilityQuery.html"><code>SharabilityQuery</code></a>.
    */
    @Deprecated
    public abstract void setImportant(boolean b);

    /** Get all children of this folder (files and subfolders). If the file does not have children
    * (does not exist or is not a folder) then an empty array should be returned. No particular order is assumed.
    *
    * @return array of direct children
    * @see #getChildren(boolean)
    * @see #getFolders
    * @see #getData
    */
    public abstract FileObject[] getChildren();

    /** Enumerate all children of this folder. If the children should be enumerated
    * recursively, first all direct children are listed; then children of direct subfolders; and so on.
    *
    * @param rec whether to enumerate recursively
    * @return enumeration of type <code>FileObject</code>
    */
    public Enumeration<? extends FileObject> getChildren(final boolean rec) {
        class WithChildren implements Enumerations.Processor<FileObject, FileObject> {
            @Override
            public FileObject process(FileObject fo, Collection<FileObject> toAdd) {
                if (rec && fo.isFolder()) {
                    for (FileObject child : fo.getChildren()) {
                        try {
                            if (!FileUtil.isRecursiveSymbolicLink(child)) { // #218795
                                toAdd.add(child);
                            }
                        } catch (IOException ex) {
                            ExternalUtil.LOG.log(Level.INFO, null, ex);
                        }
                    }
                }

                return fo;
            }
        }

        return Enumerations.queue(Enumerations.array(getChildren()), new WithChildren());
    }

    /** Enumerate the subfolders of this folder.
    * @param rec whether to recursively list subfolders
    * @return enumeration of type <code>FileObject</code> (satisfying {@link #isFolder})
    */
    public Enumeration<? extends FileObject> getFolders(boolean rec) {
        return Enumerations.filter(getChildren(rec), new OnlyFolders(true));
    }

    /** Enumerate all data files in this folder.
    * @param rec whether to recursively search subfolders
    * @return enumeration of type <code>FileObject</code> (satisfying {@link #isData})
    */
    public Enumeration<? extends FileObject> getData(boolean rec) {
        return Enumerations.filter(getChildren(rec), new OnlyFolders(false));
    }

    /** Retrieve file or folder contained in this folder by name.
    * <em>Note</em> that neither file nor folder is created on disk.
    * @param name basename of the file or folder (in this folder)
    * @param ext extension of the file; <CODE>null</CODE> or <code>""</code>
    *    if the file should have no extension or if folder is requested
    * @return the object representing this file or <CODE>null</CODE> if the file
    *   or folder does not exist
    * @exception IllegalArgumentException if <code>this</code> is not a folder
    */
    public abstract FileObject getFileObject(String name, String ext);

    /** Retrieve file or folder relative to a current folder, with a given relative path.
    * <em>Note</em> that neither file nor folder is created on disk. This method isn't final since revision 1.93.
    * Since 7.45 common implementations of this method 
    * accept also ".." which is interpreted as a reference to parent.
    * 
    * @param relativePath is just basename of the file or (since 4.16) the relative path delimited by '/'
    * @return the object representing this file or <CODE>null</CODE> if the file
    *   or folder does not exist
    * @exception IllegalArgumentException if <code>this</code> is not a folder
    */
    public FileObject getFileObject(String relativePath) {
        return getFileObject(relativePath, true);
    }

    /** Retrieve file or folder relative to a current folder, with a given relative path.
    * <em>Note</em> that neither file nor folder is created on disk. This method isn't final since revision 1.93.
    * Since 7.45 common implementations of this method
    * accept also ".." which is interpreted as a reference to parent.
    *
    * @param relativePath is just basename of the file or (since 4.16) the relative path delimited by '/'
    * @param onlyExisting if <code>false</code> then a non-<code>null</code> (but {@link #isValid() invalid}
    *   file object is returned even if it doesn't exist
    * @return the object representing requested file or <CODE>null</CODE> (when <code>onlyExisting</code> is true)
    *   if the file or folder does not exist
    * @exception IllegalArgumentException if <code>this</code> is not a folder
    * @since 9.23
    */
    public FileObject getFileObject(String relativePath, boolean onlyExisting) {
        if (relativePath.startsWith("/") && !relativePath.startsWith("//")) {
            relativePath = relativePath.substring(1);
        }

        FileObject myObj = this;
        StringTokenizer st = new StringTokenizer(relativePath, "/");
        
        if(relativePath.startsWith("//")) {
            // if it is UNC absolute path, start with //ComputerName/sharedFolder
            myObj = myObj.getFileObject("//"+st.nextToken()+"/"+st.nextToken(), null);
        }
        while ((myObj != null) && st.hasMoreTokens()) {
            String nameExt = st.nextToken();
            if (nameExt.equals("..")) { // NOI18N
                myObj = myObj.getParent();
            } else {
                if (!nameExt.equals(".")) {
                    FileObject nextObj = myObj.getFileObject(nameExt, null);
                    if (nextObj == null && !onlyExisting) {
                        try {
                            nextObj = new VirtualFileObject(myObj.getFileSystem(), myObj, nameExt, st.hasMoreTokens());
                        } catch (FileStateInvalidException ex) {
                            throw new IllegalStateException(ex);
                        }
                    }
                    myObj = nextObj;
                }
            }
        }

        return myObj;
    }

    /**
     * Create a new folder below this one with the specified name.
     * Fires {@link FileChangeListener#fileFolderCreated}.
     *
     * @param name the name of folder to create. Periods in name are allowed (but no slashes).
     * @return the new folder
     * @exception IOException if the folder cannot be created (e.g. already exists), or if <code>this</code> is not a folder
     * @see FileUtil#createFolder
     */
    public abstract FileObject createFolder(String name)
    throws IOException;

    /**
     * Create new data file in this folder with the specified name.
     * Fires {@link FileChangeListener#fileDataCreated}.
     *
     * @param name the name of data object to create (can contain a period, but no slashes)
     * @param ext the extension of the file (or <code>null</code> or <code>""</code>)
     * @return the new data file object
     * @exception IOException if the file cannot be created (e.g. already exists), or if <code>this</code> is not a folder
     * @see FileUtil#createData
     */
    public abstract FileObject createData(String name, String ext)
    throws IOException;

    /**
     * Create new data file in this folder with the specified name.
     * Fires {@link FileChangeListener#fileDataCreated}.
     *
     * @param name the name of data object to create (can contain a period, but no slashes)
     * @return the new data file object
     * @exception IOException if the file cannot be created (e.g. already exists), or if <code>this</code> is not a folder
     * @since 1.17
     * @see FileUtil#createData
     */
    public FileObject createData(String name) throws IOException {
        return createData(name, ""); // NOI18N        
    }
    
    /** 
     * Creates new file in this folder and immediately opens it for writing.
     * This method prevents possible race condition which can happen
     * when using the {@link #createData(java.lang.String)} method.  
     * Using {@link #createData(java.lang.String) that method} makes it
     * possible for someone to read the content of the newly created
     * file before its content is written. This method does its best to eliminate
     * such race condition.
     * 
     * <p class="nonnormative">
     * This method usually delivers both, {@link FileChangeListener#fileDataCreated(org.openide.filesystems.FileEvent) data created}
     * and {@link FileChangeListener#fileChanged(org.openide.filesystems.FileEvent) changed} 
     * events. Preferably it delivers them asynchronously. The assumption
     * is that the file will be (at least partially) written before
     * the listeners start to process the first event. The safety is additionally
     * ensured by <em>mutual exclusion</em> between output and input streams 
     * for the same file (any call to {@link #getInputStream()} will be blocked
     * for at least two seconds if there is existing open output stream). 
     * If you finish writing the content of your file in those two seconds,
     * you can be sure, nobody will have read its content yet.
     * </p>
     * 
     * @param name name of file to create with its extension
     * @return output stream to use to write content of the file
     * @throws IOException if the file cannot be created (e.g. already exists), or if <code>this</code> is not a folder
     * @since 7.41
     */
    public OutputStream createAndOpen(final String name) throws IOException {
        class R implements FileSystem.AsyncAtomicAction {
            OutputStream os;
            
            @Override
            public void run() throws IOException {
                FileObject fo = createData(name);
                os = fo.getOutputStream();
            }

            @Override
            public boolean isAsynchronous() {
                return true;
            }
        }
        R r = new R();
        getFileSystem().runAtomicAction(r);
        return r.os;
    }

    /** Test whether this file can be written to or not.
     * <P>
     * The value returned from this method should indicate the capabilities of the
     * file from the point of view of users of the FileObject's API, the actual
     * state of the file on a disk does not matter if the implementation of the
     * filesystem can change it when requested.
     * <P>
     * The result returned from this method should be tight together with
     * the expected behaviour of <code>getOutputStream</code>. If it is
     * likely that the method successfully returns a stream that can be
     * written to, let the <code>isReadOnly</code> return <code>false</code>.
     * <P>
     * Also other fileobject methods like <code>delete</code>
     * are suggested to be connected to result of this method. If not
     * read only, then it can be deleted, etc.
     * <p>
     * It is a good idea to call this method before attempting to perform any
     * operation on the FileObject that might throw an IOException simply
     * because it is read-only. If isReadOnly returns true, the operation may
     * be skipped, or the user notified that it cannot be done.
     * <em>However</em> it is often desirable for the user to be able to
     * continue the operation in case the filesystem supports making a file
     * writable. In this case calling code should:
     * <ol>
     * <li>Call {@link #lock} and catch any exception thrown.
     * <li>Then:
     * <ul>
     * <li>If no exception is thrown, proceed with the operation.
     * <li>If a UserQuestionException is thrown,
     * call <a href="@org-openide-util-ui@/org/openide/util/UserQuestionException.html#confirmed--" >UserQuestionException#confirmed</a> on it
     * (asynchronously - do not block any important threads). If <code>true</code>,
     * proceed with the operation. If <code>false</code>, exit.
     * If an <code>IOException</code> is thrown, notify it and exit.
     * <li>If another <code>IOException</code> is thrown, call {@link #isReadOnly}.
     * If <code>true</code>, ignore the exception (it is expected).
     * If <code>false</code>, notify it.
     * </ul>
     * In either case, exit.
     * </ol>
     * <p>
     *
     * @return <CODE>true</CODE> if file is read-only
     * @deprecated Please use the {@link #canWrite}.
     */
    @Deprecated
    public abstract boolean isReadOnly();

    /**
     * Tests if this file can be written to.
     * <P>
     * The default implementation simply uses <code> java.io.File.canWrite </code>
     * if there exists conversion to <code> java.io.File</code> (see {@link FileUtil#toFile}).
     * If conversion is not possible, then deprecated method {@link #isReadOnly} is used.
     * @return true if this file can be written, false if not.
     * @since 3.31
     */
    public boolean canWrite() {
        File f = FileUtil.toFile(this);

        if (f != null) {
            return f.canWrite();
        }

        return !isReadOnly();
    }

    /**
     * Tests if this file can be read.
     * <P>
     * The default implementation simply uses <code> java.io.File.canRead </code>
     * if there exists conversion to <code> java.io.File</code> (see {@link FileUtil#toFile}).
     * If conversion is not possible, then <code>true </code> is returned.
     * @return true if this file can be read, false if not.
     * @since 3.31
     */
    public boolean canRead() {
        File f = FileUtil.toFile(this);

        if (f != null) {
            return f.canRead();
        }

        return true;
    }

    static final String REMOVE_WRITABLES_ATTR = "removeWritables";

    /**
     * Checks whether this file can be reverted to a pristine state.
     * @return whether {@link #revert} might do something
     * @since 7.55
     */
    public final boolean canRevert() {
        return getAttribute(REMOVE_WRITABLES_ATTR) instanceof Callable<?>;
    }

    /**
     * Revert this file to a pristine state.
     * Generally only meaningful for files on the system filesystem (layers + user directory):
     * if the file is defined in a layer but modified in the user directory, it is reset;
     * if it is defined only in the user directory, it is deleted.
     * If {@link #canRevert} is false, does nothing.
     * Note that while content can be reset, it may not be possible to reset attributes.
     * <p>Implementors: for historical reasons this method checks {@link #getAttribute}
     * for a special attribute named {@code removeWritables} which must be of type
     * {@link Callable Callable&lt;Void&gt;}. If present, the file is considered modified.
     * @since 7.55
     */
    public final void revert() throws IOException {
        Object v = getAttribute(REMOVE_WRITABLES_ATTR);
        if (v instanceof Callable<?>) {
            try {
                ((Callable<?>) v).call();
            } catch (IOException x) {
                throw x;
            } catch (Exception x) {
                throw new IOException(x);
            }
        }
    }

    /** Should check for external modifications. For folders it should reread
    * the content of disk, for data file it should check for the last
    * time the file has been modified.
    *
    * @param expected should the file events be marked as expected change or not?
    * @see FileEvent#isExpected
    */
    public void refresh(boolean expected) {
    }

    /** Should check for external modifications. For folders it should reread
    * the content of disk, for data file it should check for the last
    * time the file has been modified.
    * <P>
    * The file events are marked as unexpected.
    */
    public void refresh() {
        refresh(false);
    }

    /**
     * @throws FileStateInvalidException never
     * @deprecated Use {@link #toURL} instead.
     */
    @Deprecated
    public final URL getURL() throws FileStateInvalidException {
        return toURL();
    }

    /** Get URL that can be used to access this file.
     * If the file object does not correspond to a disk file or JAR entry,
     * the URL will only be usable within NetBeans as it uses a special protocol handler.
     * Otherwise an attempt is made to produce an external URL.
    * @return URL of this file object
     * @see URLMapper#findURL
     * @see URLMapper#INTERNAL
     * @since 7.57
    */
    public final URL toURL() {
        return computeURL();
    }

    URL computeURL() {
        return URLMapper.findURL(this, URLMapper.INTERNAL);
    }

    /**
     * Gets a URI for this file.
     * Similar to {@link #toURL}.
     * @return an absolute URI representing this file location
     * @since 7.57
     */
    public final URI toURI() {
        try {
            URI uri = toURL().toURI();
            assert uri.isAbsolute() : uri;
            assert uri.equals(BaseUtilities.normalizeURI(uri)) : uri + " == " + BaseUtilities.normalizeURI(uri) + " from " + this;
            return uri;
        } catch (URISyntaxException x) {
            throw new IllegalStateException(x);
        }
    }

    /**
     * Tests if file really exists or is missing. Some operation on it may be restricted.
     * @return true indicates that the file is missing.
     * @since 1.9
     */
    public boolean isVirtual() {
        return false;
    }

    /**
     * Check whether this FileObject is a symbolic link.
     *
     * The default implementation returns false, but on filesystems that support
     * symbolic links this method should be overriden.
     *
     * @return True if this FileObject represents a symbolic link, false
     * otherwise.
     * @throws java.io.IOException If some I/O problem occurs.
     * @since openide.filesystem/9.4
     */
    public boolean isSymbolicLink() throws IOException {
        return false;
    }

    /**
     * Read symbolic link.
     *
     * If this FileObject represents a symbolic link, return a FileObject it
     * refers to, or null if the referred file or directory does not exist. If
     * this FileObject doesn't represent a symbolic link, the return value is
     * undefined.
     *
     * The default implementation returns null, but on filesystems that support
     * symbolic links this method should be overriden.
     *
     * @return The referred FileObject, or null if it is not available.
     * @throws java.io.IOException If some I/O problem occurs.
     * @since openide.filesystem/9.4
     */
    public FileObject readSymbolicLink() throws IOException {
        return null;
    }

    /**
     * Read symbolic link path.
     *
     * If this FileObject represents a symbolic link, return the path it refers
     * to, which can be absolute or relative. If this FileObject doesn't
     * represent a symbolic link, the return value is undefined.
     *
     * The default implementation returns this FileObject's path, but on
     * filesystems that support symbolic links this method should be overriden.
     *
     * @return The referred FileObject path.
     * @throws java.io.IOException If some I/O problem occurs.
     * @since openide.filesystem/9.4
     */
    public String readSymbolicLinkPath() throws IOException {
        return this.getPath();
    }

    /**
     * Return a FileObject with path where all symbolic links are resolved.
     *
     * The default implementation returns this object, but on filesystems that
     * support symbolic links this method should be overriden.
     *
     * @return The FileObject with path where all symlinks are resolved, or null
     * if it doesn't exist.
     * @throws java.io.IOException If some I/O problem occurs.
     * @since openide.filesystem/9.4
     */
    public FileObject getCanonicalFileObject() throws IOException {
        return this;
    }

    /** Listeners registered from MultiFileObject are considered as priority
     *  listeners.
     */
    static boolean isPriorityListener(FileChangeListener fcl) {
        if (fcl instanceof PriorityFileChangeListener) {
            return true;
        } else {
            return false;
        }
    }

    interface PriorityFileChangeListener extends FileChangeListener {}

    private static class ED extends FileSystem.EventDispatcher {
        private FCLSupport.Op op;
        private Enumeration<FileChangeListener> en;
        private final List<FileChangeListener> fsList;
        private final List<FileChangeListener> repList;
        
        
        private FileEvent fe;

        public ED(FCLSupport.Op op, Enumeration<FileChangeListener> en, FileEvent fe) {
            this.op = op;
            this.en = en;
            this.fe = fe;
            FileSystem fs = null;
            try {
                fs = this.fe.getFile().getFileSystem();
            } catch (FileStateInvalidException ex) {
                ExternalUtil.exception(ex);
            }
            ListenerList<FileChangeListener> fsll = (fs != null) ? fs.getFCLSupport().listeners : null;
            ListenerList<FileChangeListener> repll = (fs != null && fs.getRepository() != null) ? fs.getRepository().getFCLSupport().listeners : null;
            fsList = ListenerList.allListeners(fsll);
            repList = ListenerList.allListeners(repll);
        }

        public ED(Enumeration<FileChangeListener> en, FileEvent fe) {
            this(null, en, fe);
        }

        /** @param onlyPriority if true then invokes only priority listeners
         *  else all listeners are invoked.
         */
        @Override
        protected void dispatch(boolean onlyPriority, Collection<Runnable> postNotify) {
            if (this.op == null) {
                this.op = fe.getFile().isFolder() ? FCLSupport.Op.FOLDER_CREATED : FCLSupport.Op.DATA_CREATED;
            }

            LinkedList<FileChangeListener> newEnum = new LinkedList<FileChangeListener>(); // later lazy                

            while (en.hasMoreElements()) {
                FileChangeListener fcl = en.nextElement();

                if (onlyPriority && !isPriorityListener(fcl)) {
                    newEnum.add(fcl);

                    continue;
                }
                FCLSupport.dispatchEvent(fcl, fe, op, postNotify);
            }

            if (onlyPriority) {
                this.en = Collections.enumeration(newEnum);
            }

            /** FileEvents are forked in may cases. But FileEvents fired from
             * FileSystem and from Repository mustn`t be forked.
             */
            FileObject fo = fe.getFile();
            boolean transmit = false;            
            if (fo != null) {
                switch (op) {
                    case FILE_CHANGED:
                        transmit = fo.equals(fe.getSource());
                        break;
                    default:
                        transmit = !fo.equals(fe.getSource());
                        if (!transmit && fe instanceof Enumeration && !((Enumeration) fe).hasMoreElements()) {
                            transmit = true;
                        } 
                }
                
            }                

            if (!en.hasMoreElements() && transmit && !onlyPriority) {
                FileSystem fs = null;
                Repository rep = null;

                try {
                    fs = fe.getFile().getFileSystem();
                    rep = fs.getRepository();
                } catch (FileStateInvalidException fsix) {
                    return;
                }
                if (fs != null && fsList != null) {
                    FCLSupport.dispatchEvent(fsList, fe, op, postNotify);
                }


                if (rep != null && repList != null) {
                    FCLSupport.dispatchEvent(repList, fe, op, postNotify);
                }
            }
        }

        @Override
        protected void setAtomicActionLink(EventControl.AtomicActionLink propID) {
            fe.setAtomicActionLink(propID);
        }
    }

    /** Filters folders or data files.
     */
    private static final class OnlyFolders implements Enumerations.Processor<FileObject, FileObject> {
        private boolean folders;

        public OnlyFolders(boolean folders) {
            this.folders = folders;
        }

        @Override
        public FileObject process(FileObject obj, Collection<FileObject> coll) {
            FileObject fo = obj;

            if (folders) {
                return fo.isFolder() ? fo : null;
            } else {
                return fo.isData() ? fo : null;
            }
        }
    }
     // end of OnlyFolders
}
