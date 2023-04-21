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
package org.netbeans.modules.jakarta.web.beans.analysis.analyzer;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.jakarta.web.beans.analysis.CdiAnalysisResult;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.method.AnnotationsAnalyzer;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.method.DelegateMethodAnalyzer;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.method.ProducerMethodAnalyzer;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.method.TypedMethodAnalyzer;


/**
 * @author ads
 *
 */
public class MethodElementAnalyzer implements ElementAnalyzer {

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.analysis.analyzer.ElementAnalyzer#analyze(javax.lang.model.element.Element, javax.lang.model.element.TypeElement, java.util.concurrent.atomic.AtomicBoolean, org.netbeans.modules.jakarta.web.beans.analysis.analyzer.ElementAnalyzer.Result)
     */
    @Override
    public void analyze( Element element, TypeElement parent,
            AtomicBoolean cancel, CdiAnalysisResult result )
    {
        ExecutableElement method = (ExecutableElement) element;
        TypeMirror methodType = result.getInfo().getTypes().asMemberOf( 
                (DeclaredType)parent.asType(),  method );
        if ( methodType instanceof ExecutableType ){
            if ( cancel.get()){
                return;
            }
            TypeMirror returnType = ((ExecutableType) methodType).getReturnType();
            for (MethodAnalyzer analyzer : ANALYZERS) {
                if ( cancel.get() ){
                    return;
                }
                analyzer.analyze(method, returnType, parent, cancel, 
                        result );
            }
        }
    }
    
    public interface MethodAnalyzer {
        void analyze( ExecutableElement element , TypeMirror returnType,
                TypeElement parent, AtomicBoolean cancel , CdiAnalysisResult result );
    }
    
    private static final List<MethodAnalyzer> ANALYZERS= new LinkedList<MethodAnalyzer>(); 
    
    static {
        ANALYZERS.add( new TypedMethodAnalyzer() );
        ANALYZERS.add( new AnnotationsAnalyzer() );
        ANALYZERS.add( new DelegateMethodAnalyzer() );
        ANALYZERS.add( new ProducerMethodAnalyzer() );
    }

}
