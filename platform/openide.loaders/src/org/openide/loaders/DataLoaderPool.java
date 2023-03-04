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

import java.awt.Image;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.*;
import org.openide.filesystems.*;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/** Pool of data loaders.
 * Provides access to set of registered
 * {@link DataLoader loaders} in the system. They are used to find valid data objects
 * for given files.
 * <P>
 * The default instance can be retrieved using lookup.
 *
 * @author Jaroslav Tulach, Petr Hamernik, Dafe Simonek
 */
public abstract class DataLoaderPool extends Object
implements java.io.Serializable {

    /** SUID */
    static final long serialVersionUID=-360141823874889956L;
    /** standard system loaders. Accessed by getSystemLoaders method only */
    private static MultiFileLoader[] systemLoaders;
    /** standard default loaders. Accessed by getDefaultLoaders method only */
    private static MultiFileLoader[] defaultLoaders;
    
    private static DataLoaderPool DEFAULT;
    
    /** Getter for the default pool of loaders used in the system.
     * By default it looks it up using Lookup, if not found it provides 
     * default one that does lookup individual loaders using Lookup.
     *
     * @return instance of system DataLoaderPool
     * @since 5.1
     */
    public static synchronized DataLoaderPool getDefault() {
        if (DEFAULT == null) {
            DEFAULT = Lookup.getDefault().lookup(DataLoaderPool.class);
            if (DEFAULT == null) {
                DEFAULT = new DefaultPool();
            }
        }
        return DEFAULT;
    }
    
    /** Cache of loaders for faster toArray() method. */
    private transient DataLoader[] loaderArray;
    /** cache of loaders for allLoaders method */
    private transient List<DataLoader> allLoaders;
    private transient List<DataLoader> prefLoaders;
    /** counts number of changes in the loaders pool */
    private transient int cntchanges;
    
    private transient EventListenerList listeners;
    
    /** prefered loader */
    private transient DataLoader preferredLoader;
    
    /** Create new loader pool.
     */
    protected DataLoaderPool () {
    }
    
    /** Create new loader pool and set preferred loader.
     * The preferred loader will be asked before any other to recognize files (also before the system
     * loader).
     *
     * @param loader the preferred loader
     */
    protected DataLoaderPool (DataLoader loader) {
        preferredLoader = loader;
    }
    
    /** Get an enumeration of data loaders.
     * Must be overridden in subclasses to provide a list of additional loaders.
     * The list should <em>not</em> include the preferred loader.
     *
     * @return enumeration of loaders
     */
    protected abstract Enumeration<? extends DataLoader> loaders ();
    
    /** Add a new listener to the listener list. A listener is notified of
     * any change which was made to the loader pool (add, remove, or reorder).
     *
     * @param chl new listener
     */
    public final synchronized void addChangeListener (ChangeListener chl) {
        if (listeners == null) listeners = new EventListenerList();
        listeners.add( ChangeListener.class, chl);
    }
    
    /** Remove a listener from the listener list.
     *
     * @param chl listener to remove
     */
    public final synchronized void removeChangeListener (ChangeListener chl) {
        if (listeners != null) {
            listeners.remove( ChangeListener.class, chl);
        }
    }
    
    /** Fire change event to all listeners. Asynchronously.
     * @param che change event
     */
    protected final void fireChangeEvent (final ChangeEvent che) {
        
    	Object[] list;
        synchronized( this ) {
            cntchanges++;
            loaderArray = null;
            allLoaders = null;
            prefLoaders = null;

            if (listeners == null) return;            
            list = listeners.getListenerList();
        }
        
        // could fire on given array, modifications will copy it out before
        for (int i = list.length-2; i>=0; i-=2) {
	    if (list[i] == ChangeListener.class) {
                ChangeListener l = (ChangeListener)list[i+1];
                l.stateChanged(che);
            }
        }
    }
    
    
    /** Factory to create weak OperationListener
     *
     * @param l listener
     * @param s the source the new listener will be attached to
     */
    public static OperationListener createWeakOperationListener (OperationListener l, Object s) {
        return WeakListeners.create(OperationListener.class, l, s);
    }
    
    /** Add a listener for operations on data objects.
     * @param l the listener
     */
    public final synchronized void addOperationListener (OperationListener l) {
        if (listeners == null) listeners = new EventListenerList();
        listeners.add( OperationListener.class, l);
    }
    
    /** Remove a listener for operations on data objects.
     * @param l the listener
     */
    public final synchronized void removeOperationListener (OperationListener l) {
        if (listeners != null) {
            listeners.remove( OperationListener.class, l);
        }
    }
    
    /** Fires operation event to all listeners.
     * Clears loaderArray before firing a change.
     * @param ev event to fire
     * @param type the type of the event
     */
    final void fireOperationEvent (OperationEvent ev, int type) {
      	Object[] list;
        synchronized( this ) {
            if (listeners == null) return;            
            list = listeners.getListenerList();
        }

        // could fire on given array, modifications will copy it out before
        for (int i = list.length-2; i>=0; i-=2) {
	    if (list[i] == OperationListener.class) {
                OperationListener l = (OperationListener)list[i+1];
                switch (type) {
                    case OperationEvent.COPY:
                        l.operationCopy ((OperationEvent.Copy)ev);
                        break;
                    case OperationEvent.MOVE:
                        l.operationMove ((OperationEvent.Move)ev);
                        break;
                    case OperationEvent.DELETE:
                        l.operationDelete (ev);
                        break;
                    case OperationEvent.RENAME:
                        l.operationRename ((OperationEvent.Rename)ev);
                        break;
                    case OperationEvent.SHADOW:
                        l.operationCreateShadow ((OperationEvent.Copy)ev);
                        break;
                    case OperationEvent.TEMPL:
                        l.operationCreateFromTemplate ((OperationEvent.Copy)ev);
                        break;
                    case OperationEvent.CREATE:
                        l.operationPostCreate (ev);
                        break;
                }
            }
        }
    }
    
    /** Get an enumeration of all loaders, including the preferred and system loaders.
     * This should be the list of loaders as actually used by the system.
     * Typically it will consist of, in this order:
     * <ol>
     * <li>The preferred loader, if any.
     * <li>The system loaders, such as may be used for folders, shadows, etc.
     * <li>Module-specified loaders.
     * <li>The loader for instance data objects.
     * <li>Default loaders, which may handle files not otherwise recognizable.
     * </ol>
     * Applications should not rely on the exact contents of the pool,
     * rather the fact that this contains all the loaders which are
     * capable of recognizing files in the order in which they are
     * called.
     * @return enumeration of loaders
     */
    public final Enumeration<DataLoader> allLoaders () {
        return computeLoaders(true);
    }
    
    final Enumeration<DataLoader> computeLoaders(boolean computeAll) {
        List<DataLoader> all;
        List<DataLoader> pref;
        int oldcnt;
        synchronized (this) {
            all = this.allLoaders;
            pref = this.prefLoaders;
            oldcnt = this.cntchanges;
        }
        
        if (all == null) {
            all = new ArrayList<DataLoader>();
            if (preferredLoader != null) {
                all.add(preferredLoader);
            }
            all.addAll(Arrays.asList(getSystemLoaders()));
            Enumeration<? extends DataLoader> en = loaders();
            while(en.hasMoreElements()) {
                all.add(en.nextElement());
            }
            pref = new ArrayList<DataLoader>(all);
            all.addAll(Arrays.asList(getDefaultLoaders()));
            
            synchronized (this) {
                if (oldcnt == this.cntchanges) {
                    this.allLoaders = all;
                    this.prefLoaders = pref;
                }
            }
        }
        
        return Collections.enumeration(computeAll ? all : pref);
    }
    
    final Enumeration<DataObject.Factory> allLoaders(FileObject fo) {
        class MimeEnum implements Enumeration<DataObject.Factory> {
            final String mime;

            public MimeEnum(String mime) {
                this.mime = mime;
            }
            
            Enumeration<? extends DataObject.Factory> delegate;
            
            private Enumeration<? extends DataObject.Factory> delegate() {
                if (delegate == null) {
                    String path = "Loaders/" + mime + "/Factories"; // NOI18N
                    delegate = Collections.enumeration(Lookups.forPath(path).lookupAll(
                        DataObject.Factory.class
                    ));
                }
                return delegate;
            }
            
            public boolean hasMoreElements() {
                return delegate().hasMoreElements();
            }

            public DataObject.Factory nextElement() {
                return delegate().nextElement();
            }
        }
        String mime = fo.getMIMEType();
        Enumeration<DataObject.Factory> mimeLoaders = new MimeEnum(mime);
        mimeLoaders = Enumerations.concat(mimeLoaders, new MimeEnum("content/unknown")); // NOI18N
        
        Enumeration<DataLoader> first = computeLoaders(false);
        try {
            if (fo.getFileSystem().isDefault()) {
                first = Enumerations.concat(
                    first, 
                    Enumerations.singleton(getFolderLoader())
                );
            }
        } catch (FileStateInvalidException ex) {
            // OK
        }

        return Enumerations.concat(
            first,
            Enumerations.concat(
                mimeLoaders, 
                Enumerations.array(getDefaultLoaders())
            )
        );
    }
    
    /** Get an array of loaders that are currently registered.
     * Does not include special system loaders, etc.
     * @return array of loaders
     * @see #loaders
     */
    public DataLoader[] toArray () {
        DataLoader[] localArray = loaderArray;
        if (localArray != null)
            return localArray;
        ArrayList<DataLoader> loaders = new ArrayList<DataLoader> ();
        Enumeration<? extends DataLoader> en = loaders ();
        while (en.hasMoreElements ()) {
            loaders.add(en.nextElement ());
        }
        localArray = new DataLoader[loaders.size()];
        localArray = loaders.toArray(localArray);
        loaderArray = localArray;
        return localArray;
    }
    
    /** Finds the first producer of a representation class.
     * Scans through the list of all loaders and returns the first one
     * whose representation class is a superclass of <code>clazz</code>.
     *
     * @param clazz class to find producer for
     * @return data loader or <CODE>null</CODE> if there is no loader that
     *   can produce the class
     */
    public final DataLoader firstProducerOf(Class<? extends DataObject> clazz) {
        Enumeration<DataLoader> en = allLoaders ();
        while (en.hasMoreElements ()) {
            DataLoader dl = en.nextElement();
            if (dl.getRepresentationClass ().isAssignableFrom (clazz)) {
                // representation class is super class of clazz
                return dl;
            }
        }
        return null;
    }
    
    /** Get an enumeration of all producers of a representation class.
     * @see #firstProducerOf
     *
     * @param clazz class to find producers for
     * @return enumeration of loaders
     */
    public final Enumeration<DataLoader> producersOf(final Class<? extends DataObject> clazz) {
        class ProducerOf implements Enumerations.Processor<DataLoader,DataLoader> {
            public DataLoader process(DataLoader dl, java.util.Collection ignore) {
                return clazz.isAssignableFrom( dl.getRepresentationClass() ) ? dl : null;
            }
        }
        
        // Accepts only those loaders that produces superclass of clazz
        return Enumerations.filter (allLoaders (), new ProducerOf ());
    }

    private static class EmptyDataLoaderRecognized implements DataLoader.RecognizedFiles, Set<FileObject> {
        public void markRecognized(FileObject fo) {
        }

        public int size() {
            return 0;
        }

        public boolean isEmpty() {
            return true;
        }

        public boolean contains(Object o) {
            return false;
        }

        public Iterator<FileObject> iterator() {
            return Collections.<FileObject>emptySet().iterator();
        }

        public Object[] toArray() {
            return new Object[0];
        }

        public <T> T[] toArray(T[] a) {
            return a;
        }

        public boolean add(FileObject e) {
            return false;
        }

        public boolean remove(Object o) {
            return false;
        }

        public boolean containsAll(Collection<?> c) {
            return false;
        }

        public boolean addAll(Collection<? extends FileObject> c) {
            return false;
        }

        public boolean retainAll(Collection<?> c) {
            return false;
        }

        public boolean removeAll(Collection<?> c) {
            return false;
        }

        public void clear() {
        }
    } // end of EmptyDataLoaderRecognized


    
    /** private class for next method. Empty implementation of
     * DataLoaderRecognized.
     */
    static final EmptyDataLoaderRecognized emptyDataLoaderRecognized = new EmptyDataLoaderRecognized();
    
    /** Find a data object for this file object (not for normal users of the APIs).
     * <strong>DO NOT USE THIS</strong> as a normal user of the APIs!
     * Unless you really know what you are doing, use {@link DataObject#find} instead.
     * This call will throw an exception if it already exists, and it is normally
     * only for use by the loader infrastructure.
     * <p>All loaders are asked to recognize it according to their priority.
     * @param fo file object to recognize
     * @return the data object for this object or <CODE>null</CODE> if
     *   no loader recognizes this file
     * @exception DataObjectExistsException if the object for this primary file
     *   already exists
     * @exception IOException if the data object is recognized but
     *   an error occurs during instantiation
     * @see #findDataObject(FileObject, DataLoader.RecognizedFiles)
     */
    public DataObject findDataObject (FileObject fo) throws IOException {
        return findDataObject (fo, emptyDataLoaderRecognized);
    }
    
    /** Find a data object for this file object, considering already-recognized files (not for normal users of the APIs).
     * <strong>DO NOT USE THIS</strong> as a normal user of the APIs!
     * Unless you really know what you are doing, use {@link DataObject#find} instead.
     * This call will throw an exception if it already exists, and it is normally
     * only for use by the loader infrastructure.
     * <p>First of all looks at the
     * file extended attribute <code>NetBeansDataLoader</code>; if it is set and it
     * contains the class name of a valid {@link DataLoader}, that loader is given preference.
     * For all loaders used, the first to return non-<code>null</code> from {@link DataLoader#findDataObject}
     * is used.
     *
     * @param fo file object to recognize
     * @param r recognized files buffer
     * @return the data object for this object
     * @exception DataObjectExistsException if the object for this primary file
     *   already exists
     * @exception IOException if the data object is recognized but
     *   an error occurs during instantiation
     */
    public DataObject findDataObject (
    FileObject fo, DataLoader.RecognizedFiles r
    ) throws IOException {
        // try to find assigned loader
        DataLoader pref = getPreferredLoader (fo);
        if (pref != null) {
            DataObject obj = pref.findDataObject (fo, r);
            if (obj != null) {
                // file has been recognized
                return obj;
            }
        }

        Set<FileObject> recognized = r == emptyDataLoaderRecognized ? emptyDataLoaderRecognized : new HashSet<FileObject>();
        // scan through loaders
        Enumeration<? extends DataObject.Factory> en = allLoaders (fo);
        while (en.hasMoreElements ()) {
            DataObject.Factory l = en.nextElement ();
            DataObject obj;
            if (l instanceof DataLoader) {
                obj = l.findDataObject (fo, recognized);
            } else {
                obj = DataObjectPool.handleFindDataObject(l, fo, recognized);
            }
            if (!recognized.isEmpty()) {
                for (FileObject f : recognized) {
                    r.markRecognized(f);
                }
                recognized.clear();
            }
            if (obj != null) {
                return obj;
            }
        }
        return null;
    }
    
    /** Utility method to mark a file as belonging to a loader.
     * When the file is to be recognized this loader will be used first.
     *
     * @param fo file to mark
     * @param loader the loader to assign to the file or null if previous
     *    association should be cleared
     * @exception IOException if setting the file's attribute failed
     */
    public static void setPreferredLoader (FileObject fo, DataLoader loader)
    throws IOException {
        DataLoader prev = getPreferredLoader (fo);
        
        if (prev == loader) {
            return;
        }
        
        if (loader == null) {
            fo.setAttribute(DataObject.EA_ASSIGNED_LOADER, null);
        } else {
            Class c = loader.getClass();
            fo.setAttribute (DataObject.EA_ASSIGNED_LOADER, c.getName ());
            ModuleInfo module = Modules.getDefault().ownerOf(c);
            if (module != null) {
                fo.setAttribute(DataObject.EA_ASSIGNED_LOADER_MODULE, module.getCodeNameBase());
            }
        }
        Set<FileObject> one = new HashSet<FileObject>();
        one.add(fo);
        if (!DataObjectPool.getPOOL().revalidate(one).isEmpty()) {
            DataObject.LOG.log(Level.FINE, "It was not possible to invalidate data object: {0}", fo); // NOI18N
        }
    }
    
    /** Get the preferred loader for a file.
     * @param fo the file to get loader from
     * @return the loader or null if there is no particular preferred loader
     */
    public static DataLoader getPreferredLoader (FileObject fo) {
        String assignedLoaderName = (String)fo.getAttribute (DataObject.EA_ASSIGNED_LOADER);
        if (assignedLoaderName != null) {
            // First check to see if it comes from an uninstalled module.
            String modulename = (String)fo.getAttribute(DataObject.EA_ASSIGNED_LOADER_MODULE);
            if (modulename != null) {
                // [PENDING] in the future a more efficient API may be introduced
                // (actually currently you can look up with a template giving the name
                // as part of the lookup item ID but this is not an official API)
                Iterator<? extends ModuleInfo> modules = Lookup.getDefault().lookupAll(ModuleInfo.class).iterator();
                boolean ok = false;
                while (modules.hasNext()) {
                    ModuleInfo module = modules.next();
                    if (module.getCodeNameBase().equals(modulename)) {
                        if (module.isEnabled()) {
                            // Carry on.
                            ok = true;
                            break;
                        } else {
                            // Uninstalled module.
                            return null;
                        }
                    }
                }
                if (! ok) {
                    // Unknown module.
                    return null;
                }
            } // else don't worry about it (compatibility)
            try {
                ClassLoader load = Lookup.getDefault().lookup(ClassLoader.class);
                if (load == null) {
                    load = DataLoaderPool.class.getClassLoader ();
                }
                
                return DataLoader.getLoader(Class.forName(assignedLoaderName, true, load).
                        asSubclass(DataLoader.class));
            } catch (Exception ex) {
                Logger.getLogger(DataLoaderPool.class.getName()).log(Level.WARNING, null, ex);
            }
        }
        return null;
    }

    /** Factory method to create default implementation of a factory for
     * data objects. It takes the class of the {@link DataObject} and
     * is ready to call its constructor. The constructor needs to take two
     * arguments: {@link FileObject} and {@link MultiFileLoader}. It can throw {@link IOException} as
     * is usual among {@link DataObject} constructors.
     * <p>
     * You can also invoke this method from a layer by following definition:
     * <pre>
     * &lt;file name="nameofyourfile.instance"&gt;
     *   &lt;attr name="instanceCreate" methodvalue="org.openide.loaders.DataLoaderPool.factory"/&gt;
     *   &lt;attr name="dataObjectClass" stringvalue="org.your.pkg.YourDataObject"/&gt;
     *   &lt;attr name="mimeType" stringvalue="yourmime/type"/&gt;
     *   &lt;attr name="iconBase" stringvalue="org/your/pkg/YourDataObject.png"/&gt;
     * &lt;/file&gt;
     * </pre>
     * @param clazz the class of the data object to create. Must have appropriate
     *    constructor.
     * @param mimeType the mime type associated with the object, used for 
     *    example to create the right actions for the object's node
     * @param image icon to use by default for nodes representing data objects
     *    created with this factory
     * @return factory to be registered in <code>Loaders/mime/type/Factories</code>
     *    in some module layer file
     * @since 7.1
     */
    public static <T extends DataObject> DataObject.Factory factory(
        Class<T> dataObjectClass, String mimeType, Image image
    ) {
        return new MimeFactory<T>(dataObjectClass, mimeType, image, null);
    }
    static <T extends DataObject> DataObject.Factory factory(
        FileObject fo
    ) throws ClassNotFoundException {
        return MimeFactory.layer(fo);
    }
    
    /** Lazy getter for system loaders.
     */
    private static synchronized MultiFileLoader[] getSystemLoaders () {
        if (systemLoaders == null) {
            systemLoaders = new MultiFileLoader [] {
                (MultiFileLoader) DataLoader.getLoader(ShadowLoader.class),
                (MultiFileLoader) DataLoader.getLoader(InstanceLoaderSystem.class)
            };
        }
        return systemLoaders;
    }
    
    /** Lazy getter for default loaders.
     */
    private static synchronized MultiFileLoader[] getDefaultLoaders () {
        if (defaultLoaders == null) {
            defaultLoaders = new MultiFileLoader [] {
                (MultiFileLoader) DataLoader.getLoader(FolderLoader.class),
                (MultiFileLoader) DataLoader.getLoader(XMLDataObject.Loader.class),
                (MultiFileLoader) DataLoader.getLoader(InstanceLoader.class),
                (MultiFileLoader) DataLoader.getLoader(DefaultLoader.class)
            };
        }
        return defaultLoaders;
    }
    
    /** Getter for default file loader
     * @return the default file loader
     */
    static MultiFileLoader getDefaultFileLoader () {
        return getDefaultLoaders ()[3];
    }
    
    /** Getter for folder loader
     * @return the folder loader
     */
    static MultiFileLoader getFolderLoader () {
        return getDefaultLoaders ()[0];
    }
    
    /** Getter for shadow loader.
     */
    static MultiFileLoader getShadowLoader () {
        return getSystemLoaders ()[0];
    }
    
    static MultiFileLoader getInstanceLoader() {
        return getDefaultLoaders()[2];
    }

    /**
     * Special pool for unit testing etc.
     * Finds all relevant data loaders in default lookup.
     */
    private static final class DefaultPool extends DataLoaderPool implements LookupListener {
        
        private final Lookup.Result<DataLoader> result;
        
        public DefaultPool() {
            result = Lookup.getDefault().lookupResult(DataLoader.class);
            result.addLookupListener(this);
        }
        
        protected Enumeration<? extends DataLoader> loaders() {
            return Collections.enumeration(result.allInstances());
        }
        
        public void resultChanged(LookupEvent e) {
            fireChangeEvent(new ChangeEvent(this));
        }
        
    }
    
    //
    // Default loaders
    //

    /* Instance loader recognizing .settings files. It's placed at the beginning
     * of loader pool, .settings files must alwaus be recognized by this loader
     * otherwise IDE settings will not work at all. No module is permitted to use
     * .settings files.
     */
    @NbBundle.Messages({
            "CTL_SettingsFile=Settings Files"
    })
    @MIMEResolver.ExtensionRegistration(
        displayName = "#CTL_SettingsFile",
        extension = { InstanceDataObject.INSTANCE, InstanceDataObject.XML_EXT },
        mimeType = "application/x-nbsettings",
        position = 44
    )
    private static class InstanceLoaderSystem extends InstanceLoader {
        private static final long serialVersionUID = -935749906623354837L;
        
        /* Creates new InstanceLoader */
        public InstanceLoaderSystem() {
            super ();
        }

        @Override
        protected FileObject findPrimaryFile (FileObject fo) {
            FileSystem fs = null;
            try {
                fs = fo.getFileSystem ();
            } catch (FileStateInvalidException e) {
                return null;
            }
            if (!fs.isDefault()) {
                return null;
            }
            return super.findPrimaryFile (fo);
        }

        /** @return list of all required extensions for this loader */
        @Override
        protected String [] getRequiredExt () {
            return new String[] {
                InstanceDataObject.INSTANCE,
                InstanceDataObject.XML_EXT
            };
        }
    }


/* Instance loader recognizing .ser and .instance files. It's placed at
 * the end of loader pool among default loaders.
 */
private static class InstanceLoader extends UniFileLoader {
    private static final long serialVersionUID =-3462727693843631328L;


    /* Creates new InstanceLoader */
    public InstanceLoader () {
        super ("org.openide.loaders.InstanceDataObject"); // NOI18N
    }

    @Override
    protected void initialize () {
        super.initialize();
        setExtensions(null);
    }

    @Override
    protected String actionsContext () {
        return "Loaders/application/x-nbsettings/Actions"; // NOI18N
    }
    
    /** Get the default display name of this loader.
    * @return default display name
    */
    @Override
    protected String defaultDisplayName () {
        return NbBundle.getMessage (DataLoaderPool.class, "LBL_instance_loader_display_name");
    }

    /* Creates the right data object for given primary file.
     * It is guaranteed that the provided file is realy primary file
     * returned from the method findPrimaryFile.
     *
     * @param primaryFile the primary file
     * @return the data object for this file
     * @exception DataObjectExistsException if the primary file already has data object
     */
    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, java.io.IOException {
        InstanceDataObject obj = new InstanceDataObject(primaryFile, this);
        return obj;
    }

    @Override
    public void writeExternal (ObjectOutput oo) throws IOException {
        // does not use super serialization of extensions
        oo.writeObject (this);

        super.writeExternal (oo);
    }

    @Override
    public void readExternal (ObjectInput oi) throws IOException, ClassNotFoundException {
        // the result of following code is either ExtensionList (original version)
        // or this (current version).
        Object o = oi.readObject ();
        if (o instanceof SystemAction[]) {
            //added for compatibility with FFJ2.0
            setActions ((SystemAction[]) o);            
            setExtensions(getExtensions());
        } else if (o instanceof ExtensionList) {
            // old serialization, add new extension
            ExtensionList list = (ExtensionList)o;
            setExtensions(list);
        } else {
            // newer serialization, everything should be ok, just read
            // the original value
            super.readExternal (oi);
            setExtensions(getExtensions());
        }
    }

    /** Set the extension list for this data loader.
    * Checks if all required extensions are in new list of extensions.
    * @param ext new list of extensions
    */
    @Override
    public void setExtensions(ExtensionList ext) {
        super.setExtensions(initExtensions(ext));
    }

    /** fill in instance file's extension list; if ext == null new list is created */
    private ExtensionList initExtensions(ExtensionList ext) {
        String rqext [] = getRequiredExt ();
        if (ext == null) ext = new ExtensionList();
        for (int i = 0; i < rqext.length; i++)
            ext.addExtension(rqext[i]);
        return ext;
    }
    
    /**
     * Just avoids loaders.ser, which is not a well-formed ser file and causes confusing
     * exceptions when browsing system file system.
     * Anyway reading the contents would mutate loader singletons! Evil.
     */
    @Override
    protected FileObject findPrimaryFile(FileObject fo) {
        FileObject r = super.findPrimaryFile(fo);
        if (r != null && r.getPath().equals("loaders.ser")) { // NOI18N
            try {
                if (r.getFileSystem().isDefault()) {
                    // Skip it.
                    return null;
                }
            } catch (FileStateInvalidException e) {
                Logger.getLogger(DataLoaderPool.class.getName()).log(Level.WARNING, null, e);
            }
        }
        return r;
    }

    /** @return list of all required extensions for this loader */
    protected String [] getRequiredExt () {
        return new String[] {
            InstanceDataObject.INSTANCE,
            InstanceDataObject.SER_EXT,
            InstanceDataObject.XML_EXT
        };
    }
} // end of InstanceLoader
    
/** Loader for file objects not recognized by any other loader */
private static final class DefaultLoader extends MultiFileLoader {
    static final long serialVersionUID =-6761887227412396555L;

    /* Representation class is DefaultDataObject */
    public DefaultLoader () {
        super ("org.openide.loaders.DefaultDataObject"); // NOI18N
        //super (DefaultDataObject.class);
    }

    @Override
    protected String actionsContext () {
        return "Loaders/content/unknown/Actions"; // NOI18N
    }
    
    /** Get the default display name of this loader.
    * @return default display name
    */
    @Override
    protected String defaultDisplayName () {
        return NbBundle.getMessage (DataLoaderPool.class, "LBL_default_loader_display_name");
    }

    /** Get the primary file.
     * @param fo the file to find the primary file for
     *
     * @return the primary file
     */
    protected FileObject findPrimaryFile (FileObject fo) {
        // never recognize folders
        if (fo.isFolder()) return null;
        return fo;
    }

    /* Creates the right data object for given primary file.
     * It is guaranteed that the provided file is realy primary file
     * returned from the method findPrimaryFile.
     *
     * @param primaryFile the primary file
     * @return the data object for this file
     * @exception DataObjectExistsException if the primary file already has data object
     */
    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, java.io.IOException {
        return new DefaultDataObject(primaryFile, this);
    }

    /* Creates the right primary entry for given primary file.
     *
     * @param obj requesting object
     * @param primaryFile primary file recognized by this loader
     * @return primary entry for that file
     */
    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {
        return new FileEntry (obj, primaryFile);
    }

    /** Do not create a seconday entry.
     *
     * @param obj ignored
     * @param secondaryFile ignored
     * @return never returns
     * @exception UnsupportedOperationException because this loader supports only a primary file object
     */
    protected MultiDataObject.Entry createSecondaryEntry (MultiDataObject obj, FileObject secondaryFile) {
        throw new UnsupportedOperationException ();
    }

    /** Does nothing because this loader works only with objects
     * with one file => primary file so it is not necessary to search
     * for anything else.
     *
     * @param obj the object to test
     */
    @Override
    void checkFiles (MultiDataObject obj) {
    }
} // end of DefaultLoader


/** Loader for shadows, since 1.13 changed to UniFileLoader. */
private static final class ShadowLoader extends UniFileLoader {
    static final long serialVersionUID =-11013405787959120L;

    /* DO NOT REMOVE THIS, the ShadowChangeAdapter must be constructed, it listens
     * on filesystems changes and converts DataShadows to BrokenDataShadows and vice versa.
     */
    private static ShadowChangeAdapter changeAdapter = new ShadowChangeAdapter();

    /* Representation class is DataShadow */
    public ShadowLoader () {
        super ("org.openide.loaders.DataShadow"); // NOI18N
        //super (DataShadow.class);
    }

    /** Get the default display name of this loader.
    * @return default display name
    */
    @Override
    protected String defaultDisplayName () {
        return NbBundle.getMessage (DataLoaderPool.class, "LBL_shadow_loader_display_name");
    }

    /** For a given file finds the primary file.
     * @param fo the (secondary) file
     *
     * @return the primary file for the file or <code>null</code> if the file is not
     *  recognized by this loader
     */
    @Override
    protected FileObject findPrimaryFile(FileObject fo) {
        if (fo.isData() && fo.hasExt (DataShadow.SHADOW_EXTENSION)) {
            return fo;
        }
        return null;
    }

    /** Creates the right primary entry for a given primary file.
     *
     * @param obj requesting object
     * @param primaryFile primary file recognized by this loader
     * @return primary entry for that file
     */
    @Override
    protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
        return new FileEntry(obj, primaryFile);
    }

    /** Creates the right data object for a given primary file.
     * It is guaranteed that the provided file will actually be the primary file
     * returned by {@link #findPrimaryFile}.
     *
     * @param primaryFile the primary file
     * @return the data object for this file
     * @exception DataObjectExistsException if the primary file already has a data object
     */
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        try {
            DataObject d = DataShadow.deserialize (primaryFile);
            if (d != null) return new DataShadow (primaryFile, d, this);
        } catch (IOException ex) {
            // broken link or damaged shadow file
        }
        /* Link is broken, create BrokenDataShadow */
        return new BrokenDataShadow (primaryFile, this);
    }
    @Override
    public void writeExternal(ObjectOutput oo) throws IOException {
    }
    @Override
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
    }
} // end of ShadowLoader

/** Loader for folders.
 */
static final class FolderLoader extends UniFileLoader {
    static final long serialVersionUID =-8325525104047820255L;

    /* Representation class is DataFolder */
    public FolderLoader () {
        super ("org.openide.loaders.DataFolder"); // NOI18N
        // super (DataFolder.class);
    }

    @Override
    protected String actionsContext () {
        return "Loaders/folder/any/Actions"; // NOI18N
    }
    
    /** Get the default display name of this loader.
    * @return default display name
    */
    @Override
    protected String defaultDisplayName () {
        return NbBundle.getMessage (DataLoaderPool.class, "LBL_folder_loader_display_name");
    }

    @Override
    protected FileObject findPrimaryFile(FileObject fo) {
        if (fo.isFolder()) {
            return fo;
        }
        return null;
    }

    @Override
    protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
        return new FileEntry.Folder(obj, primaryFile);
    }

    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new DataFolder(primaryFile, this);
    }

    /** This method is used only in DataFolder.handleMove method.
     * For more comments see {@link org.openide.loaders.DataFolder#handleMove}.
     *
     * @param primaryFile the primary file of the data folder to be created
     * @param original The original DataFolder. The returned MultiDataObject 
     *      delegates createNodeDelegate and getClonedNodeDelegate methods calls
     *      to the original DataFolder.
     * @return The DataFolder that shares the nodes with the original DataFolder.
     */
    MultiDataObject createMultiObject(FileObject primaryFile, final DataFolder original) throws DataObjectExistsException, IOException {
        class NodeSharingDataFolder extends DataFolder {
            public NodeSharingDataFolder(FileObject fo) throws DataObjectExistsException, IllegalArgumentException {
                super(fo, FolderLoader.this);
            }
            @Override
            protected Node createNodeDelegate() {
                return new FilterNode(original.getNodeDelegate());
            }
            @Override
            Node getClonedNodeDelegate (DataFilter filter) {
                return new FilterNode(original.getClonedNodeDelegate(filter));
            }
        }
        return new NodeSharingDataFolder(primaryFile);
    }

    @Override
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
        try {
            super.readExternal(oi);
        } catch (OptionalDataException ode) {
            // older ser of FolderLoader which did not store actions - ignore
        }
    }

} // end of FolderLoader

}
