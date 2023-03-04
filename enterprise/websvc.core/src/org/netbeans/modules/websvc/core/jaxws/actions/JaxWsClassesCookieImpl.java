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
package org.netbeans.modules.websvc.core.jaxws.actions;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.modules.websvc.api.support.java.GenerationUtils;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import static org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public class JaxWsClassesCookieImpl implements JaxWsClassesCookie {
    Service service;
    FileObject implClassFO;
    
    public JaxWsClassesCookieImpl(Service service, FileObject implClassFO) {
        this.service = service;
        this.implClassFO = implClassFO;
    }
    
    @org.netbeans.api.annotations.common.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    @Override
    public void addOperation(final MethodTree method) {
        JavaSource targetSource = JavaSource.forFileObject(implClassFO);
        CancellableTask<WorkingCopy> modificationTask = 
            new CancellableTask<WorkingCopy>() 
            {
            @Override
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);            
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree javaClass = SourceUtils.
                    getPublicTopLevelTree(workingCopy);
                if (javaClass!=null) {
                    GenerationUtils genUtils = GenerationUtils.newInstance(
                                workingCopy);
                    
                    AnnotationTree webMethodAnnotation = make.Annotation(
                        make.QualIdent("javax.jws.WebMethod"),      //NOI18N
                        Collections.<ExpressionTree>emptyList()
                    );
                    // add @WebMethod annotation
                    ModifiersTree modifiersTree = make.addModifiersAnnotation(
                            method.getModifiers(), webMethodAnnotation);
                    
                    // add @Oneway annotation
                    if (Kind.PRIMITIVE_TYPE == method.getReturnType().getKind()) {
                        PrimitiveTypeTree primitiveType = 
                            (PrimitiveTypeTree)method.getReturnType();
                        if (TypeKind.VOID == primitiveType.getPrimitiveTypeKind()) {
                            AnnotationTree oneWayAnnotation = make.Annotation(
                                make.QualIdent("javax.jws.Oneway"),         // NOI18N
                                Collections.<ExpressionTree>emptyList()
                            );
                            modifiersTree = make.addModifiersAnnotation(
                                    modifiersTree, oneWayAnnotation);
                        }
                    }
                    
                    // add @WebParam annotations 
                    List<? extends VariableTree> parameters = method.getParameters();
                    List<VariableTree> newParameters = new ArrayList<VariableTree>();
                    for (VariableTree param:parameters) {
                        AnnotationTree paramAnnotation = make.Annotation(
                            make.QualIdent("javax.jws.WebParam"),           //NOI18N
                            Collections.<ExpressionTree>singletonList(
                                make.Assignment(make.Identifier("name"),    //NOI18N
                                        make.Literal(param.getName().toString()))) 
                        );
                        newParameters.add(genUtils.addAnnotation(param, paramAnnotation));
                    }
                    // create new (annotated) method
                    MethodTree  annotatedMethod = make.Method(
                                modifiersTree,
                                method.getName(),
                                method.getReturnType(),
                                method.getTypeParameters(),
                                newParameters,
                                method.getThrows(),
                                method.getBody(),
                                (ExpressionTree)method.getDefaultValue());
                    Comment comment = Comment.create(NbBundle.getMessage(
                            JaxWsClassesCookieImpl.class, "TXT_WSOperation"));      //NOI18N                 
                    make.addComment(annotatedMethod, comment, true);
                    
                    ClassTree modifiedClass = make.addClassMember(javaClass,
                            annotatedMethod);
                    workingCopy.rewrite(javaClass, modifiedClass);
                }
            }
            @Override
            public void cancel() {
                
            }
        };
        try {
            targetSource.runModificationTask(modificationTask).commit();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
}

