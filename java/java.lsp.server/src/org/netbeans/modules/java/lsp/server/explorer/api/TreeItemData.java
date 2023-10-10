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
package org.netbeans.modules.java.lsp.server.explorer.api;

import java.awt.Image;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Decoration(s) for the TreeItem. Allows various contributors to supply visuals or data for the remove LSP client.
 * <ul>
 * <li>the first resource URI, icon or command wins
 * <li>context values are merged
 * </ul>
 * @author sdedic
 */
public final class TreeItemData {
    public static final URI NO_URI;
    public static final String NO_COMMAND = new String("<no command>"); // NOI18N
    
    private Image iconImage;
    private String[] contextValues;
    private String command;
    private URI resourceURI;
    private boolean leaf;
    
    static {
        try {
            NO_URI = new URI(""); // NOI18N
        } catch (URISyntaxException ex) {
            throw new UnsupportedOperationException();
        }
    }

    public TreeItemData() {
    }

    public boolean isLeaf() {
        return leaf;
    }

    public TreeItemData makeLeaf() {
        this.leaf = true;
        return this;
    }
    

    public URI getResourceURI() {
        return resourceURI;
    }

    public TreeItemData setResourceURI(URI resourceURI) {
        this.resourceURI = resourceURI;
        return this;
    }

    public String[] getContextValues() {
        return contextValues;
    }
    
    public TreeItemData addContextValues(String... addValues) {
        if (addValues == null || addValues.length == 0) {
            return this;
        }
        if (this.contextValues == null) {
            this.contextValues = addValues;
        } else {
            String[] v = Arrays.copyOf(this.contextValues, this.contextValues.length + addValues.length);
            System.arraycopy(addValues, 0, v, this.contextValues.length, addValues.length);
            this.contextValues = v;
        }
        return this;
    }

    public TreeItemData setContextValues(String... contextValues) {
        this.contextValues = contextValues;
        return this;
    }
    
    public String getCommand() {
        return command;
    }

    public TreeItemData setCommand(String command) {
        this.command = command;
        return this;
    }

    public Image getIconImage() {
        return iconImage;
    }

    public TreeItemData setIconImage(Image iconImage) {
        this.iconImage = iconImage;
        return this;
    }

    public TreeItemData merge(TreeItemData data) {
        if (data.getResourceURI() != null) {
            URI u = data.getResourceURI();
            setResourceURI(u == NO_URI ? null : u);
        }
        if (data.getCommand() != null) {
            setCommand(data.getCommand());
        }
        if (data.getContextValues() != null) {
            String[] cvs = data.getContextValues();
            if (contextValues == null) {
                contextValues = cvs;
            } else {
                String[] n = Arrays.copyOf(contextValues, contextValues.length + cvs.length);
                System.arraycopy(cvs, 0, n, contextValues.length, cvs.length);
                contextValues = n;
            }
        }
        if (data.getIconImage() != null) {
            setIconImage(data.getIconImage());
        }
        leaf |= data.isLeaf();
        return this;
    }
}
