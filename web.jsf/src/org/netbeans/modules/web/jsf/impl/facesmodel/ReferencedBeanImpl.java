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
import org.netbeans.modules.web.jsf.api.facesmodel.ReferencedBean;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class ReferencedBeanImpl extends IdentifiableDescriptionGroupImpl implements
        ReferencedBean
{

    ReferencedBeanImpl( JSFConfigModelImpl model, Element element ) {
        super(model, element);
    }
    
    ReferencedBeanImpl( JSFConfigModelImpl model ) {
        super(model, createElementNS(model, JSFConfigQNames.REFERENCED_BEAN));
    }

    /**
     * Gets referenced-bean-class of the faces-config-referenced-beanType.
     * @return trimmed referenced-bean-class if any, {@code null} otherwise
     */
    public String getReferencedBeanClass() {
        String beanClass = getChildElementText(JSFConfigQNames.REFERENCED_BEAN_CLASS.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickFullyQualifiedClassType(beanClass);
    }

    /**
     * Gets referenced-bean-name of the faces-config-referenced-beanType.
     * @return trimmed referenced-bean-name if any, {@code null} otherwise
     */
    public String getReferencedBeanName() {
        String beanName = getChildElementText(JSFConfigQNames.REFERENCED_BEAN_NAME.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickJavaIdentifierType(beanName);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.ReferencedBean#setReferencedBeanClass(java.lang.String)
     */
    public void setReferencedBeanClass( String clazz ) {
        setChildElementText(REFERENCED_BEAN_CLASS, clazz, 
                JSFConfigQNames.REFERENCED_BEAN_CLASS.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.ReferencedBean#setReferencedBeanName(java.lang.String)
     */
    public void setReferencedBeanName( String name ) {
        setChildElementText(REFERENCED_BEAN_NAME, name, 
                JSFConfigQNames.REFERENCED_BEAN_NAME.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent#accept(org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor)
     */
    public void accept( JSFConfigVisitor visitor ) {
        visitor.visit( this );
    }
    
    protected List<String> getSortedListOfLocalNames(){
        return SORTED_ELEMENTS;
    }
    
    protected static final List<String> SORTED_ELEMENTS = new ArrayList<String>(
            DESCRIPTION_GROUP_SORTED_ELEMENTS.size() +2 );
    
    static {
        SORTED_ELEMENTS.add( REFERENCED_BEAN_NAME);
        SORTED_ELEMENTS.add( REFERENCED_BEAN_CLASS);
    }

}
