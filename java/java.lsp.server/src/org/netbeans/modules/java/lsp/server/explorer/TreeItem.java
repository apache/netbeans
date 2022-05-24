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

import java.net.URI;
import java.util.Objects;
import org.eclipse.lsp4j.MarkupContent;
import static org.netbeans.modules.java.lsp.server.protocol.TextDocumentServiceImpl.html2MD;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.Node;

public class TreeItem {
    enum CollapsibleState {
        None, Collapsed, Expanded
    }

    // accessibilityInformation?: AccessibilityInformation
    public CollapsibleState collapsibleState;
    // executed when the tree item is selected.
    // command?: Command
    public String command;
    // contribute item specific actions in the tree
    public String contextValue;
    // ?: string | boolean
    // rendered less prominent. When true, it is derived from resourceUri
    public String description;
    // ?: string | Uri | {dark: string | Uri, light: string | Uri} | ThemeIcon
    public URI iconUri;
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
    public Object tooltip;
    
    // NBLS-specific items, to be processed by the client:
    // Metadata for the icon.
    public IconDescriptor iconDescriptor;
    
    /**
     * Metadata that describe an icon origin or contents.
     */
    public static class IconDescriptor {
        /**
         * Base URI / imageId of the icon.
         */
        public URI baseUri;
        
        /**
         * Supplemental IDs from composed merged-in images or applied filters.
         */
        public String[] composition;
    }

    public TreeItem() {
    }
    
    static Object html2md(String s) {
        if (s != null && s.startsWith("<html>")) {
            MarkupContent markup = new MarkupContent();
            markup.setKind("markdown");
            markup.setValue(html2MD(s));
            return markup;
        } else {
            return s;
        }
    }

    TreeItem(int id, Node n, boolean wasExpanded, String contextValue) {
        if (n.isLeaf()) {
            collapsibleState = TreeItem.CollapsibleState.None;
        } else {
            collapsibleState = wasExpanded ? TreeItem.CollapsibleState.Expanded : TreeItem.CollapsibleState.Collapsed;
        }
        this.contextValue = contextValue;
        this.id = id;
        this.name = n.getName();
        this.label = n.getDisplayName();
        final String desc = n.getShortDescription();
        if (!Objects.equals(this.label, desc)) {
            this.tooltip = html2md(desc);
        }

        FileObject fo = n.getLookup().lookup(FileObject.class);
        if (fo != null) {
            this.resourceUri = URLMapper.findURL(fo, URLMapper.EXTERNAL).toString();
        }
    }
    
    public String toString() {
        return String.format(
            "TreeItem[%s, id = %d, resource = %s, context = %s",
            name, id, resourceUri, contextValue
        );
    }
}
