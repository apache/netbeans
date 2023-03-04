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

package org.netbeans.modules.java.j2seproject.api;

import java.util.Map;
import javax.swing.JComponent;

import org.netbeans.api.project.Project;

/**
 * Provider of component that will be added to Run customizer panel that will
 * be used for additional customization of set of properties affected by given
 * run configuration. Implementation of the interface should be registered using {@link org.openide.util.lookup.ServiceProvider}.
 * This interface is deprecated, use {@link org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider} instead.
 * 
 * @deprecated 
 * @author Milan Kubec
 * @since 1.10
 */
@Deprecated
public interface J2SERunConfigProvider {
    
    /**
     * Provides component that is added to Run Customizer panel of j2seproject
     * 
     * @param proj project to create the customizer component for
     * @param listener listener to be notified when properties should be updated
     */
    JComponent createComponent(Project proj, ConfigChangeListener listener);
    
    /**
     * Method is called when the config is changed (or created), 
     * component is updated according to properties of the config
     * 
     * @param props all properties (shared + private) of the new config;
     *        properites are not evaluated
     */
    void configUpdated(Map<String,String> props);
    
    /**
     * Callback listener for setting properties that are changed by interaction 
     * with the component
     */
    interface ConfigChangeListener {
        /**
         * Method is called when properties should be updated, null prop value 
         * means property will be removed from the property file, only shared 
         * properties are updated; properties are not evaluated
         * 
         * @param updates map holding updated properties
         */
        void propertiesChanged(Map<String,String> updates);
    }
    
}
