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

package org.netbeans.modules.web.inspect.webkit.knockout.unused;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Root node of the unused bindings tree.
 *
 * @author Jan Stola
 */
public class UnusedRootNode extends AbstractNode {

    /**
     * Creates a new {@code UnusedRootNode}.
     * 
     * @param unusedBindings information about unused bindings
     * ({@code name -> (id -> binding)} map).
     */
    @NbBundle.Messages({
        "UnusedRootNode.displayName=Unused Bindings"
    })
    public UnusedRootNode(Map<String,Map<Integer,UnusedBinding>> unusedBindings) {
        super(new UnusedRootChildren(unusedBindings));
        setDisplayName(Bundle.UnusedRootNode_displayName());
    }

    /**
     * Update the unused bindings represented by this node.
     * 
     * @param unusedBindings information about unused bindings represented
     * by this node.
     */
    void update(Map<String,Map<Integer,UnusedBinding>> unusedBindings) {
        ((UnusedRootChildren)getChildren()).update(unusedBindings);
    }

    /**
     * Children of {@code UnusedRootNode}.
     */
    static class UnusedRootChildren extends Children.Keys<String> {
        /** Unused binding information ({@code name -> (id -> binding)} map). */
        private java.util.Map<String,java.util.Map<Integer,UnusedBinding>> unusedBindings;

        /**
         * Creates a new {@code UnusedRootChildren}.
         * 
         * @param unusedBindings unused binding information
         * ({@code name -> (id -> binding)} map).
         */
        UnusedRootChildren(java.util.Map<String,java.util.Map<Integer,UnusedBinding>> unusedBindings) {
            this.unusedBindings = unusedBindings;
            setKeys(sortKeys(unusedBindings.keySet()));
        }

        /**
         * Update unused bindings represented by this children.
         * 
         * @param unusedBindings unused binding information.
         */
        synchronized void update(java.util.Map<String,java.util.Map<Integer,UnusedBinding>> unusedBindings) {
            for (Node node : getNodes()) {
                UnusedGroupNode groupNode = (UnusedGroupNode)node;
                String name = groupNode.getBindingName();
                java.util.Map<Integer,UnusedBinding> bindings = unusedBindings.get(name);
                if (bindings != null) {
                    groupNode.update(bindings);
                }
            }
            this.unusedBindings = unusedBindings;
            setKeys(sortKeys(unusedBindings.keySet()));
        }

        /**
         * Returns a list of the given keys sorted alphabetically.
         * 
         * @param keys keys to sort.
         * @return list of the given keys sorted alphabetically.
         */
        private List<String> sortKeys(Collection<String> keys) {
            List<String> list = new ArrayList<String>(keys);
            Collections.sort(list);
            return list;
        }

        @Override
        protected synchronized Node[] createNodes(String key) {
            java.util.Map<Integer,UnusedBinding> bindings = unusedBindings.get(key);
            if (bindings == null) {
                return new Node[0];
            } else {
                return new Node[] { new UnusedGroupNode(key, bindings) };
            }
        }
        
    }
    
}
