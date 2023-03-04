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
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemFinder;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * @author Tomasz.Slota@Sun.COM
 */
public abstract class UnifyAccessType implements Fix {
    private FileObject fileObject;
    private ElementHandle<TypeElement> classHandle;
    
    public UnifyAccessType(FileObject fileObject, ElementHandle<TypeElement> classHandle) {
        this.classHandle = classHandle;
        this.fileObject = fileObject;
    }
    
    public ChangeInfo implement(){
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>(){
            public void cancel() {}
            
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement clazz = classHandle.resolve(workingCopy);
                
                if (clazz != null){
                    
                    for (ExecutableElement methodElem : ElementFilter.methodsIn(clazz.getEnclosedElements())){
                        if (methodElem.getSimpleName().toString().startsWith("get")){ //NOI18N
                            VariableElement fieldElem = ModelUtils.getField(clazz, ModelUtils.getFieldNameFromAccessor(methodElem.getSimpleName().toString()));
                            
                            if (fieldElem != null){
                                MethodTree methodTree = workingCopy.getTrees().getTree((methodElem));
                                VariableTree fieldTree = (VariableTree) workingCopy.getTrees().getTree(fieldElem);
                                
                                ModifiersTree srcModifiersTree = getSourceModifiers(fieldTree, methodTree);
                                
                                List <AnnotationTree> remainingAnnotations = new LinkedList<AnnotationTree>();
                                List <AnnotationTree> newTargetAnnots = new LinkedList<AnnotationTree>();
                                
                                for (AnnotationTree annTree : srcModifiersTree.getAnnotations()){
                                    if (isJPAAttrAnnotation(workingCopy, annTree)){
                                        newTargetAnnots.add(annTree);
                                    } else {
                                        remainingAnnotations.add(annTree);
                                    }
                                }
                                
                                if (newTargetAnnots.size() > 0){
                                    TreeMaker make = workingCopy.getTreeMaker();
                                    ModifiersTree targetModifiers = getTargetModifiers(fieldTree, methodTree);
                                    
                                    workingCopy.rewrite(srcModifiersTree, make.Modifiers(srcModifiersTree, remainingAnnotations));
                                    newTargetAnnots.addAll(targetModifiers.getAnnotations());
                                    workingCopy.rewrite(targetModifiers,make.Modifiers(targetModifiers,newTargetAnnots));
                                }
                            }
                        }
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
    
    protected abstract ModifiersTree getSourceModifiers(VariableTree fieldTree, MethodTree methodTree);
    protected abstract ModifiersTree getTargetModifiers(VariableTree fieldTree, MethodTree methodTree);
    
    public int hashCode(){
        return 1;
    }
    
    public boolean equals(Object o){
        // TODO: implement equals properly
        return super.equals(o);
    }
    
    private boolean isJPAAttrAnnotation(CompilationInfo cinfo, AnnotationTree ann){
        TreePath path = cinfo.getTrees().getPath(cinfo.getCompilationUnit(), ann.getAnnotationType());
        Element elem = cinfo.getTrees().getElement(path);
        String annType = elem.toString();
        return JPAAnnotations.MEMBER_LEVEL.contains(annType);
    }
    
    public static class UnifyPropertyAccess extends UnifyAccessType{
        public UnifyPropertyAccess(FileObject fileObject, ElementHandle<TypeElement> classHandle){
            super(fileObject, classHandle);
        }
        
        public String getText(){
            return NbBundle.getMessage(CreatePersistenceUnit.class, "LBL_UnifyPropertyAccess");
        }
        
        protected ModifiersTree getSourceModifiers(VariableTree fieldTree, MethodTree methodTree) {
            return fieldTree.getModifiers();
        }
        
        protected ModifiersTree getTargetModifiers(VariableTree fieldTree, MethodTree methodTree) {
            return methodTree.getModifiers();
        }
    }
    
    public static class UnifyFieldAccess extends UnifyAccessType{
        public UnifyFieldAccess(FileObject fileObject, ElementHandle<TypeElement> classHandle){
            super(fileObject, classHandle);
        }
        
        public String getText(){
            return NbBundle.getMessage(CreatePersistenceUnit.class, "LBL_UnifyFieldAccess");
        }
        
        protected ModifiersTree getSourceModifiers(VariableTree fieldTree, MethodTree methodTree) {
            return methodTree.getModifiers();
        }
        
        protected ModifiersTree getTargetModifiers(VariableTree fieldTree, MethodTree methodTree) {
            return fieldTree.getModifiers();
        }
    }
}
