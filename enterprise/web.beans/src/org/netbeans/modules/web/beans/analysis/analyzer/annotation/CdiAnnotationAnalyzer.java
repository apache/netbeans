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
package org.netbeans.modules.web.beans.analysis.analyzer.annotation;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.web.beans.analysis.CdiAnalysisResult;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
abstract class CdiAnnotationAnalyzer extends TargetAnalyzer {
    
    CdiAnnotationAnalyzer(TypeElement element, CdiAnalysisResult result ) 
    {
        init( element , result.getInfo() );
        myResult =result;
    }
    
    CdiAnnotationAnalyzer(TypeElement element, WebBeansModel model, 
            CdiAnalysisResult result ) 
    {
        init( element , model.getCompilationController() );
        myModel = model;
        myResult =result;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analizer.annotation.TargetAnalyzer#handleNoTarget()
     */
    @Override
    protected void handleNoTarget() {
        if ( myResult== null){
            return;
        }
        Element subject = getElement();
        if ( myModel != null ){
            ElementHandle<Element> handle = ElementHandle.create( getElement());
            subject = handle.resolve(getResult().getInfo());
        }
        if ( subject == null ){
            return;
        }
        myResult.addError( subject, NbBundle.getMessage(ScopeAnalyzer.class, 
                "ERR_NoTarget" ,   // NOI18N
                            getCdiMetaAnnotation()));      
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analizer.annotation.RuntimeRetentionAnalyzer#handleNoRetention()
     */
    @Override
    protected void handleNoRetention() {
        if ( myResult== null){
            return;
        }
        Element subject = getElement();
        if ( myModel != null ){
            ElementHandle<Element> handle = ElementHandle.create( getElement());
            subject = handle.resolve(getResult().getInfo());
        }
        if ( subject == null ){
            return;
        }
        myResult.addError( subject, NbBundle.getMessage(ScopeAnalyzer.class, 
                "ERR_NoRetention", // NOI18N
                            getCdiMetaAnnotation()));      
    }
    
    protected abstract String getCdiMetaAnnotation();
    
    protected CdiAnalysisResult getResult(){
        return myResult;
    }
    
    private WebBeansModel myModel;
    private CdiAnalysisResult myResult;

}
