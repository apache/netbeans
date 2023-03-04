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
package org.netbeans.modules.web.clientproject.spi.platform;

import org.netbeans.spi.project.ActionProvider;

/**
 * Hook into client side project type for different browsers to provider their
 * own customizer, actions, configurations, etc.
 */
public interface ClientProjectEnhancedBrowserImplementation {

    /**
     * Browser's customizer.
     * @return can return null if none
     */
    ProjectConfigurationCustomizer getProjectConfigurationCustomizer();

    /**
     * Persist changes done in browser's customizer.
     */
    void save();
    
    /**
     * Browser's action provider.
     * @return can return null
     */
    ActionProvider getActionProvider();

    /**
     * Browser's handler for changes in project sources.
     * @return can return null
     */
    RefreshOnSaveListener getRefreshOnSaveListener();

    /**
     * Notification to browser that is not active anymore.
     */
    void deactivate();

    /**
     * Close the browser if it is opened.
     * @since 1.42
     */
    void close();

    boolean isHighlightSelectionEnabled();
    
    boolean isAutoRefresh();

}
