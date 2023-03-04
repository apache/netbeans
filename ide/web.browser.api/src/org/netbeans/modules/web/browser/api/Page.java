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
package org.netbeans.modules.web.browser.api;

import java.beans.PropertyChangeListener;
import java.util.List;
import org.openide.nodes.Node;

/**
 * Model of web page.
 */
public abstract class Page {

    /** Name of the property that is fired when a new document is loaded into the inspected browser pane. */
    public static final String PROP_DOCUMENT = "document"; // NOI18N
    /** Name of the property that is fired when the set of selected elements is changed. */
    public static final String PROP_SELECTED_NODES = "selectedNodes"; // NOI18N
    /** Name of the property that is fired when the set of highlighted nodes is changed. */
    public static final String PROP_HIGHLIGHTED_NODES = "highlightedNodes"; // NOI18N
    /** Name of the property that is fired when nodes selection changes in the browser. */
    public static final String PROP_BROWSER_SELECTED_NODES = "browserSelectedNodes"; // NOI18N

    /**
     * Adds a property change listener.
     * 
     * @param listener listener to add.
     */
    public abstract void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes a property change listener.
     * 
     * @param listener listener to remove.
     */
    public abstract void removePropertyChangeListener(PropertyChangeListener listener);
    
    /**
     * Returns highlighted nodes.
     * 
     * @return highlighted nodes.
     */
    public abstract List<? extends Node> getHighlightedNodes();
    
    /**
     * Returns the document URL.
     * 
     * @return document URL.
     */
    public abstract String getDocumentURL();

    /**
     * Sets the selected nodes.
     * 
     * @param nodes nodes to select in the page.
     */
    public abstract void setSelectedNodes(List<? extends Node> nodes);

    /**
     * Returns selected nodes.
     * 
     * @return selected nodes.
     */
    public abstract List<? extends Node> getSelectedNodes();

    /**
     * Sets the highlighted nodes.
     * 
     * @param nodes highlighted nodes.
     */
    public abstract void setHighlightedNodes(List<? extends Node> nodes);

    /**
     * Returns the document node.
     * 
     * @return document node.
     */
    public abstract Node getDocumentNode();
    
}
