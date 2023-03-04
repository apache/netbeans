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
package org.openide.nodes;

import java.io.IOException;


/** Exception indicating that a node could not be found while
* traversing a path from the root.
*
* @author Jaroslav Tulach
*/
public final class NodeNotFoundException extends IOException {
    static final long serialVersionUID = 1493446763320691906L;

    /** closest node */
    private final Node node;

    /** name of child not found */
    private final String name;

    /** depth of not founded node. */
    private final int depth;

    /** Constructor.
    * @param node closest found node to the one being looked for
    * @param name name of child not found in that node
    * @param depth depth of the node that was found
    */
    NodeNotFoundException(Node node, String name, int depth) {
        this.node = node;
        this.name = name;
        this.depth = depth;
    }

    /** Get the closest node to the target that was able to be found.
     * @return the closest node
    */
    public Node getClosestNode() {
        return node;
    }

    /** Get the name of the missing child of the closest node.
     * @return the name of the missing child
    */
    public String getMissingChildName() {
        return name;
    }

    /** Getter for the depth of the closest node found.
    * @return the depth (0 for the start node, 1 for its child, etc.)
    */
    public int getClosestNodeDepth() {
        return depth;
    }

    public @Override String getMessage() {
        return "Could not find child '" + name + "' of " + node + " at depth " + depth; // NOI18N
    }

}
