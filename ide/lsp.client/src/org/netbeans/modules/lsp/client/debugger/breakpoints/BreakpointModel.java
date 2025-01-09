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

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.lsp.client.debugger.DAPDebugger;
import org.netbeans.modules.lsp.client.debugger.DAPStackTraceAnnotationHolder;

import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.filesystems.FileObject;

@DebuggerServiceRegistration(path="BreakpointsView", types={NodeModel.class})
public final class BreakpointModel implements NodeModel {

    public static final String      LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint";
    public static final String      LINE_BREAKPOINT_PC =
        "org/netbeans/modules/debugger/resources/breakpointsView/BreakpointHit";
    public static final String      DISABLED_LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledBreakpoint";

    private List<ModelListener>   listeners = new CopyOnWriteArrayList<>();


    // NodeModel implementation ................................................

    /**
     * Returns display name for given node.
     *
     * @throws  ComputingException if the display name resolving process
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve display name for given node type
     * @return  display name for given node
     */
    @Override
    public String getDisplayName (Object node) throws UnknownTypeException {
        if (node instanceof DAPLineBreakpoint) {
            DAPLineBreakpoint breakpoint = (DAPLineBreakpoint) node;
            String nameExt;
            FileObject fileObject = breakpoint.getFileObject();
            nameExt = fileObject.getNameExt();
            return nameExt + ":" + breakpoint.getLineNumber();
        }
        throw new UnknownTypeException (node);
    }

    /**
     * Returns icon for given node.
     *
     * @throws  ComputingException if the icon resolving process
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve icon for given node type
     * @return  icon for given node
     */
    @Override
    public String getIconBase (Object node) throws UnknownTypeException {
        if (node instanceof DAPLineBreakpoint) {
            DAPLineBreakpoint breakpoint = (DAPLineBreakpoint) node;
            if (!((DAPLineBreakpoint) node).isEnabled ())
                return DISABLED_LINE_BREAKPOINT;
            DAPDebugger debugger = getDebugger ();
            if ( debugger != null &&
                 DAPStackTraceAnnotationHolder.contains (
                     debugger.getCurrentLine (),
                     breakpoint.getLine ()
                 )
             )
                return LINE_BREAKPOINT_PC;
            return LINE_BREAKPOINT;
        }
        throw new UnknownTypeException (node);
    }

    /**
     * Returns tooltip for given node.
     *
     * @throws  ComputingException if the tooltip resolving process
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve tooltip for given node type
     * @return  tooltip for given node
     */
    @Override
    public String getShortDescription (Object node)
    throws UnknownTypeException {
        if (node instanceof DAPLineBreakpoint) {
            DAPLineBreakpoint breakpoint = (DAPLineBreakpoint) node;
            return breakpoint.getFileObject().getPath() + ":" + breakpoint.getLineNumber();
        }
        throw new UnknownTypeException (node);
    }

    /**
     * Registers given listener.
     *
     * @param l the listener to add
     */
    @Override
    public void addModelListener (ModelListener l) {
        listeners.add (l);
    }

    /**
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    @Override
    public void removeModelListener (ModelListener l) {
        listeners.remove (l);
    }


    public void fireChanges () {
        ModelEvent event = new ModelEvent.TreeChanged(this);
        for (ModelListener l : listeners) {
            l.modelChanged(event);
        }
    }

    private static DAPDebugger getDebugger () {
        DebuggerEngine engine = DebuggerManager.getDebuggerManager ().
            getCurrentEngine ();
        if (engine == null) return null;
        return engine.lookupFirst(null, DAPDebugger.class);
    }
}
