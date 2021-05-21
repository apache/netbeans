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
package org.netbeans.modules.web.beans.impl.model;

import java.lang.annotation.ElementType;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.web.beans.analysis.analyzer.annotation.InterceptorBindingVerifier;
import org.netbeans.modules.web.beans.analysis.analyzer.annotation.TargetVerifier;


/**
 * @author ads
 *
 */
class InterceptorBindingChecker extends RuntimeAnnotationChecker {
    
    static final String INTERCEPTOR_BINDING = "javax.interceptor.InterceptorBinding"; // NOI18N
    
    InterceptorBindingChecker(AnnotationModelHelper helper){
        init( null,  helper );
    }
    
    void init(TypeElement element){
        init( element , getHelper() );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.RuntimeAnnotationChecker#getLogger()
     */
    @Override
    protected Logger getLogger() {
        return Logger.getLogger(InterceptorBindingChecker.class.getName());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.RuntimeAnnotationChecker#getAnnotation()
     */
    @Override
    protected String getAnnotation() {
        return INTERCEPTOR_BINDING;
    }
    
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.annotation.TargetAnalyzer#hasReqiredTarget(javax.lang.model.element.AnnotationMirror, java.util.Set)
     */
    @Override
    public boolean hasReqiredTarget( AnnotationMirror target,
            Set<ElementType> set )
    {
        boolean hasRequiredTarget = super.hasReqiredTarget(target, set);
        if (!hasRequiredTarget) {
            getLogger().log(Level.WARNING,
                    "Annotation "+getElement().getQualifiedName()+
                    "declared as Interceptor Binding but has wrong target values." +
                    " Correct target values are {METHOD, TYPE} or" +
                    " or TYPE ");// NOI18N
        }
        return hasRequiredTarget;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analizer.annotation.TargetAnalyzer#getTargetVerifier()
     */
    @Override
    protected TargetVerifier getTargetVerifier() {
        return InterceptorBindingVerifier.getInstance();
    }

    void clean() {
        init( null , getHelper() );
    }

}
