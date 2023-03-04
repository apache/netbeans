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

package org.netbeans.modules.db.api.explorer;

import java.util.List;
import javax.swing.event.ChangeListener;
import org.openide.nodes.Node;

/**
 * Interface allowing modules to add nodes to the Database Explorer
 * under the Databases node in the Services tab.  
 * <p>
 * Register a NodeProvider under "Databases/NodeProviders" in the layer file.
 * </p>
 * @author David Van Couvering
 */
public interface NodeProvider {
    /**
     * @return the list of nodes that this node provider is providing
     */
    public List<Node> getNodes(); 
    
    /**
     * Listen to state changes on this provider, which would require the
     * consumer to call getNodes() to get a new list of nodes
     * 
     * @param listener
     */
    public void addChangeListener(ChangeListener listener);
    
    /** 
     * @see #addChangeListener(ChangeListener)
     */
    public void removeChangeListener(ChangeListener listener);
}
