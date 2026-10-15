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

package org.netbeans.modules.web.javascript.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Properties;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.modules.OnStop;
import org.openide.util.RequestProcessor;

/**
 * Listens on DebuggerManager and:
 * - loads all breakpoints & watches on startup
 * - listens on all changes of breakpoints and watches (like breakoint / watch
 *     added / removed, or some property change) and saves a new values
 *
 * @author ads
 */
@DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
@OnStop
public class PersistenceManager extends  DebuggerManagerAdapter implements Runnable {
    public static final Logger LOGGER = Logger.getLogger(PersistenceManager.class.getName());
    private static final String DEBUGGER = "debugger";      // NOI18N
    private static final String JAVASCRIPT = "javascript-debugger";      // NOI18N
    
    private static final RequestProcessor RP = new RequestProcessor(PersistenceManager.class.getName());
    
    private RequestProcessor.Task saveTask;

    @Override
    public Breakpoint[] initBreakpoints() {
        Properties p = Properties.getDefault().getProperties(DEBUGGER).
            getProperties(DebuggerManager.PROP_BREAKPOINTS);
        Breakpoint[] breakpoints = (Breakpoint[]) p.getArray( JAVASCRIPT ,new Breakpoint [0]);
        List<Breakpoint> validBreakpoints = new ArrayList<Breakpoint>();
        for (Breakpoint breakpoint : breakpoints) {
            if (breakpoint != null) {
                breakpoint.addPropertyChangeListener(this);
                validBreakpoints.add(breakpoint);
            } else {
                LOGGER.warning("null stored in the array obtained from \"" + JAVASCRIPT + "\" property"); // TODO: why?
            }
        }
        return validBreakpoints.toArray(new Breakpoint[0]);
    }

    @Override
    public String[] getProperties() {
        return new String [] {
            DebuggerManager.PROP_BREAKPOINTS_INIT,
            DebuggerManager.PROP_BREAKPOINTS,
        };
    }
    
    private synchronized void scheduleSaveTask() {
        if (saveTask == null) {
            saveTask = RP.create(new Store());
        }
        saveTask.schedule(500);
    }

    @Override
    public void breakpointAdded(Breakpoint breakpoint) {
        breakpoint.addPropertyChangeListener(this);
        scheduleSaveTask();
    }

    @Override
    public void breakpointRemoved(Breakpoint breakpoint) {
        breakpoint.removePropertyChangeListener(this);
        scheduleSaveTask();
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        /*
         * Breakpoint could be disabled/enabled.
         * This notification are got in the case changing this property.
         */
        if (evt.getSource() instanceof Breakpoint) {
            scheduleSaveTask();
        }
    }
    
    @Override
    // OnStop
    public synchronized void run() {
        if (saveTask != null) {
            saveTask.waitFinished();
        }
    }

    private static class Store implements Runnable {
        
        private Properties properties = Properties.getDefault().getProperties(DEBUGGER).
            getProperties(DebuggerManager.PROP_BREAKPOINTS);

        @Override
        public void run() {
            properties.setArray(JAVASCRIPT, getBreakpoints());
        }
        
        private Breakpoint[] getBreakpoints() {
            Breakpoint[] bpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
            List<Breakpoint> result = new ArrayList<Breakpoint>();
            for ( Breakpoint breakpoint : bpoints ) {
                // Don't store hidden breakpoints
                if ( breakpoint instanceof AbstractBreakpoint) {
                    result.add( breakpoint );
                }
            }
            return result.toArray(new Breakpoint [0] );
        }

    }
}
