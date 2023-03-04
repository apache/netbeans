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

package org.openide.util;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.EventListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;

/** A generic weak listener factory.
 * Creates a weak implementation of a listener of type <CODE>lType</CODE>.
 *
 * In the following examples, I'll use following naming:<BR>
 * There are four objects involved in weak listener usage:<UL>
 *  <LI>The event <em>source</em> object
 *  <LI>The <em>observer</em> - object that wants to listen on <em>source</em>
 *  <LI>The <em>listener</em> - the implementation of the corresponding
 *     <code>*Listener</code> interface, sometimes the observer itself but
 *     often some observer's inner class delegating the events to the observer.
 *  <LI>The <em>weak listener</em> implementation.
 * </UL>
 * The examples are written for ChangeListener. The <code>Utilities</code>
 * have factory methods for the most common listeners used in NetBeans
 * and also one universal factory method you can use for other listeners.
 * <p>
 * <strong>How to use it:</strong>
 * <p>
 * Here is an example how to write a listener/observer and make it listen
 * on some source:
 * <pre>
 *  public class ListenerObserver implements ChangeListener {
 *      private void registerTo(Source source) {
 *          source.addChangeListener({@link
                #change(javax.swing.event.ChangeListener, java.lang.Object)
 *              WeakListeners.change} (this, source));
 *      }
 *
 *      public void stateChanged(ChangeEvent e) {
 *          doSomething();
 *      }
 *  }
 * </pre>
 * You can also factor out the listener implementation to some other class
 * if you don't want to expose the stateChanged method (better technique):
 * <pre>
 *  public class Observer {
 *      <b>private Listener listener;</b>
 *
 *      private void registerTo(Source source) {
 *          <b>listener = new Listener()</b>;
 *          source.addChangeListener({@link
                #change(javax.swing.event.ChangeListener, java.lang.Object)
 *              WeakListeners.change} (listener, source));
 *      }
 *
 *      private class Listener implements ChangeListener {
 *          public void stateChanged(ChangeEvent e) {
 *              doSomething();
 *          }
 *      }
 *  }
 * </pre>
 * Note: The observer keeps the reference to the listener, it won't work
 * otherwise, see below.
 *
 * <P>You can also use the universal factory for other listeners:
 * <pre>
 *  public class Observer implements SomeListener {
 *      private void registerTo(Source source) {
 *          source.addSomeListener((SomeListener){@link
 *              #create(java.lang.Class, java.util.EventListener, java.lang.Object)
 *              WeakListeners.create} (
 *                  SomeListener.class, this, source));
 *      }
 *
 *      public void someEventHappened(SomeEvent e) {
 *          doSomething();
 *      }
 *  }
 * </pre>
 * <p>
 * <strong>How to <span style="color:red">not</span> use it:</strong>
 * <p>
 * Here are examples of a common mistakes done when using <em>weak listener</em>:
 * <pre>
 *  public class Observer {
 *      private void registerTo(Source source) {
 *          source.addChangeListener(WeakListeners.change(<b>new Listener()</b>, source));
 *      }
 *
 *      private class Listener implements ChangeListener {
 *          public void stateChanged(ChangeEvent e) {
 *              doSomething();
 *          }
 *      }
 *  }
 * </pre>
 * Mistake: There is nobody holding strong reference to the Listener instance,
 * so it may be freed on the next GC cycle.
 *
 * <BR><pre>
 *  public class ListenerObserver implements ChangeListener {
 *      private void registerTo(Source source) {
 *          source.addChangeListener(WeakListeners.change(this, <b>null</b>));
 *      }
 *
 *      public void stateChanged(ChangeEvent e) {
 *          doSomething();
 *      }
 *  }
 * </pre>
 * Mistake: The weak listener is unable to unregister itself from the source
 * once the listener is freed. For explanation, read below.
 * <p>
 * <strong>How does it work:</strong>
 * <P>The <em>weak listener</em> is used as a reference-weakening wrapper
 *  around the listener. It is itself strongly referenced from the implementation
 *  of the source (e.g. from its <code>EventListenerList</code>) but it references
 *  the listener only through <code>WeakReference</code>. It also weak-references
 *  the source. Listener, on the other hand, usually strongly references
 *  the observer (typically through the outer class reference).
 *
 * This means that: <OL>
 * <LI>If the listener is not strong-referenced from elsewhere, it can be
 *  thrown away on the next GC cycle. This is why you can't use
 *  <code>WeakListeners.change(new MyListener(), ..)</code> as the only reference
 *  to the listener will be the weak one from the weak listener.
 * <LI>If the listener-observer pair is not strong-referenced from elsewhere
 *  it can be thrown away on the next GC cycle. This is what the
 *  <em>weak listener</em> was invented for.
 * <LI>If the source is not strong-referenced from anywhere, it can be
 *  thrown away on the next GC cycle taking the weak listener with it,
 *  but not the listener and the observer if they are still strong-referenced
 *  (unusual case, but possible).
 * </OL>
 *
 * <P>Now what happens when the listener/observer is removed from memory:<UL>
 * <LI>The weak listener is notified that the reference to the listener was cleared.
 * <LI>It tries to unregister itself from the source. This is why it needs
 *  the reference to the source for the registration. The unregistration
 *  is done using reflection, usually looking up the method
 *  <code>remove&lt;listenerType&gt;</code> of the source and calling it.
 *  </UL>
 *
 *  <P>This may fail if the source don't have the expected <code>remove*</code>
 *  method and/or if you provide wrong reference to source. In that case
 *  the weak listener instance will stay in memory and registered by the source,
 *  while the listener and observer will be freed.
 *
 *  <P>There is still one fallback method - if some event come to a weak listener
 *  and the listener is already freed, the weak listener tries to unregister
 *  itself from the object the event came from.
 *
 * @since 4.10
 */
public final class WeakListeners {
    /** No instances.
     */
    private WeakListeners() {
    }

    /** Generic factory method to create weak listener for any listener
     * interface.
     * @param <T> type of listener
     * @param lType the type of listener to create. It can be any interface,
     *     but only interfaces are allowed.
     * @param l the listener to delegate to, <CODE>l</CODE> must be an instance
     *     of <CODE>lType</CODE>
     * @param source the source that the listener should detach from when
     *     listener <CODE>l</CODE> is freed, can be <CODE>null</CODE>
     * @return an instance of <CODE>lType</CODE> delegating all the interface
     * calls to <CODE>l</CODE>.
     */
    public static <T extends EventListener> T create(Class<T> lType, T l, Object source) {
        if (!lType.isInterface()) {
            throw new IllegalArgumentException("Not interface: " + lType);
        }

        return WeakListenerImpl.create(lType, lType, l, source);
    }

    /** The most generic factory method to create weak listener for any listener
     * interface that moreover behaves like a listener of another type.
     * This can be useful to correctly remove listeners from a source when
     * hierarchies of listeners are used.
     * <P>
     * For example {@link javax.naming.event.EventContext} allows to add an
     * instance of {@link javax.naming.event.ObjectChangeListener} but using
     * method <code>addNamingListener</code>. Method <code>removeNamingListener</code>
     * is then used to remove it. To help the weak listener support to correctly
     * find the right method one have to use:
     * <PRE>
     * ObjectChangeListener l = (ObjectChangeListener)WeakListeners.create (
     *   ObjectChangeListener.class, // the actual class of the returned listener
     *   NamingListener.class, // but it always will be used as NamingListener
     *   yourObjectListener,
     *   someContext
     * );
     * someContext.addNamingListener ("", 0, l);
     * </PRE>
     * This will correctly create <code>ObjectChangeListener</code>
     * and unregister it by
     * calling <code>removeNamingListener</code>.
     * @param <T> type of listener
     * @param lType the type the listener shall implement. It can be any interface,
     *     but only interfaces are allowed.
     * @param apiType the interface the returned object will be used as. It
     *     shall be equal to <code>lType</code> or its superinterface
     * @param l the listener to delegate to, <CODE>l</CODE> must be an instance
     *     of <CODE>lType</CODE>
     * @param source the source that the listener should detach from when
     *     listener <CODE>l</CODE> is freed, can be <CODE>null</CODE>
     * @return an instance of <CODE>lType</CODE> delegating all the interface
     * calls to <CODE>l</CODE>.
     * @since 4.12
     */
    public static <T extends EventListener> T create(Class<T> lType, Class<? super T> apiType, T l, Object source) {
        if (!lType.isInterface()) {
            throw new IllegalArgumentException("Not interface: " + lType);
        }

        if (!apiType.isInterface()) {
            throw new IllegalArgumentException("Not interface: " + apiType);
        }

        if (!apiType.isAssignableFrom(lType)) {
            throw new IllegalArgumentException(apiType + " has to be assignableFrom " + lType); // NOI18N
        }

        return WeakListenerImpl.create(lType, apiType, l, source);
    }

    /** Creates a weak implementation of PropertyChangeListener.
     *
     * @param l the listener to delegate to
     * @param source the source that the listener should detach from when
     *     listener <CODE>l</CODE> is freed, can be <CODE>null</CODE>
     * @return a PropertyChangeListener delegating to <CODE>l</CODE>.
     */
    public static PropertyChangeListener propertyChange(PropertyChangeListener l, Object source) {
        WeakListenerImpl.PropertyChange wl = new WeakListenerImpl.PropertyChange(l);
        wl.setSource(source);

        return wl;
    }

    /** Creates a weak implementation of PropertyChangeListener to be attached
     * for a specific property name. Use with 
     * <code>addPropertyChangeListener(String propertyName, PropertyChangeListener listener)</code>
     * method. It calls
     * <code>removePropertyChangeListener(String propertyName, PropertyChangeListener listener)</code>
     * with the given property name to unregister the listener. Be sure to pass
     * the same <code>propertyName</code> to this method and to <code>addPropertyChangeListener()</code>
     * method.
     *
     * @param l the listener to delegate to
     * @param propertyName the name of the property to listen on changes
     * @param source the source that the listener should detach from when
     *     listener <CODE>l</CODE> is freed, can be <CODE>null</CODE>
     * @return a PropertyChangeListener delegating to <CODE>l</CODE>.
     * @since 9.2
     */
    public static PropertyChangeListener propertyChange(PropertyChangeListener l, String propertyName, Object source) {
        WeakListenerImpl.PropertyChange wl = new WeakListenerImpl.PropertyChange(l, propertyName);
        wl.setSource(source);

        return wl;
    }

    /** Creates a weak implementation of VetoableChangeListener.
     *
     * @param l the listener to delegate to
     * @param source the source that the listener should detach from when
     *     listener <CODE>l</CODE> is freed, can be <CODE>null</CODE>
     * @return a VetoableChangeListener delegating to <CODE>l</CODE>.
     */
    public static VetoableChangeListener vetoableChange(VetoableChangeListener l, Object source) {
        WeakListenerImpl.VetoableChange wl = new WeakListenerImpl.VetoableChange(l);
        wl.setSource(source);

        return wl;
    }

    /** Creates a weak implementation of VetoableChangeListener to be attached
     * for a specific property name. Use with 
     * <code>addVetoableChangeListener(String propertyName, PropertyChangeListener listener)</code>
     * method. It calls
     * <code>removeVetoableChangeListener(String propertyName, PropertyChangeListener listener)</code>
     * with the given property name to unregister the listener. Be sure to pass
     * the same <code>propertyName</code> to this method and to <code>addVetoableChangeListener()</code>
     * method.
     *
     * @param l the listener to delegate to
     * @param propertyName the name of the property to listen on changes
     * @param source the source that the listener should detach from when
     *     listener <CODE>l</CODE> is freed, can be <CODE>null</CODE>
     * @return a VetoableChangeListener delegating to <CODE>l</CODE>.
     * @since 9.2
     */
    public static VetoableChangeListener vetoableChange(VetoableChangeListener l, String propertyName, Object source) {
        WeakListenerImpl.VetoableChange wl = new WeakListenerImpl.VetoableChange(l, propertyName);
        wl.setSource(source);

        return wl;
    }

    /** Creates a weak implementation of DocumentListener.
     *
     * @param l the listener to delegate to
     * @param source the source that the listener should detach from when
     *     listener <CODE>l</CODE> is freed, can be <CODE>null</CODE>
     * @return a DocumentListener delegating to <CODE>l</CODE>.
     */
    public static DocumentListener document(DocumentListener l, Object source) {
        WeakListenerImpl.Document wl = new WeakListenerImpl.Document(l);
        wl.setSource(source);

        return wl;
    }

    /** Creates a weak implementation of ChangeListener.
     *
     * @param l the listener to delegate to
     * @param source the source that the listener should detach from when
     *     listener <CODE>l</CODE> is freed, can be <CODE>null</CODE>
     * @return a ChangeListener delegating to <CODE>l</CODE>.
     */
    public static ChangeListener change(ChangeListener l, Object source) {
        WeakListenerImpl.Change wl = new WeakListenerImpl.Change(l);
        wl.setSource(source);

        return wl;
    }
}
