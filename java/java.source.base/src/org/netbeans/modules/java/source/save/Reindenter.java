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
package org.netbeans.modules.java.source.save;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchExpressionTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.SourcePositions;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTool;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.swing.text.BadLocationException;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

import java.util.Arrays;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.nbjavac.services.NBParserFactory;
import org.netbeans.lib.nbjavac.services.NBTreeMaker;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.Context.Region;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.netbeans.modules.java.source.NoJavacHelper;
import org.netbeans.modules.java.source.TreeUtilitiesAccessor;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.parsing.ParsingUtils;
import org.netbeans.modules.parsing.impl.Utilities;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Dusan Balek
 */
public class Reindenter implements IndentTask {

    private final Context context;
    private CodeStyle cs;
    private TokenSequence<JavaTokenId> ts;
    private String text;
    private CompilationUnitTree cut;
    private Tree parsedTree;
    private SourcePositions sp;
    private Map<Integer, Integer> newIndents;
    private int currentEmbeddingStartOffset;
    private int currentEmbeddingLength;

    private Reindenter(Context context) {
        this.context = context;
    }

    @Override
    public void reindent() throws BadLocationException {
        ts = null;
        currentEmbeddingLength = -1;
        newIndents = new HashMap<Integer, Integer>();
        cs = CodeStyle.getDefault(context.document());
        for (Region region : context.indentRegions()) {
            if (initRegionData(region)) {
                HashSet<Integer> linesToAddStar = new HashSet<Integer>();
                Map<Integer, Integer> oldIndents = new HashMap<>();
                LinkedList<Integer> startOffsets = getStartOffsets(region);
                for (ListIterator<Integer> it = startOffsets.listIterator(); it.hasNext();) {
                    int startOffset = it.next();
                    int endOffset;
                    if (it.hasNext()) {
                        endOffset = it.next() - 1;
                        it.previous();
                    } else {
                        endOffset = region.getEndOffset();
                    }
                    String blockCommentLine;
                    int delta = ts.move(startOffset);
                    if (((startOffset == 0 || delta > 0) && ts.moveNext() || ts.movePrevious())
                            && (ts.token().id() != JavaTokenId.BLOCK_COMMENT || ts.embedded() == null)) {
                        if (cs.addLeadingStarInComment()
                                && (ts.token().id() == JavaTokenId.BLOCK_COMMENT && cs.enableBlockCommentFormatting()
                                || ts.token().id() == JavaTokenId.JAVADOC_COMMENT && cs.enableJavadocFormatting())) {
                            blockCommentLine = ts.token().text().toString();
                            if (delta > 0) {
                                int idx = blockCommentLine.indexOf('\n', delta); //NOI18N
                                blockCommentLine = (idx < 0 ? blockCommentLine.substring(delta) : blockCommentLine.substring(delta, idx)).trim();
                                int off = ts.offset() + delta - 1;
                                int prevLineStartOffset = context.lineStartOffset(off < 0 ? startOffset : off);
                                Integer prevLineIndent = newIndents.get(prevLineStartOffset);
                                newIndents.put(startOffset, (prevLineIndent != null ? prevLineIndent : context.lineIndent(prevLineStartOffset)) + (prevLineStartOffset > ts.offset() ? 0 : 1)); //NOI18N
                            } else {
                                int idx = blockCommentLine.lastIndexOf('\n'); //NOI18N
                                if (idx > 0) {
                                    blockCommentLine = blockCommentLine.substring(idx).trim();
                                }
                                newIndents.put(startOffset, getNewIndent(startOffset, endOffset) + 1);
                            }
                            if (!blockCommentLine.startsWith("*")) { //NOI18N
                                linesToAddStar.add(startOffset);
                            }
                        } else if (ts.token().id() == JavaTokenId.MULTILINE_STRING_LITERAL) {
                            String tokenText = ts.token().text().toString();
                            String[] lines = tokenText.split("\n");
                            int indent = Arrays.stream(lines, 1, lines.length)
                                               .mapToInt(this::leadingIndent)
                                               .min()
                                               .orElse(0);
                            int initialLineStartOffset = context.lineStartOffset(ts.offset());
                            int indentUpdate = newIndents.getOrDefault(initialLineStartOffset, 0);
                            oldIndents.put(startOffset, indent);
                            newIndents.put(startOffset, ts.offset() - initialLineStartOffset + indentUpdate);
                        } else {
                            if (delta == 0 && ts.moveNext() && ts.token().id() == JavaTokenId.LINE_COMMENT) {
                                newIndents.put(startOffset, 0);
                            } else {
                                newIndents.put(startOffset, getNewIndent(startOffset, endOffset));
                            }
                        }
                    }
                }
                while (!startOffsets.isEmpty()) {
                    int startOffset = startOffsets.removeLast();
                    Integer newIndent = newIndents.get(startOffset);
                    if (linesToAddStar.contains(startOffset)) {
                        context.modifyIndent(startOffset, 0);
                        context.document().insertString(startOffset, "* ", null); //NOI18N
                    }
                    if (newIndent != null) {
                        Integer oldIndent = oldIndents.get(startOffset);
                        if (oldIndent != null) {
                            context.modifyIndent(startOffset, oldIndent, IndentUtils.createIndentString(newIndent, true, -1));
                        } else {
                            context.modifyIndent(startOffset, newIndent);
                        }
                    }
                    if (!startOffsets.isEmpty()) {
                        char c;
                        int len = 0;
                        while ((c = text.charAt(startOffset - 2 - len)) != '\n' && Character.isWhitespace(c)) { //NOI18N
                            len++;
                        }
                        if (len > 0) {
                            context.document().remove(startOffset - 1 - len, len);
                        }
                    }
                }
            }
        }
    }

    private int leadingIndent(String line) {
        int indent = 0;

        for (int i = 0; i < line.length(); i++) { //TODO: code points
            if (Character.isWhitespace(line.charAt(i)))
                indent++;
            else
                break;
        }

        return indent;
    }

    @Override
    public ExtraLock indentLock() {
        return null;
    }
    
    private boolean initRegionData(final Region region) {
        if (ts == null || (currentEmbeddingLength >= 0
                && !(currentEmbeddingStartOffset <= region.getStartOffset()
                && currentEmbeddingStartOffset + currentEmbeddingLength >= region.getEndOffset()))) {
            ts = null;
            currentEmbeddingStartOffset = 0;
            currentEmbeddingLength = context.document().getLength();
            TokenSequence<?> tseq = TokenHierarchy.get(context.document()).tokenSequence();
            while(tseq != null && (region.getStartOffset() == 0 || tseq.moveNext())) {
                tseq.move(region.getStartOffset());
                if (!tseq.moveNext() && !tseq.movePrevious()) {
                    return false;
                }
                if (tseq.language() == JavaTokenId.language()) {
                    ts = (TokenSequence<JavaTokenId>)tseq;
                    tseq.moveStart();
                    tseq.moveNext();
                    currentEmbeddingStartOffset = tseq.offset();
                    tseq.moveEnd();
                    tseq.movePrevious();
                    currentEmbeddingLength = tseq.offset() - currentEmbeddingStartOffset;
                    break;
                }
                tseq = tseq.embeddedJoined();
            }
            if (ts == null) {
                return false;                
            }
            ClassLoader origCL = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(Reindenter.class.getClassLoader());
                com.sun.tools.javac.util.Context ctx = new com.sun.tools.javac.util.Context();
                NBParserFactory.preRegister(ctx);
                NBTreeMaker.preRegister(ctx);
                JavacTaskImpl javacTask = (JavacTaskImpl)JavacTool.create().getTask(null, null, new DiagnosticListener<JavaFileObject>() {
                    @Override
                    public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
                    }
                }, Collections.singletonList("-proc:none"), null, Collections.<JavaFileObject>emptySet(), ctx); //NOI18N
                JavaCompiler.instance(ctx).genEndPos = true;
                text = context.document().getText(currentEmbeddingStartOffset, currentEmbeddingLength);
                if (JavacParser.MIME_TYPE.equals(context.mimePath())) {
                    FileObject fo = Utilities.getFileObject(context.document());
                    cut = ParsingUtils.parseArbitrarySource(javacTask, FileObjects.memoryFileObject("", fo != null ? fo.getNameExt() : "", text));
                    parsedTree = cut;
                    sp = JavacTrees.instance(ctx).getSourcePositions();
                } else {
                    final SourcePositions[] psp = new SourcePositions[1];
                    cut = null;
                    parsedTree = TreeUtilitiesAccessor.getInstance().parseStatement(javacTask, "{" + text + "}", psp);
                    sp = new SourcePositions() {
                        @Override
                        public long getStartPosition(CompilationUnitTree file, Tree tree) {
                            return currentEmbeddingStartOffset + psp[0].getStartPosition(file, tree) - 1;
                        }

                        @Override
                        public long getEndPosition(CompilationUnitTree file, Tree tree) {
                            return currentEmbeddingStartOffset + psp[0].getEndPosition(file, tree) - 1;
                        }
                    };
                }
            } catch (Exception ex) {
                return false;
            } finally {
                Thread.currentThread().setContextClassLoader(origCL);
            }
        }
        return true;
    }

    private LinkedList<Integer> getStartOffsets(Region region) throws BadLocationException {
        LinkedList<Integer> offsets = new LinkedList<Integer>();
        int offset = region.getEndOffset();
        int lso;
        while (offset > 0 && (lso = context.lineStartOffset(offset)) >= region.getStartOffset()) {
            offsets.addFirst(lso);
            offset = lso - 1;
        }
        return offsets;
    }

    private int getNewIndent(int startOffset, int endOffset) throws BadLocationException {
        LinkedList<? extends Tree> path = getPath(startOffset);
        if (path.isEmpty()) {
            return 0;
        }
        Tree last = path.getFirst();
        int lastPos = getStartPosition(last);
        int currentIndent = getCurrentIndent(last, path);
        JavaTokenId nextTokenId = null;
        switch (last.getKind()) {
            case COMPILATION_UNIT:
                break;
            case MODULE:
                TokenSequence<JavaTokenId> token = findFirstNonWhitespaceToken(startOffset, endOffset);
                nextTokenId = token != null ? token.token().id() : null;
                if (nextTokenId != null && nextTokenId == JavaTokenId.RBRACE) {
                    if (isLeftBraceOnNewLine(lastPos, startOffset)) {
                        switch (cs.getModuleDeclBracePlacement()) {
                            case NEW_LINE_INDENTED:
                                currentIndent += cs.getIndentSize();
                                break;
                            case NEW_LINE_HALF_INDENTED:
                                currentIndent += (cs.getIndentSize() / 2);
                                break;
                        }
                    }
                } else {
                    Tree t = null;
                    for (Tree member : ((ModuleTree)last).getDirectives()) {
                        if (getEndPosition(member) > startOffset) {
                            break;
                        }
                        t = member;
                    }
                    if (t != null) {
                        int i = getCurrentIndent(t, path);
                        currentIndent = i < 0 ? currentIndent + (cs.indentTopLevelClassMembers() ? cs.getIndentSize() : 0) : i;
                    } else {
                        token = findFirstNonWhitespaceToken(startOffset, lastPos);
                        JavaTokenId prevTokenId = token != null ? token.token().id() : null;
                        if (prevTokenId != null) {
                            switch (prevTokenId) {
                                case LBRACE:
                                    currentIndent += cs.indentTopLevelClassMembers() ? cs.getIndentSize() : 0;
                                    break;
                                case IDENTIFIER:
                                    if (nextTokenId != null && nextTokenId == JavaTokenId.LBRACE) {
                                        switch (cs.getModuleDeclBracePlacement()) {
                                            case NEW_LINE_INDENTED:
                                                currentIndent += cs.getIndentSize();
                                                break;
                                            case NEW_LINE_HALF_INDENTED:
                                                currentIndent += (cs.getIndentSize() / 2);
                                                break;
                                        }
                                    } else {
                                        currentIndent += cs.getContinuationIndentSize();
                                    }
                                    break;
                                default:
                                    currentIndent += cs.getContinuationIndentSize();
                            }
                        }
                    }
                }
                break;
            case CLASS:
            case INTERFACE:
            case ENUM:
            case ANNOTATION_TYPE:
                token = findFirstNonWhitespaceToken(startOffset, endOffset);
                nextTokenId = token != null ? token.token().id() : null;
                if (nextTokenId != null && nextTokenId == JavaTokenId.RBRACE) {
                    if (isLeftBraceOnNewLine(lastPos, startOffset)) {
                        switch (cs.getClassDeclBracePlacement()) {
                            case NEW_LINE_INDENTED:
                                currentIndent += cs.getIndentSize();
                                break;
                            case NEW_LINE_HALF_INDENTED:
                                currentIndent += (cs.getIndentSize() / 2);
                                break;
                        }
                    }
                } else {
                    Tree t = null;
                    for (Tree member : ((ClassTree)last).getMembers()) {
                        if (getEndPosition(member) > startOffset) {
                            break;
                        }
                        t = member;
                    }
                    if (t != null) {
                        int i = getCurrentIndent(t, path);
                        currentIndent = i < 0 ? currentIndent + (cs.indentTopLevelClassMembers() ? cs.getIndentSize() : 0) : i;
                    } else {
                        token = findFirstNonWhitespaceToken(startOffset, lastPos);
                        JavaTokenId prevTokenId = token != null ? token.token().id() : null;
                        if (prevTokenId != null) {
                            switch (prevTokenId) {
                                case LBRACE:
                                    if (path.size() > 1 && path.get(1).getKind() == Kind.NEW_CLASS && isLeftBraceOnNewLine(lastPos, startOffset)) {
                                        switch (cs.getClassDeclBracePlacement()) {
                                            case SAME_LINE:
                                            case NEW_LINE:
                                                currentIndent += cs.getIndentSize();
                                                break;
                                            case NEW_LINE_HALF_INDENTED:
                                                currentIndent += (cs.getIndentSize() - cs.getIndentSize() / 2);
                                                break;
                                        }
                                    } else {
                                        currentIndent += cs.indentTopLevelClassMembers() ? cs.getIndentSize() : 0;
                                    }
                                    break;
                                case COMMA:
                                    currentIndent = getMultilineIndent(((ClassTree)last).getImplementsClause(), path, token.offset(), currentIndent, cs.alignMultilineImplements(), true);
                                    break;
                                case IDENTIFIER:
                                case GT:
                                case GTGT:
                                case GTGTGT:
                                    if (nextTokenId != null && nextTokenId == JavaTokenId.LBRACE) {
                                        switch (cs.getClassDeclBracePlacement()) {
                                            case NEW_LINE_INDENTED:
                                                currentIndent += cs.getIndentSize();
                                                break;
                                            case NEW_LINE_HALF_INDENTED:
                                                currentIndent += (cs.getIndentSize() / 2);
                                                break;
                                        }
                                    } else {
                                        currentIndent += cs.getContinuationIndentSize();
                                    }
                                    break;
                                default:
                                    currentIndent += cs.getContinuationIndentSize();
                            }
                        }
                    }
                }
                break;
            case METHOD:
                token = findFirstNonWhitespaceToken(startOffset, lastPos);
                JavaTokenId prevTokenId = token != null ? token.token().id() : null;
                if (prevTokenId != null) {
                    switch (prevTokenId) {
                        case COMMA:
                            List<? extends ExpressionTree> thrws = ((MethodTree)last).getThrows();
                            if (!thrws.isEmpty() && getStartPosition(thrws.get(0)) < token.offset()) {
                                currentIndent = getMultilineIndent(thrws, path, token.offset(), currentIndent, cs.alignMultilineThrows(), true);
                            } else {
                                currentIndent = getMultilineIndent(((MethodTree)last).getParameters(), path, token.offset(), currentIndent, cs.alignMultilineMethodParams(), true);
                            }
                            break;
                        case RPAREN:
                        case IDENTIFIER:
                        case GT:
                        case GTGT:
                        case GTGTGT:
                            token = findFirstNonWhitespaceToken(startOffset, endOffset);
                            if (token != null && token.token().id() == JavaTokenId.LBRACE) {
                                switch (cs.getMethodDeclBracePlacement()) {
                                    case NEW_LINE_INDENTED:
                                        currentIndent += cs.getIndentSize();
                                        break;
                                    case NEW_LINE_HALF_INDENTED:
                                        currentIndent += (cs.getIndentSize() / 2);
                                        break;
                                }
                                break;
                            }
                        default:
                            token = findFirstNonWhitespaceToken(startOffset, endOffset);
                            if (token == null || token.token().id() != JavaTokenId.RPAREN) {
                                currentIndent += cs.getContinuationIndentSize();
                            }
                    }
                }
                break;
            case VARIABLE:
                Tree type = ((VariableTree)last).getType();
                if (type != null && type.getKind() != Kind.ERRONEOUS) {
                    ExpressionTree init = ((VariableTree)last).getInitializer();
                    if (init == null || init.getKind() != Kind.NEW_ARRAY
                            || (token = findFirstNonWhitespaceToken(startOffset, lastPos)) == null
                            || token.token().id() != JavaTokenId.EQ
                            || (token = findFirstNonWhitespaceToken(startOffset, endOffset)) == null
                            || token.token().id() != JavaTokenId.LBRACE) {
                        if (cs.alignMultilineAssignment()) {
                            int c = getColumn(last);
                            if (c >= 0) {
                                currentIndent = c;
                            }
                        } else {
                            currentIndent += cs.getContinuationIndentSize();
                        }
                    } else {
                        switch (cs.getOtherBracePlacement()) {
                            case NEW_LINE_INDENTED:
                                currentIndent += cs.getIndentSize();
                                break;
                            case NEW_LINE_HALF_INDENTED:
                                currentIndent += (cs.getIndentSize() / 2);
                                break;
                        }
                    }
                    break;
                } else {
                    last = ((VariableTree)last).getModifiers();
                    if (last == null)
                        break;
                }
            case MODIFIERS:
                Tree t = null;
                for (Tree ann : ((ModifiersTree)last).getAnnotations()) {
                    if (getEndPosition(ann) > startOffset) {
                        break;
                    }
                    t = ann;
                }
                if (t == null || findFirstNonWhitespaceToken(startOffset, getEndPosition(t)) != null) {
                    currentIndent += cs.getContinuationIndentSize();
                }
                break;
            case DO_WHILE_LOOP:
                token = findFirstNonWhitespaceToken(startOffset, lastPos);
                if (token != null && !EnumSet.of(JavaTokenId.RBRACE, JavaTokenId.SEMICOLON).contains(token.token().id())) {
                    currentIndent = getStmtIndent(startOffset, endOffset, EnumSet.of(JavaTokenId.DO), lastPos, currentIndent);
                }
                break;
            case ENHANCED_FOR_LOOP:
                currentIndent = getStmtIndent(startOffset, endOffset, EnumSet.of(JavaTokenId.RPAREN), getEndPosition(((EnhancedForLoopTree)last).getExpression()), currentIndent);
                break;
            case FOR_LOOP:
                LinkedList<Tree> forTrees = new LinkedList<Tree>();
                for (StatementTree st : ((ForLoopTree)last).getInitializer()) {
                    if (getEndPosition(st) > startOffset) {
                        break;
                    }
                    forTrees.add(st);
                }
                t = ((ForLoopTree)last).getCondition();
                if (t != null && getEndPosition(t) <= startOffset) {
                    forTrees.add(t);
                }
                for (ExpressionStatementTree est : ((ForLoopTree)last).getUpdate()) {
                    if (getEndPosition(est) > startOffset) {
                        break;
                    }
                    forTrees.add(est);
                }
                token = findFirstNonWhitespaceToken(startOffset, lastPos);
                if (token != null && token.token().id() == JavaTokenId.SEMICOLON) {
                    currentIndent = getMultilineIndent(forTrees, path, token.offset(), currentIndent, cs.alignMultilineFor(), true);
                } else {
                    currentIndent = getStmtIndent(startOffset, endOffset, EnumSet.of(JavaTokenId.RPAREN), forTrees.isEmpty() ? lastPos : getEndPosition(forTrees.getLast()), currentIndent);
                }
                break;
            case IF:
                token = findFirstNonWhitespaceToken(startOffset, endOffset);
                if (token == null || token.token().id() != JavaTokenId.ELSE) {
                    token = findFirstNonWhitespaceToken(startOffset, lastPos);
                    if (token != null && !EnumSet.of(JavaTokenId.RBRACE, JavaTokenId.SEMICOLON).contains(token.token().id())) {
                        currentIndent = getStmtIndent(startOffset, endOffset, EnumSet.of(JavaTokenId.RPAREN, JavaTokenId.ELSE), getEndPosition(((IfTree)last).getCondition()) - 1, currentIndent);
                    }
                }
                break;
            case SYNCHRONIZED:
                currentIndent = getStmtIndent(startOffset, endOffset, EnumSet.of(JavaTokenId.RPAREN), getEndPosition(((SynchronizedTree)last).getExpression()) - 1, currentIndent);
                break;
            case TRY:
                token = findFirstNonWhitespaceToken(startOffset, endOffset);
                if (token == null || !EnumSet.of(JavaTokenId.CATCH, JavaTokenId.FINALLY).contains(token.token().id())) {
                    token = findFirstNonWhitespaceToken(startOffset, lastPos);
                    if (token != null && token.token().id() != JavaTokenId.RBRACE) {
                        t = null;
                        for (Tree res : ((TryTree)last).getResources()) {
                            if (getEndPosition(res) > startOffset) {
                                break;
                            }
                            t = res;
                        }
                        currentIndent = getStmtIndent(startOffset, endOffset, EnumSet.of(JavaTokenId.TRY, JavaTokenId.RPAREN, JavaTokenId.FINALLY), t != null ? getEndPosition(t) : lastPos, currentIndent);
                    }
                }
                break;
            case CATCH:
                currentIndent = getStmtIndent(startOffset, endOffset, EnumSet.of(JavaTokenId.RPAREN), lastPos, currentIndent);
                break;
            case WHILE_LOOP:
                currentIndent = getStmtIndent(startOffset, endOffset, EnumSet.of(JavaTokenId.RPAREN), getEndPosition(((WhileLoopTree)last).getCondition()) - 1, currentIndent);
                break;
            case BLOCK:
                boolean isStatic = ((BlockTree)last).isStatic();
                if (isStatic) {
                    token = findFirstNonWhitespaceToken(startOffset, lastPos);
                    if (token != null && token.token().id() == JavaTokenId.STATIC && token.offset() == lastPos) {
                        switch (cs.getOtherBracePlacement()) {
                            case NEW_LINE_INDENTED:
                                currentIndent += cs.getIndentSize();
                                break;
                            case NEW_LINE_HALF_INDENTED:
                                currentIndent += (cs.getIndentSize() / 2);
                                break;
                        }
                        break;
                    }
                }
                token = findFirstNonWhitespaceToken(startOffset, endOffset);
                nextTokenId = token != null ? token.token().id() : null;
                if (nextTokenId == null || nextTokenId != JavaTokenId.RBRACE) {
                    token = findFirstOtherToken(startOffset, lastPos + 1, EnumSet.of(JavaTokenId.WHITESPACE));
                    int prevTokenLineStartOffset = token != null ? context.lineStartOffset(token.offset()) : -1;
                    t = null;
                    boolean isNextLabeledStatement = false;
                    Iterator<? extends StatementTree> it = ((BlockTree)last).getStatements().iterator();
                    while (it.hasNext()) {
                        StatementTree st = it.next();
                        if (getEndPosition(st) > startOffset) {
                            isNextLabeledStatement = st.getKind() == Kind.LABELED_STATEMENT;
                            break;
                        }
                        t = st;
                    }                    
                    if (isNextLabeledStatement && cs.absoluteLabelIndent()) {
                        currentIndent = 0;
                    } else if (t != null) {
                        int i = -1;
                        if (getEndPosition(t) < prevTokenLineStartOffset) {
                            Integer newIndent = newIndents.get(prevTokenLineStartOffset);
                            i = newIndent != null ? newIndent : context.lineIndent(prevTokenLineStartOffset);
                        } else {
                            i = getCurrentIndent(t, path);
                        }
                        currentIndent = i < 0 ? currentIndent + cs.getIndentSize() : i;
                    } else if (isStatic) {
                        currentIndent += cs.getIndentSize();
                    } else if (isLeftBraceOnNewLine(lastPos, startOffset)) {
                        switch (path.size() > 1 && path.get(1).getKind() == Kind.METHOD ? cs.getMethodDeclBracePlacement() : cs.getOtherBracePlacement()) {
                            case SAME_LINE:
                            case NEW_LINE:
                                currentIndent += cs.getIndentSize();
                                break;
                            case NEW_LINE_HALF_INDENTED:
                                currentIndent += (cs.getIndentSize() - cs.getIndentSize() / 2);
                                break;
                        }
                    } else if (prevTokenLineStartOffset >= 0 && prevTokenLineStartOffset > context.lineStartOffset(lastPos)) {
                        Integer newIndent = newIndents.get(prevTokenLineStartOffset);
                        currentIndent = newIndent != null ? newIndent : context.lineIndent(prevTokenLineStartOffset);
                    } else {
                        int i = path.size() > 1 ? getCurrentIndent(path.get(1), path) : -1;
                        currentIndent = (i < 0 ? currentIndent : i) + cs.getIndentSize();
                    }
                    if (nextTokenId != null && nextTokenId == JavaTokenId.LBRACE) {
                        switch (cs.getOtherBracePlacement()) {
                            case NEW_LINE_INDENTED:
                                currentIndent += cs.getIndentSize();
                                break;
                            case NEW_LINE_HALF_INDENTED:
                                currentIndent += (cs.getIndentSize() / 2);
                                break;
                        }
                    }
                } else if (isStatic) {
                    switch (cs.getOtherBracePlacement()) {
                        case NEW_LINE_INDENTED:
                            currentIndent += cs.getIndentSize();
                            break;
                        case NEW_LINE_HALF_INDENTED:
                            currentIndent += (cs.getIndentSize() / 2);
                            break;
                    }
                } else if (!isLeftBraceOnNewLine(lastPos, startOffset)) {
                    int i = path.size() > 1 ? getCurrentIndent(path.get(1), path) : -1;
                    currentIndent = i < 0 ? currentIndent + cs.getIndentSize() : i;
                }
                break;
            case SWITCH:
               currentIndent = getSwitchIndent(startOffset, endOffset,nextTokenId,lastPos,currentIndent) ;
                break;
            case CASE:
                t = null;
                JavaTokenId tokenId = null;
                List<? extends StatementTree> statements = ((CaseTree) last).getStatements();
                if (statements != null) {
                    tokenId = JavaTokenId.COLON;
                } else {
                    Tree caseBody = ((CaseTree) last).getBody();
                    if (caseBody instanceof StatementTree) {
                        statements = Collections.singletonList((StatementTree) caseBody);
                        tokenId = JavaTokenId.ARROW;
                    } else if (getEndPosition(caseBody) > startOffset) {
                        return getContinuationIndent(path, currentIndent);
                    }
                }

                for (StatementTree st : statements) {
                    if (getEndPosition(st) > startOffset) {
                        break;
                    }
                    t = st;
                }
                    if (t != null) {
                        int i = getCurrentIndent(t, path);
                        currentIndent = i < 0 ? getStmtIndent(startOffset, endOffset, EnumSet.of(tokenId), getEndPosition(((CaseTree) last).getExpression()), currentIndent) : i; // TODO
                    } else {
                        currentIndent = getStmtIndent(startOffset, endOffset, EnumSet.of(tokenId), getEndPosition(((CaseTree) last).getExpression()), currentIndent); // TODO
                    }

                break;
            case NEW_ARRAY:
                token = findFirstNonWhitespaceToken(startOffset, endOffset);
                nextTokenId = token != null ? token.token().id() : null;
                if (nextTokenId != JavaTokenId.RBRACE) {
                    token = findFirstNonWhitespaceToken(startOffset, lastPos);
                    prevTokenId = token != null ? token.token().id() : null;
                    if (prevTokenId != null) {
                        switch (prevTokenId) {
                            case LBRACE:
                                currentIndent += cs.getIndentSize();
                                break;
                            case COMMA:
                                currentIndent = getMultilineIndent(((NewArrayTree)last).getInitializers(), path, token.offset(), currentIndent, cs.alignMultilineArrayInit(), false);
                                break;
                            case RBRACKET:
                                if (nextTokenId == JavaTokenId.LBRACE) {
                                    switch (cs.getOtherBracePlacement()) {
                                        case NEW_LINE_INDENTED:
                                            currentIndent += cs.getIndentSize();
                                            break;
                                        case NEW_LINE_HALF_INDENTED:
                                            currentIndent += (cs.getIndentSize() / 2);
                                            break;
                                    }
                                    break;
                                }
                            default:
                                currentIndent += cs.getContinuationIndentSize();
                        }
                    }
                }
                break;
            case LAMBDA_EXPRESSION:
                token = findFirstNonWhitespaceToken(startOffset, endOffset);
                nextTokenId = token != null ? token.token().id() : null;
                token = findFirstNonWhitespaceToken(startOffset, lastPos);
                prevTokenId = token != null ? token.token().id() : null;
                if (prevTokenId == JavaTokenId.ARROW && nextTokenId == JavaTokenId.LBRACE) {
                    switch (cs.getOtherBracePlacement()) {
                        case NEW_LINE_INDENTED:
                            currentIndent += cs.getIndentSize();
                            break;
                        case NEW_LINE_HALF_INDENTED:
                            currentIndent += (cs.getIndentSize() / 2);
                            break;
                    }
                } else if (nextTokenId != JavaTokenId.RPAREN) {
                    currentIndent = getContinuationIndent(path, currentIndent);
                }
                break;
            case NEW_CLASS:
                token = findFirstNonWhitespaceToken(startOffset, endOffset);
                nextTokenId = token != null ? token.token().id() : null;
                token = findFirstNonWhitespaceToken(startOffset, lastPos);
                prevTokenId = token != null ? token.token().id() : null;
                if (prevTokenId == JavaTokenId.RPAREN && nextTokenId == JavaTokenId.LBRACE) {
                    switch (cs.getClassDeclBracePlacement()) {
                        case NEW_LINE_INDENTED:
                            currentIndent += cs.getIndentSize();
                            break;
                        case NEW_LINE_HALF_INDENTED:
                            currentIndent += (cs.getIndentSize() / 2);
                            break;
                    }
                } else if (nextTokenId != JavaTokenId.RPAREN) {
                    currentIndent = getContinuationIndent(path, currentIndent);
                }
                break;
            case METHOD_INVOCATION:
                token = findFirstNonWhitespaceToken(startOffset, lastPos);
                if (token != null && token.token().id() == JavaTokenId.COMMA) {
                    currentIndent = getMultilineIndent(((MethodInvocationTree)last).getArguments(), path, token.offset(), currentIndent, cs.alignMultilineCallArgs(), true);
                } else {
                    token = findFirstNonWhitespaceToken(startOffset, endOffset);
                    if (token == null || token.token().id() != JavaTokenId.RPAREN) {
                        currentIndent = getContinuationIndent(path, currentIndent);
                    }
                }
                break;
            case ANNOTATION:
                token = findFirstNonWhitespaceToken(startOffset, lastPos);
                if (token != null && token.token().id() == JavaTokenId.COMMA) {
                    currentIndent = getMultilineIndent(((AnnotationTree)last).getArguments(), path, token.offset(), currentIndent, cs.alignMultilineAnnotationArgs(), true);
                } else {
                    token = findFirstNonWhitespaceToken(startOffset, endOffset);
                    if (token == null || token.token().id() != JavaTokenId.RPAREN) {
                        currentIndent = getContinuationIndent(path, currentIndent);
                    }
                }
                break;
            case LABELED_STATEMENT:
                token = findFirstNonWhitespaceToken(startOffset, lastPos);
                if (token == null || token.token().id() != JavaTokenId.COLON) {
                    currentIndent = getContinuationIndent(path, currentIndent);
                } else {
                    currentIndent += cs.getLabelIndent();
                }
                break;
            case SWITCH_EXPRESSION:
                currentIndent = getSwitchIndent(startOffset, endOffset,nextTokenId,lastPos,currentIndent) ;
                break;
            case RECORD:
                currentIndent = getRecordIndent(startOffset, endOffset, nextTokenId, lastPos, currentIndent);
                break;
            default:
                currentIndent = getContinuationIndent(path, currentIndent);
                break;
        }
        return currentIndent;
    }

    private int getSwitchIndent(int startOffset, int endOffset, JavaTokenId nextTokenId, int lastPos, int currentIndent) throws BadLocationException {
        LinkedList<? extends Tree> path = getPath(startOffset);
        Tree last = path.getFirst();
        TokenSequence<JavaTokenId> token = findFirstNonWhitespaceToken(startOffset, endOffset);
        boolean indentCases = cs.indentCasesFromSwitch() ;
        nextTokenId = token != null ? token.token().id() : null;
        if (nextTokenId != null && nextTokenId == JavaTokenId.RBRACE) {
            if (isLeftBraceOnNewLine(lastPos, startOffset)) {
                switch (cs.getOtherBracePlacement()) {
                    case NEW_LINE_INDENTED:
                        currentIndent += cs.getIndentSize();
                        break;
                    case NEW_LINE_HALF_INDENTED:
                        currentIndent += (cs.getIndentSize() / 2);
                        break;
                }
            }
        } else {
            Tree t = null;
            List<? extends CaseTree> cases;
            if (last.getKind() == Tree.Kind.SWITCH) {
                cases = ((SwitchTree) last).getCases();
            } else {
                cases = ((SwitchExpressionTree) last).getCases();
            }
            for (CaseTree ct : cases) {
                if (getEndPosition(ct) > startOffset) {
                    break;
                }
                t = ct;
            }
            if (t != null) {
                CaseTree ct = (CaseTree) t;
                if (nextTokenId == null || !EnumSet.of(JavaTokenId.CASE, JavaTokenId.DEFAULT).contains(nextTokenId)) {
                    t = null;
                    List<? extends StatementTree> statements = ct.getStatements();
                    if(statements == null)
                    {
                        Tree caseBody = ct.getBody();
                        if (caseBody instanceof StatementTree) {
                            statements = Collections.singletonList((StatementTree) caseBody);
                        }
                        else if (getEndPosition(caseBody) > startOffset ) {
                            return getContinuationIndent(path, currentIndent);
                        }
                    }
                    if (statements != null)
                        for (StatementTree st : statements) {
                            if (getEndPosition(st) > startOffset) {
                                break;
                            }
                        t = st;
                    }
                    if (t != null) {
                        int i = getCurrentIndent(t, path);
                        currentIndent = i < 0 ? getStmtIndent(startOffset, endOffset, EnumSet.of(JavaTokenId.COLON), getEndPosition(ct.getExpression()), currentIndent) : i; // TODO
                    } else {
                        int i = getCurrentIndent(ct, path);
                        currentIndent = i < 0 ? getStmtIndent(startOffset, endOffset, EnumSet.of(JavaTokenId.COLON), getEndPosition(ct.getExpression()), currentIndent) : i; // TODO
                        currentIndent += cs.getIndentSize();
                    }
                } else {
                    int i = getCurrentIndent(t, path);
                    currentIndent = i < 0 ? currentIndent + (indentCases ? cs.getIndentSize() : 0) : i;
                }
            } else {
                token = findFirstNonWhitespaceToken(startOffset, lastPos);
                if (token != null && token.token().id() == JavaTokenId.LBRACE) {
                    currentIndent += (indentCases ? cs.getIndentSize() : 0);
                } else {
                    if (last.getKind() == Tree.Kind.SWITCH) {
                        currentIndent = getStmtIndent(startOffset, endOffset, EnumSet.of(JavaTokenId.RPAREN), getEndPosition(((SwitchTree) last).getExpression()) - 1, currentIndent);
                    } else {
                        currentIndent = getStmtIndent(startOffset, endOffset, EnumSet.of(JavaTokenId.RPAREN), getEndPosition(((SwitchExpressionTree) last).getExpression()) - 1, currentIndent);
                    }
                }
            }
        }
        return currentIndent;
    }

    private int getRecordIndent(int startOffset, int endOffset, JavaTokenId nextTokenId, int lastPos, int currentIndent) throws BadLocationException {
        LinkedList<? extends Tree> path = getPath(startOffset);
        Tree last = path.getFirst();
        TokenSequence<JavaTokenId> token = findFirstNonWhitespaceToken(startOffset, endOffset);
        nextTokenId = token != null ? token.token().id() : null;
        if (nextTokenId != null && nextTokenId == JavaTokenId.RBRACE) {
            if (isLeftBraceOnNewLine(lastPos, startOffset)) {
                switch (cs.getClassDeclBracePlacement()) {
                    case NEW_LINE_INDENTED:
                        currentIndent += cs.getIndentSize();
                        break;
                    case NEW_LINE_HALF_INDENTED:
                        currentIndent += (cs.getIndentSize() / 2);
                        break;
                }
            }
        } else {

            token = findFirstNonWhitespaceToken(startOffset, lastPos);
            JavaTokenId prevTokenId = token != null ? token.token().id() : null;
            if (prevTokenId != null) {
                switch (prevTokenId) {
                    case LBRACE:
                        if (path.size() > 1 && path.get(1).getKind() == Kind.NEW_CLASS && isLeftBraceOnNewLine(lastPos, startOffset)) {
                            switch (cs.getClassDeclBracePlacement()) {
                                case SAME_LINE:
                                case NEW_LINE:
                                    currentIndent += cs.getIndentSize();
                                    break;
                                case NEW_LINE_HALF_INDENTED:
                                    currentIndent += (cs.getIndentSize() - cs.getIndentSize() / 2);
                                    break;
                            }
                        } else {
                            currentIndent += cs.indentTopLevelClassMembers() ? cs.getIndentSize() : 0;
                        }
                        break;
                    case COMMA:
                        List<? extends Tree> implClauses = ((ClassTree) last).getImplementsClause();
                        if (!implClauses.isEmpty() && getStartPosition(implClauses.get(0)) < token.offset()) {
                            currentIndent = getMultilineIndent(implClauses, path, token.offset(), currentIndent, cs.alignMultilineImplements(), true);
                            break;
                        }
                        List<? extends Tree> members = ((ClassTree) last).getMembers();
                        if (!members.isEmpty() && getStartPosition(members.get(0)) < token.offset()) {
                            currentIndent = getMultilineIndent(members, path, token.offset(), currentIndent, cs.alignMultilineMethodParams(), true);
                            break;
                        }
                        List<? extends TypeParameterTree> typeParams = ((ClassTree) last).getTypeParameters();
                        if (!typeParams.isEmpty() && getStartPosition(typeParams.get(0)) < token.offset()) {
                            currentIndent = getMultilineIndent(typeParams, path, token.offset(), currentIndent, cs.alignMultilineMethodParams(), true);
                            break;
                        }
                        break;
                    case IDENTIFIER:
                    case GT:
                    case GTGT:
                    case GTGTGT:
                    case RPAREN:
                        if (nextTokenId != null && nextTokenId == JavaTokenId.LBRACE) {
                            switch (cs.getClassDeclBracePlacement()) {
                                case NEW_LINE_INDENTED:
                                    currentIndent += cs.getIndentSize();
                                    break;
                                case NEW_LINE_HALF_INDENTED:
                                    currentIndent += (cs.getIndentSize() / 2);
                                    break;
                            }
                        } else {
                            currentIndent += cs.getContinuationIndentSize();
                        }
                        break;

                    default:
                        Tree t = null;
                        for (Tree member : ((ClassTree) last).getMembers()) {

                            if (member.getKind() == Tree.Kind.VARIABLE && !((VariableTree) member).getModifiers().getFlags().contains(Modifier.STATIC)) {
                                continue;
                            }
                            if (getEndPosition(member) > startOffset) {
                                break;
                            }
                            t = member;
                        }
                        if (t != null) {
                            int i = getCurrentIndent(t, path);
                            currentIndent = i < 0 ? currentIndent + (cs.indentTopLevelClassMembers() ? cs.getIndentSize() : 0) : i;
                            return currentIndent;
                        }

                        currentIndent += cs.getContinuationIndentSize();
                }
            }
        }
        return currentIndent;
    }

    private int getStartPosition(Tree last) {
        return (int) sp.getStartPosition(cut, last);
    }

    private int getEndPosition(Tree last) {
        int result = (int) sp.getEndPosition(cut, last);

        if (result == -1) {
            //see JDK-8364015:
            if (last instanceof JCClassDecl clazz &&
                (clazz.mods.flags & Flags.IMPLICIT_CLASS) != 0) {
                return getEndPosition(cut);
            }
        }

        return result;
    }

    private LinkedList<? extends Tree> getPath(final int startOffset) {
        final LinkedList<Tree> path = new LinkedList<Tree>();

        // When right at the token end move to previous token; otherwise move to the token that "contains" the offset
        if (ts.move(startOffset) == 0 && startOffset > 0 || !ts.moveNext()) {
            ts.movePrevious();
        }
        final int offset = (ts.token().id() == JavaTokenId.IDENTIFIER
                || ts.token().id().primaryCategory().startsWith("keyword") || //NOI18N
                ts.token().id().primaryCategory().startsWith("string") || //NOI18N
                ts.token().id().primaryCategory().equals("literal")) //NOI18N
                ? ts.offset() : startOffset;

        new ErrorAwareTreeScanner<Void, Void>() {

            @Override
            public Void scan(Tree node, Void p) {
                if (node != null) {
                    if (getStartPosition(node) < offset && getEndPosition(node) >= offset) {
                        super.scan(node, p);
                        if (node.getKind() != Tree.Kind.ERRONEOUS || !path.isEmpty()) {
                            path.add(node);
                        }
                    }
                }
                return null;
            }
        }.scan(parsedTree, null);

        if (path.isEmpty() || path.getFirst() == parsedTree || getEndPosition(path.getFirst()) > offset) {
            return path;
        }

        if (!path.isEmpty() && ts.move(offset) == 0) {
            if (ts.movePrevious()) {
                switch (ts.token().id()) {
                    case RPAREN:
                        if (!EnumSet.of(Kind.ENHANCED_FOR_LOOP, Kind.FOR_LOOP, Kind.IF, Kind.WHILE_LOOP, Kind.DO_WHILE_LOOP,
                                Kind.TYPE_CAST, Kind.SYNCHRONIZED).contains(path.getFirst().getKind())) {
                            path.removeFirst();
                        }
                        break;
                    case GTGTGT:
                    case GTGT:
                    case GT:
                        if (EnumSet.of(Kind.MEMBER_SELECT, Kind.CLASS, Kind.GREATER_THAN).contains(path.getFirst().getKind())) {
                            break;
                        }
                    case SEMICOLON:
                        if (path.getFirst().getKind() == Kind.FOR_LOOP
                                && ts.offset() <= getStartPosition(((ForLoopTree)path.getFirst()).getUpdate().get(0))) {
                            break;
                        }
                    case RBRACE:
                        path.removeFirst();
                        switch (path.getFirst().getKind()) {
                            case CATCH:
                                path.removeFirst();
                            case METHOD:
                            case FOR_LOOP:
                            case ENHANCED_FOR_LOOP:
                            case IF:
                            case SYNCHRONIZED:
                            case WHILE_LOOP:
                            case TRY:
                                path.removeFirst();
                        }
                        break;
                }
            }
        }

        return path;
    }

    private TokenSequence<JavaTokenId> findFirstNonWhitespaceToken(int startOffset, int endOffset) {
        return findFirstOtherToken(startOffset, endOffset, EnumSet.of(JavaTokenId.WHITESPACE, JavaTokenId.LINE_COMMENT, JavaTokenId.BLOCK_COMMENT, JavaTokenId.JAVADOC_COMMENT));
    }
    
    private TokenSequence<JavaTokenId> findFirstOtherToken(int startOffset, int endOffset, EnumSet<JavaTokenId> ids) {
        if (startOffset == endOffset) {
            return null;
        }
        ts.move(startOffset);
        boolean backward = startOffset > endOffset;
        while (backward ? ts.movePrevious() : ts.moveNext()) {
            if (backward && ts.offset() < endOffset || !backward && ts.offset() > endOffset) {
                return null;
            }
            if (!ids.contains(ts.token().id())) {
                return ts;
            }
        }
        return null;
    }

    private TokenSequence<JavaTokenId> findFirstTokenOccurrence(int startOffset, int endOffset, JavaTokenId token) {
        if (startOffset == endOffset) {
            return null;
        }
        ts.move(startOffset);
        boolean backward = startOffset > endOffset;
        while (backward ? ts.movePrevious() : ts.moveNext()) {
            if (backward && ts.offset() < endOffset || !backward && ts.offset() > endOffset) {
                return null;
            }
            if (ts.token().id() == token) {
                return ts;
            }
        }
        return null;
    }

    private boolean isLeftBraceOnNewLine(int startOffset, int endOffset) {
        ts.move(startOffset);
        while (ts.moveNext()) {
            if (ts.offset() >= endOffset) {
                return false;
            }
            if (ts.token().id() == JavaTokenId.LBRACE) {
                if (!ts.movePrevious()) {
                    return false;
                }
                return ts.token().id() == JavaTokenId.LINE_COMMENT || ts.token().id() == JavaTokenId.WHITESPACE && ts.token().text().toString().indexOf('\n') >= 0;
            }
        }
        return false;
    }
    
    private int getColumn(Tree tree) throws BadLocationException {
        int startOffset = getStartPosition(tree);
        if (startOffset < 0) {
            return -1;
        }
        int lineStartOffset = context.lineStartOffset(startOffset);
        return getCol(context.document().getText(lineStartOffset, startOffset - lineStartOffset));
    }
    
    private int getCurrentIndent(Tree tree, List<? extends Tree> path) throws BadLocationException {
        int startOffset = -1;
        switch (tree.getKind()) {
            case METHOD_INVOCATION:
                MethodInvocationTree mit = (MethodInvocationTree)tree;
                startOffset = getEndPosition(mit.getMethodSelect());
                TokenSequence<JavaTokenId> token = startOffset >= 0 ? findFirstTokenOccurrence(startOffset, getEndPosition(tree), JavaTokenId.LPAREN) : null;
                if (token != null) {
                    startOffset = token.offset();
                }
                break;
            case NEW_CLASS:
                NewClassTree nct = (NewClassTree)tree;
                startOffset = getEndPosition(nct.getIdentifier());
                token = startOffset >= 0 ? findFirstTokenOccurrence(startOffset, getEndPosition(tree), JavaTokenId.LPAREN) : null;
                if (token != null) {
                    startOffset = token.offset();
                }
                break;
            case ANNOTATION:
                AnnotationTree at = (AnnotationTree)tree;
                startOffset = getEndPosition(at.getAnnotationType());
                token = startOffset >= 0 ? findFirstTokenOccurrence(startOffset, getEndPosition(tree), JavaTokenId.LPAREN) : null;
                if (token != null) {
                    startOffset = token.offset();
                }
                break;
            case METHOD:
                MethodTree mt = (MethodTree)tree;
                startOffset = getEndPosition(mt.getReturnType());
                if (startOffset < 0) {
                    startOffset = getEndPosition(mt.getModifiers());
                }
                if (startOffset < 0) {
                    startOffset = getStartPosition(tree);
                }
                token = startOffset >= 0 ? findFirstTokenOccurrence(startOffset, mt.getBody() != null ? getStartPosition(mt.getBody()) : getEndPosition(tree), JavaTokenId.LPAREN) : null;
                if (token != null) {
                    startOffset = token.offset();
                }
                break;
        }
        if (startOffset < 0) {
            startOffset = getStartPosition(tree);
        }
        if (startOffset < 0) {
            startOffset = currentEmbeddingStartOffset;
        }
        int lineStartOffset = context.lineStartOffset(startOffset);
        Integer newIndent = newIndents.get(lineStartOffset);
        int currentIndent = newIndent != null ? newIndent : context.lineIndent(lineStartOffset);
        if (cs.absoluteLabelIndent()) {
            for (Iterator<? extends Tree> it = path.iterator(); it.hasNext();) {
                Tree t = it.next();
                if (t.getKind() == Tree.Kind.LABELED_STATEMENT && getStartPosition(t) == lineStartOffset) {
                    Tree parent = it.hasNext() ? it.next() : null;
                    if (parent != null && parent.getKind() == Kind.BLOCK) {
                        Tree stat = null;
                        for (StatementTree st : ((BlockTree)parent).getStatements()) {
                            if (getEndPosition(st) > startOffset) {
                                break;
                            }
                            stat = st;
                        }
                        if (stat != null) {
                            int i = getCurrentIndent(stat, path);
                            currentIndent = i < 0 ? currentIndent + cs.getIndentSize() : i;
                        } else {
                            int i = getCurrentIndent(parent, path);
                            currentIndent = (i < 0 ? currentIndent : i) + cs.getIndentSize();
                        }
                    }
                    break;
                }
            }
        }
        return currentIndent;
    }

    private int getContinuationIndent(LinkedList<? extends Tree> path, int currentIndent) throws BadLocationException {
        for (Tree tree : path) {
            switch (tree.getKind()) {
                case CLASS:
                case INTERFACE:
                case ENUM:
                case ANNOTATION_TYPE:
                case VARIABLE:
                case METHOD:
                case TRY:
                case RETURN:
                case BLOCK:
                case FOR_LOOP:
                case SWITCH:
                case THROW:
                case WHILE_LOOP:
                case IF:
                case EXPRESSION_STATEMENT:
                case SYNCHRONIZED:
                case ASSERT:
                case CONTINUE:
                case LABELED_STATEMENT:
                case ENHANCED_FOR_LOOP:
                case BREAK:
                case EMPTY_STATEMENT:
                case DO_WHILE_LOOP:
                    int i = getCurrentIndent(tree, path);
                    return (i < 0 ? currentIndent : i) + cs.getContinuationIndentSize();
                case METHOD_INVOCATION:
                case NEW_CLASS:
                case LAMBDA_EXPRESSION:
                case ANNOTATION:
                    return currentIndent + cs.getContinuationIndentSize();
                case ASSIGNMENT:
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
                    if (cs.alignMultilineAssignment()) {
                        int c = getColumn(tree);
                        return c < 0 ? currentIndent : c;
                    }
                    break;
                case AND:
                case CONDITIONAL_AND:
                case CONDITIONAL_OR:
                case DIVIDE:
                case EQUAL_TO:
                case GREATER_THAN:
                case GREATER_THAN_EQUAL:
                case LEFT_SHIFT:
                case LESS_THAN:
                case LESS_THAN_EQUAL:
                case MINUS:
                case MULTIPLY:
                case NOT_EQUAL_TO:
                case OR:
                case PLUS:
                case REMAINDER:
                case RIGHT_SHIFT:
                case UNSIGNED_RIGHT_SHIFT:
                case XOR:
                    if (cs.alignMultilineBinaryOp()) {
                        int c = getColumn(tree);
                        return c < 0 ? currentIndent : c;
                    }
                    break;
                case CONDITIONAL_EXPRESSION:
                    if (cs.alignMultilineTernaryOp()) {
                        int c = getColumn(tree);
                        return c < 0 ? currentIndent : c;
                    }
                    break;
            }
        }
        return currentIndent + cs.getContinuationIndentSize();
    }

    private int getStmtIndent(int startOffset, int endOffset, Set<JavaTokenId> expectedTokenIds, int expectedTokenOffset, int currentIndent) {
        TokenSequence<JavaTokenId> token = findFirstNonWhitespaceToken(startOffset, expectedTokenOffset);
        if (token != null && expectedTokenIds.contains(token.token().id())) {
            token = findFirstNonWhitespaceToken(startOffset, endOffset);
            if (token != null && token.token().id() == JavaTokenId.LBRACE) {
                switch (cs.getOtherBracePlacement()) {
                    case NEW_LINE_INDENTED:
                        currentIndent += cs.getIndentSize();
                        break;
                    case NEW_LINE_HALF_INDENTED:
                        currentIndent += (cs.getIndentSize() / 2);
                        break;
                }
            } else {
                currentIndent += cs.getIndentSize();
            }
        } else {
            currentIndent += cs.getContinuationIndentSize();
        }
        return currentIndent;
    }

    private int getMultilineIndent(List<? extends Tree> trees, LinkedList<? extends Tree> path, int commaOffset, int currentIndent, boolean align, boolean addContinuationIndent) throws BadLocationException {
        Tree tree = null;
        Tree first = null;
        for (Tree t : trees) {
            if (first == null) {
                first = t;
            }
            if (getEndPosition(t) > commaOffset) {
                break;
            }
            tree = t;
        }
        if (tree != null && findFirstNonWhitespaceToken(commaOffset, getEndPosition(tree)) == null) {
            int firstStartOffset = getStartPosition(first);
            int startOffset = first == tree ? firstStartOffset : getStartPosition(tree);
            if (firstStartOffset < 0 || startOffset < 0) {
                currentIndent = addContinuationIndent ? getContinuationIndent(path, currentIndent) : currentIndent + cs.getIndentSize();
            } else {
                int firstLineStartOffset = context.lineStartOffset(firstStartOffset);
                int lineStartOffset = firstStartOffset == startOffset ? firstLineStartOffset : context.lineStartOffset(startOffset);
                if (firstLineStartOffset != lineStartOffset) {
                    Integer newIndent = newIndents.get(lineStartOffset);
                    currentIndent = newIndent != null ? newIndent : context.lineIndent(lineStartOffset);
                } else if (align) {
                    currentIndent = getCol(context.document().getText(lineStartOffset, startOffset - lineStartOffset));
                } else {
                    currentIndent = addContinuationIndent ? getContinuationIndent(path, currentIndent) : currentIndent + cs.getIndentSize();
                }
            }
        } else {
            currentIndent = addContinuationIndent ? getContinuationIndent(path, currentIndent) : currentIndent + cs.getIndentSize();
        }
        return currentIndent;
    }

    private int getCol(String text) {
        int col = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\t') {
                col += cs.getTabSize();
                col -= (col % cs.getTabSize());
            } else {
                col++;
            }
        }
        return col;
    }
    
    public static class Factory implements IndentTask.Factory {

        @Override
        public IndentTask createTask(Context context) {
            if (!NoJavacHelper.hasWorkingJavac())
                return null;
            return new Reindenter(context);
        }
    }
}
