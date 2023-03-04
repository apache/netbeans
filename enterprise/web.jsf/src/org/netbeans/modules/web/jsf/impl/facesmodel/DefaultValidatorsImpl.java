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

import org.netbeans.modules.web.jsf.api.facesmodel.DefaultValidators;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesValidatorId;
import org.netbeans.modules.web.jsf.api.metamodel.Validator;
import org.netbeans.modules.web.jsf.api.metamodel.ValidatorId;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class DefaultValidatorsImpl extends IdentifiableComponentImpl implements
        DefaultValidators
{

    DefaultValidatorsImpl( JSFConfigModelImpl model, Element element ) {
        super(model, element);
    }
    
    DefaultValidatorsImpl( JSFConfigModelImpl model) {
        this(model, createElementNS(model, JSFConfigQNames.DEFAULT_VALIDATORS));
    }

    public void addValidatorId( FacesValidatorId id ) {
        appendChild( VALIDATOR_ID, id);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.DefaultValidators#addValidatorId(int, org.netbeans.modules.web.jsf.api.facesmodel.ValidatorId)
     */
    public void addValidatorId( int index, FacesValidatorId id ) {
        insertAtIndex( VALIDATOR_ID, id, index, FacesValidatorId.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.DefaultValidators#getValidatorIds()
     */
    public List<ValidatorId> getValidatorIds() {
        List<FacesValidatorId> list = getChildren( FacesValidatorId.class );
        List<ValidatorId> result = new ArrayList<ValidatorId>( list );
        AbstractJsfModel model = getModel().getModelSource().getLookup().lookup(
                AbstractJsfModel.class);
        List<Validator> validators = model.getElements( Validator.class );
        for (Validator validator : validators) {
            if ( validator instanceof FacesValidatorImpl){
                if ( ((FacesValidatorImpl)validator).isDefault() ){
                    result.add( new AnnotationValidatorId( 
                            validator.getValidatorId() ));
                }
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.DefaultValidators#removeValidatorId(org.netbeans.modules.web.jsf.api.facesmodel.ValidatorId)
     */
    public void removeValidatorId( FacesValidatorId id ) {
        removeChild( VALIDATOR_ID, id);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent#accept(org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor)
     */
    public void accept( JSFConfigVisitor visitor ) {
        visitor.visit( this );
    }

}
