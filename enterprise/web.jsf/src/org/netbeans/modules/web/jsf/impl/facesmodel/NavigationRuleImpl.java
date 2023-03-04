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
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRuleExtension;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl, ads
 */
class NavigationRuleImpl extends IdentifiableDescriptionGroupImpl implements NavigationRule{
    
    protected static final List<String> NAVIGATION_RULE_SORTED_ELEMENTS = 
        new ArrayList<String>( DESCRIPTION_GROUP_SORTED_ELEMENTS.size() +2 );
    static {
        NAVIGATION_RULE_SORTED_ELEMENTS.addAll(DESCRIPTION_GROUP_SORTED_ELEMENTS);
        NAVIGATION_RULE_SORTED_ELEMENTS.add(JSFConfigQNames.FROM_VIEW_ID.getLocalName());
        NAVIGATION_RULE_SORTED_ELEMENTS.add(JSFConfigQNames.NAVIGATION_CASE.getLocalName());
    }
    
    NavigationRuleImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    NavigationRuleImpl(JSFConfigModelImpl model) {
        this(model, createElementNS(model, JSFConfigQNames.NAVIGATION_RULE));
    }
    
    public List<NavigationCase> getNavigationCases() {
        return getChildren(NavigationCase.class);
    }
    
    public void addNavigationCase(NavigationCase navigationCase) {
        appendChild(NAVIGATION_CASE, navigationCase);
    }
    
    public void addNavigationCase(int index, NavigationCase navigationCase) {
        insertAtIndex(NAVIGATION_CASE, navigationCase, index, NavigationCase.class);
    }
    
    public void removeNavigationCase(NavigationCase navigationCase) {
        removeChild(NAVIGATION_CASE, navigationCase);
    }

    /**
     * Gets from-view-id of the faces-config-navigation-ruleType.
     * @return trimmed from-view-id if any, {@code null} otherwise
     */
    public String getFromViewId() {
        String fromViewId = getChildElementText(JSFConfigQNames.FROM_VIEW_ID.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickFacesConfigFromViewIdType(fromViewId);
    }
    
    public void setFromViewId(String fromView) {
        setChildElementText(FROM_VIEW_ID, fromView, JSFConfigQNames.FROM_VIEW_ID.getQName(getNamespaceURI()));
    }
    
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule#addNavigationRuleExtension(org.netbeans.modules.web.jsf.api.facesmodel.NavigationRuleExtension)
     */
    public void addNavigationRuleExtension( NavigationRuleExtension extension )
    {
        appendChild( NAVIGATION_RULE_EXTENSION,  extension );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule#addNavigationRuleExtension(int, org.netbeans.modules.web.jsf.api.facesmodel.NavigationRuleExtension)
     */
    public void addNavigationRuleExtension( int index,
            NavigationRuleExtension extension )
    {
        insertAtIndex( NAVIGATION_RULE_EXTENSION, extension, index, 
                NavigationRuleExtension.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule#getNavigationRuleExtensions()
     */
    public List<NavigationRuleExtension> getNavigationRuleExtensions() {
        return getChildren( NavigationRuleExtension.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule#removeNavigationRuleExtension(org.netbeans.modules.web.jsf.api.facesmodel.NavigationRuleExtension)
     */
    public void removeNavigationRuleExtension( NavigationRuleExtension extension )
    {
        removeChild( NAVIGATION_RULE_EXTENSION, extension );
    }
    
    protected List<String> getSortedListOfLocalNames() {
        return NAVIGATION_RULE_SORTED_ELEMENTS;
    }
    
}
