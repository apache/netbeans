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
package org.netbeans.modules.web.beans.analysis.analyzer.type;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.analyzer.ClassModelAnalyzer.ClassAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.ModelAnalyzer.Result;
import org.netbeans.modules.web.beans.api.model.CdiException;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class SessionBeanAnalyzer implements ClassAnalyzer {
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.ClassModelAnalyzer.ClassAnalyzer#analyze(javax.lang.model.element.TypeElement, javax.lang.model.element.TypeElement, org.netbeans.modules.web.beans.api.model.WebBeansModel, java.util.List, org.netbeans.api.java.source.CompilationInfo, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    public void analyze( TypeElement element, TypeElement parent,
            WebBeansModel model, AtomicBoolean cancel ,
            Result result )
    {
        boolean isSingleton = AnnotationUtil.hasAnnotation(element, 
                AnnotationUtil.SINGLETON, model.getCompilationController());
        boolean isStateless = AnnotationUtil.hasAnnotation(element, 
                AnnotationUtil.STATELESS, model.getCompilationController());
        if ( cancel.get() ){
            return;
        }
        try {
            String scope = model.getScope( element );
            if ( isSingleton ) {
                if ( AnnotationUtil.APPLICATION_SCOPED.equals( scope ) || 
                        AnnotationUtil.DEPENDENT.equals( scope ) )
                {
                    return;
                }
                result.requireCdiEnabled(element, model);
                result.addError( element, model,  
                    NbBundle.getMessage(SessionBeanAnalyzer.class, 
                            "ERR_InvalidSingletonBeanScope"));              // NOI18N
            }
            else if ( isStateless ) {
                if ( !AnnotationUtil.DEPENDENT.equals( scope ) )
                {
                    result.addError( element, model,   
                        NbBundle.getMessage(SessionBeanAnalyzer.class, 
                                "ERR_InvalidStatelessBeanScope"));              // NOI18N
                }
            }
        }
        catch (CdiException e) {
            result.requireCdiEnabled(element, model);
            informCdiException(e, element, model, result );
        }
    }
    
    private void informCdiException(CdiException exception , Element element, 
            WebBeansModel model, Result result )
    {
        result.addError(element, model, exception.getMessage());
    }

}
