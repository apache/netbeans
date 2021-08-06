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

final class TreeItem {
    enum CollapsibleState {
        None, Collapsed, Expanded
    }

    // accessibilityInformation?: AccessibilityInformation
    CollapsibleState collapsibleState;
    // executed when the tree item is selected.
    // command?: Command
    // contribute item specific actions in the tree
    String contextValue;
    // ?: string | boolean
    // rendered less prominent. When true, it is derived from resourceUri
    String description;
    // ?: string | Uri | {dark: string | Uri, light: string | Uri} | ThemeIcon
    String iconPath;
    // id for the tree item that has to be unique across tree.
    // The id is used to preserve the selection and expansion state of the tree item.
    String id;
    // human-readable string describing this item
    // ?: string | TreeItemLabel
    String label;
    // resourceUri?: Uri
    String resourceUri;
    // ?: string | MarkdownString | undefined
    String tooltip;
}
