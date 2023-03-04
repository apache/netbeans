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

package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.web.jsf.api.facesmodel.Description;
import org.netbeans.modules.web.jsf.api.facesmodel.DescriptionGroup;
import org.netbeans.modules.web.jsf.api.facesmodel.DisplayName;
import org.netbeans.modules.web.jsf.api.facesmodel.Icon;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl, ads
 */
abstract class DescriptionGroupImpl extends PropertyAttributeContainerImpl 
    implements DescriptionGroup 
{
    
    protected static final List<String> DESCRIPTION_GROUP_SORTED_ELEMENTS 
        = new ArrayList<String>(3);
    static { 
        DESCRIPTION_GROUP_SORTED_ELEMENTS.add(JSFConfigQNames.DESCRIPTION.getLocalName());
        DESCRIPTION_GROUP_SORTED_ELEMENTS.add(JSFConfigQNames.DISPLAY_NAME.getLocalName());
        DESCRIPTION_GROUP_SORTED_ELEMENTS.add(JSFConfigQNames.ICON.getLocalName());
    }
            
    DescriptionGroupImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    public List<Description> getDescriptions() {
        return getChildren(Description.class);
    }
    
    public void addDescription(Description description) {
        appendChild(DESCRIPTION, description);
    }
    
    public void addDescription(int index, Description description) {
        insertAtIndex(DESCRIPTION, description, index, NavigationCase.class);
    }
    
    public void removeDescription(Description description) {
        removeChild(DESCRIPTION, description);
    }
    
    public List<DisplayName> getDisplayNames() {
        return getChildren(DisplayName.class);
    }
    
    public void addDisplayName(DisplayName displayName) {
        appendChild(DISPLAY_NAME, displayName);
    }
    
    public void addDisplayName(int index, DisplayName displayName) {
        insertAtIndex(DISPLAY_NAME, displayName, index, NavigationCase.class);
    }
    
    public void removeDisplayName(DisplayName displayName) {
        removeChild(DISPLAY_NAME, displayName);
    }
    
    public List<Icon> getIcons() {
        return getChildren(Icon.class);
    }
    
    public void addIcon(Icon icon) {
        appendChild(ICON, icon);
    }
    
    public void addIcon(int index, Icon icon) {
        insertAtIndex(ICON, icon, index, NavigationCase.class);
    }
    
    public void removeIcon(Icon icon) {
        removeChild(ICON, icon);
    }
    
    @Override
    protected List<String> getSortedListOfLocalNames() {
        return DESCRIPTION_GROUP_SORTED_ELEMENTS;
    }
    
}
