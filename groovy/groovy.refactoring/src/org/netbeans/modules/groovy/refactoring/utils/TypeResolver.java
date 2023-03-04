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
