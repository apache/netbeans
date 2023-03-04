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
package org.netbeans.modules.debugger.jpda.js;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.Exceptions;

/**
 * When stepping into a JS function, we need to assure that we go through the stepping filters.
 *
 * @author Martin
 */
@LazyActionsManagerListener.Registration(path="netbeans-JPDASession/JS")
public class StepThroughFiltersCheck extends LazyActionsManagerListener implements PropertyChangeListener {

    private static final String STEP_THROUGH_FILTERS_PROP = "StepThroughFilters";   // NOI18N

    private final JPDADebugger debugger;
    private final Properties p;
    private boolean stepThroughFiltersWasNotSet;
    private boolean stepThroughFiltersTurnedOn;

    public StepThroughFiltersCheck(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE, this);
        p = Properties.getDefault().getProperties("debugger.options.JPDA");     // NOI18N
    }

    @Override
    public String[] getProperties() {
        return new String[] { "actionToBeRun" };                                // NOI18N
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("actionToBeRun".equals(evt.getPropertyName())) {
            Object action = evt.getNewValue();
            if (ActionsManager.ACTION_STEP_INTO.equals(action)) {
                // We need to step through the filters to get to the target function
                boolean stepThroughFilters = p.getBoolean(STEP_THROUGH_FILTERS_PROP, false);
                stepThroughFiltersWasNotSet = !stepThroughFilters && p.getBoolean(STEP_THROUGH_FILTERS_PROP, true);
                if (!stepThroughFilters) {
                    p.setBoolean(STEP_THROUGH_FILTERS_PROP, true);
                    stepThroughFiltersTurnedOn = true;
                }
            }
        } else if (JPDADebugger.PROP_STATE.equals(evt.getPropertyName())) {
            Object nv = evt.getNewValue();
            if ((nv instanceof Integer) && JPDADebugger.STATE_STOPPED == (Integer) nv) {
                unsetStepThrough();
            }
        }
    }

    @Override
    protected void destroy() {
        debugger.removePropertyChangeListener(JPDADebugger.PROP_SUSPEND, this);
        unsetStepThrough();
    }

    private void unsetStepThrough() {
        if (stepThroughFiltersTurnedOn) {
            if (stepThroughFiltersWasNotSet) {
                try {
                    java.lang.reflect.Method unsetMethod = p.getClass().getDeclaredMethod("unset", String.class);
                    unsetMethod.setAccessible(true);
                    unsetMethod.invoke(p, STEP_THROUGH_FILTERS_PROP);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                p.setBoolean(STEP_THROUGH_FILTERS_PROP, false);
            }
            stepThroughFiltersTurnedOn = false;
        }
    }

}
