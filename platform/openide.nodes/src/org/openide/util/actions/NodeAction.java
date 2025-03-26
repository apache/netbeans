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

package org.openide.util.actions;


import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;

/**
 * A type of action that listens on change in activated nodes selection and
 * allows its subclasses to simply change their enabled state and handle
 * action invocation requests.
 * <p>
 * Whenever a list of activated nodes changes (a new <a href="@org-openide-windows@/org/openide/windows/TopComponent.html">
 * TopComponent</a> is selected or
 * its internal selection changes like in 
 * <a href="@org-openide-explorer@/org/openide/explorer/ExplorerUtils.html">explorer</a>) 
 * the overriden method {@link #enable}
 * is called and state of the action is updated
 * according to the result. When the action is performed, the subclasses are
 * notified by a call to their {@link #performAction(Node[])} where they
 * can perform their operation on the currently selected array of nodes.
 *
 * <p><strong>Note:</strong> if your action involves getting cookies
 * from nodes, which in many cases is the correct design, please use
 * <a href="CookieAction.html">CookieAction</a> instead, as that permits sensitivity to cookies
 * and also listens to changes in supplied cookies.
 */
public abstract class NodeAction extends CallableSystemAction implements ContextAwareAction {
    private static final long serialVersionUID = -5672895970450115226L;

    /** whether or not anyone is listening to PROP_ENABLED */
    private static final String PROP_HAS_LISTENERS = "hasListeners"; // NOI18N

    /** last-used nodes, as a Reference<Node[]> */
    private static final String PROP_LAST_NODES = "lastNodes"; // NOI18N

    /** last-computed enablement (Boolean) */
    private static final String PROP_LAST_ENABLED = "lastEnabled"; // NOI18N

    /** the selection listener, if any */
    private static NodesL l;

    /** set of actions with listeners */
    private static final Set<NodeAction> listeningActions = new WeakSet<NodeAction>(100);

    /* Initialize the listener.
    */
    @Override
    protected void initialize() {
        super.initialize();
        putProperty(PROP_HAS_LISTENERS, Boolean.FALSE);

        // Not yet determined:
        putProperty(PROP_ENABLED, null);
    }

    /** Initializes selection listener.
     * If you override this method, you must always call the super method first.
     */
    @Override
    protected void addNotify() {
        super.addNotify();

        // initializes the listener
        putProperty(PROP_HAS_LISTENERS, Boolean.TRUE);

        synchronized (listeningActions) {
            if (l == null) {
                l = new NodesL();
            }

            if (listeningActions.isEmpty()) {
                l.setActive(true);
            }

            listeningActions.add(this);
        }
    }

    /** Shuts down the selection listener.
     * If you override this method, you must always call the super method last.
     */
    @Override
    protected void removeNotify() {
        synchronized (listeningActions) {
            listeningActions.remove(this);

            if (listeningActions.isEmpty()) {
                l.setActive(false);
            }
        }

        putProperty(PROP_HAS_LISTENERS, Boolean.FALSE);

        // Previous results should no longer be cached:
        putProperty(PROP_ENABLED, null);
        super.removeNotify();
    }

    /** Test for enablement based on {@link #enable}.
    * You probably ought not ever override this.
    * @return <code>true</code> to enable
    */
    @Override
    public boolean isEnabled() {
        Node[] ns = null;
        Boolean b = null;

        synchronized (getLock()) {
            b = (Boolean) getProperty(PROP_ENABLED);
            
            if (NodeAction.l == null) {
                NodeAction.l = new NodesL ();
                l.setActive (true);
            }

            NodesL listener = NodeAction.l;
            
            if (b == null) {
                ns = listener.getActivatedNodes(surviveFocusChange());

                Reference r = (Reference) getProperty(PROP_LAST_NODES);

                if ((r != null) && java.util.Arrays.equals((Node[]) r.get(), ns)) {
                    // Still using the same Node[] we did last time. Remember the result.
                    b = (Boolean) getProperty(PROP_LAST_ENABLED);

                    if (((Boolean) getProperty(PROP_HAS_LISTENERS)).booleanValue()) {
                        putProperty(PROP_ENABLED, b);
                    }
                } else {
                    // Really need to compute it.
                    // #17433: do this outside the lock!
                }

                // if inactive, we cannot safely cache results because node selection might change
            }
        }

        if (b == null) {
            b = (((ns != null) && enable(ns)) ? Boolean.TRUE : Boolean.FALSE);

            synchronized (getLock()) {
                putProperty(PROP_LAST_NODES, new WeakReference<Node[]>(ns));
                putProperty(PROP_LAST_ENABLED, b);

                if (((Boolean) getProperty(PROP_HAS_LISTENERS)).booleanValue()) {
                    putProperty(PROP_ENABLED, b);
                }
            }
        }

        return b.booleanValue();
    }

    /* Change enablement state.
     * Clears our previous cache.
     * Some NodeAction subclasses (CookieAction, MoveUpAction, ...) may call this
     * when some aspect of the node selection other than the selection itself
     * changes, so we should clear the cache to ensure that the enablement status
     * is respected.
     */
    @Override
    public void setEnabled(boolean e) {
        putProperty(PROP_LAST_ENABLED, null);
        putProperty(PROP_LAST_NODES, null);

        Boolean propHasListener = (Boolean) getProperty(PROP_HAS_LISTENERS);
        if (propHasListener != null && propHasListener.booleanValue()) {
            // Just set it; the next time selection chamges, we will recompute.
            super.setEnabled(e);
        } else {
            // Problematic. If we just set PROP_ENABLED then the next time isEnabled()
            // is called, even if the node selection is now different, we will be
            // in trouble; it will not bother to call enable() again.
            putProperty(PROP_ENABLED, null, true);
        }
    }

    /** Perform the action with a specific action event.
     * Normally this simply calls {@link #performAction()}, that is using
     * the global node selection.
     * However you may call this directly, with an action event whose
     * source is either a node or an array of nodes, to invoke the action
     * directly on that nodes or nodes. If you do this, the action must
     * be such that it would be enabled on that node selection, otherwise
     * the action is not required to behave correctly (that is, it can
     * be written to assume that it is never called with a node selection
     * it is not enabled on).
     * @param ev action event
     * @deprecated Using a special action event in this way is deprecated.
     * Better is to use {@link #createContextAwareInstance} and pass
     * a lookup containing all desired {@link Node} instances.
     */
    @Deprecated
    @Override
    public void actionPerformed(final ActionEvent ev) {
        final Object s = (ev == null) ? null : ev.getSource();

        if (s instanceof Node) {
            ActionInvoker.invokeAction(this, ev, amIasynchronous(), new Runnable() {
                @Override
                public void run() {
                    performAction(new Node[] { (Node) s });
                }
            });
        } else if (s instanceof Node[]) {
            ActionInvoker.invokeAction(this, ev, amIasynchronous(), new Runnable() {
                @Override
                public void run() {
                    performAction((Node[]) s);
                }
            });
        } else {
            super.actionPerformed(ev);
        }
    }

    /** Performs the action.
     * In the default implementation, calls {@link #performAction(Node[])}.
     * @deprecated Do not call this programmatically.
     * Use {@link #createContextAwareInstance} to pass in a node selection.
     * Do not override this method.
     */
    @Deprecated
    public void performAction() {
        performAction(getActivatedNodes());
    }

    /** Get the currently activated nodes.
    * @return the nodes (may be empty but not <code>null</code>)
    */
    public final Node[] getActivatedNodes() {
        NodesL listener = NodeAction.l;

        return (listener == null) ? new Node[0] : listener.getActivatedNodes(true);
    }

    /** Specify the behavior of the action when a window with no
    * activated nodes is selected.
    * If the action should then be disabled,
    * return <code>false</code> here; if the action should stay in the previous state,
    * return <code>true</code>.
    * <p>Note that {@link #getActivatedNodes} and {@link #performAction} are still
    * passed the set of selected nodes from the old window, if you keep this feature on.
    * This is useful, e.g., for an action like Compilation which should remain active
    * even if the user switches to a window like the Output Window that has no associated nodes;
    * then running the action will still use the last selection from e.g. an Explorer window
    * or the Editor, if there was one to begin with.
    *
    * @return <code>true</code> in the default implementation
    */
    protected boolean surviveFocusChange() {
        return true;
    }

    /**
    * Perform the action based on the currently activated nodes.
    * Note that if the source of the event triggering this action was itself
    * a node, that node will be the sole argument to this method, rather
    * than the activated nodes.
    *
    * @param activatedNodes current activated nodes, may be empty but not <code>null</code>
    */
    protected abstract void performAction(Node[] activatedNodes);

    /**
    * Test whether the action should be enabled based
    * on the currently activated nodes.
    *
    * @param activatedNodes current activated nodes, may be empty but not <code>null</code>
    * @return <code>true</code> to be enabled, <code>false</code> to be disabled
    */
    protected abstract boolean enable(Node[] activatedNodes);

    /** Implements <code>ContextAwareAction</code> interface method.
     *
     * Returns a delegate action that is associated with a specific lookup and
     * extracts the nodes it operates on from it. Otherwise it delegates to the
     * regular NodeAction (especially to {@link #enable} and {@link #performAction} methods).
     * Note: Never call directly methods <code>setEnabled</code> or <code>putValue</code>
     * of this delegate action, it is useless, they are empty. The enablement
     * state of the action is driven by the content of the <code>actionContext</code>.
     *
     * @param actionContext a lookup contains action context, cannot be <code>null</code>
     * @return a delegate action
     */
    public Action createContextAwareInstance(Lookup actionContext) {
        return new DelegateAction(this, actionContext);
    }

    /** Fire PROP_ENABLE if the value is currently known (and clear that value).
     */
    void maybeFireEnabledChange() {
        boolean fire = false;

        synchronized (getLock()) {
            if (getProperty(PROP_ENABLED) != null) {
                putProperty(PROP_ENABLED, null);
                fire = true;
            }
        }

        if (fire) {
            try {
                firePropertyChange(PROP_ENABLED, null, null);
            } catch (NullPointerException e) {
                // Probably because of a JDK bug that AbstractButton$ButtonActionPropertyChangeListener.propertyChange does not grok null values for "enabled" prop:
                Exceptions.attachMessage(e,
                                         "You cannot add " +
                                         getClass().getName() +
                                         " directly to a JMenu etc.; use org.openide.awt.Actions.connect instead"); // NOI18N
                Logger.getLogger(NodeAction.class.getName()).log(Level.WARNING, null, e);
            }
        }
    }

    final boolean amIasynchronous() {
        return asynchronous();
    }
    
    /** Node listener to check whether the action is enabled or not
    */
    private static final class NodesL implements LookupListener {
        /** result with Nodes we listen to */
        private volatile Lookup.Result<Node> result;

        /** whether to change enablement of nodes marked to survive focus change */
        private boolean chgSFC = false;

        /** and those marked to not survive */
        private boolean chgNSFC = false;

        /** pointer to previously activated nodes (via Reference to Node) */
        private Reference<Node>[] activatedNodes;

        /** Constructor that checks the current state
        */
        public NodesL() {
        }

        /** Computes the list of activated nodes.
         */
        public Node[] getActivatedNodes(boolean survive) {
OUTER: 
            if (survive && (activatedNodes != null)) {
                Node[] arr = new Node[activatedNodes.length];

                for (int i = 0; i < arr.length; i++) {
                    if ((arr[i] = activatedNodes[i].get()) == null) {
                        break OUTER;
                    }
                }

                return arr;
            }

            Lookup.Result<Node> r = result;

            return (r == null) ? new Node[0] : r.allInstances().toArray(new Node[0]);
        }

        /** Activates/passivates the listener.
        */
        synchronized void setActive(boolean active) {
            Lookup context = Utilities.actionsGlobalContext();

            if (active) {
                if (result == null) {
                    result = context.lookupResult(Node.class);
                    result.addLookupListener(this);
                }
            } else {
                //                result.removeLookupListener (this);
                //                result = null;
                // Any saved PROP_ENABLED will be bogus now:
                forget(true);
                forget(false);
            }
        }

        /** Property change listener.
        */
        public void resultChanged(LookupEvent ev) {
            Lookup.Result<Node> r = result;

            if (r == null) {
                return;
            }

            chgSFC = true;
            chgNSFC = true;

            Collection<? extends Lookup.Item<Node>> items = result.allItems();
            boolean updateActivatedNodes = true;

            if (items.size() == 1) {
                Lookup.Item<Node> item = items.iterator().next();

                if ("none".equals(item.getId()) && (item.getInstance() == null)) {
                    // this is change of selected node to null,
                    // do not update activatedNodes
                    updateActivatedNodes = false;
                }
            }

            if (updateActivatedNodes) {
                Iterator<? extends Node> it = result.allInstances().iterator();
                ArrayList<Reference<Node>> list = new ArrayList<Reference<Node>>();

                while (it.hasNext()) {
                    list.add(new WeakReference<Node>(it.next()));
                }

                activatedNodes = list.toArray(new Reference[0]);
            }

            update();
        }

        /** Updates the state of the action.
        */
        public void update() {
            if (chgSFC) {
                forget(true);
                chgSFC = false;
            }

            if (chgNSFC) {
                forget(false);
                chgNSFC = false;
            }
        }

        /** Checks the state of the action.
         * Or rather, it just forgets it ever knew.
         * @param sfc if true, only survive-focus-change actions affected, else only not-s-f-c
        */
        private void forget(boolean sfc) {
            List<NodeAction> as;

            synchronized (listeningActions) {
                as = new ArrayList<NodeAction>(listeningActions.size());

                for (Iterator<NodeAction> it = listeningActions.iterator(); it.hasNext();) {
                    as.add(it.next());
                }
            }

            Iterator<NodeAction> it = as.iterator();

            while (it.hasNext()) {
                final NodeAction a = it.next();

                if (a.surviveFocusChange() == sfc) {
                    Mutex.EVENT.readAccess(new Runnable() {
                        public void run() {
                            a.maybeFireEnabledChange();
                        }
                    });
                }
            }
        }
    }
     // end of NodesL

    /** A delegate action that is usually associated with a specific lookup and
     * extract the nodes it operates on from it. Otherwise it delegates to the
     * regular NodeAction.
     */
    static class DelegateAction implements Action, LookupListener, Presenter.Menu, Presenter.Popup, Presenter.Toolbar {
        private static final Node[] EMPTY_NODE_ARRAY = new Node[0];

        /** action to delegate too */
        private NodeAction delegate;

        /** lookup we are associated with (or null) */
        private org.openide.util.Lookup.Result<Node> result;

        /** previous state of enabled */
        private boolean enabled = true;

        /** support for listeners */
        private PropertyChangeSupport support = new PropertyChangeSupport(this);

        public DelegateAction(NodeAction a, Lookup actionContext) {
            this.delegate = a;

            this.result = actionContext.lookupResult(Node.class);
            this.result.addLookupListener(WeakListeners.create(LookupListener.class,
                                                               this, this.result));
            resultChanged(null);
        }

        /** Overrides superclass method, adds delegate description. */
        @Override
        public String toString() {
            return super.toString() + "[delegate=" + delegate + "]"; // NOI18N
        }

        /** Nodes are taken from the lookup if any.
         */
        public final synchronized Node[] nodes() {
            if (result != null) {
                return result.allInstances().toArray(EMPTY_NODE_ARRAY);
            } else {
                return EMPTY_NODE_ARRAY;
            }
        }

        /** Invoked when an action occurs.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            ActionInvoker.invokeAction(delegate, e, delegate.amIasynchronous(), new Runnable() {
                @Override
                public void run() {
                    delegate.performAction(nodes());
                }
            });
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            support.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            support.removePropertyChangeListener(listener);
        }

        public void putValue(String key, Object o) {
        }

        public Object getValue(String key) {
            return delegate.getValue(key);
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean b) {
        }

        public void resultChanged(LookupEvent ev) {
            final boolean old = enabled;
            enabled = delegate.enable(nodes());
            Mutex.EVENT.readAccess(new Runnable() {

                @Override
                public void run () {
                    support.firePropertyChange(PROP_ENABLED, old, enabled);
                }
            });
        }

        public JMenuItem getMenuPresenter() {
            if (isMethodOverridden(delegate, "getMenuPresenter")) { // NOI18N

                return delegate.getMenuPresenter();
            } else {
                return new Actions.MenuItem(this, true);
            }
        }

        public JMenuItem getPopupPresenter() {
            if (isMethodOverridden(delegate, "getPopupPresenter")) { // NOI18N

                return delegate.getPopupPresenter();
            } else {
                return new Actions.MenuItem(this, false);
            }
        }

        public Component getToolbarPresenter() {
            if (isMethodOverridden(delegate, "getToolbarPresenter")) { // NOI18N

                return delegate.getToolbarPresenter();
            } else {
                return new Actions.ToolbarButton(this);
            }
        }

        private boolean isMethodOverridden(NodeAction d, String name) {
            try {
                Method m = d.getClass().getMethod(name, new Class[0]);

                return m.getDeclaringClass() != CallableSystemAction.class;
            } catch (java.lang.NoSuchMethodException ex) {
                ex.printStackTrace();
                throw new IllegalStateException("Error searching for method " + name + " in " + d); // NOI18N
            }
        }
    }
     // end of DelegateAction
}
