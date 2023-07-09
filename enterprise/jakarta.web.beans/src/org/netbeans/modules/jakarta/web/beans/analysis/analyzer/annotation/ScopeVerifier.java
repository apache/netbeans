/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.jakarta.web.beans.analysis.analyzer.annotation;

import java.lang.annotation.ElementType;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;


/**
 * @author ads
 *
 */
public class ScopeVerifier implements TargetVerifier {
    
    private static final ScopeVerifier INSTANCE = new ScopeVerifier();
    
    private ScopeVerifier(  ){
    }
    
    public static ScopeVerifier getInstance(){
        return INSTANCE;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.analysis.analyzer.annotation.TargetVerifier#hasReqiredTarget(javax.lang.model.element.AnnotationMirror, java.util.Set)
     */
    @Override
    public boolean hasReqiredTarget( AnnotationMirror target,
            Set<ElementType> targetTypes )
    {
        boolean hasRequiredTarget = targetTypes.contains( 
                ElementType.METHOD) &&
                targetTypes.contains(ElementType.FIELD) &&
                targetTypes.contains( ElementType.TYPE);
        return hasRequiredTarget;
    }
    
}
