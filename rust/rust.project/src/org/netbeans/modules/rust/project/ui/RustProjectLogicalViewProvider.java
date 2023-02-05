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
package org.netbeans.modules.rust.project.ui;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.rust.project.RustProject;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;

/**
 * Rust Project LogicalViewProvider
 *
 * @see
 * <a href="https://bits.netbeans.org/dev/javadoc/org-netbeans-modules-projectuiapi/org/netbeans/spi/project/ui/LogicalViewProvider.html">LogicalViewProvider</a>
 */
public class RustProjectLogicalViewProvider implements LogicalViewProvider {

    private static final Logger LOG = Logger.getLogger(RustProjectLogicalViewProvider.class.getName());

    private final RustProject project;

    public RustProjectLogicalViewProvider(RustProject project) {
        this.project = project;
    }

    @Override
    public Node createLogicalView() {
        return new RustProjectRootNode(project);
    }

    /**
     * Same implementation as PhpLogicalViewProvider.java
     *
     * @param root The root node
     * @param target What to find
     * @return The Node, or null if not found
     */
    @Override
    public Node findPath(Node root, Object target) {

        LOG.log(Level.INFO, "RustProjectLogicalViewProvider.findPath( {0}, {1} ({2}) )", new Object[]{root, target, target == null ? "null" : target.getClass().getName()});
        Project p = root.getLookup().lookup(Project.class);
        if (p == null) {
            return null;
        }
        // Check each child node in turn.
        Node[] children = root.getChildren().getNodes(true);
        for (Node node : children) {
            if (target instanceof DataObject || target instanceof FileObject) {
                FileObject kidFO = node.getLookup().lookup(FileObject.class);
                if (kidFO == null) {
                    continue;
                }
                // Copied from org.netbeans.spi.java.project.support.ui.TreeRootNode.PathFinder.findPath:
                FileObject targetFO = null;
                if (target instanceof DataObject) {
                    targetFO = ((DataObject) target).getPrimaryFile();
                } else {
                    targetFO = (FileObject) target;
                }
                Project owner = FileOwnerQuery.getOwner(targetFO);
                if (!p.equals(owner)) {
                    return null; // Don't waste time if project does not own the fileobject
                }
                if (kidFO == targetFO) {
                    return node;
                } else if (FileUtil.isParentOf(kidFO, targetFO)) {
                    String relPath = FileUtil.getRelativePath(kidFO, targetFO);

                    // first path without extension (more common case)
                    String[] path = relPath.split("/"); // NOI18N
                    path[path.length - 1] = targetFO.getName();

                    // first try to find the file without extension (more common case)
                    Node found = findNode(node, path);
                    if (found == null) {
                        // file not found, try to search for the name with the extension
                        path[path.length - 1] = targetFO.getNameExt();
                        found = findNode(node, path);
                    }
                    if (found == null) {
                        // can happen for tests that are underneath sources directory
                        continue;
                    }
                    if (hasObject(found, target)) {
                        return found;
                    }
                    Node parent = found.getParentNode();
                    Children kids = parent.getChildren();
                    children = kids.getNodes();
                    for (Node child : children) {
                        if (hasObject(child, target)) {
                            return child;
                        }
                    }
                }
            }
        }
        return null;
    }

    private Node findNode(Node start, String[] path) {
        Node found = null;
        try {
            found = NodeOp.findPath(start, path);
        } catch (NodeNotFoundException ex) {
            // ignored
        }
        return found;
    }

    private boolean hasObject(Node node, Object obj) {
        if (obj == null) {
            return false;
        }
        FileObject fileObject = node.getLookup().lookup(FileObject.class);
        if (fileObject == null) {
            return false;
        }
        if (obj instanceof DataObject) {
            DataObject dataObject = node.getLookup().lookup(DataObject.class);
            if (dataObject == null) {
                return false;
            }
            if (dataObject.equals(obj)) {
                return true;
            }
            return hasObject(node, ((DataObject) obj).getPrimaryFile());
        } else if (obj instanceof FileObject) {
            return obj.equals(fileObject);
        }
        return false;
    }

}
