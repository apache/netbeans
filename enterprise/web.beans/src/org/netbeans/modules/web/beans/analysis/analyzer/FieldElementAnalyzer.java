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
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.web.beans.analysis.CdiAnalysisResult;
import org.netbeans.modules.web.beans.analysis.analyzer.field.DelegateFieldAnalizer;
import org.netbeans.modules.web.beans.analysis.analyzer.field.ProducerFieldAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.field.TypedFieldAnalyzer;


/**
 * @author ads
 *
 */
public class FieldElementAnalyzer implements ElementAnalyzer {

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.ElementAnalyzer#analyze(javax.lang.model.element.Element, javax.lang.model.element.TypeElement, java.util.concurrent.atomic.AtomicBoolean, org.netbeans.modules.web.beans.analysis.analyzer.ElementAnalyzer.Result)
     */
    @Override
    public void analyze( Element element, TypeElement parent,
            AtomicBoolean cancel, CdiAnalysisResult result )
    {
        VariableElement var = (VariableElement) element;
        TypeMirror varType = result.getInfo().getTypes().asMemberOf( 
                (DeclaredType)parent.asType(),  var );
        for (FieldAnalyzer analyzer : ANALYZERS) {
            if ( cancel.get()){
                return;
            }
            analyzer.analyze(var, varType, parent, cancel, result );
        }
    }
    
    public interface FieldAnalyzer {
        void analyze( VariableElement element , TypeMirror elementType,
                TypeElement parent, AtomicBoolean cancel,
                CdiAnalysisResult result );
    }
    
    private static final List<FieldAnalyzer> ANALYZERS= new LinkedList<FieldAnalyzer>(); 
    
    static {
        ANALYZERS.add( new TypedFieldAnalyzer() );
        ANALYZERS.add( new DelegateFieldAnalizer());
        ANALYZERS.add( new ProducerFieldAnalyzer());
    }
}
