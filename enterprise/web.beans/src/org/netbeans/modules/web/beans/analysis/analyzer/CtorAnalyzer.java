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
package org.netbeans.modules.web.beans.analysis.analyzer;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import org.netbeans.modules.web.beans.analysis.CdiAnalysisResult;
import org.openide.util.NbBundle;

import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.DISPOSES_FQN;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.DISPOSES_FQN_JAKARTA;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.OBSERVES_FQN;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.OBSERVES_FQN_JAKARTA;

/**
 * @author ads
 *
 */
public class CtorAnalyzer implements ElementAnalyzer {

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analizer.ElementAnalyzer#analyze(javax.lang.model.element.Element, javax.lang.model.element.TypeElement, org.netbeans.api.java.source.CompilationInfo, java.util.List, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    public void analyze( Element element, TypeElement parent,
            AtomicBoolean cancel, CdiAnalysisResult result )
    {
        ExecutableElement ctor = (ExecutableElement)element;
        List<? extends VariableElement> parameters = ctor.getParameters();
        for (VariableElement param : parameters) {
            if ( cancel.get() ){
                return;
            }
            boolean isDisposer = AnnotationUtil.hasAnnotation(param, result.getInfo(), DISPOSES_FQN_JAKARTA, DISPOSES_FQN);
            boolean isObserver = AnnotationUtil.hasAnnotation(param, result.getInfo(), OBSERVES_FQN_JAKARTA, OBSERVES_FQN);
            if ( isDisposer || isObserver ){
                result.requireCdiEnabled(element);
                String annotation = isDisposer ? AnnotationUtil.DISPOSES :
                    AnnotationUtil.OBSERVES;
                result.addError( element, NbBundle.getMessage(
                    CtorAnalyzer.class, "ERR_BadAnnotationParamCtor", annotation)); // NOI18N
                break;
            }
        }
    }

}
