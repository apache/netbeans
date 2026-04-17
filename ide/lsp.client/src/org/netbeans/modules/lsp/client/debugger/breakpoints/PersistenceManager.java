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

package org.netbeans.modules.lsp.client.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.ArrayList;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;

/**
 * Listens on DebuggerManager and:
 * - loads all breakpoints & watches on startup
 * - listens on all changes of breakpoints and watches (like breakoint / watch
 *     added / removed, or some property change) and saves a new values
 *
 */
@DebuggerServiceRegistration(types={LazyDebuggerManagerListener.class})
public final class PersistenceManager implements LazyDebuggerManagerListener {

    private static final String KEY = "dap";

    private boolean areBreakpointsPersisted() {
        Properties p = Properties.getDefault ().getProperties ("debugger");
        p = p.getProperties("persistence");
        return p.getBoolean("breakpoints", true);
    }

    @Override
    public Breakpoint[] initBreakpoints () {
        if (!areBreakpointsPersisted()) {
            return new Breakpoint[]{};
        }
        Properties p = Properties.getDefault ().getProperties ("debugger").
            getProperties (DebuggerManager.PROP_BREAKPOINTS);
        Breakpoint[] breakpoints = (Breakpoint[]) p.getArray (
            KEY,
            new Breakpoint [0]
        );
        for (int i = 0; i < breakpoints.length; i++) {
            if (breakpoints[i] == null) {
                Breakpoint[] b2 = new Breakpoint[breakpoints.length - 1];
                System.arraycopy(breakpoints, 0, b2, 0, i);
                if (i < breakpoints.length - 1) {
                    System.arraycopy(breakpoints, i + 1, b2, i, breakpoints.length - i - 1);
                }
                breakpoints = b2;
                i--;
                continue;
            }
            breakpoints[i].addPropertyChangeListener(this);
        }
        return breakpoints;
    }

    @Override
    public void initWatches () {
    }

    @Override
    public String[] getProperties () {
        return new String [] {
            DebuggerManager.PROP_BREAKPOINTS_INIT,
            DebuggerManager.PROP_BREAKPOINTS,
        };
    }

    @Override
    public void breakpointAdded (Breakpoint breakpoint) {
        if (!areBreakpointsPersisted()) {
            return ;
        }
        if (breakpoint instanceof DAPLineBreakpoint) {
            Properties p = Properties.getDefault ().getProperties ("debugger").
                getProperties (DebuggerManager.PROP_BREAKPOINTS);
            p.setArray (
                KEY,
                getBreakpoints ()
            );
            breakpoint.addPropertyChangeListener(this);
        }
    }

    @Override
    public void breakpointRemoved (Breakpoint breakpoint) {
        if (!areBreakpointsPersisted()) {
            return ;
        }
        if (breakpoint instanceof DAPLineBreakpoint) {
            Properties p = Properties.getDefault ().getProperties ("debugger").
                getProperties (DebuggerManager.PROP_BREAKPOINTS);
            p.setArray (
                KEY,
                getBreakpoints ()
            );
            breakpoint.removePropertyChangeListener(this);
        }
    }
    @Override
    public void watchAdded (Watch watch) {
    }

    @Override
    public void watchRemoved (Watch watch) {
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getSource() instanceof Breakpoint) {
            Properties.getDefault ().getProperties ("debugger").
                getProperties (DebuggerManager.PROP_BREAKPOINTS).setArray (
                    KEY,
                    getBreakpoints ()
                );
        }
    }

    @Override
    public void sessionAdded (Session session) {}
    @Override
    public void sessionRemoved (Session session) {}
    @Override
    public void engineAdded (DebuggerEngine engine) {}
    @Override
    public void engineRemoved (DebuggerEngine engine) {}


    private static Breakpoint[] getBreakpoints () {
        Breakpoint[] bs = DebuggerManager.getDebuggerManager ().
            getBreakpoints ();
        List<Breakpoint> bb = new ArrayList<>();
        for (Breakpoint b : bs) {
            if (b instanceof DAPLineBreakpoint) {
                // Don't store hidden breakpoints
                if (!((DAPLineBreakpoint) b).isHidden()) {
                    bb.add(b);
                }
            }
        }
        bs = new Breakpoint [bb.size ()];
        return bb.toArray(bs);
    }
}

