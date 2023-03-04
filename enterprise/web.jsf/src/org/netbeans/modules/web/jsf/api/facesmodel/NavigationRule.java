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

package org.netbeans.modules.web.jsf.api.facesmodel;

import java.util.List;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 * The "navigation-rule" element represents an individual
 * decision rule that will be utilized by the default
 * NavigationHandler implementation to make decisions on
 * what view should be displayed next, based on the
 * view id being processed.
 * @author Petr Pisl, ads
 */
public interface NavigationRule extends FacesConfigElement, DescriptionGroup,
    IdentifiableElement
{
    
    String FROM_VIEW_ID = JSFConfigQNames.FROM_VIEW_ID.getLocalName();
    String NAVIGATION_CASE = JSFConfigQNames.NAVIGATION_CASE.getLocalName();
    String NAVIGATION_RULE_EXTENSION = JSFConfigQNames.NAVIGATION_RULE_EXTENSION.getLocalName();
    
    
    List<NavigationCase> getNavigationCases();
    void addNavigationCase(NavigationCase navigationCase);
    void addNavigationCase(int index, NavigationCase navigationCase);
    void removeNavigationCase(NavigationCase navigationCase);
    
    String getFromViewId();
    void setFromViewId(String fromView);
    
    List<NavigationRuleExtension> getNavigationRuleExtensions();
    void addNavigationRuleExtension( NavigationRuleExtension extension );
    void removeNavigationRuleExtension( NavigationRuleExtension extension);
    void addNavigationRuleExtension( int index , NavigationRuleExtension extension);
    
}
