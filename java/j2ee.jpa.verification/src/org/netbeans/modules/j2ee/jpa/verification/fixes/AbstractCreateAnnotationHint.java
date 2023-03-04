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
package org.netbeans.modules.j2ee.jpa.verification.fixes;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.logging.Level;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemFinder;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
abstract class AbstractCreateAnnotationHint implements Fix {
    private FileObject fileObject;
    private ElementHandle<TypeElement> classHandle;
    private ElementHandle<Element> elemHandle;
    private String annotationClass;
    
    public AbstractCreateAnnotationHint(FileObject fileObject,
            ElementHandle<TypeElement> classHandle,
            ElementHandle<Element> elemHandle,
            String annotationClass) {
        this.classHandle = classHandle;
        this.fileObject = fileObject;
        this.annotationClass = annotationClass;
        this.elemHandle = elemHandle;
    }
    
    public ChangeInfo implement(){
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>(){
            public void cancel() {}
            
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement clazz = classHandle.resolve(workingCopy);
                
                if (clazz != null){
                    GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                    
                    AnnotationTree annTree = genUtils.createAnnotation(annotationClass);
                    
                    Tree elemTree = workingCopy.getTrees().getTree(elemHandle.resolve(workingCopy));
                    Tree newElemTree = null;
                    
                    switch (elemTree.getKind()){
                    case ANNOTATION_TYPE:
                    case CLASS:
                    case ENUM:
                    case INTERFACE:
                        newElemTree = genUtils.addAnnotation((ClassTree)elemTree, annTree);
                        break;
                    case METHOD:
                        newElemTree = genUtils.addAnnotation((MethodTree)elemTree, annTree);
                        break;
                    case VARIABLE:
                        newElemTree = genUtils.addAnnotation((VariableTree)elemTree, annTree);
                        break;
                    }
                    
                    if (newElemTree != null){
                        workingCopy.rewrite(elemTree, newElemTree);
                    } else{
                        throw new IllegalStateException("Unsupported element type");
                    }
                }
            }
        };
        
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        
        try{
            javaSource.runModificationTask(task).commit();
        } catch (IOException e){
            JPAProblemFinder.LOG.log(Level.SEVERE, e.getMessage(), e);
        }
        
        return null;
    }
}

