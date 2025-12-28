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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * @author   Martin Entlicher
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession/LocalsView",
                                 types=TreeExpansionModel.class,
                                 position=10000),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ResultsView",
                                 types=TreeExpansionModel.class,
                                 position=10000),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ToolTipView",
                                 types=TreeExpansionModel.class,
                                 position=10000)
})
public class VariablesTreeExpansionModel implements TreeExpansionModel {

    private final Set<Object> expandedNodes = Collections.newSetFromMap(new WeakHashMap<>());
    private final Set<Object> collapsedNodes = Collections.newSetFromMap(new WeakHashMap<>());

    /**
     * Defines default state (collapsed, expanded) of given node.
     *
     * @param node a node
     * @return default state (collapsed, expanded) of given node
     */
    public boolean isExpanded (Object node) 
    throws UnknownTypeException {
        if (node instanceof String && ((String) node).startsWith("operationArguments ")) {
            node = "operationArguments";
        }
        synchronized (this) {
            if (expandedNodes.contains(node)) {
                return true;
            }
            if (collapsedNodes.contains(node)) {
                return false;
            }
        }
        // Default behavior follows:
        if ("lastOperations".equals(node)) {
            return true;
        }
        if (node instanceof Operation) {
            return true;
        }
        return false;
    }
    
    /**
     * Called when given node is expanded.
     *
     * @param node a expanded node
     */
    public void nodeExpanded (Object node) {
        if (node instanceof String && ((String) node).startsWith("operationArguments ")) {
            node = "operationArguments";
        }
        synchronized (this) {
            expandedNodes.add(node);
            collapsedNodes.remove(node);
        }
    }
    
    /**
     * Called when given node is collapsed.
     *
     * @param node a collapsed node
     */
    public void nodeCollapsed (Object node) {
        if (node instanceof String && ((String) node).startsWith("operationArguments ")) {
            node = "operationArguments";
        }
        synchronized (this) {
            collapsedNodes.add(node);
            expandedNodes.remove(node);
        }
    }
}
