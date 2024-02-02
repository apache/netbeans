/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.websvc.core.jaxws.actions;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;

import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.JavaSource.Phase;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;

import org.netbeans.modules.websvc.api.support.java.SourceUtils;


/**
 * @author ads
 *
 */
class DispatchClientMethodGenerator implements Task<WorkingCopy> {
    
    DispatchClientMethodGenerator(String operation, String body, int position){
        this.operation =operation;
        this.position = position;
        this.body = body;
    }

    /* (non-Javadoc)
     * @see org.netbeans.api.java.source.Task#run(java.lang.Object)
     */
    @Override
    public void run( WorkingCopy copy ) throws Exception {
        copy.toPhase(Phase.RESOLVED);
        ClassTree javaClass = SourceUtils.getPublicTopLevelTree(copy);
        if (javaClass != null) {
            
            identifyConext(javaClass, copy);
            if ( isMethodBody() ){
                return;
            }

            TreeMaker maker = copy.getTreeMaker();
            Set<Modifier> methodModifs = EnumSet.of(Modifier.PRIVATE);
            Tree returnTypeTree = maker.PrimitiveType(TypeKind.VOID);
            MethodTree methodTree = maker.Method (
                    maker.Modifiers(methodModifs),
                    suggestMethodName(javaClass),
                    returnTypeTree,
                    Collections.<TypeParameterTree>emptyList(),
                    Collections.<VariableTree>emptyList(),
                    Collections.<ExpressionTree>emptyList(),
                    "{"+body+"}",
                    null); //NOI18N

            ClassTree modifiedClass = maker.addClassMember(javaClass, methodTree);
            copy.rewrite(javaClass, modifiedClass);
        }        
    }

    public boolean isMethodBody() {
        return isBodyContext;
    }
    
    private void identifyConext(ClassTree javaClass, WorkingCopy copy) {
        TreePath treePath = copy.getTreeUtilities().pathFor(position);
        boolean isBlock = false;
        while( treePath!= null ){
            Tree tree = treePath.getLeaf();
            if ( tree instanceof BlockTree ){
                isBlock = true;
            }
            if ( tree instanceof MethodTree && isBlock ){
                isBodyContext = true;
                return;
            }
            treePath = treePath.getParentPath();
        }
    }
    
    private String suggestMethodName(ClassTree javaClass) {
        String suggestedName = operation;
        boolean scanningNeeded = true;
        int i=1;
        while (scanningNeeded) {
            boolean foundDuplicity = false;
            for (Tree classMember : javaClass.getMembers()) {
                if (Tree.Kind.METHOD == classMember.getKind()) {
                    MethodTree method = (MethodTree) classMember;
                    if (method.getName().contentEquals(suggestedName)) {
                        suggestedName = operation+"_"+String.valueOf(i++); //NOI18N
                        foundDuplicity = true;
                        break;
                    }
                }
            }
            scanningNeeded = foundDuplicity;
        }
        return suggestedName;
    }

    private String operation;
    private int position;
    private String body;
    private boolean isBodyContext;
}
