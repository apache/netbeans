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

package org.netbeans.modules.web.debug.breakpoints;


import org.netbeans.api.debugger.jpda.*;
import org.netbeans.modules.web.debug.Context;

import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.openide.util.NbBundle;


/**
 * @author Martin Grebac
 */
public class JspBreakpointsNodeModel implements NodeModel {

    public static final String LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint";
    
    public static final String LINE_BREAKPOINT_DISABLED =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledBreakpoint";   
    
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o instanceof JspLineBreakpoint) {
            JspLineBreakpoint b = (JspLineBreakpoint) o;
            return NbBundle.getMessage (JspBreakpointsNodeModel.class,
                    "CTL_Jsp_Line_Breakpoint",
                    Context.getFileName (b),
                    "" + b.getLineNumber()
                );
        } 
        throw new UnknownTypeException(o);
    }
    
    public String getShortDescription (Object o) throws UnknownTypeException {
        if (o instanceof JspLineBreakpoint) {
            return NbBundle.getMessage (
                    JspBreakpointsNodeModel.class,
                    "CTL_Jsp_Line_Breakpoint",
                    Context.getFileName ((JspLineBreakpoint) o),
                    "" + ((JspLineBreakpoint) o).getLineNumber ()
                );
        }
        throw new UnknownTypeException (o);
    }
    
    public String getIconBase (Object o) throws UnknownTypeException {
        if (o instanceof JspLineBreakpoint) {
            JspLineBreakpoint breakpoint = (JspLineBreakpoint)o;
            if (breakpoint.isEnabled()) {
                return LINE_BREAKPOINT;
            }
            else {
                return LINE_BREAKPOINT_DISABLED;
            }
        }
        throw new UnknownTypeException (o);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
//        listeners.add (l);
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
//        listeners.remove (l);
    }
    
//    private void fireTreeChanged () {
//        Vector v = (Vector) listeners.clone ();
//        int i, k = v.size ();
//        for (i = 0; i < k; i++)
//            ((TreeModelListener) v.get (i)).treeChanged ();
//    }
//    
//    private void fireTreeNodeChanged (Object parent) {
//        Vector v = (Vector) listeners.clone ();
//        int i, k = v.size ();
//        for (i = 0; i < k; i++)
//            ((TreeModelListener) v.get (i)).treeNodeChanged (parent);
//    }
    
//    static String getShort (String s) {
//        if (s.indexOf ('*') >= 0) return s;
//        int i = s.lastIndexOf ('.');
//        if (i < 0) return s;
//        return s.substring (i + 1);
//    }
}
