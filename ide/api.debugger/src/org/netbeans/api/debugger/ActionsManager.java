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

package org.netbeans.api.debugger;

import java.beans.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.debugger.registry.ContextAwareServicePath;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderListener;
import org.openide.util.Cancellable;
import org.openide.util.Mutex;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 * Manages some set of actions. Loads some set of ActionProviders registered
 * for some context, and allows to call isEnabled and doAction methods on them.
 *
 * @author   Jan Jancura
 */
public final class ActionsManager {


    /** Action constant for Step Over Action. */
    public static final Object              ACTION_STEP_OVER = "stepOver";
    
    /** Action constant for breakpoint hit action. */
    public static final Object              ACTION_RUN_INTO_METHOD = "runIntoMethod";
    
    /** Action constant for Step Into Action. */
    public static final Object              ACTION_STEP_INTO = "stepInto";
    
    /** Action constant for Step Out Action. */
    public static final Object              ACTION_STEP_OUT = "stepOut";
    
    /** Action constant for Step Operation Action. */
    public static final Object              ACTION_STEP_OPERATION = "stepOperation";
    
    /** Action constant for Continue Action. */
    public static final Object              ACTION_CONTINUE = "continue";
    
    /** Action constant for Start Action. */
    public static final Object              ACTION_START = "start";
    
    /** Action constant for Kill Action. */
    public static final Object              ACTION_KILL= "kill";
    
    /** Action constant for Make Caller Current Action. */
    public static final Object              ACTION_MAKE_CALLER_CURRENT = "makeCallerCurrent";
    
    /** Action constant for Make Callee Current Action. */
    public static final Object              ACTION_MAKE_CALLEE_CURRENT = "makeCalleeCurrent";
    
    /** Action constant for Pause Action. */
    public static final Object              ACTION_PAUSE = "pause";
    
    /** Action constant for Run to Cursor Action. */
    public static final Object              ACTION_RUN_TO_CURSOR = "runToCursor";
    
    /** Action constant for Pop Topmost Call Action. */
    public static final Object              ACTION_POP_TOPMOST_CALL = "popTopmostCall";
    
    /** Action constant for Fix Action. */
    public static final Object              ACTION_FIX = "fix";
    
    /** Action constant for Restart Action. */
    public static final Object              ACTION_RESTART = "restart";

    /** Action constant for Toggle Breakpoint Action. */
    public static final Object              ACTION_TOGGLE_BREAKPOINT = "toggleBreakpoint";
    
    /** Action constant for New Watch Action.
     * @since 1.24 */
    public static final Object              ACTION_NEW_WATCH = "newWatch";

    /** Action constant for Evaluate Action.
     *  @since 1.29 */
    public static final Object              ACTION_EVALUATE = "evaluate";

    private static final Logger logger = Logger.getLogger(ActionsManager.class.getName());

    // variables ...............................................................
    
    private final List<ActionsManagerListener>    listener = new ArrayList<>();
    private final HashMap<String, List<ActionsManagerListener>> listeners = new HashMap<>();
    private HashMap<Object, List<ActionsProvider>>  actionProviders;
    private final Object            actionProvidersLock = new Object();
    private final AtomicBoolean     actionProvidersInitialized = new AtomicBoolean(false);
    private final ActionsProviderListener actionListener = (Object action, boolean enabled) -> fireActionStateChanged (action);
    private final Lookup            lookup;
    private boolean                 doiingDo = false;
    private boolean                 destroy = false;
    private volatile List<? extends ActionsProvider> aps;
    private volatile PropertyChangeListener  providersChangeListener;
    
    /**
     * Create a new instance of ActionManager.
     * This is called from synchronized blocks of other classes that need to have
     * just one instance of this. Therefore do not put any foreign calls here.
     */
    ActionsManager (Lookup lookup) {
        this.lookup = lookup;
        logger.log(Level.FINE, "new ActionsManager({0}) = {1}", new Object[] { lookup, this });
    }
    
    
    // main public methods .....................................................

    /**
     * Performs action on this DebbuggerEngine.
     *
     * @param action action constant (default set of constants are defined
     *    in this class with ACTION_ prefix)
     */
    public final void doAction (final Object action) {
        doiingDo = true;
        List<ActionsProvider> l = getActionProvidersForActionWithInit(action);
        boolean done = false;
        for (ActionsProvider ap : l) {
            if (ap.isEnabled (action)) {
                fireActionToBeRun(action);
                done = true;
                ap.doAction (action);
            }
        }
        if (done) {
            fireActionDone (action);
        }
        doiingDo = false;
        if (destroy) {
            destroyIn ();
        }
    }
    
    /**
     * Post action on this DebuggerEngine.
     * This method does not block till the action is done,
     * if <code>#canPostAsynchronously}</code> returns true.
     * Otherwise it behaves like {@link #doAction}.
     * The returned task, or
     * {@link ActionsManagerListener} can be used to
     * be notified when the action is done.
     *
     * @param action action constant (default set of constants are defined
     *    in this class with ACTION_ prefix)
     *
     * @return a task, that can be checked for whether the action finished
     *         or not.
     *
     * @since 1.5
     */
    public final Task postAction(final Object action) {
        doiingDo = true;
        boolean inited;
        synchronized (actionProvidersLock) {
            inited = (actionProviders != null);
        }
        if (!inited && Mutex.EVENT.isReadAccess()) { // is EDT
            return postActionWithLazyInit(action);
        }
        
        List<ActionsProvider> l = getActionProvidersForActionWithInit(action);
        boolean posted = false;
        
        List<ActionsProvider> postedActions = new ArrayList<>(l.size());
        final AsynchActionTask task = new AsynchActionTask(postedActions);
        for (ActionsProvider ap : l) {
            if (ap.isEnabled (action)) {
                postedActions.add(ap);
                posted = true;
            }
        }
        if (posted) {
            if (postedActions.size() > 1) {
                // We have more than one action provider for a single action.
                // Check their paths and choose the most specific one:
                postedActions = selectTheMostSpecific(postedActions);
            }
            //final int[] count = new int[] { postedActions.size() };
            final AtomicInteger count = new AtomicInteger(postedActions.size());
            Runnable notifier = () -> {
                if (count.decrementAndGet() == 0) {
                    task.actionDone();
                    fireActionDone (action);
                    doiingDo = false;
                    if (destroy) {
                        destroyIn ();
                    }
                }
            };
            fireActionToBeRun(action);
            for (ActionsProvider pa : postedActions) {
                pa.postAction (action, notifier);

            }
        } else {
            doiingDo = false;
            if (destroy) {
                destroyIn ();
            }
            task.actionDone();
        }
        return task;
    }

    private Task postActionWithLazyInit(final Object action) {
        final AsynchActionTask task = new AsynchActionTask(Collections.emptyList());
        new RequestProcessor(ActionsManager.class).post(() -> {
            try {
                doAction(action);
            } finally {
                task.actionDone();
            }
        });
        return task;
    }
    
    private static List<ActionsProvider> selectTheMostSpecific(List<ActionsProvider> aps) {
        // This method is called in the rare case of two debugger plugin are registered for the same language
        // It tries to detect which implementation is more specific.
        ArrayList<Pair<String, ActionsProvider>> providersByPath = new ArrayList<>();

        for (ActionsProvider ap : aps) {
            String path = getPath(ap);
            if (path == null) {
                return aps;
            }
            providersByPath.add(Pair.of(path, ap));
        }

        int n = providersByPath.size();
        for (int i = 0; i < n; i++) {
            String p1 = providersByPath.get(i).first();
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    continue;
                }
                String p2 = providersByPath.get(j).first();
                if (!p1.equals(p2) && p1.startsWith(p2)) {
                    providersByPath.remove(j);
                    i--;
                    n--;
                    break;
                }
            }
        }
        List<ActionsProvider> ret = aps;
        if (providersByPath.size() < aps.size()) {
            ret = new LinkedList<>();
            for (Pair<String, ActionsProvider> pair : providersByPath) {
                ret.add(pair.second());
            }
        }
        return ret;
    }
    
    private static String getPath(ActionsProvider ap) {
        if (ap instanceof ContextAwareServicePath) {
            String path = ((ContextAwareServicePath) ap).getServicePath();
            int i = path.lastIndexOf('/');
            if (i > 0) {
                return path.substring(0, i);
            } else {
                return "";
            }
        } else {
            return null;
        }
    }

    /**
     * Returns true if given action can be performed on this DebuggerEngine.
     * 
     * @param action action constant (default set of constants are defined
     *    in this class with ACTION_ prefix)
     * @return true if given action can be performed on this DebuggerEngine
     */
    public final boolean isEnabled (final Object action) {
        boolean doInit = false;
        synchronized (actionProvidersLock) {
            if (actionProviders == null) {
                actionProviders = new HashMap<>();
                doInit = true;
            }
        }
        if (doInit) {
            if (Mutex.EVENT.isReadAccess()) { // SwingUtilities.isEventDispatchThread()
                // Need to initialize lazily when called in AWT
                // A state change will be fired after actions providers are initialized.
                new RequestProcessor(ActionsManager.class).post(() -> initActionImpls());
            } else {
                initActionImpls();
            }
        }

        List<ActionsProvider> l = getActionProvidersForAction(action);
        for (ActionsProvider ap : l) {
            if (ap.isEnabled (action)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Stops listening on all actions, stops firing events.
     */
    public void destroy () {
        if (!doiingDo) {
            destroyIn ();
        }
        destroy = true;
    }

    
    // ActionsManagerListener support ..........................................

    /**
     * Add ActionsManagerListener.
     *
     * @param l listener instance
     */
    public void addActionsManagerListener (ActionsManagerListener l) {
        synchronized (listener) {
            listener.add(l);
        }
    }

    /**
     * Removes ActionsManagerListener.
     *
     * @param l listener instance
     */
    public void removeActionsManagerListener (ActionsManagerListener l) {
        synchronized (listener) {
            listener.remove(l);
        }
    }

    /** 
     * Add ActionsManagerListener.
     *
     * @param propertyName a name of property to listen on
     * @param l the ActionsManagerListener to add
     */
    public void addActionsManagerListener (
        String propertyName, 
        ActionsManagerListener l
    ) {
        synchronized (listeners) {
            List<ActionsManagerListener> ls = listeners.get (propertyName);
            if (ls == null) {
                ls = new ArrayList<>();
                listeners.put (propertyName, ls);
            }
            ls.add(l);
        }
    }

    /** 
     * Remove ActionsManagerListener.
     *
     * @param propertyName a name of property to listen on
     * @param l the ActionsManagerListener to remove
     */
    public void removeActionsManagerListener (
        String propertyName, 
        ActionsManagerListener l
    ) {
        synchronized (listeners) {
            List<ActionsManagerListener> ls = listeners.get (propertyName);
            if (ls == null) {
                return;
            }
            ls.remove(l);
            if (ls.isEmpty()) {
                listeners.remove(propertyName);
            }
        }
    }

    
    // firing support ..........................................................
    
    private void fireActionToBeRun(Object action) {
        initListeners ();
        List<ActionsManagerListener> l1;
        synchronized (listeners) {
            l1 = listeners.get("actionToBeRun");
            l1 = l1 != null ? new ArrayList<>(l1) : Collections.emptyList();
        }
        PropertyChangeEvent e = new PropertyChangeEvent(this, "actionToBeRun", null, action);
        for (ActionsManagerListener aml : l1) {
            if (aml instanceof PropertyChangeListener) {
                ((PropertyChangeListener) aml).propertyChange(e);
            }

        }
    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listener registered listeners} that a breakpoint
     * {@link DebuggerManagerListener#breakpointRemoved was removed}
     * and {@link #pcs property change listeners} that its properties
     * {@link PropertyChangeSupport#firePropertyChange(String, Object, Object)}
     * were changed.
     *
     * @param breakpoint  a breakpoint that was removed
     */
    private void fireActionDone (
        final Object action
    ) {
        initListeners ();
        List<ActionsManagerListener> l = new ArrayList<>(listener);
        List<ActionsManagerListener> l1;
        synchronized (listeners) {
            l1 = listeners.get(ActionsManagerListener.PROP_ACTION_PERFORMED);
            l1 = l1 != null ? new ArrayList<>(l1) : Collections.emptyList();
        }
        for (ActionsManagerListener aml : l) {
            aml.actionPerformed(action);
        }
        for (ActionsManagerListener aml : l1) {
            aml.actionPerformed(action);
        }
    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listener registered listeners} that a breakpoint
     * {@link DebuggerManagerListener#breakpointRemoved was removed}
     * and {@link #pcs property change listeners} that its properties
     * {@link PropertyChangeSupport#firePropertyChange(String, Object, Object)}
     * were changed.
     *
     * @param breakpoint  a breakpoint that was removed
     */
    private void fireActionStateChanged (
        final Object action
    ) {
        boolean enabled = isEnabled (action);
        initListeners ();
        List<ActionsManagerListener> l = new ArrayList<>(listener);
        List<ActionsManagerListener> l1;
        synchronized (listeners) {
            l1 = listeners.get(ActionsManagerListener.PROP_ACTION_STATE_CHANGED);
            l1 = l1 != null ? new ArrayList<>(l1) : Collections.emptyList();
        }
        for (ActionsManagerListener aml : l) {
            aml.actionStateChanged(action, enabled);
        }
        for (ActionsManagerListener aml : l1) {
            aml.actionStateChanged(action, enabled);
        }
    }
    
    
    // private support .........................................................
    
    private List<ActionsProvider> getActionProvidersForAction(Object action) {
        synchronized (actionProvidersLock) {
            List<ActionsProvider> l = actionProviders.get(action);
            return l != null ? new ArrayList<>(l) : Collections.emptyList();
        }
    }
    
    private List<ActionsProvider> getActionProvidersForActionWithInit(Object action) {
        boolean doInit = false;
        synchronized (actionProvidersLock) {
            if (actionProviders == null) {
                actionProviders = new HashMap<>();
                doInit = true;
            }
        }
        if (doInit) {
            initActionImpls ();
        } else {
            if (!actionProvidersInitialized.get()) {
                synchronized (actionProvidersInitialized) {
                    if (!actionProvidersInitialized.get()) {
                        try {
                            actionProvidersInitialized.wait();
                        } catch (InterruptedException ex) {}
                    }
                }
            }
        }
        return getActionProvidersForAction(action);
    }
    
    private void registerActionsProvider (Object action, ActionsProvider p) {
        synchronized (actionProvidersLock) {
            List<ActionsProvider> l = actionProviders.get(action);
            if (l == null) {
                l = new ArrayList<>();
                actionProviders.put (action, l);
            }
            l.add (p);
        }
        p.addActionsProviderListener (actionListener);
        fireActionStateChanged (action);
    }
    
    private void registerActionsProviders(List<? extends ActionsProvider> aps) {
        synchronized (aps) {
            if (logger.isLoggable(Level.INFO)) {
                StringBuilder sb = new StringBuilder(this.toString());
                boolean isNull = false;
                sb.append(".registerActionsProviders:");
                for (ActionsProvider ap : aps) {
                    sb.append("\n  ");
                    if (ap != null) {
                        sb.append(ap.toString());
                    } else {
                        sb.append("NULL element in list ").append(Integer.toHexString(aps.hashCode())); // NOI18N
                        isNull = true;
                    }
                }
                sb.append("\n");
                if (isNull) {
                    logger.info(sb.toString());
                } else {
                    logger.fine(sb.toString());
                }
            }
            for (ActionsProvider ap : aps) {
                if (ap != null) {
                    for (Object action : ap.getActions ()) {
                        registerActionsProvider (action, ap);
                    }
                }
            }
        }
    }

    private void initActionImpls () {
        try {
            aps = lookup.lookup(null, ActionsProvider.class);
            providersChangeListener = new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        logger.log(Level.FINE, "{0} Providers lookup changed, aps = {1}", new Object[] { this, aps });
                        synchronized (actionProvidersLock) {
                            actionProviders.clear();
                        }
                        registerActionsProviders(aps);
                    }
            };
            logger.log(Level.FINE, "{0}.initActionImpls(): Add ProvidersChangeListener to {1}", new Object[] { this, aps });
            ((Customizer) aps).addPropertyChangeListener(providersChangeListener);
            registerActionsProviders(aps);
        } finally {
            synchronized (actionProvidersInitialized) {
                actionProvidersInitialized.set(true);
                actionProvidersInitialized.notifyAll();
            }
        }
    }

    private boolean listerersLoaded = false;
    private List<? extends LazyActionsManagerListener> lazyListeners;
    
    private synchronized void initListeners () {
        if (listerersLoaded) {
            return;
        }
        listerersLoaded = true;
        lazyListeners = lookup.lookup (null, LazyActionsManagerListener.class);
        for (LazyActionsManagerListener l : lazyListeners) {
            if (l == null) {
                // instance could not be created.
                continue;
            }
            String[] props = l.getProperties ();

            if (props == null) {
                addActionsManagerListener (l);
            } else {
                for (String prop : props) {
                    addActionsManagerListener (prop, l);
                }
            }
        }
    }
    
    private void destroyIn () {
        Customizer caps = (Customizer) aps;
        PropertyChangeListener pchl = providersChangeListener;
        if (caps != null && pchl != null) {
            caps.removePropertyChangeListener(pchl);
            logger.log(Level.FINE, "{0}.destroyIn(): ProvidersChangeListener removed from {1}", new Object[] { this, caps });
        }
        synchronized (this) {
            if (lazyListeners != null) {
                for (LazyActionsManagerListener l : lazyListeners) {
                    if (l == null) {
                        // instance could not be created.
                        continue;
                    }
                    String[] props = l.getProperties ();
                    if (props == null) {
                        removeActionsManagerListener (l);
                        continue;
                    } else {
                        for (String prop : props) {
                            removeActionsManagerListener (prop, l);
                        }
                    }
                    l.destroy ();
                }
                lazyListeners = new ArrayList<>();
            }
        }
        synchronized (actionProvidersLock) {
            Collection<List<ActionsProvider>> apsc = actionProviders.values();
            for (List<ActionsProvider> ps : apsc) {
                for (ActionsProvider ap : ps) {
                    ap.removeActionsProviderListener(actionListener);
                }
            }
        }
    }

    
    // innerclasses ............................................................
    
    private static class AsynchActionTask extends Task implements Cancellable {
        
        private final Collection<? extends ActionsProvider> postedActions;
        
        public AsynchActionTask(Collection<? extends ActionsProvider> postedActions) {
            this.postedActions = postedActions;
        }
        
        void actionDone() {
            notifyFinished();
        }

        @Override
        public boolean cancel() {
            for (ActionsProvider ap : postedActions) {
                Cancellable c = getCancellable(ap);
                if (c != null) {
                    if (!c.cancel()) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        }
        
        private Cancellable getCancellable(Object action) {
            if (action instanceof Cancellable) {
                return (Cancellable) action;
            }
            try {
                // Hack because of ActionsProvider$ContextAware:
                Field delegateField = action.getClass().getDeclaredField("delegate");   // NOI18N
                delegateField.setAccessible(true);
                Object delegate = delegateField.get(action);
                if (delegate instanceof Cancellable) {
                    return (Cancellable) action;
                }
            } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException ex) {}
            return null;
        }
    }
    
}

