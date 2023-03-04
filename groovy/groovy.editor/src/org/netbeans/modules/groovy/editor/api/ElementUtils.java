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

package org.netbeans.modules.groovy.editor.api;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.groovy.editor.api.ASTUtils.FakeASTNode;

/**
 *
 * @author Martin Janicek
 */
public final class ElementUtils {

    private ElementUtils() {
    }

    public static ElementKind getKind(AstPath path, BaseDocument doc, int caret) {
        ASTNode node = path.leaf();
        ASTNode leafParent = path.leafParent();

        if ((node instanceof ClassNode) ||
            (node instanceof ClassExpression) ||
            (node instanceof CatchStatement) ||
            (node instanceof AnnotationNode) ||
            (node instanceof ConstructorCallExpression) ||
            FindTypeUtils.isCaretOnClassNode(path, doc, caret)) {
            return ElementKind.CLASS;
        } else if ((node instanceof MethodNode)) {
            if ("<init>".equals(((MethodNode) node).getName())) { // NOI18N
                return ElementKind.CONSTRUCTOR;
            }
            return ElementKind.METHOD;
        } else if ((node instanceof ConstantExpression) && (leafParent instanceof MethodCallExpression)) {
            return ElementKind.METHOD;
        } else if (node instanceof FieldNode) {
            return ElementKind.FIELD;
        } else if (node instanceof PropertyNode) {
            return ElementKind.PROPERTY;
        } else if (node instanceof VariableExpression) {
            Variable variable = ((VariableExpression) node).getAccessedVariable();
            if (variable instanceof DynamicVariable) {
                // Not sure now if this is 100% correct, but if we have VariableExpression
                // like "Book^mark.get()" the accessed variable Bookmark (which is the type
                // name) is marked as DynamicVariable and in that case we want to return
                // different ElementKind in oposite to usage of 'normal' variables
                return ElementKind.CLASS;
            }
            return ElementKind.VARIABLE;
        } else if (node instanceof Parameter) {
            return ElementKind.VARIABLE;
        } else if (node instanceof DeclarationExpression) {
            return ElementKind.VARIABLE;
        } else if ((node instanceof ConstantExpression) && (leafParent instanceof PropertyExpression)) {
            return ElementKind.VARIABLE;
        } else if (node instanceof PackageNode) {
            return ElementKind.PACKAGE;
        }
        return ElementKind.OTHER;
    }

    public static String getTypeName(ASTNode node) {
        ClassNode type = getType(node);
        return normalizeTypeName(type.getName(), type);
    }

    public static String getTypeNameWithoutPackage(ASTNode node) {
        ClassNode type = getType(node);
        return normalizeTypeName(type.getNameWithoutPackage(), type);
    }

    /**
     * Returns type for the given ASTNode. For example if FieldNode is passed
     * as a parameter, it returns type of the given field etc. If the Method call
     * is passed as a parameter, the method tried to interfere proper type and return it
     *
     * @param node where we want to know declared type
     * @return type of the given node
     * @throws IllegalStateException if an implementation is missing for the given ASTNode type
     */
    public static ClassNode getType(ASTNode node) {
        if (node instanceof FakeASTNode) {
            node = ((FakeASTNode) node).getOriginalNode();
        }

        if (node instanceof ClassNode) {
            ClassNode clazz = ((ClassNode) node);
            if (clazz.getComponentType() != null) {
                return clazz.getComponentType();
            } else {
                return clazz;
            }
        } else if (node instanceof AnnotationNode) {
            return ((AnnotationNode) node).getClassNode();
        } else if (node instanceof FieldNode) {
            return ((FieldNode) node).getType();
        } else if (node instanceof PropertyNode) {
            return ((PropertyNode) node).getType();
        } else if (node instanceof MethodNode) {
            return ((MethodNode) node).getReturnType();
        } else if (node instanceof Parameter) {
           return ((Parameter) node).getType();
        } else if (node instanceof ForStatement) {
            return ((ForStatement) node).getVariableType();
        } else if (node instanceof CatchStatement) {
            return ((CatchStatement) node).getVariable().getOriginType();
        } else if (node instanceof ImportNode) {
            return ((ImportNode) node).getType();
        } else if (node instanceof ClassExpression) {
            return ((ClassExpression) node).getType();
        } else if (node instanceof VariableExpression) {
            return ((VariableExpression) node).getType();
        } else if (node instanceof DeclarationExpression) {
            DeclarationExpression declaration = ((DeclarationExpression) node);
            if (declaration.isMultipleAssignmentDeclaration()) {
                return declaration.getTupleExpression().getType();
            } else {
                return declaration.getVariableExpression().getType();
            }
        } else if (node instanceof ConstructorCallExpression) {
            return ((ConstructorCallExpression) node).getType();
        } else if (node instanceof ArrayExpression) {
            return ((ArrayExpression) node).getElementType();
        }
        throw new IllegalStateException("Not implemented yet - GroovyRefactoringElement.getType() needs to be improve!"); // NOI18N
    }

    public static String getNameWithoutPackage(ASTNode node) {
        if (node instanceof FakeASTNode) {
            node = ((FakeASTNode) node).getOriginalNode();
        }

        String name = null;
        if (node instanceof ClassNode) {
            name = ((ClassNode) node).getNameWithoutPackage();
        } else if (node instanceof AnnotationNode) {
            return ((AnnotationNode) node).getText();
        } else if (node instanceof MethodNode) {
            name = ((MethodNode) node).getName();
            if ("<init>".equals(name)) { // NOI18N
                name = getDeclaringClassNameWithoutPackage(node);
            }
        } else if (node instanceof FieldNode) {
            name = ((FieldNode) node).getName();
        } else if (node instanceof PropertyNode) {
            name = ((PropertyNode) node).getName();
        } else if (node instanceof Parameter) {
            name = ((Parameter) node).getName();
        } else if (node instanceof ForStatement) {
            name = ((ForStatement) node).getVariableType().getNameWithoutPackage();
        } else if (node instanceof CatchStatement) {
            name = ((CatchStatement) node).getVariable().getName();
        } else if (node instanceof ImportNode) {
            name = ((ImportNode) node).getType().getNameWithoutPackage();
        } else if (node instanceof ClassExpression) {
            name = ((ClassExpression) node).getType().getNameWithoutPackage();
        } else if (node instanceof VariableExpression) {
            name = ((VariableExpression) node).getName();
        } else if (node instanceof DeclarationExpression) {
            DeclarationExpression declaration = ((DeclarationExpression) node);
            if (declaration.isMultipleAssignmentDeclaration()) {
                name = declaration.getTupleExpression().getType().getNameWithoutPackage();
            } else {
                name = declaration.getVariableExpression().getType().getNameWithoutPackage();
            }
        } else if (node instanceof ConstantExpression) {
            name = ((ConstantExpression) node).getText();
        } else if (node instanceof MethodCallExpression) {
            name = ((MethodCallExpression) node).getMethodAsString();
        } else if (node instanceof ConstructorCallExpression) {
            name = ((ConstructorCallExpression) node).getType().getNameWithoutPackage();
        } else if (node instanceof ArrayExpression) {
            name = ((ArrayExpression) node).getElementType().getNameWithoutPackage();
        }


        if (name != null) {
            return normalizeTypeName(name, null);
        }
        throw new IllegalStateException("Not implemented yet - GroovyRefactoringElement.getName() needs to be improve for type: " + node.getClass().getSimpleName()); // NOI18N
    }

    public static ClassNode getDeclaringClass(ASTNode node) {
        if (node instanceof ClassNode) {
            return (ClassNode) node;
        } else if (node instanceof AnnotationNode) {
            return ((AnnotationNode) node).getClassNode();
        } else if (node instanceof MethodNode) {
            return ((MethodNode) node).getDeclaringClass();
        } else if (node instanceof FieldNode) {
            return ((FieldNode) node).getDeclaringClass();
        } else if (node instanceof PropertyNode) {
            return ((PropertyNode) node).getDeclaringClass();
        } else if (node instanceof Parameter) {
            return ((Parameter) node).getDeclaringClass();
        } else if (node instanceof ForStatement) {
            return ((ForStatement) node).getVariableType().getDeclaringClass();
        } else if (node instanceof CatchStatement) {
            return ((CatchStatement) node).getVariable().getDeclaringClass();
        } else if (node instanceof ImportNode) {
            return ((ImportNode) node).getDeclaringClass();
        } else if (node instanceof ClassExpression) {
            return ((ClassExpression) node).getType().getDeclaringClass();
        } else if (node instanceof VariableExpression) {
            return ((VariableExpression) node).getDeclaringClass();
        } else if (node instanceof DeclarationExpression) {
            DeclarationExpression declaration = ((DeclarationExpression) node);
            if (declaration.isMultipleAssignmentDeclaration()) {
                return declaration.getTupleExpression().getDeclaringClass();
            } else {
                return declaration.getVariableExpression().getDeclaringClass();
            }
        } else if (node instanceof ConstantExpression) {
            return ((ConstantExpression) node).getDeclaringClass();
        } else if (node instanceof MethodCallExpression) {
            return ((MethodCallExpression) node).getType();
        } else if (node instanceof ConstructorCallExpression) {
            return ((ConstructorCallExpression) node).getType();
        } else if (node instanceof ArrayExpression) {
            return ((ArrayExpression) node).getDeclaringClass();
        }

        throw new IllegalStateException("Not implemented yet - GroovyRefactoringElement.getDeclaringClass() ..looks like the type: " + node.getClass().getName() + " isn't handled at the moment!"); // NOI18N
    }

    public static String getDeclaringClassName(ASTNode node) {
        ClassNode declaringClass = getDeclaringClass(node);
        if (declaringClass != null) {
            return declaringClass.getName();
        }
        return null;
    }

    public static String getDeclaringClassNameWithoutPackage(ASTNode node) {
        ClassNode declaringClass = getDeclaringClass(node);
        if (declaringClass != null) {
            return declaringClass.getNameWithoutPackage();
        }
        return null;
    }

    public static String normalizeTypeName(String typeName, ClassNode type) {
        // This will happened with all primitive type arrays, e.g. 'double [] x'
        if (typeName.startsWith("[") && type != null) { // NOI18N
            typeName = type.getComponentType().getNameWithoutPackage();
        }

        // This will happened with all arrays except primitive type arrays
        if (typeName.endsWith("[]")) { // NOI18N
            typeName = typeName.substring(0, typeName.length() - 2);
        }

        if (typeName.endsWith(";")) { // NOI18N
            typeName = typeName.substring(0, typeName.length() - 1);
        }
        return typeName;
    }
}
