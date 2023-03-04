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

package org.netbeans.modules.debugger.ui;

import java.beans.PropertyChangeEvent;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.spi.debugger.ui.EditorPin;


/**
 * Listens on DebuggerManager and:
 * - loads all breakpoints & watches on startup
 * - listens on all changes of breakpoints and watches (like breakoint / watch
 *     added / removed, or some property change) and saves a new values
 *
 * @author Jan Jancura
 */
public class PersistenceManager implements LazyDebuggerManagerListener {
    
    public Breakpoint[] initBreakpoints () {
        return new Breakpoint [0];
    }
    
    private boolean areWatchesPersisted() {
        Properties p = Properties.getDefault ().getProperties ("debugger");
        p = p.getProperties("persistence");
        return p.getBoolean("watches", true);
    }
    
    public void initWatches () {
        if (!areWatchesPersisted()) {
            return ;
        }
        // As a side-effect, creates the watches. WatchesReader is triggered.
        Properties p = Properties.getDefault ().getProperties ("debugger");
        Watch[] watches = (Watch[]) p.getArray (
            DebuggerManager.PROP_WATCHES, 
            new Watch [0]
        );
        for (Watch watch : watches) {
            watch.addPropertyChangeListener (this);
            Watch.Pin pin = watch.getPin();
            if (pin instanceof EditorPin) {
                ((EditorPin) pin).addPropertyChangeListener(this);
            }
        }
    }
    
    public String[] getProperties () {
        return new String [] {
            DebuggerManager.PROP_WATCHES_INIT,
            DebuggerManager.PROP_WATCHES
        };
    }
    
    public void breakpointAdded (Breakpoint breakpoint) {
    }

    public void breakpointRemoved (Breakpoint breakpoint) {
    }
    
    public void watchAdded (Watch watch) {
        if (!areWatchesPersisted()) {
            return ;
        }
        Properties p = Properties.getDefault ().getProperties ("debugger");
        p.setArray (
            DebuggerManager.PROP_WATCHES, 
            DebuggerManager.getDebuggerManager ().getWatches ()
        );
        watch.addPropertyChangeListener (this);
        Watch.Pin pin = watch.getPin();
        if (pin instanceof EditorPin) {
            ((EditorPin) pin).addPropertyChangeListener(this);
        }
    }
    
    public void watchRemoved (Watch watch) {
        if (!areWatchesPersisted()) {
            return ;
        }
        Properties p = Properties.getDefault ().getProperties ("debugger");
        p.setArray (
            DebuggerManager.PROP_WATCHES, 
            DebuggerManager.getDebuggerManager ().getWatches ()
        );
        watch.removePropertyChangeListener(this);
        Watch.Pin pin = watch.getPin();
        if (pin instanceof EditorPin) {
            ((EditorPin) pin).removePropertyChangeListener(this);
        }
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        Object source = evt.getSource();
        if (source instanceof Watch || source instanceof EditorPin) {
            Properties.getDefault ().getProperties ("debugger").setArray (
                DebuggerManager.PROP_WATCHES,
                DebuggerManager.getDebuggerManager ().getWatches ()
            );
        }
    }
    
    public void sessionAdded (Session session) {}
    public void sessionRemoved (Session session) {}
    public void engineAdded (DebuggerEngine engine) {}
    public void engineRemoved (DebuggerEngine engine) {}
}
