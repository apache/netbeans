/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

