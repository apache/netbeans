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

package org.netbeans.modules.groovy.editor.api;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.OffsetRange;

/**
 * Utility class for "find type usages". It provides some method for the correct
 * recognition whether we have caret location on type (respectively ClassNode) or
 * on the field, property, variable etc.
 *
 * @author Martin Janicek
 */
public final class FindTypeUtils {

    private FindTypeUtils() {
    }


    /**
     * Check if the caret location is placed on the class type of the field, property, method, etc.
     * or not. Typically if we can have declaration like <code>private String something</code>. For
     * example this method returns true in following case (assuming ^ symbol is the current caret
     * location):<br/><br/>
     *      <code>private St^ring something</code>
     *
     * <br/><br/>
     * ..but it returns false for the following case:<br/><br/>
     *      <code>private String somet^hing</code>
     *
     * <br/><br/>
     * This gives us a chance to recognize whether we are dealing with Find type usages
     * or with Find field usages. It applies of course also for property, variables,
     * methods, etc.
     *
     * @param path AST path to the current location
     * @param doc document
     * @param caret caret position
     * @return true if we are directly on the type, false otherwise
     */
    public static boolean isCaretOnClassNode(AstPath path, BaseDocument doc, int caret) {
        if (findCurrentNode(path, doc, caret) instanceof ClassNode) {
            return true;
        }
        return false;
    }

    public static ASTNode findCurrentNode(AstPath path, BaseDocument doc, int caret) {
        ASTNode leaf = path.leaf();

        if (isOnImportNode(path, doc, caret)) {
            return getCurrentImportNode(getCurrentModuleNode(path), doc, caret);
        }

        if (isOnPackageNode(path, doc, caret)) {
            return getCurrentModuleNode(path).getPackage();
        }

        if (leaf instanceof ClassNode) {
            ClassNode classNode = ((ClassNode) leaf);
            if (isCaretOnClassNode(classNode, doc, caret)) {
                return classNode;
            }
            ClassNode superClass = classNode.getUnresolvedSuperClass(false);
            if (isCaretOnClassNode(superClass, doc, caret)) {
                return superClass;
            }
            for (ClassNode interfaceNode : classNode.getInterfaces()) {
                if (isCaretOnClassNode(interfaceNode, doc, caret)) {
                    return interfaceNode;
                }
            }
            for (AnnotationNode annotation : classNode.getAnnotations()) {
                if (isCaretOnAnnotation(annotation, doc, caret)) {
                    return annotation.getClassNode();
                }
            }
        } else if (leaf instanceof FieldNode) {
            FieldNode field = (FieldNode) leaf;

            for (AnnotationNode annotation : field.getAnnotations()) {
                if (isCaretOnAnnotation(annotation, doc, caret)) {
                    return annotation.getClassNode();
                }
            }

            if (isCaretOnFieldType(field, doc, caret)) {
                return ElementUtils.getType(leaf);
            } else if (isCaretOnGenericType(field.getType(), doc, caret)) {
                return getGenericType(field.getType(), doc, caret);
            }
        } else if (leaf instanceof PropertyNode) {
            PropertyNode property = (PropertyNode) leaf;
            FieldNode field = property.getField();

            for (AnnotationNode annotation : field.getAnnotations()) {
                if (isCaretOnAnnotation(annotation, doc, caret)) {
                    return annotation.getClassNode();
                }
            }

            if (isCaretOnFieldType(field, doc, caret)) {
                return ElementUtils.getType(leaf);
            } else if (isCaretOnGenericType(field.getType(), doc, caret)) {
                return getGenericType(field.getType(), doc, caret);
            }
        } else if (leaf instanceof MethodNode) {
            MethodNode method = ((MethodNode) leaf);

            for (AnnotationNode annotation : method.getAnnotations()) {
                if (isCaretOnAnnotation(annotation, doc, caret)) {
                    return annotation.getClassNode();
                }
            }

            if (isCaretOnReturnType(method, doc, caret)) {
                return ElementUtils.getType(leaf);
            } else if (isCaretOnGenericType(method.getReturnType(), doc, caret)) {
                return getGenericType(method.getReturnType(), doc, caret);
            } else {
                return method;
            }
        } else if (leaf instanceof Parameter) {
            Parameter param = (Parameter) leaf;
            if (isCaretOnParamType(param, doc, caret)) {
                return ElementUtils.getType(leaf);
            } else if (isCaretOnGenericType(param.getType(), doc, caret)) {
                return getGenericType(param.getType(), doc, caret);
            }
        } else if (leaf instanceof ForStatement) {
            if (isCaretOnForStatementType(((ForStatement) leaf), doc, caret)) {
                return ((ForStatement) leaf).getVariableType();
            } else {
                return ((ForStatement) leaf).getVariable();
            }
        } else if (leaf instanceof CatchStatement) {
            CatchStatement catchStatement = (CatchStatement) leaf;
            if (isCaretOnCatchStatement(catchStatement, doc, caret)) {
                return catchStatement.getVariable().getType();
            }
        } else if (leaf instanceof ClassExpression) {
            if (isCaretOnClassExpressionType(((ClassExpression) leaf), doc, caret)) {
                return ElementUtils.getType(leaf);
            }
        } else if (leaf instanceof VariableExpression) {
            if (isCaretOnVariableType(((VariableExpression) leaf), doc, caret)) {
                return ElementUtils.getType(leaf);
            }
        } else if (leaf instanceof DeclarationExpression) {
            DeclarationExpression declaration = (DeclarationExpression) leaf;
            if (isCaretOnDeclarationType(declaration, doc, caret)) {
                return ElementUtils.getType(leaf);
            } else {
                ClassNode declarationType;
                if (!declaration.isMultipleAssignmentDeclaration()) {
                    declarationType = declaration.getVariableExpression().getType();
                } else {
                    declarationType = declaration.getTupleExpression().getType();
                }
                if (isCaretOnGenericType(declarationType, doc, caret)) {
                    return getGenericType(declarationType, doc, caret);
                }
            }
        } else if (leaf instanceof ArrayExpression) {
            if (isCaretOnArrayExpressionType(((ArrayExpression) leaf), doc, caret)) {
                return ElementUtils.getType(leaf);
            }
        } else if (leaf instanceof ConstructorCallExpression) {
            ClassNode constructorType = ((ConstructorCallExpression) leaf).getType();
            if (isCaretOnGenericType(constructorType, doc, caret)) {
                return getGenericType(constructorType, doc, caret);
            }
            return leaf;
        }
        return leaf;
    }

    private static boolean isOnImportNode(AstPath path, BaseDocument doc, int caret) {
        ModuleNode moduleNode = getCurrentModuleNode(path);
        if (moduleNode == null) {
            return false;
        }
        if (getCurrentImportNode(moduleNode, doc, caret) == null) {
            return false;
        }
        return true;
    }

    private static ModuleNode getCurrentModuleNode(AstPath path) {
        ASTNode leaf = path.leaf();
        ASTNode leafParent = path.leafParent();

        ModuleNode moduleNode = null;
        if (leaf instanceof ModuleNode) {
            moduleNode = (ModuleNode) leaf;
        } else if (leaf instanceof ClassNode) {
            moduleNode = ((ClassNode) leaf).getModule();
        } else if (leaf instanceof BlockStatement &&
                   leafParent instanceof MethodNode &&
                   path.root() instanceof ModuleNode) {

            // #218608 - Wrong highlighting on the import node in groovy scripts
            moduleNode = (ModuleNode) path.root();
        }
        return moduleNode;
    }

    private static ClassNode getCurrentImportNode(ModuleNode moduleNode, BaseDocument doc, int caret) {
        for (ImportNode importNode : moduleNode.getImports()) {
            if (isCaretOnImportStatement(importNode, doc, caret)) {
                if (!importNode.isStar()) {
                    return ElementUtils.getType(importNode);
                }
            }
        }
        return null;
    }

    private static boolean isOnPackageNode(AstPath path, BaseDocument doc, int caret) {
        ModuleNode moduleNode = getCurrentModuleNode(path);
        if (moduleNode == null || moduleNode.getPackage() == null) {
            return false;
        }

        if (isCaretOnPackageStatement(moduleNode.getPackage(), doc, caret)) {
            return true;
        }
        return false;
    }
    
    private static boolean isCaretOnClassNode(ClassNode classNode, BaseDocument doc, int cursorOffset) {
        if (getClassNodeRange(classNode, doc, cursorOffset) != OffsetRange.NONE) {
            return true;
        }
        return false;
    }

    private static boolean isCaretOnReturnType(MethodNode method, BaseDocument doc, int cursorOffset) {
        if (getMethodRange(method, doc, cursorOffset) != OffsetRange.NONE) {
            return true;
        }
        return false;
    }

    private static boolean isCaretOnFieldType(FieldNode field, BaseDocument doc, int cursorOffset) {
        if (getFieldRange(field, doc, cursorOffset) != OffsetRange.NONE) {
            return true;
        }
        return false;
    }

    private static boolean isCaretOnParamType(Parameter param, BaseDocument doc, int cursorOffset) {
        if (getParameterRange(param, doc, cursorOffset) != OffsetRange.NONE) {
            return true;
        }
        return false;
    }

    private static boolean isCaretOnForStatementType(ForStatement forLoop, BaseDocument doc, int cursorOffset) {
        if (getForLoopRange(forLoop, doc, cursorOffset) != OffsetRange.NONE) {
            return true;
        }
        return false;
    }

    private static boolean isCaretOnCatchStatement(CatchStatement catchStatement, BaseDocument doc, int cursorOffset) {
        if (getCatchStatementRange(catchStatement, doc, cursorOffset) != OffsetRange.NONE) {
            return true;
        }
        return false;
    }

    private static boolean isCaretOnImportStatement(ImportNode importNode, BaseDocument doc, int cursorOffset) {
        if (getImportRange(importNode, doc, cursorOffset) != OffsetRange.NONE) {
            return true;
        }
        return false;
    }

    private static boolean isCaretOnPackageStatement(PackageNode packageNode, BaseDocument doc, int cursorOffset) {
        if (getPackageRange(packageNode, doc, cursorOffset) != OffsetRange.NONE) {
            return true;
        }
        return false;
    }

    private static boolean isCaretOnDeclarationType(DeclarationExpression expression, BaseDocument doc, int cursorOffset) {
        if (getDeclarationExpressionRange(expression, doc, cursorOffset) != OffsetRange.NONE) {
            return true;
        }
        return false;
    }

    private static boolean isCaretOnClassExpressionType(ClassExpression expression, BaseDocument doc, int cursorOffset) {
        if (getClassExpressionRange(expression, doc, cursorOffset) != OffsetRange.NONE) {
            return true;
        }
        return false;
    }

    private static boolean isCaretOnArrayExpressionType(ArrayExpression expression, BaseDocument doc, int cursorOffset) {
        if (getArrayExpressionRange(expression, doc, cursorOffset) != OffsetRange.NONE) {
            return true;
        }
        return false;
    }

    private static boolean isCaretOnVariableType(VariableExpression expression, BaseDocument doc, int cursorOffset) {
        if (getVariableRange(expression, doc, cursorOffset) != OffsetRange.NONE) {
            return true;
        }
        return false;
    }

    private static boolean isCaretOnGenericType(ClassNode classNode, BaseDocument doc, int cursorOffset) {
        GenericsType[] genericsTypes = classNode.getGenericsTypes();
        if (genericsTypes != null && genericsTypes.length > 0) {
            for (GenericsType genericsType : genericsTypes) {
                if (getGenericTypeRange(genericsType, doc, cursorOffset) != OffsetRange.NONE) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isCaretOnAnnotation(AnnotationNode annotation, BaseDocument doc, int cursorOffset) {
        if (getAnnotationRange(annotation, doc, cursorOffset) != OffsetRange.NONE) {
            return true;
        }
        return false;
    }

    private static boolean isCaretOnGenericType(GenericsType genericsType, BaseDocument doc, int cursorOffset) {
        if (getGenericTypeRange(genericsType, doc, cursorOffset) != OffsetRange.NONE) {
            return true;
        }
        return false;
    }

    private static ClassNode getGenericType(ClassNode classNode, BaseDocument doc, int cursorOffset) {
        GenericsType[] genericsTypes = classNode.getGenericsTypes();
        if (genericsTypes != null && genericsTypes.length > 0) {
            for (GenericsType genericsType : genericsTypes) {
                if (isCaretOnGenericType(genericsType, doc, cursorOffset)) {
                    return genericsType.getType();
                }
            }
        }
        return null;
    }

    private static OffsetRange getGenericTypeRange(GenericsType genericType, BaseDocument doc, int cursorOffset) {
        final int offset = ASTUtils.getOffset(doc, genericType.getLineNumber(), genericType.getColumnNumber());
        final OffsetRange range = ASTUtils.getNextIdentifierByName(doc, genericType.getType().getNameWithoutPackage(), offset);
        if (range.containsInclusive(cursorOffset)) {
            return range;
        }
        return OffsetRange.NONE;
    }

    private static OffsetRange getAnnotationRange(AnnotationNode annotation, BaseDocument doc, int cursorOffset) {
        final int offset = ASTUtils.getOffset(doc, annotation.getLineNumber(), annotation.getColumnNumber());
        final OffsetRange range = ASTUtils.getNextIdentifierByName(doc, annotation.getClassNode().getNameWithoutPackage(), offset);
        if (range.containsInclusive(cursorOffset)) {
            return range;
        }
        return OffsetRange.NONE;
    }

    private static OffsetRange getDeclarationExpressionRange(DeclarationExpression expression, BaseDocument doc, int cursorOffset) {
        OffsetRange range;
        if (!expression.isMultipleAssignmentDeclaration()) {
            range = getVariableRange(expression.getVariableExpression(), doc, cursorOffset);
        } else {
            range = getRange(expression.getTupleExpression(), doc, cursorOffset);
        }
        
        return range;
    }

    private static OffsetRange getClassExpressionRange(ClassExpression expression, BaseDocument doc, int cursorOffset) {
        return getRange(expression, doc, cursorOffset);
    }

    private static OffsetRange getArrayExpressionRange(ArrayExpression expression, BaseDocument doc, int cursorOffset) {
        return getRange(expression.getElementType(), doc, cursorOffset);
    }

    private static OffsetRange getMethodRange(MethodNode method, BaseDocument doc, int cursorOffset) {
        if (method.isDynamicReturnType()) {
            return OffsetRange.NONE;
        }
        return getRange(method, doc, cursorOffset);
    }

    private static OffsetRange getClassNodeRange(ClassNode classNode, BaseDocument doc, int cursorOffset) {
        return getRange(classNode, doc, cursorOffset);
    }

    private static OffsetRange getFieldRange(FieldNode field, BaseDocument doc, int cursorOffset) {
        if (field.isDynamicTyped()) {
            return OffsetRange.NONE;
        }
        return getRange(field, doc, cursorOffset);
    }

    private static OffsetRange getParameterRange(Parameter param, BaseDocument doc, int cursorOffset) {
        if (param.isDynamicTyped()) {
            return OffsetRange.NONE;
        }
        return getRange(param, doc, cursorOffset);
    }

    private static OffsetRange getForLoopRange(ForStatement forLoop, BaseDocument doc, int cursorOffset) {
        return getRange(forLoop.getVariableType(), doc, cursorOffset);
    }

    private static OffsetRange getCatchStatementRange(CatchStatement catchStatement, BaseDocument doc, int cursorOffset) {
        return getRange(catchStatement.getVariable().getOriginType(), doc, cursorOffset);
    }

    private static OffsetRange getImportRange(ImportNode importNode, BaseDocument doc, int cursorOffset) {
        return getRange(importNode.getType(), doc, cursorOffset);
    }

    private static OffsetRange getPackageRange(PackageNode packageNode, BaseDocument doc, int cursorOffset) {
        OffsetRange range = ASTUtils.getNextIdentifierByName(doc, packageNode.getName().substring(0, packageNode.getName().length() - 1), getOffset(packageNode, doc));
        if (range.containsInclusive(cursorOffset)) {
            return range;
        }
        return OffsetRange.NONE;
    }

    private static OffsetRange getVariableRange(VariableExpression variable, BaseDocument doc, int cursorOffset) {
        if (variable == null || variable.getAccessedVariable() == null || variable.isDynamicTyped()) {
            return OffsetRange.NONE;
        }
        return getRange(variable.getAccessedVariable().getOriginType(), doc, cursorOffset);
    }

    private static OffsetRange getRange(ASTNode node, BaseDocument doc, int cursorOffset) {
        if (node.getLineNumber() < 0 || node.getColumnNumber() < 0) {
            return OffsetRange.NONE;
        }

        OffsetRange range = ASTUtils.getNextIdentifierByName(doc, ElementUtils.getTypeName(node), getOffset(node, doc));
        if (range.containsInclusive(cursorOffset)) {
            return range;
        }
        return OffsetRange.NONE;
    }

    private static int getOffset(ASTNode node, BaseDocument doc) {
        return ASTUtils.getOffset(doc, node.getLineNumber(), node.getColumnNumber());
    }
}
