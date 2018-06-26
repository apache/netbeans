/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
            Set<Modifier> methodModifs = new HashSet<Modifier>();
            methodModifs.add(Modifier.PRIVATE);
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
