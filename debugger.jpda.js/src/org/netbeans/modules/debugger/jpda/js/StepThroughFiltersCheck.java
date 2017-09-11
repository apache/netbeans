/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
