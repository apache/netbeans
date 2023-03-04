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
package org.netbeans.spi.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * Merges multiple NbActions or their stubs.
 *
 * @author Tim Boudreau
 */
final class MergeAction extends NbAction implements PropertyChangeListener {
    private NbAction[] actions;
    private Map<String, Object> knownValues = new HashMap<String, Object>();
    private Action delegateAction;
    private volatile boolean enabled;
    final boolean allowOnlyOne;

    public MergeAction(NbAction[] actions, boolean allowOnlyOne) {
        this.actions = actions;
        this.allowOnlyOne = allowOnlyOne;
        assert actions.length > 0;
        assert new HashSet<Action>(Arrays.asList(actions)).size() == actions.length :
            "Duplicate actions in " + Arrays.asList(actions);
        for (int i = 0; i < actions.length; i++) {
            Parameters.notNull("Action " + i, actions[i]); //NOI18N
        }
        //prime our key set common keys
        knownValues.put(NAME, null);
        knownValues.put(ACCELERATOR_KEY, null);
        knownValues.put(LONG_DESCRIPTION, null);
        knownValues.put(SMALL_ICON, null);
        knownValues.put(SHORT_DESCRIPTION, null);
        knownValues.put(LONG_DESCRIPTION, null);
        knownValues.put(SMALL_ICON, null);
        knownValues.put(MNEMONIC_KEY, null);
        knownValues.put("noIconInMenu", null);
        knownValues.put(PROP_ENABLED, null);
    }

    public MergeAction(NbAction[] actions) {
        this (actions, false);
    }

    @Override
    public boolean equals(Object o) {
        return o != null && MergeAction.class == o.getClass() && Arrays.equals(((MergeAction)o).actions, actions);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(actions);
    }

    Action updateDelegateAction() {
        synchronized (this) {
            return setDelegateAction(findEnabledAction());
        }
    }

    Action setDelegateAction(Action a) {
        assert Thread.holdsLock(this);
        synchronized (this) {
            Action old = this.delegateAction;
            if (old != a) {
                delegateAction = a;
                boolean nowEnabled = getDelegateAction().isEnabled();
                if (nowEnabled != enabled) {
                    enabled = nowEnabled;
                    firePropertyChange(PROP_ENABLED, !enabled, enabled);
                }
                if (a != null) {
                    sievePropertyChanges();
                }
            }
        }
        return a;
    }

    Action findEnabledAction() {
        Action result = null;
        int enaCount = 0;
        //We want to, if necessary, briefly addNotify() the action,
        //so run it inside an ActionRunnable.
        ActionRunnable<Boolean> ar = new ActionRunnable<Boolean>() {

            public Boolean run(NbAction a) {
                return a.isEnabled();
            }
        };
        for (NbAction a : actions) {
            if (runActive(ar, a)) {
                enaCount++;
                if (!allowOnlyOne) {
                    result = a;
                    break;
                } else if (result == null) {
                    result = a;
                }
            }
        }
        if (allowOnlyOne && enaCount > 1 && result != null) {
            result = null;
        }
        return result;
    }

    Action getDelegateAction() {
        Action result = null;
        synchronized (this) {
            result = delegateAction;
            if (result == null || !result.isEnabled()) {
                if (attached()) {
                    result = updateDelegateAction();
                } else {
                    result = findEnabledAction();
                }
            }
        }
        if (result == null) {
            result = actions[0];
        }
        return result;
    }

    private void sievePropertyChanges() {
        Map<String, Object> nue = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : knownValues.entrySet()) {
            String key = entry.getKey();
            Object expected = entry.getValue();
            Object found = getValue(key);//del.getValue(key);
            if (found != expected) {
                nue.put(key, found);
                firePropertyChange(key, expected, found);
            }
        }
    }

    @Override
    protected synchronized void addNotify() {
        for (Action a : actions) {
            a.addPropertyChangeListener(this);
        }
        updateDelegateAction();
    }

    @Override
    protected synchronized void removeNotify() {
        for (Action a : actions) {
            a.removePropertyChangeListener(this);
        }
        setDelegateAction(null);
    }

    @Override
    protected NbAction internalCreateContextAwareInstance(Lookup actionContext) {
        NbAction[] stubs = new NbAction[actions.length];
        for (int i = 0; i < stubs.length; i++) {
            stubs[i] = (NbAction)
                    actions[i].createContextAwareInstance(actionContext);
        }
        MergeAction result = new MergeAction(stubs, allowOnlyOne);
        result.knownValues.putAll(knownValues);
        result.pairs.putAll(pairs);
        return result;
    }

    @Override
    public Object getValue(final String key) {
        Object result = super.getValue(key);
        if (result == null) {
            if (isEnabled()) {
                Action del = getDelegateAction();
                result = del.getValue(key);
            }
        }
        if (result == null) {
            ActionRunnable<Object> ar = new ActionRunnable<Object>() {
                public Object run(NbAction a) {
                    return a.getValue(key);
                }
            };
            for (NbAction a : actions) {
                result = runActive(ar, a);
                if (result != null) {
                    break;
                }
            }
        }
        this.knownValues.put(key, result);
        return result;
    }
    boolean logged;

    @Override
    public void putValue(String key, Object value) {
        if (!logged) {
            Logger.getLogger(MergeAction.class.getName()).log(Level.INFO,
                    "putValue (" + key + ',' + value + //NOI18N
                    "called on merged action.  This is probably a mistake."); //NOI18N
        }
        super.putValue(key, value);
    }

    public boolean isEnabled() {
        boolean result = updateEnabled();
        return result;
    }

    boolean updateEnabled() {
        enabled = getDelegateAction().isEnabled();
        if (allowOnlyOne && enabled) {
            if (findEnabledAction() == null) {
                enabled = false;
            }
        }
        return enabled;
    }

    public void actionPerformed(ActionEvent e) {
        Action a = getDelegateAction();
        if (a == null) {
            throw new IllegalStateException("Not enabled or no delegate: " + //NOI18N
                    this);
        }
        a.actionPerformed(e);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        boolean old = enabled;
        synchronized (this) {
            if (attached()) {
                updateDelegateAction();
            }
        }
        boolean nowEnabled = isEnabled();
        if (!PROP_ENABLED.equals(evt.getPropertyName())) {
            Object last = knownValues.get(evt.getPropertyName());
            Object mine = getValue(evt.getPropertyName());
            if (mine != last) {
                firePropertyChange(evt.getPropertyName(), last, mine);
            }
            knownValues.put(evt.getPropertyName(), evt.getNewValue());
        }
        if (old != nowEnabled) {
            firePropertyChange(PROP_ENABLED, old, nowEnabled);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder (super.toString());
        sb.append ('['); //NOI18N
        for (int i = 0; i < actions.length; i++) {
            sb.append (actions[i]);
            if (i != actions.length - 1) {
                sb.append (','); //NOI18N
            }
        }
        sb.append(']'); //NOI18N
        return sb.toString();
    }
}
