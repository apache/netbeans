/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.web.beans.analysis;

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
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationElementAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.ClassElementAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.CtorAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.ElementAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.FieldElementAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.MethodElementAnalyzer;
import org.netbeans.modules.web.beans.hints.EditorAnnotationsHelper;
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
     * @see org.netbeans.modules.web.beans.analysis.AbstractAnalysisTask#getProblems()
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
