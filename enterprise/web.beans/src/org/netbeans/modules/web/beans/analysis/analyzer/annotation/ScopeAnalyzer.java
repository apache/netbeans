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
package org.netbeans.modules.web.beans.analysis.analyzer.annotation;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.TypeElement;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.web.beans.analysis.CdiAnalysisResult;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationElementAnalyzer.AnnotationAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class ScopeAnalyzer implements AnnotationAnalyzer {

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.AnnotationElementAnalyzer.AnnotationAnalyzer#analyze(javax.lang.model.element.TypeElement, java.util.concurrent.atomic.AtomicBoolean, org.netbeans.modules.web.beans.analysis.analyzer.ElementAnalyzer.Result)
     */
    @Override
    public void analyze( TypeElement element, AtomicBoolean cancel,
            CdiAnalysisResult result)
    {
        CompilationInfo compInfo = result.getInfo();
        boolean isScope = AnnotationUtil.hasAnnotation(element, AnnotationUtil.SCOPE_FQN, compInfo)
                || AnnotationUtil.hasAnnotation(element, AnnotationUtil.SCOPE_FQN_JAKARTA, compInfo);
        boolean isNormalScope = AnnotationUtil.hasAnnotation(element, AnnotationUtil.NORMAL_SCOPE_FQN, compInfo)
                || AnnotationUtil.hasAnnotation(element, AnnotationUtil.NORMAL_SCOPE_FQN_JAKARTA, compInfo);
        if ( isScope || isNormalScope ){
            result.requireCdiEnabled(element);
            ScopeTargetAnalyzer analyzer = new ScopeTargetAnalyzer(element, 
                    result, isNormalScope);
            if ( cancel.get() ){
                return;
            }
            if ( !analyzer.hasRuntimeRetention() ){
                result.addError( element, 
                            NbBundle.getMessage(ScopeAnalyzer.class, 
                                    INCORRECT_RUNTIME));
            }
            if ( cancel.get() ){
                return;
            }
            if ( !analyzer.hasTarget()){
                result.addError( element, 
                            NbBundle.getMessage(ScopeAnalyzer.class, 
                                    "ERR_IncorrectScopeTarget"));                // NOI18N
            }
        }
    }
    
    private static class ScopeTargetAnalyzer extends CdiAnnotationAnalyzer {
        
        ScopeTargetAnalyzer(TypeElement element, CdiAnalysisResult result, 
                boolean normalScope )
        {
            super( element , result );
            isNormalScope = normalScope;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.beans.analysis.analizer.annotation.TargetAnalyzer#getTargetVerifier()
         */
        @Override
        protected TargetVerifier getTargetVerifier() {
            return ScopeVerifier.getInstance();
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.beans.analysis.analizer.annotation.CdiAnnotationAnalyzer#getCdiMetaAnnotation()
         */
        @Override
        protected String getCdiMetaAnnotation() {
            if ( isNormalScope ){
                return AnnotationUtil.NORMAL_SCOPE;
            }
            else {
                return AnnotationUtil.SCOPE;
            }
        }

        private boolean isNormalScope; 
    }

}
