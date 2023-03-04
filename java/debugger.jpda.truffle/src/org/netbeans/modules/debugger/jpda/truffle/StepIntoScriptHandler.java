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

package org.netbeans.modules.debugger.jpda.truffle;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.InvalidTypeException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.ActionsManagerListener;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.Exceptions;

/**
 * Handler of step into guest language from Java.
 */
@LazyActionsManagerListener.Registration(path="netbeans-JPDASession/Java")
public class StepIntoScriptHandler extends LazyActionsManagerListener implements PropertyChangeListener {
    
    private static final Logger LOG = Logger.getLogger(StepIntoScriptHandler.class.getCanonicalName());
    private static final String PROP_ACTION_TO_BE_RUN = "actionToBeRun";        // NOI18N
    
    private final JPDADebugger debugger;
    private ClassType serviceClass;
    private Field steppingField;
    
    public StepIntoScriptHandler(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        debugger.addPropertyChangeListener(JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME, new CurrentSFTracker());
    }

    @Override
    protected void destroy() {
        LOG.fine("\nStepIntoJSHandler.destroy()");
    }

    @Override
    public String[] getProperties() {
        return new String[] { ActionsManagerListener.PROP_ACTION_PERFORMED, PROP_ACTION_TO_BE_RUN };
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (PROP_ACTION_TO_BE_RUN.equals(evt.getPropertyName())) {
            Object action = evt.getNewValue();
            // Just before Java's step into runs:
            if (ActionsManager.ACTION_STEP_INTO.equals(action)) {
                ClassObjectReference serviceClassRef = RemoteServices.getServiceClass(debugger);
                LOG.log(Level.FINE, "StepIntoScriptHandler.actionToBeRun: {0}, serviceClassRef = {1}", new Object[]{action, serviceClassRef});
                if (serviceClassRef != null) {
                    try {
                        serviceClass = (ClassType) ClassObjectReferenceWrapper.reflectedType(serviceClassRef);
                        steppingField = ReferenceTypeWrapper.fieldByName(serviceClass, "steppingIntoTruffle");
                        serviceClass.setValue(steppingField, serviceClass.virtualMachine().mirrorOf(1));
                        RemoteServices.interruptServiceAccessThread(debugger);
                        LOG.fine("StepIntoScriptHandler: isSteppingInto set to true.");
                    } catch (ClassNotLoadedException | ClassNotPreparedExceptionWrapper |
                             InternalExceptionWrapper | InvalidTypeException |
                             ObjectCollectedExceptionWrapper ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (VMDisconnectedExceptionWrapper ex) {}
                } else {
                    // When the service is created, perform step into...
                    DebugManagerHandler.execStepInto(debugger, true);
                }
            }
        }
    }
    
    private class CurrentSFTracker implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getNewValue() == null) {
                // Ignore resume.
                return ;
            }
            LOG.fine("Current frame changed>");
            if (steppingField != null) {
                try {
                    serviceClass.setValue(steppingField, serviceClass.virtualMachine().mirrorOf(-1));
                    steppingField = null;
                    RemoteServices.interruptServiceAccessThread(debugger);
                    LOG.fine("StepIntoScriptHandler: isSteppingInto set to false.");
                } catch (InvalidTypeException | ClassNotLoadedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                // Cancel step into when the service is created
                DebugManagerHandler.execStepInto(debugger, false);
            }
        }
    }

}
