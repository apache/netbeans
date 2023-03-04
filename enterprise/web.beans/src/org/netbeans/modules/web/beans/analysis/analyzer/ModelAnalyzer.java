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

import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.web.beans.analysis.CdiAnalysisResult;
import org.netbeans.modules.web.beans.analysis.CdiEditorAnalysisFactory;
import org.netbeans.modules.web.beans.analysis.CdiEditorAwareJavaSourceTaskFactory;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;


/**
 * @author ads
 *
 */
public interface ModelAnalyzer {
    
    void analyze( Element element , TypeElement parent, WebBeansModel model,
            AtomicBoolean cancel, Result result );
    
    public class Result extends CdiAnalysisResult {
        
        public Result( CompilationInfo info , 
                CdiEditorAwareJavaSourceTaskFactory factory )
        {
            super(info, factory);
        }
        
        public void addNotification( Severity severity, Element element,
                WebBeansModel model, String message )
        {
            ErrorDescription description = CdiEditorAnalysisFactory.
                createNotification( severity, element, model, getInfo() , 
                    message);
            if ( description == null ){
                return;
            }
            getProblems().add( description );            
        }
        
        public void addNotification( Severity severity,
                VariableElement element, ExecutableElement method,
                WebBeansModel model, String message )
        {
            ErrorDescription description = CdiEditorAnalysisFactory.
                createNotification( severity, element,method, model, getInfo() , 
                message);
            if ( description == null ){
                return;
            }
            getProblems().add( description );              
        }
        
        public void addError( Element element,
                WebBeansModel model, String message )
        {
            addNotification(Severity.ERROR, element, model, message);          
        }
        
        public void addError( VariableElement var, ExecutableElement element,
                WebBeansModel model, String message )
        {
            addNotification(Severity.ERROR, var, element, model, message);                    
        }
        
        /* (non-Javadoc)
         * @see org.netbeans.modules.web.beans.analysis.CdiAnalysisResult#addError(javax.lang.model.element.Element, java.lang.String)
         */
        @Override
        public void addError( Element subject, String message ) {
            ErrorDescription description = CdiEditorAnalysisFactory.
                createNotification( Severity.ERROR, subject, getInfo() , 
                message);
            if ( description == null ){
                return;
            }
            getProblems().add( description );                
        }
        
        public void requireCdiEnabled( Element element , WebBeansModel model){
            ElementHandle<Element> handle = ElementHandle.create(element);
            Element resolved = handle.resolve( getInfo() );
            if ( resolved == null ){
                return;
            }
            requireCdiEnabled( resolved );
        }
        
        public void requireCdiEnabled( VariableElement element , 
                ExecutableElement method ,WebBeansModel model)
        {
            VariableElement resolved = CdiEditorAnalysisFactory.
                resolveParameter(element, method, getInfo());
            if ( resolved == null ){
                return;
            }
            requireCdiEnabled( resolved );
        }
    }
}
