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

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.jakarta.web.beans.analysis.CdiAnalysisResult;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.AbstractTypedAnalyzer;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.ClassElementAnalyzer.ClassAnalyzer;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class TypedClassAnalizer extends AbstractTypedAnalyzer implements 
    ClassAnalyzer
{
    
    @Override
    public void analyze( TypeElement element, TypeElement parent,
            AtomicBoolean cancel, CdiAnalysisResult result )
    {
        analyze(element, element.asType() , cancel , result );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.analysis.analyzer.AbstractTypedAnalyzer#addError(javax.lang.model.element.Element, org.netbeans.modules.jakarta.web.beans.analysis.analyzer.ElementAnalyzer.Result)
     */
    @Override
    protected void addError( Element element, CdiAnalysisResult result  )
    {
        result.addError( element, NbBundle.getMessage(
                TypedClassAnalizer.class, "ERR_BadRestritedType"));         // NOI18N
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.analysis.analyzer.AbstractTypedAnalyzer#checkSpecializes(javax.lang.model.element.Element, javax.lang.model.type.TypeMirror, java.util.List, java.util.concurrent.atomic.AtomicBoolean, org.netbeans.modules.jakarta.web.beans.analysis.analyzer.ElementAnalyzer.Result)
     */
    @Override
    protected void checkSpecializes( Element element, TypeMirror elementType,
            List<TypeMirror> restrictedTypes, AtomicBoolean cancel , CdiAnalysisResult result )
    {
        TypeElement typeElement = (TypeElement)element;
        TypeMirror superclass = typeElement.getSuperclass();
        Element superElement = result.getInfo().getTypes().asElement(superclass);
        if ( !( superElement instanceof TypeElement )){
            return;
        }
        List<TypeMirror> restrictedSuper = getRestrictedTypes(superElement, 
                    result.getInfo(), cancel);
        if ( cancel.get()){
            return;
        }
        /*
         *  No need to look at the TypeMirrors here. The correctness of the
         *  bean types are guaranteed by inheritance hierarchy.
         *  TypeMirrors here couldn't be arrays or primitives.
         *  ( But it is possible for production elements where TypeMirrors shouldn't
         *  be checked only against corresponding TypeElement ).  
         */
        
        Set<TypeElement> specializedBeanTypes;
        if ( restrictedSuper == null ){
            specializedBeanTypes = getUnrestrictedBeanTypes(
                    (TypeElement)superElement, result.getInfo());
        }
        else {
            specializedBeanTypes = getElements( restrictedSuper, result.getInfo());
        }
        Set<TypeElement> restrictedElements = getElements(restrictedTypes, 
                result.getInfo());
        restrictedElements.add( result.getInfo().getElements().getTypeElement( 
                Object.class.getCanonicalName()));
        if ( !restrictedElements.containsAll(specializedBeanTypes)){
            result.addError( element,  NbBundle.getMessage(
                        TypedClassAnalizer.class, "ERR_BadSpecializesBeanType"));  // NOI18N 
        }
    }
    
}
