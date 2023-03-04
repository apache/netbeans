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

package org.netbeans.modules.web.wizards;

import java.io.IOException;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;

/**
 * Generator of attributes for tag handler class
 *
 * @author  milan.kuchtiak@sun.com
 * Created on May, 2004
 */
public class TagHandlerGenerator {
    private Object[][] attributes;
    private JavaSource clazz;
    private boolean isBodyTag;
    private boolean evaluateBody;

    /** Creates a new instance of ListenerGenerator */
    public TagHandlerGenerator(JavaSource clazz, Object[][] attributes, boolean isBodyTag, boolean evaluateBody) {
        this.clazz=clazz;
        this.attributes=attributes;
        this.isBodyTag=isBodyTag;
        this.evaluateBody=evaluateBody;
    }
    
    public void generate() throws IOException {
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                GenerationUtils gu = GenerationUtils.newInstance(workingCopy);
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(workingCopy);
                ClassTree oldClassTree = workingCopy.getTrees().getTree(typeElement);
                ClassTree classTree = addFields(gu, make, typeElement, oldClassTree);
                if (isBodyTag) {
                    classTree = make.addClassMember(classTree, addBodyEvaluatorCheck(evaluateBody, make));
                }
                classTree = addSetters(gu, make, typeElement, classTree);
                workingCopy.rewrite(oldClassTree, classTree);
            }
        };
        ModificationResult result = clazz.runModificationTask(task);
        result.commit();
    }
    
    private ClassTree addFields(GenerationUtils gu, TreeMaker make, TypeElement typeElement, ClassTree classTree) throws IOException {
        for (int i = 0; i < attributes.length; i++) {
            VariableTree field = gu.createField(typeElement, gu.createModifiers(Modifier.PRIVATE), (String) attributes[i][0], (String) attributes[i][1], null);
            classTree = make.insertClassMember(classTree, i, field);
            
            //TODO: generate Javadoc
        }
        
        return classTree;
    }

    private ClassTree addSetters(GenerationUtils gu, TreeMaker make, TypeElement typeElement, ClassTree newClassTree) throws IOException {
        for (int i = 0; i < attributes.length; i++) {
            MethodTree setter = gu.createPropertySetterMethod(typeElement, gu.createModifiers(Modifier.PUBLIC), (String) attributes[i][0], (String) attributes[i][1]);
            newClassTree = make.addClassMember(newClassTree, setter);
            
            //TODO: generate Javadoc
        }
        
        return newClassTree;
    }

    private MethodTree addBodyEvaluatorCheck(boolean evaluateBody, TreeMaker make) throws IOException {        
        StringBuffer methodBody = new StringBuffer();
        methodBody.append("{"); //NOI18N
        methodBody.append("\n        // TODO: code that determines whether the body should be"); //NOI18N
        methodBody.append("\n        //       evaluated should be placed here."); //NOI18N
        methodBody.append("\n        //       Called from the doStartTag() method."); //NOI18N
        methodBody.append("\nreturn " + (evaluateBody ? "true;" : "false;")); //NOI18N
        methodBody.append("\n}"); //NOI18N
        
        MethodTree method = make.Method(
                make.Modifiers(Collections.<Modifier>singleton(Modifier.PRIVATE)),
                "theBodyShouldBeEvaluated", //NOI18N
                make.PrimitiveType(TypeKind.BOOLEAN),
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                Collections.<ExpressionTree>emptyList(),
                methodBody.toString(),
                null);
        
        //TODO: generate Javadoc
        
        return method;
    }

}
