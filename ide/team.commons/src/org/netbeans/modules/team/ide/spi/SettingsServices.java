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

package org.netbeans.modules.team.ide.spi;

/**
 * Provides access to various settings related services so that 
 * the expected consumers (bugtracking and team modules) 
 * are able to independently access different IDE settings functionality 
 * and UI (like e.g. NetBeans or JDev). 
 * 
 * @author Tomas Stupka
 */
public interface SettingsServices {
    
    /**
     * The location for all team settings. To be used in team settings 
     * registrations or when accessing the particular settings section.
     * 
     * @see org.netbeans.spi.options.OptionsPanelController.SubRegistration
     */
    public static final String TEAM_SETTINGS_LOCATION = "Team";
    
    /**
     * Id for Tasks settings. To be used in team settings 
     * registrations or when accessing the particular settings section.
     * 
     * @see org.netbeans.spi.options.OptionsPanelController.SubRegistration
     */
    public static final String TASKS_SETTINGS_ID = "Tasks";
    
    /**
     * Id for ODCS settings. To be used in team settings 
     * registrations or when accessing the particular settings section.
     * 
     * @see org.netbeans.spi.options.OptionsPanelController.SubRegistration
     */
    public static final String ODCS_SETTINGS_ID = "Odcs";
    
    /**
     * Represents a particular settings section (page).
     */
    enum Section {
        /**
         * Proxy settings. Used in {@link #openSection(org.netbeans.modules.team.ide.spi.SettingsServices.Section)}
         * to open a UI to change the Proxy settings page.
         */
        PROXY,
        /**
         * Tasks settings. Used in {@link #openSection(org.netbeans.modules.team.ide.spi.SettingsServices.Section)}
         * to open a UI to change the Tasks settings page.
         */
        TASKS,
        /**
         * ODCS settings. Used in {@link #openSection(org.netbeans.modules.team.ide.spi.SettingsServices.Section)}
         * to open a UI to change the ODCS settings page.
         */
        ODCS
    }
    
    /**
     * Determines whether the capability of opening a particular settings UI is available or not.
     * 
     * @param section the particular settings UI
     * @return <code>true</code> if there is a way to open a settings section, otherwise <code>false</code>
     */
    public boolean providesOpenSection(Section section);
    
    /**
     * Opens a particular settings UI - e.g. proxy or tasks settings
     * 
     * @param section a particular settings section to be opened
     */
    public void openSection(Section section);
}
