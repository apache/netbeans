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

package org.netbeans.modules.groovy.editor.occurrences;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.netbeans.api.lexer.Token;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.ASTUtils.FakeASTNode;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.ElementUtils;
import org.netbeans.modules.groovy.editor.api.FindTypeUtils;
import org.netbeans.modules.groovy.editor.api.Methods;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;

/**
 * Visitor for finding occurrences of the class types, variables and methods.
 *
 * @author Martin Adamek
 * @author Martin Janicek
 */
public final class VariableScopeVisitor extends TypeVisitor {

    private final Set<ASTNode> occurrences;
    private final ASTNode leafParent;


    public VariableScopeVisitor(SourceUnit sourceUnit, AstPath path, BaseDocument doc, int cursorOffset) {
        super(sourceUnit, path, doc, cursorOffset, true);
        this.occurrences = new HashSet<ASTNode>();
        this.leafParent = path.leafParent();
    }

    public Set<ASTNode> getOccurrences() {
        return occurrences;
    }

    @Override
    public void visitArrayExpression(ArrayExpression visitedArray) {
        final ClassNode visitedType = visitedArray.getElementType();
        final String visitedName = ElementUtils.getTypeName(visitedType);

        if (FindTypeUtils.isCaretOnClassNode(path, doc, cursorOffset)) {
            ASTNode currentNode = FindTypeUtils.findCurrentNode(path, doc, cursorOffset);
            addOccurrences(visitedType, (ClassNode) currentNode);
        } else if (leaf instanceof Variable) {
            String varName = removeParentheses(((Variable) leaf).getName());
            if (varName.equals(visitedName)) {
                occurrences.add(new FakeASTNode(visitedType, visitedName));
            }
        } else if (leaf instanceof ConstantExpression && leafParent instanceof PropertyExpression) {
            if (visitedName.equals(((PropertyExpression) leafParent).getPropertyAsString())) {
                occurrences.add(new FakeASTNode(visitedType, visitedName));
            }
        }
        super.visitArrayExpression(visitedArray);
    }

    @Override
    protected void visitParameters(Parameter[] parameters, Variable variable) {
        // method is declaring given variable, let's visit only the method,
        // but we need to check also parameters as those are not part of method visit
        for (Parameter parameter : parameters) {
            ClassNode paramType = parameter.getType();
            if (FindTypeUtils.isCaretOnClassNode(path, doc, cursorOffset)) {
                addOccurrences(paramType, (ClassNode) FindTypeUtils.findCurrentNode(path, doc, cursorOffset));
            } else {
                if (parameter.getName().equals(variable.getName())) {
                    occurrences.add(parameter);
                    break;
                }
            }
        }
        super.visitParameters(parameters, variable);
    }

    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        if (expression.isParameterSpecified() && (leaf instanceof Variable)) {
            visitParameters(expression.getParameters(), (Variable) leaf);
        }
        super.visitClosureExpression(expression);
    }


    @Override
    protected boolean isValidToken(Token<GroovyTokenId> currentToken, Token<GroovyTokenId> previousToken) {
        // cursor must be positioned on identifier, otherwise occurences doesn't make sense
        // second check is here because we want to have occurences also at the end of the identifier (see issue #155574)
        return currentToken.id() == GroovyTokenId.IDENTIFIER || previousToken.id() == GroovyTokenId.IDENTIFIER;
    }
    
    @Override
    public void visitVariableExpression(VariableExpression variableExpression) {
        final ClassNode visitedType = variableExpression.getType();
        final String visitedName = variableExpression.getName();

        if (FindTypeUtils.isCaretOnClassNode(path, doc, cursorOffset)) {
            addOccurrences(visitedType, (ClassNode) FindTypeUtils.findCurrentNode(path, doc, cursorOffset));
        } else {
            if (leaf instanceof FieldNode) {
                if (visitedName.equals(((FieldNode) leaf).getName())) {
                    occurrences.add(variableExpression);
                }
            } else if (leaf instanceof PropertyNode) {
                if (visitedName.equals(((PropertyNode) leaf).getField().getName())) {
                    occurrences.add(variableExpression);
                }
            } else if (leaf instanceof Variable) {
                if (visitedName.equals(((Variable) leaf).getName())) {
                    occurrences.add(variableExpression);
                }
            } else if (leaf instanceof ForStatement) {
                if (visitedName.equals(((ForStatement) leaf).getVariable().getName())) {
                    occurrences.add(variableExpression);
                }
            } else if (leaf instanceof ConstantExpression && leafParent instanceof PropertyExpression) {
                PropertyExpression property = (PropertyExpression) leafParent;
                if (variableExpression.getName().equals(property.getPropertyAsString())) {
                    occurrences.add(variableExpression);
                }

            // #234000
            } else if (leaf instanceof BlockStatement) {
                ASTNode root = path.root();
                if (root instanceof ModuleNode) {
                    for (Map.Entry<String, ImportNode> entry : ((ModuleNode) root).getStaticImports().entrySet()) {
                        String alias = entry.getKey();

                        if (!alias.equals(variableExpression.getName())) {
                            continue;
                        }

                        OffsetRange range = ASTUtils.getNextIdentifierByName(doc, alias, cursorOffset);
                        if (range.containsInclusive(cursorOffset)) {
                            occurrences.add(variableExpression);
                        }
                    }
                }
            }
        }
        super.visitVariableExpression(variableExpression);
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        ClassNode visitedType;
        if (!expression.isMultipleAssignmentDeclaration()) {
            visitedType = expression.getVariableExpression().getType();
        } else {
            visitedType = expression.getTupleExpression().getType();
        }

        if (FindTypeUtils.isCaretOnClassNode(path, doc, cursorOffset)) {
            addOccurrences(visitedType, (ClassNode) FindTypeUtils.findCurrentNode(path, doc, cursorOffset));
        }
        super.visitDeclarationExpression(expression);
    }

    @Override
    public void visitField(FieldNode visitedField) {
        final String visitedName = visitedField.getName();

        if (FindTypeUtils.isCaretOnClassNode(path, doc, cursorOffset)) {
            addFieldOccurrences(visitedField, (ClassNode) FindTypeUtils.findCurrentNode(path, doc, cursorOffset));
        } else {
            if (leaf instanceof FieldNode) {
                if (visitedName.equals(((FieldNode) leaf).getName())) {
                    occurrences.add(visitedField);
                }
            } else if (leaf instanceof PropertyNode) {
                if (visitedName.equals(((PropertyNode) leaf).getField().getName())) {
                    occurrences.add(visitedField);
                }
            } else if (leaf instanceof Variable) {
                if (visitedName.equals(((Variable) leaf).getName())) {
                    occurrences.add(visitedField);
                }
            } else if (leaf instanceof ConstantExpression && leafParent instanceof PropertyExpression) {
                PropertyExpression property = (PropertyExpression) leafParent;
                if (visitedName.equals(property.getPropertyAsString())) {
                    occurrences.add(visitedField);
                }
            }
        }
        super.visitField(visitedField);
    }

    private void addFieldOccurrences(FieldNode visitedField, ClassNode findingNode) {
        addOccurrences(visitedField.getType(), findingNode);

        // Check all field level annotations
        for (AnnotationNode annotation : visitedField.getAnnotations(findingNode)) {
            addAnnotationOccurrences(annotation, findingNode);
        }
    }

    @Override
    public void visitMethod(MethodNode methodNode) {
        VariableScope variableScope = methodNode.getVariableScope();

        if (FindTypeUtils.isCaretOnClassNode(path, doc, cursorOffset)) {
            addMethodOccurrences(methodNode, (ClassNode) FindTypeUtils.findCurrentNode(path, doc, cursorOffset));
        } else {
            if (leaf instanceof Variable) {
                String name = ((Variable) leaf).getName();
                // This check is here because we can have method parameter with the same
                // name hidding property/field and we don't want to show occurences of these
                if (variableScope != null && variableScope.getDeclaredVariable(name) != null) {
                    return;
                }
            } else if (leaf instanceof MethodNode) {
                if (Methods.isSameMethod(methodNode, (MethodNode) leaf)) {
                    occurrences.add(methodNode);
                }
            } else if (leaf instanceof DeclarationExpression) {
                VariableExpression variable = ((DeclarationExpression) leaf).getVariableExpression();
                if (!variable.isDynamicTyped() && !methodNode.isDynamicReturnType()) {
                    addMethodOccurrences(methodNode, variable.getType());
                }
            } else if (leaf instanceof ConstantExpression && leafParent instanceof MethodCallExpression) {
                MethodCallExpression methodCallExpression = (MethodCallExpression) leafParent;
                if (Methods.isSameMethod(methodNode, methodCallExpression)) {
                    occurrences.add(methodNode);
                }
            }
        }
        super.visitMethod(methodNode);
    }

    private void addMethodOccurrences(MethodNode visitedMethod, ClassNode findingNode) {
        // Check return type
        addOccurrences(visitedMethod.getReturnType(), findingNode);

        // Check method parameters
        for (Parameter parameter : visitedMethod.getParameters()) {
            addOccurrences(parameter.getType(), findingNode);
        }

        // Check annotations
        for (AnnotationNode annotation : visitedMethod.getAnnotations(findingNode)) {
            addAnnotationOccurrences(annotation, findingNode);
        }
    }

    @Override
    public void visitConstructor(ConstructorNode constructor) {
        if (FindTypeUtils.isCaretOnClassNode(path, doc, cursorOffset)) {
            addConstructorOccurrences(constructor, (ClassNode) FindTypeUtils.findCurrentNode(path, doc, cursorOffset));
        } else {
            final VariableScope variableScope = constructor.getVariableScope();
            if (leaf instanceof Variable) {
                String name = ((Variable) leaf).getName();
                if (variableScope != null && variableScope.getDeclaredVariable(name) != null) {
                    return;
                }
            } else if (leaf instanceof ConstantExpression && leafParent instanceof PropertyExpression) {
                String name = ((ConstantExpression) leaf).getText();
                if (variableScope != null && variableScope.getDeclaredVariable(name) != null) {
                    return;
                }
            }

            if (leaf instanceof ConstructorNode) {
                if (Methods.isSameConstructor(constructor, (ConstructorNode) leaf)) {
                    occurrences.add(constructor);
                }
            } else if (leaf instanceof ConstructorCallExpression) {
                if (Methods.isSameConstructor(constructor, (ConstructorCallExpression) leaf)) {
                    if (!constructor.hasNoRealSourcePosition()) {
                        occurrences.add(constructor);
                    }
                }
            }
        }
        super.visitConstructor(constructor);
    }

    private void addConstructorOccurrences(ConstructorNode constructor, ClassNode findingNode) {
        for (Parameter parameter : constructor.getParameters()) {
            addOccurrences(parameter.getType(), findingNode);
        }

        for (AnnotationNode annotation : constructor.getAnnotations(findingNode)) {
            addAnnotationOccurrences(annotation, findingNode);
        }
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression methodCall) {
        if (!FindTypeUtils.isCaretOnClassNode(path, doc, cursorOffset)) {
            if (leaf instanceof MethodNode) {
                MethodNode method = (MethodNode) leaf;
                if (Methods.isSameMethod(method, methodCall)) {
                    occurrences.add(methodCall);
                }
            } else if (leaf instanceof ConstantExpression && leafParent instanceof MethodCallExpression) {
                if (Methods.isSameMethod(methodCall, (MethodCallExpression) leafParent)) {
                    occurrences.add(methodCall);
                }
            }

            // #234000
            if (leaf instanceof BlockStatement) {
                ASTNode root = path.root();
                if (root instanceof ModuleNode) {
                    for (Map.Entry<String, ImportNode> entry : ((ModuleNode) root).getStaticImports().entrySet()) {
                        String alias = entry.getKey();
                        if (!alias.equals(methodCall.getMethodAsString())) {
                            continue;
                        }

                        OffsetRange range = ASTUtils.getNextIdentifierByName(doc, alias, cursorOffset);
                        if (range.containsInclusive(cursorOffset)) {
                            occurrences.add(methodCall);
                        }
                    }
                }
            }
        }
        super.visitMethodCallExpression(methodCall);
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        // This might happened for constructor call with generics e.g. "new ArrayList<String>()"
        // In that case we want to highligt only in situation where the caret
        // is on "String" type, but not if the caret location is on ArrayList
         if (FindTypeUtils.isCaretOnClassNode(path, doc, cursorOffset)) {
             ClassNode findingNode = (ClassNode) FindTypeUtils.findCurrentNode(path, doc, cursorOffset);
             final String callName = ElementUtils.getNameWithoutPackage(call);
             final String findingNodeName = ElementUtils.getNameWithoutPackage(findingNode);
             if (!callName.equals(findingNodeName)) {
                addOccurrences(call.getType(), findingNode);
             }
        } else {
            if (leaf instanceof ConstructorNode) {
                ConstructorNode constructor = (ConstructorNode) leaf;
                if (Methods.isSameConstructor(constructor, call)) {
                    occurrences.add(call);
                }
            } else if (leaf instanceof ConstructorCallExpression) {
                if (Methods.isSameConstuctor(call, (ConstructorCallExpression) leaf)) {
                    occurrences.add(call);
                }
            }
        }
        super.visitConstructorCallExpression(call);
    }

    @Override
    public void visitClassExpression(ClassExpression clazz) {
        if (FindTypeUtils.isCaretOnClassNode(path, doc, cursorOffset)) {
            addClassExpressionOccurrences(clazz, (ClassNode) FindTypeUtils.findCurrentNode(path, doc, cursorOffset));
        }
        super.visitClassExpression(clazz);
    }

    private void addClassExpressionOccurrences(ClassExpression clazz, ClassNode findingNode) {
        final String visitedName = ElementUtils.getTypeName(clazz);
        final String findingName = ElementUtils.getTypeName(findingNode);
        if (visitedName.equals(findingName)) {
            occurrences.add(clazz);
        }
    }

    @Override
    public void visitClass(ClassNode classNode) {
        if (FindTypeUtils.isCaretOnClassNode(path, doc, cursorOffset)) {
            addClassNodeOccurrences(classNode, (ClassNode) FindTypeUtils.findCurrentNode(path, doc, cursorOffset));
        }
        super.visitClass(classNode);
    }

    private void addClassNodeOccurrences(ClassNode visitedNode, ClassNode findingNode) {
        final String findingName = ElementUtils.getTypeName(findingNode);
        final ClassNode superClass = visitedNode.getUnresolvedSuperClass(false);
        final ClassNode[] interfaces = visitedNode.getInterfaces();

        // Check if the caret is on the ClassNode itself
        if (findingName.equals(visitedNode.getName())) {
            occurrences.add(new FakeASTNode(visitedNode, visitedNode.getNameWithoutPackage()));
        }

        // Check if the caret is on the parent type
        if (superClass.getLineNumber() > 0 && superClass.getColumnNumber() > 0) {
            if (findingName.equals(superClass.getName())) {
                occurrences.add(new FakeASTNode(superClass, superClass.getNameWithoutPackage()));
            }
        }

        // Check all implemented interfaces
        for (ClassNode interfaceNode : interfaces) {
            if (interfaceNode.getLineNumber() > 0 && interfaceNode.getColumnNumber() > 0) {
                if (findingName.equals(interfaceNode.getName())) {
                    occurrences.add(new FakeASTNode(interfaceNode, interfaceNode.getNameWithoutPackage()));
                }
            }
        }

        // Check all class level annotations
        for (AnnotationNode annotation : visitedNode.getAnnotations(findingNode)) {
            addAnnotationOccurrences(annotation, findingNode);
        }
    }
    
    @Override
    public void visitAttributeExpression(AttributeExpression expression) {
        addExpressionOccurrences(expression);
        super.visitAttributeExpression(expression);
    }

    @Override
    public void visitPropertyExpression(PropertyExpression node) {
        addExpressionOccurrences(node);
        super.visitPropertyExpression(node);
    }
    
    private void addExpressionOccurrences(PropertyExpression expression) {
        final Expression property = expression.getProperty();
        final String nodeAsString = expression.getPropertyAsString();
        
        if (nodeAsString != null) {
            if (leaf instanceof Variable && nodeAsString.equals(((Variable) leaf).getName())) {
                occurrences.add(property);
            } else if (leaf instanceof ConstantExpression && leafParent instanceof PropertyExpression) {
                PropertyExpression propertyUnderCursor = (PropertyExpression) leafParent;

                if (nodeAsString.equals(propertyUnderCursor.getPropertyAsString())) {
                    occurrences.add(property);
                }
            }
        }
    }

    @Override
    public void visitForLoop(ForStatement forLoop) {
        if (FindTypeUtils.isCaretOnClassNode(path, doc, cursorOffset)) {
            addOccurrences(forLoop.getVariableType(), (ClassNode) FindTypeUtils.findCurrentNode(path, doc, cursorOffset));
        } else {
            final Parameter forLoopVar = forLoop.getVariable();
            final String varName = forLoopVar.getName();
            
            if (leaf instanceof Variable) {
                if (varName.equals(((Variable) leaf).getName())) {
                    occurrences.add(forLoopVar);
                }
            } else if (leaf instanceof ForStatement) {
                if (varName.equals(((ForStatement) leaf).getVariable().getName())) {
                    occurrences.add(forLoopVar);
                }
            }
        }
        super.visitForLoop(forLoop);
    }

    @Override
    public void visitCatchStatement(CatchStatement statement) {
        if (FindTypeUtils.isCaretOnClassNode(path, doc, cursorOffset)) {
            addOccurrences(statement.getExceptionType(), (ClassNode) FindTypeUtils.findCurrentNode(path, doc, cursorOffset));
        }
        super.visitCatchStatement(statement);
    }

    @Override
    public void visitImports(ModuleNode node) {
        if (FindTypeUtils.isCaretOnClassNode(path, doc, cursorOffset)) {
            for (ImportNode importNode : node.getImports()) {
                addOccurrences(importNode.getType(), (ClassNode) FindTypeUtils.findCurrentNode(path, doc, cursorOffset));
            }
        } else {
            // #233954
            if (leaf instanceof ConstantExpression && leafParent instanceof MethodCallExpression) {
                addAliasOccurrences(node, ((MethodCallExpression) leafParent).getMethodAsString());
            }

            // #234000
            if (leaf instanceof VariableExpression) {
                addAliasOccurrences(node, ((VariableExpression) leaf).getName());
            }

            // For both: #233954 and #234000
            if (leaf instanceof BlockStatement) {
                for (Map.Entry<String, ImportNode> entry : node.getStaticImports().entrySet()) {
                    OffsetRange range = ASTUtils.getNextIdentifierByName(doc, entry.getKey(), cursorOffset);
                    if (range.containsInclusive(cursorOffset)) {
                        occurrences.add(new FakeASTNode(entry.getValue().getType(), entry.getKey()));
                    }
                }
            }
        }
        super.visitImports(node);
    }

    private void addAliasOccurrences(ModuleNode moduleNode, String findingName) {
        for (Map.Entry<String, ImportNode> entry : moduleNode.getStaticImports().entrySet()) {
            if (findingName.equals(entry.getKey())) {
                occurrences.add(new FakeASTNode(entry.getValue().getType(), entry.getKey()));
            }
        }
    }

    private void addAnnotationOccurrences(AnnotationNode annotation, ClassNode findingNode) {
        ClassNode classNode = annotation.getClassNode();
        classNode.setLineNumber(annotation.getLineNumber());
        classNode.setColumnNumber(annotation.getColumnNumber());
        classNode.setLastLineNumber(annotation.getLastLineNumber());
        classNode.setLastColumnNumber(annotation.getLastColumnNumber());

        addOccurrences(classNode, findingNode);
    }

    private void addOccurrences(ClassNode visitedType, ClassNode findingType) {
        final String visitedTypeName = ElementUtils.getTypeName(visitedType);
        final String findingName = ElementUtils.getTypeName(findingType);
        final String findingNameWithoutPkg = ElementUtils.getTypeNameWithoutPackage(findingType);

        if (visitedTypeName.equals(findingName)) {
            occurrences.add(new FakeASTNode(visitedType, findingNameWithoutPkg));
        }
        addGenericsOccurrences(visitedType, findingType);
    }

    private void addGenericsOccurrences(ClassNode visitedType, ClassNode findingNode) {
        final String findingTypeName = ElementUtils.getTypeName(findingNode);
        final GenericsType[] genericsTypes = visitedType.getGenericsTypes();

        if (genericsTypes != null && genericsTypes.length > 0) {
            for (GenericsType genericsType : genericsTypes) {
                final String genericTypeName = genericsType.getType().getName();
                final String genericTypeNameWithoutPkg = genericsType.getName();

                if (genericTypeName.equals(findingTypeName)) {
                    occurrences.add(new FakeASTNode(genericsType, genericTypeNameWithoutPkg));
                }
            }
        }
    }

    /**
     * Removes [] parentheses.
     *
     * @param name where we want to strip parentheses off
     * @return name without [] parentheses
     */
    private String removeParentheses(String name) {
        if (name.endsWith("[]")) { // NOI18N
            name = name.substring(0, name.length() - 2);
        }
        return name;
    }
}
