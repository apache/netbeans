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
package org.openide.explorer.view;

import org.openide.nodes.Node;

import javax.swing.tree.*;


/** This class provide access to thread safe layer that
* reflects the hierarchy of Nodes, but is updated only in
* event dispatch thread (in contrast to nodes that can be updated from any thread).
* That is why this class is useful for writers of explorer views,
* because it guarantees that all changes will be done safely.
* <P>
* NodeTreeModel, NodeListModel, etc. use these objects as its
* model values.
*
* @author Jaroslav Tulach
*/
public class Visualizer extends Object {
    /** No constructor. */
    private Visualizer() {
    }

    /** Methods that create a tree node for given node.
    * The tree node reflects the state of the associated node as close
    * as possible, but is updated asynchronously in event dispatch thread.
    * <P>
    * This method can be called only from AWT-Event dispatch thread.
    *
    * @param node node to create safe representant for
    * @return tree node that represents the node
    */
    public static TreeNode findVisualizer(Node node) {
        return VisualizerNode.getVisualizer(null, node);
    }

    /** Converts visualizer object back to its node representant.
    *
    * @param visualizer visualizer create by findVisualizer method
    * @return node associated with the visualizer
    * @exception ClassCastException if the parameter is invalid
    */
    public static Node findNode(Object visualizer) {
        if (visualizer instanceof Node) {
            return (Node) visualizer;
        } else {
            return ((VisualizerNode) visualizer).node;
        }
    }
}
