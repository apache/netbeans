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
package org.netbeans.modules.web.beans.analysis.analyzer.type;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import org.netbeans.modules.web.beans.analysis.CdiAnalysisResult;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.analyzer.ClassElementAnalyzer.ClassAnalyzer;
import org.openide.util.NbBundle;

import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.INJECT_FQN;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.INJECT_FQN_JAKARTA;


/**
 * @author ads
 *
 */
public class CtorsAnalyzer implements ClassAnalyzer {

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.ClassElementAnalyzer.ClassAnalyzer#analyze(javax.lang.model.element.TypeElement, javax.lang.model.element.TypeElement, org.netbeans.api.java.source.CompilationInfo, java.util.List, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    public void analyze( TypeElement element, TypeElement parent,
            AtomicBoolean cancel, CdiAnalysisResult result )
    {
        List<ExecutableElement> constructors = ElementFilter.constructorsIn(
                element.getEnclosedElements());
        int injectCtorCount = 0;
        for (ExecutableElement ctor : constructors) {
            if ( cancel.get() ){
                return;
            }
            if (AnnotationUtil.hasAnnotation(ctor, result.getInfo(), INJECT_FQN_JAKARTA, INJECT_FQN))
            {
                result.requireCdiEnabled( ctor );
                injectCtorCount++;
            }
        }
        if ( injectCtorCount > 1){
            result.addError( element,  NbBundle.getMessage(
                CtorsAnalyzer.class, "ERR_InjectedCtor"));
        }
    }

}
