/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
            FileObject fileObject = (FileObject) breakpoint.getLine ().
                getLookup ().lookup (FileObject.class);
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
