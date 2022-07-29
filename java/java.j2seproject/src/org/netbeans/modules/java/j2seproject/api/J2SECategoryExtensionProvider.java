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

package org.netbeans.modules.java.j2seproject.api;

import java.util.Map;
import javax.swing.JComponent;

import org.netbeans.api.project.Project;

/**
 * Provider of component that will be added to customizer panel of category specified
 * by getCategory() return value. Component will be used for additional customization 
 * of a set of additional properties not customized by standard J2SE Project controls.
 * Implementation of the interface should be registered using {@link org.netbeans.spi.project.ProjectServiceProvider}.
 * 
 * @author Petr Somol
 * @author Milan Kubec
 * @since 1.46
 */
public interface J2SECategoryExtensionProvider {
    
    /**
     * Enumeration of categories for which extension is currently allowed
     */
    enum ExtensibleCategory { PACKAGING, RUN, APPLICATION, DEPLOYMENT }
            
    /**
     * Provides identifier of category whose panel should be extended by this component provider
     * 
     * @return identifier of the category to be extended
     */
    ExtensibleCategory getCategory();
    
    /**
     * Provides component that is added to the customizer panel of j2seproject
     * selected by getCategory() return value
     * 
     * @param proj project to create the customizer component for
     * @param listener listener to be notified when properties should be updated
     * @return extension panel to be added to the specified category
     */
    JComponent createComponent(Project proj, ConfigChangeListener listener);
    
    /**
     * Method is called when properties exposed by the provided component
     * get changed externally and the component needs to be updated accordingly
     * 
     * @param props all properties (shared + private);
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
