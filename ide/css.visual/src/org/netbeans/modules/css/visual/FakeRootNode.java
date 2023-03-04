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
package org.netbeans.modules.css.visual;

import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Node whose sole purpose is to serve as a hidden root node
 * in {@code TreeView} and provide a context menu for this view
 * through its actions.
 *
 * @author Jan Stola
 * @param <T> type of the real root node.
 */
public class FakeRootNode<T extends Node> extends AbstractNode {
    /** Real root node, i.e., the only child of this fake root. */
    private T realRoot;
    /** Actions of this node. */
    private Action[] actions;

    /**
     * Creates a new {@code FakeRootNode}.
     * 
     * @param realRoot real root node.
     * @param actions actions of the new fake root node.
     */
    public FakeRootNode(T realRoot, Action[] actions) {
        super(new FakeRootChildren(realRoot));
        this.realRoot = realRoot;
        this.actions = actions;
    }

    /**
     * Returns the real root node.
     * 
     * @return real root node.
     */
    public T getRealRoot() {
        return realRoot;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return actions;
    }

    /**
     * Children used by {@code FakeRootNode}. The only child provided by
     * these children is the real root node.
     * 
     * @param <T> type of the real root node.
     */
    static class FakeRootChildren<T extends Node> extends Children.Keys<String> {
        /** Key for the real root node. */
        private static final String ROOT_KEY = "root"; // NOI18N
        /** Real root node. */
        private T realRoot;

        /**
         * Creates a new {@code FakeRootChildren}.
         * 
         * @param realRoot real root node.
         */
        FakeRootChildren(T realRoot) {
            this.realRoot = realRoot;
            setKeys(new String[]{ROOT_KEY});
        }

        @Override
        protected Node[] createNodes(String key) {
            Node[] result;
            if (ROOT_KEY.equals(key)) {
                result = new Node[] {realRoot};
            } else {
                result = new Node[0];
            }
            return result;
        }

    }
    
}
