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

package org.netbeans.modules.websvc.editor.hints.fixes;

import java.io.IOException;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.websvc.editor.hints.common.Utilities;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * @author Ajit.Bhate@Sun.COM
 */
public class RemoveAnnotation implements Fix {
    private FileObject fileObject;
    private Element element;
    private String annotation;
    private String simpleName;
    
    /** Creates a new instance of RemoveAnnotation */
    public RemoveAnnotation(FileObject fileObject, Element element,
            AnnotationMirror annMirror) {
        this.element = element;
        this.fileObject = fileObject;
        Element annotationElement = annMirror.getAnnotationType().asElement();
        assert annotationElement instanceof TypeElement;
        annotation = ((TypeElement)annotationElement).getQualifiedName().toString();
        simpleName = annotationElement.getSimpleName().toString();
    }
    
    public ChangeInfo implement(){
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>(){
            public void cancel() {}
            
            public void run(WorkingCopy workingCopy) throws Exception {
                if (element.getKind() == ElementKind.PARAMETER){
                    Element method = element.getEnclosingElement();
                    if ( method instanceof ExecutableElement ){
                        ExecutableElement methodElement = (ExecutableElement)method;
                        List<? extends VariableElement> parameters = methodElement.getParameters();
                        int index = parameters.indexOf( element );
                        if ( index == -1 ){
                            return;
                        }
                        Utilities.removeAnnotation(workingCopy, 
                                ElementHandle.create(methodElement), index, 
                                annotation );
                    }
                    else {
                        return;
                    }
                }
                else {
                    Utilities.removeAnnotation(workingCopy, ElementHandle.create(element), 
                        annotation);
                }
            }
        };
        
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        
        try{
            if ( javaSource!= null){
                javaSource.runModificationTask(task).commit();
            }
        } catch (IOException e){
        }
        return null;
    }
    
    public int hashCode(){
        return 1;
    }
    
    public boolean equals(Object o){
        // TODO: implement equals properly
        return super.equals(o);
    }
    
    public String getText(){
        String annotationLabel = simpleName;
        return NbBundle.getMessage(RemoveAnnotation.class, "LBL_RemoveAnnotation",annotationLabel);
    }
}
