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
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;

/** A delegate action that is usually associated with a specific lookup and
 * listens on certain classes to appear and disappear there.
 */
final class ContextAction<T> extends Object 
implements Action, ContextAwareAction {
    //, Presenter.Menu, Presenter.Popup, Presenter.Toolbar, PropertyChangeListener {
    /** type to check */
    private final Class<T> type;
    /** selection mode */
    final ContextSelection selectMode;
    /** performer to call */
    private final ContextAction.Performer<? super T> performer;

    /** global lookup to work with */
    private final ContextManager global;

    /** support for listeners */
    private PropertyChangeSupport support;

    /** was this action enabled or not*/
    private boolean previousEnabled;

    /** Constructs new action that is bound to given context and
     * listens for changes of <code>ActionMap</code> in order to delegate
     * to right action.
     */
    public ContextAction(
        ContextAction.Performer<? super T> performer, 
        ContextSelection selectMode,
        Lookup actionContext, 
        Class<T> type,
        boolean surviveFocusChange
    ) {
        if (performer == null) {
            throw new NullPointerException("Has to provide a key!"); // NOI18N
        }
        this.type = type;
        this.selectMode = selectMode;
        this.performer = performer;
        this.global = ContextManager.findManager(actionContext, surviveFocusChange);
    }

    /** Overrides superclass method, adds delegate description. */
    @Override
    public String toString() {
        return super.toString() + "[type=" + type + ", performer=" + performer + "]"; // NOI18N
    }

    /** Invoked when an action occurs.
     */
    public void actionPerformed(final java.awt.event.ActionEvent e) {
        global.actionPerformed(e, performer, type, selectMode);
    }

    public boolean isEnabled() {
        assert EventQueue.isDispatchThread();
        boolean r = global.isEnabled(type, selectMode, performer);
        previousEnabled = r;
        return r;
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        boolean first = false;
        if (support== null) {
            support = new PropertyChangeSupport(this);
            first = true;
        }
        support.addPropertyChangeListener(listener);
        if (first) {
            global.registerListener(type, this);
        }
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        if( null != support ) {
            support.removePropertyChangeListener(listener);
            if (!support.hasListeners(null)) {
                global.unregisterListener(type, this);
                support = null;
            }
        }
    }

    public void putValue(String key, Object o) {
    }

    public Object getValue(String key) {
        if ("enabler".equals(key)) { // NOI18N
            // special API to support re-enablement
            assert EventQueue.isDispatchThread();
            updateState();
        }
        return null;
    }

    public void setEnabled(boolean b) {
    }

    void updateState() {
        PropertyChangeSupport s;
        synchronized (this) {
            s = support;
        }
        boolean prev = previousEnabled;
        if (s != null && prev != isEnabled()) {
            s.firePropertyChange("enabled", Boolean.valueOf(prev), Boolean.valueOf(!prev)); // NOI18N
        }
    }

    /** Clones itself with given context.
     */
    public Action createContextAwareInstance(Lookup actionContext) {
        return new ContextAction<T>(performer, selectMode, actionContext, type, global.isSurvive());
    }

    @Override
    public int hashCode() {
        int t = type.hashCode();
        int m = selectMode.hashCode();
        int p = performer.hashCode();
        return (t << 2) + (m << 1) + p;
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
                performer.equals(c.performer);
        }
        return false;
    }

    static class Performer<Data> {
        final Map delegate;

        public Performer(Map delegate) {
            this.delegate = delegate;
        }

        public Performer(
            ContextActionPerformer<Data> p,
            ContextActionEnabler<Data> e
        ) {
            Map<Object, Object> map = new HashMap<Object, Object>();
            map.put("delegate", p);
            map.put("enabler", e);
            this.delegate = map;
        }

        @SuppressWarnings("unchecked")
        public void actionPerformed(
            ActionEvent ev, List<? extends Data> data, Lookup.Provider everything
        ) {
            Object obj = delegate.get("delegate"); // NOI18N
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
            if (obj instanceof ContextAwareAction) {
                Action a = ((ContextAwareAction)obj).createContextAwareInstance(everything.getLookup());
                a.actionPerformed(ev);
                return;
            }
                
            GeneralAction.LOG.warning("No 'delegate' for " + delegate); // NOI18N
        }
        @SuppressWarnings("unchecked")
        public boolean enabled(List<? extends Object> data) {
            Object o = delegate.get("enabler"); // NOI18N
            if (o == null) {
                return true;
            }

            if (o instanceof ContextActionEnabler) {
                ContextActionEnabler<Object> en = (ContextActionEnabler<Object>)o;
                return en.enabled(data);
            }

            GeneralAction.LOG.warning("Wrong enabler for " + delegate + ":" + o);
            return false;
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
                Performer l = (Performer)obj;
                return delegate.equals(l.delegate);
            }
            return false;
        }
        
    }
}

