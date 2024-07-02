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
package org.netbeans.modules.java.hints.suggestions;

import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AnyPatternTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BindingPatternTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ConstantCaseLabelTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DeconstructionPatternTree;
import com.sun.source.tree.DefaultCaseLabelTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EmptyStatementTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.ExportsTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.IntersectionTypeTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.OpensTree;
import com.sun.source.tree.PackageTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PatternCaseLabelTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ProvidesTree;
import com.sun.source.tree.RequiresTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.SwitchExpressionTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.UsesTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.tree.YieldTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Type.CapturedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.errors.Utilities;

/**
 * Determines the expected type of the expression.
 * The expression node may be given in the constructor, or may be guessed as the 1st "sane" node in the traversal. When
 * guessing, the analysis may fail (no type is computed) because of amiguity, c.f. for(;;). The resolver only works
 * with expressions; it does not process statements.
 * <p/>
 * The possible outcomes are: List of candidate types returned from the scan() operation, or {@link #getCastableTo()}.
 * In the case a typecast is encountered earlier than a construct that provides requirements on the expression type,
 * it is not possible to determine a type which the expression must be <i>assignable to</i>. But it is known that the
 * expression must be <i>castable</i> to the typecast's target type so that compiler error does not occur.
 * <p/>
 * As the class iterates "upwards", it was derived from TreeVisitor rather than from TreeScanner to prevent accidental 
 * StackOverflows if execution accidentally fall to the default implementation with top-down traversal.
 * 
 * @author sdedic
 */
public class ExpectedTypeResolver implements TreeVisitor<List<? extends TypeMirror>, Object> {
    /**
     * The expression, whose type should be guessed. Possibly null if the expression is not known.
     * This field may change on parenthesis and type casts, which are ignored. Used for tracking 
     * which child node the traversal came to the parent.
     */
    private TreePath theExpression;
    
    /**
     * The original Node whose type should be guessed. The Nodes in between 'theExpression' and
     * 'originalExpression' are ballast.
     */
    private TreePath originalExpression;
    
    /**
     * The expected type's tree. Actually only filled when the tree is ultimately assigned to
     * a variable.
     */
    private TreePath expectedTree;
    private CompilationInfo info;
    
    /**
     * Path to the casted expression. When traversing through typecasts, unnecessary casts may obscure the
     * expression type for analysis
     */
    private TreePath casted;
    
    private boolean dontResetCast;
    
    /**
     * A counter, which is incremented by every Tree node but a typecast and parenthesis.
     * Directly nested typecasts and parenthesis do not count and are ignored. Once a different expression appears,
     * a typecast causes the evaluation to stop with 'castableTo' result.
     */
    private int typeCastDepth;
    /**
     * An alternative result, the type may be anything, but castable to this
     * type. This result is produced when the expression is type-casted to some specific type.
     */
    private TypeMirror castableTo;
    
    /**
     * The current path
     */
    private TreePath path;
    
    /**
     * Supplemental output: if the expression is a method or ctor parameter, this
     * field will receive the TreePath to the MethodInvocationTree. Otherwise, null.
     */
    private TreePath parentExecutable;
    
    /**
     * Supplemental ouptut: if the expression is a method or ctor parameter, this
     * field will get the parameter position. Otherwise -1.
     * If the expression is passed to a variable-length argument, the index will be set to argsize - 1, if the expression
     * MIGHT be interpreted as the entire vararg value (passed at the position of vararg), or will be set
     * to argsize (beyond the formal parameter list), if the expression is passed further in the variable-length list,
     * and must conform to vararg list item type
     */
    private int argIndex = -1;
    
    /** 
     * Type of the target method/ctor argument; specifically for varargs the value can be Type[] for the variable-length
     * argument position and Type for following positions
     */
    private TypeMirror targetArgType;
    
    /**
     * Set to true, if the expression goes through some operator, where the type is intentionally
     * widened to produce a different result.
     */
    private boolean notRedundant;
    
    public ExpectedTypeResolver(TreePath theExpression, CompilationInfo info) {
        this.originalExpression = theExpression;
        this.info = info;
    }
    
    public ExpectedTypeResolver(TreePath theExpression, TreePath prevExpression, CompilationInfo info) {
        this.originalExpression = theExpression;
        this.theExpression = prevExpression;
        this.info = info;
    }
    
    protected TreePath getCurrentPath() {
        return path;
    }
    
    public boolean isNotRedundant() {
        return notRedundant;
    }

    /**
     * Returns the type the expression is cast to. If the search terminates at cast expression,
     * it's not possible to derive type of the expression, just restriction that the type
     * must be castable to the desired type
     * 
     * @return type the expression is casted to
     */
    public TypeMirror getCastableTo() {
        return castableTo;
    }

    /**
     * The expression including parenthesis and redundant typecasts.
     * The input expression can be surrounded by (multiple) parenthesis or type casts, which can
     * be all eventually eliminated. This method provides root of the expression
     * @return the expression tree.
     */
    public TreePath getTheExpression() {
        return theExpression;
    }

    /**
     * The original input expression tree
     * @return path to the original expression 
     */
    public TreePath getOriginalExpression() {
        return originalExpression;
    }

    public List<? extends TypeMirror> scan(TreePath path, Object p) {
        typeCastDepth++;
        if (!dontResetCast) {
            casted = null;
        }
        this.path = path;
        try {
            return path.getLeaf().accept(this, p);
        } finally {
            this.path = null;
        }
    }

    public List<? extends TypeMirror> scan(Tree tree, Object p) {
        typeCastDepth++;
        if (!dontResetCast) {
            casted = null;
        }
        if (tree == null)
            return null;

        TreePath prev = path;
        path = new TreePath(path, tree);
        try {
            return tree.accept(this, p);
        } finally {
            path = prev;
        }
    }
    
    private void initExpression(Tree node) {
        initExpression(new TreePath(getCurrentPath(), node));
    }

    private void initExpression(TreePath path) {
        if (theExpression != null) {
            return;
        }
        this.originalExpression = this.theExpression = path;
    }

    @Override
    public List<? extends TypeMirror> visitVariable(VariableTree node, Object p) {
        if (theExpression == null) {
            if (node.getInitializer() == null) {
                return null;
            }
            initExpression(node.getInitializer());
        }
        if (theExpression.getLeaf() == node.getInitializer()) {
            // the expression must be assiganble to the variable.
            this.expectedTree = new TreePath(getCurrentPath(), node.getType());
            return Collections.singletonList(info.getTrees().getTypeMirror(getCurrentPath()));
        }
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitDoWhileLoop(DoWhileLoopTree node, Object p) {
        if (theExpression == null) {
            initExpression(node.getCondition());
        } else if (theExpression.getLeaf() != node.getCondition()) {
            return null;
        }
        return booleanType();
    }

    @Override
    public List<? extends TypeMirror> visitWhileLoop(WhileLoopTree node, Object p) {
        if (theExpression == null) {
            initExpression(node.getCondition());
        }
        return booleanType();
    }

    @Override
    public List<? extends TypeMirror> visitForLoop(ForLoopTree node, Object p) {
        if (theExpression == null) {
            // ambigous
            return null;
        }
        if (theExpression.getLeaf() == node.getCondition()) {
            return booleanType();
        } else {
            if (!((node.getInitializer() != null && node.getInitializer().contains(theExpression.getLeaf())) || (node.getUpdate() != null && node.getUpdate().contains(theExpression.getLeaf())))) {
                return null;
            }
            // initializer and update operation can have any result type, including none
            TypeElement tel = info.getElements().getTypeElement("java.lang.Void");
            if (tel == null) {
                return null;
            }
            return Collections.singletonList(tel.asType()); // NOI18N
        }
    }

    @Override
    public List<? extends TypeMirror> visitSwitch(SwitchTree node, Object p) {
        if (theExpression == null) {
            initExpression(node.getExpression());
        }
        for (CaseTree cs : node.getCases()) {
            if (cs.getExpression() != null) {
                TreePath casePath = new TreePath(getCurrentPath(), cs);
                TypeMirror caseType = info.getTrees().getTypeMirror(new TreePath(casePath, cs.getExpression()));
                return Collections.singletonList(caseType);
            }
        }
        // cannot determine
        return null;
    }

    private List<? extends TypeMirror> booleanType() {
        return Collections.singletonList(info.getTypes().getPrimitiveType(TypeKind.BOOLEAN));
    }

    /**
     * Handles subexpression in conditional expr. If the expression is the condition, the expected
     * type is boolean. Otherwise the parent expression is evaluated for expected types. It is expected
     * that the 'expression' will be eventually casted to the desired type, while the other branch' 
     * expression should remain as it is. Types, that theExpression cannot be casted to, or the other
     * branch' expression can't be assigned to (must be casted as well) are rejected.
     * 
     * @param node the conditional node
     * @param p dummy
     * @return list of possible types for the expression
     */
    @Override
    public List<? extends TypeMirror> visitConditionalExpression(ConditionalExpressionTree node, Object p) {
        if (theExpression == null) {
            // cannot determine
            return null;
        }
        if (theExpression.getLeaf() == node.getCondition()) {
            return booleanType();
        }
        Tree otherExpression;
        if (theExpression.getLeaf() == node.getFalseExpression()) {
            otherExpression = node.getTrueExpression();
        } else {
            otherExpression = node.getFalseExpression();
        }
        TypeMirror otherType = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), otherExpression));
        TypeMirror thisType = info.getTrees().getTypeMirror(getExpressionWithoutCasts());
        if (!(Utilities.isValidType(otherType) && Utilities.isValidType(thisType))) {
            return null;
        }

        ExpectedTypeResolver subResolver = new ExpectedTypeResolver(getCurrentPath(), getCurrentPath(), info);
        subResolver.typeCastDepth++;
        List<? extends TypeMirror> pp = subResolver.scan(getCurrentPath().getParentPath(), null);
        
        if (pp == null) {
            return null;
        }
        List<? extends TypeMirror> parentTypes = new ArrayList<TypeMirror>(pp);

        for (Iterator<? extends TypeMirror> it = parentTypes.iterator(); it.hasNext(); ) {
            TypeMirror m = it.next();
            if (!info.getTypeUtilities().isCastable(thisType, m)) {
                Scope s = info.getTrees().getScope(getCurrentPath());
                SourcePositions pos = info.getTrees().getSourcePositions();
                StringBuilder sb = new StringBuilder();
                int posFirst = (int)pos.getStartPosition(info.getCompilationUnit(), theExpression.getLeaf());
                int posSecond = (int)pos.getStartPosition(info.getCompilationUnit(), otherExpression);
                
                if (posFirst < 0 || posSecond < 0) {
                    // LOMBOK
                    return null;
                }
                String first = info.getText().substring(posFirst, 
                        (int)pos.getEndPosition(info.getCompilationUnit(), theExpression.getLeaf()));
                String second = info.getText().substring(posSecond, 
                        (int)pos.getEndPosition(info.getCompilationUnit(), otherExpression));
                sb.append(first).append("+").append(second);
                ExpressionTree expr = info.getTreeUtilities().parseExpression(sb.toString(), new SourcePositions[1]);
                TypeMirror targetType = purify(info, info.getTreeUtilities().attributeTree(expr, s));
                if (targetType == null || !info.getTypes().isAssignable(targetType, m)) {
                    it.remove();
                }
            }
        }
        return parentTypes.isEmpty() ? Collections.singletonList(otherType) : parentTypes;
    }

    private TypeMirror purify(CompilationInfo info, TypeMirror targetType) {
        if (targetType != null && targetType.getKind() == TypeKind.ERROR) {
            targetType = info.getTrees().getOriginalType((ErrorType) targetType);
        }

        if (targetType == null || targetType.getKind() == /*XXX:*/TypeKind.ERROR || targetType.getKind() == TypeKind.NONE || targetType.getKind() == TypeKind.NULL) return null;

        return Utilities.resolveCapturedType(info, targetType);
    }

    /**
     * @return Returns boolean expression for if-conditional
     */
    @Override
    public List<? extends TypeMirror> visitIf(IfTree node, Object p) {
        if (theExpression == null) {
            initExpression(node.getCondition());
        }
        return booleanType();
    }

    @Override
    public List<? extends TypeMirror> visitExpressionStatement(ExpressionStatementTree node, Object p) {
        if (theExpression == null) {
            initExpression(getCurrentPath());
        }
        return Collections.singletonList(info.getTypes().getNoType(TypeKind.VOID)); //info.getElements().getTypeElement("java.lang.Void").asType()); // NOI18N
    }

    @Override
    public List<? extends TypeMirror> visitReturn(ReturnTree node, Object p) {
        if (node.getExpression() == null) {
            return null;
        }
        if (theExpression == null) {
            initExpression(node.getExpression());
        }
        TreePath parents = getCurrentPath();
        while (parents != null && parents.getLeaf().getKind() != Tree.Kind.METHOD) {
            parents = parents.getParentPath();
        }
        if (parents != null) {
            Tree returnTypeTree = ((MethodTree) parents.getLeaf()).getReturnType();
            if (returnTypeTree != null) {
                return Collections.singletonList(info.getTrees().getTypeMirror(new TreePath(parents, returnTypeTree)));
            }
        }
        return null;
    }

    /**
     * Computes possible types for throw expression. Throw can safely throw an exception, which is
     * either declared by method as a thrown type, or catched within method, by an upper try-catch block.
     * Unchecked exceptions are permitted (derivatives of RuntimeException or Error).
     */
    @Override
    public List<? extends TypeMirror> visitThrow(ThrowTree node, Object p) {
        List<TypeMirror> result = new ArrayList<TypeMirror>();
        TreePath parents = getCurrentPath();
        Tree prev = null;
        while (parents != null && parents.getLeaf().getKind() != Tree.Kind.METHOD) {
            Tree l = parents.getLeaf();
            if (l.getKind() == Tree.Kind.TRY) {
                TryTree tt = (TryTree) l;
                if (prev == tt.getBlock()) {
                    for (CatchTree ct : tt.getCatches()) {
                        TypeMirror ex = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), ct.getParameter().getType()));
                        if (ex != null) {
                            switch (ex.getKind()) {
                                case DECLARED:
                                    if (!result.contains(ex)) {
                                        result.add(ex);
                                    }
                                    break;
                                case UNION:
                                    for (TypeMirror t : ((UnionType) ex).getAlternatives()) {
                                        if (!result.contains(t)) {
                                            result.add(t);
                                        }
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
            prev = l;
            parents = parents.getParentPath();
        }
        if (parents != null) {
            MethodTree mt = (MethodTree) parents.getLeaf();
            for (ExpressionTree etree : mt.getThrows()) {
                TypeMirror m = info.getTrees().getTypeMirror(new TreePath(parents, etree));
                if (m != null && !result.contains(m)) {
                    result.add(m);
                }
            }
        }
        TypeMirror jlre = info.getElements().getTypeElement("java.lang.RuntimeException").asType(); // NOI18N
        TypeMirror jler = info.getElements().getTypeElement("java.lang.Error").asType(); // NOI18N
        for (TypeMirror em : result) {
            if (jlre != null && info.getTypes().isAssignable(jlre, em)) {
                jlre = null;
            }
            if (jler != null && info.getTypes().isAssignable(jler, em)) {
                jler = null;
            }
            if (jlre == null && jler == null) {
                break;
            }
        }
        if (jlre != null) {
            result.add(jlre);
        }
        if (jler != null) {
            result.add(jler);
        }
        return result;
    }

    @Override
    public List<? extends TypeMirror> visitAssert(AssertTree node, Object p) {
        if (theExpression == null) {
            initExpression(node.getCondition());
        }
        return booleanType();
    }

    @Override
    public List<? extends TypeMirror> visitMethodInvocation(MethodInvocationTree node, Object p) {
        TypeMirror execType = info.getTrees().getTypeMirror(
                new TreePath(getCurrentPath(), node.getMethodSelect()));
        if (execType == null || execType.getKind() != TypeKind.EXECUTABLE) {
            return null;
        }
        return visitMethodOrNew(node, p, node.getArguments(),
                (ExecutableType)execType);
    }

    private List<? extends TypeMirror> visitMethodOrNew(Tree node, Object p, List<? extends ExpressionTree> args, 
            ExecutableType execType) {
        List<TypeMirror> proposed = new ArrayList<TypeMirror>();
        int[] index = new int[1];
        if (theExpression == null) {
            List<ExecutableElement> methods = org.netbeans.modules.editor.java.Utilities.fuzzyResolveMethodInvocation(info, getCurrentPath(), proposed, index);
            if (methods.isEmpty()) {
                return null;
            } else {
                initExpression(args.get(index[0]));
                return proposed;
            }
        } else {
            Element el = info.getTrees().getElement(getCurrentPath());
            if (el == null) {
                return null;
            }
            if (theExpression.getLeaf() != node &&
                (el.getKind() == ElementKind.METHOD || el.getKind() == ElementKind.CONSTRUCTOR)) {
                int argIndex = args.indexOf(theExpression.getLeaf());
                this.parentExecutable = getCurrentPath();
                TypeMirror argm;
                ExecutableElement ee = (ExecutableElement)el;
                boolean allowEntireVararg = false;
                boolean varargPosition = false;
                if (ee.isVarArgs() && (varargPosition = argIndex >= ee.getParameters().size() -1)) {
                    // all parameters after the vararg will be reported at the varargs position. 
                    allowEntireVararg = argIndex == ee.getParameters().size() -1;
                    argIndex = ee.getParameters().size() - 1;
                    if (allowEntireVararg) {
                        this.argIndex = ee.getParameters().size() - 1;
                    } else {
                        this.argIndex = ee.getParameters().size();
                    }
                } else {
                    this.argIndex = argIndex;
                }

                if (execType != null) {
                    // handle varargs arguments; if the argtype is a vararg, then either array of the type (reported in argm),
                    // or the component can be passed.
                    argm = execType.getParameterTypes().get(argIndex);
                    // XXX hack
                    argm = decapture(argm);
                } else {
                    argm = ((ExecutableElement)el).getParameters().get(argIndex).asType();
                }
                if (argm == null || argm.getKind() == TypeKind.ERROR) {
                    targetArgType = null;
                    return null;
                }
                if (varargPosition && argm.getKind() == TypeKind.ARRAY) {
                    TypeMirror ctype = ((ArrayType)argm).getComponentType();
                    if (allowEntireVararg) {
                        targetArgType = argm;
                        return Arrays.asList(new TypeMirror[] { argm, ctype });
                    }
                    argm = ctype;
                }
                targetArgType = argm;
                return Collections.singletonList(argm);
            }
        }
        return null;
    }
    
    private TypeMirror decapture(TypeMirror argm) {
        if (argm instanceof CapturedType) {
            argm = ((CapturedType)argm).wildcard;
        }
        if (argm.getKind() == TypeKind.WILDCARD) {
            WildcardType wctype = (WildcardType)argm;
            TypeMirror bound = wctype.getExtendsBound();
            if (bound != null) {
                return bound;
            } 
            bound = wctype.getSuperBound();
            if (bound != null) {
                return bound;
            }
            return null;
        } 
        return argm;
    }
    
    public TreePath getParentExecutable() {
        return parentExecutable;
    }
    
    public int getArgumentIndex() {
        return argIndex;
    }
    
    @Override
    public List<? extends TypeMirror> visitNewClass(NewClassTree node, Object p) {
        TypeMirror tm = info.getTrees().getTypeMirror(getCurrentPath());
        if (tm == null || tm.getKind() != TypeKind.DECLARED) {
            return null;
        }
        Element el = info.getTrees().getElement(getCurrentPath());
        if (el == null) {
            return null;
        }
        if (theExpression.getLeaf() != node.getEnclosingExpression()) {
            ExecutableType execType = (ExecutableType)info.getTypes().asMemberOf((DeclaredType)tm, el);
            return visitMethodOrNew(node, p, node.getArguments(), execType);
        } else {
            DeclaredType dt = (DeclaredType)tm;
            if (dt.getEnclosingType() == null) {
                return null;
            }
            return Collections.singletonList(dt.getEnclosingType());
        }
    }

    @Override
    public List<? extends TypeMirror> visitNewArray(NewArrayTree node, Object p) {
        if (node.getDimensions() == null) {
            return null;
        }
        if (theExpression == null && node.getDimensions().size() == 1) {
            initExpression(node.getDimensions().get(0));
        } else if (!node.getDimensions().contains(theExpression.getLeaf())) {
            return null;
        }
        return Collections.singletonList(info.getTypes().getPrimitiveType(TypeKind.INT));
    }

    @Override
    public List<? extends TypeMirror> visitParenthesized(ParenthesizedTree node, Object p) {
        // ignore parenthesis and go up:
        return scanParent();
    }

    /**
     * Assignment expects the assigned-to variable's type.
     */
    @Override
    public List<? extends TypeMirror> visitAssignment(AssignmentTree node, Object p) {
        if (theExpression == null) {
            initExpression(new TreePath(getCurrentPath(), node.getExpression()));
        }
        return Collections.singletonList(info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), node.getVariable())));
    }

    /**
     * Compound assignment expects the assigned-to variable's type.
     */
    @Override
    public List<? extends TypeMirror> visitCompoundAssignment(CompoundAssignmentTree node, Object p) {
        if (theExpression == null) {
            initExpression(new TreePath(getCurrentPath(), node.getExpression()));
        }
        return Collections.singletonList(info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), node.getVariable())));
    }

    @Override
    public List<? extends TypeMirror> visitUnary(UnaryTree node, Object p) {
        switch (node.getKind()) {
            case POSTFIX_DECREMENT: case POSTFIX_INCREMENT: case PREFIX_DECREMENT: case PREFIX_INCREMENT:
                // the incremented value is a l-value, it's type cannot be changed. We shouldn't be at this code path at all
                return null;
            case PLUS:
            case BITWISE_COMPLEMENT:
                scanParent();
                break;
            case LOGICAL_COMPLEMENT:
                return booleanType();
        }
        return null;
    }

//    private static final EnumSet<Tree.Kind>  BOOLEAN_OPS = EnumSet.of(
//            Tree.Kind.AND, Tree.Kind.OR, Tree.Kind.XOR, 
//            Tree.Kind.CONDITIONAL_AND, Tree.Kind.CONDITIONAL_OR
//    );
//    
    private static final EnumSet<Tree.Kind> ARITHMETIC_OPS = EnumSet.of(
            Tree.Kind.PLUS, Tree.Kind.MINUS, Tree.Kind.MULTIPLY, Tree.Kind.DIVIDE, Tree.Kind.REMAINDER
    );
    
    private static final EnumSet<Tree.Kind>  COMPARISON_OPS = EnumSet.of(
            Tree.Kind.LESS_THAN, Tree.Kind.LESS_THAN_EQUAL,
            Tree.Kind.GREATER_THAN, Tree.Kind.GREATER_THAN_EQUAL,
            Tree.Kind.EQUAL_TO, Tree.Kind.NOT_EQUAL_TO
            
    );

    private TreePath getExpressionWithoutCasts() {
        if (casted != null) {
            return casted;
        } else {
            return theExpression == null ? originalExpression : theExpression;
        }
    }
    
    private static boolean isPrimitiveType(TypeKind k) {
        return k.isPrimitive();
    }

    /**
     * Acceptor for binary operations. If the result is String & the operator some +, then 
     * the expression is autoconverted to String anyways, so it can retain its type without casting.
     * <p/>
     * In comparison operator, the desired type depends on both the operands and widening conversion.
     * Typecasts to smaller types may not be necessary.
     */
    @Override
    public List<? extends TypeMirror> visitBinary(BinaryTree node, Object p) {
        TypeMirror resultType = info.getTrees().getTypeMirror(getCurrentPath());
        if (node.getLeftOperand() == null || node.getRightOperand() == null) {
            return null;
        }
        TypeMirror lhsType = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), node.getLeftOperand()));
        TypeMirror rhsType = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), node.getRightOperand()));
        
        if (lhsType == null || rhsType == null || theExpression == null) {
            return null;
        }
        
        boolean resultIsString = false;
        if (resultType.getKind() == TypeKind.DECLARED) {
            Element e = ((DeclaredType)resultType).asElement();
            if (e.getKind() == ElementKind.CLASS) {
                TypeElement tel = (TypeElement)e;
                resultIsString = tel.getQualifiedName().contentEquals("java.lang.String"); // NOI18N
            }
        }
        
//        if (resultType.getKind() == TypeKind.DECLARED) {
//            Element e = ((DeclaredType)resultType).asElement();
//            if (e.getKind() == ElementKind.CLASS || e.getKind() == ElementKind.INTERFACE || e.getKind() == ElementKind.ENUM) {
//                TypeElement tel = (TypeElement)e;
//                if (tel.getQualifiedName().contentEquals("java.lang.String")) { // NOI18N
//                    if (ARITHMETIC_OPS.contains(node.getKind())) {
//                        // <something> + String, which results in String
//                    }
//                }
//            }
//        } 
        // comparison of primitive types means the type must be promoted up to the other type
        if (COMPARISON_OPS.contains(node.getKind())) {
            TreePath expPath = getExpressionWithoutCasts();
            TypeMirror expType = info.getTrees().getTypeMirror(expPath);
            TypeMirror rettype = null;
            
            if (theExpression.getLeaf() == node.getLeftOperand()) {
                lhsType = expType;
            } else {
                rhsType = expType;
            }
            if (lhsType == null || rhsType == null) {
                return null;
            }
            if (lhsType.getKind() == TypeKind.DOUBLE) {
                rettype = lhsType;
            } else if (rhsType.getKind() == TypeKind.DOUBLE) {
                rettype = rhsType;
            } else if (lhsType.getKind() == TypeKind.FLOAT) {
                rettype = lhsType;
            } else if (rhsType.getKind() == TypeKind.FLOAT) {
                rettype = rhsType;
            } else if (lhsType.getKind() == TypeKind.LONG) {
                rettype = lhsType;
            } else if (rhsType.getKind() == TypeKind.LONG) {
                rettype = rhsType;
            } else {
                rettype = info.getTypes().getPrimitiveType(TypeKind.INT);
            }
            if (rettype == null) {
                return null;
            } else {
                return Collections.singletonList(rettype);
            }
        } else if (ARITHMETIC_OPS.contains(node.getKind())) {
            TypeMirror followed;
            TypeMirror other;
            if (node.getLeftOperand() == theExpression.getLeaf()) {
                followed = lhsType;
                other = rhsType;
            } else {
                followed = rhsType;
                other = lhsType;
            }
            if (isPrimitiveType(followed.getKind())) {
                if (isPrimitiveType(resultType.getKind())) {
                    // if the followed [numeric] subexpression is casted to a type broader than the other operand, it should be left as it is. The reason is 
                    // a potential different type of the expression result influencing results or validity of operations up the tree.
                    TypeKind otherKind = Utilities.getPrimitiveKind(info, other);
                    if (otherKind != null && followed.getKind() != TypeKind.ERROR) {
                        if (followed.getKind().ordinal() > otherKind.ordinal()) {
                            // terminate, the cast is needed
                            notRedundant = true;
                            return Collections.singletonList(followed);
                        }
                    }
                } else if (resultType.getKind() == TypeKind.DECLARED) {
                    if (resultIsString) {
                        // primitive + string: 
                        TreePath expPath = getExpressionWithoutCasts();
                        TypeMirror expType = info.getTrees().getTypeMirror(expPath);
                        
                        if (expType != null && expType.getKind().isPrimitive() && expType.getKind() != followed.getKind()) {
                            // if the cast is to more narrow type, permit it, as it looses precision, and potential produces a different value. Other
                            // hint / warning should take care of precision loss.
                            if (expType.getKind() != TypeKind.CHAR && followed.getKind().ordinal() < expType.getKind().ordinal()) {
                                return null;
                            }
                            
                            // next, permit conversions to different types: int -> float, char -> int
                            switch (expType.getKind()) {
                                case INT: case LONG:
                                    if (followed.getKind().ordinal() >= TypeKind.FLOAT.ordinal()) {
                                        notRedundant = true;
                                        return Collections.singletonList(info.getTypes().getPrimitiveType(TypeKind.FLOAT));
                                    }
                                    break;
                                case CHAR:
                                    notRedundant = true;
                                    if (followed.getKind() == TypeKind.LONG) {
                                        return Collections.singletonList(info.getTypes().getPrimitiveType(TypeKind.INT));
                                    }
                                    if (followed.getKind().ordinal() < TypeKind.FLOAT.ordinal()) {
                                        return Collections.singletonList(followed);
                                    } else {
                                        return Collections.singletonList(info.getTypes().getPrimitiveType(TypeKind.FLOAT));
                                    }
                            }
                        }
                    }
                }
            } else if (resultIsString) {
                TreePath tp = getExpressionWithoutCasts();
                if (tp == null) {
                    return null;
                }
                TypeMirror m = info.getTrees().getTypeMirror(tp);
                if (m == null) {
                    return null;
                }
                return Collections.singletonList(m);
                
            }

            return Collections.singletonList(resultType);
        }
        
        return null;
    }
    
    private final EnumSet<TypeKind> NUMERIC_TYPES = EnumSet.of(TypeKind.BYTE, TypeKind.DOUBLE, TypeKind.FLOAT, TypeKind.INT, TypeKind.LONG, TypeKind.SHORT);

    /**
     * Anything object-typed could be in the instance-of
     * 
     * @param node
     * @param p
     * @return 
     */
    @Override
    public List<? extends TypeMirror> visitInstanceOf(InstanceOfTree node, Object p) {
        if (theExpression == null) {
            initExpression(new TreePath(getCurrentPath(), node.getExpression()));
        }
        TypeElement tel = info.getElements().getTypeElement("java.lang.Object");
        if (tel == null) {
            return null;
        }
        return Collections.singletonList(tel.asType()); // NOI18N
    }
    
    /**
     * Index in array access must be int
     */
    @Override
    public List<? extends TypeMirror> visitArrayAccess(ArrayAccessTree node, Object p) {
        if (theExpression == null) {
            return null;
        }
        // for now we do not guess array type, just the indexes.
        if (theExpression == node.getExpression()) {
            return null;
        }
        return Collections.singletonList(info.getTypes().getPrimitiveType(TypeKind.INT));
    }

    /**
     * For member select, find the most generic type which declares that member.
     * When traversing up the inheritance tree, the return type must be checked, as it may
     * become too general to fit the parent expression's requirements.
     */
    @Override
    public List<? extends TypeMirror> visitMemberSelect(MemberSelectTree tree, Object v) {
        if (casted != null) {
            // if the casted type is a primitive, the cast is NOT redundant as member select is applied to
            // the originally primitive value.
            TypeMirror castedType = info.getTrees().getTypeMirror(casted);
            if (castedType != null && castedType.getKind().isPrimitive()) {
                notRedundant = true;
            }
        }
        // must compute expected type of the method:
        TreePath[] p = new TreePath[1];
        ExpressionTree[] e = new ExpressionTree[1];
        Tree[] l = new Tree[1];
        List<TypeMirror> tt = new ArrayList<TypeMirror>();
        Element el = info.getTrees().getElement(getCurrentPath());
        
        if (el == null) {
            return null;
        }
        
        if (el.getKind() == ElementKind.METHOD) {
            // special hack: if the casted value is a lambda, we NEED to assign it a type prior to method invocation:
            TreePath exp = getExpressionWithoutCasts();
            if (exp != null && exp.getLeaf().getKind() == Tree.Kind.LAMBDA_EXPRESSION) {
                return null;
            }
            TreePath methodInvocation = getCurrentPath().getParentPath();
            TreePath invocationParent = methodInvocation.getParentPath();
            ExpectedTypeResolver subResolver = new ExpectedTypeResolver(methodInvocation, info);
            subResolver.theExpression = methodInvocation;
            subResolver.typeCastDepth++;
            List<? extends TypeMirror> parentTypes = subResolver.scan(invocationParent, v);
            TypeMirror castable = null;
            if (parentTypes == null) {
                castable = subResolver.getCastableTo();
            }
            if (parentTypes != null || castable != null) {
                TypeMirror exprType = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), tree.getExpression()));
                if (!(exprType instanceof DeclaredType)) {
                    return null;
                }
                ExecutableElement elem = (ExecutableElement)el;
                TreePath method = getCurrentPath();
                while (method != null && method.getLeaf().getKind() != Tree.Kind.METHOD) {
                    method = method.getParentPath();
                }
                if (method == null) {
                    method = getCurrentPath();
                }
                List<TypeMirror> cans = findBaseTypes(info, elem, (DeclaredType)exprType,
                        parentTypes, 
                        castable, 
                        info.getTrees().getScope(method));
                if (!cans.isEmpty()) {
                    return cans;
                }
            } else {
                TypeMirror exprm = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), tree.getExpression()));
                return Collections.singletonList(exprm);
            }
        } else if (el.getKind() == ElementKind.FIELD) {
            // access to a field
            Element parent = el.getEnclosingElement();
            if (parent.getKind() == ElementKind.CLASS || parent.getKind() == ElementKind.INTERFACE || parent.getKind() == ElementKind.ENUM || parent.getKind() == ElementKind.RECORD) {
                return Collections.singletonList(parent.asType());
            }
        }
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitOther(Tree node, Object p) {
        return null;
    }

    /**
     * Scans parent of the current path. Records the current node into 'theExpression', so visitor knows
     * what Tree node it came from.
     * @return 
     */
    private List<? extends TypeMirror> scanParent() {
        this.theExpression = getCurrentPath();
        this.typeCastDepth--;
        return scan(getCurrentPath().getParentPath(), null);
    }

    @Override
    public List<? extends TypeMirror> visitTypeCast(TypeCastTree node, Object p) {
        // just ingore, and go up:
        if (typeCastDepth == 1) {
            if (casted == null) {
                casted = new TreePath(getCurrentPath(), node.getExpression());
                while (casted.getLeaf().getKind() == Tree.Kind.PARENTHESIZED) {
                    casted = new TreePath(casted, ((ParenthesizedTree)casted.getLeaf()).getExpression());
                }
            } else {
                if (!info.getTypeUtilities().isCastable(
                    info.getTrees().getTypeMirror(casted),
                    info.getTrees().getTypeMirror(getCurrentPath()))) {
                    notRedundant = true;
                }
            }
            dontResetCast = true;
            return scanParent();
        } else {
            // this is a typecast after some other expression. It's not possible to determine a type which the
            // casted expression is ASSIGNABLE to, but it must be CASTABLE to the type used in the typecast.
            castableTo = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), node.getType()));
            return null;
        }
    }
    
    /**
     * Finds and returns the most generic super types, which declare the method. The expected return type and
     * accessing Element must be provided, so that the method declaration is really accessible to the potential
     * caller.
     * 
     * @param methodType the method type to search for.
     * @return 
     */
    static List<TypeMirror> findBaseTypes(CompilationInfo info, 
            ExecutableElement method,
            DeclaredType startFrom,
            Collection<? extends TypeMirror> expectedReturnType, 
            TypeMirror castableToType, 
            Scope accessor) {
        Set<TypeMirror> seenTypes = new HashSet<TypeMirror>();
        List<TypeMirror> collected = new ArrayList<TypeMirror>();
        collectMethods(info, accessor, method, expectedReturnType, castableToType,
                startFrom, collected, seenTypes);
        return collected;
    }
    
    private static boolean assignableToSomeType(CompilationInfo info, TypeMirror theValue, Collection<? extends TypeMirror> alternatives) {
        for (TypeMirror target : alternatives) {
            if (target.getKind() == TypeKind.VOID) {
                return true;
            }
            if (info.getTypes().isAssignable(theValue, target)) {
                return true;
            }
        }
        return false;
    }
    
    private static void collectMethods(CompilationInfo info, 
            Scope accessor, ExecutableElement method, Collection<? extends TypeMirror> expectedReturnType, TypeMirror castableToType,
            DeclaredType dcc,
            List<TypeMirror> collected, Set<TypeMirror> seenTypes) {
        TypeElement c = (TypeElement)dcc.asElement();
        boolean tooAbstract = false;
        ExecutableElement found = null;

        if (!seenTypes.add(c.asType())) {
            return;
        }
        
        if (accessor == null || info.getTrees().isAccessible(accessor, c)) {
            for (ExecutableElement m : ElementFilter.methodsIn(c.getEnclosedElements())) {
                if (!m.getSimpleName().contentEquals(method.getSimpleName())) {
                    continue;
                }
                if (accessor != null && !info.getTrees().isAccessible(accessor, m, dcc)) {
                    continue;
                }
                if (m == method || info.getElements().overrides(method, m, c)) {
                    if (expectedReturnType != null) {
                        if (assignableToSomeType(info, m.getReturnType(), expectedReturnType)) {
                            found = m;
                        } else {
                            tooAbstract = true;
                        }
                    } else if (castableToType != null) {
                        if (info.getTypeUtilities().isCastable(m.getReturnType(), castableToType)) {
                            found = m;
                        }
                    } else {
                        // no clue as for the return type -> accept the method.
                        found = m;
                    }
                    // in a hope the source is not broken, no other method may be overriden.
                    break;
                }
            }
        }
        // if a overriden method is found, but its ret type was too abstract for the caller, then no super-overriden
        // method may be return type - compatible with the caller (can be only the same type, or a supertype thereof).
        if (!tooAbstract) {
            List<? extends TypeMirror> supertypes = info.getTypes().directSupertypes(dcc);
            for (TypeMirror supType : supertypes) {
                if (!(supType instanceof DeclaredType)) {
                    continue;
                }
                collectMethods(info, accessor, method, expectedReturnType, 
                        castableToType, (DeclaredType)supType, collected, seenTypes);
            }
        }
        if (found != null) {
            addTypeAndReplaceMoreSpecific(info, collected, dcc);
        }
    }
    
    /**
     * Helper method, which retains the most specific types in the collection.
     * If a generic type is passed and the collection contains more specific ones, nothing happens.
     * If a specific type is passed and the collection contains some generic one(s), the generic
     * forms are removed.
     */
    private static void addTypeAndReplaceMoreSpecific(CompilationInfo info, Collection<TypeMirror> collected, TypeMirror nue) {
        for (Iterator<? extends TypeMirror> it = collected.iterator(); it.hasNext(); ) {
            TypeMirror m = it.next();
            if (info.getTypes().isAssignable(nue, m)) {
                return;
            } else if (info.getTypes().isAssignable(m, nue)) {
                it.remove();
            }
        }
        collected.add(nue);
    }

    // TODO: enh for loop should accept Collections or arrays
    @Override
    public List<? extends TypeMirror> visitEnhancedForLoop(EnhancedForLoopTree node, Object p) {
        TypeMirror varType = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), node.getVariable()));
        if (!Utilities.isValidType(varType)) {
            return null;
        } else {
            TypeMirror arrayType = info.getTypes().getArrayType(varType);
            TypeElement iterableEl = info.getElements().getTypeElement("java.lang.Iterable"); // NOI18N
            if (iterableEl == null || iterableEl.getKind() != ElementKind.INTERFACE) {
                return null;
            }
            TypeMirror iterableForVar = isPrimitiveType(varType.getKind()) ?
                    info.getTypes().getDeclaredType(iterableEl, 
                        info.getTypes().getWildcardType(
                            info.getTypes().boxedClass((PrimitiveType)varType).asType(), null))
                    :
                    info.getTypes().getDeclaredType(iterableEl, 
                        info.getTypes().getWildcardType(varType, null)
                    );
            List<TypeMirror> result = new ArrayList<TypeMirror>(2);
            result.add(arrayType);
            result.add(iterableForVar);
            return result;
        }
    }
    
    // acceptors for statements, which do not form or use an expression

    @Override
    public List<? extends TypeMirror> visitAnnotatedType(AnnotatedTypeTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitAnnotation(AnnotationTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitBlock(BlockTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitBreak(BreakTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitCase(CaseTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitCatch(CatchTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitClass(ClassTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitContinue(ContinueTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitErroneous(ErroneousTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitIdentifier(IdentifierTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitImport(ImportTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitLabeledStatement(LabeledStatementTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitLiteral(LiteralTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitMethod(MethodTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitModifiers(ModifiersTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitLambdaExpression(LambdaExpressionTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitMemberReference(MemberReferenceTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitEmptyStatement(EmptyStatementTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitSynchronized(SynchronizedTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitCompilationUnit(CompilationUnitTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitPackage(PackageTree pt, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitTry(TryTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitParameterizedType(ParameterizedTypeTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitUnionType(UnionTypeTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitIntersectionType(IntersectionTypeTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitArrayType(ArrayTypeTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitPrimitiveType(PrimitiveTypeTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitTypeParameter(TypeParameterTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitWildcard(WildcardTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitModule(ModuleTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitExports(ExportsTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitOpens(OpensTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitProvides(ProvidesTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitRequires(RequiresTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitUses(UsesTree node, Object p) {
        return null;
    }
   
    @Override
    public List<? extends TypeMirror> visitBindingPattern(BindingPatternTree bpt, Object p) {
        return null;
    }
    
    @Override
    public List<? extends TypeMirror> visitSwitchExpression(SwitchExpressionTree set, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitYield(YieldTree yt, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitDefaultCaseLabel(DefaultCaseLabelTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitConstantCaseLabel(ConstantCaseLabelTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitPatternCaseLabel(PatternCaseLabelTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitDeconstructionPattern(DeconstructionPatternTree node, Object p) {
        return null;
    }

    @Override
    public List<? extends TypeMirror> visitAnyPattern(AnyPatternTree apt, Object p) {
        return null;
    }

}
