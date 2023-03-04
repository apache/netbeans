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

import java.util.logging.Level;
import java.util.logging.Logger;


/** Simple implementation of <code>Node.Handle</code>.
* When created by {@link #createHandle} it
* looks for the parent of the node and stores the node's name.
* When {@link #getNode} is then called, it tries to restore the
* parent and then to walk down to the child.
* <p>Note that if most nodes use <code>DefaultHandle</code>, this
* may walk up to the root recursively. Otherwise, some other sort
* of handle may provide the termination case.
*
* @author Jaroslav Tulach, Jesse Glick
*/
public final class DefaultHandle extends Object implements Node.Handle {
    private static final long serialVersionUID = -8739127064355983273L;

    /** parent handle */
    private Node.Handle parent;

    /** path to the node (just one hop, really) */
    private String path;

    /* Create a new handle.
    * @param parent handle for the parent node
    * @param path desired name of child
    */
    DefaultHandle(Node.Handle parent, String path) {
        this.parent = parent;
        this.path = path;
    }

    /** Find the node.
    * @return the found node
    * @exception IOException if the parent cannot be recreated
    * @exception NodeNotFoundException if the path is not valid (exception may be examined for details)
    */
    public Node getNode() throws java.io.IOException {
        Node parentNode = parent.getNode();
        Node child = parentNode.getChildren().findChild(path);

        if (child != null) {
            return child;
        } else {
            throw new NodeNotFoundException(parentNode, path, 0);
        }
    }

    /** Create a handle for a given node.
    * A handle cannot be created under these circumstances:
    * <ol>
    * <li>The node has no name.
    * <li>The node has no parent.
    * <li>The parent has no handle.
    * <li>The parent is incapable of finding its child by the supplied name.
    * </ol>
    * @param node the node to create a handler for
    * @return the handler, or <code>null</code> if a handle cannot be created
    */
    public static DefaultHandle createHandle(final Node node) {
        try {
            Children.PR.enterReadAccess();

            String childPath = node.getName();

            if (childPath == null) {
                return null;
            }

            Node parentNode = node.getParentNode();

            if (parentNode == null) {
                return null;
            }

            Node foundChild = parentNode.getChildren().findChild(childPath);
            if (foundChild != node) {
                Logger.getLogger(DefaultHandle.class.getName()).log(Level.WARNING,
                        "parent could not find own child: node={0} parentNode={1} childPath={2} foundChild={3}",
                        new Object[] {node, parentNode, childPath, foundChild});
                return null;
            }

            Node.Handle parentHandle = parentNode.getHandle();

            if (parentHandle == null) {
                return null;
            }

            return new DefaultHandle(parentHandle, childPath);
        } finally {
            Children.PR.exitReadAccess();
        }
    }

    public String toString() {
        return "DefaultHandle[" + parent + "|" + path + "]"; // NOI18N
    }
}
