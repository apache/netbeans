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
import javax.lang.model.element.TypeElement;

import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.type.DeclaredIBindingsAnalyzer;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.type.InterceptedBeanAnalyzer;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.type.ManagedBeansAnalizer;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.type.NamedModelAnalyzer;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.type.ScopedBeanAnalyzer;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.type.SessionBeanAnalyzer;
import org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel;


/**
 * @author ads
 *
 */
public class ClassModelAnalyzer implements ModelAnalyzer {

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.analysis.analyzer.ModelAnalyzer#analyze(javax.lang.model.element.Element, javax.lang.model.element.TypeElement, org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel, java.util.List, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    public void analyze( Element element, TypeElement parent,
            WebBeansModel model, AtomicBoolean cancel,
            Result result )
    {
        TypeElement subject = (TypeElement) element;
        for( ClassAnalyzer analyzer : ANALYZERS ){
            if ( cancel.get() ){
                return;
            }
            analyzer.analyze( subject, parent, model, cancel, result);
        }
    }

    public interface ClassAnalyzer {
        void analyze( TypeElement element , TypeElement parent,
                WebBeansModel model,AtomicBoolean cancel,
                Result result );
    }

    private static final List<ClassAnalyzer> ANALYZERS= new LinkedList<ClassAnalyzer>();

    static {
        ANALYZERS.add( new ManagedBeansAnalizer());
        ANALYZERS.add( new ScopedBeanAnalyzer());
        ANALYZERS.add( new SessionBeanAnalyzer());
        ANALYZERS.add( new InterceptedBeanAnalyzer() );
        ANALYZERS.add( new NamedModelAnalyzer() );
        ANALYZERS.add( new DeclaredIBindingsAnalyzer());
    }

}
