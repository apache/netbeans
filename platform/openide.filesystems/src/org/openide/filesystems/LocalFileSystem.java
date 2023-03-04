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

package org.openide.filesystems;

import java.beans.PropertyVetoException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputValidation;
import java.io.OutputStream;
import java.io.SyncFailedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.logging.Level;
import org.openide.util.NbBundle;
import org.openide.util.BaseUtilities;

/** Local filesystem. Provides access to files on local disk.
* <p>For historical reasons many AbstractFileSystem.* methods are implemented
* as protected in this class. Do not call them! Subclasses might override
* them, or (better) use delegation.
*/
public class LocalFileSystem extends AbstractFileSystem {
    /** generated Serialized Version UID */
    private static final long serialVersionUID = -5355566113542272442L;

    /** Controlls the LocalFileSystem's automatic refresh.
    * If the refresh time interval is set from the System.property, than this value is used.
    * Otherwise, the refresh time interval is set to 0 which means the refresh
    * is disabled. */
    private static final int REFRESH_TIME = Integer.getInteger(
            "org.openide.filesystems.LocalFileSystem.REFRESH_TIME", 0
        ).intValue(); // NOI18N
    private static final int SUCCESS = 0;
    private static final int FAILURE = 1;
    private static final int NOT_EXISTS = 3;

    /** root file */
    private File rootFile = new File("."); // NOI18N

    /** is read only */
    private boolean readOnly;

    /** Constructor.
    */
    public LocalFileSystem() {
        Impl impl = new Impl(this);

        info = impl;
        change = impl;

        DefaultAttributes a = new InnerAttrs(this, info, change, impl);
        attr = a;
        list = a;
        setRefreshTime(REFRESH_TIME);
    }

    /* Human presentable name */
    public String getDisplayName() {
        return rootFile.getAbsolutePath();
    }
    
    @SuppressWarnings("deprecation") // need to set it for compat
    private void _setSystemName(String s) throws PropertyVetoException {
        setSystemName(s);
    }

    /** Set the root directory of the filesystem.
    * @param r file to set root to
    * @exception PropertyVetoException if the value if vetoed by someone else (usually
    *    by the {@link org.openide.filesystems.Repository Repository})
    * @exception IOException if the root does not exists or some other error occured
    */
    public synchronized void setRootDirectory(File r) throws PropertyVetoException, IOException {
        if (!r.exists() || r.isFile()) {
            throw new FSException(NbBundle.getMessage(LocalFileSystem.class, "EXC_RootNotExist", r.getAbsolutePath()));
        }

        String oldDisplayName = getDisplayName();
        _setSystemName(computeSystemName(r));

        rootFile = r;

        firePropertyChange(PROP_ROOT, null, refreshRoot());
        firePropertyChange(PROP_DISPLAY_NAME, oldDisplayName, getDisplayName());
    }

    /** Get the root directory of the filesystem.
     * @return root directory
    */
    public File getRootDirectory() {
        return rootFile;
    }

    /** Set whether the filesystem should be read only.
     * @param flag <code>true</code> if it should
    */
    public void setReadOnly(boolean flag) {
        if (flag != readOnly) {
            readOnly = flag;
            firePropertyChange(
                PROP_READ_ONLY, (!flag) ? Boolean.TRUE : Boolean.FALSE, flag ? Boolean.TRUE : Boolean.FALSE
            );
        }
    }

    /* Test whether filesystem is read only.
     * @return <true> if filesystem is read only
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /** Compute the system name of this filesystem for a given root directory.
    * <P>
    * The default implementation simply returns the filename separated by slashes.
    * @see FileSystem#setSystemName
    * @param rootFile root directory for the filesystem
    * @return system name for the filesystem
    */
    protected String computeSystemName(File rootFile) {
        String retVal = rootFile.getAbsolutePath().replace(File.separatorChar, '/');

        return ((BaseUtilities.isWindows() || (BaseUtilities.getOperatingSystem() == BaseUtilities.OS_OS2))) ? retVal.toLowerCase()
                                                                                                 : retVal;
    }

    //
    // List
    //
    protected String[] children(String name) {
        File f = getFile(name);

        if (f.isDirectory()) {
            return f.list();
        } else {
            return null;
        }
    }

    //
    // Change
    //
    protected void createFolder(String name) throws java.io.IOException {
        File f = getFile(name);

        if (name.equals("")) { // NOI18N
            throw new FSException(NbBundle.getMessage(LocalFileSystem.class, "EXC_CannotCreateF", f.getName(), getDisplayName(), f.getAbsolutePath()));
        }

        if (f.exists()) {
            throw new FSException(NbBundle.getMessage(LocalFileSystem.class, "EXC_FolderAlreadyExist", f.getName(), getDisplayName(), f.getAbsolutePath()));
        }

        boolean b = createRecursiveFolder(f);

        if (!b) {
            throw new FSException(NbBundle.getMessage(LocalFileSystem.class, "EXC_CannotCreateF", f.getName(), getDisplayName(), f.getAbsolutePath()));
        }
    }

    /*
    * @return true if RefreshAction should be enabled
    */
    @Override
    boolean isEnabledRefreshFolder() {
        return true;
    }

    /** Creates new folder and all necessary subfolders
    *  @param f folder to create
    *  @return <code>true</code> if the file exists when returning from this method
    */
    private static boolean createRecursiveFolder(File f) {
        if (f.exists()) {
            return true;
        }

        if (!f.isAbsolute()) {
            f = f.getAbsoluteFile();
        }

        String par = f.getParent();

        if (par == null) {
            return false;
        }

        if (!createRecursiveFolder(new File(par))) {
            return false;
        }

        f.mkdir();

        return f.exists();
    }

    protected void createData(String name) throws IOException {
        File f = getFile(name);
        boolean isError = true;
        IOException creationException = null;
        String annotationMsg = null;

        try {
            isError = f.createNewFile() ? false : true;
            isError = isError ? true : (!f.exists());

            if (isError) {
                annotationMsg = NbBundle.getMessage(LocalFileSystem.class, "EXC_DataAlreadyExist", f.getName(), getDisplayName(), f.getAbsolutePath());
                creationException = new SyncFailedException(annotationMsg);
            }
        } catch (IOException iex) {
            isError = true;
            creationException = iex;
            annotationMsg = iex.getLocalizedMessage();
        }

        if (isError) {
	    LOG.log(Level.INFO, "Trying to create new file {0}.", f.getPath());
            ExternalUtil.annotate(creationException, annotationMsg);
            throw creationException;
        }
    }

    protected void rename(String oldName, String newName)
    throws IOException {
        File of = getFile(oldName);
        File nf = getFile(newName);

        Random rndm = null;
        for (int retry = 0;; retry++) {
            // #7086 - (nf.exists() && !nf.equals(of)) instead of nf.exists() - fix for Win32
            boolean existsNF = nf.exists();
            boolean equalsOF = nf.equals(of);
            if (BaseUtilities.isMac()) {
                // File.equal on mac is not case insensitive (which it should be), 
                // so try harder
                equalsOF = of.getCanonicalFile().equals(nf.getCanonicalFile());
            }
            Boolean rename = null;
            if ((existsNF && !equalsOF) || !(rename = of.renameTo(nf))) {
                final String msg = NbBundle.getMessage(LocalFileSystem.class, "EXC_CannotRename", oldName, getDisplayName(), newName, existsNF, rename);
                if (retry > 10) {
                    throw new FSException(msg);
                }
                LOG.log(Level.WARNING, "Rename #{0} failed: {1}", new Object[]{retry, msg});
                if (rndm == null) {
                    rndm = new Random();
                }
                int sleep = rndm.nextInt(100) + 1;
                LOG.log(Level.INFO, "Sleeping for {0} ms", sleep);
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException ex) {
                    LOG.log(Level.FINE, null, ex);
                }
                continue;
            }
            return;
        }
    }

    protected void delete(String name) throws IOException {
        File file = getFile(name);

        if (deleteFile(file) != SUCCESS) {
            if (file.exists()) {
                throw new FSException(NbBundle.getMessage(LocalFileSystem.class, "EXC_CannotDelete", name, getDisplayName(), file.getAbsolutePath()));
            } else {
                /** When file externaly deleted and fo.delete () is called before
                 periodical refresh */
                FileObject thisFo = findResource(name);

                if (thisFo != null) {
                    if (thisFo.getParent() != null) {
                        thisFo.getParent().refresh();
                    }

                    thisFo.refresh();

                    if (thisFo.isValid()) {
                        throw new FSException(NbBundle.getMessage(LocalFileSystem.class, "EXC_CannotDelete", name, getDisplayName(), file.getAbsolutePath()));
                    }
                }
            }
        }
    }

    /** Method that recursivelly deletes all files in a folder.
    * @return true if successful
    */
    private static int deleteFile(File file) {
        boolean ret = file.delete();

        if (ret) {
            return SUCCESS;
        }

        if (!file.exists()) {
            return NOT_EXISTS;
        }

        if (file.isDirectory()) {
            // first of all delete whole content
            File[] arr = file.listFiles();

            for (int i = 0; i < arr.length; i++) {
                if (deleteFile(arr[i]) != SUCCESS) {
                    return FAILURE;
                }
            }
        }

        // delete the file itself
        return (file.delete() ? SUCCESS : FAILURE);
    }

    //
    // Info
    //
    protected java.util.Date lastModified(String name) {
        return new java.util.Date(getFile(name).lastModified());
    }

    protected boolean folder(String name) {
        return getFile(name).isDirectory();
    }

    protected boolean readOnly(String name) {
        File f = getFile(name);

        return !f.canWrite() && f.exists();
    }

    protected String mimeType(String name) {
        return null;
    }

    protected long size(String name) {
        return getFile(name).length();
    }

    // ===============================================================================
    //  This part of code could be used for monitoring of closing file streams.

    /*  public static java.util.HashMap openedIS = new java.util.HashMap();
      public static java.util.HashMap openedOS = new java.util.HashMap();

      static class DebugIS extends FileInputStream {
        public DebugIS(File f) throws java.io.FileNotFoundException { super(f); }
        public void close() throws IOException { openedIS.remove(this); super.close(); }
      };

      static class DebugOS extends FileOutputStream {
        public DebugOS(File f) throws java.io.IOException { super(f); }
        public void close() throws IOException { openedOS.remove(this); super.close(); }
      };

      public InputStream inputStream (String name) throws java.io.FileNotFoundException {
        DebugIS is = new DebugIS(getFile(name));
        openedIS.put(is, new Exception());
        return is;
      }

      public OutputStream outputStream (String name) throws java.io.IOException {
        DebugOS os = new DebugOS(getFile(name));
        openedOS.put(os, new Exception());
        return os;
      }*/

    //  End of the debug part
    // ============================================================================
    //  Begin of the original part
    protected InputStream inputStream(String name) throws java.io.FileNotFoundException {
        InputStream fis;
        File file = null;

        try {
            fis = new BufferedInputStream(new FileInputStream(file = getFile(name)));
        } catch (FileNotFoundException exc) {
            if ((file == null) || !file.exists()) {
                ExternalUtil.annotate(exc, NbBundle.getMessage(LocalFileSystem.class, "EXC_FileOutsideModified", getFile(name)));
            }

            throw exc;
        }

        return fis;
    }

    protected OutputStream outputStream(final String name) throws java.io.IOException {
        File f = getFile(name);
        if (!f.exists()) {
            f.getParentFile().mkdirs();
        }
        OutputStream retVal = new BufferedOutputStream(new FileOutputStream(f));

        // workaround for #42624
        if (BaseUtilities.isMac()) {
            retVal = getOutputStreamForMac42624(retVal, name);
        }

        return retVal;
    }

    private OutputStream getOutputStreamForMac42624(final OutputStream originalStream, final String name) {
        final File f = getFile(name);
        final long lModified = f.lastModified();
        OutputStream retVal = new FilterOutputStream(originalStream) {

            @Override
            public void close() throws IOException {
                super.close();

                if ((f.length() == 0) && (f.lastModified() == lModified)) {
                    f.setLastModified(System.currentTimeMillis());
                }
            }
        };

        return retVal;
    }

    //  End of the original part
    // ============================================================================
    protected void lock(String name) throws IOException {
        File file = getFile(name);

        if ((!file.canWrite() && file.exists()) || isReadOnly()) {
            throw new FSException(NbBundle.getMessage(LocalFileSystem.class, "EXC_CannotLock", null, null, file.getAbsolutePath()));
        }
    }

    protected void unlock(String name) {
    }

    protected void markUnimportant(String name) {
    }

    /**
     * Creates path for given string name.
     *
     * @param name File name.
     * @return The path.
     */
    private Path getPath(String name) {
        return Paths.get(rootFile.getPath(), name);
    }

    /** Creates file for given string name.
    * @param name the name
    * @return the file
    */
    private File getFile(String name) {
        // XXX should this be name.replace('/', File.separatorChar)? Cf. BT #4745638.
        return new File(rootFile, name);
    }

    /**
    * @param in the input stream to read from
    * @exception IOException error during read
    * @exception ClassNotFoundException when class not found
    */
    private void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, java.lang.ClassNotFoundException {
        in.defaultReadObject();

        in.registerValidation(
            new ObjectInputValidation() {
                public void validateObject() {
                    if (attr.getClass() == DefaultAttributes.class) {
                        Impl impl = new Impl(LocalFileSystem.this);
                        attr = new InnerAttrs(LocalFileSystem.this, impl, impl, impl);
                    }
                }
            }, 0
        );
    }

    /** The implementation class that implements List, Info
    * and Change interfaces and delegates all the methods
    * to appropriate methods of LocalFileSystem.
    */
    public static class Impl extends Object implements AbstractFileSystem.List,
            AbstractFileSystem.Info, AbstractFileSystem.SymlinkInfo,
            AbstractFileSystem.Change {

        /** generated Serialized Version UID */
        static final long serialVersionUID = -8432015909317698511L;

        /** pointer to local filesystem */
        private LocalFileSystem fs;

        /** Pointer to local filesystem
        * @param fs the filesystem this impl is connected to
        */
        public Impl(LocalFileSystem fs) {
            this.fs = fs;
        }

        /*
        *
        * Scans children for given name
        */
        public String[] children(String name) {
            return fs.children(name);
        }

        //
        // Change
        //

        /*
        * Creates new folder named name.
        * @param name name of folder
        * @throws IOException if operation fails
        */
        public void createFolder(String name) throws java.io.IOException {
            fs.createFolder(name);
        }

        /*
        * Create new data file.
        *
        * @param name name of the file
        *
        * @return the new data file object
        * @exception IOException if the file cannot be created (e.g. already exists)
        */
        public void createData(String name) throws IOException {
            fs.createData(name);
        }

        /*
        * Renames a file.
        *
        * @param oldName old name of the file
        * @param newName new name of the file
        */
        public void rename(String oldName, String newName)
        throws IOException {
            fs.rename(oldName, newName);
        }

        /*
        * Delete the file.
        *
        * @param name name of file
        * @exception IOException if the file could not be deleted
        */
        public void delete(String name) throws IOException {
            fs.delete(name);
        }

        //
        // Info
        //

        /*
        *
        * Get last modification time.
        * @param name the file to test
        * @return the date
        */
        public java.util.Date lastModified(String name) {
            return fs.lastModified(name);
        }

        /*
        * Test if the file is folder or contains data.
        * @param name name of the file
        * @return true if the file is folder, false otherwise
        */
        public boolean folder(String name) {
            return fs.folder(name);
        }

        /*
        * Test whether this file can be written to or not.
        * @param name the file to test
        * @return <CODE>true</CODE> if file is read-only
        */
        public boolean readOnly(String name) {
            return fs.readOnly(name);
        }

        /*
        * Get the MIME type of the file.
        * Uses {@link FileUtil#getMIMEType}.
        *
        * @param name the file to test
        * @return the MIME type textual representation, e.g. <code>"text/plain"</code>
        */
        public String mimeType(String name) {
            return fs.mimeType(name);
        }

        /*
        * Get the size of the file.
        *
        * @param name the file to test
        * @return the size of the file in bytes or zero if the file does not contain data (does not
        *  exist or is a folder).
        */
        public long size(String name) {
            return fs.size(name);
        }

        /*
        * Get input stream.
        *
        * @param name the file to test
        * @return an input stream to read the contents of this file
        * @exception FileNotFoundException if the file does not exists or is invalid
        */
        public InputStream inputStream(String name) throws java.io.FileNotFoundException {
            return fs.inputStream(name);
        }

        /*
        * Get output stream.
        *
        * @param name the file to test
        * @return output stream to overwrite the contents of this file
        * @exception IOException if an error occures (the file is invalid, etc.)
        */
        public OutputStream outputStream(String name) throws java.io.IOException {
            return fs.outputStream(name);
        }

        /*
        * Does nothing to lock the file.
        *
        * @param name name of the file
        */
        public void lock(String name) throws IOException {
            fs.lock(name);
        }

        /*
        * Does nothing to unlock the file.
        *
        * @param name name of the file
        */
        public void unlock(String name) {
            fs.unlock(name);
        }

        /*
        * Does nothing to mark the file as unimportant.
        *
        * @param name the file to mark
        */
        public void markUnimportant(String name) {
            fs.markUnimportant(name);
        }

        @Override
        public boolean isSymbolicLink(String name) throws IOException {
            return Files.isSymbolicLink(fs.getPath(name));
        }

        @Override
        public String readSymbolicLink(String name) throws IOException {
            return Files.readSymbolicLink(fs.getPath(name)).toString();
        }

        @Override
        public String getCanonicalName(String name) throws IOException {
            return fs.getPath(name).toRealPath().toString();
        }
    }

    /** This class adds new virtual attribute "java.io.File".
     * Because of the fact that FileObjects of LocalFileSystem are convertable
     * to java.io.File by means of attributes. */
    private static class InnerAttrs extends DefaultAttributes {
        static final long serialVersionUID = 1257351369229921993L;
        LocalFileSystem lfs;

        public InnerAttrs(
            LocalFileSystem lfs, AbstractFileSystem.Info info, AbstractFileSystem.Change change,
            AbstractFileSystem.List list
        ) {
            super(info, change, list);
            this.lfs = lfs;
        }

        @Override
        public Object readAttribute(String name, String attrName) {
            if (attrName.equals("java.io.File")) { // NOI18N
                return FileUtil.normalizeFile(lfs.getFile(name));
            }

            return super.readAttribute(name, attrName);
        }
    }
}
