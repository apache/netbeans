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

import java.util.logging.Logger;
import org.openide.filesystems.*;
import org.openide.nodes.*;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import java.beans.*;

import java.lang.ref.*;
import java.lang.reflect.*;

import java.util.EventListener;
import java.util.EventObject;
import java.util.logging.Level;

import javax.swing.event.*;


/**
 * @deprecated Use {@link org.openide.util.WeakListeners} class.
 * @author Jaroslav Tulach
 */
@Deprecated
public abstract class WeakListener implements java.util.EventListener {
    /** weak reference to listener */
    private Reference ref;

    /** class of the listener */
    Class listenerClass;

    /** weak reference to source */
    private Reference source;

    /**
     * @param listenerClass class/interface of the listener
     * @param l listener to delegate to, <code>l</code> must be an instance of
     * listenerClass
     */
    protected WeakListener(Class listenerClass, java.util.EventListener l) {
        this.listenerClass = listenerClass;
        ref = new ListenerReference(l, this);

        if (!listenerClass.isAssignableFrom(l.getClass())) {
            throw new IllegalArgumentException(
                getClass().getName() + " constructor is calling WeakListner.<init> with illegal arguments"
            ); // NOI18N
        }
    }

    /** Setter for the source field. If a WeakReference to an underlying listener is
     * cleared and enqueued, that is, the original listener is garbage collected,
     * then the source field is used for deregistration of this WeakListener, thus making
     * it eligible for garbage collection if no more references exist.
     *
     * This method is particularly useful in cases where the underlying listener was
     * garbage collected and the event source, on which this listener is listening on,
     * is quiet, i.e. does not fire any events for long periods. In this case, this listener
     * is not removed from the event source until an event is fired. If the source field is
     * set however, org.openide.util.WeakListeners that lost their underlying listeners are removed
     * as soon as the ReferenceQueue notifies the WeakListener.
     *
     * @param source is any Object or <code>null</code>, though only setting an object
     * that has an appropriate remove*listenerClass*Listener method and on which this listener is listening on,
     * is useful.
     */
    protected final void setSource(Object source) {
        if (source == null) {
            this.source = null;
        } else {
            this.source = new WeakReference(source);
        }
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
            removeListener((ev == null) ? null : ev.getSource());
        }

        return (EventListener) l;
    }

    /** Tries to find a remove method and invoke it.
     * It tries unregister itself from the registered source first
     * and then from the passed eventSource if they are not same.
     *
     * @param eventSource the source object to unregister from or null
     */
    private void removeListener(Object eventSource) {
        Object[] params = new Object[] { getImplementator() };
        Object src = (source == null) ? null : source.get();

        try {
            Method m = null;

            if (src != null) {
                m = getRemoveMethod(src.getClass(), removeMethodName(), listenerClass);

                if (m != null) {
                    m.invoke(src, params);
                }
            }

            if ((eventSource != src) && (eventSource != null)) {
                m = getRemoveMethod(eventSource.getClass(), removeMethodName(), listenerClass);

                if (m != null) {
                    m.invoke(eventSource, params);
                }
            }

            if ((m == null) && (source == null)) { // can't remove the listener
                Logger.getAnonymousLogger().warning(
                    "Can't remove " + listenerClass.getName() + //NOI18N
                    " using method " + removeMethodName() + //NOI18N
                    " source=" + source + //NOI18N
                    ", src=" + src + ", eventSource=" + eventSource
                ); //NOI18N
            }
        } catch (Exception ex) { // from invoke(), should not happen
            Logger.getLogger(WeakListener.class.getName()).log(Level.WARNING, null, ex);
        }
    }

    /* can return null */
    private static final Method getRemoveMethod(Class methodClass, String methodName, Class listenerClass) {
        final Class[] clarray = new Class[] { listenerClass };
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
        }

        if (
            (m != null) &&
                (!Modifier.isPublic(m.getModifiers()) || !Modifier.isPublic(m.getDeclaringClass().getModifiers()))
        ) {
            m.setAccessible(true);
        }

        return m;
    }

    Object getImplementator() {
        return this;
    }

    public String toString() {
        Object listener = ref.get();

        return getClass().getName() + "[" + ((listener == null) ? "null" : (listener.getClass().getName() + "]"));
    }

    //
    // Methods for establishing connections
    //

    /** Creates a weak implementation of NodeListener.
     *
     * @param l the listener to delegate to
     * @param source the source that the listener should detach from when
     *     listener <CODE>l</CODE> is freed, can be <CODE>null</CODE>
     * @return a NodeListener delegating to <CODE>l</CODE>.
     * @deprecated Use {@link org.openide.nodes.NodeOp#weakNodeListener} or {@link org.openide.util.WeakListeners#create}
     */
    @Deprecated
    public static NodeListener node(NodeListener l, Object source) {
        WeakListener.Node wl = new WeakListener.Node(l);
        wl.setSource(source);

        return wl;
    }

    /** Creates a weak implementation of PropertyChangeListener.
     *
     * @param l the listener to delegate to
     * @param source the source that the listener should detach from when
     *     listener <CODE>l</CODE> is freed, can be <CODE>null</CODE>
     * @return a PropertyChangeListener delegating to <CODE>l</CODE>.
     * @deprecated Use {@link org.openide.util.WeakListeners#propertyChange}
     */
    @Deprecated
    public static PropertyChangeListener propertyChange(PropertyChangeListener l, Object source) {
        WeakListener.PropertyChange wl = new WeakListener.PropertyChange(l);
        wl.setSource(source);

        return wl;
    }

    /** Creates a weak implementation of VetoableChangeListener.
     *
     * @param l the listener to delegate to
     * @param source the source that the listener should detach from when
     *     listener <CODE>l</CODE> is freed, can be <CODE>null</CODE>
     * @return a VetoableChangeListener delegating to <CODE>l</CODE>.
     * @deprecated Use {@link org.openide.util.WeakListeners#vetoableChange}
     */
    @Deprecated
    public static VetoableChangeListener vetoableChange(VetoableChangeListener l, Object source) {
        WeakListener.VetoableChange wl = new WeakListener.VetoableChange(l);
        wl.setSource(source);

        return wl;
    }

    /** Creates a weak implementation of FileChangeListener.
     *
     * @param l the listener to delegate to
     * @param source the source that the listener should detach from when
     *     listener <CODE>l</CODE> is freed, can be <CODE>null</CODE>
     * @return a FileChangeListener delegating to <CODE>l</CODE>.
     * @deprecated Use {@link FileUtil#weakFileChangeListener} or {@link org.openide.util.WeakListeners#create}
     */
    @Deprecated
    public static FileChangeListener fileChange(FileChangeListener l, Object source) {
        WeakListener.FileChange wl = new WeakListener.FileChange(l);
        wl.setSource(source);

        return wl;
    }

    /** Creates a weak implementation of FileStatusListener.
     *
     * @param l the listener to delegate to
     * @param source the source that the listener should detach from when
     *     listener <CODE>l</CODE> is freed, can be <CODE>null</CODE>
     * @return a FileStatusListener delegating to <CODE>l</CODE>.
     * @deprecated Use {@link FileUtil#weakFileStatusListener} or {@link org.openide.util.WeakListeners#create}
     */
    @Deprecated
    public static FileStatusListener fileStatus(FileStatusListener l, Object source) {
        WeakListener.FileStatus wl = new WeakListener.FileStatus(l);
        wl.setSource(source);

        return wl;
    }

    /** Creates a weak implementation of RepositoryListener.
     *
     * @param l the listener to delegate to
     * @param source the source that the listener should detach from when
     *     listener <CODE>l</CODE> is freed, can be <CODE>null</CODE>
     * @return a RepositoryListener delegating to <CODE>l</CODE>.
     */
    public static RepositoryListener repository(RepositoryListener l, Object source) {
        WeakListener.Repository wl = new WeakListener.Repository(l);
        wl.setSource(source);

        return wl;
    }

    /** Creates a weak implementation of DocumentListener.
     *
     * @param l the listener to delegate to
     * @param source the source that the listener should detach from when
     *     listener <CODE>l</CODE> is freed, can be <CODE>null</CODE>
     * @return a DocumentListener delegating to <CODE>l</CODE>.
     * @deprecated Use {@link org.openide.util.WeakListeners#document}
     */
    @Deprecated
    public static DocumentListener document(DocumentListener l, Object source) {
        WeakListener.Document wl = new WeakListener.Document(l);
        wl.setSource(source);

        return wl;
    }

    /** Creates a weak implementation of ChangeListener.
     *
     * @param l the listener to delegate to
     * @param source the source that the listener should detach from when
     *     listener <CODE>l</CODE> is freed, can be <CODE>null</CODE>
     * @return a ChangeListener delegating to <CODE>l</CODE>.
     * @deprecated Use {@link org.openide.util.WeakListeners#change}
     */
    @Deprecated
    public static ChangeListener change(ChangeListener l, Object source) {
        WeakListener.Change wl = new WeakListener.Change(l);
        wl.setSource(source);

        return wl;
    }

    /** Creates a weak implementation of FocusListener.
     *
     * @param l the listener to delegate to
     * @param source the source that the listener should detach from when
     *     listener <CODE>l</CODE> is freed, can be <CODE>null</CODE>
     * @return a FocusListener delegating to <CODE>l</CODE>.
     * @deprecated Use {@link org.openide.util.WeakListeners#create}
     */
    @Deprecated
    public static FocusListener focus(FocusListener l, Object source) {
        WeakListener.Focus wl = new WeakListener.Focus(l);
        wl.setSource(source);

        return wl;
    }

    /** A generic WeakListener factory.
     * Creates a weak implementation of a listener of type <CODE>lType</CODE>.
     *
     * @param lType the type of listener to create. It can be any interface,
     *     but only interfaces are allowed.
     * @param l the listener to delegate to, <CODE>l</CODE> must be an instance
     *     of <CODE>lType</CODE>
     * @param source the source that the listener should detach from when
     *     listener <CODE>l</CODE> is freed, can be <CODE>null</CODE>
     * @return an instance of <CODE>lType</CODE> delegating all the interface
     * calls to <CODE>l</CODE>.
     * @deprecated Use {@link org.openide.util.WeakListeners#create}
     */
    @Deprecated
    public static EventListener create(Class lType, EventListener l, Object source) {
        ProxyListener pl = new ProxyListener(lType, l);
        pl.setSource(source);

        return (EventListener) pl.proxy;
    }

    /** Weak property change listener
    * @deprecated use appropriate method instead
    */
    @Deprecated
    public static class PropertyChange extends WeakListener implements PropertyChangeListener {
        /** Constructor.
        * @param l listener to delegate to
        */
        public PropertyChange(PropertyChangeListener l) {
            super(PropertyChangeListener.class, l);
        }

        /** Constructor.
        * @param clazz required class
        * @param l listener to delegate to
        */
        PropertyChange(Class clazz, PropertyChangeListener l) {
            super(clazz, l);
        }

        /** Tests if the object we reference to still exists and
        * if so, delegate to it. Otherwise remove from the source
        * if it has removePropertyChangeListener method.
        */
        public void propertyChange(PropertyChangeEvent ev) {
            PropertyChangeListener l = (PropertyChangeListener) super.get(ev);

            if (l != null) {
                l.propertyChange(ev);
            }
        }

        /** Method name to use for removing the listener.
        * @return name of method of the source object that should be used
        *   to remove the listener from listening on source of events
        */
        protected String removeMethodName() {
            return "removePropertyChangeListener"; // NOI18N
        }
    }

    /** Weak vetoable change listener
    * @deprecated use appropriate method instead
    */
    @Deprecated
    public static class VetoableChange extends WeakListener implements VetoableChangeListener {
        /** Constructor.
        * @param l listener to delegate to
        */
        public VetoableChange(VetoableChangeListener l) {
            super(VetoableChangeListener.class, l);
        }

        /** Tests if the object we reference to still exists and
        * if so, delegate to it. Otherwise remove from the source
        * if it has removePropertyChangeListener method.
        */
        public void vetoableChange(PropertyChangeEvent ev)
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
        protected String removeMethodName() {
            return "removeVetoableChangeListener"; // NOI18N
        }
    }

    /** Weak file change listener.
    * @deprecated use appropriate method instead
    */
    @Deprecated
    public static class FileChange extends WeakListener implements FileChangeListener {
        /** Constructor.
        * @param l listener to delegate to
        */
        public FileChange(FileChangeListener l) {
            super(FileChangeListener.class, l);
        }

        /** Fired when a new folder has been created. This action can only be
        * listened in folders containing the created file up to the root of
        * file system.
        *
        * @param ev the event describing context where action has taken place
        */
        public void fileFolderCreated(FileEvent ev) {
            FileChangeListener l = (FileChangeListener) super.get(ev);

            if (l != null) {
                l.fileFolderCreated(ev);
            }
        }

        /** Fired when a new file has been created. This action can only be
        * listened in folders containing the created file up to the root of
        * file system.
        *
        * @param ev the event describing context where action has taken place
        */
        public void fileDataCreated(FileEvent ev) {
            FileChangeListener l = (FileChangeListener) super.get(ev);

            if (l != null) {
                l.fileDataCreated(ev);
            }
        }

        /** Fired when a file has been changed.
        * @param ev the event describing context where action has taken place
        */
        public void fileChanged(FileEvent ev) {
            FileChangeListener l = (FileChangeListener) super.get(ev);

            if (l != null) {
                l.fileChanged(ev);
            }
        }

        /** Fired when a file has been deleted.
        * @param ev the event describing context where action has taken place
        */
        public void fileDeleted(FileEvent ev) {
            FileChangeListener l = (FileChangeListener) super.get(ev);

            if (l != null) {
                l.fileDeleted(ev);
            }
        }

        /** Fired when a file has been renamed.
        * @param ev the event describing context where action has taken place
        *           and the original name and extension.
        */
        public void fileRenamed(FileRenameEvent ev) {
            FileChangeListener l = (FileChangeListener) super.get(ev);

            if (l != null) {
                l.fileRenamed(ev);
            }
        }

        /** Fired when a file attribute has been changed.
        * @param ev the event describing context where action has taken place,
        *           the name of attribute and old and new value.
        */
        public void fileAttributeChanged(FileAttributeEvent ev) {
            FileChangeListener l = (FileChangeListener) super.get(ev);

            if (l != null) {
                l.fileAttributeChanged(ev);
            }
        }

        /** Method name to use for removing the listener.
        * @return name of method of the source object that should be used
        *   to remove the listener from listening on source of events
        */
        protected String removeMethodName() {
            return "removeFileChangeListener"; // NOI18N
        }
    }

    /** Weak file status listener.
    * @deprecated use appropriate method instead
    */
    @Deprecated
    public static class FileStatus extends WeakListener implements FileStatusListener {
        /** Constructor.
        */
        public FileStatus(FileStatusListener l) {
            super(FileStatusListener.class, l);
        }

        /** Notifies listener about change in annotataion of a few files.
         * @param ev event describing the change
         */
        public void annotationChanged(FileStatusEvent ev) {
            FileStatusListener l = (FileStatusListener) super.get(ev);

            if (l != null) {
                l.annotationChanged(ev);
            }
        }

        /** Method name to use for removing the listener.
        * @return name of method of the source object that should be used
        *   to remove the listener from listening on source of events
        */
        protected String removeMethodName() {
            return "removeFileStatusListener"; // NOI18N
        }
    }

    /** Weak file system pool listener.
    * @deprecated use appropriate method instead
    */
    @Deprecated
    public static class Repository extends WeakListener implements RepositoryListener {
        /** Constructor.
        * @param l listener to delegate to
        */
        public Repository(RepositoryListener l) {
            super(RepositoryListener.class, l);
        }

        /** Called when new file system is added to the pool.
        * @param ev event describing the action
        */
        public void fileSystemAdded(RepositoryEvent ev) {
            RepositoryListener l = (RepositoryListener) super.get(ev);

            if (l != null) {
                l.fileSystemAdded(ev);
            }
        }

        /** Called when a file system is deleted from the pool.
        * @param ev event describing the action
        */
        public void fileSystemRemoved(RepositoryEvent ev) {
            RepositoryListener l = (RepositoryListener) super.get(ev);

            if (l != null) {
                l.fileSystemRemoved(ev);
            }
        }

        /** Called when a Repository is reordered. */
        public void fileSystemPoolReordered(RepositoryReorderedEvent ev) {
            RepositoryListener l = (RepositoryListener) super.get(ev);

            if (l != null) {
                l.fileSystemPoolReordered(ev);
            }
        }

        /** Method name to use for removing the listener.
        * @return name of method of the source object that should be used
        *   to remove the listener from listening on source of events
        */
        protected String removeMethodName() {
            return "removeRepositoryListener"; // NOI18N
        }
    }

    /** Weak document modifications listener.
    * This class if final only for performance reasons,
    * can be happily unfinaled if desired.
    * @deprecated use appropriate method instead
    */
    @Deprecated
    public static final class Document extends WeakListener implements DocumentListener {
        /** Constructor.
        * @param l listener to delegate to
        */
        public Document(final DocumentListener l) {
            super(DocumentListener.class, l);
        }

        /** Gives notification that an attribute or set of attributes changed.
        * @param ev event describing the action
        */
        public void changedUpdate(DocumentEvent ev) {
            final DocumentListener l = docGet(ev);

            if (l != null) {
                l.changedUpdate(ev);
            }
        }

        /** Gives notification that there was an insert into the document.
        * @param ev event describing the action
        */
        public void insertUpdate(DocumentEvent ev) {
            final DocumentListener l = docGet(ev);

            if (l != null) {
                l.insertUpdate(ev);
            }
        }

        /** Gives notification that a portion of the document has been removed.
        * @param ev event describing the action
        */
        public void removeUpdate(DocumentEvent ev) {
            final DocumentListener l = docGet(ev);

            if (l != null) {
                l.removeUpdate(ev);
            }
        }

        /** Method name to use for removing the listener.
        * @return name of method of the source object that should be used
        *   to remove the listener from listening on source of events
        */
        protected String removeMethodName() {
            return "removeDocumentListener"; // NOI18N
        }

        /** Getter for the target listener.
        * @param event the event the we want to distribute
        * @return null if there is no listener because it has been finalized
        */
        private DocumentListener docGet(DocumentEvent ev) {
            DocumentListener l = (DocumentListener) super.ref.get();

            if (l == null) {
                super.removeListener(ev.getDocument());
            }

            return l;
        }
    }
     // end of Document inner class

    /** Weak swing change listener.
    * This class if final only for performance reasons,
    * can be happily unfinaled if desired.
    * @deprecated use appropriate method instead
    */
    @Deprecated
    public static final class Change extends WeakListener implements ChangeListener {
        /** Constructor.
        * @param l listener to delegate to
        */
        public Change(ChangeListener l) {
            super(ChangeListener.class, l);
        }

        /** Called when new file system is added to the pool.
        * @param ev event describing the action
        */
        public void stateChanged(final ChangeEvent ev) {
            ChangeListener l = (ChangeListener) super.get(ev);

            if (l != null) {
                l.stateChanged(ev);
            }
        }

        /** Method name to use for removing the listener.
        * @return name of method of the source object that should be used
        *   to remove the listener from listening on source of events
        */
        protected String removeMethodName() {
            return "removeChangeListener"; // NOI18N
        }
    }

    /** Weak version of listener for changes in one node.
    * This class if final only for performance reasons,
    * can be happily unfinaled if desired.
    * @deprecated use appropriate method instead
    */
    @Deprecated
    public static final class Node extends WeakListener.PropertyChange implements NodeListener {
        /** Constructor.
        * @param l listener to delegate to
        */
        public Node(NodeListener l) {
            super(NodeListener.class, l);
        }

        /** Delegates to the original listener.
        */
        public void childrenAdded(NodeMemberEvent ev) {
            NodeListener l = (NodeListener) super.get(ev);

            if (l != null) {
                l.childrenAdded(ev);
            }
        }

        /** Delegates to the original listener.
        */
        public void childrenRemoved(NodeMemberEvent ev) {
            NodeListener l = (NodeListener) super.get(ev);

            if (l != null) {
                l.childrenRemoved(ev);
            }
        }

        /** Delegates to the original listener.
        */
        public void childrenReordered(NodeReorderEvent ev) {
            NodeListener l = (NodeListener) super.get(ev);

            if (l != null) {
                l.childrenReordered(ev);
            }
        }

        /** Delegates to the original listener.
        */
        public void nodeDestroyed(NodeEvent ev) {
            NodeListener l = (NodeListener) super.get(ev);

            if (l != null) {
                l.nodeDestroyed(ev);
            }
        }

        /** Method name to use for removing the listener.
        * @return name of method of the source object that should be used
        *   to remove the listener from listening on source of events
        */
        protected String removeMethodName() {
            return "removeNodeListener"; // NOI18N
        }
    }

    /** Weak version of focus listener.
    * This class if final only for performance reasons,
    * can be happily unfinaled if desired.
    * @deprecated use appropriate method instead
    */
    @Deprecated
    public static final class Focus extends WeakListener implements FocusListener {
        /** Constructor.
        * @param l listener to delegate to
        */
        public Focus(FocusListener l) {
            super(FocusListener.class, l);
        }

        /** Delegates to the original listener.
        */
        public void focusGained(FocusEvent ev) {
            FocusListener l = (FocusListener) super.get(ev);

            if (l != null) {
                l.focusGained(ev);
            }
        }

        /** Delegates to the original listener.
        */
        public void focusLost(FocusEvent ev) {
            FocusListener l = (FocusListener) super.get(ev);

            if (l != null) {
                l.focusLost(ev);
            }
        }

        /** Method name to use for removing the listener.
        * @return name of method of the source object that should be used
        *   to remove the listener from listening on source of events
        */
        protected String removeMethodName() {
            return "removeFocusListener"; // NOI18N
        }
    }

    /** Proxy interface that delegates to listeners.
    */
    private static class ProxyListener extends WeakListener implements InvocationHandler {
        /** Equals method */
        private static Method equalsMth;

        /** proxy generated for this listener */
        public final Object proxy;

        /** @param listener listener to delegate to
        */
        public ProxyListener(Class c, java.util.EventListener listener) {
            super(c, listener);

            proxy = Proxy.newProxyInstance(c.getClassLoader(), new Class[] { c }, this);
        }

        /** */
        private static Method getEquals() {
            if (equalsMth == null) {
                try {
                    equalsMth = Object.class.getMethod("equals", new Class[] { Object.class }); // NOI18N
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            return equalsMth;
        }

        public java.lang.Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                // a method from object => call it on your self
                if (method == getEquals()) {
                    boolean ret = equals(args[0]);

                    return (ret ? Boolean.TRUE : Boolean.FALSE);
                }

                return method.invoke(this, args);
            }

            // listeners method
            EventObject ev = (args[0] instanceof EventObject) ? (EventObject) args[0] : null;

            Object listener = super.get(ev);

            if (listener != null) {
                return method.invoke(listener, args);
            } else {
                return null;
            }
        }

        /** Remove method name is composed from the name of the listener.
        */
        protected String removeMethodName() {
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
        public String toString() {
            return super.toString() + "[" + listenerClass + "]"; // NOI18N
        }

        /** Equal is extended to equal also with proxy object.
        */
        public boolean equals(Object obj) {
            return (proxy == obj) || (this == obj);
        }

        Object getImplementator() {
            return proxy;
        }
    }

    /** Reference that also holds ref to WeakListener.
    */
    private static final class ListenerReference extends WeakReference implements Runnable {
        private static Class lastClass;
        private static String lastMethodName;
        private static Method lastRemove;
        private static Object LOCK = new Object();
        final WeakListener weakListener;

        public ListenerReference(Object ref, WeakListener weakListener) {
            super(ref, Utilities.activeReferenceQueue());
            this.weakListener = weakListener;
        }

        public void run() {
            ListenerReference lr = this;

            // prepare array for passing arguments to getMethod/invoke
            Object[] params = new Object[1];
            Class[] types = new Class[1];
            Object src = null; // On whom we're listening
            Method remove = null;

            WeakListener ref = lr.weakListener;

            if ((ref.source == null) || ((src = ref.source.get()) == null)) {
                return;
            }

            Class methodClass = src.getClass();
            String methodName = ref.removeMethodName();

            synchronized (LOCK) {
                if ((lastClass == methodClass) && (lastMethodName == methodName) && (lastRemove != null)) {
                    remove = lastRemove;
                }
            }

            // get the remove method or use the last one
            if (remove == null) {
                types[0] = ref.listenerClass;
                remove = getRemoveMethod(methodClass, methodName, types[0]);

                if (remove == null) {
                    Logger.getAnonymousLogger().warning(
                        "Can't remove " + ref.listenerClass.getName() + //NOI18N
                        " using method " + methodName + //NOI18N
                        " from " + src
                    ); //NOI18N

                    return;
                } else {
                    synchronized (LOCK) {
                        lastClass = methodClass;
                        lastMethodName = methodName;
                        lastRemove = remove;
                    }
                }
            }

            params[0] = ref.getImplementator(); // Whom to unregister

            try {
                remove.invoke(src, params);
            } catch (Exception ex) { // from invoke(), should not happen
                Exceptions.attachLocalizedMessage(ex,
                                                  "Problem encountered while calling " +
                                                  methodClass + "." + methodName +
                                                  "(...) on " + src); // NOI18N
                Logger.getAnonymousLogger().log(Level.WARNING, null, ex);
            }
        }
    }
}
