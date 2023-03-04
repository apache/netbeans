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
import java.util.Collections;
import java.util.List;

import org.netbeans.modules.web.jsf.api.facesmodel.If;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.Redirect;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl, ads
 */
public class NavigationCaseImpl extends IdentifiableDescriptionGroupImpl 
    implements NavigationCase
{
    
    protected static final List<String> NAVIGATION_CASE_SORTED_ELEMENTS 
        = new ArrayList<String>( DESCRIPTION_GROUP_SORTED_ELEMENTS.size() +5 );
    static {
        NAVIGATION_CASE_SORTED_ELEMENTS.addAll(DESCRIPTION_GROUP_SORTED_ELEMENTS);
        NAVIGATION_CASE_SORTED_ELEMENTS.add(JSFConfigQNames.FROM_ACTION.getLocalName());
        NAVIGATION_CASE_SORTED_ELEMENTS.add(JSFConfigQNames.FROM_OUTCOME.getLocalName());
        NAVIGATION_CASE_SORTED_ELEMENTS.add(JSFConfigQNames.IF.getLocalName());
        NAVIGATION_CASE_SORTED_ELEMENTS.add(JSFConfigQNames.TO_VIEW_ID.getLocalName());
        NAVIGATION_CASE_SORTED_ELEMENTS.add(JSFConfigQNames.REDIRECT.getLocalName());
    }
    
    /** Creates a new instance of NavigationCaseImpl */
    public NavigationCaseImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    public NavigationCaseImpl(JSFConfigModelImpl model) {
        this(model, createElementNS(model, JSFConfigQNames.NAVIGATION_CASE));
    }
    
    public String getFromAction() {
        return getChildElementText(JSFConfigQNames.FROM_ACTION.getQName(getNamespaceURI()));
    }
    
    public void setFromAction(String fromAction) {
        setChildElementText(FROM_ACTION, fromAction, 
                JSFConfigQNames.FROM_ACTION.getQName(getNamespaceURI()));
    }

    /**
     * Gets from-outcome of the faces-config-navigation-caseType.
     * @return trimmed from-outcome if any, {@code null} otherwise
     */
    public String getFromOutcome() {
        String fromOutcome = getChildElementText(JSFConfigQNames.FROM_OUTCOME.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickString(fromOutcome);
    }
    
    public void setFromOutcome(String fromOutcome) {
        setChildElementText(FROM_OUTCOME, fromOutcome, 
                JSFConfigQNames.FROM_OUTCOME.getQName(getNamespaceURI()));
    }

    public void setRedirected(boolean redirect) {
        if (redirect)
            setChildElementText(REDIRECT, "", 
                    JSFConfigQNames.REDIRECT.getQName(getNamespaceURI()));
        else
            setChildElementText(REDIRECT, null, 
                    JSFConfigQNames.REDIRECT.getQName(getNamespaceURI()));
    }
    
    public boolean isRedirected() {
        return (null != getChildElementText(
                JSFConfigQNames.REDIRECT.getQName(getNamespaceURI())));
    }
    
    public String getToViewId() {
        return getChildElementText(
                JSFConfigQNames.TO_VIEW_ID.getQName(getNamespaceURI()));
    }
    
    public void setToViewId(String toViewId) {
        setChildElementText(TO_VIEW_ID, toViewId, 
                JSFConfigQNames.TO_VIEW_ID.getQName(getNamespaceURI()));
    }
    
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase#getIf()
     */
    public If getIf() {
        return getChild(If.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase#setIf(org.netbeans.modules.web.jsf.api.facesmodel.If)
     */
    public void setIf( If iff ) {
        setChild( If.class, IF, iff , Collections.EMPTY_LIST);
        reorderChildren();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase#getRedirect()
     */
    public Redirect getRedirect() {
        return getChild(Redirect.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase#setRedirect(org.netbeans.modules.web.jsf.api.facesmodel.Redirect)
     */
    public void setRedirect(Redirect redirect) {
        setChild( Redirect.class, REDIRECT, redirect, Collections.EMPTY_LIST);
        reorderChildren();
    }
    
    protected List<String> getSortedListOfLocalNames() {
        return NAVIGATION_CASE_SORTED_ELEMENTS;
    }

}
