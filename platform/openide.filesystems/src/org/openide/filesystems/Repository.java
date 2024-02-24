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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.jar.Manifest;
import java.util.logging.Level;
import static org.openide.filesystems.FileSystem.LOG;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbCollections;
import org.openide.util.io.NbMarshalledObject;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Holder for NetBeans default (system, configuration) filesystem, used for most
 * of NetBeans' runtime configuration. The default implementation is a merge
 * of all of the XML layer files provided by enabled modules, and
 * the {@code config} subfolder of the userdir on the user's disk.
 * If you just want to modify configuration data use e.g.
 * <pre>
 * FileObject menus = FileUtil.getConfigFile("Menu");
 * // ...
 * </pre>
 * Formerly (NB 3.x) contained a list of mounted filesystems. This functionality
 * is no longer used and is now deprecated.
 */
public class Repository implements Serializable {
    /** @GuardedBy(Repository.class) */
    private static Repository repository;
    
    /** Contributes to content of {@link #getDefaultFileSystem() system file system} 
     * (which influences structure under {@link FileUtil#getConfigRoot()}). The
     * method {@link #registerLayers(java.util.Collection)} 
     * is called during initialization of {@link Repository} and 
     * implementors (registered via {@link ServiceProvider} annotation) may
     * add their <em>layers</em> (later processed via {@link XMLFileSystem}) into
     * the general collection of existing providers.
     * <p class="nonnormative">
     * The list of <em>layers</em> as well as their content may be cached.
     * In a typical NetBeans Platform application, the cache remains until
     * list of <a href="@org-openide-modules@/org/openide/modules/ModuleInfo.html">modules</a>
     * and their enabled state remain the same. While it does, the {@link LayerProvider}s
     * are not queried again.
     * </p>
     * <p>You can show a dialog letting the user log in to some server, you can call
     * {@code LoginProvider.injectLayer(...)} with the URL to an XML layer.
     * The contents of the layer will become available only after login.
     * <pre>
     * {@code @}{@link ServiceProviders}({
     *     {@code @}{@link ServiceProvider}(service=LoginProvider.class),
     *     {@code @}{@link ServiceProvider}(service=LayerProvider.class)
     * })
     * public final class LoginProvider extends LayerProvider {
     *     private URL layer;
     * 
     *     public void registerLayers(Collection{@code <? super URL>} arr) {
     *         if (layer != null) {
     *             arr.add(layer);
     *         }
     *     }
     * 
     *     public static void injectLayer(URL u) {
     *         LoginProvider lp = {@link Lookup}.getDefault().lookup(LoginProvider.class);
     *         lp.url = u;
     *         lp.refresh();
     *     }
     * }
     * </pre>
     * The additional layers are inserted below traditional layers provided
     * by modules in so called {@link FileSystem fallback mode} - e.g. modules 
     * can override and redefine layers contributed by layer providers.
     * 
     * @since 7.59
     */
    public abstract static class LayerProvider {
        /** Allows providers to add their additions to the structure
         * beneath {@link FileUtil#getConfigRoot()}. The method is
         * supposed to collect all additional layers and {@link Collection#add(java.lang.Object) add}
         * them into the <code>context</code> collection. The provided
         * layers will be processed by {@link XMLFileSystem}-like manner later.
         * 
         * @param context the context where to register the additions
         */
        protected abstract void registerLayers(Collection<? super URL> context);
        
        /** Method to call when set of URLs returned from the {@link #registerLayers(java.util.Collection)}
         * method changed and there is a need to refresh it. Refresh is very likely 
         * a time consuming task - consider invoking it on a background thread and
         * don't hold any locks while calling the method
         */
        protected final void refresh() {
            Repository.getDefault().refreshAdditionalLayers();
        }
    }

    /** Methods that tells {@link Repository} subclasses to refresh list of
     * URLs provided by {@link LayerProvider}s.
     * @since 7.59
     */
    protected void refreshAdditionalLayers() {
        if (getDefaultFileSystem() instanceof MainFS) {
            ((MainFS)getDefaultFileSystem()).refreshLayers();
        }
    }
    
    /** Allows subclasses registered as {@link Repository#getDefault()} to 
     * find out list of URLs for a given provider. The method just calls
     * {@link LayerProvider#registerLayers(java.util.Collection)}.
     * 
     * @param p the provider.
     * @return ordered list of URLs
     * @since 7.59
     */
    protected final List<? extends URL> findLayers(LayerProvider p) {
        if (this != Repository.getDefault()) {
            return Collections.emptyList();
        }
        List<URL> urls = new ArrayList<URL>();
        p.registerLayers(urls);
        return urls;
    }
    
    /**
     * Allows subclasses to accept layer contributions from {@link Repository.LayerProvider}s.
     * Layer XMLs will be collected from locations specified in manifests accessible by the 
     * passed ClassLoader, including generated layers.
     * Finally {@link LayerProvider}s will be consulted to append their
     * URLs to the list. If the passed classloader is {@code null}, the classloader which
     * loaded the Repository class will be used.
     * @param layerUrls out: collection which receives the URLs.
     * @throws IOException propagated if some I/O error occurs.
     * 
     * @since 9.5
     */
    protected static final void provideLayers(ClassLoader l, List<URL> layerUrls) throws IOException {
        if (l == null) {
            l = Repository.class.getClassLoader();
        }
        for (URL manifest : NbCollections.iterable(l.getResources("META-INF/MANIFEST.MF"))) { // NOI18N
            InputStream is = manifest.openStream();
            try {
                Manifest mani = new Manifest(is);
                String layerLoc = mani.getMainAttributes().getValue("OpenIDE-Module-Layer"); // NOI18N
                if (layerLoc != null) {
                    URL layer = l.getResource(layerLoc);
                    if (layer != null) {
                        layerUrls.add(layer);
                    } else {
                        LOG.warning("No such layer: " + layerLoc);
                    }
                }
            } finally {
                is.close();
            }
        }
        for (URL generatedLayer : NbCollections.iterable(l.getResources("META-INF/generated-layer.xml"))) { // NOI18N
            layerUrls.add(generatedLayer);
        }
        for (LayerProvider p : Lookup.getDefault().lookupAll(LayerProvider.class)) {
            List<URL> newURLs = new ArrayList<URL>();
            p.registerLayers(newURLs);
            layerUrls.addAll(newURLs);
        }
    }

    private static final class MainFS extends MultiFileSystem implements LookupListener {
        private static final Lookup.Result<FileSystem> ALL = Lookup.getDefault().lookupResult(FileSystem.class);
        private static final FileSystem MEMORY = FileUtil.createMemoryFileSystem();
        private static final XMLFileSystem layers = new XMLFileSystem();

        public MainFS() {
            ALL.addLookupListener(this);
            refreshLayers();
        }
        
        final void refreshLayers() {
            List<URL> layerUrls = new ArrayList<URL>();
            try {
                provideLayers(Thread.currentThread().getContextClassLoader(), layerUrls);
                layers.setXmlUrls(layerUrls.toArray(new URL[0]));
                LOG.log(Level.FINE, "Loading classpath layers: {0}", layerUrls);
            } catch (Exception x) {
                LOG.log(Level.WARNING, "Setting layer URLs: " + layerUrls, x);
            }
            resultChanged(null); // run after add listener - see PN1 in #26338
        }

        private static FileSystem[] computeDelegates() {
            List<FileSystem> arr = new ArrayList<FileSystem>();
            arr.add(MEMORY);
            for (FileSystem f : ALL.allInstances()) {
                if (Boolean.TRUE.equals(f.getRoot().getAttribute("fallback"))) { // NOI18N
                    continue;
                }
                arr.add(f);
            }
            arr.add(layers);
            for (FileSystem f : ALL.allInstances()) {
                if (Boolean.TRUE.equals(f.getRoot().getAttribute("fallback"))) { // NOI18N
                    arr.add(f);
                }
            }
            return arr.toArray(new FileSystem[0]);
        }


        public void resultChanged(LookupEvent ev) {
            synchronized (Repository.class) {
                setDelegates(computeDelegates());
            }
        }
    } // end of MainFS

    static final long serialVersionUID = -6344768369160069704L;

    /** list of filesystems (FileSystem) */
    private ArrayList<FileSystem> fileSystems;
    private transient List<FileSystem> fileSystemsClone = Collections.emptyList();

    /** the system filesystem */
    private FileSystem system;

    /** hashtable that maps system names to FileSystems */
    private Hashtable<String, FileSystem> names;
    private transient FCLSupport fclSupport;

    // [PENDING] access to this hashtable is apparently not propertly synched
    // should use e.g. Collections.synchronizedSet, or just synch methods using it

    /** hashtable for listeners on changes in the filesystem.
    * Its elements are of type (RepositoryListener, RepositoryListener)
    */
    private Hashtable<RepositoryListener,RepositoryListener> listeners =
            new Hashtable<RepositoryListener,RepositoryListener>();

    /** vetoable listener on systemName property of filesystem */
    private VetoableChangeListener vetoListener = new VetoableChangeListener() {
            /** @param ev event with changes */
            public void vetoableChange(PropertyChangeEvent ev)
            throws PropertyVetoException {
                if (ev.getPropertyName().equals("systemName")) {
                    final String ov = (String) ev.getOldValue();
                    final String nv = (String) ev.getNewValue();

                    if (names.get(nv) != null) {
                        throw new PropertyVetoException("system name already exists: " + ov + " -> " + nv, ev); // NOI18N
                    }
                }
            }
        };

    /** property listener on systemName property of filesystem */
    private PropertyChangeListener propListener = new PropertyChangeListener() {
            /** @param ev event with changes */
            public void propertyChange(PropertyChangeEvent ev) {
                if (ev.getPropertyName().equals("systemName")) {
                    // assign the property to new name
                    String ov = (String) ev.getOldValue();
                    String nv = (String) ev.getNewValue();
                    FileSystem fs = (FileSystem) ev.getSource();

                    if (fs.isValid()) {
                        // when a filesystem is valid then it is attached to a name
                        names.remove(ov);
                    }

                    // register name of the filesystem
                    names.put(nv, fs);

                    // the filesystem becomes valid
                    fs.setValid(true);
                }
            }
        };

    /** Creates new instance of filesystem pool and
    * registers it as the default one. Also registers the default filesystem.
    *
    * @param def the default filesystem
    */
    public Repository(FileSystem def) {
        this.system = def;
        init();
    }
    
    /** Initialazes the pool.
    */
    private void init() {
        // empties the pool
        fileSystems = new ArrayList<FileSystem>();
        names = new Hashtable<String, FileSystem>();
        if (addFileSystemDelayed(system)) {
            addFileSystem(system);
        }
    }

    /** Access method to get default instance of repository in the system.
     * The instance is either taken as a result of
     * <CODE>org.openide.util.Lookup.getDefault ().lookup (Repository.class)</CODE>
     * or (if the lookup query returns null) a default instance is created.
     * <p>
     * In a contextual environment, the method remembers the result of the default Lookup
     * query, and will return the same instance as a system-wide Repository instance.
     * Instances provided by <code>Lookup.getDefault().lookup(Repository.class)</code> may vary
     * depending on the Lookup's implementation and context - be aware that multiple Repository
     * instances may exist, possibly one for each contextual Lookup craeted.
     * <p>
     * 
     * @return default repository for the system
     * @since 9.5 support for multiple contexts
     */
    public static Repository getDefault() {
        final Lookup lkp = Lookup.getDefault();

        synchronized (Repository.class) {
            if (repository != null) {
                return repository;
            }
        }
        try {
            Repository newRepo = delayFileSystemAttachImpl(new Callable<Repository>() {
                public Repository call() throws Exception {
                    Repository r = lkp.lookup(Repository.class);
                    if (r == null) {
                        // if not provided use default one
                        r = new Repository(new MainFS());
                    }
                    return r;
                }
            });
            synchronized (Repository.class) {
                if (repository == null) {
                    repository = newRepo;
                }
                return repository;
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Last known Lookup instance, for which the {@link #lastLocalProvider} holds a value.
     */
    private static Reference<Lookup>    lastDefLookup = new WeakReference<>(null);

    /**  
     * LocalProvider associated with the {@link #lastDefLookup}.
     * The Provider is cached rather than the Repository instance, to avoid
     * several locations where Repository is cached - testsuites may recycle instances
     * and would have to be updated if another cache is created.
     */
    private static LocalProvider lastLocalProvider = null; 
    
    /**
     * Provides a safe initialization for repository in a context Lookup.
     * Because of delayed FS initialization guarded by {@link #ADD_FS}, it's not safe
     * to directly lookup Repository.class in the default Lookup; if an instance does not
     * exist yet and will be created within the lookup() call, it would obscure possible 
     * existing ADD_FS value. In fact, the Repository instance <b>is usually instantiated twice</b>,
     * because ctor initialization indirectly invokes Repository.getDefault(), resulting in another
     * initialization.
     * <p>
     * <b>Performance note:</b> during startup, the default Lookup changes frequently because of modules
     * activations. The <code>lkp.lookup(LocalProvider.class)</code> query blocks on Lookup computation
     * locks, adding up to 20% to startup time. The last discovered value of LocalProvider is therefore cached
     * ({@link #lastLocalProvider}) and the default Lookup in effect is remembered in {@link #lastDefLookup}.
     * If {@code getLocalRepository} is called again with the default Lookup instance unchanged (= the same execution context),
     * no {@code lookup()} query will be executed and the cached {@code LocalProvider} instance will be
     * used to return the repository instance.
     * <p>
     * In multi-user/execution environment, this cache is beneficial during system startup; after that, the
     * default Lookup changes with the execution context frequently, but contents of individual Lookups are rather
     * stable, so performance should be acceptable.
     * 
     * @return Repository instance
     */
    /* package private */ static Repository getLocalRepository() {
        Lookup lkp = Lookup.getDefault();
        Reference<Lookup> c;
        LocalProvider p = null;
        LocalProvider q;
        synchronized (Repository.class) {
            c = lastDefLookup;
            Lookup l = c.get();
            if (lkp == l) {
                p = lastLocalProvider;
            }
            q = p;
        }
        if (p == null) {
            q = lkp.lookup(LocalProvider.class);
            if (q == null) {
                q = NO_PROVIDER;
            }
        }
        Repository inst = null;
        try {
            inst = q.getRepository();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        if (p == null) {
            synchronized (Repository.class) {
                if (lastDefLookup == c) {
                    lastLocalProvider = q;
                    lastDefLookup = new WeakReference<>(lkp);
                }
            }
        }
        return inst;
    }
    
    private static final LocalProvider NO_PROVIDER = new LocalProvider() {
        @Override
        public Repository getRepository() throws IOException {
            return getDefault();
        }
    };
    
    
    static synchronized void reset() {
        repository = null;
        lastLocalProvider = null;
        lastDefLookup = new WeakReference<>(null);
    }
    private static final ThreadLocal<FileSystem[]> ADD_FS = new ThreadLocal<FileSystem[]>();
    private static boolean addFileSystemDelayed(FileSystem fs) {
        FileSystem[] store = ADD_FS.get();
        if (store != null) {
            assert store[0] == null;
            store[0] = fs;
            return false;
        } else {
            return true;
        }
    }

    private boolean addFileSystemImpl(FileSystem fs) {
        Thread.holdsLock(this);

        // if the filesystem is not assigned yet
        if (!fs.assigned && !fileSystems.contains(fs)) {
            // new filesystem
            fs.setRepository(this);
            fileSystems.add(fs);
            fileSystemsClone = new ArrayList<FileSystem>(fileSystems);

            String systemName = fs.getSystemName();

            boolean isReg = names.get(systemName) == null;

            if (isReg && !systemName.equals("")) { // NOI18N

                // filesystem with the same name is not there => then it is valid
                names.put(systemName, fs);
                fs.setValid(true);
            } else {
                // there is another filesystem with the same name => it is invalid
                fs.setValid(false);
            }

            // mark the filesystem as being assigned
            fs.assigned = true;

            // mark as a listener on changes in the filesystem
            fs.addPropertyChangeListener(propListener);
            fs.addVetoableChangeListener(vetoListener);

            // notify filesystem itself that it has been added
            fs.addNotify();

            // fire info about new filesystem
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the NetBeans default (system, configuration) filesystem.
     * @return the default filesystem
     * @deprecated Please use {@link FileUtil#getConfigFile(String)} or
     * {@link FileUtil#getConfigRoot()} instead.
     */
    @Deprecated
    public final FileSystem getDefaultFileSystem() {
        return system;
    }

    /** Adds new filesystem to the pool.
    * <em>Note</em> that a filesystem cannot be assigned to more than one file
    *   system pool at one time (though currently there is only one pool anyway).
    * At any given time, no two filesystems in the pool may share the same {@link FileSystem#getSystemName name}
    * (unless all but one are {@link FileSystem#isValid invalid}). To be sure, that
    * filesystem was really added in Repository, then test that <code>FileSystem</code>
    * is valid.
    * @param fs filesystem to add
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final void addFileSystem(FileSystem fs) {
        boolean fireIt;
        synchronized (this) {
            fireIt = addFileSystemImpl(fs);
        }
        // postponed firing after synchronized  block to prevent deadlock
        if (fireIt) {
            fireFileSystem(fs, true);
        }
    }

    /** Removes a filesystem from the pool.
    * @param fs filesystem to remove
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final void removeFileSystem(FileSystem fs) {
        boolean fireIt = false;

        synchronized (this) {
            if (fs.isDefault()) {
                return;
            }

            if (fireIt = fileSystems.remove(fs)) {
                fs.setRepository(null);
                fileSystemsClone = new ArrayList<FileSystem>(fileSystems);

                // the filesystem realy was here
                if (fs.isValid()) {
                    // if the filesystem is valid then is in names hashtable
                    names.remove(fs.getSystemName());
                    fs.setValid(false);
                }

                // in all cases remove it from listeners
                fs.removePropertyChangeListener(propListener);
                fs.removeVetoableChangeListener(vetoListener);

                // notify filesystem itself that it has been removed
                fs.removeNotify();
            }

            // unassign the filesystem
            fs.assigned = false;
        }

        // postponed firing after synchronized  block to prevent deadlock
        if (fireIt) {
            fireFileSystem(fs, false);
        }
    }

    /** Reorders {@link FileSystem}s by given permutation.
     * For example, if there are three filesystems, <code>new int[] {2, 0, 1}</code> cycles the filesystems forwards.
    * @param perm an array of integers
    * @throws IllegalArgumentException if the array is not a permutation, or is not the same length as the current number of filesystems in the pool
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final void reorder(int[] perm) {
        synchronized (this) {
            if (perm == null) {
                throw new IllegalArgumentException("null permutation"); // NOI18N
            } else if (perm.length != fileSystems.size()) {
                throw new IllegalArgumentException(
                    "permutation is wrong size: " + perm.length + " elements but should be " + fileSystems.size()
                ); // NOI18N
            } else if (!isPermutation(perm)) {
                StringBuffer message = new StringBuffer("permutation is not really a permutation:"); // NOI18N

                for (int i = 0; i < perm.length; i++) {
                    message.append(' ');
                    message.append(perm[i]);
                }

                throw new IllegalArgumentException(message.toString());
            }

            ArrayList<FileSystem> newList = new ArrayList<FileSystem>(fileSystems.size());
            int len = perm.length;

            for (int i = 0; i < len; i++) {
                newList.add(fileSystems.get(perm[i]));
            }

            fileSystems = newList;
            fileSystemsClone = new ArrayList<FileSystem>(fileSystems);
        }

        fireFileSystemReordered(perm);
    }

    /** @return true if the parameter describes a permutation */
    private static boolean isPermutation(int[] perm) {
        final int len = perm.length;
        boolean[] bool = new boolean[len];

        try {
            for (int i = 0; i < len; i++) {
                if (bool[perm[i]]) {
                    return false;
                } else {
                    bool[perm[i]] = true;
                }
            }

            return true;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    /** Returns enumeration of all filesystems.
    * @return enumeration of type {@link FileSystem}
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final Enumeration<? extends FileSystem> getFileSystems() {
        return Collections.enumeration(fileSystemsClone);
    }

    /** Returns enumeration of all filesystems.
    * @return enumeration of type {@link FileSystem}
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final Enumeration<? extends FileSystem> fileSystems() {
        return getFileSystems();
    }

    /**
     * Returns a sorted array of filesystems.
     * @return a sorted array of filesystems
     * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
     */
    @Deprecated
    public final FileSystem[] toArray() {
        List<FileSystem> tempFileSystems = fileSystemsClone;

        FileSystem[] fss = new FileSystem[tempFileSystems.size()];
        tempFileSystems.toArray(fss);

        return fss;
    }

    /** Finds filesystem when only its system name is known.
    * @param systemName {@link FileSystem#getSystemName name} of the filesystem
    * @return the filesystem or <CODE>null</CODE> if there is no such
    *   filesystem
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final FileSystem findFileSystem(String systemName) {
        FileSystem fs = names.get(systemName);

        return fs;
    }

    /** Saves pool to stream by saving all filesystems.
    * The default (system) filesystem, or any persistent filesystems, are skipped.
    *
    * @param oos object output stream
    * @exception IOException if an error occures
    * @deprecated Unused.
    */
    @Deprecated
    public final synchronized void writeExternal(ObjectOutput oos)
    throws IOException {
        for (FileSystem fs : fileSystems) {
            if (!fs.isDefault()) {
                oos.writeObject(new NbMarshalledObject(fs));
            }
        }

        oos.writeObject(null);
    }

    /** Reads object from stream.
    * Reads all filesystems. Persistent and system filesystems are untouched; all others are removed and possibly reread.
    * @param ois object input stream
    * @exception IOException if an error occures
    * @exception ClassNotFoundException if read class is not found
    * @deprecated Unused.
    */
    @Deprecated
    public final synchronized void readExternal(ObjectInput ois)
    throws IOException, ClassNotFoundException {
        ArrayList<FileSystem> temp = new ArrayList<FileSystem>(10);

        for (;;) {
            Object obj = ois.readObject();

            if (obj == null) {
                // all system has been read in
                break;
            }

            FileSystem fs;

            if (obj instanceof FileSystem) {
                fs = (FileSystem) obj;
            } else {
                try {
                    NbMarshalledObject mar = (NbMarshalledObject) obj;
                    fs = (FileSystem) mar.get();
                } catch (IOException ex) {
                    ExternalUtil.exception(ex);
                    fs = null;
                } catch (ClassNotFoundException ex) {
                    ExternalUtil.exception(ex);
                    fs = null;
                }
            }

            if (fs != null) {
                // add the new filesystem
                temp.add(fs);
            }
        }

        Enumeration<? extends FileSystem> ee = getFileSystems();
        FileSystem fs;

        while (ee.hasMoreElements()) {
            fs = ee.nextElement();

            if (!fs.isDefault()) {
                removeFileSystem(fs);
            }
        }

        // in init assigned is checked and we force 'system' to be added again
        system.assigned = false;
        init();

        // all is successfuly read
        for (FileSystem fileSystem : temp) {
            addFileSystem(fileSystem);
        }
    }

    /** Finds file when its name is provided. It scans in the list of
    * filesystems and asks them for the specified file by a call to
    * {@link FileSystem#find find}. The first object that is found is returned or <CODE>null</CODE>
    * if none of the filesystems contain such a file.
    *
    * @param aPackage package name where each package is separated by a dot
    * @param name name of the file (without dots) or <CODE>null</CODE> if
    *    one wants to obtain the name of a package and not a file in it
    * @param ext extension of the file or <CODE>null</CODE> if one needs
    *    a package and not a file name
    *
    * @return {@link FileObject} that represents file with given name or
    *   <CODE>null</CODE> if the file does not exist
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final FileObject find(String aPackage, String name, String ext) {
        assert false : "Deprecated.";

        Enumeration<? extends FileSystem> en = getFileSystems();

        while (en.hasMoreElements()) {
            FileSystem fs = en.nextElement();
            FileObject fo = fs.find(aPackage, name, ext);

            if (fo != null) {
                // object found
                return fo;
            }
        }

        return null;
    }

    /** Searches for the given resource among all filesystems.
    * @see FileSystem#findResource
    * @param name a name of the resource
    * @return file object or <code>null</code> if the resource can not be found
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final FileObject findResource(String name) {
        assert false : "Deprecated.";

        Enumeration<? extends FileSystem> en = getFileSystems();

        while (en.hasMoreElements()) {
            FileSystem fs = en.nextElement();
            FileObject fo = fs.findResource(name);

            if (fo != null) {
                // object found
                return fo;
            }
        }

        return null;
    }

    /** Searches for the given resource among all filesystems, returning all matches.
    * @param name name of the resource
    * @return enumeration of {@link FileObject}s
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final Enumeration<? extends FileObject> findAllResources(String name) {
        assert false : "Deprecated.";

        Vector<FileObject> v = new Vector<FileObject>(8);
        Enumeration<? extends FileSystem> en = getFileSystems();

        while (en.hasMoreElements()) {
            FileSystem fs = en.nextElement();
            FileObject fo = fs.findResource(name);

            if (fo != null) {
                v.addElement(fo);
            }
        }

        return v.elements();
    }

    /** Finds all files among all filesystems matching a given name, returning all matches.
    * All filesystems are queried with {@link FileSystem#find}.
    *
    * @param aPackage package name where each package is separated by a dot
    * @param name name of the file (without dots) or <CODE>null</CODE> if
    *    one wants to obtain the name of a package and not a file in it
    * @param ext extension of the file or <CODE>null</CODE> if one needs
    *    a package and not a file name
    *
    * @return enumeration of {@link FileObject}s
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final Enumeration<? extends FileObject> findAll(String aPackage, String name, String ext) {
        assert false : "Deprecated.";

        Enumeration<? extends FileSystem> en = getFileSystems();
        Vector<FileObject> ret = new Vector<FileObject>();

        while (en.hasMoreElements()) {
            FileSystem fs = (FileSystem) en.nextElement();
            FileObject fo = fs.find(aPackage, name, ext);

            if (fo != null) {
                ret.addElement(fo);
            }
        }

        return ret.elements();
    }

    /** Fire info about changes in the filesystem pool.
    * @param fs filesystem
    * @param add <CODE>true</CODE> if the filesystem is added,
    *   <CODE>false</CODE> if it is removed
    */
    private void fireFileSystem(FileSystem fs, boolean add) {
        RepositoryEvent ev = new RepositoryEvent(this, fs, add);
        for (RepositoryListener list : new HashSet<RepositoryListener>(listeners.values())) {
            if (add) {
                list.fileSystemAdded(ev);
            } else {
                list.fileSystemRemoved(ev);
            }
        }
    }

    /** Fires info about reordering
    * @param perm
    */
    private void fireFileSystemReordered(int[] perm) {
        RepositoryReorderedEvent ev = new RepositoryReorderedEvent(this, perm);
        for (RepositoryListener list : new HashSet<RepositoryListener>(listeners.values())) {
            list.fileSystemPoolReordered(ev);
        }
    }

    /** Adds new listener.
    * @param list the listener
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final void addRepositoryListener(RepositoryListener list) {
        listeners.put(list, list);
    }

    /** Removes listener.
    * @param list the listener
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final void removeRepositoryListener(RepositoryListener list) {
        listeners.remove(list);
    }

    /** Writes the object to the stream.
    */
    private Object writeReplace() {
        return new Replacer();
    }

    final FCLSupport getFCLSupport() {
        synchronized (FCLSupport.class) {
            if (fclSupport == null) {
                fclSupport = new FCLSupport();
            }
        }

        return fclSupport;
    }

    /** Add new listener to this object.
    * @param fcl the listener
    * @since 2.8
    * @deprecated useless because there is no filesystem but only
    * default filesystem in Repository. Add new listener directly to
    * default filesystem {@link #getDefaultFileSystem}.
    */
    @Deprecated
    public final void addFileChangeListener(FileChangeListener fcl) {
        getFCLSupport().addFileChangeListener(fcl);
    }

    /** Remove listener from this object.
    * @param fcl the listener
    * @since 2.8
    * @deprecated useless because there is no filesystem but only
    * default filesystem in Repository. Add new listener directly to
    * default filesystem {@link #getDefaultFileSystem}.
    */
    @Deprecated
    public final void removeFileChangeListener(FileChangeListener fcl) {
        getFCLSupport().removeFileChangeListener(fcl);
    }

    private static class Replacer implements java.io.Serializable {
        /** serial version UID */
        static final long serialVersionUID = -3814531276726840241L;

        Replacer() {
        }

        private void writeObject(ObjectOutputStream oos)
        throws IOException {
            getDefault().writeExternal(oos);
        }

        private void readObject(ObjectInputStream ois)
        throws IOException, ClassNotFoundException {
            getDefault().readExternal(ois);
        }

        /** @return the default pool */
        public Object readResolve() {
            return getDefault();
        }
    }
    
    private static Repository delayFileSystemAttachImpl(Callable<Repository> init) throws IOException {
        FileSystem[] previous = ADD_FS.get();
        try {
            FileSystem[] addLater = new FileSystem[1];
            ADD_FS.set(addLater);
            Repository newRepo = init.call();
            if (newRepo == null) {
                return null;
            }
            synchronized (newRepo) {
                if (addLater[0] instanceof FileSystem) {
                    newRepo.addFileSystemImpl(addLater[0]);
                }
            }
            return newRepo;
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException(ex);
        } finally {
            ADD_FS.set(previous);
        }
    }
    
    /**
     * Provides local repositories, depending on the current execution environment.
     * The caller may use {@link org.openide.util.lookup.Lookups#executeWith(org.openide.util.Lookup, java.lang.Runnable)} to temporarily modify the default
     * Lookup contents and route Repository requests to a Repository private to some task. The
     * {@code LocalProvider} is responsible for caching and lifecycle of the Repository instance;
     * it can expire the Repository if the Provider's execution context terminates. Implementations
     * must be prepared to handle requests for Repository already shutdown etc.
     * @since 9.5
     */
    public abstract static class LocalProvider {
        /**
         * Returns the local repository instance. The method can return {@code null} to indicate
         * the main Repository should be used. The method <b>must</b> return the very same instance
         * of Repository for the whole duration of the execution context. It must return the same 
         * Repository instance even after the execution context shuts down; the contents of Repository and
         * its FileSystems may be limited in that case.
         * <p>
         * Implementations must be reentrant (a call to {@code getLocalRepository} can be made during 
         * construction of the Repository instance) and must not recurse infinitely.
         * 
         * @return local repository instance.
         */
        public abstract Repository  getRepository() throws IOException;

        /**
         * Allows to delay attaching filesystems to the Repository. For backwards compatibility, the Repository
         * constructor still takes the default FileSystem instance. However it is better to actually publish the
         * FileSystem in the repository only after it is registered, and can be obtained using {@link #getDefault()} or
         * {@code getLocalRepository()}.
         * <p>
         * This method wraps the Repository creation so that the default FileSystem is attached after the repository
         * is fully constructed. The `init' callable must create a new Repository instance.
         * This method will then add the default FS for newly created repository instances.
         * 
         * @param init Callable which produces a new Repository instance or {@code null} to abort creation
         * @return new Repository instance or {@code null}, if no instance was created.
         * @throws IOException if the Callable fails. All exceptions are wrapped into {@link IOException}.
         */
        protected final Repository delayFilesystemAttach(Callable<Repository> init) throws IOException {
            // just a trampoline to the real implementation
            return delayFileSystemAttachImpl(init);
        }
    }
}
