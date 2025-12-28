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


import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.logging.*;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.util.*;
import org.openide.util.lookup.*;

/** Implements a lookup, that scans a content of a folder for its
 * data objects and asks them for instance cookie, the created objects
 * are then used to for the content of the lookup.
 * <p>Any instances which are in fact instances of <code>Lookup</code>
 * will be proxied to, permitting one file to generate many instances
 * in the lookup system easily.
 * @author  Jaroslav Tulach
 * @since 1.11
 * @deprecated use {@link Lookups#forPath} instead.
 */
@Deprecated
public class FolderLookup extends FolderInstance {
    
    /** Lock for initiliazation of lookup. */
    private static final Object LOCK = new Object ();
    
    /** Lookup to delegate to. */
    private ProxyLkp lookup; 
    
    /** The root name of the lookup. */
    private String rootName;

    /** Indicates whether this FolderLookup is at the root of (folder)tree which 
     * we are interested in and the one which collects all items from the tree. */
    private final boolean isRoot;

    
    /** Constructs the FolderLookup for given container. A default ID prefix is 
     * used for identification of located items.
     *
     * @param df container (or folder) to work on
     */
    public FolderLookup (DataObject.Container df) {
        this (df, "FL["); // NOI18N
    }
    
    /** Constructs the FolderLookup for given container.
     * @param df container (or folder) to work on
     * @param prefix the prefix to use 
     */
    public FolderLookup (DataObject.Container df, String prefix) {
        this(df, prefix, true);
    }

    /** Constructs the FolderLookup for given container.
     * @param df container (or folder) to work on
     * @param prefix the prefix to use 
     * @param isRoot indicates whether this instance is the at the root of tree
     * in which we perform the lookup -> only this instance has lookup 
     * which collects all items from the tree */
    private FolderLookup(DataObject.Container df, String prefix, boolean isRoot) {
        super(df);
        
        this.rootName = prefix;
        this.isRoot = isRoot;
    }
    

    /** The correct class that this folder recognizes.
     * @return Proxy.Lkp class. */
    public final Class<?> instanceClass () {
        return ProxyLkp.class;
    }
    
    /**
     * Getter for the lookup that should be used.
     * <p>Serializable since 3.27.
     * @return a lookup
     */
    public final Lookup getLookup () {
        boolean inited = false;
        synchronized(LOCK) {
            if(lookup == null) {
                lookup = new ProxyLkp(this);
                inited = true;
            }
        }

        if(inited) {
            checkRecreate();
        }
                
        return lookup;
    }
    
    /** Updates the content of the lookup.
     * @param cookies updated array of instance cookies for the folder
     * @return object to represent these cookies
     *
     * @exception IOException an I/O error occured
     * @exception ClassNotFoundException a class has not been found
     */
    protected final Object createInstance(InstanceCookie[] cookies) 
    throws IOException, ClassNotFoundException {
        FolderLookupData flData = new FolderLookupData();

        // If we are root, preserve place for abstract lookup which collects items.
        // see ProxyLkp.update method.
        if(isRoot) {
            flData.lookups.add(null);
        }
        
        for (int i = 0; i < cookies.length; i++) {
            try {
                // It's either result from underlying lookup or some another lookup or it is ICItem.
                Object obj = cookies[i].instanceCreate ();

                if(obj instanceof FolderLookupData) {
                    // It's from underlying 'sub'-lookup.
                    flData.items.addAll(((FolderLookupData)obj).items);
                    flData.lookups.addAll(((FolderLookupData)obj).lookups);
                } else if(obj instanceof Lookup) {
                    flData.lookups.add((Lookup)obj);
                } else {
                    // Has to be ICItem.
                    flData.items.add((ICItem)obj);
                }
            } catch(IOException ex) {
                exception(ex);
            } catch(ClassNotFoundException ex) {
                exception(ex);
            }
        }

        // If this is not the root lookup just return items+lookups
        // which will be collected by root lookup.
        if(!isRoot) {
            return flData;
        }
        
        // We are root FolderLookup. Now collect all items from underlying world.
        
        // Initializes lookup.
        getLookup();

        lookup.update(flData.items, flData.lookups);
        
        return lookup;
    }
    
    /** Overrides superclass method. It returns instance
     * for <code>DataObject</code>&amp;<code>InstanceCookie</code> 'pair'. 
     * If the instance is of <code>FolderLookup.Lkp</code> class it is created otherwise
     * new <code>Lkp.ICItem</code> created and returned.
     *
     * @param dobj the data object that is the source of the cookie
     * @param cookie the instance cookie to read the instance from
     * @exception IOException when there I/O error
     * @exception ClassNotFoundException if the class cannot be found */
    protected Object instanceForCookie(DataObject dobj, InstanceCookie cookie)
    throws IOException, ClassNotFoundException {
        boolean isLookup;
        
        if(cookie instanceof InstanceCookie.Of) {
            isLookup = ((InstanceCookie.Of)cookie).instanceOf(Lookup.class);
        } else {
            isLookup = Lookup.class.isAssignableFrom(cookie.instanceClass ());
        }

        if(isLookup) {
            // Is underlying FolderLookup create it.
            return cookie.instanceCreate();
        } else {
            return new ICItem(dobj, rootName, cookie);
        }
    }
    
    /** Folder is recognized as underlying <code>FolderLookup</code> which passes
     * its items to parent <code>FolderLookup</code>.
     * @param df the folder found
     * @return new FolderLookup
     */
    protected InstanceCookie acceptFolder (DataFolder df) {
        return new FolderLookup(df, objectName(rootName, df), false);
    }
    
    /** Container is recognized as underlying <code>FolderLookup</code> which passes
     * its items to parent <code>FolderLookup</code>.
     * @param df the container found
     * @return new FolderLookup
     */
    protected InstanceCookie acceptContainer (DataObject.Container df) {
        return new FolderLookup(
            df,
            rootName == null ? "<container>" : rootName + "<container>", // NOI18N
            false
        );
    }
    

    /** Starts the creation of the object in the Folder recognizer thread.
     * Doing all the lookup stuff in one thread should prevent deadlocks,
     * but because we call unknown data loaders, they obviously must be 
     * implemented in correct way.
     *
     * @param run runable to start
     * @return <code>null</code>, because the runnable is started immediatelly
     */
    protected final Task postCreationTask (Runnable run) {
        run.run ();
        return null;
    }
    
    /** Concatenates name of folder with name of object. Helper method.
     * @param folderName name of folder or null
     * @param obj object to concatenate
     * @return new name
     */
    private static String objectName (String name, DataObject obj) {
        if (name == null) {
            return obj.getName ();
        } else {
            return name + '/' + obj.getName ();
        }
    }
    
    /** Notifies the exception. Helper method. */
    private static void exception (Exception e) {
        Logger.getLogger(FolderLookup.class.getName()).log(Level.INFO, null, e);
    }
    private static final Set<String> notified = Collections.synchronizedSet(new HashSet<String>());
    private static void exception(Exception e, FileObject fo) {
        Logger.getLogger(FolderLookup.class.getName()).log(notified.add(fo.getPath()) ? Level.INFO : Level.FINE, "Bad file: " + fo, e);
    }

    static final class Dispatch implements Executor, Runnable {
        private static final RequestProcessor DISPATCH = new RequestProcessor("Lookup Dispatch Thread"); // NOI18N
        private static final Logger LOG = Logger.getLogger(Dispatch.class.getName());
        
        private final Queue<Runnable> queue = new ConcurrentLinkedQueue<Runnable>();
        private final RequestProcessor.Task task = DISPATCH.create(this, true);

        @Override
        public void execute(Runnable command) {
            LOG.log(Level.FINER, "Enqueued: {0}", command);
            queue.add(command);
            task.schedule(0);
        }
        
        public void waitFinished() {
            task.waitFinished();
        }
        
        @Override
        public void run() {
            LOG.finer("Processing dispatched commands");
            for (;;) {
                Runnable r = queue.poll();
                if (r == null) {
                    LOG.log(Level.FINER, "Processing done. Queue: {0}", queue.isEmpty());
                    return;
                }
                LOG.log(Level.FINER, "Processing {0}", r);
                r.run();
            }
        }
    }
    
    /** <code>ProxyLookup</code> delegate so we can change the lookups on fly. */
    static final class ProxyLkp extends ProxyLookup implements Serializable {
        static final Dispatch DISPATCH = new Dispatch();
        
        private static final long serialVersionUID = 1L;

        /** <code>FolderLookup</code> we are associated with. */
        private transient FolderLookup fl;
        
        /** Content to control the abstract lookup. */
        private transient AbstractLookup.Content content;
        
        private transient boolean readFromStream;

        /** Constructs lookup which holds all items+lookups from underlying world.
         * @param folder <code>FolderLookup</code> to associate to */
        public ProxyLkp(FolderLookup folder) {
            this(folder, new AbstractLookup.Content(DISPATCH));
        }

        /** Constructs lookup. */
        private ProxyLkp(FolderLookup folder, AbstractLookup.Content content) {
            super(new Lookup[] {new AbstractLookup(content)});
            
            this.fl = folder;
            this.content = content;
        }
        
        @Override
        public String toString() {
            return "FolderLookup.lookup[\"" + fl.rootName + "\"]";
        }
        
        private void writeObject (ObjectOutputStream oos) throws IOException {
            Lookup[] ls = getLookups();
            for (int i = 0; i < ls.length; i++) {
                oos.writeObject(ls[i]);
            }
            oos.writeObject(null);
            oos.writeObject (fl.folder);
            oos.writeObject (fl.rootName);
            oos.writeObject (content);
        }
        
        private void readObject (ObjectInputStream ois) throws IOException, ClassNotFoundException {
            List<Lookup> ls = new ArrayList<Lookup>();
            Lookup l;
            while ((l = (Lookup)ois.readObject()) != null) {
                ls.add(l);
            }
            Lookup[] arr = ls.toArray(new Lookup[0]);
            DataFolder df = (DataFolder)ois.readObject ();
            String root = (String)ois.readObject ();
            
            fl = new FolderLookup (df, root, true);
            fl.lookup = this;
            
            content = (AbstractLookup.Content)ois.readObject ();
            
            setLookups (DISPATCH, arr);

            readFromStream = true;
            org.openide.util.RequestProcessor.getDefault ().post (fl, 0, Thread.MIN_PRIORITY);
        }
        
        
        /** Updates internal data. 
         * @param items Items to assign to all pairs
         * @param lookups delegates to delegate to (first item is null)
         */
        public void update(Collection<ICItem> items, List<Lookup> lookups) {
            readFromStream = false;
            
            // remember the instance lookup 
            Lookup pairs = getLookups ()[0];

            // changes the its content
            content.setPairs (items);
            if (fl.err().isLoggable(Level.FINE)) fl.err ().fine("Changed pairs: " + items); // NOI18N

            lookups.set(0, pairs);

            Lookup[] arr = lookups.toArray(new Lookup[0]);
            setLookups (DISPATCH, arr);
            if (fl.err().isLoggable(Level.FINE)) fl.err ().fine("Changed lookups: " + lookups); // NOI18N
        }
        
        /** Waits before the processing of changes is finished. */
        @Override
        protected void beforeLookup (Template template) {
            if (readFromStream) {
                // ok
                return;
            }
            if (template.getType().equals(DataLoader.class)) {
                return;
            }
            
            // do not wait in folder recognizer, but in all other cases
            if (
                !FolderList.isFolderRecognizerThread() &&
                ICItem.DANGEROUS.get() == null
            ) {
                if (!DataObjectPool.isConstructorAllowed()) {
                    fl.waitFinished();
//                    DISPATCH.waitFinished();
                } else {
                    try {
                        // try a bit but prevent deadlock from CanYouQueryFolderLookupFromHandleFindTest
                        while (!fl.waitFinished(10000)) {
                            long blocked = DataObjectPool.getPOOL().timeInWaitNotified();
                            if (blocked > 10000L) {
                                // folder recognizer thread is waiting for more then
                                // 10s in waitFinished, which signals that there 
                                // is a very high possibility of a deadlock
                                Exception td = new Exception();
                                fl.err().log(Level.INFO, "Preventing deadlock #65543: Do not call FolderLookup from inside DataObject operations!", td); // NOI18N
                                for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
                                    td.setStackTrace(entry.getValue());
                                    fl.err().log(Level.INFO, "Thread " + entry.getKey(), td);
                                }
                                return;
                            }
                        }
                    } catch (InterruptedException ex) {
                        fl.err().log(Level.INFO, null, ex);
                    }
                }
            } else {
                // there is a special need to wait for loaders commint from
                // layers
                if (
                    fl.folder != null &&
                    fl.folder.getName().equals("Factories") &&
                    template.getType().isAssignableFrom(DataObject.Factory.class)
                ) {
                    fl.waitFinished();
                }
            }
        }
        
        /** Mostly for testing purposes, to allow the tests to wait
         * for the scan to finished after deserialization.
         */
        public void waitFinished () {
            fl.waitFinished ();
        }

    } // End of ProxyLkp class.
    
    
    /** Item that delegates to <code>InstanceCookie</code>. Item which 
     * the internal lookup data structure is made from. */
    private static final class ICItem extends AbstractLookup.Pair {
        static final long serialVersionUID = 10L;
        
        static final ThreadLocal<ICItem> DANGEROUS = new ThreadLocal<ICItem> ();

        /** error manager for ICItem */
        private static final Logger ERR = Logger.getLogger(ICItem.class.getName());

        /** when deserialized only primary file is stored */
        private FileObject fo;
        
        private transient InstanceCookie ic;
        /** source data object */
        private transient DataObject obj;
        /** reference to created object */
        private transient Reference<Object> ref;
        /** root folder */
        private String rootName;

        /** Constructs new item. */
        public ICItem (DataObject obj, String rootName, InstanceCookie ic) {
            this.ic = ic;
            this.obj = obj;
            this.rootName = rootName;
            this.fo = obj.getPrimaryFile();
            
            if (ERR.isLoggable(Level.FINE)) ERR.fine("New ICItem: " + obj); // NOI18N
        }
        
        /** Initializes the item
         */
        public void init () {
            if (ic != null) return;

            ICItem prev = DANGEROUS.get ();
            try {
                DANGEROUS.set (this);
                if (obj == null) {
                    try {
                        obj = DataObject.find(fo);
                    } catch (DataObjectNotFoundException donfe) {
                        ic = new BrokenInstance("No DataObject for " + fo.getPath(), donfe); // NOI18N
                        return;
                    }
                }

                ic = obj.getCookie(InstanceCookie.class);
                if (ic == null) {
                    ic = new BrokenInstance("No cookie for " + fo.getPath(), null); // NOI18N
                }
            } finally {
                DANGEROUS.set (prev);
            }
        }
            
        /**
         * Fake instance cookie.
         * Used in case a file had an instance in a previous session but now does not
         * (or the data object could not even be created correctly).
         */
        private static final class BrokenInstance implements InstanceCookie.Of {
            private final String message;
            private final Exception ex;
            public BrokenInstance(String message, Exception ex) {
                this.message = message;
                this.ex = ex;
            }
            public String instanceName() {
                return "java.lang.Object"; // NOI18N
            }
            private ClassNotFoundException die() {
                if (ex != null) {
                    return new ClassNotFoundException(message, ex);
                } else {
                    return new ClassNotFoundException(message);
                }
            }
            public Class instanceClass() throws IOException, ClassNotFoundException {
                throw die();
            }
            public Object instanceCreate() throws IOException, ClassNotFoundException {
                throw die();
            }
            public boolean instanceOf(Class type) {
                return false;
            }
        }


        /** The class of the result item.
         * @return the class of the item
         */
        protected boolean instanceOf (Class clazz) {
            init ();
            
            if (ERR.isLoggable(Level.FINE)) ERR.fine("instanceOf: " + clazz.getName() + " obj: " + obj); // NOI18N
            
            if (ic instanceof InstanceCookie.Of) {
                // special handling for special cookies
                InstanceCookie.Of of = (InstanceCookie.Of)ic;
                boolean res = of.instanceOf (clazz);
                if (ERR.isLoggable(Level.FINE)) ERR.fine("  of: " + res); // NOI18N
                return res;
            }

            // handling of normal instance cookies
            try {
                boolean res = clazz.isAssignableFrom (ic.instanceClass ());
                if (ERR.isLoggable(Level.FINE)) ERR.fine("  plain: " + res); // NOI18N
                return res;
            } catch (ClassNotFoundException ex) {
                exception(ex, fo);
            } catch (IOException ex) {
                exception(ex, fo);
            }
            return false;
        }

        /** The class of the result item.
         * @return the instance of the object or null if it cannot be created
         */
        public Object getInstance() {
            init ();
            
            try {
                Object obj = ic.instanceCreate();
                if (ERR.isLoggable(Level.FINE)) ERR.fine("  getInstance: " + obj + " for " + this.obj); // NOI18N
                ref = new WeakReference<Object> (obj);
                return obj;
            } catch (ClassNotFoundException ex) {
                exception(ex, fo);
            } catch (IOException ex) {
                exception(ex, fo);
            }
            return null;
        }

        /** Hash code is the <code>InstanceCookie</code>'s code. */
        public int hashCode () {
            init ();
            
            return System.identityHashCode (ic);
        }

        /** Two items are equal if they point to the same cookie. */
        public boolean equals (Object obj) {
            if (obj instanceof ICItem) {
                ICItem i = (ICItem)obj;
                i.init ();
                init ();
                return ic == i.ic;
            }
            return false;
        }

        /** An identity of the item.
         * @return string representing the item, that can be used for
         *   persistance purposes to locate the same item next time */
        public String getId() {
            init ();

            if (obj == null) {
                // Deser problems.
                return "<broken: " + fo.getPath() + ">"; // NOI18N
            }
            
            return objectName(rootName, obj);
        }

        /** Display name is extracted from name of the objects node. */
        public String getDisplayName () {
            init ();
            
            if (obj == null) {
                // Deser problems.
                return "<broken: " + fo.getPath() + ">"; // NOI18N
            }
            
            return obj.getNodeDelegate ().getDisplayName ();
        }

        /** Method that can test whether an instance of a class has been created
         * by this item.
         *
         * @param obj the instance
         * @return if the item has already create an instance and it is the same
         *  as obj.
         */
        protected boolean creatorOf(Object obj) {
            Reference w = ref;
            if (w != null && w.get () == obj) {
                return true;
            }
            if (this.obj instanceof InstanceDataObject) {
                return ((InstanceDataObject)this.obj).creatorOf (obj);
            }
            return false;
        }

        /** The class of this item.
         * @return the correct class
         */
        public Class getType() {
            init ();
            
            try {
                return ic.instanceClass ();
            } catch (IOException ex) {
                // ok, no class available
            } catch (ClassNotFoundException ex) {
                // ok, no class available
            }
            return Object.class;
        }

    } // End of ICItem class.

    
    /** Data structure which holds <code>ICItem</code>'s and <code>Lookup</code>'s got
     * from current folder and underlying sub-folders making it possible to
     * pass to parent folder together. */
    private static class FolderLookupData {

        /** Collection of <code>ICItem</code>'s found in current 
         * folder and its sub-folders. */
        private Collection<ICItem> items;
        
        /** List of <code>Lookup</code>'s found in current folder
         * and its sub-folders. */
        private List<Lookup> lookups;
        
        
        /** Constructs data structure with inited fields. */
        public FolderLookupData() {
            items = new ArrayList<ICItem>(30);
            lookups = new ArrayList<Lookup>(5);
        }
        
    } // End of FolderLookupData class.
}
