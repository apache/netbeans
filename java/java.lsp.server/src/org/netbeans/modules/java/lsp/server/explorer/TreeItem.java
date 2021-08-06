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
package org.netbeans.modules.java.lsp.server.explorer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.Node;

public final class TreeItem {
    enum CollapsibleState {
        None, Collapsed, Expanded
    }

    // accessibilityInformation?: AccessibilityInformation
    public CollapsibleState collapsibleState;
    // executed when the tree item is selected.
    // command?: Command
    // contribute item specific actions in the tree
    public String contextValue;
    // ?: string | boolean
    // rendered less prominent. When true, it is derived from resourceUri
    public String description;
    // ?: string | Uri | {dark: string | Uri, light: string | Uri} | ThemeIcon
    public String iconPath;
    // id for the tree item that has to be unique across tree.
    // The id is used to preserve the selection and expansion state of the tree item.
    public int id;
    // programatic name of the node
    public String name;
    // human-readable string describing this item
    // ?: string | TreeItemLabel
    public String label;
    // resourceUri?: Uri
    public String resourceUri;
    // ?: string | MarkdownString | undefined
    public String tooltip;

    public TreeItem() {
    }

    private TreeItem(int id, Node n) {
        if (n.isLeaf()) {
            collapsibleState = TreeItem.CollapsibleState.None;
        } else {
            collapsibleState = TreeItem.CollapsibleState.Collapsed;
        }
        this.id = id;
        this.name = n.getName();
        this.label = n.getDisplayName();
        final String desc = n.getShortDescription();
        this.description = Objects.equals(this.label, desc) ? null : desc;
        this.tooltip = n.getHtmlDisplayName();

        FileObject fo = n.getLookup().lookup(FileObject.class);
        if (fo != null) {
            this.resourceUri = URLMapper.findURL(fo, URLMapper.EXTERNAL).toString();
        }
    }

    private static int counter = 0;
    private static final Map<Integer, Node> MAP = new HashMap<>();

    public static synchronized int findId(Node n) {
        Object lspId = n.getValue("lspId");
        if (!(lspId instanceof Integer)) {
            lspId = ++counter;
            n.setValue("lspId", lspId);
            MAP.put((Integer) lspId, n);
        }
        return (int) lspId;
    }

    public static TreeItem find(Node n) {
        return new TreeItem(findId(n), n);
    }

    public static synchronized TreeItem find(int id) {
        Node n = findNode(id);
        return n == null ? null : find(n);
    }

    public synchronized static Node findNode(int id) {
        return MAP.get(id);
    }

}
