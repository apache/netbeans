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

package org.netbeans.modules.debugger.jpda.ui.options;

/**
 *
 * @author Martin Entlicher
 */
public abstract class StorablePanel extends javax.swing.JPanel {
    
    /**
     * Read settings and initialize GUI
     */
    public abstract void load();

    /**
     * Store modified settings
     */
    public abstract void store();
    
    /**
     * Notify whether any settings are modified. This will help the infrastructure decide 
     * if the Apply button in the Options window should be enabled or disabled.
     * @return <code>true</code> if any setting is modified by the user in the UI, <code>false</code> otherwise.
     * @since 1.41
     */
    public abstract boolean isChanged();
    
    public static interface Provider {
        
        /**
         * Provides the display name of the panel.
         * @return The display name of the panel
         */
        public abstract String getPanelName();

        public StorablePanel getPanel();
    }

}
