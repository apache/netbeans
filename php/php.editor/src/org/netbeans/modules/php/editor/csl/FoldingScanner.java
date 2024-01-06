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
package org.netbeans.modules.php.editor.csl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.editor.fold.FoldTemplate;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.GroupUseScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.UseScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTError;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Attribute;
import org.netbeans.modules.php.editor.parser.astnodes.CatchClause;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.EmptyStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FinallyClause;
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.MatchExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.netbeans.modules.php.editor.parser.astnodes.SwitchCase;
import org.netbeans.modules.php.editor.parser.astnodes.SwitchStatement;
import org.netbeans.modules.php.editor.parser.astnodes.TryStatement;
import org.netbeans.modules.php.editor.parser.astnodes.UseTraitStatement;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class FoldingScanner {

    public static final FoldType TYPE_CODE_BLOCKS = FoldType.CODE_BLOCK;

    /**
     * FoldType for the PHP class (either nested, or top-level).
     */
    @NbBundle.Messages("FT_Classes=Classes")
    public static final FoldType TYPE_CLASS = FoldType.NESTED.derive(
            "class",
            Bundle.FT_Classes(), FoldTemplate.DEFAULT_BLOCK);

    /**
     * PHP documentation comments.
     */
    @NbBundle.Messages("FT_PHPDoc=PHPDoc documentation")
    public static final FoldType TYPE_PHPDOC = FoldType.DOCUMENTATION.override(
            Bundle.FT_PHPDoc(),        // NOI18N
            new FoldTemplate(3, 2, "/**...*/")); // NOI18N

    public static final FoldType TYPE_COMMENT = FoldType.COMMENT.override(
            FoldType.COMMENT.getLabel(),
            new FoldTemplate(2, 2, "/*...*/")); // NOI18N

    /**
     *
     */
    @NbBundle.Messages("FT_Functions=Functions and methods")
    public static final FoldType TYPE_FUNCTION = FoldType.MEMBER.derive("function",
            Bundle.FT_Functions(),
            FoldTemplate.DEFAULT_BLOCK);

    @NbBundle.Messages("FT_Arrays=Arrays")
    public static final FoldType TYPE_ARRAY = FoldType.NESTED.derive(
            "array",
            Bundle.FT_Arrays(), new FoldTemplate(0, 0, "[...]")); // NOI18N

    @NbBundle.Messages("FT_Use=Use statements")
    public static final FoldType TYPE_USE = FoldType.IMPORT.derive(
            "use", // NOI18N
            Bundle.FT_Use(),
            new FoldTemplate(0, 0, "...") // NOI18N
    );

    /**
     * PHP tags (&lt;?php...?&gt; blocks).
     *
     * <b>NOTE:</b> &lt;?=...?&gt; blocks are not folded.
     */
    @NbBundle.Messages("FT_PHPTag=<?php ?> blocks")
    public static final FoldType TYPE_PHPTAG = FoldType.CODE_BLOCK.derive(
            "phptag", // NOI18N
            Bundle.FT_PHPTag(),
            new FoldTemplate(0, 0, "...") // NOI18N
    );

    @NbBundle.Messages("FT_Attributes=Attributes")
    public static final FoldType TYPE_ATTRIBUTES = FoldType.MEMBER.derive(
            "attribute", // NOI18N
            Bundle.FT_Attributes(),
            new FoldTemplate(0, 0, "#[...]") // NOI18N
    );

    private static final String LAST_CORRECT_FOLDING_PROPERTY = "LAST_CORRECT_FOLDING_PROPERY"; //NOI18N
    private static final boolean FOLD_PHPTAG = !Boolean.getBoolean("nb.php.editor.doNotFoldPhptag"); // NOI18N NETBEANS-5480

    public static FoldingScanner create() {
        return new FoldingScanner();
    }

    private FoldingScanner() {
    }

    public Map<String, List<OffsetRange>> folds(ParserResult info) {
        final Map<String, List<OffsetRange>> folds = new HashMap<>();
        Program program = Utils.getRoot(info);
        if (program != null) {
            assert info instanceof PHPParseResult;
            PHPParseResult phpParseResult = (PHPParseResult) info;
            if (program.getStatements().size() == 1) {
                // check whether the ast is broken.
                if (program.getStatements().get(0) instanceof ASTError) {
                    final Document document = phpParseResult.getSnapshot().getSource().getDocument(false);
                    @SuppressWarnings("unchecked") //NOI18N
                    Map<String, List<OffsetRange>> lastCorrect = document != null
                            ? ((Map<String, List<OffsetRange>>) document.getProperty(LAST_CORRECT_FOLDING_PROPERTY))
                            : null;
                    if (lastCorrect != null) {
                        Map<String, List<OffsetRange>> modifiedFolds = filterErrorFold(lastCorrect, phpParseResult.getErrorRange());
                        setFoldingProperty(document, modifiedFolds);
                        return modifiedFolds;
                    } else {
                        return Collections.emptyMap();
                    }
                }
            }
            processComments(folds, program.getComments());
            final Model model = phpParseResult.getModel(Model.Type.COMMON);
            FileScope fileScope = model.getFileScope();
            processScopes(folds, getEmbededScopes(fileScope, null));
            program.accept(new FoldingVisitor(folds));
            Source source = phpParseResult.getSnapshot().getSource();
            assert source != null : "source was null";
            Document doc = source.getDocument(true);
            if (FOLD_PHPTAG) {
                processPHPTags(folds, doc);
            }
            setFoldingProperty(doc, folds);
            return folds;
        }
        return Collections.emptyMap();
    }

    private static Map<String, List<OffsetRange>> filterErrorFold(Map<String, List<OffsetRange>> lastCorrect, OffsetRange errorRange) {
        Map<String, List<OffsetRange>> result = new HashMap<>();
        for (Map.Entry<String, List<OffsetRange>> entry : lastCorrect.entrySet()) {
            ArrayList<OffsetRange> modifiedRanges = new ArrayList<>();
            result.put(entry.getKey(), modifiedRanges);
            for (OffsetRange foldRange : entry.getValue()) {
                if (!foldRange.overlaps(errorRange)) {
                    modifiedRanges.add(foldRange);
                }
            }
        }
        return result;
    }

    private static void setFoldingProperty(Document document, Map<String, List<OffsetRange>> folds) {
        if (document != null) {
            document.putProperty(LAST_CORRECT_FOLDING_PROPERTY, folds);
        }
    }

    private void processComments(Map<String, List<OffsetRange>> folds, List<Comment> comments) {
        for (Comment comment : comments) {
            if (comment.getCommentType() == Comment.Type.TYPE_PHPDOC) {
                OffsetRange offsetRange = createOffsetRange(comment, -3);
                if (offsetRange != null && offsetRange.getLength() > 1) {
                    getRanges(folds, TYPE_PHPDOC).add(offsetRange);
                }
            } else {
                if (comment.getCommentType() == Comment.Type.TYPE_MULTILINE) {
                    OffsetRange offsetRange = createOffsetRange(comment);
                    if (offsetRange != null && offsetRange.getLength() > 1) {
                        getRanges(folds, TYPE_COMMENT).add(offsetRange);
                    }
                }
            }
        }
    }

    private void processPHPTags(Map<String, List<OffsetRange>> folds, Document document) {
        if (document instanceof BaseDocument) {
            BaseDocument doc = (BaseDocument) document;
            doc.readLock();
            try {
                TokenSequence<PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, 0);
                if (ts == null) {
                    return;
                }
                ts.move(0);
                int startOffset = -1;
                int endOffset = -1;
                int shortTagBalance = 0; // for <?= ... ?>
                while (ts.moveNext()) {
                    Token<PHPTokenId> token = ts.token();
                    if (token != null) {
                        PHPTokenId id = token.id();
                        switch (id) {
                            case PHP_OPENTAG:
                                startOffset = ts.offset() + token.length();
                                break;
                            case PHP_CLOSETAG:
                                if (shortTagBalance == 0) {
                                    assert startOffset != -1;
                                    endOffset = ts.offset();
                                    getRanges(folds, TYPE_PHPTAG).add(new OffsetRange(startOffset, endOffset));
                                } else {
                                    shortTagBalance--;
                                }
                                break;
                            case T_OPEN_TAG_WITH_ECHO:
                                shortTagBalance++;
                                break;
                            default:
                                break;
                        }
                    }
                }
            } finally {
                doc.readUnlock();
            }
        }
    }

    private void processScopes(Map<String, List<OffsetRange>> folds, List<Scope> scopes) {
        processUseScopes(folds, scopes);
        processTypeAndFunctionScopes(folds, scopes);
    }

    private void processTypeAndFunctionScopes(Map<String, List<OffsetRange>> folds, List<Scope> scopes) {
        for (Scope scope : scopes) {
            OffsetRange offsetRange = scope.getBlockRange();
            if (offsetRange == null || offsetRange.getLength() <= 1) {
                continue;
            }
            if (scope instanceof TypeScope) {
                getRanges(folds, TYPE_CLASS).add(offsetRange);
            } else {
                if (scope instanceof FunctionScope || scope instanceof MethodScope) {
                    getRanges(folds, TYPE_FUNCTION).add(offsetRange);
                }
            }
        }
    }

    private void processUseScopes(Map<String, List<OffsetRange>> folds, List<Scope> scopes) {
        List<Scope> allScopes = new ArrayList<>(scopes);
        allScopes.sort((o1, o2) -> Integer.compare(o1.getOffset(), o2.getOffset()));
        int startOffset = -1;
        OffsetRange lastOffsetRange = OffsetRange.NONE;
        for (Scope scope : allScopes) {
            boolean isPartOfGroupUse = false;
            if (scope instanceof UseScope) {
                UseScope useScope = (UseScope) scope;
                isPartOfGroupUse = useScope.isPartOfGroupUse();
            }
            if (scope instanceof UseScope || scope instanceof GroupUseScope) {
                if (!isPartOfGroupUse) {
                    lastOffsetRange = scope.getNameRange();
                    if (startOffset == -1) {
                        startOffset = lastOffsetRange.getStart();
                    }
                }
            } else {
                // +1 : ";"
                // XXX ";" may not be the next char
                addUseScope(startOffset, lastOffsetRange.getEnd() + 1, folds);
                startOffset = -1;
            }
        }
        addUseScope(startOffset, lastOffsetRange.getEnd() + 1, folds);
    }

    private void addUseScope(int startOffset, int endOffset, Map<String, List<OffsetRange>> folds) {
        if (startOffset != -1) {
            getRanges(folds, TYPE_USE).add(new OffsetRange(startOffset, endOffset));
        }
    }

    private OffsetRange createOffsetRange(ASTNode node, int startShift) {
        return new OffsetRange(node.getStartOffset() + startShift, node.getEndOffset());
    }

    private OffsetRange createOffsetRange(ASTNode node) {
        return createOffsetRange(node, 0);
    }

    private List<OffsetRange> getRanges(Map<String, List<OffsetRange>> folds, FoldType kind) {
        List<OffsetRange> ranges = folds.get(kind.code());
        if (ranges == null) {
            ranges = new ArrayList<>();
            folds.put(kind.code(), ranges);
        }
        return ranges;
    }

    private List<Scope>  getEmbededScopes(Scope scope, Set<Scope> collectedScopes) {
        if (collectedScopes == null) {
            collectedScopes = new HashSet<>();
        }
        List<? extends ModelElement> elements = scope.getElements();
        for (ModelElement element : elements) {
            if (element instanceof Scope) {
                if (collectedScopes.add((Scope) element)) {
                    // #258713 - scopes can be duplicated, typically anonymous classes;
                    // (we must scan all method bodies for them so they appear twice here)
                    getEmbededScopes((Scope) element, collectedScopes);
                }
            }
        }
        return new ArrayList<>(collectedScopes);
    }

    private class FoldingVisitor extends DefaultVisitor {

        private final Map<String, List<OffsetRange>> folds;

        public FoldingVisitor(final Map<String, List<OffsetRange>> folds) {
            this.folds = folds;
        }

        @Override
        public void visit(IfStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            super.visit(node);
            if (node.getTrueStatement() != null) {
                addFold(node.getTrueStatement());
            }
            if (node.getFalseStatement() != null && !(node.getFalseStatement() instanceof IfStatement)) {
                addFold(node.getFalseStatement());
            }
        }

        @Override
        public void visit(UseTraitStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            super.visit(node);
            if (node.getBody() != null) {
                addFold(node.getBody());
            }
        }

        @Override
        public void visit(ForEachStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            super.visit(node);
            if (node.getStatement() != null) {
                addFold(node.getStatement());
            }
        }

        @Override
        public void visit(ForStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            super.visit(node);
            if (node.getBody() != null) {
                addFold(node.getBody());
            }
        }

        @Override
        public void visit(WhileStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            super.visit(node);
            if (node.getBody() != null) {
                addFold(node.getBody());
            }
        }

        @Override
        public void visit(DoStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            super.visit(node);
            if (node.getBody() != null) {
                addFold(node.getBody());
            }
        }

        @Override
        public void visit(SwitchStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            super.visit(node);
            if (node.getBody() != null) {
                addFold(node.getBody());
            }
        }

        @Override
        public void visit(SwitchCase node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            super.visit(node);
            List<Statement> actions = node.getActions();
            if (!actions.isEmpty()) {
                OffsetRange offsetRange = null;
                if (node.isDefault()) {
                    offsetRange = new OffsetRange(node.getStartOffset() + "default:".length(), actions.get(actions.size() - 1).getEndOffset()); //NOI18N
                } else {
                    Expression value = node.getValue();
                    if (value != null) {
                        offsetRange = new OffsetRange(value.getEndOffset() + ":".length(), actions.get(actions.size() - 1).getEndOffset()); //NOI18N
                    }
                }
                addFold(offsetRange);
            }
        }

        @Override
        public void visit(MatchExpression node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            // NETBEANS-4443 PHP 8.0
            super.visit(node);
            addFold(node.getBlockRange());
        }

        @Override
        public void visit(TryStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            super.visit(node);
            if (node.getBody() != null) {
                addFold(node.getBody());
            }
        }

        @Override
        public void visit(CatchClause node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            super.visit(node);
            if (node.getBody() != null) {
                addFold(node.getBody());
            }
        }

        @Override
        public void visit(FinallyClause node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            super.visit(node);
            if (node.getBody() != null) {
                addFold(node.getBody());
            }
        }

        @Override
        public void visit(ArrayCreation node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            super.visit(node);
            ArrayCreation.Type type = node.getType();
            if (type == ArrayCreation.Type.NEW) {
                addFold(node, TYPE_ARRAY);
            } else {
                addFold(new OffsetRange(node.getStartOffset() + "array".length(), node.getEndOffset()), TYPE_ARRAY); // NOI18N
            }
        }

        @Override
        public void visit(Attribute node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node != null) {
                addFold(node, TYPE_ATTRIBUTES);
            }
            super.visit(node);
        }

        private void addFold(final ASTNode node) {
            if (!(node instanceof ASTError) && !(node instanceof EmptyStatement)) {
                addFold(createOffsetRange(node));
            }
        }

        private void addFold(final ASTNode node, FoldType type) {
            if (!(node instanceof ASTError) && !(node instanceof EmptyStatement)) {
                addFold(createOffsetRange(node), type);
            }
        }

        private void addFold(final OffsetRange offsetRange) {
            if (offsetRange != null && offsetRange.getLength() > 1) {
                getRanges(folds, TYPE_CODE_BLOCKS).add(offsetRange);
            }
        }

        private void addFold(final OffsetRange offsetRange, FoldType type) {
            if (offsetRange != null && offsetRange.getLength() > 1) {
                getRanges(folds, type).add(offsetRange);
            }
        }

    }

}
