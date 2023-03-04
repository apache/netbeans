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

package org.netbeans.modules.websvc.design.configuration;

import java.awt.Image;
import java.beans.PropertyChangeListener;

/**
 *
 * @author Ajit Bhate
 */
public interface WSConfiguration {
    
    public static String PROPERTY="value";

    public static String PROPERTY_ENABLE="enabled";
    
    /**
     * Returns the user interface component for this WSConfiguration.
     *
     * @return  the user interface component.
     */
    java.awt.Component getComponent();

    /**
     * Returns the user-oriented description of this WSConfiguration, for use in
     * tooltips in the usre interface.
     *
     * @return  the human-readable description of this WSConfiguration.
     */
    String getDescription();

    /**
     * Returns the display icon of this WSConfiguration.
     *
     * @return  icon for this WSConfiguration.
     */
    Image getIcon();

    /**
     * Returns the display name of this WSConfiguration.
     *
     * @return  title for this WSConfiguration.
     */
    String getDisplayName();
    
    /**
     *  Called to apply changes made by the user 
     */ 
    void set();
    
    /**
     *  Called to cancel changes made by the user
     */
    void unset();
    
    
    /**
     * Used to determine if a functionality is active.
     */ 
    boolean isSet();

    /**
     * Used to determine if a functionality is enabled.
     */ 
    boolean isEnabled();

    /**
     * Allows to register for changes on the client.
     */ 
    public void registerListener(PropertyChangeListener listener);

    /**
     * Required to unregister the listeners when not needed.
     */ 
    public void unregisterListener(PropertyChangeListener listener);
        
}
