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

package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;


/**
 * This creates the hint to change the type of a variable to the type of 
 * initializer expression. In effect it works opposite of Add Cast hint.
 * 
 * @author Sandip Chitale
 */
public final class ChangeType implements ErrorRule<Void> {
    
    static void computeType(CompilationInfo info, int offset, TypeMirror[] tm, TypeMirror[] expressionType, Tree[] leaf) {
        TreePath path = info.getTreeUtilities().pathFor(offset);
        
        // Try to locate the VARIABLE tree
        if (path != null) {
            Tree scope = path.getLeaf();
            TypeMirror expected = null;
            TypeMirror resolved = null;
            
            // Check if this is an assignment. 
            if (scope.getKind() == Kind.ASSIGNMENT) {
            	path = path.getParentPath();
            	if (path != null) {
            		// set it's parent as the scope
            		scope = path.getLeaf();
            	}
            } else if (scope.getKind() == Kind.MEMBER_SELECT) {
                path = path.getParentPath();
                if (path != null) {
                    // set it's parent as the scope
                    scope = path.getLeaf();
                    if (scope.getKind() == Kind.METHOD_INVOCATION) {
                        path = path.getParentPath();
                        if (path != null) {
                            // set it's parent as the scope
                            scope = path.getLeaf();
                        }
                    }
                }
            } else if (scope.getKind() == Kind.IDENTIFIER) {
                path = path.getParentPath();
                if (path != null) {
                    // set it's parent as the scope
                    scope = path.getLeaf();
                    if (scope.getKind() == Kind.MEMBER_SELECT ||
                            scope.getKind() == Kind.METHOD_INVOCATION) {
                        path = path.getParentPath();
                        if (path != null) {
                            // set it's parent as the scope
                            scope = path.getLeaf();
                        }
                    }
                }
            }
            
            if (scope.getKind() == Kind.ENHANCED_FOR_LOOP) {
                EnhancedForLoopTree efl = (EnhancedForLoopTree) scope;

                path = new TreePath(path, efl.getVariable());
                scope = efl.getVariable();

                resolved = org.netbeans.modules.java.hints.errors.Utilities.getIterableGenericType(info, new TreePath(path, efl.getExpression()));
            }

            // Is this a VARIABLE tree
            if (scope.getKind() == Kind.VARIABLE) {
                if (((VariableTree) scope).getInitializer() != null) {
                    resolved = info.getTrees().getTypeMirror(new TreePath(path, ((VariableTree) scope).getInitializer()));
                }

                expected = info.getTrees().getTypeMirror(path);
            }

            if (expected != null && resolved != null) {
                resolved = org.netbeans.modules.java.hints.errors.Utilities.resolveTypeForDeclaration(info, resolved);

                if (resolved == null || resolved.getKind() == TypeKind.VOID || resolved.getKind() == TypeKind.NONE ||resolved.getKind() == TypeKind.EXECUTABLE || resolved.getKind() == TypeKind.NULL) {
                } else if (resolved.getKind() != TypeKind.ERROR &&
                		expected.getKind() != TypeKind.ERROR) {
                    tm[0] = expected;
                    expressionType[0] = resolved;
                    leaf[0] = scope;
                }
            }
        }
    }

    // Initialize the compiler error codes to which this hint responds.
    private static Set<String> codes = new HashSet<String>();
    static
    {
        codes = new HashSet<String>();
        codes.add("compiler.err.prob.found.req"); // NOI18N
        codes.add("compiler.err.incomparable.types"); // NOI18N
        codes = Collections.unmodifiableSet(codes);
    }
    
    public Set<String> getCodes() {
        return codes;
    }
    
    public List<Fix> run(CompilationInfo info,
            String diagnosticKey,
            int offset,
            TreePath treePath,
            Data<Void> data) {
        List<Fix> result = new ArrayList<Fix>();
        TypeMirror[] tm = new TypeMirror[1];
        TypeMirror[] expressionType = new TypeMirror[1];
        Tree[] leaf = new Tree[1];
        
        computeType(info, offset, tm, expressionType, leaf);
        
        if (leaf[0] instanceof VariableTree) {
            if (tm[0] != null) {
                // special hack: if the reported path is lambda assigned to the variable, do not offer anything:
                // I don't know how to compute the desired var type for lambda (lambdas are attributed with respect to the environment) or what the
                // proper lambda type should be.
                VariableTree vt = (VariableTree)leaf[0];
                if (treePath != null && treePath.getLeaf() == vt.getInitializer() && treePath.getLeaf().getKind() == Tree.Kind.LAMBDA_EXPRESSION) {
                    return null;
                }
                //anonymous class?
                expressionType[0] = org.netbeans.modules.java.hints.errors.Utilities.convertIfAnonymous(expressionType[0]);

                result.add(new ChangeTypeFix(info.getJavaSource(),
                        ((VariableTree) leaf[0]).getName().toString(), 
                        Utilities.getTypeName(info, expressionType[0], false).toString(), offset));
            }
        }
        
        return result;
    }
    
    public void cancel() {
        //XXX: not done yet
    }
    
    public String getId() {
        return ChangeType.class.getName();
    }
    
    public String getDisplayName() {
        return  NbBundle.getMessage(ChangeType.class, "MSG_ChangeVariablesTypeDisplayName"); // NOI18N
    }
    
    public String getDescription() {
        return NbBundle.getMessage(ChangeType.class, "MSG_ChangeVariablesTypeDescription"); // NOI18N
    }
}
