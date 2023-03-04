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
package org.netbeans.modules.versioning.util;

import javax.swing.*;

/**
 * Groups outputs from versioning commands in one window.
 *
 * @author Maros Sandor
 */
public final class VersioningOutputManager {

    private static final VersioningOutputManager instance = new VersioningOutputManager();
    
    public static VersioningOutputManager getInstance() {
        return instance;
    }
    
    VersioningOutputManager() {
    }

    /**
     * Adds a component to the Versioning Output window and brings it to front.
     * Only one component with a given key can be displayed in the output window at any one time so if a component
     * added with the same key already exists in the Versioning Output window, it is removed. 
     * The supplied component's name, obtained by getName(), is used as a title for the component. 
     * 
     * @param key category key of the component or null if the component should be independent
     * @param component component to display in the Versioning Output window
     */
    public void addComponent(String key, JComponent component) {
        VersioningOutputTopComponent tc = VersioningOutputTopComponent.getInstance();
        tc.addComponent(key, component);
        tc.open();
    }
}
