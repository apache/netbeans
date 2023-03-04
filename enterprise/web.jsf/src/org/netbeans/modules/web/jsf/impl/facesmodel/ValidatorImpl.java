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
