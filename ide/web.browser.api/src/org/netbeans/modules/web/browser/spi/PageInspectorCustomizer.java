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

package org.netbeans.modules.web.browser.spi;

import java.beans.PropertyChangeListener;

/**
 * A contract through which Project can communicate to PageInspector. Instance
 * of this class is expected in project's lookup.
 */
public interface PageInspectorCustomizer {

    public static final String PROPERTY_HIGHLIGHT_SELECTION = "highlight.selection";
    
    /**
     * Controls whether hovering over an element in IDE's Live DOM tree should 
     * highlight the element in browser or not.
     * Note: hovering over elements directly in browser (and impact of such action on IDE) is
     * not controlled by this option - Select Mode in browser is.
     */
    boolean isHighlightSelectionEnabled();
    
    void addPropertyChangeListener(PropertyChangeListener l);
    
    void removePropertyChangeListener(PropertyChangeListener l);
}
