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

package org.openide.awt;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Mutex;

/** A delegate action that is usually associated with a specific lookup and
 * listens on certain classes to appear and disappear there.
 */
class ContextAction<T> extends Object
implements Action, ContextAwareAction, ChangeListener, Runnable {
    //, Presenter.Menu, Presenter.Popup, Presenter.Toolbar, PropertyChangeListener {
    static final Logger LOG = Logger.getLogger(ContextAction.class.getName());

    /** type to check */
    final Class<T> type;
    /** selection mode */
    final ContextSelection selectMode;
    /** performer to call */
    final ContextAction.Performer<T> performer;
    /** performer factory to create copies of this action */
    final Supplier<ContextAction.Performer<T>> performerSupplier;

    /** global lookup to work with */
    final ContextManager global;

    /** support for listeners */
    private PropertyChangeSupport support;

    /** was this action enabled or not*/
    private boolean previousEnabled;

    protected final StatefulMonitor enableMonitor;

    /** Constructs new action that is bound to given context and
     * listens for changes of <code>ActionMap</code> in order to delegate
     * to right action.
     */
    public ContextAction(
        Supplier<ContextAction.Performer<T>> performerSupplier,
        ContextSelection selectMode,
        Lookup actionContext,
        Class<T> type,
        boolean surviveFocusChange, StatefulMonitor enableMonitor
    ) {
        if (performerSupplier == null) {
            throw new NullPointerException("Has to performerSupplier a key!"); // NOI18N
        }
        this.type = type;
        this.selectMode = selectMode;
        this.performerSupplier = performerSupplier;
        this.performer = performerSupplier.get();
        this.global = ContextManager.findManager(actionContext, surviveFocusChange);
        this.enableMonitor = enableMonitor;
        if (enableMonitor != null) {
            LOG.log(Level.FINE, "Setting enable monitor {0}: {1}", new Object[] {
                    this, enableMonitor} );
        }
    }

    /** Overrides superclass method, adds delegate description. */
    @Override
    public String toString() {
        return super.toString() + "[type=" + type + ", performer=" + performer + "]"; // NOI18N
    }

    /** Invoked when an action occurs.
     */
    @Override
    public void actionPerformed(final java.awt.event.ActionEvent e) {
        global.actionPerformed(e, performer, type, selectMode);
    }

    @Override
    public boolean isEnabled() {
        assert EventQueue.isDispatchThread();
        boolean r;
        if (enableMonitor != null) {
            r = fetchEnabledValue();
        } else {
            r = global.isEnabled(type, selectMode, performer);
        }
        previousEnabled = r;
        return r;
    }

    private boolean fetchEnabledValue() {
        return global.runEnabled(type, selectMode, (all, everything) -> {
            Supplier<Action> af = () -> (Action)performer.delegate(everything, all);
            if (enableMonitor.getType() == Action.class) {
                // special case for monitoring the action itself
                Action dele = (Action)performer.delegate(everything, all);
                // delegate to the action
                return enableMonitor.enabled(Collections.singletonList(dele), () -> dele);
            } else if (enableMonitor.getType() != type) {
                return global.runEnabled(enableMonitor.getType(), selectMode,
                    (all2, everything2) -> {
                        // run enable monitor for the other type and the original action
                        return enableMonitor.enabled(all2, af);
                    }
                );
            } else {
                return enableMonitor.enabled(all, af);
            }
        });
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        // remap PropertyMonitor change events into EDT
        Mutex.EVENT.readAccess(this);
    }

    @Override
    public void run() {
        updateStateProperties();
    }

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        boolean first = false;
        if (support== null) {
            support = new PropertyChangeSupport(this);
            first = true;
        }
        support.addPropertyChangeListener(listener);
        if (first) {
            startListeners();
        }
    }

    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        if( null != support ) {
            support.removePropertyChangeListener(listener);
            if (!support.hasListeners(null)) {
                stopListeners();
                support = null;
            }
        }
    }

    protected void startListeners() {
        global.registerListener(type, this);
        if (enableMonitor != null) {
            fetchEnabledValue();
            enableMonitor.addChangeListener(this);
        }
    }

    protected void stopListeners() {
        global.unregisterListener(type, this);
        if (enableMonitor != null) {
            enableMonitor.removeChangeListener(this);
        }
    }

    @Override
    public void putValue(String key, Object o) {
    }

    @Override
    public Object getValue(String key) {
        if ("enabler".equals(key)) { // NOI18N
            // special API to support re-enablement
            assert EventQueue.isDispatchThread();
            updateState();
        } else if (ACTION_COMMAND_KEY.equals(key)) {
            Object o = performer.delegate.get(ACTION_COMMAND_KEY);
            if (o == null) {
                o = performer.delegate.get("key"); // NOI18N
            }
            if (o != null) {
                return o.toString();
            }
        }
        return null;
    }

    @Override
    public void setEnabled(boolean b) {
    }

    void clearState() {
        performer.clear();
        if (enableMonitor != null) {
            enableMonitor.clear();
        }
    }

    /**
     * Called from context manager, when the objects watched for in
     * Lookup change.
     */
    void updateState() {
        clearState();
        if (!isListening()) {
            return;
        }
        updateStateProperties();
    }

    void updateStateProperties() {
        boolean prev = previousEnabled;
        boolean now = isEnabled();
        if (prev != now) {
            updateEnabledState(now);
        }
    }

    boolean wasEnabled() {
        return previousEnabled;
    }

    protected boolean isListening() {
        synchronized (this) {
            return support != null;
        }
    }

    protected void firePropertyChange(String property, Boolean old, Boolean current) {
        PropertyChangeSupport s;
        synchronized (this) {
            s = support;
            if (s == null) {
                return;
            }
        }
        s.firePropertyChange(property, old, current);
    }

    protected void updateEnabledState(boolean enabled) {
        this.previousEnabled = enabled;
        firePropertyChange("enabled", !enabled, enabled); // NOI18N
    }

    /** Clones itself with given context.
     */
    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return  new ContextAction<>(performerSupplier, selectMode, actionContext, type, global.isSurvive(),
            enableMonitor == null ? null : enableMonitor.createContextMonitor(actionContext));
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, selectMode, performer, enableMonitor);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof ContextAction) {
            ContextAction c = (ContextAction)obj;

            return type.equals(c.type) &&
                selectMode.equals(c.selectMode) &&
                performer.equals(c.performer) &&
                Objects.equals(enableMonitor, c.enableMonitor);
        }
        return false;
    }

    static class Performer<Data> {
        final Map delegate;
        Reference<Object> instDelegate = null;

        public Performer(Map delegate) {
            this.delegate = delegate;
        }

        public Performer(
            ContextActionPerformer<Data> p,
            ContextActionEnabler<Data> e
        ) {
            Map<Object, Object> map = new HashMap<>();
            map.put("delegate", p); // NOI18N
            map.put("enabler", e);  // NOI18N
            this.delegate = map;
        }

        void clear() {
            Reference<Object> r = instDelegate;
            instDelegate = null;
            if (r != null) {
                Object o = r.get();
                if (o instanceof Performer) {
                    ((Performer)o).clear();
                }
            }
        }

        /**
         * Creates a delegate.
         * @param everything
         * @param data
         * @return
         */
        Object delegate(Lookup.Provider everything, List<?> data) {
            return delegate0(everything, data, true);
        }

        private Object delegate0(Lookup.Provider everything, List<?> data, boolean getAction) {
            Object d = instDelegate != null ? instDelegate.get() : null;
            if (d != null) {
                if (getAction && (d instanceof Performer)) {
                    return ((Performer)d).delegate0(everything, data, getAction);
                }
                return d;
            }
            d = createDelegate(everything, data);
            if (d != null) {
                if (getAction && (d instanceof Performer)) {
                    // WHY??????????
                    // If I'm not mistaken this is a strange way to make a
                    // WeakReference a hard reference.
                    final Object fd = d;
                    instDelegate = new WeakReference<Object>(d) { private Object hardRef = fd; };
                    return ((Performer)d).delegate0(everything, data, getAction);
                }
                if (d instanceof ContextAwareAction) {
                    d = ((ContextAwareAction)d).createContextAwareInstance(everything.getLookup());
                }
                instDelegate = new WeakReference<>(d);
            } else {
                instDelegate = null;
            }
            return d;
        }

        @SuppressWarnings("unchecked")
        public boolean enabled(List<? extends Object> data, Lookup.Provider everything) {
            Object o = delegate.get("enabler"); // NOI18N
            if (o == null) {
                return true;
            }

            if (o instanceof ContextActionEnabler) {
                ContextActionEnabler<Object> en = (ContextActionEnabler<Object>)o;
                return en.enabled(data);
            }

            GeneralAction.LOG.log(Level.WARNING, "Wrong enabler for {0}:{1}", new Object[]{delegate, o});
            return false;
        }

        protected Object createDelegate(Lookup.Provider everything, List<?> data) {
            Object obj = delegate.get("delegate"); // NOI18N
            if (obj instanceof ContextActionPerformer) {
                return obj;
            }
            if (obj instanceof Performer) {
                return obj;
            }
            if (!(obj instanceof ActionListener)) {
                GeneralAction.LOG.log(Level.WARNING, "Wrong delegate for {0}:{1}", new Object[]{delegate, obj});
            }
            return obj;
        }

        @SuppressWarnings("unchecked")
        public void actionPerformed(
            ActionEvent ev, List<? extends Data> data, Lookup.Provider everything
        ) {
            Object obj = delegate0(everything, data, false);
            if (obj instanceof ContextActionPerformer) {
                ContextActionPerformer<Data> perf = (ContextActionPerformer<Data>)obj;
                perf.actionPerformed(ev, data);
                return;
            }
            if (obj instanceof Performer) {
                Performer<Data> perf = (Performer<Data>)obj;
                perf.actionPerformed(ev, data, everything);
                return;
            }
            if (obj instanceof ActionListener) {
                ((ActionListener)obj).actionPerformed(ev);
            }
        }

        @Override
        public int hashCode() {
            return delegate.hashCode() + 117;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof Performer) {
                Performer<?> l = (Performer<?>) obj;
                return delegate.equals(l.delegate);
            }
            return false;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            Object o = delegate.get(ACTION_COMMAND_KEY);
            if (o == null) {
                o = delegate.get("key"); // NOI18N
            }
            Object d = instDelegate == null ? null : instDelegate.get();
            sb.append("Performer{id = ").append(Objects.toString(o))
                    .append(", del = ").append(Objects.toString(d))
                    .append("}");
            return sb.toString();
        }
    }

    /**
     * Interface between Performer and value monitors.
     * @param <T>
     */
    static interface StatefulMonitor<T> {
        public void clear();
        public void addChangeListener(ChangeListener l);
        public void removeChangeListener(ChangeListener l);

        /**
         * Factory interface allows first to evaluate guard conditions, then
         * query action; delays action creation.
         */
        public boolean enabled(List<? extends T> data, Supplier<Action> actionFactory);
        public Class<?> getType();
        public StatefulMonitor<T> createContextMonitor(Lookup context);
    }

}

