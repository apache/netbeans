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
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemFinder;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * @author Tomasz.Slota@Sun.COM
 */
public class CreateTemporalAnnotationHint implements Fix {
        private FileObject fileObject;
    private ElementHandle<Element> elementHandle;
    
    /** Creates a new instance of ImplementSerializable */
    public CreateTemporalAnnotationHint(FileObject fileObject, ElementHandle<Element> elementHandle) {
        this.elementHandle = elementHandle;
        this.fileObject = fileObject;
    }
    
    public ChangeInfo implement(){
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>(){
            public void cancel() {}
            
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                Element elem = elementHandle.resolve(workingCopy);
                
                if (elem != null){    
                    Tree tree = workingCopy.getTrees().getTree(elem);
                    TreeMaker make = workingCopy.getTreeMaker();
                    
                    ModifiersTree modifiersTree = null;
                    
                    if (tree.getKind() == Tree.Kind.VARIABLE){
                        modifiersTree = ((VariableTree)tree).getModifiers();
                    }
                    else if (tree.getKind() == Tree.Kind.METHOD){
                        modifiersTree = ((MethodTree)tree).getModifiers();
                    }
                    else{
                        throw new IllegalStateException();
                    }
                    
                    TypeElement temporalAnnType = workingCopy.getElements().getTypeElement(JPAAnnotations.TEMPORAL);
                    Tree annType = make.QualIdent(temporalAnnType);
                    AnnotationTree temporalAnn = make.Annotation(annType, Collections.singletonList(make.Identifier("javax.persistence.TemporalType.DATE")));
                    
                    List<AnnotationTree> newAnnots = new ArrayList<AnnotationTree>();
                    newAnnots.addAll(modifiersTree.getAnnotations());
                    newAnnots.add(temporalAnn);
                    
                    ModifiersTree newModifiers = make.Modifiers(modifiersTree, newAnnots);
                    workingCopy.rewrite(modifiersTree, newModifiers);
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
    
    public int hashCode(){
        return 1;
    }
    
    public boolean equals(Object o){
        // TODO: implement equals properly
        return super.equals(o);
    }
    
    public String getText(){
        return NbBundle.getMessage(CreatePersistenceUnit.class, "LBL_CreateTemporalAnnotationHint");
    }
}
