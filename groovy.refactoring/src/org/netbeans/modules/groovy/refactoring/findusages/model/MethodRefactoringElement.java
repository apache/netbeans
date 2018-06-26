/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.refactoring.findusages.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.groovy.editor.api.ElementUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Janicek
 */
public class MethodRefactoringElement extends RefactoringElement {

    private ClassNode methodType;

    public MethodRefactoringElement(FileObject fileObject, ASTNode node) {
        this(fileObject, node, null);
    }

    public MethodRefactoringElement(FileObject fileObject, ASTNode node, ClassNode methodType) {
        super(fileObject, node);
        this.methodType = methodType;
    }


    @Override
    public String getName() {
        return ElementUtils.getNameWithoutPackage(node);
    }

    @Override
    public ElementKind getKind() {
        if (node instanceof ConstructorNode || node instanceof ConstructorCallExpression) {
            return ElementKind.CONSTRUCTOR;
        }
        return ElementKind.METHOD;
    }

    @Override
    public String getShowcase() {
        if (node instanceof ConstructorNode) {
            return getConstructorSignature((ConstructorNode) node);
        } else  if (node instanceof MethodNode) {
            return getMethodSignature((MethodNode) node);
        } else if (node instanceof ConstructorCallExpression) {
            return getConstructorSignature((ConstructorCallExpression) node);
        } else if (node instanceof MethodCallExpression) {
            return getMethodSignature((MethodCallExpression) node);
        }
        return "Should not happened - MethodRefactoringElement: getShowcase():85"; // NOI18N
    }

    public String getMethodTypeName() {
        return methodType.getName();
    }

    public ClassNode getMethodType() {
        return methodType;
    }

    public List<String> getMethodParameters() {
        if (node instanceof MethodCallExpression) {
            return getParamTypes((MethodCallExpression) node);
        }
        if (node instanceof MethodNode) {
            return getParams((MethodNode) node);
        }
        assert false : "Weird class type found: " + node.getClass().getSimpleName();
        return Collections.emptyList();
    }

    private List<String> getParamTypes(MethodCallExpression methodCall) {
        final Expression arguments = methodCall.getArguments();
        final List<String> paramTypes = new ArrayList<String>();

        if (arguments instanceof ArgumentListExpression) {
            ArgumentListExpression argumentList = ((ArgumentListExpression) arguments);
            if (argumentList.getExpressions().size() > 0) {
                for (Expression argument : argumentList.getExpressions()) {
                    paramTypes.add(ElementUtils.getTypeName(argument.getType()));
                }
            }
        }
        return paramTypes;
    }

    private List<String> getParams(MethodNode methodNode) {
        final List<String> paramTypes = new ArrayList<String>();

        for (Parameter param : methodNode.getParameters()) {
            paramTypes.add(ElementUtils.getTypeName(param));
        }
        return paramTypes;
    }

    public void setMethodType(ClassNode methodType) {
        this.methodType = methodType;
    }

    private String getMethodSignature(MethodNode method) {
        final MethodSignatureBuilder builder = new MethodSignatureBuilder();
        builder.appendMethodName(method);
        builder.appendMethodParams(method);
        builder.appendReturnType(method);

        return builder.toString();
    }

    private String getConstructorSignature(ConstructorNode constructor) {
        final MethodSignatureBuilder builder = new MethodSignatureBuilder();
        builder.appendMethodName(constructor);
        builder.appendMethodParams(constructor);

        return builder.toString();
    }

    private String getConstructorSignature(ConstructorCallExpression constructorCall) {
        final MethodSignatureBuilder builder = new MethodSignatureBuilder();
        builder.appendMethodName(constructorCall);
        builder.appendMethodParams(constructorCall);

        return builder.toString();
    }

    private String getMethodSignature(MethodCallExpression methodCall) {
        final MethodSignatureBuilder builder = new MethodSignatureBuilder();
        builder.appendMethodName(methodCall);
        builder.appendMethodParams(methodCall);
        builder.appendReturnType(methodCall);

        return builder.toString();
    }
}
