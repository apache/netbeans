/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.openide.awt;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import org.openide.awt.ContextAction.Performer;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.ActionInvoker;

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
        return new ContextAction<T>(perf, selectionType, context, dataType, false);
    }
    
    public static ContextAwareAction context(Map map) {
        Class<?> dataType = readClass(map.get("type")); // NOI18N
        return new DelegateAction(map, _context(map, dataType, Utilities.actionsGlobalContext()));
    }
    public static Action bindContext(Map map, Lookup context) {
        Class<?> dataType = readClass(map.get("type")); // NOI18N
        return new BaseDelAction(map, _context(map, dataType, context));
    }
    private static <T> ContextAwareAction _context(Map map, Class<T> dataType, Lookup context) {
        ContextSelection sel = readSelection(map.get("selectionType")); // NOI18N
        Performer<T> perf = new Performer<T>(map);
        boolean survive = Boolean.TRUE.equals(map.get("surviveFocusChange")); // NOI18N
        return new ContextAction<T>(
            perf, sel, context, dataType, survive
        );
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
    private static Class<?> readClass(Object obj) {
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
        throw new IllegalStateException("Cannot read 'type' value: " + obj); // NOI18N
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
        final PropertyChangeListener weakL;
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
            return super.toString() + "[key=" + key + "]"; // NOI18N
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
            if (support== null) {
                support = new PropertyChangeSupport(this);
                first = true;
            }
            support.addPropertyChangeListener(listener);
            if (first) {
                global.registerListener(key, this);
            }
        }

        public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
            if( support != null ) {
                support.removePropertyChangeListener(listener);
                if (!support.hasListeners(null)) {
                    global.unregisterListener(key, this);
                    support = null;
                }
            }
        }


        public void putValue(String key, Object value) {
            if (attrs == null) {
                attrs = new HashMap<String,Object>();
            }
            attrs.put(key, value);
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

        /** Clones itself with given context.
         */
        public Action createContextAwareInstance(Lookup actionContext) {
            Action f = fallback;
            if (f instanceof ContextAwareAction) {
                f = ((ContextAwareAction)f).createContextAwareInstance(actionContext);
            }
            DelegateAction other = new DelegateAction(map, key, actionContext, f, global.isSurvive(), async);
            if (attrs != null) {
                other.attrs = new HashMap<String,Object>(attrs);
            }
            return other;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if ("enabled".equals(evt.getPropertyName())) { // NOI18N
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
