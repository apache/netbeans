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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.EventListener;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A listener wrapper that delegates to another listener but hold
 * only weak reference to it, so it does not prevent it to be finalized.
 *
 * @author Jaroslav Tulach
 */
abstract class WeakListenerImpl implements java.util.EventListener {

    private static final Logger LOG = Logger.getLogger(WeakListenerImpl.class.getName());

    /** weak reference to listener */
    private ListenerReference ref;

    /** class of the listener */
    Class<?> listenerClass;

    /** weak reference to source */
    private Reference<Object> source;

    /**
     * @param listenerClass class/interface of the listener
     * @param l listener to delegate to, <code>l</code> must be an instance of
     * listenerClass
     */
    protected WeakListenerImpl(Class<?> listenerClass, java.util.EventListener l) {
        this(listenerClass, l, null);
    }
    
    /**
     * @param listenerClass class/interface of the listener
     * @param l listener to delegate to, <code>l</code> must be an instance of
     * listenerClass
     */
    protected WeakListenerImpl(Class<?> listenerClass, java.util.EventListener l, String name) {
        this.listenerClass = listenerClass;
        ref = new ListenerReference(l, name, this);
    }

    /** Setter for the source field. If a WeakReference to an underlying listener is
     * cleared and enqueued, that is, the original listener is garbage collected,
     * then the source field is used for deregistration of this WeakListenerImpl, thus making
     * it eligible for garbage collection if no more references exist.
     *
     * This method is particularly useful in cases where the underlying listener was
     * garbage collected and the event source, on which this listener is listening on,
     * is quiet, i.e. does not fire any events for long periods. In this case, this listener
     * is not removed from the event source until an event is fired. If the source field is
     * set however, WeakListenerImpls that lost their underlying listeners are removed
     * as soon as the ReferenceQueue notifies the WeakListenerImpl.
     *
     * @param source is any Object or <code>null</code>, though only setting an object
     * that has an appropriate remove*listenerClass*Listener method and on which this listener is listening on,
     * is useful.
     */
    protected final void setSource(Object source) {
        if (source == null) {
            this.source = null;
        } else {
            this.source = new WeakReference<Object>(source);
        }
    }

    final Object getSource() {
        Reference<Object> r = this.source;
        return r == null ? null : r.get();
    }

    /** Method name to use for removing the listener.
    * @return name of method of the source object that should be used
    *   to remove the listener from listening on source of events
    */
    protected abstract String removeMethodName();

    /** Getter for the target listener.
    * @param ev the event the we want to distribute
    * @return null if there is no listener because it has been finalized
    */
    protected final java.util.EventListener get(java.util.EventObject ev) {
        Object l = ref.get(); // get the consumer

        // if the event consumer is gone, unregister us from the event producer
        if (l == null) {
            ref.requestCleanUp((ev == null) ? null : ev.getSource());
        }

        return (EventListener) l;
    }

    Object getImplementator() {
        return this;
    }

    @Override
    public String toString() {
        Object listener = ref.get();

        return getClass().getName() + "[" + ((listener == null) ? "null" : (listener.getClass().getName() + "]"));
    }

    public static <T extends EventListener> T create(Class<T> lType, Class<? super T> apiType, T l, Object source) {
        ProxyListener pl = new ProxyListener(lType, apiType, l);
        pl.setSource(source);

        return lType.cast(pl.proxy);
    }

    /** Weak property change listener
    */
    static class PropertyChange extends WeakListenerImpl implements PropertyChangeListener {
        /** Constructor.
        * @param l listener to delegate to
        */
        PropertyChange(PropertyChangeListener l) {
            super(PropertyChangeListener.class, l);
        }

        /** Constructor.
        * @param clazz required class
        * @param l listener to delegate to
        */
        PropertyChange(Class<?> clazz, PropertyChangeListener l) {
            super(clazz, l);
        }
        
        /** Constructor.
        * @param l listener to delegate to
        * @param propertyName the associated property name
        */
        PropertyChange(PropertyChangeListener l, String propertyName) {
            super(PropertyChangeListener.class, l, propertyName);
        }

        /** Tests if the object we reference to still exists and
        * if so, delegate to it. Otherwise remove from the source
        * if it has removePropertyChangeListener method.
        */
        @Override public void propertyChange(PropertyChangeEvent ev) {
            PropertyChangeListener l = (PropertyChangeListener) super.get(ev);

            if (l != null) {
                l.propertyChange(ev);
            }
        }

        /** Method name to use for removing the listener.
        * @return name of method of the source object that should be used
        *   to remove the listener from listening on source of events
        */
        @Override protected String removeMethodName() {
            return "removePropertyChangeListener"; // NOI18N
        }
    }

    /** Weak vetoable change listener
    */
    static class VetoableChange extends WeakListenerImpl implements VetoableChangeListener {
        /** Constructor.
        * @param l listener to delegate to
        */
        VetoableChange(VetoableChangeListener l) {
            super(VetoableChangeListener.class, l);
        }

        /** Constructor.
        * @param l listener to delegate to
        * @param propertyName the associated property name
        */
        VetoableChange(VetoableChangeListener l, String propertyName) {
            super(VetoableChangeListener.class, l, propertyName);
        }

        /** Tests if the object we reference to still exists and
        * if so, delegate to it. Otherwise remove from the source
        * if it has removePropertyChangeListener method.
        */
        @Override public void vetoableChange(PropertyChangeEvent ev)
        throws PropertyVetoException {
            VetoableChangeListener l = (VetoableChangeListener) super.get(ev);

            if (l != null) {
                l.vetoableChange(ev);
            }
        }

        /** Method name to use for removing the listener.
        * @return name of method of the source object that should be used
        *   to remove the listener from listening on source of events
        */
        @Override protected String removeMethodName() {
            return "removeVetoableChangeListener"; // NOI18N
        }
    }

    /** Weak document modifications listener.
    * This class if final only for performance reasons,
    * can be happily unfinaled if desired.
    */
    static final class Document extends WeakListenerImpl implements DocumentListener {
        /** Constructor.
        * @param l listener to delegate to
        */
        Document(final DocumentListener l) {
            super(DocumentListener.class, l);
        }

        /** Gives notification that an attribute or set of attributes changed.
        * @param ev event describing the action
        */
        @Override public void changedUpdate(DocumentEvent ev) {
            final DocumentListener l = docGet(ev);

            if (l != null) {
                l.changedUpdate(ev);
            }
        }

        /** Gives notification that there was an insert into the document.
        * @param ev event describing the action
        */
        @Override public void insertUpdate(DocumentEvent ev) {
            final DocumentListener l = docGet(ev);

            if (l != null) {
                l.insertUpdate(ev);
            }
        }

        /** Gives notification that a portion of the document has been removed.
        * @param ev event describing the action
        */
        @Override public void removeUpdate(DocumentEvent ev) {
            final DocumentListener l = docGet(ev);

            if (l != null) {
                l.removeUpdate(ev);
            }
        }

        /** Method name to use for removing the listener.
        * @return name of method of the source object that should be used
        *   to remove the listener from listening on source of events
        */
        @Override protected String removeMethodName() {
            return "removeDocumentListener"; // NOI18N
        }

        /** Getter for the target listener.
        * @param ev the event the we want to distribute
        * @return null if there is no listener because it has been finalized
        */
        private DocumentListener docGet(DocumentEvent ev) {
            DocumentListener l = (DocumentListener) super.ref.get();

            if (l == null) {
                super.ref.requestCleanUp(ev.getDocument());
            }

            return l;
        }
    }
     // end of Document inner class

    /** Weak swing change listener.
    * This class if final only for performance reasons,
    * can be happily unfinaled if desired.
    */
    static final class Change extends WeakListenerImpl implements ChangeListener {
        /** Constructor.
        * @param l listener to delegate to
        */
        Change(ChangeListener l) {
            super(ChangeListener.class, l);
        }

        /** Called when new file system is added to the pool.
        * @param ev event describing the action
        */
        @Override public void stateChanged(final ChangeEvent ev) {
            ChangeListener l = (ChangeListener) super.get(ev);

            if (l != null) {
                l.stateChanged(ev);
            }
        }

        /** Method name to use for removing the listener.
        * @return name of method of the source object that should be used
        *   to remove the listener from listening on source of events
        */
        @Override protected String removeMethodName() {
            return "removeChangeListener"; // NOI18N
        }
    }

    /** Weak version of focus listener.
    * This class if final only for performance reasons,
    * can be happily unfinaled if desired.
    */
    static final class Focus extends WeakListenerImpl implements FocusListener {
        /** Constructor.
        * @param l listener to delegate to
        */
        Focus(FocusListener l) {
            super(FocusListener.class, l);
        }

        /** Delegates to the original listener.
        */
        @Override public void focusGained(FocusEvent ev) {
            FocusListener l = (FocusListener) super.get(ev);

            if (l != null) {
                l.focusGained(ev);
            }
        }

        /** Delegates to the original listener.
        */
        @Override public void focusLost(FocusEvent ev) {
            FocusListener l = (FocusListener) super.get(ev);

            if (l != null) {
                l.focusLost(ev);
            }
        }

        /** Method name to use for removing the listener.
        * @return name of method of the source object that should be used
        *   to remove the listener from listening on source of events
        */
        @Override protected String removeMethodName() {
            return "removeFocusListener"; // NOI18N
        }
    }

    /** Proxy interface that delegates to listeners.
    */
    private static class ProxyListener extends WeakListenerImpl implements InvocationHandler {
        /** Equals method */
        private static Method equalsMth;

        /** Class -> Reference(Constructor) */
        private static final Map<Class<?>,Reference<Constructor<?>>> constructors = Collections.synchronizedMap(
            new WeakHashMap<Class<?>,Reference<Constructor<?>>>()
        );

        /** proxy generated for this listener */
        public final Object proxy;

        /** @param listener listener to delegate to
        */
        ProxyListener(Class<?> c, Class<?> api, EventListener listener) {
            super(api, listener);

            try {
                Reference<Constructor<?>> ref = constructors.get(c);
                Constructor<?> proxyConstructor = ref == null ? null : ref.get();

                if (proxyConstructor == null) {
                    Class<?> proxyClass = Proxy.getProxyClass(c.getClassLoader(), c);
                    proxyConstructor = proxyClass.getConstructor(InvocationHandler.class);
                    proxyConstructor.setAccessible(true);
                    constructors.put(c, new SoftReference<Constructor<?>>(proxyConstructor));
                }

                Object p;

                try {
                    p = proxyConstructor.newInstance(this);
                } catch (NoClassDefFoundError err) {
                    // if for some reason the actual creation of the instance
                    // from constructor fails, try it once more using regular
                    // method, see issue 30449
                    p = Proxy.newProxyInstance(c.getClassLoader(), new Class<?>[] { c }, this);
                }

                proxy = p;
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }

        private static Method getEquals() {
            if (equalsMth == null) {
                try {
                    equalsMth = Object.class.getMethod("equals", Object.class); // NOI18N
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            return equalsMth;
        }

        @Override public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
            try {
            if (method.getDeclaringClass() == Object.class) {
                // a method from object => call it on your self
                if (method == getEquals()) {
                    return equals(args[0]);
                }

                return method.invoke(this, args);
            }

            // listeners method
            EventObject ev = ((args != null) && (args[0] instanceof EventObject)) ? (EventObject) args[0] : null;

            Object listener = super.get(ev);

            if (listener != null) {
                return method.invoke(listener, args);
            } else {
                return null;
            }
            } catch (InvocationTargetException x) {
                throw x.getCause();
            }
        }

        /** Remove method name is composed from the name of the listener.
        */
        @Override protected String removeMethodName() {
            String name = listenerClass.getName();

            // strip package name
            int dot = name.lastIndexOf('.');
            name = name.substring(dot + 1);

            // in case of inner interfaces/classes we also strip the outer
            // class' name
            int i = name.lastIndexOf('$'); // NOI18N

            if (i >= 0) {
                name = name.substring(i + 1);
            }

            return "remove".concat(name); // NOI18N
        }

        /** To string prints class.
        */
        @Override
        public String toString() {
            return super.toString() + "[" + listenerClass + "]"; // NOI18N
        }

        /** Equal is extended to equal also with proxy object.
        */
        @Override
        public boolean equals(Object obj) {
            return (proxy == obj) || (this == obj);
        }

        @Override
        Object getImplementator() {
            return proxy;
        }
    }

    /** Reference that also holds ref to WeakListenerImpl.
    */
    private static final class ListenerReference extends WeakReference<Object> implements Runnable {
        private static Class<?> lastClass;
        private static Class<?> lastNClass;
        private static String lastMethodName;
        private static String lastNMethodName;
        private static Method lastRemove;
        private static Method lastNRemove;
        private static final Object LOCK = new Object();
        WeakListenerImpl weakListener;
        private String name;

        ListenerReference(Object ref, String name, WeakListenerImpl weakListener) {
            super(ref, BaseUtilities.activeReferenceQueue());
            this.weakListener = weakListener;
            this.name = name;
        }

        /** Requestes cleanup of the listener with a provided source.
         * @param source source of the cleanup
         */
        public synchronized void requestCleanUp(Object source) {
            if (weakListener == null) {
                // already being handled
                return;
            }

            if (weakListener.getSource() != source) {
                // plan new cleanup into the activeReferenceQueue with this listener and 
                // provided source
                weakListener.source = new WeakReference<Object> (source) {
                    ListenerReference doNotGCRef = new ListenerReference(new Object(), name, weakListener);
                };
            }
        }

        @Override public void run() {
            Object src = null; // On whom we're listening
            Method remove = null;

            WeakListenerImpl ref;

            synchronized (this) {
                ref = weakListener;

                if ((ref.source == null) || ((src = ref.source.get()) == null)) {
                    return;
                }

                // we are going to clean up the listener
                weakListener = null;
            }

            Class<?> methodClass;
            if (src instanceof Class) {
                // Handle static listener methods sanely.
                methodClass = (Class) src;
            } else {
                methodClass = src.getClass();
            }
            String methodName = ref.removeMethodName();

            synchronized (LOCK) {
                if (name == null) {
                    if (lastClass == methodClass) {
                        if (lastRemove != null && methodName.equals(lastMethodName)) {
                            remove = lastRemove;
                        }
                    }
                } else {
                    if (lastNClass == methodClass) {
                        if (lastNRemove != null && methodName.equals(lastNMethodName)) {
                            remove = lastNRemove;
                        }
                    }
                }
            }

            // get the remove method or use the last one
            if (remove == null) {
                if (name == null) {
                    remove = getRemoveMethod(methodClass, methodName, ref.listenerClass);
                }
                if (remove == null) {
                    remove = getRemoveMethod(methodClass, methodName, String.class, ref.listenerClass);
                }

                if (remove == null) {
                    LOG.log(Level.WARNING, "Can''t remove {0} using method {1}.{2} from {3}", new Object[] {ref.listenerClass.getName(), methodClass.getName(), methodName, src});
                    return;
                } else {
                    synchronized (LOCK) {
                        if (name == null) {
                            lastClass = methodClass;
                            lastMethodName = methodName;
                            lastRemove = remove;
                        } else {
                            lastNClass = methodClass;
                            lastNMethodName = methodName;
                            lastNRemove = remove;
                        }
                    }
                }
            }
            
            try {
                if (remove.getParameterTypes().length == 1) {
                    remove.invoke(src, new Object[]{ref.getImplementator()});
                } else {
                    String nameParam = (name == null) ? "" : name;
                    remove.invoke(src, new Object[]{nameParam, ref.getImplementator()});
                }
            } catch (Exception ex) { // from invoke(), should not happen
                // #151415 - ignore exception from AbstractPreferences if node has been removed
                if (!"removePreferenceChangeListener".equals(methodName) && !"removeNodeChangeListener".equals(methodName)) {  //NOI18N
                    String errMessage = "Problem encountered while calling " + methodClass + "." + methodName + "(" + remove + ") on " + src + "\n" + ref.getImplementator(); // NOI18N
                    LOG.warning( errMessage );
                    //detailed logging needed in some cases
                    boolean showErrMessage = ex instanceof InvocationTargetException
                            || "object is not an instance of declaring class".equals(ex.getMessage());

                    LOG.log(Level.WARNING, showErrMessage ? errMessage : null, ex);
                }
            }
        }

        /* can return null */
        private Method getRemoveMethod(Class<?> methodClass, String methodName, Class<?>... clarray) {
            Method m = null;

            try {
                m = methodClass.getMethod(methodName, clarray);
            } catch (NoSuchMethodException e) {
                do {
                    try {
                        m = methodClass.getDeclaredMethod(methodName, clarray);
                    } catch (NoSuchMethodException ex) {
                    }

                    methodClass = methodClass.getSuperclass();
                } while ((m == null) && (methodClass != Object.class));
            } catch (RuntimeException x) {
                LOG.log(Level.WARNING, "called get[Declared]Method on " + methodClass.getName(), x);
            } catch (LinkageError e) {
                LOG.log(Level.WARNING, null, e);
            }

            if (
                (m != null) &&
                    (!Modifier.isPublic(m.getModifiers()) || !Modifier.isPublic(m.getDeclaringClass().getModifiers()))
            ) {
                m.setAccessible(true);
            }

            return m;
        }
    }
}
