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

package org.netbeans.modules.debugger.jpda.ui.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.openide.util.RequestProcessor;


/**
* Representation of a debugging session.
*
* @author   Jan Jancura
* @author  Marian Petras
*/
abstract class JPDADebuggerAction extends ActionsProviderSupport implements
PropertyChangeListener {

    private JPDADebugger debugger;

    JPDADebuggerAction (JPDADebugger debugger) {
        this.debugger = debugger;
        debugger.addPropertyChangeListener (debugger.PROP_STATE, this);
    }
    
    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        try {
            checkEnabled (debugger.getState ());
        } catch (com.sun.jdi.VMDisconnectedException e) {
            // Causes kill action when something is being evaluated
        }
    }
    
    protected abstract void checkEnabled (int debuggerState);

    private boolean canApplyLazyEnabled = false;

    /**
     * Call this from {@link #checkEnabled(int)} when the code needs to be run outside of AWT.
     * Override {@link #checkEnabledLazyImpl(int)} method, which will be called
     * in the provided RequestProcessor. The returned enabled status is set through
     * {@link #setEnabledSingleAction(boolean)}.
     * <p>
     * Do not call {@link #setEnabled(java.lang.Object, boolean)} method! When
     * you also need to set the status directly, use {@link #setEnabledSingleAction(boolean)},
     * which correctly cooperates with the lazy code.
     * 
     * @param debuggerState
     * @param rp
     */
    protected final void checkEnabledLazySingleAction(final int debuggerState, RequestProcessor rp) {
        canApplyLazyEnabled = true;
        rp.post(new Runnable() {
            @Override
            public void run() {
                final boolean enabled = checkEnabledLazyImpl(debuggerState);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (canApplyLazyEnabled) {
                            setEnabledSingleAction(enabled);
                            canApplyLazyEnabled = true; // In case there were several lazy invocations
                        }
                    }
                });
            }
        });
    }

    /** Do not call setEnabled(), return the enabled state instead. */
    protected boolean checkEnabledLazyImpl (int debuggerState) {
        return false;
    }

    protected final void setEnabledSingleAction(boolean enabled) {
        canApplyLazyEnabled = false;
        setEnabled(getActions().iterator().next(), enabled);
    }

    JPDADebugger getDebuggerImpl () {
        return debugger;
    }
}
