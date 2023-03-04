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

package org.netbeans.modules.javascript2.debug.breakpoints.io;

import java.beans.PropertyChangeEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
/**
 * Listens on DebuggerManager and:
 * - loads all JavaScript breakpoints
 * - listens on all changes of JavaScript breakpoints and saves new values
 *
 * @author Martin
 */
@DebuggerServiceRegistration(types = LazyDebuggerManagerListener.class)
public class PersistenceManager implements LazyDebuggerManagerListener {

    private static final String JS_PROPERTY = "JS";
    
    private boolean areBreakpointsPersisted() {
        Properties p = Properties.getDefault ().getProperties ("debugger");
        p = p.getProperties("persistence");
        return p.getBoolean("breakpoints", true);
    }
    
    @Override
    public String[] getProperties() {
        return new String [] {
            DebuggerManager.PROP_BREAKPOINTS_INIT,
            DebuggerManager.PROP_BREAKPOINTS,
        };
    }

    @Override
    public Breakpoint[] initBreakpoints() {
        if (!areBreakpointsPersisted()) {
            return new Breakpoint[]{};
        }
        Properties p = Properties.getDefault ().getProperties ("debugger").
            getProperties (DebuggerManager.PROP_BREAKPOINTS);
        Breakpoint[] breakpoints = (Breakpoint[]) p.getArray (
            JS_PROPERTY, 
            new Breakpoint [0]
        );
        for (int i = 0; i < breakpoints.length; i++) {
            Breakpoint b = breakpoints[i];
            if (b == null) {
                Breakpoint[] breakpoints2 = new Breakpoint[breakpoints.length - 1];
                System.arraycopy(breakpoints, 0, breakpoints2, 0, i);
                if (i < breakpoints.length - 1) {
                    System.arraycopy(breakpoints, i + 1, breakpoints2, i, breakpoints.length - i - 1);
                }
                breakpoints = breakpoints2;
                i--;
                continue;
            }
            b.addPropertyChangeListener(this);
        }
        return breakpoints;
    }

    @Override
    public void breakpointAdded(Breakpoint breakpoint) {
        if (!areBreakpointsPersisted()) {
            return ;
        }
        if (breakpoint instanceof JSLineBreakpoint) {
            Properties p = Properties.getDefault ().getProperties ("debugger").
                getProperties (DebuggerManager.PROP_BREAKPOINTS);
            p.setArray (
                JS_PROPERTY,
                getBreakpoints()
            );
            breakpoint.addPropertyChangeListener(this);
        }
    }

    @Override
    public void breakpointRemoved(Breakpoint breakpoint) {
        if (!areBreakpointsPersisted()) {
            return ;
        }
        if (breakpoint instanceof JSLineBreakpoint) {
            Properties p = Properties.getDefault ().getProperties ("debugger").
                getProperties (DebuggerManager.PROP_BREAKPOINTS);
            p.setArray (
                JS_PROPERTY,
                getBreakpoints()
            );
            breakpoint.removePropertyChangeListener(this);
        }
    }

    @Override
    public void initWatches() {}

    @Override
    public void watchAdded(Watch watch) {}

    @Override
    public void watchRemoved(Watch watch) {}

    @Override
    public void sessionAdded(Session session) {}

    @Override
    public void sessionRemoved(Session session) {}

    @Override
    public void engineAdded(DebuggerEngine engine) {}

    @Override
    public void engineRemoved(DebuggerEngine engine) {}

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof Breakpoint) {
            Properties.getDefault ().getProperties ("debugger").
                getProperties (DebuggerManager.PROP_BREAKPOINTS).setArray (
                    JS_PROPERTY,
                    getBreakpoints ()
                );
        }
    }
    
    private static Breakpoint[] getBreakpoints () {
        Breakpoint[] bs = DebuggerManager.getDebuggerManager().getBreakpoints();
        int i, k = bs.length;
        ArrayList<Breakpoint> bb = new ArrayList<>();
        for (i = 0; i < k; i++)
            // We store only the JS breakpoints
            if (bs[i] instanceof JSLineBreakpoint) {
                bb.add (bs [i]);
            }
        bs = new Breakpoint [bb.size ()];
        return bb.toArray(bs);
    }
}
