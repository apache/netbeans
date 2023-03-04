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

package org.netbeans.modules.db.explorer;

import java.util.List;
import javax.swing.event.ChangeListener;
import org.openide.nodes.Node;

/**
 * Loads nodes registered into the dbapi module.  This provides a new
 * non-public API that allows other modules add nodes to the Databases
 * node (such as a node to manage a local database server).
 * 
 * @author David Van Couvering
 */
public interface DbNodeLoader {
    /**
     * Get all the registered nodes
     */
    public List<Node> getAllNodes();
    
    /** 
     * Listen on state changes to this loader.  A state change indicates
     * that the consumer should reload the nodes by calling getAllNodes()
     * 
     * @param listener the listener to be added
     * @see #getAllNodes()
     */
    public void addChangeListener(ChangeListener listener);

    /**
     * @see #removeChangeListener(ChangeListener)
     */
    public void removeChangeListener(ChangeListener listener);    
}
