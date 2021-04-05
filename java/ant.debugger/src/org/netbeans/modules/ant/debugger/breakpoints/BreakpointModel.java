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

package org.netbeans.modules.ant.debugger.breakpoints;

import java.util.Vector;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.ant.debugger.AntDebugger;
import org.netbeans.modules.ant.debugger.Utils;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.filesystems.FileObject;

/**
 *
 * @author   Jan Jancura
 */
public class BreakpointModel implements NodeModel {
    
    public static final String      LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/editor/Breakpoint";
    public static final String      LINE_BREAKPOINT_PC =
        "org/netbeans/modules/debugger/resources/editor/Breakpoint+PC";
    public static final String      DISABLED_LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/editor/DisabledBreakpoint";
    
    private Vector                  listeners = new Vector ();
    
    
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
        if (node instanceof AntBreakpoint) {
            AntBreakpoint breakpoint = (AntBreakpoint) node;
            FileObject fileObject = breakpoint.getLine().getLookup().lookup(FileObject.class);
            return fileObject.getNameExt () + ":" + 
                (breakpoint.getLine ().getLineNumber () + 1);
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
        if (node instanceof AntBreakpoint) {
            AntBreakpoint breakpoint = (AntBreakpoint) node;
            if (!((AntBreakpoint) node).isEnabled ())
                return DISABLED_LINE_BREAKPOINT;
            AntDebugger debugger = getDebugger ();
            if ( debugger != null &&
                 Utils.contains (
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
        if (node instanceof AntBreakpoint) {
            AntBreakpoint breakpoint = (AntBreakpoint) node;
            return breakpoint.getLine ().getDisplayName ();
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
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((ModelListener) v.get (i)).modelChanged (
                new ModelEvent.TreeChanged (this)
            );
    }
    
    private static AntDebugger getDebugger () {
        DebuggerEngine engine = DebuggerManager.getDebuggerManager ().
            getCurrentEngine ();
        if (engine == null) return null;
        return engine.lookupFirst(null, AntDebugger.class);
    }
}
