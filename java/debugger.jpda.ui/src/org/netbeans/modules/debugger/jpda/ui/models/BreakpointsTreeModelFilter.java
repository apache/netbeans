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
        List<Object> l = new ArrayList<>();
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
                mI = line;
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
