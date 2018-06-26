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

import org.netbeans.modules.web.jsf.api.facesmodel.FacesValidator;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.ValidatorExtension;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class ValidatorImpl extends IdentifiableDescriptionGroupImpl implements
        FacesValidator
{

    ValidatorImpl( JSFConfigModelImpl model, Element element ) {
        super(model, element);
    }
    
    ValidatorImpl( JSFConfigModelImpl model ) {
        super(model, createElementNS(model, JSFConfigQNames.VALIDATOR));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesValidator#addValidatorExtension(org.netbeans.modules.web.jsf.api.facesmodel.ValidatorExtension)
     */
    public void addValidatorExtension( ValidatorExtension extension ) {
        appendChild( VALIDATOR_EXTENSION, extension);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesValidator#addValidatorExtension(int, org.netbeans.modules.web.jsf.api.facesmodel.ValidatorExtension)
     */
    public void addValidatorExtension( int index, ValidatorExtension extension )
    {
        insertAtIndex( VALIDATOR_EXTENSION, extension, index, ValidatorExtension.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesValidator#getValidatorExtensions()
     */
    public List<ValidatorExtension> getValidatorExtensions() {
        return getChildren( ValidatorExtension.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesValidator#removeValidatorExtension(org.netbeans.modules.web.jsf.api.facesmodel.ValidatorExtension)
     */
    public void removeValidatorExtension( ValidatorExtension extension ) {
        removeChild( VALIDATOR_EXTENSION , extension );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesValidator#setValidatorClass(java.lang.String)
     */
    public void setValidatorClass( String clazz ) {
        setChildElementText(VALIDATOR_CLASS, clazz, 
                JSFConfigQNames.VALIDATOR_CLASS.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesValidator#setValidatorId(java.lang.String)
     */
    public void setValidatorId( String id ) {
        setChildElementText(VALIDATOR_ID, id, 
                JSFConfigQNames.VALIDATOR_ID.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent#accept(org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor)
     */
    public void accept( JSFConfigVisitor visitor ) {
        visitor.visit( this );
    }

    /**
     * Gets validator-class of the faces-config-validatorType.
     * @return trimmed validator-class if any, {@code null} otherwise
     */
    public String getValidatorClass() {
        String validatorClass = getChildElementText(JSFConfigQNames.VALIDATOR_CLASS.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickFullyQualifiedClassType(validatorClass);
    }

    /**
     * Gets validator-id of the faces-config-validatorType.
     * @return trimmed validator-id if any, {@code null} otherwise
     */
    public String getValidatorId() {
        String validatorId = getChildElementText(JSFConfigQNames.VALIDATOR_ID.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickString(validatorId);
    }
    
    protected List<String> getSortedListOfLocalNames(){
        return SORTED_ELEMENTS;
    }
    
    protected static final List<String> SORTED_ELEMENTS = new ArrayList<String>(
            DESCRIPTION_GROUP_SORTED_ELEMENTS.size() + 5);
    
    static {
        SORTED_ELEMENTS.addAll( DESCRIPTION_GROUP_SORTED_ELEMENTS);
        SORTED_ELEMENTS.add( VALIDATOR_ID);
        SORTED_ELEMENTS.add( VALIDATOR_CLASS );  
        SORTED_ELEMENTS.add( ATTRIBUTE ); 
        SORTED_ELEMENTS.add( PROPERTY ); 
        SORTED_ELEMENTS.add( VALIDATOR_EXTENSION ); 
    }

}
