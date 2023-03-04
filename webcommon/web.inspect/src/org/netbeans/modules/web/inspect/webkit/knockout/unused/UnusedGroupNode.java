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
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Node representing a group of unused bindings (with the same name).
 *
 * @author Jan Stola
 */
public class UnusedGroupNode extends AbstractNode {
    /** Name of the binding this node represents. */
    private final String bindingName;

    /**
     * Creates a new {@code UnusedGroupNode}.
     * 
     * @param bindingName name of the binding the node represents.
     * @param unusedBindings information about unused bindings
     * ({@code id -> binding} map).
     */
    public UnusedGroupNode(String bindingName, Map<Integer,UnusedBinding> unusedBindings) {
        super(new UnusedGroupChildren(unusedBindings));
        this.bindingName = bindingName;
        UnusedBinding binding = unusedBindings.values().iterator().next();
        setDisplayName(binding.getName());
        setIconBaseWithExtension("org/netbeans/modules/web/inspect/resources/binding.png"); // NOI18N
    }

    /**
     * Returns the name of the binding this node represents.
     * 
     * @return name of the binding this node represents.
     */
    String getBindingName() {
        return bindingName;
    }

    /**
     * Update the unused bindings represented by this node.
     * 
     * @param unusedBindings information about unused bindings represented
     * by this node.
     */
    void update(Map<Integer,UnusedBinding> unusedBindings) {
        ((UnusedGroupChildren)getChildren()).update(unusedBindings);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[0];
    }

    /**
     * Children of {@code UnusedGroupNode}.
     */
    static class UnusedGroupChildren extends Children.Keys<Integer> {
        /** Unused binding information ({@code id -> binding} map). */
        private java.util.Map<Integer,UnusedBinding> unusedBindings;

        /**
         * Creates new {@code UnusedGroupChildren}.
         * 
         * @param unusedBindings unused binding information ({@code id -> binding} map).
         */
        UnusedGroupChildren(java.util.Map<Integer,UnusedBinding> unusedBindings) {
            this.unusedBindings = unusedBindings;
            setKeys(sortKeys(unusedBindings.keySet()));
        }

        /**
         * Update unused bindings represented by this children.
         * 
         * @param unusedBindings unused binding information.
         */
        synchronized void update(java.util.Map<Integer,UnusedBinding> unusedBindings) {
            for (Node node : getNodes()) {
                UnusedBindingNode unusedBindingNode = (UnusedBindingNode)node;
                UnusedBinding oldInfo = unusedBindingNode.getUnusedBinding();
                UnusedBinding newInfo = unusedBindings.get(oldInfo.getId());
                if (newInfo != null) {
                    unusedBindingNode.setUnusedBinding(newInfo);
                }
            }
            this.unusedBindings = unusedBindings;
            setKeys(sortKeys(unusedBindings.keySet()));
        }

        /**
         * Returns a list of the given keys sorted (by unused binding ID).
         * 
         * @param keys keys to sort.
         * @return list of the given keys sorted (by unused binding ID).
         */
        private List<Integer> sortKeys(Collection<Integer> keys) {
            List<Integer> list = new ArrayList<Integer>(keys);
            Collections.sort(list);
            return list;
        }

        @Override
        protected synchronized Node[] createNodes(Integer key) {
            UnusedBinding binding = unusedBindings.get(key);
            if (binding == null) {
                return new Node[0];
            } else {
                return new Node[] { new UnusedBindingNode(binding) };
            }
        }

    }

}
