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

import org.netbeans.modules.web.jsf.api.metamodel.ValidatorId;


/**
 * @author ads
 *
 */
class AnnotationValidatorId implements ValidatorId {
    
    AnnotationValidatorId( String text){
        myText = text;
    }

    /**
     * Gets validator-id text.
     * @return trimmed validator-id text if any, {@code null} otherwise
     */
    public String getText() {
        return ElementTypeHelper.pickString(myText);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object object ) {
        if ( object instanceof AnnotationValidatorId){
            String text = ((AnnotationValidatorId)object).myText;
            if ( text == null ){
                return myText == null;
            }
            else {
                return text.equals( myText );
            }
        }
        else {
            return false;
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        if ( myText == null ){
            return 0;
        }
        else {
            return myText.hashCode();
        }
    }

    private String myText;
}
