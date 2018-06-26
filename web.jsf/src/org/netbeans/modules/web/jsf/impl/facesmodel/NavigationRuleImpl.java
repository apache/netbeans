/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
