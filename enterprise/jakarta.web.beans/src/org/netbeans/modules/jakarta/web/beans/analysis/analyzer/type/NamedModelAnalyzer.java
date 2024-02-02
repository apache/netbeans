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
package org.netbeans.modules.jakarta.web.beans.analysis.analyzer.type;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.ClassModelAnalyzer.ClassAnalyzer;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.ModelAnalyzer.Result;
import org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class NamedModelAnalyzer implements ClassAnalyzer {

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.analysis.analyzer.ClassModelAnalyzer.ClassAnalyzer#analyze(javax.lang.model.element.TypeElement, javax.lang.model.element.TypeElement, org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel, java.util.List, org.netbeans.api.java.source.CompilationInfo, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    public void analyze( TypeElement element, TypeElement parent,
            WebBeansModel model, AtomicBoolean cancel,
            Result result )
    {
        if ( !AnnotationUtil.hasAnnotation(element, AnnotationUtil.SPECIALIZES, 
                model.getCompilationController()))
        {
            return;
        }
        result.requireCdiEnabled(element, model);
        if ( !AnnotationUtil.hasAnnotation(element, AnnotationUtil.NAMED, 
                model.getCompilationController()))
        {
            return;
        }
        TypeMirror superclass = element.getSuperclass();
        Element superElement = model.getCompilationController().getTypes().
            asElement( superclass );
        if ( cancel.get() ){
            return;
        }
        String name = model.getName(superElement);
        if ( name == null ){
            return;
        }
        result.addError( element, model,  NbBundle.getMessage(
                NamedModelAnalyzer.class, "ERR_NamedSpecializes"));     // NOI18N
    }

}
