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

package org.openide.util.lookup;

import java.util.Arrays;
import org.netbeans.modules.openide.util.GlobalLookup;
import org.openide.util.Lookup;
import org.openide.util.lookup.implspi.NamedServicesProvider;

/**
 * Static factory methods for creating common lookup implementations.
 *
 * @author David Strupl
 * @since 2.21
 */
public class Lookups {

    /** static methods only */
    private Lookups() {}

    /**
     * Creates a singleton lookup. It means lookup that contains only
     * one object specified via the supplied parameter. The lookup will
     * either return the object or null if the supplied template does
     * not match the class. If the specified argument is null the method
     * will end with NullPointerException.
     * @param objectToLookup object to lookup
     * @return Fully initialized lookup object ready to use
     * @throws NullPointerException if the supplied argument is null
     * @since 2.21
     */
    public static Lookup singleton(Object objectToLookup) {
        if (objectToLookup == null) {
            throw new NullPointerException();
        }

        return new SingletonLookup(objectToLookup);
    }

    /**
     * Creates a lookup that contains an array of objects specified via the
     * parameter. The resulting lookup is fixed in the following sense: it
     * contains only fixed set of objects passed in by the array parameter.
     * Its contents never changes so registering listeners on such lookup
     * does not have any observable effect (the listeners are never called).
     *
     * @param objectsToLookup list of objects to include
     * @return Fully initialized lookup object ready to use
     * @throws NullPointerException if the supplied argument is null
     * @since 2.21
     *
     */
    public static Lookup fixed(Object... objectsToLookup) {
        if (objectsToLookup == null) {
            throw new NullPointerException();
        }

        if (objectsToLookup.length == 0) {
            return Lookup.EMPTY;
        }

        if (objectsToLookup.length == 1) {
            return singleton(objectsToLookup[0]);
        }

        return new SimpleLookup(Arrays.asList(objectsToLookup));
    }

    /**
     * Creates a lookup that contains an array of objects specified via the
     * parameter. The resulting lookup is fixed in the following sense: it
     * contains only fixed set of objects passed in by the array parameter.
     * The objects returned from this lookup are converted to real objects
     * before they are returned by the lookup.
     * Its contents never changes so registering listeners on such lookup
     * does not have any observable effect (the listeners are never called).
     *
     * @param <T> type for key
     * @param <R> type for result
     * @param keys set of object
     * @param convertor convertor for object
     * @return Fully initialized lookup object ready to use
     * @throws NullPointerException if the any of the arguments is null
     * @since 2.21
     *
     */
    public static <T,R> Lookup fixed(T[] keys, InstanceContent.Convertor<? super T,R> convertor) {
        if (keys == null) {
            throw new NullPointerException();
        }

        if (convertor == null) {
            throw new NullPointerException();
        }

        return new SimpleLookup(Arrays.asList(keys), convertor);
    }

    /** Creates a lookup that delegates to another one but that one can change
     * from time to time. The returned lookup checks every time somebody calls
     * <code>lookup</code> or <code>lookupItem</code> method whether the
     * provider still returns the same lookup. If not, it updates state of
     * all {@link org.openide.util.Lookup.Result}s 
     * that it created (and that still exists).
     * <P>
     * The user of this method has to implement its provider's <code>getLookup</code>
     * method (must be thread safe and fast, will be called often and from any thread)
     * pass it to this method and use the returned lookup. Whenever the user
     * changes the return value from the <code>getLookup</code> method and wants
     * to notify listeners on the lookup about that it should trigger the event
     * firing, for example by calling <code>lookup.lookup (Object.class)</code>
     * directly on the lookup returned by this method
     * that forces a check of the return value of {@link org.openide.util.Lookup.Provider#getLookup}.
     *
     * @param provider the provider that returns a lookup to delegate to
     * @return lookup delegating to the lookup returned by the provider
     * @since 3.9
     */
    public static Lookup proxy(Lookup.Provider provider) {
        return new SimpleProxyLookup(provider);
    }

    /** Returns a lookup that implements the JDK1.3 JAR services mechanism and delegates
     * to META-INF/services/name.of.class files.
     * <p>Some extensions to the JAR services specification are implemented:
     * <ol>
     * <li>An entry may be followed by a line of the form <code>#position=<i>integer</i></code>
     *     to specify ordering. (Smaller numbers first, entries with unspecified position last.)
     * <li>A line of the form <code>#-<i>classname</i></code> suppresses an entry registered
     *     in another file, so can be used to supersede one implementation with another.
     * </ol>
     * <p>Note: It is not dynamic - so if you need to change the classloader or JARs,
     * wrap it in a {@link ProxyLookup} and change the delegate when necessary.
     * Existing instances will be kept if the implementation classes are unchanged,
     * so there is "stability" in doing this provided some parent loaders are the same
     * as the previous ones.
     * @param classLoader class loader to use for loading
     * @return lookup associatied with classloader
     * @since 3.35
     * @see ServiceProvider
     */
    public static Lookup metaInfServices(ClassLoader classLoader) {
        return new MetaInfServicesLookup(classLoader, "META-INF/services/"); // NOI18N
    }

    /** Returns a lookup that behaves exactly like {@link #metaInfServices(ClassLoader)}
     * except that it does not read data from <code>META-INF/services/</code>, but instead
     * from the specified prefix.
     * @param classLoader class loader to use for loading
     * @param prefix prefix to prepend to the class name when searching
     * @return lookup associatied with classloader
     * @since 7.9
     * @see ServiceProvider#path
     */
    public static Lookup metaInfServices(ClassLoader classLoader, String prefix) {
        return new MetaInfServicesLookup(classLoader, prefix);
    }
    
    /** Creates a <code>named</code> lookup.
     * It is a lookup identified by a given path.
     * Two lookups with the same path should have the same content.
     * <p>It is expected that each <code>named</code> lookup
     * will contain a superset of what would be created by:
     * <code>{@linkplain #metaInfServices(ClassLoader,String) metaInfServices}(theRightLoader, "META-INF/namedservices/" + path + "/")</code>
     *
     * <p class="nonnormative">Various environments can add their own
     * extensions to its content. As such
     * {@link Lookups#forPath(java.lang.String)} can combine lookups
     * from several sources. In current NetBeans Runtime Container, two lookups are used:
     * </p>
     * <ul>
     * <li><code>Lookups.metaInfServices("META-INF/namedservices/" + path)</code></li>
     * <li><code>org.openide.loaders.FolderLookup(path)</code></li>
     * </ul>
     * <p class="nonnormative">
     * Please note that these lookups differ in the way they inspect sub-folders.
     * The first lookup just returns instances from the given path, ignoring
     * sub-folders, the second one retrieves instances from the whole sub-tree.
     * </p>
     * <p>
     * Read more about the <a href="@org-openide-util@/org/openide/util/doc-files/api.html#folderlookup">usage of this method</a>.
     * 
     * @param path the path identifying the lookup, e.g. <code>Servers/J2EEWrapper</code>
     * @return lookup associated with this path
     * @since 7.9
     * @see NamedServiceDefinition
     */
    public static Lookup forPath(String path) {
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        return NamedServicesProvider.forPath(path);
    }
    
    /** Creates a lookup that wraps another one and filters out instances
     * of specified classes. If you have a lookup and
     * you want to remove all instances of ActionMap you can use:
     * <pre>
     * l = Lookups.exclude(lookup, ActionMap.class);
     * </pre>
     * Then anybody who asks for <code>l.lookup(ActionMap.class)</code> or
     * subclass will get <code>null</code>. Even if the original lookup contains the
     * value.
     * To create empty lookup (well, just an example, otherwise use {@link Lookup#EMPTY}) one could use:
     * <pre>
     * Lookup.exclude(anyLookup, Object.class);
     * </pre>
     * as any instance in any lookup is of type Object and thus would be excluded.
     * <p>
     * The complete behavior can be described as <code>classes</code> being
     * a barrier. For an object not to be excluded, there has to be an inheritance
     * path between the queried class and the actual class of the instance,
     * that is not blocked by any of the excluded classes and
     * the queried class cannot be subclass of an excluded class:
     * <pre>
     * interface A {}
     * interface B {}
     * class C implements A, B {}
     * Object c = new C();
     * Lookup l1 = Lookups.singleton(c);
     * Lookup l2 = Lookups.exclude(l1, A.class);
     * assertNull("A is directly excluded", l2.lookup(A.class));
     * assertEquals("Returns C as A.class is not between B and C", c, l2.lookup(B.class));
     * </pre>
     * For more info check the
     * <a href="https://github.com/apache/netbeans/tree/master/platform/openide.util.lookup/test/unit/src/org/openide/util/lookup/ExcludingLookupTest.java">
     * excluding lookup tests</a> and the discussion in issue
     * <a href="https://bz.apache.org/netbeans/show_bug.cgi?id=53058">53058</a>.
     *
     * @param lookup the original lookup that should be filtered
     * @param classes array of classes those instances should be excluded
     * @return filtred lookup
     * @since 5.4
     */
    public static Lookup exclude(Lookup lookup, Class... classes) {
        return new ExcludingLookup(lookup, classes);
    }

    /** Creates <code>Lookup.Item</code> representing the instance passed in.
     * @param <T> type of the object
     * @param instance the object for which Lookup.Item should be creted
     * @param id unique identification of the object, for details see {@link org.openide.util.Lookup.Item#getId},
     * can be <code>null</code>
     * @return lookup item representing instance
     * @since 4.8
     */
    public static <T> Lookup.Item<T> lookupItem(T instance, String id) {
        return new LookupItem<T>(instance, id);
    }
    
    /** Temporarily (while the <code>code</code> is running) changes value
     * of {@link Lookup#getDefault()} to here-in provided lookup. Useful in a
     * multi user environment where different users and their requests should
     * be associated with different content of default lookup.
     * <p>
     * As a special case, {@code executeWith} will execute the Runnable with
     * the system global lookup (the one effective during system bootstrap), when
     * the passed {@code defaultLookup} parameter is {@code null}. This feature may
     * be useful to switch from a specialized Lookup back to a default one for
     * some limited processing, or when the caller needs to bypass potential
     * execution-local content temporary effective in the default Lookup and
     * work with system-wide services only.
     * 
     * @param defaultLookup the lookup to be come default while code is running
     * @param code the code to execute (synchronously) before the method returns
     * @since 8.30
     * @since 8.31 can delegate to the system Lookup
     */
    public static void executeWith(Lookup defaultLookup, Runnable code) {
        if (!GlobalLookup.execute(defaultLookup, code)) {
            code.run();
        }
    }

    private static class LookupItem<T> extends Lookup.Item<T> {
        private String id;
        private T instance;

        public LookupItem(T instance) {
            this(instance, null);
        }

        public LookupItem(T instance, String id) {
            this.id = id;
            this.instance = instance;
        }

        public String getDisplayName() {
            return getId();
        }

        public String getId() {
            return (id == null) ? instance.toString() : id;
        }

        public T getInstance() {
            return instance;
        }

        @SuppressWarnings("unchecked")
        public Class<? extends T> getType() {
            return (Class<? extends T>)instance.getClass();
        }

        public @Override boolean equals(Object object) {
            if (object instanceof LookupItem) {
                return instance == ((LookupItem) object).getInstance();
            }

            return false;
        }

        public @Override int hashCode() {
            return instance.hashCode();
        }
    }
     // End of LookupItem class
}
