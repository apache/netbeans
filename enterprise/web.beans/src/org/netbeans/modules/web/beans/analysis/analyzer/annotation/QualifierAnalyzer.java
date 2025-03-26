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

import org.netbeans.modules.web.beans.analysis.CdiAnalysisResult;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.openide.util.NbBundle;

import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.QUALIFIER_FQN;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.QUALIFIER_FQN_JAKARTA;

/**
 * @author ads
 *
 */
public class QualifierAnalyzer extends InterceptorBindingMembersAnalyzer {

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.AnnotationElementAnalyzer.AnnotationAnalyzer#analyze(javax.lang.model.element.TypeElement, java.util.concurrent.atomic.AtomicBoolean, org.netbeans.modules.web.beans.analysis.analyzer.ElementAnalyzer.Result)
     */
    @Override
    public void analyze( TypeElement element, AtomicBoolean cancel,
            CdiAnalysisResult result )
    {
        if (AnnotationUtil.hasAnnotation(element, result.getInfo(), QUALIFIER_FQN_JAKARTA, QUALIFIER_FQN))
        {
            result.requireCdiEnabled(element);
            QualifierTargetAnalyzer analyzer = new QualifierTargetAnalyzer(element,
                    result );
            if ( !analyzer.hasRuntimeRetention() ){
                result.addError( element,
                        NbBundle.getMessage(QualifierTargetAnalyzer.class,
                                INCORRECT_RUNTIME));
            }
            if ( !analyzer.hasTarget()){
                result.addError( element,
                            NbBundle.getMessage(QualifierTargetAnalyzer.class,
                                    "ERR_IncorrectQualifierTarget"));  // NOI18N
            }
            if ( cancel.get() ){
                return;
            }
            checkMembers( element, result , NbBundle.getMessage(
                    QualifierAnalyzer.class,
                        "WARN_ArrayAnnotationValuedQualifierMember"));  // NOI18N
        }
    }

    private static class QualifierTargetAnalyzer extends CdiAnnotationAnalyzer{

        QualifierTargetAnalyzer( TypeElement element, CdiAnalysisResult result)
        {
            super(element, result);
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.beans.analysis.analizer.annotation.CdiAnnotationAnalyzer#getCdiMetaAnnotation()
         */
        @Override
        protected String getCdiMetaAnnotation() {
            return AnnotationUtil.QUALIFIER;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.beans.analysis.analizer.annotation.TargetAnalyzer#getTargetVerifier()
         */
        @Override
        protected TargetVerifier getTargetVerifier() {
            return QualifierVerifier.getInstance( true );
        }

    }
}