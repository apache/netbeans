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

package org.netbeans.modules.groovy.refactoring.utils;

import java.util.Set;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Janicek
 */
public final class TypeResolver {

    private TypeResolver() {
    }


    public static ClassNode resolveType(AstPath path, FileObject fo) {
        final ASTNode leaf = path.leaf();
        final ASTNode leafParent = path.leafParent();

        if (leaf instanceof VariableExpression) {
            return resolveType(path, (VariableExpression) leaf, fo);
        }
        if (leaf instanceof ConstantExpression) {
            if (leafParent instanceof MethodCallExpression) {
                return resolveMethodType(path, (MethodCallExpression) leafParent, fo);
            }
            if (leafParent instanceof PropertyExpression) {
                return resolveVariableType(path, (PropertyExpression) leafParent, fo);
            }
        }

        return null;
    }

    private static ClassNode resolveVariableType(AstPath path, PropertyExpression propertyExpression, FileObject fo) {
        return resolveType(path, propertyExpression.getObjectExpression(), fo);
    }

    private static ClassNode resolveMethodType(AstPath path, MethodCallExpression methodCall, FileObject fo) {
        return resolveType(path, methodCall.getObjectExpression(), fo);
    }

    private static ClassNode resolveType(AstPath path, Expression expression, FileObject fo) {
        if (expression instanceof VariableExpression) {
            VariableExpression variableExpression = ((VariableExpression) expression);
            Variable variable = variableExpression.getAccessedVariable();

            // Accessing through 'this' (e.g. this.someInt = 3)
            if (variable == null) {
                return ASTUtils.getOwningClass(path);
            }

            // Typically accessing class field directly without type or 'this' (e.g. someInt = 3)
            if (variable instanceof FieldNode) {
                return ((FieldNode) variable).getOwner();
            }

            // Normal accessing through the type (e.g. someLocalVar.someInt = 3)
            if (variable instanceof VariableExpression) {
                return variable.getType();
            }

            // Situations like: "GroovySupportObject.println()" but in cases where
            // GroovySupportObject is not recognized as a class type (e.g. it's not
            // imported --> see issue #226027 for more details)
            if (variable instanceof DynamicVariable) {
                Set<ClassNode> types = TypeFinder.findTypes(fo, variable.getName());

                if (!types.isEmpty()) {
                    return types.iterator().next();
                } else {
                    return null;
                }
            }
        } else if (expression instanceof ClassExpression) {
            // Situations like: "GroovySupportObject.println()"
            return ((ClassExpression) expression).getType();
        } else if (expression instanceof ConstructorCallExpression) {
            // Situations like: "new GalacticMaster().destroyWorldMethod()"
            return ((ConstructorCallExpression) expression).getType();
        }

        assert false; // Should never happened!
        return null;
    }
}
