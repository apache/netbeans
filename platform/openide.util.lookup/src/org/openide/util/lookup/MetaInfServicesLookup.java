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

package org.openide.util.lookup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.lookup.implspi.SharedClassObjectBridge;

/**
 * @author Jaroslav Tulach, Jesse Glick
 * @see Lookups#metaInfServices(ClassLoader,String)
 * @see "#14722"
 */
final class MetaInfServicesLookup extends AbstractLookup {
    static final Logger LOGGER = Logger.getLogger(MetaInfServicesLookup.class.getName());
    private static final MetaInfCache CACHE = new MetaInfCache(512);
    private static Reference<Executor> RP = new WeakReference<Executor>(null);
    static synchronized Executor getRP() {
        Executor res = RP.get();
        if (res == null) {
            try {
                Class<?> seek = Class.forName("org.openide.util.RequestProcessor");
                res = (Executor)seek.getDeclaredConstructor().newInstance();
            } catch (Throwable t) {
                try {
                    res = Executors.newSingleThreadExecutor();
                } catch (Throwable t2) {
                    res = new Executor() {
                        @Override
                        public void execute(Runnable command) {
                            command.run();
                        }
                    };
                }
            }
            RP = new SoftReference<Executor>(res);
        }
        return res;
    }

    /** A set of all requested classes.
     * Note that classes that we actually succeeded on can never be removed
     * from here because we hold a strong reference to the loader.
     * However we also hold classes which are definitely not loadable by
     * our loader.
     */
    private final Map<Class<?>,Object> classes = new WeakHashMap<Class<?>,Object>();

    /** class loader to use */
    private final ClassLoader loader;
    /** prefix to prepend */
    private final String prefix;

    /** Create a lookup reading from a specified classloader.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public MetaInfServicesLookup(ClassLoader loader, String prefix) {
        this.loader = loader;
        this.prefix = prefix;

        LOGGER.log(Level.FINE, "Created: {0}", this);
    }

    @Override
    public String toString() {
        return "MetaInfServicesLookup[" + loader + "]"; // NOI18N
    }

    /** Initialize soon, before result's listeners are activated
     */
    @Override
    void beforeLookupResult(Template<?> template) {
        beforeLookup(template);
    }

    /* Tries to load appropriate resources from manifest files.
     */
    @Override
    protected final void beforeLookup(Lookup.Template<?> t) {
        Class<?> c = t.getType();

        Collection<AbstractLookup.Pair<?>> toAdd = null;
        synchronized (this) {
            if (classes.get(c) == null) { // NOI18N
                toAdd = new ArrayList<Pair<?>>();
            } else {
                // ok, nothing needs to be done
                return;
            }
        }
        if (toAdd != null) {
            Set<Class<?>> all = new HashSet<Class<?>>();
            for (Class type : allSuper(c, all)) {
                search(type, toAdd);
            }
        }
        HashSet<R> listeners = null;
        synchronized (this) {
            if (classes.put(c, "") == null) { // NOI18N
                // Added new class, search for it.
                LinkedHashSet<AbstractLookup.Pair<?>> lhs = getPairsAsLHS();
                List<Item> arr = new ArrayList<Item>();
                for (Pair<?> lh : lhs) {
                    arr.add((Item)lh);
                }
                for (Pair<?> p : toAdd) {
                    insertItem((Item) p, arr);
                }
                listeners = setPairsAndCollectListeners(arr);
            }
        }
        if (listeners != null) {
            notifyIn(getRP(), listeners);
        }
    }
    
    private Set<Class<?>> allSuper(Class<?> clazz, Set<Class<?>> all) {
        all.add(clazz);
        Class<?> sup = clazz.getSuperclass();
        if (sup != null && sup != Object.class) {
            all.add(sup);
        }
        for (Class<?> c : clazz.getInterfaces()) {
            allSuper(c, all);
        }
        return all;
    }

    /** Finds all pairs and adds them to the collection.
     *
     * @param clazz class to find
     * @param result collection to add Pair to
     */
    private void search(Class<?> clazz, Collection<AbstractLookup.Pair<?>> result) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "Searching for {0} in {1} from {2}", new Object[] {clazz.getName(), clazz.getClassLoader(), this});
        }

        String res = prefix + clazz.getName(); // NOI18N
        Enumeration<URL> en;

        try {
            en = loader.getResources(res);
        } catch (IOException ioe) {
            // do not use ErrorManager because we are in the startup code
            // and ErrorManager might not be ready
            ioe.printStackTrace();

            return;
        }

        // Do not create multiple instances in case more than one JAR
        // has the same entry in it (and they load to the same class).
        // Probably would not happen, assuming JARs only list classes
        // they own, but just in case...
        List<Item> foundClasses = new ArrayList<Item>();
        Collection<Class<?>> removeClasses = new ArrayList<Class<?>>();

        boolean foundOne = false;

        while (en.hasMoreElements()) {
            if (!foundOne) {
                foundOne = true;

                // Double-check that in fact we can load the *interface* class.
                // For example, say class I is defined in two JARs, J1 and J2.
                // There is also an implementation M1 defined in J1, and another
                // implementation M2 defined in J2.
                // Classloaders C1 and C2 are made from J1 and J2.
                // A MetaInfServicesLookup is made from C1. Then the user asks to
                // lookup I as loaded from C2. J1 has the services line and lists
                // M1, and we can in fact make it. However it is not of the desired
                // type to be looked up. Don't do this check, which could be expensive,
                // unless we expect to be getting some results, however.
                Class<?> realMcCoy = null;

                try {
                    realMcCoy = loader.loadClass(clazz.getName());
                } catch (ClassNotFoundException cnfe) {
                    // our loader does not know about it, OK
                }

                if (realMcCoy != clazz) {
                    // Either the interface class is not available at all in our loader,
                    // or it is not the same version as we expected. Don't provide results.
                    if (realMcCoy != null) {
                        LOGGER.log(Level.WARNING, "{0} is not the real McCoy! Actually found it in {1} (from {2}) but searched for from {3}",
                                new Object[] {clazz.getName(), realMcCoy.getClassLoader(), loader, clazz.getClassLoader()}); // NOI18N
                    } else {
                        LOGGER.log(Level.WARNING, "{0} could not be found in {1}", new Object[] {clazz.getName(), loader}); // NOI18N
                    }

                    return;
                }
            }

            URL url = en.nextElement();
            Item currentItem = null;

            try {
                InputStream is = url.openStream();

                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

                    // XXX consider using ServiceLoaderLine instead
                    while (true) {
                        String line = reader.readLine();

                        if (line == null) {
                            break;
                        }

                        line = line.trim();

                        // is it position attribute?
                        if (line.startsWith("#position=")) {
                            if (currentItem == null) {
                                LOGGER.log(Level.INFO, "Found line ''{0}'' in {1} but there is no item to associate it with", new Object[] {line, url});
                                continue;
                            }

                            try {
                                currentItem.position = Integer.parseInt(line.substring(10));
                            } catch (NumberFormatException e) {
                                // do not use ErrorManager because we are in the startup code
                                // and ErrorManager might not be ready
                                e.printStackTrace();
                            }
                        }

                        if (currentItem != null) {
                            insertItem(currentItem, foundClasses);
                            currentItem = null;
                        }

                        // Ignore blank lines and comments.
                        if (line.length() == 0) {
                            continue;
                        }

                        boolean remove = false;

                        if (line.charAt(0) == '#') {
                            if ((line.length() == 1) || (line.charAt(1) != '-')) {
                                continue;
                            }

                            // line starting with #- is a sign to remove that class from lookup
                            remove = true;
                            line = line.substring(2);
                        }
                        Class<?> inst = null;
                        try {
                            Object ldr = url.getContent(new Class[] { ClassLoader.class });
                            if (ldr instanceof ClassLoader) {
                                inst = Class.forName(line, false, (ClassLoader)ldr);
                            }
                        } catch (LinkageError err) {
                            // go on
                        } catch (ClassNotFoundException ex) {
                            LOGGER.log(Level.FINER, "No class found in " + url, ex);
                        } catch (IOException ex) {
                            LOGGER.log(Level.FINER, "URL does not support classloader protocol " + url, ex);
                        }
                        
                        if (inst == null) try {
                            // Most lines are fully-qualified class names.
                            inst = Class.forName(line, false, loader);
                        } catch (LinkageError err) {
                            if (remove) {
                                continue;
                            }
                            throw new ClassNotFoundException(err.getMessage(), err);
                        } catch (ClassNotFoundException cnfe) {
                            if (remove) {
                                // if we are removing somthing and the something
                                // cannot be found it is ok to do nothing
                                continue;
                            } else {
                                // but if we are not removing just rethrow
                                throw cnfe;
                            }
                        }

                        if (!clazz.isAssignableFrom(inst)) {
                            throw new ClassNotFoundException(clazzToString(inst) + " not a subclass of " + clazzToString(clazz)); // NOI18N
                        }

                        if (remove) {
                            removeClasses.add(inst);
                        } else {
                            // create new item here, but do not put it into
                            // foundClasses array yet because following line
                            // might specify its position
                            currentItem = new Item(inst);
                        }
                    }

                    if (currentItem != null) {
                        insertItem(currentItem, foundClasses);
                        currentItem = null;
                    }
                } finally {
                    is.close();
                }
            } catch (ClassNotFoundException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }

        LOGGER.log(Level.FINER, "Found impls of {0}: {1} and removed: {2} from: {3}", new Object[] {clazz.getName(), foundClasses, removeClasses, this});

        /* XXX makes no sense, wrong types:
        foundClasses.removeAll(removeClasses);
         */

        for (Item item : foundClasses) {
            if (removeClasses.contains(item.clazz())) {
                continue;
            }

            result.add(item);
        }
    }
    private static String clazzToString(Class<?> clazz) {
        String loc = null;
        try {
            if (clazz.getProtectionDomain() != null && clazz.getProtectionDomain().getCodeSource() != null) {
                loc = clazz.getProtectionDomain().getCodeSource().getLocation().toString();
            }
        } catch (Throwable ex) {
            loc = ex.getMessage();
        }
        return clazz.getName() + "@" + clazz.getClassLoader() + ":" + loc; // NOI18N
    }

    /**
     * Insert item to the list according to item.position value.
     */
    private void insertItem(Item item, List<Item> list) {
        // no position? -> add it to the end
        if (item.position == -1) {
            if (!list.contains(item)) {
                list.add(item);
            }

            return;
        }

        int foundIndex = -1;
        int index = -1;
        for (Item i : list) {
            if (i.equals(item)) {
                return;
            }
            index++;

            if (foundIndex < 0) {
                if (i.position == -1 || i.position > item.position) {
                    foundIndex = index;
                }
            }
        }
        if (foundIndex < 0) {
            list.add(item);             // add to the end
        } else {
            list.add(foundIndex, item); // insert at found index
        }
    }

    static Item createPair(Class<?> clazz) {
        return new Item(clazz);
    }

    /** Pair that holds name of a class and maybe the instance.
     */
    private static final class Item extends AbstractLookup.Pair<Object> {
        /** May be one of three things:
         * 1. The implementation class which was named in the services file.
         * 2. An instance of it.
         * 3. Null, if creation of the instance resulted in an error.
         */
        private Object object;
        private int position = -1;

        public Item(Class<?> clazz) {
            this.object = clazz;
        }

        @Override
        public String toString() {
            return "MetaInfServicesLookup.Item[" + clazz().getName() + "]"; // NOI18N
        }
        
        /** Finds the class.
         */
        private Class<? extends Object> clazz() {
            Object o = object;
            if (o instanceof CantInstantiate) {
                return ((CantInstantiate)o).clazz;
            }
            if (o instanceof Class<?>) {
                return (Class<? extends Object>) o;
            } else if (o != null) {
                return o.getClass();
            } else {
                // Broken.
                return Object.class;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Item) {
                return ((Item) o).clazz().equals(clazz());
            }

            return false;
        }

        @Override
        public int hashCode() {
            return clazz().hashCode();
        }

        protected @Override boolean instanceOf(Class<?> c) {
            return c.isAssignableFrom(clazz());
        }

        public @Override Class<?> getType() {
            return clazz();
        }

        public @Override Object getInstance() {
            Object o = object; // keeping local copy to avoid another
            if (o instanceof CantInstantiate) {
                return null;
            }

            // thread to modify it under my hands
            if (o instanceof Class<?>) {
                synchronized (o) { // o is Class and we will not create 
                                   // 2 instances of the same class
                    Class<?> c = ((Class<?>) o);
                    try {
                        o = CACHE.findInstance(c);

                        if (o == null) {
                            o = SharedClassObjectBridge.newInstance(c);
                            // if the instance was created during newInstance call
                            // and returned, return always the 1st instance, so 
                            // only a single instance is ever observable outside Lookup.
                            Object other = CACHE.findInstance(c);
                            if (other != null) {
                                return object = other;
                            }
                            CACHE.storeInstance(o);
                        }

                        // Do not assign to instance var unless there is a complete synch
                        // block between the newInstance and this line. Otherwise we could
                        // be assigning a half-constructed instance that another thread
                        // could see and return immediately.
                        object = o;
                    } catch (Exception ex) {
                        LOGGER.log(Level.INFO, "Cannot create " + object, ex);
                        object = new CantInstantiate(c);
                        return null;
                    } catch (LinkageError x) { // #174055 + NoClassDefFoundError
                        LOGGER.log(Level.FINE, "Cannot create " + object, x); //NOI18N
                        object = new CantInstantiate(c);
                        return null;
                    }
                }
            }

            return object;
        }

        public @Override String getDisplayName() {
            return clazz().getName();
        }

        public @Override String getId() {
            return clazz().getName();
        }

        protected @Override boolean creatorOf(Object obj) {
            return obj == object;
        }

    }
    private static final class CantInstantiate {
        final Class<?> clazz;

        public CantInstantiate(Class<?> clazz) {
            assert clazz != null;
            this.clazz = clazz;
        }
    }
}
