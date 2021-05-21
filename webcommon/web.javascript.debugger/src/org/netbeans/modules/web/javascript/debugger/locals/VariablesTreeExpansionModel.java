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

package org.netbeans.modules.web.javascript.debugger.locals;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.WeakSet;


/**
 * @author   Martin Entlicher
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="javascript-debuggerengine/LocalsView",
                                 types=TreeExpansionModel.class),
    @DebuggerServiceRegistration(path="javascript-debuggerengine/ResultsView",
                                 types=TreeExpansionModel.class),
    @DebuggerServiceRegistration(path="javascript-debuggerengine/WatchesView",
                                 types=TreeExpansionModel.class),
})
public class VariablesTreeExpansionModel implements TreeExpansionModel {
    
    private static final Logger LOGGER = Logger.getLogger(VariablesTreeExpansionModel.class.getName());

    private Set<Object> expandedNodes = new WeakSet<>();
    private Set<Object> collapsedNodes = new WeakSet<>();

    /**
     * Defines default state (collapsed, expanded) of given node.
     *
     * @param node a node
     * @return default state (collapsed, expanded) of given node
     */
    @Override
    public boolean isExpanded (Object node) 
    throws UnknownTypeException {
        synchronized (this) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE,"isExpanded({0}):\n"+
                                      " expandedNodes = {1}\n"+
                                      " => {2}",
                           new Object[]{node, expandedNodes, expandedNodes.contains(node)});
            }
            if (expandedNodes.contains(node)) {
                return true;
            }
            if (collapsedNodes.contains(node)) {
                return false;
            }
        }
        // Default behavior follows:
        return false;
    }
    
    /**
     * Called when given node is expanded.
     *
     * @param node a expanded node
     */
    @Override
    public void nodeExpanded (Object node) {
        synchronized (this) {
            expandedNodes.add(node);
            collapsedNodes.remove(node);
            LOGGER.log(Level.FINE,"nodeExpanded({0}):\n"+
                                  " => expandedNodes = {1}",
                       new Object[]{node, expandedNodes});
        }
    }
    
    /**
     * Called when given node is collapsed.
     *
     * @param node a collapsed node
     */
    @Override
    public void nodeCollapsed (Object node) {
        synchronized (this) {
            collapsedNodes.add(node);
            expandedNodes.remove(node);
        }
    }
}
