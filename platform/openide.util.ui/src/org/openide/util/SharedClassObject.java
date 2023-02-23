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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.implspi.SharedClassObjectBridge;

/** Shared object that allows different instances of the same class
* to share common data.
* <p>The data are shared only between instances of the same class (not subclasses).
* Thus, such "variables" have neither instance nor static behavior.
*
* @author Ian Formanek, Jaroslav Tulach
*/
public abstract class SharedClassObject extends Object implements Externalizable {

    static {
        SharedClassObjectBridge.setInstance(new SharedClassObjectBridge() {
            protected @Override <T> T findObject(Class<T> c) throws InstantiationException, IllegalAccessException {
                if (SharedClassObject.class.isAssignableFrom(c)) {
                    return c.cast(SharedClassObject.findObject(c.asSubclass(SharedClassObject.class), true));
                } else {
                    return null;
                }
            }
        });
    }

    /** serialVersionUID */
    private static final long serialVersionUID = 4527891234589143259L;

    /** property change support (PropertyChangeSupport) */
    private static final Object PROP_SUPPORT = new Object();

    /** Map (Class, DataEntry) that maps Classes to maps of any objects */
    private static final Map<Class,DataEntry> values = new WeakHashMap<Class,DataEntry>(37);

    /** A set of all classes for which we are currently inside createInstancePrivileged.
     * If a SCO constructor is called when an instance of that class already exists, normally
     * this will print a warning. However it is common to create an instance inside a static
     * block; in this case the constructor is actually called twice. Only the first instance
     * is ever returned, but this set ensures that no warning is printed during creation of the
     * second instance (because it is nobody's fault and it will be handled OK).
     * Map from class name to nesting count.
     */
    private static final Map<String,Integer> instancesBeingCreated = new HashMap<String,Integer>(7);

    /** Set of classes to not warn about any more.
     * Names only.
     */
    private static final Set<String> alreadyWarnedAboutDupes = new HashSet<String>();
    private static final Logger err = Logger.getLogger("org.openide.util.SharedClassObject"); // NOI18N

    /** data entry for this class */
    private final DataEntry dataEntry;

    /** Lock for the object */
    private Object lock;

    /** hard reference to primary instance of this class
    * This is here not to allow the finalization till at least
    * one object exists
    */
    private final SharedClassObject first;

    /** Stack trace indicating where the first instance was created.
     * This is only set on the first instance; and only with the error manager on.
     */
    private Throwable firstTrace = null;

    /** Set by {@link SystemOption}s through the special property, see {@link #putProperty}.
     * SystemOption needs special handling, e.g. it needs to be deserialized by the lookup
     * after its first instance is created in {@link #findObject} method, only
     * SystemOption can be reset.
     */
    private boolean systemOption = false;

    /** If set, this means we have a system option waiting to be loaded from lookup.
     * If anyone changes a property on it before this happens, the exception is filled in,
     * so we know when it is loaded that something went wrong.
     */
    private boolean waitingOnSystemOption = false;
    private IllegalStateException prematureSystemOptionMutation = null;
    private boolean inReadExternal = false;

    /** Check that addNotify, removeNotify, initialize call super sometime. */
    private boolean addNotifySuper;

    /** Check that addNotify, removeNotify, initialize call super sometime. */
    private boolean removeNotifySuper;

    /** Check that addNotify, removeNotify, initialize call super sometime. */
    private boolean initializeSuper;

    /** Create a shared object.
    * Typically shared-class constructors should not take parameters, since there
    * will conventionally be no instance variables.
    * @see #initialize
    */
    protected SharedClassObject() {
        synchronized (getLock()) {
            DataEntry de = values.get(getClass());

            //System.err.println("SCO create: " + this + " de=" + de);
            if (de == null) {
                de = new DataEntry();
                values.put(getClass(), de);
            }

            dataEntry = de;
            de.increase();

            // finds reference for the first object of the class
            first = de.first(this);
        }

        if (first != null) {
            if (first == this) {
                // Could be a performance hit, so only do this when developing.
                if (err.isLoggable(Level.FINE)) {
                    Throwable t = new Throwable("First instance created here"); // NOI18N
                    t.fillInStackTrace();
                    first.firstTrace = t;
                }
            } else {
                String clazz = getClass().getName();
                boolean creating;

                synchronized (instancesBeingCreated) {
                    creating = instancesBeingCreated.containsKey(clazz);
                }

                if (creating) {
                    //System.err.println ("Nesting: " + getClass ().getName () + " " + instancesBeingCreated.get (clazz));
                } else {
                    if (!alreadyWarnedAboutDupes.contains(clazz)) {
                        alreadyWarnedAboutDupes.add(clazz);

                        Exception e = new IllegalStateException(
                                        "Warning: multiple instances of shared class " + clazz + " created."
                            ); // NOI18N

                        if (first.firstTrace != null) {
                            err.log(Level.WARNING, "First stack trace", first.firstTrace);
                        } else {
                            err.warning("(Run with -J-Dorg.openide.util.SharedClassObject.level=0 for more details.)"); // NOI18N
                        }

                        err.log(Level.WARNING, null, e);
                    }
                }
            }
        }
    }

    /* Calls a referenceLost to decrease the counter on the shared data.
    * This method is final so no descendant can override it, but
    * it calls the method unreferenced() that can be overriden to perform any
    * additional tasks on finalizing.
    */
    protected final void finalize() throws Throwable {
        referenceLost();
    }

    /** Indicate whether the shared data of the last existing instance of this class
    * should be cleared when that instance is finalized.
    *
    * Subclasses may perform additional tasks
    * on finalization if desired. This method should be overridden
    * in lieu of {@link #finalize}.
    * <p>The default implementation returns <code>true</code>.
    * Classes which have precious shared data may want to return <code>false</code>, so that
    * all instances may be finalized, after which new instances will pick up the same shared variables
    * without requiring a recalculation.
    *
    * @return <code>true</code> if all shared data should be cleared,
    *   <code>false</code> if it should stay in memory
    */
    protected boolean clearSharedData() {
        return true;
    }

    /** Test whether the classes of the compared objects are the same.
    * @param obj the object to compare to
    * @return <code>true</code> if the classes are equal
    */
    public final boolean equals(Object obj) {
        return ((obj instanceof SharedClassObject) && (getClass().equals(obj.getClass())));
    }

    /** Get a hashcode of the shared class.
    * @return the hash code
    */
    public final int hashCode() {
        return getClass().hashCode();
    }

    /** Obtain lock for synchronization on manipulation with this
    * class.
    * Can be used by subclasses when performing nonatomic writes, e.g.
    * @return an arbitrary synchronizable lock object
    */
    protected final Object getLock() {
        if (lock == null) {
            lock = getClass().getName().intern();
        }

        return lock;
    }

    /** Should be called from within a finalize method to manage references
    * to the shared data (when the last reference is lost, the object is
    * removed)
    */
    private void referenceLost() {
        //System.err.println ("SharedClassObject.referenceLost:");
        //System.err.println ("\tLock: " + getLock());
        //System.err.println ("\tDataEntry: " + dataEntry);
        //System.err.println ("\tValues: " + values.containsKey(getClass()));
        synchronized (getLock()) {
            if ((dataEntry == null) || (dataEntry.decrease() == 0)) {
                if (clearSharedData()) {
                    // clears the data
                    values.remove(getClass());
                }
            }
        }

        //System.err.println("\tValues after: " + values.containsKey(getClass()));
    }

    /** Set a shared variable.
    * Automatically {@link #getLock locks}.
    * @param key name of the property
    * @param value value for that property (may be null)
    * @return the previous value assigned to the property, or <code>null</code> if none
    */
    protected final Object putProperty(Object key, Object value) {
        if (key == null) {
            throw new NullPointerException("Tried to pass null key (value=" + value + ") to putProperty"); // NOI18N
        }

        synchronized (getLock()) {
            if (
                waitingOnSystemOption && (key != PROP_SUPPORT) && (prematureSystemOptionMutation == null) &&
                    !dataEntry.isInInitialize() && !inReadExternal
            ) {
                // See below in findObject. Note that if we are still in initialize(),
                // it is harmless to set default values of properties, and from readExternal()
                // it is expected.
                prematureSystemOptionMutation = new IllegalStateException("...setting property here..."); // NOI18N
            }

            return dataEntry.getMap(this).put(key, value);

            //return dataEntry.getMap().put (key, value);
        }
    }

    /** Set a shared variable available only for string names.
    * Automatically {@link #getLock locks}.
     * <p><strong>Important:</strong> remember that <code>SharedClassObject</code>s
     * are like singleton beans; when you use <code>putProperty</code> with a value
     * of <code>true</code>, or call {@link #firePropertyChange}, you must consider that
     * the property name should match the JavaBeans name for a natural (introspected) property
     * for the bean, if such a property uses this key. For example, if you have a method
     * <code>getFoo</code> which uses {@link #getProperty} and a method <code>setFoo</code>
     * which uses <code>putProperty(..., true)</code>, then the key used <em>must</em>
     * be named <code>foo</code> (assuming you did not override this name in a BeanInfo).
     * Otherwise various listeners may not be prepared for the property change and may just
     * ignore it. For example, the property sheet for a <a href="@org-openide-nodes@/org/openide/nodes/BeanNode.html">BeanNode</a> based on a
     * <code>SharedClassObject</code> which stores its properties using a misnamed key
     * will probably not refresh correctly.
    * @param key name of the property
    * @param value value for that property (may be null)
    * @param notify should all listeners be notified about property change?
    * @return the previous value assigned to the property, or <code>null</code> if none
    */
    protected final Object putProperty(String key, Object value, boolean notify) {
        Object previous = putProperty(key, value);

        if (notify) {
            firePropertyChange(key, previous, value);
        }

        return previous;
    }

    /** Get a shared variable.
    * Automatically {@link #getLock locks}.
    * @param key name of the property
    * @return value of the property, or <code>null</code> if none
    */
    protected final Object getProperty(Object key) {
        synchronized (getLock()) {
            //System.err.println("SCO: " + this + " get: " + key + " de=" + dataEntry);
            if ("org.openide.util.SharedClassObject.initialize".equals(key)) { // NOI18N

                return dataEntry.isInInitialize() ? Boolean.TRUE : null;
            }

            return dataEntry.get(this, key);
        }
    }

    /** Initialize shared state.
    * Should use {@link #putProperty} to set up variables.
    * Subclasses should always call the super method.
    * <p>This method need <em>not</em> be called explicitly; it will be called once
    * the first time a given shared class is used (not for each instance!).
    */
    protected void initialize() {
        initializeSuper = true;
    }

    /** Adds the specified property change listener to receive property
     * change events from this object.
     * @param         l the property change listener
     */
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        boolean noListener;

        synchronized (getLock()) {
            //      System.out.println ("added listener: " + l + " to: " + getClass ()); // NOI18N
            PropertyChangeSupport supp = (PropertyChangeSupport) getProperty(PROP_SUPPORT);

            if (supp == null) {
                //        System.out.println ("Creating support"); // NOI18N
                putProperty(PROP_SUPPORT, supp = new PropertyChangeSupport(this));
            }

            noListener = !supp.hasListeners(null);
            supp.addPropertyChangeListener(l);
        }

        if (noListener) {
            addNotifySuper = false;
            addNotify();

            if (!addNotifySuper) {
                // [PENDING] theoretical race condition for this warning if listeners are added
                // and removed very quickly from two threads, I guess, and addNotify() impl is slow
                String msg = "You must call super.addNotify() from " + getClass().getName() + ".addNotify()"; // NOI18N
                err.warning(msg);
            }
        }
    }

    /**
     * Removes the specified property change listener so that it
     * no longer receives property change events from this object.
     * @param         l     the property change listener
     */
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        boolean callRemoved;

        synchronized (getLock()) {
            PropertyChangeSupport supp = (PropertyChangeSupport) getProperty(PROP_SUPPORT);

            if (supp == null) {
                return;
            }

            boolean hasListener = supp.hasListeners(null);
            supp.removePropertyChangeListener(l);
            callRemoved = hasListener && !supp.hasListeners(null);
        }

        if (callRemoved) {
            putProperty(PROP_SUPPORT, null); // clean the PCS, see #25417
            removeNotifySuper = false;
            removeNotify();

            if (!removeNotifySuper) {
                String msg = "You must call super.removeNotify() from " + getClass().getName() + ".removeNotify()"; // NOI18N
                err.warning(msg);
            }
        }
    }

    /** Notify subclasses that the first listener has been added to this object.
    * Subclasses should always call the super method.
    * The default implementation does nothing.
    */
    protected void addNotify() {
        addNotifySuper = true;
    }

    /** Notify subclasses that the last listener has been removed from this object.
    * Subclasses should always call the super method.
    * The default implementation does nothing.
    */
    protected void removeNotify() {
        removeNotifySuper = true;
    }

    /** Fire a property change event to all listeners.
    * @param name the name of the property
    * @param oldValue the old value
    * @param newValue the new value
    */

    // not final - SystemOption overrides it, e.g.
    protected void firePropertyChange(String name, Object oldValue, Object newValue) {
        PropertyChangeSupport supp = (PropertyChangeSupport) getProperty(PROP_SUPPORT);

        if (supp != null) {
            supp.firePropertyChange(name, oldValue, newValue);
        }
    }

    /** Writes nothing to the stream.
    * @param oo ignored
    */
    public void writeExternal(ObjectOutput oo) throws IOException {
    }

    /** Reads nothing from the stream.
    * @param oi ignored
    */
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
    }

    /** This method provides correct handling of serialization and deserialization.
    * When serialized the method writeExternal is used to store the state.
    * When deserialized first an instance is located by a call to findObject (clazz, true)
    * and then a method readExternal is called to read its state from stream.
    * <P>
    * This allows to have only one instance of the class in the system and work
    * only with it.
    *
    * @return write replace object that handles the described serialization/deserialization process
    */
    protected Object writeReplace() {
        return new WriteReplace(this);
    }

    /** Obtain an instance of the desired class, if there is one.
    * @param clazz the shared class to look for
    * @return the instance, or <code>null</code> if such does not exists
    */
    public static <T extends SharedClassObject> T findObject(Class<T> clazz) {
        return findObject(clazz, false);
    }

    /** Find an existing object, possibly creating a new one as needed.
    * To create a new instance the class must be public and have a public
    * default constructor.
    *
    * @param clazz the class of the object to find (must extend <code>SharedClassObject</code>)
    * @param create <code>true</code> if the object should be created if it does not yet exist
    * @return an instance, or <code>null</code> if there was none and <code>create</code> was <code>false</code>
    * @exception IllegalArgumentException if a new instance could not be created for some reason
    */
    public static <T extends SharedClassObject> T findObject(Class<T> clazz, boolean create) {
        // synchronizing on the same object as returned from getLock()
        synchronized (clazz.getName().intern()) {
            DataEntry de = values.get(clazz);

            // either null or the object
            SharedClassObject obj = (de == null) ? null : de.get();
            boolean created = false;

            if ((obj == null) && create) {
                // try to create new instance
                SetAccessibleAction action = new SetAccessibleAction(clazz);

                try {
                    obj = AccessController.doPrivileged(action);
                } catch (PrivilegedActionException e) {
                    Exception ex = e.getException();
                    IllegalArgumentException newEx = new IllegalArgumentException(ex.toString());
                    newEx.initCause(ex);
                    throw newEx;
                }

                created = true;
            }

            de = values.get(clazz);

            if (de != null) {
                SharedClassObject obj2 = de.get();

                if ((obj != null) && (obj != obj2)) {
                    // Tricked! The static initializer for the class called findObject on itself.
                    // So we created two instances of it.
                    // Returning only the first (that created by the static initializer, rather
                    // than by us explicitly), to avoid duplication.
                    //System.err.println ("Nesting #2: " + clazz.getName ());
                    if ((obj2 == null) && create) {
                        throw new IllegalStateException("Inconsistent state: " + clazz); // NOI18N
                    }

                    return clazz.cast(obj2);
                }
            }

            if (created) {
                // This hack was created due to the remove of SystemOptions deserialization
                // from project open operation, all SystemOptions are deserialized at this place
                // the first time anybody asks for the option.
                // It's crutial to do this just for SystemOptions and not for any other SharedClassObject,
                // otherwise it can cause deadlocks.
                // Lookup in the active session is used to find serialized state of the option,
                // if such state exists it is deserialized before the object is returned from lookup.
                if (obj.isSystemOption()) {
                    // Lookup will find serialized version of searched object and deserialize it
                    final Lookup.Result<T> r = Lookup.getDefault().lookup(new Lookup.Template<T>(clazz));

                    if (r.allInstances().isEmpty()) {
                        // #17711: folder lookup not yet initialized. Try to load the option later.
                        // In the meantime the default state of the option will be available.
                        // If any attempt is made to change the option, _and_ it is later loaded,
                        // then we print a stack trace of the mutation for debugging (since the mutations
                        // would get clobbered by loading the settings from layer or whatever).
                        obj.waitingOnSystemOption = true;

                        final SharedClassObject _obj = obj;
                        final IllegalStateException start = new IllegalStateException(
                                "Making a SystemOption here that is not in lookup..."
                            ); // NOI18N

                        class SOLoader implements LookupListener {
                            public void resultChanged(LookupEvent ev) {
                                if (!r.allInstances().isEmpty()) {
                                    // Got it.
                                    r.removeLookupListener(SOLoader.this);

                                    synchronized (_obj.getLock()) {
                                        _obj.waitingOnSystemOption = false;

                                        if (_obj.prematureSystemOptionMutation != null) {
                                            warn(start);
                                            warn(_obj.prematureSystemOptionMutation);
                                            warn(
                                                new IllegalStateException(
                                                    "...and maybe getting clobbered here, see #17711."
                                                )
                                            ); // NOI18N
                                            _obj.prematureSystemOptionMutation = null;
                                        }
                                    }
                                }
                            }
                        }
                        r.addLookupListener(new SOLoader());
                    }
                }
            }

            if ((obj == null) && create) {
                throw new IllegalStateException("Inconsistent state: " + clazz); // NOI18N
            }

            return clazz.cast(obj);
        }
    }

    /** checks whether we are instance of system option.
     */
    private boolean isSystemOption() {
        Class c = this.getClass();

        while (c != SharedClassObject.class) {
            if ("org.openide.options.SystemOption".equals(c.getName())) {
                return true; // NOI18N
            }

            c = c.getSuperclass();
        }

        return false;
    }

    // See above:
    private static void warn(Throwable t) {
        err.log(Level.WARNING, null, t);
    }

    static SharedClassObject createInstancePrivileged(Class<? extends SharedClassObject> clazz)
    throws Exception {
        java.lang.reflect.Constructor<? extends SharedClassObject> c = clazz.getDeclaredConstructor(new Class[0]);
        c.setAccessible(true);

        String name = clazz.getName();
        assert instancesBeingCreated != null;

        synchronized (instancesBeingCreated) {
            Integer i =  instancesBeingCreated.get(name);
            instancesBeingCreated.put(name, (i == null) ? new Integer(1) : new Integer(i.intValue() + 1));
        }

        try {
            return c.newInstance(new Object[0]);
        } finally {
            synchronized (instancesBeingCreated) {
                Integer i = instancesBeingCreated.get(name);

                if (i.intValue() == 1) {
                    instancesBeingCreated.remove(name);
                } else {
                    instancesBeingCreated.put(name, new Integer(i.intValue() - 1));
                }
            }

            c.setAccessible(false);
        }
    }

    /** Is called by the infrastructure in cases when a clean instance is requested.
     * As instances of <code>SharedClassObject</code> are singletons, there is
     * no way how to create new instance that would not contain the same data
     * as previously existing one. This method allows all subclasses that care
     * about the ability to refresh the settings (like <code>SystemOption</code>s)
     * to be notified about the cleaning request and clean their settings themselves.
     * <p>
     * Default implementation does nothing.
     *
     * @since made protected in version 4.46
     */
    protected void reset() {
    }

    /** Class that is used as default write replace.
    */
    static final class WriteReplace extends Object implements Serializable {
        /** serialVersionUID */
        static final long serialVersionUID = 1327893248974327640L;

        /** the class  */
        private Class<? extends SharedClassObject> clazz;

        /** class name, in case clazz could not be reloaded */
        private String name;

        /** shared instance */
        private transient SharedClassObject object;

        /** Constructor.
        * @param object instance
        */
        public WriteReplace(SharedClassObject object) {
            this.object = object;
            this.clazz = object.getClass();
            this.name = clazz.getName();
        }

        /** Write object.
        */
        private void writeObject(ObjectOutputStream oos)
        throws IOException {
            oos.defaultWriteObject();

            object.writeExternal(oos);
        }

        /** Read object.
        */
        private void readObject(ObjectInputStream ois)
        throws IOException, ClassNotFoundException {
            ois.defaultReadObject();

            if (clazz == null) {
                // Means that the class is no longer available in the restoring classloader.
                // Normal enough if the module has been uninstalled etc. #15654
                if (name != null) {
                    throw new ClassNotFoundException(name);
                } else {
                    // Compatibility with older WR's.
                    throw new ClassNotFoundException();
                }
            }

            object = findObject(clazz, true);
            object.inReadExternal = true;

            try {
                object.readExternal(ois);
            } finally {
                object.inReadExternal = false;
            }
        }

        /** Read resolve to the read object.
         * We give chance to actual instance to do its own resolution as well. It
         * is necessary for achieving back compatability of certain types of settings etc.
         */
        private Object readResolve() throws ObjectStreamException {
            SharedClassObject resolved = object;

            Method resolveMethod = findReadResolveMethod(object.getClass());

            if (resolveMethod != null) {
                // invoke resolve method and accept its result
                try {
                    // make readResolve accessible (it can have any access modifier)
                    resolveMethod.setAccessible(true);

                    return resolveMethod.invoke(object);
                } catch (Exception ex) {
                    // checked or runtime does not matter - we must survive
                    String banner = "Skipping " + object.getClass() + " resolution:"; //NOI18N
                    err.log(Level.WARNING, banner, ex);
                } finally {
                    resolveMethod.setAccessible(false);
                }
            }

            return resolved;
        }

        /** Tries to find readResolve method in given class. Finds
        * both public and non-public occurrences of the method and
        * searches also in superclasses */
        private static Method findReadResolveMethod(Class clazz) {
            Method result = null;

            //  try ANY-MODIFIER occurences; search also in superclasses
            for (Class<?> i = clazz; i != null; i = i.getSuperclass()) {
                try {
                    result = accept(i.getDeclaredMethod("readResolve")); // NOI18N

                    // get out of cycle if method found
                    if (result != null) {
                        break;
                    }
                } catch (NoSuchMethodException exc) {
                    // readResolve does not exist in current class
                }
            }

            return result;
        }

        /*
         * @return passed method if method matches exactly readResolve declaration as defined in
         *         Serializetion specification otherwise null
         */
        private static Method accept(Method candidate) {
            if (candidate != null) {
                // check exceptions clause
                Class[] result = candidate.getExceptionTypes();

                if ((result.length == 1) && ObjectStreamException.class.equals(result[0])) {
                    // returned value type
                    if (Object.class.equals(candidate.getReturnType())) {
                        return candidate;
                    }
                }
            }

            return null;
        }
    }

    /** The inner class that encapsulates the shared data together with
    * a reference counter
    */
    static final class DataEntry extends Object {
        /** The data */
        private HashMap<Object,Object> map;

        /** The reference counter */
        private int count = 0;

        /** weak reference to an object of this class */
        private WeakReference<SharedClassObject> ref = new WeakReference<SharedClassObject>(null);

        /** inited? */
        private boolean initialized = false;
        private boolean initializeInProgress = false;

        /** #7479: if initialize() threw unchecked exception, keep it here */
        private Throwable invalid = null;

        public String toString() { // for debugging

            return "SCO.DataEntry[ref=" + ref.get() + ",count=" + count + ",initialized=" + initialized + ",invalid=" +
            invalid + ",map=" + map + "]"; // NOI18N
        }

        /** initialize() is in progress? */
        boolean isInInitialize() {
            return initializeInProgress;
        }

        /** Returns the data
        * @param obj the requestor object
        * @return the data
        */
        Map<Object,Object> getMap(SharedClassObject obj) {
            ensureValid(obj);

            if (map == null) {
                // to signal invalid state
                map = new HashMap<Object,Object>();
            }

            if (!initialized) {
                initialized = true;

                // no data for this class yet
                tryToInitialize(obj);
            }

            return map;
        }

        /** Returns a value for given key
        * @param obj the requestor object
        * @return the data
        */
        Object get(SharedClassObject obj, Object key) {
            ensureValid(obj);

            Object ret;

            if (map == null) {
                // to signal invalid state
                map = new HashMap<Object,Object>();
                ret = null;
            } else {
                ret = map.get(key);
            }

            if ((ret == null) && !initialized) {
                if (key == PROP_SUPPORT) {
                    return null;
                }

                initialized = true;

                // no data for this class yet
                tryToInitialize(obj);
                ret = map.get(key);
            }

            return ret;
        }

        /** Returns the data
        * @return the data
        */
        Map getMap() {
            ensureValid(get());

            if (map == null) {
                // to signal invalid state
                map = new HashMap<Object,Object>();
            }

            return map;
        }

        private void ensureValid(SharedClassObject obj)
        throws IllegalStateException {
            if (invalid != null) {
                String msg;

                if (obj != null) {
                    msg = obj.toString();
                } else {
                    msg = "<unknown object>"; // NOI18N
                }

                throw (IllegalStateException) new IllegalStateException(msg).initCause(invalid);
            }
             // else fine
        }

        private void tryToInitialize(SharedClassObject obj)
        throws IllegalStateException {
            initializeInProgress = true;
            obj.initializeSuper = false;

            try {
                obj.initialize();
            } catch (Exception e) {
                invalid = e;
                throw (IllegalStateException) new IllegalStateException(invalid.toString() + " from " + obj).initCause(invalid); // NOI18N
            } catch (LinkageError e) {
                invalid = e;
                throw (IllegalStateException) new IllegalStateException(invalid.toString() + " from " + obj).initCause(invalid); // NOI18N
            } finally {
                initializeInProgress = false;
            }

            if (!obj.initializeSuper) {
                String msg = "You must call super.initialize() from " + obj.getClass().getName() + ".initialize()"; // NOI18N
                err.warning(msg);
            }
        }

        /** Increases the counter (thread safe)
        * @return new counter value
        */
        int increase() {
            return ++count;
        }

        /** Dereases the counter (thread safe)
        * @return new counter value
        */
        int decrease() {
            return --count;
        }

        /** Request for first object. If there is none, use the requestor
        * @param obj requestor
        * @return the an object of this type
        */
        SharedClassObject first(SharedClassObject obj) {
            SharedClassObject s = ref.get();

            if (s == null) {
                ref = new WeakReference<SharedClassObject>(obj);

                return obj;
            } else {
                return s;
            }
        }

        /** @return shared object or null
        */
        public SharedClassObject get() {
            return ref.get();
        }

        /** Reset map of values. */
        public void reset(SharedClassObject obj) {
            SharedClassObject s = get();

            if ((s != null) && (s != obj)) {
                return;
            }

            invalid = null;
            getMap().clear();

            initialized = true;
            tryToInitialize(obj);
        }
    }

    static final class SetAccessibleAction implements PrivilegedExceptionAction<SharedClassObject> {
        Class<? extends SharedClassObject> klass;

        SetAccessibleAction(Class<? extends SharedClassObject> klass) {
            this.klass = klass;
        }

        public SharedClassObject run() throws Exception {
            return createInstancePrivileged(klass);
        }
    }
}
