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
/*
 * Contributor(s): Alexandru Gyori <Alexandru.Gyori at gmail.com>
 */
package org.netbeans.modules.java.hints.jdk.mapreduce;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.EnumSet;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author alexandrugyori
 */
 class ProspectiveOperation {

    private static final String UNKNOWN_NAME = "_item";

    private static boolean isLong(ExpressionTree reducing, WorkingCopy workingCopy) {
        return isType(reducing, workingCopy, "java.lang.Long");
    }

    private static boolean isChar(ExpressionTree reducing, WorkingCopy workingCopy) {
        return isType(reducing, workingCopy, "java.lang.Character");
    }

    private static List<ProspectiveOperation> createProspectiveReducer(StatementTree tree, WorkingCopy workingCopy, OperationType operationType, PreconditionsChecker precond, List<ProspectiveOperation> ls) throws IllegalStateException {
        ExpressionTree expr = ((ExpressionStatementTree) tree).getExpression();
        TreeMaker tm = workingCopy.getTreeMaker();
        ProspectiveOperation redOp = null;
        Tree.Kind opKind = expr.getKind();
        if (TreeUtilities.isCompoundAssignementAssignement(opKind)) {
            redOp = handleCompoundAssignementReducer(tm, expr, operationType, precond, workingCopy, ls, redOp);
        } else if (TreeUtilities.isPreOrPostfixOp(opKind)) {
            redOp = handlePreOrPostFixReducer(expr, workingCopy, tm, operationType, precond, ls, redOp);
        }
        ls.add(redOp);
        return ls;
    }

    private static ProspectiveOperation handleCompoundAssignementReducer(TreeMaker tm, ExpressionTree expr, OperationType operationType, PreconditionsChecker precond, WorkingCopy workingCopy, List<ProspectiveOperation> ls, ProspectiveOperation redOp) {
        //this variable will be removed at a later stage.
        VariableTree var = tm.Variable(tm.Modifiers(EnumSet.noneOf(Modifier.class)), "dummyVar18912", tm.Type("Object"), ((CompoundAssignmentTree) expr).getExpression());
        ProspectiveOperation map = new ProspectiveOperation(var, operationType.MAP, precond.getInnerVariables(), workingCopy, precond.getVarToName());
        map.getAvailableVariables().add(var.getName());
        ls.add(map);
        redOp = new ProspectiveOperation(expr, operationType, precond.getInnerVariables(), workingCopy, precond.getVarToName());
        redOp.neededVariables = new HashSet<Name>();
        redOp.neededVariables.add(var.getName());
        redOp.reducingVariable = ((CompoundAssignmentTree) expr).getVariable();
        return redOp;
    }

    private static ProspectiveOperation handlePreOrPostFixReducer(ExpressionTree expr, WorkingCopy workingCopy, TreeMaker tm, OperationType operationType, PreconditionsChecker precond, List<ProspectiveOperation> ls, ProspectiveOperation redOp) {
        ExpressionTree reducing = ((UnaryTree) expr).getExpression();
        ProspectiveOperation map;
        if (isInteger(reducing, workingCopy) || isLong(reducing, workingCopy) || isChar(reducing, workingCopy)) {
            map = new ProspectiveOperation(tm.Literal(1), operationType.MAP, precond.getInnerVariables(), workingCopy, precond.getVarToName());
        } else {
            map = new ProspectiveOperation(tm.Literal(1.), operationType.MAP, precond.getInnerVariables(), workingCopy, precond.getVarToName());
        }
        ls.add(map);
        redOp = new ProspectiveOperation(expr, operationType, precond.getInnerVariables(), workingCopy, precond.getVarToName());
        redOp.reducingVariable = reducing;
        return redOp;
    }
    private boolean isUnmodifiable;

    private Tree blockify( StatementTree correspondingTree) {
        return treeMaker.Block(Arrays.asList(correspondingTree), false);
    }

    public Boolean isLazy() {
        return this.opType == OperationType.MAP || this.opType == OperationType.FILTER;
    }

    private Boolean isMergeable() {
        return this.opType == OperationType.FOREACH
                || this.opType == OperationType.MAP
                || this.opType == OperationType.FILTER;
    }

    boolean shouldReturn() {
        return this.opType == OperationType.ANYMATCH || this.opType == OperationType.NONEMATCH;
    }

    boolean shouldAssign() {
        return this.opType == OperationType.REDUCE;
    }

    private Set<Name> buildAvailables(PreconditionsChecker.VariablesVisitor treeVariableVisitor) {
        Set<Name> allVariablesUsedInCurrentOp = treeVariableVisitor.getAllLocalVariablesUsed();
        Set<Name> allVariablesDeclaredInCurrentOp = treeVariableVisitor.getInnervariables();
        allVariablesUsedInCurrentOp.addAll(allVariablesDeclaredInCurrentOp);
        return allVariablesUsedInCurrentOp;
    }

    private Set<Name> buildNeeded(PreconditionsChecker.VariablesVisitor treeVariableVisitor) {
        Set<Name> allVariablesUsedInCurrentOp = treeVariableVisitor.getAllLocalVariablesUsed();
        //Remove the ones also declared in the current block.
        allVariablesUsedInCurrentOp.removeAll(treeVariableVisitor.getInnervariables());
        //Keeps the ones that are local to the loop. These are the ones that need to be passed around
        //in a pipe-like fashion.
        allVariablesUsedInCurrentOp.retainAll(this.innerLoopVariables);
        return allVariablesUsedInCurrentOp;
    }

    private StatementTree castToStatementTree(Tree currentTree) {
        if (currentTree instanceof StatementTree) {
            return (StatementTree) currentTree;
        } else {
            return this.treeMaker.ExpressionStatement((ExpressionTree) currentTree);
        }
    }

    private Tree.Kind getSuitableOperator(Tree.Kind kind) {
        if (Tree.Kind.AND_ASSIGNMENT == kind) {
            return Tree.Kind.AND;
        }
        if (Tree.Kind.OR_ASSIGNMENT == kind) {
            return Tree.Kind.OR;
        }
        if (Tree.Kind.PLUS_ASSIGNMENT == kind) {
            return Tree.Kind.PLUS;
        }
        if (Tree.Kind.MINUS_ASSIGNMENT == kind) {
            return Tree.Kind.MINUS;
        }
        if (Tree.Kind.DIVIDE_ASSIGNMENT == kind) {
            return Tree.Kind.DIVIDE;
        }
        if (Tree.Kind.MULTIPLY_ASSIGNMENT == kind) {
            return Tree.Kind.MULTIPLY;
        }
        if (Tree.Kind.REMAINDER_ASSIGNMENT == kind) {
            return Tree.Kind.REMAINDER;
        }
        if (Tree.Kind.LEFT_SHIFT_ASSIGNMENT == kind) {
            return Tree.Kind.LEFT_SHIFT;
        }
        if (Tree.Kind.RIGHT_SHIFT_ASSIGNMENT == kind) {
            return Tree.Kind.RIGHT_SHIFT;
        }
        if (Tree.Kind.UNSIGNED_RIGHT_SHIFT_ASSIGNMENT == kind) {
            return Tree.Kind.UNSIGNED_RIGHT_SHIFT;
        }
        return null;
    }

    private boolean isString(ExpressionTree reducingVariable) {
        TypeMirror tm = this.workingCopy.getTrees().getTypeMirror(TreePath.getPath(this.workingCopy.getCompilationUnit(), this.reducingVariable));
        return tm != null && tm.toString().equals("java.lang.String");
    }

    private static boolean isInteger(ExpressionTree reducingVariable, CompilationInfo workingCopy) {
        return isType(reducingVariable, workingCopy, "java.lang.Integer");
    }

    private static boolean isType(ExpressionTree reducingVariable, CompilationInfo workingCopy, String fqn) {
        TypeMirror tm = workingCopy.getTrees().getTypeMirror(TreePath.getPath(workingCopy.getCompilationUnit(), reducingVariable));
        TypeElement typeEl = workingCopy.getElements().getTypeElement(fqn);
        if (typeEl != null) {
            TypeMirror integer = typeEl.asType();

            if (tm != null && workingCopy.getTypeUtilities().isCastable(tm, integer)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNumericLiteral(Tree currentTree) {
        Tree.Kind kind = currentTree.getKind();
        return kind == Tree.Kind.INT_LITERAL
                || kind == Tree.Kind.CHAR_LITERAL
                || kind == Tree.Kind.DOUBLE_LITERAL
                || kind == Tree.Kind.FLOAT_LITERAL
                || kind == Tree.Kind.LONG_LITERAL;
    }

    private void beautifyBlock(Tree currentTree, Set<Name> needed) {
        BlockTree currentBlock = (BlockTree) currentTree;
        if (currentBlock.getStatements().size() == 1) {
            this.correspondingTree = currentBlock.getStatements().get(0);
            this.beautify(needed);
        } else {
            this.correspondingTree = this.addReturn(currentBlock, getOneFromSet(needed));
        }
    }

    private void beautifyVariable(Tree currentTree, Set<Name> needed) {
        VariableTree varTree = (VariableTree) currentTree;
        if (needed.contains(varTree.getName())) {
            this.correspondingTree = varTree.getInitializer() != null
                    ? treeMaker.ExpressionStatement(varTree.getInitializer())
                    : null;
        } else {

            this.correspondingTree = this.addReturn(castToStatementTree(currentTree), getOneFromSet(needed));
        }
    }

    private void beautifyAssignement(Tree currentTree, Set<Name> needed) {
        AssignmentTree assigned = (AssignmentTree) ((ExpressionStatementTree) currentTree).getExpression();
        ExpressionTree variable = assigned.getVariable();
        if (variable.getKind() == Tree.Kind.IDENTIFIER) {
            IdentifierTree id = (IdentifierTree) variable;

            if (needed.contains(id.getName())) {
                this.correspondingTree = treeMaker.ExpressionStatement(assigned.getExpression());
            } else {

                this.correspondingTree = this.addReturn(castToStatementTree(currentTree), getOneFromSet(needed));
            }
        } else {
            this.correspondingTree = this.addReturn(castToStatementTree(currentTree), getOneFromSet(needed));
        }
    }

    private Tree getLambdaForMap() {
        Tree lambdaBody;
        if (isNumericLiteral(this.correspondingTree)) {
            lambdaBody = this.correspondingTree;
        } else {
            lambdaBody = ((ExpressionStatementTree) this.correspondingTree).getExpression();
        }
        return lambdaBody;
    }

    private List<ExpressionTree> getArgumentsForReducer() {
        VariableTree var;
        ExpressionTree lambda;
        Tree lambdaBody;
        Tree.Kind opKind = this.correspondingTree.getKind();
        List<ExpressionTree> args = new ArrayList<ExpressionTree>();
        args.add(this.reducingVariable);
        if (TreeUtilities.isPreOrPostfixOp(opKind)) {
            Tree type = null;//treeMaker.Type("Integer");
            var = this.treeMaker.Variable(treeMaker.Modifiers(EnumSet.noneOf(Modifier.class)), "accumulator", null, null);
            VariableTree var1 = makeUnknownVariable();
            if (opKind == Tree.Kind.POSTFIX_INCREMENT || opKind == Tree.Kind.PREFIX_INCREMENT) {
                if (isInteger(this.reducingVariable, workingCopy)) {
                    lambda = makeIntegerSumReducer();
                } else {
                    lambdaBody = this.treeMaker.Binary(Tree.Kind.PLUS, this.treeMaker.Identifier("accumulator"), this.treeMaker.Literal(1));
                    lambda = treeMaker.LambdaExpression(Arrays.asList(var, var1), lambdaBody);
                }

            } else //if (opKind == Tree.Kind.POSTFIX_DECREMENT || opKind == Tree.Kind.PREFIX_DECREMENT) {
            {
                lambdaBody = this.treeMaker.Binary(Tree.Kind.MINUS, this.treeMaker.Identifier("accumulator"), this.treeMaker.Literal(1));
                lambda = treeMaker.LambdaExpression(Arrays.asList(var, var1), lambdaBody);
            }

            args.add(lambda);


        } else if (TreeUtilities.isCompoundAssignementAssignement(opKind)) {
            Tree type = null;//treeMaker.Type("Integer");

            var = this.treeMaker.Variable(treeMaker.Modifiers(EnumSet.noneOf(Modifier.class)), "accumulator", null, null);
            VariableTree var1 = makeUnknownVariable();
            if (opKind == Tree.Kind.PLUS_ASSIGNMENT) {
                if (isString(this.reducingVariable)) {
                    lambda = makeStringConcatReducer();
                } else {
                    if (isInteger(this.reducingVariable, workingCopy)) {
                        lambda = makeIntegerSumReducer();
                    } else {
                        lambda = makeSimpleExplicitReducer(opKind, var, var1);
                    }
                }
            } else //if (opKind == Tree.Kind.MINUS_ASSIGNEMENT  ||  any other compound op) {
            {
                lambda = makeSimpleExplicitReducer(opKind, var, var1);
            }

            args.add(lambda);
            return args;
        } else {
            return null;
        }
        return args;
    }

    private VariableTree getLambdaArguments() {
        VariableTree var;
        if (this.getNeededVariables().isEmpty()) {
            var = makeUnknownVariable();
        } else {
            Name varName = getOneFromSet(this.neededVariables);
            //If types need to be made explicit the null should be replaced with the commented expression
            Tree type = null;// treeMaker.Type(this.varToType.get(varName).toString());
            var = this.treeMaker.Variable(treeMaker.Modifiers(EnumSet.noneOf(Modifier.class)), varName.toString(), type, null);
        }
        return var;
    }

    private ExpressionTree makeSimpleExplicitReducer(Tree.Kind opKind, VariableTree var, VariableTree var1) {
        Tree lambdaBody;
        ExpressionTree lambda;
        lambdaBody = this.treeMaker.Binary(this.getSuitableOperator(opKind), this.treeMaker.Identifier("accumulator"), this.treeMaker.Identifier(UNKNOWN_NAME));
        lambda = treeMaker.LambdaExpression(Arrays.asList(var, var1), lambdaBody);
        return lambda;
    }

    private MemberReferenceTree makeIntegerSumReducer() {
        return this.treeMaker.MemberReference(MemberReferenceTree.ReferenceMode.INVOKE, this.treeMaker.Identifier("Integer"), "sum", new ArrayList<ExpressionTree>());
    }

    private MemberReferenceTree makeStringConcatReducer() {
        return this.treeMaker.MemberReference(MemberReferenceTree.ReferenceMode.INVOKE, this.treeMaker.Identifier("String"), "concat", new ArrayList<ExpressionTree>());
    }

    private VariableTree makeUnknownVariable() {
        return this.treeMaker.Variable(treeMaker.Modifiers(EnumSet.noneOf(Modifier.class)), UNKNOWN_NAME, null, null);
    }

    public static enum OperationType {

        MAP, FOREACH, FILTER, REDUCE, ANYMATCH, NONEMATCH
    }
    private OperationType opType;
    private Tree correspondingTree;
    private final Set<Name> innerLoopVariables;
    private final TreeMaker treeMaker;
    private final CompilationInfo workingCopy;
    private final Map<Name, String> varToType;
    private ExpressionTree reducingVariable;

    private ProspectiveOperation(Tree tree, OperationType operationType, Set<Name> innerLoopVariables, WorkingCopy workingCopy, Map<Name, String> varToType) {
        this.opType = operationType;
        this.correspondingTree = tree;
        this.innerLoopVariables = innerLoopVariables;
        this.treeMaker = workingCopy.getTreeMaker();
        this.workingCopy = workingCopy;
        this.varToType = varToType;
    }

    //Creates a non-eager operation according to the tree type
    public static List<ProspectiveOperation> createOperator(StatementTree tree,
            OperationType operationType, PreconditionsChecker precond, WorkingCopy workingCopy) {
        List<ProspectiveOperation> ls = new ArrayList<ProspectiveOperation>();
        if (OperationType.REDUCE == operationType) {
            return createProspectiveReducer(tree, workingCopy, operationType, precond, ls);
        } else {
            ProspectiveOperation operation = new ProspectiveOperation(tree, operationType, precond.getInnerVariables(), workingCopy, precond.getVarToName());
            operation.getNeededVariables();
            ls.add(operation);
            return ls;
        }
    }

    public static List<ProspectiveOperation> mergeIntoComposableOperations(List<ProspectiveOperation> ls) {
        List<ProspectiveOperation> result = mergeRecursivellyIntoComposableOperations(ls);
        if (result == null || result.contains(null)) {
            return null;
        } else {
            return result;
        }
    }

    private static List<ProspectiveOperation> mergeRecursivellyIntoComposableOperations(List<ProspectiveOperation> ls) {
        for ( int i = ls.size() - 1; i > 0; i--) {
            ProspectiveOperation current = ls.get(i);
            ProspectiveOperation prev = ls.get(i - 1);
            if (!(areComposable(current, prev))) {
                if (!current.isMergeable() || !prev.isMergeable()) {
                    return null;
                }
                if (current.opType == OperationType.FILTER || prev.opType == OperationType.FILTER) {
                    int lengthOfLs;
                    ProspectiveOperation last;
                    ProspectiveOperation nlast;
                    while ((lengthOfLs = ls.size()) > i) {
                        last = ls.get(lengthOfLs - 1);
                        nlast = ls.get(lengthOfLs - 2);
                        ls.remove(lengthOfLs - 1);
                        //method mutates in place, no need to remove and add again.
                        nlast.merge(last);
                    }
                } else {
                    prev.merge(current);
                    ls.remove(i);
                }
            }
        }
        beautify(ls);
        return ls;
    }

    private static void beautify(List<ProspectiveOperation> ls) {
        for ( int i = ls.size() - 1; i > 0; i--) {
            ProspectiveOperation current = ls.get(i - 1);
            ProspectiveOperation next = ls.get(i);
            Set<Name> needed = next.getNeededVariables();
            current.beautify(needed);
        }
        for (Iterator<ProspectiveOperation> it = ls.iterator(); it.hasNext();) {
            if (it.next().correspondingTree == null)
                it.remove();
        }
    }

    public Tree getCorrespondingTree() {
        return this.correspondingTree;
    }

    public void eagerize() {
        if (this.opType == OperationType.MAP) {
            this.opType = OperationType.FOREACH;
        }
    }

    private void beautify(Set<Name> needed) {
        if (this.opType == OperationType.MAP) {
            beautifyLazy(needed);
        }
    }

    private void beautifyLazy(Set<Name> needed) {
        if (needed.isEmpty()) {
            {
                if (!this.getNeededVariables().isEmpty()) {
                    this.beautify(this.getNeededVariables());
                } else {
                    Set<Name> newSet = new HashSet<Name>();
                    newSet.add(null);
                    beautifyLazy(newSet);
                }
            }
        } else {
            Tree currentTree = this.correspondingTree;
            if (currentTree.getKind() == Tree.Kind.BLOCK) {
                beautifyBlock(currentTree, needed);
            } else if (currentTree.getKind() == Tree.Kind.VARIABLE) {
                beautifyVariable(currentTree, needed);
            } else if (currentTree.getKind() == Tree.Kind.EXPRESSION_STATEMENT
                    && ((ExpressionStatementTree) currentTree).getExpression().getKind() == Tree.Kind.ASSIGNMENT) {
                beautifyAssignement(currentTree, needed);
            } else if (isNumericLiteral(currentTree)) {
                //do nothing
            } else {
                this.correspondingTree = this.addReturn(castToStatementTree(currentTree), getOneFromSet(needed));
            }
        }
    }

    private BlockTree addReturn(StatementTree statement, Name varName) {
        List<StatementTree> ls = new ArrayList<StatementTree>();
        if (statement.getKind() == Tree.Kind.BLOCK) {
            ls.addAll(((BlockTree) statement).getStatements());
        } else {
            ls.add(statement);
        }
        if (varName != null) {
            ls.add(this.treeMaker.Return(treeMaker.Identifier(varName.toString())));
        } else {
            ls.add(this.treeMaker.Return(treeMaker.Identifier(UNKNOWN_NAME)));
        }
        return treeMaker.Block(ls, false);
    }

    String getSuitableMethod() {
        if (this.opType == OperationType.FOREACH) {
            return "forEachOrdered";
        } else if (this.opType == OperationType.MAP) {
            return "map";
        } else if (this.opType == OperationType.FILTER) {
            return "filter";
        } else if (this.opType == OperationType.ANYMATCH) {
            return "anyMatch";
        } else if (this.opType == OperationType.NONEMATCH) {
            return "noneMatch";
        } else //if (this.opType == OperationType.REDUCE) 
        {
            return "reduce";
        }
    }

    List<ExpressionTree> getArguments() {
        VariableTree var;
        ExpressionTree lambda;
        Tree lambdaBody;
        if (this.correspondingTree.getKind() == Tree.Kind.BLOCK) {
            lambdaBody = this.correspondingTree;
        } else {
            if (this.opType == OperationType.FILTER || this.opType == OperationType.ANYMATCH || this.opType == OperationType.NONEMATCH) {
                lambdaBody = ((IfTree) this.correspondingTree).getCondition();
            } else if (this.opType == OperationType.MAP) {
                lambdaBody = getLambdaForMap();
            } else if (this.opType == OperationType.FOREACH) {
                lambdaBody = blockify(castToStatementTree(this.correspondingTree));
            } else //if(this.opType== OperationType.REDUCE)
            {
                return getArgumentsForReducer();
            }
        }
        var = getLambdaArguments();
        lambda = treeMaker.LambdaExpression(Arrays.asList(var), lambdaBody);
        List<ExpressionTree> args = new ArrayList<ExpressionTree>();

        args.add(lambda);
        return args;
    }

    private Name getOneFromSet(Set<Name> needed) {
        return needed.iterator().next();
    }

    public void merge(ProspectiveOperation op) {
        if (this.opType == OperationType.FILTER) {
            this.opType = op.opType;
            IfTree ifTree = this.treeMaker.If(((IfTree) this.correspondingTree).getCondition(), (StatementTree) op.correspondingTree, null);
            this.correspondingTree = ifTree;
        } else {
            this.opType = op.opType;
            List<StatementTree> statements = new ArrayList<StatementTree>();

            if (this.correspondingTree.getKind() == Tree.Kind.BLOCK) {
                statements.addAll(((BlockTree) this.correspondingTree).getStatements());
            } else {
                statements.add(castToStatementTree(this.correspondingTree));
            }

            if (op.correspondingTree.getKind() == Tree.Kind.BLOCK) {
                statements.addAll(((BlockTree) op.correspondingTree).getStatements());
            } else {
                statements.add(castToStatementTree(op.correspondingTree));
            }
            HashSet<Name> futureAvailable = new HashSet<Name>();
            HashSet<Name> futureNeeded = new HashSet<Name>();

            futureAvailable.addAll(this.getAvailableVariables());
            futureAvailable.addAll(op.getAvailableVariables());

            futureNeeded.addAll(op.getNeededVariables());
            futureNeeded.removeAll(this.getAvailableVariables());
            futureNeeded.addAll(this.getNeededVariables());

            this.neededVariables = futureNeeded;
            this.availableVariables = futureAvailable;
            this.correspondingTree = this.treeMaker.Block(statements, false);
        }
    }

    private static boolean areComposable(ProspectiveOperation current, ProspectiveOperation prev) {
        Set<Name> needed = current.getNeededVariables();
        return needed.size() <= 1 && prev.areAvailableVariables(needed);
    }

    private Set<Name> getAvailableVariables() {
        if (this.availableVariables == null) {
            PreconditionsChecker.VariablesVisitor treeVariableVisitor = new PreconditionsChecker.VariablesVisitor(new TreePath(this.workingCopy.getCompilationUnit()));
            if (this.correspondingTree.getKind() == Tree.Kind.VARIABLE) {
                treeVariableVisitor.scan(((VariableTree) correspondingTree).getInitializer(), this.workingCopy.getTrees());
                this.availableVariables = buildAvailables(treeVariableVisitor);
                this.availableVariables.add(((VariableTree) correspondingTree).getName());
            } else {
                treeVariableVisitor.scan(correspondingTree, this.workingCopy.getTrees());
                this.availableVariables = buildAvailables(treeVariableVisitor);
            }
        }
        //If the operation is a filter, then it only makes available what it gets
        //if needed is empty, it can pull anything needed from upstream.
        if (this.opType == OperationType.FILTER) {
            return this.getNeededVariables();
        }
        return this.availableVariables;
    }
    Set<Name> neededVariables;

    public String getTypeForVar(Name varName) {
        return this.varToType.get(varName);
    }

    public Set<Name> getNeededVariables() {
        if (neededVariables == null) {
            if (this.opType == OperationType.REDUCE) {
                return new HashSet<Name>();
            }
            PreconditionsChecker.VariablesVisitor treeVariableVisitor = new PreconditionsChecker.VariablesVisitor(new TreePath(this.workingCopy.getCompilationUnit()));
            if (this.correspondingTree.getKind() == Tree.Kind.VARIABLE) {
                treeVariableVisitor.scan(((VariableTree) correspondingTree).getInitializer(), this.workingCopy.getTrees());
            } else {
                treeVariableVisitor.scan(correspondingTree, this.workingCopy.getTrees());
            }
            this.neededVariables = buildNeeded(treeVariableVisitor);
        }
        return this.neededVariables;
    }
    Set<Name> availableVariables;

    public Boolean areAvailableVariables(Set<Name> needed) {
        Set<Name> available = this.getAvailableVariables();
        //If the prospective operations does not need any variables from upstream
        //(available is a superset of needeld so the test is sound - the available set includes all the uses)
        //(because, for example, it uses fields or other variables that remain in scope even after refactoring)        
        if (available.isEmpty()) {
            //then the needed variables propagate from the downstream operation in order to facillitate chaining.
            //(both to the needed and available sets).
            available.addAll(needed);
            this.getNeededVariables().addAll(needed);
            return true;
        }
        return available.containsAll(needed);
    }
    
    public boolean isForeach() {
        return opType == OperationType.FOREACH;
    }
}
