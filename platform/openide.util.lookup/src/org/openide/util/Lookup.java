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

package org.openide.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.openide.util.GlobalLookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * A general registry permitting clients to find instances of services
 * (implementation of a given interface).
 * This class is inspired by the
 * <a href="https://river.apache.org">Jini</a>
 * registration and lookup mechanism. The difference is that the methods do
 * not throw checked exceptions (as they usually work only locally and not over the network)
 * and that the Lookup API concentrates on the lookup, not on the registration
 * (although {@link Lookup#getDefault()} is strongly encouraged to support
 * {@link Lookups#metaInfServices(java.lang.ClassLoader) } for registration in addition to whatever
 * else it decides to support).
 * <p>
 * For a general talk about the idea behind the lookup pattern please see
 * <UL>
 *      <LI><a href="lookup/doc-files/index.html">The Solution to Communication Between Components</a>
 *      page
 *      <LI>the introduction to the <a href="lookup/doc-files/lookup-api.html">lookup API via
 *      use cases</a>
 *      <LI>the examples of <a href="lookup/doc-files/lookup-spi.html">how to write your own lookup</a>
 * </UL>
 *
 * @see org.openide.util.lookup.AbstractLookup
 * @see Lookups
 * @see LookupListener
 * @see LookupEvent
 * @author  Jaroslav Tulach
 */
public abstract class Lookup {
    private static final Logger LOG = Logger.getLogger("org.openide.util.lookup.init"); // NOI18N
    
    /** A dummy lookup that never returns any results.
     */
    public static final Lookup EMPTY = new Empty();

    /** default instance */
    private static Lookup defaultLookup;
    
    /**
     * Default instance's provider
     */
    private static Lookup.Provider defaultLookupProvider;

    /** Empty constructor for use by subclasses. */
    public Lookup() {
    }

    /** Static method to obtain the global lookup in the whole system.
     * The actual returned implementation can be different in different
     * systems, but the default one is based on
     * {@link org.openide.util.lookup.Lookups#metaInfServices}
     * with the context classloader of the first caller. Each system is
     * adviced to honor this and include some form of <code>metaInfServices</code>
     * implementation in the returned lookup as usage of <code>META-INF/services</code>
     * is a JDK standard.
     *
     * @return the global lookup in the system
     * @see ServiceProvider
     */
    public static synchronized Lookup getDefault() {
        Lookup gLpk = GlobalLookup.current();
        if (gLpk != null) {
            return gLpk;
        }
        
        if (defaultLookup != null || defaultLookupProvider != null) {
            if (defaultLookupProvider != null) {
                Lookup lkp = defaultLookupProvider.getLookup();
                if (lkp != null) {
                    return lkp;
                }
            }
            return defaultLookup;
        }
        LOG.log(Level.FINER, "About to initialize Lookup@{0}.getDefault() by {1}", 
            new Object[] { Lookup.class.getClassLoader(), Thread.currentThread() }
        );
        // You can specify a Lookup impl using a system property if you like.
        String className = System.getProperty("org.openide.util.Lookup"); // NOI18N

        LOG.log(Level.FINER, "Specified by property? Value: {0}", className);

        if ("-".equals(className)) { // NOI18N
            // Suppress even MetaInfServicesLookup.
            return EMPTY;
        }

        ClassLoader l = Thread.currentThread().getContextClassLoader();
        LOG.log(Level.FINER, "Searching in classloader {0}", l);
        try {
            if (className != null) {
                Object o = Class.forName(className, true, l).getDeclaredConstructor().newInstance();
                defaultLookup = (Lookup)o;
                // set the global global Lookuo
                GlobalLookup.setSystemLookup(defaultLookup);
                LOG.log(Level.FINE, "Default lookup initialized {0}", defaultLookup);
                // for testing purposes, tests may setup a class implementing both interfaces
                if (o instanceof Lookup.Provider) {
                    defaultLookupProvider = (Lookup.Provider)o;
                    Lookup lkp = defaultLookupProvider.getLookup();
                    if (lkp != null) {
                        return lkp;
                    }
                }
                return defaultLookup;
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Constuction of " + className + " via " + l + " failed", e);
        }

        // OK, none specified (successfully) in a system property.
        // Try MetaInfServicesLookup as a default, which may also
        // have a org.openide.util.Lookup line specifying the lookup.
        Lookup misl = Lookups.metaInfServices(l);
        defaultLookup = misl.lookup(Lookup.class);
        LOG.log(Level.FINER, "Searching for {0} in {1} yields {2}", new Object[]{Lookup.class, misl, defaultLookup});
        if (defaultLookup != null) {
            if (defaultLookup instanceof Lookup.Provider) {
                defaultLookupProvider = (Lookup.Provider)defaultLookup;
                Lookup lkp = defaultLookupProvider.getLookup();
                if (lkp != null) {
                    return lkp;
                }
            }
            LOG.log(Level.FINE, "Default lookup initialized {0}", defaultLookup);
            return defaultLookup;
        }

        // You may also specify a Lookup.Provider.
        Lookup.Provider prov = misl.lookup(Lookup.Provider.class);
        LOG.log(Level.FINER, "Searching for {0} in {1} yields {2}", new Object[]{Lookup.Provider.class, misl, defaultLookup});
        if (prov != null) {
            defaultLookup = Lookups.proxy(prov);
            LOG.log(Level.FINE, "Default lookup initialized {0}", defaultLookup);
            return defaultLookup;
        }

        DefLookup def = new DefLookup();
        def.init(l, misl, false);
        defaultLookup = def;
        def.init(l, misl, true);
        LOG.log(Level.FINE, "Default lookup initialized {0}", defaultLookup);
        return defaultLookup;
    }
    
    private static final class DefLookup extends ProxyLookup {
        public DefLookup() {
            super(new Lookup[0]);
        }
        
        public void init(ClassLoader loader, Lookup metaInfLookup, boolean addPath) {
            // Had no such line, use simple impl.
            // It does however need to have ClassLoader available or many things will break.
            // Use the thread context classloader in effect now.
            Lookup clLookup = Lookups.singleton(loader);
            List<Lookup> arr = new ArrayList<Lookup>();
            arr.add(metaInfLookup);
            arr.add(clLookup);
            String paths = System.getProperty("org.openide.util.Lookup.paths"); // NOI18N
            if (addPath && paths != null) {
                LOG.log(Level.FINE, "Adding search paths {0}", paths);
                for (String p : paths.split(":")) { // NOI18N
                    arr.add(Lookups.forPath(p));
                }
            }
            LOG.log(Level.FINER, "Setting DefLookup delegates {0}", arr);
            setLookups(arr.toArray(new Lookup[0]));
        }
    }
    
    /** Called from MockServices to reset default lookup in case services change
     */
    private static synchronized void resetDefaultLookup() {
        if (defaultLookup == null || defaultLookup instanceof DefLookup) {
            DefLookup def = (DefLookup)defaultLookup;
            ClassLoader l = Thread.currentThread().getContextClassLoader();
            Lookup misl = Lookups.metaInfServices(l);
            if (def == null) {
                def = new DefLookup();
                def.init(l, misl, false);
                defaultLookup = def;
            }
            def.init(l, misl, true);
        }
    }

    /** Look up an object matching a given interface.
     * This is the simplest method to use.
     * If more than one object matches, the first will be returned.
     * The template class may be a class or interface; the instance is
     * guaranteed to be assignable to it.
     * 
     * @param <T> type of interface we are searching for
     * @param clazz class of the object we are searching for
     * @return an object implementing the given class or <code>null</code> if no such
     *         implementation is found
     */
    public abstract <T> T lookup(Class<T> clazz);

    /** The general lookup method. Callers can get list of all instances and classes
     * that match the given <code>template</code>, request more info about
     * them in form of {@link Lookup.Item} and attach a listener to
     * this be notified about changes. The general interface does not
     * specify whether subsequent calls with the same template produce new
     * instance of the {@link Lookup.Result} or return shared instance. The
     * prefered behaviour however is to return shared one.
     * 
     * @param <T> type of interface we are searching for
     * @param template a template describing the services to look for
     * @return an object containing the results
     */
    public abstract <T> Result<T> lookup(Template<T> template);

    /** Look up the first item matching a given template.
     * Includes not only the instance but other associated information.
     * 
     * @param <T> type of interface we are searching for
     * @param template the template to check
     * @return a matching item or <code>null</code>
     *
     * @since 1.8
     */
    public <T> Item<T> lookupItem(Template<T> template) {
        Result<T> res = lookup(template);
        Iterator<? extends Item<T>> it = res.allItems().iterator();
        return it.hasNext() ? it.next() : null;
    }

    /**
     * Find a result corresponding to a given class.
     * Equivalent to calling {@link #lookup(Lookup.Template)} but slightly more convenient.
     * Subclasses may override this method to produce the same semantics more efficiently.
     * 
     * @param <T> type of interface we are searching for
     * @param clazz the supertype of the result
     * @return a live object representing instances of that type
     * @since org.openide.util 6.10
     */
    public <T> Lookup.Result<T> lookupResult(Class<T> clazz) {
        return lookup(new Lookup.Template<T>(clazz));
    }

    /**
     * Find all instances corresponding to a given class.
     * Equivalent to calling {@link #lookupResult} and asking for {@link Lookup.Result#allInstances} but slightly more convenient.
     * Subclasses may override this method to produce the same semantics more efficiently.
     * <p>Example usage:</p>
     * {@snippet file="org/openide/util/lookup/SampleLookupUsages.java" region="iterate"}
     * @param clazz the supertype of the result
     * @return all currently available instances of that type
     * @since org.openide.util 6.10
     */
    public <T> Collection<? extends T> lookupAll(Class<T> clazz) {
        return lookupResult(clazz).allInstances();
    }

    /**
     * Objects implementing interface Lookup.Provider are capable of
     * and willing to provide a lookup (usually bound to the object).
     * @since 3.6
     */
    public interface Provider {
        /**
         * Returns lookup associated with the object.
         * @return fully initialized lookup instance provided by this object
         */
        Lookup getLookup();
    }

    /*
     * I expect this class to grow in the future, but for now, it is
     * enough to start with something simple.
     */

    /** Template defining a pattern to filter instances by.
     */
    public static final class Template<T> extends Object {
        /** cached hash code */
        private int hashCode;

        /** type of the service */
        private Class<T> type;

        /** identity to search for */
        private String id;

        /** instance to search for */
        private T instance;

        /** General template to find all possible instances.
         * @deprecated Use <code>new Template (Object.class)</code> which
         *   is going to be better typed with JDK1.5 templates and should produce
         *   the same result.
         */
        @Deprecated
        public Template() {
            this(null);
        }

        /** Create a simple template matching by class.
         * @param type the class of service we are looking for (subclasses will match)
         */
        public Template(Class<T> type) {
            this(type, null, null);
        }

        /** Constructor to create new template.
         * @param type the class of service we are looking for or <code>null</code> to leave unspecified
         * @param id the ID of the item/service we are looking for or <code>null</code> to leave unspecified
         * @param instance a specific known instance to look for or <code>null</code> to leave unspecified
         */
        public Template(Class<T> type, String id, T instance) {
            this.type = extractType(type);
            this.id = id;
            this.instance = instance;
        }

        @SuppressWarnings("unchecked")
        private Class<T> extractType(Class<T> type) {
            return (type == null) ? (Class<T>)Object.class : type;
        }

        /** Get the class (or superclass or interface) to search for.
         * If it was not specified in the constructor, <code>Object</code> is used as
         * this will match any instance.
         * @return the class to search for
         */
        public Class<T> getType() {
            return type;
        }

        /** Get the persistent identifier being searched for, if any.
         * @return the ID or <code>null</code>
         * @see Lookup.Item#getId
         *
         * @since 1.8
         */
        public String getId() {
            return id;
        }

        /** Get the specific instance being searched for, if any.
         * Most useful for finding an <code>Item</code> when the instance
         * is already known.
         *
         * @return the object to find or <code>null</code>
         *
         * @since 1.8
         */
        public T getInstance() {
            return instance;
        }

        /* Computes hashcode for this template. The hashcode is cached.
         * @return hashcode
         */
        @Override
        public int hashCode() {
            if (hashCode != 0) {
                return hashCode;
            }

            hashCode = ((type == null) ? 1 : type.hashCode()) + ((id == null) ? 2 : id.hashCode()) +
                ((instance == null) ? 3 : 0);

            return hashCode;
        }

        /* Checks whether two templates represent the same query.
         * @param obj another template to check
         * @return true if so, false otherwise
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Template)) {
                return false;
            }

            Template t = (Template) obj;

            if (hashCode() != t.hashCode()) {
                // this is an optimalization - the hashCodes should have been
                // precomputed
                return false;
            }

            if (type != t.type) {
                return false;
            }

            if (id == null) {
                if (t.id != null) {
                    return false;
                }
            } else {
                if (!id.equals(t.id)) {
                    return false;
                }
            }

            if (instance == null) {
                return (t.instance == null);
            } else {
                return instance.equals(t.instance);
            }
        }

        /* for debugging */
        @Override
        public String toString() {
            return "Lookup.Template[type=" + type + ",id=" + id + ",instance=" + instance + "]"; // NOI18N
        }
    }

    /** Result of a lookup request.
     * Allows access to all matching instances at once.
     * Also permits listening to changes in the result.
     * Result can contain duplicate items.
     */
    public abstract static class Result<T> extends Object {
        /** Registers a listener that is invoked when there is a possible
         * change in this result. 
         *
         * <div class="nonnormative">
         * Sometimes people report that their listener is not receiving 
         * events (for example <a href="https://bz.apache.org/netbeans/show_bug.cgi?id=191471">IZ 191471</a>)
         * or that the listener receives few events, but then it <em>stops</em>
         * listening.
         * Such behavior is often caused by not keeping strong reference to 
         * the {@link Result} object. When it gets garbage collected
         * it can no longer deliver events. Thus remember to keep reference
         * to the object you are attaching listener to.
         * </div>
         *
         * @param l the listener to add
         */
        public abstract void addLookupListener(LookupListener l);

        /** Unregisters a listener previously added.
         * @param l the listener to remove
         */
        public abstract void removeLookupListener(LookupListener l);

        /** Get all instances in the result. The return value is an
         * unmodifiable list (hence the type
         * should be {@link List} as the order matters, but the {@link Collection}
         * is kept for compatibility reasons) of all instances present in
         * the {@link Result} right now that will never change its content.
         *
         * <div class="nonnormative">
         * While the returned collection never changes its content, some
         * implementation like {@link ProxyLookup} may
         * <a href="@TOP@/apichanges.html#lazy.proxy.lookup">compute the content
         * lazily</a>. At least
         * <a href="https://github.com/apache/netbeans/pull/1739">once</a>
         * such behavior resulted in a deadlock.
         * </div>
         * @return unmodifiable collection of all instances that will never change its content
         */
        public abstract Collection<? extends T> allInstances();

        /** Get all classes represented in the result.
         * That is, the set of concrete classes
         * used by instances present in the result.
         * All duplicate classes will be omitted.
         * @return unmodifiable set of <code>Class</code> objects that will never change its content
         *
         * @since 1.8
         */
        public Set<Class<? extends T>> allClasses() {
            return Collections.emptySet();
        }

        /** Get all registered items.
         * This includes all pairs of instances together
         * with their classes, {@link Item#getId() IDs}, and so on.
         * The return value is an unmodifiable list (hence the type
         * should be {@link List} as the order matters, but the {@link Collection}
         * is kept for compatibility reasons) of all {@link Item items} present in
         * the {@link Result} right now that will never change its content.
         *
         * <div class="nonnormative">
         * While the returned collection never changes its content, some
         * implementation like {@link ProxyLookup} may
         * <a href="@TOP@/apichanges.html#lazy.proxy.lookup">compute the content
         * lazily</a>.
         * </div>
         *
         * @return unmodifiable collection of {@link Lookup.Item} that will never change its content
         *
         * @since 1.8
         */
        public Collection<? extends Item<T>> allItems() {
            return Collections.emptyList();
        }
    }

    /** A single item in a lookup result.
     * This wrapper provides unified access to not just the instance,
     * but its class, a possible persistent identifier, and so on.
     *
     * @since 1.25
     */
    public abstract static class Item<T> extends Object {
        /** Get the instance itself.
         * @return the instance or null if the instance cannot be created
         */
        public abstract T getInstance();

        /** Get the implementing class of the instance.
         * @return the class of the item
         */
        public abstract Class<? extends T> getType();

        // XXX can it be null??

        /** Get a persistent identifier for the item.
         * This identifier should uniquely represent the item
         * within its containing lookup (and if possible within the
         * global lookup as a whole). For example, it might represent
         * the source of the instance as a file name. The ID may be
         * persisted and in a later session used to find the same instance
         * as was encountered earlier, by means of passing it into a
         * lookup template.
         *
         * @return a string ID of the item
         */
        public abstract String getId();

        /** Get a human presentable name for the item.
         * This might be used when summarizing all the items found in a
         * lookup result in some part of a GUI.
         * @return the string suitable for presenting the object to a user
         */
        public abstract String getDisplayName();

        /* show ID for debugging */
        @Override
        public String toString() {
            return getId();
        }
    }

    //
    // Implementation of the default lookup
    //
    private static final class Empty extends Lookup {
        private static final Result NO_RESULT = new Result() {
                public void addLookupListener(LookupListener l) {
                }

                public void removeLookupListener(LookupListener l) {
                }

                public Collection allInstances() {
                    return Collections.EMPTY_SET;
                }
            };

        Empty() {
        }

        public <T> T lookup(Class<T> clazz) {
            return null;
        }

        @SuppressWarnings("unchecked")
        public <T> Result<T> lookup(Template<T> template) {
            return NO_RESULT;
        }
    }
}
