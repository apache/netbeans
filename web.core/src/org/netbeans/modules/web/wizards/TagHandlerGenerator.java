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
