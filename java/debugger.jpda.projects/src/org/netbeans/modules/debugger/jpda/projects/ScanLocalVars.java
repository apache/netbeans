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
package org.netbeans.modules.debugger.jpda.projects;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;

/**
 * Refactored from IntroduceFix originally by lahvac.
 * We need to pass in local variables and private fields.
 *
 * @author sdedic, Martin Entlicher
 */
final class ScanLocalVars extends ErrorAwareTreePathScanner<Void, Void> {
    
    private static final Set<ElementKind> LOCAL_VARIABLES = EnumSet.of(ElementKind.EXCEPTION_PARAMETER,
                                                                       ElementKind.LOCAL_VARIABLE,
                                                                       ElementKind.PARAMETER,
                                                                       ElementKind.RESOURCE_VARIABLE);
    private static final Set<ElementKind> FIELDS = EnumSet.of(ElementKind.FIELD);
    
    private final CompilationInfo info;
    private boolean hasReturns = false;
    
    private final Set<VariableElement> declaredVariables = new HashSet<>();
    private final Set<VariableElement> referencedVariables = new LinkedHashSet<>();
    private final StatementTree lastStatement;
    private final Set<TypeMirror> returnTypes = new HashSet<>();
    
    /**
     * Nesting level for local classes and lambdas. Ignore returns in nested scopes
     */
    private int nesting;

    public ScanLocalVars(CompilationInfo info, StatementTree lastStatement) {
        this.info = info;
        this.lastStatement = lastStatement;
    }

    @Override
    public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
        nesting++;
        super.visitLambdaExpression(node, p);
        nesting--;
        return null;
    }

    @Override
    public Void visitNewClass(NewClassTree node, Void p) {
        nesting++;
        super.visitNewClass(node, p);
        nesting--;
        return null;
    }

    @Override
    public Void visitClass(ClassTree node, Void p) {
        nesting++;
        super.visitClass(node, p);
        nesting--;
        return null;
    }
    
    

    @Override
    public Void visitVariable(VariableTree node, Void p) {
        Element e = info.getTrees().getElement(getCurrentPath());
        if (e != null && (LOCAL_VARIABLES.contains(e.getKind()) ||
                          FIELDS.contains(e.getKind()))) {
            declaredVariables.add((VariableElement) e);
        }
        return super.visitVariable(node, p);
    }

    @Override
    public Void visitIdentifier(IdentifierTree node, Void p) {
        Element e = info.getTrees().getElement(getCurrentPath());
        if (e != null && (LOCAL_VARIABLES.contains(e.getKind()) ||
                          FIELDS.contains(e.getKind()) && e.getModifiers().contains(Modifier.PRIVATE))) {
            if (!declaredVariables.contains((VariableElement) e)) {
                referencedVariables.add((VariableElement) e);
            }
        }
        return super.visitIdentifier(node, p);
    }
    
    private boolean isMethodCode() {
        return nesting == 0;
    }

    @Override
    public Void visitReturn(ReturnTree node, Void p) {
        if (isMethodCode() /*&& phase == PHASE_INSIDE_SELECTION*/) {
            hasReturns = true;
            TypeMirror type = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), node.getExpression())); //.asType().toString();
            if (type != null && type.getKind() != TypeKind.ERROR) {
                returnTypes.add(type);
            } else {
                // Unresolved element
                TypeElement object = info.getElements().getTypeElement("java.lang.Object");
                if (object != null) {
                    returnTypes.add(object.asType());
                }
            }
        }
        return super.visitReturn(node, p);
    }

    @Override
    public Void visitExpressionStatement(ExpressionStatementTree node, Void p) {
        if (node == lastStatement) {
            if (!hasReturns) {
                ExpressionTree expression = node.getExpression();
                TypeMirror type = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), expression));
                if (type != null && TypeKind.ERROR != type.getKind()) {
                    returnTypes.add(type);
                }
            }
        }
        return super.visitExpressionStatement(node, p);
    }
    
    Set<VariableElement> getReferencedVariables() {
        return referencedVariables;
    }

    String getReturnType() {
        if (returnTypes.isEmpty()) {
            return null;
        } else {
            return returnTypes.iterator().next().toString();
        }
    }

    TypeMirror getReturnTypeMirror() {
        if (returnTypes.isEmpty()) {
            return null;
        } else {
            return returnTypes.iterator().next();
        }
    }

    boolean hasReturns() {
        return hasReturns;
    }
    
}
