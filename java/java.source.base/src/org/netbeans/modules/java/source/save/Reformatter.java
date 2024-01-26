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

import com.sun.source.tree.*;
import com.sun.source.tree.LambdaExpressionTree.BodyKind;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.*;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import java.net.URL;
import java.util.*;
import javax.lang.model.element.Name;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.lang.model.element.Modifier;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.guards.DocumentGuards;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import static org.netbeans.api.java.lexer.JavaTokenId.*;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.CodeStyle.WrapStyle;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.ReformatTask;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.NoJavacHelper;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.util.Pair;

/**
 *
 * @author Dusan Balek
 */
public class Reformatter implements ReformatTask {

    private static final Object CT_HANDLER_DOC_PROPERTY = "code-template-insert-handler"; // NOI18N

    private Source source;
    private Context context;
    private CompilationController controller;
    private Embedding currentEmbedding;
    private Document doc;

    public Reformatter(Source source, Context context) {
        this.source = source;
        this.context = context;
        this.doc = context.document();
    }

    @Override
    public void reformat() throws BadLocationException {
        CodeStyle cs = (CodeStyle) doc.getProperty(CodeStyle.class);
        if (cs == null) {
            cs = CodeStyle.getDefault(doc);
        }
        List<Context.Region> indentRegions = context.indentRegions();
        Collections.reverse(indentRegions);
        for (Context.Region region : indentRegions) {
            if (initRegionData(region)) {
                reformatImpl(region, cs);
            }
        }
    }

    public static String reformat(String text, CodeStyle style) {
        return reformat(text, style, style.getRightMargin());
    }

    public static String reformat(String text, CodeStyle style, int rightMargin) {
        StringBuilder sb = new StringBuilder(text);
            ClassPath empty = ClassPathSupport.createClassPath(new URL[0]);
            ClasspathInfo cpInfo = ClasspathInfo.create(JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries(), empty, empty);
            JavacTaskImpl javacTask = JavacParser.createJavacTask(cpInfo, null, null, null, null, null, null, null, Arrays.asList(FileObjects.memoryFileObject("","Scratch.java", text)));
            com.sun.tools.javac.util.Context ctx = javacTask.getContext();
            JavaCompiler.instance(ctx).genEndPos = true;
            CompilationUnitTree tree = javacTask.parse().iterator().next(); //NOI18N
            SourcePositions sp = JavacTrees.instance(ctx).getSourcePositions();
            TokenSequence<JavaTokenId> tokens = TokenHierarchy.create(text, JavaTokenId.language()).tokenSequence(JavaTokenId.language());
            for (Diff diff : Pretty.reformat(text, tokens, new TreePath(tree), sp, style, rightMargin)) {
                int start = diff.getStartOffset();
                int end = diff.getEndOffset();
                sb.delete(start, end);
                String t = diff.getText();
                if (t != null && t.length() > 0) {
                    sb.insert(start, t);
                }
            }

        return sb.toString();
    }

    private boolean initRegionData(final Context.Region region) {
        if (controller == null || (currentEmbedding != null
                && !(currentEmbedding.containsOriginalOffset(region.getStartOffset())
                && currentEmbedding.containsOriginalOffset(region.getEndOffset())))) {
            try {
                if (JavacParser.MIME_TYPE.equals(context.mimePath())) {
                    controller = JavaSourceAccessor.getINSTANCE().createCompilationController(source, null);
                } else {
                    ParserManager.parse(Collections.singletonList(source), new UserTask() {
                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            Parser.Result result = findEmbeddedJava(resultIterator);
                            if (result != null) {
                                controller = CompilationController.get(result);
                            }
                        }

                        private Parser.Result findEmbeddedJava(final ResultIterator theMess) throws ParseException {
                            final Collection<Embedding> todo = new LinkedList<Embedding>();
                            //BFS should perform better than DFS in this dark.
                            for (Embedding embedding : theMess.getEmbeddings()) {
                                if (JavacParser.MIME_TYPE.equals(embedding.getMimeType())
                                        && embedding.containsOriginalOffset(region.getStartOffset())
                                        && embedding.containsOriginalOffset(region.getEndOffset())) {
                                    return theMess.getResultIterator(currentEmbedding = embedding).getParserResult();
                                } else {
                                    todo.add(embedding);
                                }
                            }
                            for (Embedding embedding : todo) {
                                Parser.Result result = findEmbeddedJava(theMess.getResultIterator(embedding));
                                if (result != null) {
                                    return result;
                                }
                            }
                            return null;
                        }
                    });
                }
                if (controller == null) {
                    return false;
                }
                controller.toPhase(JavaSource.Phase.PARSED);
            } catch (Exception ex) {
                return false;
            }
        }
        return true;
    }

    private void reformatImpl(Context.Region region, CodeStyle cs) throws BadLocationException {
        boolean templateEdit = doc.getProperty(CT_HANDLER_DOC_PROPERTY) != null;
        int startOffset = region.getStartOffset();
        int endOffset = region.getEndOffset();
        startOffset = controller.getSnapshot().getEmbeddedOffset(startOffset);
        if (startOffset < 0)
            return;
        endOffset = controller.getSnapshot().getEmbeddedOffset(endOffset);
        if (endOffset < 0)
            return;
        int embeddingOffset = -1;
        int firstLineIndent = -1;
        if (!"text/x-java".equals(context.mimePath())) { //NOI18N
            firstLineIndent = context.lineIndent(context.lineStartOffset(region.getStartOffset()));
            TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
            if (ts != null) {
                ts.move(startOffset);
                if (ts.moveNext()) {
                    if (ts.token().id() == WHITESPACE) {
                        String t = ts.token().text().toString();
                        if (ts.offset() < startOffset)
                            t = t.substring(startOffset - ts.offset());
                        if (t.indexOf('\n') < 0) //NOI18N
                            embeddingOffset = ts.offset() + ts.token().length();
                    } else {
                        embeddingOffset = startOffset;
                    }
                }
                ts.move(endOffset);
                if (ts.moveNext() && ts.token().id() == WHITESPACE) {
                    String t = ts.token().text().toString();
                    if (ts.offset() + t.length() > endOffset)
                        t = t.substring(0, endOffset - ts.offset());
                    int i = t.lastIndexOf('\n'); //NOI18N
                    if (i >= 0)
                        endOffset -= (t.length() - i);
                }
            }
        }
        if (startOffset >= endOffset)
            return;
        TreePath path = getCommonPath(startOffset, endOffset);
        if (path == null)
            return;
        DocumentGuards guards = LineDocumentUtils.as(doc, DocumentGuards.class);
        for (Diff diff : Pretty.reformat(controller, path, cs, startOffset, endOffset, templateEdit, firstLineIndent)) {
            int start = diff.getStartOffset();
            int end = diff.getEndOffset();
            String text = diff.getText();
            if (startOffset > end)
                continue;
            if (endOffset < start)
                continue;
            if (endOffset == start && endOffset < doc.getLength() && (text == null || !text.trim().equals("}"))) //NOI18N
                continue;
            if (embeddingOffset >= start)
                continue;
            if (guards != null && guards.isPositionGuarded(start, false))
                continue;
            if (startOffset >= start) {
                if (text != null && text.length() > 0) {
                    TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                    if (ts == null)
                        continue;
                    if (ts.move(startOffset) == 0) {
                        if (!ts.movePrevious() && !ts.moveNext())
                            continue;
                    } else {
                        if (!ts.moveNext() && !ts.movePrevious())
                            continue;
                    }
                    if (ts.token().id() == WHITESPACE) {
                        String t = ts.token().text().toString();
                        t = t.substring(0, startOffset - ts.offset());
                        if (ts.movePrevious() && ts.token().id() == LINE_COMMENT)
                            t = "\n" + t; //NOI18N
                        if (templateEdit) {
                            int idx = t.lastIndexOf('\n'); //NOI18N
                            if (idx >= 0) {
                                t = t.substring(idx + 1);
                                idx = text.lastIndexOf('\n'); //NOI18N
                                if (idx >= 0)
                                    text = text.substring(idx + 1);
                                if (text.trim().length() > 0)
                                    text = null;
                                else if (text.length() > t.length())
                                    text = text.substring(t.length());
                                else
                                    text = null;
                            } else {
                                text = null;
                            }
                        } else {
                            int idx1 = 0;
                            int idx2 = 0;
                            int lastIdx1 = 0;
                            int lastIdx2 = 0;
                            while ((idx1 = t.indexOf('\n', lastIdx1)) >=0 && (idx2 = text.indexOf('\n', lastIdx2)) >= 0) { //NOI18N
                                lastIdx1 = idx1 + 1;
                                lastIdx2 = idx2 + 1;
                            }
                            if ((idx2 = text.lastIndexOf('\n')) >= 0 && idx2 >= lastIdx2) { //NOI18N
                                if (lastIdx1 == 0) {
                                    t = null;
                                } else {
                                    text = text.substring(idx2 + 1);
                                    t = t.substring(lastIdx1);
                                }
                            } else if ((idx1 = t.lastIndexOf('\n')) >= 0 && idx1 >= lastIdx1) { //NOI18N
                                t = t.substring(idx1 + 1);
                                text = text.substring(lastIdx2);
                            } else {
                                t = t.substring(lastIdx1);
                                text = text.substring(lastIdx2);
                            }
                            if (text != null && t != null)
                                text = text.length() > t.length() ? text.substring(t.length()) : null;
                        }
                    } else if (startOffset > 0) {
                        continue;
                    }
                }
                start = startOffset;
            }
            if (endOffset < end) {
                if (text != null && text.length() > 0 && !templateEdit) {
                    TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                    if (ts != null) {
                        ts.move(endOffset);
                        String t = null;
                        if (ts.moveNext()) {
                            switch (ts.token().id()) {
                                case WHITESPACE:
                                    t = ts.token().text().toString();
                                    t = t.substring(endOffset - ts.offset());
                                    break;
                                case JAVADOC_COMMENT:
                                case BLOCK_COMMENT:
                                    t = ts.token().text().toString();
                                    int idx = endOffset - ts.offset();
                                    while (idx < t.length() && (t.charAt(idx) <= ' ')) {
                                        idx++;
                                    }
                                    t = t.substring(endOffset - ts.offset(), idx);
                                    break;
                            }
                        }
                        if (t != null) {
                            int idx1, idx2;
                            while ((idx1 = t.lastIndexOf('\n')) >=0 && (idx2 = text.lastIndexOf('\n')) >= 0) { //NOI18N
                                t = t.substring(0, idx1);
                                text = text.substring(0, idx2);
                            }
                            text = text.length() > t.length() ? text.substring(0, text.length() - t.length()) : null;
                        }
                    }
                }
                end = endOffset;
            }
            start = controller.getSnapshot().getOriginalOffset(start);
            end = controller.getSnapshot().getOriginalOffset(end);
            if (start == (-1) || end == (-1) || start < region.getStartOffset() || end > region.getEndOffset()) continue;
            if (end - start > 0)
                doc.remove(start, end - start);
            if (text != null && text.length() > 0)
                doc.insertString(start, text, null);
        }
        return;
    }

    @Override
    public ExtraLock reformatLock() {
        return JavacParser.MIME_TYPE.equals(source.getMimeType()) ? null : new ExtraLock() {
            public void lock() {
                Utilities.acquireParserLock();
            }
            public void unlock() {
                Utilities.releaseParserLock();
            }
        };
    }

    private TreePath getCommonPath(int startOffset, int endOffset) {
        TreeUtilities tu = controller.getTreeUtilities();
        TreePath startPath = tu.pathFor(startOffset);
        com.sun.tools.javac.util.List<Tree> reverseStartPath = com.sun.tools.javac.util.List.<Tree>nil();
        for (Tree t : startPath)
            reverseStartPath = reverseStartPath.prepend(t);
        TreePath endPath = tu.pathFor(endOffset);
        com.sun.tools.javac.util.List<Tree> reverseEndPath = com.sun.tools.javac.util.List.<Tree>nil();
        for (Tree t : endPath)
            reverseEndPath = reverseEndPath.prepend(t);
        TreePath path = null;
        TreePath statementPath = null;
        while(reverseStartPath.head != null && reverseStartPath.head == reverseEndPath.head) {
            path = reverseStartPath.head instanceof CompilationUnitTree ? new TreePath((CompilationUnitTree)reverseStartPath.head) : new TreePath(path, reverseStartPath.head);
            if (reverseStartPath.head instanceof StatementTree)
                statementPath = path;
            reverseStartPath = reverseStartPath.tail;
            reverseEndPath = reverseEndPath.tail;
        }
        return statementPath != null ? statementPath : path;
    }

    public static class Factory implements ReformatTask.Factory {

        public ReformatTask createTask(Context context) {
            if (!NoJavacHelper.hasWorkingJavac())
                return null;
            Source source = Source.create(context.document());
            return source != null ? new Reformatter(source, context) : null;
        }
    }

    private static class Pretty extends ErrorAwareTreePathScanner<Boolean, Void> {

        private static final String OPERATOR = "operator"; //NOI18N
        private static final String EMPTY = ""; //NOI18N
        private static final String SPACE = " "; //NOI18N
        private static final String NEWLINE = "\n"; //NOI18N
        private static final String LEADING_STAR = "*"; //NOI18N
        private static final String P_TAG = "<p>"; //NOI18N
        private static final String END_P_TAG = "<p/>"; //NOI18N
        private static final String CODE_TAG = "<code>"; //NOI18N
        private static final String CODE_END_TAG = "</code>"; //NOI18N
        private static final String PRE_TAG = "<pre>"; //NOI18N
        private static final String PRE_END_TAG = "</pre>"; //NOI18N
        private static final String JDOC_CODE_TAG = "@code"; //NOI18N
        private static final String JDOC_DOCROOT_TAG = "@docRoot"; //NOI18N
        private static final String JDOC_EXCEPTION_TAG = "@exception"; //NOI18N
        private static final String JDOC_INHERITDOC_TAG = "@inheritDoc"; //NOI18N
        private static final String JDOC_LINK_TAG = "@link"; //NOI18N
        private static final String JDOC_LINKPLAIN_TAG = "@linkplain"; //NOI18N
        private static final String JDOC_LITERAL_TAG = "@literal"; //NOI18N
        private static final String JDOC_PARAM_TAG = "@param"; //NOI18N
        private static final String JDOC_RETURN_TAG = "@return"; //NOI18N
        private static final String JDOC_THROWS_TAG = "@throws"; //NOI18N
        private static final String JDOC_VALUE_TAG = "@value"; //NOI18N
        private static final String JDOC_SNIPPET_TAG = "@snippet"; //NOI18N
        private static final String JDOC_SUMMARY_TAG = "@summary"; //NOI18N
        private static final String ERROR = "<error>"; //NOI18N

        private final String fText;
        private final SourcePositions sp;
        private final CodeStyle cs;

        private final int rightMargin;
        private final int tabSize;
        private final int indentSize;
        private final int continuationIndentSize;
        private final boolean expandTabToSpaces;

        private TokenSequence<JavaTokenId> tokens;
        private int indent;
        private boolean continuationIndent;
        private int col;
        private int endPos;
        private int maxPreservedBlankLines;
        private int lastBlankLines;
        private int lastBlankLinesTokenIndex;
        private Diff lastBlankLinesDiff;
        private int lastNewLineOffset;
        private boolean afterAnnotation;
        private boolean wrapAnnotation;
        private WrapAbort checkWrap;
        private boolean fieldGroup;
        private boolean templateEdit;
        private final LinkedList<Diff> diffs = new LinkedList<>();
        private DanglingElseChecker danglingElseChecker = new DanglingElseChecker();
        private CompilationUnitTree root;
        private int startOffset;
        private int endOffset;
        private int tpLevel;
        private boolean eof = false;
        private boolean bof = false;
        private boolean insideAnnotation = false;
        private int lastIndent = 0;
        private boolean isLastIndentContinuation = false;

        private Pretty(CompilationInfo info, TreePath path, CodeStyle cs, int startOffset, int endOffset, boolean templateEdit) {
            this(info.getText(), info.getTokenHierarchy().tokenSequence(JavaTokenId.language()),
                    path, info.getTrees().getSourcePositions(), cs, startOffset, endOffset, cs.getRightMargin());
            this.templateEdit = templateEdit;
        }

        private Pretty(String text, TokenSequence<JavaTokenId> tokens, TreePath path, SourcePositions sp, CodeStyle cs, int startOffset, int endOffset, int rightMargin) {
            this.fText = text;
            this.sp = sp;
            this.cs = cs;
            this.rightMargin = rightMargin > 0 ? rightMargin : Integer.MAX_VALUE;
            this.tabSize = cs.getTabSize();
            this.indentSize = cs.getIndentSize();
            this.continuationIndentSize = cs.getContinuationIndentSize();
            this.expandTabToSpaces = cs.expandTabToSpaces();
            this.maxPreservedBlankLines = insideBlock(path) ? cs.getMaximumBlankLinesInCode() : cs.getMaximumBlankLinesInDeclarations();
            this.lastBlankLines = -1;
            this.lastBlankLinesTokenIndex = -1;
            this.lastBlankLinesDiff = null;
            this.lastNewLineOffset = -1;
            this.afterAnnotation = false;
            this.wrapAnnotation = false;
            this.fieldGroup = false;
            Tree tree = path.getLeaf();
            this.indent = this.lastIndent = tokens != null ? getIndentLevel(tokens, path) : 0;
            this.col = this.indent;
            this.tokens = tokens;
            if (tree.getKind() == Tree.Kind.COMPILATION_UNIT) {
                tokens.moveEnd();
                tokens.movePrevious();
            } else {
                tokens.move((int)sp.getEndPosition(path.getCompilationUnit(), tree));
                if (!tokens.moveNext())
                    tokens.movePrevious();
            }
            this.endPos = tokens.offset();
            if (tree.getKind() == Tree.Kind.COMPILATION_UNIT) {
                tokens.moveStart();
                bof = true;
            } else {
                tokens.move((int)sp.getStartPosition(path.getCompilationUnit(), tree));
            }
            tokens.moveNext();
            this.root = path.getCompilationUnit();
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.tpLevel = 0;
        }

        public static LinkedList<Diff> reformat(CompilationInfo info, TreePath path, CodeStyle cs, int startOffset, int endOffset, boolean templateEdit, int firstLineIndent) {
            Pretty pretty = new Pretty(info, path, cs, startOffset, endOffset, templateEdit);
            if (pretty.indent >= 0) {
                if (firstLineIndent >= 0)
                    pretty.indent = pretty.lastIndent = firstLineIndent;
                pretty.scan(path, null);
            }
            if (path.getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
                CompilationUnitTree cut = (CompilationUnitTree) path.getLeaf();
                List<? extends Tree> typeDecls = cut.getTypeDecls();
                int size = typeDecls.size();
                int cnt = size > 0 && org.netbeans.api.java.source.TreeUtilities.CLASS_TREE_KINDS.contains(typeDecls.get(size - 1).getKind()) ? cs.getBlankLinesAfterClass() : 1;
                if (cnt < 1)
                    cnt = 1;
                String s = pretty.getNewlines(cnt);
                pretty.tokens.moveEnd();
                pretty.tokens.movePrevious();
                if (pretty.tokens.token().id() != WHITESPACE) {
                    if (!pretty.tokens.token().text().toString().endsWith(s)) {
                        String text = info.getText();
                        pretty.diffs.addFirst(new Diff(text.length(), text.length(), s));
                    }
                } else if (!s.contentEquals(pretty.tokens.token().text())) {
                    pretty.diffs.addFirst(new Diff(pretty.tokens.offset(), pretty.tokens.offset() + pretty.tokens.token().length(), s));
                }
            }
            return pretty.diffs;
        }

        public static LinkedList<Diff> reformat(String text, TokenSequence<JavaTokenId> tokens, TreePath path, SourcePositions sp, CodeStyle cs, int rightMargin) {
            Pretty pretty = new Pretty(text, tokens, path, sp, cs, 0, text.length(), rightMargin);
            pretty.scan(path, null);
            CompilationUnitTree cut = (CompilationUnitTree) path.getLeaf();
            List<? extends Tree> typeDecls = cut.getTypeDecls();
            int size = typeDecls.size();
            int cnt = size > 0 && org.netbeans.api.java.source.TreeUtilities.CLASS_TREE_KINDS.contains(typeDecls.get(size - 1).getKind()) ? cs.getBlankLinesAfterClass() : 1;
            if (cnt < 1)
                cnt = 1;
            String s = pretty.getNewlines(cnt);
            tokens.moveEnd();
            tokens.movePrevious();
            if (tokens.token().id() != WHITESPACE)
                pretty.diffs.addFirst(new Diff(text.length(), text.length(), s));
            else if (!s.contentEquals(tokens.token().text()))
                pretty.diffs.addFirst(new Diff(tokens.offset(), tokens.offset() + tokens.token().length(), s));
            return pretty.diffs;
        }

        @Override
        public Boolean scan(Tree tree, Void p) {
            int lastEndPos = endPos;
            if (tree != null && tree.getKind() != Tree.Kind.COMPILATION_UNIT) {
                if (tree instanceof FakeBlock) {
                    endPos = Integer.MAX_VALUE;
                } else {
                    endPos = (int)sp.getEndPosition(getCurrentPath().getCompilationUnit(), tree);
                }
                if (tree.getKind() != Tree.Kind.ERRONEOUS && tree.getKind() != Tree.Kind.BLOCK
                        && (tree.getKind() != Tree.Kind.CLASS || getCurrentPath().getLeaf().getKind() != Tree.Kind.NEW_CLASS)
                        && (tree.getKind() != Tree.Kind.NEW_ARRAY)) {
                    int startPos = (int)sp.getStartPosition(getCurrentPath().getCompilationUnit(), tree);
                    if (startPos >= 0 && startPos > tokens.offset()) {
                        tokens.move(startPos);
                        if (!tokens.moveNext())
                            tokens.movePrevious();
                    }
                    if (startPos >= endPos)
                        endPos = -1;
                }
            }
            try {
                if (endPos < 0)
                    return false;
                if (tokens.offset() > endPos)
                    return true;

                Boolean ret;

                ret = super.scan(tree, p);

                return ret != null ? ret : true;
            }
            finally {
                endPos = lastEndPos;
            }
        }

        @Override
        public Boolean visitCompilationUnit(CompilationUnitTree node, Void p) {
            ExpressionTree pkg = node.getPackageName();
            if (pkg != null) {
                blankLines(cs.getBlankLinesBeforePackage());
                if (!node.getPackageAnnotations().isEmpty()) {
                    wrapList(cs.wrapAnnotations(), false, false, COMMA, node.getPackageAnnotations());
                    newline();
                }
                accept(PACKAGE);
                boolean old = continuationIndent;
                try {
                    continuationIndent = true;
                    space();
                    scan(pkg, p);
                    accept(SEMICOLON);
                } finally {
                    continuationIndent = old;
                }
                blankLines(cs.getBlankLinesAfterPackage());
            }
            List<? extends ImportTree> imports = node.getImports();
            if (imports != null && !imports.isEmpty()) {
                blankLines(cs.getBlankLinesBeforeImports());
                for (ImportTree imp : imports) {
                    newline();
                    scan(imp, p);
                }
                blankLines(cs.getBlankLinesAfterImports());
            }
            boolean semiRead = false;
            for (Tree typeDecl : node.getTypeDecls()) {
                if (semiRead && typeDecl.getKind() == Tree.Kind.EMPTY_STATEMENT)
                    continue;
                if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                    blankLines(cs.getBlankLinesBeforeClass());
                }
                scan(typeDecl, p);
                int index = tokens.index();
                int c = col;
                Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                if (accept(SEMICOLON) == SEMICOLON) {
                    semiRead = true;
                } else {
                    rollback(index, c, d);
                    semiRead = false;
                }
                if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind()))
                    blankLines(cs.getBlankLinesAfterClass());
            }
            return true;
        }

        @Override
        public Boolean visitModule(ModuleTree node, Void p) {
            if (node.getModuleType() == ModuleTree.ModuleKind.OPEN) {
                accept(OPEN);
                space();
            }
            accept(MODULE);
            boolean old = continuationIndent;
            try {
                continuationIndent = true;
                space();
                scan(node.getName(), p);
            } finally {
                continuationIndent = old;
            }
            CodeStyle.BracePlacement bracePlacement = cs.getModuleDeclBracePlacement();
            boolean spaceBeforeLeftBrace = cs.spaceBeforeModuleDeclLeftBrace();
            int oldIndent = indent = lastIndent;
            int halfIndent = lastIndent;
            switch(bracePlacement) {
                case SAME_LINE:
                    spaces(spaceBeforeLeftBrace ? 1 : 0, tokens.offset() < startOffset);
                    accept(LBRACE);
                    indent = lastIndent + indentSize;
                    break;
                case NEW_LINE:
                    newline();
                    accept(LBRACE);
                    indent = lastIndent + indentSize;
                    break;
                case NEW_LINE_HALF_INDENTED:
                    int oldLast = lastIndent;
                    indent = lastIndent + (indentSize >> 1);
                    halfIndent = indent;
                    newline();
                    accept(LBRACE);
                    indent = oldLast + indentSize;
                    break;
                case NEW_LINE_INDENTED:
                    indent = lastIndent + indentSize;
                    halfIndent = indent;
                    newline();
                    accept(LBRACE);
                    break;
            }
            if (node.getDirectives().isEmpty()) {
                newline();
            } else {
                if (!cs.indentTopLevelClassMembers())
                    indent = oldIndent;
                blankLines(cs.getBlankLinesAfterModuleHeader());
                boolean first = true;
                for (Tree directive : node.getDirectives()) {
                    if (!first)
                        blankLines(cs.getBlankLinesBeforeModuleDirectives());
                    scan(directive, p);
                    blankLines(cs.getBlankLinesAfterModuleDirectives());
                }
                if (lastBlankLinesTokenIndex < 0)
                    newline();
                blankLines(cs.getBlankLinesBeforeModuleClosingBrace());
            }
            indent = halfIndent;
            Diff diff = diffs.isEmpty() ? null : diffs.getFirst();
            if (diff != null && diff.end == tokens.offset()) {
                if (diff.text != null) {
                    int idx = diff.text.lastIndexOf('\n'); //NOI18N
                    if (idx < 0)
                        diff.text = getIndent();
                    else
                        diff.text = diff.text.substring(0, idx + 1) + getIndent();

                }
                String spaces = diff.text != null ? diff.text : getIndent();
                if (spaces.equals(fText.substring(diff.start, diff.end)))
                    diffs.removeFirst();
            } else if (tokens.movePrevious()) {
                if (tokens.token().id() == WHITESPACE) {
                    String text =  tokens.token().text().toString();
                    int idx = text.lastIndexOf('\n'); //NOI18N
                    if (idx >= 0) {
                        text = text.substring(idx + 1);
                        String ind = getIndent();
                        if (!ind.equals(text))
                            addDiff(new Diff(tokens.offset() + idx + 1, tokens.offset() + tokens.token().length(), ind));
                    } else if (tokens.movePrevious()) {
                        if (tokens.token().id() == LINE_COMMENT) {
                            tokens.moveNext();
                            String ind = getIndent();
                            if (!ind.equals(text))
                                addDiff(new Diff(tokens.offset(), tokens.offset() + tokens.token().length(), ind));

                        } else {
                            tokens.moveNext();
                        }
                    }
                }
                tokens.moveNext();
            }
            col = indent();
            accept(RBRACE);
            indent = lastIndent = oldIndent;
            return true;
        }

        @Override
        public Boolean visitExports(ExportsTree node, Void p) {
            accept(EXPORTS);
            boolean old = continuationIndent;
            try {
                continuationIndent = true;
                space();
                scan(node.getPackageName(), p);
                if (node.getModuleNames() != null) {
                    wrapToken(cs.wrapExportsToKeyword(), 1, TO);
                    wrapList(cs.wrapExportsToList(), cs.alignMultilineExports(), true, COMMA, node.getModuleNames());
                }
                accept(SEMICOLON);
            } finally {
                continuationIndent = old;
            }
            return true;
        }

        @Override
        public Boolean visitOpens(OpensTree node, Void p) {
            accept(OPENS);
            boolean old = continuationIndent;
            try {
                continuationIndent = true;
                space();
                scan(node.getPackageName(), p);
                if (node.getModuleNames() != null) {
                    wrapToken(cs.wrapOpensToKeyword(), 1, TO);
                    wrapList(cs.wrapOpensToList(), cs.alignMultilineOpens(), true, COMMA, node.getModuleNames());
                }
                accept(SEMICOLON);
            } finally {
                continuationIndent = old;
            }
            return true;
        }

        @Override
        public Boolean visitProvides(ProvidesTree node, Void p) {
            accept(PROVIDES);
            boolean old = continuationIndent;
            try {
                continuationIndent = true;
                space();
                scan(node.getServiceName(), p);
                if (node.getImplementationNames() != null) {
                    wrapToken(cs.wrapProvidesWithKeyword(), 1, WITH);
                    wrapList(cs.wrapProvidesWithList(), cs.alignMultilineProvides(), true, COMMA, node.getImplementationNames());
                }
                accept(SEMICOLON);
            } finally {
                continuationIndent = old;
            }
            return true;
        }

        @Override
        public Boolean visitRequires(RequiresTree node, Void p) {
            accept(REQUIRES);
            boolean old = continuationIndent;
            try {
                continuationIndent = true;
                space();
                if (node.isStatic() || node.isTransitive()) {
                    JavaTokenId id = accept(STATIC, TRANSITIVE);
                    space();
                    switch (id) {
                        case STATIC:
                            if (node.isTransitive()) {
                                accept(TRANSITIVE);
                                space();
                            }
                            break;
                        case TRANSITIVE:
                            if (node.isStatic()) {
                                accept(STATIC);
                                space();
                            }
                            break;
                    }
                }
                scan(node.getModuleName(), p);
                accept(SEMICOLON);
            } finally {
                continuationIndent = old;
            }
            return true;
        }

        @Override
        public Boolean visitUses(UsesTree node, Void p) {
            accept(USES);
            boolean old = continuationIndent;
            try {
                continuationIndent = true;
                space();
                scan(node.getServiceName(), p);
                accept(SEMICOLON);
            } finally {
                continuationIndent = old;
            }
            return true;
        }

        @Override
        public Boolean visitImport(ImportTree node, Void p) {
            accept(IMPORT);
            boolean old = continuationIndent;
            try {
                continuationIndent = true;
                space();
                if (node.isStatic()) {
                    accept(STATIC);
                    space();
                }
                scan(node.getQualifiedIdentifier(), p);
                accept(SEMICOLON);
            } finally {
                continuationIndent = old;
            }
            return true;
        }

        @Override
        public Boolean visitClass(ClassTree node, Void p) {
            if (node.getKind() == Kind.RECORD) {
                return scanRecord(node, p);
            }
            Tree parent = getCurrentPath().getParentPath().getLeaf();
            if (parent.getKind() != Tree.Kind.NEW_CLASS && (parent.getKind() != Tree.Kind.VARIABLE || !isEnumerator((VariableTree)parent))) {
                boolean old = continuationIndent;
                try {
                    ModifiersTree mods = node.getModifiers();
                    if (mods != null) {
                        if (scan(mods, p)) {
                            continuationIndent = true;
                            if (cs.placeNewLineAfterModifiers())
                                newline();
                            else
                                space();
                        } else if (afterAnnotation) {
                            newline();
                        }
                        afterAnnotation = false;
                    }
                    JavaTokenId id = accept(CLASS, INTERFACE, ENUM, AT);
                    continuationIndent = true;
                    if (id == AT)
                        accept(INTERFACE);
                    space();
                    if (!ERROR.contentEquals(node.getSimpleName()))
                        accept(IDENTIFIER, UNDERSCORE);
                    List<? extends TypeParameterTree> tparams = node.getTypeParameters();
                    if (tparams != null && !tparams.isEmpty()) {
                        if (LT == accept(LT))
                            tpLevel++;
                        for (Iterator<? extends TypeParameterTree> it = tparams.iterator(); it.hasNext();) {
                            TypeParameterTree tparam = it.next();
                            scan(tparam, p);
                            if (it.hasNext()) {
                                spaces(cs.spaceBeforeComma() ? 1 : 0);
                                accept(COMMA);
                                spaces(cs.spaceAfterComma() ? 1 : 0);
                            }
                        }
                        JavaTokenId accepted;
                        if (tpLevel > 0 && (accepted = accept(GT, GTGT, GTGTGT)) != null) {
                            switch (accepted) {
                                case GTGTGT:
                                    tpLevel -= 3;
                                    break;
                                case GTGT:
                                    tpLevel -= 2;
                                    break;
                                case GT:
                                    tpLevel--;
                                    break;
                            }
                        }
                    }
                    Tree ext = node.getExtendsClause();
                    if (ext != null) {
                        wrapToken(cs.wrapExtendsImplementsKeyword(), 1, EXTENDS);
                        spaces(1, true);
                        scan(ext, p);
                    }
                    List<? extends Tree> impls = node.getImplementsClause();
                    if (impls != null && !impls.isEmpty()) {
                        wrapToken(cs.wrapExtendsImplementsKeyword(), 1, id == INTERFACE ? EXTENDS : IMPLEMENTS);
                        wrapList(cs.wrapExtendsImplementsList(), cs.alignMultilineImplements(), true, COMMA, impls);
                    }
                    List<? extends Tree> perms = node.getPermitsClause();
                    if (perms != null && !perms.isEmpty()) {
                        wrapToken(cs.wrapExtendsImplementsKeyword(), 1, EXTENDS); 
                        wrapList(cs.wrapExtendsImplementsList(), cs.alignMultilineImplements(), true, COMMA, perms);
                    }
                } finally {
                    continuationIndent = old;
                }
            }
            CodeStyle.BracePlacement bracePlacement = cs.getClassDeclBracePlacement();
            boolean spaceBeforeLeftBrace = cs.spaceBeforeClassDeclLeftBrace();
            int old = indent = lastIndent;
            int halfIndent = lastIndent;
            switch(bracePlacement) {
                case SAME_LINE:
                    spaces(spaceBeforeLeftBrace ? 1 : 0, tokens.offset() < startOffset);
                    accept(LBRACE);
                    indent = lastIndent + indentSize;
                    break;
                case NEW_LINE:
                    newline();
                    accept(LBRACE);
                    indent = lastIndent + indentSize;
                    break;
                case NEW_LINE_HALF_INDENTED:
                    int oldLast = lastIndent;
                    indent = lastIndent + (indentSize >> 1);
                    halfIndent = indent;
                    newline();
                    accept(LBRACE);
                    indent = oldLast + indentSize;
                    break;
                case NEW_LINE_INDENTED:
                    indent = lastIndent + indentSize;
                    halfIndent = indent;
                    newline();
                    accept(LBRACE);
                    break;
            }
            int lastMaxPreservedBlankLines = maxPreservedBlankLines;
            maxPreservedBlankLines = cs.getMaximumBlankLinesInDeclarations();
            boolean emptyClass = true;
            for (Tree member : node.getMembers()) {
                if (!isSynthetic(getCurrentPath().getCompilationUnit(), member)) {
                    emptyClass = false;
                    break;
                }
            }
            if (emptyClass) {
                newline();
            } else {
                if (!cs.indentTopLevelClassMembers())
                    indent = old;
                blankLines(node.getSimpleName().length() == 0 ? cs.getBlankLinesAfterAnonymousClassHeader() : node.getKind() == Tree.Kind.ENUM ? cs.getBlankLinesAfterEnumHeader() : cs.getBlankLinesAfterClassHeader());
                JavaTokenId id = null;
                boolean first = true;
                boolean semiRead = false;
                for (Tree member : node.getMembers()) {
                    if (!isSynthetic(getCurrentPath().getCompilationUnit(), member)) {
                        switch(member.getKind()) {
                            case VARIABLE:
                                if (isEnumerator((VariableTree)member)) {
                                    wrapTree(cs.wrapEnumConstants(), -1, id == COMMA ? 1 : 0, member);
                                    int index = tokens.index();
                                    int c = col;
                                    Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                                    id = accept(COMMA, SEMICOLON);
                                    if (id == COMMA) {
                                        index = tokens.index();
                                        c = col;
                                        d = diffs.isEmpty() ? null : diffs.getFirst();
                                        if (accept(SEMICOLON) == null)
                                            rollback(index, c, d);
                                    } else if (id == SEMICOLON) {
                                        blankLines(cs.getBlankLinesAfterFields());
                                    } else {
                                        rollback(index, c, d);
                                        blankLines(cs.getBlankLinesAfterFields());
                                    }
                                } else {
                                    boolean b = tokens.moveNext();
                                    if (b) {
                                        tokens.movePrevious();
                                        if (!fieldGroup && !first)
                                            blankLines(cs.getBlankLinesBeforeFields());
                                        scan(member, p);
                                        if(!fieldGroup)
                                            blankLines(cs.getBlankLinesAfterFields());
                                    }
                                }
                                break;
                            case METHOD:
                                if (!first)
                                   blankLines(cs.getBlankLinesBeforeMethods());
                                scan(member, p);
                                blankLines(cs.getBlankLinesAfterMethods());
                                break;
                            case BLOCK:
                                if (semiRead && !((BlockTree)member).isStatic() && ((BlockTree)member).getStatements().isEmpty()) {
                                    semiRead = false;
                                    continue;
                                }
                                if (!first) {
                                    blankLines(cs.getBlankLinesBeforeMethods());
                                    int index = tokens.index();
                                    int c = col;
                                    Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                                    if (accept(SEMICOLON) == SEMICOLON) {
                                        continue;
                                    } else {
                                        rollback(index, c, d);
                                    }
                                }
                                scan(member, p);
                                blankLines(cs.getBlankLinesAfterMethods());
                                break;
                            case ANNOTATION_TYPE:
                            case CLASS:
                            case ENUM:
                            case INTERFACE:
                                if (!first)
                                    blankLines(cs.getBlankLinesBeforeClass());
                                scan(member, p);
                                int index = tokens.index();
                                int c = col;
                                Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                                if (accept(SEMICOLON) == SEMICOLON) {
                                    semiRead = true;
                                } else {
                                    rollback(index, c, d);
                                    semiRead = false;
                                }
                                blankLines(cs.getBlankLinesAfterClass());
                                break;
                            case RECORD:
                                if (!first)
                                    blankLines(cs.getBlankLinesBeforeMethods());
                                scanRecord((ClassTree)member, p);
                                blankLines(cs.getBlankLinesAfterMethods());
                                break;
                        }
                        first = false;
                    }
                }
                if (lastBlankLinesTokenIndex < 0)
                    newline();
                blankLines(node.getSimpleName().length() == 0 ? cs.getBlankLinesBeforeAnonymousClassClosingBrace() : node.getKind() == Tree.Kind.ENUM ? cs.getBlankLinesBeforeEnumClosingBrace() : cs.getBlankLinesBeforeClassClosingBrace());
            }
            indent = halfIndent;
            Diff diff = diffs.isEmpty() ? null : diffs.getFirst();
            if (diff != null && diff.end == tokens.offset()) {
                if (diff.text != null) {
                    int idx = diff.text.lastIndexOf('\n'); //NOI18N
                    if (idx < 0)
                        diff.text = getIndent();
                    else
                        diff.text = diff.text.substring(0, idx + 1) + getIndent();

                }
                String spaces = diff.text != null ? diff.text : getIndent();
                if (spaces.equals(fText.substring(diff.start, diff.end)))
                    diffs.removeFirst();
            } else if (tokens.movePrevious()) {
                if (tokens.token().id() == WHITESPACE) {
                    String text =  tokens.token().text().toString();
                    int idx = text.lastIndexOf('\n'); //NOI18N
                    if (idx >= 0) {
                        text = text.substring(idx + 1);
                        String ind = getIndent();
                        if (!ind.equals(text))
                            addDiff(new Diff(tokens.offset() + idx + 1, tokens.offset() + tokens.token().length(), ind));
                    } else if (tokens.movePrevious()) {
                        if (tokens.token().id() == LINE_COMMENT) {
                            tokens.moveNext();
                            String ind = getIndent();
                            if (!ind.equals(text))
                                addDiff(new Diff(tokens.offset(), tokens.offset() + tokens.token().length(), ind));

                        } else {
                            tokens.moveNext();
                        }
                    }
                }
                tokens.moveNext();
            }
            col = indent();
            accept(RBRACE);
            maxPreservedBlankLines = lastMaxPreservedBlankLines;
            indent = lastIndent = old;
            return true;
        }

        @Override
        public Boolean visitVariable(VariableTree node, Void p) {
            boolean old = continuationIndent;
            try {
                Tree parent = getCurrentPath().getParentPath().getLeaf();
                boolean insideForTryOrCatch = EnumSet.of(Tree.Kind.FOR_LOOP, Tree.Kind.TRY, Tree.Kind.CATCH).contains(parent.getKind());
                ModifiersTree mods = node.getModifiers();
                if (mods != null && !fieldGroup && sp.getStartPosition(root, mods) < sp.getEndPosition(root, mods)) {
                    if (scan(mods, p)) {
                        if (!insideForTryOrCatch) {
                            continuationIndent = true;
                            if (cs.placeNewLineAfterModifiers())
                                newline();
                            else
                                space();
                        } else {
                            space();
                        }
                    } else if (afterAnnotation) {
                        WrapStyle newWrapStyle = cs.wrapAnnotations();
                        if (parent instanceof ClassTree) {
                            for (Tree member : ((ClassTree) parent).getMembers()) {
                                if (member.getKind() == Kind.RECORD) {
                                    ClassTree cls = (ClassTree) member;
                                    for (Tree recMember : cls.getMembers()) {
                                        if (recMember.equals(getCurrentPath().getLeaf())) {
                                            newWrapStyle = WrapStyle.WRAP_NEVER;
                                        }
                                    }
                                }
                            }
                        }
                        if (org.netbeans.api.java.source.TreeUtilities.CLASS_TREE_KINDS.contains(parent.getKind()) || parent.getKind() == Tree.Kind.BLOCK) {
                            switch (newWrapStyle) {
                                case WRAP_ALWAYS:
                                    newline();
                                    break;
                                case WRAP_IF_LONG:
                                    if (col >= rightMargin)
                                        newline();
                                    else
                                        spaces(1, true);
                                    break;
                                case WRAP_NEVER:
                                    spaces(1, true);
                            }
                        } else {
                            space();
                        }
                    }
                    afterAnnotation = false;
                }
                if (isEnumerator(node)) {
                    continuationIndent = true;
                    accept(IDENTIFIER, UNDERSCORE);
                    ExpressionTree init = node.getInitializer();
                    if (init != null && init.getKind() == Tree.Kind.NEW_CLASS) {
                        NewClassTree nct = (NewClassTree)init;
                        int index = tokens.index();
                        int c = col;
                        Diff d = diffs.isEmpty() ? null :diffs.getFirst();
                        spaces(cs.spaceBeforeMethodCallParen() ? 1 : 0);
                        JavaTokenId id = accept(LPAREN);
                        if (id != LPAREN)
                            rollback(index, c, d);
                        List<? extends ExpressionTree> args = nct.getArguments();
                        if (args != null && !args.isEmpty()) {
                            int oldIndent = indent;
                            int oldLastIndent = lastIndent;
                            boolean continuation = isLastIndentContinuation;
                            if (continuation) {
                                indent = indent();
                                isLastIndentContinuation = false;
                            }
                            try {
                                spaces(cs.spaceWithinMethodCallParens() ? 1 : 0, true);
                                wrapList(cs.wrapMethodCallArgs(), cs.alignMultilineCallArgs(), false, COMMA, args);
                            } finally {
                                indent = oldIndent;
                                lastIndent = oldLastIndent;
                                continuationIndent = isLastIndentContinuation = continuation;
                            }
                            spaces(cs.spaceWithinMethodCallParens() ? 1 : 0, true);
                        }
                        if (id == LPAREN)
                            accept(RPAREN);
                        continuationIndent = false;
                        ClassTree body = nct.getClassBody();
                        if (body != null)
                            scan(body, p);
                    }
                } else {
                    if (!insideForTryOrCatch)
                        continuationIndent = true;
                    if (node.getType() == null || tokens.token().id() == JavaTokenId.VAR || scan(node.getType(), p)) {
                        if (node.getType() != null && tokens.token().id() != JavaTokenId.VAR) {
                            spaces(1, fieldGroup);
                        } else {
                            if (tokens.token().id() == JavaTokenId.VAR) {
                                //Add space after 'var' token
                                addDiff(new Diff(tokens.offset() + 3, tokens.offset() + 3, " "));
                                tokens.moveNext();
                            }
                        }
                        if (!ERROR.contentEquals(node.getName()))
                            accept(IDENTIFIER, UNDERSCORE);
                    }
                    ExpressionTree init = node.getInitializer();
                    if (init != null) {
                        int alignIndent = -1;
                        if (cs.alignMultilineAssignment()) {
                            alignIndent = col;
                            if (!ERROR.contentEquals(node.getName()))
                                alignIndent -= node.getName().length();
                        }
                        if (cs.wrapAfterAssignOps()) {
                            boolean containedNewLine = spaces(cs.spaceAroundAssignOps() ? 1 : 0, false);
                            if (accept(EQ) == EQ && containedNewLine)
                                newline();
                            if (init.getKind() == Tree.Kind.NEW_ARRAY && ((NewArrayTree)init).getType() == null) {
                                if (cs.getOtherBracePlacement() == CodeStyle.BracePlacement.SAME_LINE)
                                    spaces(cs.spaceAroundAssignOps() ? 1 : 0);
                                scan(init, p);
                            } else {
                                wrapTree(cs.wrapAssignOps(), alignIndent, !containedNewLine && cs.spaceAroundAssignOps() ? 1 : 0, init);
                            }
                        } else {
                            wrapOperatorAndTree(cs.wrapAssignOps(), alignIndent, cs.spaceAroundAssignOps() ? 1 : 0, init);
                        }
                   }
                    fieldGroup = accept(SEMICOLON, COMMA) == COMMA;
                }
            } finally {
                continuationIndent = old;
            }
            return true;
        }

        private Boolean scanRecord(ClassTree node, Void p) {
            boolean old = continuationIndent;
            int oldIndent = indent;
            try {
                ModifiersTree mods = node.getModifiers();
                if (mods != null) {
                    if (scan(mods, p)) {
                        continuationIndent = true;
                        if (cs.placeNewLineAfterModifiers()) {
                            newline();
                        } else {
                            space();
                        }
                    } else if (afterAnnotation) {
                        newline();
                    }
                    afterAnnotation = false;
                }
                accept(IDENTIFIER);
                continuationIndent = true;
                space();

                if (!ERROR.contentEquals(node.getSimpleName())) {
                    accept(IDENTIFIER, UNDERSCORE);
                }

                List<? extends TypeParameterTree> tparams = node.getTypeParameters();
                if (tparams != null && !tparams.isEmpty()) {
                    if (LT == accept(LT)) {
                        tpLevel++;
                    }

                    for (Iterator<? extends TypeParameterTree> it = tparams.iterator(); it.hasNext();) {
                        TypeParameterTree tparam = it.next();
                        scan(tparam, p);
                        if (it.hasNext()) {
                            spaces(cs.spaceBeforeComma() ? 1 : 0);
                            accept(COMMA);
                            spaces(cs.spaceAfterComma() ? 1 : 0);
                        }
                    }
                    JavaTokenId accepted;
                    if (tpLevel > 0 && (accepted = accept(GT, GTGT, GTGTGT)) != null) {
                        switch (accepted) {
                            case GTGTGT:
                                tpLevel -= 3;
                                break;
                            case GTGT:
                                tpLevel -= 2;
                                break;
                            case GT:
                                tpLevel--;
                                break;
                        }
                    }
                    spaces(0, true);
                }

                spaces(cs.spaceBeforeMethodDeclParen() ? 1 : 0);
                accept(LPAREN);
                List<? extends Tree> members = node.getMembers();
                List recParams = new ArrayList<Tree>();

                for (Tree member : members) {
                    if (member.getKind() == Tree.Kind.VARIABLE) {
                        ModifiersTree modifiers = ((VariableTree) member).getModifiers();
                        Set<Modifier> modifierSet = modifiers.getFlags();

                        if (!modifierSet.contains(Modifier.STATIC)) {
                            recParams.add(member);
                        }
                    }
                }

                if (!recParams.isEmpty()) {
                    spaces(cs.spaceWithinMethodDeclParens() ? 1 : 0, true);
                    wrapList(cs.wrapMethodParams(), cs.alignMultilineMethodParams(), false, COMMA, recParams);
                }
                accept(RPAREN);
                List<? extends Tree> impls = node.getImplementsClause();
                if (impls != null && !impls.isEmpty()) {
                    wrapToken(cs.wrapExtendsImplementsKeyword(), 1, IMPLEMENTS);
                    wrapList(cs.wrapExtendsImplementsList(), cs.alignMultilineImplements(), true, COMMA, impls);
                }
                int oldLastIndent = lastIndent;
                int lastMaxPreservedBlankLines = maxPreservedBlankLines;
                maxPreservedBlankLines = cs.getMaximumBlankLinesInDeclarations();
                classLeftBracePlacement();

                continuationIndent = old;
                try {
                    if (members != null && !members.isEmpty()) {

                        boolean isFirstMember = true;
                        blankLines(node.getSimpleName().length() == 0 ? 0 : cs.getBlankLinesAfterClassHeader());
                        for (Tree member : members) {
                            if (recParams.contains(member)) {
                                continue;
                            }
                            blankLines(0);
                            switch (member.getKind()) {
                                case VARIABLE:
                                    boolean b = tokens.moveNext();
                                    if (b) {
                                        tokens.movePrevious();
                                        if (!isFirstMember) {
                                            blankLines(cs.getBlankLinesBeforeFields());
                                        }
                                        scan(member, p);
                                        blankLines(cs.getBlankLinesAfterFields());
                                    }
                                    break;
                                default:
                                    if (!isFirstMember) {
                                        blankLines(cs.getBlankLinesBeforeMethods());
                                    }
                                    scan(member, p);
                                    blankLines(cs.getBlankLinesAfterMethods());
                            }
                            if (isFirstMember) {
                                isFirstMember = false;
                            }
                        }

                        spaces(cs.spaceWithinMethodDeclParens() ? 1 : 0, true);
                    }
                } finally {
                    indent = oldIndent;
                    lastIndent = oldLastIndent;
                    continuationIndent = old;
                    maxPreservedBlankLines = lastMaxPreservedBlankLines;
                }
                newline();
                accept(RBRACE);
            } finally {
                continuationIndent = old;
            }
            return true;
        }

        private void classLeftBracePlacement() {
            CodeStyle.BracePlacement bracePlacement = cs.getClassDeclBracePlacement();
            boolean spaceBeforeLeftBrace = cs.spaceBeforeClassDeclLeftBrace();
            int old = indent = lastIndent;
            int halfIndent = lastIndent;
            switch (bracePlacement) {
                case SAME_LINE:
                    spaces(spaceBeforeLeftBrace ? 1 : 0, tokens.offset() < startOffset);
                    accept(LBRACE);
                    indent = lastIndent + indentSize;
                    break;
                case NEW_LINE:
                    newline();
                    accept(LBRACE);
                    indent = lastIndent + indentSize;
                    break;
                case NEW_LINE_HALF_INDENTED:
                    int oldLast = lastIndent;
                    indent = lastIndent + (indentSize >> 1);
                    halfIndent = indent;
                    newline();
                    accept(LBRACE);
                    indent = oldLast + indentSize;
                    break;
                case NEW_LINE_INDENTED:
                    indent = lastIndent + indentSize;
                    halfIndent = indent;
                    newline();
                    accept(LBRACE);
                    break;
            }
        }

        @Override
        public Boolean visitMethod(MethodTree node, Void p) {
            boolean old = continuationIndent;
            try {
                ModifiersTree mods = node.getModifiers();
                if (mods != null) {
                    if (scan(mods, p)) {
                        continuationIndent = true;
                        if (cs.placeNewLineAfterModifiers())
                            newline();
                        else
                            space();
                    } else {
                        newline();
                    }
                    afterAnnotation = false;
                }
                List<? extends TypeParameterTree> tparams = node.getTypeParameters();
                if (tparams != null && !tparams.isEmpty()) {
                    if (LT == accept(LT))
                        tpLevel++;
                    continuationIndent = true;
                    for (Iterator<? extends TypeParameterTree> it = tparams.iterator(); it.hasNext();) {
                        TypeParameterTree tparam = it.next();
                        scan(tparam, p);
                        if (it.hasNext()) {
                            spaces(cs.spaceBeforeComma() ? 1 : 0);
                            accept(COMMA);
                            spaces(cs.spaceAfterComma() ? 1 : 0);
                        }
                    }
                    JavaTokenId accepted;
                    if (tpLevel > 0 && (accepted = accept(GT, GTGT, GTGTGT)) != null) {
                        switch (accepted) {
                            case GTGTGT:
                                tpLevel -= 3;
                                break;
                            case GTGT:
                                tpLevel -= 2;
                                break;
                            case GT:
                                tpLevel--;
                                break;
                        }
                    }
                    spaces(1, true);
                }
                Tree retType = node.getReturnType();
                if (retType != null) {
                    scan(retType, p);
                    continuationIndent = true;
                    spaces(1, true);
                }
                if (!ERROR.contentEquals(node.getName()))
                    accept(IDENTIFIER, UNDERSCORE);
                continuationIndent = true;
                spaces(cs.spaceBeforeMethodDeclParen() ? 1 : 0);
                accept(LPAREN);
                List<? extends VariableTree> params = node.getParameters();
                if (params != null && !params.isEmpty()) {
                    int oldIndent = indent;
                    int oldLastIndent = lastIndent;
                    boolean continuation = isLastIndentContinuation;
                    if (continuation) {
                        indent = indent();
                        isLastIndentContinuation = false;
                    }
                    try {
                        spaces(cs.spaceWithinMethodDeclParens() ? 1 : 0, true);
                        wrapList(cs.wrapMethodParams(), cs.alignMultilineMethodParams(), false, COMMA, params);
                    } finally {
                        indent = oldIndent;
                        lastIndent = oldLastIndent;
                        continuationIndent = isLastIndentContinuation = continuation;
                    }
                    spaces(cs.spaceWithinMethodDeclParens() ? 1 : 0, true);
                }
                accept(RPAREN);
                continuationIndent = true;
                List<? extends ExpressionTree> threxs = node.getThrows();
                if (threxs != null && !threxs.isEmpty()) {
                    wrapToken(cs.wrapThrowsKeyword(), 1, THROWS);
                    wrapList(cs.wrapThrowsList(), cs.alignMultilineThrows(), true, COMMA, threxs);
                }
                Tree init = node.getDefaultValue();
                if (init != null) {
                    spaces(1, true);
                    accept(DEFAULT);
                    space();
                    scan(init, p);
                }
            } finally {
                continuationIndent = old;
            }
            BlockTree body = node.getBody();
            if (body != null) {
                scan(body, p);
            } else {
                accept(SEMICOLON);
            }
            return true;
        }

        @Override
        public Boolean visitModifiers(ModifiersTree node, Void p) {
            boolean ret = true;
            JavaTokenId id = null;
            afterAnnotation = false;
            Iterator<? extends AnnotationTree> annotations = node.getAnnotations().iterator();
            TreePath path = getCurrentPath().getParentPath();
            Tree parent = path.getLeaf();
            path = path.getParentPath();
            Tree grandParent = path != null ? path.getLeaf(): parent;
            boolean isStandalone = parent.getKind() != Tree.Kind.VARIABLE ||
                    org.netbeans.api.java.source.TreeUtilities.CLASS_TREE_KINDS.contains(grandParent.getKind()) || grandParent.getKind() == Tree.Kind.BLOCK;
            while (tokens.offset() < endPos) {
                if (afterAnnotation) {
                    if (!isStandalone) {
                        spaces(1, true);
                    } else {
                        switch (cs.wrapAnnotations()) {
                            case WRAP_ALWAYS:
                                newline();
                                break;
                            case WRAP_IF_LONG:
                                if (col >= rightMargin)
                                    newline();
                                else
                                    spaces(1, true);
                                break;
                            case WRAP_NEVER:
                                spaces(1, true);
                        }
                    }
                } else if (id != null) {
                    space();
                }
                int index = tokens.index();
                int c = col;
                Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                int lbl = lastBlankLines;
                int lblti = lastBlankLinesTokenIndex;
                Diff lbld = lastBlankLinesDiff;
                id = accept(PRIVATE, PROTECTED, PUBLIC, STATIC, DEFAULT, TRANSIENT, FINAL,
                        ABSTRACT, NATIVE, VOLATILE, SYNCHRONIZED, STRICTFP, AT);
                if (id == null)
                    break;
                if (id == AT) {
                    if (annotations.hasNext()) {
                        rollback(index, c, d);
                        lastBlankLines = lbl;
                        lastBlankLinesTokenIndex = lblti;
                        lastBlankLinesDiff = lbld;
                        wrapAnnotation = cs.wrapAnnotations() == CodeStyle.WrapStyle.WRAP_ALWAYS;
                        if (!isStandalone || !afterAnnotation) {
                            scan(annotations.next(), p);
                        } else {
                            wrapTree(cs.wrapAnnotations(), -1, 0, annotations.next());
                        }
                        wrapAnnotation = false;
                        afterAnnotation = true;
                        ret = false;
                        continue;
                    }
                    afterAnnotation = false;
                    ret = false;
                } else {
                    afterAnnotation = false;
                    ret = true;
                }
            }
            return ret;
        }

        @Override
        public Boolean visitAnnotation(AnnotationTree node, Void p) {
            accept(AT);
            boolean old = continuationIndent;
            try {
                continuationIndent = true;
                spaces(0, true);
                scan(node.getAnnotationType(), p);
                List<? extends ExpressionTree> args = node.getArguments();
                spaces(cs.spaceBeforeAnnotationParen() ? 1 : 0);
                accept(LPAREN);
                if (args != null && !args.isEmpty()) {
                    boolean oldInsideAnnotation = insideAnnotation;
                    insideAnnotation = true;
                    int oldIndent = indent;
                    int oldLastIndent = lastIndent;
                    boolean continuation = isLastIndentContinuation;
                    if (continuation) {
                        indent = indent();
                        isLastIndentContinuation = false;
                    }
                    try {
                        spaces(0, true);
                        wrapList(cs.wrapAnnotationArgs(), cs.alignMultilineAnnotationArgs(), cs.spaceWithinAnnotationParens(), COMMA, args);
                    } finally {
                        indent = oldIndent;
                        lastIndent = oldLastIndent;
                        continuationIndent = isLastIndentContinuation = continuation;
                        insideAnnotation = oldInsideAnnotation;
                    }
                    spaces(cs.spaceWithinAnnotationParens() ? 1 : 0, true);
                }
                accept(RPAREN);
            } finally {
                continuationIndent = old;
            }
            return true;
        }

        @Override
        public Boolean visitAnnotatedType(AnnotatedTypeTree node, Void p) {
            List<? extends AnnotationTree> annotations = node.getAnnotations();
            if (annotations != null && !annotations.isEmpty()) {
                switch (node.getUnderlyingType().getKind()) {
                    case MEMBER_SELECT:
                        MemberSelectTree mst = (MemberSelectTree)node.getUnderlyingType();
                        scan(mst.getExpression(), p);
                        accept(DOT);
                        spaces(0);
                        for (Iterator<? extends AnnotationTree> it = annotations.iterator(); it.hasNext();) {
                            scan(it.next(), p);
                            if (it.hasNext())
                                spaces(1, true);
                        }
                        space();
                        if (ERROR.contentEquals(mst.getIdentifier())) {
                            do {
                                if (tokens.offset() >= endPos)
                                    break;
                                int len = tokens.token().length();
                                if (tokens.token().id() == WHITESPACE && tokens.offset() + len >= endPos)
                                    break;
                                col += len;
                            } while (tokens.moveNext());
                            lastBlankLines = -1;
                            lastBlankLinesTokenIndex = -1;
                            lastBlankLinesDiff = null;
                        } else {
                            accept(IDENTIFIER, UNDERSCORE, STAR, THIS, SUPER, CLASS);
                        }
                        return true;
                    case ARRAY_TYPE:
                        ArrayTypeTree att = (ArrayTypeTree)node.getUnderlyingType();
                        boolean ret = scan(att.getType(), p);
                        space();
                        for (Iterator<? extends AnnotationTree> it = annotations.iterator(); it.hasNext();) {
                            scan(it.next(), p);
                            if (it.hasNext())
                                spaces(1, true);
                        }
                        space();
                        JavaTokenId id = accept(LBRACKET, ELLIPSIS);
                        if (id == ELLIPSIS)
                            return ret;
                        accept(RBRACKET);
                        return ret;
                    default:
                        for (Iterator<? extends AnnotationTree> it = annotations.iterator(); it.hasNext();) {
                            scan(it.next(), p);
                            if (it.hasNext())
                                spaces(1, true);
                        }
                        space();
                }
            }
            scan(node.getUnderlyingType(), p);
            return true;
        }

        @Override
        public Boolean visitTypeParameter(TypeParameterTree node, Void p) {
            List<? extends AnnotationTree> annotations = node.getAnnotations();
            if (annotations != null && !annotations.isEmpty()) {
                for (Iterator<? extends AnnotationTree> it = annotations.iterator(); it.hasNext();) {
                    scan(it.next(), p);
                    if (it.hasNext())
                        spaces(1, true);
                }
                space();
            }
            if (!ERROR.contentEquals(node.getName()))
                accept(IDENTIFIER, UNDERSCORE);
            List<? extends Tree> bounds = node.getBounds();
            if (bounds != null && !bounds.isEmpty()) {
                space();
                accept(EXTENDS);
                space();
                for (Iterator<? extends Tree> it = bounds.iterator(); it.hasNext();) {
                    Tree bound = it.next();
                    scan(bound, p);
                    if (it.hasNext()) {
                        space();
                        accept(AMP);
                        space();
                    }
                }
            }
            return true;
        }

        @Override
        public Boolean visitParameterizedType(ParameterizedTypeTree node, Void p) {
            scan(node.getType(), p);
            int index = tokens.index();
            int c = col;
            Diff d = diffs.isEmpty() ? null : diffs.getFirst();
            boolean ltRead;
            if (LT == accept(LT)) {
                tpLevel++;
                ltRead = true;
            } else {
                rollback(index, c, d);
                ltRead = false;
            }
            List<? extends Tree> targs = node.getTypeArguments();
            if (targs != null && !targs.isEmpty()) {
                Iterator<? extends Tree> it = targs.iterator();
                Tree targ = it.hasNext() ? it.next() : null;
                while (true) {
                    scan(targ, p);
                    targ = it.hasNext() ? it.next() : null;
                    if (targ == null)
                        break;
                    if (targ.getKind() != Tree.Kind.ERRONEOUS || !((ErroneousTree)targ).getErrorTrees().isEmpty() || it.hasNext()) {
                        spaces(cs.spaceBeforeComma() ? 1 : 0);
                        accept(COMMA);
                        spaces(cs.spaceAfterComma() ? 1 : 0);
                    } else {
                        scan(targ, p);
                        return true;
                    }
                }
            }
            JavaTokenId accepted;
            if (ltRead && tpLevel > 0 && (accepted = accept(GT, GTGT, GTGTGT)) != null) {
                switch (accepted) {
                    case GTGTGT:
                        tpLevel -= 3;
                        break;
                    case GTGT:
                        tpLevel -= 2;
                        break;
                    case GT:
                        tpLevel--;
                        break;
                }
            }
            return true;
        }

        @Override
        public Boolean visitWildcard(WildcardTree node, Void p) {
            accept(QUESTION);
            Tree bound = node.getBound();
            if (bound != null) {
                space();
                accept(EXTENDS, SUPER);
                space();
                scan(bound, p);
            }
            return true;
        }

        @Override
        public Boolean visitBlock(BlockTree node, Void p) {
            if (node.isStatic())
                accept(STATIC);
            CodeStyle.BracePlacement bracePlacement;
            boolean spaceBeforeLeftBrace = false;
            switch (getCurrentPath().getParentPath().getLeaf().getKind()) {
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                case RECORD:
                    bracePlacement = cs.getOtherBracePlacement();
                    if (node.isStatic())
                        spaceBeforeLeftBrace = cs.spaceBeforeStaticInitLeftBrace();
                    break;
                case METHOD:
                    bracePlacement = cs.getMethodDeclBracePlacement();
                    spaceBeforeLeftBrace = cs.spaceBeforeMethodDeclLeftBrace();
                    break;
                case LAMBDA_EXPRESSION:
                    bracePlacement = cs.getOtherBracePlacement();
                    break;
                case TRY:
                    bracePlacement = cs.getOtherBracePlacement();
                    if (((TryTree)getCurrentPath().getParentPath().getLeaf()).getBlock() == node)
                        spaceBeforeLeftBrace = cs.spaceBeforeTryLeftBrace();
                    else
                        spaceBeforeLeftBrace = cs.spaceBeforeFinallyLeftBrace();
                    break;
                case CATCH:
                    bracePlacement = cs.getOtherBracePlacement();
                    spaceBeforeLeftBrace = cs.spaceBeforeCatchLeftBrace();
                    break;
                case WHILE_LOOP:
                    bracePlacement = cs.getOtherBracePlacement();
                    spaceBeforeLeftBrace = cs.spaceBeforeWhileLeftBrace();
                    break;
                case FOR_LOOP:
                case ENHANCED_FOR_LOOP:
                    bracePlacement = cs.getOtherBracePlacement();
                    spaceBeforeLeftBrace = cs.spaceBeforeForLeftBrace();
                    break;
                case DO_WHILE_LOOP:
                    bracePlacement = cs.getOtherBracePlacement();
                    spaceBeforeLeftBrace = cs.spaceBeforeDoLeftBrace();
                    break;
                case IF:
                    bracePlacement = cs.getOtherBracePlacement();
                    if (((IfTree)getCurrentPath().getParentPath().getLeaf()).getThenStatement() == node)
                        spaceBeforeLeftBrace = cs.spaceBeforeIfLeftBrace();
                    else
                        spaceBeforeLeftBrace = cs.spaceBeforeElseLeftBrace();
                    break;
                case SYNCHRONIZED:
                    bracePlacement = cs.getOtherBracePlacement();
                    spaceBeforeLeftBrace = cs.spaceBeforeSynchronizedLeftBrace();
                    break;
                case CASE:
                    bracePlacement = cs.getOtherBracePlacement();
                    spaceBeforeLeftBrace = true;
                    break;
                default:
                    bracePlacement = cs.getOtherBracePlacement();
                    break;
            }
            int old = lastIndent;
            int halfIndent = lastIndent;
            switch(bracePlacement) {
                case SAME_LINE:
                    spaces(spaceBeforeLeftBrace ? 1 : 0, tokens.offset() < startOffset);
                    if (node instanceof FakeBlock) {
                        appendToDiff("{"); //NOI18N
                        lastBlankLines = -1;
                        lastBlankLinesTokenIndex = -1;
                        lastBlankLinesDiff = null;
                    } else {
                        accept(LBRACE);
                    }
                    indent = lastIndent + indentSize;
                    break;
                case NEW_LINE:
                    newline();
                    if (node instanceof FakeBlock) {
                        indent = lastIndent + indentSize;
                        appendToDiff("{"); //NOI18N
                        lastBlankLines = -1;
                        lastBlankLinesTokenIndex = -1;
                        lastBlankLinesDiff = null;
                    } else {
                        accept(LBRACE);
                        indent = lastIndent + indentSize;
                    }
                    break;
                case NEW_LINE_HALF_INDENTED:
                    int oldLast = lastIndent;
                    indent = lastIndent + (indentSize >> 1);
                    halfIndent = indent;
                    newline();
                    if (node instanceof FakeBlock) {
                        indent = oldLast + indentSize;
                        appendToDiff("{"); //NOI18N
                        lastBlankLines = -1;
                        lastBlankLinesTokenIndex = -1;
                        lastBlankLinesDiff = null;
                    } else {
                        accept(LBRACE);
                        indent = oldLast + indentSize;
                    }
                    break;
                case NEW_LINE_INDENTED:
                    indent = lastIndent + indentSize;
                    halfIndent = indent;
                    newline();
                    if (node instanceof FakeBlock) {
                        appendToDiff("{"); //NOI18N
                        lastBlankLines = -1;
                        lastBlankLinesTokenIndex = -1;
                        lastBlankLinesDiff = null;
                    } else {
                        accept(LBRACE);
                    }
                    break;
            }
            boolean isEmpty = true;
            int lastMaxPreservedBlankLines = maxPreservedBlankLines;
            maxPreservedBlankLines = cs.getMaximumBlankLinesInCode();
            for (StatementTree stat  : node.getStatements()) {
                if (!isSynthetic(getCurrentPath().getCompilationUnit(), stat)) {
                    isEmpty = false;
                    if (stat.getKind() == Tree.Kind.LABELED_STATEMENT && cs.absoluteLabelIndent()) {
                        int o = indent;
                        int oLDiff = lastIndent - indent;
                        boolean oCI = continuationIndent;
                        try {
                            indent = 0;
                            continuationIndent = false;
                            if (node instanceof FakeBlock) {
                                appendToDiff(getNewlines(1) + getIndent());
                                col = indent();
                            } else {
                                newline();
                            }
                            oLDiff = lastIndent - indent;
                        } finally {
                            indent = o;
                            lastIndent = oLDiff + indent;
                            continuationIndent = oCI;
                        }
                    } else if (node instanceof FakeBlock) {
                        appendToDiff(getNewlines(1) + getIndent());
                        col = indent();
                    } else if (stat.getKind() == Tree.Kind.EMPTY_STATEMENT || stat.getKind() == Tree.Kind.EXPRESSION_STATEMENT && ((ExpressionStatementTree)stat).getExpression().getKind() == Tree.Kind.ERRONEOUS) {
                        spaces(0, true);
                    } else if (!fieldGroup || stat.getKind() != Tree.Kind.VARIABLE) {
                        newline();
                    }
                    scan(stat, p);
                }
            }
            if (isEmpty) {
                newline();
            }
            if (node instanceof FakeBlock) {
                indent = halfIndent;
                int i = tokens.index();
                boolean loop = true;
                while(loop) {
                    switch (tokens.token().id()) {
                        case WHITESPACE:
                            if (tokens.token().text().toString().indexOf('\n') < 0) {
                                tokens.moveNext();
                            } else {
                                loop = false;
                                appendToDiff("\n"); //NOI18N
                                col = 0;
                            }
                            break;
                        case LINE_COMMENT:
                            loop = false;
                        case BLOCK_COMMENT:
                            tokens.moveNext();
                            break;
                        default:
                            if (tokens.index() != i) {
                                tokens.moveIndex(i);
                                tokens.moveNext();
                            }
                            loop = false;
                            appendToDiff("\n"); //NOI18N
                            col = 0;
                    }
                }
                appendToDiff(getIndent() + "}"); //NOI18N
                col = indent() + 1;
                lastBlankLines = -1;
                lastBlankLinesTokenIndex = -1;
                lastBlankLinesDiff = null;
            } else {
                newline();
                indent = halfIndent;
                Diff diff = diffs.isEmpty() ? null : diffs.getFirst();
                if (diff != null && diff.end == tokens.offset()) {
                    if (diff.text != null) {
                        int idx = diff.text.lastIndexOf('\n'); //NOI18N
                        if (idx < 0)
                            diff.text = getIndent();
                        else
                            diff.text = diff.text.substring(0, idx + 1) + getIndent();
                    }
                    String spaces = diff.text != null ? diff.text : getIndent();
                    if (spaces.equals(fText.substring(diff.start, diff.end)))
                        diffs.removeFirst();
                } else if (tokens.movePrevious()) {
                    if (tokens.token().id() == WHITESPACE) {
                        String text =  tokens.token().text().toString();
                        int idx = text.lastIndexOf('\n'); //NOI18N
                        if (idx >= 0) {
                            text = text.substring(idx + 1);
                            String ind = getIndent();
                            if (!ind.equals(text))
                                addDiff(new Diff(tokens.offset() + idx + 1, tokens.offset() + tokens.token().length(), ind));
                        } else if (tokens.movePrevious()) {
                            if (tokens.token().id() == LINE_COMMENT) {
                                tokens.moveNext();
                                String ind = getIndent();
                                if (!ind.equals(text))
                                    addDiff(new Diff(tokens.offset(), tokens.offset() + tokens.token().length(), ind));

                            } else {
                                tokens.moveNext();
                            }
                        }
                    }
                    tokens.moveNext();
                }
                col = indent();
                accept(RBRACE);
            }
            maxPreservedBlankLines = lastMaxPreservedBlankLines;
            indent = lastIndent = old;
            return true;
        }

        @Override
        public Boolean visitMemberSelect(MemberSelectTree node, Void p) {
            scan(node.getExpression(), p);
            if (ERROR.contentEquals(node.getIdentifier())) {
                do {
                    if (tokens.offset() >= endPos)
                        break;
                    int len = tokens.token().length();
                    if (tokens.token().id() == WHITESPACE && tokens.offset() + len >= endPos)
                        break;
                    col += len;
                } while (tokens.moveNext());
                lastBlankLines = -1;
                lastBlankLinesTokenIndex = -1;
                lastBlankLinesDiff = null;
            } else {
                accept(DOT);
                accept(IDENTIFIER, UNDERSCORE, STAR, THIS, SUPER, CLASS);
            }
            return true;
        }

        @Override
        public Boolean visitMemberReference(MemberReferenceTree node, Void p) {
            scan(node.getQualifierExpression(), p);
            spaces(cs.spaceAroundMethodReferenceDoubleColon() ? 1 : 0);
            accept(COLONCOLON);
            spaces(cs.spaceAroundMethodReferenceDoubleColon() ? 1 : 0);
            int index = tokens.index();
            int c = col;
            Diff d = diffs.isEmpty() ? null : diffs.getFirst();
            boolean ltRead;
            if (LT == accept(LT)) {
                tpLevel++;
                ltRead = true;
            } else {
                rollback(index, c, d);
                ltRead = false;
            }
            List<? extends Tree> targs = node.getTypeArguments();
            if (targs != null && !targs.isEmpty()) {
                Iterator<? extends Tree> it = targs.iterator();
                Tree targ = it.hasNext() ? it.next() : null;
                while (true) {
                    scan(targ, p);
                    targ = it.hasNext() ? it.next() : null;
                    if (targ == null)
                        break;
                    if (targ.getKind() != Tree.Kind.ERRONEOUS || !((ErroneousTree)targ).getErrorTrees().isEmpty() || it.hasNext()) {
                        spaces(cs.spaceBeforeComma() ? 1 : 0);
                        accept(COMMA);
                        spaces(cs.spaceAfterComma() ? 1 : 0);
                    } else {
                        scan(targ, p);
                        return true;
                    }
                }
            }
            JavaTokenId accepted;
            if (ltRead && tpLevel > 0 && (accepted = accept(GT, GTGT, GTGTGT)) != null) {
                switch (accepted) {
                    case GTGTGT:
                        tpLevel -= 3;
                        break;
                    case GTGT:
                        tpLevel -= 2;
                        break;
                    case GT:
                        tpLevel--;
                        break;
                }
            }
            if (ERROR.contentEquals(node.getName())) {
                do {
                    if (tokens.offset() >= endPos)
                        break;
                    int len = tokens.token().length();
                    if (tokens.token().id() == WHITESPACE && tokens.offset() + len >= endPos)
                        break;
                    col += len;
                } while (tokens.moveNext());
                lastBlankLines = -1;
                lastBlankLinesTokenIndex = -1;
                lastBlankLinesDiff = null;
            } else {
                accept(IDENTIFIER, UNDERSCORE, NEW);
            }
            return true;
        }

        @Override
        public Boolean visitLambdaExpression(LambdaExpressionTree node, Void p) {
            List<? extends VariableTree> params = node.getParameters();
            JavaTokenId accepted = params != null && params.size() == 1 ? accept(LPAREN, IDENTIFIER, UNDERSCORE) : accept(LPAREN);
            if (accepted == LPAREN) {
                boolean old = continuationIndent;
                try {
                    if (params != null && !params.isEmpty()) {
                        int oldIndent = indent;
                        int oldLastIndent = lastIndent;
                        boolean continuation = isLastIndentContinuation;
                        if (continuation) {
                            indent = indent();
                            isLastIndentContinuation = false;
                        }
                        try {
                            spaces(cs.spaceWithinLambdaParens() ? 1 : 0, true);
                            wrapList(cs.wrapLambdaParams(), cs.alignMultilineLambdaParams(), false, COMMA, params);
                        } finally {
                            indent = oldIndent;
                            lastIndent = oldLastIndent;
                            continuationIndent = isLastIndentContinuation = continuation;
                        }
                        spaces(cs.spaceWithinLambdaParens() ? 1 : 0);
                    }
                    accept(RPAREN);
                } finally {
                    continuationIndent = old;
                }
            }
            if (cs.wrapAfterLambdaArrow()) {
                boolean containedNewLine = spaces(cs.spaceAroundLambdaArrow() ? 1 : 0, false);
                if (accept(ARROW) != null) {
                    col += 2;
                    lastBlankLines = -1;
                    lastBlankLinesTokenIndex = -1;
                    if (containedNewLine)
                        newline();
                }
                boolean old = continuationIndent;
                int oldIndent = indent;
                int oldLastIndent = lastIndent;
                boolean oldLastIndentContinuation = isLastIndentContinuation;
                if (node.getBodyKind() == BodyKind.STATEMENT) {
                    if (continuationIndent) {
                        lastIndent = indent;
                        continuationIndent = false;
                    }
                }
                try {
                    wrapTree(cs.wrapLambdaArrow(), -1, cs.spaceAroundLambdaArrow() ? 1 : 0, node.getBody());
                } finally {
                    continuationIndent = old;
                    indent = oldIndent;
                    lastIndent = oldLastIndent;
                    isLastIndentContinuation = oldLastIndentContinuation;
                }
            } else {
                boolean old = continuationIndent;
                int oldIndent = indent;
                int oldLastIndent = lastIndent;
                boolean oldLastIndentContinuation = isLastIndentContinuation;
                if (node.getBodyKind() == BodyKind.STATEMENT) {
                    if (continuationIndent) {
                        lastIndent = indent;
                        indent += continuationIndentSize;
                        continuationIndent = false;
                    }
                }
                try {
                    wrapOperatorAndTree(cs.wrapLambdaArrow(), -1, cs.spaceAroundLambdaArrow() ? 1 : 0, cs.spaceAroundLambdaArrow() ? 1 : 0, lastIndent, node.getBody());
                } finally {
                    continuationIndent = old;
                    indent = oldIndent;
                    lastIndent = oldLastIndent;
                    isLastIndentContinuation = oldLastIndentContinuation;
                }
            }
            return true;
        }

        @Override
        public Boolean visitMethodInvocation(MethodInvocationTree node, Void p) {
            ExpressionTree ms = node.getMethodSelect();
            if (ms.getKind() == Tree.Kind.MEMBER_SELECT) {
                int old = indent;
                if (isLastIndentContinuation) {
                    indent += continuationIndentSize;
                    isLastIndentContinuation = false;
                }
                ExpressionTree exp = ((MemberSelectTree)ms).getExpression();
                scan(exp, p);
                WrapStyle wrapStyle = cs.wrapChainedMethodCalls();
                if (wrapStyle == WrapStyle.WRAP_ALWAYS && exp.getKind() != Tree.Kind.METHOD_INVOCATION)
                    wrapStyle = WrapStyle.WRAP_IF_LONG;
                switch (wrapStyle) {
                    case WRAP_ALWAYS:
                        if (cs.wrapAfterDotInChainedMethodCalls()) {
                            accept(DOT);
                            newline();
                        } else {
                            newline();
                            accept(DOT);
                        }
                        scanMethodCall(node);
                        break;
                    case WRAP_IF_LONG:
                        int index = tokens.index();
                        int c = col;
                        Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                        int o = tokens.offset();
                        WrapAbort oldCheckWrap = checkWrap;
                        checkWrap = new WrapAbort(o);
                        try {
                            spaces(0, true);
                            accept(DOT);
                            spaces(0, true);
                            scanMethodCall(node);
                        } catch (WrapAbort wa) {
                        } finally {
                            checkWrap = oldCheckWrap;
                        }
                        if (col > rightMargin && o >= lastNewLineOffset) {
                            rollback(index, c, d);
                            if (cs.wrapAfterDotInChainedMethodCalls()) {
                                accept(DOT);
                                newline();
                            } else {
                                newline();
                                accept(DOT);
                            }
                            scanMethodCall(node);
                        }
                        break;
                    case WRAP_NEVER:
                        spaces(0, true);
                        accept(DOT);
                        spaces(0, true);
                        scanMethodCall(node);
                        break;
                }
                indent = old;
            } else {
                scanMethodCall(node);
            }
            return true;
        }

        @Override
        public Boolean visitNewClass(NewClassTree node, Void p) {
            ExpressionTree encl = node.getEnclosingExpression();
            if (encl != null) {
                scan(encl, p);
                accept(DOT);
            }
            accept(NEW);
            space();
            List<? extends Tree> targs = node.getTypeArguments();
            if (targs != null && !targs.isEmpty()) {
                if (LT == accept(LT))
                    tpLevel++;
                for (Iterator<? extends Tree> it = targs.iterator(); it.hasNext();) {
                    Tree targ = it.next();
                    scan(targ, p);
                    if (it.hasNext()) {
                        spaces(cs.spaceBeforeComma() ? 1 : 0);
                        accept(COMMA);
                        spaces(cs.spaceAfterComma() ? 1 : 0);
                    }
                }
                JavaTokenId accepted;
                if (tpLevel > 0 && (accepted = accept(GT, GTGT, GTGTGT)) != null) {
                    switch (accepted) {
                        case GTGTGT:
                            tpLevel -= 3;
                            break;
                        case GTGT:
                            tpLevel -= 2;
                            break;
                        case GT:
                            tpLevel--;
                            break;
                    }
                }
            }
            scan(node.getIdentifier(), p);
            spaces(cs.spaceBeforeMethodCallParen() ? 1 : 0);
            accept(LPAREN);
            boolean old = continuationIndent;
            try {
                List<? extends ExpressionTree> args = node.getArguments();
                if (args != null && !args.isEmpty()) {
                    int oldIndent = indent;
                    int oldLastIndent = lastIndent;
                    boolean continuation = isLastIndentContinuation;
                    if (continuation) {
                        indent = indent();
                        isLastIndentContinuation = false;
                    }
                    try {
                        spaces(cs.spaceWithinMethodCallParens() ? 1 : 0, true);
                        wrapList(cs.wrapMethodCallArgs(), cs.alignMultilineCallArgs(), false, COMMA, args);
                    } finally {
                        indent = oldIndent;
                        lastIndent = oldLastIndent;
                        continuationIndent = isLastIndentContinuation = continuation;
                    }
                    spaces(cs.spaceWithinMethodCallParens() ? 1 : 0, true);
                }
                accept(RPAREN);
                continuationIndent = old;
                int oldIndent = indent;
                int oldLastIndent = lastIndent;
                boolean oldLastIndentContinuation = isLastIndentContinuation;
                ClassTree body = node.getClassBody();
                if (body != null) {
                    if (continuationIndent) {
                        lastIndent = indent;
                        continuationIndent = false;
                    }
                    try {
                        scan(body, p);
                    } finally {
                        indent = oldIndent;
                        lastIndent = oldLastIndent;
                        isLastIndentContinuation = oldLastIndentContinuation;
                    }
                }
            } finally {
                continuationIndent = old;
            }
            return true;
        }

        @Override
        public Boolean visitAssert(AssertTree node, Void p) {
            accept(ASSERT);
            boolean old = continuationIndent;
            try {
                continuationIndent = true;
                space();
                scan(node.getCondition(), p);
                ExpressionTree detail = node.getDetail();
                if (detail != null) {
                    spaces(cs.spaceBeforeColon() ? 1 : 0);
                    accept(COLON);
                    wrapTree(cs.wrapAssert(), -1, cs.spaceAfterColon() ? 1 : 0, detail);
                }
                accept(SEMICOLON);
            } finally {
                continuationIndent = old;
            }
            return true;
        }

        @Override
        public Boolean visitReturn(ReturnTree node, Void p) {
            accept(RETURN);
            boolean old = continuationIndent;
            try {
                continuationIndent = true;
                ExpressionTree exp = node.getExpression();
                if (exp != null) {
                    space();
                    scan(exp, p);
                }
                accept(SEMICOLON);
            } finally {
                continuationIndent = old;
            }
            return true;
        }

        @Override
        public Boolean visitThrow(ThrowTree node, Void p) {
            accept(THROW);
            boolean old = continuationIndent;
            try {
                continuationIndent = true;
                ExpressionTree exp = node.getExpression();
                if (exp != null) {
                    space();
                    scan(exp, p);
                }
                accept(SEMICOLON);
            } finally {
                continuationIndent = old;
            }
            return true;
        }

        @Override
        public Boolean visitTry(TryTree node, Void p) {
            accept(TRY);
            List<? extends Tree> res = node.getResources();
            if (res != null && !res.isEmpty()) {
                boolean old = continuationIndent;
                try {
                    continuationIndent = true;
                    spaces(cs.spaceBeforeTryParen() ? 1 : 0);
                    accept(LPAREN);
                    spaces(cs.spaceWithinTryParens() ? 1 : 0, true);
                    wrapList(cs.wrapTryResources(), cs.alignMultilineTryResources(), false, SEMICOLON, res);
                    int index = tokens.index();
                    int c = col;
                    Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                    if (accept(SEMICOLON) == null) {
                        rollback(index, c, d);
                    }
                    spaces(cs.spaceWithinTryParens() ? 1 : 0);
                    accept(RPAREN);
                } finally {
                    continuationIndent = old;
                }
            }
            scan(node.getBlock(), p);
            for (CatchTree catchTree : node.getCatches()) {
                if (cs.placeCatchOnNewLine())
                    newline();
                else
                    spaces(cs.spaceBeforeCatch() ? 1 : 0);
                scan(catchTree, p);
            }
            BlockTree finallyBlockTree = node.getFinallyBlock();
            if (finallyBlockTree != null) {
                if (cs.placeFinallyOnNewLine())
                    newline();
                else
                    spaces(cs.spaceBeforeFinally() ? 1 : 0);
                accept(FINALLY);
                scan(finallyBlockTree, p);
            }
            return true;
        }

        @Override
        public Boolean visitCatch(CatchTree node, Void p) {
            accept(CATCH);
            boolean old = continuationIndent;
            try {
                continuationIndent = true;
                spaces(cs.spaceBeforeCatchParen() ? 1 : 0);
                accept(LPAREN);
                spaces(cs.spaceWithinCatchParens() ? 1 : 0);
                scan(node.getParameter(), p);
                spaces(cs.spaceWithinCatchParens() ? 1 : 0);
                accept(RPAREN);
            } finally {
                continuationIndent = old;
            }
            scan(node.getBlock(), p);
            return true;
        }

        @Override
        public Boolean visitUnionType(UnionTypeTree node, Void p) {
            List<? extends Tree> alts = node.getTypeAlternatives();
            if (alts != null && !alts.isEmpty()) {
                wrapList(cs.wrapDisjunctiveCatchTypes(), cs.alignMultilineDisjunctiveCatchTypes(), false, BAR, cs.wrapAfterDisjunctiveCatchBar(), alts);
            }
            return true;
        }

        /**
         * Finds the end of the line (for brace insertion) after the statement Tree, provided
         * the statement is followed nu whitespace only.
         * @param statement
         * @return
         */
        private int findNewlineAfterStatement(Tree statement) {
            int pos = (int)sp.getEndPosition(root, statement);
            if (pos < 0) {
                return pos;
            }
            int index = tokens.index();
            try {
                tokens.move(pos);
                while (tokens.moveNext()) {
                    Token<JavaTokenId> tukac = tokens.token();
                    switch (tukac.id()) {
                        case WHITESPACE: {
                            int nl = tukac.text().toString().indexOf('\n');
                            if (nl != -1) {
                                return tokens.offset() + nl + 1;
                            }
                            break;
                        }
                        case LINE_COMMENT:
                            // up to and including EOL:
                            return tokens.offset() + tukac.length();
                        case BLOCK_COMMENT:
                            break;
                        default:
                            return pos;
                    }
                }
            } finally {
                tokens.moveIndex(index);
                tokens.moveNext();
            }
            return pos;
        }

        @Override
        public Boolean visitIf(final IfTree node, Void p) {
            accept(IF);
            boolean old = continuationIndent;
            try {
                continuationIndent = true;
                spaces(cs.spaceBeforeIfParen() ? 1 : 0);
                scan(node.getCondition(), p);
            } finally {
                continuationIndent = old;
            }
            StatementTree elseStat = node.getElseStatement();
            CodeStyle.BracesGenerationStyle redundantIfBraces = cs.redundantIfBraces();
            int eoln = findNewlineAfterStatement(node);
            if ((elseStat != null && redundantIfBraces == CodeStyle.BracesGenerationStyle.ELIMINATE && danglingElseChecker.hasDanglingElse(node.getThenStatement())) ||
                    (redundantIfBraces == CodeStyle.BracesGenerationStyle.GENERATE && (startOffset > sp.getStartPosition(root, node) || endOffset < eoln || node.getCondition().getKind() == Tree.Kind.ERRONEOUS))) {
                redundantIfBraces = CodeStyle.BracesGenerationStyle.LEAVE_ALONE;
            }
            lastIndent = indent;
            boolean prevblock = wrapStatement(cs.wrapIfStatement(), redundantIfBraces, cs.spaceBeforeIfLeftBrace() ? 1 : 0, node.getThenStatement());
            if (elseStat != null) {
                if (cs.placeElseOnNewLine() || !prevblock) {
                    newline();
                } else {
                    spaces(cs.spaceBeforeElse() ? 1 : 0, tokens.offset() < startOffset);
                }
                accept(ELSE);
                if (elseStat.getKind() == Tree.Kind.IF && cs.specialElseIf()) {
                    int index = tokens.index();
                    int c = col;
                    Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                    if (!spaces(1, false)) {
                        scan(elseStat, p);
                        return true;
                    }
                    rollback(index, c, d);
                }
                WrapStyle wrapElse;
                boolean preserveNewLine = true;
                if (cs.specialElseIf() && elseStat.getKind() == Tree.Kind.IF) {
                    redundantIfBraces = CodeStyle.BracesGenerationStyle.ELIMINATE;
                    wrapElse = CodeStyle.WrapStyle.WRAP_NEVER;
                    preserveNewLine = false;
                    lastIndent -= indentSize;
                } else {
                    redundantIfBraces = cs.redundantIfBraces();
                    if (redundantIfBraces == CodeStyle.BracesGenerationStyle.GENERATE && (startOffset > sp.getStartPosition(root, node) || endOffset < eoln)) {
                        redundantIfBraces = CodeStyle.BracesGenerationStyle.LEAVE_ALONE;
                    }
                    wrapElse = cs.wrapIfStatement();
                }
                wrapStatement(wrapElse, redundantIfBraces, cs.spaceBeforeElseLeftBrace() ? 1 : 0, preserveNewLine, elseStat);
            }
            return true;
        }

        @Override
        public Boolean visitDoWhileLoop(DoWhileLoopTree node, Void p) {
            accept(DO);
            lastIndent = indent;
            boolean old = continuationIndent;
            try {
                int eoln = findNewlineAfterStatement(node);
                CodeStyle.BracesGenerationStyle redundantDoWhileBraces = cs.redundantDoWhileBraces();
                if (redundantDoWhileBraces == CodeStyle.BracesGenerationStyle.GENERATE && (startOffset > sp.getStartPosition(root, node) || endOffset <  eoln || node.getCondition().getKind() == Tree.Kind.ERRONEOUS)) {
                    redundantDoWhileBraces = CodeStyle.BracesGenerationStyle.LEAVE_ALONE;
                }
                boolean isBlock = node.getStatement().getKind() == Tree.Kind.BLOCK || redundantDoWhileBraces == CodeStyle.BracesGenerationStyle.GENERATE;
                if (isBlock && redundantDoWhileBraces == CodeStyle.BracesGenerationStyle.ELIMINATE) {
                    Iterator<? extends StatementTree> stats = ((BlockTree)node.getStatement()).getStatements().iterator();
                    if (stats.hasNext()) {
                        StatementTree stat = stats.next();
                        if (!stats.hasNext() && stat.getKind() != Tree.Kind.VARIABLE) {
                            isBlock = false;
                        }
                    }
                }
                isBlock = wrapStatement(cs.wrapDoWhileStatement(), redundantDoWhileBraces, !isBlock || cs.spaceBeforeDoLeftBrace() ? 1 : 0, node.getStatement());
                if (cs.placeWhileOnNewLine() || !isBlock) {
                    newline();
                } else {
                    spaces(cs.spaceBeforeWhile() ? 1 : 0);
                }
                accept(WHILE);
                continuationIndent = true;
                spaces(cs.spaceBeforeWhileParen() ? 1 : 0);
                scan(node.getCondition(), p);
                accept(SEMICOLON);
            } finally {
                continuationIndent = old;
            }
            return true;
        }

        @Override
        public Boolean visitWhileLoop(WhileLoopTree node, Void p) {
            accept(WHILE);
            boolean old = continuationIndent;
            try {
                continuationIndent = true;
                spaces(cs.spaceBeforeWhileParen() ? 1 : 0);
                scan(node.getCondition(), p);
            } finally {
                continuationIndent = old;
            }
            lastIndent = indent;
            int eoln = findNewlineAfterStatement(node);
            CodeStyle.BracesGenerationStyle redundantWhileBraces = cs.redundantWhileBraces();
            if (redundantWhileBraces == CodeStyle.BracesGenerationStyle.GENERATE && (startOffset > sp.getStartPosition(root, node) || endOffset < eoln || node.getCondition().getKind() == Tree.Kind.ERRONEOUS)) {
                redundantWhileBraces = CodeStyle.BracesGenerationStyle.LEAVE_ALONE;
            }
            wrapStatement(cs.wrapWhileStatement(), redundantWhileBraces, cs.spaceBeforeWhileLeftBrace() ? 1 : 0, node.getStatement());
            return true;
        }

        @Override
        public Boolean visitForLoop(ForLoopTree node, Void p) {
            accept(FOR);
            boolean old = continuationIndent;
            try {
                continuationIndent = true;
                spaces(cs.spaceBeforeForParen() ? 1 : 0);
                accept(LPAREN);
                spaces(cs.spaceWithinForParens() ? 1 : 0);
                List<? extends StatementTree> inits = node.getInitializer();
                int alignIndent = -1;
                if (inits != null && !inits.isEmpty()) {
                    if (cs.alignMultilineFor())
                        alignIndent = col;
                    for (Iterator<? extends StatementTree> it = inits.iterator(); it.hasNext();) {
                        scan(it.next(), p);
                        if (it.hasNext() && !fieldGroup) {
                            spaces(cs.spaceBeforeComma() ? 1 : 0);
                            accept(COMMA);
                            spaces(cs.spaceAfterComma() ? 1 : 0);
                        }
                    }
                    spaces(cs.spaceBeforeSemi() ? 1 : 0);
                }
                accept(SEMICOLON);
                ExpressionTree cond = node.getCondition();
                if (cond != null) {
                    wrapTree(cs.wrapFor(), alignIndent, cs.spaceAfterSemi() ? 1 : 0, cond);
                    spaces(cs.spaceBeforeSemi() ? 1 : 0);
                }
                accept(SEMICOLON);
                List<? extends ExpressionStatementTree> updates = node.getUpdate();
                if (updates != null && !updates.isEmpty()) {
                    boolean first = true;
                    for (Iterator<? extends ExpressionStatementTree> it = updates.iterator(); it.hasNext();) {
                        ExpressionStatementTree update = it.next();
                        if (first) {
                            wrapTree(cs.wrapFor(), alignIndent, cs.spaceAfterSemi() ? 1 : 0, update);
                        } else {
                            scan(update, p);
                        }
                        first = false;
                        if (it.hasNext()) {
                            spaces(cs.spaceBeforeComma() ? 1 : 0);
                            accept(COMMA);
                            spaces(cs.spaceAfterComma() ? 1 : 0);
                        }
                    }
                }
                spaces(cs.spaceWithinForParens() ? 1 : 0);
                accept(RPAREN);
            } finally {
                continuationIndent = old;
            }
            lastIndent = indent;
            CodeStyle.BracesGenerationStyle redundantForBraces = cs.redundantForBraces();
            int eoln = findNewlineAfterStatement(node);
            if (redundantForBraces == CodeStyle.BracesGenerationStyle.GENERATE && (startOffset > sp.getStartPosition(root, node) || endOffset < eoln || (node.getCondition() != null && node.getCondition().getKind() == Tree.Kind.ERRONEOUS))) {
                redundantForBraces = CodeStyle.BracesGenerationStyle.LEAVE_ALONE;
            }
            wrapStatement(cs.wrapForStatement(), redundantForBraces, cs.spaceBeforeForLeftBrace() ? 1 : 0, node.getStatement());
            return true;
        }

        @Override
        public Boolean visitEnhancedForLoop(EnhancedForLoopTree node, Void p) {
            accept(FOR);
            boolean old = continuationIndent;
            try {
                continuationIndent = true;
                spaces(cs.spaceBeforeForParen() ? 1 : 0);
                accept(LPAREN);
                spaces(cs.spaceWithinForParens() ? 1 : 0);
                int alignIndent = cs.alignMultilineFor() ? col : -1;
                scan(node.getVariable(), p);
                wrapOperatorAndTree(cs.wrapFor(), alignIndent, cs.spaceBeforeColon() ? 1 : 0, cs.spaceAfterColon() ? 1 : 0, -1, node.getExpression());
                spaces(cs.spaceWithinForParens() ? 1 : 0);
                accept(RPAREN);
            } finally {
                continuationIndent = old;
            }
            lastIndent = indent;
            CodeStyle.BracesGenerationStyle redundantForBraces = cs.redundantForBraces();
            int eoln = findNewlineAfterStatement(node);
            if (redundantForBraces == CodeStyle.BracesGenerationStyle.GENERATE && (startOffset > sp.getStartPosition(root, node) || endOffset < eoln)) {
                redundantForBraces = CodeStyle.BracesGenerationStyle.LEAVE_ALONE;
            }
            wrapStatement(cs.wrapForStatement(), redundantForBraces, cs.spaceBeforeForLeftBrace() ? 1 : 0, node.getStatement());
            return true;
        }

        @Override
        public Boolean visitSynchronized(SynchronizedTree node, Void p) {
            accept(SYNCHRONIZED);
            boolean old = continuationIndent;
            try {
                continuationIndent = true;
                spaces(cs.spaceBeforeSynchronizedParen() ? 1 : 0);
                scan(node.getExpression(), p);
            } finally {
                continuationIndent = old;
            }
            lastIndent = indent;
            scan(node.getBlock(), p);
            return true;
        }

        @Override
        public Boolean visitSwitch(SwitchTree node, Void p) {
            return handleSwitch(node, p);
        }

        @Override
        public Boolean visitSwitchExpression(SwitchExpressionTree node, Void p) {
            return handleSwitch(node,p);
        }

        @Override
        public Boolean visitYield(YieldTree node, Void p) {
            ExpressionTree exprTree = node.getValue();
            if (exprTree != null) {
                accept(IDENTIFIER);
                space();
                scan(exprTree, p);
            }
            accept(SEMICOLON);
            return true;
        }

        @Override
        public Boolean visitBindingPattern(BindingPatternTree node, Void p) {
            scan(node.getVariable(), p);
            return true;
        }

        @Override
        public Boolean visitDefaultCaseLabel(DefaultCaseLabelTree node, Void p) {
            accept(DEFAULT);
            return true;
        }

        @Override
        public Boolean visitConstantCaseLabel(ConstantCaseLabelTree node, Void p) {
            scan(node.getConstantExpression(), p);
            return true;
        }

        @Override
        public Boolean visitPatternCaseLabel(PatternCaseLabelTree node, Void p) {
            scan(node.getPattern(), p);
            return true;
        }

        @Override
        public Boolean visitDeconstructionPattern(DeconstructionPatternTree node, Void p) {
            scan(node.getDeconstructor(), p);
            spaces(0);
            accept(LPAREN);
            spaces(cs.spaceWithinMethodDeclParens() ? 1 : 0, true);
            wrapList(cs.wrapMethodParams(), cs.alignMultilineMethodParams(), false, COMMA, node.getNestedPatterns());
            accept(RPAREN);
            return true;
        }

        private boolean handleSwitch(Tree node, Void p) {
            ExpressionTree selExpr;
            List<? extends CaseTree> cases;
            if (node.getKind() == Kind.SWITCH) {
                selExpr = ((SwitchTree) node).getExpression();
                cases = ((SwitchTree) node).getCases();
            } else {
                selExpr = ((SwitchExpressionTree) node).getExpression();
                cases = ((SwitchExpressionTree) node).getCases();
            }
            accept(SWITCH);
            boolean oldContinuationIndent = continuationIndent;
            try {
                continuationIndent = true;
                spaces(cs.spaceBeforeSwitchParen() ? 1 : 0);
                scan(selExpr, p);
            } finally {
                continuationIndent = oldContinuationIndent;
            }
            CodeStyle.BracePlacement bracePlacement = cs.getOtherBracePlacement();
            boolean spaceBeforeLeftBrace = cs.spaceBeforeSwitchLeftBrace();
            boolean indentCases = cs.indentCasesFromSwitch() ;
            int old = lastIndent;
            int halfIndent = lastIndent;
            if (node.getKind() == Kind.SWITCH_EXPRESSION) {
                continuationIndent = false;
            }
            switch (bracePlacement) {
                case SAME_LINE:
                    spaces(spaceBeforeLeftBrace ? 1 : 0, tokens.offset() < startOffset);
                    accept(LBRACE);
                    if (indentCases) {
                        indent = lastIndent + indentSize;
                    }
                    break;
                case NEW_LINE:
                    newline();
                    accept(LBRACE);
                    if (indentCases) {
                        indent = lastIndent + indentSize;
                    }
                    break;
                case NEW_LINE_HALF_INDENTED:
                    int oldLast = lastIndent;
                    indent = lastIndent + (indentSize >> 1);
                    halfIndent = indent;
                    newline();
                    accept(LBRACE);
                    if (indentCases) {
                        indent = oldLast + indentSize;
                    } else {
                        indent = old;
                    }
                    break;
                case NEW_LINE_INDENTED:
                    indent = lastIndent + indentSize;
                    halfIndent = indent;
                    newline();
                    accept(LBRACE);
                    if (!indentCases) {
                        indent = old;
                    }
                    break;
            }
            if (node.getKind() == Kind.SWITCH_EXPRESSION) {
                indent = lastIndent + indentSize;
            }
            try {
                for (CaseTree caseTree : cases) {
                    newline();
                    scan(caseTree, p);
                }

                newline();
                indent = halfIndent;
                Diff diff = diffs.isEmpty() ? null : diffs.getFirst();
                if (diff != null && diff.end == tokens.offset()) {
                    if (diff.text != null) {
                        int idx = diff.text.lastIndexOf('\n'); //NOI18N
                        if (idx < 0) {
                            diff.text = getIndent();
                        } else {
                            diff.text = diff.text.substring(0, idx + 1) + getIndent();
                        }

                    }
                    String spaces = diff.text != null ? diff.text : getIndent();
                    if (spaces.equals(fText.substring(diff.start, diff.end))) {
                        diffs.removeFirst();
                    }
                } else if (tokens.movePrevious()) {
                    if (tokens.token().id() == WHITESPACE) {
                        String text = tokens.token().text().toString();
                        int idx = text.lastIndexOf('\n'); //NOI18N
                        if (idx >= 0) {
                            text = text.substring(idx + 1);
                            String ind = getIndent();
                            if (!ind.equals(text)) {
                                addDiff(new Diff(tokens.offset() + idx + 1, tokens.offset() + tokens.token().length(), ind));
                            }
                        }
                    }
                    tokens.moveNext();
                }
            } finally {
                continuationIndent = oldContinuationIndent;
            }
                accept(RBRACE);
            indent = lastIndent = old;
            return true;
        }

        @Override
        public Boolean visitCase(CaseTree node, Void p) {
            List<? extends CaseLabelTree> labels = node.getLabels();
            if (labels != null && !labels.isEmpty()) {
                if (tokens.token().id() == JavaTokenId.DEFAULT && labels.get(0).getKind() == Kind.DEFAULT_CASE_LABEL) {
                    accept(DEFAULT);
                } else {
                    accept(CASE);
                    space();
                    for (Iterator<? extends CaseLabelTree> it = labels.iterator(); it.hasNext();) {
                        CaseLabelTree label = it.next();
                        scan(label, p);
                        if (it.hasNext()) {
                            spaces(0);
                            accept(COMMA);
                            space();
                        }
                    }
                    if (node.getGuard() != null) {
                        space();
                        accept(IDENTIFIER);
                        space();
                        scan(node.getGuard(), p);
                    }
                }
            } else if (!node.getExpressions().isEmpty()) {
                List<? extends ExpressionTree> exprs = node.getExpressions();
                accept(CASE);
                space();
                exprs.forEach(exp -> {
                    scan(exp, p);
                });
            } else {
                accept(DEFAULT);
            }
            List<? extends StatementTree> statements = node.getStatements();
            Tree caseBody = null;
            if(statements != null)
                accept(COLON);
            else {
                space();
                accept(ARROW);
                caseBody = node.getBody();
                if (caseBody instanceof StatementTree)
                    statements = Collections.singletonList((StatementTree) caseBody);
            }
            int old = indent;
            indent = lastIndent + indentSize;
            boolean first = true;
            if(statements != null)
            {
                for (StatementTree stat : statements) {
                    if (first) {
                        if (stat.getKind() == Tree.Kind.BLOCK) {
                            indent = lastIndent;
                        }
                        if (stat.getKind() == Tree.Kind.TRY) {
                            wrapTree(cs.wrapCaseStatements(), -1, 1, stat);
                        } else {
                            wrapStatement(cs.wrapCaseStatements(), CodeStyle.BracesGenerationStyle.LEAVE_ALONE, 1, stat);
                        }
                    } else {
                        newline();
                        scan(stat, p);
                    }
                    first = false;
                }
            }
            else if (caseBody != null) {
                newline();
                scan(caseBody, p);
                spaces(cs.spaceBeforeSemi() ? 1 : 0);
                accept(SEMICOLON);
            }
            indent = old;
            return true;
        }

        private void removeWhiteSpace(JavaTokenId forToken) {
            do {
                if (tokens.offset() >= endPos) {
                    break;
                }
                if (tokens.token().id() == WHITESPACE) {
                    String text = tokens.token().text().toString();
                    String ind = getIndent();
                    if (!ind.equals(text)) {
                        addDiff(new Diff(tokens.offset(), tokens.offset() + tokens.token().length(), " "));
                    }
                } else if (forToken == null || tokens.token().id() == forToken) {
                    break;
                }
            } while (tokens.moveNext());
        }

        @Override
        public Boolean visitBreak(BreakTree node, Void p) {
            JavaTokenId token = accept(BREAK);
            Name label = node.getLabel();
            if (label != null) {
                space();
                accept(IDENTIFIER, UNDERSCORE);
            }
            accept(SEMICOLON);
            return true;
        }

        @Override
        public Boolean visitContinue(ContinueTree node, Void p) {
            accept(CONTINUE);
            Name label = node.getLabel();
            if (label != null) {
                space();
                accept(IDENTIFIER, UNDERSCORE);
            }
            accept(SEMICOLON);
            return true;
        }

        @Override
        public Boolean visitAssignment(AssignmentTree node, Void p) {
            int alignIndent = cs.alignMultilineAssignment() ? col : -1;
            boolean b = scan(node.getVariable(), p);
            if (b || getCurrentPath().getParentPath().getLeaf().getKind() != Tree.Kind.ANNOTATION) {
                boolean spaceAroundAssignOps = insideAnnotation ? cs.spaceAroundAnnotationValueAssignOps() : cs.spaceAroundAssignOps();
                if (cs.wrapAfterAssignOps()) {
                    boolean containedNewLine = spaces(spaceAroundAssignOps ? 1 : 0, false);
                    if (accept(EQ) == EQ && containedNewLine)
                        newline();
                    ExpressionTree expr = node.getExpression();
                    if (expr.getKind() == Tree.Kind.NEW_ARRAY && ((NewArrayTree)expr).getType() == null) {
                        if (cs.getOtherBracePlacement() == CodeStyle.BracePlacement.SAME_LINE)
                            spaces(spaceAroundAssignOps ? 1 : 0);
                        scan(expr, p);
                    } else {
                        if (wrapAnnotation && expr.getKind() == Tree.Kind.ANNOTATION) {
                            wrapTree(CodeStyle.WrapStyle.WRAP_ALWAYS, alignIndent, spaceAroundAssignOps ? 1 : 0, expr);
                        } else {
                            wrapTree(cs.wrapAssignOps(), alignIndent, spaceAroundAssignOps ? 1 : 0, expr);
                        }
                    }
                } else {
                    wrapOperatorAndTree(cs.wrapAssignOps(), alignIndent, spaceAroundAssignOps ? 1 : 0, node.getExpression());
                }
            } else {
                scan(node.getExpression(), p);
            }
            return true;
        }

        @Override
        public Boolean visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
            int alignIndent = cs.alignMultilineAssignment() ? col : -1;
            scan(node.getVariable(), p);
            if (cs.wrapAfterAssignOps()) {
                boolean containedNewLine = spaces(cs.spaceAroundAssignOps() ? 1 : 0, false);
                if (OPERATOR.equals(tokens.token().id().primaryCategory())) {
                    col += tokens.token().length();
                    lastBlankLines = -1;
                    lastBlankLinesTokenIndex = -1;
                    lastBlankLinesDiff = null;
                    tokens.moveNext();
                    if (containedNewLine)
                        newline();
                }
                wrapTree(cs.wrapAssignOps(), alignIndent, cs.spaceAroundAssignOps() ? 1 : 0, node.getExpression());
            } else {
                wrapOperatorAndTree(cs.wrapAssignOps(), alignIndent, cs.spaceAroundAssignOps() ? 1 : 0, node.getExpression());
            }
            return true;
        }

        @Override
        public Boolean visitPrimitiveType(PrimitiveTypeTree node, Void p) {
            switch (node.getPrimitiveTypeKind()) {
            case BOOLEAN:
                accept(BOOLEAN);
                break;
            case BYTE:
                accept(BYTE);
                break;
            case CHAR:
                accept(CHAR);
                break;
            case DOUBLE:
                accept(DOUBLE);
                break;
            case FLOAT:
                accept(FLOAT);
                break;
            case INT:
                accept(INT);
                break;
            case LONG:
                accept(LONG);
                break;
            case SHORT:
                accept(SHORT);
                break;
            case VOID:
                accept(VOID);
                break;
            }
            return true;
        }

        @Override
        public Boolean visitArrayType(ArrayTypeTree node, Void p) {
            boolean ret = scan(node.getType(), p);
            int index = tokens.index();
            int c = col;
            Diff d = diffs.isEmpty() ? null : diffs.getFirst();
            JavaTokenId id = accept(LBRACKET, ELLIPSIS, IDENTIFIER, UNDERSCORE);
            if (id == ELLIPSIS)
                return ret;
            if (id != IDENTIFIER && id != UNDERSCORE) {
                accept(RBRACKET);
                return ret;
            }
            rollback(index, c, d);
            spaces(1, fieldGroup);
            accept(IDENTIFIER, UNDERSCORE);
            accept(LBRACKET);
            accept(RBRACKET);
            return false;
        }

        @Override
        public Boolean visitArrayAccess(ArrayAccessTree node, Void p) {
            scan(node.getExpression(), p);
            accept(LBRACKET);
            spaces(cs.spaceWithinArrayIndexBrackets() ? 1 : 0);
            scan(node.getIndex(), p);
            spaces(cs.spaceWithinArrayIndexBrackets() ? 1 : 0);
            accept(RBRACKET);
            return true;
        }

        @Override
        public Boolean visitNewArray(NewArrayTree node, Void p) {
            Tree type = node.getType();
            List<? extends ExpressionTree> inits = node.getInitializers();
            if (type != null) {
                accept(NEW);
                space();
                int n = inits != null ? 1 : 0;
                while (type.getKind() == Tree.Kind.ARRAY_TYPE) {
                    n++;
                    type = ((ArrayTypeTree)type).getType();
                }
                scan(type, p);
                for (ExpressionTree dim : node.getDimensions()) {
                    accept(LBRACKET);
                    spaces(cs.spaceWithinArrayInitBrackets() ? 1 : 0);
                    scan(dim, p);
                    spaces(cs.spaceWithinArrayInitBrackets() ? 1 : 0);
                    accept(RBRACKET);
                }
                while(--n >= 0) {
                    accept(LBRACKET);
                    accept(RBRACKET);
                }
            }
            if (inits != null) {
                CodeStyle.BracePlacement bracePlacement = cs.getOtherBracePlacement();
                boolean spaceBeforeLeftBrace = cs.spaceBeforeArrayInitLeftBrace();
                boolean oldContinuationIndent = continuationIndent;
                try {
                    continuationIndent = isLastIndentContinuation;
                    int old = lastIndent;
                    int halfIndent = lastIndent;
                    switch(bracePlacement) {
                        case SAME_LINE:
                            if (type != null)
                                spaces(spaceBeforeLeftBrace ? 1 : 0, tokens.offset() < startOffset);
                            accept(LBRACE);
                            indent = lastIndent + indentSize;
                            break;
                        case NEW_LINE:
                            newline();
                            accept(LBRACE);
                            indent = lastIndent + indentSize;
                            break;
                        case NEW_LINE_HALF_INDENTED:
                            int oldLast = lastIndent;
                            indent = lastIndent + (indentSize >> 1);
                            halfIndent = indent;
                            newline();
                            accept(LBRACE);
                            indent = oldLast + indentSize;
                            break;
                        case NEW_LINE_INDENTED:
                            indent = lastIndent + indentSize;
                            halfIndent = indent;
                            newline();
                            accept(LBRACE);
                            break;
                    }
                    boolean afterNewline = bracePlacement != CodeStyle.BracePlacement.SAME_LINE;
                    if (!inits.isEmpty()) {
                        if (afterNewline)
                            newline();
                        else
                            spaces(cs.spaceWithinBraces() ? 1 : 0, true);
                        WrapStyle ws = insideAnnotation && inits.get(0).getKind() == Tree.Kind.ANNOTATION ? cs.wrapAnnotations() : cs.wrapArrayInit();
                        wrapList(ws, cs.alignMultilineArrayInit(), false, COMMA, inits);
                        if (tokens.token().text().toString().indexOf('\n') >= 0)
                            afterNewline = true;
                        int index = tokens.index();
                        int c = col;
                        Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                        if (accept(COMMA) == null)
                            rollback(index, c, d);
                        indent = lastIndent - indentSize;
                        if (afterNewline)
                            newline();
                        else
                            spaces(cs.spaceWithinBraces() ? 1 : 0);
                    } else if (afterNewline) {
                        newline();
                    }
                    indent = halfIndent;
                    if (afterNewline) {
                        Diff diff = diffs.isEmpty() ? null : diffs.getFirst();
                        if (diff != null && diff.end == tokens.offset()) {
                            if (diff.text != null) {
                                int idx = diff.text.lastIndexOf('\n'); //NOI18N
                                if (idx < 0)
                                    diff.text = getIndent();
                                else
                                    diff.text = diff.text.substring(0, idx + 1) + getIndent();

                            }
                            String spaces = diff.text != null ? diff.text : getIndent();
                            if (spaces.equals(fText.substring(diff.start, diff.end)))
                                diffs.removeFirst();
                        } else if (tokens.movePrevious()) {
                            if (tokens.token().id() == WHITESPACE) {
                                String text =  tokens.token().text().toString();
                                int idx = text.lastIndexOf('\n'); //NOI18N
                                if (idx >= 0) {
                                    text = text.substring(idx + 1);
                                    String ind = getIndent();
                                    if (!ind.equals(text))
                                        addDiff(new Diff(tokens.offset() + idx + 1, tokens.offset() + tokens.token().length(), ind));
                                }
                            }
                            tokens.moveNext();
                        }
                    }
                    accept(RBRACE);
                    indent = lastIndent = old;
                } finally {
                    continuationIndent = oldContinuationIndent;
                }
            }
            return true;
        }

        @Override
        public Boolean visitIdentifier(IdentifierTree node, Void p) {
            accept(IDENTIFIER, UNDERSCORE, THIS, SUPER);
            return true;
        }

        @Override
        public Boolean visitUnary(UnaryTree node, Void p) {
            JavaTokenId id = tokens.token().id();
            if (OPERATOR.equals(id.primaryCategory())) {
                spaces(cs.spaceAroundUnaryOps() ? 1 : 0);
                col += tokens.token().length();
                lastBlankLines = -1;
                lastBlankLinesTokenIndex = -1;
                lastBlankLinesDiff = null;
                tokens.moveNext();
                int index = tokens.index();
                int c = col;
                Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                spaces(cs.spaceAroundUnaryOps() ? 1 : 0);
                if (tokens.token().id() == id) {
                    rollback(index, c, d);
                    space();
                }
                scan(node.getExpression(), p);
            } else {
                scan(node.getExpression(), p);
                spaces(cs.spaceAroundUnaryOps() ? 1 : 0);
                col += tokens.token().length();
                lastBlankLines = -1;
                lastBlankLinesTokenIndex = -1;
                lastBlankLinesDiff = null;
                tokens.moveNext();
                spaces(cs.spaceAroundUnaryOps() ? 1 : 0);
            }
            return true;
        }

        @Override
        public Boolean visitBinary(BinaryTree node, Void p) {
            int alignIndent = cs.alignMultilineBinaryOp() ? col : -1;
            scan(node.getLeftOperand(), p);
            if (cs.wrapAfterBinaryOps()) {
                boolean containedNewLine = spaces(cs.spaceAroundBinaryOps() ? 1 : 0, false);
                if (OPERATOR.equals(tokens.token().id().primaryCategory())) {
                    col += tokens.token().length();
                    lastBlankLines = -1;
                    lastBlankLinesTokenIndex = -1;
                    tokens.moveNext();
                    if (containedNewLine)
                        newline();
                }
                wrapTree(cs.wrapBinaryOps(), alignIndent, cs.spaceAroundBinaryOps() ? 1 : 0, node.getRightOperand());
            } else {
                wrapOperatorAndTree(cs.wrapBinaryOps(), alignIndent, cs.spaceAroundBinaryOps() ? 1 : 0, node.getRightOperand());
            }
            return true;
        }

        @Override
        public Boolean visitConditionalExpression(ConditionalExpressionTree node, Void p) {
            int alignIndent = cs.alignMultilineTernaryOp() ? col : -1;
            scan(node.getCondition(), p);
            boolean old = continuationIndent;
            int oldIndent = indent;
            try {
                if (isLastIndentContinuation) {
                    indent = indent();
                }
                if (cs.wrapAfterTernaryOps()) {
                    boolean containedNewLine = spaces(cs.spaceAroundTernaryOps() ? 1 : 0, false);
                    accept(QUESTION);
                    if (containedNewLine)
                        newline();
                    wrapTree(cs.wrapTernaryOps(), alignIndent, cs.spaceAroundTernaryOps() ? 1 : 0, node.getTrueExpression());
                    containedNewLine = spaces(cs.spaceAroundTernaryOps() ? 1 : 0, false);
                    accept(COLON);
                    if (containedNewLine)
                        newline();
                    wrapTree(cs.wrapTernaryOps(), alignIndent, cs.spaceAroundTernaryOps() ? 1 : 0, node.getFalseExpression());
                } else {
                    wrapOperatorAndTree(cs.wrapTernaryOps(), alignIndent, cs.spaceAroundTernaryOps() ? 1 : 0, node.getTrueExpression());
                    wrapOperatorAndTree(cs.wrapTernaryOps(), alignIndent, cs.spaceAroundTernaryOps() ? 1 : 0, node.getFalseExpression());
                }
            } finally {
                indent = oldIndent;
                continuationIndent = old;
            }
            return true;
        }

        @Override
        public Boolean visitEmptyStatement(EmptyStatementTree node, Void p) {
            accept(SEMICOLON);
            return true;
        }

        @Override
        public Boolean visitExpressionStatement(ExpressionStatementTree node, Void p) {
            boolean old = continuationIndent;
            try {
                continuationIndent = true;
                scan(node.getExpression(), p);
                accept(SEMICOLON);
            } finally {
                continuationIndent = old;
            }
            return true;
        }

        @Override
        public Boolean visitInstanceOf(InstanceOfTree node, Void p) {
            scan(node.getExpression(), p);
            space();
            accept(INSTANCEOF);
            space();
            Tree pattern = node.getPattern();
            if (pattern == null)
                pattern = node.getType();
            scan(pattern, p);
            return true;
        }

        @Override
        public Boolean visitLabeledStatement(LabeledStatementTree node, Void p) {
            if (!ERROR.contentEquals(node.getLabel()))
                accept(IDENTIFIER, UNDERSCORE);
            accept(COLON);
            int old = indent;
            if (!cs.absoluteLabelIndent()) {
                indent += cs.getLabelIndent();
            }
            int cnt = indent() - col;
            if (cnt < 0)
                newline();
            else
                spaces(cnt);
            scan(node.getStatement(), p);
            indent = old;
            return true;
        }

        @Override
        public Boolean visitTypeCast(TypeCastTree node, Void p) {
            accept(LPAREN);
            boolean spaceWithinParens = cs.spaceWithinTypeCastParens();
            spaces(spaceWithinParens ? 1 : 0);
            scan(node.getType(), p);
            spaces(spaceWithinParens ? 1 : 0);
            accept(RPAREN);
            spaces(cs.spaceAfterTypeCast() ? 1 : 0);
            scan(node.getExpression(), p);
            return true;
        }

        @Override
        public Boolean visitIntersectionType(IntersectionTypeTree node, Void p) {
            for (Iterator<? extends Tree> it = node.getBounds().iterator(); it.hasNext();) {
                Tree bound = it.next();
                scan(bound, p);
                if (it.hasNext()) {
                    space();
                    accept(AMP);
                    space();
                }
            }
            return true;
        }

        @Override
        public Boolean visitParenthesized(ParenthesizedTree node, Void p) {
            accept(LPAREN);
            boolean spaceWithinParens;
            int old = indent;
            boolean oldContinuationIndent = continuationIndent;
            try {
                switch(getCurrentPath().getParentPath().getLeaf().getKind()) {
                    case IF:
                        spaceWithinParens = cs.spaceWithinIfParens();
                        break;
                    case FOR_LOOP:
                        spaceWithinParens = cs.spaceWithinForParens();
                        break;
                    case DO_WHILE_LOOP:
                    case WHILE_LOOP:
                        spaceWithinParens = cs.spaceWithinWhileParens();
                        break;
                    case SWITCH:
                    case SWITCH_EXPRESSION:
                        spaceWithinParens = cs.spaceWithinSwitchParens();
                        break;
                    case SYNCHRONIZED:
                        spaceWithinParens = cs.spaceWithinSynchronizedParens();
                        break;
                    case VARIABLE:
                        spaceWithinParens = cs.spaceWithinSwitchParens();
                        break;
                    default:
                        spaceWithinParens = cs.spaceWithinParens();
                        if (cs.alignMultilineParenthesized()) {
                            indent = col;
                            continuationIndent = false;
                        }
                }
                spaces(spaceWithinParens ? 1 : 0);
                scan(node.getExpression(), p);
                spaces(spaceWithinParens ? 1 : 0);
            } finally {
                indent = old;
                continuationIndent = oldContinuationIndent;
            }
            accept(RPAREN);
            return true;
        }

        @Override
        public Boolean visitLiteral(LiteralTree node, Void p) {
            do {
                col += tokens.token().length();
            } while (tokens.moveNext() && tokens.offset() < endPos);
            lastBlankLines = -1;
            lastBlankLinesTokenIndex = -1;
            lastBlankLinesDiff = null;
            return true;
        }

        @Override
        public Boolean visitErroneous(ErroneousTree node, Void p) {
            for (Tree tree : node.getErrorTrees()) {
                int pos = (int)sp.getStartPosition(getCurrentPath().getCompilationUnit(), tree);
                do {
                    if (tokens.offset() >= pos)
                        break;
                    col += tokens.token().length();
                } while (tokens.moveNext());
                lastBlankLines = -1;
                lastBlankLinesTokenIndex = -1;
                lastBlankLinesDiff = null;
                scan(tree, p);
            }
            do {
                if (tokens.offset() >= endPos)
                    break;
                int len = tokens.token().length();
                if (tokens.token().id() == WHITESPACE && tokens.offset() + len >= endPos)
                    break;
                col += len;
            } while (tokens.moveNext());
            lastBlankLines = -1;
            lastBlankLinesTokenIndex = -1;
            lastBlankLinesDiff = null;
            return true;
        }

        @Override
        public Boolean visitStringTemplate(StringTemplateTree node, Void p) {
            scan(node.getProcessor(), p);
            accept(DOT);
            for (ExpressionTree expression : node.getExpressions()) {
                accept(STRING_LITERAL);
                scan(expression, p);
            }
            accept(STRING_LITERAL);
            return true;
        }

        @Override
        public Boolean visitOther(Tree node, Void p) {
            do {
                col += tokens.token().length();
            } while (tokens.moveNext() && tokens.offset() < endPos);
            lastBlankLines = -1;
            lastBlankLinesTokenIndex = -1;
            lastBlankLinesDiff = null;
            return true;
        }

        private JavaTokenId accept(JavaTokenId first, JavaTokenId... rest) {
            if (checkWrap != null && col > rightMargin && checkWrap.pos >= lastNewLineOffset) {
                throw checkWrap;
            }
            if (tokens.offset() >= endPos || eof) {
                return null;
            }
            lastBlankLines = -1;
            lastBlankLinesTokenIndex = -1;
            lastBlankLinesDiff = null;
            EnumSet<JavaTokenId> tokenIds = EnumSet.of(first, rest);
            Token<JavaTokenId> lastWSToken = null;
            int after = 0;
            do {
                if (tokens.offset() >= endPos || eof) {
                    if (lastWSToken != null) {
                        lastBlankLines = 0;
                        lastBlankLinesTokenIndex = tokens.index() - 1;
                        lastBlankLinesDiff = diffs.isEmpty() ? null : diffs.getFirst();
                    }
                    return null;
                }
                JavaTokenId id = tokens.token().id();
                boolean contains = tokenIds.contains(id);
                if (!contains && id == IDENTIFIER) {
                    for (JavaTokenId tokenId : tokenIds) {
                        if (tokenId.fixedText() != null && tokenId.fixedText().contentEquals(tokens.token().text())) {
                            contains = true;
                            break;
                        }
                    }
                    if (TokenUtilities.textEquals(tokens.token().text(), "sealed") || TokenUtilities.textEquals(tokens.token().text(), "permits")) {
                        contains = true;
                    }
                    if (TokenUtilities.textEquals(tokens.token().text(), "non") && tokens.moveNext()) {
                        if (TokenUtilities.textEquals(tokens.token().text(), "-") && tokens.moveNext()) {
                            if (TokenUtilities.textEquals(tokens.token().text(), "sealed")) {// NOI18N
                                contains = true;
                            } else {
                                tokens.movePrevious();
                                tokens.movePrevious();
                            }
                        } else {
                            tokens.movePrevious();
                        }
                    }
                }
                if (contains) {
                    String spaces = after == 1 //after line comment
                            ? getIndent()
                            : after == 2 //after javadoc comment
                            ? getNewlines(1) + getIndent()
                            : null;
                    if (lastWSToken != null) {
                        if (spaces == null || !spaces.contentEquals(lastWSToken.text()))
                            addDiff(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                    } else {
                        if (spaces != null && spaces.length() > 0)
                            addDiff(new Diff(tokens.offset(), tokens.offset(), spaces));
                    }
                    if (after > 0)
                        col = indent();
                    col += tokens.token().length();
                    bof = false;
                    return tokens.moveNext() ? id : null;
                }
                switch(id) {
                    case WHITESPACE:
                        lastWSToken = tokens.token();
                        break;
                    case LINE_COMMENT:
                        if (lastWSToken != null) {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : SPACE;
                            if (!spaces.contentEquals(lastWSToken.text()))
                                addDiff(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                            lastWSToken = null;
                        } else {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : null;
                            if (spaces != null && spaces.length() > 0)
                                addDiff(new Diff(tokens.offset(), tokens.offset(), spaces));
                        }
                        col = 0;
                        after = 1; //line comment
                        break;
                    case JAVADOC_COMMENT:
                        if (lastWSToken != null) {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : SPACE;
                            if (!spaces.contentEquals(lastWSToken.text()))
                                addDiff(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                            lastWSToken = null;
                            if (after > 0)
                                col = indent();
                            else
                                col++;
                        } else {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : null;
                            if (spaces != null && spaces.length() > 0)
                                addDiff(new Diff(tokens.offset(), tokens.offset(), spaces));
                            if (after > 0)
                                col = indent();
                        }
                        String tokenText = tokens.token().text().toString();
                        int idx = tokenText.lastIndexOf('\n'); //NOI18N
                        if (idx >= 0)
                            tokenText = tokenText.substring(idx + 1);
                        col += getCol(tokenText);
                        reformatComment();
                        after = 2; //javadoc comment
                        break;
                    case BLOCK_COMMENT:
                        if (lastWSToken != null) {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : SPACE;
                            if (!spaces.contentEquals(lastWSToken.text()))
                                addDiff(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                            lastWSToken = null;
                            if (after > 0)
                                col = indent();
                            else
                                col++;
                        } else {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : null;
                            if (spaces != null && spaces.length() > 0)
                                addDiff(new Diff(tokens.offset(), tokens.offset(), spaces));
                            if (after > 0)
                                col = indent();
                        }
                        tokenText = tokens.token().text().toString();
                        idx = tokenText.lastIndexOf('\n'); //NOI18N
                        if (idx >= 0)
                            tokenText = tokenText.substring(idx + 1);
                        col += getCol(tokenText);
                        reformatComment();
                        after = 0;
                        break;
                    default:
                        if (lastWSToken != null) {
                            lastBlankLines = -1;
                            lastBlankLinesTokenIndex = tokens.index() - 1;
                            lastBlankLinesDiff = diffs.isEmpty() ? null : diffs.getFirst();
                        }
                        bof = false;
                        return null;
                }
            } while(tokens.moveNext());
            eof = true;
            return null;
        }

        private void space() {
            spaces(1);
        }

        private void spaces(int count) {
            spaces(count, false);
        }

        private boolean spaces(int count, boolean preserveNewline) {
            if (checkWrap != null && col > rightMargin && checkWrap.pos >= lastNewLineOffset) {
                throw checkWrap;
            }
            Token<JavaTokenId> lastWSToken = null;
            boolean containedNewLine = false;
            int after = 0;
            do {
                if (tokens.offset() >= endPos) {
                    if (lastWSToken != null) {
                        tokens.movePrevious();
                    }
                    return containedNewLine;
                }
                switch(tokens.token().id()) {
                    case WHITESPACE:
                        lastWSToken = tokens.token();
                        break;
                    case LINE_COMMENT:
                        containedNewLine = true;
                        if (lastWSToken != null) {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : SPACE;
                            String text = lastWSToken.text().toString();
                            int idx = text.lastIndexOf('\n'); //NOI18N
                            if (idx >= 0) {
                                if (preserveNewline) {
                                    spaces = getNewlines(1) + getIndent();
                                    lastBlankLines = 1;
                                    lastBlankLinesTokenIndex = tokens.index();
                                    lastBlankLinesDiff = diffs.isEmpty() ? null : diffs.getFirst();
                                    lastNewLineOffset = tokens.offset();
                                }
                            }
                            if (!spaces.contentEquals(lastWSToken.text()))
                                addDiff(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                            lastWSToken = null;
                        } else {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : null;
                            if (spaces != null && spaces.length() > 0)
                                addDiff(new Diff(tokens.offset(), tokens.offset(), spaces));
                        }
                        col = 0;
                        after = 1; //line comment
                        break;
                    case JAVADOC_COMMENT:
                        if (lastWSToken != null) {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : SPACE;
                            String text = lastWSToken.text().toString();
                            int idx = text.lastIndexOf('\n'); //NOI18N
                            if (idx >= 0) {
                                containedNewLine = true;
                                if (preserveNewline) {
                                    spaces = getNewlines(1) + getIndent();
                                    after = 3;
                                    lastBlankLines = 1;
                                    lastBlankLinesTokenIndex = tokens.index();
                                    lastBlankLinesDiff = diffs.isEmpty() ? null : diffs.getFirst();
                                    lastNewLineOffset = tokens.offset();
                                }
                            }
                            if (!spaces.contentEquals(lastWSToken.text()))
                                addDiff(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                            lastWSToken = null;
                            if (after > 0)
                                col = indent();
                            else
                                col++;
                        } else {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : null;
                            if (spaces != null && spaces.length() > 0)
                                addDiff(new Diff(tokens.offset(), tokens.offset(), spaces));
                            if (after > 0)
                                col = indent();
                        }
                        String tokenText = tokens.token().text().toString();
                        int idx = tokenText.lastIndexOf('\n'); //NOI18N
                        if (idx >= 0)
                            tokenText = tokenText.substring(idx + 1);
                        col += getCol(tokenText);
                        reformatComment();
                        after = 2; //javadoc comment
                        break;
                    case BLOCK_COMMENT:
                        if (lastWSToken != null) {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : SPACE;
                            String text = lastWSToken.text().toString();
                            idx = text.lastIndexOf('\n'); //NOI18N
                            if (idx >= 0) {
                                containedNewLine = true;
                                if (preserveNewline) {
                                    spaces = getNewlines(1) + getIndent();
                                    after = 3;
                                    lastBlankLines = 1;
                                    lastBlankLinesTokenIndex = tokens.index();
                                    lastBlankLinesDiff = diffs.isEmpty() ? null : diffs.getFirst();
                                    lastNewLineOffset = tokens.offset();
                                }
                            }
                            if (!spaces.contentEquals(lastWSToken.text()))
                                addDiff(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                            lastWSToken = null;
                            if (after > 0)
                                col = indent();
                            else
                                col++;
                        } else {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : null;
                            if (spaces != null && spaces.length() > 0)
                                addDiff(new Diff(tokens.offset(), tokens.offset(), spaces));
                            if (after > 0)
                                col = indent();
                        }
                        tokenText = tokens.token().text().toString();
                        idx = tokenText.lastIndexOf('\n'); //NOI18N
                        if (idx >= 0)
                            tokenText = tokenText.substring(idx + 1);
                        col += getCol(tokenText);
                        reformatComment();
                        after = 0;
                        break;
                    default:
                        String spaces = after == 1 //after line comment
                                ? getIndent()
                                : after == 2 //after javadoc comment
                                ? getNewlines(1) + getIndent()
                                : getSpaces(count);
                        if (lastWSToken != null) {
                            String text = lastWSToken.text().toString();
                            idx = text.lastIndexOf('\n'); //NOI18N
                            if (idx >= 0) {
                                containedNewLine = true;
                                if (preserveNewline) {
                                    spaces = getNewlines(1) + getIndent();
                                    after = 3;
                                    lastBlankLines = 1;
                                    lastBlankLinesTokenIndex = tokens.index();
                                    lastBlankLinesDiff = diffs.isEmpty() ? null : diffs.getFirst();
                                    lastNewLineOffset = tokens.offset();
                                }
                            }
                            if (!spaces.contentEquals(lastWSToken.text()))
                                addDiff(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                        } else if (spaces.length() > 0) {
                            addDiff(new Diff(tokens.offset(), tokens.offset(), spaces));
                        }
                        if (after > 0)
                            col = indent();
                        else
                            col += count;
                        return containedNewLine;
                }
            } while(tokens.moveNext());
            return containedNewLine;
        }

        private void newline() {
            blankLines(0);
        }

        private void blankLines(int count) {
            if (checkWrap != null && col > rightMargin && checkWrap.pos >= lastNewLineOffset) {
                throw checkWrap;
            }
            int maxCount = bof ? 0 : maxPreservedBlankLines;
            if (maxCount < count) {
                maxCount = count;
            }
            if (!bof && templateEdit && maxCount < 1) {
                maxCount = 1;
            }
            if (lastBlankLinesTokenIndex < 0) {
                lastBlankLines = count;
                lastBlankLinesTokenIndex = tokens.index();
                lastBlankLinesDiff = diffs.isEmpty() ? null : diffs.getFirst();
            } else if (lastBlankLines < count) {
                lastBlankLines = count;
                rollback(lastBlankLinesTokenIndex, lastBlankLinesTokenIndex, lastBlankLinesDiff);
            } else {
                Diff diff = diffs.isEmpty() ? null : diffs.getFirst();
                if (diff != null && diff.end == tokens.offset()) {
                    if (diff.text != null) {
                        int idx = diff.text.lastIndexOf('\n'); //NOI18N
                        if (idx < 0)
                            diff.text = getIndent();
                        else
                            diff.text = diff.text.substring(0, idx + 1) + getIndent();
                    }
                    String spaces = diff.text != null ? diff.text : getIndent();
                    if (spaces.equals(fText.substring(diff.start, diff.end)))
                        diffs.removeFirst();
                } else if (tokens.movePrevious()) {
                    if (tokens.token().id() == WHITESPACE) {
                        String text =  tokens.token().text().toString();
                        int idx = text.lastIndexOf('\n'); //NOI18N
                        if (idx >= 0) {
                            text = text.substring(idx + 1);
                            String ind = getIndent();
                            if (!ind.equals(text))
                                addDiff(new Diff(tokens.offset() + idx + 1, tokens.offset() + tokens.token().length(), ind));
                        } else if (tokens.movePrevious()) {
                            if (tokens.token().id() == LINE_COMMENT) {
                                tokens.moveNext();
                                String ind = getIndent();
                                if (!ind.equals(text))
                                    addDiff(new Diff(tokens.offset(), tokens.offset() + tokens.token().length(), ind));

                            } else {
                                tokens.moveNext();
                            }
                        }
                    }
                    tokens.moveNext();
                }
                col = indent();
                return;
            }
            lastNewLineOffset = tokens.offset();
            checkWrap = null;
            Token<JavaTokenId> lastToken = null;
            int after = 0;
            Diff pendingDiff = null;
            String pendingText = null;
            int beforeCnt = 0;
            do {
                if (tokens.offset() >= endPos)
                    return;
                switch(tokens.token().id()) {
                    case WHITESPACE:
                        lastToken = tokens.token();
                        break;
                    case BLOCK_COMMENT:
                        if (tokens.index() > 1 && after != 1) {
                            if (maxCount < Integer.MAX_VALUE)
                                maxCount++;
                            count++;
                        }
                        if (lastToken != null) {
                            int offset = tokens.offset() - lastToken.length();
                            String text = lastToken.text().toString();
                            int idx = 0;
                            int lastIdx = 0;
                            while(maxCount > 0 && (idx = text.indexOf('\n', lastIdx)) >= 0) { //NOI18N
                                if (idx > lastIdx)
                                    addDiff(new Diff(offset + lastIdx, offset + idx, null));
                                lastIdx = idx + 1;
                                maxCount--;
                                count--;
                                beforeCnt--;
                            }
                            if ((idx = text.lastIndexOf('\n')) >= 0) { //NOI18N
                                if (idx > lastIdx)
                                    addDiff(new Diff(offset + lastIdx, offset + idx + 1, null));
                                lastIdx = idx + 1;
                            }
                            if (lastIdx == 0 && count < 0 && after != 1) {
                                count = 0;
                            }
                            String ind;
                            if (pendingDiff != null) {
                                pendingDiff.text = beforeCnt < 0 ? getIndent() : getNewlines(count) + getIndent();
                                if (!pendingDiff.text.equals(pendingText)) {
                                    addDiff(pendingDiff);
                                    pendingDiff = null;
                                }
                                ind = after == 3 ? SPACE : beforeCnt < 0 ? getNewlines(count) + getIndent() : getIndent();
                            } else {
                                ind = after == 3 ? SPACE : getNewlines(count) + getIndent();
                            }
                            if (!ind.equals(text.substring(lastIdx)))
                                addDiff(new Diff(offset + lastIdx, tokens.offset(), ind));
                            lastToken = null;
                            col = after == 3 ? col + 1 : indent();
                        }
                        reformatComment();
                        count = 0;
                        after = 3;
                        break;
                    case JAVADOC_COMMENT:
                        if (tokens.index() > 1 && after != 1) {
                            if (maxCount < Integer.MAX_VALUE)
                                maxCount++;
                            count++;
                        }
                        if (lastToken != null) {
                            int offset = tokens.offset() - lastToken.length();
                            String text = lastToken.text().toString();
                            int idx = 0;
                            int lastIdx = 0;
                            while(maxCount > 0 && (idx = text.indexOf('\n', lastIdx)) >= 0) { //NOI18N
                                if (idx > lastIdx)
                                    addDiff(new Diff(offset + lastIdx, offset + idx, null));
                                lastIdx = idx + 1;
                                maxCount--;
                                count--;
                                beforeCnt--;
                            }
                            if ((idx = text.lastIndexOf('\n')) >= 0) { //NOI18N
                                after = 0;
                                if (idx >= lastIdx)
                                    addDiff(new Diff(offset + lastIdx, offset + idx + 1, null));
                                lastIdx = idx + 1;
                            }
                            if (lastIdx == 0 && count < 0 && after != 1) {
                                count = 0;
                            }
                            String ind;
                            if (pendingDiff != null) {
                                pendingDiff.text = beforeCnt < 0 ? getIndent() : getNewlines(count) + getIndent();
                                if (!pendingDiff.text.equals(pendingText)) {
                                    addDiff(pendingDiff);
                                    pendingDiff = null;
                                }
                                ind = after == 3 ? SPACE : beforeCnt < 0 ? getNewlines(count) + getIndent() : getIndent();
                            } else {
                                ind = after == 3 ? SPACE : getNewlines(count) + getIndent();
                            }
                            if (!ind.equals(text.substring(lastIdx)))
                                addDiff(new Diff(offset + lastIdx, tokens.offset(), ind));
                            lastToken = null;
                            col = after == 3 ? col + 1 : indent();
                        } else {
                            String text = getNewlines(count) + getIndent();
                            if (text.length() > 0)
                                addDiff(new Diff(tokens.offset(), tokens.offset(), text));
                            col = indent();
                        }
                        reformatComment();
                        count = 0;
                        after = 2;
                        break;
                    case LINE_COMMENT:
                        if (lastToken != null) {
                            int offset = tokens.offset() - lastToken.length();
                            String text = lastToken.text().toString();
                            if (tokens.index() > 1 && after != 1 && text.indexOf('\n') >= 0) {
                                if (maxCount < Integer.MAX_VALUE)
                                    maxCount++;
                                count++;
                            }
                            int idx = 0;
                            int lastIdx = 0;
                            while(maxCount > 0 && (idx = text.indexOf('\n', lastIdx)) >= 0) { //NOI18N
                                if (idx > lastIdx)
                                    addDiff(new Diff(offset + lastIdx, offset + idx, null));
                                lastIdx = idx + 1;
                                maxCount--;
                                count--;
                                if (pendingText == null)
                                    beforeCnt++;
                            }
                            if ((idx = text.lastIndexOf('\n')) >= 0) { //NOI18N
                                if (idx >= lastIdx)
                                    addDiff(new Diff(offset + lastIdx, offset + idx + 1, null));
                                lastIdx = idx + 1;
                            }
                            if (lastIdx == 0 && after == 1) {
                                String indent = getIndent();
                                if (!indent.equals(text))
                                    addDiff(new Diff(offset, tokens.offset(), indent));
                            } else if (lastIdx > 0 && lastIdx < lastToken.length()) {
                                pendingText = text.substring(lastIdx);
                                String indent = getIndent();
                                if (!indent.equals(pendingText)) {
                                    addDiff(new Diff(offset + lastIdx, tokens.offset(), indent));
                                    pendingText = null;
                                } else {
                                    pendingDiff = new Diff(offset + lastIdx, tokens.offset(), null);
                                }
                            }
                            lastToken = null;
                        }
                        after = 1;
                        break;
                    default:
                        if (tokens.index() > 1 && after != 1) {
                            if (maxCount < Integer.MAX_VALUE)
                                maxCount++;
                            count++;
                        }
                        if (lastToken != null) {
                            int offset = tokens.offset() - lastToken.length();
                            String text = lastToken.text().toString();
                            int idx = 0;
                            int lastIdx = 0;
                            while(maxCount > 0 && (idx = text.indexOf('\n', lastIdx)) >= 0) { //NOI18N
                                if (idx > 0) {
                                    if (templateEdit && idx >= lastIdx)
                                        addDiff(new Diff(offset + lastIdx, offset + idx, getIndent()));
                                    else if (idx > lastIdx)
                                        addDiff(new Diff(offset + lastIdx, offset + idx, null));
                                }
                                lastIdx = idx + 1;
                                maxCount--;
                                count--;
                                beforeCnt--;
                            }
                            if ((idx = text.lastIndexOf('\n')) >= 0) { //NOI18N
                                after = 0;
                                if (idx >= lastIdx)
                                    addDiff(new Diff(offset + lastIdx, offset + idx + 1, null));
                                lastIdx = idx + 1;
                            }
                            if (lastIdx == 0 && count < 0 && after != 1) {
                                count = 0;
                            }
                            String indent;
                            if (pendingDiff != null) {
                                pendingDiff.text = beforeCnt < 0 ? getIndent() : getNewlines(count) + getIndent();
                                if (!pendingDiff.text.equals(pendingText)) {
                                    addDiff(pendingDiff);
                                    pendingDiff = null;
                                }
                                indent = after == 3 ? SPACE : beforeCnt < 0 ? getNewlines(count) + getIndent() : getIndent();
                            } else {
                                indent = after == 3 ? SPACE : getNewlines(count) + getIndent();
                            }
                            if (!indent.equals(text.substring(lastIdx)))
                                addDiff(new Diff(offset + lastIdx, tokens.offset(), indent));
                        } else {
                            String text = getNewlines(count) + getIndent();
                            if (text.length() > 0)
                                addDiff(new Diff(tokens.offset(), tokens.offset(), text));
                        }
                        col = indent();
                        return;
                }
            } while(tokens.moveNext());
            eof = true;
        }

        private void rollback(int index, int col, Diff diff) {
            tokens.moveIndex(index);
            tokens.moveNext();
            if (diff == null) {
                diffs.clear();
            } else {
                while (!diffs.isEmpty() && diffs.getFirst() != diff)
                    diffs.removeFirst();
            }
            this.col = col;
            if (index < lastBlankLinesTokenIndex) {
                lastBlankLinesTokenIndex = -1;
            }
        }

        private void appendToDiff(String s) {
            int offset = tokens.offset();
            Diff d = diffs.isEmpty() ? null : diffs.getFirst();
            if (d != null && d.getEndOffset() == offset) {
                d.text += s;
            } else {
                addDiff(new Diff(offset, offset, s));
            }
        }

        private void addDiff(Diff diff) {
            Diff d = diffs.isEmpty() ? null : diffs.getFirst();
            if (d == null || d.getStartOffset() <= diff.getStartOffset())
                diffs.addFirst(diff);
        }

        private int wrapToken(CodeStyle.WrapStyle wrapStyle, int spacesCnt, JavaTokenId first, JavaTokenId... rest) {
            int ret = -1;
            switch (wrapStyle) {
                case WRAP_ALWAYS:
                    newline();
                    ret = col;
                    accept(first, rest);
                    break;
                case WRAP_IF_LONG:
                    int index = tokens.index();
                    int c = col;
                    Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                    int o = tokens.offset();
                    WrapAbort oldCheckWrap = checkWrap;
                    checkWrap = new WrapAbort(o);
                    try {
                        spaces(spacesCnt, true);
                        ret = col;
                        accept(first, rest);
                    } catch (WrapAbort wa) {
                    } finally {
                        checkWrap = oldCheckWrap;
                    }
                    if (this.col > rightMargin && o >= lastNewLineOffset) {
                        rollback(index, c, d);
                        newline();
                        ret = col;
                        accept(first, rest);
                    }
                    break;
                case WRAP_NEVER:
                    spaces(spacesCnt, true);
                    ret = col;
                    accept(first, rest);
                    break;
            }
            return ret;
        }

        private int wrapTree(CodeStyle.WrapStyle wrapStyle, int alignIndent, int spacesCnt, Tree tree) {
            return wrapTree(wrapStyle, alignIndent, spacesCnt, true, tree);
        }

        private int wrapTree(CodeStyle.WrapStyle wrapStyle, int alignIndent, int spacesCnt, boolean preserveNewLine, Tree tree) {
            int ret = -1;
            int oldLast = lastIndent;
            try {
                switch (wrapStyle) {
                    case WRAP_ALWAYS:
                        int old = indent;
                        try {
                            if (alignIndent >= 0) {
                                indent = continuationIndent ? alignIndent - continuationIndentSize : alignIndent;
                            }
                            newline();
                            ret = col;
                            scan(tree, null);
                        } finally {
                            indent = old;
                        }
                        break;
                    case WRAP_IF_LONG:
                        int index = tokens.index();
                        int c = col;
                        Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                        old = indent;
                        WrapAbort oldCheckWrap = checkWrap;
                        int o = tokens.offset();
                        checkWrap = new WrapAbort(tokens.offset());
                        try {
                            try {
                                if (alignIndent >= 0) {
                                    indent = continuationIndent ? alignIndent - continuationIndentSize : alignIndent;
                                }
                                spaces(spacesCnt, preserveNewLine);
                                ret = col;
                                scan(tree, null);
                            } finally {
                                indent = old;
                            }
                        } catch (WrapAbort wa) {
                        } finally {
                            checkWrap = oldCheckWrap;
                        }
                        if (col > rightMargin && o >= lastNewLineOffset) {
                            rollback(index, c, d);
                            try {
                                if (alignIndent >= 0) {
                                    indent = continuationIndent ? alignIndent - continuationIndentSize : alignIndent;
                                } else {
                                    indent = old;
                                }
                                newline();
                                ret = col;
                                scan(tree, null);
                            } finally {
                                indent = old;
                            }
                        }
                        break;
                    case WRAP_NEVER:
                        old = indent;
                        try {
                            if (alignIndent >= 0) {
                                indent = continuationIndent ? alignIndent - continuationIndentSize : alignIndent;
                            }
                            spaces(spacesCnt, preserveNewLine);
                            ret = col;
                            scan(tree, null);
                        } finally {
                            indent = old;
                        }
                        break;
                }
            } finally {
                lastIndent = oldLast;
            }
            return ret;
        }

        private int wrapOperatorAndTree(CodeStyle.WrapStyle wrapStyle, int alignIndent, int spacesCnt, Tree tree) {
            return wrapOperatorAndTree(wrapStyle, alignIndent, spacesCnt, spacesCnt, -1, tree);
        }

        private int wrapOperatorAndTree(CodeStyle.WrapStyle wrapStyle, int alignIndent, int spacesCntBeforeOp, int spacesCntAfterOp, int treeIndent, Tree tree) {
            int ret = -1;
            switch (wrapStyle) {
                case WRAP_ALWAYS:
                    int old = indent;
                    int oldLast = lastIndent;
                    boolean oldContinuationIndent = continuationIndent;
                    try {
                        if (alignIndent >= 0) {
                            indent = continuationIndent ? alignIndent - continuationIndentSize : alignIndent;
                        }
                        newline();
                    } finally {
                        indent = old;
                    }
                    ret = col;
                    if (OPERATOR.equals(tokens.token().id().primaryCategory())) {
                        col += tokens.token().length();
                        lastBlankLines = -1;
                        lastBlankLinesTokenIndex = -1;
                        tokens.moveNext();
                    }
                    try {
                        if (tree.getKind() == Tree.Kind.BLOCK) {
                            spaces(spacesCntAfterOp);
                            continuationIndent = false;
                        } else if (tree.getKind() != Tree.Kind.NEW_ARRAY
                                || ((NewArrayTree) tree).getType() != null
                                || cs.getOtherBracePlacement() == CodeStyle.BracePlacement.SAME_LINE) {
                            spaces(spacesCntAfterOp);
                        } else {
                            continuationIndent = false;
                        }
                        if (treeIndent >= 0) {
                            indent = treeIndent;
                        }
                        scan(tree, null);
                    } finally {
                        lastIndent = oldLast;
                        continuationIndent = oldContinuationIndent;
                    }
                    break;
                case WRAP_IF_LONG:
                    int index = tokens.index();
                    int c = col;
                    Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                    old = indent;
                    oldLast = lastIndent;
                    oldContinuationIndent = continuationIndent;
                    int o = tokens.offset();
                    WrapAbort oldCheckWrap = checkWrap;
                    checkWrap = new WrapAbort(o);
                    try {
                        try {
                            if (alignIndent >= 0) {
                                indent = continuationIndent ? alignIndent - continuationIndentSize : alignIndent;
                                indent = alignIndent;
                            }
                            spaces(spacesCntBeforeOp, true);
                        } finally {
                            indent = old;
                            lastIndent = oldLast;
                        }
                        ret = col;
                        if (OPERATOR.equals(tokens.token().id().primaryCategory())) {
                            col += tokens.token().length();
                            lastBlankLines = -1;
                            lastBlankLinesTokenIndex = -1;
                            tokens.moveNext();
                        }
                        try {
                            if (tree.getKind() == Tree.Kind.BLOCK) {
                                spaces(spacesCntAfterOp);
                                continuationIndent = false;
                            } else if (tree.getKind() != Tree.Kind.NEW_ARRAY
                                    || ((NewArrayTree) tree).getType() != null
                                    || cs.getOtherBracePlacement() == CodeStyle.BracePlacement.SAME_LINE) {
                                spaces(spacesCntAfterOp);
                            } else {
                                continuationIndent = false;
                            }
                            if (treeIndent >= 0) {
                                indent = treeIndent;
                            }
                            scan(tree, null);
                        } finally {
                            continuationIndent = oldContinuationIndent;
                        }
                    } catch (WrapAbort wa) {
                    } finally {
                        checkWrap = oldCheckWrap;
                    }
                    if (col > rightMargin && o >= lastNewLineOffset) {
                        rollback(index, c, d);
                        try {
                            if (alignIndent >= 0) {
                                indent = continuationIndent ? alignIndent - continuationIndentSize : alignIndent;
                            } else {
                                indent = old;
                                lastIndent = oldLast;
                                continuationIndent = oldContinuationIndent;
                            }
                            newline();
                        } finally {
                            indent = old;
                            lastIndent = oldLast;
                            continuationIndent = oldContinuationIndent;
                        }
                        ret = col;
                        if (OPERATOR.equals(tokens.token().id().primaryCategory())) {
                            col += tokens.token().length();
                            lastBlankLines = -1;
                            lastBlankLinesTokenIndex = -1;
                            tokens.moveNext();
                        }
                        try {
                            if (tree.getKind() == Tree.Kind.BLOCK) {
                                spaces(spacesCntAfterOp);
                                continuationIndent = false;
                            } else if (tree.getKind() != Tree.Kind.NEW_ARRAY
                                    || ((NewArrayTree) tree).getType() != null
                                    || cs.getOtherBracePlacement() == CodeStyle.BracePlacement.SAME_LINE) {
                                spaces(spacesCntAfterOp);
                            } else {
                                continuationIndent = false;
                            }
                            if (treeIndent >= 0) {
                                indent = treeIndent;
                            }
                            scan(tree, null);
                        } finally {
                            continuationIndent = oldContinuationIndent;
                        }
                    }
                    break;
                case WRAP_NEVER:
                    index = tokens.index();
                    c = col;
                    d = diffs.isEmpty() ? null : diffs.getFirst();
                    old = indent;
                    oldLast = lastIndent;
                    oldContinuationIndent = continuationIndent;
                    try {
                        if (alignIndent >= 0) {
                            indent = continuationIndent ? alignIndent - continuationIndentSize : alignIndent;
                        }
                        spaces(spacesCntBeforeOp, true);
                    } finally {
                        indent = old;
                        lastIndent = oldLast;
                    }
                    ret = col;
                    if (OPERATOR.equals(tokens.token().id().primaryCategory())) {
                        col += tokens.token().length();
                        lastBlankLines = -1;
                        lastBlankLinesTokenIndex = -1;
                        tokens.moveNext();
                    }
                    try {
                        if (tree.getKind() == Tree.Kind.BLOCK) {
                            if (cs.getOtherBracePlacement() == CodeStyle.BracePlacement.SAME_LINE) {
                                if (spaces(spacesCntAfterOp, false)) {
                                    rollback(index, c, d);
                                    old = indent;
                                    oldLast = lastIndent;
                                    try {
                                        if (alignIndent >= 0) {
                                            indent = continuationIndent ? alignIndent - continuationIndentSize : alignIndent;
                                        }
                                        newline();
                                    } finally {
                                        indent = old;
                                        lastIndent = oldLast;
                                    }
                                    ret = col;
                                    if (OPERATOR.equals(tokens.token().id().primaryCategory())) {
                                        col += tokens.token().length();
                                        lastBlankLines = -1;
                                        lastBlankLinesTokenIndex = -1;
                                        tokens.moveNext();
                                    }
                                    spaces(spacesCntAfterOp);
                                }
                                continuationIndent = isLastIndentContinuation;
                            }
                        } else if (tree.getKind() != Tree.Kind.NEW_ARRAY
                                || ((NewArrayTree) tree).getType() != null
                                || cs.getOtherBracePlacement() == CodeStyle.BracePlacement.SAME_LINE) {
                            if (spaces(spacesCntAfterOp, false)) {
                                rollback(index, c, d);
                                old = indent;
                                oldLast = lastIndent;
                                try {
                                    if (alignIndent >= 0) {
                                        indent = continuationIndent ? alignIndent - continuationIndentSize : alignIndent;
                                    }
                                    newline();
                                } finally {
                                    indent = old;
                                    lastIndent = oldLast;
                                }
                                ret = col;
                                if (OPERATOR.equals(tokens.token().id().primaryCategory())) {
                                    col += tokens.token().length();
                                    lastBlankLines = -1;
                                    lastBlankLinesTokenIndex = -1;
                                    tokens.moveNext();
                                }
                                spaces(spacesCntAfterOp);
                            }
                        } else {
                            continuationIndent = isLastIndentContinuation;
                        }
                        if (treeIndent >= 0) {
                            indent = treeIndent;
                        }
                        scan(tree, null);
                    } finally {
                        continuationIndent = oldContinuationIndent;
                    }
                    break;
            }
            return ret;
        }

        private boolean wrapStatement(CodeStyle.WrapStyle wrapStyle, CodeStyle.BracesGenerationStyle bracesGenerationStyle, int spacesCnt, StatementTree tree) {
            return wrapStatement(wrapStyle, bracesGenerationStyle, spacesCnt, true, tree);
        }

        private boolean wrapStatement(CodeStyle.WrapStyle wrapStyle, CodeStyle.BracesGenerationStyle bracesGenerationStyle, int spacesCnt, boolean preserveNewLine, StatementTree tree) {
            Tree.Kind kind = tree.getKind();
            if (kind == Tree.Kind.EMPTY_STATEMENT) {
                scan(tree, null);
                return true;
            }
            if (kind == Tree.Kind.BLOCK || kind == Tree.Kind.TRY || kind == Tree.Kind.SYNCHRONIZED ) {
                if (kind == Tree.Kind.BLOCK && bracesGenerationStyle == CodeStyle.BracesGenerationStyle.ELIMINATE) {
                    Iterator<? extends StatementTree> stats = ((BlockTree)tree).getStatements().iterator();
                    if (stats.hasNext()) {
                        StatementTree stat = stats.next();
                        if (!stats.hasNext() && stat.getKind() != Tree.Kind.VARIABLE) {
                            accept(LBRACE);
                            int start = tokens.offset() - 1;
                            Diff d;
                            while (!diffs.isEmpty() && (d = diffs.getFirst()) != null && d.getStartOffset() >= start)
                                diffs.removeFirst();
                            addDiff(new Diff(start, tokens.offset(), null));
                            int old = indent;
                            indent = lastIndent + indentSize;
                            wrapTree(wrapStyle, -1, spacesCnt, preserveNewLine, stat);
                            indent = old;
                            accept(RBRACE);
                            tokens.moveIndex(tokens.index() - 2);
                            tokens.moveNext();
                            if (tokens.token().id() == JavaTokenId.WHITESPACE) {
                                start = tokens.offset();
                                if (tokens.movePrevious()) {
                                    if (tokens.token().id() == JavaTokenId.LINE_COMMENT)
                                        start--;
                                    tokens.moveNext();
                                }
                                tokens.moveNext();
                            } else {
                                tokens.moveNext();
                                start = tokens.offset();
                            }
                            tokens.moveNext();
                            while (!diffs.isEmpty() && (d = diffs.getFirst()) != null && d.getStartOffset() >= start && (d.text == null || d.text.indexOf('}') < 0))
                                diffs.removeFirst();
                            addDiff(new Diff(start, tokens.offset(), null));
                            return false;
                        }
                    }
                }
                scan(tree, null);
                return true;
            }
            if (bracesGenerationStyle == CodeStyle.BracesGenerationStyle.GENERATE) {
                scan(new FakeBlock(tree), null);
                return true;
            }
            int old = indent;
            indent = lastIndent + indentSize;
            wrapTree(wrapStyle, -1, spacesCnt, preserveNewLine, tree);
            indent = old;
            return false;
        }

        private void wrapList(CodeStyle.WrapStyle wrapStyle, boolean align, boolean prependSpace, JavaTokenId separator, List<? extends Tree> trees) {
            wrapList(wrapStyle, align, prependSpace, separator, true, trees);
        }

        private void wrapList(CodeStyle.WrapStyle wrapStyle, boolean align, boolean prependSpace, JavaTokenId separator, boolean wrapAfterSeparator, List<? extends Tree> trees) {
            boolean first = true;
            int alignIndent = -1;
            boolean spaceBeforeSeparator, spaceAfterSeparator;
            switch (separator) {
                case COMMA:
                    spaceBeforeSeparator = cs.spaceBeforeComma();
                    spaceAfterSeparator = cs.spaceAfterComma();
                    break;
                case SEMICOLON:
                    spaceBeforeSeparator = cs.spaceBeforeSemi();
                    spaceAfterSeparator = cs.spaceAfterSemi();
                    break;
                default:
                    spaceBeforeSeparator = spaceAfterSeparator = cs.spaceAroundBinaryOps();
                    break;
            }
            for (Iterator<? extends Tree> it = trees.iterator(); it.hasNext();) {
                Tree impl = it.next();
                if (wrapAnnotation && impl.getKind() == Tree.Kind.ANNOTATION) {
                    if (!first) {
                        boolean containedNewLine = spaces(spaceBeforeSeparator ? 1 : 0, false);
                        if (separator.equals(accept(separator)) && containedNewLine) {
                            newline();
                        }
                    }
                    wrapTree(CodeStyle.WrapStyle.WRAP_ALWAYS, alignIndent, spaceAfterSeparator ? 1 : 0, impl);
                } else if (impl.getKind() == Tree.Kind.ERRONEOUS) {
                    scan(impl, null);
                } else if (first) {
                    int index = tokens.index();
                    int c = col;
                    Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                    spaces(prependSpace ? 1 : 0, true);
                    if (align)
                        alignIndent = col;
                    if (wrapStyle == CodeStyle.WrapStyle.WRAP_NEVER || c <= indent()) {
                        scan(impl, null);
                    } else {
                        int o = tokens.offset();
                        WrapAbort oldCheckWrap = checkWrap;
                        checkWrap = new WrapAbort(o);
                        try {
                            scan(impl, null);
                        } catch (WrapAbort wa) {
                        } finally {
                            checkWrap = oldCheckWrap;
                        }
                        if (col > rightMargin && o >= lastNewLineOffset) {
                            rollback(index, c, d);
                            newline();
                            if (align)
                                alignIndent = col;
                            scan(impl, null);
                        }
                    }
                } else if (wrapAfterSeparator) {
                    boolean containedNewLine = spaces(spaceBeforeSeparator ? 1 : 0, false);
                    if (separator.equals(accept(separator)) && containedNewLine) {
                        newline();
                    }
                    wrapTree(wrapStyle, alignIndent, spaceAfterSeparator ? 1 : 0, impl);
                } else {
                    wrapOperatorAndTree(wrapStyle, alignIndent, spaceAfterSeparator ? 1 : 0, impl);
                }
                first = false;
            }
        }

        private void scanMethodCall(MethodInvocationTree node) {
            List<? extends Tree> targs = node.getTypeArguments();
            if (targs != null && !targs.isEmpty()) {
                if (LT == accept(LT))
                    tpLevel++;
                for (Iterator<? extends Tree> it = targs.iterator(); it.hasNext();) {
                    Tree targ = it.next();
                    scan(targ, null);
                    if (it.hasNext()) {
                        spaces(cs.spaceBeforeComma() ? 1 : 0);
                        accept(COMMA);
                        spaces(cs.spaceAfterComma() ? 1 : 0);
                    }
                }
                JavaTokenId accepted;
                if (tpLevel > 0 && (accepted = accept(GT, GTGT, GTGTGT)) != null) {
                    switch (accepted) {
                        case GTGTGT:
                            tpLevel -= 3;
                            break;
                        case GTGT:
                            tpLevel -= 2;
                            break;
                        case GT:
                            tpLevel--;
                            break;
                    }
                }
            }
            accept(IDENTIFIER, UNDERSCORE, THIS, SUPER);
            spaces(cs.spaceBeforeMethodCallParen() ? 1 : 0);
            accept(LPAREN);
            boolean old = continuationIndent;
            try {
                List<? extends ExpressionTree> args = node.getArguments();
                if (args != null && !args.isEmpty()) {
                    int oldIndent = indent;
                    int oldLastIndent = lastIndent;
                    boolean continuation = isLastIndentContinuation;
                    if (continuation) {
                        indent = indent();
                        isLastIndentContinuation = false;
                    }
                    try {
                        spaces(cs.spaceWithinMethodCallParens() ? 1 : 0, true);
                        wrapList(cs.wrapMethodCallArgs(), cs.alignMultilineCallArgs(), false, COMMA, args);
                    } finally {
                        indent = oldIndent;
                        lastIndent = oldLastIndent;
                        continuationIndent = continuation;
                    }
                    spaces(cs.spaceWithinMethodCallParens() ? 1 : 0, true);
                }
                accept(RPAREN);
            } finally {
                continuationIndent = old;
            }
        }

        private void reformatComment() {
            if (tokens.token().id() != BLOCK_COMMENT && tokens.token().id() != JAVADOC_COMMENT)
                return;
            TokenSequence<JavadocTokenId> javadocTokens = null;
            TokenSequence<?> embedded = tokens.embedded();
            if (embedded != null) {
                if (JavadocTokenId.language().equals(embedded.language())) {
                    javadocTokens = (TokenSequence<JavadocTokenId>) embedded;
                } else {
                    return;
                }
            }
            String text = tokens.token().text().toString();
            int offset = tokens.offset();
            LinkedList<Pair<Integer, Integer>> marks = new LinkedList<Pair<Integer, Integer>>();
            int maxParamNameLength = 0;
            int maxExcNameLength = 0;
            int initTextEndOffset = Integer.MAX_VALUE;
            if (javadocTokens != null) {
                int state = 0; // 0 - initial text, 1 - after param tag, 2 - param description, 3 - return description,
                               // 4 - after throws tag, 5 - exception description, 6 - after pre tag, 7 - after other tag
                int currWSOffset = -1;
                int lastWSOffset = -1;
                int identStart = -1;
                int lastNLOffset = -1;
                int lastAddedNLOffset = -1;
                boolean afterText = false;
                boolean insideTag = false;
                int nestedParenCnt = 0;
                StringBuilder cseq = null;
                Pair<Integer, Integer> toAdd = null;
                Pair<Integer, Integer> nlAdd = null;
                while (javadocTokens.moveNext()) {
                    switch (javadocTokens.token().id()) {
                        case TAG:
                            toAdd = null;
                            nlAdd = null;
                            String tokenText = javadocTokens.token().text().toString();
                            int newState;
                            if (JDOC_PARAM_TAG.equalsIgnoreCase(tokenText)) {
                                newState = 1;
                            } else if (JDOC_RETURN_TAG.equalsIgnoreCase(tokenText)) {
                                newState = 3;
                            } else if (JDOC_THROWS_TAG.equalsIgnoreCase(tokenText)
                                    || JDOC_EXCEPTION_TAG.equalsIgnoreCase(tokenText)) {
                                newState = 4;
                            } else if (JDOC_LINK_TAG.equalsIgnoreCase(tokenText)
                                    || JDOC_LINKPLAIN_TAG.equalsIgnoreCase(tokenText)
                                    || JDOC_CODE_TAG.equalsIgnoreCase(tokenText)
                                    || JDOC_SNIPPET_TAG.equalsIgnoreCase(tokenText)
                                    || JDOC_DOCROOT_TAG.equalsIgnoreCase(tokenText)
                                    || JDOC_INHERITDOC_TAG.equalsIgnoreCase(tokenText)
                                    || JDOC_VALUE_TAG.equalsIgnoreCase(tokenText)
                                    || JDOC_SUMMARY_TAG.equalsIgnoreCase(tokenText)
                                    || JDOC_LITERAL_TAG.equalsIgnoreCase(tokenText)) {
                                insideTag = true;
                                addMark(Pair.of(currWSOffset >= 0 ? currWSOffset : javadocTokens.offset() - offset, 5), marks, state);
                                lastWSOffset = currWSOffset = -1;
                                break;
                            } else {
                                if (insideTag)
                                    break;
                                newState = 7;
                            }
                            if (lastWSOffset < initTextEndOffset && newState > 0) {
                                initTextEndOffset = lastWSOffset;
                            }
                            if (currWSOffset >= 0 && afterText) {
                                addMark(Pair.of(currWSOffset, state == 0 && cs.blankLineAfterJavadocDescription()
                                        || state == 2 && newState != 1 && cs.blankLineAfterJavadocParameterDescriptions()
                                        || state == 3 && cs.blankLineAfterJavadocReturnTag() ? 0 : 1), marks, state);
                            }
                            state = newState;
                            if (state == 3 && cs.alignJavadocReturnDescription()) {
                                toAdd = Pair.of(javadocTokens.offset() + javadocTokens.token().length() - offset, 3);
                            }
                            lastWSOffset = currWSOffset = -1;
                            break;
                        case IDENT:
                            if (toAdd != null) {
                                addMark(toAdd, marks, state);
                                toAdd = null;
                            }
                            nlAdd = null;
                            if (identStart < 0 && (state == 1 || state == 4))
                                identStart = javadocTokens.offset() - offset;
                            lastWSOffset = currWSOffset = -1;
                            afterText = true;
                            break;
                        case HTML_TAG:
                            if (toAdd != null) {
                                addMark(toAdd, marks, state);
                            }
                            nlAdd = null;
                            tokenText = javadocTokens.token().text().toString();
                            if (tokenText.endsWith(">")) { //NOI18N
                                if (P_TAG.equalsIgnoreCase(tokenText) || END_P_TAG.equalsIgnoreCase(tokenText)) {
                                    if (currWSOffset >= 0 && currWSOffset > lastAddedNLOffset && (toAdd == null || toAdd.first() < currWSOffset)) {
                                        addMark(Pair.of(currWSOffset, 1), marks, state);
                                    }
                                    lastAddedNLOffset = javadocTokens.offset() + javadocTokens.token().length() - offset;
                                    addMark(Pair.of(lastAddedNLOffset, 1), marks, state);
                                    afterText = false;
                                } else if (PRE_TAG.equalsIgnoreCase(tokenText)) {
                                    if (currWSOffset >= 0 && state == 0 && (toAdd == null || toAdd.first() < currWSOffset)) {
                                        addMark(Pair.of(currWSOffset, 1), marks, state);
                                    }
                                    addMark(Pair.of(javadocTokens.offset() - offset, 5), marks, state);
                                    state = 6;
                                } else if (CODE_TAG.equalsIgnoreCase(tokenText)) {
                                    addMark(Pair.of(javadocTokens.offset() - offset, 5), marks, state);
                                } else if (PRE_END_TAG.equalsIgnoreCase(tokenText)) {
                                    state = 0;
                                    addMark(Pair.of(currWSOffset >= 0 ? currWSOffset : javadocTokens.offset() - offset, 6), marks, state);
                                } else if (CODE_END_TAG.equalsIgnoreCase(tokenText)) {
                                    addMark(Pair.of(currWSOffset >= 0 ? currWSOffset : javadocTokens.offset() - offset, 6), marks, state);
                                } else {
                                    if (currWSOffset >= 0 && lastNLOffset >= currWSOffset
                                            && lastAddedNLOffset < currWSOffset && (toAdd == null || toAdd.first() < currWSOffset)) {
                                        addMark(Pair.of(currWSOffset, 1), marks, state);
                                    }
                                    addMark(Pair.of(javadocTokens.offset() - offset, 5), marks, state);
                                    addMark(Pair.of(javadocTokens.offset() + javadocTokens.token().length() - offset - 1, 6), marks, state);
                                    nlAdd = Pair.of(javadocTokens.offset() + javadocTokens.token().length() - offset, 1);
                                }
                            } else {
                                cseq = new StringBuilder(tokenText);
                            }
                            toAdd = null;
                            lastWSOffset = currWSOffset = -1;
                            break;
                        case OTHER_TEXT:
                            lastWSOffset = currWSOffset = -1;
                            if (cseq == null)
                                cseq = new StringBuilder();
                            cseq.append(javadocTokens.token().text());
                            int nlNum = 1;
                            int insideTagEndOffset = -1;
                            boolean addNow = false;
                            boolean nlFollows = false;
                            for (int i = cseq.length(); i >= 0; i--) {
                                if (i == 0) {
                                    if (lastWSOffset < 0)
                                        lastWSOffset = javadocTokens.offset() - offset;
                                    if (currWSOffset < 0 && nlNum >= 0)
                                        currWSOffset = javadocTokens.offset() - offset;
                                } else {
                                    char c = cseq.charAt(i - 1);
                                    if (Character.isWhitespace(c)) {
                                        if (c == '\n') {
                                            nlNum--;
                                            nlFollows = true;
                                            int off = javadocTokens.offset() + i - offset;
                                            if (off > lastNLOffset)
                                                lastNLOffset = off;
                                        }
                                        if (lastWSOffset < 0 && currWSOffset >= 0)
                                            lastWSOffset = -2;
                                    } else {
                                        nlFollows = false;
                                        if (c != '*') {
                                            if (toAdd != null) {
                                                addMark(toAdd, marks, state);
                                                toAdd = null;
                                            } else {
                                                addNow = true;
                                            }
                                            if (insideTag) {
                                                if (c == '{') {
                                                    nestedParenCnt++;
                                                } else if (c == '}') {
                                                    if (nestedParenCnt > 0) {
                                                        nestedParenCnt--;
                                                    } else {
                                                        insideTagEndOffset = javadocTokens.offset() + i - offset - 1;
                                                        insideTag = false;
                                                    }
                                                }
                                            }
                                            if (lastWSOffset == -2)
                                                lastWSOffset = javadocTokens.offset() + i - offset;
                                            if (currWSOffset < 0 && nlNum >= 0)
                                                currWSOffset = javadocTokens.offset() + i - offset;
                                            afterText = true;
                                        }
                                    }
                                }
                            }
                            if (nlFollows && nlAdd != null) {
                                toAdd = nlAdd;
                            }
                            nlAdd = null;
                            if (identStart >= 0) {
                                int len = javadocTokens.offset() - offset - identStart;
                                for (int i = 0; i <= cseq.length(); i++) {
                                    if (i == cseq.length() || Character.isWhitespace(cseq.charAt(i))) {
                                        len += i;
                                        break;
                                    }
                                }
                                if (state == 1) {
                                    if (len > maxParamNameLength)
                                        maxParamNameLength = len;
                                    if (cs.alignJavadocParameterDescriptions())
                                        toAdd = Pair.of(identStart + len, 2);
                                    state = 2;
                                } else if (state == 4) {
                                    if (len > maxExcNameLength)
                                        maxExcNameLength = len;
                                    if (cs.alignJavadocExceptionDescriptions())
                                        toAdd = Pair.of(identStart + len, 4);
                                    state = 5;
                                }
                                if (addNow && toAdd != null) {
                                    addMark(toAdd, marks, state);
                                    toAdd = null;
                                }
                                identStart = -1;
                            }
                            if (insideTagEndOffset >= 0) {
                                addMark(Pair.of(insideTagEndOffset, 6), marks, state);
                            }
                            cseq = null;
                            break;
                        default:
                            if (toAdd != null) {
                                addMark(toAdd, marks, state);
                                toAdd = null;
                            }
                            nlAdd = null;
                    }
                }
            }
            int checkOffset, actionType; // 0 - add blank line, 1 - add newline, 2 - align params, 3 - align return,
                                         // 4 - align exceptions, 5 - no format, 6 - format
            Iterator<Pair<Integer, Integer>> it = marks.iterator();
            if (it.hasNext()) {
                Pair<Integer, Integer> next = it.next();
                checkOffset = next.first();
                actionType = next.second();
            } else {
                checkOffset = Integer.MAX_VALUE;
                actionType = -1;
            }
            String indentString = getIndent();
            String lineStartString = cs.addLeadingStarInComment() ? indentString + SPACE + LEADING_STAR + SPACE : indentString + SPACE;
            String blankLineString;
            int currNWSPos = -1;
            int lastNWSPos = -1;
            int currWSPos = -1;
            int lastWSPos = -1;
            int lastNewLinePos = -1;
            Diff pendingDiff = null;
            int start = javadocTokens != null ? 3 : 2;
            int end = text.length() - 2;
            col += start;
            boolean preserveNewLines = true;
            boolean firstLine = true;
            boolean enableCommentFormatting = javadocTokens != null ? cs.enableJavadocFormatting() : cs.enableBlockCommentFormatting();
            boolean noFormat = bof;
            int align = -1;
            for (int i = start; i < text.length(); i++) {
                if (i >= initTextEndOffset) {
                    blankLineString = cs.addLeadingStarInComment() ? indentString + SPACE + LEADING_STAR : EMPTY;
                } else if (javadocTokens != null && !noFormat && cs.generateParagraphTagOnBlankLines()) {
                    blankLineString = cs.addLeadingStarInComment() ? indentString + SPACE + LEADING_STAR + SPACE + P_TAG : indentString + SPACE + P_TAG;
                } else {
                    blankLineString = cs.addLeadingStarInComment() ? indentString + SPACE + LEADING_STAR : EMPTY;
                }
                char c = text.charAt(i);
                if (Character.isWhitespace(c)) {
                    if (enableCommentFormatting) {
                        if (currNWSPos >= 0) {
                            lastNWSPos = currNWSPos;
                            currNWSPos = -1;
                        }
                        if (currWSPos < 0) {
                            currWSPos = i;
                            if (noFormat) {
                                col++;
                            } else {
                                if (col > rightMargin && cs.wrapCommentText() && lastWSPos >= 0) {
                                    int endOff = pendingDiff != null ? pendingDiff.getEndOffset() - offset : lastWSPos + 1;
                                    String s = pendingDiff != null && pendingDiff.text != null && pendingDiff.text.charAt(0) == '\n' ? pendingDiff.text : NEWLINE + lineStartString;
                                    col = getCol(lineStartString) + i - endOff;
                                    if (align > 0) {
                                        int num = align - getCol(lineStartString);
                                        if (num > 0) {
                                            s += getSpaces(num);
                                            col += num;
                                        }
                                    }
                                    col++;
                                    if (endOff > lastWSPos && !s.equals(text.substring(lastWSPos, endOff)))
                                        addDiff(new Diff(offset + lastWSPos, offset + endOff, s));
                                } else if (pendingDiff != null) {
                                    String sub = text.substring(pendingDiff.start - offset, pendingDiff.end - offset);
                                    if (!sub.equals(pendingDiff.text)) {
                                        addDiff(pendingDiff);
                                    }
                                    col++;
                                } else {
                                    col++;
                                }
                                pendingDiff = null;
                            }
                        }
                    }
                    if (c == '\n') {
                        if (lastNewLinePos >= 0) {
                            if (enableCommentFormatting) {
                                String subs = text.substring(lastNewLinePos + 1, i);
                                if (!blankLineString.equals(subs)) {
                                    addDiff(new Diff(offset + lastNewLinePos + 1, offset + i, blankLineString));
                                }
                            }
                            preserveNewLines = true;
                            lastNewLinePos = i;
                            align = -1;
                        } else {
                            lastNewLinePos = currWSPos >= 0 ? currWSPos : i;
                        }
                        firstLine = false;
                    }
                    if (i >= checkOffset && actionType == 5) {
                        noFormat = true;
                        align = -1;
                        if (it.hasNext()) {
                            Pair<Integer, Integer> next = it.next();
                            checkOffset = next.first();
                            actionType = next.second();
                        } else {
                            checkOffset = Integer.MAX_VALUE;
                            actionType = -1;
                        }
                    }
                } else {
                    if (pendingDiff != null) {
                        String sub = text.substring(pendingDiff.start - offset, pendingDiff.end - offset);
                        if (sub.equals(pendingDiff.text)) {
                            pendingDiff = null;
                        }
                    }
                    if (enableCommentFormatting) {
                        if (currNWSPos < 0) {
                            currNWSPos = i;
                        }
                        if (i >= checkOffset) {
                            noFormat = false;
                            switch (actionType) {
                                case 0:
                                    pendingDiff = new Diff(currWSPos >= 0 ? offset + currWSPos : offset + i, offset + i, NEWLINE + blankLineString + NEWLINE);
                                    lastNewLinePos = i - 1;
                                    preserveNewLines = true;
                                    align = -1;
                                    break;
                                case 1:
                                    pendingDiff = new Diff(currWSPos >= 0 ? offset + currWSPos : offset + i, offset + i, NEWLINE);
                                    lastNewLinePos = i - 1;
                                    preserveNewLines = true;
                                    align = -1;
                                    break;
                                case 2:
                                    col += (maxParamNameLength + lastNWSPos- currWSPos);
                                    align = col;
                                    currWSPos = -1;
                                    if (lastNewLinePos < 0) {
                                        int num = maxParamNameLength + lastNWSPos + 1 - i;
                                        if (num > 0) {
                                            addDiff(new Diff(offset + i, offset + i, getSpaces(num)));
                                        } else if (num < 0) {
                                            addDiff(new Diff(offset + i + num, offset + i, null));
                                        }
                                    }
                                    break;
                                case 3:
                                    align = col;
                                    break;
                                case 4:
                                    col += (maxExcNameLength + lastNWSPos- currWSPos);
                                    align = col;
                                    currWSPos = -1;
                                    if (lastNewLinePos < 0) {
                                        int num = maxExcNameLength + lastNWSPos + 1 - i;
                                        if (num > 0) {
                                            addDiff(new Diff(offset + i, offset + i, getSpaces(num)));
                                        } else if (num < 0) {
                                            addDiff(new Diff(offset + i + num, offset + i, null));
                                        }
                                    }
                                    break;
                                case 5:
                                    noFormat = true;
                                    if (currWSPos > 0)
                                        lastWSPos = currWSPos;
                                    break;
                                case 6:
                                    preserveNewLines = true;
                                    break;
                            }
                            if (it.hasNext()) {
                                Pair<Integer, Integer> next = it.next();
                                checkOffset = next.first();
                                actionType = next.second();
                            } else {
                                checkOffset = Integer.MAX_VALUE;
                                actionType = -1;
                            }
                        }
                    }
                    if (lastNewLinePos >= 0) {
                        if (!preserveNewLines && !noFormat && i < text.length() - 2 && enableCommentFormatting && !cs.preserveNewLinesInComments() && cs.wrapCommentText()) {
                            lastWSPos = lastNewLinePos;
                            if (pendingDiff != null) {
                                pendingDiff.text += SPACE;
                            } else {
                                pendingDiff = new Diff(offset + lastNewLinePos, offset + i, SPACE);
                            }
                            lastNewLinePos = -1;
                            if (c == '*') {
                                int diff = 0;
                                while(++i < text.length()) {
                                    col++;
                                    c = text.charAt(i);
                                    if (c == '\n') {
                                        pendingDiff.text = NEWLINE + blankLineString + NEWLINE;
                                        diff++;
                                        preserveNewLines = true;
                                        lastNewLinePos = i;
                                        align = -1;
                                        break;
                                    } else if (!Character.isWhitespace(c)) {
                                        break;
                                    }
                                }
                                if (pendingDiff != null) {
                                    diff += offset + i - pendingDiff.end;
                                    pendingDiff.end += diff;
                                    col -= diff;
                                }
                            }
                        } else {
                            if (pendingDiff != null) {
                                pendingDiff.text += indentString + SPACE;
                                String subs = text.substring(pendingDiff.start - offset, i);
                                if (pendingDiff.text.equals(subs)) {
                                    lastNewLinePos = pendingDiff.start - offset;
                                    pendingDiff = null;
                                }
                            } else {
                                // do format last line with only whitespaces and end-comment marker, even though
                                // comment formatting is disabled. Do properly indent javadoc, if the line nonblank text
                                // starts with * (as javadoc should)
                                if (enableCommentFormatting || i == end || (javadocTokens != null && c == '*')) {
                                    String s = NEWLINE + indentString + SPACE;
                                    String subs = text.substring(lastNewLinePos, i);
                                    if (!s.equals(subs))
                                        pendingDiff = new Diff(offset + lastNewLinePos, offset + i, s);
                                }
                            }
                            lastWSPos = currWSPos = -1;
                            col = getCol(indentString + SPACE);
                            if (enableCommentFormatting) {
                                if (c == '*') {
                                    col++;
                                    while (++i < text.length()) {
                                        c = text.charAt(i);
                                        if (c == '\n') {
                                            if (!cs.addLeadingStarInComment()) {
                                                String subs = text.substring(lastNewLinePos + 1, i);
                                                if (blankLineString.equals(subs)) {
                                                    pendingDiff = null;
                                                } else {
                                                    if (pendingDiff != null) {
                                                        pendingDiff.end = offset + i;
                                                        pendingDiff.text = NEWLINE + blankLineString;
                                                    } else {
                                                        pendingDiff = new Diff(offset + lastNewLinePos + 1, offset + i, blankLineString);
                                                    }
                                                }
                                            } else if (currWSPos >= 0) {
                                                if (pendingDiff != null) {
                                                    String sub = text.substring(pendingDiff.start - offset, pendingDiff.end - offset);
                                                    if (!sub.equals(pendingDiff.text)) {
                                                        addDiff(pendingDiff);
                                                    }
                                                }
                                                pendingDiff = new Diff(offset + currWSPos, offset + i, javadocTokens != null && lastNWSPos >= 0 && i < initTextEndOffset && !noFormat && cs.generateParagraphTagOnBlankLines() ? SPACE + P_TAG : EMPTY);
                                            } else if (javadocTokens != null && lastNWSPos >= 0 && i < initTextEndOffset && !noFormat && cs.generateParagraphTagOnBlankLines()) {
                                                if (pendingDiff != null) {
                                                    String sub = text.substring(pendingDiff.start - offset, pendingDiff.end - offset);
                                                    if (!sub.equals(pendingDiff.text)) {
                                                        addDiff(pendingDiff);
                                                    }
                                                }
                                                pendingDiff = new Diff(offset + i, offset + i, SPACE + P_TAG);
                                            }
                                            currWSPos = -1;
                                            lastNewLinePos = i;
                                            align = -1;
                                            break;
                                        } else if (Character.isWhitespace(c)) {
                                            if (currWSPos < 0) {
                                                currWSPos = i;
                                                col++;
                                            }
                                        } else if (c == '*' || c == '/') {
                                            col++;
                                            lastNewLinePos = -1;
                                            break;
                                        } else {
                                            if (i >= checkOffset && actionType == 6) {
                                                noFormat = false;
                                                preserveNewLines = true;
                                                if (it.hasNext()) {
                                                    Pair<Integer, Integer> next = it.next();
                                                    checkOffset = next.first();
                                                    actionType = next.second();
                                                } else {
                                                    checkOffset = Integer.MAX_VALUE;
                                                    actionType = -1;
                                                }
                                            }
                                            if (!cs.addLeadingStarInComment()) {
                                                if (noFormat) {
                                                    if (pendingDiff != null) {
                                                        pendingDiff.end = currWSPos >= 0 ? offset + currWSPos + 1 : pendingDiff.end + 1;
                                                    } else {
                                                        pendingDiff = new Diff(offset + lastNewLinePos + 1, currWSPos >= 0 ? offset + currWSPos + 1 : offset + i, indentString + SPACE);
                                                    }

                                                } else {
                                                    if (pendingDiff != null) {
                                                        pendingDiff.end = offset + i;
                                                    } else {
                                                        pendingDiff = new Diff(offset + lastNewLinePos + 1, offset + i, indentString + SPACE);
                                                    }
                                                    col = getCol(indentString + SPACE);
                                                }
                                            } else {
                                                if (currWSPos < 0) {
                                                    currWSPos = i;
                                                    col++;
                                                }
                                                String subs = text.substring(currWSPos, i);
                                                String s = getSpaces(align < 0 ? 1 : align - getCol(lineStartString) + 1);
                                                if (!noFormat && !s.equals(subs)) {
                                                    if (pendingDiff != null) {
                                                        String sub = text.substring(pendingDiff.start - offset, pendingDiff.end - offset);
                                                        if (!sub.equals(pendingDiff.text)) {
                                                            addDiff(pendingDiff);
                                                        }
                                                    }
                                                    pendingDiff = new Diff(offset + currWSPos, offset + i, s);
                                                }
                                            }
                                            lastNewLinePos = -1;
                                            currWSPos = -1;
                                            break;
                                        }
                                    }
                                } else {
                                    if (cs.addLeadingStarInComment()) {
                                        int num = Math.max(align - col - 1, 1);
                                        String s = getSpaces(num);
                                        if (pendingDiff != null) {
                                            pendingDiff.text += (LEADING_STAR + s);
                                        } else {
                                            pendingDiff = new Diff(offset + i, offset + i, LEADING_STAR + s);
                                        }
                                        col += (num + 1);
                                    } else if (align > col) {
                                        int num = align - col;
                                        String s = getSpaces(num);
                                        if (pendingDiff != null) {
                                            pendingDiff.text += s;
                                        } else {
                                            pendingDiff = new Diff(offset + i, offset + i, s);
                                        }
                                        col += num;
                                    }
                                    lastNewLinePos = -1;
                                }
                            } else {
                                lastNewLinePos = -1;
                            }
                            if (pendingDiff != null) {
                                String sub = text.substring(pendingDiff.start - offset, pendingDiff.end - offset);
                                if (!sub.equals(pendingDiff.text)) {
                                    addDiff(pendingDiff);
                                }
                                pendingDiff = null;
                            }
                        }
                    } else if (enableCommentFormatting) {
                        if (firstLine) {
                            String s = !noFormat && cs.wrapOneLineComments() ? NEWLINE + lineStartString : SPACE;
                            String sub = currWSPos >= 0 ? text.substring(currWSPos, i) : null;
                            if (!s.equals(sub))
                                addDiff(new Diff(currWSPos >= 0 ? offset + currWSPos : offset + i, offset + i, s));
                            if (!noFormat && cs.wrapOneLineComments())
                                col = getCol(lineStartString);
                            firstLine = false;
                        } else if (currWSPos >= 0) {
                            if (!noFormat) {
                                lastWSPos = currWSPos;
                                if (currWSPos < i - 1)
                                    pendingDiff = new Diff(offset + currWSPos + 1, offset + i, null);
                            }
                        } else if (c != '*') {
                            preserveNewLines = false;
                        }
                    } else if (c != '*') {
                        preserveNewLines = false;
                    }
                    currWSPos = -1;
                    col++;
                }
            }
            if (enableCommentFormatting) {
                for (int i = text.length() - 3; i >= 0; i--) {
                    char c = text.charAt(i);
                    if (c == '\n') {
                        break;
                    } else if (!Character.isWhitespace(c)) {
                        String s = !noFormat && cs.wrapOneLineComments() ? NEWLINE + indentString + SPACE : SPACE;
                        String sub = text.substring(i + 1, text.length() - 2);
                        if (!s.equals(sub))
                            addDiff(new Diff(offset + i + 1, offset + text.length() - 2, s));
                        break;
                    }
                }
            }
        }

        private void addMark(Pair<Integer, Integer> mark, List<Pair<Integer, Integer>> marks, int state) {
            if (state != 6) {
                marks.add(mark);
            }
        }

        private int indent() {
            return continuationIndent ? indent + continuationIndentSize : indent;
        }

        private String getSpaces(int count) {
            if (count <= 0)
                return EMPTY;
            if (count == 1)
                return SPACE;
            StringBuilder sb = new StringBuilder();
            while (count-- > 0)
                sb.append(' '); //NOI18N
            return sb.toString();
        }

        private String getNewlines(int count) {
            if (count <= 0)
                return EMPTY;
            if (count == 1)
                return NEWLINE;
            StringBuilder sb = new StringBuilder();
            while (count-- > 0)
                sb.append('\n'); //NOI18N
            return sb.toString();
        }

        private String getIndent() {
            StringBuilder sb = new StringBuilder();
            int col = 0;
            if (!expandTabToSpaces) {
                while (col + tabSize <= indent()) {
                    sb.append('\t'); //NOI18N
                    col += tabSize;
                }
            }
            while (col < indent()) {
                sb.append(SPACE); //NOI18N
                col++;
            }
            lastIndent = indent;
            isLastIndentContinuation = continuationIndent;
            return sb.toString();
        }

        private int getIndentLevel(TokenSequence<JavaTokenId> tokens, TreePath path) {
            if (path.getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT)
                return 0;
            Tree lastTree = null;
            int indent = -1;
            while (path != null) {
                int offset = (int)sp.getStartPosition(path.getCompilationUnit(), path.getLeaf());
                if (offset < 0)
                    return indent;
                if (offset == 0) {
                    return 0;
                }
                tokens.move(offset);
                String text = null;
                while (tokens.movePrevious()) {
                    Token<JavaTokenId> token = tokens.token();
                    if (token.id() == WHITESPACE) {
                        text = token.text().toString();
                        int idx = text.lastIndexOf('\n');
                        if (idx >= 0) {
                            text = text.substring(idx + 1);
                            indent = getCol(text);
                            break;
                        }
                    } else if (token.id() == LINE_COMMENT) {
                        indent = text != null ? getCol(text) : 0;
                        break;
                    } else if (token.id() == BLOCK_COMMENT || token.id() == JAVADOC_COMMENT) {
                        text = null;
                    } else {
                        break;
                    }
                }
                if (indent >= 0)
                    break;
                lastTree = path.getLeaf();
                path = path.getParentPath();
            }
            if (lastTree != null && path != null) {
                switch (path.getLeaf().getKind()) {
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                case RECORD:
                    for (Tree tree : ((ClassTree)path.getLeaf()).getMembers()) {
                        if (tree == lastTree) {
                            indent += tabSize;
                            break;
                        }
                    }
                    break;
                case BLOCK:
                    for (Tree tree : ((BlockTree)path.getLeaf()).getStatements()) {
                        if (tree == lastTree) {
                            indent += tabSize;
                            break;
                        }
                    }
                    break;
                }
            }
            return indent;
        }

        private int getCol(String text) {
            int col = 0;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    col = 0;
                } else if (c == '\t') {
                    col += tabSize;
                    col -= (col % tabSize);
                } else {
                    col++;
                }
            }
            return col;
        }

        private boolean insideBlock(TreePath path) {
            while (path != null) {
                if (Tree.Kind.BLOCK == path.getLeaf().getKind())
                    return true;
                if (Tree.Kind.CLASS == path.getLeaf().getKind())
                    return false;
                path = path.getParentPath();
            }
            return false;
        }

        private boolean isEnumerator(VariableTree tree) {
            return (((JCModifiers)tree.getModifiers()).flags & Flags.ENUM) != 0;
        }

        private boolean isSynthetic(CompilationUnitTree cut, Tree leaf) {
            JCTree tree = (JCTree) leaf;
            if (tree.pos == (-1))
                return true;
            if (leaf.getKind() == Tree.Kind.METHOD) {
                //check for synthetic constructor:
                return (((JCMethodDecl)leaf).mods.flags & Flags.GENERATEDCONSTR) != 0L;
            }
            //check for synthetic superconstructor call:
            if (leaf.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                ExpressionStatementTree est = (ExpressionStatementTree) leaf;
                if (est.getExpression().getKind() == Tree.Kind.METHOD_INVOCATION) {
                    MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                    if (mit.getMethodSelect().getKind() == Tree.Kind.IDENTIFIER) {
                        IdentifierTree it = (IdentifierTree) mit.getMethodSelect();
                        if ("super".equals(it.getName().toString())) {
                            return sp.getEndPosition(cut, leaf) == (-1);
                        }
                    }
                }
            }
            return false;
        }

        private static class WrapAbort extends Error {

            private int pos;

            public WrapAbort(int pos) {
                this.pos = pos;
            }

            @Override
            public synchronized Throwable fillInStackTrace() {
                return null;
            }
        }

        private static class FakeBlock extends JCBlock {

            private StatementTree stat;

            private FakeBlock(StatementTree stat) {
                super(0L, com.sun.tools.javac.util.List.of((JCStatement)stat));
                this.stat = stat;
            }
        }

        private static class DanglingElseChecker extends SimpleTreeVisitor<Void, Void> {

            private boolean foundDanglingElse;

            public boolean hasDanglingElse(Tree t) {
                if (t == null)
                    return false;
                foundDanglingElse = false;
                visit(t, null);
                return foundDanglingElse;
            }

            @Override
            public Void visitBlock(BlockTree node, Void p) {
                // Do dangling else checks on single statement blocks since
                // they often get eliminated and replaced by their constained statement
                Iterator<? extends StatementTree> it = node.getStatements().iterator();
                StatementTree stat = it.hasNext() ? it.next() : null;
                if (stat != null && !it.hasNext())
                    visit(stat, p);
                return null;
            }

            @Override
            public Void visitDoWhileLoop(DoWhileLoopTree node, Void p) {
                return visit(node.getStatement(), p);
            }

            @Override
            public Void visitEnhancedForLoop(EnhancedForLoopTree node, Void p) {
                return visit(node.getStatement(), p);
            }

            @Override
            public Void visitForLoop(ForLoopTree node, Void p) {
                return visit(node.getStatement(), p);
            }

            @Override
            public Void visitIf(IfTree node, Void p) {
                if (node.getElseStatement() == null)
                    foundDanglingElse = true;
                else
                    visit(node.getElseStatement(), p);
                return null;
            }

            @Override
            public Void visitLabeledStatement(LabeledStatementTree node, Void p) {
                return visit(node.getStatement(), p);
            }

            @Override
            public Void visitSynchronized(SynchronizedTree node, Void p) {
                return visit(node.getBlock(), p);
            }

            @Override
            public Void visitWhileLoop(WhileLoopTree node, Void p) {
                return visit(node.getStatement(), p);
            }
        }
    }

    private static class Diff {
        private int start;
        private int end;
        private String text;

        private Diff(int start, int end, String text) {
            this.start = start;
            this.end = end;
            this.text = text;
        }

        public int getStartOffset() {
            return start;
        }

        public int getEndOffset() {
            return end;
        }

        public String getText() {
            return text;
        }

        @Override
        public String toString() {
            return "Diff<" + start + "," + end + ">:" + text; //NOI18N
        }
    }
}