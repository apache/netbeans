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
package org.netbeans.modules.jakarta.web.beans.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.AnnotationElementAnalyzer;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.ClassElementAnalyzer;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.CtorAnalyzer;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.ElementAnalyzer;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.FieldElementAnalyzer;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.MethodElementAnalyzer;
import org.netbeans.modules.jakarta.web.beans.hints.EditorAnnotationsHelper;
import org.netbeans.spi.editor.hints.ErrorDescription;


/**
 * @author ads
 *
 */
public class CdiAnalysisTask extends AbstractAnalysisTask {
    
    public CdiAnalysisTask( CdiEditorAwareJavaSourceTaskFactory factory ){
        myFactory =factory;
    }
    
    
    protected CdiAnalysisResult createResult( CompilationInfo info ){
        return new CdiAnalysisResult(info, myFactory );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.analysis.AbstractAnalysisTask#getProblems()
     */
    @Override
    List<ErrorDescription> getProblems() {
        return getResult().getProblems();
    }
    
    @Override
    protected void run( CompilationInfo compInfo ) {
        setResult( createResult( compInfo ) );
        List<? extends TypeElement> types = compInfo.getTopLevelElements();
        for (TypeElement typeElement : types) {
            if ( isCancelled() ){
                break;
            }
            analyzeType(typeElement, null );
        }
        EditorAnnotationsHelper helper = EditorAnnotationsHelper.getInstance(getResult());
        if ( helper == null ){
            return;
        }
        helper.publish( getResult() );
    }
    
    private void analyzeType(TypeElement typeElement , TypeElement parent )
    {
        ElementKind kind = typeElement.getKind();
        ElementAnalyzer analyzer = ANALIZERS.get( kind );
        if ( analyzer != null ){
            analyzer.analyze(typeElement, parent, getCancel(), getResult() );
        }
        if ( isCancelled() ){
            return;
        }
        List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
        List<TypeElement> types = ElementFilter.typesIn(enclosedElements);
        for (TypeElement innerType : types) {
            analyzeType(innerType, typeElement );
        }
        Set<Element> enclosedSet = new HashSet<Element>( enclosedElements );
        enclosedSet.removeAll( types );
        for(Element element : enclosedSet ){
            analyze(typeElement, element);
        }
    }

    private void analyze( TypeElement typeElement, Element element )
    {
        ElementAnalyzer analyzer;
        if ( isCancelled() ){
            return;
        }
        analyzer = ANALIZERS.get( element.getKind() );
        if ( analyzer == null ){
            return;
        }
        analyzer.analyze(element, typeElement, getCancel(), getResult() );
    }

    private CdiEditorAwareJavaSourceTaskFactory myFactory;
    private static final Map<ElementKind,ElementAnalyzer> ANALIZERS = 
        new HashMap<ElementKind, ElementAnalyzer>();

    static {
        ANALIZERS.put(ElementKind.CLASS, new ClassElementAnalyzer());
        ANALIZERS.put(ElementKind.FIELD, new FieldElementAnalyzer());
        ANALIZERS.put(ElementKind.METHOD, new MethodElementAnalyzer());
        ANALIZERS.put(ElementKind.CONSTRUCTOR, new CtorAnalyzer());
        ANALIZERS.put(ElementKind.ANNOTATION_TYPE, new AnnotationElementAnalyzer());
    }

}
