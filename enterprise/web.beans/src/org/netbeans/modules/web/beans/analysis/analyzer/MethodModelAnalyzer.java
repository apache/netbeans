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
package org.netbeans.modules.web.beans.analysis.analyzer;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.web.beans.analysis.analyzer.method.InjectionPointParameterAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.method.InterceptedMethodAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.method.ScopedMethodAnalyzer;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;


/**
 * @author ads
 *
 */
public class MethodModelAnalyzer implements ModelAnalyzer {

    @Override
    public void analyze( Element element, TypeElement parent,
            WebBeansModel model, AtomicBoolean cancel, 
            Result result )
    {
        ExecutableElement method = (ExecutableElement) element;
        TypeMirror methodType = model.getCompilationController().getTypes().
            asMemberOf( (DeclaredType)parent.asType(),  method );
        if ( methodType instanceof ExecutableType ){
            if ( cancel.get()){
                return;
            }
            TypeMirror returnType = ((ExecutableType) methodType).getReturnType();
            for (MethodAnalyzer analyzer : ANALYZERS) {
                if ( cancel.get() ){
                    return;
                }
                analyzer.analyze(method, returnType, parent, model, 
                        cancel, result );
            }
        }
    }
    
    public interface MethodAnalyzer {
        void analyze( ExecutableElement element , TypeMirror returnType,
                TypeElement parent, WebBeansModel model, 
                AtomicBoolean cancel , Result result);
    }
    
    private static final List<MethodAnalyzer> ANALYZERS= new LinkedList<MethodAnalyzer>(); 
    
    static {
        ANALYZERS.add( new ScopedMethodAnalyzer() );
        ANALYZERS.add( new InjectionPointParameterAnalyzer() );
        ANALYZERS.add( new InterceptedMethodAnalyzer() );
    }

}
