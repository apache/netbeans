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
package org.openide.nodes;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import javax.swing.Action;
import org.openide.util.NbBundle;

/**
 * Factory used to create <a href="Children.html">Children</a>
 * objects.  Children objects supply child Nodes for a
 * Node.  Usage is to write a class that extends ChildFactory and
 * pass that to Children.create().  When the Node is expanded or its
 * children are programmatically requested, the
 * <a href="#createKeys(java.util.List)">createKeys(List &lt;T&gt;)</a> method
 * will be invoked to create the List of objects to be modelled as Nodes.
 * Later, on demand, each object from the List will be passed in turn to
 * <a href="#createNodesForKey(java.lang.Object)">createNodesForKey(T)</a>,
 * which may return an array of zero or more Nodes for the object.
 * <p>
 * A ChildFactory can be used either to create typical Children object, or
 * one which will be initialized on a background thread (providing
 * a "Please Wait" Node in the meantime).  It can be used most simple cases
 * that Children.Keys has been historically used for, and makes it easy to
 * change a Children object to compute its keys asynchronously if that is
 * needed for performance reasons.
 * <p>
 * Only one ChildFactory object may be used per Children object;  if you wish
 * to have multiple Nodes modelling children produced by a single
 * ChildFactory, use @link FilterNode to wrap the Node that owns the
 * Children object for this ChildFactory.
 * <p>
 * To use, simply override
 * <a href="#createKeys(java.util.List)">createKeys(List &lt;T&gt;) and
 * <a href="#createNodesForKey(java.lang.Object)">createNodesForKey(T)</a> or
 * <a href="#createNodeForKey(java.lang.Object)">createNodeForKey(T)</a>.
 *
 * @param T The type of objects in the keys collection
 * @author Tim Boudreau
 * @see Children#create(ChildFactory, boolean)
 * @since org.openide.nodes 7.1
 */
public abstract class ChildFactory <T> {
    /**
     * Create a Node for a given key that was put into the list passed into
     * createKeys().  Either override this method if there will always be
     * 0 or 1 nodes per key, or createNodesForKey() if there may be more
     * than one.
     *
     * The default implementation throws an AssertionError.  If you override
     * createNodesForKey() and do not call super, then you do not need to
     * override this method; but at least one of the two must be overridden.
     *
     * @param key An object that was previously put into the list passed
     *            to createKeys()
     * @return A node, or null if no node should be shown for this object.
     */
    protected Node createNodeForKey(T key) {
        throw new AssertionError("Neither createNodeForKey() nor " + //NOI18N
                "createNodesForKey() overridden in " + getClass().getName()); //NOI18N
    }
    
    /**
     * Create Nodes for a given key object (one from the <code>List</code>
     * passed to createKeys(List <T>)).  The default implementation simply
     * delegates to <code>createNodeForKey</code> and returns the result of
     * that call in an array of nodes.
     * <p>
     * Most Children objects have a 1:1 mapping between keys and nodes.  For
     * convenience in that situation, simply override createNodeForKey(T).
     *
     * @param key An object from the list returned by
     *        <code>asynchCreateKeys()</code>
     * @return null if no nodes, or zero or more Nodes to represent this key
     */
    protected Node[] createNodesForKey(T key) {
        Node n = createNodeForKey(key);
        return n == null ? null : new Node[] { n };
    }
    /**
     * Create a list of keys which can be individually passed to
     * createNodes() to create child Nodes.  Implementations of
     * this method should regularly check Thread.interrupted(), and
     * if it returns true (meaning the parent Node was collapsed or
     * destroyed), stop creating keys immediately and return
     * true.  This method is guaranteed <i>not</i> to be called on the
     * AWT event thread if this ChildFactory was passed to
     * Children.create() with the <code>asynchronous</code> parameter
     * set to true.  If not, then no guarantees are made as to what
     * the calling thread is.
     * <p>Returning false is tricky since there is no way to tell whether
     * the loop has been restarted except by examining what is already in
     * the list. As of 7.27 it is generally unnecessary since calls to
     * {@link List#add(Object)} will immediately display the new element
     * as well as checking for interruption.
     * @param toPopulate A list to add key objects to
     * @return true if the list of keys has been completely populated,
     *         false if the list has only been partially populated and
     *         this method should be called again to batch more keys
     */
    protected abstract boolean createKeys(List <T> toPopulate);
    
    /**
     * Call this method when the list of objects being modelled by the
     * has changed and the child Nodes of this Node should be updated.  The
     * boolean argument is a <i>hint</i> to the refresh mechanism (which
     * will cause createKeys() to be invoked again) that it is safe to
     * synchronously recreate.
     *
     * @param immediate If true, the refresh should occur in the calling
     *   thread (be careful not to be holding any locks that might
     *   deadlock with your key/child creation methods if you pass true).
     *   Note that this parameter is only meaningful when using an
     *   asynchronous children instance (i.e. true was passed as the
     *   second parameter to <code>Children.create()</code>).  If the
     *   Children object for this ChildFactory is called with <code>immediate</code>
     *   true on the AWT event dispatch thread, and it is an asynchronous
     *   Children object, this parameter will be ignored and computation
     *   will be scheduled on a background thread.
     */
    protected final void refresh(boolean immediate) {
        Observer obs = observer == null ? null : observer.get();
        if (obs != null) {
            obs.refresh(immediate);
        }
    }
    
    Node getWaitNode() {
        Node n = createWaitNode();
        return n == null ? null : new WaitFilterNode(n);
    }
    
    /**
     * Create the Node that should be shown while the keys are being computed
     * on a background thread.
     * This method will not be called if this ChildFactory is used for a
     * synchronous children which does not compute its keys on a background
     * thread.  Whether an instance is synchronous or not is determined by a
     * parameter to
     * <a href="Children.html#create(ChildFactory, boolean)">Children.create()</a>.
     * <p>
     * To show no node at all when the Children object is initially expanded in
     * the UI, simply return null.
     * <p>
     * The default implementation returns a Node that shows an hourglass cursor
     * and the localized text &quot;Please Wait...&quot;.
     *
     * @return A Node, or null if no wait node should be shown.
     */
    protected Node createWaitNode() {
        AbstractNode n = new AbstractNode(Children.LEAF) {
            public @Override Action[] getActions(boolean context) {
                return new Action[0];
            }
        };
        n.setIconBaseWithExtension("org/openide/nodes/wait.gif"); //NOI18N
        n.setDisplayName(NbBundle.getMessage(ChildFactory.class, "LBL_WAIT")); //NOI18N
        return n;
    }
    
    private Reference <Observer> observer = null;
    final void setObserver(Observer observer) {
        if (this.observer != null) {
            throw new IllegalStateException("Attempting to create two Children" + //NOI18N
                    " objects for a single ChildFactory " + this + ".  Use " + //NOI18N
                    "FilterNode.Children over the existing Children object " + //NOI18N
                    "instead"); //NOI18N
        }
        this.observer = new WeakReference <Observer> (observer);
    }

    void removeNotify() {
        //do nothing
    }

    void addNotify() {
        //do nothing
    }
    
    void destroyNodes(Node[] arr) {
        //do nothing
    }

    interface Observer {
        public void refresh(boolean immediate);
    }
    
    static boolean isWaitNode(Object n) {
        return n instanceof WaitFilterNode;
    }
    
    /**
     * This class exists to uniquify/mark any Node returned by createWaitNode
     * such that AsynchChildren can identify it absolutely as not being an
     * object that should be passed to createNodeForKey().
     */
    private static final class WaitFilterNode extends FilterNode {
        public WaitFilterNode(Node orig) {
            super(orig);
        }
    }

    /**
     * Subclass of ChildFactory with lifecycle methods which will be called
     * on first use and last use.
     *
     * @param <T> The key type for this child factory
     * @since org.openide.nodes 7.7
     */
    public abstract static class Detachable<T> extends ChildFactory<T>{
        /**
         * Called immediately before the first call to createKeys().  Override
         * to set up listening for changes, allocating expensive-to-create
         * resources, etc.
         */
        @Override
        protected void addNotify() {
            //do nothing
        }
        /**
         * Called when this child factory is no longer in memory. 
         * Does nothing by default; override if you need notification when not in use anymore.
         *
         * Note that this is usually not the best place for unregistering listeners, etc.,
         * as listeners usually keep the child factory in memory, preventing it from being collected, thus preventing
         * this method to be called in the first place.
         */
        @Override
        protected void removeNotify() {
            //do nothing
        }

    }

}
