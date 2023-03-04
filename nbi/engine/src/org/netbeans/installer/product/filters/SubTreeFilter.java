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

package org.netbeans.installer.product.filters;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.RegistryNode;

/**
 *
 * @author Kirill Sorokin
 */
public class SubTreeFilter implements RegistryFilter {
    private List<RegistryNode> leaves;
    
    public SubTreeFilter(List<? extends RegistryNode> nodes) {
        this.leaves = new LinkedList<RegistryNode>();
        this.leaves.addAll(nodes);
    }
    
    public boolean accept(final RegistryNode node) {
        if (leaves.contains(node)) {
            return true;
        }
        
        for (RegistryNode leaf: leaves) {
            if (node.isAncestor(leaf)) {
                return true;
            }
        }
        
        return false;
    }
}
