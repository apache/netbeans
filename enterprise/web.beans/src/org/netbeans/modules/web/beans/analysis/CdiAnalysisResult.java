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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.Element;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.beans.CdiUtil;
import org.netbeans.modules.web.beans.hints.CDIAnnotation;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;



/**
 * @author ads
 *
 */
public class CdiAnalysisResult {
    
    public CdiAnalysisResult( CompilationInfo info, 
            CdiEditorAwareJavaSourceTaskFactory factory )
    {
        myInfo = info;
        myProblems = new LinkedList<ErrorDescription>();
        myCollectedAnnotations = new LinkedList<CDIAnnotation>();
    }

    public void addError( Element subject, String message ) {
        addNotification( Severity.ERROR, subject, message);
    }
    
    public void addError( Element subject, String message , Fix fix ) {
        addNotification( Severity.ERROR, subject, message, fix );
    }
    
    public void addNotification( Severity severity,
            Element element, String message )
    {
        addNotification(severity, element, message , null );           
    }
    
    public void addNotification( Severity severity,
            Element element, String message , Fix fix )
    {
        ErrorDescription description = CdiEditorAnalysisFactory.
            createNotification( severity, element, myInfo , message, fix );
        if ( description == null ){
            return;
        }
        getProblems().add( description );              
    }

    public CompilationInfo getInfo() {
        return myInfo;
    }
    
    public List<ErrorDescription> getProblems(){
        return myProblems;
    }
    
    public void requireCdiEnabled( Element element ){
        if ( isCdiRequired ){
            return;
        }
        isCdiRequired = true;
        FileObject fileObject = getInfo().getFileObject();
        if ( fileObject ==null ){
            return;
        }
        Project project = FileOwnerQuery.getOwner( fileObject );
        if ( project == null ){
            return;
        }
        CdiUtil lookup = project.getLookup().lookup( CdiUtil.class );
        boolean needFix = false;
        if ( lookup == null ){
            //in general main use is is when lookup!=null, if lookup == null nly some general hevavior is supported
            needFix = !CdiUtil.isCdiEnabled(project);
        }
        else {
            needFix = !lookup.isCdiEnabled();
        }
        if ( needFix) {
            Fix fix = new BeansXmlFix( project , fileObject , myFactory );
            addError(element, NbBundle.getMessage(CdiAnalysisResult.class, 
                "ERR_RequireWebBeans"), fix );        // NOI18N
        }
    }
    
    public boolean requireBeansXml(){
        return isCdiRequired;
    }
    
    public void addAnnotation( CDIAnnotation annotation ) {
        myCollectedAnnotations.add(annotation);
    }
    
    public List<CDIAnnotation> getAnnotations(){
        return Collections.unmodifiableList(myCollectedAnnotations);
    }
    
    private CompilationInfo myInfo ;
    private List<ErrorDescription> myProblems;
    private boolean isCdiRequired;
    private List<CDIAnnotation> myCollectedAnnotations;
    private CdiEditorAnalysisFactory myFactory;

}
