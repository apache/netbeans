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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.ImportTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.YieldTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
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
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.java.editor.base.semantic.Utilities;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.netbeans.modules.java.hints.introduce.Flow;
import org.openide.ErrorManager;
import static org.netbeans.modules.java.hints.errors.Utilities.getIterableGenericType;

/**
 *
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
                return computeReturn(types, info, currentPath, unresolved);
            case YIELD:
                return computeYield(types, info, currentPath, unresolved, offset);
            case CASE:
                return computeCase(types, info, currentPath, unresolved, offset);
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
                
            case LAMBDA_EXPRESSION:
                return computeLambdaReturn(types, info, currentPath, unresolved, offset);
                
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
            types.add(ElementKind.RECORD);
            types.add(ElementKind.ENUM);
        }

        List<? extends ExpressionTree> throwList = mt.getThrows();
	if (throwList != null && !throwList.isEmpty()) {
            for (ExpressionTree t : throwList) {
                if (t == error) {
                    types.add(ElementKind.CLASS);
                    TypeElement tel = info.getElements().getTypeElement("java.lang.Exception");
                    if (tel == null) {
                        return null;
                    }
                    typeParameterBound[0] = tel.asType();
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
                int bodyStart = Utilities.findBodyStart(info, parent.getLeaf(), info.getCompilationUnit(), info.getTrees().getSourcePositions(), doc);
                int bodyEnd   = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), parent.getLeaf());

                types.add(ElementKind.PARAMETER);
                types.add(ElementKind.LOCAL_VARIABLE);
                types.add(ElementKind.FIELD);

                if (bodyStart <= offset && offset <= bodyEnd) {
                    return typeMirrorCollection(info, "java.lang.Object");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.INFO, ex.getMessage(), ex);
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeMemberSelect(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        //class or field:
        MemberSelectTree ms = (MemberSelectTree) parent.getLeaf();
        final TypeElement jlObject = info.getElements().getTypeElement("java.lang.Object");
        
        if (jlObject != null) { //may happen if the platform is broken
            if (!"class".equals(ms.getIdentifier().toString())) {
                types.add(ElementKind.FIELD);
                types.add(ElementKind.CLASS);
                return Collections.singletonList(jlObject.asType());
            } else {
                List<? extends TypeMirror> targetTypes = resolveType(new HashSet<ElementKind>(), info, parent.getParentPath(), ms, offset, null, null);
                boolean alreadyAddedObject = false;
                List<TypeMirror> resolvedTargetTypes = new ArrayList<>();
                if (targetTypes == null || targetTypes.isEmpty()) {
                    resolvedTargetTypes.add(jlObject.asType());
                } else {
                    for (TypeMirror tm : targetTypes) {
                        if (   tm != null
                            && tm.getKind() == TypeKind.DECLARED
                            && ((TypeElement) info.getTypes().asElement(tm)).getQualifiedName().contentEquals("java.lang.Class")
                            && ((DeclaredType) tm).getTypeArguments().size() == 1) {
                            resolvedTargetTypes.add(((DeclaredType) tm).getTypeArguments().get(0));
                            continue;
                        }
                        if (!alreadyAddedObject) {
                            alreadyAddedObject = true;
                            resolvedTargetTypes.add(jlObject.asType());
                        }
                    }
                }
                types.add(ElementKind.CLASS);
                return resolvedTargetTypes;
            }
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeAssignment(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        AssignmentTree at = (AssignmentTree) parent.getLeaf();
        TypeMirror     type = null;
        
        types.add(ElementKind.PARAMETER);
        types.add(ElementKind.LOCAL_VARIABLE);
        types.add(ElementKind.FIELD);

        if (at.getVariable() == error) {
            type = info.getTrees().getTypeMirror(new TreePath(parent, at.getExpression()));

            if (type != null) {
                //anonymous class?
                type = org.netbeans.modules.java.hints.errors.Utilities.convertIfAnonymous(type);

                if (type.getKind() == TypeKind.EXECUTABLE) {
                    //TODO: does not actualy work, attempt to solve situations like:
                    //t = Collections.emptyList()
                    //t = Collections.<String>emptyList();
                    //see also testCreateFieldMethod1 and testCreateFieldMethod2 tests:
                    type = ((ExecutableType) type).getReturnType();
                }
            }

            if (parent.getParentPath() != null && parent.getParentPath().getLeaf().getKind() == Kind.TRY) {
                types.clear();
                types.add(ElementKind.RESOURCE_VARIABLE);
            }
            if (parent.getParentPath() != null &&
                parent.getParentPath().getLeaf().getKind() == Kind.EXPRESSION_STATEMENT &&
                parent.getParentPath().getParentPath() != null &&
                parent.getParentPath().getParentPath().getLeaf().getKind() == Kind.FOR_LOOP &&
                ((ForLoopTree) parent.getParentPath().getParentPath().getLeaf()).getInitializer().contains(parent.getParentPath().getLeaf())) {
                types.add(ElementKind.OTHER);
            }
        }
        
        if (at.getExpression() == error) {
            type = info.getTrees().getTypeMirror(new TreePath(parent, at.getVariable()));
        }
        
        //class or field:
        if (type == null) {
            if (ErrorHintsProvider.ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ErrorHintsProvider.ERR.log(ErrorManager.INFORMATIONAL, "offset=" + offset);
                ErrorHintsProvider.ERR.log(ErrorManager.INFORMATIONAL, "errorTree=" + error);
                ErrorHintsProvider.ERR.log(ErrorManager.INFORMATIONAL, "type=null");
            }
            
            return null;
        }
        
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
            return typeMirrorCollection(info, "java.lang.Object");
        }
        
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeLambdaReturn(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        LambdaExpressionTree let = (LambdaExpressionTree)parent.getLeaf();
        if (let.getBody() != error) {
            return null;
        }

        List<TypeMirror> result = new ArrayList<>();
        for (TypeMirror target : resolveType(types, info, parent.getParentPath(), let, offset, null, null)) {
            if (!org.netbeans.modules.java.hints.errors.Utilities.isValidType(target) ||
                target.getKind() != TypeKind.DECLARED) {
                continue;
            }

            DeclaredType declaredTarget = (DeclaredType) target;
            ExecutableElement functionalMethod =
                    info.getElementUtilities()
                        .getDescriptorElement((TypeElement) (declaredTarget).asElement());

            if (functionalMethod == null) {
                continue;
            }

            result.add(((ExecutableType) info.getTypes().asMemberOf(declaredTarget, functionalMethod)).getReturnType());
        }

        return result;
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
            return typeMirrorCollectionOrNull(info, type);
        }
        
        return null;
    }

    private static List<? extends TypeMirror> typeMirrorCollectionOrNull(CompilationInfo info, String type) {
            TypeElement tel = info.getElements().getTypeElement(type);
            return tel == null ? null : Collections.singletonList(tel.asType());
    }
    
    private static List<?extends TypeMirror> typeMirrorCollection(CompilationInfo info, String type) {
            TypeElement tel = info.getElements().getTypeElement(type);
            return tel == null ? Collections.<TypeMirror>emptyList() : Collections.singletonList(tel.asType());
    }
    
    private static List<? extends TypeMirror> computeImport(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        ImportTree tree = (ImportTree) parent.getLeaf();

        if (tree.getQualifiedIdentifier() == error) {
            types.add(ElementKind.ANNOTATION_TYPE);
            types.add(ElementKind.CLASS);
            types.add(ElementKind.ENUM);
            types.add(ElementKind.INTERFACE);
            types.add(ElementKind.RECORD);
            
            return typeMirrorCollectionOrNull(info, "java.lang.Object");
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
    
    private static List<? extends TypeMirror> computeReturn(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error) {
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
    
    private static List<? extends TypeMirror> computeYield(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        YieldTree yieldStatement = (YieldTree) parent.getLeaf();

        if (yieldStatement.getValue() == error) {
            Tree yieldTargetTree = info.getTreeUtilities().getBreakContinueTargetTree(parent);
            TreePath switchExpression = parent;

            while (switchExpression != null && switchExpression.getLeaf() != yieldTargetTree) {
                switchExpression = switchExpression.getParentPath();
            }

            if (switchExpression == null) {
                return null;
            }

            types.add(ElementKind.PARAMETER);
            types.add(ElementKind.LOCAL_VARIABLE);
            types.add(ElementKind.FIELD);

            return resolveType(types, info, switchExpression.getParentPath(), switchExpression.getLeaf(), offset, null, null);
        }

        return null;
    }

    private static List<? extends TypeMirror> computeCase(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        TreePath switchCandidate = parent.getParentPath();

        if (switchCandidate.getLeaf().getKind() == Kind.SWITCH_EXPRESSION) {
            types.add(ElementKind.PARAMETER);
            types.add(ElementKind.LOCAL_VARIABLE);
            types.add(ElementKind.FIELD);

            return resolveType(types, info, switchCandidate.getParentPath(), switchCandidate.getLeaf(), offset, null, null);
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
                types.add(ElementKind.RECORD);
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
            types.add(ElementKind.RECORD);
            
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
        int realArgumentError = -1;
        int i = 0;
        for (Tree param : nat.getArguments()) {
            if (param == error) {
                realArgumentError = i;
                break;
            }
            i++;
        }
        
        if (realArgumentError != (-1)) {
            List<TypeMirror> proposedTypes = new ArrayList<TypeMirror>();
            int[] proposedIndex = new int[1];
            List<ExecutableElement> ee = org.netbeans.modules.editor.java.Utilities.fuzzyResolveMethodInvocation(info, parent, proposedTypes, proposedIndex);
            
            if (ee.isEmpty()) { //cannot be resolved
                TypeMirror executable = info.getTrees().getTypeMirror(new TreePath(parent, nat.getMethodSelect()));
                
                if (executable == null || executable.getKind() != TypeKind.EXECUTABLE) return null;
                
                ExecutableType et = (ExecutableType) executable;
                
                if (realArgumentError >= et.getParameterTypes().size()) {
                    return null;
                }
                
                proposedTypes.add(et.getParameterTypes().get(realArgumentError));
            }
            
            types.add(ElementKind.PARAMETER);
            types.add(ElementKind.LOCAL_VARIABLE);
            types.add(ElementKind.FIELD);
            
            return proposedTypes;
        }
        
        return null;
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
            List<ExecutableElement> ee = org.netbeans.modules.editor.java.Utilities.fuzzyResolveMethodInvocation(info, parent, proposedTypes, proposedIndex);
            
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

        return typeMirrorCollectionOrNull(info, "java.lang.Object");
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
    
    /**
     * Determines whether a ctor variable/parameter can be declared as a final field.
     * The class is checked whether it has other ctors that do not call this one (at least one
     * of the other ctors does not begin with this() call). If `theVar' is not null, the ctor
     * body is checked for definitive assignment to that variable. If it is null, no assignment
     * check si done.
     * 
     * @param info context
     * @param ctorTree path to the constructor
     * @param theVar optional; the variable / symbol that is being converted to a field
     * @return true, if the field could be declared final.
     */
    public static boolean canDeclareVariableFinal(CompilationInfo info, 
            TreePath ctorTree, @NullAllowed Element theVar) {
        TreePath classTree = ctorTree.getParentPath();
        boolean hasOtherConstructors = false;
        for (Tree member : ((ClassTree)classTree.getLeaf()).getMembers()) {
            if (member.getKind() == Tree.Kind.METHOD && "<init>".contentEquals(((MethodTree)member).getName()) && ctorTree.getLeaf() != member) { //NOI18N
                BlockTree body = ((MethodTree) member).getBody();
                Iterator<? extends StatementTree> stats = body != null ? body.getStatements().iterator() : Collections.<StatementTree>emptyList().iterator();
                if (stats.hasNext()) {
                    StatementTree stat = stats.next();
                    if (stat.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                        ExpressionTree exp = ((ExpressionStatementTree)stat).getExpression();
                        if (exp.getKind() == Tree.Kind.METHOD_INVOCATION) {
                            ExpressionTree meth = ((MethodInvocationTree)exp).getMethodSelect();
                            if (meth.getKind() == Tree.Kind.IDENTIFIER && "this".contentEquals(((IdentifierTree)meth).getName())) { //NOI18N
                                continue;
                            }
                        }
                    }
                }
                // TODO: the field will be declared as non-final even though it may
                // me actually initialized from all the relevant constructors. To fix that, Flow
                // analysis should be executed on the class as a whole to get all final candidates.
                hasOtherConstructors = true;
                break;
            }
        }
        if (!hasOtherConstructors) {
            if (theVar == null) {
                return true;
            }
            BlockTree constructorBody = ((MethodTree) ctorTree.getLeaf()).getBody();
            if (constructorBody == null) {
                // no other constructors present && this one does not have any body :)
                return true;
            }
            TypeElement source = (TypeElement)info.getTrees().getElement(classTree);
            if (source == null) {
                return false;
            }
            // FIXME: the check is insufficient; the symbol may be assigned in other parts of the code
            // despite it's undefined at the moment. The IDE will generate final field based on ctor analysis,
            // but then report errors on the field's assignments in regular methods.
            if (Flow.unknownSymbolFinalCandidate(info, 
                    theVar, source, Collections.singletonList(new TreePath(ctorTree, constructorBody)), new AtomicBoolean())) {
                return true;
            }
        }
        
        return false;
    }
}
