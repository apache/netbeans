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
package org.netbeans.modules.cloud.oracle;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentNode;

/**
 *
 * @author Jan Horvath
 */
public class CompartmentNodes {
    private static CompartmentNodes INSTANCE = new CompartmentNodes();
    
    Set<Reference<CompartmentNode>> nodes = new HashSet<> ();
    
    public static synchronized CompartmentNodes getDefault() {
        return INSTANCE;
    }
    
    public void addNode(CompartmentNode node) {
        nodes.add(new WeakReference<>(node));
    }
    
    public void refresh() {
        for (Reference<CompartmentNode> node : nodes) {
            if (node.get() != null) {
                node.get().refresh();
            }
        }
    }
    
}
