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

import java.awt.Component;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JMenuItem;
import org.openide.awt.ContextAction.Performer;
import org.openide.awt.ContextAction.StatefulMonitor;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.ActionInvoker;
import org.openide.util.actions.ActionPresenterProvider;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Jaroslav Tulach
 */
final class GeneralAction {

    /** Creates a new instance of DelegatingAction */
    private GeneralAction() {
    }
    
    static final Logger LOG = Logger.getLogger(GeneralAction.class.getName());
    
    public static ContextAwareAction callback(
        String key, Action defaultDelegate, Lookup context, boolean surviveFocusChange, boolean async
    ) {
        if (key == null) {
            throw new NullPointerException();
        }
        return new DelegateAction(null, key, context, defaultDelegate, surviveFocusChange, async);
    }
    
    public static Action alwaysEnabled(Map map) {
        return new AlwaysEnabledAction(map);
    }

    public static ContextAwareAction callback(Map map) {
        Action fallback = (Action)map.get("fallback");
        DelegateAction d = new DelegateAction(map, fallback);
        Parameters.notNull("key", d.key);
        return d;
    }

    public static <T> ContextAwareAction context(
        ContextAction.Performer<? super T> perf,
        ContextSelection selectionType, 
        Lookup context, 
        Class<T> dataType
    ) {
        return new ContextAction<T>(perf, selectionType, context, dataType, false, null);
    }
    
    public static ContextAwareAction context(Map map) {
        return context(map, false);
    }
    
    static ContextAwareAction context(Map map, boolean instanceReady) {
        Class<?> dataType = readClass(map.get("type")); // NOI18N
        ContextAwareAction ca = _context(map, dataType, Utilities.actionsGlobalContext(), instanceReady);
        // autodetect on/off actions
        if (ca.getValue(Action.SELECTED_KEY) != null) {
            return new StateDelegateAction(map, ca);
        } else {
            return new DelegateAction(map, ca);
        }
    }
    
    public static Action bindContext(Map map, Lookup context) {
        Class<?> dataType = readClass(map.get("type")); // NOI18N
        return new BaseDelAction(map, _context(map, dataType, context, false));
    }
    
    private static <T> ContextAwareAction _context(Map map, Class<T> dataType, Lookup context, boolean instanceReady) {
        ContextSelection sel = readSelection(map.get("selectionType")); // NOI18N
        Performer<T> perf = new Performer<T>(map);
        boolean survive = Boolean.TRUE.equals(map.get("surviveFocusChange")); // NOI18N
        StatefulMonitor enableMonitor = null;
        StatefulMonitor checkMonitor = null;
        Class enableType = tryReadClass(map.get("enableOnType"));
        if (enableType == null) {
            enableType = dataType;
        }
        Object del = map.get("enableOnActionProperty");
        Object o = map.get("enableOnProperty"); // NOI18N
        
        if (o instanceof String || (o == null && (del instanceof String))) {
            enableMonitor = new PropertyMonitor(enableType, (String)o, "enableOn", map);
        }
        o = map.get("checkedOnProperty"); // NOI18N
        if (o instanceof String) {
            Class c = tryReadClass(map.get("checkedOnType")); // NOI18N
            if (c != null) {
                checkMonitor = new PropertyMonitor(c, (String)o, "checkedOn", map);
            }
        }
        // special case to hook on existing action instances
        if (instanceReady) { // NOI18N
            enableMonitor = new PropertyMonitor(Action.class, "enabled"); // NOI18N
            Object ao = map.get("delegate");
            if (ao instanceof Action) {
                if (((Action)ao).getValue(Action.SELECTED_KEY) != null) {
                    checkMonitor = new PropertyMonitor(Action.class, Action.SELECTED_KEY);
                }
            }
        }
        
        ContextAction a;
        
        if (checkMonitor == null) {
            a = new ContextAction<T>(
                perf, sel, context, dataType, survive, enableMonitor
            );
        } else {
            a = new StatefulAction<>(perf, sel, context, dataType, survive, enableMonitor, checkMonitor);
            LOG.log(Level.FINE, "Created stateful delegate for {0}, instance {1}, value monitor {2}", 
                    new Object[] { map, a, checkMonitor });
        }
        
        return a;
    }
    
    private static ContextSelection readSelection(Object obj) {
        if (obj instanceof ContextSelection) {
            return (ContextSelection)obj;
        }
        if (obj instanceof String) {
            return ContextSelection.valueOf((String)obj);
        }
        throw new IllegalStateException("Cannot parse 'selectionType' value: " + obj); // NOI18N
    }
    
    static Class<?> readClass(Object obj) {
        Class<?> r = tryReadClass(obj);
        if (r == null) {
            throw new IllegalStateException("Cannot read 'type' value: " + obj); // NOI18N   
        }
        return r;
    }
    
    static Class<?> tryReadClass(Object obj) {
        if (obj instanceof Class) {
            return (Class)obj;
        }
        if (obj instanceof String) {
            ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
            if (l == null) {
                l = Thread.currentThread().getContextClassLoader();
            }
            if (l == null) {
                l = GeneralAction.class.getClassLoader();
            }
            try {
                return Class.forName((String)obj, false, l);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        return null;
    }
    static final Object extractCommonAttribute(Map fo, Action action, String name) {
        return AlwaysEnabledAction.extractCommonAttribute(fo, name);
    }

    public Logger getLOG() {
        return LOG;
    }
    
    /** A delegate action that is usually associated with a specific lookup and
     * extract the nodes it operates on from it. Otherwise it delegates to the
     * regular NodeAction.
     */
    static final class DelegateAction extends BaseDelAction 
    implements ContextAwareAction {
        public DelegateAction(Map map, Object key, Lookup actionContext, Action fallback, boolean surviveFocusChange, boolean async) {
            super(map, key, actionContext, fallback, surviveFocusChange, async);
        }

        public DelegateAction(Map map, Action fallback) {
            super(map, fallback);
        }
    } // end of DelegateAction
    
    /**
     * Specialization that handles {@link #SELECTED_KEY} action value. Delegats to either the {@link #fallback} or the
     * action delegated to by the {@link #key}. Uses toggle button as Toolbar presenter and checkbox as menu presenter.
     */
    static final class StateDelegateAction extends BaseDelAction implements ContextAwareAction, 
            Presenter.Toolbar, Presenter.Menu, Presenter.Popup, PropertyChangeListener {

        public StateDelegateAction(Map map, Object key, Lookup actionContext, Action fallback, boolean surviveFocusChange, boolean async) {
            super(map, key, actionContext, fallback, surviveFocusChange, async);
            putValue(SELECTED_KEY, fallback.getValue(SELECTED_KEY));
        }

        public StateDelegateAction(Map map, Action fallback) {
            super(map, fallback);
        }
        
        @Override
        public Component getToolbarPresenter() {
            return ActionPresenterProvider.getDefault().createToolbarPresenter(this);
        }

        @Override
        public JMenuItem getMenuPresenter() {
            return ActionPresenterProvider.getDefault().createMenuPresenter(this);
        }

        @Override
        public JMenuItem getPopupPresenter() {
            return ActionPresenterProvider.getDefault().createPopupPresenter(this);
        }

        @Override
        void updateState(ActionMap prev, ActionMap now, boolean fire) {
            super.updateState(prev, now, fire); 
            if (key == null) {
                return;
            }
            Action pa = prev.get(key);
            Action na = now.get(key);
            if (pa == na) {
                return;
            }
            Boolean os;
            Boolean ns;
            if (pa != null) {
                os = Boolean.TRUE.equals(pa.getValue(SELECTED_KEY));
            } else {
                os = Boolean.TRUE.equals(fallback.getValue(SELECTED_KEY));
            }
            if (na != null) {
                ns = Boolean.TRUE.equals(na.getValue(SELECTED_KEY));
            } else {
                ns = Boolean.TRUE.equals(fallback.getValue(SELECTED_KEY));
            }
            if (os != ns) {
                putValue(SELECTED_KEY, ns);
            }
        }


        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            super.propertyChange(evt);
            if (SELECTED_KEY.equals(evt.getPropertyName())) {
                Object o = evt.getNewValue();
                putValue(SELECTED_KEY, o != null ? fallback.getValue(SELECTED_KEY) : o);
            }
        }

        @Override
        protected BaseDelAction copyDelegate(Action f, Lookup actionContext) {
            return new StateDelegateAction(map, key, actionContext, f, global.isSurvive(), async);
        }
    }
    
    static class BaseDelAction extends Object 
    implements Action, PropertyChangeListener {
        /** file object, if we are associated to any */
        final Map map;
        /** action to delegate too */
        final Action fallback;
        /** key to delegate to */
        final Object key;
        /** are we asynchronous? */
        final boolean async;

        /** global lookup to work with */
        final GlobalManager global;

        /** support for listeners */
        private PropertyChangeSupport support;

        /** listener to check listen on state of action(s) we delegate to */
        PropertyChangeListener weakL;
        Map<String,Object> attrs;
        
        /** Constructs new action that is bound to given context and
         * listens for changes of <code>ActionMap</code> in order to delegate
         * to right action.
         */
        protected BaseDelAction(Map map, Object key, Lookup actionContext, Action fallback, boolean surviveFocusChange, boolean async) {
            this.map = map;
            this.key = key;
            this.fallback = fallback;
            this.global = GlobalManager.findManager(actionContext, surviveFocusChange);
            this.weakL = WeakListeners.propertyChange(this, fallback);
            this.async = async;
            if (fallback != null) {
                LOG.log(Level.FINER, "Action {0}: Attaching propchange to {1}", new Object[] {
                    this, fallback
                });
                fallback.addPropertyChangeListener(weakL);
            }
        }
        
        protected BaseDelAction(Map map, Action fallback) {
            this(
                map,
                map.get("key"), // NOI18N
                Utilities.actionsGlobalContext(), // NOI18N
                fallback, // NOI18N
                Boolean.TRUE.equals(map.get("surviveFocusChange")), // NOI18N
                Boolean.TRUE.equals(map.get("asynchronous")) // NOI18N
            );
        }

        /** Overrides superclass method, adds delegate description. */
        @Override
        public String toString() {
            return super.toString() + "[key=" + key + ", map=" + map + "]"; // NOI18N
        }

        /** Invoked when an action occurs.
         */
        public void actionPerformed(final java.awt.event.ActionEvent e) {
            assert EventQueue.isDispatchThread();
            final javax.swing.Action a = findAction();
            if (a != null) {
                ActionInvoker.invokeAction(a, e, async, null);
            }
        }

        public boolean isEnabled() {
            assert EventQueue.isDispatchThread();
            javax.swing.Action a = findAction();
            return a == null ? false : a.isEnabled();
        }
        
        public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
            boolean first = false;
            if (support == null) {
                support = new PropertyChangeSupport(this);
                first = true;
            }
            support.addPropertyChangeListener(listener);
            if (first) {
                LOG.log(Level.FINER, "Action {0}: Adding global listener for key {1}", new Object[]{this, key});
                global.registerListener(key, this);
            }
        }

        public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
            if (support != null) {
                support.removePropertyChangeListener(listener);
                if (!support.hasListeners(null)) {
                    global.unregisterListener(key, this);
                    LOG.log(Level.FINER, "Action {0}: Removed global listener for key {1}", new Object[]{this, key});
                    support = null;
                }
            }
        }

        public void putValue(String key, Object value) {
            if (attrs == null) {
                attrs = new HashMap<String,Object>();
            }
            PropertyChangeSupport s;
            
            synchronized (this) {
                s = support;
            }
            Object old = null;
            if (s != null) {
                old = getValue(key);
            }
            attrs.put(key, value);
            if (s != null) {
                s.firePropertyChange(key, old, old != null ? value : null);
            }
        }

        public Object getValue(String key) {
            if (attrs != null && attrs.containsKey(key)) {
                return attrs.get(key);
            }
            Object ret = GeneralAction.extractCommonAttribute(map, this, key);
            if (ret != null) {
                return ret;
            }
            
            Action a = findAction();
            return a == null ? null : a.getValue(key);
        }

        public void setEnabled(boolean b) {
        }

        void updateState(ActionMap prev, ActionMap now, boolean fire) {
            if (key == null) {
                return;
            }

            boolean prevEnabled = false;
            if (prev != null) {
                Action prevAction = prev.get(key);
                if (prevAction != null) {
                    prevEnabled = fire && prevAction.isEnabled();
                    prevAction.removePropertyChangeListener(weakL);
                }
            }
            if (now != null) {
                Action nowAction = now.get(key);
                boolean nowEnabled;
                if (nowAction != null) {
                    nowAction.addPropertyChangeListener(weakL);
                    nowEnabled = nowAction.isEnabled();
                } else {
                    nowEnabled = fallback != null && fallback.isEnabled();
                }
                PropertyChangeSupport sup = fire ? support : null;
                if (sup != null && nowEnabled != prevEnabled) {
                    sup.firePropertyChange("enabled", prevEnabled, !prevEnabled); // NOI18N
                }
            }
        }

        /*** Finds an action that we should delegate to
         * @return the action or null
         */
        private Action findAction() {
            Action a = global.findGlobalAction(key);
            return a == null ? fallback : a;
        }

        protected BaseDelAction copyDelegate(Action f, Lookup actionContext) {
            return new DelegateAction(map, key, actionContext, f, global.isSurvive(), async);
        }
        
        /** Clones itself with given context.
         */
        public Action createContextAwareInstance(Lookup actionContext) {
            Action f = fallback;
            if (f instanceof ContextAwareAction) {
                f = ((ContextAwareAction)f).createContextAwareInstance(actionContext);
            }
            BaseDelAction other = copyDelegate(f, actionContext);
            if (attrs != null) {
                if (other.attrs == null) {
                    other.attrs = new HashMap<>(attrs);
                } else {
                    for (String k : attrs.keySet()) {
                        other.attrs.putIfAbsent(k, attrs.get(k));
                    }
                }
            }
            return other;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if ("enabled".equals(evt.getPropertyName())) { // NOI18N
                LOG.log(Level.FINE, "Action {0}: got property change from fallback {1}", new Object[] { this, fallback });
                PropertyChangeSupport sup;
                synchronized (this) {
                    sup = support;
                }
                if (sup != null) {
                    sup.firePropertyChange("enabled", evt.getOldValue(), evt.getNewValue()); // NOI18N
                }
            }
        }

        @Override
        public int hashCode() {
            int k = key == null ? 37 : key.hashCode();
            int m = map == null ? 17 : map.hashCode();
            int f = fallback == null ? 7 : fallback.hashCode();
            
            return (k << 2) + (m << 1) + f;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof DelegateAction) {
                DelegateAction d = (DelegateAction)obj;
                
                if (key != null && !key.equals(d.key)) {
                    return false;
                }
                if (map != null && !map.equals(d.map)) {
                    return false;
                }
                if (fallback != null && !fallback.equals(d.fallback)) {
                    return false;
                }
                return true;
            }
            return false;
        }
    }   // end of DelegateAction
}
