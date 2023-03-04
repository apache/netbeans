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
package org.netbeans.modules.web.beans.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.web.beans.analysis.analyzer.ModelAnalyzer.Result;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;


/**
 * @author ads
 *
 */
public class WebBeansAnalysisTestResult extends Result implements TestProblems {

    public WebBeansAnalysisTestResult( CompilationInfo info)
    {
        super(info, null);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.CdiAnalysisResult#addError(javax.lang.model.element.Element, java.lang.String)
     */
    @Override
    public void addError( Element subject, String message ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.CdiAnalysisResult#addNotification(org.netbeans.spi.editor.hints.Severity, javax.lang.model.element.Element, java.lang.String)
     */
    @Override
    public void addNotification( Severity severity, Element element,
            String message )
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.CdiAnalysisResult#getProblems()
     */
    @Override
    public List<ErrorDescription> getProblems() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.CdiAnalysisResult#requireCdiEnabled(javax.lang.model.element.Element)
     */
    @Override
    public void requireCdiEnabled( Element element ) {
    }
    
    public void addNotification( Severity severity, Element element,
            WebBeansModel model, String message )
    {
        ElementHandle<Element> handle = ElementHandle.create(element);
        Element origElement = handle.resolve(getInfo());
        if ( severity == Severity.ERROR){
            myErrors.put( origElement , message );
        }
        else if ( severity == Severity.WARNING){
            myWarnings.put( origElement , message );
        }
        else {
            assert false;
        }
    }
    
    public void addNotification( Severity severity,
            VariableElement element, ExecutableElement method,
            WebBeansModel model, String message )
    {
        int index = method.getParameters().indexOf( element );
        ElementHandle<ExecutableElement> handle = ElementHandle.create(method);
        ExecutableElement origMethod = handle.resolve(getInfo());
        VariableElement param = origMethod.getParameters().get(index);
        if ( severity == Severity.ERROR){
            myErrors.put( param , message );
        }
        else if ( severity == Severity.WARNING){
            myWarnings.put( param , message );
        }
        else {
            assert false;
        }            
    }
    
    public void requireCdiEnabled( Element element , WebBeansModel model){
    }
    
    public void requireCdiEnabled( VariableElement element , 
            ExecutableElement method ,WebBeansModel model)
    {
    }
    
    @Override
    public Map<Element,String> getErrors(){
        return myErrors;
    }

    @Override
    public Map<Element,String> getWarings(){
        return myWarnings;
    }
    
    private Map<Element,String> myErrors = new HashMap<Element, String>();
    private Map<Element,String> myWarnings = new HashMap<Element, String>();

}
