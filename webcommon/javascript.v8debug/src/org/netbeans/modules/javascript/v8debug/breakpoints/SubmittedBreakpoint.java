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

package org.netbeans.modules.javascript.v8debug.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.v8debug.V8Breakpoint;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.netbeans.modules.javascript2.debug.breakpoints.JSBreakpointStatus;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator;
import org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator.Location;
import org.openide.util.WeakListeners;

/**
 *
 * @author Martin Entlicher
 */
public final class SubmittedBreakpoint {
    
    private static final Logger LOG = Logger.getLogger(SubmittedBreakpoint.class.getName());
    
    private final JSLineBreakpoint breakpoint;
    private final List<Long> ids;
    private final List<Location> bpLocs;
    private final V8Debugger dbg;
    private final PropertyChangeListener bpChangeListener;
    private final PropertyChangeListener addedChangeListener;
    
    SubmittedBreakpoint(JSLineBreakpoint breakpoint, 
                        List<Long> ids,
                        List<Location> bpLocs,
                        List<V8Breakpoint.ActualLocation[]> actualLocations,
                        V8Debugger dbg) {
        this.breakpoint = breakpoint;
        this.ids = ids;
        this.bpLocs = bpLocs;
        this.dbg = dbg;
        LOG.log(Level.FINE, "SubmittedBreakpoint({0}, {1})", new Object[]{breakpoint, ids});
        adjustLocation(ids, actualLocations);
        LOG.log(Level.FINE, "  adjusted BP => {0}", breakpoint);
        bpChangeListener = new BPChangeListener();
        addedChangeListener = WeakListeners.propertyChange(bpChangeListener, breakpoint);
        breakpoint.addPropertyChangeListener(addedChangeListener);
        JSBreakpointStatus.setValid(breakpoint, null);
    }

    public JSLineBreakpoint getBreakpoint() {
        return breakpoint;
    }
    
    public List<Long> getIds() {
        return ids;
    }
    
    private void adjustLocation(List<Long> ids, List<V8Breakpoint.ActualLocation[]> actualLocationsList) {
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            V8Breakpoint.ActualLocation[] actualLocations = actualLocationsList.get(i);
            if (actualLocations != null && actualLocations.length > 0) {
                long line = actualLocations[0].getLine();
                long column = actualLocations[0].getColumn();
                updatePosition(id, line, column);
            }
        }
    }
    
    void updatePosition(long id, long line, long column) {
        SourceMapsTranslator smt = dbg.getScriptsHandler().getSourceMapsTranslator();
        Location bpLoc = bpLocs.get(ids.indexOf(id));
        if (smt != null && bpLoc != null) {
            if (line == 0) {
                column -= dbg.getScriptsHandler().getScriptFirstLineColumnShift(bpLoc.getFile());
            }
            Location newLoc = new Location(bpLoc.getFile(), (int) line, (int) column);
            Location l = smt.getSourceLocation(newLoc);
            if (l != newLoc) {
                line = l.getLine();
            }
        }
        if(ids.indexOf(id) == 0) {
            breakpoint.setLine((int) line + 1);
        }
    }

    void notifyDestroyed() {
        breakpoint.removePropertyChangeListener(addedChangeListener);
        JSBreakpointStatus.resetValidity(breakpoint);
    }

    private final class BPChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            switch (evt.getPropertyName()) {
                case JSLineBreakpoint.PROP_ENABLED:
                case JSLineBreakpoint.PROP_CONDITION:
                    BreakpointsHandler
                            .createChangeRequestArguments(SubmittedBreakpoint.this)
                            .stream()
                            .forEach(cbargs -> dbg.sendCommandRequest(V8Command.Changebreakpoint, cbargs));
            }
        }
    }
}
