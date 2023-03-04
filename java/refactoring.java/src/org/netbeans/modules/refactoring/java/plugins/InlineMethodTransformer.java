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
package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.*;
import static com.sun.source.tree.Tree.Kind.*;
import com.sun.source.util.*;
import com.sun.tools.javac.tree.JCTree;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import static org.netbeans.modules.refactoring.java.plugins.Bundle.*;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;

/**
 *
 * @author Ralph Ruijs
 */
public class InlineMethodTransformer extends RefactoringVisitor {
    
    private static final EnumSet<Tree.Kind> LITERALS = EnumSet.of(LAMBDA_EXPRESSION, PRIMITIVE_TYPE, PREFIX_INCREMENT, PREFIX_DECREMENT, BITWISE_COMPLEMENT, LEFT_SHIFT, RIGHT_SHIFT, UNSIGNED_RIGHT_SHIFT, MULTIPLY_ASSIGNMENT, DIVIDE_ASSIGNMENT, REMAINDER_ASSIGNMENT, PLUS_ASSIGNMENT, MINUS_ASSIGNMENT, LEFT_SHIFT_ASSIGNMENT, RIGHT_SHIFT_ASSIGNMENT, UNSIGNED_RIGHT_SHIFT_ASSIGNMENT, AND_ASSIGNMENT, XOR_ASSIGNMENT, OR_ASSIGNMENT, INT_LITERAL, LONG_LITERAL, FLOAT_LITERAL, DOUBLE_LITERAL, BOOLEAN_LITERAL, CHAR_LITERAL, STRING_LITERAL, NULL_LITERAL, ERRONEOUS);
    private Trees trees;
    private MethodTree methodTree;
    private boolean hasParameters;
    private Problem problem;
    private HashMap<Tree, Tree> original2Translated;
    private final Deque<Map<Tree, List<StatementTree>>> newStatsQueue;
    private final Deque<Map<Tree, Tree>> translateQueue;
    private final LinkedList<String> definedIds;
    private final HashSet<Element> changedParamters;
    private final TreePathHandle tph;

    public InlineMethodTransformer(TreePathHandle tph) {
        this.tph = tph;
        newStatsQueue = new LinkedList<>();
        translateQueue = new LinkedList<>();
        definedIds = new LinkedList<>();
        changedParamters = new HashSet<>();
    }

    @Override
    public void setWorkingCopy(WorkingCopy workingCopy) throws ToPhaseException {
        SourceUtils.forceSource(workingCopy, tph.getFileObject());
        super.setWorkingCopy(workingCopy);
    }

    public Problem getProblem() {
        return problem;
    }

    @Override
    public Tree visitCompilationUnit(CompilationUnitTree node, Element p) {
        try {
            GeneratorUtilities genUtils = GeneratorUtilities.get(workingCopy);
            genUtils.importComments(node, node);
            trees = workingCopy.getTrees();
            methodTree = (MethodTree) trees.getTree(p);
            hasParameters = methodTree.getParameters().size() > 0;
            return super.visitCompilationUnit(node, p);
        } finally {
            trees = null;
            methodTree = null;
            hasParameters = false;
        }
    }

    @Override
    public Tree visitClass(ClassTree node, Element p) {
        final TreePath classPath = getCurrentPath();
        ClassTree classTree = node;
        for (Tree member : classTree.getMembers()) {
            TreePath memberPath = new TreePath(classPath, member);
            Element element = workingCopy.getTrees().getElement(memberPath);
            if (p.equals(element)) {
                classTree = make.removeClassMember(classTree, member);
                break;
            }
        }
        if (classPath.getParentPath().getLeaf().getKind() != Tree.Kind.COMPILATION_UNIT) {
            original2Translated.put(node, classTree);
            return super.visitClass(node, p);
        }

        original2Translated = new HashMap<>();
        Tree value = super.visitClass(node, p);

        classTree = (ClassTree) workingCopy.getTreeUtilities().translate(classTree, original2Translated);
        rewrite(node, classTree);
        return value;
    }

    @Override
    public Tree visitBlock(BlockTree node, Element p) {
        newStatsQueue.add(new HashMap<Tree, List<StatementTree>>());
        translateQueue.add(new HashMap<Tree, Tree>());
        int nrOfIds = definedIds.size();
        Tree value = super.visitBlock(node, p);
        while(definedIds.size() > nrOfIds) {
            definedIds.pop();
        }
        Map<Tree, List<StatementTree>> newStatementsForBlock = newStatsQueue.pollLast();
        Map<Tree, Tree> original2TranslatedForBlock = translateQueue.pollLast();

        if (!newStatementsForBlock.isEmpty()) {
            List<StatementTree> newStatementList = new LinkedList<>();
            for (StatementTree statementTree : node.getStatements()) {
                List<StatementTree> stats = newStatementsForBlock.get(statementTree);
                if (stats != null) {
                    newStatementList.addAll(stats);
                }
                Tree tree = original2TranslatedForBlock.get(statementTree);
                if(tree == null || tree.getKind() != EMPTY_STATEMENT ||
                        ((JCTree)tree).pos >= 0) {
                    newStatementList.add(statementTree);
                }
            }
            BlockTree newBlock = make.Block(newStatementList, node.isStatic());
            if(!original2TranslatedForBlock.isEmpty()) {
                newBlock = (BlockTree) workingCopy.getTreeUtilities().translate(newBlock, original2TranslatedForBlock);
            }
            original2Translated.put(node, newBlock);
        }
        return value;
    }

    @Override
    public Tree visitMethod(MethodTree node, Element p) {
        if (workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            return node;
        }
        return super.visitMethod(node, p);
    }

    @Override
    public Tree visitMemberReference(MemberReferenceTree node, Element methodElement) {
        final TreePath methodInvocationPath = getCurrentPath();
        Element el = trees.getElement(methodInvocationPath);
//        TypeMirror tm = workingCopy.getTrees().getTypeMirror(getCurrentPath());
        if (el != null && el.getKind() == ElementKind.METHOD && methodElement.equals(el)) {
            List<? extends VariableTree> parameters = methodTree.getParameters();
            BlockTree body = methodTree.getBody();

            final HashMap<Tree, Tree> original2TranslatedBody = new HashMap<>();
            scanForNameClash(methodInvocationPath, body, methodElement, original2TranslatedBody);
            if (problem != null && problem.isFatal()) {
                return node;
            }
            body = (BlockTree) workingCopy.getTreeUtilities().translate(body, original2TranslatedBody);
            LambdaExpressionTree lambda = make.LambdaExpression(parameters, body);
            rewrite(node, lambda);
        }
        return super.visitMemberReference(node, methodElement);
    }

    @Override
    @NbBundle.Messages({
        "ERR_InlineMethodMultipleReturn=Cannot inline method with multiple return statements at {0}.",
        "WRN_InlineMethodMultipleLines=The method body has multiple statements, will not update reference at {0}. The sources will not compile after refactoring!",
        "ERR_InlineMethodNoLastReturn=The last statement is not a return statement, cannot inline at {0}."})
    public Tree visitMethodInvocation(MethodInvocationTree node, Element methodElement) {
        Tree value = super.visitMethodInvocation(node, methodElement);
        final TreePath methodInvocationPath = getCurrentPath();
        Element el = trees.getElement(methodInvocationPath);
        if (el != null && el.getKind() == ElementKind.METHOD && methodElement.equals(el)) {
            GeneratorUtilities genUtils = GeneratorUtilities.get(workingCopy);
            ExecutableElement method = (ExecutableElement) el;
            List<StatementTree> newStatementList = new LinkedList<>();

            final TypeElement bodyEnclosingTypeElement = workingCopy.getElementUtilities().enclosingTypeElement(methodElement);
            TreePath findEnclosingClass = JavaRefactoringUtils.findEnclosingClass(workingCopy, methodInvocationPath, true, true, true, true, true);
            final Element invocationEnclosingTypeElement = workingCopy.getTrees().getElement(findEnclosingClass);

            boolean inSameClass = bodyEnclosingTypeElement.equals(invocationEnclosingTypeElement);
            boolean inStatic = !methodElement.getModifiers().contains(Modifier.STATIC) && isInStaticContext(methodInvocationPath);
            boolean invokeOnInstance = false;
            MethodInvocationTree invTree = (MethodInvocationTree)methodInvocationPath.getLeaf();
            if (!methodElement.getModifiers().contains(Modifier.STATIC) && invTree.getMethodSelect().getKind() == Tree.Kind.MEMBER_SELECT) {
                invokeOnInstance = true;
                MemberSelectTree mst = (MemberSelectTree)invTree.getMethodSelect();
                if (mst.getExpression().getKind() == Tree.Kind.IDENTIFIER) {
                    // check for this.method(). super. qualifier should be omitted if and only if the symbols is not shadowed in subclass.
                    Name n = ((IdentifierTree)mst.getExpression()).getName();
                    invokeOnInstance = !(n.contentEquals("this")); // NOI18N
                }
            }

            TreePath statementPath = findCorrespondingStatement(methodInvocationPath);
            StatementTree statementTree = (StatementTree) statementPath.getLeaf();

            BlockTree body = methodTree.getBody();

            final HashMap<Tree, Tree> original2TranslatedBody = new HashMap<>();
            scanForNameClash(methodInvocationPath, body, methodElement, original2TranslatedBody);
            if (problem != null && problem.isFatal()) {
                return node;
            }
            TreeUtilities treeUtilities = workingCopy.getTreeUtilities();
            body = (BlockTree) treeUtilities.translate(body, original2TranslatedBody);
            
            final Boolean[] multiplereturn = {null};
            new ErrorAwareTreeScanner<Void, Void>() {

                @Override
                public Void scan(Tree node, Void p) {
                    if(Boolean.TRUE == multiplereturn[0]) {
                        return null;
                    }
                    return super.scan(node, p);
                }
                
                @Override
                public Void visitNewClass(NewClassTree node, Void p) {
                    return null; //not interested in return from innerclass
                }
                
                @Override
                public Void visitReturn(ReturnTree node, Void p) {
                    multiplereturn[0] = multiplereturn[0] == null ? Boolean.FALSE : Boolean.TRUE;
                    return super.visitReturn(node, p);
                }
            }.scan(body, null);
            
            if (hasParameters) {
                final HashMap<Tree, Tree> original2TranslatedBody2 = new HashMap<>();
                replaceParametersWithArguments(original2TranslatedBody2, method, node, methodInvocationPath, body, newStatementList);
                body = (BlockTree) treeUtilities.translate(body, original2TranslatedBody2);
            }

            TreePath methodPath = trees.getPath(methodElement);
            TreePath bodyPath = new TreePath(methodPath, methodTree.getBody());
            Scope scope = workingCopy.getTrees().getScope(methodInvocationPath);

            ExpressionTree methodSelect = node.getMethodSelect();
            if (methodSelect.getKind() == Tree.Kind.MEMBER_SELECT) {
                methodSelect = ((MemberSelectTree) methodSelect).getExpression();
            } else {
                methodSelect = null;
            }

            // Add statements up to the last statement (return)
            for (int i = 0; i < body.getStatements().size() - 1; i++) {
                StatementTree statement = body.getStatements().get(i);
                if ((!inSameClass || invokeOnInstance) || inStatic) {
                    statement = (StatementTree) fixReferences(statement, new TreePath(bodyPath, statement), method, scope, methodSelect);
                    if (!inSameClass) {
                        statement = genUtils.importFQNs(statement);
                    }
                }
                newStatementList.add(statement);
            }

            Tree parent = methodInvocationPath.getParentPath().getLeaf();
            Tree grandparent = null;
            Tree methodInvocation;

            if (parent.getKind() != Tree.Kind.EXPRESSION_STATEMENT) {
                methodInvocation = node;
            } else {
                grandparent = methodInvocationPath.getParentPath().getParentPath().getLeaf();
                switch (grandparent.getKind()) {
                    case FOR_LOOP:
                    case ENHANCED_FOR_LOOP:
                    case WHILE_LOOP:
                    case DO_WHILE_LOOP:
                    case IF:
                        methodInvocation = grandparent;
                        break;
                    default:
                        methodInvocation = parent;
                }
            }

            Tree lastStatement;
            if (body.getStatements().size() > 0) {
                lastStatement = body.getStatements().get(body.getStatements().size() - 1);
            } else {
                lastStatement = null;
            }
            if (lastStatement != null && ((!inSameClass || invokeOnInstance) || inStatic)) {
                lastStatement = fixReferences(lastStatement, new TreePath(bodyPath, lastStatement), method, scope, methodSelect);
                if (!inSameClass) {
                    lastStatement = genUtils.importFQNs(lastStatement);
                }
            }
            
            if(Boolean.TRUE == multiplereturn[0]) {
                if(parent.getKind() == RETURN) {
                    Map<Tree, List<StatementTree>> addedStatementsForBlock = newStatsQueue.getLast();
                    Map<Tree, Tree> translateMap = translateQueue.getLast();
                    List<StatementTree> stats = addedStatementsForBlock.get(statementTree);
                    if(stats == null) {
                        addedStatementsForBlock.put(statementTree, stats = new LinkedList<>());
                    }
                    stats.addAll(newStatementList);
                    genUtils.copyComments(methodInvocation, lastStatement, true);
                    List<Comment> comments = workingCopy.getTreeUtilities().getComments(methodInvocation, false);
                    for (Comment comment : comments) {
                        make.addComment(lastStatement, Comment.create(comment.style(), comment.getText()), false);
                    }
                    translateMap.put(parent, lastStatement);
                    return value;
                } else {
                    SourcePositions positions = workingCopy.getTrees().getSourcePositions();
                    long startPosition = positions.getStartPosition(workingCopy.getCompilationUnit(), node);
                    long lineNumber = workingCopy.getCompilationUnit().getLineMap().getLineNumber(startPosition);
                    String source = FileUtil.getFileDisplayName(workingCopy.getFileObject()) + ':' + lineNumber;
                    problem = JavaPluginUtils.chainProblems(problem,
                            new Problem(true, ERR_InlineMethodMultipleReturn(source)));
                    return value;
                }
            }

            Map<Tree, Tree> translateMap = translateQueue.size() > 0 ? translateQueue.getLast() : original2Translated;
            lastStatement = translateLastStatement(genUtils, parent, grandparent, newStatementList, lastStatement, node, methodInvocationPath, method, translateMap);
            Element element = workingCopy.getTrees().getElement(statementPath);
            if (element != null && element.getKind() == ElementKind.FIELD) {
                if (!newStatementList.isEmpty()) {
                    SourcePositions positions = workingCopy.getTrees().getSourcePositions();
                    long startPosition = positions.getStartPosition(workingCopy.getCompilationUnit(), node);
                    long lineNumber = workingCopy.getCompilationUnit().getLineMap().getLineNumber(startPosition);
                    String source = FileUtil.getFileDisplayName(workingCopy.getFileObject()) + ':' + lineNumber;
                    problem = JavaPluginUtils.chainProblems(problem,
                            new Problem(false, WRN_InlineMethodMultipleLines(source)));
                } else if (lastStatement instanceof StatementTree) {
                    SourcePositions positions = workingCopy.getTrees().getSourcePositions();
                    long startPosition = positions.getStartPosition(workingCopy.getCompilationUnit(), node);
                    long lineNumber = workingCopy.getCompilationUnit().getLineMap().getLineNumber(startPosition);
                    String source = FileUtil.getFileDisplayName(workingCopy.getFileObject()) + ':' + lineNumber;
                    problem = JavaPluginUtils.chainProblems(problem,
                            new Problem(true, ERR_InlineMethodNoLastReturn(source)));
                    return value;
                } else {
                    genUtils.copyComments(methodInvocation, lastStatement, true);
                    List<Comment> comments = workingCopy.getTreeUtilities().getComments(methodInvocation, false);
                    for (Comment comment : comments) {
                        make.addComment(lastStatement, Comment.create(comment.style(), comment.getText()), false);
                    }
                    rewrite(methodInvocation, lastStatement);
                }
            } else {
                if (lastStatement instanceof StatementTree) {
                    if(parent.getKind() == EXPRESSION_STATEMENT) {
                    } else {
                        SourcePositions positions = workingCopy.getTrees().getSourcePositions();
                        long startPosition = positions.getStartPosition(workingCopy.getCompilationUnit(), node);
                        long lineNumber = workingCopy.getCompilationUnit().getLineMap().getLineNumber(startPosition);
                        String source = FileUtil.getFileDisplayName(workingCopy.getFileObject()) + ':' + lineNumber;
                        problem = JavaPluginUtils.chainProblems(problem,
                                new Problem(true, ERR_InlineMethodNoLastReturn(source)));
                        return value;
                    }
                }
                Map<Tree, List<StatementTree>> addedStatementsForBlock = newStatsQueue.getLast();
                List<StatementTree> stats = addedStatementsForBlock.get(statementTree);
                if(stats == null) {
                    addedStatementsForBlock.put(statementTree, stats = new LinkedList<>());
                }
                stats.addAll(newStatementList);
                genUtils.copyComments(methodInvocation, lastStatement, true);
                List<Comment> comments = workingCopy.getTreeUtilities().getComments(methodInvocation, false);
                for (Comment comment : comments) {
                    make.addComment(lastStatement, Comment.create(comment.style(), comment.getText()), false);
                }
                translateMap.put(methodInvocation, lastStatement == null? make.EmptyStatement(): lastStatement);
            }
        }
        return value;
    }

    private boolean isInStaticContext(TreePath methodInvocationPath) {
        TreePath parent = methodInvocationPath.getParentPath();
        while (parent != null) {
            if (parent.getLeaf().getKind() == Tree.Kind.METHOD) {
                break;
            }
            parent = parent.getParentPath();
        }
        return parent != null && ((MethodTree) parent.getLeaf()).getModifiers().getFlags().contains(Modifier.STATIC);
    }

    @NbBundle.Messages({
        "WRN_InlineNotAccessible={0} is not accessible from {1}."})
    private Tree fixReferences(Tree tree, TreePath treePath, final ExecutableElement method, final Scope scope, final ExpressionTree methodSelect) {
        final HashMap<Tree, Tree> orig2trans = new HashMap<>();

        final ElementUtilities elementUtilities = workingCopy.getElementUtilities();
        final TypeElement bodyEnclosingTypeElement = elementUtilities.enclosingTypeElement(method);

        ErrorAwareTreePathScanner<Void, ExecutableElement> idScan = new ErrorAwareTreePathScanner<Void, ExecutableElement>() {
            @Override
            public Void visitIdentifier(IdentifierTree node, ExecutableElement p) {
                TreePath currentPath = getCurrentPath();
                if (currentPath.getParentPath().getLeaf().getKind() == Tree.Kind.MEMBER_SELECT) {
                    return super.visitIdentifier(node, p); // Already checked by visitMemberSelect
                }
                Element el = trees.getElement(currentPath);
                if (el != null) {
                    TypeMirror targetType = el.getEnclosingElement().asType();
                    if (methodSelect != null 
                            && targetType != null && targetType.getKind() == TypeKind.DECLARED 
                            && el.getEnclosingElement() != method
                            && !workingCopy.getTrees().isAccessible(scope, el, (DeclaredType)targetType)) {
                        problem = JavaPluginUtils.chainProblems(problem, new Problem(false, WRN_InlineNotAccessible(el, targetType)));
                    }
                    TypeElement invocationEnclosingTypeElement = elementUtilities.enclosingTypeElement(el);
                    if (el.getKind() != ElementKind.LOCAL_VARIABLE && bodyEnclosingTypeElement.equals(invocationEnclosingTypeElement)) {
                        if (el.getModifiers().contains(Modifier.STATIC)) {
                            Tree newTree = make.QualIdent(el);
                            orig2trans.put(node, newTree);
                        } else if(methodSelect != null) {
                            Tree newTree = make.MemberSelect(methodSelect, el);
                            orig2trans.put(node, newTree);
                        }
                    }
                }
                return super.visitIdentifier(node, p);
            }

            @Override
            public Void visitMethodInvocation(MethodInvocationTree node, ExecutableElement p) {
                TreePath currentPath = getCurrentPath();
                if (currentPath.getParentPath().getLeaf().getKind() == Tree.Kind.MEMBER_SELECT) {
                    return super.visitMethodInvocation(node, p); // Already checked by visitMemberSelect
                }

                Element el = trees.getElement(currentPath);
                if (el != null && el != p) {
                    DeclaredType declaredType = workingCopy.getTypes().getDeclaredType(scope.getEnclosingClass());
                    if (methodSelect != null
                            && el.getEnclosingElement() != method
                            && !workingCopy.getTrees().isAccessible(scope, el, declaredType)) {
                        problem = JavaPluginUtils.chainProblems(problem, new Problem(false, WRN_InlineNotAccessible(el, declaredType)));
                    }
                    TypeElement invocationEnclosingTypeElement = elementUtilities.enclosingTypeElement(el);
                    if (bodyEnclosingTypeElement.equals(invocationEnclosingTypeElement)) {
                        if (el.getModifiers().contains(Modifier.STATIC)) {
                            Tree newTree = make.QualIdent(el);
                            orig2trans.put(node.getMethodSelect(), newTree);
                        } else {
                            ExpressionTree methodInvocationSelect = node.getMethodSelect();
                            if (methodInvocationSelect.getKind() == Tree.Kind.MEMBER_SELECT) {
                                ExpressionTree expression = ((MemberSelectTree) methodInvocationSelect).getExpression();
                                String isThis = expression.toString();
                                if (isThis.equals("this") || isThis.endsWith(".this")) { //NOI18N
                                    if (methodSelect == null) { // We must be in Inner class.
                                        MemberSelectTree memberSelect = make.MemberSelect(workingCopy.getTreeUtilities().parseExpression(bodyEnclosingTypeElement.getSimpleName() + ".this", new SourcePositions[1]), el);
                                        orig2trans.put(node, memberSelect);
                                    } else {
                                        orig2trans.put(expression, methodSelect);
                                    }
                                } else if(methodSelect != null) {
                                    Tree newTree = make.MemberSelect(methodSelect, el);
                                    orig2trans.put(node, newTree);
                                }
                            } else if(methodSelect != null) {
                                Tree newTree = make.MemberSelect(methodSelect, el);
                                orig2trans.put(methodInvocationSelect, newTree);
                            }
                        }
                    }
                }
                if(el != null && el == p) {
                    return null;
                } else {
                    return super.visitMethodInvocation(node, p);
                }
            }

            @Override
            public Void visitMemberSelect(MemberSelectTree node, ExecutableElement p) {
                TreePath currentPath = getCurrentPath();
                Element el = trees.getElement(currentPath);
                if (el != null && el.getKind() != ElementKind.PACKAGE) {
                    DeclaredType declaredType = workingCopy.getTypes().getDeclaredType(scope.getEnclosingClass());
                    if (methodSelect != null
                            && el.getEnclosingElement() != method
                            && !workingCopy.getTrees().isAccessible(scope, el, declaredType)) {
                        problem = JavaPluginUtils.chainProblems(problem, new Problem(false, WRN_InlineNotAccessible(el, declaredType)));
                    }
                    TypeElement invocationEnclosingTypeElement = elementUtilities.enclosingTypeElement(el);
                    if (bodyEnclosingTypeElement.equals(invocationEnclosingTypeElement)) {
                        if (el.getModifiers().contains(Modifier.STATIC)) {
                            Tree newTree = make.QualIdent(el);
                            orig2trans.put(node, newTree);
                        } else {
                            ExpressionTree expression = node.getExpression();
                            String isThis = expression.toString();
                            if (isThis.equals("this") || isThis.endsWith(".this")) { //NOI18N
                                if (methodSelect == null) { // We must be in Inner class.
                                    MemberSelectTree memberSelect = make.MemberSelect(workingCopy.getTreeUtilities().parseExpression(bodyEnclosingTypeElement.getSimpleName() + ".this", new SourcePositions[1]), el);
                                    orig2trans.put(node, memberSelect);
                                } else {
                                    orig2trans.put(expression, methodSelect);
                                }
                            } else {
                                if (methodSelect != null) {
                                    Tree newTree = make.MemberSelect(methodSelect, el);
                                    orig2trans.put(node, newTree);
                                }
                            }
                        }
                    }
                }
                return super.visitMemberSelect(node, p);
            }
        };
        idScan.scan(treePath, method);

        Tree result = workingCopy.getTreeUtilities().translate(tree, orig2trans);
        return result;
    }

    private void scanForNameClash(final TreePath methodInvocationPath, final Tree body, Element p, final HashMap<Tree, Tree> original2TranslatedBody) {
        Element resolved = tph.getElementHandle().resolve(workingCopy);
        final CompilationUnitTree compilationUnitTree = workingCopy.getTrees().getPath(resolved).getCompilationUnit();
        final LinkedList<Pair<Element, String>> renames = new LinkedList<>();
        // Scan the body and look for name clashes
        ErrorAwareTreeScanner<Void, ExecutableElement> nameClashScanner = new ErrorAwareTreeScanner<Void, ExecutableElement>() {
            @Override
            public Void visitVariable(VariableTree node, ExecutableElement p) {
                TreePath path = trees.getPath(compilationUnitTree, node);
                if (path != null) {
                    Element variable = trees.getElement(path);
                    if (variable != null && !(variable.getKind() == ElementKind.PARAMETER && p.getParameters().contains((VariableElement) variable))) {
                        String varName = node.getName().toString();
                        String uniqueName = JavaPluginUtils.makeNameUnique(workingCopy,
                                                       workingCopy.getTrees().getScope(methodInvocationPath), varName, definedIds);
                        if(uniqueName == null ? varName != null : !uniqueName.equals(varName)) {
                            original2TranslatedBody.put(node, make.setLabel(node, uniqueName));
                            renames.add(Pair.of(variable, uniqueName));
                            definedIds.add(uniqueName);
                        } else {
                            definedIds.add(varName);
                        }
                    }
                }
                return super.visitVariable(node, p);
            }

            @Override
            public Void visitAssignment(AssignmentTree node, ExecutableElement p) {
                checkParameter(node.getVariable());
                return super.visitAssignment(node, p);
            }

            @Override
            public Void visitCompoundAssignment(CompoundAssignmentTree node, ExecutableElement p) {
                checkParameter(node.getVariable());
                return super.visitCompoundAssignment(node, p);
            }

            @Override
            public Void visitUnary(UnaryTree node, ExecutableElement p) {
                checkParameter(node.getExpression());
                return super.visitUnary(node, p);
            }

            private void checkParameter(Tree node) {
                TreePath path = trees.getPath(compilationUnitTree, node);
                if (path != null) {
                    Element variable = trees.getElement(path);
                    if(variable != null && variable.getKind() == ElementKind.PARAMETER) {
                        changedParamters.add(variable);
                    }
                }
            }
            
            
        };
        nameClashScanner.scan(body, (ExecutableElement) p);
        ErrorAwareTreeScanner<Void, Pair<Element, String>> idScan = new ErrorAwareTreeScanner<Void, Pair<Element, String>>() {
            @Override
            public Void visitIdentifier(IdentifierTree node, Pair<Element, String> p) {
                TreePath path = trees.getPath(compilationUnitTree, node);
                Element el = null;
                if(path != null) {
                    el = trees.getElement(path);
                }
                if (p.first().equals(el)) {
                    original2TranslatedBody.put(node, make.setLabel(node, p.second()));
                }
                return super.visitIdentifier(node, p);
            }
        };
        for (Pair<Element, String> pair : renames) {
            idScan.scan(body, pair);
        }
    }

    private TreePath findCorrespondingStatement(TreePath methodInvocationPath) {
        TreePath statementPath = methodInvocationPath;
        WHILE: while (statementPath != null) {
            if (statementPath.getParentPath() != null) {
                switch (statementPath.getParentPath().getLeaf().getKind()) {
                    case BLOCK:
                    case CLASS:
                        break WHILE;
                }
            }
            statementPath = statementPath.getParentPath();
        }
        return statementPath;
    }

    @SuppressWarnings("string-comparison-by-operator")
    private void replaceParametersWithArguments(final HashMap<Tree, Tree> original2TranslatedBody, ExecutableElement el, MethodInvocationTree node, final TreePath methodInvocationPath, BlockTree body, List<StatementTree> newVars) {
        Element resolved = tph.getElementHandle().resolve(workingCopy);
        final CompilationUnitTree compilationUnitTree = workingCopy.getTrees().getPath(resolved).getCompilationUnit();
        final LinkedList<Pair<Element, String>> renames = new LinkedList<>();
        
        ErrorAwareTreeScanner<Void, Pair<VariableElement, ExpressionTree>> idScan = new ErrorAwareTreeScanner<Void, Pair<VariableElement, ExpressionTree>>() {
            @Override
            public Void visitIdentifier(IdentifierTree node, Pair<VariableElement, ExpressionTree> p) {
                TreePath currentPath = trees.getPath(compilationUnitTree, node);
                Element el = null;
                if(currentPath != null) {
                    el = trees.getElement(currentPath);
                }
                if (p.first().equals(el)) {
                    original2TranslatedBody.put(node, p.second());
                }
                return super.visitIdentifier(node, p);
            }
        };
        for (int i = 0; i < el.getParameters().size(); i++) {
            VariableElement element = el.getParameters().get(i);
            if(el.isVarArgs() && el.getParameters().size() == i+1) {
                ArrayType asType = (ArrayType) element.asType();
                Tree type = make.Type(asType.getComponentType());
                Tree arraytype = make.Type(asType);
                List<ExpressionTree> arguments = new LinkedList<>();
                if(node.getArguments().size() > i) {
                    arguments.addAll(node.getArguments().subList(i, node.getArguments().size()));
                }
                String varName = element.getSimpleName().toString();
                String uniqueName = JavaPluginUtils.makeNameUnique(workingCopy,
                                               workingCopy.getTrees().getScope(methodInvocationPath), varName, definedIds);
                if(uniqueName == null ? varName != null : !uniqueName.equals(varName)) {
                    renames.add(Pair.of((Element)element, uniqueName));
                    definedIds.add(uniqueName);
                } else {
                    definedIds.add(varName);
                }
                newVars.add(make.Variable(make.Modifiers(element.getModifiers()), (uniqueName == null ? varName != null : !uniqueName.equals(varName))? uniqueName : varName, arraytype, make.NewArray(type, Collections.EMPTY_LIST, arguments)));
            } else {
                ExpressionTree argument = node.getArguments().get(i);
                if(!translateQueue.isEmpty() && translateQueue.getLast().containsKey(argument)) {
                    argument = (ExpressionTree) translateQueue.getLast().get(argument);
                }
                if(LITERALS.contains(argument.getKind()) && changedParamters.contains(element)) {
                    Tree arraytype = make.Type(element.asType());
                    String varName = element.getSimpleName().toString();
                    String uniqueName = JavaPluginUtils.makeNameUnique(workingCopy,
                                                   workingCopy.getTrees().getScope(methodInvocationPath), varName, definedIds);
                    if(uniqueName == null ? varName != null : !uniqueName.equals(varName)) {
                        renames.add(Pair.of((Element)element, uniqueName));
                        definedIds.add(uniqueName);
                    } else {
                        definedIds.add(varName);
                    }
                    newVars.add(make.Variable(make.Modifiers(element.getModifiers()), (uniqueName == null ? varName != null : !uniqueName.equals(varName))? uniqueName : varName, arraytype, argument));
                } else {
                    final Pair<VariableElement, ExpressionTree> pair = Pair.of(element, argument);
                    idScan.scan(body, pair);
                }
            }
        }

        ErrorAwareTreeScanner<Void, Pair<Element, String>> renameScan = new ErrorAwareTreeScanner<Void, Pair<Element, String>>() {
            @Override
            public Void visitIdentifier(IdentifierTree node, Pair<Element, String> p) {
                TreePath path = trees.getPath(compilationUnitTree, node);
                Element el = null;
                if(path != null) {
                    el = trees.getElement(path);
                }
                if (p.first().equals(el)) {
                    original2TranslatedBody.put(node, make.setLabel(node, p.second()));
                }
                return super.visitIdentifier(node, p);
            }
        };
        for (Pair<Element, String> pair : renames) {
            renameScan.scan(body, pair);
        }
    }

    @NbBundle.Messages(
            "WRN_InlineChangeReturn=Unsafe -- the return expression is not used in {0}.")
    private Tree translateLastStatement(GeneratorUtilities genUtils, Tree parent, Tree grandparent, List<StatementTree> newStatementList, Tree lastStatement, Tree node, TreePath location, Element method, Map<Tree, Tree> translateMap) {
        Tree result = lastStatement;
        TreeDuplicator duplicator = new TreeDuplicator(make, genUtils);
        if (parent.getKind() != Tree.Kind.EXPRESSION_STATEMENT) {
            if (result != null) {
                switch (result.getKind()) {
                    case EXPRESSION_STATEMENT:
                        result = ((ExpressionStatementTree) result).getExpression();
                        break;
                    case RETURN:
                        result = ((ReturnTree) result).getExpression();
                        break;
                    default:
                    // TODO: Problem, need an expression, but last statement is not an expression.
                }
                if(result instanceof ExpressionTree) {
                    boolean needsParentheses = OperatorPrecedence.needsParentheses(location, method, (ExpressionTree) result, workingCopy);
                    if(needsParentheses) {
                        result = workingCopy.getTreeMaker().Parenthesized((ExpressionTree) result);
                    }
                }
            }
        } else {
            if (result != null) {
                if (result.getKind() == Tree.Kind.RETURN) {
                    ExpressionTree returnExpression = ((ReturnTree) result).getExpression();
                    /*
                     * Allowed expressions in expressionstatement:
                     * Assignment
                     * PreIncrementExpression
                     * PreDecrementExpression
                     * PostIncrementExpression
                     * PostDecrementExpression
                     * MethodInvocation
                     * ClassInstanceCreationExpression
                     */
                    switch (returnExpression.getKind()) {
                        case ASSIGNMENT:
                        case PREFIX_INCREMENT:
                        case PREFIX_DECREMENT:
                        case POSTFIX_DECREMENT:
                        case POSTFIX_INCREMENT:
                        case METHOD_INVOCATION:
                        case NEW_CLASS:
                        case NEW_ARRAY:
                            result = make.ExpressionStatement(returnExpression);
                            break;
                        default:
                            result = make.EmptyStatement();
                            SourcePositions positions = workingCopy.getTrees().getSourcePositions();
                            long startPosition = positions.getStartPosition(workingCopy.getCompilationUnit(), node);
                            long lineNumber = workingCopy.getCompilationUnit().getLineMap().getLineNumber(startPosition);
                            String source = FileUtil.getFileDisplayName(workingCopy.getFileObject()) + ':' + lineNumber;
                            problem = JavaPluginUtils.chainProblems(problem,
                                                                    new Problem(false, WRN_InlineChangeReturn(source)));
                            break;
                    }
                }
            }
            switch (grandparent.getKind()) {
                case FOR_LOOP: {
                    ForLoopTree forLoopTree = (ForLoopTree) grandparent;
                    Tree newTree = translateMap.get(grandparent);
                    if (newTree != null && newTree.getKind() == FOR_LOOP) {
                        forLoopTree = (ForLoopTree) newTree;
                    }
                    
                    StatementTree statement = forLoopTree.getStatement();
                    if (statement == parent) {
                        addResultToStatementList(result, newStatementList);
                        statement = make.Block(newStatementList, false);
                        newStatementList.clear();
                    }
                    List<? extends ExpressionStatementTree> updates = forLoopTree.getUpdate();
                    List<ExpressionStatementTree> newUpdates = new LinkedList<ExpressionStatementTree>();
                    for (ExpressionStatementTree update : updates) {
                        if (update == parent) {
                            addResultToStatementList(result, newStatementList);
                            for (StatementTree statementTree1 : newStatementList) {
                                if (statementTree1.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                                    newUpdates.add((ExpressionStatementTree) statementTree1);
                                } else {
                                    // TODO: Problem forloop list only accepts expression statements.
                                    break;
                                }
                            }
                            newStatementList.clear();
                        } else {
                            newUpdates.add(update);
                        }
                    }
                    List<? extends StatementTree> initializers = forLoopTree.getInitializer();
                    List<StatementTree> newInitializers = new LinkedList<StatementTree>();
                    for (StatementTree initiazer : initializers) {
                        if (initiazer == parent) {
                            addResultToStatementList(result, newStatementList);
                            for (StatementTree statementTree1 : newStatementList) {
                                if (statementTree1.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                                    newInitializers.add((ExpressionStatementTree) statementTree1);
                                } else if (statementTree1.getKind() == Tree.Kind.VARIABLE) {
                                    newInitializers.add((VariableTree) statementTree1);
                                } else {
                                    // TODO: Problem forloop list only accepts expression statements.
                                    break;
                                }
                            }
                            newStatementList.clear();
                        } else {
                            newInitializers.add(initiazer);
                        }
                    }
                    result = make.ForLoop(newInitializers, forLoopTree.getCondition(), newUpdates, statement);
                }
                break;
                case ENHANCED_FOR_LOOP: {
                    EnhancedForLoopTree enhancedForLoopTree = (EnhancedForLoopTree) grandparent;
                    StatementTree statement = enhancedForLoopTree.getStatement();
                    if (statement == parent) {
                        addResultToStatementList(result, newStatementList);
                        statement = make.Block(newStatementList, false);
                        newStatementList.clear();
                    }
                    result = make.EnhancedForLoop(enhancedForLoopTree.getVariable(), enhancedForLoopTree.getExpression(), statement);
                }
                break;
                case WHILE_LOOP: {
                    WhileLoopTree whileLoopTree = (WhileLoopTree) grandparent;
                    StatementTree statement = whileLoopTree.getStatement();
                    if (statement == parent) {
                        addResultToStatementList(result, newStatementList);
                        statement = make.Block(newStatementList, false);
                        newStatementList.clear();
                    }
                    result = make.WhileLoop(whileLoopTree.getCondition(), statement);
                }
                break;
                case DO_WHILE_LOOP: {
                    DoWhileLoopTree doWhileLoopTree = (DoWhileLoopTree) grandparent;
                    StatementTree statement = doWhileLoopTree.getStatement();
                    if (statement == parent) {
                        addResultToStatementList(result, newStatementList);
                        statement = make.Block(newStatementList, false);
                        newStatementList.clear();
                    }
                    result = make.DoWhileLoop(doWhileLoopTree.getCondition(), statement);
                }
                break;
                case IF: {
                    IfTree ifTree = (IfTree) grandparent;
                    StatementTree thenStatement = ifTree.getThenStatement();
                    if (thenStatement == parent) {
                        addResultToStatementList(result, newStatementList);
                        thenStatement = make.Block(newStatementList, false);
                        newStatementList.clear();
                    }
                    StatementTree elseStatement = ifTree.getElseStatement();
                    if (elseStatement == parent) {
                        addResultToStatementList(result, newStatementList);
                        elseStatement = make.Block(newStatementList, false);
                        newStatementList.clear();
                    }
                    result = make.If(ifTree.getCondition(), thenStatement, elseStatement);
                }
                break;
            }
        }
        if(result != null) {
            result = duplicator.duplicate(result);
            genUtils.copyComments(lastStatement, result, true);
            genUtils.copyComments(lastStatement, result, false);
        }
        return result;
    }

    private void addResultToStatementList(Tree result, List<StatementTree> newStatementList) {
        if (result != null) {
            newStatementList.add((StatementTree) result);
        }
    }
}
