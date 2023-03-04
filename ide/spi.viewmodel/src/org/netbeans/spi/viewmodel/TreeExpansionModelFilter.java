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

package org.netbeans.spi.viewmodel;


/**
 * This model filter controlls expansion, collapsion of nodes in tree view, and
 * defindes default expand state for all node in it. It may delegate to the supplied
 * TreeExpansionModel.
 *
 * @author   Martin Entlicher
 * @since 1.15
 */
public interface TreeExpansionModelFilter extends Model {

    /**
     * Defines default state (collapsed, expanded) of given node.
     *
     * @param node a node
     * @return default state (collapsed, expanded) of given node
     */
    public abstract boolean isExpanded (TreeExpansionModel original, Object node)
    throws UnknownTypeException;

    /**
     * Called when given node is expanded.
     *
     * @param node a expanded node
     */
    public abstract void nodeExpanded (Object node);
    
    /**
     * Called when given node is collapsed.
     *
     * @param node a collapsed node
     */
    public abstract void nodeCollapsed (Object node);
    
    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public abstract void addModelListener (ModelListener l);

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public abstract void removeModelListener (ModelListener l);
}
