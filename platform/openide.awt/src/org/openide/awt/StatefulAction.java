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

import java.util.Collections;
import java.util.Objects;
import java.util.logging.Level;
import javax.swing.Action;
import org.openide.util.Lookup;

/**
 * Represents stateful context-aware action.
 * @author sdedic
 */
final class StatefulAction<T> extends ContextAction<T> {
    /**
     * Monitor for "checked" property
     */
    private final StatefulMonitor checkValueMonitor;
    
    /**
     * The last selected value.
     */
    private boolean selValue;
    
    /**
     * Tracks first attach
     */
    private boolean first = true;
    
    public StatefulAction(Performer performer, ContextSelection selectMode, Lookup actionContext, Class type, boolean surviveFocusChange, 
            StatefulMonitor enableMonitor, StatefulMonitor valueMonitor) {
        super(performer, selectMode, actionContext, type, surviveFocusChange, enableMonitor);
        this.checkValueMonitor = valueMonitor;
    }

    @Override
    void updateStateProperties() {
        super.updateStateProperties();
        if (!wasEnabled()) {
            LOG.log(Level.FINE, "Action {0} disabled, unchecked", this);
            putValue(SELECTED_KEY, false);
            return;
        }
        boolean nowState = fetchStateValue();
        boolean oldState = this.selValue;
        this.selValue = nowState;
        LOG.log(Level.FINE, "Action {0}: old check state {1}, new check state {2}", new Object[] { 
            this, oldState, nowState
        });
        firePropertyChange(SELECTED_KEY, oldState, nowState);
    }
    
    private boolean fetchStateValue() {
        first = false;
        if (checkValueMonitor.getType() == Action.class) {
            return global.runEnabled(type, selectMode, (all, everything) -> {
                return checkValueMonitor.enabled(
                        Collections.singletonList(performer.delegate(everything, all)),
                        () -> (Action)performer.delegate(everything, all));
            });
        } else {
            return global.runEnabled(checkValueMonitor.getType(), selectMode, (all, everything) -> {
                return checkValueMonitor.enabled(all, () -> (Action)performer.delegate(everything, all));
            });
        }
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 97 * hash + Objects.hashCode(this.checkValueMonitor);
        return hash;
    }

    /**
     * Overrides ContextAction. Must found in the {@link #checkValueMonitor} otherwise
     * two actions driven by different property could clash in {@link ContextManager}
     * and won't get 'enable' notification when data appears.
     * 
     * @param obj other object
     * @return true, if equal
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        final StatefulAction<?> other = (StatefulAction<?>) obj;
        if (!Objects.equals(this.checkValueMonitor, other.checkValueMonitor)) {
            return false;
        }
        return true;
    }

    @Override
    public Object getValue(String key) {
        if (SELECTED_KEY.equals(key)) {
            LOG.log(Level.FINER, "Action {0} state: {1}", new Object[] {
                this, selValue
            });
            return selValue;
        }
        return super.getValue(key);
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        StatefulMonitor checkMon = checkValueMonitor.createContextMonitor(actionContext);
        StatefulMonitor enableMon = enableMonitor == null ? null : enableMonitor.createContextMonitor(actionContext);
        Action a = new StatefulAction<>(performer, 
                selectMode, 
                actionContext, 
                type, 
                global.isSurvive(),
                enableMon,
                checkMon);
        LOG.log(Level.FINE, "Created context Stateful instance: {0} from {1}, check monitor {2}, enable monitor {3}", new Object[] {
            a, this, checkMon, enableMon
        });
        return a;
    }

    @Override
    void clearState() {
        super.clearState();
        checkValueMonitor.clear();
    }

    @Override
    protected void stopListeners() {
        Class c = checkValueMonitor.getType();
        if (c != Action.class) {
            global.unregisterListener(checkValueMonitor.getType(), this);
        }
        checkValueMonitor.removeChangeListener(this);
        super.stopListeners(); 
    }

    @Override
    protected void startListeners() {
        super.startListeners();
        Class c = checkValueMonitor.getType();
        if (c != Action.class) {
            global.registerListener(checkValueMonitor.getType(), this);
        }
        if (first) {
            selValue = fetchStateValue();
        }
        checkValueMonitor.addChangeListener(this);
    }
}
