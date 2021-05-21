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

import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.BaseUtilities;

/** A virtual filesystem based on a JAR archive.
* <p>For historical reasons many AbstractFileSystem.* methods are implemented
* as protected in this class. Do not call them! Subclasses might override
* them, or (better) use delegation.
 * <p><strong>Most module code should never create an instance of this class directly.</strong>
 * Use {@link FileUtil#getArchiveRoot(FileObject)} instead.</p>
* @author Jan Jancura, Jaroslav Tulach, Petr Hamernik, Radek Matous
*/
public class JarFileSystem extends AbstractFileSystem {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -98124752801761145L;

    /** One request proccesor shared for all instances of JarFileSystem*/
    private static final RequestProcessor req = new RequestProcessor("JarFs - modification watcher", 1, false, false); // NOI18N

    /** Controlls the LocalFileSystem's automatic refresh.
    * If the refresh time interval is set from the System.property, than this value is used.
    * Otherwise, the refresh time interval is set to 0, which means the refresh is disabled. */
    private static final int REFRESH_TIME = Integer.getInteger("org.openide.filesystems.JarFileSystem.REFRESH_TIME", 0)
                                                   .intValue(); // NOI18N

    /** maxsize for passing ByteArrayInputStream*/
    private static final long MEM_STREAM_SIZE = 100000;

    /** Maximal time for which closing of jar is postponed. */
    private static final int CLOSE_DELAY_MAX = 5000;
    /** Mininal time for which closing of jar is postponed. */
    private static final int CLOSE_DELAY_MIN = 300;

    /**
    * Opened zip file of this filesystem is stored here or null.
    */
    private transient JarFile jar;

    /** Manifest file for jar
    */
    private transient Manifest manifest;

    /** Archive file.1
    */
    private File root = new File("."); // NOI18N

    /** Watches modification on root file */
    private transient volatile RequestProcessor.Task watcherTask = null;
    private transient volatile RequestProcessor.Task closeTask = null;
    private transient long lastModification = 0;
    private static final Logger LOGGER = Logger.getLogger(JarFileSystem.class.getName());

    /*Should help to prevent closing JarFile if anybody has InputStream. Also this variable
     is used as object for synchronization: synchronized(closeSync)*/
    private transient Object closeSync = new Object();
    private int checkTime = REFRESH_TIME;

    /** number of FileObjects in using. If no one is used then the cached data
     * is freed */
    private transient long aliveCount = 0;

    /** Cached image of JarFile capable of answering queries on type and children.
     * There is a strong reference held while there is a living FileObject
     * and a SoftReference for caching after all FOs are freed.*/
    private transient Cache strongCache;

    /** The soft part of the cache reference. For simplicity never null*/
    private transient Reference<Cache> softCache = new SoftReference<Cache>(null);
    private transient FileObject foRoot;
    private transient FileChangeListener fcl;

    /** Actual time for which closing of jar is postponed. */
    private transient int closeDelay = CLOSE_DELAY_MIN;
    /** Time of request for opening of jar. */
    private transient long openRequestTime = 0;

    /**
    * Default constructor.
     * <p><strong>Most module code should never create an instance of this class directly.</strong>
     * Use {@link FileUtil#getArchiveRoot(FileObject)} instead.</p>
    */
    public JarFileSystem() {
        Impl impl = new Impl(this);
        this.list = impl;
        this.info = impl;
        this.change = impl;
        this.attr = impl;
    }

   /** Creates new JAR for a given JAR file. This constructor
     * behaves basically like:
     * <pre>
     * JarFileSystem fs = new JarFileSystem();
     * fs.setJarFile(jar);
     * </pre>
     * but it is more effective in some situations. It does not open and
     * read the content of the jar file immediately. Instead
     * it waits until somebody asks for resources from inside the JAR.
     *
     * @param jar location of the JAR file
     * @since 7.34
     */
    public JarFileSystem(File jar) throws IOException {
        this();
        try {
            setJarFile(jar, true, false);
        } catch (PropertyVetoException ex) {
            // cannot happen, setSystemName can throw the exception only
            // if the filesystem is already in Repository, which this one
            // is not.
            throw (IOException)new IOException().initCause(ex);
        }
    }

    /* Creates Reference. In FileSystem, which subclasses AbstractFileSystem, you can overload method
     * createReference(FileObject fo) to achieve another type of Reference (weak, strong etc.)
     * @param fo is FileObject. It`s reference yourequire to get.
     * @return Reference to FileObject
     */
    @Override
    protected <T extends FileObject> Reference<T> createReference(T fo) {
        aliveCount++;

        if ((checkTime > 0) && (watcherTask == null)) {
            watcherTask = req.post(watcherTask(), checkTime);
        }

        return new Ref<T>(fo);
    }

    private void freeReference() {
        aliveCount--;

        // Nobody uses this JarFileSystem => stop watcher, close JarFile and throw away cache.
        if (aliveCount == 0) {
            Task w = watcherTask;
            if (w != null) {
                w.cancel();
                watcherTask = null;
            }

            strongCache = null; // no more active FO, keep only soft ref
            closeCurrentRoot(false);
        }
    }

    /** Get the JAR manifest.
    * It will be lazily initialized.
    * @return parsed manifest file for this archive
    */
    public Manifest getManifest() {
        if (manifest == null) {
            try {
                synchronized (closeSync) {
                    JarFile j = reOpenJarFile();
                    manifest = (j == null) ? null : j.getManifest();
                    manifest = (manifest == null) ? null : new Manifest(manifest);
                }
            } catch (IOException ex) {
            } finally {
                closeCurrentRoot(false);
            }

            if (manifest == null) {
                manifest = new Manifest();
            }
        }

        return manifest;
    }

    /**
    * Set name of the ZIP/JAR file.
    * @param aRoot path to new ZIP or JAR file
    * @throws IOException if the file is not valid
    * @throws IllegalArgumentException if the file is {@link FileUtil#normalizeFile(java.io.File) not normalized}
    */
    public void setJarFile(final File aRoot) throws IOException, PropertyVetoException {
        setJarFile(aRoot, true, true);
    }
    
    @SuppressWarnings("deprecation") // need to set it for compat
    private void _setSystemName(String s) throws PropertyVetoException {
        setSystemName(s);
    }

    private void setJarFile(final File aRoot, boolean refreshRoot, boolean openJar)
    throws IOException, PropertyVetoException {
        if (!aRoot.equals(FileUtil.normalizeFile(aRoot))) {
            throw new IllegalArgumentException(
                "Parameter aRoot was not " + // NOI18N
                "normalized. Was " + aRoot + " instead of " + FileUtil.normalizeFile(aRoot)
            ); // NOI18N
        }

        FileObject newRoot = null;
        String oldDisplayName = getDisplayName();

        if (getRefreshTime() > 0) {
            setRefreshTime(0);
        }

        if (aRoot == null) {
            throw new FSException(NbBundle.getMessage(JarFileSystem.class, "EXC_NotValidFile", aRoot));
        }

        if (!aRoot.exists()) {
            throw new FSException(NbBundle.getMessage(JarFileSystem.class, "EXC_FileNotExists", aRoot.getAbsolutePath()));
        }

        if (!aRoot.canRead()) {
            throw new FSException(NbBundle.getMessage(JarFileSystem.class, "EXC_CanntRead", aRoot.getAbsolutePath()));
        }

        if (!aRoot.isFile()) {
            throw new FSException(NbBundle.getMessage(JarFileSystem.class, "EXC_NotValidFile", aRoot.getAbsolutePath()));
        }

        String s;
        s = aRoot.getAbsolutePath();
        s = s.intern();

        JarFile tempJar = null;

        if (openJar) {
            try {
                tempJar = new JarFile(s, false);
                LOGGER.log(Level.FINE, "opened: "+ System.currentTimeMillis()+ "   " + s);//NOI18N
            } catch (ZipException e) {
                throw new FSException(NbBundle.getMessage(JarFileSystem.class, "EXC_NotValidJarFile2", e.getLocalizedMessage(), s));
            }
        }

        synchronized (closeSync) {
            _setSystemName(s);

            closeCurrentRoot(false);
            setJar(tempJar);
            openRequestTime = System.currentTimeMillis();
            root = new File(s);

            if (refreshRoot) {
                strongCache = null;
                softCache.clear();
                aliveCount = 0;
                newRoot = refreshRoot();
                manifest = null;
                lastModification = 0;

                if (newRoot != null) {
                    firePropertyChange("root", null, newRoot); // NOI18N
                }
            }
        }

        firePropertyChange(PROP_DISPLAY_NAME, oldDisplayName, getDisplayName());

        foRoot = FileUtil.toFileObject(root);

        if ((foRoot != null) && (fcl == null)) {
            fcl = new FileChangeAdapter() {

                @Override
                public void fileChanged(FileEvent fe) {
                    if (watcherTask == null) {
                        parse(true);
                    }
                }

                @Override
                public void fileRenamed(FileRenameEvent fe) {
                    File f = FileUtil.toFile(fe.getFile());

                    if ((f != null) && !f.equals(aRoot)) {
                        try {
                            setJarFile(f, false, true);
                        } catch (IOException iex) {
                            ExternalUtil.exception(iex);
                        } catch (PropertyVetoException pvex) {
                            ExternalUtil.exception(pvex);
                        }
                    }
                }

                @Override
                public void fileDeleted(FileEvent fe) {
                    Enumeration<? extends FileObject> en = existingFileObjects(getRoot());

                    while (en.hasMoreElements()) {
                        AbstractFolder fo = (AbstractFolder) en.nextElement();
                        fo.validFlag = false;
                        fo.fileDeleted0(new FileEvent(fo));
                    }

                    refreshRoot();
                }
            };

            if (refreshRoot) {
                foRoot.addFileChangeListener(FileUtil.weakFileChangeListener(fcl, foRoot));
            }
        }
    }

    /** Get the file path for the ZIP or JAR file.
    * @return the file path
    */
    public File getJarFile() {
        return root;
    }

    /*
    * Provides name of the system that can be presented to the user.
    * @return user presentable name of the filesystem
    */
    public String getDisplayName() {
        return root != null ? root.getAbsolutePath() : NbBundle.getMessage(JarFileSystem.class, "JAR_UnknownJar");
    }

    /** This filesystem is read-only.
    * @return <code>true</code>
    */
    public boolean isReadOnly() {
        return true;
    }

    /* Closes associated JAR file on cleanup, if possible. */
    @Override
    public void removeNotify() {
        closeCurrentRoot(true);
    }

    /* initialization of jar variable, that is necessary after JarFileSystem was removed from Repository */

    //    public void addNotify () {
    //        super.addNotify ();
    //    }


    //
    // List
    //
    protected String[] children(String name) {
        Cache cache = getCache();

        return cache.getChildrenOf(name);
    }

    //
    // Change
    //
    protected void createFolder(String name) throws java.io.IOException {
        throw new IOException();
    }

    protected void createData(String name) throws IOException {
        throw new IOException();
    }

    protected void rename(String oldName, String newName)
    throws IOException {
        throw new IOException();
    }

    protected void delete(String name) throws IOException {
        throw new IOException();
    }

    //
    // Info
    //
    protected Date lastModified(String name) {
        long t;
        if (name.length() == 0) {
            t = getJarFile().lastModified();
        } else {
            try {
                t = getEntry(name).getTime();
            } finally {
                closeCurrentRoot(false);
            }
        }
        return new Date(t);
    }

    protected boolean folder(String name) {
        if ("".equals(name)) {
            return true; // NOI18N
        }

        Cache cache = getCache();

        return cache.isFolder(name);
    }

    protected boolean readOnly(String name) {
        return true;
    }

    protected String mimeType(String name) {
        return null;
    }

    protected long size(String name) {
        long retVal = getEntry(name).getSize();
        closeCurrentRoot(false);

        return (retVal == -1) ? 0 : retVal;
    }

    private InputStream getMemInputStream(JarFile jf, JarEntry je)
    throws IOException {
        InputStream is = getInputStream4336753(jf, je);
        ByteArrayOutputStream os = new ByteArrayOutputStream(is.available());

        try {
            FileUtil.copy(is, os);
        } finally {
            os.close();
        }

        return new ByteArrayInputStream(os.toByteArray());
    }

    private InputStream getTemporaryInputStream(JarFile jf, JarEntry je, boolean forceRecreate)
    throws IOException {
        String filePath = jf.getName();
        String entryPath = je.getName();
        StringBuffer jarCacheFolder = new StringBuffer("jarfscache"); //NOI18N
        jarCacheFolder.append(System.getProperty("user.name")).append("/"); //NOI18N        

        File jarfscache = new File(System.getProperty("java.io.tmpdir"), jarCacheFolder.toString()); //NOI18N

        if (!jarfscache.exists()) {
            jarfscache.mkdirs();
        }

        File f = new File(jarfscache, temporaryName(filePath, entryPath));

        boolean createContent = !f.exists();

        if (createContent) {
            f.createNewFile();
        } else {
            forceRecreate |= (Math.abs((System.currentTimeMillis() - f.lastModified())) > 10000);
        }

        if (createContent || forceRecreate) {
            // JDK 1.3 contains bug #4336753
            //is = j.getInputStream (je);
            InputStream is = getInputStream4336753(jf, je);

            try {
                OutputStream os = new FileOutputStream(f);

                try {
                    FileUtil.copy(is, os);
                } finally {
                    os.close();
                }
            } finally {
                is.close();
            }
        }

        f.deleteOnExit();

        return new FileInputStream(f);
    }

    private static String temporaryName(String filePath, String entryPath) {
        String fileHash = String.valueOf(filePath.hashCode());
        String entryHash = String.valueOf(entryPath.hashCode());

        StringBuffer sb = new StringBuffer();
        sb.append("f").append(fileHash).append("e").append(entryHash);

        return sb.toString().replace('-', 'x'); //NOI18N
    }

    protected InputStream inputStream(String name) throws java.io.FileNotFoundException {
        InputStream is = null;

        try {
            synchronized (closeSync) {
                JarFile j = reOpenJarFile();

                if (j != null) {
                    JarEntry je = j.getJarEntry(name);

                    if (je != null) {
                        if (je.getSize() < MEM_STREAM_SIZE) {
                            is = getMemInputStream(j, je);
                        } else {
                            is = getTemporaryInputStream(j, je, (strongCache != null));
                        }
                    }
                }
            }
        } catch (java.io.FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            FileNotFoundException fnfe = new FileNotFoundException(root.getAbsolutePath());
            fnfe.initCause(e);
            throw fnfe;
        } catch (RuntimeException e) {
            FileNotFoundException fnfe = new FileNotFoundException(root.getAbsolutePath());
            fnfe.initCause(e);
            throw fnfe;
        } finally {
            closeCurrentRoot(false);
        }

        if (is == null) {
            throw new java.io.FileNotFoundException(name);
        }

        return is;
    }

    // 4336753 workaround
    private InputStream getInputStream4336753(JarFile j, JarEntry je)
    throws IOException {
        InputStream in = null;

        while (in == null) {
            try {
                in = j.getInputStream(je);

                break;
            } catch (NullPointerException ex) {
                // ignore, it occured during reseting reused Inflanter
                // try again until there will be no Inflanter to reuse
            }
        }

        return in;
    }

    protected OutputStream outputStream(String name) throws java.io.IOException {
        throw new IOException();
    }

    protected void lock(String name) throws IOException {
        throw new FSException(NbBundle.getMessage(JarFileSystem.class, "EXC_CannotLock_JAR", name, root));
    }

    protected void unlock(String name) {
    }

    protected void markUnimportant(String name) {
    }

    protected Object readAttribute(String name, String attrName) {
        if ("java.io.File".equals(attrName)) {
            return null;
        }
        Attributes attr1 = getManifest().getAttributes(name);

        try {
            return (attr1 == null) ? null : attr1.getValue(attrName);
        } catch (IllegalArgumentException iax) {
            return null;
        }
    }

    protected void writeAttribute(String name, String attrName, Object value)
    throws IOException {
        throw new IOException("Setting attribute not allowed for JarFileSystem [" + this.getDisplayName() + "!" + name + " <- " + attrName + "=" + value + "]");  //NOI18N
    }

    protected Enumeration<String>  attributes(String name) {
        Attributes attr1 = getManifest().getAttributes(name);

        if (attr1 != null) {
            class ToString implements org.openide.util.Enumerations.Processor<Object, String> {
                public String process(Object obj, Collection<Object> ignore) {
                    return obj.toString();
                }
            }

            return org.openide.util.Enumerations.convert(Collections.enumeration(attr1.keySet()), new ToString());
        } else {
            return org.openide.util.Enumerations.empty();
        }
    }

    protected void renameAttributes(String oldName, String newName) {
    }

    protected void deleteAttributes(String name) {
    }

    /** Close the jar file when we go away...*/
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        closeCurrentRoot(false);
    }

    /** Initializes the root of FS.
    */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        closeSync = new Object();
        strongCache = null;
        softCache = new SoftReference<Cache>(null);
        aliveCount = 0;

        try {
            setJarFile(root);
        } catch (PropertyVetoException ex) {
            throw new IOException(ex.getMessage());
        } catch (IOException iex) {
            ExternalUtil.log(iex.getLocalizedMessage());
        }
    }

    /** Must be called from synchronized block*/
    private JarFile reOpenJarFile() throws IOException {
        synchronized (closeSync) {
            if (closeTask != null) {
                closeTask.cancel();
            }

            // #167527 - calculate adaptive delay before closing jar
            long now = System.currentTimeMillis();
            long requestPeriod = now - openRequestTime;
            openRequestTime = now;
            // 150% of time from last open request, but between CLOSE_DELAY_MIN and CLOSE_DELAY_MAX
            closeDelay = (int) Math.min(CLOSE_DELAY_MAX, Math.max(CLOSE_DELAY_MIN, (1.5 * requestPeriod)));

            return getJar(true);
        }
    }

    /** Performs a clean-up
     * After close of JarFile must be always reference to JarFile set to null
     */
    private void closeCurrentRoot(boolean isRealClose) {
        synchronized (closeSync) {
            if (closeTask != null) {
                closeTask.cancel();
            }

            if (isRealClose) {
                realClose().run();
            } else {
                closeTask = req.post(realClose(), closeDelay);
            }
        }
    }

    private Runnable realClose() {
        return new Runnable() {
                public void run() {
                    synchronized (closeSync) {
                    final JarFile jarFile = getJar(false);
                        if (jarFile != null) {
                            try {
                                jarFile.close();
                                LOGGER.log(Level.FINE, "closed: "+ System.currentTimeMillis()+ "   " + root.getAbsolutePath());//NOI18N
                            } catch (Exception exc) {
                                // ignore exception during closing, just log it
                                ExternalUtil.exception(exc);
                            } finally {
                                setJar(null);
                                closeTask = null;
                            }
                        }
                    }
                }
            };
    }

    private Cache getCache() {
        Cache ret = strongCache;

        if (ret == null) {
            ret = softCache.get();
        }

        if (ret == null) {
            ret = parse(false);
        }

        assert ret != null;

        return ret;
    }

    /** refreshes children recursively.*/
    private void refreshExistingFileObjects() {
        Cache cache = getCache();
        String[] empty = new String[0];

        Enumeration<? extends FileObject> en = existingFileObjects(getRoot());

        while (en.hasMoreElements()) {
            AbstractFolder fo = (AbstractFolder) en.nextElement();
            assert fo != null;

            if (fo.isFolder() && !fo.isInitialized()) {
                continue;
            }

            String[] children = cache.getChildrenOf(fo.getPath());

            if (children == null) {
                children = empty;
            }

            fo.refresh(null, null, true, true, children);
        }
    }

    /**parses entries of JarFile into EntryCache hierarchical structure and sets
     * lastModified to actual value.
     */
    private Cache parse(boolean refresh) {
        // force watcher to reschedule us if not succesfull       
        JarFile j = null;
        long start;

        beginAtomicAction();

        try {
            synchronized (closeSync) {
                start = System.currentTimeMillis();

                lastModification = 0;
                closeCurrentRoot(false);

                for (int i = 0; i <= 2; i++) {
                    try {
                        j = reOpenJarFile();

                        break;
                    } catch (IOException ex) {
                        if (i >= 2) {
                            return Cache.INVALID;
                        }

                        continue;
                    }
                }

                try {
                    Enumeration<JarEntry> en = j.entries();
                    // #144166 - If duplicate entries found in jar, it is logged
                    // and only unique entries are show. It can happen because
                    // Ant's jar task can produce such jars.
                    Set<String> duplicateCheck = new HashSet<String>();
                    Set<JarEntry> uniqueEntries = new HashSet<JarEntry>();
                    boolean duplicateReported = false;
                    while (en.hasMoreElements()) {
                        JarEntry entry = en.nextElement();
                        String name = entry.getName();
                        if (duplicateCheck.add(name)) {
                            uniqueEntries.add(entry);
                        } else {
                            if (!duplicateReported) {
                                LOGGER.warning("Duplicate entries in " + getJarFile() + ": " + name + "; please report to JAR creator.");
                                // report just once
                                duplicateReported = true;
                            }
                        }
                    }
                    Cache newCache = new Cache(uniqueEntries);
                    lastModification = root.lastModified();
                    strongCache = newCache;
                    softCache = new SoftReference<Cache>(newCache);

                    return newCache;
                } catch (Throwable t) {
                    // jar is invalid; perhaps it's being rebuilt
                    // don'w touch filesystem
                    return Cache.INVALID;
                }
            }
        } finally {
            closeCurrentRoot(false);

            if (refresh) {
                refreshExistingFileObjects();
            }

            if ((checkTime > 0) && (watcherTask == null)) {
                watcherTask = req.post(watcherTask(), checkTime);
            }

            finishAtomicAction();
        }
    }

    /* Anonymous Runnable class - responsible for checking whether JarFile was modified => standalone thread.
     * If JarFile was modified, parsing is invoked.
     */
    private Runnable watcherTask() {
        return new Runnable() {
                public void run() {
                    try {
                        if (root == null) {
                            return;
                        }

                        /** JarFile was modified => parse it and refresh existing FileObjects*/
                        if (root.lastModified() != lastModification) {
                            parse(true);
                        }
                    } finally {
                        /** reschedule watcherTask*/
                        Task w = watcherTask;
                        if (w != null) {
                            w.schedule(checkTime);
                        }
                    }
                }
            };
    }
    
    @Override
    final void waitRefreshed() {
        Task w = watcherTask;
        if (w != null) {
            w.waitFinished();
        }
    }

    /** Getter for entry.
    */
    private final JarEntry getEntry(String file) {
        JarFile j = null;

        try {
            synchronized (closeSync) {
                j = reOpenJarFile();

                JarEntry je = null;
                if (j != null) {
                    je = j.getJarEntry(file);
                }

                if (je != null) {
                    return je;
                }
            }
        } catch (IOException iox) {
        }

        return new JarEntry(file);
    }

    /**
     * @return the jar
     */
    private JarFile getJar(boolean create) {
        assert Thread.holdsLock(closeSync);
        if (jar == null && create) {
            try {
                if (root.canRead()) {
                    jar = new JarFile(root, false);
                    LOGGER.log(Level.FINE, "opened: {0} {1}", new Object[]{root.getAbsolutePath(), System.currentTimeMillis()}); //NOI18N
                    return jar;
                }
            } catch (ZipException ex) {
                dumpFDs();
                LOGGER.log(Level.INFO, ex.getMessage(), ex);
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, ex.getMessage(), ex);
            }
            LOGGER.log(Level.WARNING, "cannot open {0}", root.getAbsolutePath()); // NOI18N
        }
        return jar;
    }

    /**
     * @param jar the jar to set
     */
    private void setJar(JarFile jar) {
        this.jar = jar;
    }
    
    private static void dumpFDs() {
        if (BaseUtilities.isUnix()) {
            String selfName = ManagementFactory.getRuntimeMXBean().getName().replaceAll("@.*", ""); // NOI18N
            LOGGER.log(Level.INFO, "Dumping file descriptors for pid {0}", selfName); // NOI18N
            int pid;
            try {
                pid = Integer.parseInt(selfName);
            } catch (NumberFormatException ex) {
                LOGGER.info("Cannot get pid"); // NOI18N
                return;
            }
            File descriptors = new File("/proc/" + pid + "/fd"); // NOI18N
            final File[] arr = descriptors.listFiles();
            String size = arr == null ? "nothing" : (arr.length + " files"); // NOI18N
            LOGGER.log(Level.INFO, "There is {0} in {1}", new Object[]{size, descriptors}); // NOI18N
            if (arr != null) {
                for (File fd : arr) {
                    try {
                        LOGGER.log(Level.INFO, "{0} -> {1}", new Object[]{fd, fd.getCanonicalFile()}); // NOI18N
                    } catch (IOException ex) {
                        LOGGER.log(Level.INFO, "{0}", fd); // NOI18N
                    }
                }
            }
        }
    }

    /** Use soft-references to not throw away the data that quickly.
     * JarFS if often queried for its FOs e.g. by java parser, which
     * leaves the references immediately.
     */
    private class Ref<T extends FileObject> extends WeakReference<T> implements Runnable {
        public Ref(T fo) {
            super(fo, BaseUtilities.activeReferenceQueue());
        }

        // do the cleanup
        public void run() {
            freeReference();
        }
    }

    /** Implementation of all interfaces List, Change, Info and Attr
    * that delegates to JarFileSystem
    */
    public static class Impl extends Object implements AbstractFileSystem.List, AbstractFileSystem.Info,
        AbstractFileSystem.Change, AbstractFileSystem.Attr {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -67233308132567232L;

        /** the pointer to filesystem */
        private JarFileSystem fs;

        /** Constructor.
        * @param fs the filesystem to delegate to
        */
        public Impl(JarFileSystem fs) {
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

        /*
        * Get the file attribute with the specified name.
        * @param name the file
        * @param attrName name of the attribute
        * @return appropriate (serializable) value or <CODE>null</CODE> if the attribute is unset (or could not be properly restored for some reason)
        */
        public Object readAttribute(String name, String attrName) {
            return fs.readAttribute(name, attrName);
        }

        /*
        * Set the file attribute with the specified name.
        * @param name the file
        * @param attrName name of the attribute
        * @param value new value or <code>null</code> to clear the attribute. Must be serializable, although particular filesystems may or may not use serialization to store attribute values.
        * @exception IOException if the attribute cannot be set. If serialization is used to store it, this may in fact be a subclass such as {@link NotSerializableException}.
        */
        public void writeAttribute(String name, String attrName, Object value)
        throws IOException {
            fs.writeAttribute(name, attrName, value);
        }

        /*
        * Get all file attribute names for the file.
        * @param name the file
        * @return enumeration of keys (as strings)
        */
        public Enumeration<String> attributes(String name) {
            return fs.attributes(name);
        }

        /*
        * Called when a file is renamed, to appropriatelly update its attributes.
        * <p>
        * @param oldName old name of the file
        * @param newName new name of the file
        */
        public void renameAttributes(String oldName, String newName) {
            fs.renameAttributes(oldName, newName);
        }

        /*
        * Called when a file is deleted to also delete its attributes.
        *
        * @param name name of the file
        */
        public void deleteAttributes(String name) {
            fs.deleteAttributes(name);
        }
    }

    private static class Cache {
        private static final Set<JarEntry> EMPTY_SET = Collections.emptySet();
        static final Cache INVALID = new Cache(EMPTY_SET);
        byte[] names = new byte[1000];
        private int nameOffset = 0;
        int[] EMPTY = new int[0];
        private Map<String, Folder> folders = new HashMap<String, Folder>();

        public Cache(Set<JarEntry> entries) {
            parse(entries);
            trunc();
        }

        public boolean isFolder(String name) {
            return folders.get(name) != null;
        }

        public String[] getChildrenOf(String folder) {
            Folder fol = folders.get(folder);

            if (fol != null) {
                return fol.getNames();
            }

            return new String[] {  };
        }

        private void parse(Set<JarEntry> entries) {
            folders.put("", new Folder()); // root folder

            for (JarEntry entry : entries) {
                String name = entry.getName();
                boolean isFolder = false;

                // work only with slashes
                name = name.replace('\\', '/');

                if (name.startsWith("/")) {
                    name = name.substring(1); // NOI18N
                }

                if (name.endsWith("/")) {
                    name = name.substring(0, name.length() - 1); // NOI18N
                    isFolder = true;
                }

                int lastSlash = name.lastIndexOf('/');
                String dirName = ""; // root
                String realName = name;

                if (lastSlash > 0) {
                    dirName = name.substring(0, lastSlash); // or folder
                    realName = name.substring(lastSlash + 1);
                }

                if (isFolder) {
                    getFolder(name); // will create the folder item
                } else {
                    Folder fl = getFolder(dirName);
                    fl.addChild(realName);
                }
            }
        }

        private Folder getFolder(String name) {
            Folder fl = folders.get(name);

            if (fl == null) {
                // add all the superfolders on the way to the root
                int lastSlash = name.lastIndexOf('/');
                String dirName = ""; // root
                String realName = name;

                if (lastSlash > 0) {
                    dirName = name.substring(0, lastSlash); // or folder
                    realName = name.substring(lastSlash + 1);
                }

                getFolder(dirName).addChild(realName);

                fl = new Folder();
                folders.put(name, fl);
            }

            return fl;
        }

        private void trunc() {
            // strip the name array:
            byte[] newNames = new byte[nameOffset];
            System.arraycopy(names, 0, newNames, 0, nameOffset);
            names = newNames;

            // strip all the indices arrays:
            for (Folder folder : folders.values()) {
                folder.trunc();
            }
        }

        private int putName(byte[] name) {
            int start = nameOffset;

            if ((start + name.length) > names.length) {
                byte[] newNames = new byte[(names.length * 2) + name.length];
                System.arraycopy(names, 0, newNames, 0, start);
                names = newNames;
            }

            System.arraycopy(name, 0, names, start, name.length);
            nameOffset += name.length;

            return start;
        }

        private class Folder {
            private int[] indices = EMPTY;
            private int idx = 0;

            public Folder() {
            }

            public String[] getNames() {
                String[] ret = new String[idx / 2];

                for (int i = 0; i < ret.length; i++) {
                    byte[] name = new byte[indices[(2 * i) + 1]];
                    System.arraycopy(names, indices[2 * i], name, 0, name.length);

                    try {
                        ret[i] = new String(name, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new InternalError("No UTF-8");
                    }
                }

                return ret;
            }

            void addChild(String name) {
                // ensure enough space
                if ((idx + 2) > indices.length) {
                    int[] newInd = new int[(2 * indices.length) + 2];
                    System.arraycopy(indices, 0, newInd, 0, idx);
                    indices = newInd;
                }

                try {
                    byte[] bytes = name.getBytes("UTF-8");
                    indices[idx++] = putName(bytes);
                    indices[idx++] = bytes.length;
                } catch (UnsupportedEncodingException e) {
                    throw new InternalError("No UTF-8");
                }
            }

            void trunc() {
                if (indices.length > idx) {
                    int[] newInd = new int[idx];
                    System.arraycopy(indices, 0, newInd, 0, idx);
                    indices = newInd;
                }
            }
        }
    }
}
