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

import javax.swing.Action;
import javax.swing.JComponent;

/**
 *
 *  
 * @author Tomas Stupka
 */
public interface TeamDashboardComponentProvider {
    
    /**
     * Creates a component composed from the given sections components 
     * 
     * @param sections
     * @return 
     */
    public JComponent create(Section... sections);   
   
    /**
     * Represents a section in the team Dashboard - e.g. Issues, Builds, Sources
     * Usable in containers containing more UI components/panels
     */
    public interface Section {
        
        /**
         * Call this if the section is to be expanded or collapsed
         * 
         * @param expand 
         */
        public void setExpanded(boolean expand);
        
        /**
         * Determines whether the section is expanded or not
         * @return <code>true</code> in case the section is expanded, otherwise <code>false</code>
         */
        public boolean isExpanded();
        
        /**
         * Returns a sections component
         * @return 
         */
        public JComponent getComponent();
        
        /**
         * The sections display name
         * @return 
         */
        public String getDisplayName();
    }
    
    /**
     * Creates a component which is shown in case there is no project selected in the dashboard.
     * 
     * @param newServerAction provided in case there should be some UI to create a new server instance. Otherwise <code>null</code>.
     * @return the no project selected component
     */
    public JComponent createNoProjectComponent(Action newServerAction);
}
