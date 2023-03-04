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
package org.netbeans.modules.maven.model.settings;

import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;

/**
 * Interface for all the components in the model.
 *
 * @author mkleint
 */
public interface SettingsComponent extends DocumentComponent<SettingsComponent> {
    
    public static final String EXTENSIBILITY_ELEMENT_PROPERTY = "extensibilityElement"; // NOI18N
    
    /**
     * Get the owner model of this component.
     * 
     * @return  the owner model
     */
    @Override
    SettingsModel getModel();
    
    void accept(SettingsComponentVisitor visitor);
        
    /**
     * Adds a child extensibility element.
     * 
     * @param ee    a new child extensibility element
     */
    void addExtensibilityElement(SettingsExtensibilityElement ee);
    
    /**
     * Removes an existing child extensibility element.
     * 
     * @param ee    an existing child extensibility element
     */
    void removeExtensibilityElement(SettingsExtensibilityElement ee);
    
    /**
     * Gets a list of all child extensibility elements.
     * 
     * @return  a list of all child extensibility elements
     */
    List<SettingsExtensibilityElement> getExtensibilityElements();
    
    /**
     * Gets a list of child extensibility elements of the given type.
     * 
     * @param type  type of child extensibility elements
     * @return  a list of child extensibility elements of the given type
     */
    <T extends SettingsExtensibilityElement> List<T> getExtensibilityElements(Class<T> type);
        

    String getChildElementText(QName qname);
    void setChildElementText(String propertyName, String text, QName qname);

}
