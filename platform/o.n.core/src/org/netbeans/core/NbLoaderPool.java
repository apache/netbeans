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

package org.netbeans.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import org.netbeans.core.startup.ManifestSection;
import org.netbeans.core.startup.StartLog;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.modules.SpecificationVersion;
import org.openide.util.*;
import org.openide.util.io.NbMarshalledObject;
import org.openide.util.io.NbObjectInputStream;
import org.openide.util.io.NbObjectOutputStream;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=DataLoaderPool.class)
public final class NbLoaderPool extends DataLoaderPool implements PropertyChangeListener, Runnable, LookupListener, TaskListener {
    private static final Logger err = Logger.getLogger(NbLoaderPool.class.getName()); // NOI18N

    /**
     * Currently managed data loaders.
     * @see org.openide.loaders.AddLoaderManuallyHid
     */
    private static List<DataLoader> loaders = new ArrayList<DataLoader> ();
    /** Those which have been modified since being read from the pool */
    private static Set<DataLoader> modifiedLoaders = new HashSet<DataLoader>();
    /** Loaders by class name */
    private static Map<String,DataLoader> names2Loaders = new HashMap<String,DataLoader>(200);
    /** Loaders by representation class name */
    private static Map<String,DataLoader> repNames2Loaders = new HashMap<String,DataLoader>(200);

    /** Map from loader class names to arrays of class names for Install-Before's */
    private static Map<String,String[]> installBefores = new HashMap<String,String[]> ();
    /** Map from loader class names to arrays of class names for Install-After's */
    private static Map<String,String[]> installAfters = new HashMap<String,String[]> ();

    /**
     * Copy of the loaders to prevent copying.
     * @see org.openide.loaders.AddLoaderManuallyHid
     */
    private static DataLoader[] loadersArray;

    /** true if changes in loaders should be notified */
    private static boolean installationFinished = false;
    
    /** if true, we are adding/removing a bunch of loaders; resort later */
    private static boolean updatingBatch = false;
    /** see above; true if at least one change */
    private static boolean updatingBatchUsed = false;

    public static synchronized void beginUpdates() {
        updatingBatch = true;
        updatingBatchUsed = false;
    }
    public static void endUpdates() {
        NbLoaderPool pool = getNbLoaderPool();
        synchronized (NbLoaderPool.class) {
            if (!updatingBatch) throw new IllegalStateException();
            updatingBatch = false;
            if (updatingBatchUsed) {
                updatingBatchUsed = false;
                resort(pool);
            }
        }
    }
    
    /** Allows tests to wait while processing of events is finished.
     */
    public static void waitFinished() {
        getNbLoaderPool().fireTask.waitFinished();
    }

    /** Adds new loader when previous and following are specified.
    * An attempt will be made to (re-)order the loader pool according to specified
    * dependencies.
    * <p>If a loader of the same class already existed in the pool, that will be <b>removed</b>
    * and replaced with the new one.
    * @param s adds loader section
    */
    public static void add (ManifestSection.LoaderSection s) throws Exception {
        // the instantiation of the loader is done outside of synchronized block,
        // because foreign code is called and can cause deadlocks
        DataLoader l = (DataLoader)s.getInstance ();
        doAdd (l, s, getNbLoaderPool());
    }

    /** Really adds the loader.
     */
    static synchronized void doAdd (DataLoader l, ManifestSection.LoaderSection s, NbLoaderPool pool) throws Exception {
        if (err.isLoggable(Level.FINE) && s != null) {
            List before = s.getInstallBefore() == null ? null : Arrays.asList(s.getInstallBefore());
            List after = s.getInstallAfter() == null ? null : Arrays.asList(s.getInstallAfter());
            err.fine("add: " + l + " repclass: " + l.getRepresentationClass().getName() + " before: " + before + " after: " + after);
        }
        Iterator it = loaders.iterator ();
        Class c = l.getClass();
        while (it.hasNext ()) {
            if (it.next ().getClass () == c) {
                it.remove ();
                break;
            }
        }
        loaders.add (l);
        l.removePropertyChangeListener (pool);
        l.addPropertyChangeListener (pool);
        
        String cname = c.getName();
        names2Loaders.put(cname, l);
        repNames2Loaders.put(l.getRepresentationClassName(), l);
        if (s != null) {
            String[] ib = s.getInstallBefore();
            if (ib != null) installBefores.put(cname, ib);
            String[] ia = s.getInstallAfter();
            if (ia != null) installAfters.put(cname, ia);
        }
        if (updatingBatch) {
            updatingBatchUsed = true;
        } else {
            resort (pool);
        }
    }


    /** Resort the loader pool according to stated dependencies.
    * Attempts to keep a stable order whenever possible, i.e. more-recently-installed
    * loaders will tend to stay near the end unless they need to be moved forward.
    * Note that dependencies on nonexistent (or unloadable) representation classes are simply
    * ignored and have no effect on ordering.
    * If there is a cycle (contradictory set of dependencies) in the loader pool,
    * its order is not changed.
    * In any case, a change event is fired afterwards.
    */
    private static synchronized void resort (NbLoaderPool pool) {
        // A partial ordering over loaders based on their Install-* tags:
        Map<DataLoader,List<DataLoader>> deps = new HashMap<DataLoader,List<DataLoader>>();
        add2Deps(deps, installBefores, true);
        add2Deps(deps, installAfters, false);
        if (err.isLoggable(Level.FINE)) {
            err.fine("Before sort: " + loaders);
        }
        
        try {
            loaders = Utilities.topologicalSort(loaders, deps);
            if (err.isLoggable(Level.FINE)) {
                err.fine("After sort: " + loaders);
            }
        } catch (TopologicalSortException ex) {
            err.log(Level.WARNING, null, ex);
            err.warning("Contradictory loader ordering: " + deps); // NOI18N
        }
        update (pool);
    }
    /**
     * Add to loader ordering dependencies.
     * Only pays attention to dependencies among loaders that actually exist.
     * @param deps a map from loaders to lists of loaders they must come before
     * @param orderings either {@link #installBefore} or {@link #installAfter}
     * @param before true if orderings refers to before, false if to after
     * @see Utilities#topologicalSort
     */
    private static void add2Deps(Map<DataLoader,List<DataLoader>> deps, Map orderings, boolean before) {
        Iterator it = orderings.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            String loaderClassName = (String) e.getKey();
            DataLoader l = names2Loaders.get(loaderClassName);

            if (l == null) {
                throw new IllegalStateException("No such loader: " + loaderClassName); // NOI18N
            }
            String[] repClassNames = (String[]) e.getValue();

            if (repClassNames == null) {
                throw new IllegalStateException("Null Install-" + (before ? "Before" : "After") + " for " + loaderClassName); // NOI18N
            }
            for (int i = 0; i < repClassNames.length; i++) {
                String repClassName = repClassNames[i];
                DataLoader l2 = repNames2Loaders.get(repClassName);

                if (l2 != null) {
                    if (before) {
                        addDep(deps, l, l2);
                    }
                    else {
                        addDep(deps, l2, l);
                    }
                }
                else {
                    l2 = names2Loaders.get(repClassName);
                    if (l2 != null) {
                        warn(loaderClassName, repClassName,
                             l2.getRepresentationClassName());
                    }
                }
            }
        }
    }
    /**
     * Add one loader ordering dependency.
     * @param deps see {@link #add2Deps}
     * @param a the earlier loader
     * @param b the later loader
     */
    private static void addDep(Map<DataLoader,List<DataLoader>> deps, DataLoader a, DataLoader b) {
        List<DataLoader> l = deps.get(a);
        if (l == null) {
            deps.put(a, l = new LinkedList<DataLoader>());
        }
        if (!l.contains(b)) {
            l.add(b);
        }
    }
    /**
     * Warn about misuse of Install-{After,Before} to refer to loader class names rather
     * than representation class names.
     */
    private static void warn(String yourLoader, String otherLoader, String otherRepn) {
        err.warning("Warning: a possible error in the manifest containing " + yourLoader + " was found."); // NOI18N
        err.warning("The loader specified an Install-{After,Before} on " + otherLoader + ", but this is a DataLoader class."); // NOI18N
        err.warning("Probably you wanted " + otherRepn + " which is the loader's representation class."); // NOI18N
    }

    /** Notification to finish installation of nodes during startup.
    */
    static void installationFinished () {
        installationFinished = true;
        
        if (!modifiedLoaders.isEmpty()) {
            getNbLoaderPool().superFireChangeEvent();
        }
    }
    
    /** Checks whether a loader is modified. E.g. whether the loader
     * considers it to be modified and necessary to be saved.
     */
    static synchronized boolean isModified (DataLoader l) {
        return modifiedLoaders.contains (l);
    }

    /** Stores all the objects into stream.
    * @param oos object output stream to write to
    */
    private static synchronized void writePool (ObjectOutputStream oos, NbLoaderPool pool)
    throws IOException {
        if (err.isLoggable(Level.FINE)) err.fine("writePool");
        // No longer bother storing these (#29671):
        oos.writeObject (new HashMap()/*installBefores*/);
        oos.writeObject (new HashMap()/*installAfters*/);
        
        Iterator it = loaders.iterator ();

        while (it.hasNext ()) {
            DataLoader l = (DataLoader)it.next ();
            
            if (!isModified (l)) {
                // #27190 - no real need to write this in detail.
                String c = l.getClass().getName();
                if (err.isLoggable(Level.FINE)) err.fine("writing unmodified " + c);
                // '=' not a permissible part of a cnb, so this distinguishes it
                oos.writeObject("=" + c); // NOI18N
                continue;
            }

            NbMarshalledObject obj;
            try {
                obj = new NbMarshalledObject (l);
            } catch (IOException ex) {
                err.log(Level.WARNING, null, ex);
                obj = null;
            }

            if (obj != null) {
                if (err.isLoggable(Level.FINE)) err.fine("writing modified " + l.getClass().getName());
                // Find its module, if any.
                boolean found = false;
                ModuleInfo m = Modules.getDefault().ownerOf(l.getClass());
                if (m != null && m.isEnabled()) {
                        if (err.isLoggable(Level.FINE)) err.fine("belongs to module: " + m.getCodeNameBase());
                        oos.writeObject(m.getCodeNameBase());
                        int r = m.getCodeNameRelease();
                        oos.writeInt(r); // might be -1, note
                        SpecificationVersion v = m.getSpecificationVersion();
                        if (v != null) {
                            oos.writeObject(v.toString());
                        } else {
                            oos.writeObject(null);
                        }
                        found = true;
                }
                if (!found) {
                    if (err.isLoggable(Level.FINE)) err.fine("does not belong to any module");
                    // just write the NbMarshalledObject<DataLoader> itself;
                    // we need to support that for compatibility of old loader
                    // pools anyway
                }
                oos.writeObject (obj);
            }
        }
        if (err.isLoggable(Level.FINE)) err.fine("writing null");
        oos.writeObject (null);

        // Write out system loaders now:
        Enumeration e = pool.allLoaders ();
        while (e.hasMoreElements ()) {
            DataLoader l = (DataLoader) e.nextElement ();
            if (loaders.contains (l)) continue;
            if (!isModified (l)) {
                // #27190 again. No need to write anything
                String c = l.getClass().getName();
                if (err.isLoggable(Level.FINE)) err.fine("skipping unmodified " + c);
                continue;
            }
            NbMarshalledObject obj;
            try {
                obj = new NbMarshalledObject (l);
            } catch (IOException ex) {
                err.log(Level.WARNING, null, ex);
                obj = null;
            }
            if (obj != null) {
                if (err.isLoggable(Level.FINE)) err.fine("writing " + l.getClass().getName());
                // No associated module, no need to write such info.
                oos.writeObject (obj);
            }
        }
        if (err.isLoggable(Level.FINE)) err.fine("writing null");
        oos.writeObject (null);

        if (err.isLoggable(Level.FINE)) err.fine("done writing");
    }

    /** Reads loader from the input stream.
    * @param ois object input stream to read from
    */
    private static synchronized void readPool (ObjectInputStream ois, NbLoaderPool pool)
    throws IOException, ClassNotFoundException {
        /*installBefores = (Map)*/ois.readObject ();
        /*installAfters = (Map)*/ois.readObject ();

        HashSet<Class> classes = new HashSet<Class> ();
        LinkedList<DataLoader> l = new LinkedList<DataLoader> ();
        
        Iterator<? extends ModuleInfo> mit = Lookup.getDefault().lookupAll(ModuleInfo.class).iterator();
        Map<String,ModuleInfo> modules = new HashMap<String,ModuleInfo>();
        while (mit.hasNext()) {
            ModuleInfo m = mit.next();
            modules.put(m.getCodeNameBase(), m);
        }

        for (;;) {
            Object o1 = ois.readObject();

            if (o1 == null) {
                if (err.isLoggable(Level.FINE))
                    err.fine("reading null");
                break;
            }
            NbMarshalledObject obj;

            if (o1 instanceof String) {
                String name = (String)o1;
                if (name.length() > 0 && name.charAt(0) == '=') { // NOI18N
                    // #27190: unmodified loader, just here for the ordering.
                    String cname = name.substring(1);
                    DataLoader dl = names2Loaders.get(cname);

                    if (dl != null) {
                        if (err.isLoggable(Level.FINE))
                            err.fine("reading unmodified " + cname);
                        l.add(dl);
                        classes.add(dl.getClass());
                    }
                    else {
                        // No such known loaded - presumably disabled module.
                        if (err.isLoggable(Level.FINE))
                            err.fine("skipping unmodified nonexistent " + cname);
                    }
                    continue;
                }
                // Module information.
                int rel = ois.readInt();
                String spec = (String) ois.readObject();

                obj = (NbMarshalledObject) ois.readObject();
                ModuleInfo m = modules.get(name);

                if (m == null) {
                    if (err.isLoggable(Level.FINE))
                        err.fine("No known module " + name + ", skipping loader");
                    continue;
                }
                if (!m.isEnabled()) {
                    if (err.isLoggable(Level.FINE))
                        err.fine("Module " + name +
                                 " is disabled, skipping loader");
                    continue;
                }
                if (m.getCodeNameRelease() < rel) {
                    if (err.isLoggable(Level.FINE))
                        err.fine("Module " + name + " is too old (major vers.), skipping loader");
                    continue;
                }
                if (spec != null) {
                    SpecificationVersion v = m.getSpecificationVersion();

                    if (v == null ||
                        v.compareTo(new SpecificationVersion(spec)) < 0) {
                        if (err.isLoggable(Level.FINE))
                            err.fine("Module " + name + " is too old (spec. vers.), skipping loader");
                        continue;
                    }
                }
                if (err.isLoggable(Level.FINE))
                    err.fine("Module " + name +
                             " is OK, will try to restore loader");
            }
            else {
                // Loader with no known module, or backward compatibility.
                obj = (NbMarshalledObject) o1;
            }
            Exception t = null;

            try {
                DataLoader loader = (DataLoader) obj.get();

                if (loader == null) {
                    // loader that wishes to be skipped (right now WSLoader from
                    // issue 38658)
                    continue;
                }
                Class<?> clazz = loader.getClass();

                if (err.isLoggable(Level.FINE))
                    err.fine("reading modified " + clazz.getName());
                l.add(loader);
                classes.add(clazz);
            }
            catch (IOException ex) {
                t = ex;
            }
            catch (ClassNotFoundException ex) {
                t = ex;
            }
        }

        // Read system loaders. But not into any particular order.
        for (;;) {
            NbMarshalledObject obj = (NbMarshalledObject) ois.readObject ();
            if (obj == null) {
                if (err.isLoggable(Level.FINE)) err.fine("reading null");
                break;
            }
            Exception t = null;
            try {
                // Just reads its shared state, nothing more.
                DataLoader loader = (DataLoader) obj.get ();
                if (err.isLoggable(Level.FINE)) err.fine("reading " + loader.getClass().getName());
            } catch (IOException ex) {
                t = ex;
            } catch (ClassNotFoundException ex) {
                t = ex;
            }
        }

        if (err.isLoggable(Level.FINE)) err.fine("done reading");

        // Explanation: modules are permitted to restoreDefault () before
        // the loader pool is de-externalized. This means that all loader manifest
        // sections will add a default-instance entry to the pool at startup
        // time. Later, when the pool is restored, this may reorder existing ones,
        // as well as change properties. But if any loader is missing (typically
        // due to failed deserialization), it will nonetheless be added to the end
        // now (and the pool resorted just in case).

        Iterator it = loaders.iterator ();
        while (it.hasNext ()) {
            DataLoader loader = (DataLoader)it.next ();
            if (!classes.contains (loader.getClass ())) {
                l.add (loader);
            }
        }
        if (l.size() > new HashSet<DataLoader>(l).size()) throw new IllegalStateException("Duplicates in " + l); // NOI18N

        loaders = l;
        // Always "resort": if the existing order was in fact compatible with the
        // current install-befores/afters, then this is no op (besides firing an
        // update event). Cf. #29671.
        resort (pool);
        
    }
    
    // I/O with loaders.ser; moved from NbProjectOperation:
    public static void store() throws IOException {
        if (modifiedLoaders.isEmpty()) {
            return;
        }

        FileObject ser = getLoaderPoolStorage(true);
        OutputStream os = ser.getOutputStream();
        try {
            ObjectOutputStream oos = new NbObjectOutputStream(os);
            NbObjectOutputStream.writeSafely(oos, getNbLoaderPool());
            oos.flush();
            oos.close();
        } finally {
            os.close();
        }
    }
    public static void load() throws IOException {
        FileObject ser = getLoaderPoolStorage(false);
        if (ser != null) {
            try {
                ObjectInputStream ois = new NbObjectInputStream(ser.getInputStream());
                try {
                    NbObjectInputStream.readSafely(ois);
                } finally {
                    ois.close();
                }
            } catch (IOException x) {
                ser.delete(); // #144158: probably not valuable, just kill it
                throw x;
            }
        }
    }
    private static final String LOADER_POOL_NAME = "loaders.ser"; // NOI18N
    private static FileObject getLoaderPoolStorage(boolean create) throws IOException {
        FileObject fo = FileUtil.getConfigFile(LOADER_POOL_NAME);
        if (fo == null && create) {
            fo = FileUtil.getConfigRoot().createData(LOADER_POOL_NAME);
        }
        return fo;
    }


    /** Notification that the state of pool has changed
    */
    private static synchronized void update (NbLoaderPool lp) {;
        if (err.isLoggable(Level.FINE)) err.fine("update");
        // clear the cache of loaders
        loadersArray = null;

        if (lp != null && installationFinished) {
            lp.superFireChangeEvent();
        }
        
        if (lp != null) {
            Enumeration e = lp.allLoaders();
            while (e.hasMoreElements()) {
                DataLoader l = (DataLoader)e.nextElement();
                // so the pool is there only once
                l.removePropertyChangeListener(lp);
                l.addPropertyChangeListener(lp);
            }
        }
    }


    /** Removes the loader. It is only removed from the list but
    * if an DataObject instance created exists it will be still
    * valid.
    * <P>
    * So the only difference is that when a DataObject is searched
    * for a FileObject this loader will not be taken into account.
    * <P>The loader pool may be resorted.
    * @param dl data loader to remove
    * @return true if the loader was registered and false if not
    */
    public static synchronized boolean remove (DataLoader dl, NbLoaderPool pool) {
        if (loaders.remove (dl)) {
            if (err.isLoggable(Level.FINE)) err.fine("remove: " + dl);
            String cname = dl.getClass().getName();
            names2Loaders.remove(cname);
            repNames2Loaders.remove(dl.getRepresentationClassName());
            installBefores.remove(cname);
            installAfters.remove(cname);
            dl.removePropertyChangeListener (pool);
        
            if (updatingBatch) {
                updatingBatchUsed = true;
            } else {
                resort (pool);
            }
            modifiedLoaders.remove(dl);
            return true;
        }
        return false;
    }

    /** Returns the only instance of the loader pool in our system.
    * There's no other way to get an instance of this class,
    * loader pool is singleton too.
    * @return loader pool instance
    */
    public static NbLoaderPool getNbLoaderPool () {
        assert !Thread.holdsLock(NbLoaderPool.class);
        return (NbLoaderPool)DataLoaderPool.getDefault ();
    }


        private static final long serialVersionUID =-8488524097175567566L;
        static boolean IN_TEST = false;

        private transient RequestProcessor.Task fireTask;

        private transient Lookup.Result mimeResolvers;
        // holds reference to not loose FileChangeListener
        private transient FileObject declarativeResolvers;
        private transient boolean listenersRegistered;
        private static RequestProcessor rp = new RequestProcessor("Refresh Loader Pool"); // NOI18N
        
        public NbLoaderPool() {
            fireTask = rp.create(this, true);
        }
        
        private final DataFolder findServicesFolder() throws Exception {
            Method m = DataLoaderPool.class.getDeclaredMethod("getFolderLoader"); // NOI18N
            m.setAccessible(true);
            DataLoader dl = (DataLoader) m.invoke(null);
            FileObject services = FileUtil.getConfigFile("Services"); // NOI18N
            return (DataFolder)dl.findDataObject(services, new HashSet<FileObject>()); 
        }

        final Lookup findServicesLookup() {
            StartLog.logProgress("Got Services folder"); // NOI18N
            DataFolder servicesF;
            try {
                // mark as registered, they will be added in taskFinished
                listenersRegistered = true;
                servicesF = findServicesFolder();
                org.openide.loaders.FolderLookup f = new org.openide.loaders.FolderLookup(servicesF, "SL[");
                f.addTaskListener(this);
                return f.getLookup();
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
                return Lookup.EMPTY;
            } finally {
                StartLog.logProgress("created FolderLookup"); // NOI18N
            }
        }

        @Override
        public void taskFinished(Task task) {
            initListeners();
        }
        
        private void initListeners() {
            mimeResolvers = Lookup.getDefault().lookupResult(MIMEResolver.class);
            mimeResolvers.addLookupListener(this);
            listenToDeclarativeResolvers();
        }
        private final FileChangeListener listener = new FileChangeAdapter() {
            public @Override void fileDataCreated(FileEvent ev) {
                maybeFireChangeEvent();
            }
            public @Override void fileDeleted(FileEvent ev) {
                maybeFireChangeEvent();
            }
        };
        private void listenToDeclarativeResolvers() {
            declarativeResolvers = FileUtil.getConfigFile("Services/MIMEResolver"); // NOI18N
            if (declarativeResolvers != null) { // might be inside test which overrides SFS?
                declarativeResolvers.addFileChangeListener(listener);
            }
        }

        /** Enumerates all loaders. Loaders are taken from children
        * structure of NbLoaderPool. */
        @Override
        protected Enumeration<DataLoader> loaders () {
            if (!listenersRegistered) {
                initListeners();
            }
            //
            // prevents from extensive copying
            //

            DataLoader[] arr;
            synchronized (NbLoaderPool.class) {
                if (loadersArray == null) {
                    List<DataLoader> ldrs = new ArrayList<DataLoader>(loaders);
                    // Since unit tests frequently add a bare DataLoader to default lookup:
                    ldrs.addAll(Lookup.getDefault().lookupAll(DataLoader.class));
                    loadersArray = ldrs.toArray(new DataLoader[0]);
                }
                arr = loadersArray;
            }
            return org.openide.util.Enumerations.array (arr);
        }

        /** Listener to property changes.
        */
        public void propertyChange (PropertyChangeEvent ev) {
            DataLoader l = (DataLoader)ev.getSource();
            String prop = ev.getPropertyName ();
            if (DataLoader.PROP_ACTIONS.equals (prop) && ev.getNewValue () == null) {
                // skip this change as this means the loader is using new storage mechanism
                return;
            }
            modifiedLoaders.add(l);
            if (err.isLoggable(Level.FINE)) err.fine("Got change in " + l.getClass().getName() + "." + prop);
            if (DataLoader.PROP_ACTIONS.equals (prop) || DataLoader.PROP_DISPLAY_NAME.equals (prop))
                return; // these are not important to the pool, i.e. to file recognition
            if (installationFinished) {
                superFireChangeEvent ();
            }
        }

        /** Fires change event to all listeners
        * (Delegates all work to its superclass)
        * Accessor for inner classes only.
        * @param che change event
        */
        void superFireChangeEvent () {
            err.fine("Change in loader pool scheduled"); // NOI18N
            fireTask.schedule (1000);
        }

        /** Called from the request task */
        public void run () {
            err.fine("going to fire change in loaders"); // NOI18N
            super.fireChangeEvent(new ChangeEvent (this));
            err.fine("change event fired"); // NOI18N
        }


        /** Write the object.
        */
        private void writeObject (ObjectOutputStream oos) throws IOException {
            NbLoaderPool.writePool (oos, getNbLoaderPool());
        }

        /** Reads the object.
        */
        private void readObject (ObjectInputStream ois)
        throws IOException, ClassNotFoundException {
            NbLoaderPool.readPool (ois, getNbLoaderPool());
        }

        /** Replaces the pool with default instance.
        */
        private Object readResolve () {
            return getNbLoaderPool ();
        }

        public void resultChanged(LookupEvent ev) {
            maybeFireChangeEvent();
        }

        private void maybeFireChangeEvent() {
            if (IN_TEST || org.netbeans.core.startup.Main.isInitialized()) {
                superFireChangeEvent();
            }
        }

}
