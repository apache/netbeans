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

import java.util.Collections;
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
            Set<Modifier> methodModifs = new HashSet<Modifier>();
            methodModifs.add(Modifier.PRIVATE);
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
