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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.EnumSet;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import static org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;

/**
 *
 * @author mkuchtiak
 */
public class JaxWsClientMethodGenerator implements CancellableTask<WorkingCopy> {
    boolean isStatic;
    String operationName;
    String returnType;
    String[] paramTypes;
    String[] paramNames;
    String[] exceptionTypes;
    String body;

    public JaxWsClientMethodGenerator(boolean isStatic, String operationName, String returnType, String[] paramTypes, String[] paramNames, String[] exceptionTypes, String body ) {
        this.isStatic = isStatic;
        this.operationName = operationName;
        this.returnType = returnType;
        this.paramTypes = paramTypes;
        this.paramNames = paramNames;
        this.exceptionTypes = exceptionTypes;
        this.body = body;
    }

    @Override
    public void run(WorkingCopy copy) throws Exception {
        copy.toPhase(Phase.RESOLVED);
        ClassTree javaClass = SourceUtils.getPublicTopLevelTree(copy);
        if (javaClass != null) {

            TreeMaker maker = copy.getTreeMaker();
            Set<Modifier> methodModifs = EnumSet.of(Modifier.PRIVATE);
            if (isStatic) {
               methodModifs.add(Modifier.STATIC);
            }
            TypeElement returnTypeEl = copy.getElements().getTypeElement(returnType);
            ExpressionTree returnTypeTree =  returnTypeEl == null ? maker.Identifier(returnType) : maker.QualIdent(returnTypeEl);
            List<VariableTree> variables = new ArrayList<VariableTree>();
            ModifiersTree fieldModif = maker.Modifiers(Collections.<Modifier>emptySet());
            for (int i=0; i<paramTypes.length; i++) {
                ExpressionTree paramType = maker.Identifier(paramTypes[i]);
                variables.add(maker.Variable(fieldModif, paramNames[i], paramType, null)); //NOI18N
            }

            List<ExpressionTree> exceptions = new ArrayList<ExpressionTree>();
            for (int i=0; i<exceptionTypes.length; i++) {
                TypeElement exceptionTypeEl = copy.getElements().getTypeElement(exceptionTypes[i]);
                ExpressionTree exceptionTypeTree =  exceptionTypeEl == null ?
                    maker.Identifier(exceptionTypes[i]) : maker.QualIdent(exceptionTypeEl);
                exceptions.add(exceptionTypeTree);
            }

            MethodTree methodTree = maker.Method (
                    maker.Modifiers(methodModifs),
                    suggestMethodName(javaClass, operationName),
                    returnTypeTree,
                    Collections.<TypeParameterTree>emptyList(),
                    variables,
                    exceptions,
                    body,
                    null); //NOI18N

            ClassTree modifiedClass = maker.addClassMember(javaClass, methodTree);
            copy.rewrite(javaClass, modifiedClass);
        }
    }

    @Override
    public void cancel() {}

    private String suggestMethodName(ClassTree javaClass, String suggestedMethodName) {
        String suggestedName = suggestedMethodName;
        boolean scanningNeeded = true;
        int i=1;
        while (scanningNeeded) {
            boolean foundDuplicity = false;
            for (Tree classMember : javaClass.getMembers()) {
                if (Tree.Kind.METHOD == classMember.getKind()) {
                    MethodTree method = (MethodTree) classMember;
                    if (method.getName().contentEquals(suggestedName)) {
                        suggestedName = suggestedMethodName+"_"+String.valueOf(i++); //NOI18N
                        foundDuplicity = true;
                        break;
                    }
                }
            }
            scanningNeeded = foundDuplicity;
        }
        return suggestedName;
    }

}
