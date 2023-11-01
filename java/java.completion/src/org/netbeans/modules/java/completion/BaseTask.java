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

package org.netbeans.modules.java.completion;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;

import javax.lang.model.element.Element;
import static javax.lang.model.element.ElementKind.METHOD;
import javax.lang.model.element.ExecutableElement;
import static javax.lang.model.element.Modifier.*;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.sun.source.tree.*;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import javax.lang.model.element.Name;

import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;

/**
 *
 * @author Dusan Balek
 */
abstract class BaseTask extends UserTask {

    protected final int caretOffset;
    protected final Callable<Boolean> cancel;
    private int caretInSnapshot;

    protected BaseTask(int caretOffset, Callable<Boolean> cancel) {
        this.caretOffset = caretOffset;
        this.cancel = cancel;
    }

    
    final int getCaretInSnapshot() {
        return caretInSnapshot;
    }
    
    private CompilationController controller;
    
    final int snapshotPos(int pos) {
        if (pos < 0) {
            return pos;
        }
        int r = controller.getSnapshot().getEmbeddedOffset(pos);
        if (r == -1) {
            return pos;
        } else {
            return r;
        }
    }

    @Override
    public void run(ResultIterator resultIterator) throws Exception {
        Parser.Result result = resultIterator.getParserResult(caretOffset);
        CompilationController controller = result != null ? CompilationController.get(result) : null;
        if (controller != null && (cancel == null || !cancel.call())) {
            try {
                this.controller = controller;
                caretInSnapshot = snapshotPos(caretOffset);
                resolve(controller);
            } finally {
                this.controller = null;
            }
        }
    }

    protected abstract void resolve(CompilationController controller) throws IOException;

    Tree unwrapErrTree(Tree tree) {
        if (tree != null && tree.getKind() == Tree.Kind.ERRONEOUS) {
            Iterator<? extends Tree> it = ((ErroneousTree) tree).getErrorTrees().iterator();
            tree = it.hasNext() ? it.next() : null;
        }
        return tree;
    }

    TypeMirror asMemberOf(Element element, TypeMirror type, Types types) {
        TypeMirror ret = element.asType();
        TypeMirror enclType = element.getEnclosingElement().asType();
        if (enclType.getKind() == TypeKind.DECLARED) {
            enclType = types.erasure(enclType);
        }
        while (type != null && type.getKind() == TypeKind.DECLARED) {
            if ((enclType.getKind() != TypeKind.DECLARED || ((DeclaredType) enclType).asElement().getSimpleName().length() > 0) && types.isSubtype(type, enclType)) {
                ret = types.asMemberOf((DeclaredType) type, element);
                break;
            }
            type = ((DeclaredType) type).getEnclosingType();
        }
        return ret;
    }

    List<Tree> getArgumentsUpToPos(Env env, Iterable<? extends ExpressionTree> args, int startPos, int position, boolean strict) {
        List<Tree> ret = new ArrayList<>();
        CompilationUnitTree root = env.getRoot();
        SourcePositions sourcePositions = env.getSourcePositions();
        if (args == null) {
            return null; //TODO: member reference???
        }
        for (ExpressionTree e : args) {
            int pos = (int) sourcePositions.getEndPosition(root, e);
            if (pos != Diagnostic.NOPOS && (position > pos || !strict && position == pos)) {
                startPos = pos;
                ret.add(e);
            } else {
                break;
            }
        }
        if (startPos < 0) {
            return ret;
        }
        if (position >= startPos) {
            TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(env, startPos, position);
            if (last == null) {
                if (!strict && !ret.isEmpty()) {
                    ret.remove(ret.size() - 1);
                    return ret;
                }
            } else if (last.token().id() == JavaTokenId.LPAREN || last.token().id() == JavaTokenId.COMMA) {
                return ret;
            }
        }
        return null;
    }

    TokenSequence<JavaTokenId> findFirstNonWhitespaceToken(Env env, int startPos, int endPos) {
        TokenSequence<JavaTokenId> ts = env.getController().getTokenHierarchy().tokenSequence(JavaTokenId.language());
        ts.move(startPos);
        ts = nextNonWhitespaceToken(ts);
        if (ts == null || ts.offset() >= endPos) {
            return null;
        }
        return ts;
    }

    TokenSequence<JavaTokenId> nextNonWhitespaceToken(TokenSequence<JavaTokenId> ts) {
        while (ts.moveNext()) {
            switch (ts.token().id()) {
                case WHITESPACE:
                case LINE_COMMENT:
                case BLOCK_COMMENT:
                case JAVADOC_COMMENT:
                    break;
                default:
                    return ts;
            }
        }
        return null;
    }

    TokenSequence<JavaTokenId> findLastNonWhitespaceToken(Env env, Tree tree, int position) {
        int startPos = (int) env.getSourcePositions().getStartPosition(env.getRoot(), tree);
        return findLastNonWhitespaceToken(env, startPos, position);
    }

    TokenSequence<JavaTokenId> findLastNonWhitespaceToken(Env env, int startPos, int endPos) {
        TokenSequence<JavaTokenId> ts = env.getController().getTokenHierarchy().tokenSequence(JavaTokenId.language());
        ts.move(endPos);
        ts = previousNonWhitespaceToken(ts);
        if (ts == null || ts.offset() < startPos) {
            return null;
        }
        return ts;
    }

    TokenSequence<JavaTokenId> previousNonWhitespaceToken(TokenSequence<JavaTokenId> ts) {
        while (ts.movePrevious()) {
            switch (ts.token().id()) {
                case WHITESPACE:
                case LINE_COMMENT:
                case BLOCK_COMMENT:
                case JAVADOC_COMMENT:
                    break;
                default:
                    return ts;
            }
        }
        return null;
    }

    Env getCompletionEnvironment(CompilationController controller, boolean bottomUpSearch) throws IOException {
        controller.toPhase(JavaSource.Phase.PARSED);
        int offset = controller.getSnapshot().getEmbeddedOffset(caretOffset);
        if (offset < 0 || offset > controller.getText().length()) {
            return null;
        }
        String prefix = null;
        if (offset > 0) {
            if (bottomUpSearch) {
                TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                // When right at the token end move to previous token; otherwise move to the token that "contains" the offset
                if (ts.move(offset) == 0 || !ts.moveNext()) {
                    ts.movePrevious();
                }
                int len = offset - ts.offset();
                if (len > 0 && ts.token().length() >= len) {
                    if (ts.token().id() == JavaTokenId.IDENTIFIER
                            || ts.token().id().primaryCategory().startsWith("keyword") || //NOI18N
                            ts.token().id().primaryCategory().startsWith("string") || //NOI18N
                            ts.token().id().primaryCategory().equals("literal")) //NOI18N
                    { //TODO: Use isKeyword(...) when available
                        String prefixInToken = ts.token().text().toString().substring(0, len);
                        if (!ts.token().id().primaryCategory().startsWith("string") || !prefixInToken.endsWith("\\{")) {
                            prefix = prefixInToken;
                            offset = ts.offset();
                        }
                    } else if ((ts.token().id() == JavaTokenId.DOUBLE_LITERAL
                            || ts.token().id() == JavaTokenId.FLOAT_LITERAL
                            || ts.token().id() == JavaTokenId.FLOAT_LITERAL_INVALID
                            || ts.token().id() == JavaTokenId.LONG_LITERAL)
                            && ts.token().text().charAt(0) == '.') {
                        prefix = ts.token().text().toString().substring(1, len);
                        offset = ts.offset() + 1;
                    }
                }
            } else {
                TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                // When right at the token start move offset to the position "inside" the token
                ts.move(offset);
                if (!ts.moveNext()) {
                    ts.movePrevious();
                }
                if (ts.offset() == offset && ts.token().length() > 0
                        && (ts.token().id() == JavaTokenId.IDENTIFIER
                        || ts.token().id().primaryCategory().startsWith("keyword") || //NOI18N
                        ts.token().id().primaryCategory().startsWith("string") || //NOI18N
                        ts.token().id().primaryCategory().equals("literal"))) { //NOI18N
                    offset++;
                }
            }
        }
        offset = Math.min(offset, controller.getText().length());
        TreePath path = controller.getTreeUtilities().pathFor(offset);
        if (bottomUpSearch) {
            TreePath treePath = path;
            while (treePath != null) {
                TreePath pPath = treePath.getParentPath();
                TreePath gpPath = pPath != null ? pPath.getParentPath() : null;
                JavaCompletionTask.Env env = getEnvImpl(controller, path, treePath, pPath, gpPath, offset, prefix, true);
                if (env != null) {
                    return env;
                }
                treePath = treePath.getParentPath();
            }
        } else {
            if (JavaSource.Phase.RESOLVED.compareTo(controller.getPhase()) > 0) {
                LinkedList<TreePath> reversePath = new LinkedList<>();
                TreePath treePath = path;
                while (treePath != null) {
                    reversePath.addFirst(treePath);
                    treePath = treePath.getParentPath();
                }
                for (TreePath tp : reversePath) {
                    TreePath pPath = tp.getParentPath();
                    TreePath gpPath = pPath != null ? pPath.getParentPath() : null;
                    Env env = getEnvImpl(controller, path, tp, pPath, gpPath, offset, prefix, false);
                    if (env != null) {
                        return env;
                    }
                }
            }
        }
        return new Env(offset, prefix, controller, path, controller.getTrees().getSourcePositions(), null);
    }

    private Env getEnvImpl(CompilationController controller, TreePath orig, TreePath path, TreePath pPath, TreePath gpPath, int offset, String prefix, boolean upToOffset) throws IOException {
        Tree tree = path != null ? path.getLeaf() : null;
        Tree parent = pPath != null ? pPath.getLeaf() : null;
        Tree grandParent = gpPath != null ? gpPath.getLeaf() : null;
        SourcePositions sourcePositions = controller.getTrees().getSourcePositions();
        CompilationUnitTree root = controller.getCompilationUnit();
        TreeUtilities tu = controller.getTreeUtilities();
        if (upToOffset && TreeUtilities.CLASS_TREE_KINDS.contains(tree.getKind())) {
            controller.toPhase(withinAnonymousOrLocalClass(tu, path) ? JavaSource.Phase.RESOLVED : JavaSource.Phase.ELEMENTS_RESOLVED);
            return new Env(offset, prefix, controller, orig, sourcePositions, null);
        } else if (parent != null && tree.getKind() == Tree.Kind.BLOCK
                && (parent.getKind() == Tree.Kind.METHOD || TreeUtilities.CLASS_TREE_KINDS.contains(parent.getKind()))) {
            controller.toPhase(withinAnonymousOrLocalClass(tu, path) ? JavaSource.Phase.RESOLVED : JavaSource.Phase.ELEMENTS_RESOLVED);
            int blockPos = (int) sourcePositions.getStartPosition(root, tree);
            String blockText = fixStringTemplates(path, controller.getText().substring(blockPos, upToOffset ? offset : (int) sourcePositions.getEndPosition(root, tree)));
            final SourcePositions[] sp = new SourcePositions[1];
            final StatementTree block = (((BlockTree) tree).isStatic() ? tu.parseStaticBlock(blockText, sp) : tu.parseStatement(blockText, sp));
            if (block == null) {
                return null;
            }
            sourcePositions = new SourcePositionsImpl(block, sourcePositions, sp[0], blockPos, upToOffset ? offset : -1);
            Scope scope = controller.getTrees().getScope(path);
            path = tu.pathFor(new TreePath(pPath, block), offset, sourcePositions);
            if (upToOffset) {
                Tree last = path.getLeaf();
                List<? extends StatementTree> stmts = null;
                switch (path.getLeaf().getKind()) {
                    case BLOCK:
                        stmts = ((BlockTree) path.getLeaf()).getStatements();
                        break;
                    case FOR_LOOP:
                        stmts = ((ForLoopTree) path.getLeaf()).getInitializer();
                        break;
                    case ENHANCED_FOR_LOOP:
                        stmts = Collections.singletonList(((EnhancedForLoopTree) path.getLeaf()).getStatement());
                        break;
                    case METHOD:
                        stmts = ((MethodTree) path.getLeaf()).getParameters();
                        break;
                    case SWITCH:
                        CaseTree lastCase = null;
                        for (CaseTree caseTree : ((SwitchTree) path.getLeaf()).getCases()) {
                            lastCase = caseTree;
                        }
                        if (lastCase != null) {
                            stmts = lastCase.getStatements();
                            if (stmts == null || stmts.isEmpty()) {
                                Tree body = lastCase.getBody();
                                if (body != null) {
                                    last = body;
                                } else {
                                    Tree guard = lastCase.getGuard();
                                    if (guard != null) {
                                        last = guard;
                                    }
                                }
                            }
                        }
                        break;
                    case CASE:
                        stmts = ((CaseTree) path.getLeaf()).getStatements();
                        if (stmts == null || stmts.isEmpty()) {
                            Tree body = ((CaseTree) path.getLeaf()).getBody();
                            if (body != null) {
                                last = body;
                            } else {
                                Tree guard = ((CaseTree) path.getLeaf()).getGuard();
                                if (guard != null) {
                                    last = guard;
                                }
                            }
                        }
                        break;
                    case CONDITIONAL_AND: case CONDITIONAL_OR:
                        BinaryTree bt = (BinaryTree) last;
                        if (sourcePositions.getStartPosition(path.getCompilationUnit(), bt.getRightOperand()) == offset &&
                            bt.getRightOperand().getKind() == Kind.ERRONEOUS) {
                            last = bt.getRightOperand();
                        }
                        break;
                }
                if (stmts != null) {
                    for (StatementTree st : stmts) {
                        if (sourcePositions.getEndPosition(root, st) <= offset) {
                            last = st;
                        }
                    }
                }
                scope = tu.reattributeTreeTo(block, scope, last);
            } else {
                tu.reattributeTreeTo(block, scope, block);
            }
            return new Env(offset, prefix, controller, path, sourcePositions, scope);
        } else if (tree.getKind() == Tree.Kind.LAMBDA_EXPRESSION) {
            controller.toPhase(JavaSource.Phase.RESOLVED);
            Tree lambdaBody = ((LambdaExpressionTree) tree).getBody();
            Scope scope = null;
            TreePath blockPath = path.getParentPath();
            int bodyPos = 0;
            while (blockPath != null) {
                if (blockPath.getLeaf().getKind() == Tree.Kind.BLOCK) {
                    if (blockPath.getParentPath().getLeaf().getKind() == Tree.Kind.METHOD
                            || TreeUtilities.CLASS_TREE_KINDS.contains(blockPath.getParentPath().getLeaf().getKind())) {
                        final int blockPos = (int) sourcePositions.getStartPosition(root, blockPath.getLeaf());
                        final String blockText = upToOffset && getCaretInSnapshot() > offset
                                ? controller.getText().substring(blockPos, offset) + whitespaceString(getCaretInSnapshot() - offset) + controller.getText().substring(getCaretInSnapshot(), (int) sourcePositions.getEndPosition(root, blockPath.getLeaf()))
                                : controller.getText().substring(blockPos, (int) sourcePositions.getEndPosition(root, blockPath.getLeaf()));
                        final SourcePositions[] sp = new SourcePositions[1];
                        final StatementTree block = (((BlockTree) blockPath.getLeaf()).isStatic() ? tu.parseStaticBlock(blockText, sp) : tu.parseStatement(blockText, sp));
                        if (block == null) {
                            return null;
                        }
                        sourcePositions = new SourcePositionsImpl(block, sourcePositions, sp[0], blockPos, -1);
                        path = tu.getPathElementOfKind(Tree.Kind.LAMBDA_EXPRESSION, tu.pathFor(new TreePath(blockPath.getParentPath(), block), offset, sourcePositions));
                        if (path == null) {
                            return null;
                        }
                        lambdaBody = ((LambdaExpressionTree) path.getLeaf()).getBody();
                        bodyPos = (int) sourcePositions.getStartPosition(root, lambdaBody);
                        if (bodyPos >= offset) {
                            TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                            ts.move(offset);
                            while (ts.movePrevious()) {
                                switch (ts.token().id()) {
                                    case WHITESPACE:
                                    case LINE_COMMENT:
                                    case BLOCK_COMMENT:
                                    case JAVADOC_COMMENT:
                                        break;
                                    case ARROW:
                                        scope = controller.getTrees().getScope(blockPath);
                                        scope = tu.reattributeTreeTo(block, scope, lambdaBody);
                                        return new Env(offset, prefix, controller, path, sourcePositions, scope);
                                    default:
                                        return null;
                                }
                            }
                        }
                        scope = controller.getTrees().getScope(blockPath);
                        scope = tu.reattributeTreeTo(block, scope, lambdaBody);
                        break;
                    }
                }
                blockPath = blockPath.getParentPath();
            }
            if (scope == null) {
                scope = controller.getTrees().getScope(new TreePath(path, lambdaBody));
                bodyPos = (int) sourcePositions.getStartPosition(root, lambdaBody);
                if (bodyPos >= offset) {
                    TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                    ts.move(offset);
                    while (ts.movePrevious()) {
                        switch (ts.token().id()) {
                            case WHITESPACE:
                            case LINE_COMMENT:
                            case BLOCK_COMMENT:
                            case JAVADOC_COMMENT:
                                break;
                            case ARROW:
                                return new Env(offset, prefix, controller, path, sourcePositions, scope);
                            default:
                                return null;
                        }
                    }
                }
            }
            String bodyText = controller.getText().substring(bodyPos, upToOffset ? offset : (int) sourcePositions.getEndPosition(root, lambdaBody));
            final SourcePositions[] sp = new SourcePositions[1];
            final Tree body = bodyText.charAt(0) == '{' ? tu.parseStatement(bodyText, sp) : tu.parseExpression(bodyText, sp);
            final Tree fake = body instanceof ExpressionTree ? new ExpressionStatementTree() {
                @Override
                public Object accept(TreeVisitor v, Object p) {
                    return v.visitExpressionStatement(this, p);
                }

                @Override
                public ExpressionTree getExpression() {
                    return (ExpressionTree) body;
                }

                @Override
                public Tree.Kind getKind() {
                    return Tree.Kind.EXPRESSION_STATEMENT;
                }
            } : body;
            sourcePositions = new SourcePositionsImpl(fake, sourcePositions, sp[0], bodyPos, upToOffset ? offset : -1);
            path = tu.pathFor(new TreePath(path, fake), offset, sourcePositions);
            if (upToOffset && !(body instanceof ExpressionTree)) {
                Tree last = path.getLeaf();
                List<? extends StatementTree> stmts = null;
                switch (path.getLeaf().getKind()) {
                    case BLOCK:
                        stmts = ((BlockTree) path.getLeaf()).getStatements();
                        break;
                    case FOR_LOOP:
                        stmts = ((ForLoopTree) path.getLeaf()).getInitializer();
                        break;
                    case ENHANCED_FOR_LOOP:
                        stmts = Collections.singletonList(((EnhancedForLoopTree) path.getLeaf()).getStatement());
                        break;
                    case METHOD:
                        stmts = ((MethodTree) path.getLeaf()).getParameters();
                        break;
                    case SWITCH:
                        CaseTree lastCase = null;
                        for (CaseTree caseTree : ((SwitchTree) path.getLeaf()).getCases()) {
                            lastCase = caseTree;
                        }
                        if (lastCase != null) {
                            stmts = lastCase.getStatements();
                            if (stmts == null || stmts.isEmpty()) {
                                Tree caseBody = lastCase.getBody();
                                if (caseBody != null) {
                                    last = caseBody;
                                } else {
                                    Tree guard = lastCase.getGuard();
                                    if (guard != null) {
                                        last = guard;
                                    }
                                }
                            }
                        }
                        break;
                    case CASE:
                        stmts = ((CaseTree) path.getLeaf()).getStatements();
                        if (stmts == null || stmts.isEmpty()) {
                            Tree caseBody = ((CaseTree) path.getLeaf()).getBody();
                            if (caseBody != null) {
                                last = caseBody;
                            } else {
                                Tree guard = ((CaseTree) path.getLeaf()).getGuard();
                                if (guard != null) {
                                    last = guard;
                                }
                            }
                        }
                        break;
                }
                if (stmts != null) {
                    for (StatementTree st : stmts) {
                        if (sourcePositions.getEndPosition(root, st) <= offset) {
                            last = st;
                        }
                    }
                }
                scope = tu.reattributeTreeTo(body, scope, last);
            } else {
                scope = tu.reattributeTreeTo(body, scope, body);
            }
            return new Env(offset, prefix, controller, path, sourcePositions, scope);
        } else if (grandParent != null && TreeUtilities.CLASS_TREE_KINDS.contains(grandParent.getKind())
                && parent != null && parent.getKind() == Tree.Kind.VARIABLE && unwrapErrTree(((VariableTree) parent).getInitializer()) == tree) {
            if (tu.isEnum((ClassTree) grandParent)) {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                return null;
            }
            controller.toPhase(withinAnonymousOrLocalClass(tu, path) ? JavaSource.Phase.RESOLVED : JavaSource.Phase.ELEMENTS_RESOLVED);
            Scope scope = controller.getTrees().getScope(path);
            final int initPos = (int) sourcePositions.getStartPosition(root, tree);
            String initText = controller.getText().substring(initPos, upToOffset ? offset : (int) sourcePositions.getEndPosition(root, tree));
            if (initText.length() > 0) {
                final SourcePositions[] sp = new SourcePositions[1];
                final ExpressionTree init = tu.parseVariableInitializer(initText, sp);
                final ExpressionStatementTree fake = new ExpressionStatementTree() {
                    @Override
                    public Object accept(TreeVisitor v, Object p) {
                        return v.visitExpressionStatement(this, p);
                    }

                    @Override
                    public ExpressionTree getExpression() {
                        return init;
                    }

                    @Override
                    public Tree.Kind getKind() {
                        return Tree.Kind.EXPRESSION_STATEMENT;
                    }
                };
                sourcePositions = new SourcePositionsImpl(fake, sourcePositions, sp[0], initPos, upToOffset ? offset : -1);
                path = tu.pathFor(new TreePath(pPath, fake), offset, sourcePositions);
                if (upToOffset && sp[0].getEndPosition(root, init) + initPos > offset) {
                    scope = tu.reattributeTreeTo(init, scope, path.getLeaf());
                } else {
                    tu.reattributeTree(init, scope);
                }
            }
            return new Env(offset, prefix, controller, path, sourcePositions, scope);
        } else if (parent != null && TreeUtilities.CLASS_TREE_KINDS.contains(parent.getKind()) && tree.getKind() == Tree.Kind.VARIABLE
                && ((VariableTree) tree).getInitializer() != null && orig == path
                && sourcePositions.getStartPosition(root, ((VariableTree) tree).getInitializer()) >= 0
                && sourcePositions.getStartPosition(root, ((VariableTree) tree).getInitializer()) <= offset) {
            controller.toPhase(withinAnonymousOrLocalClass(tu, path) ? JavaSource.Phase.RESOLVED : JavaSource.Phase.ELEMENTS_RESOLVED);
            tree = ((VariableTree) tree).getInitializer();
            Scope scope = controller.getTrees().getScope(new TreePath(path, tree));
            final int initPos = (int) sourcePositions.getStartPosition(root, tree);
            String initText = controller.getText().substring(initPos, offset);
            if (initText.length() > 0) {
                final SourcePositions[] sp = new SourcePositions[1];
                final ExpressionTree init = tu.parseVariableInitializer(initText, sp);
                final ExpressionStatementTree fake = new ExpressionStatementTree() {
                    @Override
                    public Object accept(TreeVisitor v, Object p) {
                        return v.visitExpressionStatement(this, p);
                    }

                    @Override
                    public ExpressionTree getExpression() {
                        return init;
                    }

                    @Override
                    public Tree.Kind getKind() {
                        return Tree.Kind.EXPRESSION_STATEMENT;
                    }
                };
                sourcePositions = new SourcePositionsImpl(fake, sourcePositions, sp[0], initPos, offset);
                path = tu.pathFor(new TreePath(path, fake), offset, sourcePositions);
                tu.reattributeTree(init, scope);
            }
            return new Env(offset, prefix, controller, path, sourcePositions, scope);
        }
        return null;
    }

    private static String fixStringTemplates(TreePath tp, String blockText) {
        if (!blockText.contains("\\{")) {
            return blockText;
        }

        TokenHierarchy<String> th = TokenHierarchy.create(blockText, JavaTokenId.language());
        TokenSequence<JavaTokenId> ts = th.tokenSequence(JavaTokenId.language());
        StringBuilder augmented = new StringBuilder();

        augmented.append(blockText);
        ts.moveEnd();

        while (ts.movePrevious()) {
            if ((ts.token().id() == JavaTokenId.STRING_LITERAL ||
                 ts.token().id() == JavaTokenId.MULTILINE_STRING_LITERAL) &&
                ts.token().partType() == PartType.START) {
                if (ts.token().id() == JavaTokenId.STRING_LITERAL) {
                    augmented.append("}\"");
                } else {
                    augmented.append("}\"\"\"");
                }
            }
        }

        return augmented.toString();
    }

    private static String whitespaceString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private static class SourcePositionsImpl extends ErrorAwareTreeScanner<Void, Tree> implements SourcePositions {

        private final Tree root;
        private final SourcePositions original;
        private final SourcePositions modified;
        private final int startOffset;
        private final int endOffset;

        private boolean found;

        private SourcePositionsImpl(Tree root, SourcePositions original, SourcePositions modified, int startOffset, int endOffset) {
            this.root = root;
            this.original = original;
            this.modified = modified;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        @Override
        public long getStartPosition(CompilationUnitTree compilationUnitTree, Tree tree) {
            if (tree == root) {
                return startOffset;
            }
            found = false;
            scan(root, tree);
            return found ? modified.getStartPosition(compilationUnitTree, tree) + startOffset : original.getStartPosition(compilationUnitTree, tree);
        }

        @Override
        public long getEndPosition(CompilationUnitTree compilationUnitTree, Tree tree) {
            if (tree == root) {
                return endOffset;
            }
            found = false;
            scan(root, tree);
            return found ? modified.getEndPosition(compilationUnitTree, tree) + startOffset : original.getEndPosition(compilationUnitTree, tree);
        }

        @Override
        public Void scan(Tree node, Tree p) {
            if (node == p) {
                found = true;
            } else {
                super.scan(node, p);
            }
            return null;
        }


    }

    private static boolean isCamelCasePrefix(String prefix) {
        if (prefix == null || prefix.length() < 2 || prefix.charAt(0) == '"') {
            return false;
        }
        for (int i = 1; i < prefix.length(); i++) {
            if (Character.isUpperCase(prefix.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean withinAnonymousOrLocalClass(TreeUtilities tu, TreePath path) {
        do {
            path = tu.getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, path);
            if (path == null) {
                return false;
            }
            path = path.getParentPath();
            if (path.getLeaf().getKind() != Tree.Kind.COMPILATION_UNIT && !TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind())) {
                return true;
            }
        } while (true);
    }

    static final class Env {

        private final int offset;
        private final String prefix;
        private final boolean isCamelCasePrefix;
        private final CompilationController controller;
        private final TreePath path;
        private final SourcePositions sourcePositions;
        private Scope scope;
        private ReferencesCount referencesCount;
        private Map<Name, Element> refs = null;
        private boolean afterExtends = false;
        private boolean insideNew = false;
        private boolean insideForEachExpression = false;
        private boolean insideClass = false;
        private Set<? extends TypeMirror> smartTypes = null;
        private Set<Element> excludes = null;
        private Set<String> kws = new HashSet<>();
        private boolean addSemicolon = false;
        private boolean checkAddSemicolon = true;
        private int assignToVarPos = -2;
        private boolean checkAccessibility = true;

        private Env(int offset, String prefix, CompilationController controller, TreePath path, SourcePositions sourcePositions, Scope scope) {
            this.offset = offset;
            this.prefix = prefix;
            this.isCamelCasePrefix = BaseTask.isCamelCasePrefix(prefix);
            this.controller = controller;
            this.path = path;
            this.sourcePositions = sourcePositions;
            this.scope = scope;
        }

        public int getOffset() {
            return offset;
        }

        public String getPrefix() {
            return prefix;
        }

        public boolean isCamelCasePrefix() {
            return isCamelCasePrefix;
        }

        public CompilationController getController() {
            return controller;
        }

        public CompilationUnitTree getRoot() {
            return path.getCompilationUnit();
        }

        public TreePath getPath() {
            return path;
        }

        public SourcePositions getSourcePositions() {
            return sourcePositions;
        }

        public Scope getScope() throws IOException {
            if (scope == null) {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                scope = controller.getTreeUtilities().scopeFor(offset);
            }
            return scope;
        }

        public ReferencesCount getReferencesCount() {
            if (referencesCount == null) {
                referencesCount = ReferencesCount.get(controller.getClasspathInfo());
            }
            return referencesCount;
        }

        public Map<Name, ? extends Element> getForwardReferences() {
            if (refs == null) {
                refs = new HashMap<>();
                for (Element ref : SourceUtils.getForwardReferences(path, offset, sourcePositions, controller.getTrees())) {
                    refs.put(ref.getSimpleName(), ref);
                }
            }
            return refs;
        }

        public boolean isAfterExtends() {
            return afterExtends;
        }

        public void afterExtends() {
            this.afterExtends = true;
        }

        public void insideForEachExpression() {
            this.insideForEachExpression = true;
        }

        public boolean isInsideForEachExpression() {
            return insideForEachExpression;
        }

        public void insideNew() {
            this.insideNew = true;
        }

        public boolean isInsideNew() {
            return insideNew;
        }

        public void insideClass() {
            this.insideClass = true;
        }

        public boolean isInsideClass() {
            return insideClass;
        }

        public void setSmartTypes(Set<? extends TypeMirror> smartTypes) throws IOException {
            this.smartTypes = smartTypes;
        }

        public Set<? extends TypeMirror> getSmartTypes() throws IOException {
            return smartTypes;
        }

        public void addToExcludes(Element toExclude) {
            if (toExclude != null) {
                if (excludes == null) {
                    excludes = new HashSet<>();
                }
                excludes.add(toExclude);
            }
        }

        public Set<? extends Element> getExcludes() {
            return excludes;
        }

        public void addExcludedKW(String kw) {
            if (kw != null) {
                kws.add(kw);
            }
        }

        public boolean isExcludedKW(String kw) {
            return kws.contains(kw);
        }

        public void skipAccessibilityCheck() {
            this.checkAccessibility = false;
        }

        public boolean isAccessible(Scope scope, Element member, TypeMirror type, boolean selectSuper) {
            if (!checkAccessibility) {
                return true;
            }
            if (type.getKind() != TypeKind.DECLARED) {
                return member.getModifiers().contains(PUBLIC);
            }
            if (getController().getTrees().isAccessible(scope, member, (DeclaredType) type)) {
                return true;
            }
            return selectSuper
                    && member.getModifiers().contains(PROTECTED) && !member.getModifiers().contains(STATIC)
                    && !member.getKind().isClass() && !member.getKind().isInterface()
                    && getController().getTrees().isAccessible(scope, (TypeElement) ((DeclaredType) type).asElement())
                    && (member.getKind() != METHOD
                    || getController().getElementUtilities().getImplementationOf((ExecutableElement) member, (TypeElement) ((DeclaredType) type).asElement()) == member);
        }

        public boolean addSemicolon() {
            if (checkAddSemicolon) {
                TreePath tp = getPath();
                Tree tree = tp.getLeaf();
                if (tree.getKind() == Tree.Kind.IDENTIFIER || tree.getKind() == Tree.Kind.PRIMITIVE_TYPE) {
                    tp = tp.getParentPath();
                    if (tp.getLeaf().getKind() == Tree.Kind.VARIABLE && ((VariableTree) tp.getLeaf()).getType() == tree) {
                        addSemicolon = true;
                    }
                }
                if (tp.getLeaf().getKind() == Tree.Kind.MEMBER_SELECT
                        || (tp.getLeaf().getKind() == Tree.Kind.METHOD_INVOCATION && ((MethodInvocationTree) tp.getLeaf()).getMethodSelect() == tree)
                        || tp.getLeaf().getKind() == Tree.Kind.VARIABLE) {
                    tp = tp.getParentPath();
                }
                if (tp.getLeaf().getKind() == Tree.Kind.EXPRESSION_STATEMENT
                        && tp.getParentPath().getLeaf().getKind() != Tree.Kind.LAMBDA_EXPRESSION
                        || tp.getLeaf().getKind() == Tree.Kind.BLOCK
                        || tp.getLeaf().getKind() == Tree.Kind.RETURN) {
                    addSemicolon = true;
                }
                checkAddSemicolon = false;
            }
            return addSemicolon;
        }

        public int assignToVarPos() {
            if (assignToVarPos < -1) {
                TreePath tp = getPath();
                Tree tree = tp.getLeaf();
                if (tp.getLeaf().getKind() == Tree.Kind.MEMBER_SELECT
                        || (tp.getLeaf().getKind() == Tree.Kind.METHOD_INVOCATION && ((MethodInvocationTree) tp.getLeaf()).getMethodSelect() == tree)) {
                    tp = tp.getParentPath();
                }
                if (tp.getLeaf().getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                    assignToVarPos = getController().getSnapshot().getOriginalOffset((int) getSourcePositions().getStartPosition(getRoot(), tree));
                } else if (tp.getLeaf().getKind() == Tree.Kind.BLOCK) {
                    assignToVarPos = getController().getSnapshot().getOriginalOffset(offset);
                } else {
                    assignToVarPos = -1;
                }
            }
            return assignToVarPos;
        }
    }
}
