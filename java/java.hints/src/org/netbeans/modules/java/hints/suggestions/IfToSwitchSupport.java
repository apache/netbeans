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
package org.netbeans.modules.java.hints.suggestions;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.ArithmeticUtilities;
import org.netbeans.modules.java.hints.bugs.NPECheck;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.util.NbBundle;

/**
 * Evaluates a series of if-else-if conditions
 */
@NbBundle.Messages({
    "# initial label for breaking out of the innermost loop",
    "LABEL_OuterGeneratedLabelInitial=OUTER",
    "# template for generated label names, must form a valid Java identifiers",
    "# {0} - unique integer",
    "LABEL_OuterGeneratedLabel=OUTER_{0}"
    
})
public class IfToSwitchSupport {
    private final HintContext ctx;
    protected final CompilationInfo ci;
    /**
     * True, if the control variable is used as selector -- it's definitely not null
     */
    private boolean controlVarNotNull;
    /**
     * The control variable
     */
    TreePath variable;
    /**
     * Type of the control variable; or the type to be casted to
     */
    private TypeMirror controlTypeMirror;
    /**
     * Literals which occur in more than one branch.
     */
    private Map<TreePath, Object> duplicateLiterals = Collections.emptyMap();
    /**
     * All literals already seen
     */
    private Set<Object> seenLiterals;
    /**
     * Temporary: literals activating one if branch
     */
    private List literals;
    List<BranchDescription> literal2Statement = new ArrayList<>();
    
    /**
     * Non-null, if a branch contains a null-check.
     */
    private TreePath nullBranch;
    
    public IfToSwitchSupport(HintContext ctx) {
        this.ctx = ctx;
        this.ci = ctx.getInfo();
    }

    /**
     * Gets number of branches, including the `else' default branch.
     * @return number of branches
     */
    public int getNumberOfBranches() {
        return literal2Statement.size();
    }

    public List<BranchDescription> getBranches() {
        return literal2Statement;
    }

    public boolean isControlNotNull() {
        return controlVarNotNull;
    }

    protected TypeMirror acceptArgType(TypeMirror controlType, TypeMirror argType) {
        if (ci.getTypes().isSameType(argType, controlType)) {
            return controlType;
        } else if (ci.getTypes().isAssignable(argType, controlType)) {
            return controlType;
        } else if (ci.getTypes().isAssignable(controlType, argType)) {
            return argType;
        }
        return null;
    }
    
    protected Object convert(Object o, TypeMirror m) {
        if (o instanceof EnumConst) {
            TypeMirror elM = ((EnumConst)o).constEl.asType();
            if (ci.getTypes().isSubtype(elM, m)) {
                return o;
            } else {
                return m;
            }
        }
        return ArithmeticUtilities.implicitConversion(ci, o, m);
    }

    /**
     * Adds a literal to the list of seen literals. If the new literal
     * is of a different type, it would not compare
     * @param l
     * @param commonType
     * @param litPath
     * @return
     */
    boolean addLiteral(Object l, TypeMirror commonType, TreePath litPath) {
        if (!ctx.getInfo().getTypes().isSameType(commonType, controlTypeMirror)) {
            // convert the seenLiterals to the new type:
            Set<Object> newLiterals = new HashSet<>(seenLiterals.size());
            for (Object o : seenLiterals) {
                Object converted = convert(o, commonType);
                if (converted == null) {
                    return false;
                }
                newLiterals.add(converted);
            }
            seenLiterals = newLiterals;
        }
        literals.add(TreePathHandle.create(litPath, ctx.getInfo()));
        if (seenLiterals.contains(l)) {
            if (duplicateLiterals.isEmpty()) {
                duplicateLiterals = new LinkedHashMap<>(3);
            }
            duplicateLiterals.put(litPath, l);
        } else {
            seenLiterals.add(l);
        }
        return true;
    }

    void reset() {
        if (!literals.isEmpty()) {
            literals = new ArrayList<>(2);
            duplicateLiterals = Collections.emptyMap();
            seenLiterals = new HashSet<>();
        }
    }

    protected void controlVariableNotNull() {
        controlVarNotNull = true;
    }
    
    protected void reportConstantAndLiteral(TreePath c1, TreePath c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    /**
     * Checks whether the code matches the comparison pattern.
     * @param test
     * @param initial
     * @return
     */
    protected TreePath matches(TreePath test, boolean initial) {
        return null;
    }
    
    public TypeMirror getVariableMirror() {
        return controlTypeMirror;
    }

    /**
     * Checks whether a chained item matched the comparison patterns.
     *
     * @param test
     * @param variable
     * @return
     */
    protected TreePath matchesChainedItem(TreePath test, TreePath variable) {
        return null;
    }

    private boolean isRealValue(Object o) {
        return ArithmeticUtilities.isNull(o) || o instanceof EnumConst || ArithmeticUtilities.isRealValue(o);
    }

    /**
     * Evaluates the constant. The method should return a constant value, possibly wrapped
     * if the value is primitive. It may return an encapsulated constant, with well-defned
     * equals/hashcode. The value is just compared and {@link #convert}ed. <p/>
     * Returns {@code null} if the path is not a compile-time constant.
     * @param path
     * @return constant value.
     */
    protected Object evalConstant(TreePath path) {
        TypeMirror m = ci.getTrees().getTypeMirror(path);
        if (m != null && m.getKind() != TypeKind.DECLARED) {
            return ArithmeticUtilities.compute(ci, path, true, true);
        }
        Element e = ci.getTrees().getElement(path);
        if (e != null && e.getKind() == ElementKind.ENUM_CONSTANT) {
            return new EnumConst(e);
        }
        return null;
    }

    static class EnumConst {

        final Element constEl;

        public EnumConst(Element constEl) {
            this.constEl = constEl;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 79 * hash + Objects.hashCode(this.constEl);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final EnumConst other = (EnumConst) obj;
            return this.constEl == other.constEl;
        }
    }
    TreePath c1;
    TreePath c2;
    
    public boolean process(TreePath initCond) {
        Iterable<? extends TreePath> split = linearizeOrs(initCond);
        TreePath body = ctx.getVariables().get("$body"); // NOI18N
        if (body == null) {
            return false;
        }
        if (!start(split, body)) {
            return false;
        }
        controlVarNotNull |= controlTypeMirror.getKind().isPrimitive() || NPECheck.isSafeToDereference(ci, variable);
        literal2Statement.add(new BranchDescription(literals, TreePathHandle.create(body, ctx.getInfo())));
        TreePath ifPath = body.getParentPath();
        Tree e = ((IfTree) ifPath.getLeaf()).getElseStatement();
        while (e != null && e.getKind() == Tree.Kind.IF) {
            literals = new ArrayList<>();
            ifPath = new TreePath(ifPath, e);
            IfTree it = (IfTree) ifPath.getLeaf();
            literals = new LinkedList<>();
            TreePath lastCondition = new TreePath(ifPath, it.getCondition());
            for (TreePath cond : linearizeOrs(lastCondition)) {
                TreePath constPath = matchesChainedItem(cond, variable);
                if (constPath == null) {
                    return false;
                }
                Object o = evalConstant(constPath);
                TypeMirror constType = ctx.getInfo().getTrees().getTypeMirror(constPath);
                boolean isNull = (constType != null && constType.getKind() == TypeKind.NULL);
                TypeMirror common = isNull ? controlTypeMirror : acceptArgType(controlTypeMirror, constType);
                if (isNull) {
                    nullBranch = new TreePath(ifPath, it.getThenStatement());
                }
                if (!isRealValue(o) || common == null) {
                    return false;
                }
                if (!addLiteral(convert(o, common), common, constPath)) {
                    return false;
                }
            }
            literal2Statement.add(new BranchDescription(literals, TreePathHandle.create(new TreePath(ifPath, it.getThenStatement()), ctx.getInfo())));
            e = it.getElseStatement();
        }
        if (e != null) {
            // the default statement
            TreePath defPath = new TreePath(ifPath, e);
            literal2Statement.add(new BranchDescription(null, TreePathHandle.create(defPath, ci)));
            if (!controlVarNotNull && nullBranch == null) {
                // the default branch may also trigger for null value - issue #259071
                nullBranch = defPath;
            }
        }
        return true;
    }

    boolean start(Iterable<? extends TreePath> conds, TreePath ifBody) {
        Iterator<? extends TreePath> iter = conds.iterator();
        TreePath first = iter.next();
        TreePath constPath = matches(first, true);
        if (constPath == null) {
            return false;
        }
        literals = new LinkedList<>();
        seenLiterals = new HashSet<>();
        Object c = evalConstant(c1);
        if (isRealValue(c)) {
            variable = c2;
            controlTypeMirror = ci.getTrees().getTypeMirror(c2);
            constPath = c1;
        } else {
            c = evalConstant(c2);
            if (isRealValue(c)) {
                variable = c1;
                constPath = c2;
            } else {
                // matches but does not compute
                return false;
            }
        }
        if (variable.getParentPath().getLeaf().getKind() == Tree.Kind.MEMBER_SELECT) {
            controlVarNotNull = true;
        }
        TypeMirror constType = ci.getTrees().getTypeMirror(c1);
        controlTypeMirror = ctx.getInfo().getTrees().getTypeMirror(variable);
        if (!Utilities.isValidType(controlTypeMirror)) {
            return false;
        }
        TypeMirror common = acceptArgType(controlTypeMirror, constType);
        if (common == null) {
            // next pattern, no common type
            return false;
        }
        if (ArithmeticUtilities.isNull(c)) {
            nullBranch = ifBody;
        }
        if (!addLiteral(convert(c, constType), common, constPath)) {
            return false;
        }
        while (iter.hasNext()) {
            TreePath lt = matchesChainedItem(iter.next(), this.variable);
            if (lt == null) {
                return false;
            }
            Object o = evalConstant(lt);
            if (!ArithmeticUtilities.isRealValue(o)) {
                return false;
            }
            constType = ci.getTrees().getTypeMirror(lt);
            common = acceptArgType(controlTypeMirror, constType);
            if (common == null) {
                return false;
            }
            if (!addLiteral(convert(o, constType), common, lt)) {
                return false;
            }
            if (ArithmeticUtilities.isNull(o)) {
                nullBranch = ifBody;
            }
        }
        return true;
    }

    /**
     * Unwraps OR alternatives from potential parenthesis.
     *
     * @param cond condition
     * @return list of OR alternatives
     */
    @SuppressWarnings(value = "AssignmentToMethodParameter")
    private static Iterable<? extends TreePath> linearizeOrs(TreePath cond) {
        List<TreePath> result = new LinkedList<>();
        while (cond.getLeaf().getKind() == Tree.Kind.CONDITIONAL_OR || cond.getLeaf().getKind() == Tree.Kind.PARENTHESIZED) {
            if (cond.getLeaf().getKind() == Tree.Kind.PARENTHESIZED) {
                cond = new TreePath(cond, ((ParenthesizedTree) cond.getLeaf()).getExpression());
                continue;
            }
            BinaryTree bt = (BinaryTree) cond.getLeaf();
            result.add(new TreePath(cond, bt.getRightOperand()));
            cond = new TreePath(cond, bt.getLeftOperand());
        }
        result.add(cond);
        Collections.reverse(result);
        return result;
    }

    public Map<TreePath, Object> getDuplicateConstants() {
        return duplicateLiterals;
    }

    public boolean containsDuplicateConstants() {
        return !duplicateLiterals.isEmpty();
    }
    
    public static final class BranchDescription {
        private final @NullAllowed Iterable<TreePathHandle> literals;
        private final @NonNull TreePathHandle path;

        public BranchDescription(Iterable literals, TreePathHandle path) {
            this.literals = literals;
            this.path = path;
        }
    }
    
    public JavaFix createFix(String fixLabel, boolean alwaysDefault) {
        TreePathHandle nHandle = nullBranch == null ? null : TreePathHandle.create(nullBranch, ctx.getInfo());
        ConvertToSwitch fix = new ConvertToSwitch(ctx.getInfo(),
                                 ctx.getPath(),
                                 TreePathHandle.create(variable, ctx.getInfo()),
                                 nHandle,
                                 literal2Statement,
                                 isControlNotNull(), fixLabel);
        if (alwaysDefault) {
            fix.addDefaultAlways();
        }
        return fix;
    }


    public static final class ConvertToSwitch extends JavaFix {
        private final TreePathHandle value;
        private final List<BranchDescription> literal2Statement;
        private final String label;
        private boolean alwaysCreateDefault;
        private boolean varNotNull;
        private Set<Tree> ifSeen = new HashSet<Tree>();
        private boolean enumType;
        private final TreePathHandle nullBranch;

        public ConvertToSwitch(CompilationInfo info, TreePath create, TreePathHandle value, TreePathHandle nullBranch, 
                List<BranchDescription> literal2Statement, 
                boolean varNotNull, String label) {
            super(info, create);
            this.value = value;
            this.literal2Statement = literal2Statement;
            this.varNotNull = varNotNull;
            this.label = label;
            this.nullBranch = nullBranch;
        }
        
        public void addDefaultAlways() {
            alwaysCreateDefault = true;
        }

        public String getText() {
            return label;
        }

        @Override
        protected void performRewrite(JavaFix.TransformationContext ctx) {
            WorkingCopy copy = ctx.getWorkingCopy();
            TreePath it = ctx.getPath();
            TreeMaker make = copy.getTreeMaker();
            List<CaseTree> cases = new LinkedList<CaseTree>();
            List<TreePath> resolved = new ArrayList<TreePath>(this.literal2Statement.size());
            Map<TreePath, Set<Name>> catch2Declared = new IdentityHashMap<TreePath, Set<Name>>();
            Map<TreePath, Set<Name>> catch2Used = new IdentityHashMap<TreePath, Set<Name>>();
            Map<BreakTree, StatementTree> break2Target = new IdentityHashMap<BreakTree, StatementTree>();
            
            TreePath value = this.value.resolve(copy);
            TreePath nullBranchResolved = null;
            if (value == null) {
                // FIXME - report an error
                return;
            }
            TypeMirror valType = copy.getTrees().getTypeMirror(value);
            if (!Utilities.isValidType(valType)) {
                // FIXME - report an error
                return;
            }
            if (nullBranch != null) {
                nullBranchResolved = this.nullBranch.resolve(copy);
                if (nullBranchResolved == null || !(nullBranchResolved.getLeaf() instanceof StatementTree)) {
                    return;
                }
            }
            enumType = valType.getKind() == TypeKind.DECLARED && 
                    ((DeclaredType)valType).asElement().getKind() == ElementKind.ENUM;

            boolean defaultPresent = false;
            for (BranchDescription d : this.literal2Statement) {
                TreePath s = d.path.resolve(copy);
                
                if (s == null) {
                    // FIXME - report an error
                    return ;
                }

                resolved.add(s);
                catch2Declared.put(s, declaredVariables(s));
                catch2Used.put(s, usedVariables(copy, s, break2Target));
                if (d.literals == null) {
                    defaultPresent = true;
                }
            }
            Iterator<TreePath> branchPaths = resolved.iterator();
            for (BranchDescription d : literal2Statement) {
                if (addCase(copy, d, branchPaths.next(), cases, catch2Declared, catch2Used)) {
                    return;
                }
            }
            if (!defaultPresent && alwaysCreateDefault) {
                addCase(copy, new BranchDescription(null, null), null, cases, catch2Declared, catch2Used);
            }

            varNotNull |= NPECheck.isSafeToDereference(copy, value);
            
            SwitchTree s = make.Switch((ExpressionTree) value.getLeaf(), cases);

            Utilities.copyComments(copy, it.getLeaf(), s, true);
            
            Tree nue = s;  
            if (!varNotNull) {
                // if the control variable is not null, AND there's a null-check branch, make
                // the null-branch first, it's easier to read.
                if (nullBranchResolved == null) {
                    nue = make.If(
                            make.Parenthesized(
                                make.Binary(Tree.Kind.NOT_EQUAL_TO, make.Literal(null), (ExpressionTree)value.getLeaf())
                            ),
                        s, null
                    );
                } else {
                    nue = make.If(
                        make.Parenthesized(make.Binary(Tree.Kind.EQUAL_TO, 
                                make.Literal(null), 
                                (ExpressionTree)value.getLeaf()
                        )), (StatementTree)nullBranchResolved.getLeaf(), s
                    );
                }
            } else if (nullBranchResolved != null) {
                nue = make.If(
                    make.Parenthesized(
                        make.Binary(Tree.Kind.EQUAL_TO, 
                                make.Literal(null), 
                                (ExpressionTree)value.getLeaf()
                        )
                    ), (StatementTree)nullBranchResolved.getLeaf(), s
                );
            }
            copy.rewrite(it.getLeaf(), nue); //XXX

            TreePath topLevelMethod = Utilities.findTopLevelBlock(it);
            final Set<String> seenLabels = new HashSet<String>();

            new ErrorAwareTreeScanner<Void, Void>() {
                @Override public Void visitLabeledStatement(LabeledStatementTree node, Void p) {
                    seenLabels.add(node.getLabel().toString());
                    return super.visitLabeledStatement(node, p);
                }
            }.scan(topLevelMethod.getLeaf(), null);

            Map<StatementTree, String> labels = new IdentityHashMap<StatementTree, String>();

            for (Map.Entry<BreakTree, StatementTree> e : break2Target.entrySet()) {
                String label = labels.get(e.getValue());

                if (label == null) {
                    labels.put(e.getValue(), label = computeLabel(seenLabels));
                    copy.rewrite(e.getValue(), make.LabeledStatement(label, e.getValue()));
                }
                
                copy.rewrite(e.getKey(), make.Break(label));
            }
        }

        private static String computeLabel(Set<String> labels) {
            int index = 0;
            String label = Bundle.LABEL_OuterGeneratedLabelInitial();

            while (labels.contains(label)) {
                label = Bundle.LABEL_OuterGeneratedLabel(++index);
            }

            labels.add(label);

            return label;
        }
        
        private Tree findExpressionParentIf(TreePath p) {
            while (p != null && !StatementTree.class.isAssignableFrom(p.getLeaf().getKind().asInterface())) {
                p = p.getParentPath();
            }
            return p == null ? null : p.getLeaf();
        }

        private boolean addCase(WorkingCopy copy, BranchDescription desc, TreePath path, List<CaseTree> cases, Map<TreePath, Set<Name>> catch2Declared, Map<TreePath, Set<Name>> catch2Used) {
            TreeMaker make = copy.getTreeMaker();
            List<StatementTree> statements = new LinkedList<StatementTree>();
            Tree replacedByCase = null;
            boolean breakGenerated = false;
            if (path != null) {
                Tree then = path.getLeaf();

                if (then.getKind() == Tree.Kind.BLOCK) {
                    Set<Name> currentDeclared = catch2Declared.get(path);
                    boolean keepBlock = false;

                    for (Map.Entry<TreePath, Set<Name>> e : catch2Declared.entrySet()) {
                        if (e.getKey() == path) continue;
                        if (!Collections.disjoint(currentDeclared, e.getValue())) {
                            keepBlock = true;
                            break;
                        }
                    }

                    if (!keepBlock) {
                        for (Map.Entry<TreePath, Set<Name>> e : catch2Used.entrySet()) {
                            if (e.getKey() == path) continue;
                            if (!Collections.disjoint(currentDeclared, e.getValue())) {
                                keepBlock = true;
                                break;
                            }
                        }
                    }

                    boolean exitsFromAllBranches = false;

                    for (Tree st : ((BlockTree) then).getStatements()) {
                        exitsFromAllBranches |= Utilities.exitsFromAllBranchers(copy, new TreePath(path, st));
                    }

                    BlockTree block = (BlockTree) then;
                    if (keepBlock) {
                        if (!exitsFromAllBranches) {
                            statements.add(
                                    make.asReplacementOf(
                                        make.addBlockStatement(block, make.Break(null)), block, true));
                        } else {
                            statements.add(block);
                        }
                    } else {
                        statements.addAll(block.getStatements());
                        replacedByCase = block;
                        if (!exitsFromAllBranches) {
                            statements.add(make.Break(null));
                        }
                    }
                    breakGenerated = true;
                } else {
                    statements.add((StatementTree) then);
                    if (!Utilities.exitsFromAllBranchers(copy, path)) {
                        statements.add(make.Break(null));
                    }
                }
            } else {
                // path == null, implicit default.
                statements.add(make.Break(null));
            }
            if (desc.literals == null) {
                CaseTree ct = make.Case(null, statements);
                if (replacedByCase != null) {
                    ct = make.asReplacementOf(ct, replacedByCase, true);
                }
                cases.add(ct);

                return false;
            }
            
            for (Iterator<TreePathHandle> it = desc.literals.iterator(); it.hasNext(); ) {
                TreePathHandle tph = it.next();
                TreePath lit = tph.resolve(copy);

                if (lit == null) {
                    //XXX: log
                    return true;
                }
                Tree ifSt = findExpressionParentIf(lit);
                if (ifSt != null && !ifSeen.add(ifSt)) {
                    ifSt = null;
                }

                List<StatementTree> body = it.hasNext() ? Collections.<StatementTree>emptyList() : statements;
                // special case: if the literal is an enum-type, use only the simple name.
                ExpressionTree litTree = (ExpressionTree)lit.getLeaf();
                TypeMirror m = copy.getTrees().getTypeMirror(lit);
                if (m == null || m.getKind() == TypeKind.NULL) {
                    // do not add case
                    continue;
                }
                if (enumType) {
                    Element c = copy.getTrees().getElement(lit);
                    if (c != null && c.getKind() == ElementKind.ENUM_CONSTANT) {
                        litTree = make.Identifier(((VariableElement)c).getSimpleName());
                    }
                }
                CaseTree nc = make.Case(litTree, body);
                if (ifSt != null) {
                    nc = make.asReplacementOf(nc, ifSt, true);
                }
                cases.add(nc);
            }
                
            return false;
        }

        private Set<Name> declaredVariables(TreePath where) {
            Set<Name> result = new HashSet<Name>();
            Iterable<? extends Tree> statements;

            if (where.getLeaf().getKind() == Tree.Kind.BLOCK) {
                statements = ((BlockTree) where.getLeaf()).getStatements();
            } else {
                statements = Collections.singletonList(where.getLeaf());
            }

            for (Tree t : statements) {
                if (t.getKind() == Tree.Kind.VARIABLE) {
                    result.add(((VariableTree) t).getName());
                }
            }

            return result;
        }

        private Set<Name> usedVariables(final CompilationInfo info, TreePath where, final Map<BreakTree, StatementTree> break2Target) {
            final Set<Name> result = new HashSet<Name>();
            final Set<Element> declared = new HashSet<Element>();
            final Set<Tree> above = new HashSet<Tree>();

            for (Tree t : where) {
                above.add(t);
            }

            new ErrorAwareTreePathScanner<Void, Void>() {
                @Override public Void visitIdentifier(IdentifierTree node, Void p) {
                    if (declared.contains(info.getTrees().getElement(getCurrentPath())))
                        return null;
                    result.add(node.getName());
                    return super.visitIdentifier(node, p);
                }
                @Override public Void visitVariable(VariableTree node, Void p) {
                    declared.add(info.getTrees().getElement(getCurrentPath()));
                    return super.visitVariable(node, p);
                }
                @Override
                public Void visitBreak(BreakTree node, Void p) {
                    if (node.getLabel() == null) {
                        StatementTree target = info.getTreeUtilities().getBreakContinueTarget(getCurrentPath());

                        if (above.contains(target)) {
                            break2Target.put(node, target);
                        }
                    }
                    return super.visitBreak(node, p);
                }
            }.scan(where, null);

            return result;
        }
    }
}
