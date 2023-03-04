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
package org.netbeans.modules.java.lsp.server.explorer;

import org.openide.nodes.Node;

/**
 * Originally a project UI API, but is a good candidate for a path-searching SPI for
 * general explorers.
 */
public interface PathFinder {

    /**
    * Try to find a given node in the logical view.
    * If some node within the logical view tree has the supplied object
    * in its lookup, it ought to be returned if that is practical.
    * If there are multiple such nodes, the one most suitable for display
    * to the user should be returned.<BR>
    * This may be used to select nodes corresponding to files, etc.
    * The following constraint should hold:
    * <pre>
    * private static boolean isAncestor(Node root, Node n) {
    *     if (n == null) return false;
    *     if (n == root) return true;
    *     return isAncestor(root, n.getParentNode());
    * }
    * // ...
    * Node root = ...;
    * Object target = ...;
    * LogicalViewProvider lvp = ...;
    * Node n = lvp.findPath(root, target);
    * if (n != null) {
    *     assert isAncestor(root, n);
    *     Lookup.Template tmpl = new Lookup.Template(null, null, target);
    *     Collection res = n.getLookup().lookup(tmpl).allInstances();
    *     assert Collections.singleton(target).equals(new HashSet(res));
    * }
    * </pre>
    * @param root a root node. E.g. a node from {@link #createLogicalView} or some wapper
    *        (FilterNode) around the node. The provider of the functionality is
    *        responsible for finding the appropriate node in the wrapper's children.
    * @param target a target cookie, such as a {@link org.openide.loaders.DataObject}
    * @return a subnode with that cookie, or null
    */
    Node findPath(Node root, Object target);
}
