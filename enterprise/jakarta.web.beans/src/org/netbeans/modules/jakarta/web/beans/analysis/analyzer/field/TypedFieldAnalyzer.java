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
package org.netbeans.modules.jakarta.web.beans.analysis.analyzer.field;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.AbstractTypedAnalyzer;
import org.netbeans.modules.jakarta.web.beans.analysis.CdiAnalysisResult;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.FieldElementAnalyzer.FieldAnalyzer;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class TypedFieldAnalyzer extends AbstractTypedAnalyzer implements FieldAnalyzer {

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.analysis.analyzer.FieldElementAnalyzer.FieldAnalyzer#analyze(javax.lang.model.element.VariableElement, javax.lang.model.type.TypeMirror, javax.lang.model.element.TypeElement, java.util.concurrent.atomic.AtomicBoolean, org.netbeans.modules.jakarta.web.beans.analysis.analyzer.ElementAnalyzer.Result)
     */
    @Override
    public void analyze( VariableElement element, TypeMirror elementType,
            TypeElement parent, AtomicBoolean cancel,
            CdiAnalysisResult result  )
    {
        analyze(element, elementType, cancel , result );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.analysis.analyzer.AbstractTypedAnalyzer#addError(javax.lang.model.element.Element, org.netbeans.modules.jakarta.web.beans.analysis.analyzer.ElementAnalyzer.Result)
     */
    @Override
    protected void addError( Element element, CdiAnalysisResult result  )
    {
        result.addError( element, NbBundle.getMessage(
                TypedFieldAnalyzer.class, "ERR_BadRestritedFieldType"));        // NOI18N
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.analysis.analyzer.AbstractTypedAnalyzer#checkSpecializes(javax.lang.model.element.Element, javax.lang.model.type.TypeMirror, java.util.List, java.util.concurrent.atomic.AtomicBoolean, org.netbeans.modules.jakarta.web.beans.analysis.analyzer.ElementAnalyzer.Result)
     */
    @Override
    protected void checkSpecializes( Element element, TypeMirror elementType,
            List<TypeMirror> restrictedTypes, AtomicBoolean cancel , CdiAnalysisResult result)
    {
        // production fields cannot be specialized
    }
    
}
