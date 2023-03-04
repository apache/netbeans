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

package org.openide.explorer;

import org.openide.nodes.Node;
import java.io.IOException;

/**
 * Register implementation of this interface into META-INF services
 * lookup if you want to intercept Node deletion in explorer.
 * If more instances are registered, they are invoked in order
 * until one of them claim to have performed the action
 * by returning true. 
 *
 * @author Jan Becicka
 * @since 6.10
 */
public interface ExtendedDelete {
    
    /**
     * handle delete of nodes
     * @param nodes nodes to delete
     * @return true if delete was handled 
     *         false if delete was not handled
     * @throws IOException to signal some problem while performing the delete.
     *         The exception also means that the instance tried to handle
     *         node deletion and no further processing on the nodes
     *         should be done.
     */ 
    boolean delete(Node[] nodes) throws IOException;
        
}
