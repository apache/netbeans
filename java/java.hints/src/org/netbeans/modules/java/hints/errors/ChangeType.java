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

package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

                result.add(new ChangeTypeFix(info, treePath,
                        ((VariableTree) leaf[0]).getName().toString(), 
                        Utilities.getTypeName(info, expressionType[0], false).toString(), offset).toEditorFix());
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
