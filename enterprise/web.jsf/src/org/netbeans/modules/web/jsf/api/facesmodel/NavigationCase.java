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

import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 * The "navigation-case" element describes a particular
 * combination of conditions that must match for this case to
 * be executed, and the view id of the component tree that
 * should be selected next.
 * @author Petr Pisl, ads
 */
public interface NavigationCase extends JSFConfigComponent, DescriptionGroup, 
    IdentifiableElement 
{
    
    String FROM_ACTION = JSFConfigQNames.FROM_ACTION.getLocalName();
    String FROM_OUTCOME = JSFConfigQNames.FROM_OUTCOME.getLocalName();
    String TO_VIEW_ID = JSFConfigQNames.TO_VIEW_ID.getLocalName();
    String REDIRECT = JSFConfigQNames.REDIRECT.getLocalName();
    
    String IF = JSFConfigQNames.IF.getLocalName();
    
    
    // TODO : Incorrect signature. FromAction should be separate element. 
    // It has additional attribute.
    public String getFromAction();
    public void setFromAction(String fromAction);
    
    public String getFromOutcome();
    public void setFromOutcome(String fromOutcome);
    
    /**
     * This method along with getter should not be used for JSF 2.0 spec.
     * Redirect has number of subelements and attributes.
     * Accessor methods to Redirect should be used instead.
     */
    public void setRedirected(boolean redirect);
    /**
     * This method along with setter should not be used for JSF 2.0 spec.
     * Redirect has number of subelements and attributes.
     * Accessor methods to Redirect should be used instead.
     */
    public boolean isRedirected();
    
    public String getToViewId();
    public void setToViewId(String toViewId);
    
    If getIf();
    void setIf( If iff );
    
    Redirect getRedirect();
    void setRedirect(Redirect redirect);
}
