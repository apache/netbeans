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

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.editor.java.Utilities;
import static org.netbeans.modules.java.hints.errors.Utilities.findOwningExecutable;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Lahoda
 */
public final class AddCast implements ErrorRule<Void> {

    private static final Set<String> ERROR_CODES = new HashSet<String>(Arrays.asList(
            "compiler.err.prob.found.req", // NOI18N
            "compiler.err.cant.apply.symbol", // NOI18N
            "compiler.err.cant.apply.symbol.1", // NOI18N
            "compiler.err.cant.resolve.location.args", // NOI18N
            "compiler.err.cant.apply.symbols",
            "compiler.err.prob.found.req/compiler.misc.incompatible.ret.type.in.lambda/compiler.misc.inconvertible.types")); // NOI18N
    
    static void computeType(CompilationInfo info, int offset, List<TypeMirror> targetType, TreePath[] typeTree, ExpressionTree[] expression, Tree[] leaf) {
        TreePath path = info.getTreeUtilities().pathFor(offset + 1);
        int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), path.getLeaf());
        
        //TODO: this does not seem nice:
        while (path != null) {
            Tree scope = path.getLeaf();
            List<TypeMirror> expected = null;
            TreePath expectedTree = null;
            TypeMirror resolved = null;
            ExpressionTree found = null;
            
            if (scope.getKind() == Kind.VARIABLE && ((VariableTree) scope).getInitializer() != null) {
                expected = Collections.singletonList(info.getTrees().getTypeMirror(path));
                expectedTree = new TreePath(path, ((VariableTree) scope).getType());
                found = ((VariableTree) scope).getInitializer();
                resolved = info.getTrees().getTypeMirror(new TreePath(path, found));
            }
            
            if (scope.getKind() == Kind.ASSIGNMENT) {
                expected = Collections.singletonList(info.getTrees().getTypeMirror(path));
                found = ((AssignmentTree) scope).getExpression();
                resolved = info.getTrees().getTypeMirror(new TreePath(path, found));
            }
            
            if (scope.getKind() == Kind.RETURN) {
                TreePath parents = path;
                
                while (parents != null && 
                    (parents.getLeaf().getKind() != Kind.METHOD && parents.getLeaf().getKind() != Kind.LAMBDA_EXPRESSION)) {
                    parents = parents.getParentPath();
                }                
                if (parents != null) {
                    Tree p = parents.getLeaf();
                    TypeMirror returnType = null;
                    if (p.getKind() == Kind.METHOD) {
                        Tree returnTypeTree = ((MethodTree) parents.getLeaf()).getReturnType();
                        if (returnTypeTree != null) {
                            returnType = info.getTrees().getTypeMirror(new TreePath(parents, returnTypeTree));
                        }
                    } else if (p.getKind() == Kind.LAMBDA_EXPRESSION) {
                        TypeMirror lambdaType = info.getTrees().getTypeMirror(parents);
                        if (org.netbeans.modules.java.hints.errors.Utilities.isValidType(lambdaType) &&
                            lambdaType.getKind() == TypeKind.DECLARED) {
                            ExecutableType et = info.getTypeUtilities().getDescriptorType((DeclaredType)lambdaType);
                            if (et != null && 
                                org.netbeans.modules.java.hints.errors.Utilities.isValidType(et.getReturnType()) &&
                                et.getReturnType().getKind() != TypeKind.VOID) {
                                returnType = et.getReturnType();
                            }
                        }
                    }
                    if (returnType != null && (found = ((ReturnTree) scope).getExpression()) != null) {
                        expected = Collections.singletonList(returnType);
                        resolved = info.getTrees().getTypeMirror(new TreePath(path, found));
                    }
                }
            }
            
            if (scope.getKind() == Kind.METHOD_INVOCATION || scope.getKind() == Kind.NEW_CLASS) {
                List<TypeMirror> proposed = new ArrayList<TypeMirror>();
                int[] index = new int[1];
                
                if (!Utilities.fuzzyResolveMethodInvocation(info, path, proposed, index).isEmpty()) {
                    expected = proposed;
                    found = scope.getKind() == Kind.METHOD_INVOCATION ? ((MethodInvocationTree) scope).getArguments().get(index[0]) : ((NewClassTree) scope).getArguments().get(index[0]);
                    resolved = info.getTrees().getTypeMirror(new TreePath(path, found));
                }
            }
            
            if (scope.getKind() == Kind.LAMBDA_EXPRESSION) {
                LambdaExpressionTree let = (LambdaExpressionTree)scope;
                if (let.getBodyKind() == LambdaExpressionTree.BodyKind.EXPRESSION) {
                    TypeMirror expIfaceType = info.getTrees().getTypeMirror(path);
                    
                    // rule out weird errors
                    if (expIfaceType != null && expIfaceType.getKind() == TypeKind.DECLARED) {
                        Element el = info.getTypes().asElement(expIfaceType);
                        if (el != null && (el.getKind().isClass() || el.getKind().isInterface())) {
                            for (Element m : el.getEnclosedElements()) {
                                if (m.getKind() == ElementKind.METHOD) {
                                    TypeMirror t = info.getTypes().asMemberOf((DeclaredType)expIfaceType, m);
                                    if (t.getKind() == TypeKind.EXECUTABLE) {
                                        expected = Collections.singletonList(
                                                ((ExecutableType)t).getReturnType());
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    found = (ExpressionTree)let.getBody();
                    resolved = info.getTrees().getTypeMirror(new TreePath(path, found));
                }
            }
            
            if (expected != null && resolved != null) {
                TypeMirror foundTM = info.getTrees().getTypeMirror(new TreePath(path, found));

                if (foundTM.getKind() == TypeKind.ERROR) {
                    foundTM = info.getTrees().getOriginalType((ErrorType) foundTM);
                }

                if (resolved.getKind() == TypeKind.ERROR) {
                    resolved = info.getTrees().getOriginalType((ErrorType) resolved);
                }

                if (   foundTM.getKind() == TypeKind.EXECUTABLE
                    || foundTM.getKind() == TypeKind.PACKAGE
                    || foundTM.getKind() == TypeKind.NONE
                    || foundTM.getKind() == TypeKind.OTHER) {
                    //XXX: ignoring executable, see AddCast9 for more information when this happens.
                    //XXX: ignoring NONE, see test161450
                } else {
                    targetType.clear();//clean up, - may be related to test136313
                    
                    for (TypeMirror expectedType : expected) {
                        if (!org.netbeans.modules.java.hints.errors.Utilities.isValidType(expectedType)) {
                            continue;
                        }
                        if (info.getTypeUtilities().isCastable(resolved, expectedType)) {
                            if (!info.getTypes().isAssignable(foundTM, expectedType)
                                    /*#85346: cast hint should not be proposed for error types:*/
                                    && foundTM.getKind() != TypeKind.ERROR
                                    && expectedType.getKind() != TypeKind.ERROR) {
                                targetType.add(org.netbeans.modules.java.hints.errors.Utilities.resolveTypeForDeclaration(info, expectedType));
                                typeTree[0] = expectedTree;
                                expression[0] = found;
                                leaf[0] = scope;
                            }
                        }
                    }
                }
            }
            
            if (info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), scope) < start && scope.getKind() != Kind.PARENTHESIZED) {
                break;
            }

            path = path.getParentPath();
        }
    }

    public Set<String> getCodes() {
        return ERROR_CODES;
    }
    
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        List<Fix> result = new ArrayList<Fix>();
        List<TypeMirror> targetType = new ArrayList<TypeMirror>();
        TreePath[] tmTree = new TreePath[1];
        ExpressionTree[] expression = new ExpressionTree[1];
        Tree[] leaf = new Tree[1];
        
        computeType(info, offset, targetType, tmTree, expression, leaf);
        
        if (!targetType.isEmpty()) {
            TreePath expressionPath = TreePath.getPath(info.getCompilationUnit(), expression[0]); //XXX: performance
            for (TypeMirror type : targetType) {
                if (type.getKind() != TypeKind.NULL) {
                    result.add(new AddCastFix(info, expressionPath, tmTree[0], type).toEditorFix());
                }
            }
        }
        
        return result;
    }
    
    public void cancel() {
        //XXX: not done yet
    }
    
    public String getId() {
        return AddCast.class.getName();
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(AddCast.class, "LBL_Add_Cast");
    }
    
    public String getDescription() {
        return NbBundle.getMessage(AddCast.class, "DSC_Add_Cast");
    }
    
}
