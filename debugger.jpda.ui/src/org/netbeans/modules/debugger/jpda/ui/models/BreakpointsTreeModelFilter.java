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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * Filters content of some original tree of nodes (represented by 
 * {@link TreeModel}).
 *
 * @author   Jan Jancura
 */
@DebuggerServiceRegistration(path="BreakpointsView", types=TreeModelFilter.class)
public class BreakpointsTreeModelFilter implements TreeModelFilter {
    
    private static Logger logger = Logger.getLogger(BreakpointsTreeModelFilter.class.getName());

    static Map MAX_LINES = new WeakHashMap();
    
    private static boolean verbose = 
        System.getProperty ("netbeans.debugger.show_hidden_breakpoints") != null;

    /** 
     * Returns filtered root of hierarchy.
     *
     * @param   original the original tree model
     * @return  filtered root of hierarchy
     */
    @Override
    public Object getRoot (TreeModel original) {
        return original.getRoot ();
    }
    
    /** 
     * Returns filtered children for given parent on given indexes.
     *
     * @param   original the original tree model
     * @param   parent a parent of returned nodes
     * @throws  NoInformationException if the set of children can not be 
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException if this TreeModelFilter implementation is not
     *          able to resolve children for given node type
     *
     * @return  children for given parent on given indexes
     */
    @Override
    public Object[] getChildren (
        TreeModel   original, 
        Object      parent, 
        int         from, 
        int         to
    ) throws UnknownTypeException {
        if (to - from <= 0) {
            logger.log(Level.FINE, "getChildren({0}, {1}): RETURNING an empty array.", new Object[]{from, to});
            return new Object[0];
        }
        Object[] ch = original.getChildren (parent, from, to);
        List l = new ArrayList ();
        int i, k = ch.length, n = to - from;
        Map maxLines = new HashMap();
        for (i = 0; i < k; i++) {
            if ( (!verbose) &&
                 (ch [i] instanceof JPDABreakpoint) &&
                 ((JPDABreakpoint) ch [i]).isHidden ()
            ) continue;
            if (--from >= 0) continue;
            l.add (ch [i]);
            if (ch[i] instanceof LineBreakpoint) {
                LineBreakpoint lb = (LineBreakpoint) ch[i];
                String fn = EditorContextBridge.getFileName(lb);
                int line = lb.getLineNumber();
                Integer mI = (Integer) maxLines.get(fn);
                if (mI != null) {
                    line = Math.max(line, mI.intValue());
                }
                mI = new Integer(line);
                maxLines.put(fn, mI);
            }
            if (--n == 0) break;
        }
        for (i = l.size() - 1; i >= 0; i--) {
            Object o = l.get(i);
            if (o instanceof LineBreakpoint) {
                LineBreakpoint lb = (LineBreakpoint) o;
                MAX_LINES.put(lb, maxLines.get(EditorContextBridge.getFileName(lb)));
            }
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getChildren("+from+", "+to+"): Original Breakpoints: "+Arrays.asList(ch)+";  RETURNING: "+l);
        }
        return l.toArray();
    }
    
    /**
     * Returns number of filtered children for given node.
     * 
     * @param   original the original tree model
     * @param   node the parent node
     * @throws  NoInformationException if the set of children can not be 
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  true if node is leaf
     */
    @Override
    public int getChildrenCount (
        TreeModel original,
        Object node
    ) throws UnknownTypeException {
        return Integer.MAX_VALUE;
    }
    
    /**
     * Returns true if node is leaf.
     * 
     * @param   original the original tree model
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     * @return  true if node is leaf
     */
    @Override
    public boolean isLeaf (
        TreeModel original, 
        Object node
    ) throws UnknownTypeException {
        return original. isLeaf (node);
    }

    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    @Override
    public void addModelListener (ModelListener l) {
    }

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    @Override
    public void removeModelListener (ModelListener l) {
    }
}
