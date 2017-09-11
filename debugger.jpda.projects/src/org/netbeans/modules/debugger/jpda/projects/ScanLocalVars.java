/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
import com.sun.source.util.TreePathScanner;
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
final class ScanLocalVars extends TreePathScanner<Void, Void> {
    
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
            Element retExpElem = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getExpression())); //.asType().toString();
            if (retExpElem != null) {
                returnTypes.add(getElementType(retExpElem));
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
                Element retExpElem = info.getTrees().getElement(new TreePath(getCurrentPath(), expression));
                if (retExpElem == null) {
                    TreePath elementPath = null;
                    if (Tree.Kind.ASSIGNMENT.equals(expression.getKind())) {
                        elementPath = new TreePath(getCurrentPath(), ((AssignmentTree) expression).getVariable());
                    } else if (Tree.Kind.VARIABLE.equals(expression.getKind())) {
                        elementPath = new TreePath(getCurrentPath(), ((VariableTree) expression));
                    }
                    if (elementPath != null) {
                        retExpElem = info.getTrees().getElement(elementPath);
                    }
                }
                if (retExpElem != null && !TypeKind.ERROR.equals(retExpElem.asType().getKind())) {
                    returnTypes.add(getElementType(retExpElem));
                }
            }
        }
        return super.visitExpressionStatement(node, p);
    }
    
    private TypeMirror getElementType(Element element) {
        switch (element.getKind()) {
            case METHOD:
            case CONSTRUCTOR:
                return ((ExecutableElement) element).getReturnType();
            default:
                return element.asType();
        }
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
