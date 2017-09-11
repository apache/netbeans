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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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
package org.netbeans.modules.refactoring.java;

import com.sun.source.tree.ImportTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.refactoring.java.plugins.JavaPluginUtils;

/**
 * Copy of org.netbeans.modules.java.hints.errors.CreateElementUtilities
 * @author Jan Lahoda
 */
public final class CreateElementUtilities {

    private CreateElementUtilities() {}

    public static List<? extends TypeMirror> resolveType(Set<ElementKind> types, CompilationInfo info, TreePath currentPath, Tree unresolved, int offset, TypeMirror[] typeParameterBound, int[] numTypeParameters) {
        switch (currentPath.getLeaf().getKind()) {
            case METHOD:
                return computeMethod(types, info, currentPath, typeParameterBound ,unresolved, offset);
            case MEMBER_SELECT:
                return computeMemberSelect(types, info, currentPath, unresolved, offset);
            case ASSIGNMENT:
                return computeAssignment(types, info, currentPath, unresolved, offset);
            case ENHANCED_FOR_LOOP:
                return computeEnhancedForLoop(types, info, currentPath, unresolved, offset);
            case ARRAY_ACCESS:
                return computeArrayAccess(types, info, currentPath, unresolved, offset);
            case VARIABLE:
                return computeVariableDeclaration(types, info, currentPath, unresolved, offset);
            case ASSERT:
                return computeAssert(types, info, currentPath, unresolved, offset);
            case PARENTHESIZED:
                return computeParenthesis(types, info, currentPath, unresolved, offset);            
            case DO_WHILE_LOOP:
                return computePrimitiveType(types, info, ((DoWhileLoopTree) currentPath.getLeaf()).getCondition(), unresolved, TypeKind.BOOLEAN);
            case FOR_LOOP:
                return computePrimitiveType(types, info, ((ForLoopTree) currentPath.getLeaf()).getCondition(), unresolved, TypeKind.BOOLEAN);
            case IF:
                return computePrimitiveType(types, info, ((IfTree) currentPath.getLeaf()).getCondition(), unresolved, TypeKind.BOOLEAN);
            case WHILE_LOOP:
                return computePrimitiveType(types, info, ((WhileLoopTree) currentPath.getLeaf()).getCondition(), unresolved, TypeKind.BOOLEAN);
            case SYNCHRONIZED:
                return computeReferenceType(types, info, ((SynchronizedTree) currentPath.getLeaf()).getExpression(), unresolved, "java.lang.Object");
            case THROW:
                return computeReferenceType(types, info, ((ThrowTree) currentPath.getLeaf()).getExpression(), unresolved, "java.lang.Exception");
            case INSTANCE_OF:
                return computeReferenceType(types, info, ((InstanceOfTree) currentPath.getLeaf()).getExpression(), unresolved, "java.lang.Object");
            case SWITCH:
                //TODO: should consider also values in the cases?:
                return computePrimitiveType(types, info, ((SwitchTree) currentPath.getLeaf()).getExpression(), unresolved, TypeKind.INT);
            case EXPRESSION_STATEMENT:
                return Collections.singletonList(info.getTypes().getNoType(TypeKind.VOID));

            case RETURN:
                return computeReturn(types, info, currentPath, unresolved, offset);
            case TYPE_PARAMETER:
                return computeTypeParameter(types, info, currentPath, unresolved, offset);
            case PARAMETERIZED_TYPE:
                return computeParametrizedType(types, info, currentPath, unresolved, offset, typeParameterBound, numTypeParameters);
            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
                return computeClass(types, info, currentPath, unresolved, offset);
                
            case CONDITIONAL_EXPRESSION:
                return computeConditionalExpression(types, info, currentPath, unresolved, offset);
                
            case NEW_ARRAY:
                return computeNewArray(types, info, currentPath, unresolved, offset);
                
            case METHOD_INVOCATION:
                return computeMethodInvocation(types, info, currentPath, unresolved, offset);
                
            case NEW_CLASS:
                return computeNewClass(types, info, currentPath, unresolved, offset);
                
            case POSTFIX_INCREMENT:
            case POSTFIX_DECREMENT:
            case PREFIX_INCREMENT:
            case PREFIX_DECREMENT:
            case UNARY_PLUS:
            case UNARY_MINUS:
            case BITWISE_COMPLEMENT:
            case LOGICAL_COMPLEMENT:
                return computeUnary(types, info, currentPath, unresolved, offset);

            case MULTIPLY:
            case DIVIDE:
            case REMAINDER:
            case PLUS:
            case MINUS:
            case LEFT_SHIFT:
            case RIGHT_SHIFT:
            case UNSIGNED_RIGHT_SHIFT:
            case LESS_THAN:
            case GREATER_THAN:
            case LESS_THAN_EQUAL:
            case GREATER_THAN_EQUAL:
            case EQUAL_TO:
            case NOT_EQUAL_TO:
            case AND:
            case XOR:
            case OR:
            case CONDITIONAL_AND:
            case CONDITIONAL_OR:
                return computeBinaryOperator(types, info, currentPath, unresolved, offset);
                
            case MULTIPLY_ASSIGNMENT:
            case DIVIDE_ASSIGNMENT:
            case REMAINDER_ASSIGNMENT:
            case PLUS_ASSIGNMENT:
            case MINUS_ASSIGNMENT:
            case LEFT_SHIFT_ASSIGNMENT:
            case RIGHT_SHIFT_ASSIGNMENT:
            case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
            case AND_ASSIGNMENT:
            case XOR_ASSIGNMENT:
            case OR_ASSIGNMENT:
                //XXX: return computeCompoundAssignment(types, info, currentPath, unresolved, offset);
                return null;
                        
            case ARRAY_TYPE:
                return computeArrayType(types, info, currentPath, unresolved, offset);

            case IMPORT:
                return computeImport(types, info, currentPath, unresolved, offset);
                
            case BLOCK:
            case BREAK:
            case CATCH:
            case COMPILATION_UNIT:
            case CONTINUE:
            case IDENTIFIER:
            case TYPE_CAST:
            case TRY:
            case EMPTY_STATEMENT:
            case PRIMITIVE_TYPE:
            case LABELED_STATEMENT:
            case MODIFIERS:
            case ERRONEOUS:
            case OTHER:
            case INT_LITERAL:
            case LONG_LITERAL:
            case FLOAT_LITERAL:
            case DOUBLE_LITERAL:
            case BOOLEAN_LITERAL:
            case CHAR_LITERAL:
            case STRING_LITERAL:
            case NULL_LITERAL:
                //ignored:
                return null;
                
            case CASE:
            case ANNOTATION:
            case UNBOUNDED_WILDCARD:
            case EXTENDS_WILDCARD:
            case SUPER_WILDCARD:
                //XXX: currently unhandled
                return null;
                
            default:
                //should not happen unless set of Tree.Kind changes:
                return null;
        }
    }
    
    private static List<? extends TypeMirror> computeBinaryOperator(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        BinaryTree bt = (BinaryTree) parent.getLeaf();
        TreePath typeToResolve = null;
        
        if (bt.getLeftOperand() == error) {
            typeToResolve = new TreePath(parent, bt.getRightOperand());
        }
        
        if (bt.getRightOperand() == error) {
            typeToResolve = new TreePath(parent, bt.getLeftOperand());
        }
        
        if (typeToResolve != null) {
            TypeMirror resolvedType = info.getTrees().getTypeMirror(typeToResolve);
            
            if (resolvedType != null) {
                types.add(ElementKind.PARAMETER);
                types.add(ElementKind.LOCAL_VARIABLE);
                types.add(ElementKind.FIELD);
                
                if (resolvedType.getKind() == TypeKind.ERROR || resolvedType.getKind() == TypeKind.OTHER) {
                    return resolveType(types, info, parent.getParentPath(), bt, offset, null, null);
                }
                
                return Collections.singletonList(resolvedType);
            }
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeMethod(Set<ElementKind> types, CompilationInfo info, TreePath parent, TypeMirror[] typeParameterBound, Tree error, int offset) {
        //class or field:
        //check the error is in the body:
        //#92419: check for abstract method/method without body:
        MethodTree mt = (MethodTree) parent.getLeaf();
        
        if (mt.getReturnType() == error) {
            types.add(ElementKind.CLASS);
            types.add(ElementKind.INTERFACE);
            types.add(ElementKind.ENUM);
        }

        List<? extends ExpressionTree> throwList = mt.getThrows();
	if (throwList != null && !throwList.isEmpty()) {
            for (ExpressionTree t : throwList) {
                if (t == error) {
                    types.add(ElementKind.CLASS);
                    typeParameterBound[0] = info.getElements().getTypeElement("java.lang.Exception").asType();
                    break;
                }
            }
	}
        
        if (mt.getBody() == null) {
            return null;
        }
        
        try {
            Document doc = info.getDocument();
            
            if (doc != null) {//XXX
                int bodyStart = findBodyStart(parent.getLeaf(), info.getCompilationUnit(), info.getTrees().getSourcePositions(), doc);
                int bodyEnd   = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), parent.getLeaf());

                types.add(ElementKind.PARAMETER);
                types.add(ElementKind.LOCAL_VARIABLE);
                types.add(ElementKind.FIELD);

                if (bodyStart <= offset && offset <= bodyEnd)
                    return Collections.singletonList(info.getElements().getTypeElement("java.lang.Object").asType());
            }
        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.INFO, ex.getMessage(), ex);
        }
        
        return null;
    }
    
    private static int findBodyStartImpl(Tree cltree, CompilationUnitTree cu, SourcePositions positions, Document doc) {
        int start = (int)positions.getStartPosition(cu, cltree);
        int end   = (int)positions.getEndPosition(cu, cltree);
        
        if (start == (-1) || end == (-1)) {
            return -1;
        }
        
        if (start > doc.getLength() || end > doc.getLength()) {
            return (-1);
        }
        
        try {
            String text = doc.getText(start, end - start);
            
            int index = text.indexOf('{');
            
            if (index == (-1)) {
                return -1;
//                throw new IllegalStateException("Should NEVER happen.");
            }
            
            return start + index;
        } catch (BadLocationException e) {
            LOG.log(Level.INFO, null, e);
        }
        
        return (-1);
    }
    private static final Logger LOG = Logger.getLogger(CreateElementUtilities.class.getName());
    
    public static int findBodyStart(final Tree cltree, final CompilationUnitTree cu, final SourcePositions positions, final Document doc) {
        Kind kind = cltree.getKind();
        if (!TreeUtilities.CLASS_TREE_KINDS.contains(kind) && kind != Kind.METHOD)
            throw new IllegalArgumentException("Unsupported kind: "+ kind);
        final int[] result = new int[1];
        
        doc.render(new Runnable() {
            public void run() {
                result[0] = findBodyStartImpl(cltree, cu, positions, doc);
            }
        });
        
        return result[0];
    }
    
    private static List<? extends TypeMirror> computeMemberSelect(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        //class or field:
        MemberSelectTree ms = (MemberSelectTree) parent.getLeaf();
        final TypeElement jlObject = info.getElements().getTypeElement("java.lang.Object");
        
        if (   jlObject != null //may happen if the platform is broken
            && !"class".equals(ms.getIdentifier().toString())) {//we obviously should not propose "Create Field" for unknown.class:
            types.add(ElementKind.FIELD);
            types.add(ElementKind.CLASS);
            return Collections.singletonList(jlObject.asType());
        }
        
        return null;
    }
    
    /**
     *
     * @param info context {@link CompilationInfo}
     * @param iterable tested {@link TreePath}
     * @return generic type of an {@link Iterable} or {@link ArrayType} at a TreePath
     */
    public static TypeMirror getIterableGenericType(CompilationInfo info, TreePath iterable) {
        TypeElement iterableElement = info.getElements().getTypeElement("java.lang.Iterable"); //NOI18N
        if (iterableElement == null) {
            return null;
        }
        TypeMirror iterableType = info.getTrees().getTypeMirror(iterable);
        if (iterableType == null) {
            return null;
        }
        TypeMirror designedType = null;
        if (iterableType.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) iterableType;
            if (!info.getTypes().isSubtype(info.getTypes().erasure(declaredType), info.getTypes().erasure(iterableElement.asType()))) {
                return null;
            }
            ExecutableElement iteratorMethod = (ExecutableElement) iterableElement.getEnclosedElements().get(0);
            ExecutableType iteratorMethodType = (ExecutableType) info.getTypes().asMemberOf(declaredType, iteratorMethod);
            List<? extends TypeMirror> typeArguments = ((DeclaredType) iteratorMethodType.getReturnType()).getTypeArguments();
            if (!typeArguments.isEmpty()) {
                designedType = typeArguments.get(0);
            } else {
                TypeElement jlObject = info.getElements().getTypeElement("java.lang.Object");

                if (jlObject != null) {
                    designedType = jlObject.asType();
                }
            }
        } else if (iterableType.getKind() == TypeKind.ARRAY) {
            designedType = ((ArrayType) iterableType).getComponentType();
        }
        if (designedType == null) {
            return null;
        }
        return JavaPluginUtils.resolveCapturedType(info, designedType);
    }
    
    private static List<? extends TypeMirror> computeAssignment(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        AssignmentTree at = (AssignmentTree) parent.getLeaf();
        TypeMirror     type = null;
        
        if (at.getVariable() == error) {
            type = info.getTrees().getTypeMirror(new TreePath(parent, at.getExpression()));

            if (type != null) {
                //anonymous class?
                type = JavaPluginUtils.convertIfAnonymous(type);

                if (type.getKind() == TypeKind.EXECUTABLE) {
                    //TODO: does not actualy work, attempt to solve situations like:
                    //t = Collections.emptyList()
                    //t = Collections.<String>emptyList();
                    //see also testCreateFieldMethod1 and testCreateFieldMethod2 tests:
                    type = ((ExecutableType) type).getReturnType();
                }
            }
        }
        
        if (at.getExpression() == error) {
            type = info.getTrees().getTypeMirror(new TreePath(parent, at.getVariable()));
        }
        
        //class or field:
        if (type == null) {
            return null;
        }
        
        types.add(ElementKind.PARAMETER);
        types.add(ElementKind.LOCAL_VARIABLE);
        types.add(ElementKind.FIELD);
        
        return Collections.singletonList(type);
    }
    
    private static List<? extends TypeMirror> computeEnhancedForLoop(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        EnhancedForLoopTree efl = (EnhancedForLoopTree) parent.getLeaf();
        
        if (efl.getExpression() != error) {
            return null;
        }
                        
        TypeMirror argument = info.getTrees().getTypeMirror(new TreePath(new TreePath(parent, efl.getVariable()), efl.getVariable().getType()));
        
        if (argument == null)
            return null;
        
        if (argument.getKind().isPrimitive()) {
            types.add(ElementKind.PARAMETER);
            types.add(ElementKind.LOCAL_VARIABLE);
            types.add(ElementKind.FIELD);

            return Collections.singletonList(info.getTypes().getArrayType(argument));
        }
        
        TypeElement iterable = info.getElements().getTypeElement("java.lang.Iterable"); //NOI18N
        if (iterable == null) {
            return null;
        }
        
        types.add(ElementKind.PARAMETER);
        types.add(ElementKind.LOCAL_VARIABLE);
        types.add(ElementKind.FIELD);
        
        return Collections.singletonList(info.getTypes().getDeclaredType(iterable, argument));
    }
    
    private static List<? extends TypeMirror> computeArrayAccess(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        ArrayAccessTree aat = (ArrayAccessTree) parent.getLeaf();
        
        if (aat.getExpression() == error) {
            TreePath parentParent = parent.getParentPath();
            List<? extends TypeMirror> upperTypes = resolveType(types, info, parentParent, aat, offset, null, null);
            
            if (upperTypes == null) {
                return null;
            }
            
            List<TypeMirror> arrayTypes = new ArrayList<TypeMirror>();
            
            for (TypeMirror tm : upperTypes) {
                if (tm == null)
                    continue;
                switch (tm.getKind()) {
                    case VOID:
                    case EXECUTABLE:
                    case WILDCARD:
                    case PACKAGE:
                        continue;
                }
                
                arrayTypes.add(info.getTypes().getArrayType(tm));
            }
            
            if (arrayTypes.isEmpty())
                return null;
            
            return arrayTypes;
        }
        
        if (aat.getIndex() == error) {
            types.add(ElementKind.PARAMETER);
            types.add(ElementKind.LOCAL_VARIABLE);
            types.add(ElementKind.FIELD);
            
            return Collections.singletonList(info.getTypes().getPrimitiveType(TypeKind.INT));
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeVariableDeclaration(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        VariableTree vt = (VariableTree) parent.getLeaf();
        
        if (vt.getInitializer() == error) {
            types.add(ElementKind.PARAMETER);
            types.add(ElementKind.LOCAL_VARIABLE);
            types.add(ElementKind.FIELD);
            
            return Collections.singletonList(info.getTrees().getTypeMirror(new TreePath(parent, vt.getType())));
        }
        
        TreePath context = parent.getParentPath();
        if (vt.getType() != error || context == null) {
            return null;
        }

        switch (context.getLeaf().getKind()) {
            case ENHANCED_FOR_LOOP:
                ExpressionTree iterableTree = ((EnhancedForLoopTree) context.getLeaf()).getExpression();
                TreePath iterablePath = new TreePath(context, iterableTree);
                TypeMirror type = getIterableGenericType(info, iterablePath);
                types.add(ElementKind.LOCAL_VARIABLE);
                return Collections.singletonList(type);
            default:
                types.add(ElementKind.CLASS);
                return Collections.<TypeMirror>emptyList();
        }
    }
    
    private static List<? extends TypeMirror> computeAssert(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        AssertTree at = (AssertTree) parent.getLeaf();
        
        types.add(ElementKind.PARAMETER);
        types.add(ElementKind.LOCAL_VARIABLE);
        types.add(ElementKind.FIELD);
        
        if (at.getCondition() == error) {
            return Collections.singletonList(info.getTypes().getPrimitiveType(TypeKind.BOOLEAN));
        }
        
        if (at.getDetail() == error) {
            return Collections.singletonList(info.getElements().getTypeElement("java.lang.Object").asType());
        }
        
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeParenthesis(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        ParenthesizedTree pt = (ParenthesizedTree) parent.getLeaf();
        
        if (pt.getExpression() != error) {
            return null;
        }
        
        TreePath parentParent = parent.getParentPath();
        List<? extends TypeMirror> upperTypes = resolveType(types, info, parentParent, pt, offset, null, null);
        
        if (upperTypes == null) {
            return null;
        }
        
        return upperTypes;
    }
    
    private static List<? extends TypeMirror> computeConditionalExpression(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        ConditionalExpressionTree cet = (ConditionalExpressionTree) parent.getLeaf();
        
        if (cet.getCondition() == error) {
            types.add(ElementKind.PARAMETER);
            types.add(ElementKind.LOCAL_VARIABLE);
            types.add(ElementKind.FIELD);
            
            return Collections.singletonList(info.getTypes().getPrimitiveType(TypeKind.BOOLEAN));
        }
        
        if (cet.getTrueExpression() == error || cet.getFalseExpression() == error) {
            types.add(ElementKind.PARAMETER);
            types.add(ElementKind.LOCAL_VARIABLE);
            types.add(ElementKind.FIELD);
            
            return resolveType(types, info, parent.getParentPath(), cet, offset, null, null);
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computePrimitiveType(Set<ElementKind> types, CompilationInfo info, Tree expression, Tree error, TypeKind kind) {
        if (expression == error) {
            types.add(ElementKind.PARAMETER);
            types.add(ElementKind.LOCAL_VARIABLE);
            types.add(ElementKind.FIELD);
            
            return Collections.singletonList(info.getTypes().getPrimitiveType(kind));
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeReferenceType(Set<ElementKind> types, CompilationInfo info, Tree expression, Tree error, String type) {
        if (expression == error) {
            types.add(ElementKind.PARAMETER);
            types.add(ElementKind.LOCAL_VARIABLE);
            types.add(ElementKind.FIELD);
            
            return Collections.singletonList(info.getElements().getTypeElement(type).asType());
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeImport(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        ImportTree tree = (ImportTree) parent.getLeaf();

        if (tree.getQualifiedIdentifier() == error) {
            types.add(ElementKind.ANNOTATION_TYPE);
            types.add(ElementKind.CLASS);
            types.add(ElementKind.ENUM);
            types.add(ElementKind.INTERFACE);
            
            return Collections.singletonList(info.getElements().getTypeElement("java.lang.Object").asType());
        }

        return null;
    }

    private static List<? extends TypeMirror> computeUnary(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        UnaryTree tree = (UnaryTree) parent.getLeaf();
        
        if (tree.getExpression() == error) {
            List<? extends TypeMirror> parentTypes = resolveType(types, info, parent.getParentPath(), tree, offset, null, null);
            
            if (parentTypes != null) {
                //may contain only "void", ignore:
                if (parentTypes.size() != 1) {
                    return parentTypes;
                }
                if (parentTypes.get(0).getKind() != TypeKind.VOID) {
                    return parentTypes;
                }
            }
            
            types.add(ElementKind.PARAMETER);
            types.add(ElementKind.LOCAL_VARIABLE);
            types.add(ElementKind.FIELD);

            return Collections.singletonList(info.getTypes().getPrimitiveType(TypeKind.INT));
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeReturn(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        ReturnTree rt = (ReturnTree) parent.getLeaf();
        
        if (rt.getExpression() == error) {
            TreePath method = findMethod(parent);
            
            if (method == null) {
                return null;
            }
            
            Element el = info.getTrees().getElement(method);
            
            if (el == null || el.getKind() != ElementKind.METHOD) {
                return null;
            }
            
            types.add(ElementKind.PARAMETER);
            types.add(ElementKind.LOCAL_VARIABLE);
            types.add(ElementKind.FIELD);
            
            return Collections.singletonList(((ExecutableElement) el).getReturnType());
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeTypeParameter(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        TypeParameterTree tpt = (TypeParameterTree) parent.getLeaf();
        
        for (Tree t : tpt.getBounds()) {
            if (t == error) {
                types.add(ElementKind.CLASS); //XXX: class/interface/enum/annotation?
                return null;
            }
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeParametrizedType(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset, TypeMirror[] typeParameterBound, int[] numTypeParameters) {
        ParameterizedTypeTree ptt = (ParameterizedTypeTree) parent.getLeaf();
        
        if (ptt.getType() == error) {
            Tree gpt = parent.getParentPath().getLeaf();
            if (TreeUtilities.CLASS_TREE_KINDS.contains(gpt.getKind()) && ((ClassTree)gpt).getExtendsClause() == ptt) {
                types.add(ElementKind.CLASS);
            } else if (TreeUtilities.CLASS_TREE_KINDS.contains(gpt.getKind()) && ((ClassTree)gpt).getImplementsClause().contains(ptt)) {
                types.add(ElementKind.INTERFACE);
            } else {
                types.add(ElementKind.CLASS);
                types.add(ElementKind.INTERFACE);
            }
            
            if (numTypeParameters != null) {
                numTypeParameters[0] = ptt.getTypeArguments().size();
            }
            return null;
        }
        
        TypeMirror resolved = info.getTrees().getTypeMirror(parent);
        DeclaredType resolvedDT = null;
        
        if (resolved != null && resolved.getKind() == TypeKind.DECLARED) {
            resolvedDT = (DeclaredType) resolved;
        }
        
        int index = 0;
        
        for (Tree t : ptt.getTypeArguments()) {
            if (t == error) {
                if (resolvedDT != null && typeParameterBound != null) {
                    List<? extends TypeMirror> typeArguments = ((DeclaredType) resolvedDT.asElement().asType()).getTypeArguments();
                    
                    if (typeArguments.size() > index) {
                        typeParameterBound[0] = ((TypeVariable) typeArguments.get(index)).getUpperBound();
                    }
                }
                
                types.add(ElementKind.CLASS); //XXX: class/interface/enum/annotation?
                return null;
            }
            
            index++;
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeClass(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        ClassTree ct = (ClassTree) parent.getLeaf();
        
        if (ct.getExtendsClause() == error) {
            types.add(ElementKind.CLASS);
            return null;
        }
        
        for (Tree t : ct.getImplementsClause()) {
            if (t == error) {
                types.add(ElementKind.INTERFACE);
                return null;
            }
        }
        
        //XXX: annotation types...
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeNewArray(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        NewArrayTree nat = (NewArrayTree) parent.getLeaf();
        
        if (nat.getType() == error) {
            types.add(ElementKind.CLASS);
            types.add(ElementKind.ENUM);
            types.add(ElementKind.INTERFACE);
            
            return null;
        }
        
        for (Tree dimension : nat.getDimensions()) {
            if (dimension == error) {
                types.add(ElementKind.PARAMETER);
                types.add(ElementKind.LOCAL_VARIABLE);
                types.add(ElementKind.FIELD);
                
                return Collections.singletonList(info.getTypes().getPrimitiveType(TypeKind.INT));
            }
        }
        
        for (Tree init : nat.getInitializers()) {
            if (init == error) {
                TypeMirror whole = info.getTrees().getTypeMirror(parent);
                
                if (whole == null || whole.getKind() != TypeKind.ARRAY)
                    return null;
                
                types.add(ElementKind.PARAMETER);
                types.add(ElementKind.LOCAL_VARIABLE);
                types.add(ElementKind.FIELD);
                
                return Collections.singletonList(((ArrayType) whole).getComponentType());
            }
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeMethodInvocation(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        MethodInvocationTree nat = (MethodInvocationTree) parent.getLeaf();
        boolean errorInRealArguments = false;
        
        for (Tree param : nat.getArguments()) {
            errorInRealArguments |= param == error;
        }
        
        if (errorInRealArguments) {
            List<TypeMirror> proposedTypes = new ArrayList<TypeMirror>();
            int[] proposedIndex = new int[1];
            List<ExecutableElement> ee = fuzzyResolveMethodInvocation(info, parent, proposedTypes, proposedIndex);
            
            if (ee.isEmpty()) { //cannot be resolved
                return null;
            }
            
            types.add(ElementKind.PARAMETER);
            types.add(ElementKind.LOCAL_VARIABLE);
            types.add(ElementKind.FIELD);
            
            return proposedTypes;
        }
        
        return null;
    }
    
    /**
     * @since 2.12
     */
    public static @NonNull List<ExecutableElement> fuzzyResolveMethodInvocation(CompilationInfo info, TreePath path, List<TypeMirror> proposed, int[] index) {
        assert path.getLeaf().getKind() == Kind.METHOD_INVOCATION || path.getLeaf().getKind() == Kind.NEW_CLASS;
        
        if (path.getLeaf().getKind() == Kind.METHOD_INVOCATION) {
            List<TypeMirror> actualTypes = new LinkedList<TypeMirror>();
            MethodInvocationTree mit = (MethodInvocationTree) path.getLeaf();

            for (Tree a : mit.getArguments()) {
                TreePath tp = new TreePath(path, a);
                actualTypes.add(info.getTrees().getTypeMirror(tp));
            }

            String methodName;
            TypeMirror on;

            switch (mit.getMethodSelect().getKind()) {
                case IDENTIFIER:
                    Scope s = info.getTrees().getScope(path);
                    TypeElement enclosingClass = s.getEnclosingClass();
                    on = enclosingClass != null ? enclosingClass.asType() : null;
                    methodName = ((IdentifierTree) mit.getMethodSelect()).getName().toString();
                    break;
                case MEMBER_SELECT:
                    on = info.getTrees().getTypeMirror(new TreePath(path, ((MemberSelectTree) mit.getMethodSelect()).getExpression()));
                    methodName = ((MemberSelectTree) mit.getMethodSelect()).getIdentifier().toString();
                    break;
                default:
                    throw new IllegalStateException();
            }

            if (on == null || on.getKind() != TypeKind.DECLARED) {
                return Collections.emptyList();
            }
            
            return resolveMethod(info, actualTypes, (DeclaredType) on, false, false, methodName, proposed, index);
        }
        
        if (path.getLeaf().getKind() == Kind.NEW_CLASS) {
            List<TypeMirror> actualTypes = new LinkedList<TypeMirror>();
            NewClassTree nct = (NewClassTree) path.getLeaf();

            for (Tree a : nct.getArguments()) {
                TreePath tp = new TreePath(path, a);
                actualTypes.add(info.getTrees().getTypeMirror(tp));
            }

            TypeMirror on = info.getTrees().getTypeMirror(new TreePath(path, nct.getIdentifier()));
            
            if (on == null || on.getKind() != TypeKind.DECLARED) {
                return Collections.emptyList();
            }
            
            return resolveMethod(info, actualTypes, (DeclaredType) on, false, true, null, proposed, index);
        }
        
        return Collections.emptyList();
    }
    
    private static List<ExecutableElement> resolveMethod(CompilationInfo info, List<TypeMirror> foundTypes, DeclaredType on, boolean statik, boolean constr, String name, List<TypeMirror> candidateTypes, int[] index) {
        if (on.asElement() == null) return Collections.emptyList();
        
        List<ExecutableElement> found = new LinkedList<ExecutableElement>();
        
        OUTER:
        for (ExecutableElement ee : execsIn(info, (TypeElement) on.asElement(), constr, name)) {
            TypeMirror currType = ((TypeElement) ee.getEnclosingElement()).asType();
            if (!info.getTypes().isSubtype(on, currType) && !on.asElement().equals(((DeclaredType)currType).asElement())) //XXX: fix for #132627, a clearer fix may exist
                continue;
            if (ee.getParameters().size() == foundTypes.size() /*XXX: variable arg count*/) {
                TypeMirror innerCandidate = null;
                int innerIndex = -1;
                ExecutableType et = (ExecutableType) info.getTypes().asMemberOf(on, ee);
                Iterator<? extends TypeMirror> formal = et.getParameterTypes().iterator();
                Iterator<? extends TypeMirror> actual = foundTypes.iterator();
                boolean mismatchFound = false;
                int i = 0;

                while (formal.hasNext() && actual.hasNext()) {
                    TypeMirror currentFormal = formal.next();
                    TypeMirror currentActual = actual.next();

                    if (!info.getTypes().isAssignable(currentActual, currentFormal) || currentActual.getKind() == TypeKind.ERROR) {
                        if (mismatchFound) {
                            //only one mismatch supported:
                            continue OUTER;
                        }
                        mismatchFound = true;
                        innerCandidate = currentFormal;
                        innerIndex = i;
                    }

                    i++;
                }

                if (mismatchFound) {
                    if (candidateTypes.isEmpty()) {
                        index[0] = innerIndex;
                        candidateTypes.add(innerCandidate);
                        found.add(ee);
                    } else {
                        //see testFuzzyResolveConstructor2:
                        if (index[0] == innerIndex) {
                            boolean add = true;
                            for (TypeMirror tm : candidateTypes) {
                                if (info.getTypes().isSameType(tm, innerCandidate)) {
                                    add = false;
                                    break;
                                }
                            }
                            if (add) {
                                candidateTypes.add(innerCandidate);
                                found.add(ee);
                            }
                        }
                    }
                }
            }
        }

        return found;
    }
    
    private static Iterable<ExecutableElement> execsIn(CompilationInfo info, TypeElement e, boolean constr, String name) {
        if (constr) {
            return ElementFilter.constructorsIn(info.getElements().getAllMembers(e));
        }
        
        List<ExecutableElement> result = new LinkedList<ExecutableElement>();
        
        for (ExecutableElement ee : ElementFilter.methodsIn(info.getElements().getAllMembers(e))) {
            if (name.equals(ee.getSimpleName().toString())) {
                result.add(ee);
            }
        }
        
        return result;
    }
    
    private static List<? extends TypeMirror> computeNewClass(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        NewClassTree nct = (NewClassTree) parent.getLeaf();
        boolean errorInRealArguments = false;
        
        for (Tree param : nct.getArguments()) {
            errorInRealArguments |= param == error;
        }
        
        if (errorInRealArguments) {
            List<TypeMirror> proposedTypes = new ArrayList<TypeMirror>();
            int[] proposedIndex = new int[1];
            List<ExecutableElement> ee = fuzzyResolveMethodInvocation(info, parent, proposedTypes, proposedIndex);
            
            if (ee.isEmpty()) { //cannot be resolved
                return null;
            }
            
            types.add(ElementKind.PARAMETER);
            types.add(ElementKind.LOCAL_VARIABLE);
            types.add(ElementKind.FIELD);
            
            return proposedTypes;
        }

        Tree id = nct.getIdentifier();

        if (id.getKind() == Kind.PARAMETERIZED_TYPE) {
            id = ((ParameterizedTypeTree) id).getType();
        }

        if (id == error) {
            return resolveType(EnumSet.noneOf(ElementKind.class), info, parent.getParentPath(), nct, offset, null, null);
        }
        
        return null;
    }

    private static List<? extends TypeMirror> computeArrayType(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        types.add(ElementKind.CLASS);

        return Collections.singletonList(info.getElements().getTypeElement("java.lang.Object").asType());
    }
    
    private static final Set<Kind> STOP_LOOKING_FOR_METHOD = EnumSet.of(Kind.METHOD, Kind.ANNOTATION_TYPE, Kind.CLASS, Kind.ENUM, Kind.INTERFACE, Kind.COMPILATION_UNIT);
            
    private static TreePath findMethod(TreePath tp) {
        while (!STOP_LOOKING_FOR_METHOD.contains(tp.getLeaf().getKind())) {
            tp = tp.getParentPath();
        }
        
        if (tp.getLeaf().getKind() == Kind.METHOD) {
            return tp;
        }
        
        return null;
    }
    
}
